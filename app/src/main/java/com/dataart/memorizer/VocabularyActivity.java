package com.dataart.memorizer;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.TextView;

import com.dataart.memorizer.data.UnitContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class VocabularyActivity extends ActionBarActivity implements  LoaderManager.LoaderCallbacks<Cursor>,View.OnClickListener {
    private static final String LOG_TAG = VocabularyActivity.class.getSimpleName();

    private Uri mUri;
    private Cursor mCursor;
    private int mCount;
    private int idx;
    private List<Integer> ids;

    private static final int VOCABULARY_LOADER_ID = 43;

    private static final String[] VOCABULARY_COLUMNS = {
            UnitContract.VocabularyEntry.TABLE_NAME + "." + UnitContract.VocabularyEntry._ID,
            UnitContract.VocabularyEntry.COLUMN_WORD,
            UnitContract.VocabularyEntry.COLUMN_DEFINITION,
            UnitContract.VocabularyEntry.COLUMN_TRANSLATION
    };

    public static final int COL_VOCABULARY_ID = 0;
    public static final int COL_VOCABULARY_WORD = 1;
    public static final int COL_VOCABULARY_DEFINITION = 2;
    public static final int COL_VOCABULARY_TRANSLATION = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary);
        mUri = getIntent().getData();
        getSupportLoaderManager().initLoader(VOCABULARY_LOADER_ID, null, this);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new EmptyFragmentWithProgress())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_vocabulary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /* if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        changeFragment();
    }

    private void changeFragment() {
        Bundle args = new Bundle();

        if(idx == mCount) {
            idx = 0;
        }

        int position = ids.get(idx++);
        mCursor.moveToPosition(position);

        Log.i(LOG_TAG, "Another word " + position + " of " + mCount);

        args.putString("word", mCursor.getString(COL_VOCABULARY_WORD));
        args.putString("definition", mCursor.getString(COL_VOCABULARY_DEFINITION));
        args.putString("translation", mCursor.getString(COL_VOCABULARY_TRANSLATION));

        PlaceholderFragment vocabularyFragment = new PlaceholderFragment();
        vocabularyFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, vocabularyFragment)
                .commit();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader " + mUri);
        if (mUri == null) {
            return null;
        }
        return new CursorLoader(this, mUri, VOCABULARY_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            mCursor = data;
            mCount = data.getCount();

            ids = new ArrayList<>(mCount);
            for(int i = 0; i < mCount; i++) {
                ids.add(i);
            }
            Collections.shuffle(ids);

            idx = 0;

            final int WHAT = 1;
            Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    if(msg.what == WHAT) changeFragment();
                }
            };
            handler.sendEmptyMessage(WHAT);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        TextView mWordText;
        TextView mDefinitionText;
        TextView mTranslationText;

        Integer mIndex;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_vocabulary, container, false);
            Button next = (Button) rootView.findViewById(R.id.vocabulary_button_next);
            next.setOnClickListener((View.OnClickListener)getActivity());

            mWordText = (TextView) rootView.findViewById(R.id.vocabulary_word_text);
            mDefinitionText  = (TextView) rootView.findViewById(R.id.vocabulary_definition_text);
            mTranslationText = (TextView) rootView.findViewById(R.id.vocabulary_translation_text);

            Bundle args = getArguments();
            if(args != null) {
                mIndex = args.getInt("index");
                mWordText.setText(args.getString("word"));
                mTranslationText.setText(args.getString("translation"));
                mDefinitionText.setText(args.getString("definition"));
            }

            return rootView;
        }


    }
}
