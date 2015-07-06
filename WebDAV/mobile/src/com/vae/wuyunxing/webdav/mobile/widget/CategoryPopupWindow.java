package com.vae.wuyunxing.webdav.mobile.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;

import com.vae.wuyunxing.webdav.mobile.R;

import java.util.List;
import java.util.Map;

public class CategoryPopupWindow extends PopupWindow {
    public CategoryPopupWindow(Context context) {
        super(context);
    }

    public CategoryPopupWindow(Context context, int width, int height) {
        super(context);
        setWidth(width);
        setHeight(height);
    }

    void apply(final Builder builder) {
        GridView view = (GridView) View.inflate(builder.mContext, R.layout.drive_browser_tool_popups_content, null);
        view.setNumColumns(builder.mColumn);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (builder.mListener != null) {
                    builder.mListener.onItemClick(parent, view, position, id);
                }
                dismiss();
            }
        });
        view.setAdapter(new DriveBrowserToolPopupsAdapter(builder.mContext, builder.mData, builder.mKeys));

        setContentView(view);
        setOutsideTouchable(builder.mOutsideTouchable);
        setFocusable(true);
        setAnimationStyle(builder.mAnimationStyle);
    }

    public static class Builder {

        Context mContext;
        boolean mOutsideTouchable = true;
        int     mWidth            = ViewGroup.LayoutParams.WRAP_CONTENT;
        int     mHeight           = ViewGroup.LayoutParams.WRAP_CONTENT;
        int     mColumn           = 4;
        int     mAnimationStyle   = R.style.CategoryPopupWindowAnimation;
        List<? extends Map<String, ?>> mData;
        String[]                        mKeys;
        AdapterView.OnItemClickListener mListener;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder setLayoutParam(int width, int height) {
            mWidth = width;
            mHeight = height;
            return this;
        }

        public Builder setOutsideTouchable(boolean outsideTouchable) {
            mOutsideTouchable = outsideTouchable;
            return this;
        }

        public Builder setColumn(int column) {
            mColumn = column;
            return this;
        }

        public Builder setAnimationStyle(int animationStyle) {
            mAnimationStyle = animationStyle;
            return this;
        }

        public Builder setDisplayData(List<? extends Map<String, ?>> data, String[] keys) {
            mData = data;
            mKeys = keys;
            return this;
        }

        public Builder setOnItemClickListener(AdapterView.OnItemClickListener l) {
            mListener = l;
            return this;
        }

        public CategoryPopupWindow create() {
            CategoryPopupWindow window = new CategoryPopupWindow(mContext, mWidth, mHeight);
            window.apply(this);
            return window;
        }

        public CategoryPopupWindow showAsDropDown(View anchor) {
            CategoryPopupWindow window = create();
            window.showAsDropDown(anchor);
            return window;
        }
    }
}
