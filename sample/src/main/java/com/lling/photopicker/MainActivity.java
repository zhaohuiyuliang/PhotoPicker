package com.lling.photopicker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.lling.photopickerr.PhotoPickerActivity;
import com.lling.photopickerr.beans.Photo;
import com.lling.photopickerr.utils.OtherUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import runtimepermissions.PermissionsManager;
import runtimepermissions.PermissionsResultAction;

public class MainActivity extends Activity {
    private static final int PICK_PHOTO = 1;
    protected com.nostra13.universalimageloader.core.ImageLoader imageLoader_head;
    protected DisplayImageOptions options_head; // 设置图片显示相关参数
    private RadioGroup mChoiceMode, mShowCamera;
    private EditText mRequestNum;
    private LinearLayout mRequestNumLayout;
    private GridView mGrideView;
    private GridAdapter mAdapter;
    private int mColumnWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int screenWidth = OtherUtils.getWidthInPx(getApplicationContext());
        mColumnWidth = (screenWidth - OtherUtils.dip2px(getApplicationContext(), 4)) / 3;
        mChoiceMode = (RadioGroup) findViewById(R.id.choice_mode);
        mShowCamera = (RadioGroup) findViewById(R.id.show_camera);
        mRequestNum = (EditText) findViewById(R.id.request_num);
        mRequestNumLayout = (LinearLayout) findViewById(R.id.num_layout);
        mGrideView = (GridView) findViewById(R.id.gridview);
        requestPermissions();
        mChoiceMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.multi) {
                    mRequestNumLayout.setVisibility(View.VISIBLE);
                } else {
                    mRequestNumLayout.setVisibility(View.GONE);
                    mRequestNum.setText("");
                }
            }
        });

        findViewById(R.id.picker_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int selectedMode;
                if (mChoiceMode.getCheckedRadioButtonId() == R.id.multi) {
                    selectedMode = PhotoPickerActivity.MODE_MULTI;
                } else {
                    selectedMode = PhotoPickerActivity.MODE_SINGLE;
                }

                boolean showCamera = false;
                if (mShowCamera.getCheckedRadioButtonId() == R.id.show) {
                    showCamera = true;
                }

                int maxNum = PhotoPickerActivity.DEFAULT_NUM;
                if (!TextUtils.isEmpty(mRequestNum.getText())) {
                    maxNum = Integer.valueOf(mRequestNum.getText().toString());
                }

                Intent intent = new Intent(MainActivity.this, PhotoPickerActivity.class);
                intent.putExtra(PhotoPickerActivity.EXTRA_SHOW_CAMERA, showCamera);
                intent.putExtra(PhotoPickerActivity.EXTRA_SELECT_MODE, selectedMode);
                intent.putExtra(PhotoPickerActivity.EXTRA_MAX_MUN, maxNum);
                startActivityForResult(intent, PICK_PHOTO);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO) {
            if (resultCode == RESULT_OK) {
                List<Photo> result = (ArrayList<Photo>) data.getSerializableExtra(PhotoPickerActivity.KEY_RESULT);
                showResult(result);
            }
        }
    }

    @TargetApi(23)
    private void requestPermissions() {
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this,
                new PermissionsResultAction() {
                    @Override
                    public void onGranted() {

                    }

                    @Override
                    public void onDenied(String permission) {
                    }
                });
    }

    private void showResult(List<Photo> paths) {
        if (mAdapter == null) {
            mAdapter = new GridAdapter(paths);
            mGrideView.setAdapter(mAdapter);
        } else {
            mAdapter.setPathList(paths);
            mAdapter.notifyDataSetChanged();
        }
    }

    private class GridAdapter extends BaseAdapter {
        private List<Photo> pathList;

        public GridAdapter(List<Photo> listUrls) {
            this.pathList = listUrls;
            imageLoader_head = com.nostra13.universalimageloader.core.ImageLoader.getInstance();
            options_head = new DisplayImageOptions.Builder()
                    .showImageOnLoading(com.lling.photopickerr.R.drawable.ic_stub) // 设置图片下载期间显示的图片
                    .showImageForEmptyUri(com.lling.photopickerr.R.drawable.ic_stub) // 设置图片URI为空或是错误的时候显示的图片
                    .showImageOnFail(com.lling.photopickerr.R.drawable.ic_stub) // 设置图片加载或解码过程中发生错误显示的图片
                    .cacheInMemory(true) // 设置下载的图片是否缓存在内存中
                    .cacheOnDisk(true) // 设置下载的图片是否缓存在SD卡中
                    .displayer(new SimpleBitmapDisplayer())
                    .build(); // 构建完成
        }

        @Override
        public int getCount() {
            return pathList == null ? 0 : pathList.size();
        }

        @Override
        public Photo getItem(int position) {
            return  pathList == null ?  null : pathList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setPathList(List<Photo> pathList) {
            this.pathList = pathList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_image, null);
                imageView = (ImageView) convertView.findViewById(R.id.imageView);
                convertView.setTag(imageView);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mColumnWidth, mColumnWidth);
                imageView.setLayoutParams(params);
            } else {
                imageView = (ImageView) convertView.getTag();
            }
            // 新闻图片显示
            ImageAware imageAware = new ImageViewAware(imageView, false);
            File file = new File(getItem(position).getPath());

            imageLoader_head.displayImage(Uri.fromFile(file).toString(), imageAware, options_head);
            return convertView;
        }
    }

}
