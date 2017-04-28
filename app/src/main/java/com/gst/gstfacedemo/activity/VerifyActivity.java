package com.gst.gstfacedemo.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.gst.gstfacedemo.R;
import com.gst.gstfacedemo.controller.CloudManager;
import com.gst.gstfacedemo.util.ImageTools;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class VerifyActivity extends BaseActivity {

    private ImageView mImageView;
    private ImageView mImageView2;
    private TextView mResultText;
    private Button mBtnSelect;
    public static final int UPLOAD_FACE = 502;
    private Bitmap mSelectBitmap;
    private TextView mCreateText;
    private String mFaceId;
    private String mPersonGroupId = "";
    private String mPersonId = "";
    private String mFaceId2 = "";
    private int num = 0;
    private Button mBtnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verity);

        initToolbar();
        getDataFromPerson();
        initUI();
        setListener();


    }

    private void getDataFromPerson() {

        mPersonGroupId = getIntent().getStringExtra("mPersonGroupId");
        mPersonId = getIntent().getStringExtra("mPersonId");

    }

    private void setListener() {

        mBtnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (num >= 2) {
                        Toast.makeText(VerifyActivity.this, "您已经选择了两张图片，请退出后再选择", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, UPLOAD_FACE);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });



        mCreateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                verify();
            }
        });

        mBtnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                num = 0;

            }
        });

    }

    private void verify() {
        try {
            if (mFaceId2 == null) {
                mFaceId2 = "";
            }else if (mFaceId == null) {
                mFaceId = "";
            }
            Log.e("mFaceId---->",mFaceId + "----" + mFaceId2);

            dialog.show();

            CloudManager.verify(mFaceId, mFaceId2, mPersonGroupId, mPersonId, new CloudManager.CallBack() {
                @Override
                public void onFailure(final String resultString) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mResultText.setText(resultString);
                            dialog.dismiss();
                        }
                    });
                }

                @Override
                public void onResponse(final String resultString, Object resultObject) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mResultText.setText(resultString);
                            dialog.dismiss();
                        }
                    });
                }
            });
        }catch (Exception e) {
            Toast.makeText(VerifyActivity.this,"cuowu",Toast.LENGTH_SHORT).show();
        }

    }

    private void initUI() {

        mImageView = (ImageView) findViewById(R.id.img_detect);
        mImageView2 = (ImageView) findViewById(R.id.img_detect2);
        mResultText = (TextView) findViewById(R.id.resultText);
        mBtnSelect = (Button) findViewById(R.id.btn_select);
        mBtnReset = (Button) findViewById(R.id.btn_reset);

        mCreateText = (TextView) findViewById(R.id.create);
        mCreateText.setText("Verify");
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

                    detect(dataByte);

                    break;
                }

            }
        }
    }

    private void detect(byte[] dataByte) {

        dialog.show();

        CloudManager.Detect("age,gender,smile,facialHair,headPose,glasses,emotion", dataByte, new CloudManager.CallBack() {
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
            public void onResponse(final String result,final Object resultObject) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mResultText.setText(result);
                        try {
                            JSONArray jsonArray = new JSONArray(result);
                            for (int i=0;i<jsonArray.length();i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                if (num ==0) {
                                    mImageView.setImageBitmap(mSelectBitmap);
                                    mFaceId = jsonObject.getString("faceId");
                                    dialog.dismiss();
                                } else if (num == 1) {
                                    mImageView2.setImageBitmap(mSelectBitmap);
                                    mFaceId2 = jsonObject.getString("faceId");
                                    dialog.dismiss();
                                }
                            }
                            num++;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

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

}
