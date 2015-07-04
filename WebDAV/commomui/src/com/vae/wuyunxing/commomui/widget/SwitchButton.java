/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vae.wuyunxing.commomui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.CompoundButton;

import com.vae.wuyunxing.commomui.R;


/**
 * A Switch is a two-state toggle switch widget that can select between two
 * options. The user may drag the "thumb" back and forth to choose the selected
 * option, or simply tap to toggle as if it were a checkbox. The
 * {@link #setText(CharSequence) text} property controls the text displayed in
 * the label for the switch, whereas the {@link #setTextOff(CharSequence) off}
 * and {@link #setTextOn(CharSequence) on} text controls the text on the thumb.
 * Similarly, the {@link #setTextAppearance(Context, int)
 * textAppearance} and the related setTypeface() methods control the typeface
 * and style of label text, whereas the
 * {@link #setSwitchTextAppearance(Context, int)
 * switchTextAppearance} and the related seSwitchTypeface() methods control that
 * of the thumb.
 */
public class SwitchButton extends CompoundButton {
    private static final int TOUCH_MODE_IDLE = 0;
    private static final int TOUCH_MODE_DOWN = 1;
    private static final int TOUCH_MODE_DRAGGING = 2;

    // Enum for the "typeface" XML parameter.
    private static final int SANS = 1;
    private static final int SERIF = 2;
    private static final int MONOSPACE = 3;

    private AttributeSet mAttrs;
    private Context mContext;
    
    private Drawable mThumbDrawable;
    private Drawable mTrackDrawable;
    private int mThumbTextPadding;
    private int mSwitchMinWidth;
    private int mSwitchPadding;
    private CharSequence mTextOn;
    private CharSequence mTextOff;

    private int mTouchMode;
    private int mTouchSlop;
    private float mTouchX;
    private float mTouchY;
//    private VelocityTracker mVelocityTracker = VelocityTracker.obtain();
    private int mMinFlingVelocity;

    private float mThumbPosition;
    private int mSwitchWidth;
    private int mSwitchHeight;
    private int mThumbWidth; // Does not include padding

    private int mSwitchLeft;
    private int mSwitchTop;
    private int mSwitchRight;
    private int mSwitchBottom;

    private TextPaint mTextPaint;
    private ColorStateList mTextColors;
    private Layout mOnLayout;
    private Layout mOffLayout;

    private final Rect mTempRect = new Rect();

    private static final int[] CHECKED_STATE_SET = {
        android.R.attr.state_checked
    };

    /**
     * Construct a new Switch with default styling.
     * 
     * @param context The Context that will determine this widget's theming.
     */
    public SwitchButton(Context context) {
        this(context, null);
    }
    
    /**
     * Construct a new Switch with default styling, overriding specific style
     * attributes as requested.
     * 
     * @param context The Context that will determine this widget's theming.
     * @param attrs Specification of attributes that should deviate from default
     *            styling.
     */
    public SwitchButton(Context context, AttributeSet attrs) {
    	this(context, attrs, R.attr.SwitchButtonStyle);
    }

    /**
     * Construct a new Switch with default styling, overriding specific style
     * attributes as requested.
     * 
     * @param context The Context that will determine this widget's theming.
     * @param attrs Specification of attributes that should deviate from default
     *            styling.
     * @param defStyle 
     */
    public SwitchButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mAttrs = attrs;
        mContext = context;
        
