package com.jims8161.shujichinese;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ShujiList extends AppCompatActivity {

	private static final int FILE_SELECT_CODE = 0;
	private static final int TXTFILE_SELECT_CODE = 1;
	private static final String BACKUP_FOLDER = "ShujiChnDB";
	private static final String BACKUP_DB_PREFIX = "SHUJI_DB_VERSION";

	// for word list
	private ArrayList<Boolean> alChecked;
	private ArrayList<WordListItem> alWordList;

	ListView listView;
	WordListAdapter adapterWordList;
	
	public static boolean isDataChanged;

    private Context mContext;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuji_list);
        mContext = this;

		alChecked = new ArrayList<Boolean>();
		alWordList = new ArrayList<WordListItem>();

        adapterWordList = new WordListAdapter(this, R.layout.layout_wordlist_adapter, alWordList);
        
        listView = (ListView) findViewById(R.id.listViewShujiList);
        listView.setAdapter(adapterWordList);
        listView.setOnItemClickListener(mListItemClickListener);
        isDataChanged = true;
        updateWordList();
    }
    
    private void updateWordList() {
    	if(!isDataChanged)
    		return;
    	
    	String[] projection = {TableWordData.Columns.HANZI,
				TableWordData.Columns.MEANING,
				TableWordData.Columns.PINYIN,
				TableWordData.Columns.REAL_PINYIN,
				TableWordData.Columns.WORDGROUP,
				TableWordData.Columns.EXCLUDE,  // 5
				TableWordData.Columns._ID};

    	String selection = "(";
    	boolean bFirst = true;


        ArrayList<GroupItem> groupList = GroupHelper.getGroupList(mContext);

    	//for(int i=0; i< groupList.size(); i++) {
        for (GroupItem item : groupList) {
    		if(item.mDisplayInList) {
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
    	
    	if(!MainActivity.bListIncludeMemzed)
    		selection = selection + " AND " + TableWordData.Columns.EXCLUDE + "=0";

    	SQLiteDatabase db = new ShujiDatabaseHelper(getApplicationContext()).getWritableDatabase();
    	Cursor cursor = db.query(TableWordData.TABLE_NAME, projection, selection, null, null, null,
    			(MainActivity.list_display_order == PrefUtil.LIST_DISP_DIVISION_ORDER) ? TableWordData.Columns.WORDGROUP : null);


		alChecked.clear();
		alWordList.clear();
    	WordListItem item;
    	boolean bCheck = false;
    	if(cursor != null) {
	    	try{
	    		if(cursor.moveToFirst()) {
	    			while(!cursor.isAfterLast()) {
	   				
	    				alChecked.add(bCheck);
	    				
	    				item = new WordListItem(cursor.getInt(6), cursor.getString(0), cursor.getString(2), cursor.getString(1),
	    						cursor.getString(3), cursor.getString(4), cursor.getInt(5));
	    				alWordList.add(item);
	    				
	    				cursor.moveToNext();
	    			}
	   				//adapterWordList.notifyDataSetChanged();
	    		}
	    		
	    	} catch (NullPointerException e) {
	    		Toast.makeText(getApplicationContext(), "null", Toast.LENGTH_SHORT).show();
	    	}
	    	cursor.close();
    	}
    	db.close();
    	adapterWordList.notifyDataSetChanged();
    	isDataChanged = false;
    }
    
    AdapterView.OnItemClickListener mListItemClickListener = new AdapterView.OnItemClickListener() {
    	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    		Intent intentWrite = new Intent(getApplication(), ShujiAdd.class);
    		intentWrite.putExtra("isNewWord", "0");
    		intentWrite.putExtra("wordNum", alWordList.get(position)._id);
    		startActivity(intentWrite);
    	}
    };


    
    @Override
    protected void onResume() {
    	super.onResume();
    	updateWordList();
    }
    
	static class ViewHolder {
	    protected TextView tv1;
	    protected TextView tv2;
	    protected TextView tv3;
	    protected TextView tvGroup;
	    protected TextView tvMemzed;
	    protected CheckBox cbCheck;
	}
	
    class WordListAdapter extends BaseAdapter {
		Context mainContext;
		LayoutInflater Inflater;
		ArrayList<WordListItem> arSrc;
		int layout;
				
		
		public WordListAdapter(Context context, int alayout, ArrayList<WordListItem> aarSrc) {
			mainContext = context;
			Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			arSrc = aarSrc;
			layout = alayout;
		}

		public int getCount() {
			return arSrc.size();
		}

		public WordListItem getItem(int position) {
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
				viewHolder.tv1 = (TextView)convertView.findViewById(R.id.textViewWordListText1);
				viewHolder.tv2 = (TextView)convertView.findViewById(R.id.textViewWordListText2);
				viewHolder.tv3 = (TextView)convertView.findViewById(R.id.textViewWordListText3);
				viewHolder.tvGroup = (TextView)convertView.findViewById(R.id.textViewWordListGroup);
				viewHolder.tvMemzed = (TextView)convertView.findViewById(R.id.textViewWordListMemorized);
				viewHolder.cbCheck = (CheckBox)convertView.findViewById(R.id.checkBoxWordListCheck);
			
				viewHolder.cbCheck.setOnCheckedChangeListener(new OnCheckedChangeListener(){
	
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						int getPosition = (Integer) buttonView.getTag();
						if(alChecked.size() > getPosition)
							alChecked.set(getPosition, buttonView.isChecked());
					}
				});
				convertView.setTag(viewHolder);
				convertView.setTag(R.id.checkBoxWordListCheck, viewHolder.cbCheck);

			}

			view = convertView;
			
			ViewHolder holder = (ViewHolder) view.getTag();
			holder.cbCheck.setTag(position);

			holder.tv1.setText(arSrc.get(position).Meaning);
			holder.tv2.setText(arSrc.get(position).Hanzi);
			holder.tv3.setText(arSrc.get(position).Pinyin);
			holder.tvGroup.setText("" + arSrc.get(position).groupName);
			holder.tvMemzed.setText( arSrc.get(position).memorized > 0 ? "V" : "" );
			holder.cbCheck.setChecked(alChecked.get(position));
			return view;
		}
    }

	public boolean onKeyDown(int KeyCode, KeyEvent event) {
		super.onKeyDown(KeyCode, event);
//		if(event.getAction() == KeyEvent.ACTION_UP) {
			if(KeyCode == KeyEvent.KEYCODE_BACK) {

	    		
				//View view = adapterWordList.getView(1, null, (ViewGroup)findViewById(R.id.listViewShujiList));
				
	    		//CheckBox cb = (CheckBox)view.findViewById(R.id.checkBoxWordListCheck);
	    		
	    		//if(cb.isChecked())
				/*
				if(alChecked.get(1))
	    			Toast.makeText(getApplicationContext(), "checked", Toast.LENGTH_SHORT).show();
	    		else
	    			Toast.makeText(getApplicationContext(), "Not checked", Toast.LENGTH_SHORT).show();
    			*/				

				return true;
			}
//		}
		return false;
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_shuji_list, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //Log.i("Got into onOptionsItemSelected", "Yes");
        switch(item.getItemId()){

        /*
        case R.id.itemMenuShujiListSort:
        	Toast.makeText(getApplicationContext(), "준비중인 메뉴입니다.", Toast.LENGTH_LONG).show();
        	break;
        */

        // 리스트 화면 설정
        case R.id.itemMenuShujiListSetting:
			Intent settingIntent = new Intent(this, ShujiListSetting.class);
			startActivity(settingIntent);
        	break;

        case R.id.itemMenuShujiListSelectAll:
        	for(int i=0; i< alChecked.size(); i++) {
        		alChecked.set(i, true);
        	}
        	adapterWordList.notifyDataSetChanged();
        	break;
        	
        case R.id.itemMenuShujiListUnSelectAll:
        	for(int i=0; i< alChecked.size(); i++) {
        		alChecked.set(i, false);
        	}
        	adapterWordList.notifyDataSetChanged();
        	break;        	
        ///////////// 선택단어 처리 /////////////
        case R.id.itemMenuShujiListSelMemzed:
        	updateWordListMemorized(true);
        	updateWordList();
        	break;

        case R.id.itemMenuShujiListSelUnMemzed:
        	updateWordListMemorized(false);
        	updateWordList();
        	break;

        // TODO change group
        case R.id.itemMenuChangeGroup:
            changeGroup();
            break;
        /*case R.id.itemMenuShujiListSelGroup1:
        	updateWordListChangeGroup(0);
        	updateWordList();
        	break;
        case R.id.itemMenuShujiListSelGroup2:
        	updateWordListChangeGroup(1);
        	updateWordList();
        	break;
        case R.id.itemMenuShujiListSelGroup3:
        	updateWordListChangeGroup(2);
        	updateWordList();
        	break;
        case R.id.itemMenuShujiListSelGroup4:
        	updateWordListChangeGroup(3);
        	updateWordList();
        	break;
        case R.id.itemMenuShujiListSelGroup5:
        	updateWordListChangeGroup(4);
        	updateWordList();
        	break;*/

        case R.id.itemMenuShujiListSelDelete:
        	updateWordListDelete();
        	break;
        ///////////// 선택단어 처리 /////////////        	


        ///////////// 백업 처리 ///////////// 
        case R.id.itemMenuShujiListBackupAll:
	        {
	    		new AlertDialog.Builder(ShujiList.this).setTitle("데이터 백업").setMessage("csv 파일에 백업하시겠습니까?")
	    		.setPositiveButton("예", new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int whichButton) {
	    				backupWordData();
	    			}
	    		})
	    		.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int whichButton) {
	    			}
	    		})
	    		.show();	        	
	        }        
        	break;

        case R.id.itemMenuShujiListReadBackup:
	        {
	    		new AlertDialog.Builder(ShujiList.this).setTitle("데이터 백업").setMessage("csv 파일에서 읽어오겠습니까?")
	    		.setPositiveButton("예", new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int whichButton) {
	    				showFileChooser();
	    			}
	    		})
	    		.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int whichButton) {
	    			}
	    		})
	    		.show();	        	
	        
	        }
	        break;
