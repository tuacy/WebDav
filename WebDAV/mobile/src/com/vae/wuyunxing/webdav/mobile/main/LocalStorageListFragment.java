package com.vae.wuyunxing.webdav.mobile.main;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;


import com.vae.wuyunxing.webdav.library.FileCategory;
import com.vae.wuyunxing.webdav.library.FileInfo;
import com.vae.wuyunxing.webdav.library.filter.FileFilter;
import com.vae.wuyunxing.webdav.library.imp.local.LocalFileExplorer;
import com.vae.wuyunxing.webdav.library.sort.FileSorter;
import com.vae.wuyunxing.webdav.library.util.FileUtil;
import com.vae.wuyunxing.webdav.mobile.MobileBaseActivity;
import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.message.AccessSubDirEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.FilterFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.LocalFileListBackEvent;

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
import de.greenrobot.event.EventBus;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;

public class LocalStorageListFragment extends Fragment {

	public LocalStorageListFragment() {
		// Required empty public constructor
	}

	@InjectView(R.id.drive_browser_ptr_frame_list)
    PtrFrameLayout mPtrFrameList;

	@InjectView(R.id.file_list)
	GridView mListView;

	@InjectView(R.id.drive_browser_empty_hint)
	FrameLayout mEmptyHint;

	@InjectView(R.id.drive_browser_list_hint)
	TextView mListHint;


	private List<FileInfo> mFileList;

	private FileSorter mSorter         = FileSorter.FILE_NAME_ASCENDING;
	private boolean    mIsRefreshing   = false;
	private int        mCategoryType   = FilterFileEvent.FILTER_TYPE_ALL;
	private boolean    mIsViewInited   = false;
	private boolean    mIsFragmentHide = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_upload_file_list, container, false);
		ButterKnife.inject(this, view);
		EventBus.getDefault().register(this);
		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		EventBus.getDefault().unregister(this);
		ButterKnife.reset(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initView();
		updateCurrentFileList();
	}

	private void initView() {
		if (!mIsViewInited) {
			initPtrFrameLayout(mPtrFrameList);
			mListView.setAdapter(new LocalFileListAdapter(LayoutInflater.from(getActivity())));
			mListView.setNumColumns(1);
			mIsViewInited = true;
		}
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

	private void updateFileListView(List<FileInfo> fileList) {
		initView();
		((MobileBaseActivity) getActivity()).dismissWaitingDialog();
		if (isRefreshing()) {
			refreshComplete();
		}
		if (fileList == null || fileList.isEmpty()) {
			showEmptyHint();
		} else {
			clearEmptyHint();
			if (mCategoryType == FilterFileEvent.FILTER_TYPE_PHOTO) {
				((UploadGridAdapter) mListView.getAdapter()).setFileList(fileList);
			} else {
				((LocalFileListAdapter) mListView.getAdapter()).setFileList(fileList);
			}
		}
	}

	private void updateCurrentFileList() {
		Task.call(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				if (!isRefreshing()) {
					((MobileBaseActivity) getActivity()).showWaitingDialog();
				}
				return null;
			}
		}).onSuccess(new Continuation<Void, List<FileInfo>>() {
			@Override
			public List<FileInfo> then(Task<Void> task) throws Exception {
				List<FileInfo> fileList = LocalFileExplorer.getAllStorageList(getActivity());
				return sortAndFilterFiles(fileList);
			}
		}, Task.BACKGROUND_EXECUTOR).continueWith(new Continuation<List<FileInfo>, Void>() {
			@Override
			public Void then(Task<List<FileInfo>> task) throws Exception {
				mFileList = task.getResult();
				updateFileListView(mFileList);
				return null;
			}
		}, Task.UI_THREAD_EXECUTOR);
	}


	public void onEventMainThread(LocalFileListBackEvent event) {
		if (isRefreshing()) {
			refreshComplete();
		}

		if (mIsFragmentHide) {
			return;
		}
		getActivity().onBackPressed();
	}


	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		mIsFragmentHide = hidden;
	}

	@OnClick(R.id.drive_browser_list_refresh)
	void emptyRefresh() {
		updateCurrentFileList();
	}

	@OnItemClick(R.id.file_list)
	void clickOnFileList(int position) {
		FileInfo file = (FileInfo) mListView.getAdapter().getItem(position);
		if (file.isDir()) {
			EventBus.getDefault ().post(new AccessSubDirEvent(file.getPath()));
		}
	}

	//TODO: below should be common.
	private List<FileInfo> sortAndFilterFiles(final List<FileInfo> all) {
		return sortFiles(filterFiles(all, mCategoryType), mSorter);
	}

	private List<FileInfo> sortFiles(final List<FileInfo> list, final FileSorter sorter) {
		final Comparator<FileInfo> comparator = sorter.getSorter();
		Collections.sort(list, comparator);
		return list;
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

	private void showEmptyHint() {
		mPtrFrameList.setVisibility(View.GONE);
		mEmptyHint.setVisibility(View.VISIBLE);
		mListHint.setText(R.string.empty_folder_hint);
	}

	private void clearEmptyHint() {
		mPtrFrameList.setVisibility(View.VISIBLE);
		mEmptyHint.setVisibility(View.GONE);
	}
}
