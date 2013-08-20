package org.ssast.minecraft.download;

public interface DownloadCallback {

	public void downloadDone(Downloadable d, boolean succeed, boolean queueEmpty);

	public void downloadStart(Downloadable d);

}
