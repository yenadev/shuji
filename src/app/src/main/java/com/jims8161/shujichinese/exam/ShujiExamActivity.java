package com.jims8161.shujichinese.exam;

import java.util.ArrayList;
import java.util.Random;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.jims8161.shujichinese.GroupHelper;
import com.jims8161.shujichinese.GroupItem;
import com.jims8161.shujichinese.MainActivity;
import com.jims8161.shujichinese.PrefUtil;
import com.jims8161.shujichinese.R;
import com.jims8161.shujichinese.ShujiConstants;
import com.jims8161.shujichinese.ShujiDatabaseHelper;
import com.jims8161.shujichinese.TableWordData;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


public class ShujiExamActivity extends AppCompatActivity {
	private static final String TAG = "ShujiExamActivity";
	ListView listView;
	ProblemListAdapter adapterProblemList;

	public static ArrayList <ProblemItem> problemList = null;

	private static ArrayList <View> problemViews = null;

	public static boolean bOnExam = false;

	Button btnSubmit;

	Context mContext;

	TextView txtInfo;

	private boolean bScoreMode = false;
	private int mScore = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shuji_exam);

		mContext = this;

		if (problemList == null)
			problemList = new ArrayList<ProblemItem>();
		adapterProblemList = new ProblemListAdapter(this, R.layout.layout_dailyexam_problem_list_adapter, problemList);

		listView = (ListView) findViewById(R.id.listViewProblemList);
		listView.setAdapter(adapterProblemList);

		bScoreMode = false;

		txtInfo = (TextView) findViewById(R.id.textViewDailyExamInfo);
		txtInfo.setVisibility(View.GONE);
		btnSubmit = (Button) findViewById(R.id.buttonDailyExamSubmit);
		btnSubmit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(bScoreMode == false) {
					bScoreMode = true;
					txtInfo.setVisibility(View.VISIBLE);
					calcResult();
					updateList();
				} else {
					bOnExam = false;
					finish();
				}
			}
		});

		if (MainActivity.bTurnOffAd == false) {
			AdView adView = new AdView(this);
			adView.setAdSize(AdSize.BANNER);
			adView.setAdUnitId(ShujiConstants.ADV_ID);
			LinearLayout adLayout = (LinearLayout)findViewById(R.id.layoutAdv);
			adLayout.setOrientation(LinearLayout.VERTICAL);
			adLayout.addView(adView);
			AdRequest adRequest = new AdRequest.Builder().build();
			adView.loadAd(adRequest);
		}

		if(problemList == null || problemList.size() <= 0) {
			Toast.makeText(mContext, "단어수가 부족합니다", Toast.LENGTH_SHORT).show();
			bOnExam = false;
			finish();
		} else {
			bOnExam = true;
			createProblemViews();
			updateList();
		}
	}

	private void createProblemViews() {
		if (problemList == null || problemList.size() <= 0)
			return;

		if (problemViews == null) {
			problemViews = new ArrayList<View>();
		}
		problemViews.removeAll(problemViews);

		int problemCnt = problemList.size();

		for (int i=0; i<problemCnt; i++) {
			View v = createProblemView(i);
			problemViews.add(v);
		}
	}

	private View createProblemView(final int index) {
		LayoutInflater Inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);;
		View convertView = Inflater.inflate(R.layout.layout_dailyexam_problem_list_adapter, listView, false);
		final ViewHolder viewHolder = new ViewHolder();
		viewHolder.rgAnswer = (RadioGroup)convertView.findViewById(R.id.radioGroupAnswer);
		viewHolder.tvWord = (TextView)convertView.findViewById(R.id.textViewProblemWord);
		viewHolder.radioAns1 = (RadioButton)convertView.findViewById(R.id.radioAnswer1);
		viewHolder.radioAns2 = (RadioButton)convertView.findViewById(R.id.radioAnswer2);
		viewHolder.radioAns3 = (RadioButton)convertView.findViewById(R.id.radioAnswer3);
		viewHolder.radioAns4 = (RadioButton)convertView.findViewById(R.id.radioAnswer4);

		ProblemItem item = problemList.get(index);
		viewHolder.tvWord.setText("" + (index+1) +". " + item.mWord);

		viewHolder.radioAns1.setText(item.mAns1);
		viewHolder.radioAns2.setText(item.mAns2);
		viewHolder.radioAns3.setText(item.mAns3);
		viewHolder.radioAns4.setText(item.mAns4);

		viewHolder.rgAnswer.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

			public void onCheckedChanged(RadioGroup rg, int id) {
				if(problemList.size() > index) {
					int ans = -1;
					if(id == R.id.radioAnswer1)
						ans = 0;
					else if(id == R.id.radioAnswer2)
						ans = 1;
					else if(id == R.id.radioAnswer3)
						ans = 2;
					else if(id == R.id.radioAnswer4)
						ans = 3;
					problemList.get(index).nSelected = ans;
					Log.d(TAG, "index:" + index + ", ans:" + ans);
				}
			}
		});
		convertView.setTag(viewHolder);
		return convertView;
	}

	private void calcResult() {

		if (problemList == null) return;

		String msg;

		mScore = 0;

		for(int i=0; i<problemList.size(); i++) {
			if(problemList.get(i).correct())
				mScore ++;
		}


		msg = "점수 : " + mScore + " / " + problemList.size();
		txtInfo.setText(msg);
		btnSubmit.setText("종료");

	}

	static class ViewHolder {
		protected TextView tvWord;
		protected RadioGroup rgAnswer;
		protected RadioButton radioAns1;
		protected RadioButton radioAns2;
		protected RadioButton radioAns3;
		protected RadioButton radioAns4;
	}

	class ProblemListAdapter extends BaseAdapter {
		Context mainContext;
		LayoutInflater Inflater;
		ArrayList<ProblemItem> arSrc;
		int layout;


		public ProblemListAdapter(Context context, int alayout, ArrayList<ProblemItem> aarSrc) {
			mainContext = context;
			Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			arSrc = aarSrc;
			layout = alayout;
		}

		public int getCount() {
			return arSrc.size();
		}

		public ProblemItem getItem(int position) {
			return arSrc.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = null;

			if(arSrc.size() == 0)
				return null;

			convertView = problemViews.get(position);
			view = convertView;

			ProblemItem item = arSrc.get(position);

			ViewHolder holder = (ViewHolder) view.getTag();

			int selected = item.nSelected;
			Log.d(TAG, "..position:" + position + ", nSelected:" + selected);

			if(bScoreMode == true){
				RadioButton rdo = null;
				String str = null;
				if(item.nAnswer == 0) {
					rdo = holder.radioAns1;
					str = item.mAns1;
				} else if(item.nAnswer == 1) {
					rdo = holder.radioAns2;
					str = item.mAns2;
				} else if(item.nAnswer == 2) {
					rdo = holder.radioAns3;
					str = item.mAns3;
				} else if(item.nAnswer == 3) {
					rdo = holder.radioAns4;
					str = item.mAns4;
				}

				if (rdo != null && str != null) {
					str = str + " - 정답";
					rdo.setText(str);
					if (item.correct())
						rdo.setTextColor(Color.BLUE);
					else
						rdo.setTextColor(Color.RED);
				}
			}

			return view;
		}
	}

	static class ProblemItem {
		ProblemItem(int no, String strWord, String strAns1, String strAns2, String strAns3, String strAns4,
					int answer) {
			_no = no;
			mWord = strWord;
			mAns1 = strAns1;
			mAns2 = strAns2;
			mAns3 = strAns3;
			mAns4 = strAns4;
			nAnswer = answer;
			nSelected = -1;
		}

		int _no;
		String mWord;
		String mAns1, mAns2, mAns3, mAns4;
		int nAnswer;
		int nSelected;

		boolean correct() {
			return (nAnswer == nSelected);
		}
	}

	private void updateList() {
		adapterProblemList.notifyDataSetChanged();
	}

	public static int makeProblem(Context context, int problemCnt) {

		//boolean bDispDivision[];
		boolean bIncludeMemzed = false;
		int list_display_order = 0;

		//bDispDivision = new boolean[ShujiConstants.MAX_DIVISION];

		SharedPreferences mPref = context.getSharedPreferences(PrefUtil.PREF_FILE, 0);

		bIncludeMemzed = mPref.getBoolean(
				PrefUtil.PREFS_SHUJI_KEY_BOOL_INCLUDE_MEMORIZED, false);
		list_display_order = mPref.getInt(
				PrefUtil.PREFS_SHUJI_KEY_INT_LIST_DISP_ORDER,
				PrefUtil.LIST_DISP_DIVISION_ORDER);

		/*for (int i = 0; i < ShujiConstants.MAX_DIVISION; i++) {
			bDispDivision[i] = mPref.getBoolean(
					PrefUtil.PREFS_SHUJI_KEY_BOOL_DISP_DIVISION + i,
					true);
		}*/


		if (problemList == null)
			problemList = new ArrayList<ProblemItem>();
		problemList.clear();

		String[] projection = {TableWordData.Columns.HANZI,
                TableWordData.Columns.MEANING,
                TableWordData.Columns.PINYIN,
                TableWordData.Columns.REAL_PINYIN,
				TableWordData.Columns.WORDGROUP, // 4
                TableWordData.Columns.EXCLUDE,
                TableWordData.Columns._ID};
		String selection = "(";
		boolean bFirst = true;

		ArrayList<GroupItem> groupList = GroupHelper.getGroupList(context);
		for (GroupItem item : groupList) {
			if(item.mDisplay) {
				if(bFirst) {
					bFirst = false;
				}
				else {
					selection = selection + " OR ";
				}
				selection = selection + TableWordData.Columns.WORDGROUP + "='" + item.mName + "'";
			}
		}
		selection = selection + ")";

		/*for(int i=0; i<ShujiConstants.MAX_DIVISION; i++) {
			if(bDispDivision[i]) {
				if(bFirst) {
					bFirst = false;
				}
				else {
					selection = selection + " OR ";
				}
				selection = selection + TableWordData.Columns.DIVISION + "=" + i;
			}
		}
		selection = selection + ")";*/

		if(!bIncludeMemzed)
			selection = selection + " AND " + TableWordData.Columns.EXCLUDE + "=0";

		SQLiteDatabase db = new ShujiDatabaseHelper(context).getWritableDatabase();
		Cursor cursor = db.query(TableWordData.TABLE_NAME, projection, selection, null, null, null,
				(list_display_order == PrefUtil.LIST_DISP_DIVISION_ORDER) ? TableWordData.Columns.WORDGROUP : null);


		int max = problemCnt;

		int total = 0;
		if(cursor != null)
			total = cursor.getCount();

		if(total < problemCnt * 2)
			return (-total);

		if (total < problemCnt) max = total;
		if (total == 0) {
			db.close();
			cursor.close();
			return 0;
		}

		ProblemItem item;

		//cursor.moveToPosition(position)
		int wordlist[] = new int[total];
		int s, i, t, j;
		Random r = new Random();
		for(i=0; i<total; i++) {
			wordlist[i] = i;
		}
		for(i=0; i<total; i++) {
			s = r.nextInt(total);
			t = wordlist[s];
			wordlist[s] = wordlist[i];
			wordlist[i] = t;
		}

		String strWord, strAns1, strAns2, strAns3, strAns4;
		String strAns[] = new String[4];

		int answer;
		int wrongAns[] = new int[4];

		try{
			if(cursor.moveToFirst()) {
				for(i=0; i<max; i++) {
					cursor.moveToPosition(wordlist[i]);

					answer = r.nextInt(4);
					strWord = cursor.getString(0);
					strAns[answer] = cursor.getString(1);

					for(j=0; j<4; j++)
						wrongAns[j] = -1;


					// 오답리스트 만들기
					for(j=0; j<4; j++) {
						if(j == answer) continue;	// 정답 자리는 건너뜀

						do {
							s = r.nextInt(total);

							if (s == wordlist[i]) continue; // 정답과 같은 번호라면 건너뜀

							// 다른 오답과 중복이면 건너뜀
							if (s == wrongAns[0] || s == wrongAns[1] || s == wrongAns[2] || s == wrongAns[3])
								continue;

							break;
						}while(true);

						wrongAns[j] = s;
					}

					for(j=0; j<4; j++) {
						if(j == answer) continue;	// 정답 자리는 건너뜀
						cursor.moveToPosition(wrongAns[j]);
						strAns[j] = cursor.getString(1);
					}

					//model.addWord(cursor.getString(0), cursor.getString(2), cursor.getString(1), i);

					item = new ProblemItem(i, strWord, strAns[0], strAns[1], strAns[2], strAns[3], answer);
					problemList.add(item);

				}
			}

		} catch (NullPointerException e) {
			Toast.makeText(context, "null", Toast.LENGTH_SHORT).show();
		}
		cursor.close();
		db.close();

		return problemList.size();
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(mContext).setTitle("종료").setMessage("종료 하시겠습니까?")
				.setPositiveButton("예", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						bOnExam = false;
						finish();
					}
				})
				.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				})
				.show();
	}

	@Override
	public void onDestroy() {
		bOnExam = false;
		super.onDestroy();
	}
}
