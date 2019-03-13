package com.jims8161.shujichinese;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by hoyeong on 2016-03-08.
 */
public class GroupManageActivity extends AppCompatActivity {
    public static final String TAG = "GroupManageActivity";

    ListAdapter aa;
    ListView listView;
    AlertDialog mDialog;
    Button buttonAdd;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_manage);
        mContext = this;

        listView = (ListView) findViewById(R.id.listView);

        aa = new ListAdapter(getApplicationContext(),
                R.layout.layout_group_manage_list_adapter, GroupHelper.getGroupList(this));
        listView.setAdapter(aa);
        listView.setOnItemClickListener(mListItemClickListener);

        buttonAdd = (Button) findViewById(R.id.buttonAdd);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertItem();
            }
        });
        updateList();
    }

    @Override
    protected void onDestroy() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        super.onDestroy();
    }

    private void updateList() {
        aa.notifyDataSetChanged();
    }

    private void deleteItem(int position) {
        GroupItem item = GroupHelper.getGroupList(this).get(position);
        if (item == null) {
            Log.e(TAG, "deleteItem fail. item == null. position=" + position);
        }
        GroupHelper.deleteGroup(this, item.mId, item.mName);
        updateList();
    }

    private void insertItem() {
        LayoutInflater adbInflater = LayoutInflater.from(mContext);
        LinearLayout layout = (LinearLayout) adbInflater.inflate(R.layout.popup_input_title, null);

        final EditText editText = (EditText) layout.findViewById(R.id.editText);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(layout).setTitle("그룹 이름 입력");
        AlertDialog dialog = builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String title = editText.getText().toString();
                if (TextUtils.isEmpty(title)) {
                    return;
                }
                if (GroupHelper.insertGroup(mContext, title)) {
                    updateList();
                } else {
                    Log.e(TAG, "insert group failed.");
                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();
    }

    AdapterView.OnItemClickListener mListItemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

            GroupItem item = GroupHelper.getGroupList(mContext).get(position);
            if (item.mName.equals(GroupHelper.DEFAULT_GROUP)) {
                return;
            }

            LayoutInflater adbInflater = LayoutInflater.from(mContext);
            LinearLayout layout = (LinearLayout) adbInflater.inflate(R.layout.popup_select_group_option, null);

            RadioButton radioDelete;//, radioGoMock, radioMoveFavorite;

            radioDelete = (RadioButton) layout.findViewById(R.id.radioDelete);
            radioDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.delete);
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            deleteItem(position);
                            if (mDialog != null) {
                                mDialog.dismiss();
                                mDialog = null;
                            }
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }
                    }).show();

                }
            });


            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setView(layout).setTitle(item.mName);
            mDialog = builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (mDialog != null) {
                        mDialog.dismiss();
                        mDialog = null;
                    }
                }
            }).show();
        }
    };

    private class ListAdapter extends BaseAdapter {
        Context mainContext;
        LayoutInflater Inflater;
        ArrayList<GroupItem> arSrc;
        int layout;

        public ListAdapter(Context context, int alayout, ArrayList<GroupItem> aarSrc) {
            mainContext = context;
            Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            arSrc = aarSrc;
            layout = alayout;
        }

        // 아이템 갯수 리턴
        public int getCount() {
            return arSrc.size();
        }

        // 아이템 데이터 리턴
        public Object getItem(int position) {
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

            holder.textView.setText(strData);

            return view;
        }
    }

    static class ViewHolder {
        protected TextView textView;
    }
}