        mTextOn = "";
        mTextOff = "";
        
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        Resources res = context.getResources();
        mTextPaint.density = res.getDisplayMetrics().density;
//         mTextPaint.setCompatibilityScaling(res.getCompatibilityInfo().applicationScale);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwitchButton, defStyle,
				R.style.SwitchButton);

        mThumbDrawable = a.getDrawable(R.styleable.SwitchButton_thumb);
        mTrackDrawable = a.getDrawable(R.styleable.SwitchButton_track);
        CharSequence textOn = a.getText(R.styleable.SwitchButton_textOn);
        if (textOn != null) {
        	mTextOn = textOn;
        }
        CharSequence textOff = a.getText(R.styleable.SwitchButton_textOff);
        if (textOff != null) {
        	mTextOff = textOff;
        }
        mThumbTextPadding = a.getDimensionPixelSize(R.styleable.SwitchButton_thumbTextPadding, 0);
        mSwitchMinWidth = a.getDimensionPixelSize(R.styleable.SwitchButton_switchMinWidth, 0);
        mSwitchPadding = a.getDimensionPixelSize(R.styleable.SwitchButton_switchPadding, 0);

        int appearance = a.getResourceId(R.styleable.SwitchButton_switchTextAppearance, 0);
        if (appearance != 0) {
            setSwitchTextAppearance(context, appearance);
        }
        a.recycle();

        ViewConfiguration config = ViewConfiguration.get(context);
        mTouchSlop = config.getScaledTouchSlop();
        mMinFlingVelocity = config.getScaledMinimumFlingVelocity();

        // Refresh display with current params
        refreshDrawableState();
        setChecked(isChecked());
        setClickable(true);
    }

    /**
     * Sets the switch text color, size, style, hint color, and highlight color
     * from the specified TextAppearance resource.
     */
    public void setSwitchTextAppearance(Context context, int resid) {
        TypedArray appearance = context.obtainStyledAttributes(resid, R.styleable.SwitchBtnTextAppearance);

        ColorStateList colors;
        int ts;

        colors = appearance.getColorStateList(R.styleable.SwitchBtnTextAppearance_android_textColor);
        if (colors != null) {
            mTextColors = colors;
        } else {
            // If no color set in TextAppearance, default to the view's
            // textColor
            mTextColors = getTextColors();
        }

        ts = appearance.getDimensionPixelSize(R.styleable.SwitchBtnTextAppearance_android_textSize, 0);
        if (ts != 0) {
            if (ts != mTextPaint.getTextSize()) {
                mTextPaint.setTextSize(ts);
                requestLayout();
            }
        }

        int typefaceIndex, styleIndex;

        typefaceIndex = appearance.getInt(R.styleable.SwitchBtnTextAppearance_android_typeface, -1);
        styleIndex = appearance.getInt(R.styleable.SwitchBtnTextAppearance_android_textStyle, -1);

        setSwitchTypefaceByIndex(typefaceIndex, styleIndex);

        appearance.recycle();
    }

    private void setSwitchTypefaceByIndex(int typefaceIndex, int styleIndex) {
        Typeface tf = null;
        switch (typefaceIndex) {
            case SANS:
                tf = Typeface.SANS_SERIF;
                break;

            case SERIF:
                tf = Typeface.SERIF;
                break;

            case MONOSPACE:
                tf = Typeface.MONOSPACE;
                break;
        }

        setSwitchTypeface(tf, styleIndex);
    }

    /**
     * Sets the typeface and style in which the text should be displayed on the
     * switch, and turns on the fake bold and italic bits in the Paint if the
     * Typeface that you provided does not have all the bits in the style that
     * you specified.
     */
    public void setSwitchTypeface(Typeface tf, int style) {
        if (style > 0) {
            if (tf == null) {
                tf = Typeface.defaultFromStyle(style);
            } else {
                tf = Typeface.create(tf, style);
            }

            setSwitchTypeface(tf);
            // now compute what (if any) algorithmic styling is needed
            int typefaceStyle = tf != null ? tf.getStyle() : 0;
            int need = style & ~typefaceStyle;
            mTextPaint.setFakeBoldText((need & Typeface.BOLD) != 0);
            mTextPaint.setTextSkewX((need & Typeface.ITALIC) != 0 ? -0.25f : 0);
        } else {
            mTextPaint.setFakeBoldText(false);
            mTextPaint.setTextSkewX(0);
            setSwitchTypeface(tf);
        }
    }

    /**
     * Sets the typeface in which the text should be displayed on the switch.
     * Note that not all Typeface families actually have bold and italic
     * variants, so you may need to use
     * {@link #setSwitchTypeface(Typeface, int)} to get the appearance that you
     * actually want.
     * 
     * @attr ref android.R.styleable#TextView_typeface
     * @attr ref android.R.styleable#TextView_textStyle
     */
    public void setSwitchTypeface(Typeface tf) {
        if (mTextPaint.getTypeface() != tf) {
            mTextPaint.setTypeface(tf);

            requestLayout();
            invalidate();
        }
    }

    /**
     * Returns the text displayed when the button is in the checked state.
     */
    public CharSequence getTextOn() {
        return mTextOn;
    }

    /**
     * Sets the text displayed when the button is in the checked state.
     */
    public void setTextOn(CharSequence textOn) {
    	if (textOn != null) {
    		mTextOn = textOn;
    	}
        requestLayout();
    }

    /**
     * Returns the text displayed when the button is not in the checked state.
     */
    public CharSequence getTextOff() {
        return mTextOff;
    }

    /**
     * Sets the text displayed when the button is not in the checked state.
     */
    public void setTextOff(CharSequence textOff) {
    	if (textOff != null) {
    		mTextOff = textOff;
    	}
        requestLayout();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (mOnLayout == null) {
        	mOnLayout = makeLayout(mTextOn);
        }
        if (mOffLayout == null) {
        	mOffLayout = makeLayout(mTextOff);
        }

        mTrackDrawable.getPadding(mTempRect);
        final int maxTextWidth = Math.max(mOnLayout.getWidth(), mOffLayout.getWidth());
		final int switchWidth = Math.max(mSwitchMinWidth, /*maxTextWidth * 2 + mThumbTextPadding * 4 +*/
					 mTrackDrawable.getIntrinsicWidth() + mTempRect.left + mTempRect.right);
		final int switchHeight = mTrackDrawable.getIntrinsicHeight();

        mThumbWidth = mThumbDrawable.getIntrinsicWidth();/*maxTextWidth + mThumbTextPadding * 2*/;

        switch (widthMode) {
            case MeasureSpec.AT_MOST:
                widthSize = Math.min(widthSize, switchWidth);
                break;

            case MeasureSpec.UNSPECIFIED:
                widthSize = switchWidth;
                break;

            case MeasureSpec.EXACTLY:
                // Just use what we were given
                break;
        }

        switch (heightMode) {
            case MeasureSpec.AT_MOST:
                heightSize = Math.min(heightSize, switchHeight);
                break;

            case MeasureSpec.UNSPECIFIED:
                heightSize = switchHeight;
                break;

            case MeasureSpec.EXACTLY:
                // Just use what we were given
                break;
        }

        mSwitchWidth = switchWidth;
        mSwitchHeight = switchHeight;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int measuredHeight = getMeasuredHeight();
        if (measuredHeight < switchHeight) {
            setMeasuredDimension(getMeasuredWidth(), switchHeight);
        }
    }

    private Layout makeLayout(CharSequence text) {
        return new StaticLayout(text, mTextPaint, (int) Math.ceil(Layout.getDesiredWidth(text,
                mTextPaint)), Layout.Alignment.ALIGN_NORMAL, 1.f, 0, true);
    }

    /**
     * @return true if (x, y) is within the target area of the switch thumb
     */
    private boolean hitThumb(float x, float y) {
        mThumbDrawable.getPadding(mTempRect);
        final int thumbTop = mSwitchTop - mTouchSlop;
        final int thumbLeft = mSwitchLeft + (int) (mThumbPosition + 0.5f) - mTouchSlop;
        final int thumbRight = thumbLeft + mThumbWidth + mTempRect.left + mTempRect.right
                + mTouchSlop;
        final int thumbBottom = mSwitchBottom + mTouchSlop;
        return x > thumbLeft && x < thumbRight && y > thumbTop && y < thumbBottom;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
    	VelocityTracker mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(ev);
        final int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                if (isEnabled() && hitThumb(x, y)) {
                    mTouchMode = TOUCH_MODE_DOWN;
                    mTouchX = x;
                    mTouchY = y;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                switch (mTouchMode) {
                    case TOUCH_MODE_IDLE:
                        // Didn't target the thumb, treat normally.
                        break;

                    case TOUCH_MODE_DOWN: {
                        final float x = ev.getX();
                        final float y = ev.getY();
                        if (Math.abs(x - mTouchX) > mTouchSlop
                                || Math.abs(y - mTouchY) > mTouchSlop) {
                            mTouchMode = TOUCH_MODE_DRAGGING;
                            getParent().requestDisallowInterceptTouchEvent(true);
                            mTouchX = x;
                            mTouchY = y;
                            return true;
                        }
                        break;
                    }

                    case TOUCH_MODE_DRAGGING: {
                        final float x = ev.getX();
                        final float dx = x - mTouchX;
                        float newPos = Math.max(0,
                                Math.min(mThumbPosition + dx, getThumbScrollRange()));
                        if (newPos != mThumbPosition) {
                            mThumbPosition = newPos;
                            mTouchX = x;
                            invalidate();
                        }
                        return true;
                    }
                }
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mTouchMode == TOUCH_MODE_DRAGGING) {
                    stopDrag(ev, mVelocityTracker);
                    return true;
                }
                mTouchMode = TOUCH_MODE_IDLE;
                mVelocityTracker.clear();
                break;
            }
        }
        mVelocityTracker.recycle();

        return super.onTouchEvent(ev);
    }

    private void cancelSuperTouch(MotionEvent ev) {
        MotionEvent cancel = MotionEvent.obtain(ev);
        cancel.setAction(MotionEvent.ACTION_CANCEL);
        super.onTouchEvent(cancel);
        cancel.recycle();
    }

    /**
     * Called from onTouchEvent to end a drag operation.
     * 
     * @param ev Event that triggered the end of drag mode - ACTION_UP or
     *            ACTION_CANCEL
     */
    private void stopDrag(MotionEvent ev, VelocityTracker mVelocityTracker) {
        mTouchMode = TOUCH_MODE_IDLE;
        // Up and not canceled, also checks the switch has not been disabled
        // during the drag
        boolean commitChange = ev.getAction() == MotionEvent.ACTION_UP && isEnabled();

        cancelSuperTouch(ev);

        if (commitChange) {
            boolean newState;
            mVelocityTracker.computeCurrentVelocity(1000);
            float xvel = mVelocityTracker.getXVelocity();
            if (Math.abs(xvel) > mMinFlingVelocity) {
                newState = xvel > 0;
            } else {
                newState = getTargetCheckedState();
            }
            animateThumbToCheckedState(newState);
        } else {
            animateThumbToCheckedState(isChecked());
        }
    }

    private void animateThumbToCheckedState(boolean newCheckedState) {
        // TODO animate!
        // float targetPos = newCheckedState ? 0 : getThumbScrollRange();
        // mThumbPosition = targetPos;
        setChecked(newCheckedState);
    }

    private boolean getTargetCheckedState() {
        return mThumbPosition >= getThumbScrollRange() / 2;
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        mThumbPosition = checked ? getThumbScrollRange() : 0;
        invalidate();
    }
    
    @Override
	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		super.setEnabled(enabled);
		
		TypedArray a = mContext.obtainStyledAttributes(mAttrs, R.styleable.SwitchButton, R.attr.SwitchButtonStyle, 0);
		if (enabled) {
			mThumbDrawable = a.getDrawable(R.styleable.SwitchButton_thumb);
		} else {
			mThumbDrawable = a.getDrawable(R.styleable.SwitchButton_thumbUnable);
		}
        a.recycle();
	}
    

	@Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mThumbPosition = isChecked() ? getThumbScrollRange() : 0;

        int switchRight = getWidth() - getPaddingRight();
        int switchLeft = switchRight - mSwitchWidth;
        int switchTop = 0;
        int switchBottom = 0;
        switch (getGravity() & Gravity.VERTICAL_GRAVITY_MASK) {
            default:
            case Gravity.TOP:
                switchTop = getPaddingTop();
                switchBottom = switchTop + mSwitchHeight;
                break;

            case Gravity.CENTER_VERTICAL:
                switchTop = (getPaddingTop() + getHeight() - getPaddingBottom()) / 2
                        - mSwitchHeight / 2;
                switchBottom = switchTop + mSwitchHeight;
                break;

            case Gravity.BOTTOM:
                switchBottom = getHeight() - getPaddingBottom();
                switchTop = switchBottom - mSwitchHeight;
                break;
        }

        mSwitchLeft = switchLeft;
        mSwitchTop = switchTop;
        mSwitchBottom = switchBottom;
        mSwitchRight = switchRight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the switch
        int switchLeft = mSwitchLeft;
        int switchTop = mSwitchTop;
        int switchRight = mSwitchRight;
        int switchBottom = mSwitchBottom;

        mTrackDrawable.setBounds(switchLeft, switchTop, switchRight, switchBottom);
        mTrackDrawable.draw(canvas);

        canvas.save();

        mTrackDrawable.getPadding(mTempRect);
        int switchInnerLeft = switchLeft + mTempRect.left;
        int switchInnerTop = switchTop + mTempRect.top;
        int switchInnerRight = switchRight - mTempRect.right;
        int switchInnerBottom = switchBottom - mTempRect.bottom;
        canvas.clipRect(switchInnerLeft, switchTop, switchInnerRight, switchBottom);

        mThumbDrawable.getPadding(mTempRect);
        final int thumbPos = (int) (mThumbPosition + 0.5f);
    	int thumbLeft = getTargetCheckedState() ?
    			(switchInnerLeft - mTempRect.left + thumbPos) :
    				(switchInnerLeft - mTempRect.left + thumbPos + 5);
        int thumbRight = getTargetCheckedState() ?
        		(switchInnerLeft + thumbPos + mThumbWidth + mTempRect.right - 5) :
        			(switchInnerLeft + thumbPos + mThumbWidth + mTempRect.right);
        int thumbTop = (switchTop + switchBottom) / 2 - mThumbDrawable.getIntrinsicHeight() / 2;
        int thumbBottom = (switchTop + switchBottom) / 2 + mThumbDrawable.getIntrinsicHeight() / 2;

        mThumbDrawable.setBounds(thumbLeft, thumbTop, thumbRight, thumbBottom);
        mThumbDrawable.draw(canvas);

        // mTextColors should not be null, but just in case
        if (mTextColors != null) {
//            mTextPaint.setColor(mTextColors.getColorForState(getDrawableState(),
//                    mTextColors.getDefaultColor()));
        	TypedArray a = mContext.obtainStyledAttributes(mAttrs, R.styleable.SwitchButton, R.attr.SwitchButtonStyle, 0);
        	mTextPaint.setColor(getTargetCheckedState() ?
        			a.getColor(R.styleable.SwitchButton_textOnColor, mTextColors.getDefaultColor()) :
        				a.getColor(R.styleable.SwitchButton_textOffColor, mTextColors.getDefaultColor()));
        	a.recycle();
        }
        mTextPaint.drawableState = getDrawableState();

        Layout switchText = getTargetCheckedState() ? mOnLayout : mOffLayout;

