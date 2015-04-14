package com.dataart.memorizer;

/**
 * Created by kirilldavidenko on 29.03.15.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public abstract class TaskFragment extends Fragment {


    protected TextView mTextViewInstructions;

    protected Button mSubmit;
    protected Button mContinue;

    protected String correctAnswer;
    protected String userAnswer;

    protected Toast mToast;

    protected boolean success;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected void init(View rootView) {
        mSubmit = (Button) rootView.findViewById(R.id.task_button_submit);
        mContinue = (Button) rootView.findViewById(R.id.task_button_continue);
        mContinue.setVisibility(View.INVISIBLE);

        mTextViewInstructions = (TextView) rootView.findViewById(R.id.task_instructions);

        mContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mToast.cancel();
                ((Callback)getActivity()).onContinue(success);
            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmit();
            }
        });

        mToast = new Toast(getActivity());
        mToast.setGravity(Gravity.FILL_HORIZONTAL, 0, 0);
        mToast.setDuration(Toast.LENGTH_LONG);

    }

    protected void onSubmit() {
        mSubmit.setVisibility(View.INVISIBLE);
        mContinue.setVisibility(View.VISIBLE);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_layout_success,
                (ViewGroup) getActivity().findViewById(R.id.toast_layout_root));

        TextView text = (TextView) layout.findViewById(R.id.toast_text);

        String[] correct = getResources().getStringArray(R.array.correct);
        text.setText(correct[(int) (Math.random() * correct.length)]);

        mToast.setView(layout);
        mToast.show();
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onContinue(boolean success);
    }
}