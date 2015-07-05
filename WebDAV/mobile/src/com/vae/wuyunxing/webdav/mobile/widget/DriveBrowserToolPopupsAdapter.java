package com.vae.wuyunxing.webdav.mobile.widget;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.vae.wuyunxing.webdav.mobile.R;

import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

class DriveBrowserToolPopupsAdapter extends BaseAdapter {

	private static final int BRIGHTNESS_NORMAL  = 0;
	private static final int BRIGHTNESS_PRESSED = -50;
	
	private Context                        mContext;
	private List<? extends Map<String, ?>> mData;
	private String[]                       mKeys;

	public DriveBrowserToolPopupsAdapter(Context context, List<? extends Map<String, ?>> data, String[] keys) {
		mContext = context;
		mData = data;
		mKeys = keys;
	}

	@Override
	public int getCount() {
		if (mData == null) {
			return 0;
		}
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		if (mData == null) {
			return null;
		}
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView != null) {
			holder = (ViewHolder) convertView.getTag();
		} else {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.drive_browser_tool_popups_item, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}

		Map<String, ?> map = (Map<String, ?>) getItem(position);
		if (map != null && mKeys.length > 0) {
			holder.mName.setText((Integer) map.get(mKeys[0]));
			if (mKeys.length > 1) {
				holder.mIcon.setImageResource((Integer) map.get(mKeys[1]));
			} else {
				holder.mIcon.setVisibility(View.GONE);
				holder.mName.setPadding(0,
										mContext.getResources().getDimensionPixelOffset(R.dimen.padding_mini_1),
										0,
										mContext.getResources().getDimensionPixelOffset(R.dimen.padding_mini_1));
			}
			convertView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					ViewHolder holder = (ViewHolder) v.getTag();
					switch (event.getAction()) {
						case MotionEvent.ACTION_UP:
						case MotionEvent.ACTION_CANCEL:
							changeLight(holder.mIcon, BRIGHTNESS_NORMAL);
							break;
						case MotionEvent.ACTION_DOWN:
							changeLight(holder.mIcon, BRIGHTNESS_PRESSED);
							break;
					}
					return false;
				}
			});
		}

		return convertView;
	}

	private void changeLight(ImageView imageView, int brightness) {
		ColorMatrix colorMatrix = new ColorMatrix();
		colorMatrix.set(new float[]{1,
									0,
									0,
									0,
									brightness,
									0,
									1,
									0,
									0,
									brightness,
									0,
									0,
									1,
									0,
									brightness,
									0,
									0,
									0,
									1,
									0});
		imageView.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
	}

	static final class ViewHolder {

		@InjectView(R.id.drive_browser_tool_popups_icon)
		ImageView mIcon;

		@InjectView(R.id.drive_browser_tool_popups_name)
		TextView mName;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}
}
