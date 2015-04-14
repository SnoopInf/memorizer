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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Defines table and column names for the Memorizer database.
 */
public class UnitContract {
    public static final String CONTENT_AUTHORITY = "com.dataart.memorizer";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_VOCABULARY = "vocabulary";
    public static final String PATH_TASK = "task";
    public static final String PATH_UNIT = "unit";

    public static final class UnitEntry implements BaseColumns {
        public static final String TABLE_NAME = "unit";
        public static final String COLUMN_UNIT_NAME = "name";
        public static final String COLUMN_UNIT_NUMBER = "number";
        public static final String COLUMN_UNIT_ENABLED = "enabled";
        public static final String COLUMN_UNIT_TOTAL = "total";
        public static final String COLUMN_UNIT_SUCCESSFUL = "successful";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_UNIT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_UNIT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_UNIT;

        public static Uri buildUnitUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getUnitFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "task";

        public static final String COLUMN_UNIT_KEY = "unit_id";

        public static final String COLUMN_TASK_DESCRIPTION = "description";
        public static final String COLUMN_TASK_QUERY = "query";

        public static final String COLUMN_TASK_TYPE = "type";

        public static final String COLUMN_TASK_OPTION1 = "option1";
        public static final String COLUMN_TASK_OPTION2 = "option2";
        public static final String COLUMN_TASK_OPTION3 = "option3";
        public static final String COLUMN_TASK_OPTION4 = "option4";

        public static final String COLUMN_TASK_CORRECT = "correctAnswer";

        public static final String COLUMN_TASK_NUMBER = "number";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASK).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TASK;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TASK;

        public static Uri buildTaskUriByUnit(long unitId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(unitId)).build();
        }

        public static String getUnitFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class VocabularyEntry implements BaseColumns {

        public static final String TABLE_NAME = "vocabulary";

        public static final String COLUMN_UNIT_KEY = "unit_id";

        public static final String COLUMN_WORD = "word";
        public static final String COLUMN_DEFINITION = "definition";
        public static final String COLUMN_TRANSLATION = "translation";
        public static final String COLUMN_ENTRY_NUMBER = "number";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_VOCABULARY).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VOCABULARY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VOCABULARY;

        public static Uri buildVocabularyUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildVocabularyUriByUnit(long unitId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(unitId)).build();
        }

        public static String getUnitFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }
}
