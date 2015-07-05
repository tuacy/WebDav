package com.vae.wuyunxing.webdav.mobile.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;


import com.vae.wuyunxing.webdav.mobile.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SortPopupWindow extends PopupWindow {

	private static final String KEY_NAME = "name";

	public SortPopupWindow(Context context) {
		super(context);
	}

	public SortPopupWindow(Context context, int width, int height) {
		super(context);
		setWidth(width);
		setHeight(height);
	}

	void apply(final Builder builder) {
		List<Map<String, Integer>> data = new ArrayList<Map<String, Integer>>();
		String[] keys = new String[]{KEY_NAME};
		for (int name : builder.mData) {
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put(KEY_NAME, name);
			data.add(map);
		}

		GridView view = (GridView) View.inflate(builder.mContext, R.layout.drive_browser_tool_popups_content, null);
		view.setNumColumns(1);
		view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (builder.mListener != null) {
					builder.mListener.onItemClick(parent, view, position, id);
				}
				dismiss();
			}
		});
		view.setAdapter(new DriveBrowserToolPopupsAdapter(builder.mContext, data, keys));
		view.setSelector(R.drawable.drive_browser_sort_list_selector);
		view.setPadding(0, 0, 0, 0);
		view.setGravity(Gravity.CENTER);
		
		setContentView(view);
		setOutsideTouchable(builder.mOutsideTouchable);
		setFocusable(true);
		setAnimationStyle(builder.mAnimationStyle);
	}

	public static class Builder {

		Context mContext;
		boolean mOutsideTouchable = true;
		int     mWidth            = ViewGroup.LayoutParams.WRAP_CONTENT;
		int     mHeight           = ViewGroup.LayoutParams.WRAP_CONTENT;
		int     mAnimationStyle   = R.style.SortPopupWindowAnimation;
		int[]                           mData;
		AdapterView.OnItemClickListener mListener;

		public Builder(Context context) {
			mContext = context;
		}

		public Builder setLayoutParam(int width, int height) {
			mWidth = width;
			mHeight = height;
			return this;
		}

		public Builder setOutsideTouchable(boolean outsideTouchable) {
			mOutsideTouchable = outsideTouchable;
			return this;
		}

		public Builder setAnimationStyle(int animationStyle) {
			mAnimationStyle = animationStyle;
			return this;
		}

		public Builder setDisplayData(int[] data) {
			mData = data;
			return this;
		}

		public Builder setOnItemClickListener(AdapterView.OnItemClickListener l) {
			mListener = l;
			return this;
		}

		public SortPopupWindow create() {
			SortPopupWindow window = new SortPopupWindow(mContext, mWidth, mHeight);
			window.apply(this);
			return window;
		}

		public SortPopupWindow show(View anchor) {
			SortPopupWindow window = create();
			window.showAsDropDown(anchor);
			return window;
		}
	}
}
