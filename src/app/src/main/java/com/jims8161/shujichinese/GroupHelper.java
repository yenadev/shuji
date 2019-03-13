package com.jims8161.shujichinese;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by hoyeong on 2016-03-07.
 */
public class GroupHelper {
    private static final String TAG = "GroupHelper";

    public static final String DEFAULT_GROUP = "Default";

    private static GroupHelper ourInstance = new GroupHelper();

    private static final Object mSynObj = new Object();

    private ArrayList<GroupItem> mGroupList;

    public static GroupHelper getInstance() {
        return ourInstance;
    }

    private GroupHelper() {

    }

    public ArrayList<String> getGroupNameList(Context context) {
        synchronized (mSynObj) {
            ArrayList<String> result = new ArrayList<>();
            if (getInstance().mGroupList == null || getInstance().mGroupList.size() == 0) {
                return result;
            }

            for (GroupItem item : getInstance().mGroupList) {
                result.add(item.mName);
            }
            return result;
        }
    }

    public static ArrayList<GroupItem> getGroupList(Context context) {
        synchronized (mSynObj) {
            if (getInstance().mGroupList == null) {
                loadGroups(context);
            }
            return getInstance().mGroupList;
        }
    }

    public static boolean checkGroupNameExists(Context context, String name) {
        synchronized (mSynObj) {
            if (DEFAULT_GROUP.equals(name)) {
                return true;
            }
            boolean result = false;
            String[] projection = {TableGroupData.Columns._ID, TableGroupData.Columns.GROUP_NAME};
            SQLiteDatabase db = new ShujiDatabaseHelper(context).getReadableDatabase();
            String selection = TableGroupData.Columns.GROUP_NAME + "=?";

            try {
                Cursor cursor = db.query(TableGroupData.TABLE_NAME, projection, selection, new String[]{name}, null, null, null);

                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        result = true;
                    }
                    cursor.close();
                }

            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                db.close();
            }

