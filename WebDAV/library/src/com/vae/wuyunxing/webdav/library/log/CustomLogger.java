package com.vae.wuyunxing.webdav.library.log;

public interface CustomLogger {

	public boolean isDebugEnabled();

	public void d(Class<?> clazz, String format, Object... args);

	public void e(Class<?> clazz, String format, Object... args);

	public void e(Class<?> clazz, Throwable t, String format, Object... args);
	
}
