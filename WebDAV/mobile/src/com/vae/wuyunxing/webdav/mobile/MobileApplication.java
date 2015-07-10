package com.vae.wuyunxing.webdav.mobile;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.vae.wuyunxing.webdav.library.config.LibraryConfig;
import com.vae.wuyunxing.webdav.library.log.CustomLogger;
import com.vae.wuyunxing.webdav.library.log.MKLog;
import com.vae.wuyunxing.webdav.mobile.config.MobileConfig;
import com.vae.wuyunxing.webdav.mobile.main.transmission.TransferService;

import de.greenrobot.event.EventBus;

import greendao.DaoMaster;
import greendao.DaoSession;

public class MobileApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private static MobileApplication sInstance;
	/** greenDao database DaoSession */
	private DaoSession mDaoSession;

    private TransferService mTransferManager;

	@Override
	public void onCreate() {
		super.onCreate();
        sInstance = this;

		/** setup greenDao database */
		setupDatabase();

		LibraryConfig.initialize(MobileApplication.this);
		MobileConfig.initialize(MobileApplication.this);

		if (MobileConfig.getInstance().getBoolean(MobileConfig.DEBUG, false)) {
			MKLog.setCustomLogger(new MobileLogger());
		}

        /** start transfer service */
        startTransferService();
	}

	private void setupDatabase() {
		DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "androidwebdav-mobile.db", null);
		SQLiteDatabase db = helper.getWritableDatabase();
		DaoMaster daoMaster = new DaoMaster(db);
		mDaoSession = daoMaster.newSession();
	}

    public static MobileApplication getInstance() {
        return sInstance;
    }

	/***
	 * Get DaoSession
	 * @return
	 */
	public DaoSession getDaoSession() {
		return mDaoSession;
	}

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


	public static final class MobileLogger implements CustomLogger {

		@Override
		public boolean isDebugEnabled() {
			return true;
		}

		@Override
		public void d(Class<?> clazz, String format, Object... args) {
			Log.d(clazz.getSimpleName(), String.format(format, args));
		}

		@Override
		public void e(Class<?> clazz, String format, Object... args) {
			Log.e(clazz.getSimpleName(), String.format(format, args));
		}

		@Override
		public void e(Class<?> clazz, Throwable t, String format, Object... args) {
			Log.e(clazz.getSimpleName(), String.format(format, args), t);
		}
	}

    /*** transfer service ***/
    private final ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof TransferService.TransmitterBinder) {
                mTransferManager = ((TransferService.TransmitterBinder) service).getTransferService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTransferManager = null;
        }
    };

    private void startTransferService() {
        TransferService.startup(getApplicationContext());
        TransferService.bind(getApplicationContext(), mServiceConn);
    }

    public TransferService getTransferManager() {
        return mTransferManager;
    }
}