/*
        case R.id.itemMenuShujiListReadTXTBackup:
        {
    		new AlertDialog.Builder(shujiList.this).setTitle("데이터 백업").setMessage("txt 파일에서 읽어오겠습니까?(탭으로 구분)")
    		.setPositiveButton("예", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int whichButton) {
    				showTxtFileChooser();
    			}
    		})
    		.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int whichButton) {
    			}
    		})
    		.show();
        }
        break;	        
*/
	    ///////////// 백업 처리 /////////////

        default:
        	break;
        }

        return true;

    }

    private void changeGroup() {
        GroupHelper.showSelectGroupPopup(this, new GroupHelper.SelectGroupListener() {

            @Override
            public void onSelected(Context context, int id, String groupName) {
                updateWordListChangeGroup(groupName);
                updateWordList();
            }

            @Override
            public void onCanceled(Context context) {

            }
        });
    }

    public void updateWordListMemorized(boolean bMemorized) {
		ContentValues values = new ContentValues();
		int count = 0;
		values.put(TableWordData.Columns.EXCLUDE, bMemorized);
		int size = alChecked.size();

		SQLiteDatabase db = new ShujiDatabaseHelper(getApplicationContext()).getWritableDatabase();
		
		for(int i=0; i<size; i++) {
		
			if(!alChecked.get(i))
				continue;
		
			String selection = "_id=" + alWordList.get(i)._id;
			if(-1 != db.update(TableWordData.TABLE_NAME, values, selection, null)) {
				count ++;
			}        				
			
		}
		db.close();
		if(count > 0)
			Toast.makeText(getApplicationContext(), "저장되었습니다", Toast.LENGTH_SHORT).show();
		else {
			Toast.makeText(getApplicationContext(), "선택된 단어가 없습니다", Toast.LENGTH_SHORT).show();
			return;
		}
		isDataChanged = true;
    }
    
    public void updateWordListChangeGroup(String groupName) {
		ContentValues values = new ContentValues();
		int count = 0;
		values.put(TableWordData.Columns.WORDGROUP, groupName);
		int size = alChecked.size();

		SQLiteDatabase db = new ShujiDatabaseHelper(getApplicationContext()).getWritableDatabase();
		
		for(int i=0; i<size; i++) {
		
			if(!alChecked.get(i))
				continue;
		
			String selection = "_id=" + alWordList.get(i)._id;
			if(-1 != db.update(TableWordData.TABLE_NAME, values, selection, null)) {
				count ++;
			}        				
		
		}
		db.close();
		if(count > 0)
			Toast.makeText(getApplicationContext(), "저장되었습니다", Toast.LENGTH_SHORT).show();
		else {
			Toast.makeText(getApplicationContext(), "선택된 단어가 없습니다", Toast.LENGTH_SHORT).show();
			return;
		}
		isDataChanged = true;
    }    
    
    public void updateWordListDelete() {
    	
    	if(alChecked.size() == 0) {
    		Toast.makeText(getApplicationContext(), "선택된 단어가 없습니다", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	

		new AlertDialog.Builder(ShujiList.this).setTitle("단어삭제").setMessage("선택된 단어들을 삭제하시겠습니까?")
		.setPositiveButton("예", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
								
        		SQLiteDatabase db = new ShujiDatabaseHelper(getApplicationContext()).getWritableDatabase();
        		int count = 0;
        		for(int i=0; i< alChecked.size(); i++) {
        			
        			if(alChecked.get(i) == false)
        				continue;
        			
        			String selection = TableWordData.Columns._ID + "=" + alWordList.get(i)._id;
        			
	        		if( 0 < db.delete(TableWordData.TABLE_NAME, selection, null)) {
	        			count ++;
	        		}
        		}
        		db.close();
        		isDataChanged = true;
        		if(count > 0)
        			Toast.makeText(getApplicationContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
        		else {
        			Toast.makeText(getApplicationContext(), "선택된 단어가 없습니다", Toast.LENGTH_SHORT).show();
        			return;
        		}
        		updateWordList();
			}
		})
		.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		})
		.show();
    	
    }        
    
    public void backupWordData() {
    	new Thread() {
    		public void run() {

    		    SQLiteDatabase db = new ShujiDatabaseHelper(getApplicationContext()).getWritableDatabase();

    		    File exportDir = new File(Environment.getExternalStorageDirectory(), BACKUP_FOLDER);
    		    exportDir.setExecutable(true, false);
    		    exportDir.setReadable(true, false);
    		    exportDir.setWritable(true, false);
    		    if (!exportDir.exists()) 
    		    {
    		    	exportDir.mkdirs();
    		    }

    	    	String[] projection = {TableWordData.Columns.HANZI,
                        TableWordData.Columns.MEANING,
                        TableWordData.Columns.PINYIN,
						TableWordData.Columns.REAL_PINYIN,
                        TableWordData.Columns.WORDGROUP,
                        TableWordData.Columns.EXCLUDE, // 5
                        TableWordData.Columns._ID};
    	    	Locale current = getResources().getConfiguration().locale;
    	    	SimpleDateFormat formatter = new SimpleDateFormat( "yyMMdd_HHmmss", current );
    	        String today = formatter.format ( new Date() );
    	        //System.out.println ( today );
    	        
    		    File file = new File(exportDir, "ShujiDB_" + today + ".csv");
    		    file.setExecutable(true, false);
    		    file.setReadable(true, false);
    		    file.setWritable(true, false);
    		    
    		    byte[] utf8BOM = new byte[3];
    		    utf8BOM[0] = (byte)0xEF;
    		    utf8BOM[1] = (byte)0xBB;
    		    utf8BOM[2] = (byte)0xBF;
    		    

    		    try
    		    {

    		        if (file.createNewFile()){
    		            System.out.println("File is created!");
    		            System.out.println(file.getAbsolutePath());
    		        }else{
    		            System.out.println("File already exists."+file.getAbsolutePath());
    		        }


    		        FileOutputStream fOut = new FileOutputStream(file);
    		        DataOutputStream myOutWriter = new DataOutputStream(fOut);
    		        Cursor curCSV=db.query(TableWordData.TABLE_NAME, projection, null, null, null, null, null);

    		        String strVer = BACKUP_DB_PREFIX + "=" + ShujiDatabaseHelper.DB_VERSION + "\n";
    		        myOutWriter.write(utf8BOM);
    		        myOutWriter.write(strVer.getBytes());
    		        
    		        if (curCSV != null) {
	    		        while(curCSV.moveToNext())
	    		        {
	    		            String str = curCSV.getString(0).replace(",", ";") +","+ curCSV.getString(1).replace(",", ";") +","+
	    		            		curCSV.getString(2).replace(",", ";") +","+ curCSV.getString(3).replace(",", ";") +","+ 
	    		            		curCSV.getString(4) +","+ curCSV.getInt(5) + "\n";
	
	    		            myOutWriter.write(str.getBytes());
	    		        }
	    		        curCSV.close();
    		        }
    		        
    		        myOutWriter.close();
    		        fOut.close();
    		        db.close();

    		        Log.e("ShujiChn Complete", "Complete");
    		        backupCompleteHandler.sendEmptyMessage(0);
    		        return ;

    		    }
    		    catch(SQLException sqlEx)
    		    {
    		        Log.e("ShujiChn", sqlEx.getMessage(), sqlEx);
    		        return ;
    		    }

    		    catch (IOException e)
    		    {
    		        Log.e("ShujiChn", e.getMessage(), e);
    		        return ;
    		    }   
    		    
    		}
    	}.start();
    }
    
    
    public void readBackedUpData(final String backupFile) {
    	new Thread() {
    		public void run() {
    			
    			SQLiteDatabase db = new ShujiDatabaseHelper(getApplicationContext()).getWritableDatabase();
    		    File file = new File(backupFile);
    		    file.setExecutable(true, false);
    		    file.setReadable(true, false);
    		    file.setWritable(true, false);
    			
    		    try {
    		    	FileInputStream fIn = new FileInputStream(file);
    		    	BufferedReader bufReader = new BufferedReader(new InputStreamReader(fIn));
    		    	
    		    	String line = null;
    		    	String strData[];
    		    	int dbVersion=0;
    		    	
    		    	line = bufReader.readLine();
    		    	
    		    	if(line == null || line.contains(BACKUP_DB_PREFIX+"=") == false) {
        		    	bufReader.close();
        		    	fIn.close();
        		    	db.close();
        		    	Log.e("ShujiChn Read Error", "Read Error!");
        		    	return;
    		    	}
    		    	else {
    		    		strData = line.split(",")[0].split("=");
    		    		dbVersion = Integer.parseInt(strData[1]);
    		    		
    		    		if(dbVersion != ShujiDatabaseHelper.DB_VERSION) {
    		    			Log.e("DB Version is differ", "File Version=" + dbVersion + ", Current Version=" + ShujiDatabaseHelper.DB_VERSION);
    		    		}
    		    	}
    		    	
    		    	while((line = bufReader.readLine()) != null) {
    		    		strData = line.split(",");
    		    		int count_col = strData.length;
    		    		if(count_col == 0) continue;
    		    		
    		    		ContentValues values = new ContentValues();
    		    		
    		    		if(count_col > 0)
    		    			values.put(TableWordData.Columns.HANZI, strData[0].replace(";", ","));
    		    		if(count_col > 1)
    		    			values.put(TableWordData.Columns.MEANING, strData[1].replace(";", ","));
    		    		if(count_col > 2)
    		    			values.put(TableWordData.Columns.PINYIN, strData[2].replace(";", ","));
    		    		if(count_col > 3)
    		    			values.put(TableWordData.Columns.REAL_PINYIN, strData[3].replace(";", ","));
    		    		if(count_col > 4) {
                            String groupName;
                            if (dbVersion <= 2) {
                                groupName = String.valueOf( Integer.parseInt(strData[4]) + 1);
                            } else {
                                groupName = strData[4];
                            }
                            if (!GroupHelper.checkGroupNameExists(mContext, groupName)) {
                                GroupHelper.insertGroup(mContext, groupName);
                            }
                            values.put(TableWordData.Columns.WORDGROUP, groupName);
                        }
    		    		if(count_col > 5)
    		    			values.put(TableWordData.Columns.EXCLUDE, Integer.parseInt(strData[5])>0 ? 1 : 0);
            			
    		    		if(-1 != db.insert(TableWordData.TABLE_NAME, null, values)) {
    		    			
    		    		}
    		    	}
    		    	
    		    	bufReader.close();
    		    	fIn.close();
    		    	db.close();
    		    	Log.e("ShujiChn Read Complete", "Read Complete");
    		    	backupCompleteHandler.sendEmptyMessage(1);
    		    	return;
    		    }
    		    catch(SQLException sqlEx)
    		    {
    		        Log.e("ShujiChn", sqlEx.getMessage(), sqlEx);
    		        return ;
    		    }

    		    catch (IOException e)
    		    {
    		        Log.e("ShujiChn", e.getMessage(), e);
    		        return ;
    		    }    
    			
    			
    		}
    	}.start();
    }
    
    
    public void readTXTBackedUpData(final String backupFile) {
    	new Thread() {
    		public void run() {
    			
    			SQLiteDatabase db = new ShujiDatabaseHelper(getApplicationContext()).getWritableDatabase();
    		    File file = new File(backupFile);
    		    file.setExecutable(true, false);
    		    file.setReadable(true, false);
    		    file.setWritable(true, false);
    			
    		    try {
    		    	FileInputStream fIn = new FileInputStream(file);
    		    	BufferedReader bufReader = new BufferedReader(new InputStreamReader(fIn));
    		    	
    		    	String line = null;
    		    	String strData[];
    		    	int dbVersion=0;

    		    	/*
    		    	line = bufReader.readLine();
    		    	
    		    	if(line == null || line.contains(BACKUP_DB_PREFIX+"=") == false) {
        		    	bufReader.close();
        		    	fIn.close();
        		    	db.close();
        		    	Log.e("ShujiChn Read Error", "Read Error!");
        		    	return;
    		    	}
    		    	else {
    		    		strData = line.split(",")[0].split("=");
    		    		dbVersion = Integer.parseInt(strData[1]);
    		    		
    		    		if(dbVersion != shujiDatabaseHelper.DB_VERSION) {
    		    			Log.e("DB Version is differ", "File Version="+dbVersion+", Current Version="+shujiDatabaseHelper.DB_VERSION);
    		    		}
    		    	}
    		    	*/
    		    	while((line = bufReader.readLine()) != null) {
    		    		strData = line.split("\t");
    		    		int count_col = strData.length;
    		    		if(count_col == 0) continue;

    		    		ContentValues values = new ContentValues();

    		    		if(count_col > 0)
    		    			values.put(TableWordData.Columns.HANZI, strData[0]);
    		    		if(count_col > 1)
    		    			values.put(TableWordData.Columns.MEANING, strData[1]);
    		    		if(count_col > 2)
    		    			values.put(TableWordData.Columns.PINYIN, strData[2]);
    		    		if(count_col > 3)
    		    			values.put(TableWordData.Columns.REAL_PINYIN, strData[3]);

    		    		try {
	    		    		if(count_col > 4) {
                                String groupName = strData[4];

                                if (!GroupHelper.checkGroupNameExists(mContext, groupName)) {
                                    GroupHelper.insertGroup(mContext, groupName);
                                }
                                values.put(TableWordData.Columns.WORDGROUP, groupName);
                                //values.put(TableWordData.Columns.DIVISION, Integer.parseInt(strData[4]));
                            }
	    		    		else {
                                //values.put(TableWordData.Columns.DIVISION, 0);
                                values.put(TableWordData.Columns.WORDGROUP, GroupHelper.DEFAULT_GROUP);
                            }
    		    		} catch (NumberFormatException e) {
    		    			values.put(TableWordData.Columns.DIVISION, 0);
    		    		}

    		    		try {
	    		    		if(count_col > 5)
	    		    			values.put(TableWordData.Columns.EXCLUDE, Integer.parseInt(strData[5])>0 ? 1 : 0);
	    		    		else
	    		    			values.put(TableWordData.Columns.EXCLUDE, 0);
    		    		} catch (NumberFormatException e) {
    		    			values.put(TableWordData.Columns.EXCLUDE, 0);
    		    		}
            			
    		    		if(-1 != db.insert(TableWordData.TABLE_NAME, null, values)) {

    		    		}
    		    	}
    		    	
    		    	bufReader.close();
    		    	fIn.close();
    		    	db.close();
    		    	Log.e("ShujiChn Read Complete", "Read Complete");
    		    	backupCompleteHandler.sendEmptyMessage(1);
    		    	return;
    		    }
    		    catch(SQLException sqlEx)
    		    {
    		        Log.e("ShujiChn", sqlEx.getMessage(), sqlEx);
    		        return ;
    		    }

    		    catch (IOException e)
    		    {
    		        Log.e("ShujiChn", e.getMessage(), e);
    		        return ;
    		    }    
    			
    			
    		}
    	}.start();
    }
    
    
    
    private void showFileChooser() {
    	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    	Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath());
    	
    	intent.setDataAndType(uri, "file/*");
    	intent.addCategory(Intent.CATEGORY_DEFAULT);
    	
    	try {
    		startActivityForResult(Intent.createChooser(intent, "읽어올 csv 백업파일을 선택하세요(파일매니저 어플 필요)"),FILE_SELECT_CODE);
    	} catch (android.content.ActivityNotFoundException ex) {
    		Toast.makeText(getApplicationContext(), "파일매니저 어플을 설치해주세요.", Toast.LENGTH_SHORT).show();
    	}
    }
    
    private void showTxtFileChooser() {
    	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    	Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath());
    	
    	intent.setDataAndType(uri, "file/*");
    	intent.addCategory(Intent.CATEGORY_DEFAULT);
    	
    	try {
    		startActivityForResult(Intent.createChooser(intent, "읽어올 txt 백업파일을 선택하세요(파일매니저 어플 필요)"),TXTFILE_SELECT_CODE);
    	} catch (android.content.ActivityNotFoundException ex) {
    		Toast.makeText(getApplicationContext(), "파일매니저 어플을 설치해주세요.", Toast.LENGTH_SHORT).show();
    	}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch(requestCode) {
    		case FILE_SELECT_CODE:
    			if(resultCode == RESULT_OK) {
    				Uri uri = data.getData();
    				String path = null;
    				try {
    					path = getPath(this, uri);
    				} catch (URISyntaxException e) {
    					e.printStackTrace();
    				}
    				readBackedUpData(path);
    			}
    			break;  
    			
    		case TXTFILE_SELECT_CODE:
    			if(resultCode == RESULT_OK) {
    				Uri uri = data.getData();
    				String path = null;
    				try {
    					path = getPath(this, uri);
    				} catch (URISyntaxException e) {
    					e.printStackTrace();
    				}
    				readTXTBackedUpData(path);
    			}
    			break;
    	}
    	super.onActivityResult(requestCode, resultCode, data);
    }
    
    
    public static String getPath(Context context, Uri uri) throws URISyntaxException {
    	if("content".equalsIgnoreCase(uri.getScheme())) {
    		String[] projection = {"_data"};
    		Cursor cursor = null;
    		String path = null;
    		
    		try {
    			cursor = context.getContentResolver().query(uri, projection, null, null, null);
    			int column_index = cursor.getColumnIndexOrThrow("_data");
    			if(cursor.moveToFirst()) {
    				path = cursor.getString(column_index);
    				cursor.close();
    				cursor = null;
    				return path;
    			}
    		} catch (Exception e) {
    			
    		}
    		if (cursor != null)
    			cursor.close();
    		
    	}
    	else if("file".equalsIgnoreCase(uri.getScheme())) {
    		return uri.getPath();
    	}
    	return null;
    }
    
	Handler backupCompleteHandler = new Handler() {
		public void handleMessage(Message message) {
			if(message.what == 0)
				Toast.makeText(getApplicationContext(), "데이터 백업 완료하였습니다\n" + BACKUP_FOLDER + "폴더에 저장완료", Toast.LENGTH_LONG).show();
			else if(message.what == 1)
				Toast.makeText(getApplicationContext(), "데이터 로드 완료하였습니다", Toast.LENGTH_LONG).show();
			isDataChanged = true;
			updateWordList();
		}
	};
}


