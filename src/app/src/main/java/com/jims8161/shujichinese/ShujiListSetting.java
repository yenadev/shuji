package com.jims8161.shujichinese;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ShujiListSetting extends AppCompatActivity {
	private static final String TAG = "ShujiListSetting";
	CheckBox cbIncludeMemorized;

	TextView tvFirstGuide;
	Button btnSubmit;

	RadioButton radioDivision, radioAdded;
    ArrayList<GroupItem> mGroupItemList;
    GroupListAdapter mListAdapter;
	ListView listView;

	Context mContext;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shuji_list_setting);

		mContext = this;
		tvFirstGuide = (TextView) findViewById(R.id.textViewListSetFirstGuide);
		tvFirstGuide.setVisibility( PrefUtil.isFirstLaunch(mContext) ? View.VISIBLE : View.INVISIBLE );

		radioDivision = (RadioButton)findViewById(R.id.radioDivision);
		radioAdded = (RadioButton)findViewById(R.id.radioAdded);

		radioDivision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = radioDivision.isChecked();
                MainActivity.list_display_order = PrefUtil.LIST_DISP_DIVISION_ORDER;
                PrefUtil.setIntPref(mContext, PrefUtil.PREFS_SHUJI_KEY_INT_LIST_DISP_ORDER, PrefUtil.LIST_DISP_DIVISION_ORDER);
                Log.d(TAG, "radioSingle checked=" + isChecked);
                ShujiList.isDataChanged = true;
            }
        });

		radioAdded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = radioAdded.isChecked();
                MainActivity.list_display_order = PrefUtil.LIST_DISP_ADDED_ORDER;
                PrefUtil.setIntPref(mContext, PrefUtil.PREFS_SHUJI_KEY_INT_LIST_DISP_ORDER, PrefUtil.LIST_DISP_ADDED_ORDER);
                Log.d(TAG, "radioSingle checked=" + isChecked);
                ShujiList.isDataChanged = true;
            }
        });

		listView = (ListView) findViewById(R.id.listView);
        mGroupItemList = GroupHelper.getGroupList(mContext);
        mListAdapter = new GroupListAdapter(getApplicationContext(),
                R.layout.layout_list_grouplist_adapter, mGroupItemList, GroupListAdapter.TYPE_LIST);
        listView.setAdapter(mListAdapter);

        if(MainActivity.list_display_order == PrefUtil.LIST_DISP_DIVISION_ORDER)
            radioDivision.setChecked(true);
        else if(MainActivity.list_display_order == PrefUtil.LIST_DISP_ADDED_ORDER)
        	radioAdded.setChecked(true);
        

        cbIncludeMemorized = (CheckBox)findViewById(R.id.checkIncludeMemorized);
        cbIncludeMemorized.setChecked(MainActivity.bListIncludeMemzed);
        cbIncludeMemorized.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.bListIncludeMemzed = cbIncludeMemorized.isChecked();
                PrefUtil.setBoolean(mContext, PrefUtil.PREFS_SHUJI_KEY_BOOL_LIST_INCLUDE_MEMORIZED, MainActivity.bListIncludeMemzed);
                ShujiList.isDataChanged = true;
            }
        });

        btnSubmit = (Button)findViewById(R.id.buttonListSetSubmit);
                
        btnSubmit.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				if(PrefUtil.isFirstLaunch(mContext)) {
			        Intent intent = new Intent(getApplication(), ShujiList.class);
			        startActivity(intent);
					PrefUtil.setFirstLaunchFalse(mContext);
				}
				else {
					ShujiList.isDataChanged = true;
				}
				//saveListPrefs();
		        finish();		       
			}
        });

        updateGroupList();
	}

    private void updateGroupList() {
        mGroupItemList = GroupHelper.getGroupList(mContext);
        mListAdapter.notifyDataSetChanged();
    }

    /*private void saveListPrefs() {
    	MainActivity.bListIncludeMemzed = cbIncludeMemorized.isChecked();

        SharedPreferences mPref = getSharedPreferences(PrefUtil.PREF_FILE, 0);
        SharedPreferences.Editor mPrefEdit = mPref.edit();
        mPrefEdit.putBoolean(PrefUtil.PREFS_SHUJI_KEY_BOOL_LIST_INCLUDE_MEMORIZED, MainActivity.bListIncludeMemzed);
        mPrefEdit.putInt(PrefUtil.PREFS_SHUJI_KEY_INT_LIST_DISP_ORDER, MainActivity.list_display_order);
        mPrefEdit.commit();
    }*/

}
