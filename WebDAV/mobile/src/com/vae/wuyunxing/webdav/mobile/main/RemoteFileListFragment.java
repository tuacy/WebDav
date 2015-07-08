package com.vae.wuyunxing.webdav.mobile.main;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.vae.wuyunxing.webdav.library.FileBrowserFactory;
import com.vae.wuyunxing.webdav.library.FileCategory;
import com.vae.wuyunxing.webdav.library.FileExplorer;
import com.vae.wuyunxing.webdav.library.FileInfo;
import com.vae.wuyunxing.webdav.library.filter.FileFilter;
import com.vae.wuyunxing.webdav.library.imp.jackrabbit.JackrabbitPath;
import com.vae.wuyunxing.webdav.library.log.MKLog;
import com.vae.wuyunxing.webdav.library.sort.FileSorter;
import com.vae.wuyunxing.webdav.library.util.FileUtil;
import com.vae.wuyunxing.webdav.library.util.PathUtil;
import com.vae.wuyunxing.webdav.mobile.MobileBaseActivity;
import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.message.CreateFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.DirChangedEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.FilterFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.SortFileEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;
import butterknife.ButterKnife;
import butterknife.InjectView;

import de.greenrobot.event.EventBus;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;

public class RemoteFileListFragment extends Fragment {

	@InjectView(R.id.drive_browser_ptr_frame_list)
	PtrFrameLayout mPtrFrameList;

	@InjectView(R.id.drive_browser_file_list)
	ListView mListView;

	@InjectView(R.id.drive_browser_empty_hint)
	FrameLayout mEmptyHint;

	@InjectView(R.id.drive_browser_list_hint)
	TextView mListHint;

	/**
	 * Explorer
	 */
	private FileExplorer   mFileExplorer;
	private List<FileInfo> mCurDirFileList;
	private List<FileInfo> mDispFileList;

	/**
	 * state
	 */
	private boolean    mIsRefreshing = false;
	private boolean    mIsInEditMode = false;
	private int        mFilterType   = FilterFileEvent.FILTER_TYPE_ALL;
	private FileSorter mSorter       = FileSorter.FILE_NAME_ASCENDING;

	private Context mContext;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_drive_browser_file_list, container, false);
		ButterKnife.inject(this, view);
		mContext = getActivity();

		EventBus.getDefault().register(this);

		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.reset(this);
		EventBus.getDefault().unregister(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		initPtrFrameLayout(mPtrFrameList);
		initListView(mListView);
		getRemoteFileList();
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

	private void initListView(ListView listView) {
		listView.setAdapter(new FileListAdapter(LayoutInflater.from(getActivity())));
	}

	private void updateFileListView(List<FileInfo> fileList) {
		((MobileBaseActivity) getActivity()).dismissWaitingDialog();
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

	/** refresh state **/
	private void refreshBegin() {
		setRefreshing(true);
		mPtrFrameList.setEnabled(false);
	}

	private void setRefreshing(boolean refreshing) {
		mIsRefreshing = refreshing;
	}

	private boolean isRefreshing() {
		return mIsRefreshing;
	}

	private void refreshComplete() {
		setRefreshing(false);
		mPtrFrameList.setEnabled(true);
		mPtrFrameList.refreshComplete();
	}
	/****************/

	private void initPtrFrameLayout(PtrFrameLayout ptrFrame) {
		ptrFrame.setPtrHandler(new PtrDefaultHandler() {
			@Override
			public void onRefreshBegin(PtrFrameLayout ptrFrameLayout) {
				refreshBegin();
				updateCurrentFileList();
			}
		});
	}

	/**
	 * get remote file list from WebDAV server
	 */
	private void updateCurrentFileList() {
		getRemoteFileList();
	}

	private void getRemoteFileList() {
		Task.call(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				if (!isRefreshing()) {
					((MobileBaseActivity) getActivity()).showWaitingDialog();
				}
				mCurDirFileList = null;
				mDispFileList = null;
				return null;
			}
		}, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<Void, Void>() {
			@Override
			public Void then(Task<Void> task) throws Exception {
				String password = "admin";
				if (mFileExplorer == null) {
					JackrabbitPath path = getJackrabbitPath(password);
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
					((MobileBaseActivity) getActivity()).showWaitingDialog();
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

	private List<FileInfo> sortAndFilterFiles(final List<FileInfo> all) {
		return sortFiles(filterFiles(all, mFilterType), mSorter);
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

	private JackrabbitPath getJackrabbitPath(String password) {
		String domain = "192.168.11.1";
		String sambaUser = "root";
		String currentUser = "hardy";
		String userStoragePath = "Home";
		String path = PathUtil.appendPath(true, JackrabbitPath.MOUNT_DIR, userStoragePath, currentUser);
		return new JackrabbitPath(domain, path, sambaUser, password);
	}

	/**
	 * filter file event
	 */
	public void onEventMainThread(FilterFileEvent event) {
		MKLog.d(RemoteFileListFragment.class, "get filter event");
	}

	/**
	 * sort file event
	 */
	public void onEventMainThread(SortFileEvent event) {
	}

	/**
	 * create new file event
	 */
	public void onEventMainThread(CreateFileEvent event) {
	}

}
