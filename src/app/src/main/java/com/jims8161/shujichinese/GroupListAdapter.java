package com.jims8161.shujichinese;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;

/**
 * Created by hoyeong on 2016-03-07.
 */
public class GroupListAdapter extends BaseAdapter {
    private static final String TAG = "GroupListAdapter";
    public static final int TYPE_MEMORIZE = 0;
    public static final int TYPE_LIST = 1;
    Context mContext;
    LayoutInflater Inflater;
    ArrayList<GroupItem> arSrc;
    int layout;
    int mType;

    public GroupListAdapter(Context context, int alayout, ArrayList<GroupItem> aarSrc, int type) {
        mContext = context;
        Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        arSrc = aarSrc;
        layout = alayout;
        mType = type;
    }

    @Override
    public int getCount() {
        return arSrc.size();
    }

    @Override
    public Object getItem(int position) {
        return arSrc.get(position);
    }

    @Override
    public long getItemId(int position) {
        return arSrc.get(position).mId;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = null;

        Log.d(TAG, "getView");
        if(arSrc.size() == 0) {
            Log.d(TAG, "arSrc.size == 0");
            return null;
        }

        if(convertView == null) {
            // convertView가 null 이면 처음 생성하는 아이템이다.
            convertView = Inflater.inflate(layout, parent, false);

            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.checkBox = (CheckBox)convertView.findViewById(R.id.checkBox);
            convertView.setTag(viewHolder);
            viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(TAG, "onCheckedChanged checked = " + isChecked + ", type=" + mType);
                    int getPosition = (Integer) buttonView.getTag();
                    if (mType == TYPE_MEMORIZE) {
                        arSrc.get(getPosition).mDisplay = buttonView.isChecked();
                        GroupHelper.updateDisplay(mContext, arSrc.get(getPosition).mId, buttonView.isChecked());
                    } else if (mType == TYPE_LIST) {
                        arSrc.get(getPosition).mDisplayInList = buttonView.isChecked();
                        GroupHelper.updateDisplayInList(mContext, arSrc.get(getPosition).mId, buttonView.isChecked());
                    }
                }
            });
            convertView.setTag(R.id.checkBox, viewHolder.checkBox);
        }

        view = convertView;
        ViewHolder holder = (ViewHolder) view.getTag();
        String strData = arSrc.get(position).mName;

        //TextView textView = (TextView)view;
        holder.checkBox.setText(strData);
        holder.checkBox.setTag(position);
        if (mType == TYPE_MEMORIZE) {
            holder.checkBox.setChecked(arSrc.get(position).mDisplay);
        } else if (mType == TYPE_LIST) {
            holder.checkBox.setChecked(arSrc.get(position).mDisplayInList);
        }
        /*if (mType == TYPE_MEMORIZE) {
            holder.checkBox.setChecked(arSrc.get(position).mDisplay);
        } else if (mType == TYPE_LIST) {
            holder.checkBox.setChecked(arSrc.get(position).mDisplayInList);
        }*/
        return view;
    }

    private static class ViewHolder {
        protected CheckBox checkBox;
    }
}

