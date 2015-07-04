package com.vae.wuyunxing.commomui.widget;


import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vae.wuyunxing.commomui.R;


public class CommonDialog extends Dialog {

	private TextView       mTitle        = null;
	private Button         mPositiveBtn  = null;
	private Button         mNegativeBtn  = null;
	private RelativeLayout mDialogPanel  = null;
	private LinearLayout   mContentPanel = null;
	private LinearLayout   mButtonPanel  = null;
	private LinearLayout   mTitlePanel   = null;

	public CommonDialog(Context context) {
		this(context, 0);
	}
	
	public CommonDialog(Context context, int theme) {
		super(context, theme);
		initData();
	}

	private void initData() {
		setContentView(R.layout.dialog_common);
		mTitle = (TextView) getWindow().findViewById(R.id.dialog_title);
		mPositiveBtn = (Button) getWindow().findViewById(R.id.dialog_positive_button);
		mNegativeBtn = (Button) getWindow().findViewById(R.id.dialog_negative_button);
		mDialogPanel = (RelativeLayout) getWindow().findViewById(R.id.dialog_layout);
		mContentPanel = (LinearLayout) getWindow().findViewById(R.id.dialog_content_panel);
		mButtonPanel = (LinearLayout) getWindow().findViewById(R.id.dialog_button_panel);
		mTitlePanel = (LinearLayout) getWindow().findViewById(R.id.dialog_title_panel);
	}

	public CommonDialog setDialogBackground(int resid) {
		mDialogPanel.setBackgroundResource(resid);
		return this;
	}

	public CommonDialog setDialogBackground(Drawable drawable) {
		mDialogPanel.setBackgroundDrawable(drawable);
		return this;
	}

	public CommonDialog setView(View view) {
		mContentPanel.addView(view);
		return this;
	}

	public CommonDialog setView(View view, LayoutParams params) {
		mContentPanel.addView(view, params);
		return this;
	}

	public CommonDialog setTitleText(int resid) {
		mTitle.setText(resid);
		return this;
	}
	
	public CommonDialog setTitleText(CharSequence title) {
		mTitle.setText(title);
		return this;
	}

	public CommonDialog setTitleView(View view) {
		mTitle.setVisibility(View.GONE);
		mTitlePanel.addView(view);
		return this;
	}

	public CommonDialog setTitleTextColor(int color) {
		mTitle.setTextColor(color);
		return this;
	}

	public CommonDialog setTitleBackground(int resid) {
		mTitlePanel.setBackgroundResource(resid);
		return this;
	}

	public CommonDialog setTitleBackground(Drawable drawable) {
		mTitlePanel.setBackgroundDrawable(drawable);
		return this;
	}

	public CommonDialog setContentBackground(Drawable drawable) {
		mContentPanel.setBackgroundDrawable(drawable);
		return this;
	}

	public CommonDialog setButtonsVisibility(int visibility) {
		mButtonPanel.setVisibility(visibility);
		return this;
	}
	
	public CommonDialog setButtonView(View view) {
		mPositiveBtn.setVisibility(View.GONE);
		mNegativeBtn.setVisibility(View.GONE);
		mButtonPanel.addView(view);
		return this;
	}

	public CommonDialog setButtonBackground(Drawable drawable) {
		mButtonPanel.setBackgroundDrawable(drawable);
		return this;
	}

	public CommonDialog setPositiveButton(int textId, View.OnClickListener listener) {
		mPositiveBtn.setText(textId);
		mPositiveBtn.setOnClickListener(listener);
		mPositiveBtn.setVisibility(View.VISIBLE);
		return this;
	}

	public CommonDialog setNegativeButton(int textId, View.OnClickListener listener) {
		mNegativeBtn.setText(textId);
		mNegativeBtn.setOnClickListener(listener);
		mNegativeBtn.setVisibility(View.VISIBLE);
		return this;
	}

	public CommonDialog setPositiveButtonEnabled(boolean enabled) {
		mPositiveBtn.setEnabled(enabled);
		return this;
	}

	public CommonDialog setNegativeButtonEnabled(boolean enabled) {
		mNegativeBtn.setEnabled(enabled);
		return this;
	}

	public CommonDialog setPositiveButtonBackground(int resid) {
		mPositiveBtn.setBackgroundResource(resid);
		return this;
	}

	public CommonDialog setPositiveButtonBackground(Drawable drawable) {
		mPositiveBtn.setBackgroundDrawable(drawable);
		return this;
	}

	public CommonDialog setNegativeButtonBackground(int resid) {
		mNegativeBtn.setBackgroundResource(resid);
		return this;
	}

	public CommonDialog setNegativeButtonBackground(Drawable drawable) {
		mNegativeBtn.setBackgroundDrawable(drawable);
		return this;
	}

	public CommonDialog setPositiveButtonTextColor(int color) {
		mPositiveBtn.setTextColor(color);
		return this;
	}

	public CommonDialog setNegativeButtonTextColor(int color) {
		mNegativeBtn.setTextColor(color);
		return this;
	}
}

