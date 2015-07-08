package com.vae.wuyunxing.webdav.mobile.storage;

import android.content.Context;


import com.vae.wuyunxing.webdav.mobile.MobileApplication;

import de.greenrobot.dao.query.QueryBuilder;

import java.util.List;

import greendao.DownloadInfo;
import greendao.DownloadInfoDao;

public class DownloadInfoRepository {

	public static final int FAIL        = -1;
	public static final int FINISH      = 0;
	public static final int READY       = 1;
	public static final int DOWNLOADING = 2;
	public static final int STOP        = 3;

	public static List<DownloadInfo> getAll(Context context) {
		return getDownloadInfoDao(context).loadAll();
	}

	public static List<DownloadInfo> getManualUpload(Context context) {
		QueryBuilder<DownloadInfo> qb = getDownloadInfoDao(context).queryBuilder();
		qb.where(DownloadInfoDao.Properties.AutoSyncDownload.eq(Boolean.valueOf(false)));

		return qb.list();
	}

	public static DownloadInfo getWithId(Context context, long id) {
		return getDownloadInfoDao(context).load(id);
	}

	public static DownloadInfo getWithState(Context context, int state) {
		QueryBuilder<DownloadInfo> qb = getDownloadInfoDao(context).queryBuilder();
		return qb.where(DownloadInfoDao.Properties.State.eq(state)).limit(1).unique();
	}

	public static String getFilenameWithId(Context context, long id) {
		return getDownloadInfoDao(context).load(id).getFilename();
	}

	public static long insertOrUpdate(Context context, DownloadInfo info) {
		return getDownloadInfoDao(context).insertOrReplace(info);
	}

	public static void delete(Context context, DownloadInfo info) {
		getDownloadInfoDao(context).delete(info);
	}

	public static void deleteWithId(Context context, long id) {
		getDownloadInfoDao(context).deleteByKey(id);
	}

	public static void deleteInTx(Context context, DownloadInfo... infos) {
		getDownloadInfoDao(context).deleteInTx(infos);
	}

	public static void deleteWithIdInTx(Context context, Long... ids) {
		getDownloadInfoDao(context).deleteByKeyInTx(ids);
	}

	private static DownloadInfoDao getDownloadInfoDao(Context c) {
		return ((MobileApplication) c.getApplicationContext()).getDaoSession().getDownloadInfoDao();
	}
}
