package com.vae.wuyunxing.webdav.mobile.main;

import android.os.Bundle;

import com.vae.wuyunxing.webdav.mobile.MobileBaseActivity;
import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.message.EnterEditModeEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.ExitEditModeEvent;

import butterknife.ButterKnife;

public class MainActivity extends MobileBaseActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser_main);
        ButterKnife.inject(this);
        replaceFragment(R.id.activity_drive_browser_action_bar, NormalActionBarFragment.class);
        replaceFragment(R.id.activity_drive_browser_file_list, RemoteFileListFragment.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    public void onEventMainThread(EnterEditModeEvent event) {
        addFragment(R.id.activity_drive_browser_action_bar, EditActionBarFragment.class, R.animator.slide_in_from_top,
                    R.animator.slide_out_to_top);
        addFragment(R.id.activity_drive_browser_function_bar, EditFunctionBarFragment.class, R.animator.slide_in_from_bottom,
                    R.animator.slide_out_to_bottom);
    }

    public void onEventMainThread(ExitEditModeEvent event) {
        removeFragment(R.id.activity_drive_browser_action_bar, EditActionBarFragment.class);
        removeFragment(R.id.activity_drive_browser_function_bar, EditFunctionBarFragment.class);
    }
}
