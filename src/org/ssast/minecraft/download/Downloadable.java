package org.ssast.minecraft.download;

public class Downloadable {
	String url = null;
	String saveFilePath = null;
	DownloadCallback callback = null;
	String downloaded = null;
	
	public Downloadable(String url, String saveFilePath) {
		this(url, saveFilePath, null);
	}

	public Downloadable(String url) {
		this(url, null, null);
	}

	public Downloadable(String url, DownloadCallback callback) {
		this(url, null, callback);
	}

	public Downloadable(String url, String saveFilePath, DownloadCallback callback) {
		this.url = url;
		this.saveFilePath = saveFilePath;
		this.callback = callback;
	}
	
	public String getDownloaded() {
		return downloaded;
	}
	
	public String getSavedFile() {
		return saveFilePath;
	}
}
