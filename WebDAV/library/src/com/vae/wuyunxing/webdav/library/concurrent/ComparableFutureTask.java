package com.vae.wuyunxing.webdav.library.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ComparableFutureTask<V> extends FutureTask<V> implements Comparable<ComparableFutureTask<V>> {

	private final Object o;

	public ComparableFutureTask(Callable<V> callable) {
		super(callable);
		o = callable;
	}

	public ComparableFutureTask(Runnable runnable, V result) {
		super(runnable, result);
		o = runnable;
	}

	@SuppressWarnings({"unchecked",
					   "NullableProblems"})
	@Override
	public int compareTo(ComparableFutureTask another) {
		if (this == another) {
			return 0;
		}
		if (another == null) {
			return -1; // high priority
		}
		if (o.getClass().equals(another.o.getClass())) {
			if (o instanceof Comparable) {
				return ((Comparable) o).compareTo(another.o);
			}
		}
		return 0;
	}
}
