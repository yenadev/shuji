package com.jims8161.shujichinese;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

/**
 * Created by Administrator on 2016-02-19.
 */
public class TableGroupData {
    private static final String TAG = MainActivity.TAG;
    public static final String TABLE_NAME = "group_table";

    public interface Columns {
        String _ID = "_id";
        String GROUP_NAME = "group_name";
        String DESCRIPTION = "group_description";
        String DISPLAYING = "displaying";
        String DISPLAYING_LIST = "displaying_in_list";
    }

    private static final String SQL_CREATE =  "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + " ("
            + Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Columns.GROUP_NAME + " TEXT NOT NULL UNIQUE,"
            + Columns.DESCRIPTION + " TEXT, "
            + Columns.DISPLAYING + " INTEGER NOT NULL DEFAULT 1, "
            + Columns.DISPLAYING_LIST + " INTEGER NOT NULL DEFAULT 1) ";

    public static void create(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
        Log.d(TAG, TABLE_NAME + " table is created.");

        ContentValues values = new ContentValues();
        values.put(TableGroupData.Columns.GROUP_NAME, GroupHelper.DEFAULT_GROUP);
        try {
            if (-1 != db.insert(TableGroupData.TABLE_NAME, null, values)) {
                //Toast.makeText(getApplicationContext(), "저장되었습니다", Toast.LENGTH_SHORT).show();
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void delete(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
        Log.d(TAG, TABLE_NAME + " table is deleted.");
    }
}
