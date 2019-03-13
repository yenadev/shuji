package com.jims8161.shujichinese;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.jims8161.shujichinese.exam.ShujiExamActivity;
import com.jims8161.shujichinese.exam.ShujiExamSetting;

import java.util.ArrayList;

public class FrontActivity extends AppCompatActivity {
	private static String TAG = "FrontActivity";
	ListView listView;
	ArrayList<String> al;
	ArrayAdapter<String> aa;

	static final int FRONT_RESULT_EXIT = 0;

	private final static String MENU_VERSION = "버전정보";
	private final static String MENU_VISIT_DEVELOPER = "개발 사이트 방문 - http://sparr250.cafe24.com/shuji";
	private final static String MENU_DOWN_WORDS = "단어 다운받기";
	private final static String MENU_EXIT = "종료";
	private final static String MENU_DAILY_EXAM_SETTING = "매일 단어 테스트 설정";
	//private final static String MENU_SREMINDER = "S 리마인더";
	private final static String MENU_EXAM = "단어 암기 시험";
	private final static String MENU_GROUP_MANAGER = "단어 그룹 관리";
	
	private final String strMenus[] = {MENU_GROUP_MANAGER, MENU_DOWN_WORDS, MENU_DAILY_EXAM_SETTING, MENU_EXAM, MENU_EXIT };
	
	Button btnMemorize;
	Button btnWordList;
	Button btnAddWord;

	TextView textViewVersion;

	Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_front);

		listView = (ListView) findViewById(R.id.listViewShujiFront);

		al = new ArrayList<>();
		aa = new ArrayAdapter<>(getApplicationContext(),
				R.layout.layout_frontlist_adapter, al);

		listView.setAdapter(aa);
		listView.setOnItemClickListener(mListItemClickListener);

		if(!MainActivity.bTurnOffAd) {
		    //AdView adView = new AdView(this, AdSize.BANNER, ShujiConstants.ADV_ID);
			AdView adView = new AdView(this);
			adView.setAdSize(AdSize.BANNER);
			adView.setAdUnitId(ShujiConstants.ADV_ID);
		    LinearLayout adLayout = (LinearLayout)findViewById(R.id.layoutFrontAdv);
		    adLayout.setOrientation(LinearLayout.VERTICAL);
		    adLayout.addView(adView);
		    //adView.loadAd(new AdRequest());
			AdRequest adRequest = new AdRequest.Builder().build();
			adView.loadAd(adRequest);
		}
		
        btnMemorize = (Button) findViewById(R.id.buttonShujiFrontMemorize);
        btnWordList = (Button) findViewById(R.id.buttonShujiFrontShowList);
        btnAddWord = (Button) findViewById(R.id.buttonShujiFrontAdd);
        
        btnMemorize.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplication(),
						ShujiMemorizeSetting.class);
				startActivity(intent);	       
			}
        });
        
        btnWordList.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent;
				if(!PrefUtil.isFirstLaunch(mContext)) {
					intent = new Intent(getApplication(), ShujiList.class);
					startActivity(intent);
				}
				else {
					intent = new Intent(getApplication(), ShujiListSetting.class);
					startActivity(intent);
				}       
			}
        });
        
        btnAddWord.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplication(), ShujiAdd.class);
				intent.putExtra("isNewWord", "1");
				startActivity(intent);	       
			}
        });
		textViewVersion = (TextView) findViewById(R.id.textViewVersion);
		textViewVersion.setText(MainActivity.SHUJI_VERSION_NAME);
		updateMenuList();
		mContext = this;
		ShujiExamActivity.bOnExam = false;
	}

	public void updateMenuList() {
		aa.clear();
	
		for(int i=0; i<strMenus.length; i++) {
			if(strMenus[i].equals(MENU_VERSION)) {
				al.add(strMenus[i] + " : " + MainActivity.SHUJI_VERSION_NAME + "\n데이터베이스 버전 : "
						+ ShujiDatabaseHelper.DB_VERSION);
				continue;
			}
			al.add(strMenus[i]);
		}

		aa.notifyDataSetChanged();
	}

	AdapterView.OnItemClickListener mListItemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent;
			String strMenu = al.get(position);

			if (strMenu.contains(MENU_EXIT)) {
				finish();
			} else if (strMenu.contains(MENU_VERSION)) {
				// build info. do nothing
			} else if (strMenu.contains(MENU_VISIT_DEVELOPER)) {
				// visit developer site
				String url = "http://sparr250.cafe24.com/shuji";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			} else if (strMenu.contains(MENU_DOWN_WORDS)) {
				intent = new Intent(getApplication(), DownloadWordActivity.class);
				startActivity(intent);
			} else if (strMenu.contains(MENU_DAILY_EXAM_SETTING)) {
				intent = new Intent(getApplication(), ShujiExamSetting.class);
				startActivity(intent);
			} else if (strMenu.contains(MENU_EXAM)) {
	        	int problemCnt = ShujiExamActivity.makeProblem(mContext, 5);
	        	
	        	if (problemCnt <= 0) {
	        		Log.e(TAG, "Exam alarm triggered ID : deficient in word !!!");
	        		Toast.makeText(mContext, "단어수가 부족합니다", Toast.LENGTH_SHORT).show();
	        		return;
	        	}
				intent = new Intent(getApplication(), ShujiExamActivity.class);
				startActivity(intent);
			} else if (strMenu.contains(MENU_GROUP_MANAGER)) {
				intent = new Intent(getApplication(), GroupManageActivity.class);
				startActivity(intent);
			} else {
				return;
			}
		}
	};

	@Override
	public void onDestroy() {
		setResult(FRONT_RESULT_EXIT);
		super.onDestroy();
	}
}
