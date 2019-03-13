package com.jims8161.shujichinese;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Administrator on 2016-02-19.
 */
public class TableEtcData {
    private static final String TAG = MainActivity.TAG;
    public static final String TABLE_NAME = "etc_data_table";

    public interface Columns {
        String _ID = "_id";
        String ALARM_EXAM_TIME = "exam_time";
        String ALARM_EXAM_TYPE = "exam_type";
        String ALARM_EXAM_TIME_ACTIVE = "exam_time_active";
    }

    private static final String SQL_CREATE =  "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + " ("
            + Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Columns.ALARM_EXAM_TIME + " INTEGER, "
            + Columns.ALARM_EXAM_TYPE + " INTEGER, "
            + Columns.ALARM_EXAM_TIME_ACTIVE + " INTEGER) ";

    public static void create(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
        Log.d(TAG, TABLE_NAME + " table is created.");
    }

    public static void delete(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
        Log.d(TAG, TABLE_NAME + " table is deleted.");
    }
}
