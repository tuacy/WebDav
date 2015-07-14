package com.vae.wuyunxing.webdav.mobile.main;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.vae.wuyunxing.webdav.library.FileBrowserFactory;
import com.vae.wuyunxing.webdav.library.FileCategory;
import com.vae.wuyunxing.webdav.library.FileExplorer;
import com.vae.wuyunxing.webdav.library.FileInfo;
import com.vae.wuyunxing.webdav.library.exception.DirectoryAlreadyExistsException;
import com.vae.wuyunxing.webdav.library.filter.FileFilter;
import com.vae.wuyunxing.webdav.library.imp.jackrabbit.JackrabbitPath;
import com.vae.wuyunxing.webdav.library.sort.FileSorter;
import com.vae.wuyunxing.webdav.library.util.FileUtil;
import com.vae.wuyunxing.webdav.library.util.PathUtil;
import com.vae.wuyunxing.webdav.mobile.MobileBaseActivity;
import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.message.DirChangedEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.FilterFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.SelectRelativePathEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.SelectUploadPathEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.StartMoveRemoteFileEvent;
import com.vae.wuyunxing.webdav.mobile.widget.CreateNewFolderDialog;

import de.greenrobot.event.EventBus;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;

public class RemoteFilePathSelectActivity extends MobileBaseActivity {

	@InjectView(R.id.drive_browser_ptr_frame_list)
	PtrFrameLayout mPtrFrameList;

	@InjectView(R.id.drive_browser_file_list)
	ListView mListView;

	@InjectView(R.id.drive_browser_empty_hint)
	FrameLayout mEmptyHint;

	@InjectView(R.id.drive_browser_list_hint)
	TextView mListHint;

	@InjectView(R.id.select_path_id)
	TextView sambaPath;

	@InjectView(R.id.select_title_id)
	TextView mTitle;

	private Context mContext;


	public static final String KEY_TYPE                 = "key_type";
	public static final int    PATH_SELECT_FOR_MOVE     = 0;
	public static final int    PATH_SELECT_FOR_UPLOAD   = 1;
	public static final int    PATH_SELECT_FOR_RELATIVE = 2;

	/* Explorer */
	private FileExplorer   mFileExplorer;
	private List<FileInfo> mCurDirFileList;
	private List<FileInfo> mDispFileList;

