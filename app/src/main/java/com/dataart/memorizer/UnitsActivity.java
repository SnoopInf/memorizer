package com.dataart.memorizer;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import com.dataart.memorizer.data.UnitContract;
import com.dataart.memorizer.sync.MemorizerSyncAdapter;


public class UnitsActivity extends ActionBarActivity {

    public static final double NEXT_UNIT_BARRIER = 0.75;
    public static final String LOG_TAG = UnitsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "Activity started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_units);

        Intent intent = getIntent();
        if(intent  != null && intent.getData() != null) {

            Uri uri = intent.getData();
            long unitId = Long.parseLong(UnitContract.TaskEntry.getUnitFromUri(uri));
            int total = intent.getIntExtra("total", 0);
            int successful = intent.getIntExtra("successful", 0);

            if(total != 0) {
                ContentValues values = new ContentValues();
                values.put(UnitContract.UnitEntry.COLUMN_UNIT_SUCCESSFUL, successful);
                values.put(UnitContract.UnitEntry.COLUMN_UNIT_TOTAL, total);

                getContentResolver().update(UnitContract.UnitEntry.buildUnitUri(unitId),
                        values,
                        UnitContract.UnitEntry._ID + " = ?",
                        new String[]{String.valueOf(unitId)});

                if(successful >= total * NEXT_UNIT_BARRIER) {
                    ContentValues nextValues = new ContentValues();
                    nextValues.put(UnitContract.UnitEntry.COLUMN_UNIT_ENABLED, true);

                    getContentResolver().update(UnitContract.UnitEntry.buildUnitUri(unitId + 1), //FIXME next unit number, not ID
                            nextValues,
                            UnitContract.UnitEntry._ID + " = ?",
                            new String[]{String.valueOf(unitId + 1)});
                }

            }
        }

        MemorizerSyncAdapter.initializeSyncAdapter(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_units, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }


}
