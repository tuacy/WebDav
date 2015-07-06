package com.vae.wuyunxing.webdav.library.concurrent;

public abstract class SafeRunnable implements Runnable {

	private static final int RETRY_LIMIT = 3;
	
	public SafeRunnable() {
		
	}

	protected abstract void onRun() throws Throwable;

	protected void onStart() {
		
	}
	
	protected void onComplete() {

	}

	protected void onCancel(Throwable t) {

	}

	protected void onFailed() {
		
	}

	protected boolean shouldReRunOnThrowable(Throwable t) {
		return false;
	}

	protected int getRetryLimit() {
		return RETRY_LIMIT;
	}

	/**
	 * Runs the job and catches any exception
	 *
	 * @param currentRunCount number of the Job ran.
	 * @return true if no need to retry, false otherwise.
	 */
	public final boolean safeRun(int currentRunCount) {
		boolean reRun = false;
		boolean failed = false;
		boolean canRetry = true;
		boolean ret = true;
		Throwable throwable = null;

		onStart();
		
		try {
			onRun();
		} catch (Throwable t) {
			failed = true;
			canRetry = currentRunCount < getRetryLimit();
			if (canRetry) {
				try {
					reRun = shouldReRunOnThrowable(t);
				} catch (Throwable ignored) {
				}
				throwable = t;
			}
		} finally {
			if (reRun) {
				ret = false;
			} else if (failed) {
				if (canRetry) {
					try {
						onCancel(throwable);
					} catch (Throwable ignored) {
					}
				} else {
					try {
						onFailed();
					} catch (Throwable ignored) {
					}
				}
			} else {
				try {
					onComplete();
				} catch (Throwable ignored) {
				}
			}
		}
		return ret;
	}

	@Override
	public void run() {
		int runCount = 0;
		while (!safeRun(runCount)) {
			runCount++;
		}
	}
}
