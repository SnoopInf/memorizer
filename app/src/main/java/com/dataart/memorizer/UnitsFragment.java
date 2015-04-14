package com.dataart.memorizer;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dataart.memorizer.data.UnitContract;

/**
 * Created by kirilldavidenko on 28.03.15.
 */
public class UnitsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = UnitsFragment.class.getSimpleName();

    private static final int UNITS_LOADER_ID = 42;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    UnitsAdapter mAdapter;


    private static final String[] UNIT_COLUMNS = {

            UnitContract.UnitEntry.TABLE_NAME + "." + UnitContract.UnitEntry._ID,
            UnitContract.UnitEntry.COLUMN_UNIT_NAME,
            UnitContract.UnitEntry.COLUMN_UNIT_NUMBER,
            UnitContract.UnitEntry.COLUMN_UNIT_SUCCESSFUL,
            UnitContract.UnitEntry.COLUMN_UNIT_TOTAL,
            UnitContract.UnitEntry.COLUMN_UNIT_ENABLED
    };

    static final int COL_UNIT_ID = 0;
    static final int COL_UNIT_NAME = 1;
    static final int COL_UNIT_NUMBER = 2;
    static final int COL_UNIT_SUCCESSFUL = 3;
    static final int COL_UNIT_TOTAL= 4;
    static final int COL_UNIT_ENABLED = 5;

    public UnitsFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(UNITS_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_units, container, false);


        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.units_recycler_view);


        // use a linear layout manager
        mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);


        mAdapter = new UnitsAdapter(null);
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(LOG_TAG, "Created loader for UnitsFragment");
        // Sort order:  Ascending, by date.
        String sortOrder = UnitContract.UnitEntry.COLUMN_UNIT_NUMBER + " ASC";
        return new CursorLoader(getActivity(), UnitContract.UnitEntry.CONTENT_URI, UNIT_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOG_TAG, "Loading data for UnitsFragment. Found units - " + data.getCount());

        mAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
