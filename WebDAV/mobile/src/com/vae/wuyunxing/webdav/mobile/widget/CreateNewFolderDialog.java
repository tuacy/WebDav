package com.vae.wuyunxing.webdav.mobile.widget;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;

import com.vae.wuyunxing.commomui.widget.CommonDialog;
import com.vae.wuyunxing.webdav.mobile.R;

public class CreateNewFolderDialog extends CommonDialog {

	public interface OnCreateFolderListener {

		public void onCreate(String folder);

	}

	private OnCreateFolderListener mListener;

	public CreateNewFolderDialog(Context context) {
		this(context, R.style.NoTitleDialog);
	}

	public CreateNewFolderDialog(Context context, int theme) {
		super(context, theme);
		init();
	}

	private void init() {
		final EditText editText = new EditText(getContext());
		editText.setBackgroundResource(R.drawable.dialog_edit_selector);
		editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.new_folder, 0, 0, 0);
		editText.setHint(R.string.new_folder);
		setView(editText);
		setTitleText(R.string.new_folder);
		setTitleTextColor(Color.WHITE);
		
		setCanceledOnTouchOutside(true);

		setNegativeButtonBackground(R.drawable.dialog_button_selector);
		setNegativeButtonTextColor(getContext().getResources().getColor(R.color.black_60alpha));
		setNegativeButton(R.string.cancel, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		setPositiveButtonBackground(R.drawable.dialog_button_selector);
		setPositiveButtonTextColor(getContext().getResources().getColor(R.color.black_60alpha));
		setPositiveButton(R.string.ok, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onCreate(editText.getText().toString());
				}
				dismiss();
			}
		});
	}

	public CreateNewFolderDialog setOnCreateFolderListener(OnCreateFolderListener l) {
		mListener = l;
		return this;
	}
}
