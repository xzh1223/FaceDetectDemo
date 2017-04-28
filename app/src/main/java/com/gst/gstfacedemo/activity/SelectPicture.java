package com.gst.gstfacedemo.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.GridView;

import com.gst.gstfacedemo.R;
import com.gst.gstfacedemo.adapter.GridViewAdapter;
import com.gst.gstfacedemo.model.ImageBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhenghangxia on 17-4-24.
 */
public class SelectPicture extends BaseActivity {

    private GridView gridView;
    private Button okBtn;
    private List<ImageBean> mList = new ArrayList<>();
    private GridViewAdapter mAdapter;
    public String mPersonGroupId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_all);

        initToolbar();
        getDataFromIdentify();
        initUI();
        initData();
        setGridView();

    }

    private void getDataFromIdentify() {

        mPersonGroupId = getIntent().getStringExtra("mPersonGroupId");

    }

    private void setGridView() {
        mAdapter = new GridViewAdapter(this,mList);
        gridView.setAdapter(mAdapter);
    }

    /**
     *  初始化数据
     */
    private void initData() {
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, null,null,null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String string = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

                ImageBean imageBean = new ImageBean();
                imageBean.setImagePath(string);
                imageBean.setChecked(false);

                mList.add(imageBean);
                Log.e("-------->","添加成功");
            }
            cursor.close();
        }

    }

    private void initUI() {
        gridView = (GridView) findViewById(R.id.gridView);
        okBtn = (Button) findViewById(R.id.btn_ok);
    }

}