//        canvas.translate((thumbLeft + thumbRight) / 2 - switchText.getWidth() / 2,
//                (switchInnerTop + switchInnerBottom) / 2 - switchText.getHeight() / 2);
        if (getTargetCheckedState()) {
        	canvas.translate((switchInnerLeft + thumbLeft) / 2 - switchText.getWidth() / 2,
        			(switchInnerTop + switchInnerBottom) / 2 - switchText.getHeight() / 2);
        } else {
            canvas.translate((thumbRight + switchInnerRight) / 2 - switchText.getWidth() / 2,
            		(switchInnerTop + switchInnerBottom) / 2 - switchText.getHeight() / 2);
        }
        switchText.draw(canvas);

        canvas.restore();
    }

    @Override
    public int getCompoundPaddingRight() {
        int padding = super.getCompoundPaddingRight() + mSwitchWidth;
        if (!TextUtils.isEmpty(getText())) {
            padding += mSwitchPadding;
        }
        return padding;
    }

    private int getThumbScrollRange() {
        if (mTrackDrawable == null) {
            return 0;
        }
        mTrackDrawable.getPadding(mTempRect);
        return mSwitchWidth - mThumbWidth - mTempRect.left - mTempRect.right;
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        int[] myDrawableState = getDrawableState();

        // Set the state of the Drawable
        // Drawable may be null when checked state is set from XML, from super
        // constructor
        if (mThumbDrawable != null)
            mThumbDrawable.setState(myDrawableState);
        if (mTrackDrawable != null)
            mTrackDrawable.setState(myDrawableState);

        invalidate();
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == mThumbDrawable || who == mTrackDrawable;
    }
}
