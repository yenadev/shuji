package com.jims8161.shujichinese;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hoyeong on 2016-03-10.
 */
public class MemorizeViewPagerAdapter extends PagerAdapter {
    private static final String TAG = "PagerAdapter";
    Context mContext;
    LayoutInflater mInflater;
    private int mCurrentPosition;
    private ArrayList<MemorizeItem> mArrMemorizeItem;
    private HashMap<Integer, ViewContainer> mContainerMap;
    private int word_order[];

    public MemorizeViewPagerAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;
        mContainerMap = new HashMap<>();
    }

    @Override
    public int getCount() {
        return mArrMemorizeItem.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public void setItemList(ArrayList<MemorizeItem> list) {
        mArrMemorizeItem = list;
    }

    public void setItemOrderList(int[] orderList) {
        word_order = orderList;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.d(TAG, "destroyItem : position=" + position);
        container.removeView((View)object);
        if (mContainerMap != null) {
            mContainerMap.remove(position);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        if (mContainerMap.get(position) != null) {
            Log.d(TAG, "instantiateItem : View already exist");
            ViewContainer oldViewContainder = mContainerMap.get(position);
            container.addView(oldViewContainder.layout);
            return oldViewContainder.layout;
        }
        Log.d(TAG, "instantiateItem : position="+position);

        View view = mInflater.inflate(R.layout.layout_memorize_item, null);

        final ViewContainer viewContainer = new ViewContainer();
        viewContainer.layout = view;
        viewContainer.tvChn = (TextView)view.findViewById(R.id.textViewMemChn);
        viewContainer.tvPinyin = (TextView)view.findViewById(R.id.textViewMemPinyin);
        viewContainer.tvMeaning = (TextView)view.findViewById(R.id.textViewMemMeaning);
        viewContainer.tvCount = (TextView)view.findViewById(R.id.textViewMemCount);

        viewContainer.tvPinyin.setVisibility( (MainActivity.bHidePinyin) ? View.INVISIBLE : View.VISIBLE);
        viewContainer.chkMemorized = (CheckBox)view.findViewById(R.id.checkBoxMemMemorized);
        viewContainer.chkMemorized.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //int order_cur_word = (Integer)chkMemorized.getTag(0);

                int checked = viewContainer.chkMemorized.isChecked() ? 1 : 0;
                //arMemorized.set(cur_disp_word, checked);
                mArrMemorizeItem.get(word_order[position]).mMemorized = viewContainer.chkMemorized.isChecked();

                ContentValues values = new ContentValues();
                values.put(TableWordData.Columns.EXCLUDE, checked);
                SQLiteDatabase db = new ShujiDatabaseHelper(mContext).getWritableDatabase();

                String selection = TableWordData.Columns._ID + "=" + mArrMemorizeItem.get(word_order[position]).mId;//arID.get(cur_disp_word);
                if(-1 != db.update(TableWordData.TABLE_NAME, values, selection, null)) {
                    //Toast.makeText(getApplicationContext(), "저장되었습니다", Toast.LENGTH_SHORT).show();
                }

                db.close();
                //clearField();
                ShujiList.isDataChanged = true;

            }
        });

        viewContainer.chkHidePinyin = (CheckBox)view.findViewById(R.id.checkBoxMemHidePinyin);
        viewContainer.chkHidePinyin.setChecked(MainActivity.bHidePinyin);
        viewContainer.chkHidePinyin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.bHidePinyin = viewContainer.chkHidePinyin.isChecked();
                viewContainer.tvPinyin.setVisibility((MainActivity.bHidePinyin) ? View.INVISIBLE : View.VISIBLE);

                SharedPreferences pref = mContext.getSharedPreferences(PrefUtil.PREF_FILE, 0);
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean(PrefUtil.PREFS_SHUJI_KEY_BOOL_HIDE_PINYIN, MainActivity.bHidePinyin);
                editor.commit();
            }
        });

        mContainerMap.put(position, viewContainer);
        container.addView(view);
        return view;
    }

    public int updateView(int display_status, int currentPosition) {
        Log.d(TAG, "updateView display_status=" + display_status);
        if (mArrMemorizeItem == null || mArrMemorizeItem.size() == 0)
            return ShujiMemorize.DISPLAY_INTERVAL;
        String str, str2 = null, strCount;
        int disp_target=0;  // 0 : Chinese, 1 : Korean
        MemorizeItem curItem = mArrMemorizeItem.get(word_order[currentPosition]);
        ViewContainer viewContainer = mContainerMap.get(currentPosition);

        viewContainer.chkHidePinyin.setChecked(MainActivity.bHidePinyin);
        viewContainer.tvPinyin.setVisibility((MainActivity.bHidePinyin) ? View.INVISIBLE : View.VISIBLE);
        if(MainActivity.memorize_display_order == PrefUtil.MEMORIZE_DISP_MEAN_FIRST) {
            if(display_status == 0) {
                str = curItem.mMean;
                disp_target = 1;
                viewContainer.tvChn.setText("");
                viewContainer.tvPinyin.setText("");
                viewContainer.chkMemorized.setChecked(curItem.mMemorized);
            }
            else {
                str = curItem.mChn;
                str2 = curItem.mPinyin;
                disp_target = 0;
            }
        }
        else { //if(MainActivity.memorize_display_order == MainActivity.MEMORIZE_DISP_CHN_FIRST) {
            if(display_status == 0) {
                str = curItem.mChn;
                str2 = curItem.mPinyin;
                disp_target = 0;
                viewContainer.tvMeaning.setText("");
                viewContainer.chkMemorized.setChecked(curItem.mMemorized);
            }
            else {
                str = curItem.mMean;
                disp_target = 1;
            }
        }

        if(disp_target == 0) {
            viewContainer.tvChn.setText(str);
            viewContainer.tvPinyin.setText(str2);
            if(MainActivity.bUseTTS) {
                Intent serviceintent = new Intent(mContext, ShujiService.class);
                serviceintent.putExtra(ShujiConstants.EXTRAS_HANZI, str);
                mContext.startService(serviceintent);
            }
        }
        else {
            viewContainer.tvMeaning.setText(str);
            if(MainActivity.bUseTTS) {
                Intent serviceintent = new Intent(mContext, ShujiService.class);
                serviceintent.putExtra(ShujiConstants.EXTRAS_MEANING, str);
                mContext.startService(serviceintent);
            }
        }
        strCount = "" + (currentPosition+1) + " / " + mArrMemorizeItem.size();
        viewContainer.tvCount.setText(strCount);


        int length = str.length();

        if(disp_target == 0) {
            int dip = 60;
            if(length < 5)
                dip = 80;
            else if(length < 12)
                dip = 60;
            else if(length < 20)
                dip = 45;
            else if(length < 30)
                dip = 35;
            else
                dip = 30;

            viewContainer.tvChn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, dip);
        }

        int interval = ShujiMemorize.DISPLAY_INTERVAL;
        if(disp_target == 1 && length > 8)	// 뜻이 길때
            interval = interval + ((length-8)*300);
        else if(disp_target == 0 && length > 5)	// 중국 단어가 길떄
            interval = interval + ((length-5)*400);

        return interval;
    }

    private static class ViewContainer {
        View layout;
        TextView tvChn;
        TextView tvPinyin;
        TextView tvMeaning;
        TextView tvCount;
        CheckBox chkMemorized;
        CheckBox chkHidePinyin;
    }
}
