package com.jims8161.shujichinese.exam;

import java.util.Calendar;

import com.jims8161.shujichinese.R;
import com.jims8161.shujichinese.ShujiConstants;
import com.jims8161.shujichinese.ShujiDatabaseHelper;
import com.jims8161.shujichinese.TableEtcData;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.TimePicker.OnTimeChangedListener;

public class ShujiExamTimeSelectActivity extends Activity {
	private TimePicker mTimePicker;
	private Button btnSubmit;
	private Button btnCancel;

	private int trigger_h = ShujiExamConstants.MEMORIZE_ALARM_DEFAULT_TIME;
	private int trigger_m = 0;
	private long trigger_time = 0;

	private boolean isModify = false;
	private int _id = 0;

	private String TAG = ShujiExamTimeSelectActivity.class.getName();
	Context mContext;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shuji_exam_time_select);

		mContext = this;

		isModify = getIntent().getBooleanExtra("isModify", false);

		mTimePicker = (TimePicker) findViewById(R.id.timeExamTimePicker);
		mTimePicker.setIs24HourView(DateFormat.is24HourFormat(this));

		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, ShujiExamConstants.MEMORIZE_ALARM_DEFAULT_TIME);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);

		//SharedPreferences mPref = getSharedPreferences(ShujiReminderConstants.PREFS_SHUJI_REMINDER, 0);
		//trigger_time = mPref.getLong(ShujiReminderConstants.PREFS_SHUJI_REMINDER_KEY_ALARM_TIME, c.getTimeInMillis());

		if(isModify == true) {
			trigger_time = getIntent().getLongExtra("trigger_time", c.getTimeInMillis());
			_id = getIntent().getIntExtra("_id", -1);
			c.setTimeInMillis(trigger_time);
		}

		trigger_h = c.get(Calendar.HOUR_OF_DAY);
		trigger_m = c.get(Calendar.MINUTE);

		mTimePicker.setCurrentHour(trigger_h);
		mTimePicker.setCurrentMinute(trigger_m);

		mTimePicker.setOnTimeChangedListener(new OnTimeChangedListener() {

			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
			}

		});

		btnSubmit = (Button)findViewById(R.id.buttonSubmit);
		btnCancel = (Button)findViewById(R.id.buttonCancel);

		btnSubmit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				trigger_h = mTimePicker.getCurrentHour();
				trigger_m = mTimePicker.getCurrentMinute();
				if (saveTriggerTime() == true) {
					setResult(RESULT_OK);
					finish();
				}
			}
		});

		btnCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}

	private boolean saveTriggerTime() {

		int examtime = trigger_h * 100 + trigger_m;
		int examtype = 0;

		SQLiteDatabase db = new ShujiDatabaseHelper(getApplicationContext()).getWritableDatabase();

		String selection = TableEtcData.Columns.ALARM_EXAM_TIME + "=" + examtime;
		String[] projection = {TableEtcData.Columns._ID,
				TableEtcData.Columns.ALARM_EXAM_TIME,
				TableEtcData.Columns.ALARM_EXAM_TYPE,
				TableEtcData.Columns.ALARM_EXAM_TIME_ACTIVE};
		Cursor cursor = db.query(TableEtcData.TABLE_NAME, projection, selection, null, null, null, null);

		if(cursor != null && cursor.getCount() > 0) {
			Toast.makeText(getApplicationContext(), "같은 시간이 이미 존재합니다.", Toast.LENGTH_SHORT).show();
			cursor.close();
			db.close();
			return false;
		}

		ContentValues values = new ContentValues();
		values.put(TableEtcData.Columns.ALARM_EXAM_TIME, examtime);
		values.put(TableEtcData.Columns.ALARM_EXAM_TYPE, examtype);	// reserved
		values.put(TableEtcData.Columns.ALARM_EXAM_TIME_ACTIVE, true);

		if (isModify == false) {
			if(-1 != db.insert(TableEtcData.TABLE_NAME, null, values)) {
				Log.d(TAG, "Saved");
				Toast.makeText(getApplicationContext(), "저장되었습니다", Toast.LENGTH_SHORT).show();
			}
		} else {
			selection = TableEtcData.Columns._ID + "=" + _id;
			if(-1 != db.update(TableEtcData.TABLE_NAME, values, selection, null)) {
				Toast.makeText(getApplicationContext(), "변경되었습니다", Toast.LENGTH_SHORT).show();
			}
		}

		db.close();
		ShujiExamSetting.removeExamAlarm(getApplicationContext(), _id);
		ShujiExamSetting.setExamAlarm(getApplicationContext(), _id, trigger_h, trigger_m, examtype);
		ShujiExamSetting.isDataChanged = true;
		return true;
	}
}
