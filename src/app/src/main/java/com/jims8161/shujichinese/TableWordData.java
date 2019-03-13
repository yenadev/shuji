package com.jims8161.shujichinese;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Administrator on 2016-02-19.
 */
public class TableWordData {
    private static final String TAG = MainActivity.TAG;
    public static final String TABLE_NAME = "worddata";

    public interface Columns {
        String _ID = "_id";
        String HANZI = "hanzi";
        String MEANING = "meaning";
        String PINYIN = "pinyin";
        String DIVISION = "division";       // DEPRECATED
        String EXCLUDE = "exclude";
        String REAL_PINYIN = "realpinyin";
        String WORDGROUP = "wordgroup";
    }

    private static final String SQL_CREATE =  "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + " ("
            + Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Columns.HANZI + " TEXT, "
            + Columns.MEANING + " TEXT, "
            + Columns.PINYIN + " TEXT, "
            + Columns.REAL_PINYIN + " TEXT, "
            + Columns.DIVISION + " INTEGER, "
            + Columns.EXCLUDE + " INTEGER, "
            + Columns.WORDGROUP + " TEXT )";

    public static void create(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
        Log.d(TAG, TABLE_NAME + " table is created.");
    }

    public static void delete(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
        Log.d(TAG, TABLE_NAME + " table is deleted.");
    }
}
