package com.vae.wuyunxing.webdav.mobile;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/7/3. MobileApplication
 */
public class MobileApplication extends Application implements Application.ActivityLifecycleCallbacks {



	private int mActivityCount = 0;
	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		if (mActivityCount == 0) {
			/** first run the app */
			initializeApplication();
		}
		mActivityCount++;
	}

	@Override
	public void onActivityStarted(Activity activity) {

	}

	@Override
	public void onActivityResumed(Activity activity) {

	}

	@Override
	public void onActivityPaused(Activity activity) {

	}

	@Override
	public void onActivityStopped(Activity activity) {

	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

	}

	@Override
	public void onActivityDestroyed(Activity activity) {
		mActivityCount--;
		if (mActivityCount == 0) {
			/** exit the app */
			deinitializeApplication();
		}
	}

	protected void initializeApplication() {

		EventBus.getDefault().postSticky(new APPStartupEvent());
	}

	protected void deinitializeApplication() {
		EventBus.getDefault().postSticky(new APPTerminateEvent());
	}
}
