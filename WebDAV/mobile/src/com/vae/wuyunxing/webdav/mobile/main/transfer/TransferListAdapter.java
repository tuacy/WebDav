package com.vae.wuyunxing.webdav.mobile.main.transfer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.vae.wuyunxing.webdav.library.util.FileSizeConvertUtils;
import com.vae.wuyunxing.webdav.library.util.FileUtil;
import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.storage.UploadInfoRepository;
import com.vae.wuyunxing.webdav.mobile.widget.CircleProgressBar;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;


public class TransferListAdapter extends BaseAdapter {

	private Context              mContext;
	private List<TransferEntity> mTransferEntityList;
	private Set<Integer>         mSelections;
	private Boolean mInEditMode = false;

	private int mIngItemCount = 0;
	private int mOkItemCount  = 0;

	private OnOptClickListener mOnOptClickListener;

	public TransferListAdapter(Context context) {
		mContext = context;
	}

	public int getIngItemCount() {
		return mIngItemCount;
	}

	public void setEditMode(Set<Integer> selections) {
		mInEditMode = true;
		mSelections = selections;
		notifyDataSetChanged();
	}

	public void clearEditMode() {
		mInEditMode = false;
		mSelections = null;
		notifyDataSetChanged();
	}

	public void updateIngView(ListView listView, int index, TransferEntity infoStruct) {
		int firstVisiablePos = listView.getFirstVisiblePosition();
		int offset = index - firstVisiablePos;
		if (offset < 0) {
			return;
		}

		mTransferEntityList.set(index, infoStruct);

		View view = listView.getChildAt(offset);
		ViewHolder holder = (ViewHolder) view.getTag();
		refreshStateUi(holder, infoStruct);
	}

	private void refreshStateItemCount() {
		mIngItemCount = 0;
		mOkItemCount = 0;
		for (int index = 0; mTransferEntityList != null && index < mTransferEntityList.size(); index++) {
			if (mTransferEntityList.get(index).getState() == UploadInfoRepository.FINISH) {
				mOkItemCount++;
			}
		}

		if (mTransferEntityList != null) {
			mIngItemCount = mTransferEntityList.size() - mOkItemCount;
		}
	}

	public void setAdapterData(List<TransferEntity> list) {
		mTransferEntityList = list;
		refreshStateItemCount();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (mTransferEntityList == null) {
			return 0;
		}
		return mTransferEntityList.size();
	}

	@Override
	public TransferEntity getItem(int position) {
		return mTransferEntityList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_transfer_list_item, parent, false);

