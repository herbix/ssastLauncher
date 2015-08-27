package org.ssast.minecraft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Date;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.swing.JOptionPane;

import org.ssast.minecraft.util.Lang;
import org.ssast.minecraft.util.OS;

public class Updater {

	private static final String UPDATE_URL = "https://raw.githubusercontent.com/herbix/ssastLauncher/concise-version/build/SSASTLauncher.jar";

	private String currentFile;
	
	private String eTag = "";
	private int size = 0;
	
	private InputStream getRemoteFileInfo() throws Exception {
		URLConnection conn;
		try {
			conn = new URL(UPDATE_URL).openConnection();
			if(!eTag.equals("")) {
				conn.addRequestProperty("If-None-Match", "\"" + eTag + "\"");
			}
			conn.connect();
			int code = ((HttpURLConnection)conn).getResponseCode();
			if(code == 200) {
				size = conn.getContentLength();
				eTag = conn.getHeaderField("ETag");
				eTag = eTag.substring(1, eTag.length()-1);
				return conn.getInputStream();
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean checkSizeEqual() {
		return size == new File(currentFile).length();
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
		new Thread() {
			@Override
			public void run() {
				checkUpdate0();
			}
		}.start();
	}
	
	void checkUpdate0() {

		if(Config.dontUpdateUntil > new Date().getTime()) {
			return;
		}
		
		eTag = Config.currentETag;

		try {
			currentFile = URLDecoder.decode(
				Launcher.class.getResource("/org/ssast/minecraft/Launcher.class").toString(), "UTF-8");
			
			if(!currentFile.startsWith("jar:")) {
				return;
			}

			currentFile = currentFile.substring(4 + 5);
			currentFile = currentFile.substring(0, currentFile.lastIndexOf('!'));

			InputStream in = getRemoteFileInfo();
			if(in == null)
				return;

			if(Config.currentETag.equals(""))
				if(checkSizeEqual()) {
					Config.currentETag = eTag;
					return;
				}

			if(eTag.equals(Config.currentETag))
				return;

			int selection = JOptionPane.showConfirmDialog(null, Lang.getString("msg.update.request"), "SSAST Launcher", JOptionPane.YES_NO_OPTION);
			if(selection != JOptionPane.YES_OPTION) {
				Config.dontUpdateUntil = new Date().getTime() + 7 * 24 * 60 * 60 * 1000;
				eTag = Config.currentETag;
				in.close();
				return;
			}
			
			Launcher.hideFrame();
			
			UpdateDialog dialog = new UpdateDialog();
			dialog.setVisible(true);
			
			try {
				if(!extractUpdater())
					throw new Exception("Cannot extract updater.");

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
				
				Config.currentETag = eTag;
				Config.dontUpdateUntil = Long.MIN_VALUE;
				Config.saveConfig();
				
				ProcessBuilder pb = new ProcessBuilder(java, "-cp", Config.TEMP_DIR, "org.ssast.minecraft.UpdaterLater", 
						tempFile.getAbsolutePath(), currentFile);

				Launcher.removeShutdownHook();
				
				pb.start();
				System.exit(0);
			
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, Lang.getString("msg.update.exception1") + e.toString() + Lang.getString("msg.update.exception2"), "SSAST Launcher",
						JOptionPane.ERROR_MESSAGE);
				if(dialog != null) {
					dialog.setVisible(false);
				}
				Launcher.unhideFrame();
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			Launcher.exceptionReport(e);
		}
	}
}
