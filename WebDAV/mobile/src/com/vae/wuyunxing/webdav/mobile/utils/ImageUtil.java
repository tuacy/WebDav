package com.vae.wuyunxing.webdav.mobile.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;

import com.vae.wuyunxing.webdav.mobile.R;


public class ImageUtil {

	public static Bitmap generateNumberFlagIcon(Context context, Bitmap icon, int count) {
		Bitmap contactIcon = Bitmap.createBitmap(icon.getWidth(), icon.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(contactIcon);

		Paint iconPaint = new Paint();
		iconPaint.setDither(true);
		iconPaint.setFilterBitmap(true);
		Rect src = new Rect(0, 0, icon.getWidth(), icon.getHeight());
		Rect dst = new Rect(0, 0, icon.getWidth(), icon.getHeight());
		canvas.drawBitmap(icon, src, dst, iconPaint);

		Paint countPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
		countPaint.setColor(Color.RED);
		countPaint.setTypeface(Typeface.DEFAULT_BOLD);
		canvas.drawCircle(context.getResources().getInteger(R.integer.draw_circle_cx),
						  context.getResources().getInteger(R.integer.draw_circle_cy),
						  context.getResources().getInteger(R.integer.draw_circle_radius), countPaint);
		countPaint.setColor(Color.WHITE);
		if (count >= 10) {    //2 bit
			countPaint.setTextSize(context.getResources().getInteger(R.integer.draw_text_size_small));
			canvas.drawText(String.valueOf(count), context.getResources().getInteger(R.integer.draw_text_cx_big),
							context.getResources().getInteger(R.integer.draw_text_cy), countPaint);
		} else {    //1 bit
			countPaint.setTextSize(context.getResources().getInteger(R.integer.draw_text_size_big));
			canvas.drawText(String.valueOf(count), context.getResources().getInteger(R.integer.draw_text_cx_small),
							context.getResources().getInteger(R.integer.draw_text_cy), countPaint);
		}

		return contactIcon;
	}

	/**
	 * convert a picture as a circular picture.
	 * @param bitmap
	 * @return
	 */
	public static Bitmap generateRoundBitmap(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dstLeft, dstTop, dstRight, dstBottom;
		if (width <= height) {
			roundPx = width / 2;
			top = 0;
			bottom = width;
			left = 0;
			right = width;
			height = width;
			dstLeft = 0;
			dstTop = 0;
			dstRight = width;
			dstBottom = width;
		} else {
			roundPx = height / 2;
			float clip = (width - height) / 2;
			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;
			dstLeft = 0;
			dstTop = 0;
			dstRight = height;
			dstBottom = height;
		}

		Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right,
								  (int) bottom);
		final Rect dst = new Rect((int) dstLeft, (int) dstTop,
								  (int) dstRight, (int) dstBottom);
		final RectF rectF = new RectF(dst);

		paint.setAntiAlias(true);

		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint);
		return output;
	}

	/**
	 * compress a big picture to you want size.
	 *
	 * @param 				url the path name of local.
	 * @param reqWidth		target width of picture.
	 * @param reqHeight		target height of picture.
	 * @return
	 */
	public static Bitmap compressPicture(String url, int reqWidth, int reqHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // must set as false when unuse.
		Bitmap bitmap = null;
		bitmap = BitmapFactory.decodeFile(url, options);

		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inPreferredConfig = Bitmap.Config.ARGB_4444;
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inJustDecodeBounds = false;
		try {
			bitmap = BitmapFactory.decodeFile(url, options);
		} catch (OutOfMemoryError e) {
			System.gc();
			Log.e(null, "OutOfMemoryError");
		}
		return bitmap;
	}

	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		//source h & w
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}
}
