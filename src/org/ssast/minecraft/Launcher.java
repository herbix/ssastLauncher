package org.ssast.minecraft;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.ssast.minecraft.auth.AuthDoneCallback;
import org.ssast.minecraft.auth.AuthType;
import org.ssast.minecraft.auth.ServerAuth;
import org.ssast.minecraft.download.DownloadCallbackAdapter;
import org.ssast.minecraft.download.Downloadable;
import org.ssast.minecraft.download.Downloader;
import org.ssast.minecraft.mod.Mod;
import org.ssast.minecraft.mod.ModManager;
import org.ssast.minecraft.process.Runner;
import org.ssast.minecraft.util.EasyFileAccess;
import org.ssast.minecraft.util.Lang;
import org.ssast.minecraft.version.Module;
import org.ssast.minecraft.version.ModuleCallbackAdapter;
import org.ssast.minecraft.version.ModuleManager;
import org.ssast.minecraft.version.VersionManager;
import org.ssast.minecraft.version.Version;

public class Launcher {

	private static final String helpWords = "SSAST Launcher V1.4.2\n" + Lang.getString("msg.help");

	private LauncherFrame frame = null;

	private Downloader mainDownloader = null;

	private ModuleCallbackAdapter mcallback = new ModuleCallbackAdapter() {
		@Override
		public void installStart() {
			refreshComponentsList();
		}
		@Override
		public void installDone() {
			refreshComponentsList();
		}
		@Override
		public void uninstallStart() {
			refreshComponentsList();
		}
		@Override
		public void uninstallDone() {
			refreshComponentsList();
		}
	};
	
	private void initFrame() {
		frame = new LauncherFrame();
		Config.updateToFrame(frame);
		frame.setVisible(true);
		frame.setStdOut();
	}

	private void initMainDownloader() {
		mainDownloader = new Downloader();
		mainDownloader.start();
	}

	private void initGameDirs() {
		File gameFolder = new File(Config.gamePath);
		if(!gameFolder.isDirectory()) {
			if(!gameFolder.mkdirs())
				System.out.println(Lang.getString("msg.gamepath.error"));
		}
		new File(Config.gamePath + "/versions").mkdirs();
		new File(Config.gamePath + "/assets").mkdirs();
		new File(Config.gamePath + "/libraries").mkdirs();
		new File(Config.TEMP_DIR).mkdirs();
		new File(Config.MOD_DIR).mkdirs();
	}
	
	private void initGameComponentsList() {
		VersionManager.initVersionInfo(Config.gamePath + Config.MINECRAFT_VERSION_FILE);
		Map<String, Version> versionList = VersionManager.getVersionList();
		if(versionList != null) {
			ModuleManager.initModules(versionList, mcallback, mcallback);
			refreshComponentsList();
		}
		mainDownloader.addDownload(
			new Downloadable(Config.MINECRAFT_DOWNLOAD_BASE + Config.MINECRAFT_VERSION_FILE, 
			Config.TEMP_DIR + "/version_temp", new VersionDownloadCallback())
			);
	}
	
	private void refreshComponentsList() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int i = frame.modules.getSelectedRow();
				ModuleManager.showModules(frame.modulesModel);
				frame.modules.getSelectionModel().setSelectionInterval(i, i);
				
				Object s = frame.gameVersion.getSelectedItem();
				if(s == null)
					s = Config.version;
				ModuleManager.showModules(frame.gameVersion);
				frame.gameVersion.setSelectedItem(s);

