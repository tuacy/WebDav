package com.vae.wuyunxing.webdav.mobile.main;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;


import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.vae.wuyunxing.webdav.library.FileBrowserFactory;
import com.vae.wuyunxing.webdav.library.FileCategory;
import com.vae.wuyunxing.webdav.library.FileInfo;
import com.vae.wuyunxing.webdav.library.filter.FileFilter;
import com.vae.wuyunxing.webdav.library.imp.local.LocalFileExplorer;
import com.vae.wuyunxing.webdav.library.imp.local.LocalFileInfo;
import com.vae.wuyunxing.webdav.library.imp.local.LocalPath;
import com.vae.wuyunxing.webdav.library.sort.FileSorter;
import com.vae.wuyunxing.webdav.library.util.FileUtil;
import com.vae.wuyunxing.webdav.mobile.MobileApplication;
import com.vae.wuyunxing.webdav.mobile.MobileBaseActivity;
import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.message.DirChangedEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.ExitToImageOrVideoDirEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.ExitToLocalStorageList;
import com.vae.wuyunxing.webdav.mobile.main.message.FilterFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.LocalFileListBackEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.UploadloadFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.transmission.TransferService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

public class LocalFileListFragment extends Fragment {

	public LocalFileListFragment() {
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


	private LocalFileExplorer mFileExplorer;
	private List<FileInfo>    mFileList;

	private FileSorter mSorter       = FileSorter.FILE_NAME_ASCENDING;
	private boolean    mIsRefreshing = false;
	private int        mCategoryType = FilterFileEvent.FILTER_TYPE_ALL;
	private boolean    mIsViewInited = false;
	private String mSbuDirName;
	private       boolean      mIsFragmentHide = false;
	private final Set<Integer> mSelections     = new HashSet<Integer>();

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

		Bundle bundle = getArguments();
		mCategoryType = bundle.getInt(UploadActivity.CATEGORY_KEY, FilterFileEvent.FILTER_TYPE_ALL);
		mSbuDirName = bundle.getString(UploadActivity.BUCKET_KEY);
		initView();
		updateCurrentFileList();
	}

