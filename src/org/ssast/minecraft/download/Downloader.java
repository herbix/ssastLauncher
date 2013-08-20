package org.ssast.minecraft.download;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import org.ssast.minecraft.util.HttpFetcher;
import org.ssast.minecraft.util.Lang;

public class Downloader extends Thread {
	
	private static boolean downloadStop = false;
	
	private boolean stopAfterAllDone = false;
	
	private boolean forceStopped = false;
	
	private Queue<Downloadable> downloading = new LinkedList<Downloadable>();
	
	@Override
	public void run() {
		while(!downloadStop && !forceStopped && !(stopAfterAllDone && downloading.isEmpty())) {
			if(!downloading.isEmpty()) {
				Downloadable todown = downloading.poll();
				if(todown.url == null) {
					continue;
				}
				boolean succeed;
				if(todown.callback != null) {
					todown.callback.downloadStart(todown);
				}
				System.out.println(Lang.getString("msg.download.start") + todown.url);
				if(todown.saveFilePath == null) {
					todown.downloaded = HttpFetcher.fetch(todown.url);
					succeed = (todown.downloaded != null);
				} else {
					try {
						succeed = HttpFetcher.fetchAndSave(todown.url, todown.saveFilePath);
					} catch (IOException e) {
						succeed = false;
					}
				}
				if(succeed)
					System.out.println(Lang.getString("msg.download.succeeded") + todown.url);
				else
					System.out.println(Lang.getString("msg.download.failed") + todown.url);
				if(todown.callback != null) {
					todown.callback.downloadDone(todown, succeed, downloading.isEmpty());
				}
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	public void addDownload(Downloadable d) {
		synchronized(downloading) {
			downloading.add(d);
		}
	}
	
	public void stopAfterAllDone() {
		stopAfterAllDone = true;
	}
	
	public void forceStop() {
		forceStopped = true;
	}
	
	public static void stopAll() {
		downloadStop = true;
	}
}
