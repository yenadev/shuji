package com.jims8161.shujichinese;

import com.jims8161.shujichinese.exam.ShujiExamSetting;

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

import java.util.ArrayList;


public class ShujiMemorizeSetting extends AppCompatActivity {
    private static final String TAG = "MemorizeSetting";
	CheckBox cbIncludeMemorized;
	CheckBox cbUseTTS;
	CheckBox cbRandom;
	CheckBox cbPinyin;

	Button btnStart;
	Button btnExit;

	RadioButton radioMeanFirst, radioChnFirst;

	String launchedBy = null;

	ArrayList<GroupItem> mGroupItemList;
	GroupListAdapter mListAdapter;
	ListView listView;

	Context mContext;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shuji_memorize_setting);
        mContext = this;

		launchedBy = getIntent().getStringExtra("from");

        radioMeanFirst = (RadioButton)findViewById(R.id.radioMeanFirst);
        radioChnFirst = (RadioButton)findViewById(R.id.radioChnFirst);

        radioMeanFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = radioMeanFirst.isChecked();
                MainActivity.memorize_display_order = PrefUtil.MEMORIZE_DISP_MEAN_FIRST;
                PrefUtil.setIntPref(mContext, PrefUtil.PREFS_SHUJI_KEY_INT_MEM_DISP_ORDER, PrefUtil.MEMORIZE_DISP_MEAN_FIRST);
                Log.d(TAG, "radioSingle checked=" + isChecked);
            }
        });
        radioChnFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = radioChnFirst.isChecked();
                MainActivity.memorize_display_order = PrefUtil.MEMORIZE_DISP_CHN_FIRST;
                PrefUtil.setIntPref(mContext, PrefUtil.PREFS_SHUJI_KEY_INT_MEM_DISP_ORDER, PrefUtil.MEMORIZE_DISP_CHN_FIRST);
                Log.d(TAG, "radioSingle checked=" + isChecked);
            }
        });

        listView = (ListView) findViewById(R.id.listView);
        mGroupItemList = GroupHelper.getGroupList(mContext);
        mListAdapter = new GroupListAdapter(getApplicationContext(),
                R.layout.layout_list_grouplist_adapter, mGroupItemList, GroupListAdapter.TYPE_MEMORIZE);
        listView.setAdapter(mListAdapter);

		if(MainActivity.memorize_display_order == PrefUtil.MEMORIZE_DISP_MEAN_FIRST)
            radioMeanFirst.setChecked(true);
		else if(MainActivity.memorize_display_order == PrefUtil.MEMORIZE_DISP_CHN_FIRST)
            radioChnFirst.setChecked(true);

		cbIncludeMemorized = (CheckBox)findViewById(R.id.cbIncludeMemorized);
		cbIncludeMemorized.setChecked(MainActivity.bIncludeMemzed);
        cbIncludeMemorized.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.bIncludeMemzed = cbIncludeMemorized.isChecked();
                PrefUtil.setBoolean(mContext, PrefUtil.PREFS_SHUJI_KEY_BOOL_INCLUDE_MEMORIZED, MainActivity.bIncludeMemzed);
            }
        });

		cbUseTTS = (CheckBox)findViewById(R.id.cbUseTTS);
		cbUseTTS.setChecked(MainActivity.bUseTTS);
        cbUseTTS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.bUseTTS = cbUseTTS.isChecked();
                PrefUtil.setBoolean(mContext, PrefUtil.PREFS_SHUJI_KEY_BOOL_USE_TTS, MainActivity.bUseTTS);
            }
        });

		cbRandom = (CheckBox)findViewById(R.id.cbRandom);
		cbRandom.setChecked(MainActivity.bRandom);
        cbRandom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.bRandom = cbRandom.isChecked();
                PrefUtil.setBoolean(mContext, PrefUtil.PREFS_SHUJI_KEY_BOOL_RANDOM, MainActivity.bRandom);
            }
        });

		cbPinyin = (CheckBox)findViewById(R.id.cbPinyin);
		cbPinyin.setChecked(MainActivity.bHidePinyin);
        cbPinyin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.bHidePinyin = cbPinyin.isChecked();
                PrefUtil.setBoolean(mContext, PrefUtil.PREFS_SHUJI_KEY_BOOL_HIDE_PINYIN, MainActivity.bHidePinyin);
            }
        });

		btnStart = (Button)findViewById(R.id.buttonMemSetStart);
		btnExit = (Button)findViewById(R.id.buttonMemSetExit);

		if(launchedBy == null){
			btnStart.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					//savePrefs();
					Intent intent = new Intent(getApplication(), ShujiMemorize.class);
					startActivity(intent);
					finish();
				}

			});
		} else if (launchedBy.equals(ShujiExamSetting.class.getName())) {
			btnStart.setText("저장");
			btnStart.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					//savePrefs();
					finish();
				}

			});
			//rgDispOrder.setVisibility(View.INVISIBLE);
            radioMeanFirst.setVisibility(View.INVISIBLE);
            radioChnFirst.setVisibility(View.INVISIBLE);
			cbUseTTS.setVisibility(View.INVISIBLE);
			cbRandom.setVisibility(View.INVISIBLE);
			cbPinyin.setVisibility(View.INVISIBLE);
		}

		btnExit.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}
		});

        updateGroupList();
	}

    private void updateGroupList() {
        mGroupItemList = GroupHelper.getGroupList(mContext);
        mListAdapter.notifyDataSetChanged();
    }

	/*public void savePrefs() {

		if(bDataChanged == false) {
			return;
		}

		MainActivity.bIncludeMemzed = cbIncludeMemorized.isChecked();
		MainActivity.bUseTTS = cbUseTTS.isChecked();
		MainActivity.bRandom = cbRandom.isChecked();
		MainActivity.bHidePinyin = cbPinyin.isChecked();

		SharedPreferences pref = getSharedPreferences(PrefUtil.PREF_FILE, 0);
		SharedPreferences.Editor editor = pref.edit();

		editor.putBoolean(PrefUtil.PREFS_SHUJI_KEY_BOOL_INCLUDE_MEMORIZED, MainActivity.bIncludeMemzed);
		editor.putBoolean(PrefUtil.PREFS_SHUJI_KEY_BOOL_USE_TTS, MainActivity.bUseTTS);
		editor.putBoolean(PrefUtil.PREFS_SHUJI_KEY_BOOL_RANDOM, MainActivity.bRandom);
		editor.putBoolean(PrefUtil.PREFS_SHUJI_KEY_BOOL_HIDE_PINYIN, MainActivity.bHidePinyin);
		editor.putInt(PrefUtil.PREFS_SHUJI_KEY_INT_MEM_DISP_ORDER, MainActivity.memorize_display_order);
		editor.commit();
		bDataChanged = false;
	}*/

	@Override
	public void onDestroy() {
		//savePrefs();
		super.onDestroy();
	}
}
