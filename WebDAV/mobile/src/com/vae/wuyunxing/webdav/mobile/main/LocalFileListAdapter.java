package com.vae.wuyunxing.webdav.mobile.main;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.vae.wuyunxing.webdav.library.FileInfo;
import com.vae.wuyunxing.webdav.mobile.R;

import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LocalFileListAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<FileInfo> mFileList;
	private Set<Integer>   mSelections;

	public LocalFileListAdapter(LayoutInflater inflater) {
		mInflater = inflater;
	}

	public List<FileInfo> getFileList() {
		return mFileList;
	}

	public void setFileList(List<FileInfo> list) {
		mFileList = list;
		notifyDataSetChanged();
	}

	public void setSelections(Set<Integer> selections) {
		mSelections = selections;
		notifyDataSetChanged();
	}

	public void clearSelections() {
		mSelections.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (mFileList == null) {
			return 0;
		}
		return mFileList.size();
	}

	@Override
	public Object getItem(int position) {
		if (mFileList == null) {
			return null;
		}
		return mFileList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.fragment_drive_browser_file_list_item, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		FileInfo file = (FileInfo) getItem(position);
		if (file != null) {
			String filename = file.getName();
			int iconId;
			if (file.isDir()) {
				iconId = R.drawable.drive_browser_folder;
				filename = filename.substring(0, filename.length());
			} else {
				switch (file.category()) {
					case VIDEO:
						iconId = R.drawable.drive_browser_video;
						break;
					case AUDIO:
						iconId = R.drawable.drive_browser_music;
						break;
					case IMAGE:
						iconId = R.drawable.drive_browser_picture;
						break;
					case BIT_TORRENT:
						iconId = R.drawable.drive_browser_bt;
						break;
					case APPLICATION:
						iconId = R.drawable.drive_browser_apk;
						break;
					default:
						iconId = R.drawable.drive_browser_doc;
						break;
				}
			}
			holder.mIcon.setImageResource(iconId);
			holder.mName.setText(filename);
			holder.mDate.setText(DateFormat.format("MM/dd/yyyy hh:mm:ss", file.lastModified()));

			if (mSelections != null && mSelections.contains(file.hashCode())) {
				holder.mSelection.setVisibility(View.VISIBLE);
				holder.mSelection.setImageResource(R.drawable.drive_browser_selection);
			} else {
				holder.mSelection.setVisibility(View.GONE);
			}

		}

		return convertView;
	}

	static class ViewHolder {

		@InjectView(R.id.drive_browser_file_list_item_icon)
		public ImageView mIcon;

		@InjectView(R.id.drive_browser_file_list_item_name)
		public TextView mName;

		@InjectView(R.id.drive_browser_file_list_item_date)
		public TextView mDate;

		@InjectView(R.id.drive_browser_file_list_item_selection)
		public ImageView mSelection;

		private ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}
}
