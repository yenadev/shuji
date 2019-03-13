package com.jims8161.shujichinese;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.jims8161.shujichinese.exam.ShujiExamActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "ShujiChinese";


    public static String SHUJI_BUILD_DATE = "";
    public static String SHUJI_VERSION_NAME = "";

    // Request codes
    static final int RC_FRONT = 0;

    // shared data for Memorize Setting
    public static int memorize_display_order = 0;
    public static boolean bIncludeMemzed = false;
    public static boolean bUseTTS = false;
    public static boolean bRandom = false;
    public static boolean bHidePinyin = false;

    // shared data for List Setting
    public static int list_display_order = 0;
    public static boolean bListIncludeMemzed = false;

    public static boolean bExamAlarmSound = false;
    public static boolean bExamAlarmVibe = false;

    static public MainActivity mContext;

    public static final boolean bTurnOffAd = false;
    public static final boolean bShowPopupLog = false;

    //public static NotificationManager nm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, "ca-app-pub-8960156935811974~6884670043");
        mContext = this;

        GroupHelper.loadGroups(mContext);

        SharedPreferences pref = getSharedPreferences(
                PrefUtil.PREF_FILE, 0);

        // 메모라이즈화면 관련 설정
        bIncludeMemzed = pref.getBoolean(
                PrefUtil.PREFS_SHUJI_KEY_BOOL_INCLUDE_MEMORIZED, false);
        bUseTTS = pref.getBoolean(PrefUtil.PREFS_SHUJI_KEY_BOOL_USE_TTS,
                false);
        bRandom = pref.getBoolean(PrefUtil.PREFS_SHUJI_KEY_BOOL_RANDOM,
                false);
        memorize_display_order = pref.getInt(
                PrefUtil.PREFS_SHUJI_KEY_INT_MEM_DISP_ORDER,
                PrefUtil.MEMORIZE_DISP_MEAN_FIRST);

        // 리스트화면 관련 설정

        bListIncludeMemzed = pref.getBoolean(
                PrefUtil.PREFS_SHUJI_KEY_BOOL_LIST_INCLUDE_MEMORIZED,
                true);
        list_display_order = pref.getInt(
                PrefUtil.PREFS_SHUJI_KEY_INT_LIST_DISP_ORDER,
                PrefUtil.LIST_DISP_DIVISION_ORDER);

        bExamAlarmSound = pref.getBoolean(
                PrefUtil.PREFS_SHUJI_KEY_BOOL_EXAM_ALARM_SOUND, true);
        bExamAlarmVibe = pref.getBoolean(
                PrefUtil.PREFS_SHUJI_KEY_BOOL_EXAM_ALARM_VIBE, true);

        // /////////////////////////////////////////

        buildDate();
        SHUJI_VERSION_NAME = getVersionName(this);

        //nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        ShujiExamActivity.bOnExam = false;
        // start menu
        Intent intent;
        intent = new Intent(getApplication(), FrontActivity.class);
        startActivityForResult(intent, RC_FRONT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 메뉴 액티비티
        if (requestCode == RC_FRONT) {
            if (resultCode == FrontActivity.FRONT_RESULT_EXIT) {
                finish();
            }
        }
    }



    public void buildDate() {
        try {

            PackageManager pm = getPackageManager();
            PackageInfo packageInfo = null;
            try {
                packageInfo = pm.getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            // install datetime
            String appInstallDate = DateUtils.getDate(
                    "yyyy/MM/dd hh:mm:ss.SSS", packageInfo.lastUpdateTime);

            // build datetime
            SHUJI_BUILD_DATE = DateUtils.getDate("yyyy/MM/dd HH:mm",
                    DateUtils.getBuildDate(this));

            Log.i(TAG, "appBuildDate = " + SHUJI_BUILD_DATE);
            Log.i(TAG, "appInstallDate = " + appInstallDate);

        } catch (Exception e) {
        }

    }

    static class DateUtils {

        public static String getDate(String dateFormat) {
            Calendar calendar = Calendar.getInstance();
            return new SimpleDateFormat(dateFormat, Locale.getDefault())
                    .format(calendar.getTime());
        }

        public static String getDate(String dateFormat, long currenttimemillis) {
            return new SimpleDateFormat(dateFormat, Locale.getDefault())
                    .format(currenttimemillis);
        }

        public static long getBuildDate(Context context) {

            try {
                ApplicationInfo ai = context.getPackageManager()
                        .getApplicationInfo(context.getPackageName(), 0);
                ZipFile zf = new ZipFile(ai.sourceDir);
                ZipEntry ze = zf.getEntry("classes.dex");
                long time = ze.getTime();
                zf.close();

                return time;

            } catch (Exception e) {
            }

            return 0l;
        }

    }

    public static String getVersionName(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

}
