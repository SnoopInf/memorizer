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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class UnitProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private UnitDbHelper mOpenHelper;

    static final int UNIT = 100;
    static final int UNIT_BY_NUMBER = 101;
    static final int VOCABULARY = 200;
    static final int VOCABULARY_BY_UNIT = 201;
    static final int TASK = 300;
    static final int TASK_BY_UNIT = 301;

    private static final SQLiteQueryBuilder sVocabularyByUnitQueryBuilder;

    static{
        sVocabularyByUnitQueryBuilder = new SQLiteQueryBuilder();
        sVocabularyByUnitQueryBuilder.setTables(
                UnitContract.VocabularyEntry.TABLE_NAME + " INNER JOIN " +
                        UnitContract.UnitEntry.TABLE_NAME +
                        " ON " + UnitContract.VocabularyEntry.TABLE_NAME +
                        "." + UnitContract.VocabularyEntry.COLUMN_UNIT_KEY +
                        " = " + UnitContract.UnitEntry.TABLE_NAME +
                        "." + UnitContract.UnitEntry.TABLE_NAME);
    }

    private static final SQLiteQueryBuilder sTasksByUnitQueryBuilder;
    static{
        sTasksByUnitQueryBuilder = new SQLiteQueryBuilder();
        sTasksByUnitQueryBuilder.setTables(
                UnitContract.TaskEntry.TABLE_NAME + " INNER JOIN " +
                        UnitContract.TaskEntry.TABLE_NAME +
                        " ON " + UnitContract.TaskEntry.TABLE_NAME +
                        "." + UnitContract.TaskEntry.COLUMN_UNIT_KEY +
                        " = " + UnitContract.UnitEntry.TABLE_NAME +
                        "." + UnitContract.UnitEntry._ID);
    }

    private static final String sUnitSelection =
            UnitContract.UnitEntry.TABLE_NAME+
                    "." + UnitContract.UnitEntry.COLUMN_UNIT_NUMBER + " = ? ";

    private static final String sUnitTaskSelection =
            UnitContract.TaskEntry.TABLE_NAME+
                    "." + UnitContract.TaskEntry.COLUMN_UNIT_KEY + " = ? ";


    private static final String sUnitVocabularySelection =
            UnitContract.VocabularyEntry.TABLE_NAME +
                    "." + UnitContract.VocabularyEntry.COLUMN_UNIT_KEY + " = ? ";


    static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(UnitContract.CONTENT_AUTHORITY, UnitContract.PATH_UNIT, UNIT);
        matcher.addURI(UnitContract.CONTENT_AUTHORITY, UnitContract.PATH_UNIT+"/#", UNIT_BY_NUMBER);

        matcher.addURI(UnitContract.CONTENT_AUTHORITY, UnitContract.PATH_VOCABULARY, VOCABULARY);
        matcher.addURI(UnitContract.CONTENT_AUTHORITY, UnitContract.PATH_TASK, TASK);
        matcher.addURI(UnitContract.CONTENT_AUTHORITY, UnitContract.PATH_VOCABULARY + "/#", VOCABULARY_BY_UNIT);
        matcher.addURI(UnitContract.CONTENT_AUTHORITY, UnitContract.PATH_TASK + "/#", TASK_BY_UNIT);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new UnitDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case UNIT:
                return UnitContract.UnitEntry.CONTENT_TYPE;
            case UNIT_BY_NUMBER:
                return UnitContract.UnitEntry.CONTENT_ITEM_TYPE;
            case VOCABULARY:
                return UnitContract.VocabularyEntry.CONTENT_TYPE;
            case TASK:
                return UnitContract.TaskEntry.CONTENT_TYPE;
            case VOCABULARY_BY_UNIT:
                return UnitContract.VocabularyEntry.CONTENT_TYPE;
            case TASK_BY_UNIT:
                return UnitContract.TaskEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "unit"
            case UNIT:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(UnitContract.UnitEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            // "unit/#"
            case UNIT_BY_NUMBER:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(UnitContract.UnitEntry.TABLE_NAME, projection, sUnitSelection, new String[]{UnitContract.UnitEntry.getUnitFromUri(uri)}, null, null, sortOrder);
                break;
            }
            // "vocabulary"
            case VOCABULARY:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(UnitContract.VocabularyEntry.TABLE_NAME, projection,selection,selectionArgs,null,null,sortOrder);
                break;
            }
            // "task"
            case TASK:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(UnitContract.TaskEntry.TABLE_NAME, projection,selection,selectionArgs,null,null,sortOrder);
                break;
            }
            // "vocabulary/#"
            case VOCABULARY_BY_UNIT: {
                retCursor = mOpenHelper.getReadableDatabase().query(UnitContract.VocabularyEntry.TABLE_NAME, projection, sUnitVocabularySelection, new String[]{UnitContract.VocabularyEntry.getUnitFromUri(uri)}, null, null, sortOrder);
                break;
            }
            // "task/#"
            case TASK_BY_UNIT: {
                retCursor = mOpenHelper.getReadableDatabase().query(UnitContract.TaskEntry.TABLE_NAME, projection, sUnitTaskSelection, new String[]{UnitContract.TaskEntry.getUnitFromUri(uri)}, null, null, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case UNIT: {
                long _id = db.insert(UnitContract.UnitEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = UnitContract.UnitEntry.buildUnitUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case VOCABULARY: {
                long _id = db.insert(UnitContract.VocabularyEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = UnitContract.VocabularyEntry.buildVocabularyUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TASK: {
                long _id = db.insert(UnitContract.TaskEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = UnitContract.TaskEntry.buildTaskUriByUnit(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;
        if(selection == null) {
            selection = "1";
        }
        switch (match) {
            case UNIT: {
                rowsDeleted = db.delete(UnitContract.UnitEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case VOCABULARY: {
                rowsDeleted = db.delete(UnitContract.VocabularyEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case TASK: {
                rowsDeleted = db.delete(UnitContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }


    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;
        switch (match) {
            case UNIT: {
                rowsUpdated = db.update(UnitContract.UnitEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case UNIT_BY_NUMBER: {
                rowsUpdated = db.update(UnitContract.UnitEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case VOCABULARY: {
                rowsUpdated = db.update(UnitContract.VocabularyEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case TASK: {
                rowsUpdated = db.update(UnitContract.TaskEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case UNIT:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(UnitContract.UnitEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            case VOCABULARY:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(UnitContract.VocabularyEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            case TASK:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(UnitContract.TaskEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            default:
                return super.bulkInsert(uri, values);
        }
        return returnCount;
    }
}
