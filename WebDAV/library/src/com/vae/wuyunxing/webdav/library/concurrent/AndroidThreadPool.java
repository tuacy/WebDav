package com.vae.wuyunxing.webdav.library.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class AndroidThreadPool extends ThreadPoolExecutor implements PausableExecutorService {

	private boolean isPaused;
	private ReentrantLock pauseLock = new ReentrantLock();
	private Condition     unpaused  = pauseLock.newCondition();

	public AndroidThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	public AndroidThreadPool(int corePoolSize,
							 int maximumPoolSize,
							 long keepAliveTime,
							 TimeUnit unit,
							 BlockingQueue<Runnable> workQueue,
							 ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
	}

	public AndroidThreadPool(int corePoolSize,
							 int maximumPoolSize,
							 long keepAliveTime,
							 TimeUnit unit,
							 BlockingQueue<Runnable> workQueue,
							 RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
	}

	public AndroidThreadPool(int corePoolSize,
							 int maximumPoolSize,
							 long keepAliveTime,
							 TimeUnit unit,
							 BlockingQueue<Runnable> workQueue,
							 ThreadFactory threadFactory,
							 RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
		return new ComparableFutureTask<T>(runnable, value);
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
		return new ComparableFutureTask<T>(callable);
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		pauseLock.lock();
		try {
			while (isPaused) {
				unpaused.await();
			}
		} catch (InterruptedException ie) {
			t.interrupt();
		} finally {
			pauseLock.unlock();
		}
	}

	@Override
	public void pause() {
		pauseLock.lock();
		try {
			isPaused = true;
		} finally {
			pauseLock.unlock();
		}
	}

	@Override
	public void resume() {
		pauseLock.lock();
		try {
			isPaused = false;
			unpaused.signalAll();
		} finally {
			pauseLock.unlock();
		}
	}

	@Override
	public boolean isPause() {
		return isPaused;
	}
}
