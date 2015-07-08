package com.wuyunxing.vae.webdav.greendao;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class DaoTemplate {
	public void generateAll(String outDir) throws Exception {
		Schema schema = new Schema(1, "greendao");
		addCloudDriveUploadTable(schema);
		addCloudDriveDownloadTable(schema);
		new DaoGenerator().generateAll(schema, outDir);
	}

	/**
	 * Upload table
	 * @param schema
	 */
	private static void addCloudDriveUploadTable(Schema schema) {
		Entity uploadInfo = schema.addEntity("UploadInfo");
		uploadInfo.addIdProperty().autoincrement();
		uploadInfo.addStringProperty("user").notNull();
		uploadInfo.addStringProperty("filename").notNull();
		uploadInfo.addStringProperty("from").notNull();
		uploadInfo.addStringProperty("to").notNull();
		uploadInfo.addDateProperty("uploadTime").notNull();
		uploadInfo.addIntProperty("percent");
		uploadInfo.addIntProperty("state");
		uploadInfo.addBooleanProperty("autoSyncUpload");
		uploadInfo.addLongProperty("totalSize");
		uploadInfo.addIntProperty("hashCode");
	}

	/**
	 * Downloader table
	 * @param schema
	 */
	private static void addCloudDriveDownloadTable(Schema schema) {
		Entity downloadInfo = schema.addEntity("DownloadInfo");
		downloadInfo.addIdProperty().autoincrement();
		downloadInfo.addStringProperty("user").notNull();
		downloadInfo.addStringProperty("filename").notNull();
		downloadInfo.addStringProperty("from").notNull();
		downloadInfo.addStringProperty("to").notNull();
		downloadInfo.addDateProperty("uploadTime").notNull();
		downloadInfo.addIntProperty("percent");
		downloadInfo.addIntProperty("state");
		downloadInfo.addBooleanProperty("autoSyncDownload");
		downloadInfo.addLongProperty("totalSize");
		downloadInfo.addIntProperty("hashCode");
	}
}
