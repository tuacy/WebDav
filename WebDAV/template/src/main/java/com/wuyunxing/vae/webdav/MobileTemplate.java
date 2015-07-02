package com.wuyunxing.vae.webdav;

import com.wuyunxing.vae.webdav.greendao.DaoTemplate;

public class MobileTemplate {
	public static void main(String[] args) throws Exception {
		new DaoTemplate().generateAll(args[0]);
	}
}
