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
import com.gst.gstfacedemo.model.FaceListsBean;
import com.gst.gstfacedemo.util.Base64Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhenghangxia on 17-4-21.
 */
public class FaceListsActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private TextView mCreateText;
    private List<FaceListsBean> mList = new ArrayList<>();
    private boolean isLongClick = false;
    private MyRecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        initToolbar();
        initUI();
        getFaceLists();
        setRecyclerView();
        setListener();

    }

    private void setListener() {

        mCreateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final View view = LayoutInflater.from(FaceListsActivity.this).inflate(
                        R.layout.layout_create_item,null);

                new AlertDialog.Builder(FaceListsActivity.this)
                        .setTitle("Create a face list")
                        .setView(view)
                        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                EditText personGroupIdET = (EditText)view.findViewById(R.id.personGroupIdET);
                                EditText nameET = (EditText)view.findViewById(R.id.nameET);
                                EditText userDataNameET = (EditText) view.findViewById(R.id.userDataName);
                                EditText userDataInfoET = (EditText) view.findViewById(R.id.userDataInfo);

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

                                userDataString = Base64Util.encode(userDataString);

                                String personGroupId = personGroupIdET.getText().toString().trim();


                                createFaceList(personGroupId, nameString, userDataString);

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

    private void deleteFaceList(String faceListId) {

        CloudManager.deleteFaceList(faceListId, new CloudManager.CallBack() {
            @Override
            public void onFailure(final String resultString) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FaceListsActivity.this,resultString,Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(final String resultString, Object resultObject) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(FaceListsActivity.this,resultString,Toast.LENGTH_SHORT).show();

                        getFaceLists();

                    }
                });
            }
        });

    }

    private void createFaceList(String faceListId, String name, String userData) {

        CloudManager.createFaceList(faceListId, name, userData, new CloudManager.CallBack() {
            @Override
            public void onFailure(final String resultString) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FaceListsActivity.this,resultString,Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(final String resultString, Object resultObject) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FaceListsActivity.this,resultString,Toast.LENGTH_SHORT).show();

                        getFaceLists();
                    }
                });
            }
        });

    }

    private void getFaceLists() {

        dialog.show();

        CloudManager.getFaceLists("", new CloudManager.CallBack() {
            @Override
            public void onFailure(final String resultString) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FaceListsActivity.this,resultString,Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(final String resultString, Object resultObject) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(FaceListsActivity.this,resultString,Toast.LENGTH_SHORT).show();

                        mList.clear();

                        try {
                            JSONArray jsonArray = new JSONArray(resultString);
                            for (int i=0;i<jsonArray.length();i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                FaceListsBean faceListsBean = new FaceListsBean();
                                faceListsBean.setFaceListId(jsonObject.getString("faceListId"));
                                faceListsBean.setName(jsonObject.getString("name"));
                                faceListsBean.setUserData(jsonObject.getString("userData"));
                                mList.add(faceListsBean);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
        private List<FaceListsBean> mList;

        public MyRecyclerViewAdapter(Context mContext, List<FaceListsBean> mList) {
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

            final FaceListsBean faceListBean = mList.get(position);

            String userDataString = Base64Util.decode(faceListBean.getUserData());
            Log.e("------>",userDataString);

            holder.mTextView.setText("faceListId : " + faceListBean.getFaceListId() +
                    "\nname : " + faceListBean.getName() +
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

                                        userDataString = Base64Util.encode(userDataString);

                                        update(faceListBean.getFaceListId(), nameString, userDataString);
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
                        Intent intent = new Intent(mContext, FaceListActivity.class);
                        intent.putExtra("faceListId",faceListBean.getFaceListId());
                        startActivity(intent);
                    }
                }
            });

            holder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    isLongClick = true;

                    new AlertDialog.Builder(FaceListsActivity.this)
                            .setTitle("提示")
                            .setMessage("删除？请确认...")
                            .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteFaceList(faceListBean.getFaceListId());
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

    private void update(String faceListId, String nameString, String userDataString) {

        CloudManager.updateFaceList(faceListId, nameString, userDataString, new CloudManager.CallBack() {
            @Override
            public void onFailure(final String resultString) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FaceListsActivity.this,resultString,Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(String resultString, Object resultObject) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FaceListsActivity.this,"update success",Toast.LENGTH_SHORT).show();

                        getFaceLists();
                    }
                });
            }
        });

    }

}
