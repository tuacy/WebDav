package com.vae.wuyunxing.webdav.mobile;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.Toast;

import com.vae.wuyunxing.webdav.mobile.widget.WaitingDialog;
import com.vae.wuyunxing.commomui.swipebacklayout.SwipeBackActivity;
import com.vae.wuyunxing.webdav.library.log.MKLog;

import de.greenrobot.event.EventBus;

public class MobileBaseActivity extends SwipeBackActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/** EventBus */
		EventBus.getDefault().register(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dismissWaitingDialog();
		EventBus.getDefault().unregister(this);
	}

	@Override
	public void onBackPressed() {
		scrollToFinishActivity();
	}

	protected void replaceFragment(int layoutId, Class<? extends Fragment> clz) {
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		try {
			Fragment f;
			if ((f = fm.findFragmentByTag(clz.getName())) == null) {
				f = clz.newInstance();
			}
			ft.replace(layoutId, f, clz.getName()).show(f).commit();
		} catch (Exception e) {
			MKLog.e(MobileBaseActivity.class, e, "Cannot get new instance of %s . Throw: %s. Message: %s", clz.getName(), e,
					e.getMessage());
		}
	}

	protected void replaceFragment(int layoutId, Class<? extends Fragment> clz, Bundle args) {
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		try {
			Fragment f;
			if ((f = fm.findFragmentByTag(clz.getName())) == null) {
				f = clz.newInstance();
			}
			f.setArguments(args);
			ft.replace(layoutId, f, clz.getName()).show(f).commit();
		} catch (Exception e) {
			MKLog.e(MobileBaseActivity.class, e, "Cannot get new instance of %s . Throw: %s. Message: %s", clz.getName(), e,
					e.getMessage());
		}
	}

	protected void addFragment(int layoutId, Class<? extends Fragment> clz) {
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		try {
			Fragment f = clz.newInstance();
			ft.add(layoutId, f, clz.getName()).show(f).commit();
		} catch (Exception e) {
			MKLog.e(MobileBaseActivity.class, e, "Cannot get new instance of %s . Throw: %s. Message: %s", clz.getName(), e, e.getMessage());
		}
	}

	protected void addFragment(int layoutId, Class<? extends Fragment> clz, int enterAnim, int exitAnim) {
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		try {
			Fragment f = clz.newInstance();
			ft.add(layoutId, f, clz.getName()).setCustomAnimations(enterAnim, exitAnim).show(f).commit();
		} catch (Exception e) {
			MKLog.e(MobileBaseActivity.class, e, "Cannot get new instance of %s . Throw: %s. Message: %s", clz.getName(), e,
					e.getMessage());
		}
	}

	protected void removeFragment(int layoutId, Class<? extends Fragment> clz) {
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment f = fm.findFragmentByTag(clz.getName());
		if (f != null) {
			ft.remove(f).commit()
            ;
		}
	}


	/** waiting dialog */
	private WaitingDialog mWaitingDialog;

	private boolean isWaitingDialogShowing() {
		return mWaitingDialog != null && mWaitingDialog.isShowing();
	}

	public void showWaitingDialog() {
		if (!isWaitingDialogShowing()) {
			mWaitingDialog = new WaitingDialog(this);
			mWaitingDialog.show();
		}
	}

	public void showWaitingDialog(int strId) {
		if (!isWaitingDialogShowing()) {
			mWaitingDialog = new WaitingDialog(this, strId);
			mWaitingDialog.show();
		}
	}

	public void showWaitingDialog(String info) {
		if (!isWaitingDialogShowing()) {
			mWaitingDialog = new WaitingDialog(this, info);
			mWaitingDialog.show();
		}
	}

	public void dismissWaitingDialog() {
		if (isWaitingDialogShowing()) {
			mWaitingDialog.dismiss();
		}
	}

	/** toast */
	public void toasts(int strId) {
		Toast.makeText(MobileBaseActivity.this, strId, Toast.LENGTH_SHORT).show();
	}

	public void toasts(int strId, int duration) {
		Toast.makeText(MobileBaseActivity.this, strId, duration).show();
	}

	public void toasts(String str) {
		Toast.makeText(MobileBaseActivity.this, str, Toast.LENGTH_SHORT).show();
	}

	public void toasts(String str, int duration) {
		Toast.makeText(MobileBaseActivity.this, str, duration).show();
	}

	public void onEventMainThread(MobileNoUseEvent event) {

	}
}
