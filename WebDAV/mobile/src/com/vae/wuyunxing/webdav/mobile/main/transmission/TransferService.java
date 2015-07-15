package com.vae.wuyunxing.webdav.mobile.main.transmission;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.util.LongSparseArray;

import com.vae.wuyunxing.webdav.library.concurrent.AndroidExecutors;
import com.vae.wuyunxing.webdav.library.log.MKLog;
import com.vae.wuyunxing.webdav.mobile.main.message.CancellationEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.CompletionEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.FailureEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.UpdateProgressEvent;
import com.vae.wuyunxing.webdav.mobile.storage.DownloadInfoRepository;
import com.vae.wuyunxing.webdav.mobile.storage.UploadInfoRepository;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import de.greenrobot.event.EventBus;
import greendao.DownloadInfo;
import greendao.UploadInfo;

public class TransferService extends Service {
    public static void startup(Context context) {
        Intent intent = new Intent(context, TransferService.class);
        context.startService(intent);
    }

    public static void bind(Context context, ServiceConnection conn) {
        Intent intent = new Intent(context, TransferService.class);
        context.bindService(intent, conn, BIND_AUTO_CREATE);
    }

    public static void unbind(Context context, ServiceConnection conn) {
        context.unbindService(conn);
    }

    private final TransmitterBinder mBinder = new TransmitterBinder();

    private static final ExecutorService sUploadExecutor = AndroidExecutors.newSingleThreadExecutor();
    private static final ExecutorService sDownloadExecutor = AndroidExecutors.newSingleThreadExecutor();
    private final LongSparseArray<Future<?>> mUploaders = new LongSparseArray<Future<?>>();
    private final LongSparseArray<Future<?>> mDownloaders = new LongSparseArray<Future<?>>();

