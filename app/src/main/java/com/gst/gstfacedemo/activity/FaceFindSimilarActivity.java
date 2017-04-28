package com.gst.gstfacedemo.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gst.gstfacedemo.R;
import com.gst.gstfacedemo.controller.CloudManager;
import com.gst.gstfacedemo.model.FindSimilarBean;
import com.gst.gstfacedemo.util.ImageTools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FaceFindSimilarActivity extends BaseActivity implements View.OnClickListener {

    public static final int TAKE_PHOTO = 503;
    private ImageView mImgDetect;
    private TextView mTvResult;

    private Button mBtnSelectPicture;
    private Button mBtnFind;

    private ListView mLvFindResult;
    private FindResultAdapter mFindAdapter;
    private List<FindSimilarBean> mResultPaths = new ArrayList<FindSimilarBean>();

    private Bitmap mSelectBitmap;

    private String findFaceId = "";
    private String mFaceListId = "";
    private TextView mCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_similar);

        initToolbar();
        initUI();
        getDataFromFaceList();
    }

    private void getDataFromFaceList() {

        mFaceListId = getIntent().getStringExtra("mFaceListId");

    }

    private void initUI() {

        mCreate = (TextView) findViewById(R.id.create);
        mCreate.setVisibility(View.GONE);

        mBtnSelectPicture = (Button) findViewById(R.id.btn_select_picture);
        mBtnFind = (Button) findViewById(R.id.btn_find);
        mLvFindResult = (ListView) findViewById(R.id.lv_find_result);

        mFindAdapter = new FindResultAdapter();
        mLvFindResult.setAdapter(mFindAdapter);

        mBtnSelectPicture.setOnClickListener(this);
        mBtnFind.setOnClickListener(this);

        mImgDetect = (ImageView) findViewById(R.id.img_detect);
        mTvResult = (TextView) findViewById(R.id.tv_result);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_select_picture: {
                findFaceId = "";
                takePhoto();
                break;
            }
            case R.id.btn_find: {
                FindSimilar();
                break;
            }
        }
    }

    private void FindSimilar() {
        if (findFaceId.length() <= 0) {
            Toast.makeText(this, "Please select picture to find", Toast.LENGTH_SHORT).show();
            return;
        }

        mTvResult.setText("Loading...");

        dialog.show();

        CloudManager.FindSimilar(findFaceId, mFaceListId, new CloudManager.CallBack() {
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
            public void onResponse(final String resultString, Object resultValue) {
                ArrayList<FindSimilarBean> resultList = (ArrayList<FindSimilarBean>) resultValue;
                if (resultList != null) {
                    mResultPaths = resultList;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mFindAdapter.notifyDataSetChanged();
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvResult.setText("find similar success\n\n" + resultString);
                        }
                    });
                }
                else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvResult.setText(resultString);
                        }
                    });
                }
            }
        });
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    class FindResultAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public FindResultAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mResultPaths.size();
        }

        @Override
        public Object getItem(int position) {
            return mResultPaths.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.lv_findresult_item, null);
                holder = new ViewHolder();
                holder.imgPhoto = (ImageView) convertView.findViewById(R.id.img_photo);
                holder.tvSimilar = (TextView) convertView.findViewById(R.id.tv_similar);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            FindSimilarBean bean = mResultPaths.get(position);
            String filePath = ImageTools.IMAGE_PATH + "/" + bean.persistedFaceId + ".png";
            File file = new File(filePath);
            if (file.exists()) {
                Bitmap bm = BitmapFactory.decodeFile(filePath);
                //将图片显示到ImageView中
                holder.imgPhoto.setImageBitmap(bm);
            }
            holder.tvSimilar.setText(bean.confidence + "");

            dialog.dismiss();

            return convertView;
        }

        class ViewHolder {
            ImageView imgPhoto;
            TextView tvSimilar;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case TAKE_PHOTO: {
                    Bundle bundle = data.getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");

                    mImgDetect.setImageBitmap(bitmap);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] dataByte = baos.toByteArray();

                    mTvResult.setText("Loading...");

                    dialog.show();

                    CloudManager.Detect(dataByte, new CloudManager.CallBack() {
                        @Override
                        public void onFailure(final String result) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTvResult.setText(result);
                                    dialog.dismiss();
                                }
                            });
                        }

                        @Override
                        public void onResponse(final String result, Object resultObject) {
                            findFaceId = (String)resultObject;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTvResult.setText("detect success\n\n" + result);
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
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
                mImgDetect.setImageBitmap(smallBitmap);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
