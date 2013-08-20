package org.ssast.minecraft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Date;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ssast.minecraft.util.HttpFetcher;
import org.ssast.minecraft.util.Lang;
import org.ssast.minecraft.util.OS;

public class Updater {
	
	private String fileList = "";
	private boolean fileListGot = false;

	private String currentFile;
	
	private Object[] lock = new Object[0];
	
	private long lastUpdate;
	
	private boolean getLastUpdate() throws Exception {
		
		File f = new File(currentFile);

		if(!f.exists())
			return false;
		
		lastUpdate = f.lastModified();
		return true;
	}
	
	private boolean getRemoteFileList() throws Exception {
		Thread downloadFile = new Thread() {
			public void run() {
				synchronized (lock) {
					fileList = HttpFetcher.fetch("http://minecraft.ssast.org/filelist.php");
					fileListGot = true;
					lock.notify();
				}
			}
		};
		
		synchronized (lock) {
			downloadFile.start();
			lock.wait(500);
		}

		if(!fileListGot)
			return false;
		
		return true;
	}
	
	private JSONObject getLauncherJarInfo() {
		JSONObject listObj = new JSONObject(fileList);
		JSONArray listArr = listObj.getJSONArray("list");
		
		for(int i=0; i<listArr.length(); i++) {
			JSONObject elem = listArr.getJSONObject(i);
			if(!elem.getString("name").equals("SSASTLauncher.jar"))
				continue;
			return elem;
		}
		return null;
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
		
		lastUpdate = Config.lastUpdate;

		try {
			currentFile = URLDecoder.decode(
				Launcher.class.getResource("/org/ssast/minecraft/Launcher.class").toString(), "UTF-8");
			
			if(!currentFile.startsWith("jar:")) {
				return;
			}

			currentFile = currentFile.substring(4 + 5);
			currentFile = currentFile.substring(0, currentFile.lastIndexOf('!'));

			if(lastUpdate == Long.MIN_VALUE)
				if(!getLastUpdate())
					return;

			if(!getRemoteFileList())
				return;
			
			long remoteTime = 0;
			long size = 0;
			
			JSONObject elem = getLauncherJarInfo();

			remoteTime = elem.getLong("time");
			size = elem.getLong("size");

			if(remoteTime - lastUpdate < 1000)
				return;
			
			if(!extractUpdater())
				return;
			
			int selection = JOptionPane.showConfirmDialog(null, Lang.getString("msg.update.request"), "SSAST Launcher", JOptionPane.YES_NO_OPTION);
			if(selection == JOptionPane.NO_OPTION) {
				Config.dontUpdateUntil = new Date().getTime() + 7 * 25 * 60 * 60 * 1000;
				return;
			}
			
			try {
				URLConnection conn = new URL("http://minecraft.ssast.org/SSASTLauncher.jar").openConnection();
				conn.setReadTimeout(500);
				InputStream in = conn.getInputStream();
				UpdateDialog dialog = new UpdateDialog();
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
				tempFile.setLastModified(remoteTime);
				
				dialog.setVisible(false);
				
				String java = System.getProperty("java.home") + "/bin/java";
				
				if(OS.getCurrentPlatform() == OS.WINDOWS) {
					if(new File(java + "w.exe").exists()) {
						java += "w.exe";
					}
				}
				
				Config.lastUpdate = remoteTime;
				Config.dontUpdateUntil = Long.MIN_VALUE;
				Config.saveConfig();
				
				ProcessBuilder pb = new ProcessBuilder(java, "-cp", Config.TEMP_DIR, "org.ssast.minecraft.UpdaterLater", 
						tempFile.getAbsolutePath(), currentFile);
				
				pb.start();
				System.exit(0);
			
			} catch (Exception e) {
				JOptionPane.showConfirmDialog(null, Lang.getString("msg.update.exception1") + e.toString() + Lang.getString("msg.update.exception2"), "SSAST Launcher",
						JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Config.lastUpdate = lastUpdate;
		}
	}
}
