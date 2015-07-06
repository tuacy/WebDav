package com.vae.wuyunxing.webdav.library.concurrent;

public abstract class PriorityTask<T> implements PriorityCallable<T> {

	private final int mPriority;

	public PriorityTask(int priority) {
		mPriority = priority;
	}

	@Override
	public int getPriority() {
		return mPriority;
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public int compareTo(PriorityCallable<T> another) {
		int lhs = this.getPriority();
		int rhs = another.getPriority();
		return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
	}
}
