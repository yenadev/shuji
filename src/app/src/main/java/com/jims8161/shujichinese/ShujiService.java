package com.jims8161.shujichinese;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;

public class ShujiService extends Service {
    private static final String TAG = "ShujiService";
	// TTS
	private TextToSpeech mTTS;
	private TextToSpeech mTTSkr;
	private boolean bInit = false;

	private boolean bKrInit = false;
	private boolean bCnInit = false;


    ArrayList<SpeakText> mTextList;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
        mTextList = new ArrayList<>();
        initTTS();
	}

    private static class SpeakText {
        public static final int TYPE_CN = 0;
        public static final int TYPE_KR = 1;
        int mType;
        String mWord;
        public SpeakText(int type, String word) {
            mType = type;
            mWord = word;
        }
    }

	private void initTTS() {
        Log.d(TAG, "initTTS");
		if (mTTS == null || !bCnInit) {
			mTTS = new TextToSpeech(this, new OnInitListener() {
                public void onInit(int status) {
                    Log.d(TAG, "bCnInit : " + status);
                    if (status == TextToSpeech.SUCCESS) {
                        int result = mTTS.setLanguage(Locale.CHINA);
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Log.d(TAG, "bCnInit setLanguage result : " + result);
                        if (result == TextToSpeech.LANG_MISSING_DATA
                                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            // Toast.makeText(getApplicationContext(),
                            // "TTS 중국어가 지원되지 않고 있습니다.", Toast.LENGTH_SHORT).show();
                            result = mTTS.setLanguage(Locale.CHINESE);
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            Log.d(TAG, "bCnInit setLanguage result : " + result);
                        }
                        mTTS.setSpeechRate((float) 0.6);
                        bCnInit = true;

                        if (bKrInit) {
                            bInit = true;
                            speak();
                        }
                    }
                }
            });
		}

        if (mTTSkr == null || !bKrInit) {
            mTTSkr = new TextToSpeech(this, new OnInitListener() {
                public void onInit(int status) {
                    Log.d(TAG, "bKrInit : " + status);
                    if (status == TextToSpeech.SUCCESS) {
                        int result = mTTSkr.setLanguage(Locale.KOREA);
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Log.d(TAG, "bKrInit setLanguage result : " + result);
                        if (result == TextToSpeech.LANG_MISSING_DATA
                                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            // Toast.makeText(getApplicationContext(),
                            // "TTS 한국어가 지원되지 않고 있습니다.", Toast.LENGTH_SHORT).show();
                            result = mTTSkr.setLanguage(Locale.KOREAN);
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            Log.d(TAG, "bKrInit setLanguage result : " + result);
                        }
                        mTTSkr.setSpeechRate((float) 0.9);
                        bKrInit = true;

                        if (bCnInit) {
                            bInit = true;
                            speak();
                        }
                    }
                }
            });
        }
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		String strHanzi = null;
		String strMeaning = null;
		if (intent != null) {
			strHanzi = intent.getStringExtra(ShujiConstants.EXTRAS_HANZI);
			strMeaning = intent.getStringExtra(ShujiConstants.EXTRAS_MEANING);
		}
		//String strPinyin = intent.getStringExtra(ShujiReminderConstants.EXTRAS_PINYIN);



		//if(strHanzi == null || strHanzi.length() <= 0) return START_STICKY; 

		if(strHanzi != null && strHanzi.length() > 0) {
			//mTTS.speak(strHanzi, TextToSpeech.QUEUE_FLUSH, null);
            mTextList.add(new SpeakText(SpeakText.TYPE_CN, strHanzi));
		}

		if(strMeaning != null && strMeaning.length() > 0) {
			//mTTSkr.speak(strMeaning, TextToSpeech.QUEUE_FLUSH, null);
            mTextList.add(new SpeakText(SpeakText.TYPE_KR, strMeaning));
		}

        speak();

		return START_NOT_STICKY;
	}

    private void speak() {
        if (!bInit) {
            Log.d(TAG, "TTS not initialized..");
            return;
        }

        if (mTextList == null) {
            mTextList = new ArrayList<>();
        }

        if (mTextList.size() == 0) {
            return;
        }

        for(SpeakText data : mTextList) {
            if (data.mType == SpeakText.TYPE_CN) {
                //Log.d(TAG, "mTTS.speak CN");
                mTTS.speak(data.mWord, TextToSpeech.QUEUE_FLUSH, null);
            } else if (data.mType == SpeakText.TYPE_KR) {
                //Log.d(TAG, "mTTSkr.speak KR");
                mTTSkr.speak(data.mWord, TextToSpeech.QUEUE_FLUSH, null);
            }
        }

        mTextList.clear();
    }

	/*public void onInit(int status) {
		// TODO Auto-generated method stub
		if (status == TextToSpeech.SUCCESS) {
			int result = mTTS.setLanguage(Locale.CHINESE);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				// Toast.makeText(getApplicationContext(),
				// "TTS 중국어가 지원되지 않고 있습니다.", Toast.LENGTH_SHORT).show();
			}
			mTTS.setSpeechRate((float) 0.6);

			result = mTTSkr.setLanguage(Locale.KOREAN);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				// Toast.makeText(getApplicationContext(),
				// "TTS 한국어가 지원되지 않고 있습니다.", Toast.LENGTH_SHORT).show();
			}
			mTTSkr.setSpeechRate((float) 0.9);


			if(mStrHanzi != null && mStrHanzi.length() > 0) {
				mTTS.speak(mStrHanzi, TextToSpeech.QUEUE_FLUSH, null);
				mStrHanzi = null;
			}

			if(mStrMeaning != null && mStrMeaning.length() > 0) {
				//mTTSkr.speak(mStrMeaning, TextToSpeech.QUEUE_ADD, null);
				//mStrMeaning = null;


				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						mTTSkr.speak(mStrMeaning, TextToSpeech.QUEUE_FLUSH, null);
						mStrMeaning = null;
					}
				}, 500);
			}
		} else {
			// init failed
			Toast.makeText(getApplicationContext(), "TTS 초기화 실패.",
					Toast.LENGTH_SHORT).show();
		}

	}
	*/
	@Override
	public void onDestroy() {

		if (mTTS != null) {
			mTTS.stop();
			mTTS.shutdown();
		}
		if (mTTSkr != null) {
			mTTSkr.stop();
			mTTSkr.shutdown();
		}
        bInit = false;
        bKrInit = false;
        bCnInit = false;

        super.onDestroy();
	}
}
