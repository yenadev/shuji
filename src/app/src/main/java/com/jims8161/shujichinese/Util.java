package com.jims8161.shujichinese;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by Administrator on 2016-02-23.
 */
public class Util {
    public static final String TAG = "Util";
    private Util() {}

    public static String loadXML(Context context, int resID) {
        String result = null;

        InputStream inputStream = context.getResources().openRawResource(resID);
        try {
            byte[] txt = new byte[inputStream.available()];
            int size = inputStream.read(txt);
            if (size <= 0)
                return null;

            result = new String(txt, Charset.defaultCharset());
        } catch (IOException e) {
            Log.e(TAG, "ParsingCML exception. " + e.getMessage());
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Close inputstream Exception. " + e.getMessage());
            }
        }

        return result;
    }

}
