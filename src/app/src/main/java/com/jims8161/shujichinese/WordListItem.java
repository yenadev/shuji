package com.jims8161.shujichinese;

/**
 * Created by hoyeong on 2016-03-07.
 */
public class WordListItem {
    WordListItem(int id, String strHanzi, String strPinyin,
                 String strMeaning, String strRealPinyin, String group,
                 int bMemorized) {
        _id = id;
        Hanzi = strHanzi;
        Pinyin = strPinyin;
        Meaning = strMeaning;
        RealPinyin = strRealPinyin;
        groupName = group;
        memorized = bMemorized;
    }

    int _id;
    String Hanzi;
    String Pinyin;
    String Meaning;
    String RealPinyin;
    //int group;
    int memorized;
    String groupName;
}
