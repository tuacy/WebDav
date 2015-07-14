package com.vae.wuyunxing.webdav.mobile.main.transfer;


import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.transfer.message.TransferEditCancelEvent;
import com.vae.wuyunxing.webdav.mobile.main.transfer.message.TransferListEnterEditEvent;
import com.vae.wuyunxing.webdav.mobile.main.transfer.message.TransferListExitEditEvent;
import com.vae.wuyunxing.webdav.mobile.main.transfer.message.TransferListOperationEvent;

import de.greenrobot.event.EventBus;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class TransferListActivity extends FragmentActivity {

	@InjectView(R.id.pager)
	ViewPager mViewPager;

	@InjectView(R.id.cursor_text)
	TextView mCursor;

	private static final int    TAB_NUM            = 2;
	private static final int    SCROLL_STATE_UP    = 2;
	private static final int    SCROLL_STATE_PRESS = 1;
	public static final  String SELECT_KEY         = "sekect_page";
	private int mScrollState;
	private int     mSelectedPage    = TransferListFragment.SECTION_UPLOAD;
	private int     mPreSelectedPage = TransferListFragment.SECTION_UPLOAD;
	private boolean mIsInEditMode    = false;
	private float                          mCurrentPositionPix;
	private float                          mTabWidth;
	private TransferTitleFragment          mTransfeTitleFragment;
	private TransferListEditBottomFragment mLongPressBottomView;
	private SectionsPagerAdapter           mAdapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transfer_list);
		ButterKnife.inject(TransferListActivity.this);
		EventBus.getDefault().register(this);
		initView();
		initTopBottomView();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ButterKnife.reset(TransferListActivity.this);
		EventBus.getDefault().unregister(this);
	}

	private void initView() {
		mViewPager.setOnPageChangeListener(pageChangeListener);
		mViewPager.setOnTouchListener(viewPageTouchListener);
		setCursorWidth();
		mAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mAdapter);
	}

	private void setCursorWidth() {
		int cursorWidth = getWindowWidth() / TAB_NUM;
		mTabWidth = (float) getWindowWidth() / TAB_NUM;

		ViewGroup.LayoutParams params = mCursor.getLayoutParams();
		params.width = cursorWidth;

		mCursor.setLayoutParams(params);
	}

	public void onEventMainThread(TransferEditCancelEvent event) {
		cancelEditMode();
	}


	public void onEventMainThread(TransferListOperationEvent event) {
		if (((TransferListFragment) mAdapter.instantiateItem(mViewPager, mViewPager.getCurrentItem())).isEmptyList()) {
			Toast.makeText(TransferListActivity.this, R.string.str_nothing_can_opt, Toast.LENGTH_SHORT).show();
			return;
		}

		mIsInEditMode = true;
		Bundle bundle = new Bundle();
		bundle.putInt(SELECT_KEY, mSelectedPage);

		android.app.FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction mFragmentTransaction = fragmentManager.beginTransaction();
		TransferListEditTopFragment itemLongPressTopView = new TransferListEditTopFragment();
		itemLongPressTopView.setArguments(bundle);
		mFragmentTransaction.replace(R.id.title_id, itemLongPressTopView);
		mFragmentTransaction.commit();

		mFragmentTransaction = fragmentManager.beginTransaction();
		mLongPressBottomView = new TransferListEditBottomFragment();
		mLongPressBottomView.setArguments(bundle);
		mFragmentTransaction.add(R.id.bottom_id, mLongPressBottomView);
		mFragmentTransaction.commit();

		EventBus.getDefault().post(new TransferListEnterEditEvent());
	}

	private void uiExitEditModeDisplay() {
		android.app.FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction mFragmentTransaction = fragmentManager.beginTransaction();
		mTransfeTitleFragment = new TransferTitleFragment();
		mFragmentTransaction.replace(R.id.title_id, mTransfeTitleFragment);
		mFragmentTransaction.commit();

		if (mLongPressBottomView != null && mLongPressBottomView.isVisible()) {
			android.app.FragmentManager fragmentManager1 = getFragmentManager();
			FragmentTransaction mFragmentTransaction1 = fragmentManager1.beginTransaction();
			mFragmentTransaction1.remove(mLongPressBottomView);
			mFragmentTransaction1.commit();
		}
	}

	private void cancelEditMode() {
		uiExitEditModeDisplay();
		mIsInEditMode = false;
		EventBus.getDefault().post(new TransferListExitEditEvent());
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.ACTION_DOWN == event.getAction() && keyCode == KeyEvent.KEYCODE_BACK) {
			if (mIsInEditMode) {
				cancelEditMode();
				return true;
			}
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private int getWindowWidth() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}

	private void initTopBottomView() {
		android.app.FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction mFragmentTransaction = fragmentManager.beginTransaction();

		mTransfeTitleFragment = new TransferTitleFragment();
		mFragmentTransaction.add(R.id.title_id, mTransfeTitleFragment);
		mFragmentTransaction.commit();
	}

	@OnClick(R.id.tab0_text)
	void clickOnTab0() {
		if (mIsInEditMode) {
			return;
		}

		if (mSelectedPage == TransferListFragment.SECTION_DOWNLOAD) {
			mViewPager.setCurrentItem(TransferListFragment.SECTION_UPLOAD, true);
		}
	}

	@OnClick(R.id.tab1_text)
	void clickOnTab1() {
		if (mIsInEditMode) {
			return;
		}

		if (mSelectedPage == TransferListFragment.SECTION_UPLOAD) {
			mViewPager.setCurrentItem(TransferListFragment.SECTION_DOWNLOAD, true);
		}
	}

	private View.OnTouchListener viewPageTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (mIsInEditMode) {
				return true;
			}
			return false;
		}
	};

	private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
		@Override
		public void onPageSelected(int position) {
			mSelectedPage = position;
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			if (positionOffsetPixels != 0) {
				if (mScrollState == SCROLL_STATE_PRESS) {
					if (mSelectedPage == position) {
						mCursor.setTranslationX(mCurrentPositionPix + positionOffsetPixels / TAB_NUM);
					} else {
						mCursor.setTranslationX(mCurrentPositionPix - (mTabWidth - positionOffsetPixels / TAB_NUM));
					}
				} else if (mScrollState == SCROLL_STATE_UP) {
					if (mPreSelectedPage == position) {
						mCursor.setTranslationX(mCurrentPositionPix + positionOffsetPixels / TAB_NUM);
					} else {
						mCursor.setTranslationX(mCurrentPositionPix - (mTabWidth - positionOffsetPixels / TAB_NUM));
					}
				}
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			mCurrentPositionPix = mSelectedPage * mTabWidth;
			mScrollState = state;
			mPreSelectedPage = mSelectedPage;
		}
	};

	private class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public TransferListFragment getItem(int position) {
			TransferListFragment fragment = new TransferListFragment();
			Bundle args = new Bundle();
			switch (position) {
				case TransferListFragment.SECTION_UPLOAD:
					args.putInt(TransferListFragment.ARG_SECTION_NUMBER, position);
					break;
				case TransferListFragment.SECTION_DOWNLOAD:
					args.putInt(TransferListFragment.ARG_SECTION_NUMBER, position);
					break;
				default:
					break;
			}
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public TransferListFragment instantiateItem(View container, int position) {
			return (TransferListFragment) super.instantiateItem(container, position);
		}

		@Override
		public int getCount() {
			return TAB_NUM;
		}

	}
}
