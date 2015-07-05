package com.vae.wuyunxing.webdav.mobile.main;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.message.FilterFileEvent;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import in.srain.cube.views.ptr.PtrFrameLayout;

/**
 * Created by vae on 2015/7/5.
 */
public class RemoteFileListFragment extends Fragment {

    @InjectView(R.id.drive_browser_ptr_frame_list)
    PtrFrameLayout mPtrFrameList;

    @InjectView(R.id.drive_browser_file_list)
    ListView mListView;

    @InjectView(R.id.drive_browser_empty_hint)
    FrameLayout mEmptyHint;

    @InjectView(R.id.drive_browser_list_hint)
    TextView mListHint;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drive_browser_file_list, container, false);
        ButterKnife.inject(this, view);

        EventBus.getDefault().register(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        EventBus.getDefault().unregister(this);
    }

//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        initPtrFrameLayout(mPtrFrameList);
//        initListView(mListView);
//
//    }

    public void onEventMainThread(FilterFileEvent event) {
    }
}
