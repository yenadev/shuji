package com.jims8161.shujichinese;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class ShujiMemorize extends AppCompatActivity {
    private static final String TAG = "ShujiMemorize";
	public static final int DISPLAY_INTERVAL = 3000;
	public static final int DISPLAY_CHN_INTERVAL = 3000;
	public static final int DISPLAY_MEANING_INTERVAL = 3000;

	Button btnStop;
	Button btnExit;
	Button btnNext;

	boolean bPlaying = false;

	private DisplayTimerHandler mDisplayTimerHandler;
	private int display_status = 0;

    ArrayList<MemorizeItem> mArrMemorizeItem;

	private static int word_count = 0;
	private static int cur_word = 0;
	private int word_order[];
    private int mInterval = DISPLAY_INTERVAL;
    private boolean mDragged = false;

	private AdView adView = null;
	private LinearLayout adLayout;
	private AdverTimerHandler mAdverTimerHandler;

    private MemorizeViewPager mViewPager;
    private MemorizeViewPagerAdapter mAdapter;
    private Context mContext;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shuji_memorize);

        mContext = this;
        mViewPager = (MemorizeViewPager) findViewById(R.id.pager);

        mDisplayTimerHandler = new DisplayTimerHandler(this);

        mArrMemorizeItem = new ArrayList<>();


		btnStop = (Button)findViewById(R.id.buttonMemStop);
		btnExit = (Button)findViewById(R.id.buttonMemExit);
		btnNext = (Button)findViewById(R.id.buttonMemNext);

		btnStop.setOnClickListener(new OnClickListener () {

			public void onClick(View v) {
				if(bPlaying) {
					mDisplayTimerHandler.removeMessages(0);
					bPlaying = false;
					btnStop.setText("계속");
				}
				else{
					bPlaying = true;
					display_status = 0;
					btnStop.setText("정지");
					mDisplayTimerHandler.removeMessages(0);
					mDisplayTimerHandler.sendEmptyMessageDelayed(0, DISPLAY_INTERVAL);
				}
			}
		});

		btnExit.setOnClickListener(new OnClickListener () {

			public void onClick(View v) {
				finish();
			}
		});

		btnNext.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                mDisplayTimerHandler.removeMessages(0);
                mDisplayTimerHandler.sendEmptyMessageDelayed(0, 0);
            }
        });

		if(MainActivity.bTurnOffAd == false) {
			adView = new AdView(this);
			adView.setAdSize(AdSize.BANNER);
			adView.setAdUnitId(ShujiConstants.ADV_ID);
			adLayout = (LinearLayout)findViewById(R.id.layoutMedAdv);
			adLayout.setOrientation(LinearLayout.VERTICAL);
			adLayout.addView(adView);
			AdRequest adRequest = new AdRequest.Builder().build();
			adView.loadAd(adRequest);
		}

		mAdverTimerHandler = new AdverTimerHandler(this);

		if(!MainActivity.bTurnOffAd) {
			mAdverTimerHandler.sendEmptyMessageDelayed(0, 1000 * 32);	// 광고는 30초뒤에 사라지도록함
		}

		prepare_word_list();

		if(word_count == 0) {
			Toast.makeText(getApplicationContext(), "해당되는 단어의 수가 0개 입니다.", Toast.LENGTH_LONG).show();
			finish();
            return;
		}

        mAdapter = new MemorizeViewPagerAdapter(mContext, getLayoutInflater());
        mAdapter.setItemList(mArrMemorizeItem);
        mAdapter.setItemOrderList(word_order);
        mViewPager.setAdapter(mAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Log.d(TAG, "onPageScrolled : position=" + position);
            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected : position=" + position);
                if (mDragged) {
                    cur_word = position;
                    display_status = 0;
                    mDragged = false;
                    mDisplayTimerHandler.removeMessages(0);
                    if (bPlaying)
                        mDisplayTimerHandler.sendEmptyMessageDelayed(0, 0);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d(TAG, "onPageScrollStateChanged : state=" + state);
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    mDragged = true;
                    mDisplayTimerHandler.removeMessages(0);
                } else if (mDragged && state == ViewPager.SCROLL_STATE_IDLE) {
                    mDragged = false;
                    if (bPlaying)
                        mDisplayTimerHandler.sendEmptyMessageDelayed(0, mInterval);
                }
            }
        });

		cur_word = 0;
		display_status = 0;
		bPlaying = true;
        mDragged = false;
		mDisplayTimerHandler.removeMessages(0);
		mDisplayTimerHandler.sendEmptyMessageDelayed(0, 10);
	}

    private static class AdverTimerHandler extends Handler {
        private ShujiMemorize mContext;
        public AdverTimerHandler(ShujiMemorize context) {
            mContext = context;
        }
        @Override
        public void handleMessage(Message msg) {
            if (mContext.adView != null)
                mContext.adView.setVisibility(View.GONE);
        }
    }

    private static class DisplayTimerHandler extends Handler {
        private ShujiMemorize mContext;
        public DisplayTimerHandler(ShujiMemorize context) {
            mContext = context;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {

                mContext.mInterval = DISPLAY_INTERVAL;

                if(mContext.display_status == 0) {
                    mContext.mInterval = mContext.display_word();
                    mContext.display_status = 1;
                }
                else {
                    mContext.mInterval = mContext.display_word();
                    mContext.display_status = 0;
                    cur_word++;
                    if(cur_word >= word_count) {
                        cur_word = 0;
                        mContext.shuffle_word();
                    }
                }

                if(mContext.bPlaying) {
                    this.sendEmptyMessageDelayed(0, mContext.mInterval);
                }
            }
        }
    }

	public int display_word() {
        mViewPager.setCurrentItem(cur_word);
        return mAdapter.updateView(display_status, cur_word);
	}

	public void prepare_word_list() {
        mArrMemorizeItem.clear();

		String[] projection = {TableWordData.Columns.HANZI,
                TableWordData.Columns.MEANING,
                TableWordData.Columns.PINYIN,
                TableWordData.Columns.REAL_PINYIN,
				TableWordData.Columns.WORDGROUP, // 4
                TableWordData.Columns.EXCLUDE,
                TableWordData.Columns._ID};

		String selection = "(";
		boolean bFirst = true;

		ArrayList<GroupItem> groupList = GroupHelper.getGroupList(mContext);
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

		if(!PrefUtil.getBoolean(mContext, PrefUtil.PREFS_SHUJI_KEY_BOOL_INCLUDE_MEMORIZED, false))
			selection = selection + " AND " + TableWordData.Columns.EXCLUDE + "=0";

		SQLiteDatabase db = new ShujiDatabaseHelper(getApplicationContext()).getWritableDatabase();
		Cursor cursor = db.query(TableWordData.TABLE_NAME, projection, selection, null, null, null,
                (MainActivity.list_display_order == PrefUtil.LIST_DISP_DIVISION_ORDER) ? TableWordData.Columns.WORDGROUP : null);

		int wordCount = 0;

        MemorizeItem item;
        int id;
        boolean memorized;
        String chn, mean, pinyin, realPinyin;
		if (cursor != null) {
			try{
				if(cursor.moveToFirst()) {
					while(!cursor.isAfterLast()) {

                        id = cursor.getInt(6);
                        memorized = cursor.getInt(5) != 0;
                        chn = cursor.getString(0);
                        mean = cursor.getString(1);
                        pinyin = cursor.getString(2);
                        realPinyin = cursor.getString(3);
                        item = new MemorizeItem(id, chn, pinyin, mean, realPinyin, memorized);
						wordCount++;
						cursor.moveToNext();
                        mArrMemorizeItem.add(item);
					}
				}
			} catch (NullPointerException e) {
				Toast.makeText(getApplicationContext(), "null", Toast.LENGTH_SHORT).show();
			}
			cursor.close();
		}
		word_count = wordCount;

		if(word_count == 0)
			return;

		word_order = new int[word_count];
		for(int i=0; i<word_count; i++)
			word_order[i] = i;
		db.close();
		shuffle_word();
	}

	private void shuffle_word() {
		if(MainActivity.bRandom) {
			int tempOrder, change;
			for(int i=0; i<word_count; i++) {
				tempOrder = (int)(Math.random() * word_count);

				change = word_order[i];
				word_order[i] = word_order[tempOrder];
				word_order[tempOrder] = change;
			}
		}
	}

	@Override
	public void onDestroy() {
		bPlaying = false;
        if (mDisplayTimerHandler != null) {
            mDisplayTimerHandler.removeMessages(0);
            mDisplayTimerHandler = null;
        }
        if (mAdverTimerHandler != null) {
            mAdverTimerHandler.removeMessages(0);
            mAdverTimerHandler = null;
        }
        if (mArrMemorizeItem != null)
            mArrMemorizeItem.clear();
		super.onDestroy();
	}

}
