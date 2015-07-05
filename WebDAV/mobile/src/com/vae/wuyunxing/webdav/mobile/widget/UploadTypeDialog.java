package com.vae.wuyunxing.webdav.mobile.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;


import com.vae.wuyunxing.webdav.mobile.R;

import java.util.List;
import java.util.Map;

public class UploadTypeDialog extends Dialog {

	public UploadTypeDialog(Context context) {
		this(context, R.style.Dialog);
	}

	public UploadTypeDialog(Context context, int theme) {
		super(context, theme);
	}

	void apply(final Builder builder) {
		GridView view = (GridView) View.inflate(getContext(), R.layout.drive_browser_tool_popups_content, null);
		view.setNumColumns(builder.mColumn);
		view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (builder.mListener != null) {
					builder.mListener.onItemClick(parent, view, position, id);
				}
				dismiss();
			}
		});
		view.setAdapter(new DriveBrowserToolPopupsAdapter(getContext(), builder.mData, builder.mKeys));
		setContentView(view);

		setTitle(builder.mTitle);
		setCanceledOnTouchOutside(builder.mCanceledOnTouchOutside);
		getWindow().setBackgroundDrawableResource(R.drawable.drive_browser_popups_background);
	}

	public static class Builder {

		Context mContext;
		boolean mCanceledOnTouchOutside = true;
		int     mTheme                  = R.style.Dialog;
		int     mTitle                  = R.string.choose_upload_file_type;
		int     mColumn                 = 3;
		List<? extends Map<String, ?>>  mData;
		String[]                        mKeys;
		AdapterView.OnItemClickListener mListener;

		public Builder(Context context) {
			mContext = context;
		}

		public Builder(Context context, int theme) {
			mContext = context;
			mTheme = theme;
		}

		public Builder setTitle(int titleId) {
			mTitle = titleId;
			return this;
		}

		public Builder setCanceledOnTouchOutside(boolean cancel) {
			mCanceledOnTouchOutside = cancel;
			return this;
		}

		public Builder setColumn(int column) {
			mColumn = column;
			return this;
		}

		public Builder setDisplayData(List<? extends Map<String, ?>> data, String[] keys) {
			mData = data;
			mKeys = keys;
			return this;
		}

		public Builder setOnItemClickListener(AdapterView.OnItemClickListener l) {
			mListener = l;
			return this;
		}

		public UploadTypeDialog build() {
			UploadTypeDialog dialog = new UploadTypeDialog(mContext, mTheme);
			dialog.apply(this);
			return dialog;
		}

		public UploadTypeDialog show() {
			UploadTypeDialog dialog = build();
			dialog.show();
			return dialog;
		}
	}
}
