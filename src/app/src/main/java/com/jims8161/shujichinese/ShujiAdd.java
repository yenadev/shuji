package com.jims8161.shujichinese;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.security.acl.Group;

public class ShujiAdd extends AppCompatActivity {

	EditText editHanzi;
	EditText editMeaning;
	EditText editPinyin;

	Button btnSave;
	Button btnExit;
	Button btnRead;
	Button btnClear;
	Button btnDelete;
	Button btnConvert;
	Button btnEditPinyin;
	Button buttonChangeGroup;
	CheckBox chkExclude;
    TextView textGroup;

	boolean isNewWord;
	int _id;

	String strEditPinyin;
	String strEditPinyinResult;
	int nEditPinyinPos;
	int nEditPinyinLength;

	String strRealPinyin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuji_add);

		editHanzi = (EditText)findViewById(R.id.editTextHanzi);
		editMeaning = (EditText)findViewById(R.id.editTextMeaning);
		editPinyin = (EditText)findViewById(R.id.editTextPinyin);

		//editHanzi.setPrivateImeOptions("defaultInputmode=hanja;");
		editMeaning.setPrivateImeOptions("defaultInputmode=korea;");
		editPinyin.setPrivateImeOptions("defaultInputmode=english;");

		chkExclude = (CheckBox)findViewById(R.id.checkBoxExclude);

        textGroup = (TextView) findViewById(R.id.textGroup);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		isNewWord = "1".equals(extras.getString("isNewWord"));
		if(isNewWord == false) {
			_id = extras.getInt("wordNum");

			String[] projection = {TableWordData.Columns.HANZI,
                    TableWordData.Columns.MEANING,
                    TableWordData.Columns.PINYIN,
					TableWordData.Columns.WORDGROUP,     // 3
                    TableWordData.Columns.EXCLUDE,
                    TableWordData.Columns.REAL_PINYIN,
                    TableWordData.Columns.WORDGROUP,    // 6
            };

			String selection = TableWordData.Columns._ID + "=" + _id;

			SQLiteDatabase db = new ShujiDatabaseHelper(getApplicationContext()).getWritableDatabase();
			Cursor cursor = db.query(TableWordData.TABLE_NAME, projection, selection, null, null, null, null);
			if (cursor != null) {
				cursor.moveToFirst();

				if(cursor != null) {
					String str = cursor.getString(0);
					editHanzi.setText(str);
					str = cursor.getString(1);
					editMeaning.setText(str);
					str = cursor.getString(2);
					editPinyin.setText(str);
					chkExclude.setChecked(cursor.getInt(4) > 0);
					strRealPinyin = cursor.getString(5);
                    textGroup.setText(cursor.getString(6));
					cursor.close();
				}
			}
			db.close();
		} else {
            textGroup.setText(GroupHelper.DEFAULT_GROUP);
        }

		btnSave = (Button)findViewById(R.id.buttonAddSave);
		btnSave.setOnClickListener(new OnClickListener() {
			//@Override
			public void onClick(View v) {
				String strHanzi = editHanzi.getText().toString();
				strHanzi.replace("\n", " ");
				if(strHanzi.length() > 0){
					ContentValues values = new ContentValues();
					values.put(TableWordData.Columns.HANZI, strHanzi);
					values.put(TableWordData.Columns.MEANING, editMeaning.getText().toString());
					values.put(TableWordData.Columns.PINYIN, editPinyin.getText().toString());
					values.put(TableWordData.Columns.REAL_PINYIN, strHanzi);	// TODO : change real pinyin
                    values.put(TableWordData.Columns.WORDGROUP, textGroup.getText().toString());
					values.put(TableWordData.Columns.EXCLUDE, chkExclude.isChecked() ? 1 : 0);
					SQLiteDatabase db = new ShujiDatabaseHelper(getApplicationContext()).getWritableDatabase();

					if(isNewWord) {
						if(-1 != db.insert(TableWordData.TABLE_NAME, null, values)) {

							Toast.makeText(getApplicationContext(), "저장되었습니다", Toast.LENGTH_SHORT).show();
						}
					}
					else {
						String selection = TableWordData.Columns._ID + "=" + _id;
						if(-1 != db.update(TableWordData.TABLE_NAME, values, selection, null)) {
							Toast.makeText(getApplicationContext(), "저장되었습니다", Toast.LENGTH_SHORT).show();
						}
					}
					db.close();
					clearField();
					ShujiList.isDataChanged = true;
					if(isNewWord == false)
						finish();
				}
			}
		});

		btnExit = (Button)findViewById(R.id.buttonAddExit);
		btnExit.setOnClickListener(new OnClickListener() {
			//@Override
			public void onClick(View v) {
				finish();
			}
		});

		btnRead = (Button)findViewById(R.id.buttonAddRead);
		btnRead.setOnClickListener(new OnClickListener() {
			//@Override
			public void onClick(View v) {
				String strHanzi = editHanzi.getText().toString();
				Intent serviceintent = new Intent(getApplicationContext(), ShujiService.class);
				if(strHanzi.length() > 0) {
					//MainActivity.mTTS.speak(strHanzi, TextToSpeech.QUEUE_FLUSH, null);
					serviceintent.putExtra(ShujiConstants.EXTRAS_HANZI, strHanzi);
				}
				String strMeaning = editMeaning.getText().toString();
				if(strMeaning.length() > 0) {
					//MainActivity.mTTSkr.speak(strMeaning, TextToSpeech.QUEUE_FLUSH, null);
					serviceintent.putExtra(ShujiConstants.EXTRAS_MEANING, strMeaning);
				}
				//Toast.makeText(getApplicationContext(), editHanzi.getPrivateImeOptions(), Toast.LENGTH_LONG).show();
				getApplicationContext().startService(serviceintent);
			}
		});

		btnClear = (Button)findViewById(R.id.buttonAddClear);
		btnClear.setOnClickListener(new OnClickListener() {
			//@Override
			public void onClick(View v) {
				clearField();
				// TODO set group as 1st item. spin.setSelection(0);
			}
		});

		btnDelete = (Button)findViewById(R.id.buttonAddDelete);
		btnDelete.setEnabled(!isNewWord);
		btnDelete.setVisibility(isNewWord ? View.INVISIBLE : View.VISIBLE);

		btnDelete.setOnClickListener(new OnClickListener() {
            //@Override
            public void onClick(View v) {
                if (isNewWord)
                    return;

                //deleteWordHandler.sendEmptyMessage(0);
                new AlertDialog.Builder(ShujiAdd.this).setTitle("단어삭제").setMessage("현재 단어 삭제하시겠습니까?")
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String selection = TableWordData.Columns._ID + "=" + _id;
                                SQLiteDatabase db = new ShujiDatabaseHelper(getApplicationContext()).getWritableDatabase();
                                if( 0 < db.delete(TableWordData.TABLE_NAME, selection, null))
                                    Toast.makeText(getApplicationContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                db.close();
                                ShujiList.isDataChanged = true;
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
            }
        });

		btnConvert = (Button)findViewById(R.id.buttonAddConvertPinyin);
		btnConvert.setOnClickListener(new OnClickListener() {
			//@Override
			public void onClick(View v) {
				String strHanzi = editHanzi.getText().toString();
				try	{
					HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
					outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
					String strPinyin = "";

					outputFormat.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
					outputFormat.setToneType(HanyuPinyinToneType.WITH_TONE_MARK);

					for(int i=0; i<strHanzi.length(); i++) {
						String[] strOut = PinyinHelper.toHanyuPinyinStringArray(strHanzi.charAt(i), outputFormat);
						if(strOut != null)
							strPinyin += strOut[0];
					}
					editPinyin.setText(strPinyin);
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			}
		});

		btnEditPinyin = (Button)findViewById(R.id.buttonAddPinyinEdit);
		btnEditPinyin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editPinyin();
			}
		});

		buttonChangeGroup = (Button) findViewById(R.id.buttonChangeGroup);
        buttonChangeGroup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeGroup();
            }
        });
        /*
        editHanzi.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
			}
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				try	{
					HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
					outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
					String strPinyin = "";
					
		            outputFormat.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
		            outputFormat.setToneType(HanyuPinyinToneType.WITH_TONE_MARK);
					
		            for(int i=0; i<s.length(); i++) {
		            	String[] strOut = PinyinHelper.toHanyuPinyinStringArray(s.toString().charAt(i), outputFormat);
		            	if(strOut != null)
		            		strPinyin += strOut[0];
		            }
		            editPinyin.setText(strPinyin);
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			}			
        	
        }); //*/

		if(!MainActivity.bTurnOffAd) {
			AdView adView = new AdView(this);
			adView.setAdSize(AdSize.BANNER);
			adView.setAdUnitId(ShujiConstants.ADV_ID);
			LinearLayout adLayout = (LinearLayout)findViewById(R.id.layoutAddAdv);
			adLayout.setOrientation(LinearLayout.VERTICAL);
			adLayout.addView(adView);
			AdRequest adRequest = new AdRequest.Builder().build();
			adView.loadAd(adRequest);
		}
	}


	public void clearField() {
		editHanzi.setText("");
		editMeaning.setText("");
		editPinyin.setText("");
		//spin.setSelection(0);
		chkExclude.setChecked(false);
		editHanzi.requestFocus();
	}

	/*Handler deleteWordHandler = new Handler() {
		public void handleMessage(Message message) {
			new AlertDialog.Builder(ShujiAdd.this).setTitle("단어삭제").setMessage("현재 단어 삭제하시겠습니까?")
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							String selection = TableWordData.Columns._ID + "=" + _id;
							SQLiteDatabase db = new ShujiDatabaseHelper(getApplicationContext()).getWritableDatabase();
							if( 0 < db.delete(TableWordData.TABLE_NAME, selection, null))
								Toast.makeText(getApplicationContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
							db.close();
							ShujiList.isDataChanged = true;
							finish();
						}
					})
					.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					})
					.show();
		}
	};*/



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

	private void editPinyin() {

		strEditPinyinResult="";
		nEditPinyinPos = 0;

		strEditPinyin = editHanzi.getText().toString();
		nEditPinyinLength = strEditPinyin.length();

		if(nEditPinyinLength < 1)
			return;

		editNextPinyin();
	}

	private void editPinyinFinish() {
		editPinyin.setText(strEditPinyinResult);
	}

	private void editNextPinyin() {
		if(nEditPinyinPos >= nEditPinyinLength) {
			editPinyinFinish();
			return;
		}

		try	{
			HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
			outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);

			outputFormat.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
			outputFormat.setToneType(HanyuPinyinToneType.WITH_TONE_MARK);


			final String[] strOut = PinyinHelper.toHanyuPinyinStringArray(strEditPinyin.charAt(nEditPinyinPos), outputFormat);
			if(strOut != null) {

				if(strOut.length == 1) {
					strEditPinyinResult += strOut[0];
					nEditPinyinPos++;
					editNextPinyin();
				}else if(strOut.length > 1) {
					AlertDialog.Builder b = new AlertDialog.Builder(ShujiAdd.this);
					String strTitle = "" + strEditPinyin.charAt(nEditPinyinPos);
					b.setTitle(strTitle);

					b.setItems(strOut, new DialogInterface.OnClickListener() {

						//@Override
						public void onClick(DialogInterface dialog, int which) {
							strEditPinyinResult += strOut[which];
							nEditPinyinPos++;
							editNextPinyin();
						}

					});
					b.show();
				}
			}
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}
	}

}
