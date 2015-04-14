package com.dataart.memorizer;

/**
 * Created by kirilldavidenko on 29.03.15.
 */

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TaskFragmentMatchDefinition extends TaskFragmentMatch {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_match_definition, container, false);

        init(rootView);

        return rootView;
    }
}