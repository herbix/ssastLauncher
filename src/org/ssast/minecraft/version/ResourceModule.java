package org.ssast.minecraft.version;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.ssast.minecraft.Config;
import org.ssast.minecraft.download.DownloadCallbackAdapter;
import org.ssast.minecraft.download.Downloadable;
import org.ssast.minecraft.download.Downloader;
import org.ssast.minecraft.util.EasyFileAccess;
import org.ssast.minecraft.util.Lang;

public class ResourceModule extends Module {

	private int installState = -1;
	
	private boolean isUninstalling = false;
	
	public ResourceModule(ModuleInstallCallback icallback,	ModuleUninstallCallback ucallback) {
		super(icallback, ucallback);
	}

	public void install() {
		if(isDownloading()) {
			System.out.println(Lang.getString("msg.module.isinstalling"));
			return;
		}
		moduleDownloader = new Downloader();

		moduleDownloader.addDownload(
				new Downloadable(Config.MINECRAFT_RESOURCE_BASE, new ResourceDownloadCallback("xml", "")));

		installCallback.installStart();

		moduleDownloader.stopAfterAllDone();
		moduleDownloader.start();
		System.out.println(Lang.getString("msg.module.start") + "[" + getName() + "]");
	}
	
	class ResourceDownloadCallback extends DownloadCallbackAdapter {
		private String type;
		private String key;
		public ResourceDownloadCallback(String type, String key) {
			this.type = type;
			this.key = key;
		}

		@Override
		public void downloadDone(Downloadable d, boolean succeed, boolean queueEmpty) {

			if(succeed) {
				int count = 0;
				if(type.equals("xml")) {
					JSONObject obj = XML.toJSONObject(d.getDownloaded());
					
					File assetsFolder = new File(Config.TEMP_DIR + Config.MINECRAFT_ASSET_PATH);
					assetsFolder.mkdirs();
					
					EasyFileAccess.saveFile(Config.gamePath + Config.MINECRAFT_RESOURCE_FILE, obj.toString(2));
					
					obj = obj.getJSONObject("ListBucketResult");
					JSONArray arr = obj.getJSONArray("Contents");
					
					for(int i=0; i<arr.length(); i++) {
						
						JSONObject elem = arr.getJSONObject(i);
						String key = elem.getString("Key");
						if(key.endsWith("/"))
							continue;
						if(new File(Config.gamePath + Config.MINECRAFT_ASSET_PATH + "/" + key).isFile())
							continue;
						
						new File(Config.gamePath + Config.MINECRAFT_ASSET_PATH + "/" + key).getParentFile().mkdirs();
						
						File file = new File(assetsFolder.getPath() + "/" + key);
						file.getParentFile().mkdirs();
						
						moduleDownloader.addDownload(new Downloadable(
							Config.MINECRAFT_RESOURCE_BASE + "/" + key,
							file.getPath(),
							new ResourceDownloadCallback("bin", key)
							));
						count++;
					}
				} else {
					File file = new File(d.getSavedFile());
					File fileReal = new File(Config.gamePath + Config.MINECRAFT_ASSET_PATH + "/" + key);
					
					fileReal.delete();
					file.renameTo(fileReal);
				}

				if(queueEmpty && count == 0) {
					System.out.println(Lang.getString("msg.module.succeeded") + "[" + getName() + "]");
					installState = 1;
					if(installCallback != null)
						installCallback.installDone();
				}

			} else {
				moduleDownloader.forceStop();
				System.out.println(Lang.getString("msg.module.failed") + "[" + getName() + "]");
			}
		}
	}

	public void uninstall() {
		if(isUninstalling) {
			System.out.println(Lang.getString("msg.module.isuninstalling"));
			return;
		}
		if(isDownloading()) {
			System.out.println(Lang.getString("msg.module.isinstalling"));
			return;
		}

		System.out.println(Lang.getString("msg.module.startuninstall") + "[" + getName() + "]");
		
		isUninstalling = true;
		uninstallCallback.uninstallStart();
		
		new Thread() {
			@Override
			public void run() {
				File assetsDir = new File(Config.gamePath + Config.MINECRAFT_ASSET_PATH);
				System.out.println(Lang.getString("msg.module.delete") + assetsDir.getPath());
				EasyFileAccess.deleteFileForce(assetsDir);
				assetsDir.mkdirs();
				isUninstalling = false;
				installState = 0;
				System.out.println(Lang.getString("msg.module.uninstallsucceeded") + "[" + ResourceModule.this.getName() + "]");
				uninstallCallback.uninstallDone();
			}
		}.start();
	}

	public String getName() {
		return "Assets";
	}

	public boolean isInstalled() {

		if(installState == -1) {

			String resourceStr = EasyFileAccess.loadFile(Config.gamePath + Config.MINECRAFT_RESOURCE_FILE);
			if(resourceStr == null) {
				installState = 0;
				return false;
			}
			
			try {
				JSONObject obj = new JSONObject(resourceStr);
				obj = obj.getJSONObject("ListBucketResult");
				JSONArray arr = obj.getJSONArray("Contents");
	
				for(int i=0; i<arr.length(); i++) {
					JSONObject elem = arr.getJSONObject(i);
					String key = elem.getString("Key");
					if(key.endsWith("/"))
						continue;
					if(!new File(Config.gamePath + Config.MINECRAFT_ASSET_PATH + "/" + key).isFile()) {
						installState = 0;
						return false;
					}
				}
			} catch(Exception e) {
				installState = 0;
				return false;
			}

			installState = 1;
		}

		return installState == 1;
	}

	public String getReleaseTime() {
		return "z";
	}
	
	public String getType() {
		return "-";
	}

	public String getState() {
		return isInstalled() ? Lang.getString("ui.module.installed") : Lang.getString("ui.module.notinstalled");
	}

}
