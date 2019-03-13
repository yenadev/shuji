package com.jims8161.shujichinese;

/**
 * Created by hoyeong on 2016-03-07.
 */
public class GroupItem {
    public int mId;
    public String mName;
    public boolean mDisplay;
    public boolean mDisplayInList;
    public GroupItem(int id, String name) {
        mId = id;
        mName = name;
        mDisplay = true;
        mDisplayInList = true;
    }
    public GroupItem(int id, String name, boolean bDisplay, boolean bDisplayInList) {
        mId = id;
        mName = name;
        mDisplay = bDisplay;
        mDisplayInList = bDisplayInList;
    }
}
