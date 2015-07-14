package com.vae.wuyunxing.webdav.mobile.main.transfer;


import android.app.Fragment;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;


import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.transfer.message.TransferListDeleteEvent;

import de.greenrobot.event.EventBus;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TransferListEditBottomFragment extends Fragment {

	@InjectView(R.id.bottom_gridview_id)
	GridView mGridView;

	private int mSelectedPage = TransferListFragment.SECTION_UPLOAD;

	public TransferListEditBottomFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View mParentView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_item_long_press_bottom_view, container, false);
		ButterKnife.inject(this, mParentView);
		return mParentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		initData();
	}

	private void initData() {
		TypedArray categoryIcons = getResources().obtainTypedArray(R.array.file_broswer_trans_edit_icon);
		String[] titles = getResources().getStringArray(R.array.file_broswer_trans_edit_txt);
		mGridView.setNumColumns(titles.length);
		mGridView.setOnItemClickListener(gridViewItemClickListener);
		TransferListEditItemAdapter adapter = new TransferListEditItemAdapter(getActivity(), categoryIcons, titles);
		mGridView.setAdapter(adapter);
		Bundle bundle = getArguments();
		mSelectedPage = bundle.getInt(TransferListActivity.SELECT_KEY, TransferListFragment.SECTION_UPLOAD);
	}

	private AdapterView.OnItemClickListener gridViewItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			EventBus.getDefault().post(new TransferListDeleteEvent(mSelectedPage == TransferListFragment.SECTION_UPLOAD));
		}
	};

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.reset(this);
	}
}
