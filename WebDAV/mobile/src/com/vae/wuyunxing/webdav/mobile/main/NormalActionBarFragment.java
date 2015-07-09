package com.vae.wuyunxing.webdav.mobile.main;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vae.wuyunxing.webdav.mobile.MobileNoUseEvent;
import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.message.DirChangedEvent;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

public class NormalActionBarFragment extends Fragment {

    @InjectView(R.id.action_bar_drive_browser_home_as_up)
    ImageButton mHomeAsUp;

    @InjectView(R.id.action_bar_drive_browser_text)
    TextView mText;

    @InjectView(R.id.action_bar_drive_browser_user)
    ImageButton mUser;

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

    private void setActionBar(boolean isInRoot, String dir) {
        if (isInRoot) {
            mHomeAsUp.setVisibility(View.GONE);
            mText.setText("vae");
            mUser.setVisibility(View.VISIBLE);
        } else {
            mHomeAsUp.setVisibility(View.VISIBLE);
            mText.setText(dir);
            mUser.setVisibility(View.GONE);
        }
    }

    public void onEventMainThread(DirChangedEvent event) {
        setActionBar(event.mIsRootDir, event.mDir);
    }
}
