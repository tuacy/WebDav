package com.vae.wuyunxing.webdav.mobile.storage;

import android.content.Context;

import com.vae.wuyunxing.webdav.mobile.MobileApplication;

import de.greenrobot.dao.query.QueryBuilder;

import java.util.List;

import greendao.UploadInfo;
import greendao.UploadInfoDao;

public class UploadInfoRepository {

	public static final int FAIL      = -1;
	public static final int FINISH    = 0;
	public static final int READY     = 1;
	public static final int UPLOADING = 2;
	public static final int STOP      = 3;

	public static List<UploadInfo> getAll(Context context) {
		return getUploadInfoDao(context).loadAll();
	}

	public static List<UploadInfo> getManualUpload(Context context) {
		QueryBuilder<UploadInfo> qb = getUploadInfoDao(context).queryBuilder();
		qb.where(UploadInfoDao.Properties.AutoSyncUpload.eq(Boolean.valueOf(false)));

		return qb.list();
	}

	public static boolean isContainFile(Context context, int hashCode, boolean autoSync) {
		QueryBuilder<UploadInfo> qb = getUploadInfoDao(context).queryBuilder();
		qb.where(UploadInfoDao.Properties.AutoSyncUpload.eq(Boolean.valueOf(autoSync)), UploadInfoDao.Properties.HashCode.eq(hashCode));
		return (qb.buildCount().count() > 0);
	}

	public static List<UploadInfo> getWaitUploadInfo(Context context, boolean autoSync) {
		QueryBuilder<UploadInfo> qb = getUploadInfoDao(context).queryBuilder();
		qb.where(UploadInfoDao.Properties.AutoSyncUpload.eq(Boolean.valueOf(autoSync)),
				 qb.or(UploadInfoDao.Properties.State.eq(FAIL), UploadInfoDao.Properties.State.eq(READY),
					   UploadInfoDao.Properties.State.eq(STOP)));
		return qb.list();
	}

	public static UploadInfo getWithId(Context context, long id) {
		return getUploadInfoDao(context).load(id);
	}

	public static UploadInfo getWithState(Context context, int state) {
		QueryBuilder<UploadInfo> qb = getUploadInfoDao(context).queryBuilder();
		return qb.where(UploadInfoDao.Properties.State.eq(state)).limit(1).unique();
	}

	public static UploadInfo getWithStateOnlyManualUpload(Context context, int state) {
		QueryBuilder<UploadInfo> qb = getUploadInfoDao(context).queryBuilder();
		return qb.where(UploadInfoDao.Properties.State.eq(state), UploadInfoDao.Properties.AutoSyncUpload.eq(false)).limit(1).unique();
	}

	public static String getFilenameWithId(Context context, long id) {
		return getUploadInfoDao(context).load(id).getFilename();
	}

	public static long insertOrUpdate(Context context, UploadInfo info) {
		return getUploadInfoDao(context).insertOrReplace(info);
	}

	public static void delete(Context context, UploadInfo info) {
		getUploadInfoDao(context).delete(info);
	}

	public static void deleteWithId(Context context, long id) {
		getUploadInfoDao(context).deleteByKey(id);
	}

	public static void deleteInTx(Context context, UploadInfo... infos) {
		getUploadInfoDao(context).deleteInTx(infos);
	}

	public static void deleteWithIdInTx(Context context, Long... ids) {
		getUploadInfoDao(context).deleteByKeyInTx(ids);
	}

	private static UploadInfoDao getUploadInfoDao(Context c) {
		return ((MobileApplication) c.getApplicationContext()).getDaoSession().getUploadInfoDao();
	}
}
