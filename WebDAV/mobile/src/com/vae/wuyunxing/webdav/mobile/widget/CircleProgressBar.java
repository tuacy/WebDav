package com.vae.wuyunxing.webdav.mobile.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import com.vae.wuyunxing.webdav.mobile.R;


public class CircleProgressBar extends View {

	private static final int MAX         = 100;
	private static final int START_ANGLE = 90;

	private int  mMaxProgress;
	private int  mProgress;
	private Rect mBounds;

	private Drawable mBackgroundDrawable;
	private Drawable mProgressDrawable;

	public CircleProgressBar(Context context) {
		this(context, null);
	}

	public CircleProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.CircleProgressBarStyle);
	}

	public CircleProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		initAttrs(context, attrs, defStyle, R.style.DefaultCircleProgressBar);

		mBounds = new Rect();
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

	private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar, defStyleAttr, defStyleRes);
		mBackgroundDrawable = attr.getDrawable(R.styleable.CircleProgressBar_progress_background);
		mProgressDrawable = attr.getDrawable(R.styleable.CircleProgressBar_progress_drawable);
		mMaxProgress = attr.getInteger(R.styleable.CircleProgressBar_progress_max, MAX);
		mProgress = attr.getInteger(R.styleable.CircleProgressBar_progress_percent, 0);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		Drawable backgroundDrawable = mBackgroundDrawable;
		Drawable progressDrawable = mProgressDrawable;

		final int backgroundWidth = backgroundDrawable == null ? 0 : backgroundDrawable.getIntrinsicWidth();
		final int backgroundHeight = backgroundDrawable == null ? 0 : backgroundDrawable.getIntrinsicHeight();
		final int progressWidth = progressDrawable == null ? 0 : progressDrawable.getIntrinsicWidth();
		final int progressHeight = progressDrawable == null ? 0 : progressDrawable.getIntrinsicHeight();

		final int widgetWidth = Math.max(backgroundWidth, progressWidth) + getPaddingLeft() + getPaddingRight();
		final int widgetHeight = Math.max(backgroundHeight, progressHeight) + getPaddingTop() + getPaddingBottom();

		switch (widthMode) {
			case MeasureSpec.AT_MOST:
				widthSize = Math.min(widthSize, widgetWidth);
				break;
			case MeasureSpec.UNSPECIFIED:
				widthSize = widgetWidth;
				break;
			case MeasureSpec.EXACTLY:
				break;
		}

		switch (heightMode) {
			case MeasureSpec.AT_MOST:
				heightSize = Math.min(heightSize, widgetHeight);
				break;
			case MeasureSpec.UNSPECIFIED:
				heightSize = widgetHeight;
				break;
			case MeasureSpec.EXACTLY:
				break;
		}

		setMeasuredDimension(widthSize, heightSize);
	}

	private Path getSectorClip(float centerX, float centerY, float radius, float startAngle, float sweepAngle) {
		Path path = new Path();
		/* Get a clip region of triangle */
		path.moveTo(centerX, centerY);
		path.lineTo((float) (centerX + radius * Math.cos(startAngle * Math.PI / 180)),
					(float) (centerY + radius * Math.sin(startAngle * Math.PI / 180)));
		path.lineTo((float) (centerX + radius * Math.cos((startAngle + sweepAngle) * Math.PI / 180)),
					(float) (centerY + radius * Math.sin((startAngle + sweepAngle) * Math.PI / 180)));
		/* Set a inscribed rectangle of the sector */
		RectF rectF = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
		/* Set a sector path */
		path.addArc(rectF, startAngle, sweepAngle);
		return path;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		final int width = getWidth();
		final int height = getHeight();

		mBounds.set(0, 0, width, height);

		if (mBackgroundDrawable != null) {
			canvas.save();
			mBackgroundDrawable.setBounds(mBounds);
			mBackgroundDrawable.draw(canvas);
			canvas.restore();
		}

		if (mProgressDrawable != null) {
			canvas.save();
			Path path = getSectorClip(width / 2, height / 2, width / 2, START_ANGLE, ((float) mProgress / mMaxProgress) * 360);
			canvas.clipPath(path);
			mProgressDrawable.setBounds(mBounds);
			mProgressDrawable.draw(canvas);
			canvas.restore();
		}
	}

	public int getMaxProgress() {
		return mMaxProgress;
	}

	public void setMaxProgress(int maxProgress) {
		this.mMaxProgress = maxProgress;
	}

	public void setProgress(int progress) {
		this.mProgress = progress;
		if (Looper.getMainLooper() == Looper.myLooper()) {
			invalidate();
		} else {
			postInvalidate();
		}
	}

}
