package com.jims8161.shujichinese.exam;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.jims8161.shujichinese.MainActivity;
import com.jims8161.shujichinese.PrefUtil;
import com.jims8161.shujichinese.R;
import com.jims8161.shujichinese.ShujiConstants;
import com.jims8161.shujichinese.ShujiDatabaseHelper;
import com.jims8161.shujichinese.ShujiMemorizeSetting;
import com.jims8161.shujichinese.TableEtcData;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

public class ShujiExamSetting extends Activity {
	private static final int TIME_SELECT_CODE = 0;
	private static final int TIME_CHANGE_CODE = 1;
	private static final String TAG = "ShujiExamSetting";
	Button btnMemSetting;
	Button btnExit;
	Button btnAddAlarm;
	CheckBox chkSound;
	CheckBox chkVibe;

	ListView listView;
	ArrayList<String> al;
	ArrayAdapter<String> aa;
	public static ArrayList<ExamListItem> alExamList;
	public static boolean isDataChanged;

	Context context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shuji_exam_setting);
		context = this;
		listView = (ListView) findViewById(R.id.listViewExamSettingTimeList);

		al = new ArrayList<String>();
		alExamList = new ArrayList<ExamListItem>();
		aa = new ArrayAdapter<String>(getApplicationContext(),
				R.layout.layout_dailyexam_time_list_adapter, al);

		listView.setAdapter(aa);
		listView.setOnItemClickListener(mListItemClickListener);

		btnMemSetting = (Button) findViewById(R.id.buttonMemSetting);
		btnExit = (Button) findViewById(R.id.buttonExit);
		btnAddAlarm = (Button) findViewById(R.id.buttonAdd);

		btnMemSetting.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplication(),
						ShujiMemorizeSetting.class);
				intent.putExtra("from", ShujiExamSetting.class.getName());
				startActivity(intent);
			}
		});

		btnAddAlarm.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplication(),
						ShujiExamTimeSelectActivity.class);
				startActivityForResult(intent, TIME_SELECT_CODE);
			}
		});
		btnExit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		chkSound = (CheckBox) findViewById(R.id.checkBoxExamSettingSound);
		chkSound.setChecked(MainActivity.bExamAlarmSound);
		chkSound.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MainActivity.bExamAlarmSound = chkSound.isChecked();
				SharedPreferences mPref = getSharedPreferences(PrefUtil.PREF_FILE, 0);
				SharedPreferences.Editor mPrefEdit = mPref.edit();
				mPrefEdit.putBoolean(PrefUtil.PREFS_SHUJI_KEY_BOOL_EXAM_ALARM_SOUND, MainActivity.bExamAlarmSound);
				mPrefEdit.commit();
			}
		});

		chkVibe = (CheckBox) findViewById(R.id.checkBoxExamSettingVibe);
		chkVibe.setChecked(MainActivity.bExamAlarmVibe);
		chkVibe.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MainActivity.bExamAlarmVibe = chkVibe.isChecked();
				SharedPreferences mPref = getSharedPreferences(PrefUtil.PREFS_SHUJI, 0);
				SharedPreferences.Editor mPrefEdit = mPref.edit();
				mPrefEdit.putBoolean(PrefUtil.PREFS_SHUJI_KEY_BOOL_EXAM_ALARM_VIBE, MainActivity.bExamAlarmVibe);
				mPrefEdit.commit();
			}
		});

		isDataChanged = true;
		updateList();
	}

	AdapterView.OnItemClickListener mListItemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, final int position,
								long id) {

			new AlertDialog.Builder(context).setMessage("데일리 테스트")
					.setPositiveButton("시간변경", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							Intent intent = new Intent(getApplication(),
									ShujiExamTimeSelectActivity.class);
							intent.putExtra("isModify", true);
							intent.putExtra("_id", alExamList.get(position)._id);
							intent.putExtra("trigger_time", alExamList.get(position).timestamp);

							startActivityForResult(intent, TIME_CHANGE_CODE);
						}
					})
					.setNegativeButton("삭제", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							deleteItem(alExamList.get(position)._id);
						}
					})
					.show();


		}
	};

	private void deleteItem(final int id) {
		new AlertDialog.Builder(context).setMessage("삭제하시겠습니까?")
				.setPositiveButton("예", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						SQLiteDatabase db = new ShujiDatabaseHelper(getApplicationContext()).getWritableDatabase();

						String selection = TableEtcData.Columns._ID + "=" + id;

						if( 0 < db.delete(TableEtcData.TABLE_NAME, selection, null)) {
						}
						db.close();
						removeExamAlarm(getApplicationContext(), id);
						isDataChanged = true;
						Toast.makeText(getApplicationContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
						updateList();
					}
				})
				.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				})
				.show();
	}

	public static void removeExamAlarm(Context context, int id) {
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(ShujiExamConstants.SCHEME);
		builder.authority(ShujiExamConstants.AUTHORITY);
		builder.appendPath(TAG);
		builder.appendPath(""+id);

		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(ShujiExamConstants.ACTION_ALARM_DAILY_EXAM);
		Uri dataName = builder.build();
		intent.setData(dataName);
		intent.putExtra("_id", id);
		PendingIntent pintent = PendingIntent.getBroadcast(context, 0, intent, 0);
		alarmManager.cancel(pintent);
	}

	public static void setExamAlarm(Context context, int id, int hh, int mm, int examtype) {
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(ShujiExamConstants.SCHEME);
		builder.authority(ShujiExamConstants.AUTHORITY);
		builder.appendPath(TAG);
		builder.appendPath(""+id);

		long current = System.currentTimeMillis();

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(current);
		c.set(Calendar.HOUR_OF_DAY, hh);
		c.set(Calendar.MINUTE, mm);
		c.set(Calendar.SECOND, 0);

		if (c.getTimeInMillis() < current) {
			c.add(Calendar.DAY_OF_MONTH, 1);
		}

		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(ShujiExamConstants.ACTION_ALARM_DAILY_EXAM);
		Uri dataName = builder.build();
		intent.setData(dataName);
		intent.putExtra(TableEtcData.Columns._ID, id);
		intent.putExtra(TableEtcData.Columns.ALARM_EXAM_TIME, hh*100 + mm);
		intent.putExtra(TableEtcData.Columns.ALARM_EXAM_TYPE, examtype);
		PendingIntent pintent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timestamp, AlarmManager.INTERVAL_DAY, pintent);
		//alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, timestamp, AlarmManager.INTERVAL_DAY, pintent);

		try {
			if (Build.VERSION.SDK_INT < 19) {
				alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pintent);
			} else {
				alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pintent);
			}
		} catch (SecurityException e) {
			e.printStackTrace();
			Log.e(TAG, "Alarm is full. Set alarm fail ID : " + id);
			return;
		}

		Log.d(TAG, "Exam alarm set ID : " + id);

		if (MainActivity.bShowPopupLog)
			Toast.makeText(context, "Exam alarm set ID : " + id + " = HH/MM : " + hh + "/" + mm , Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateList();
	}

	private void updateList() {
		if (isDataChanged == false)
			return;
		aa.clear();
		String[] projection = {TableEtcData.Columns._ID,
				TableEtcData.Columns.ALARM_EXAM_TIME,
				TableEtcData.Columns.ALARM_EXAM_TYPE,
				TableEtcData.Columns.ALARM_EXAM_TIME_ACTIVE};

		SQLiteDatabase db = new ShujiDatabaseHelper(getApplicationContext()).getWritableDatabase();
		Cursor cursor = db.query(TableEtcData.TABLE_NAME, projection, null, null, null, null, null);
		ExamListItem item;
		alExamList.removeAll(alExamList);
		al.removeAll(al);

		if (cursor != null) {
			try{
				if(cursor.moveToFirst()) {
					while(!cursor.isAfterLast()) {

						int tmptime = cursor.getInt(1);
						Calendar c = Calendar.getInstance();
						c.set(Calendar.HOUR_OF_DAY, tmptime/100);
						c.set(Calendar.MINUTE, tmptime%100);
						c.set(Calendar.SECOND, 0);

						item = new ExamListItem(cursor.getInt(0), c.getTimeInMillis(), cursor.getInt(3), cursor.getInt(2));
						alExamList.add(item);

						SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());

						al.add("" + format.format(new Date(item.timestamp)));
						cursor.moveToNext();
					}
					//adapterWordList.notifyDataSetChanged();
				}

			} catch (NullPointerException e) {
				Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();
			}
			cursor.close();
		}
		db.close();
		aa.notifyDataSetChanged();
		isDataChanged = false;
	}

	class ExamListItem {
		ExamListItem(int id, long time, int bActive, int nType) {
			_id = id;
			timestamp = time;
			isActive = bActive;
			type = nType;
		}

		int _id;
		long timestamp;
		int type;
		int isActive;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
			case TIME_SELECT_CODE:
				updateList();
				break;
			case TIME_CHANGE_CODE:
				updateList();
				break;
		}
	}
}
