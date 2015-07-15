package com.vae.wuyunxing.webdav.mobile.main;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.vae.wuyunxing.commomui.widget.CommonDialog;
import com.vae.wuyunxing.webdav.library.FileBrowserFactory;
import com.vae.wuyunxing.webdav.library.FileCategory;
import com.vae.wuyunxing.webdav.library.FileExplorer;
import com.vae.wuyunxing.webdav.library.FileInfo;
import com.vae.wuyunxing.webdav.library.exception.DirectoryAlreadyExistsException;
import com.vae.wuyunxing.webdav.library.filter.FileFilter;
import com.vae.wuyunxing.webdav.library.imp.jackrabbit.JackrabbitPath;
import com.vae.wuyunxing.webdav.library.log.MKLog;
import com.vae.wuyunxing.webdav.library.sort.FileSorter;
import com.vae.wuyunxing.webdav.library.util.FileUtil;
import com.vae.wuyunxing.webdav.library.util.LocalStorageUtils;
import com.vae.wuyunxing.webdav.library.util.PathUtil;
import com.vae.wuyunxing.webdav.mobile.MobileApplication;
import com.vae.wuyunxing.webdav.mobile.MobileBaseActivity;
import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.message.BackParentEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.CreateFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.DeleteRemoteFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.DirChangedEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.DownloadRemoteFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.EditCheckAllEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.EditSelectionEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.EnterEditModeEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.ExitEditModeEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.FilterFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.MoveRemoteFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.PlayFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.RenameRemoteFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.SortFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.StartDownloadEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.StartMoveRemoteFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.StartUploadEvent;
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

import de.greenrobot.event.EventBus;

