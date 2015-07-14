package com.vae.wuyunxing.webdav.mobile.main.transfer;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.transfer.message.TransferEditCancelEvent;
import com.vae.wuyunxing.webdav.mobile.main.transfer.message.TransferEditSelectAll;
import com.vae.wuyunxing.webdav.mobile.main.transfer.message.TransferListSelectItemEvent;

import de.greenrobot.event.EventBus;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class TransferListEditTopFragment extends Fragment {

	@InjectView(R.id.top_center_info_id)
	TextView mInfoTxt;
	@InjectView(R.id.top_select_all_not_id)
	Button   mSelectAllBtn;

	private boolean mIsAllSelected = false;
	private int     mSelectedPage  = TransferListFragment.SECTION_UPLOAD;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View mParentView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_item_long_press_top_view, container, false);
		ButterKnife.inject(this, mParentView);
		EventBus.getDefault().register(this);
		return mParentView;
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
		mInfoTxt.setText(R.string.str_delete_select);
		initView();
		Bundle bundle = getArguments();
		mSelectedPage = bundle.getInt(TransferListActivity.SELECT_KEY, TransferListFragment.SECTION_UPLOAD);
	}

	private void initView() {
		mSelectAllBtn.setText(R.string.str_select_all);
		mInfoTxt.setText(R.string.str_delete_select);
	}


	@OnClick(R.id.top_cancel_id)
	void clickOnCancel() {
		EventBus.getDefault().post(new TransferEditCancelEvent());
	}

	public void onEventMainThread(TransferListSelectItemEvent event) {
		if (event.selectionCount == event.totalCount && event.totalCount != 0) {
			mIsAllSelected = true;
		} else {
			mIsAllSelected = false;
		}
		refreshInfoText(event.selectionCount);
		refreshSelectAllBtnText();
	}

	private void refreshInfoText(int count) {
		if (count > 0) {
			mInfoTxt.setText(getActivity().getResources().getString(R.string.select_count, count));
		} else {
			mInfoTxt.setText(R.string.str_delete_select);
		}
	}

	private void refreshSelectAllBtnText() {
		if (mIsAllSelected) {
			mSelectAllBtn.setText(R.string.check_none);
		} else {
			mSelectAllBtn.setText(R.string.str_select_all);
		}
	}

	@OnClick(R.id.top_select_all_not_id)
	void clickOnSelectAllBtn() {

		mIsAllSelected = !mIsAllSelected;
		refreshSelectAllBtnText();
		EventBus.getDefault().post(new TransferEditSelectAll(mIsAllSelected, mSelectedPage == TransferListFragment.SECTION_UPLOAD));
	}
}