			holder.mFileImage = (ImageView) convertView.findViewById(R.id.file_info_img);
			holder.mTitleTxt = (TextView) convertView.findViewById(R.id.display_title_id);
			holder.mFileNameTxt = (TextView) convertView.findViewById(R.id.transfer_list_file_name);
			holder.mDateTxt = (TextView) convertView.findViewById(R.id.date_id);
			holder.mStateTxt = (TextView) convertView.findViewById(R.id.state_id);
			holder.mOptImgView = (ImageView) convertView.findViewById(R.id.select_id);
			holder.mStatePercent = (TextView) convertView.findViewById(R.id.text_view_progress_percent);
			holder.mProgressBar = (CircleProgressBar) convertView.findViewById(R.id.upload_progress_bar);
			holder.mOptImgView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mOnOptClickListener != null) {
						mOnOptClickListener.clicked((Integer) v.getTag(), getItem((Integer) v.getTag()));
					}
				}
			});

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (mIngItemCount > 0 && position == 0) {
			holder.mTitleTxt.setVisibility(View.VISIBLE);
			holder.mTitleTxt.setText(String.format(mContext.getResources().getString(R.string.str_transmitting), mIngItemCount));
		} else if (mOkItemCount > 0 && position == mIngItemCount) {
			holder.mTitleTxt.setVisibility(View.VISIBLE);
			holder.mTitleTxt.setText(String.format(mContext.getResources().getString(R.string.str_trans_ok), mOkItemCount));
		} else {
			holder.mTitleTxt.setVisibility(View.GONE);
		}

		holder.mOptImgView.setTag(position);

		refreshStateUi(holder, mTransferEntityList.get(position));

		return convertView;
	}

	private void refreshStateUi(ViewHolder holder, TransferEntity infoStruct) {
		if (infoStruct.getState() == UploadInfoRepository.STOP) {
			holder.mStateTxt.setText(R.string.str_waiting);
		} else if (infoStruct.getState() == UploadInfoRepository.FAIL) {
			holder.mStateTxt.setText(R.string.str_trans_error);
		} else {
			holder.mStateTxt.setText(
				FileSizeConvertUtils.formatFileSizeUnit(infoStruct.getTotalSize() * infoStruct.getPercent() / 100) + "/" +
				FileSizeConvertUtils.formatFileSizeUnit(infoStruct.getTotalSize()));
		}

		if (mInEditMode) {
			holder.mStatePercent.setVisibility(View.GONE);
			holder.mProgressBar.setVisibility(View.GONE);
			holder.mOptImgView.setVisibility(View.VISIBLE);
			if (mSelections.contains(infoStruct.hashCode())) {
				holder.mOptImgView.setImageResource(R.drawable.icon_item_selected);
			} else {
				holder.mOptImgView.setImageResource(R.drawable.icon_item_unselected);
			}
		} else {
			if (infoStruct.getState() == UploadInfoRepository.STOP || infoStruct.getState() == UploadInfoRepository.FAIL) {
				holder.mStatePercent.setVisibility(View.GONE);
				holder.mProgressBar.setVisibility(View.GONE);
				holder.mOptImgView.setVisibility(View.VISIBLE);
				holder.mOptImgView.setImageResource(R.drawable.icon_trans_pause);
			} else if ((infoStruct.getState() == UploadInfoRepository.UPLOADING || infoStruct.getState() == UploadInfoRepository.READY)) {
				holder.mProgressBar.setVisibility(View.VISIBLE);
				holder.mStatePercent.setVisibility(View.VISIBLE);
				holder.mOptImgView.setVisibility(View.GONE);
				holder.mProgressBar.setProgress(infoStruct.getPercent());
				holder.mStatePercent.setText(infoStruct.getPercent() + "%");
			} else if (infoStruct.getState() == UploadInfoRepository.FINISH) {
				holder.mOptImgView.setVisibility(View.GONE);
				holder.mStatePercent.setVisibility(View.GONE);
				holder.mProgressBar.setVisibility(View.GONE);
			}
		}

		String fileName = infoStruct.getFilename();
		switch (FileUtil.filterFileCategory(fileName)) {
			case VIDEO:
				holder.mFileImage.setImageResource(R.drawable.drive_browser_video);
				break;
			case AUDIO:
				holder.mFileImage.setImageResource(R.drawable.drive_browser_music);
				break;
			case IMAGE:
				holder.mFileImage.setImageResource(R.drawable.drive_browser_picture);
				break;
			case APPLICATION:
				holder.mFileImage.setImageResource(R.drawable.drive_browser_apk);
				break;
			case BIT_TORRENT:
				holder.mFileImage.setImageResource(R.drawable.drive_browser_bt);
				break;
			default:
				holder.mFileImage.setImageResource(R.drawable.drive_browser_doc);
				break;
		}

		holder.mFileNameTxt.setText(fileName);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = format.format(infoStruct.getUploadTime());
		holder.mDateTxt.setText(dateString);
	}

	private class ViewHolder {

		ImageView         mFileImage;
		TextView          mTitleTxt;
		TextView          mFileNameTxt;
		TextView          mDateTxt;
		TextView          mStateTxt;
		ImageView         mOptImgView;
		TextView          mStatePercent;
		CircleProgressBar mProgressBar;
	}

	public void setOnOptClickListener(OnOptClickListener l) {
		mOnOptClickListener = l;
	}

	public interface OnOptClickListener {

		public void clicked(int position, TransferEntity TransferEntity);
	}
}
