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
import com.gst.gstfacedemo.model.PersonListBean;
import com.gst.gstfacedemo.util.Base64Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhenghangxia on 17-4-21.
 */

public class PersonListActivity extends BaseActivity {

    private String mPersonGroupId = "";
    private RecyclerView mRecyclerView;
    private TextView mCreateText;
    private List<PersonListBean> mList = new ArrayList<>();
    private MyRecyclerViewAdapter mAdapter;
    private boolean isLongClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        initToolbar();
        getDataFromGroupList();
        initUI();
        getPersonList();
        setRecyclerView();
        setListener();

    }

    private void setListener() {

        mCreateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final View view = LayoutInflater.from(PersonListActivity.this).inflate(
                        R.layout.layout_create_item,null);

                TextView personGroupIdET = (EditText)view.findViewById(R.id.personGroupIdET);
                personGroupIdET.setVisibility(View.GONE);

                new AlertDialog.Builder(PersonListActivity.this)
                        .setTitle("Create a person group")
                        .setView(view)
                        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                EditText nameET = (EditText)view.findViewById(R.id.nameET);
                                EditText userDataNameET = (EditText) view.findViewById(R.id.userDataName);
                                EditText userDataInfoET = (EditText) view.findViewById(R.id.userDataInfo);
                                String name = nameET.getText().toString().trim();
                                String userDataNameString = userDataNameET.getText().toString().trim();
                                String userDataInfoString = userDataInfoET.getText().toString().trim();


                                JSONObject json = new JSONObject();
                                try{
                                    json.put("name",userDataNameString);
                                    json.put("info",userDataInfoString);
                                    json.put("time",getDate());

                                }catch (Exception e) {
                                    e.printStackTrace();
                                }

                                String userDataString = json.toString();

                                createPerson(mPersonGroupId, name, userDataString);

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

    private void createPerson(String personId, String name, String userData) {

        CloudManager.createPerson(personId, name, userData, new CloudManager.CallBack() {
            @Override
            public void onFailure(final String resultString) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PersonListActivity.this,resultString,Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(final String resultString, Object resultObject) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PersonListActivity.this,resultString,Toast.LENGTH_SHORT).show();

                        getPersonList();
                    }
                });
            }
        });

    }

    private void getPersonList() {

        dialog.show();

        CloudManager.getPersonList(mPersonGroupId, new CloudManager.CallBack() {
            @Override
            public void onFailure(final String resultString) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PersonListActivity.this,resultString,
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
                                PersonListBean personListBean = new PersonListBean();
                                personListBean.setPersonId(jsonObject.getString("personId"));
                                personListBean.setName(jsonObject.getString("name"));
                                personListBean.setUserData(jsonObject.getString("userData"));
                                mList.add(personListBean);
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

    private void getDataFromGroupList() {

        mPersonGroupId = getIntent().getStringExtra("personGroupId");

    }

    /**
     *  初始化控件
     */
    private void initUI() {

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mCreateText = (TextView) findViewById(R.id.create);

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

    public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private Context mContext;
        private List<PersonListBean> mList;

        public MyRecyclerViewAdapter(Context mContext, List<PersonListBean> mList) {
            this.mContext = mContext;
            this.mList = mList;
        }

        @Override
        public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_list_item,parent,false);
            MyRecyclerViewAdapter.ViewHolder viewHolder = new MyRecyclerViewAdapter.ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MyRecyclerViewAdapter.ViewHolder holder, int position) {
            final PersonListBean personListBean = mList.get(position);

            String userDataString = personListBean.getUserData();
            try {
                JSONObject jsonObject = new JSONObject(userDataString);
                String departString = jsonObject.getString("departString");
                String sexString = jsonObject.getString("sexString");
                String ageString = jsonObject.getString("ageString");
                String timeString = jsonObject.getString("timeString");
                userDataString = "\n 部门: " + departString + ",\n 性别: " + sexString
                        + ",\n 年龄: " + ageString + ",\n 时间: " + timeString;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            holder.mTextView.setText("personId : " + personListBean.getPersonId() +
                    "\nname : " + personListBean.getName() +
                    "\nuserData : " + userDataString);

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
                                        EditText nameET = (EditText)dialogView.findViewById(R.id.nameET);
                                        EditText userDataNameET = (EditText) dialogView.findViewById(R.id.userDataName);
                                        EditText userDataInfoET = (EditText) dialogView.findViewById(R.id.userDataInfo);
                                        String nameString = nameET.getText().toString().trim();
                                        String userDataNameString = userDataNameET.getText().toString().trim();
                                        String userDataInfoString = userDataInfoET.getText().toString().trim();

                                        JSONObject json = new JSONObject();
                                        try{
                                            json.put("name",userDataNameString);
                                            json.put("info",userDataInfoString);
                                            json.put("time",getDate());

                                        }catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        String userDataString = json.toString();

                                        Log.e("userData--->",userDataString);
                                        Log.e("加密：",Base64Util.encode(userDataString));



                                        update(mPersonGroupId, personListBean.getPersonId() ,nameString, userDataString);
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
                        Intent intent = new Intent(mContext, PersonActivity.class);
                        intent.putExtra("personGroupId",mPersonGroupId);
                        intent.putExtra("personId", personListBean.getPersonId());
                        startActivity(intent);
                    }
                }
            });

            holder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    isLongClick = true;

                    new AlertDialog.Builder(PersonListActivity.this)
                            .setTitle("提示")
                            .setMessage("删除？请确认...")
                            .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deletePerson(personListBean.getPersonId());
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

            TextView mUpdate;
            TextView mTextView;
            CardView mCardView;

            public ViewHolder(View itemView) {
                super(itemView);
                mCardView = (CardView) itemView;
                mTextView = (TextView) itemView.findViewById(R.id.info_text);
                mUpdate = (TextView) itemView.findViewById(R.id.btn_update);
            }
        }
    }

    private void update(String personGroupId, String personId, String nameString, String userDataString) {

        CloudManager.updatePerson(personGroupId, personId, nameString, userDataString, new CloudManager.CallBack() {
            @Override
            public void onFailure(final String resultString) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PersonListActivity.this,resultString,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(String resultString, Object resultObject) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PersonListActivity.this,"update success",
                                Toast.LENGTH_SHORT).show();

                        getPersonList();

                    }
                });
            }
        });

    }

    private void deletePerson(String personId) {

        CloudManager.deletePerson(mPersonGroupId, personId, new CloudManager.CallBack() {
            @Override
            public void onFailure(final String resultString) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PersonListActivity.this,resultString,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(final String resultString, Object resultObject) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PersonListActivity.this,resultString,
                                Toast.LENGTH_SHORT).show();

                        getPersonList();

                    }
                });
            }
        });

    }

}
