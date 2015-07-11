package com.vae.wuyunxing.webdav.mobile.main;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.utils.ImageUtil;

public abstract class ImageCacheAdapter extends BaseAdapter {

	private final int CACHE_PERCENT = 8;
	private final int CENT          = 1024;
	private LruCache<String, Bitmap> mMemoryCache;
	private int                      mReqH;
	private int                      mReqW;

	public ImageCacheAdapter(int reqWidth, int reqHight) {
		mReqH = reqHight;
		mReqW = reqWidth;
		//		int maxMemory = (int) (Runtime.getRuntime().maxMemory() / CENT);
		//		int cacheSize = maxMemory / CACHE_PERCENT;
		//		mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
		//			@Override
		//			protected int sizeOf(String key, Bitmap value) {
		//				// TODO Auto-generated method stub
		//				return value.getByteCount() / CENT;
		//			}
		//		};
	}

	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	private Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}

	public void loadBitmap(Context context, Uri uri, ImageView imageView) {
		// use Picasso library instead.
		//		final String imageKey = String.valueOf(url);
		//		final Bitmap bitmap = getBitmapFromMemCache(imageKey);
		//		if (bitmap != null) {
		//			imageView.setImageBitmap(bitmap);
		//		} else {
		////			imageView.setImageResource(R.drawable.image_placeholder);
		//			BitmapWorkerTask task = new BitmapWorkerTask(imageView);
		//			task.execute(url);
		//		}

		Picasso.with(context).load(uri).error(R.drawable.normal_file).resize(mReqW, mReqH).into(imageView);
	}

	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

		private ImageView mImageView;

		public BitmapWorkerTask(ImageView imageView) {
			mImageView = imageView;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			final Bitmap bitmap = ImageUtil.compressPicture(params[0], mReqH, mReqW);
			addBitmapToMemoryCache(String.valueOf(params[0]), bitmap);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			if (mImageView != null) {
				mImageView.setImageBitmap(result);
			}
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}
}
