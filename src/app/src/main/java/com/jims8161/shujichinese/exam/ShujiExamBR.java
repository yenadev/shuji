package com.jims8161.shujichinese.exam;

import com.jims8161.shujichinese.PrefUtil;
import com.jims8161.shujichinese.R;
import com.jims8161.shujichinese.ShujiConstants;
import com.jims8161.shujichinese.ShujiDatabaseHelper;
import com.jims8161.shujichinese.TableEtcData;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;


public class ShujiExamBR extends BroadcastReceiver {
	private final String TAG = ShujiExamBR.class.getName();

	@Override
	public void onReceive(final Context context, Intent intent) {
		if(intent == null) return;
		String action = intent.getAction();
		if(action == null) return;

		if (action.equals("android.intent.action.BOOT_COMPLETED")) {
			restartAlarms(context);
			Log.d(TAG, "Boot completed. Re-start Alarms");
		} else if (action.equals(ShujiExamConstants.ACTION_ALARM_DAILY_EXAM)) {
			if (ShujiExamActivity.bOnExam == true) {
				Toast.makeText(context, "단어 암기 테스트 알람.\n 시험이 이미 진행중입니다.", Toast.LENGTH_LONG).show();
				return;
			}
			int problemCnt = ShujiExamActivity.makeProblem(context, 5);

			if (problemCnt <= 0) {
				Log.e(TAG, "Exam alarm triggered ID : deficient in word !!!");
				return;
			}
			makeNotification(context);
			final int id = intent.getIntExtra(TableEtcData.Columns._ID, -1);
			final int examtime = intent.getIntExtra(TableEtcData.Columns.ALARM_EXAM_TIME, -1);
			final int examtype = intent.getIntExtra(TableEtcData.Columns.ALARM_EXAM_TYPE, 0);
			Log.d(TAG, "Exam alarm triggered ID : " + id);
			if (id != -1 && examtime != -1) {
				Handler mHandler = new Handler();
				mHandler.postDelayed(new Runnable() {
					public void run() {
						ShujiExamSetting.setExamAlarm(context, id, examtime / 100, examtime % 100, examtype);
					}
				}, 5000);
			}
		}
	}

	private void makeNotification(Context context) {

		Intent intent = new Intent(context, ShujiExamActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("problemCount", 5);
		PendingIntent pintent = PendingIntent.getActivity(
				context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		String ticker = "단어 암기 테스트";
		String title = "Shuji Chinese";
		String text = "단어 암기 테스트 시간";

		if (ShujiExamActivity.problemList != null && ShujiExamActivity.problemList.size() > 0) {
			ShujiExamActivity.ProblemItem item = ShujiExamActivity.problemList.get(0);
			ticker = item.mWord + "의 뜻은?";
			title = item.mWord + "의 뜻은?";
		}

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setSmallIcon(R.mipmap.ic_launcher)
				.setTicker(ticker)
				.setContentTitle(ticker)
				.setContentText(text)
				.setContentIntent(pintent)
				.setAutoCancel(true);
		Notification notification = builder.build();

		SharedPreferences pref = context.getSharedPreferences(PrefUtil.PREF_FILE, 0);
		boolean bExamAlarmSound = pref.getBoolean(
				PrefUtil.PREFS_SHUJI_KEY_BOOL_EXAM_ALARM_SOUND, true);
		boolean bExamAlarmVibe = pref.getBoolean(
				PrefUtil.PREFS_SHUJI_KEY_BOOL_EXAM_ALARM_VIBE, true);

		if (bExamAlarmSound)
			notification.defaults |= Notification.DEFAULT_SOUND;
		if (bExamAlarmVibe)
			notification.defaults |= Notification.DEFAULT_VIBRATE;

		NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

		nm.notify(ShujiExamConstants.NOTIFICATION_ID_DAILY_EXAM, notification);
	}

	private void restartAlarms(Context context) {
		String[] projection = {TableEtcData.Columns._ID,
				TableEtcData.Columns.ALARM_EXAM_TIME,
				TableEtcData.Columns.ALARM_EXAM_TYPE,
				TableEtcData.Columns.ALARM_EXAM_TIME_ACTIVE};

		SQLiteDatabase db = new ShujiDatabaseHelper(context).getWritableDatabase();
		Cursor cursor = db.query(TableEtcData.TABLE_NAME, projection, null, null, null, null, null);

		if (cursor != null) {
			try{
				if(cursor.moveToFirst()) {
					while(!cursor.isAfterLast()) {

						int tmptime = cursor.getInt(1);

						ShujiExamSetting.setExamAlarm(context, cursor.getInt(0), tmptime/100, tmptime%100, cursor.getInt(2));

						cursor.moveToNext();
					}
				}

			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			cursor.close();
		}
		db.close();
	}
}