	private void initView() {
		if (!mIsViewInited) {
			initPtrFrameLayout(mPtrFrameList);
			if (mCategoryType == FilterFileEvent.FILTER_TYPE_PHOTO) {
				mListView.setNumColumns(GridView.AUTO_FIT);
				mListView.setColumnWidth(getResources().getDimensionPixelSize(R.dimen.grid_view_item_width));
				mListView.setHorizontalSpacing(getActivity().getResources().getInteger(R.integer.image_thumb_gridview_space));
				mListView.setVerticalSpacing(getActivity().getResources().getInteger(R.integer.image_thumb_gridview_space));
				mListView.setAdapter(new UploadGridAdapter(getActivity()));
				mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
				((UploadGridAdapter) mListView.getAdapter()).setSelections(mSelections);
			} else {
				mListView.setAdapter(new LocalFileListAdapter(LayoutInflater.from(getActivity())));
				mListView.setNumColumns(1);
				((LocalFileListAdapter) mListView.getAdapter()).setSelections(mSelections);
			}
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
				if (mFileExplorer == null) {
					try {
						LocalPath localPath = new LocalPath(mSbuDirName, "");
						mFileExplorer = (LocalFileExplorer) FileBrowserFactory.createLocalFileExplorer(localPath);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return null;
			}
		}).onSuccess(new Continuation<Void, List<FileInfo>>() {
			@Override
			public List<FileInfo> then(Task<Void> task) throws Exception {
				List<FileInfo> fileList = null;
				switch (mCategoryType) {
					case FilterFileEvent.FILTER_TYPE_DOC:
						fileList = getAllDocumentList();
						break;
					case FilterFileEvent.FILTER_TYPE_MUSIC:
						fileList = getAllMusicList();
						break;
					case FilterFileEvent.FILTER_TYPE_BT:
						fileList = getAllBtFileList();
						break;
					case FilterFileEvent.FILTER_TYPE_APP:
						fileList = getAllApkFileList();
						break;
					case FilterFileEvent.FILTER_TYPE_VIDEO:
						fileList = getSelectVideoList(mSbuDirName);
						break;
					case FilterFileEvent.FILTER_TYPE_PHOTO:
						fileList = getSelectPhotoList(mSbuDirName);
						break;
					case FilterFileEvent.FILTER_TYPE_ALL:
					default:
						fileList = accessAndGetFileList(".");
						break;
				}
				return fileList;
			}
		}, Task.BACKGROUND_EXECUTOR).continueWith(new Continuation<List<FileInfo>, Void>() {
			@Override
			public Void then(Task<List<FileInfo>> task) throws Exception {
				List<FileInfo> fileList = task.getResult();
				updateFileListView(fileList);
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

		if (mSelections.size() > 0) {
			if (mCategoryType == FilterFileEvent.FILTER_TYPE_PHOTO) {
				((UploadGridAdapter) mListView.getAdapter()).clearSelections();
			} else {
				((LocalFileListAdapter) mListView.getAdapter()).clearSelections();
			}
			return;
		}

		switch (mCategoryType) {
			case FilterFileEvent.FILTER_TYPE_DOC:
			case FilterFileEvent.FILTER_TYPE_MUSIC:
			case FilterFileEvent.FILTER_TYPE_BT:
			case FilterFileEvent.FILTER_TYPE_APP:
				getActivity().onBackPressed();
				break;
			case FilterFileEvent.FILTER_TYPE_ALL:
				if (!mFileExplorer.isRoot()) {
					List<FileInfo> fileList = accessAndGetFileList("..");
					updateFileListView(fileList);
				} else {
					EventBus.getDefault().post(new ExitToLocalStorageList());
				}
				break;
			case FilterFileEvent.FILTER_TYPE_PHOTO:
			case FilterFileEvent.FILTER_TYPE_VIDEO:
				EventBus.getDefault().post(new ExitToImageOrVideoDirEvent());
				break;
			default:
				break;
		}
	}

	private List<FileInfo> accessAndGetFileList(final String path) {
		try {
			mFileExplorer.cd(path);
			List<FileInfo> tempList = mFileExplorer.ls("-l");
			mFileList = sortAndFilterFiles(tempList);
			return mFileList;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private List<FileInfo> getAllMusicList() {
		List<FileInfo> tmpList = LocalFileExplorer.getMusicList(getActivity());
		mFileList = sortAndFilterFiles(tmpList);
		return mFileList;
	}

	private List<FileInfo> getAllDocumentList() {
		List<FileInfo> tmpList = LocalFileExplorer.getDocumentList();
		mFileList = sortAndFilterFiles(tmpList);
		return mFileList;
	}

	private List<FileInfo> getAllApkFileList() {
		List<FileInfo> tmpList = LocalFileExplorer.getApkFileList();
		mFileList = sortAndFilterFiles(tmpList);
		return mFileList;
	}

	private List<FileInfo> getAllBtFileList() {
		List<FileInfo> tmpList = LocalFileExplorer.getBtFileList();
		mFileList = sortAndFilterFiles(tmpList);
		return mFileList;
	}

	private List<FileInfo> getSelectVideoList(String bucketDisplayName) {
		List<FileInfo> tmpList = LocalFileExplorer.getSelectVideoList(getActivity(), bucketDisplayName);
		mFileList = sortAndFilterFiles(tmpList);
		return mFileList;
	}

	private List<FileInfo> getSelectPhotoList(String bucketDisplayName) {
		List<FileInfo> tmpList = LocalFileExplorer.getSelectPhotoList(getActivity(), bucketDisplayName);
		mFileList = sortAndFilterFiles(tmpList);
		return mFileList;
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

    private void getAndDisplayFileList(final String path) {
        Task.call(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (!isRefreshing()) {
                    ((MobileBaseActivity) getActivity()).showWaitingDialog();
                }
                mFileList = null;
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<Void, List<FileInfo>>() {
            @Override
            public List<FileInfo> then(Task<Void> task) throws Exception {
                /** cd */
                mFileExplorer.cd(path);
                /** ls */
                List<FileInfo> tempList = mFileExplorer.ls("-l");
                return sortAndFilterFiles(tempList);
            }
        }, Task.BACKGROUND_EXECUTOR).continueWith(new Continuation<List<FileInfo>, Void>() {
            @Override
            public Void then(Task<List<FileInfo>> task) throws Exception {
                if (task.isCompleted()) {
                    try {
                        mFileList = task.getResult();
                        EventBus.getDefault().post(new DirChangedEvent(mFileExplorer.isRoot(), mFileExplorer.pwd(true)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                updateFileListView(mFileList);
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }


    @OnItemClick(R.id.file_list)
	void clickOnFileList(int position) {

		FileInfo file = (FileInfo) mListView.getAdapter().getItem(position);
		if (file.isDir()) {
			((LocalFileListAdapter) mListView.getAdapter()).clearSelections();
            getAndDisplayFileList(file.getName());
		} else {
			if (file instanceof LocalFileInfo) {
				int hash = mListView.getAdapter().getItem(position).hashCode();
				if (mSelections.contains(hash)) {
					mSelections.remove(hash);
				} else {
					mSelections.add(hash);
				}
				((BaseAdapter) (mListView.getAdapter())).notifyDataSetChanged();
			}
		}
	}

	private TransferService.Param getUploadParam(FileInfo fileInfo, String uploadPath) {
		String filename = fileInfo.getName();
		String from = fileInfo.getParent();
		return new TransferService.Param(filename, from, uploadPath, false, fileInfo.size(), fileInfo.hashCode());
	}

	public void onEventMainThread(UploadloadFileEvent event) {
		if (mSelections.size() <= 0) {
			return;
		}

		String uploadPath = event.uploadPath;
		List<TransferService.Param> params = new ArrayList<TransferService.Param>();
		params.clear();
		for (FileInfo fileInfo : mFileList) {
			int hash = fileInfo.hashCode();
			if (mSelections.contains(hash)) {
				params.add(getUploadParam(fileInfo, uploadPath));
			}
		}

		if (params.size() > 0) {
			MobileApplication.getInstance().getTransferManager().enqueue(true, params);
			((MobileBaseActivity) getActivity()).toasts(getString(R.string.uploading_wait));
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