import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
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
	private FileExplorer   mFileExplorer   = null;
	/**
	 * current directory file list
	 */
	private List<FileInfo> mCurDirFileList = null;
	/**
	 * display file list (current directory file list sort)
	 */
	private List<FileInfo> mDispFileList   = null;

	/**
	 * state
	 */
	private boolean    mIsRefreshing = false;
	private boolean    mIsInEditMode = false;
	/**
	 * filter type
	 */
	private int        mFilterType   = FilterFileEvent.FILTER_TYPE_ALL;
	/**
	 * sorter type
	 */
	private FileSorter mSorter       = FileSorter.FILE_NAME_ASCENDING;

	/**
	 * Selections
	 */
	private final Set<Integer> mSelections = new HashSet<Integer>();

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

	/**
	 * refresh state *
	 */
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

	/**
	 * ************
	 */

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
		}, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<Void, Boolean>() {
			@Override
			public Boolean then(Task<Void> task) throws Exception {
				if (mFileExplorer == null) {
					JackrabbitPath path = getJackrabbitPath();
					mFileExplorer = FileBrowserFactory.createJackrabbitFileExplorer(path, mContext);
				}
				return true;
			}
		}, Task.BACKGROUND_EXECUTOR).continueWith(new Continuation<Boolean, Void>() {
			@Override
			public Void then(Task<Boolean> task) throws Exception {
				if (task.isFaulted()) {
					updateFileListView(mDispFileList);
					mFileExplorer = null;
				} else if (task.isCompleted() && task.getResult()) {
					getAndDisplayFileList(".");
				} else {
					/** exception */
					updateFileListView(mDispFileList);
					mFileExplorer = null;
				}
				return null;
			}
		}, Task.UI_THREAD_EXECUTOR);
	}

	/**
	 * get the remote file list and display in ListView
	 */
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
				/** cd (goto path)*/
				mFileExplorer.cd(path);
				/** ls (get the file list)*/
				mCurDirFileList = mFileExplorer.ls("-l");
				/** sort and filter */
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

	/**
	 * Sort and filter the list
	 */
	private List<FileInfo> sortAndFilterFiles(final List<FileInfo> all) {
		return sortFiles(filterFiles(all, mFilterType), mSorter);
	}

	/**
	 * filter
	 */
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

	/**
	 * sorte
	 */
	private List<FileInfo> sortFiles(final List<FileInfo> list, final FileSorter sorter) {
		final Comparator<FileInfo> comparator = sorter.getSorter();
		Collections.sort(list, comparator);
		return list;
	}

	/**
	 * get jackrabbit path
	 */
	private JackrabbitPath getJackrabbitPath() {
		String domain = getActivity().getResources().getString(R.string.webdav_domain);
		String sambaUser = getActivity().getResources().getString(R.string.webdav_root);
		String password = getResources().getString(R.string.webdav_password);
		String path = PathUtil.appendPath(true, "/");
		return new JackrabbitPath(domain, path, sambaUser, password);
	}

	private void postSelectionEvent(int selection, int total) {
		EventBus.getDefault().post(new EditSelectionEvent(selection, total));
	}

	void exitEditMode() {
		mIsInEditMode = false;

		mSelections.clear();
		((FileListAdapter) mListView.getAdapter()).clearEditMode();
	}

	private FileSorter updateSorter(FileSorter oldSorter, int sortType) {
		FileSorter newSorter = null;
		switch (sortType) {
			case MainActivity.SORT_TYPE_FILE_NAME:
				if (oldSorter == FileSorter.FILE_NAME_ASCENDING) {
					newSorter = FileSorter.FILE_NAME_DESCENDING;
				} else {
					newSorter = FileSorter.FILE_NAME_ASCENDING;
				}
				break;
			case MainActivity.SORT_TYPE_FILE_SIZE:
				if (oldSorter == FileSorter.FILE_SIZE_ASCENDING) {
					newSorter = FileSorter.FILE_SIZE_DESCENDING;
				} else {
					newSorter = FileSorter.FILE_SIZE_ASCENDING;
				}
				break;
			case MainActivity.SORT_TYPE_DATE:
				if (oldSorter == FileSorter.FILE_DATE_ASCENDING) {
					newSorter = FileSorter.FILE_DATE_DESCENDING;
				} else {
					newSorter = FileSorter.FILE_DATE_ASCENDING;
				}
				break;
			case MainActivity.SORT_TYPE_SUFFIX:
				if (oldSorter == FileSorter.FILE_SUFFIX_ASCENDING) {
					newSorter = FileSorter.FILE_SUFFIX_DESCENDING;
				} else {
					newSorter = FileSorter.FILE_SUFFIX_ASCENDING;
				}
				break;
		}
		return newSorter;
	}

	private int getCurrentSelectNum() {
		int selectItem = 0;
		if (!mIsInEditMode) {
			return selectItem;
		}

		for (FileInfo fileInfo : mDispFileList) {
			int hash = fileInfo.getName().hashCode();
			if (mSelections.contains(hash)) {
				selectItem++;
			}
		}
		return selectItem;
	}

	private void showDeleteDialog() {
		final CommonDialog deleteDialog = new CommonDialog(getActivity(), R.style.Dialog);
		TextView textTips = new TextView(getActivity());
		textTips.setText(R.string.str_delete_notice);
		deleteDialog.setView(textTips);
		deleteDialog.setTitleText(R.string.str_delete);
		deleteDialog.setTitleTextColor(getResources().getColor(R.color.white));
		deleteDialog.setTitleBackground(R.drawable.dialog_title_blue);
		deleteDialog.setPositiveButtonBackground(R.drawable.dialog_button_selector);
		deleteDialog.setPositiveButtonTextColor(getResources().getColor(R.color.black_80alpha));
		deleteDialog.setNegativeButtonTextColor(getResources().getColor(R.color.black_80alpha));
		deleteDialog.setNegativeButtonBackground(R.drawable.dialog_button_selector);
		deleteDialog.setPositiveButton(R.string.ok, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				deleteDialog.cancel();
				startDeleteFiles();
			}

		});
		deleteDialog.setNegativeButton(R.string.cancel, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				deleteDialog.cancel();
			}
		});
		deleteDialog.show();
	}

	private void startDeleteFiles() {
		Task.call(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				((MobileBaseActivity) getActivity()).showWaitingDialog();

				return null;
			}
		}, Task.UI_THREAD_EXECUTOR).onSuccess(new Continuation<Void, Void>() {
			@Override
			public Void then(Task<Void> task) throws Exception {
				for (FileInfo fileInfo : mDispFileList) {
					int hash = fileInfo.getName().hashCode();
					if (mSelections.contains(hash)) {
						mFileExplorer.rm(fileInfo.getName());
					}
				}
				return null;
			}
		}, Task.BACKGROUND_EXECUTOR).continueWith(new Continuation<Void, Void>() {
			@Override
			public Void then(Task<Void> task) throws Exception {
				((MobileBaseActivity) getActivity()).dismissWaitingDialog();
				EventBus.getDefault().post(new ExitEditModeEvent());
				getAndDisplayFileList(".");
				return null;
			}
		}, Task.UI_THREAD_EXECUTOR);
	}

	private void showRenameDialog(final FileInfo renameFile) {
		final CommonDialog renameDialog = new CommonDialog(getActivity(), R.style.Dialog);
		final EditText editFile = new EditText(getActivity());
		editFile.setCompoundDrawablesWithIntrinsicBounds(R.drawable.new_folder, 0, 0, 0);
		StringBuilder builder = new StringBuilder(renameFile.getName());
		if (builder.charAt(builder.length() - 1) == '/') {
			builder.deleteCharAt(builder.length() - 1);
		}
		editFile.setText(builder.toString());
		editFile.setBackgroundResource(R.drawable.dialog_edit_selector);
		renameDialog.setView(editFile);
		renameDialog.setTitleBackground(R.drawable.dialog_title_blue);
		renameDialog.setTitleTextColor(getResources().getColor(R.color.white));
		renameDialog.setTitleText(R.string.str_rename);
		renameDialog.setPositiveButtonBackground(R.drawable.dialog_button_selector);
		renameDialog.setPositiveButtonTextColor(getResources().getColor(R.color.black_80alpha));
		renameDialog.setNegativeButtonTextColor(getResources().getColor(R.color.black_80alpha));
		renameDialog.setNegativeButtonBackground(R.drawable.dialog_button_selector);
		renameDialog.setNegativeButton(R.string.cancel, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				renameDialog.cancel();
			}

		});
		renameDialog.setPositiveButton(R.string.ok, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Task.call(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						renameDialog.dismiss();
						((MobileBaseActivity) getActivity()).showWaitingDialog();

						return null;
					}
				}, Task.UI_THREAD_EXECUTOR).onSuccess(new Continuation<Void, Boolean>() {
					@Override
					public Boolean then(Task<Void> task) throws Exception {
						String fileName = editFile.getText().toString();
						for (FileInfo fileInfo : mCurDirFileList) {
							if (fileInfo.getName().equals(fileName) || fileInfo.getName().equals(fileName + '/')) {
								return false;
							}
						}
						return mFileExplorer.renameFileOrDir(renameFile.getName(), fileName);
					}
				}, Task.BACKGROUND_EXECUTOR).continueWith(new Continuation<Boolean, Void>() {
					@Override
					public Void then(Task<Boolean> task) throws Exception {
						((MobileBaseActivity) getActivity()).dismissWaitingDialog();
						if (task.isFaulted()) {
							((MobileBaseActivity) getActivity()).toasts(getString(R.string.rename_failed));
						} else if (task.isCompleted()) {
							if (task.getResult()) {
								((MobileBaseActivity) getActivity()).toasts(getString(R.string.rename_success));
							} else {
								((MobileBaseActivity) getActivity()).toasts(getString(R.string.rename_conflict));
							}
						}
						EventBus.getDefault().post(new ExitEditModeEvent());
						getAndDisplayFileList(".");
						return null;
					}
				}, Task.UI_THREAD_EXECUTOR);
			}
		});
		renameDialog.show();
	}

	/**
	 * view operation  *
	 */
	@OnItemClick(R.id.drive_browser_file_list)
	void fileClick(int position) {
		if (mIsInEditMode) {
			FileListAdapter adapter = (FileListAdapter) mListView.getAdapter();
			int hash = ((FileInfo) adapter.getItem(position)).getName().hashCode();
			if (mSelections.contains(hash)) {
				mSelections.remove(hash);
			} else {
				mSelections.add(hash);
			}
			adapter.notifyDataSetChanged();

			postSelectionEvent(mSelections.size(), mCurDirFileList.size());
		} else {
			FileInfo file = (FileInfo) mListView.getAdapter().getItem(position);
			if (file.isDir()) {
				getAndDisplayFileList(file.getName());
			} else {
				EventBus.getDefault().post(new PlayFileEvent(file.getUri()));
			}
		}
	}


	@OnItemLongClick(R.id.drive_browser_file_list)
	boolean enterEditMode(int position) {
		mIsInEditMode = true;

		FileInfo file = (FileInfo) mListView.getAdapter().getItem(position);
		mSelections.add(file.getName().hashCode());
		((FileListAdapter) mListView.getAdapter()).setEditMode(mSelections);

		EventBus.getDefault().post(new EnterEditModeEvent());

		return true;
	}

	/** EventBus event **/

	/**
	 * back event
	 */
	public void onEventMainThread(BackParentEvent event) {
		if (mIsInEditMode) {
			EventBus.getDefault().post(new ExitEditModeEvent());
		} else if (mFileExplorer == null || mFileExplorer.isRoot()) {
			getActivity().onBackPressed();
		} else {
			getAndDisplayFileList("..");
		}
	}

	/**
	 * exit edit mode event
	 */
	public void onEventMainThread(ExitEditModeEvent event) {
		exitEditMode();
	}

	/**
	 * edit check all event
	 */
	public void onEventMainThread(EditCheckAllEvent event) {
		if (mIsInEditMode) {
			if (event.mIsCheckAll) {
				for (FileInfo file : mCurDirFileList) {
					mSelections.add(file.getName().hashCode());
				}
			} else {
				mSelections.clear();
			}
			((FileListAdapter) mListView.getAdapter()).notifyDataSetChanged();

			postSelectionEvent(mSelections.size(), mCurDirFileList.size());
		}
	}

	/**
	 * filter file event
	 */
	public void onEventMainThread(FilterFileEvent event) {
		if (!event.mIsLocalFileFilterEvent) {
			mFilterType = event.mCategory;
			Task.callInBackground(new Callable<List<FileInfo>>() {
				@Override
				public List<FileInfo> call() throws Exception {
					return sortAndFilterFiles(mCurDirFileList);
				}
			}).onSuccess(new Continuation<List<FileInfo>, Void>() {
				@Override
				public Void then(Task<List<FileInfo>> task) throws Exception {
					mDispFileList = task.getResult();
					updateFileListView(mDispFileList);
					return null;
				}
			}, Task.UI_THREAD_EXECUTOR);
		} else {
			MKLog.d(MainActivity.class, "FilterType: " + event.mCategory);
			Intent intent = new Intent();
			intent.setClass(getActivity(), UploadActivity.class);
			intent.putExtra(MainActivity.KEY_CATEGORY_TYPE, event.mCategory);
			startActivity(intent);
		}
	}

	/**
	 * sort file event
	 */
	public void onEventMainThread(SortFileEvent event) {
		mSorter = updateSorter(mSorter, event.mSortType);
		Task.callInBackground(new Callable<List<FileInfo>>() {
			@Override
			public List<FileInfo> call() throws Exception {
				return sortFiles(((FileListAdapter) mListView.getAdapter()).getFileList(), mSorter);
			}
		}).onSuccess(new Continuation<List<FileInfo>, Void>() {
			@Override
			public Void then(Task<List<FileInfo>> task) throws Exception {
				mDispFileList = task.getResult();
				updateFileListView(mDispFileList);
				return null;
			}
		}, Task.UI_THREAD_EXECUTOR);
	}

	/**
	 * crete file event
	 */
	public void onEventMainThread(CreateFileEvent event) {
		final String filename = event.mFilename;
		Task.callInBackground(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return mFileExplorer != null && mFileExplorer.mkdir(filename);
			}
		}).continueWith(new Continuation<Boolean, Void>() {
			@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
			@Override
			public Void then(Task<Boolean> task) throws Exception {
				if (task.isFaulted()) {
					if (task.getError() instanceof DirectoryAlreadyExistsException) {
						((MobileBaseActivity) getActivity()).toasts(getString(R.string.directory_already_exists, filename));
					}
				} else {
					if (task.getResult()) {
						getAndDisplayFileList(".");
					} else {
						((MobileBaseActivity) getActivity()).toasts(getString(R.string.new_folder_fail));
					}
				}

				return null;
			}
		}, Task.UI_THREAD_EXECUTOR);
	}

	/**
	 * delete remote file event
	 */
	public void onEventMainThread(DeleteRemoteFileEvent event) {
		if (getCurrentSelectNum() > 0) {
			showDeleteDialog();
		}
	}

	/**
	 * rename remote file event
	 */
	public void onEventMainThread(RenameRemoteFileEvent event) {
		int selectItem = getCurrentSelectNum();
		FileInfo renameFile = null;

		if (selectItem == 1) {
			for (FileInfo fileInfo : mDispFileList) {
				int hash = fileInfo.getName().hashCode();
				if (mSelections.contains(hash)) {
					renameFile = fileInfo;
				}
			}
			if (renameFile != null) {
				showRenameDialog(renameFile);
			}
		} else if (selectItem > 1) {
			((MobileBaseActivity) getActivity()).toasts(getString(R.string.str_can_not_rename_at_same_time));
		}
	}

	/**
	 * move remote file event
	 */
	public void onEventMainThread(MoveRemoteFileEvent event) {
		MKLog.d(RemoteFileListFragment.class, "MoveRemoteFileEvent 0");
		if (getCurrentSelectNum() == 0) {
			return;
		}

		MKLog.d(RemoteFileListFragment.class, "MoveRemoteFileEvent 1");
		Intent intent = new Intent();
		intent.putExtra(RemoteFilePathSelectActivity.KEY_TYPE, RemoteFilePathSelectActivity.PATH_SELECT_FOR_MOVE);
		intent.setClass(getActivity(), RemoteFilePathSelectActivity.class);
		startActivity(intent);
	}

	/**
	 * start move remote file event
	 */
	public void onEventMainThread(final StartMoveRemoteFileEvent event) {
		if (getCurrentSelectNum() == 0) {
			return;
		}

		Task.call(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				((MobileBaseActivity) getActivity()).showWaitingDialog();
				return null;
			}
		}, Task.UI_THREAD_EXECUTOR).onSuccess(new Continuation<Void, Boolean>() {
			@Override
			public Boolean then(Task<Void> task) throws Exception {
				String newPath = event.moveToPath;
				for (FileInfo fileInfo : mCurDirFileList) {
					int hash = fileInfo.getName().hashCode();
					if (mSelections.contains(hash)) {
						try {
							mFileExplorer.mv(fileInfo.getUri(), newPath);
						} catch (DirectoryAlreadyExistsException e) {
							return false;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				return true;
			}
		}, Task.BACKGROUND_EXECUTOR).continueWith(new Continuation<Boolean, Void>() {
			@Override
			public Void then(Task<Boolean> task) throws Exception {
				((MobileBaseActivity) getActivity()).dismissWaitingDialog();
				if (task.isFaulted()) {
					((MobileBaseActivity) getActivity()).toasts(getString(R.string.str_move_failed));
				} else if (task.isCompleted()) {
					if (task.getResult()) {
						EventBus.getDefault().post(new ExitEditModeEvent());
						getAndDisplayFileList(".");
						((MobileBaseActivity) getActivity()).toasts(getString(R.string.str_move_success));
					} else {
						((MobileBaseActivity) getActivity()).toasts(getString(R.string.rename_conflict));
					}
				}
				return null;
			}
		}, Task.UI_THREAD_EXECUTOR);
	}

	/**
	 * download remote file event (select local file path)
	 */
	public void onEventMainThread(DownloadRemoteFileEvent event) {

		MKLog.d(RemoteFileListFragment.class, "DownloadRemoteFileEvent 0");
		if (getCurrentSelectNum() == 0) {
			return;
		}

		MKLog.d(RemoteFileListFragment.class, "DownloadRemoteFileEvent 1");
		Intent intent = new Intent();
		intent.setClass(getActivity(), LocalFilePathSelectActivity.class);
		startActivity(intent);
	}

	public void onEventMainThread(StartDownloadEvent event) {
		String localPath = event.selectLocalPath;
		List<TransferService.Param> params = new ArrayList<TransferService.Param>();
		params.clear();
		for (FileInfo fileInfo : mDispFileList) {
			int hash = fileInfo.getName().hashCode();
			if (mSelections.contains(hash)) {
				if (fileInfo.isDir()) {
					//TODO:
				} else {
					params.add(getDownloadParam(fileInfo, localPath));
				}
			}
		}

		if (params.size() > 0) {
			MobileApplication.getInstance().getTransferManager().enqueue(false, params);
			EventBus.getDefault().post(new ExitEditModeEvent());
			((MobileBaseActivity) getActivity()).toasts(getString(R.string.downloading_wait));
		}
		exitEditMode();
	}

	private TransferService.Param getDownloadParam(FileInfo fileInfo, String localPath) {
		String filename = fileInfo.getName();
		String to = localPath;
		String from = fileInfo.getParent();
		return new TransferService.Param(filename, from, to, false, fileInfo.size(), fileInfo.hashCode());
	}
}
