package com.vae.wuyunxing.webdav.mobile.main.transfer;


import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vae.wuyunxing.webdav.mobile.R;


public class TransferListEditItemAdapter extends BaseAdapter {

	private Context    mContext;
	private TypedArray mCategoryIcons;
	private String[]   mTitles;

	public TransferListEditItemAdapter(Context context, TypedArray categoryIcons, String[] titles) {
		mContext = context;
		mCategoryIcons = categoryIcons;
		mTitles = titles;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		int retLength = 0;
		if (mTitles != null && mCategoryIcons != null && mCategoryIcons.length() == mTitles.length) {
			retLength = mTitles.length;
		}
		return retLength;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_bottom_edit_gridview_item, parent, false);
		}

		ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
		TextView textView = (TextView) convertView.findViewById(R.id.name);
		imageView.setImageDrawable(mCategoryIcons.getDrawable(position));
		textView.setText(mTitles[position]);
		return convertView;
	}
}
