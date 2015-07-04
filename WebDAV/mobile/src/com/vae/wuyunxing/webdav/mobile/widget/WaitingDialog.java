package com.vae.wuyunxing.webdav.mobile.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.vae.wuyunxing.webdav.mobile.R;

public class WaitingDialog extends Dialog {

	private TextView mTextView;

	public WaitingDialog(Context context) {
		this(context, "");
	}
	
	public WaitingDialog(Context context, int infoId) {
		this(context, context.getResources().getString(infoId));
	}
	
	public WaitingDialog(Context context, String infoStr) {
		super(context, R.style.NoTitleDialog);
		
		initView();
		setTextInfo(infoStr);
	}

	private void initView() {
		View parentView = View.inflate(getContext(), R.layout.dialog_waitting, null);
		setContentView(parentView);
		
		this.setCanceledOnTouchOutside(false);
		
		ImageView image = (ImageView) parentView.findViewById(R.id.wait_anim_image);
		mTextView = (TextView) parentView.findViewById(R.id.wait_notice);

		Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.waiting_dialog);
		LinearInterpolator interpolator = new LinearInterpolator();
		anim.setInterpolator(interpolator);
		image.startAnimation(anim);
	}
	
	private void setTextInfo(String string) {
		if (mTextView != null) {
			mTextView.setText(string);
		}
	}
	
	@SuppressWarnings("NullableProblems")
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return KeyEvent.ACTION_DOWN == event.getAction() && keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
	}
}
