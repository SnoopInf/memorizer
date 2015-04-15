package com.dataart.memorizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.dataart.memorizer.data.UnitContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TaskActivity extends ActionBarActivity implements  LoaderManager.LoaderCallbacks<Cursor>, TaskFragment.Callback {
    public static final String INSTRUCTIONS = "instructions";
    public static final String QUERY = "query";
    public static final String OPTION1 = "option1";
    public static final String OPTION2 = "option2";
    public static final String OPTION3 = "option3";
    public static final String OPTION4 = "option4";
    public static final String CORRECT = "correct";



    private static final String LOG_TAG = TaskActivity.class.getSimpleName();
    private Uri mUri;
    private Cursor mCursor;
    private int mCount;


    private static final int TOTAL_TASK_ROUND = 20;

    private TextView mTaskInfo;

    private int idx;
    private int successCount;
    private int total = TOTAL_TASK_ROUND;
    private List<Integer> taskIds;

    private static final int TASK_LOADER_ID = 44;

    private static final String[] TASK_COLUMNS = {
            UnitContract.TaskEntry.TABLE_NAME + "." + UnitContract.TaskEntry._ID,
            UnitContract.TaskEntry.COLUMN_TASK_QUERY,
            UnitContract.TaskEntry.COLUMN_TASK_DESCRIPTION,
            UnitContract.TaskEntry.COLUMN_TASK_CORRECT,

            UnitContract.TaskEntry.COLUMN_TASK_TYPE,

            UnitContract.TaskEntry.COLUMN_TASK_OPTION1,
            UnitContract.TaskEntry.COLUMN_TASK_OPTION2,
            UnitContract.TaskEntry.COLUMN_TASK_OPTION3,
            UnitContract.TaskEntry.COLUMN_TASK_OPTION4
    };


    public static final int COL_TASK_ID = 0;
    public static final int COL_TASK_QUERY = 1;
    public static final int COL_TASK_DESCRIPTION = 2;
    public static final int COL_TASK_CORRECT = 3;
    public static final int COL_TASK_TYPE = 4;
    public static final int COL_TASK_OPTION1 = 5;
    public static final int COL_TASK_OPTION2 = 6;
    public static final int COL_TASK_OPTION3 = 7;
    public static final int COL_TASK_OPTION4 = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        mTaskInfo = (TextView) findViewById(R.id.task_info);
        mUri = getIntent().getData();
        getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, this);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new EmptyFragmentWithProgress())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
       /* if (id == R.id.action_settings) {
            return true;
        }
*/
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onContinue(boolean success) {
        if(success) {
            ++successCount;
        }
        if(idx >= total) {
            if(successCount >= ((int)total * UnitsActivity.NEXT_UNIT_BARRIER)) {
                showSuccessDialog();
            } else {
                showFailedDialog();
            }

        } else {
            changeFragment();
        }
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.congratulations)
                .setMessage(String.format(getString(R.string.format_success_msg), successCount))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(TaskActivity.this, UnitsActivity.class).setData(mUri);
                        intent.putExtra("successful", successCount);
                        intent.putExtra("total", total);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.retry, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        successCount = 0;
                        idx = 0;
                        generateIds();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showFailedDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.better)
                .setMessage(String.format(getString(R.string.format_fail_msg), successCount, (int)(total * UnitsActivity.NEXT_UNIT_BARRIER)))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(TaskActivity.this, UnitsActivity.class).setData(mUri);
                        intent.putExtra("successful", successCount);
                        intent.putExtra("total", total);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.retry, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        successCount = 0;
                        idx = 0;
                        generateIds();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader " + mUri);
        if (mUri == null) {
            return null;
        }
        return new CursorLoader(this, mUri, TASK_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            mCursor = data;
            mCount = data.getCount();

            total = Math.min(TOTAL_TASK_ROUND, mCount);
            generateIds();
        }
    }

    private void generateIds() {
        List<Integer> ids = new ArrayList<>(mCount);
        for(int i = 0; i < mCount; i++) {
            ids.add(i);
        }
        Collections.shuffle(ids);

        idx = 0;
        taskIds = ids.subList(0, total);

        final int WHAT = 1;
        Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == WHAT) changeFragment();
            }
        };
        handler.sendEmptyMessage(WHAT);
    }

    private void changeFragment() {
        Bundle args = new Bundle();


        int position = taskIds.get(idx++);

        mTaskInfo.setText(idx + "/" + total);

        mCursor.moveToPosition(position);
        int type = mCursor.getInt(COL_TASK_TYPE);

        Log.i(LOG_TAG, "Another word " + position + " of " + mCount);

        args.putString(INSTRUCTIONS, mCursor.getString(COL_TASK_DESCRIPTION));
        args.putString(QUERY, mCursor.getString(COL_TASK_QUERY));
        args.putString(CORRECT, mCursor.getString(COL_TASK_CORRECT));

        args.putString(OPTION1, mCursor.getString(COL_TASK_OPTION1));
        args.putString(OPTION2, mCursor.getString(COL_TASK_OPTION2));
        args.putString(OPTION3, mCursor.getString(COL_TASK_OPTION3));
        args.putString(OPTION4, mCursor.getString(COL_TASK_OPTION4));

        TaskFragment taskFragment;

        switch(type) {
            case 1: taskFragment = new TaskFragmentMatch(); break;
            case 2: taskFragment = new TaskFragmentTypeIn(); break;
            case 3: taskFragment = new TaskFragmentMatchDefinition(); break;
            default: taskFragment = new TaskFragmentTypeIn();
        }

        taskFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, taskFragment)
                .commit();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
