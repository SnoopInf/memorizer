package com.dataart.memorizer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.dataart.memorizer.data.UnitContract;

/**
 * Created by kirilldavidenko on 25.03.15.
 */
public class SelectionActivity extends ActionBarActivity implements  View.OnClickListener {
    private Button mTaskButton;
    private Button mVocabularyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        mTaskButton = (Button) findViewById(R.id.button_tasks);
        mTaskButton.setOnClickListener(this);

        mVocabularyButton = (Button) findViewById(R.id.button_vocabulary);
        mVocabularyButton.setOnClickListener(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select, menu);
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        long unitId = getIntent().getLongExtra("unit", 1);
        if(v.equals(mTaskButton)){
            Uri uri = UnitContract.TaskEntry.buildTaskUriByUnit(unitId);
            Intent intent = new Intent(this, TaskActivity.class)
                    .setData(uri);
            this.startActivity(intent);
        } else if(v.equals(mVocabularyButton)) {
            Uri uri = UnitContract.VocabularyEntry.buildVocabularyUriByUnit(unitId);
            Intent intent = new Intent(this, VocabularyActivity.class)
                    .setData(uri);
            this.startActivity(intent);
        }
    }
}