    @Override
    public void onCreate() {
        super.onCreate();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public final class TransmitterBinder extends Binder {

        public TransferService getTransferService() {
            return TransferService.this;
        }
    }

    /** EventBus event (download and upload event) */
    public void onEventAsync(CompletionEvent event) {
        long id = event.mID;
        String filename = event.mFilename;
        if (event.mIsUpload) {
            removeUploader(id);
            if (!event.mIsSyncEvent) {
                cancelUploadingNotification();
                showUploadFinishNotification(filename);
            }
            notifyUploader(null);
        } else {
            removeDownloader(id);
            if (!event.mIsSyncEvent) {
                cancelDownloadingNotification();
                showDownloadFinishNotification(filename);
            }
            notifyDownloader(null);
        }
    }

    public void onEventAsync(CancellationEvent event) {
        long id = event.mID;
        if (event.mIsUpload) {
            removeUploader(id);
            if (!event.mIsSyncEvent) {
                cancelUploadingNotification();
            }
            notifyUploader(null);
        } else {
            removeDownloader(id);
            if (!event.mIsSyncEvent) {
                cancelDownloadingNotification();
            }
            notifyDownloader(null);
        }
    }

    public void onEventAsync(FailureEvent event) {
        long id = event.mID;
        String filename = event.mFilename;
        if (event.mIsUpload) {
            removeUploader(id);
            if (!event.mIsSyncEvent) {
                cancelUploadingNotification();
                showUploadFailNotification(filename);
            }
            notifyUploader(null);
        } else {
            removeDownloader(id);
            if (!event.mIsSyncEvent) {
                cancelDownloadingNotification();
                showDownloadFailNotification(filename);
            }
            notifyDownloader(null);
        }
    }

    public void onEventAsync(UpdateProgressEvent event) {
        int progress = event.mProgress;
        String filename = event.mFilename;
        if (event.mIsSyncEvent) {
            return;
        }
        if (event.mIsUpload) {
            updateUploadingNotification(progress, filename);
        } else {
            updateDownloadNotification(progress, filename);
        }
    }


    /** notification UI */

    private static final int NOTIFICATION_UPLOADING_ID   = 0x301;
    private static final int NOTIFICATION_DOWNLOADING_ID = 0x401;

    private void updateUploadingNotification(int progress, String filename) {
    }

    private void cancelUploadingNotification() {
    }

    private void showUploadFinishNotification(String filename) {
    }

    private void showUploadFailNotification(String filename) {
    }

    private void updateDownloadNotification(int progress, String filename) {
    }

    private void cancelDownloadingNotification() {
    }

    private void showDownloadFinishNotification(String filename) {
    }

    private void showDownloadFailNotification(String filename) {
    }

    /** Actions */

    private void updateUploadInfoState(long id, int state) {
        updateUploadInfoState(UploadInfoRepository.getWithId(TransferService.this, id), state);
    }

    private void updateUploadInfoState(UploadInfo info, int state) {
        info.setState(state);
        UploadInfoRepository.insertOrUpdate(TransferService.this, info);
    }

    private void updateDownloadInfoState(long id, int state) {
        updateDownloadInfoState(DownloadInfoRepository.getWithId(TransferService.this, id), state);
    }

    private void updateDownloadInfoState(DownloadInfo info, int state) {
        info.setState(state);
        DownloadInfoRepository.insertOrUpdate(TransferService.this, info);
    }

    /**
     * Get next upload info
     * @param id
     * @return
     */
    private UploadInfo getNextUpload(Long id) {
        Context context = TransferService.this;
        UploadInfo info;
        if (id == null) {
            if (canAutoSync()) {
                info = UploadInfoRepository.getWithState(context, UploadInfoRepository.READY);
            } else {
                info = UploadInfoRepository.getWithStateOnlyManualUpload(context, UploadInfoRepository.READY);
            }
        } else {
            info = UploadInfoRepository.getWithId(context, id);
            switch (info.getState()) {
                case UploadInfoRepository.UPLOADING:
                case UploadInfoRepository.FINISH:
                    return null;
            }
        }
        return info;
    }

    private boolean canAutoSync() {
        return true;
    }

    /**
     * Get next download info
     * @param id
     * @return
     */
    private DownloadInfo getNextDownload(Long id) {
        Context context = TransferService.this;
        DownloadInfo info;
        if (id == null) {
            info = DownloadInfoRepository.getWithState(context, DownloadInfoRepository.READY);
        } else {
            info = DownloadInfoRepository.getWithId(context, id);
            switch (info.getState()) {
                case DownloadInfoRepository.DOWNLOADING:
                case DownloadInfoRepository.FINISH:
                    return null;
            }
        }
        return info;
    }

    private void removeUploader(final long id) {
        synchronized (mUploaders) {
            mUploaders.remove(id);
        }
    }

    private void removeDownloader(final long id) {
        synchronized (mDownloaders) {
            mDownloaders.remove(id);
        }
    }

    /**
     * Notification start uploadr
     * @param id
     */
    private void notifyUploader(final Long id) {
        UploadInfo info;
        synchronized (mUploaders) {
            if (mUploaders.size() <= 0 && (info = getNextUpload(id)) != null) {
                Future<?> task = sUploadExecutor.submit(new WebDAVJackrabbitUploader(TransferService.this, info));
                mUploaders.append(info.getId(), task);
            }
        }
    }

    /**
     * Notification start downloader
     * @param id
     */
    private void notifyDownloader(final Long id) {
        DownloadInfo info;
        synchronized (mDownloaders) {
            if (mDownloaders.size() <= 0 && (info = getNextDownload(id)) != null) {
                Future<?> task = sDownloadExecutor.submit(new WebDAVJackrabbitDownloader(TransferService.this, info));
                mDownloaders.append(info.getId(), task);
            }
        }
    }

    /** add upload or download info to database */
    public static long enqueue(Context context, boolean upload, Param param) {
        long id;

        if (upload) {
            UploadInfo info = new UploadInfo(null, null, param.getFilename(), param.getFrom(), param.getTo(),
                    Calendar.getInstance().getTime(), 0, UploadInfoRepository.READY,
                    Boolean.valueOf(param.isAutoSync()), param.getTotalSize(), param.getHashCode());
            id = UploadInfoRepository.insertOrUpdate(context, info);
        } else {
            DownloadInfo info = new DownloadInfo(null, null, param.getFilename(), param.getFrom(), param.getTo(),
                    Calendar.getInstance().getTime(), 0, DownloadInfoRepository.READY,
                    Boolean.valueOf(param.isAutoSync()), param.getTotalSize(), param.getHashCode());
            id = DownloadInfoRepository.insertOrUpdate(context, info);
        }

        return id;
    }

    public long[] enqueue(boolean upload, List<Param> params) {
        if (params == null || params.size() <= 0) {
            return null;
        }

        int i = 0;
        long[] id = new long[params.size()];

        for (Param param : params) {
            if (upload) {
                UploadInfo info = new UploadInfo(null, "root", param.getFilename(), param.getFrom(), param.getTo(),
                        Calendar.getInstance().getTime(), 0, UploadInfoRepository.READY,
                        Boolean.valueOf(param.isAutoSync()), param.getTotalSize(), param.getHashCode());
                id[i++] = UploadInfoRepository.insertOrUpdate(TransferService.this, info);
            } else {
                DownloadInfo info = new DownloadInfo(null, "root", param.getFilename(), param.getFrom(), param.getTo(),
                        Calendar.getInstance().getTime(), 0, DownloadInfoRepository.READY,
                        Boolean.valueOf(param.isAutoSync()), param.getTotalSize(), param.getHashCode());
                id[i++] = DownloadInfoRepository.insertOrUpdate(TransferService.this, info);
            }
        }

        if (upload) {
            notifyUploader(null);
        } else {
            notifyDownloader(null);
        }

        return id;
    }

    /**
     * Start upload or downloader
     * @param id
     * @param upload
     */
    public void start(long id, boolean upload) {
        if (upload) {
            updateUploadInfoState(id, UploadInfoRepository.READY);
            notifyUploader(id);
        } else {
            updateDownloadInfoState(id, DownloadInfoRepository.READY);
            notifyDownloader(id);
        }
    }

    /**
     * Stop upload and downloader
     */
    private void stopAll() {
        synchronized (mUploaders) {
            for (int i = 0; i < mUploaders.size(); i++) {
                Future<?> task = mUploaders.valueAt(i);
                try {
                    task.get();
                } catch (Exception e) {
                    MKLog.e(TransferService.class, "Throw: %s. Message: %s", e, e.getMessage());
                }
            }
            mUploaders.clear();
        }

        synchronized (mDownloaders) {
            for (int i = 0; i < mDownloaders.size(); i++) {
                Future<?> task = mDownloaders.valueAt(i);
                try {
                    task.get();
                } catch (Exception e) {
                    MKLog.e(TransferService.class, "Throw: %s. Message: %s", e, e.getMessage());
                }
            }
            mDownloaders.clear();
        }
    }

    /**
     * Stop uploader by id
     * @param id
     */
    private void stopUploader(long id) {
        synchronized (mUploaders) {
            Future<?> task = mUploaders.get(id);
            if (task != null) {
                task.cancel(true);
                mUploaders.remove(id);
            }
        }
    }

    /**
     * Stop downloader by id
     * @param id
     */
    private void stopDownloader(long id) {
        synchronized (mDownloaders) {
            Future<?> task = mDownloaders.get(id);
            if (task != null) {
                task.cancel(true);
                mDownloaders.remove(id);
            }
        }
    }

    /**
     * Stop upload or downloader by id
     * @param id
     * @param upload
     */
    public void stop(long id, boolean upload) {
        if (upload) {
            stopUploader(id);
            updateUploadInfoState(id, UploadInfoRepository.STOP);
        } else {
            stopDownloader(id);
            updateDownloadInfoState(id, DownloadInfoRepository.STOP);
        }
    }

    public static final class Param {

        private String  mFilename;
        private String  mFrom;
        private String  mTo;
        private boolean mAutoSync;
        private int     mHashCode;

        private long mTotalSize;

        public Param() {

        }

        public Param(String filename, String from, String to, boolean autoSync, long totalSize, int hashCode) {
            mFilename = filename;
            mFrom = from;
            mTo = to;
            mAutoSync = autoSync;
            mTotalSize = totalSize;
            mHashCode = hashCode;
        }

        public String getFilename() {
            return mFilename;
        }

        public void setFilename(String filename) {
            mFilename = filename;
        }

        public String getFrom() {
            return mFrom;
        }

        public void setFrom(String from) {
            mFrom = from;
        }

        public String getTo() {
            return mTo;
        }

        public void setTo(String to) {
            mTo = to;
        }

        public long getTotalSize() {
            return mTotalSize;
        }

        public boolean isAutoSync() {
            return mAutoSync;
        }

        public int getHashCode() {
            return mHashCode;
        }
    }
}
