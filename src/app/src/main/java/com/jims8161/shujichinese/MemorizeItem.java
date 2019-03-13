package com.jims8161.shujichinese;

/**
 * Created by hoyeong on 2016-03-10.
 */
public class MemorizeItem {
    public int mId;
    public String mChn;
    public String mPinyin;
    public String mMean;
    public String mRealPinyin;
    public boolean mMemorized;

    public MemorizeItem(int id, String chn, String pinyin, String mean, String realPinyin, boolean memorized) {
        mId = id;
        mChn = chn;
        mPinyin = pinyin;
        mMean = mean;
        mRealPinyin = realPinyin;
        mMemorized = memorized;
    }
}
