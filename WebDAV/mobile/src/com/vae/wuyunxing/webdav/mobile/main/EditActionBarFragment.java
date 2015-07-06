package com.vae.wuyunxing.webdav.mobile.main;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.message.EditCheckAllEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.EditSelectionEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.ExitEditModeEvent;

import de.greenrobot.event.EventBus;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class EditActionBarFragment extends Fragment{

	@InjectView(R.id.action_bar_drive_browser_check_all)
	Button mCheckAll;

	@InjectView(R.id.action_bar_drive_browser_select_count)
	TextView mSelectCount;

	private boolean mIsAll = false;


	public EditActionBarFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_action_bar_drive_browser_edit, container, false);
		ButterKnife.inject(this, view);

		EventBus.getDefault().register(this);

		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		EventBus.getDefault().unregister(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		updateSelectCount(1);
		updateCheckAll(true);
	}

	private void updateSelectCount(int count) {
		mSelectCount.setText(getString(R.string.select_count, count));
	}

	private void updateCheckAll(boolean all) {
		mCheckAll.setText(all ? R.string.check_all : R.string.check_none);
	}

	@OnClick(R.id.action_bar_drive_browser_cancel)
	void exitEditMode() {
		EventBus.getDefault().post(new ExitEditModeEvent());
	}

	@OnClick(R.id.action_bar_drive_browser_check_all)
	void check() {
		EventBus.getDefault().post(new EditCheckAllEvent(!mIsAll));
	}

	public void onEventMainThread(EditSelectionEvent event) {
		updateSelectCount(event.mSelectionCount);
		updateCheckAll(!(mIsAll = (event.mSelectionCount == event.mTotalCount)));
	}
}