				Module module = ModuleManager.getSelectedModule(frame.gameVersion);
				String version = module == null ? "" : module.getName();
				ModManager.showMods(frame.mods, version);
			}
		});
	}
	
	class VersionDownloadCallback extends DownloadCallbackAdapter {
		@Override
		public void downloadStart(Downloadable d) {
			System.out.println(Lang.getString("msg.version.downloading"));
		}
		@Override
		public void downloadDone(Downloadable d, boolean succeed, boolean queueEmpty) {
			if(succeed) {
				System.out.println(Lang.getString("msg.version.succeeded"));
				File versionFile = new File(Config.gamePath + Config.MINECRAFT_VERSION_FILE);
				File versionFileNew = new File(Config.TEMP_DIR + "/version_temp");
				versionFile.getParentFile().mkdirs();
				versionFile.delete();
				versionFileNew.renameTo(versionFile);
				VersionManager.initVersionInfo(Config.gamePath + Config.MINECRAFT_VERSION_FILE);
				Map<String, Version> versionList = VersionManager.getVersionList();
				if(versionList == null)
					return;
				ModuleManager.initModules(versionList, mcallback, mcallback);
				if(!ModuleManager.getAssetsModule().isInstalled()) {
					ModuleManager.getAssetsModule().install();
				}
				refreshComponentsList();
				ModManager.showMods(frame.mods, "");
			} else {
				System.out.println(Lang.getString("msg.version.failed"));
			}
		}
	}

	private void initModList() {
		Module module = ModuleManager.getSelectedModule(frame.gameVersion);
		String version = module == null ? "" : module.getName();
		ModManager.initModInfo(Config.MOD_DIR, Config.loadedMod, version);
		ModManager.showMods(frame.mods, version);
	}

	private void initListeners() {
		frame.loadMod.addActionListener(new ModActionListener());
		frame.unloadMod.addActionListener(new ModActionListener());
		frame.installModules.addActionListener(new ModuleActionListener());
		frame.uninstallModules.addActionListener(new ModuleActionListener());
		frame.launch.addActionListener(new LaunchActionListener());
		
		frame.gameVersion.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				Module module = ModuleManager.getSelectedModule(frame.gameVersion);
				String version = module == null ? "" : module.getName();
				ModManager.showMods(frame.mods, version);
			}
		});
	}

	class ModActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Mod m = ModManager.getSelectedMod(frame.mods);
			if(m == null) {
				System.out.println(Lang.getString("msg.mod.noselection"));
				return;
			}
			if(e.getSource() == frame.loadMod) {
				if(m.isLoaded())
					System.out.println(Lang.getString("msg.mod.alreadyloaded"));
				else
					if(!m.setLoaded(true))
						System.out.println(Lang.getString("msg.mod.conflict"));
			} else {
				m.setLoaded(false);
			}
			ModManager.showMods(frame.mods, ModuleManager.getSelectedModule(frame.gameVersion).getName());
		}
	}

	class ModuleActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Module m = ModuleManager.getSelectedModule(frame.modules);
			if(m == null) {
				System.out.println(Lang.getString("msg.module.noselection"));
				return;
			}
			if(e.getSource() == frame.installModules) {
				if(m.isInstalled())
					System.out.println(Lang.getString("msg.module.alreadyinstalled"));
				else
					m.install();
			} else {
				m.uninstall();
			}
		}
	}
	
	class LaunchActionListener implements ActionListener {
		
		private boolean isLoggingIn = false;
		
		public void actionPerformed(ActionEvent e) {
			
			if(isLoggingIn) {
				System.out.println(Lang.getString("msg.game.isloggingin"));
				return;
			}
			
			if(frame.gameVersion.getSelectedIndex() == -1) {
				System.out.println(Lang.getString("msg.game.basicrequire"));
				return;
			}
			
			if(frame.user.getText().equals("")) {
				System.out.println(Lang.getString("msg.game.nousername"));
				return;
			}

			final ServerAuth auth;
			
			if(frame.authType.getSelectedItem() != null) {
				auth = ((AuthType)frame.authType.getSelectedItem()).
					newInstance(frame.user.getText(), frame.pass.getText());
			} else {
				System.out.println("msg.auth.noselection");
				return;
			}
			
			isLoggingIn = true;
			
			new Thread() {
				@Override
				public void run() {
					auth.login(new AuthDoneCallback() {
						public void authDone(ServerAuth auth, boolean succeed) {
							if(succeed) {
								System.out.println(Lang.getString("msg.auth.succeeded"));
								Config.updateFromFrame(frame);
								Runner runner = new Runner(ModuleManager.getSelectedModule(frame.gameVersion), auth);
								if(!runner.prepare()) {
									isLoggingIn = false;
									//auth.logout();
									return;
								}
								frame.setVisible(false);
								Config.saveConfig();
								Downloader.stopAll();
								runner.start();
								//auth.logout();
								frame.dispose();
							} else {
								System.out.println(Lang.getString("msg.auth.failed"));
								isLoggingIn = false;
							}
						}
					});
				}
			}.start();
		}
	}

	private void run() {

		initFrame();
		System.out.println(helpWords);
		
		initMainDownloader();
		
		initGameDirs();
		
		initGameComponentsList();
		
		initModList();
		
		initListeners();
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		new Updater().checkUpdate();

		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				EasyFileAccess.deleteFileForce(new File(Config.TEMP_DIR));
			}
		});
		
		new Launcher().run();
	}
}
