package com.dataart.memorizer;

/**
 * Created by kirilldavidenko on 29.03.15.
 */

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class TaskFragmentMatch extends TaskFragment implements View.OnClickListener {

    protected TextView mTextView;

    protected Button mOption1;
    protected Button mOption2;
    protected Button mOption3;
    protected Button mOption4;
    protected Drawable defaultBackground;

    public TaskFragmentMatch() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_match, container, false);

        init(rootView);

        return rootView;
    }

    @Override
    protected void init(View rootView) {
        super.init(rootView);

        mTextView = (TextView) rootView.findViewById(R.id.task_text);

        mOption1 = (Button) rootView.findViewById(R.id.task_button_option1);
        mOption2 = (Button) rootView.findViewById(R.id.task_button_option2);
        mOption3 = (Button) rootView.findViewById(R.id.task_button_option3);
        mOption4 = (Button) rootView.findViewById(R.id.task_button_option4);

        mOption1.setOnClickListener(this);
        mOption2.setOnClickListener(this);
        mOption3.setOnClickListener(this);
        mOption4.setOnClickListener(this);


        Bundle args = getArguments();
        if(args != null) {

            mTextView.setText(args.getString(TaskActivity.QUERY));
            mTextViewInstructions.setText(args.getString(TaskActivity.INSTRUCTIONS));

            mOption1.setText(args.getString(TaskActivity.OPTION1));
            mOption2.setText(args.getString(TaskActivity.OPTION2));
            mOption3.setText(args.getString(TaskActivity.OPTION3));
            mOption4.setText(args.getString(TaskActivity.OPTION4));

            defaultBackground = mOption1.getBackground();

            correctAnswer = args.getString(TaskActivity.CORRECT);
        }
    }

    protected void onSubmit() {
        mSubmit.setVisibility(View.INVISIBLE);
        mContinue.setVisibility(View.VISIBLE);

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View layout;
        if(correctAnswer.equals(userAnswer)) {
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
    public void onClick(View v) {

        mOption1.setBackground(defaultBackground);
        mOption2.setBackground(defaultBackground);
        mOption3.setBackground(defaultBackground);
        mOption4.setBackground(defaultBackground);

        Button button = (Button) v;
        button.setBackgroundColor(getResources().getColor(R.color.task_selected));

        userAnswer = button.getText().toString();

    }
}