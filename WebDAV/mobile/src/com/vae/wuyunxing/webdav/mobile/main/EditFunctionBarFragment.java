package com.vae.wuyunxing.webdav.mobile.main;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.message.DeleteRemoteFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.DownloadRemoteFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.MoveRemoteFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.RenameRemoteFileEvent;

import de.greenrobot.event.EventBus;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditFunctionBarFragment extends Fragment {

	public EditFunctionBarFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_drive_brwoser_function_bar, container, false);
		ButterKnife.inject(this, view);
		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.reset(this);
	}

	@OnClick(R.id.drive_browser_function_bar_download)
	void download() {
		EventBus.getDefault().post(new DownloadRemoteFileEvent());
	}

	@OnClick(R.id.drive_browser_function_bar_delete)
	void delete() {
		EventBus.getDefault().post(new DeleteRemoteFileEvent());
	}

	@OnClick(R.id.drive_browser_function_bar_rename)
	void rename() {
		EventBus.getDefault().post(new RenameRemoteFileEvent());
	}

	@OnClick(R.id.drive_browser_function_bar_move)
	void move() {
		EventBus.getDefault().post(new MoveRemoteFileEvent());
	}
}
