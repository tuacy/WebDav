package com.vae.wuyunxing.webdav.library.log;

public class MKLog {

	private static CustomLogger mLogger = new CustomLogger() {
		@Override
		public boolean isDebugEnabled() {
			return false;
		}

		@Override
		public void d(Class<?> clz, String format, Object... args) {
			//void
		}

		@Override
		public void e(Class<?> clz, String format, Object... args) {
			//void
		}

		@Override
		public void e(Class<?> clz, Throwable t, String format, Object... args) {
			//void
		}
	};

	public static void setCustomLogger(CustomLogger logger) {
		MKLog.mLogger = logger;
	}

	public static boolean isDebugEnabled() {
		return mLogger.isDebugEnabled();
	}

	public static void d(Class<?> clz, String format, Object... args) {
		if (isDebugEnabled()) {
			mLogger.d(clz, format, args);
		}
	}

	public static void e(Class<?> clz, Throwable t, String format, Object... args) {
		if (isDebugEnabled()) {
			mLogger.e(clz, t, format, args);
		}
	}

	public static void e(Class<?> clz, String format, Object... args) {
		if (isDebugEnabled()) {
			mLogger.e(clz, format, args);
		}
	}

}
