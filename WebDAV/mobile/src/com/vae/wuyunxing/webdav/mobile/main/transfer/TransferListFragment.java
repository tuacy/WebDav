package com.vae.wuyunxing.webdav.mobile.main.transfer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.vae.wuyunxing.webdav.mobile.MobileApplication;
import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.message.CancellationEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.CompletionEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.FailureEvent;
import com.vae.wuyunxing.webdav.mobile.main.transfer.message.TransferEditSelectAll;
import com.vae.wuyunxing.webdav.mobile.main.transfer.message.TransferListDeleteEvent;
import com.vae.wuyunxing.webdav.mobile.main.transfer.message.TransferListEnterEditEvent;
import com.vae.wuyunxing.webdav.mobile.main.transfer.message.TransferListExitEditEvent;
import com.vae.wuyunxing.webdav.mobile.main.transfer.message.TransferListSelectItemEvent;
import com.vae.wuyunxing.webdav.mobile.main.transfer.message.TransferStartEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.UpdateProgressEvent;
import com.vae.wuyunxing.webdav.mobile.storage.DownloadInfoRepository;
import com.vae.wuyunxing.webdav.mobile.storage.UploadInfoRepository;

import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import greendao.DownloadInfo;
import greendao.UploadInfo;


public class TransferListFragment extends Fragment {

	@InjectView(R.id.trans_listview_id)
	ListView mListView;

	@InjectView(R.id.nothing_id)
	TextView mTipsTxt;

	public static final String ARG_SECTION_NUMBER = "section_number";

	public static final int SECTION_UPLOAD   = 0;
	public static final int SECTION_DOWNLOAD = 1;

	private int mCurSectionNum;
	private List<TransferEntity> mList = new ArrayList<TransferEntity>();
	private TransferListAdapter mTransferListAdapter;
	private final Set<Integer> mSelections = new HashSet<Integer>();