	/* States */
	private int        mFilterType     = FilterFileEvent.FILTER_TYPE_ALL;
	private FileSorter mSorter         = FileSorter.FILE_NAME_ASCENDING;
	private boolean    mIsRefreshing   = false;
	private int mSelectFor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drive_browser_path_select);
		ButterKnife.inject(RemoteFilePathSelectActivity.this);

		mContext = this;

		mSelectFor = getIntent().getIntExtra(KEY_TYPE, PATH_SELECT_FOR_MOVE);
		initPtrFrameLayout(mPtrFrameList);
		initView();

		getSambaPasswordAndFileList();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ButterKnife.reset(RemoteFilePathSelectActivity.this);
	}

	private void initPtrFrameLayout(PtrFrameLayout ptrFrame) {
		ptrFrame.setPtrHandler(new PtrDefaultHandler() {
			@Override
			public void onRefreshBegin(PtrFrameLayout ptrFrameLayout) {
				refreshBegin();
				updateCurrentFileList();
			}
		});
	}

	private void initView() {
		if (mSelectFor == RemoteFilePathSelectActivity.PATH_SELECT_FOR_UPLOAD) {
			mTitle.setText(R.string.str_select_ap_save_path);
		} else if (mSelectFor == RemoteFilePathSelectActivity.PATH_SELECT_FOR_RELATIVE) {
			mTitle.setText(R.string.str_choose_remote_sync_file);
		}
		mListView.setAdapter(new FileListAdapter(LayoutInflater.from(RemoteFilePathSelectActivity.this)));
	}

	private JackrabbitPath getJackrabbitPath() {
		String domain = getResources().getString(R.string.webdav_domain);
		String sambaUser = getResources().getString(R.string.webdav_root);
		String password = getResources().getString(R.string.webdav_password);
		String path = PathUtil.appendPath(true, "/");
		return new JackrabbitPath(domain, path, sambaUser, password);
	}


	private void getSambaPasswordAndFileList() {
		Task.call(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				if (!isRefreshing()) {
					showWaitingDialog();
				}
				mCurDirFileList = null;
				mDispFileList = null;
				return null;
			}
		}, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<Void, Void>() {
			@Override
			public Void then(Task<Void> task) throws Exception {
				if (mFileExplorer == null) {
					JackrabbitPath path = getJackrabbitPath();
					mFileExplorer = FileBrowserFactory.createJackrabbitFileExplorer(path, mContext);
				}

				return null;
			}
		}, Task.BACKGROUND_EXECUTOR).continueWith(new Continuation<Void, Void>() {
			@Override
			public Void then(Task<Void> task) throws Exception {
				if (task.isCompleted()) {
					getAndDisplayFileList(".");
				} else {
					updateFileListView(mDispFileList);
				}
				return null;
			}
		}, Task.UI_THREAD_EXECUTOR);
	}

	private void getAndDisplayFileList(final String path) {
		Task.call(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				if (!isRefreshing()) {
					showWaitingDialog();
				}
				mCurDirFileList = null;
				mDispFileList = null;
				return null;
			}
		}, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<Void, List<FileInfo>>() {
			@Override
			public List<FileInfo> then(Task<Void> task) throws Exception {
				mFileExplorer.cd(path);
				mCurDirFileList = mFileExplorer.ls("-l");
				return sortAndFilterFiles(mCurDirFileList);
			}
		}, Task.BACKGROUND_EXECUTOR).continueWith(new Continuation<List<FileInfo>, Void>() {
			@Override
			public Void then(Task<List<FileInfo>> task) throws Exception {
				if (task.isCompleted()) {
					try {
						mDispFileList = task.getResult();
						EventBus.getDefault().post(new DirChangedEvent(mFileExplorer.isRoot(), mFileExplorer.pwd(true)));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				updateFileListView(mDispFileList);
				return null;
			}
		}, Task.UI_THREAD_EXECUTOR);
	}

	private void updateCurrentFileList() {
		getSambaPasswordAndFileList();
	}

	private void updateFileListView(List<FileInfo> fileList) {
		dismissWaitingDialog();
		sambaPath.setText(mFileExplorer.pwd(false));
		if (isRefreshing()) {
			refreshComplete();
		}
		if (fileList == null || fileList.isEmpty()) {
			showEmptyHint();
		} else {
			clearEmptyHint();
			((FileListAdapter) mListView.getAdapter()).setFileList(fileList);
		}
	}

	private List<FileInfo> filterFiles(List<FileInfo> all, int filterType) {
		FileFilter filter;
		switch (filterType) {
			case FilterFileEvent.FILTER_TYPE_DOC:
				filter = FileCategory.DOCUMENT.getFilter();
				break;
			case FilterFileEvent.FILTER_TYPE_MUSIC:
				filter = FileCategory.AUDIO.getFilter();
				break;
			case FilterFileEvent.FILTER_TYPE_VIDEO:
				filter = FileCategory.VIDEO.getFilter();
				break;
			case FilterFileEvent.FILTER_TYPE_PHOTO:
				filter = FileCategory.IMAGE.getFilter();
				break;
			case FilterFileEvent.FILTER_TYPE_BT:
				filter = FileCategory.BIT_TORRENT.getFilter();
				break;
			case FilterFileEvent.FILTER_TYPE_APP:
				filter = FileCategory.APPLICATION.getFilter();
				break;
			case FilterFileEvent.FILTER_TYPE_ALL:
			default:
				filter = FileCategory.OTHERS.getFilter();
				break;
		}
		return FileUtil.filter(all, filter);
	}

	private List<FileInfo> sortFiles(final List<FileInfo> list, final FileSorter sorter) {
		final Comparator<FileInfo> comparator = sorter.getSorter();
		Collections.sort(list, comparator);
		return list;
	}

	private List<FileInfo> sortAndFilterFiles(final List<FileInfo> all) {
		return sortFiles(filterFiles(all, mFilterType), mSorter);
	}

	private void showEmptyHint() {
		mPtrFrameList.setVisibility(View.GONE);
		mEmptyHint.setVisibility(View.VISIBLE);
		mListHint.setText(R.string.empty_folder_hint);
	}

	private void clearEmptyHint() {
		mPtrFrameList.setVisibility(View.VISIBLE);
		mEmptyHint.setVisibility(View.GONE);
	}

	private void refreshBegin() {
		setRefreshing(true);
		mPtrFrameList.setEnabled(false);
	}

	private void refreshComplete() {
		setRefreshing(false);
		mPtrFrameList.setEnabled(true);
		mPtrFrameList.refreshComplete();
	}

	private boolean isRefreshing() {
		return mIsRefreshing;
	}

	private void setRefreshing(boolean refreshing) {
		mIsRefreshing = refreshing;
	}

	@OnItemClick(R.id.drive_browser_file_list)
	void fileClick(int position) {

		FileInfo file = (FileInfo) mListView.getAdapter().getItem(position);
		if (file.isDir()) {
			getAndDisplayFileList(file.getName());
		}
	}

	@OnClick(R.id.drive_browser_list_refresh)
	void emptyRefresh() {
		updateCurrentFileList();
	}

	@OnClick(R.id.select_back_btn_id)
	void clickOnbackBtn() {
		if (mFileExplorer == null || mFileExplorer.isRoot()) {
			onBackPressed();
		} else {
			getAndDisplayFileList("..");
		}
	}

	@OnClick(R.id.select_new_dir_btn_id)
	void clickOnNewDirBtn() {
		CreateNewFolderDialog mCreateNewFolderDialog;
		mCreateNewFolderDialog = new CreateNewFolderDialog(RemoteFilePathSelectActivity.this);
		mCreateNewFolderDialog.setOnCreateFolderListener(mCreateFolderListener);
		mCreateNewFolderDialog.show();
	}

	private void createNewFile(final String filename) {
		Task.callInBackground(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				if (mFileExplorer != null) {
					return mFileExplorer.mkdir(filename);
				}
				return false;
			}
		}).continueWith(new Continuation<Boolean, Void>() {
			@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
			@Override
			public Void then(Task<Boolean> task) throws Exception {
				if (task.isFaulted()) {
					if (task.getError() instanceof DirectoryAlreadyExistsException) {
						toasts(getString(R.string.directory_already_exists, filename));
					}
				} else {
					if (task.getResult()) {
						getAndDisplayFileList(".");
					} else {
						toasts(getString(R.string.new_folder_fail));
					}
				}

				return null;
			}
		}, Task.UI_THREAD_EXECUTOR);
	}

	private CreateNewFolderDialog.OnCreateFolderListener mCreateFolderListener = new CreateNewFolderDialog.OnCreateFolderListener() {
		@Override
		public void onCreate(String folder) {
			if (!folder.isEmpty()) {
				createNewFile(folder);
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				if (mFileExplorer == null || mFileExplorer.isRoot()) {
					onBackPressed();
				} else {
					getAndDisplayFileList("..");
				}
				return true;
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@OnClick(R.id.left_btn_id)
	void clickOnCancelBtn() {
		onBackPressed();
	}

	@OnClick(R.id.right_btn_id)
	void clickOnConfirmBtn() {
		if (mFileExplorer != null) {
			switch (mSelectFor) {
				case PATH_SELECT_FOR_MOVE:
					EventBus.getDefault().post(new StartMoveRemoteFileEvent(mFileExplorer.pwd(false)));
					break;
				case PATH_SELECT_FOR_UPLOAD:
					EventBus.getDefault().post(new SelectUploadPathEvent(mFileExplorer.pwd(true), mFileExplorer.pwd(false)));
					break;
				case PATH_SELECT_FOR_RELATIVE:
					EventBus.getDefault().post(new SelectRelativePathEvent(mFileExplorer.pwd(true), mFileExplorer.pwd(false)));
					break;
			}
		}
		onBackPressed();
	}
}
