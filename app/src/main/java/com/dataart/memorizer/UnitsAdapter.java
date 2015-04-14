package com.dataart.memorizer;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dataart.memorizer.data.UnitContract;

public class UnitsAdapter extends CursorRecyclerAdapter<UnitsAdapter.ViewHolder> {

    public static final String LOG_TAG = UnitsAdapter.class.getSimpleName();
    protected int _disabledAlpha = 50;

    private int disabledColor;
    private int mainColor;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView mCardView;
        public TextView mTextName;
        public TextView mTextInfo;
        public long mUnitId;

        public ViewHolder(View v, final CardClickHandler clickHandler) {
            super(v);
            mCardView = (CardView) v;
            mTextName = (TextView) mCardView.findViewById(R.id.unit_name);
            mTextInfo = (TextView) mCardView.findViewById(R.id.unit_info);
            mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickHandler.onClick(mUnitId);
                }
            });
        }
    }

    public UnitsAdapter(Cursor cursor) {
        super(cursor);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UnitsAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent,
                                                   int viewType) {

        disabledColor = parent.getContext().getResources().getColor(R.color.unit_disabled);
        mainColor = parent.getContext().getResources().getColor(R.color.unit_main);
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.units_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v, new CardClickHandler() {
            @Override
            public void onClick(long unitId) {
                Intent intent = new Intent(parent.getContext(), SelectionActivity.class)
                        .putExtra("unit", unitId);
                parent.getContext().startActivity(intent);
            }
        });
        return vh;
    }

    @TargetApi(21)
    @Override
    public void onBindViewHolderCursor(ViewHolder holder, Cursor cursor) {
        holder.mTextName.setText(cursor.getString(UnitsFragment.COL_UNIT_NAME));
        int total = cursor.getInt(UnitsFragment.COL_UNIT_TOTAL);
        int success = cursor.getInt(UnitsFragment.COL_UNIT_SUCCESSFUL);
        boolean enabled = cursor.getInt(UnitsFragment.COL_UNIT_ENABLED) == 1;
        if( enabled && total != 0) {
            holder.mTextInfo.setText(success + "/" + total);
        } else {
            holder.mTextInfo.setVisibility(View.INVISIBLE);
        }

        holder.mCardView.setEnabled(enabled);

        if(!enabled) {
            holder.mCardView.setCardBackgroundColor(disabledColor);
            holder.mTextName.setTextColor(holder.mTextName.getTextColors().withAlpha(_disabledAlpha));
        }  else {
            int api = android.os.Build.VERSION.SDK_INT;
            if (api >= Build.VERSION_CODES.LOLLIPOP){
                holder.mCardView.setElevation(10);
            }
            holder.mCardView.setCardBackgroundColor(mainColor);
        }

        holder.mUnitId = cursor.getLong(UnitsFragment.COL_UNIT_ID);
    }

    public static interface CardClickHandler {
        public void onClick(long unitId);
    }


}