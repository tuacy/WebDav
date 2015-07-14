package com.vae.wuyunxing.webdav.mobile.main;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;

import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.message.CreateFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.FilterFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.SortFileEvent;
import com.vae.wuyunxing.webdav.mobile.main.transfer.TransferListActivity;
import com.vae.wuyunxing.webdav.mobile.widget.CategoryPopupWindow;
import com.vae.wuyunxing.webdav.mobile.widget.CreateNewFolderDialog;
import com.vae.wuyunxing.webdav.mobile.widget.SortPopupWindow;
import com.vae.wuyunxing.webdav.mobile.widget.UploadTypeDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class ToolBarFragment extends Fragment {

    private static final String KEY_ICON = "icon";
    private static final String KEY_NAME = "name";

    private CategoryPopupWindow   mCategoryPopupWindow;
    private UploadTypeDialog      mUploadTypeDialog;
    private SortPopupWindow       mSortPopupWindow;
    private CreateNewFolderDialog mCreateNewFolderDialog;

    public ToolBarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drive_browser_tool_bar, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /** category Popup windows */
    @OnClick(R.id.drive_browser_tool_category)
    void showCategoryPopup(Button categoryButton) {
        mCategoryPopupWindow = new CategoryPopupWindow.Builder(getActivity()).setLayoutParam(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
                .setOutsideTouchable(true)
                .setColumn(getResources().getInteger(
                        R.integer.category_popup_window_column_count))
                .setAnimationStyle(R.style.CategoryPopupWindowAnimation)
                .setDisplayData(
                        getPopupsData(R.array.category_popup_window_icons,
                                R.array.category_popup_window_names),
                        new String[]{KEY_NAME,
                                KEY_ICON})
                .setOnItemClickListener(mCategoryItemClickListener)
                .showAsDropDown(categoryButton);
    }

    private List<Map<String, Integer>> getPopupsData(int iconsId, int namesId) {
        List<Map<String, Integer>> data = new ArrayList<Map<String, Integer>>();
        TypedArray nameArray = getResources().obtainTypedArray(namesId);
        TypedArray iconArray = getResources().obtainTypedArray(iconsId);
        for (int i = 0; i < iconArray.length(); i++) {
            Map<String, Integer> map = new HashMap<String, Integer>();
            map.put(KEY_NAME, nameArray.getResourceId(i, 0));
            map.put(KEY_ICON, iconArray.getResourceId(i, 0));
            data.add(map);
        }
        iconArray.recycle();
        nameArray.recycle();
        return data;
    }

    private AdapterView.OnItemClickListener mCategoryItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            EventBus.getDefault().post(new FilterFileEvent(position, false));
        }
    };

    void dismissCategoryPopup() {
        if (mCategoryPopupWindow != null && mCategoryPopupWindow.isShowing()) {
            mCategoryPopupWindow.dismiss();
        }
    }

    /** upload category dialog */
    @OnClick(R.id.drive_browser_tool_upload)
    void showUploadDialog() {
        mUploadTypeDialog = new UploadTypeDialog.Builder(getActivity()).setColumn(
                getResources().getInteger(R.integer.upload_type_dialog_column_count))
                .setDisplayData(getPopupsData(R.array.category_popup_window_icons,
                                R.array.category_popup_window_names),
                        new String[]{KEY_NAME,
                                KEY_ICON})
                .setCanceledOnTouchOutside(true)
                .setOnItemClickListener(mUploadItemClickListener)
                .show();
    }

    void dismissUploadDialog() {
        if (mUploadTypeDialog != null && mUploadTypeDialog.isShowing()) {
            mUploadTypeDialog.dismiss();
        }
    }

    private AdapterView.OnItemClickListener mUploadItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            EventBus.getDefault().post(new FilterFileEvent(position, true));
        }
    };


    /** sort Popup Window */
    @OnClick(R.id.drive_browser_tool_sort)
    void showSortPopup(ImageButton sortButton) {
        mSortPopupWindow = new SortPopupWindow.Builder(getActivity()).setLayoutParam(sortButton.getWidth(),
                ViewGroup.LayoutParams.WRAP_CONTENT)
                .setOutsideTouchable(true)
                .setAnimationStyle(R.style.SortPopupWindowAnimation)
                .setDisplayData(getSortData(R.array.sort_file_types))
                .setOnItemClickListener(mSortItemClickListener)
                .show(sortButton);
    }

    private int[] getSortData(int namesId) {
        TypedArray nameArray = getResources().obtainTypedArray(namesId);
        int length = nameArray.length();
        int[] nameIds = new int[length];
        for (int i = 0; i < length; i++) {
            nameIds[i] = nameArray.getResourceId(i, 0);
        }
        nameArray.recycle();
        return nameIds;
    }

    void dismissSortPopup() {
        if (mSortPopupWindow != null && mSortPopupWindow.isShowing()) {
            mSortPopupWindow.dismiss();
        }
    }

    private AdapterView.OnItemClickListener mSortItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            EventBus.getDefault().post(new SortFileEvent(position));
        }
    };

    /** create new folder dialog */
    @OnClick(R.id.drive_browser_tool_new_folder)
    void showNewFolderDialog() {
        mCreateNewFolderDialog = new CreateNewFolderDialog(getActivity());
        mCreateNewFolderDialog.setOnCreateFolderListener(mCreateFolderListener);
        mCreateNewFolderDialog.show();
    }

    void dismissNewFolderDialog() {
        if (mCreateNewFolderDialog != null && mCreateNewFolderDialog.isShowing()) {
            mCreateNewFolderDialog.dismiss();
        }
    }

    private CreateNewFolderDialog.OnCreateFolderListener mCreateFolderListener = new CreateNewFolderDialog.OnCreateFolderListener() {
        @Override
        public void onCreate(String folder) {
            if (!folder.isEmpty()) {
                EventBus.getDefault().post(new CreateFileEvent(folder));
            }
        }
    };

    @OnClick(R.id.drive_browser_tool_transfer_list)
    void showTransferLis() {
        startActivity(new Intent(getActivity(), TransferListActivity.class));
    }

}
