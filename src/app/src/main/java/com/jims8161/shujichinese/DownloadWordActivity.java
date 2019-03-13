package com.jims8161.shujichinese;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadWordActivity extends AppCompatActivity {
	private static final String TAG = "DownloadWordActivity";

	Context mContext;

	Button buttonChangeGroup;
    TextView textGroup;
	ListView listView;
	DownWordListAdapter downWordListAdapter;
	ArrayList<DownMenu> downMenuList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shuji_downloadwords);

		mContext = this;

        buttonChangeGroup = (Button) findViewById(R.id.buttonChangeGroup);
        buttonChangeGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeGroup();
            }
        });
        textGroup = (TextView) findViewById(R.id.textGroup);
        textGroup.setText(GroupHelper.DEFAULT_GROUP);

		listView = (ListView) findViewById(R.id.listView);

		downMenuList = new ArrayList<DownMenu>();
		downWordListAdapter = new DownWordListAdapter(this, R.layout.layout_downwordlist_adapter, downMenuList);

		listView.setAdapter(downWordListAdapter);
		listView.setOnItemClickListener(mListItemClickListener);

		new DownReqAsyncTask().execute(ShujiConstants.URL_WORD_LIST);
	}

    private void changeGroup() {
        GroupHelper.showSelectGroupPopup(this, new GroupHelper.SelectGroupListener() {

            @Override
            public void onSelected(Context context, int id, String groupName) {
                textGroup.setText(groupName);
            }

            @Override
            public void onCanceled(Context context) {

            }
        });
    }

	AdapterView.OnItemClickListener mListItemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			final DownMenu menu = downMenuList.get((int)id);
			if(menu == null) return;

			new AlertDialog.Builder(DownloadWordActivity.this).setTitle("단어 다운받기").setMessage(
                    menu.strTitle + "을(를) " + (textGroup.getText().toString()) + "그룹에 다운 받으시겠습니까?")
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            new WordReqAsyncTask().execute(menu.strUrl);
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					})
					.show();
		}
	};

	public class WordItem {
		String strHanzi;
		String strMeaning;
		String strPinyin;
		WordItem(String hanzi, String mean) {
			strHanzi = hanzi;
			strMeaning = mean;
			strPinyin = "";
		}
		WordItem(String hanzi, String mean, String pinyin) {
			strHanzi = hanzi;
			strMeaning = mean;
			strPinyin = pinyin;
		}
	}

	public class DownMenu {
		String strTitle;
		String strComment;
		int count;
		String strUrl;

		DownMenu(String title, String cmt, int cnt, String url) {
			strTitle = title;
			strComment = cmt;
			count = cnt;
			strUrl = url;
		}
	}

	// 단어들을 다운받아 저장
	public class WordReqAsyncTask extends AsyncTask<String, Void, ArrayList<WordItem>> {
		String url;
        String groupName;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
            groupName = textGroup.getText().toString();
		}


		@Override
		protected ArrayList<WordItem> doInBackground(String... arg0) {

			ArrayList<WordItem> wordList = null;
			url = arg0[0];

			try {
				wordList = requestWord(url);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return wordList;
		}

		@Override
		protected void onPostExecute(ArrayList<WordItem> result) {
			super.onPostExecute(result);
			SQLiteDatabase db = new ShujiDatabaseHelper(getApplicationContext()).getWritableDatabase();
			HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
			outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
			outputFormat.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
			outputFormat.setToneType(HanyuPinyinToneType.WITH_TONE_MARK);
			String strPinyin = "";

			for(WordItem item : result) {
				if(item.strHanzi.length() <= 0) continue;

				if(item.strPinyin != null && item.strPinyin.length() > 0) {
					strPinyin = item.strPinyin;
				}
				else {
					try	{

						strPinyin = "";
						for(int i=0; i<item.strHanzi.length(); i++) {
							String[] strOut = PinyinHelper.toHanyuPinyinStringArray(item.strHanzi.charAt(i), outputFormat);
							if(strOut != null)
								strPinyin += strOut[0];
						}
					} catch (BadHanyuPinyinOutputFormatCombination e) {
						e.printStackTrace();
					}
				}

				ContentValues values = new ContentValues();
				values.put(TableWordData.Columns.HANZI, item.strHanzi);
				values.put(TableWordData.Columns.MEANING, item.strMeaning);
				values.put(TableWordData.Columns.PINYIN, strPinyin);
				values.put(TableWordData.Columns.REAL_PINYIN, item.strHanzi);	// TODO : change real pinyin
				values.put(TableWordData.Columns.EXCLUDE, 0);
                values.put(TableWordData.Columns.WORDGROUP, groupName);

				if(-1 != db.insert(TableWordData.TABLE_NAME, null, values)) {
					Log.d(TAG, "Saved - " + item.strHanzi);
				}
			}
			db.close();

			if(result.size() == 0)
				Toast.makeText(getApplicationContext(), "단어 추가 실패", Toast.LENGTH_LONG).show();
			else
				Toast.makeText(getApplicationContext(), "단어 " + result.size() + "개 " + groupName + " 그룹에 추가 완료", Toast.LENGTH_LONG).show();
		}

		private ArrayList<WordItem> requestWord(String url) throws IOException {

            int resID = getResources().getIdentifier(url , "raw", getPackageName());

			ArrayList<WordItem> wordList = null;
			String strResult = null;
			InputStream in = null;

            String xmlData = Util.loadXML(mContext, resID);
            strResult = xmlData.replaceAll("^.*<", "<");
            wordList = parseWordXml(strResult);
			return wordList;
		}

		private ArrayList<WordItem> parseWordXml(String data) {

			ArrayList<WordItem> wordList = new ArrayList<WordItem>();

			try {
				XmlPullParser parser = Xml.newPullParser();
				parser.setInput(new StringReader(data));

				int eventType = parser.getEventType();
				int total_count = 0;

				WordItem item = null;
				String hanzi = null;
				String mean = null;
				String pinyin = null;
				//http://stackoverflow.com/questions/15254089/kxmlparser-throws-unexpected-token-exception-at-the-start-of-rss-pasing

				while(eventType != XmlPullParser.END_DOCUMENT)
				{
					switch(eventType) {
						case XmlPullParser.START_TAG:
							if(parser.getName().equals("worditem")) {
								total_count ++;
							} else if(parser.getName().equals("hanzi")) {
								hanzi = parser.nextText();
							} else if(parser.getName().equals("mean")) {
								mean = parser.nextText();
							} else if(parser.getName().equals("pinyin")) {
								pinyin = parser.nextText();
							}
							break;
					}

					eventType = parser.next();
					if(eventType == XmlPullParser.END_TAG){
						if(parser.getName().equals("worditems"))
							break;
						else if(parser.getName().equals("worditem")) {
							if(pinyin != null) {
								item = new WordItem(hanzi, mean, pinyin);
								pinyin = null;
							} else {
								item = new WordItem(hanzi, mean);
							}
							wordList.add(item);
						}
					}
				}
				Log.e(TAG, "Words total count : " + total_count);

			} catch (StringIndexOutOfBoundsException e) {
				Log.e(TAG, "HTTP get failed to retrieve valid XML");
			} catch (XmlPullParserException pe) {
				Log.e(TAG, pe.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}

			return wordList;
		}
	}







	// 단어 메뉴 리스트를 다운
	public class DownReqAsyncTask extends AsyncTask<String, Void, Boolean> {
		String url;

		@Override
		protected Boolean doInBackground(String... arg0) {
			boolean result = false;
			url = arg0[0];

			try {
				result = requestMenu(url);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			if(downMenuList.size() == 0) {
				Toast.makeText(getApplicationContext(), "단어 리스트가 없습니다.", Toast.LENGTH_LONG).show();
				finish();
			} else {
				downWordListAdapter.notifyDataSetChanged();
				//Toast.makeText(getApplicationContext(), "단어 리스트 다운 완료", Toast.LENGTH_LONG).show();
			}

		}

		private boolean requestMenu(String url) throws IOException {
			String strResult = null;
			InputStream in = null;

			String xmlData = Util.loadXML(mContext, R.raw.shujiwordlist);
			strResult = xmlData.replaceAll("^.*<", "<");
            downMenuList.clear();
			parseMenuXml(strResult);

			return true;
		}

		private boolean parseMenuXml(String data) {
			try {
				XmlPullParser parser = Xml.newPullParser();
				parser.setInput(new StringReader(data));

				int eventType = parser.getEventType();
				int total_count = 0;

				DownMenu menu = null;
				String title = null;
				String comment = null;
				int count = 0;
				String url = null;
				//http://stackoverflow.com/questions/15254089/kxmlparser-throws-unexpected-token-exception-at-the-start-of-rss-pasing

				while(eventType != XmlPullParser.END_DOCUMENT)
				{
					switch(eventType) {
						case XmlPullParser.START_TAG:
							if(parser.getName().equals("menuitem")) {
								total_count ++;
							} else if(parser.getName().equals("title")) {
								title = parser.nextText();
							} else if(parser.getName().equals("comment")) {
								comment = parser.nextText();
							} else if(parser.getName().equals("count")) {
								count = Integer.parseInt(parser.nextText());
							} else if(parser.getName().equals("url")) {
								url = parser.nextText();
							}
							break;
					}

					eventType = parser.next();
					if(eventType == XmlPullParser.END_TAG){
						if(parser.getName().equals("menuitems"))
							break;
						else if(parser.getName().equals("menuitem")) {
							menu = new DownMenu(title, comment, count, url);
							downMenuList.add(menu);
						}
					}
				}
				Log.e(TAG, "Menu total count : " + total_count);

			} catch (StringIndexOutOfBoundsException e) {
				Log.e(TAG, "HTTP get failed to retrieve valid XML");
			} catch (XmlPullParserException pe) {
				Log.e(TAG, pe.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}

			return true;
		}

	}

	static class ViewHolder {
		protected TextView tv1;
		protected TextView tv2;
		protected TextView tv3;
	}

	class DownWordListAdapter extends BaseAdapter {
		Context mainContext;
		LayoutInflater Inflater;
		ArrayList<DownMenu> arSrc;
		int layout;


		public DownWordListAdapter(Context context, int alayout, ArrayList<DownMenu> aarSrc) {
			mainContext = context;
			Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			arSrc = aarSrc;
			layout = alayout;
		}

		public int getCount() {
			return arSrc.size();
		}

		public DownMenu getItem(int position) {
			return arSrc.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;

			if(arSrc.size() == 0)
				return null;

			if(convertView == null) {
				convertView = Inflater.inflate(layout, parent, false);
				final ViewHolder viewHolder = new ViewHolder();
				viewHolder.tv1 = (TextView)convertView.findViewById(R.id.textViewDownWordListText1);
				viewHolder.tv2 = (TextView)convertView.findViewById(R.id.textViewDownWordListText2);
				viewHolder.tv3 = (TextView)convertView.findViewById(R.id.textViewDownWordListText3);

				convertView.setTag(viewHolder);
			}

			view = convertView;

			ViewHolder holder = (ViewHolder) view.getTag();

			holder.tv1.setText(arSrc.get(position).strTitle);
			holder.tv2.setText("" + arSrc.get(position).count + "개");
			holder.tv3.setText(arSrc.get(position).strComment);

			return view;
		}
	}
}
