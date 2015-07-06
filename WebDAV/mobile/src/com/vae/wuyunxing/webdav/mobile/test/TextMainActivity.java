package com.vae.wuyunxing.webdav.mobile.test;

import android.os.Bundle;

import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.MobileBaseActivity;

import butterknife.ButterKnife;

public class TextMainActivity extends MobileBaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		ButterKnife.inject(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ButterKnife.reset(this);
	}
}
