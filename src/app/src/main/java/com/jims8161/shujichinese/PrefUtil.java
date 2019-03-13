package com.jims8161.shujichinese;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2016-02-20.
 */
public class PrefUtil {
    private static final String TAG = MainActivity.TAG;
    private static final Object mObj = new Object();
    private PrefUtil() {};

    public static final String PREFS_SHUJI = "shujiPrefs";
    public static final String PREF_FILE = PREFS_SHUJI;

    public static final String PREFS_SHUJI_KEY_BOOL_INCLUDE_MEMORIZED = "bIncludeMemzed";
    public static final String PREFS_SHUJI_KEY_BOOL_USE_TTS = "bUseTTS";
    public static final String PREFS_SHUJI_KEY_BOOL_RANDOM = "bRandom";
    public static final String PREFS_SHUJI_KEY_BOOL_HIDE_PINYIN = "bHidePinyin";
    public static final String PREFS_SHUJI_KEY_INT_MEM_DISP_ORDER = "memorize_display_order";
    public static final String PREFS_SHUJI_KEY_BOOL_DISP_DIVISION = "bDispDivision";


    public static final String PREFS_SHUJI_KEY_BOOL_LIST_FIRST_LAUNCH = "bListFirstLaunch";
    public static final String PREFS_SHUJI_KEY_INT_LIST_DISP_ORDER = "list_display_order";
    public static final String PREFS_SHUJI_KEY_BOOL_LIST_INCLUDE_MEMORIZED = "bListIncludeMemzed";
    public static final String PREFS_SHUJI_KEY_BOOL_LIST_DISP_DIVISION = "bListDispDivision";

    public static final String PREFS_SHUJI_KEY_BOOL_EXAM_ALARM_SOUND = "bExamAlarmSound";
    public static final String PREFS_SHUJI_KEY_BOOL_EXAM_ALARM_VIBE = "bExamAlarmVibe";

    public static final int MEMORIZE_DISP_MEAN_FIRST = 0;
    public static final int MEMORIZE_DISP_CHN_FIRST = 1;

    public static final int LIST_DISP_DIVISION_ORDER = 0;
    public static final int LIST_DISP_ADDED_ORDER = 1;

    public static int getIntPref(Context context, String key, int defValue) {
        synchronized (mObj) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
            return sharedPrefs.getInt(key, defValue);
        }
    }

    public static void setIntPref(Context context, String key, int value) {
        synchronized (mObj) {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit();
            editor.putInt(key, value);
            editor.commit();
        }
    }

    public static long getLongPref(Context context, String key, long defValue) {
        synchronized (mObj) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
            return sharedPrefs.getLong(key, defValue);
        }
    }

    public static void setLongPref(Context context, String key, long value) {
        synchronized (mObj) {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit();
            editor.putLong(key, value);
            editor.commit();
        }
    }

    public static void setBoolean(Context context, String key, boolean value) {
        synchronized (mObj) {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit();
            editor.putBoolean(key, value);
            editor.commit();
        }
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        synchronized (mObj) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
            return sharedPrefs.getBoolean(key, defValue);
        }
    }

    public static void setString(Context context, String key, String value) {
        synchronized (mObj) {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit();
            editor.putString(key, value);
            editor.commit();
        }
    }

    public static String getString(Context context, String key, String defValue) {
        synchronized (mObj) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
            return sharedPrefs.getString(key, defValue);
        }
    }

    public static boolean isFirstLaunch(Context context) {
        synchronized (mObj) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
            return sharedPrefs.getBoolean(PREFS_SHUJI_KEY_BOOL_LIST_FIRST_LAUNCH, true);
        }
    }

    public static void setFirstLaunchFalse(Context context) {
        synchronized (mObj) {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit();
            editor.putBoolean(PREFS_SHUJI_KEY_BOOL_LIST_FIRST_LAUNCH, false);
            editor.commit();
        }
    }
}
