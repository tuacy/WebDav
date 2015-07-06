package com.vae.wuyunxing.webdav.library.filter;


import com.vae.wuyunxing.webdav.library.FileInfo;

public interface FileFilter {

    public boolean accept(FileInfo file);
    
}
