package com.wuyunxing.vae.webdav.greendao;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * Created by wuyunxing on 2015/7/2. for crate greendao database file
 */
public class DaoTemplate {
	public void generateAll(String outDir) throws Exception {
		Schema schema = new Schema(1, "greendao");
		new DaoGenerator().generateAll(schema, outDir);
	}

	/** only use for test */
	private static void addCloudAccounts(Schema schema) {
		Entity accounts = schema.addEntity("User");
		accounts.addStringProperty("password").notNull();
		accounts.addStringProperty("username").notNull().primaryKey();
		accounts.addBooleanProperty("rememberPassword");
	}
}
