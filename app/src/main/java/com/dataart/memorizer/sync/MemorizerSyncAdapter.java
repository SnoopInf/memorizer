package com.dataart.memorizer.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.Log;

import com.dataart.memorizer.R;
import com.dataart.memorizer.UnitsActivity;
import com.dataart.memorizer.Utility;
import com.dataart.memorizer.data.UnitContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;


public class MemorizerSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String LOG_TAG = MemorizerSyncAdapter.class.getSimpleName();

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int MEMORIZER_NOTIFICATION_ID = 5042;

    private static final String[] NOTIFY_UNIT_PROJECTION = new String[] {
            UnitContract.UnitEntry.COLUMN_UNIT_NAME
    };

    private static final int INDEX_UNIT_NAME = 0;


    public MemorizerSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called.");

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String unitsJsonStr = null;


        int timestamp = Utility.getLastTimestamp(getContext());

        try {
            // Construct the URL for the query

            final String UNITS_BASE_URL =
                    "http://memorizer-snoopns.rhcloud.com/api/updates?";
            final String TIMESTAMP_PARAM = "timestamp";


            Uri builtUri = Uri.parse(UNITS_BASE_URL).buildUpon()
                    .appendQueryParameter(TIMESTAMP_PARAM, String.valueOf(timestamp))
                    .build();
            Log.i(LOG_TAG, "URI: " + builtUri.toString());

            URL url = new URL(builtUri.toString());

            // Create the request to the api, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // for debugging.
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            unitsJsonStr = buffer.toString();
            getUnitsDataFromJson(unitsJsonStr, timestamp);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        } catch(JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    /**
     * Take the String representing the complete Units data in JSON Format and
     * pull out the data we need to construct the Strings needed for the UI.
     */
    private void getUnitsDataFromJson(String jsonStr, int timestamp)
            throws JSONException {


        //Brand new info timestamp
        final String TIMESTAMP = "timestamp";
        // Unit information is in "units"
        final String UNIT = "units";
        final String UNIT_NUMBER = "number";
        final String UNIT_NAME = "name";

        // Task information.  Each task info is an element of the "tasks" array.
        final String TASK = "tasks";
        final String TASK_QUERY = "query";
        final String TASK_UNIT_NUMBER = "unit";
        final String TASK_INDEX = "index";
        final String TASK_DESCRIPTION = "description";
        final String TASK_ANSWER = "answer";
        final String TASK_OPTIONS = "options";
        final String TASK_TYPE = "type";
        final String TASK_DELETED = "deleted";

        // Vocabulary information.  Each task info is an element of the "vocabularies" array.
        final String VOCABULARY = "vocabularies";
        final String VOCABULARY_WORD = "word";
        final String VOCABULARY_UNIT_NUMBER = "unit";
        final String VOCABULARY_INDEX = "index";
        final String VOCABULARY_DEFINITION = "definition";
        final String VOCABULARY_TRANSLATION = "translation";
        final String VOCABULARY_DELETED = "deleted";

        List<ContentValues> vocabularyValues = new ArrayList<>();
        List<ContentValues> taskValues = new ArrayList<>();


        List<String> vocabularyDeleted = new ArrayList<>();
        List<String> taskDeleted = new ArrayList<>();

        Map<Integer, Long> unitsMap = new HashMap<>();

        try {

            JSONObject json = new JSONObject(jsonStr);
            Log.i(LOG_TAG, jsonStr);
            int lastTimeStamp = json.getInt(TIMESTAMP);

            JSONArray units = json.getJSONArray(UNIT);
            for(int i = 0; i < units.length(); i++) {
                JSONObject unitJson = units.getJSONObject(i);
                String unitName = unitJson.getString(UNIT_NAME);
                int unitNumber = unitJson.getInt(UNIT_NUMBER);
                long unitId = addUnit(unitName, unitNumber);

                unitsMap.put(unitNumber, unitId);
            }


            JSONArray vocabularies = json.getJSONArray(VOCABULARY);
            for(int i = 0; i < vocabularies.length(); i++) {
                JSONObject vocabulary = vocabularies.getJSONObject(i);

                String word = vocabulary.getString(VOCABULARY_WORD);
                String definition = vocabulary.getString(VOCABULARY_DEFINITION);
                String translation = vocabulary.getString(VOCABULARY_TRANSLATION);
                int index = vocabulary.getInt(VOCABULARY_INDEX);
                int unit = vocabulary.getInt(VOCABULARY_UNIT_NUMBER);

                boolean deleted = vocabulary.optBoolean(VOCABULARY_DELETED, false);

                if(deleted) {
                    vocabularyDeleted.add(""+index);
                } else {
                    ContentValues vocabularyValue = new ContentValues();

                    vocabularyValue.put(UnitContract.VocabularyEntry.COLUMN_UNIT_KEY, unitsMap.get(unit));
                    vocabularyValue.put(UnitContract.VocabularyEntry.COLUMN_WORD, word);
                    vocabularyValue.put(UnitContract.VocabularyEntry.COLUMN_DEFINITION, definition);
                    vocabularyValue.put(UnitContract.VocabularyEntry.COLUMN_TRANSLATION, translation);
                    //other fields
                    vocabularyValue.put(UnitContract.VocabularyEntry.COLUMN_ENTRY_NUMBER, index);
                    Log.i(LOG_TAG, "VOC " + vocabularyValue);
                    vocabularyValues.add(vocabularyValue);
                }
            }

            JSONArray tasks = json.getJSONArray(TASK);
            for(int i = 0; i < tasks.length(); i++) {
                 JSONObject task = tasks.getJSONObject(i);
                 int index = task.getInt(TASK_INDEX);
                int unit = task.getInt(TASK_UNIT_NUMBER);

                boolean deleted = task.optBoolean(TASK_DELETED, false);

                if(deleted) {
                    taskDeleted.add(""+index);
                } else {
                    ContentValues taskValue = new ContentValues();
                    taskValue.put(UnitContract.TaskEntry.COLUMN_UNIT_KEY, unitsMap.get(unit));
                    taskValue.put(UnitContract.TaskEntry.COLUMN_TASK_QUERY, task.getString(TASK_QUERY));
                    taskValue.put(UnitContract.TaskEntry.COLUMN_TASK_CORRECT, task.getString(TASK_ANSWER));
                    taskValue.put(UnitContract.TaskEntry.COLUMN_TASK_DESCRIPTION, task.getString(TASK_DESCRIPTION));
                    taskValue.put(UnitContract.TaskEntry.COLUMN_TASK_TYPE, task.getInt(TASK_TYPE));
                    taskValue.put(UnitContract.TaskEntry.COLUMN_TASK_NUMBER, index);

                    JSONArray options = task.optJSONArray(TASK_OPTIONS);
                    if (options != null) {
                        taskValue.put(UnitContract.TaskEntry.COLUMN_TASK_OPTION1, options.getString(0));
                        taskValue.put(UnitContract.TaskEntry.COLUMN_TASK_OPTION2, options.getString(1));
                        taskValue.put(UnitContract.TaskEntry.COLUMN_TASK_OPTION3, options.getString(2));
                        taskValue.put(UnitContract.TaskEntry.COLUMN_TASK_OPTION4, options.getString(3));
                    }

                    taskValues.add(taskValue);
                }
            }

            int inserted = 0;
            // add to database
            if ( vocabularyValues.size() > 0 ) {
                ContentValues[] values = new ContentValues[vocabularyValues.size()];
                vocabularyValues.toArray(values);
                inserted += getContext().getContentResolver().bulkInsert(UnitContract.VocabularyEntry.CONTENT_URI, values);

                if(!vocabularyDeleted.isEmpty()) {
                    String[] inClause = vocabularyDeleted.toArray(new String[vocabularyDeleted.size()]);
                    getContext().getContentResolver().delete(UnitContract.VocabularyEntry.CONTENT_URI, UnitContract.VocabularyEntry.COLUMN_ENTRY_NUMBER + " IN  ( " + makePlaceholders(inClause.length) + ")", inClause);
                }

            }

            if (taskValues.size() > 0 ) {
                ContentValues[] values = new ContentValues[taskValues.size()];
                taskValues.toArray(values);
                inserted += getContext().getContentResolver().bulkInsert(UnitContract.TaskEntry.CONTENT_URI, values);
                if(!taskDeleted.isEmpty()) {
                    String[] inClause = vocabularyDeleted.toArray(new String[taskDeleted.size()]);
                    getContext().getContentResolver().delete(UnitContract.TaskEntry.CONTENT_URI, UnitContract.TaskEntry.COLUMN_TASK_NUMBER + " IN  ( " + makePlaceholders(inClause.length) + ")", inClause);
                }

            }

            if(inserted > 0) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                prefs.edit().putInt(getContext().getString(R.string.pref_timestamp_key), lastTimeStamp).apply();
            }
            Log.d(LOG_TAG, "onPerfSync Complete. " + inserted + " Inserted");
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private String makePlaceholders(int len) {
        if (len < 1) {
            // It will lead to an invalid query anyway ..
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }

    private long addUnit(String unitName, int unitNumber) {
        Cursor unitCursor = getContext().getContentResolver().query(UnitContract.UnitEntry.CONTENT_URI,
                new String[]{UnitContract.UnitEntry._ID},
                UnitContract.UnitEntry.COLUMN_UNIT_NUMBER + " = ?",
                new String[]{Integer.toString(unitNumber)}, null);
        long unitId;
        if(unitCursor!= null && unitCursor.moveToFirst()) {
            int idx = unitCursor.getColumnIndex(UnitContract.UnitEntry._ID);
            unitId = unitCursor.getLong(idx);
        } else {
            ContentValues values = new ContentValues();
            values.put(UnitContract.UnitEntry.COLUMN_UNIT_NAME, unitName);
            values.put(UnitContract.UnitEntry.COLUMN_UNIT_NUMBER, unitNumber);
            values.put(UnitContract.UnitEntry.COLUMN_UNIT_ENABLED, unitNumber == 1);
            values.put(UnitContract.UnitEntry.COLUMN_UNIT_SUCCESSFUL, 0);
            values.put(UnitContract.UnitEntry.COLUMN_UNIT_TOTAL, 0);

            Log.v(LOG_TAG, "UNIT VALUES" + values.toString());
            Uri insertedUri = getContext().getContentResolver().insert(UnitContract.UnitEntry.CONTENT_URI, values);
            unitId = ContentUris.parseId(insertedUri);
        }
        if(unitCursor != null) {
            unitCursor.close();
        }
        return unitId;
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Log.i(LOG_TAG,  "Synchronizing");

        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));
        Log.i(LOG_TAG,  "Creating account. Password - " + accountManager.getPassword(newAccount));
        if ( null == accountManager.getPassword(newAccount) ) {
            Log.i(LOG_TAG,  "Creating account - OK");
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            Log.i(LOG_TAG,  "Creating account - OK - OK");
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        Log.i(LOG_TAG,  "Account created, setting sync interval");
        MemorizerSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        Log.i(LOG_TAG,  "Initializing sync adapter");
        getSyncAccount(context);
    }
}