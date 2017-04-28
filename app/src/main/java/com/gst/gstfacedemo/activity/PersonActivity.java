package com.gst.gstfacedemo.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gst.gstfacedemo.R;
import com.gst.gstfacedemo.controller.CloudManager;
import com.gst.gstfacedemo.util.Base64Util;
import com.gst.gstfacedemo.util.ImageTools;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PersonActivity extends BaseActivity {

    private String mPersonGroupId = "";
    private String mPersonId = "";
    private TextView mResultText;
    private GridView mGridView;
    private TextView mCreateText;
    public static final int UPLOAD_FACE = 502;
    private Bitmap mSelectBitmap;
    private List<String> mList = new ArrayList<>();
    private GridViewAdapter mAdapter;
    private TextView mIdentifyText;
    private ArrayList<String> mImageList = new ArrayList<>();
    private TextView mVerityText;
    private String userDataString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person2);

        initToolbar();
        getDataFromPersonList();
        initUI();
        getPerson();
        setListener();
        setGridView();

    }

    private void setGridView() {

        mAdapter = new GridViewAdapter(PersonActivity.this,mList);

        mGridView.setAdapter(mAdapter);

    }

    private void setListener() {

        mCreateText.setText("Add Face");

        mCreateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                final View view = LayoutInflater.from(PersonActivity.this).inflate(R.layout.layout_create_item,null);
                EditText personGroupIdET = (EditText) view.findViewById(R.id.personGroupIdET);
                EditText nameET = (EditText) view.findViewById(R.id.nameET);
                personGroupIdET.setVisibility(View.GONE);
                nameET.setVisibility(View.GONE);

                new AlertDialog.Builder(PersonActivity.this)
                        .setTitle("提示")
                        .setView(view)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                EditText userDataNameET = (EditText) view.findViewById(R.id.userDataName);
                                EditText userDataInfoET = (EditText) view.findViewById(R.id.userDataInfo);


                                String userDataNameString = userDataNameET.getText().toString().trim();
                                String userDataInfoString = userDataInfoET.getText().toString().trim();

                                JSONObject json = new JSONObject();
                                try {
                                    json.put("name",userDataNameString);
                                    json.put("info",userDataInfoString);
                                    json.put("time",getDate());
                                }catch (Exception e ){
                                    e.printStackTrace();
                                }
                                userDataString = json.toString();

                                userDataString = Base64Util.encode(userDataString);

                                if (userDataString.length()<=0) {
                                    Toast.makeText(PersonActivity.this,"请输入您的个人信息。。。",Toast.LENGTH_SHORT).show();
                                }else {
                                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                    intent.setType("image/*");
                                    startActivityForResult(intent, UPLOAD_FACE);
                                }
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

        mIdentifyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(PersonActivity.this,IdentifyActivity.class);
                intent.putExtra("mPersonGroupId",mPersonGroupId);
                startActivity(intent);
                Toast.makeText(PersonActivity.this,mPersonGroupId.toString(),Toast.LENGTH_SHORT).show();

            }
        });

        mVerityText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(PersonActivity.this,VerifyActivity.class);
                intent.putExtra("mPersonGroupId",mPersonGroupId);
                intent.putExtra("mPersonId",mPersonId);
                startActivity(intent);

            }
        });

    }

    private void addFace(byte[] dataByte, String userData) {

        CloudManager.addPersonFace(mPersonGroupId, mPersonId, dataByte, userData, new CloudManager.CallBack() {
            @Override
            public void onFailure(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mResultText.setText(result);
                    }
                });
            }

            @Override
            public void onResponse(final String resultString, Object resultObject) {
                if (ImageTools.checkSDCardAvailable()) {
                    ImageTools.savePhotoToSDCard(mSelectBitmap, ImageTools.IMAGE_PATH,resultString);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mResultText.setText("Not found SDCard");
                        }
                    });
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mResultText.setText("add picture success\n" + "persistedFaceId:\n" + resultString);

                        getPerson();
                    }
                });
            }
        });

    }

    private void getPerson() {

        dialog.show();
        mResultText.setText("Loading...");

        CloudManager.getPerson(mPersonGroupId, mPersonId, new CloudManager.CallBack() {
            @Override
            public void onFailure(final String resultString) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PersonActivity.this,resultString,Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(final String resultString, Object resultObject) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PersonActivity.this,resultString,Toast.LENGTH_SHORT).show();
                        mList.clear();
                        try {
                            JSONObject jsonObject = new JSONObject(resultString);
                            JSONArray jsonArray = new JSONArray(jsonObject.getString("persistedFaceIds"));
                            for (int i=0;i<jsonArray.length();i++) {
                                String mString = (String) jsonArray.get(i);
                                mList.add(mString);
                            }

                        } catch (Exception e) {
                            Log.e("------->",e.toString());
                            e.printStackTrace();
                        }
                        mResultText.setText(resultString);

                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

    }

    private void initUI() {

        mResultText = (TextView) findViewById(R.id.resultText);

        mGridView = (GridView) findViewById(R.id.gridView);

        mCreateText = (TextView) findViewById(R.id.create);

        mIdentifyText = (TextView) findViewById(R.id.findSimilarTV);
        mIdentifyText.setText("Identify");

        mVerityText = (TextView) findViewById(R.id.VerityTV);

    }

    private void getDataFromPersonList() {

        mPersonGroupId = getIntent().getStringExtra("personGroupId");
        mPersonId = getIntent().getStringExtra("personId");

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case UPLOAD_FACE: {
                    //data中自带有返回的uri
                    pickPhotoCallBack(data);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    mSelectBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] dataByte = baos.toByteArray();
                    mResultText.setText("Loading...");

                    addFace(dataByte,userDataString);
                    break;
                }

            }
        }
    }

    private void pickPhotoCallBack(Intent data) {
        ContentResolver resolver = getContentResolver();
        //照片的原始资源地址
        Uri originalUri = data.getData();
        try {
            //使用ContentProvider通过URI获取原始图片
            Bitmap photo = MediaStore.Images.Media.getBitmap(resolver, originalUri);
            if (photo != null) {
                //为防止原始图片过大导致内存溢出，这里先缩小原图显示，然后释放原始Bitmap占用的内存
                String imagePath = ImageTools.getImagePathByUri(this, originalUri);
                int degree = ImageTools.getImageDegree(imagePath);
                Bitmap smallBitmap = ImageTools.zoomBitmap(photo, degree);
                //释放原始图片占用的内存，防止out of memory异常发生
                photo.recycle();
                if (ImageTools.checkSDCardAvailable()) {
                    ImageTools.savePhotoToSDCard(smallBitmap, ImageTools.IMAGE_PATH, ImageTools.IMAGE_TEMP_NAME);
                } else {
                    Toast.makeText(this, "Not found SDCard", Toast.LENGTH_SHORT).show();
                }

                mSelectBitmap = smallBitmap;
//                mImgDetect.setImageBitmap(smallBitmap);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class GridViewAdapter extends ArrayAdapter {

        private Context mContext;
        private List mList;

        public GridViewAdapter(Context context, List mList) {
            super(context, 0, mList);
            this.mContext = context;
            this.mList = mList;
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

                String filePath = ImageTools.IMAGE_PATH + "/" + mParams + ".png";

                File file = new File(filePath);

                if (file.exists()) {
                    Bitmap bm = BitmapFactory.decodeFile(filePath);
                    //将图片显示到ImageView中
                    viewHolder.imageView.setImageBitmap(bm);
                } else {

                }


                viewHolder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        new AlertDialog.Builder(mContext)
                                .setTitle("提示")
                                .setMessage("是否确认删除选中项？")
                                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        deleteFace(mParams);

                                    }
                                })
                                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();

                        return false;
                    }
                });

            } else {

            }

            dialog.dismiss();

            return convertView;
        }

        private class ViewHolder {

            ImageView imageView;
        }

    }

    private void deleteFace(String mPersistedFaceId) {

        CloudManager.deletePersonFace(mPersonGroupId, mPersonId, mPersistedFaceId, new CloudManager.CallBack() {
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

                        getPerson();
                    }
                });
            }
        });
    }


}
