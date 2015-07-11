package com.vae.wuyunxing.webdav.mobile.main;


import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;


import com.vae.wuyunxing.webdav.library.imp.local.LocalFileExplorer;
import com.vae.wuyunxing.webdav.mobile.MobileBaseActivity;
import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.message.AccessSubDirEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.FilterFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.LocalFileListBackEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class LocalImageOrVideoDirSelectFragment extends Fragment {

	public LocalImageOrVideoDirSelectFragment() {
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

	/* States */
	private boolean mIsRefreshing = false;
	private boolean mIsViewInited = false;
	private SrcAdapter mSrcAdapter;
	private       int                       mCategoryType   = FilterFileEvent.FILTER_TYPE_PHOTO;
	private       List<Map<String, Object>> mSourceList     = new ArrayList<Map<String, Object>>();
	private final String                    KEY_COUNT       = "count";
	private final String                    KEY_BUCKET      = "bucket";
	private final String                    KEY_THUMB       = "thumb";
	private       boolean                   mIsFragmentHide = false;

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
		mCategoryType = getArguments().getInt(UploadActivity.CATEGORY_KEY, FilterFileEvent.FILTER_TYPE_PHOTO);
		updateCurrentFileList();
	}

	private void initView() {
		if (!mIsViewInited) {
			initPtrFrameLayout(mPtrFrameList);
			mListView.setNumColumns(GridView.AUTO_FIT);
			mListView.setHorizontalSpacing(getActivity().getResources().getInteger(R.integer.image_thumb_gridview_space));
			mListView.setVerticalSpacing(getActivity().getResources().getInteger(R.integer.image_thumb_gridview_space));
			mListView.setColumnWidth(getResources().getDimensionPixelSize(R.dimen.fb_gridview_width_height));
			mSrcAdapter = new SrcAdapter(getActivity(), mSourceList);
			mListView.setAdapter(mSrcAdapter);

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

	@OnItemClick(R.id.file_list)
	void clickOnDirList(int position) {
		String bucketString = (String) mSourceList.get(position).get(KEY_BUCKET);
		EventBus.getDefault().post(new AccessSubDirEvent(bucketString));
	}

	private void updateFileList(List<Map<String, Object>> fileList) {
		initView();
		((MobileBaseActivity) getActivity()).dismissWaitingDialog();
		if (isRefreshing()) {
			refreshComplete();
		}
		if (fileList == null || fileList.isEmpty()) {
			showEmptyHint();
		} else {
			clearEmptyHint();
			((SrcAdapter) mListView.getAdapter()).setFileList(fileList);
		}
	}

	private void updateCurrentFileList() {
		Task.call(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				((MobileBaseActivity) getActivity()).showWaitingDialog();
				mSourceList.clear();
				return null;
			}
		}).onSuccess(new Continuation<Void, Void>() {
			@Override
			public Void then(Task<Void> task) throws Exception {
				if (mCategoryType == FilterFileEvent.FILTER_TYPE_PHOTO) {
					ArrayList<String> imageList = LocalFileExplorer.getImageBucketDir(getActivity());
					for (int index = 0; index < imageList.size(); index++) {
						Map<String, Object> temp = new HashMap<String, Object>();
						temp.put(KEY_BUCKET, imageList.get(index));
						ArrayList<Map<String, Object>> imageThumbList = LocalFileExplorer.getBucketImages(getActivity(),
																										  imageList.get(index));
						temp.put(KEY_THUMB, imageThumbList);
						temp.put(KEY_COUNT, imageThumbList.size());
						mSourceList.add(temp);
					}
				} else if (mCategoryType == FilterFileEvent.FILTER_TYPE_VIDEO) {
					ArrayList<String> videoList = LocalFileExplorer.getVideoBucketDir(getActivity());
					for (int index = 0; index < videoList.size(); index++) {
						Map<String, Object> temp = new HashMap<String, Object>();
						temp.put(KEY_BUCKET, videoList.get(index));
						ArrayList<Map<String, Object>> imageThumbList = LocalFileExplorer.getBucketVideos(getActivity(),
																										  videoList.get(index));
						ArrayList<Bitmap> tempList = new ArrayList<Bitmap>();
						for (int loop = 0; loop < imageThumbList.size(); loop++) {
							Bitmap bitmap = (Bitmap) imageThumbList.get(loop).get(LocalFileExplorer.KEY_THUMB_BITMAP);
							tempList.add(bitmap);
						}
						temp.put(KEY_THUMB, tempList);
						temp.put(KEY_COUNT, tempList.size());
						mSourceList.add(temp);
					}
				}
				return null;
			}
		}, Task.BACKGROUND_EXECUTOR).continueWith(new Continuation<Void, Void>() {
			@Override
			public Void then(Task<Void> task) throws Exception {
				updateFileList(mSourceList);
				return null;
			}
		}, Task.UI_THREAD_EXECUTOR);


	}


	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		mIsFragmentHide = hidden;
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

	@OnClick(R.id.drive_browser_list_refresh)
	void emptyRefresh() {
		updateCurrentFileList();
	}

	private class SrcAdapter extends ImageCacheAdapter {

		private List<Map<String, Object>> sourceList;
		private Context                   mContext;

		public SrcAdapter(Context context, List<Map<String, Object>> sourceList) {
			super(getActivity().getResources().getInteger(R.integer.image_dir_thumb_width_height),
				  getActivity().getResources().getInteger(R.integer.image_dir_thumb_width_height));
			mSourceList = sourceList;
			mContext = context;
		}

		public void setFileList(List<Map<String, Object>> list) {
			sourceList = list;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (sourceList == null) {
				return 0;
			}
			return sourceList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return sourceList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		class ViewHolder {

			int[]       mIds  = {R.id.top_left_img_id,
								 R.id.top_right_img_id,
								 R.id.bottom_left_img_id,
								 R.id.bottom_right_img_id};
			ImageView[] mImgs = new ImageView[mIds.length];
			View      mAlbumInfo;
			TextView  mAlbumTitle;
			TextView  mAlbumPhotoNum;
			ImageView mVideoLogo;
		}

		@SuppressWarnings("unchecked")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_upload_image_or_video_gridview_item, parent, false);
				for (int loop = 0; loop < viewHolder.mIds.length; loop++) {
					viewHolder.mImgs[loop] = (ImageView) convertView.findViewById(viewHolder.mIds[loop]);
				}
				viewHolder.mAlbumInfo = convertView.findViewById(R.id.albume_info);
				viewHolder.mAlbumTitle = (TextView) convertView.findViewById(R.id.albume_title);
				viewHolder.mAlbumPhotoNum = (TextView) convertView.findViewById(R.id.albume_photo_number);
				viewHolder.mVideoLogo = (ImageView) convertView.findViewById(R.id.video_logo);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
				for (int i = 0; i < viewHolder.mImgs.length; i++) {
					viewHolder.mImgs[i].setImageResource(android.R.color.transparent);
				}
			}

			@SuppressWarnings("unchecked") int size = ((ArrayList<Bitmap>) sourceList.get(position).get(KEY_THUMB)).size();

			viewHolder.mAlbumTitle.setText((CharSequence) sourceList.get(position).get(KEY_BUCKET));
			viewHolder.mAlbumPhotoNum.setText("" + sourceList.get(position).get(KEY_COUNT));

			for (int i = 0; i < viewHolder.mIds.length; i++) {
				if (size > i) {
					if (mCategoryType == FilterFileEvent.FILTER_TYPE_PHOTO) {
						loadBitmap(mContext, (Uri) ((ArrayList<Map<String, Object>>) mSourceList.get(position).get(KEY_THUMB)).get(i)
																															  .get(
																																  LocalFileExplorer.KEY_PATH),
								   viewHolder.mImgs[i]);
						viewHolder.mVideoLogo.setVisibility(View.INVISIBLE);
					} else if (mCategoryType == FilterFileEvent.FILTER_TYPE_VIDEO) {
						viewHolder.mImgs[i].setImageBitmap(((ArrayList<Bitmap>) mSourceList.get(position).get(KEY_THUMB)).get(i));
						viewHolder.mVideoLogo.setVisibility(View.VISIBLE);
					}
				}
			}

			return convertView;
		}
	}

	//TODO: below should be common.
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
