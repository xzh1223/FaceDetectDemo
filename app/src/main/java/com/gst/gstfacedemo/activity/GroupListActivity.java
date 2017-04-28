package com.gst.gstfacedemo.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gst.gstfacedemo.R;
import com.gst.gstfacedemo.controller.CloudManager;
import com.gst.gstfacedemo.model.GroupListBean;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GroupListActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private List<GroupListBean> mList = new ArrayList<GroupListBean>();
    private MyRecyclerViewAdapter mAdapter;
    private TextView mCreateText;
    private boolean isLongClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        initToolbar();
        initUI();
        getPersonGroupList();
        setRecyclerView();
        setListener();

    }

    /**
     *  设置点击监听事件
     */
    private void setListener() {

        mCreateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final View view = LayoutInflater.from(GroupListActivity.this).inflate(
                        R.layout.layout_create_item,null);

                new AlertDialog.Builder(GroupListActivity.this)
                        .setTitle("Create a person group")
                        .setView(view)
                        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                EditText personGroupIdET = (EditText)view.findViewById(R.id.personGroupIdET);
                                EditText nameET = (EditText)view.findViewById(R.id.nameET);
//                                EditText userDataET = (EditText)view.findViewById(R.id.userDataET);
                                EditText userDataNameET = (EditText) view.findViewById(R.id.userDataName);
                                EditText userDataInfoET = (EditText) view.findViewById(R.id.userDataInfo);

                                String personGroupId = personGroupIdET.getText().toString().trim();
                                String userDataNameString = userDataNameET.getText().toString().trim();
                                String userDataInfoString = userDataInfoET.getText().toString().trim();
                                String nameString = nameET.getText().toString().trim();

                                JSONObject json = new JSONObject();
                                try{
                                    json.put("name",userDataNameString);
                                    json.put("info",userDataInfoString);
                                    json.put("time",getDate());
                                }catch (Exception e) {
                                    e.printStackTrace();
                                }

                                String userDataString = json.toString();
                                createPersonGroup(personGroupId, nameString, userDataString);

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();

            }
        });

    }


    private void deletePerson(String personGroupId) {

        CloudManager.deletePersonGroup(personGroupId, new CloudManager.CallBack() {
            @Override
            public void onFailure(final String resultString) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GroupListActivity.this,resultString,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(final String resultString, Object resultObject) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GroupListActivity.this,resultString,
                                Toast.LENGTH_SHORT).show();

                        getPersonGroupList();

                    }
                });
            }
        });

    }

    private void createPersonGroup(String personGroupId, String name, String userData) {


        CloudManager.createPersonGroup(personGroupId, name, userData, new CloudManager.CallBack() {
            @Override
            public void onFailure(final String resultString) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GroupListActivity.this,resultString,Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(final String resultString, Object resultObject) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GroupListActivity.this,resultString,Toast.LENGTH_SHORT).show();

                        getPersonGroupList();

                    }
                });
            }
        });

    }

    private void getPersonGroupList() {

        dialog.show();

        CloudManager.getPersonGroupList(new CloudManager.CallBack() {
            @Override
            public void onFailure(final String resultString) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GroupListActivity.this,resultString,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(final String resultString, Object resultObject) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mList.clear();
                        try {
                            JSONArray jsonArray = new JSONArray(resultString);
                            for (int i=0;i<jsonArray.length();i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                GroupListBean groupListBean = new GroupListBean();
                                groupListBean.setPersonGroupId(jsonObject.getString("personGroupId"));
                                groupListBean.setName(jsonObject.getString("name"));
                                groupListBean.setUserData(jsonObject.getString("userData"));
                                mList.add(groupListBean);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Log.e("mList--->",mList.toString());
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

    }

    /**
     *  设置 RecyclerView 属性
     */
    private void setRecyclerView() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new MyRecyclerViewAdapter(this,mList);
        mRecyclerView.setAdapter(mAdapter);

    }

    /**
     *  初始化控件
     */
    private void initUI() {

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mCreateText = (TextView) findViewById(R.id.create);

    }

    public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private Context mContext;
        private List<GroupListBean> mList;

        public MyRecyclerViewAdapter(Context mContext, List<GroupListBean> mList) {
            this.mContext = mContext;
            this.mList = mList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_list_item,parent,false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final GroupListBean groupListBean = mList.get(position);

            holder.mTextView.setText("personGroupId : " + groupListBean.getPersonGroupId() +
                    "\nname : " + groupListBean.getName() +
                    "\nuserData : " + groupListBean.getUserData());

            dialog.dismiss();

            holder.mUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (isLongClick) {

                    } else {
                        final View dialogView = LayoutInflater.from(mContext).inflate(R.layout.layout_create_item,null);
                        EditText personGroupIdET = (EditText) dialogView.findViewById(R.id.personGroupIdET);

                        personGroupIdET.setVisibility(View.GONE);
                        new AlertDialog.Builder(mContext)
                                .setTitle("提示")
                                .setView(dialogView)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        EditText nameET = (EditText) dialogView.findViewById(R.id.nameET);
                                        EditText userDataNameET = (EditText) dialogView.findViewById(R.id.userDataName);
                                        EditText userDataInfoET = (EditText) dialogView.findViewById(R.id.userDataInfo);

                                        String userDataNameString = userDataNameET.getText().toString().trim();
                                        String userDataInfoString = userDataInfoET.getText().toString().trim();
                                        String nameString = nameET.getText().toString().trim();

                                        JSONObject json = new JSONObject();
                                        try{
                                            json.put("name",userDataNameString);
                                            json.put("info",userDataInfoString);
                                            json.put("time",getDate());
                                        }catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        String userDataString = json.toString();

                                        update(groupListBean.getPersonGroupId(),nameString, userDataString);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }

                }
            });

            holder.mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isLongClick) {

                    } else {
                        Intent intent = new Intent(mContext, PersonListActivity.class);
                        intent.putExtra("personGroupId", groupListBean.getPersonGroupId());
                        startActivity(intent);
                    }
                }
            });

            holder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    isLongClick = true;

                    new AlertDialog.Builder(GroupListActivity.this)
                            .setTitle("提示")
                            .setMessage("删除？请确认...")
                            .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deletePerson(groupListBean.getPersonGroupId());
                                    isLongClick = false;
                                }
                            })
                            .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    isLongClick = false;
                                }
                            })
                            .setCancelable(false)
                            .show();
                    return false;
                }
            });

        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView mTextView;
            TextView mUpdate;
            CardView mCardView;

            public ViewHolder(View itemView) {
                super(itemView);
                mCardView = (CardView) itemView;
                mTextView = (TextView) itemView.findViewById(R.id.info_text);
                mUpdate = (TextView) itemView.findViewById(R.id.btn_update);
            }
        }
    }

    private void update(String personGroupId, String nameString, String userDataString) {

        Log.e("---->",personGroupId +"---"+ nameString +"---"+ userDataString);

        CloudManager.updatePersonGroup(personGroupId, nameString, userDataString, new CloudManager.CallBack() {
            @Override
            public void onFailure(String resultString) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GroupListActivity.this,"update failed",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(String resultString, Object resultObject) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GroupListActivity.this,"update success",Toast.LENGTH_SHORT).show();

                        getPersonGroupList();

                    }
                });
            }
        });

    }

}
