package com.gst.gstfacedemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gst.gstfacedemo.R;
import com.gst.gstfacedemo.activity.IdentifyActivity;
import com.gst.gstfacedemo.activity.SelectPicture;
import com.gst.gstfacedemo.model.ImageBean;
import com.gst.gstfacedemo.util.GlideCacheUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhenghangxia on 17-4-17.
 *
 *  GridView适配器
 */

public class GridViewAdapter extends ArrayAdapter<ImageBean> {

    private Context mContext;
    private List<ImageBean> mList;
    public List<String> mImageList = new ArrayList<>();

    public GridViewAdapter(Context context, List<ImageBean> mList) {
        super(context, 0, mList);
        this.mContext = context;
        this.mList = mList;
    }

    /**
     * 获取单个Item的位置
     */
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public ImageBean getItem(int position) {
        return mList.get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ImageBean imageBean = getItem(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.layout_gridview_item,parent,false);
            viewHolder = new ViewHolder();

            viewHolder.view_main = (RelativeLayout) convertView.findViewById(R.id.view_main);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (imageBean != null) {

            final ViewHolder finalViewHolder = viewHolder;
            // 加载图片显示
            Glide.with(mContext).load(imageBean.getImagePath()).into(viewHolder.imageView);
            GlideCacheUtil.getInstance().clearImageAllCache(mContext);

            // 图片选择
            viewHolder.view_main.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!imageBean.isChecked()) {
                        imageBean.setChecked(true);
                        finalViewHolder.checkBox.setChecked(true);
                        Log.e("----->","当前选中： "+imageBean.getImagePath());

                        mImageList.add(imageBean.getImagePath());

                        for (int i=0;i<mImageList.size();i++) {
                            Log.e("List数据-------->",mImageList.get(i));
                        }

                    } else {
                        imageBean.setChecked(false);
                        finalViewHolder.checkBox.setChecked(false);

                        for (int i=0;i<mImageList.size();i++) {
                            if (mImageList.get(i).equals(imageBean.getImagePath())) {
                                mImageList.remove(i);
                                Log.e("----->","当前取消位置： "+i);
                            }
                        }
                    }
                }
            });

            // 确认
            final SelectPicture activity = (SelectPicture) mContext;
            activity.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mImageList == null) {
                        Toast.makeText(activity, "请选择", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(activity, IdentifyActivity.class);
                        intent.putStringArrayListExtra("mImageList", (ArrayList<String>) mImageList);
                        intent.putExtra("mPersonGroupId",activity.mPersonGroupId);
                        activity.startActivity(intent);
                        activity.finish();
                    }
                }
            });

            // 貌似没用
            if (imageBean.isChecked()) {
                viewHolder.checkBox.setChecked(true);

            } else {
                viewHolder.checkBox.setChecked(false);
            }
            Log.e("---->",mImageList.toString());
        }

        return convertView;
    }

    private class ViewHolder {

        ImageView imageView;
        CheckBox checkBox;
        RelativeLayout view_main;

    }
}
