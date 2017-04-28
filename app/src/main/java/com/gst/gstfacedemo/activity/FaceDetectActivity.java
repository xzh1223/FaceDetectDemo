package com.gst.gstfacedemo.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gst.gstfacedemo.R;
import com.gst.gstfacedemo.controller.CloudManager;
import com.gst.gstfacedemo.util.ImageTools;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FaceDetectActivity extends BaseActivity implements View.OnClickListener {

    public static final int PICK_PHOTO = 501;

    private ImageView mImgDetect;
    private TextView mTvResult;

    private Button mBtnPicture;
    private Button mBtnDetect;


    private Bitmap mSelectBitmap;
    private TextView mCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detect);

        initToolbar();
        initUI();
    }

    private void initUI() {

        mCreate = (TextView) findViewById(R.id.create);
        mCreate.setVisibility(View.GONE);

        mBtnPicture = (Button) findViewById(R.id.btn_picture);
        mBtnDetect = (Button) findViewById(R.id.btn_detect);

        mBtnPicture.setOnClickListener(this);
        mBtnDetect.setOnClickListener(this);

        mImgDetect = (ImageView) findViewById(R.id.img_detect);
        mTvResult = (TextView) findViewById(R.id.tv_result);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_picture: {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_PHOTO);
                break;
            }
            case R.id.btn_detect: {
                dialog.show();
                Detect();
                break;
            }
        }
    }


    private void Detect() {
        if(mSelectBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            mSelectBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] dataByte = baos.toByteArray();

            CloudManager.Detect("age,gender,smile,facialHair,headPose,glasses,emotion", dataByte, new CloudManager.CallBack() {
                @Override
                public void onFailure(final String result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvResult.setText(result);
                        }
                    });
                }

                @Override
                public void onResponse(final String result,final Object resultObject) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvResult.setText(result);
                            dialog.dismiss();
                        }
                    });
                }
            });
        }
        else {
            Toast.makeText(this, "Please select picture", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_PHOTO:
                    pickPhotoCallBack(data);
                    break;
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

                mSelectBitmap = smallBitmap;
                mImgDetect.setImageBitmap(smallBitmap);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
