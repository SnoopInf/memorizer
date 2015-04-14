/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dataart.memorizer.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.dataart.memorizer.data.UnitContract.VocabularyEntry;
import static com.dataart.memorizer.data.UnitContract.UnitEntry;
import static com.dataart.memorizer.data.UnitContract.TaskEntry;


/**
 * Manages a local database for units data.
 */
public class UnitDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "unit.db";

    public UnitDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_UNIT_TABLE = "CREATE TABLE " + UnitEntry.TABLE_NAME + " (" +
                UnitEntry._ID + " INTEGER PRIMARY KEY, " +
                UnitEntry.COLUMN_UNIT_NAME + " TEXT NOT NULL, " +
                UnitEntry.COLUMN_UNIT_NUMBER + " INTEGER NOT NULL," +
                UnitEntry.COLUMN_UNIT_ENABLED + " INTEGER NOT NULL," +
                UnitEntry.COLUMN_UNIT_TOTAL + " INTEGER NOT NULL," +
                UnitEntry.COLUMN_UNIT_SUCCESSFUL + " INTEGER NOT NULL," +

                " UNIQUE (" + UnitEntry.COLUMN_UNIT_NUMBER + ") ON CONFLICT REPLACE);";


        sqLiteDatabase.execSQL(SQL_CREATE_UNIT_TABLE);

        final String SQL_CREATE_VOCABULARY_TABLE = "CREATE TABLE " + VocabularyEntry.TABLE_NAME + " (" +
                VocabularyEntry._ID + " INTEGER PRIMARY KEY," +

                // the ID of the unit  associated with this vocabulary entry
                VocabularyEntry.COLUMN_UNIT_KEY + " INTEGER NOT NULL, " +

                VocabularyEntry.COLUMN_WORD + " TEXT NOT NULL, " +
                VocabularyEntry.COLUMN_DEFINITION + " TEXT NOT NULL, " +
                VocabularyEntry.COLUMN_TRANSLATION + " TEXT NOT NULL, " +

                VocabularyEntry.COLUMN_ENTRY_NUMBER + " INTEGER NOT NULL," +

                " UNIQUE (" + VocabularyEntry.COLUMN_UNIT_KEY + ", " +
                VocabularyEntry.COLUMN_ENTRY_NUMBER + ") ON CONFLICT REPLACE," +
               // Set up the unit column as a foreign key to unit table.
                " FOREIGN KEY (" + VocabularyEntry.COLUMN_UNIT_KEY + ") REFERENCES " +
                UnitEntry.TABLE_NAME + " (" + UnitEntry._ID + "));";




        sqLiteDatabase.execSQL(SQL_CREATE_VOCABULARY_TABLE);

        final String SQL_CREATE_TASK_TABLE = "CREATE TABLE " + TaskEntry.TABLE_NAME + " (" +
                TaskEntry._ID + " INTEGER PRIMARY KEY," +

                // the ID of the unit  associated with this task entry
                TaskEntry.COLUMN_UNIT_KEY + " INTEGER NOT NULL, " +

                // the ID of the location entry associated with this weather data
                TaskEntry.COLUMN_TASK_DESCRIPTION + " TEXT NOT NULL, " +
                TaskEntry.COLUMN_TASK_QUERY + " TEXT NOT NULL, " +
                TaskEntry.COLUMN_TASK_CORRECT + " TEXT NOT NULL, " +
                TaskEntry.COLUMN_TASK_OPTION1 + " TEXT, " +
                TaskEntry.COLUMN_TASK_OPTION2 + " TEXT, " +
                TaskEntry.COLUMN_TASK_OPTION3 + " TEXT, " +
                TaskEntry.COLUMN_TASK_OPTION4 + " TEXT, " +

                TaskEntry.COLUMN_TASK_TYPE + " INTEGER NOT NULL, " +

                TaskEntry.COLUMN_TASK_NUMBER + " INTEGER NOT NULL, " +

                " UNIQUE (" + TaskEntry.COLUMN_UNIT_KEY + ", " +
                TaskEntry.COLUMN_TASK_NUMBER + ") ON CONFLICT REPLACE," +

                // Set up the unit column as a foreign key to unit table.
                " FOREIGN KEY (" + TaskEntry.COLUMN_UNIT_KEY + ") REFERENCES " +
                UnitEntry.TABLE_NAME + " (" + UnitEntry._ID + "));";

        sqLiteDatabase.execSQL(SQL_CREATE_TASK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TaskEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + VocabularyEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + UnitEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
