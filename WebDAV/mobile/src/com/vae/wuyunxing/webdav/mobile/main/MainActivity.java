package com.vae.wuyunxing.webdav.mobile.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.MimeTypeMap;

import com.vae.wuyunxing.webdav.library.log.MKLog;
import com.vae.wuyunxing.webdav.library.play.util.Utils;
import com.vae.wuyunxing.webdav.library.play.webdav.WebDAVFilePlayService;
import com.vae.wuyunxing.webdav.library.play.webdav.WebDAVFileServer;
import com.vae.wuyunxing.webdav.mobile.MobileBaseActivity;
import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.message.BackParentEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.EnterEditModeEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.ExitEditModeEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.FilterFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.PlayFileEvent;

import de.greenrobot.event.EventBus;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import butterknife.ButterKnife;

public class MainActivity extends MobileBaseActivity{

    public static final String KEY_CATEGORY_TYPE = "category_type";

    public static final int SORT_TYPE_FILE_NAME = 0;
    public static final int SORT_TYPE_FILE_SIZE = 1;
    public static final int SORT_TYPE_DATE      = 2;
    public static final int SORT_TYPE_SUFFIX    = 3;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser_main);
        mContext = this;
        ButterKnife.inject(this);
        replaceFragment(R.id.activity_drive_browser_action_bar, NormalActionBarFragment.class);
        replaceFragment(R.id.activity_drive_browser_file_list, RemoteFileListFragment.class);
        /** WebDAV file play server */
        WebDAVFilePlayService.startup(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        /** WebDAV file play stop */
        WebDAVFilePlayService.stopdown(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                EventBus.getDefault().post(new BackParentEvent());
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /** EventBus event **/
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

    /**
     * play remote file
     */
    public void onEventMainThread(PlayFileEvent event) {
        String curPath = event.mUri;

        String httpRequest = "http://" + WebDAVFilePlayService.IP + ':' + WebDAVFilePlayService.PORT + WebDAVFileServer.WEBDAV_CONTENT_EXPORT_URI;
        String ext = Utils.getFileExtension(curPath);
        String routerIP = mContext.getResources().getString(R.string.webdav_domain);
        String WebDAVFileCurrentEncodePath = null;
        try {
            WebDAVFileCurrentEncodePath = URLEncoder.encode(curPath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String url = httpRequest + routerIP + WebDAVFileCurrentEncodePath;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(url);
        intent.setDataAndType(uri, MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext));
        startActivity(intent);

    }

}
