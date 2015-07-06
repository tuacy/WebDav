package com.vae.wuyunxing.webdav.library.concurrent;

import java.util.concurrent.Callable;

public interface PriorityCallable<T> extends Callable<T>, Comparable<PriorityCallable<T>> {

	public int getPriority();
	
}
