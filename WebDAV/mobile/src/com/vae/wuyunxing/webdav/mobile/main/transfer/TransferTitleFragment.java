package com.vae.wuyunxing.webdav.mobile.main.transfer;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.vae.wuyunxing.webdav.library.log.MKLog;
import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.transfer.message.TransferListOperationEvent;

import de.greenrobot.event.EventBus;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class TransferTitleFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View mParentView = inflater.inflate(R.layout.fragment_transfer_title_view, container, false);
		ButterKnife.inject(this, mParentView);
		return mParentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.reset(this);
	}

	@OnClick(R.id.select_back_btn_id)
	void onClickBackBtn() {
		getActivity().onBackPressed();
	}

	@OnClick(R.id.select_opt_btn_id)
	void onClickOperatekBtn() {
		MKLog.d(TransferListActivity.class, "click 0");
		EventBus.getDefault().post(new TransferListOperationEvent());
	}
}
