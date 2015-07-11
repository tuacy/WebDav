package com.vae.wuyunxing.webdav.mobile.main;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;


import com.vae.wuyunxing.webdav.mobile.MobileBaseActivity;
import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.message.AccessSubDirEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.ExitToImageOrVideoDirEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.ExitToLocalStorageList;
import com.vae.wuyunxing.webdav.mobile.main.message.FilterFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.LocalFileListBackEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.SelectUploadPathEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.UploadloadFileEvent;


import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class UploadActivity extends MobileBaseActivity {

	@InjectView(R.id.ap_path_id)
	TextView apPathText;

	public static final String CATEGORY_KEY = "category_type";

	public static final String BUCKET_KEY = "bucket_display_name";

	private int mCategoryType = FilterFileEvent.FILTER_TYPE_ALL;
	private FragmentManager                    mFragmentManager;
	private LocalFileListFragment mLocalFileListFragment;
	private LocalImageOrVideoDirSelectFragment mDirSelectFragment;
	private LocalStorageListFragment mLocalStorageList;
	private String mSubDirName = null;
	private String mUploadPath = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		ButterKnife.inject(UploadActivity.this);
        /** get upload remote path */
		mFragmentManager = getFragmentManager();

		if (getIntent().getExtras() != null) {
			mCategoryType = getIntent().getExtras().getInt(MainActivity.KEY_CATEGORY_TYPE, FilterFileEvent.FILTER_TYPE_ALL);
		}
		if (mCategoryType == FilterFileEvent.FILTER_TYPE_PHOTO || mCategoryType == FilterFileEvent.FILTER_TYPE_VIDEO) {
            /** video or image fragment */
			switchFragment(LocalImageOrVideoDirSelectFragment.class, mDirSelectFragment, false, false);
		} else if (mCategoryType == FilterFileEvent.FILTER_TYPE_ALL) {
            /** all file fragment */
			switchFragment(LocalStorageListFragment.class, mLocalStorageList, false, false);
		} else {
            /** other file fragment */
			switchFragment(LocalFileListFragment.class, mLocalFileListFragment, false, false);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ButterKnife.reset(UploadActivity.this);
	}

	@OnClick(R.id.select_back_btn_id)
	void clickOnBack() {
		EventBus.getDefault().post(new LocalFileListBackEvent());
	}

	@OnClick(R.id.right_btn_id)
	void clickOnUploadBtn() {
        if (null == mUploadPath) {
            toasts("please select the upload path");
            return ;
        }
		EventBus.getDefault().post(new UploadloadFileEvent(mUploadPath));
	}

	@OnClick(R.id.left_btn_id)
	void clickOnChoosePathBtn() {
		Intent intent = new Intent(UploadActivity.this, RemoteFilePathSelectActivity.class);
		intent.putExtra(RemoteFilePathSelectActivity.KEY_TYPE, RemoteFilePathSelectActivity.PATH_SELECT_FOR_UPLOAD);
		startActivity(intent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				EventBus.getDefault().post(new LocalFileListBackEvent());
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onEventMainThread(SelectUploadPathEvent event) {
		apPathText.setText(event.displayPath);
		mUploadPath = Uri.parse(event.displayPath).getPath();
	}

	public void onEventMainThread(AccessSubDirEvent event) {
		mSubDirName = event.DirName;
		switchFragment(LocalFileListFragment.class, mLocalFileListFragment, true, true);
	}

	public void onEventMainThread(ExitToImageOrVideoDirEvent event) {
		switchFragment(LocalImageOrVideoDirSelectFragment.class, mDirSelectFragment, false, false);
	}

	public void onEventMainThread(ExitToLocalStorageList event) {
		switchFragment(LocalStorageListFragment.class, mLocalStorageList, false, false);
	}

	private <T extends Fragment> void switchFragment(Class<T> className, T fragment, boolean needRestart, boolean needbucketDisplayName) {
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		hideFragments(transaction);
		Bundle bundle = new Bundle();
		bundle.putInt(CATEGORY_KEY, mCategoryType);
		if (needbucketDisplayName) {
			bundle.putString(BUCKET_KEY, mSubDirName);
		}

		if (fragment == null) {
			try {
				fragment = className.newInstance();
				fragment.setArguments(bundle);
				transaction.add(R.id.activity_drive_browser_file_list, fragment);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			if (needRestart) {
				transaction.remove(fragment);
				try {
					fragment = className.newInstance();
					fragment.setArguments(bundle);
					transaction.add(R.id.activity_drive_browser_file_list, fragment);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				transaction.show(fragment);
			}
		}

		if (fragment instanceof LocalFileListFragment) {
			mLocalFileListFragment = (LocalFileListFragment) fragment;
		} else if (fragment instanceof LocalImageOrVideoDirSelectFragment) {
			mDirSelectFragment = (LocalImageOrVideoDirSelectFragment) fragment;
		} else if (fragment instanceof LocalStorageListFragment) {
			mLocalStorageList = (LocalStorageListFragment) fragment;
		}

		transaction.commit();
	}

	private void hideFragments(FragmentTransaction transaction) {
		if (mLocalFileListFragment != null) {
			transaction.hide(mLocalFileListFragment);
		}
		if (mDirSelectFragment != null) {
			transaction.hide(mDirSelectFragment);
		}
		if (mLocalStorageList != null) {
			transaction.hide((mLocalStorageList));
		}
	}
}