	private boolean mInEditMode = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mParentView = inflater.inflate(R.layout.fragment_transfer_list, container, false);
		mCurSectionNum = getArguments().getInt(ARG_SECTION_NUMBER);
		EventBus.getDefault().register(this);
		ButterKnife.inject(this, mParentView);
		return mParentView;
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
		mTransferListAdapter = new TransferListAdapter(getActivity());
		mListView.setAdapter(mTransferListAdapter);
		mTransferListAdapter.setOnOptClickListener(mOptClickListener);
		getListAndSetAdapter(mCurSectionNum == SECTION_UPLOAD);
	}

	private void clickOnListItem(int position) {
		TransferListAdapter adapter = (TransferListAdapter) mListView.getAdapter();
		TransferEntity TransferEntity = adapter.getItem(position);
		int hash = TransferEntity.hashCode();
		if (mSelections.contains(hash)) {
			mSelections.remove(hash);
		} else {
			mSelections.add(hash);
		}
		adapter.notifyDataSetChanged();
		EventBus.getDefault().post(new TransferListSelectItemEvent(mSelections.size(), mListView.getAdapter().getCount()));
	}

	@OnItemClick(R.id.trans_listview_id)
	void clickOnTransferList(int position) {
		if (mInEditMode) {
			clickOnListItem(position);
		}
	}

	private boolean isNeedDiscardEvent(boolean isUploadEvent) {
		if ((isUploadEvent && mCurSectionNum == SECTION_DOWNLOAD) || (!isUploadEvent && mCurSectionNum == SECTION_UPLOAD)) {
			return true;
		}
		return false;
	}

	private void getListAndSetAdapter(boolean isGetUploadList) {

		if (isNeedDiscardEvent(isGetUploadList)) {
			return;
		}

		List<TransferEntity> tempList = new ArrayList<TransferEntity>();
		List<TransferEntity> ingList = new ArrayList<TransferEntity>();
		List<TransferEntity> finishList = new ArrayList<TransferEntity>();
		mList.clear();
		if (isGetUploadList) {
			List<UploadInfo> uploadInfoList = UploadInfoRepository.getManualUpload(getActivity());
			for (UploadInfo tmp : uploadInfoList) {
				tempList.add(new TransferUploadEntity(tmp));
			}
		} else {
			List<DownloadInfo> downloadInfoList = DownloadInfoRepository.getManualUpload(getActivity());
			for (DownloadInfo tmp : downloadInfoList) {
				tempList.add(new TransferDownloadEntity(tmp));
			}
		}
		if (tempList != null) {
			for (TransferEntity TransferEntity : tempList) {
				if (TransferEntity.getState() == UploadInfoRepository.FINISH) {
					finishList.add(TransferEntity);
				} else {
					ingList.add(TransferEntity);
				}
			}

			for (TransferEntity temp : ingList) {
				mList.add(temp);
			}
			for (TransferEntity temp : finishList) {
				mList.add(temp);
			}
			mTransferListAdapter.setAdapterData(mList);
		}
		setTipsFlag();
	}

	private void updateIngView(boolean isUploadEvent, long mID) {

		if (isNeedDiscardEvent(isUploadEvent)) {
			return;
		}

		for (int i = 0; i < mTransferListAdapter.getIngItemCount(); i++) {
			if (mTransferListAdapter.getItem(i).getId() == mID) {
				TransferEntity newInfo = null;
				if (mCurSectionNum == SECTION_DOWNLOAD) {
					newInfo = new TransferDownloadEntity(DownloadInfoRepository.getWithId(getActivity(), mID));
				} else {
					newInfo = new TransferUploadEntity(UploadInfoRepository.getWithId(getActivity(), mID));
				}
				mList.set(i, newInfo);
				mTransferListAdapter.updateIngView(mListView, i, newInfo);
				break;
			}
		}
	}

	public void onEventMainThread(UpdateProgressEvent event) {
		if (event.mIsSyncEvent) {
			return;
		}
		if (event.mProgress >= 0 && event.mProgress < 100) {
			updateIngView(event.mIsUpload, event.mID);
		} else if (event.mProgress == 100) {
			getListAndSetAdapter(event.mIsUpload);
		}
	}

	public void onEventMainThread(FailureEvent event) {
		if (event.mIsSyncEvent) {
			return;
		}
		updateIngView(event.mIsUpload, event.mID);
	}

	public void onEventMainThread(CancellationEvent event) {
		if (event.mIsSyncEvent) {
			return;
		}
		updateIngView(event.mIsUpload, event.mID);
	}

	public void onEventMainThread(CompletionEvent event) {
		if (event.mIsSyncEvent) {
			return;
		}
		getListAndSetAdapter(event.mIsUpload);
	}

	public void onEventMainThread(TransferStartEvent event) {
		if (event.mIsSyncEvent) {
			return;
		}
		updateIngView(event.mIsUpload, event.mID);
	}

	public void onEventMainThread(TransferListEnterEditEvent event) {
		mSelections.clear();
		mInEditMode = true;
		((TransferListAdapter) mListView.getAdapter()).setEditMode(mSelections);
	}

	public void onEventMainThread(TransferEditSelectAll event) {

		if (isNeedDiscardEvent(event.isUploadEvent)) {
			return;
		}

		if (event.selectAll) {
			for (TransferEntity file : mList) {
				mSelections.add(file.hashCode());
			}
		} else {
			mSelections.clear();
		}
		((TransferListAdapter) mListView.getAdapter()).notifyDataSetChanged();
		EventBus.getDefault().post(new TransferListSelectItemEvent(mSelections.size(), mListView.getAdapter().getCount()));
	}

	public void onEventMainThread(TransferListExitEditEvent event) {
		mSelections.clear();
		mInEditMode = false;
		((TransferListAdapter) mListView.getAdapter()).clearEditMode();
	}

	public void onEventMainThread(TransferListDeleteEvent event) {

		if (!mInEditMode || isNeedDiscardEvent(event.isUploadEvent)) {
			return;
		}

		Iterator<TransferEntity> iterator = mList.iterator();
		while (iterator.hasNext()) {
			TransferEntity temp = iterator.next();
			int hash = temp.hashCode();
			if (mSelections.contains(hash)) {
				iterator.remove();
				mSelections.remove(hash);
				if (temp instanceof TransferUploadEntity) {
					MobileApplication.getInstance().getTransferManager().stop(temp.getId(), true);
					UploadInfoRepository.deleteWithId(getActivity(), temp.getId());
				} else {
					MobileApplication.getInstance().getTransferManager().stop(temp.getId(), false);
					DownloadInfoRepository.deleteWithId(getActivity(), temp.getId());
				}
			}
		}
		((TransferListAdapter) mListView.getAdapter()).notifyDataSetChanged();
	}

	private void setTipsFlag() {
		if (mList == null || mList.size() <= 0) {
			mTipsTxt.setVisibility(View.VISIBLE);
			if (mCurSectionNum == SECTION_UPLOAD) {
				mTipsTxt.setText(R.string.str_no_upload_notice);
			} else if (mCurSectionNum == SECTION_DOWNLOAD) {
				mTipsTxt.setText(R.string.str_no_download_notice);
			}
		} else {
			mTipsTxt.setVisibility(View.GONE);
		}
	}

	private TransferListAdapter.OnOptClickListener mOptClickListener = new TransferListAdapter.OnOptClickListener() {
		@Override
		public void clicked(int position, TransferEntity fileInfoStruct) {
			if (mInEditMode) {
				clickOnListItem(position);
			} else {
				if (mCurSectionNum == SECTION_DOWNLOAD) {
					DownloadInfo downloadInfo = ((TransferDownloadEntity) fileInfoStruct).getDownloadInfo();
					downloadInfo.setState(DownloadInfoRepository.READY);
					DownloadInfoRepository.insertOrUpdate(MobileApplication.getInstance(), downloadInfo);
				} else {
					UploadInfo uploadInfo = ((TransferUploadEntity) fileInfoStruct).getUploadInfo();
					uploadInfo.setState(UploadInfoRepository.READY);
					UploadInfoRepository.insertOrUpdate(MobileApplication.getInstance(), uploadInfo);
				}
			}
			updateIngView(mCurSectionNum == SECTION_UPLOAD, fileInfoStruct.getId());
		}
	};

	public boolean isEmptyList() {
		if (mList == null || mList.size() <= 0) {
			return true;
		}
		return false;
	}
}
