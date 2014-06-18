package org.ssast.minecraft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.JOptionPane;

import org.ssast.minecraft.util.Lang;
import org.ssast.minecraft.util.OS;

public class Updater {

	private static final String UPDATE_URL = "https://raw.githubusercontent.com/herbix/ssastLauncher/master/build/SSASTLauncher.jar";

	private boolean filePropertyGot = false;

	private String currentFile;
	
	private long lastModified = Long.MIN_VALUE;
	
	private boolean getRemoteFileInfo() throws Exception {
		final byte[] lock = new byte[0];
		
		Thread downloadFile = new Thread() {
			public void run() {
				HttpsURLConnection conn;
				try {
					conn = (HttpsURLConnection) new URL(UPDATE_URL).openConnection();
					conn.setReadTimeout(500);
					String timeStr = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US).format(new Date(lastModified));
					conn.addRequestProperty("If-Modified-Since", timeStr);
					conn.setDoOutput(false);
					conn.connect();
					if(200 != conn.getResponseCode()) {
						synchronized (lock) {
							lock.notify();
						}
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				synchronized (lock) {
					filePropertyGot = true;
					lock.notify();
				}
			}
		};
		
		synchronized (lock) {
			downloadFile.start();
			lock.wait(500);
		}
		
		return filePropertyGot;
	}

	private boolean extractUpdater() throws Exception {
		JarFile file = new JarFile(currentFile);
		
		ZipEntry updaterEntry = file.getEntry("org/ssast/minecraft/UpdaterLater.class");
		
		InputStream in = file.getInputStream(updaterEntry);
		File saved = new File(new File(Config.TEMP_DIR), "org/ssast/minecraft/UpdaterLater.class");
		if(!saved.getParentFile().mkdirs())
			return false;
		FileOutputStream out = new FileOutputStream(saved);
		
		byte[] buffer = new byte[65536];
		int count;
		while((count = in.read(buffer)) >= 0) {
			out.write(buffer, 0, count);
		}

		in.close();
		out.close();
		
		return true;
	}
	
	void checkUpdate() {

		if(Config.dontUpdateUntil > new Date().getTime()) {
			return;
		}

		try {
			currentFile = URLDecoder.decode(
				Launcher.class.getResource("/org/ssast/minecraft/Launcher.class").toString(), "UTF-8");

			if(!currentFile.startsWith("jar:")) {
				return;
			}

			currentFile = currentFile.substring(4 + 5);
			currentFile = currentFile.substring(0, currentFile.lastIndexOf('!'));

			lastModified = new File(currentFile).lastModified();

			if(!getRemoteFileInfo())
				return;
			
			if(!extractUpdater())
				return;

			int selection = JOptionPane.showConfirmDialog(null, Lang.getString("msg.update.request"), "SSAST Launcher", JOptionPane.YES_NO_OPTION);
			if(selection != JOptionPane.YES_OPTION) {
				Config.dontUpdateUntil = new Date().getTime() + 7 * 24 * 60 * 60 * 1000;
				return;
			}
			
			UpdateDialog dialog = null;
			
			try {
				URLConnection conn = new URL(UPDATE_URL).openConnection();
				conn.setReadTimeout(500);
				InputStream in = conn.getInputStream();
				int size = conn.getContentLength();
				dialog = new UpdateDialog();
				dialog.setVisible(true);
				
				File tempFile = new File(new File(Config.TEMP_DIR), "SSASTLauncher.jar");
				FileOutputStream out = new FileOutputStream(tempFile);
				
				byte[] buffer = new byte[65536];
				int count;
				int downloaded = 0;
				while((count = in.read(buffer)) >= 0) {
					if(count > 0) {
						downloaded += count;
						out.write(buffer, 0, count);
						dialog.setProgress((double)downloaded / size);
					}
					if(!dialog.isVisible()) {
						in.close();
						out.close();
						return;
					}
				}
				
				in.close();
				out.close();
				
				dialog.setVisible(false);
				
				String java = System.getProperty("java.home") + "/bin/java";
				
				if(OS.getCurrentPlatform() == OS.WINDOWS) {
					if(new File(java + "w.exe").exists()) {
						java += "w.exe";
					}
				}

				Config.dontUpdateUntil = Long.MIN_VALUE;
				Config.saveConfig();
				
				tempFile.setLastModified(conn.getLastModified());
				
				ProcessBuilder pb = new ProcessBuilder(java, "-cp", Config.TEMP_DIR, "org.ssast.minecraft.UpdaterLater", 
						tempFile.getAbsolutePath(), currentFile);
				
				pb.start();
				System.exit(0);
			
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, Lang.getString("msg.update.exception1") + e.toString() + Lang.getString("msg.update.exception2"), "SSAST Launcher",
						JOptionPane.ERROR_MESSAGE);
				if(dialog != null) {
					dialog.setVisible(false);
				}
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
