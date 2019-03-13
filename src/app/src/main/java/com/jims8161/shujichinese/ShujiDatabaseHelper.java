package com.jims8161.shujichinese;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class ShujiDatabaseHelper extends SQLiteOpenHelper {
	public static final String DB_NAME = "shuji.db";
	private static final String TAG = MainActivity.TAG;
	public static final int DB_VERSION = 3;
	
	public ShujiDatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		Log.d(TAG, "onCreate() is called.");
		try {
			db.beginTransaction();
			createTables(db);
			db.setTransactionSuccessful();
			db.endTransaction();
		} catch (SQLiteException e) {
			Log.d(TAG, "onCreate() is failed.: " + e.toString());
			e.printStackTrace();
		}

		Log.d(TAG, "complete to create.");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < 2) {
			TableEtcData.create(db);
		}
		if (oldVersion < 3) {
			upgradeTo3(db);
		}
	}

	private void createTables(SQLiteDatabase db) {
		TableWordData.create(db);
		TableEtcData.create(db);
		TableGroupData.create(db);
	}

	private void upgradeTo3(SQLiteDatabase db) {
		TableGroupData.create(db);

		db.beginTransaction();

		String sql = "ALTER TABLE " + TableWordData.TABLE_NAME
				+ " ADD COLUMN " + TableWordData.Columns.WORDGROUP
				+ " TEXT";
		db.execSQL(sql);

		db.setTransactionSuccessful();
		db.endTransaction();



        db.beginTransaction();
        String[] projection = {TableWordData.Columns._ID, TableWordData.Columns.DIVISION};
        Cursor cursor = db.query(TableWordData.TABLE_NAME, projection, null, null, null, null, null);

        // DIVISION value to WORDGROUP
        if (cursor != null) {
            try{
                if(cursor.moveToFirst()) {
                    while(!cursor.isAfterLast()) {
                        int id = cursor.getInt(0);
                        int group = cursor.getInt(1) + 1;

                        ContentValues values = new ContentValues();
                        values.put(TableWordData.Columns.WORDGROUP, String.valueOf(group));
                        String selection = TableWordData.Columns._ID + "=" + id;

                        if(-1 != db.update(TableWordData.TABLE_NAME, values, selection, null)) {
                            //Toast.makeText(getApplicationContext(), "저장되었습니다", Toast.LENGTH_SHORT).show();
                        }

                        cursor.moveToNext();
                    }
                }
            } catch (NullPointerException e) {
                //Toast.makeText(getApplicationContext(), "null", Toast.LENGTH_SHORT).show();
                Log.e(TAG, e.toString());
            }
            cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put(TableGroupData.Columns.GROUP_NAME, GroupHelper.DEFAULT_GROUP);
        if(-1 != db.insert(TableGroupData.TABLE_NAME, null, values)) {
            //Toast.makeText(getApplicationContext(), "저장되었습니다", Toast.LENGTH_SHORT).show();
        }
        // Add default group name 1~5 group
        for (int i=1; i<=5; i++) {
            values = new ContentValues();
            values.put(TableGroupData.Columns.GROUP_NAME, String.valueOf(i));
            if(-1 != db.insert(TableGroupData.TABLE_NAME, null, values)) {
                //Toast.makeText(getApplicationContext(), "저장되었습니다", Toast.LENGTH_SHORT).show();
            }
        }

        // TODO : drop column DIVISION from TableWordData
        db.setTransactionSuccessful();
        db.endTransaction();
	}
}
