package com.dataart.memorizer;

/**
 * Created by kirilldavidenko on 29.03.15.
 */

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TaskFragmentTypeIn extends TaskFragment {

    private TextView mTextView;
    private EditText typeIn;


    public TaskFragmentTypeIn() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_type_in, container, false);

        init(rootView);

        return rootView;
    }

    @Override
    protected void onSubmit() {
        mSubmit.setVisibility(View.INVISIBLE);
        mContinue.setVisibility(View.VISIBLE);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        userAnswer = typeIn.getText()==null?"":typeIn.getText().toString().trim();

        View layout;
        if(correctAnswer.equalsIgnoreCase(userAnswer)) {
            layout = inflater.inflate(R.layout.dialog_layout_success,
                    (ViewGroup) getActivity().findViewById(R.id.toast_layout_root));
            TextView text = (TextView) layout.findViewById(R.id.toast_text);
            String[] correctMsg = getResources().getStringArray(R.array.correct);
            text.setText(correctMsg[(int)(Math.random()*correctMsg.length)]);
            success = true;
        } else {
            layout = inflater.inflate(R.layout.dialog_layout_fail,
                    (ViewGroup) getActivity().findViewById(R.id.toast_layout_root));
            TextView text = (TextView) layout.findViewById(R.id.toast_text);
            String[] wrongMsgArr = getResources().getStringArray(R.array.wrong);
            String wrongMsg = wrongMsgArr[(int)(Math.random()*wrongMsgArr.length)] + " " + correctAnswer;
            text.setText(wrongMsg);
            success = false;
        }

        mToast.setView(layout);
        mToast.show();
    }

    @Override
    protected void init(View rootView) {
        super.init(rootView);

        Bundle args = getArguments();
        if(args != null) {
            mTextView = (TextView) rootView.findViewById(R.id.task_text);
            typeIn = (EditText) rootView.findViewById(R.id.task_type_in);
            mTextView.setText(args.getString(TaskActivity.QUERY));
            mTextViewInstructions.setText(args.getString(TaskActivity.INSTRUCTIONS));
            correctAnswer = args.getString(TaskActivity.CORRECT);

        }
    }
}