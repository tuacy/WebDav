package com.vae.wuyunxing.webdav.mobile.main;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vae.wuyunxing.webdav.mobile.MobileNoUseEvent;
import com.vae.wuyunxing.webdav.mobile.R;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by vae on 2015/7/5.
 */
public class NormalActionBarFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_action_bar_drive_browser_normal, container, false);
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

    /** no used */
    public void onEventMainThread(MobileNoUseEvent event) {

    }
}
