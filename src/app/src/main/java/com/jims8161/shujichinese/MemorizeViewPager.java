package com.jims8161.shujichinese;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * Created by hoyeong on 2016-03-10.
 */
public class MemorizeViewPager extends ViewPager {
    private MemorizeViewPagerAdapter mAdapter;
    public MemorizeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAdapter(PagerAdapter adapter) {
        mAdapter = (MemorizeViewPagerAdapter)adapter;
        super.setAdapter(adapter);
    }
}