            return result;
        }
    }

    public static boolean insertGroup(Context context, String name) {
        synchronized (mSynObj) {
            if (checkGroupNameExists(context, name)) {
                Log.e(TAG, "group name already exist.");
                return false;
            }

            SQLiteDatabase db = new ShujiDatabaseHelper(context).getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(TableGroupData.Columns.GROUP_NAME, name);
            values.put(TableGroupData.Columns.DISPLAYING, 1);

            long ret;
            try {
                ret = db.insert(TableGroupData.TABLE_NAME, null, values);
            } catch (SQLiteException e) {
                Log.e(TAG, e.toString());
                ret = -1;
            } catch (Exception e) {
                e.printStackTrace();
                ret = -1;
            }

            db.close();
            if (ret < 0) {
                return false;
            }
            loadGroups(context);
            return true;
        }
    }

    public static boolean deleteGroup(Context context, int id, String groupName) {
        synchronized (mSynObj) {
            if (DEFAULT_GROUP.equals(groupName)) {
                return false;
            }
            SQLiteDatabase db = new ShujiDatabaseHelper(context).getWritableDatabase();
            String field = TableGroupData.Columns._ID;

            long ret;

            try {
                db.beginTransaction();
                ret = db.delete(TableGroupData.TABLE_NAME, field + "=?", new String[]{String.valueOf(id)});
                if (ret>=0) {
                    field = TableWordData.Columns.WORDGROUP;
                    ret = db.delete(TableWordData.TABLE_NAME, field + "=?", new String[]{groupName});
                }
                db.setTransactionSuccessful();
            } catch (SQLiteException e) {
                Log.e(TAG, e.toString());
                ret = -1;
            } catch (Exception e) {
                e.printStackTrace();
                ret = -1;
            } finally {
                db.endTransaction();
            }

            if (ret < 0) {
                return false;
            }
            loadGroups(context);
            return true;
        }
    }

    public static void updateDisplayInList(Context context, int id, boolean bDisplay) {
        SQLiteDatabase db = new ShujiDatabaseHelper(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TableGroupData.Columns.DISPLAYING_LIST, bDisplay);

        String selection = TableGroupData.Columns._ID + "=" + id;

        try {
            if (-1 != db.update(TableGroupData.TABLE_NAME, values, selection, null)) {
                Log.d(TAG, "updateDisplayInList success");
                //Toast.makeText(getApplicationContext(), "저장되었습니다", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "updateDisplayInList FAIL!!");
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        db.close();
    }

    public static void updateDisplay(Context context, int id, boolean bDisplay) {
        SQLiteDatabase db = new ShujiDatabaseHelper(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TableGroupData.Columns.DISPLAYING, bDisplay);

        String selection = TableGroupData.Columns._ID + "=" + id;

        try {
            if (-1 != db.update(TableGroupData.TABLE_NAME, values, selection, null)) {
                Log.d(TAG, "updateDisplay success");
                //Toast.makeText(getApplicationContext(), "저장되었습니다", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "updateDisplay FAIL!!");
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        db.close();
    }

    public static void loadGroups(Context context) {
        synchronized (mSynObj) {
            if (getInstance().mGroupList != null) {
                getInstance().mGroupList.clear();
            } else {
                getInstance().mGroupList = new ArrayList<>();
            }
            boolean bDefaultExist = false;
            String[] projection = {TableGroupData.Columns._ID,
                    TableGroupData.Columns.GROUP_NAME,
                    TableGroupData.Columns.DISPLAYING,
                    TableGroupData.Columns.DISPLAYING_LIST,
            };
            SQLiteDatabase db = new ShujiDatabaseHelper(context).getWritableDatabase();
            Cursor cursor = db.query(TableGroupData.TABLE_NAME, projection, null, null, null, null, null);

            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {

                        while (!cursor.isAfterLast()) {
                            int id = cursor.getInt(0);
                            String name = cursor.getString(1);
                            boolean bDisplaying = cursor.getInt(2) > 0;
                            boolean bDisplayingInList = cursor.getInt(3) > 0;
                            GroupItem item = new GroupItem(id, name, bDisplaying, bDisplayingInList);

                            if (name.equals(DEFAULT_GROUP)) {
                                bDefaultExist = true;
                                getInstance().mGroupList.add(0, item);
                            } else {
                                getInstance().mGroupList.add(item);
                            }
                            cursor.moveToNext();
                        }

                        cursor.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
            db.close();

            if (!bDefaultExist) {
                insertGroup(context, DEFAULT_GROUP);
            }
        }
    }

    public static void showSelectGroupPopup(final Activity activity, final SelectGroupListener listener) {
        LayoutInflater adbInflater = LayoutInflater.from(activity);
        LinearLayout layout = (LinearLayout) adbInflater.inflate(R.layout.popup_select_group, null);

        final AlertDialog dialog;

        ListView listView = (ListView) layout.findViewById(R.id.listView);
        ListAdapter aa = new ListAdapter(activity,
                R.layout.layout_selectgroup_adapter, getInstance().mGroupList);

        listView.setAdapter(aa);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("그룹 선택").setView(layout);
        dialog = builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                listener.onCanceled(activity);
            }
        }).show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                GroupItem item = getInstance().mGroupList.get(position);
                listener.onSelected(activity, item.mId, item.mName);
                dialog.dismiss();
            }
        });

        aa.notifyDataSetChanged();
    }

    public interface SelectGroupListener {
        void onSelected(Context context, int id, String groupName);
        void onCanceled(Context context);
    }

    private static class ListAdapter extends BaseAdapter {
        Context mContext;
        LayoutInflater Inflater;
        ArrayList<GroupItem> arSrc;
        int layout;

        public ListAdapter(Context context, int alayout, ArrayList<GroupItem> aarSrc) {
            mContext = context;
            Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            arSrc = aarSrc;
            layout = alayout;
        }

        // 아이템 갯수 리턴
        public int getCount() {
            return arSrc.size();
        }

        // 아이템 데이터 리턴
        public GroupItem getItem(int position) {
            return arSrc.get(position);
        }

        // 아이템의 순서. 사용하지 않는 함수.
        public long getItemId(int position) {
            return position;
        }

        // 리스트뷰에 항목이 추가될때 마다, 각 아이템별 뷰를 생성한다. 메모를 보여주는 텍스트와 콤보박스가 들어간다.
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;

            if(arSrc.size() == 0)
                return null;

            if(convertView == null) {
                // convertView가 null 이면 처음 생성하는 아이템이다.
                convertView = Inflater.inflate(layout, parent, false);

                final ViewHolder viewHolder = new ViewHolder();
                viewHolder.textView = (TextView)convertView.findViewById(R.id.textView);
                convertView.setTag(viewHolder);
            }

            view = convertView;
            ViewHolder holder = (ViewHolder) view.getTag();
            String strData = arSrc.get(position).mName;

            //TextView textView = (TextView)view;
            holder.textView.setText(strData);
            /*if (arSrc.get(position).mLocation.getLatitude() == -200.0f) {
                //textView.setBackgroundColor(Color.parseColor("#990000"));
                holder.textView.setBackgroundColor(0xFFAA3333);
                //textView.setTextColor(0xFFFF0000);
            } else {
                holder.textView.setBackgroundColor(0xFFFFFFFF);
            }*/
            return view;
        }
    }

    static class ViewHolder {
        protected TextView textView;
    }
}
