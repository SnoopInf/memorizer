package com.dataart.memorizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

/**
 * Created by kirilldavidenko on 03.04.15.
 */
public class SuccessDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        LayoutInflater inflater = getActivity().getLayoutInflater();


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(inflater.inflate(R.layout.dialog_layout_success, null));

        String[] correct = getResources().getStringArray(R.array.correct);
        builder.setMessage(correct[(int)(Math.random()*correct.length)]);

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
