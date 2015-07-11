package com.vae.wuyunxing.webdav.mobile.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;


import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.vae.wuyunxing.webdav.library.FileInfo;
import com.vae.wuyunxing.webdav.library.imp.local.LocalFileInfo;
import com.vae.wuyunxing.webdav.mobile.R;

import java.util.List;
import java.util.Set;

public class UploadGridAdapter extends BaseAdapter {

	private Context        mContext;
	private List<FileInfo> mUploadFilesInfoList;
	private Set<Integer>   mSelections;
	private DisplayImageOptions mOptions;

	public UploadGridAdapter(Context context) {
		mContext = context;
		mOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
													.cacheOnDisk(true)
													.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
													.bitmapConfig(Bitmap.Config.RGB_565)
													.showImageOnLoading(mContext.getResources().getDrawable(R.color.black_10alpha))
													.build();
	}

	public void setFileList(List<FileInfo> list) {
		mUploadFilesInfoList = list;
		notifyDataSetChanged();
	}

	public void setSelections(Set<Integer> selections) {
		mSelections = selections;
		notifyDataSetChanged();
	}

	public void clearSelections() {
		mSelections.clear();
		notifyDataSetChanged();
	}

	public int getCount() {
		// TODO Auto-generated method stub
		if (mUploadFilesInfoList == null) {
			return 0;
		}
		return mUploadFilesInfoList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mUploadFilesInfoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		FileInfo file = mUploadFilesInfoList.get(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_upload_photo_item, parent, false);
			holder = new ViewHolder();
			holder.mIconImage = (ImageView) convertView.findViewById(R.id.image_id);
			holder.mSelectedImage = (ImageView) convertView.findViewById(R.id.select_id);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (file instanceof LocalFileInfo) {
			String imageUrl = ImageDownloader.Scheme.FILE.wrap(file.getPath());
			ImageLoader.getInstance().displayImage(imageUrl, holder.mIconImage, mOptions);
		}

		if (mSelections.contains(file.hashCode())) {
			holder.mSelectedImage.setVisibility(View.VISIBLE);
		} else {
			holder.mSelectedImage.setVisibility(View.GONE);
		}

		return convertView;
	}

	class ViewHolder {

		public ImageView mIconImage;
		public ImageView mSelectedImage;
	}
}
