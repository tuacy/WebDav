package com.vae.wuyunxing.webdav.mobile.main;

import android.os.Bundle;

import com.vae.wuyunxing.webdav.mobile.MobileBaseActivity;
import com.vae.wuyunxing.webdav.mobile.R;

import butterknife.ButterKnife;

/**
 * Created by vae on 2015/7/5.
 */
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
}
