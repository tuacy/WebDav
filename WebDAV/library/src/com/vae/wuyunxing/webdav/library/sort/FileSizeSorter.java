package com.vae.wuyunxing.webdav.library.sort;


import com.vae.wuyunxing.webdav.library.DotDotFile;
import com.vae.wuyunxing.webdav.library.DotFile;
import com.vae.wuyunxing.webdav.library.FileInfo;

import java.util.Comparator;

public class FileSizeSorter implements Comparator<FileInfo> {

    private final boolean mIsAscending;
    
    public FileSizeSorter(boolean ascending) {
        this.mIsAscending = ascending;
    }

    @Override
    public int compare(FileInfo lhs, FileInfo rhs) {
		DotFile dot = new DotFile();
		DotDotFile dotDot = new DotDotFile();
		if (lhs.equals(dot)) {
			return rhs.equals(dot) ? 0 : -1;
		} else if (rhs.equals(dot)) {
			return 1;
		} else if (lhs.equals(dotDot)) {
			return rhs.equals(dotDot) ? 0 : -1;
		} else if (rhs.equals(dotDot)) {
			return lhs.equals(dotDot) ? 0 : 1;
		} else if (lhs.isDir() && rhs.isFile()) {
			return -1;
		} else if (lhs.isFile() && rhs.isDir()) {
			return 1;
		} else {
			long lsize = lhs.size();
			long rsize = rhs.size();
			int ret = lsize >= rsize ? (lsize > rsize ? 1 : 0) : -1;
			return mIsAscending ? ret : -ret;
		}
    }
}
