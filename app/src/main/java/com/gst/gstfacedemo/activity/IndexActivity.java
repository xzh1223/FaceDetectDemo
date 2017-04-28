package com.gst.gstfacedemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import com.gst.gstfacedemo.R;

public class IndexActivity extends AppCompatActivity {

    private RelativeLayout mIntoGroupList;
    private RelativeLayout mIntoFaceList;
    private RelativeLayout mIntoDetect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        initUI();
        setListener();

    }

    /**
     *  设置点击监听事件
     */
    private void setListener() {

        mIntoGroupList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(IndexActivity.this,GroupListActivity.class);
                startActivity(intent);

            }
        });

        mIntoFaceList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(IndexActivity.this,FaceListsActivity.class);
                startActivity(intent);

            }
        });

        mIntoDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(IndexActivity.this,FaceDetectActivity.class);
                startActivity(intent);

            }
        });

    }

    /**
     *  初始化控件
     */
    private void initUI() {

        mIntoGroupList = (RelativeLayout) findViewById(R.id.into_groupList);
        mIntoFaceList = (RelativeLayout) findViewById(R.id.into_faceList);
        mIntoDetect = (RelativeLayout) findViewById(R.id.into_detect);

    }


}
