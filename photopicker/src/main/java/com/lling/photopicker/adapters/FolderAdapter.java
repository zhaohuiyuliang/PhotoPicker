package com.lling.photopicker.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lling.photopicker.R;
import com.lling.photopicker.beans.PhotoFolder;
import com.lling.photopicker.utils.OtherUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.io.File;
import java.util.List;

/**
 * @Class: FolderAdapter
 * @Description: 图片目录适配器
 * @author: lling(www.liuling123.com)
 * @Date: 2015/11/6
 */
public class FolderAdapter extends BaseAdapter {

    List<PhotoFolder> mDatas;
    Context mContext;
    protected DisplayImageOptions options_head; // 设置图片显示相关参数

    int mWidth;
    protected com.nostra13.universalimageloader.core.ImageLoader imageLoader_head;


    public FolderAdapter(Context context, List<PhotoFolder> mDatas) {
        this.mDatas = mDatas;
        this.mContext = context;
        mWidth = OtherUtils.dip2px(context, 90);
        imageLoader_head = com.nostra13.universalimageloader.core.ImageLoader.getInstance();
        options_head = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub) // 设置图片下载期间显示的图片
                .showImageForEmptyUri(R.drawable.ic_stub) // 设置图片URI为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.ic_stub) // 设置图片加载或解码过程中发生错误显示的图片
                .cacheInMemory(true) // 设置下载的图片是否缓存在内存中
                .cacheOnDisk(true) // 设置下载的图片是否缓存在SD卡中
                .displayer(new SimpleBitmapDisplayer())
                .build(); // 构建完成
    }

    @Override
    public int getCount() {
        if (mDatas == null) {
            return 0;
        }
        return mDatas.size();
    }

    @Override
    public PhotoFolder getItem(int position) {
        if (mDatas == null || mDatas.size() == 0) {
            return null;
        }
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.item_folder_layout, null);
            holder.photoIV = (ImageView) convertView.findViewById(R.id.imageview_folder_img);
            holder.folderNameTV = (TextView) convertView.findViewById(R.id.textview_folder_name);
            holder.photoNumTV = (TextView) convertView.findViewById(R.id.textview_photo_num);
            holder.selectIV = (ImageView) convertView.findViewById(R.id.imageview_folder_select);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        PhotoFolder folder = getItem(position);
        if (folder == null) {
            return convertView;
        }
        if (folder.getPhotoList() == null || folder.getPhotoList().size() == 0) {
            return convertView;
        }
        holder.selectIV.setVisibility(View.GONE);
        holder.photoIV.setImageResource(R.drawable.ic_photo_loading);
        if (folder.isSelected()) {
            holder.selectIV.setVisibility(View.VISIBLE);
        }
        holder.folderNameTV.setText(folder.getName());

            holder.photoNumTV.setText(folder.getPhotoList().size() + "张");
        // 新闻图片显示
        ImageAware imageAware = new ImageViewAware(holder.photoIV, false);
        File file = new File(folder.getPhotoList().get(0).getPath());

        imageLoader_head.displayImage(Uri.fromFile(file).toString(), imageAware, options_head);
        return convertView;
    }

    private class ViewHolder {
        private ImageView photoIV;
        private TextView folderNameTV;
        private TextView photoNumTV;
        private ImageView selectIV;
    }

}
