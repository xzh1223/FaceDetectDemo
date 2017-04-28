package com.gst.gstfacedemo.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gst.gstfacedemo.R;
import com.gst.gstfacedemo.controller.CloudManager;
import com.gst.gstfacedemo.util.ImageTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IdentifyActivity extends BaseActivity {

    private TextView mResultText;
    private GridView mGridView;
    private TextView mCreateText;
    private TextView mIdentifyText;
    private String mPersonGroupId = "";
    private List<String> mImageList = new ArrayList<>();
    private List<String> mList = new ArrayList<>();
    private String mResultString = "";
    private GridViewAdapter mAdapter;
    private TextView mVerityTV;
    private TextView mGroup;
    private int num = 0;
    private String mStatus = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person2);

        initToolbar();
        initUI();
        getDataFromPerson();
        train();
        getDataFromSelectPicture();
        setListener();


    }

    private void setGridView() {

        Log.e("mImageList.size()---->",""+mImageList.size());

        mAdapter = new GridViewAdapter(IdentifyActivity.this,mImageList);

        mGridView.setAdapter(mAdapter);

    }

    private void train() {

        CloudManager.getTrainingStatus(mPersonGroupId, new CloudManager.CallBack() {
            @Override
            public void onFailure(final String resultString) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mResultText.setText(resultString);
                    }
                });
            }

            @Override
            public void onResponse(final String resultString, Object resultObject) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mResultText.setText(resultString);
                        mStatus = resultString;
                        Log.e("mStatus---->",mStatus);
                    }
                });
            }
        });

        if (!mStatus.equals("succeeded")) {

            CloudManager.train(mPersonGroupId, new CloudManager.CallBack() {
                @Override
                public void onFailure(final String resultString) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mResultText.setText(resultString);
                        }
                    });
                }

                @Override
                public void onResponse(final String resultString, Object resultObject) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mResultText.setText(resultString);
                        }
                    });
                }
            });
        } else {

        }

    }

    private void getDataFromSelectPicture() {

        mImageList = getIntent().getStringArrayListExtra("mImageList");

        try {
            handlePicture();
            setGridView();
        }catch (Exception e) {

        }
    }

    private void handlePicture() {

                if (num < mImageList.size()) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                Bitmap mBitmap = BitmapFactory.decodeFile(mImageList.get(num));

                                Log.e("num--->",num+"");
                                if (mBitmap != null) {

                                    int degree = ImageTools.getImageDegree(mImageList.get(num));
                                    Bitmap smallBitmap = ImageTools.zoomBitmap(mBitmap, degree);
                                    //释放原始图片占用的内存，防止out of memory异常发生
                                    mBitmap.recycle();

                                    Detect(smallBitmap);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
            }
    }

    private void Detect(Bitmap bitmap) {

        if(bitmap != null) {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] dataByte = baos.toByteArray();

            CloudManager.Detect("age,gender,smile,facialHair,headPose,glasses,emotion", dataByte, new CloudManager.CallBack() {
                @Override
                public void onFailure(final String result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mResultString += result;
                            mResultText.setText(result);
                        }
                    });
                }

                @Override
                public void onResponse(final String result,final Object resultObject) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mResultString += result;
                            mResultText.setText(mResultString);

                            try {
                                JSONArray jsonArray = new JSONArray(result);
                                for (int i=0;i<jsonArray.length();i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String faceId = jsonObject.getString("faceId");
                                    mList.add(faceId);

                                }
                                num++;
                                handlePicture();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        }
        else {
            Toast.makeText(this, "Please select picture", Toast.LENGTH_SHORT).show();
        }

    }

    private void setListener() {

        mCreateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(IdentifyActivity.this,SelectPicture.class);
                intent.putExtra("mPersonGroupId",mPersonGroupId);
                startActivity(intent);
                finish();

            }
        });

        mIdentifyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mList.size() <= 0) {
                    Toast.makeText(IdentifyActivity.this,"请先选择图片再进行Identify",Toast.LENGTH_SHORT).show();
                }else {
                    CloudManager.identify(mPersonGroupId, mList, new CloudManager.CallBack() {
                        @Override
                        public void onFailure(final String resultString) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mResultText.setText(resultString);
                                }
                            });
                        }

                        @Override
                        public void onResponse(final String resultString, Object resultObject) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mResultText.setText(resultString);
                                }
                            });
                        }
                    });
                }
            }
        });

        mGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mList.size() <= 0) {
                    Toast.makeText(IdentifyActivity.this,"请先选择图片再进行Group",Toast.LENGTH_SHORT).show();
                }else {

                    /*Intent intent = new Intent(IdentifyActivity.this, GroupActivity.class);
                    intent.putStringArrayListExtra("mList", (ArrayList<String>) mList);
                    Log.e("mList------>",mList.size()+"");
                    startActivity(intent);*/
                    CloudManager.getGroup(mList, new CloudManager.CallBack() {
                        @Override
                        public void onFailure(final String resultString) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    mResultText.setText(resultString);

                                }
                            });
                        }

                        @Override
                        public void onResponse(final String resultString, Object resultObject) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    Log.e("group--->", resultString);

                                    mResultText.setText(resultString);

                                }
                            });

                        }
                    });

                }
            }
        });

    }

    private void getDataFromPerson() {

        mPersonGroupId = getIntent().getStringExtra("mPersonGroupId");

    }

    private void initUI() {

        mResultText = (TextView) findViewById(R.id.resultText);

        mGridView = (GridView) findViewById(R.id.gridView);

        mCreateText = (TextView) findViewById(R.id.create);
        mCreateText.setText("Select face");

        mIdentifyText = (TextView) findViewById(R.id.findSimilarTV);
        mIdentifyText.setText("Begin Identify");
        mGroup = (TextView) findViewById(R.id.VerityTV);
        mGroup.setText("Group");

    }

    class GridViewAdapter extends ArrayAdapter {

        private Context mContext;
        private List mList;

        public GridViewAdapter(Context context, List mList) {
            super(context, 0, mList);
            this.mContext = context;
            this.mList = mList;
            Log.e("mList.get(0)",mList.get(0).toString());
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            final String mParams = (String)getItem(position);

            if (convertView == null) {

                convertView = LayoutInflater.from(mContext).inflate(R.layout.lv_findresult_item,parent,false);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.img_photo);

                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (mParams != null) {

                File file = new File(mParams);

                if (file.exists()) {

                    Glide.with(mContext).load(mParams).into(viewHolder.imageView);
                } else {
                    Toast.makeText(IdentifyActivity.this,"no",Toast.LENGTH_SHORT).show();
                }

            } else {

            }

            dialog.dismiss();
            return convertView;
        }

        private class ViewHolder {

            ImageView imageView;
        }

    }

}
