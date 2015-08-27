package org.ssast.minecraft;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.ssast.minecraft.auth.AuthDoneCallback;
import org.ssast.minecraft.auth.AuthType;
import org.ssast.minecraft.auth.ServerAuth;
import org.ssast.minecraft.download.DownloadCallbackAdapter;
import org.ssast.minecraft.download.Downloadable;
import org.ssast.minecraft.download.Downloader;
import org.ssast.minecraft.process.Runner;
import org.ssast.minecraft.util.EasyFileAccess;
import org.ssast.minecraft.util.Lang;
import org.ssast.minecraft.version.Module;
import org.ssast.minecraft.version.ModuleCallbackAdapter;
import org.ssast.minecraft.version.ModuleManager;
import org.ssast.minecraft.version.RunnableModule;
import org.ssast.minecraft.version.VersionManager;
import org.ssast.minecraft.version.Version;

public class Launcher {

	private static final String VERSION = "1.6.10";
	private static final String helpWords = "SSAST Launcher V" + VERSION + " " + Lang.getString("msg.help");

	private static Launcher instance;
	private static Thread shutdownHook;

	private boolean showFrame = true;
	private LauncherFrame frame = null;

	private Downloader mainDownloader = null;

	private Map<String, RunnableModule> moduleFromChioceItem = new HashMap<String, RunnableModule>();

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
	
	@SuppressWarnings("resource")
	private void initFrame() {
		frame = new LauncherFrame();
		Config.updateToFrame(frame);
		System.setOut(new PrintStream(new OutputStream() {
			private ByteArrayOutputStream buf = new ByteArrayOutputStream();
			@Override
			public void write(int b) throws IOException {
				if(b == '\n') {
					frame.commentLabel.setText(new String(buf.toByteArray()));
					buf.reset();
				} else {
					buf.write(b);
				}
				System.err.write(b);
			}
		}));
		synchronized (this) {
			if(!showFrame) {
				return;
			}
			frame.setVisible(true);
		}
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
				Object s = frame.gameVersion.getSelectedItem();
				if(s == null)
					s = Config.currentProfile.version;

				JComboBox list = frame.gameVersion;
				Module[] modules = ModuleManager.modules;

				list.removeAllItems();
				moduleFromChioceItem.clear();
				for(int i=0; i<modules.length; i++) {
					Module m = modules[i];
					if(!Config.showOld && m.getType().startsWith("old")) {
						continue;
					}
					if(!Config.showSnapshot && m.getType().startsWith("snapshot")) {
						continue;
					}
					if(!(m instanceof RunnableModule)) {
						continue;
					}
					list.addItem(m.getName());
					moduleFromChioceItem.put(m.getName(), (RunnableModule) m);
				}
				
				frame.gameVersion.setSelectedItem(s);
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
				refreshComponentsList();
			} else {
				System.out.println(Lang.getString("msg.version.failed"));
			}
		}
	}

	private void initListeners() {
		frame.launch.addActionListener(new LaunchActionListener());
	}
/*
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
	*/
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
			
			final RunnableModule module = moduleFromChioceItem.get(frame.gameVersion.getSelectedItem());
			
			new Thread() {
				@Override
				public void run() {
					auth.login(new AuthDoneCallback() {
						public void authDone(ServerAuth auth, boolean succeed) {
							if(succeed) {
								System.out.println(Lang.getString("msg.auth.succeeded"));
								Config.updateFromFrame(frame);
								
								if(!module.isInstalled()) {
									module.install(frame.installProgress);
									while(module.isDownloading()) {
										try {
											Thread.sleep(100);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
									if(!module.isInstalled()) {
										JOptionPane.showMessageDialog(frame, Lang.getString("msg.module.failed"));
										return;
									}
								}
								
								Runner runner = new Runner(module, auth);
								if(!runner.prepare()) {
									isLoggingIn = false;
									return;
								}
								frame.setVisible(false);
								Config.saveConfig();
								Downloader.stopAll();
								runner.start();
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
		
		if(Config.proxy != null) {
			System.out.println(Lang.getString("msg.useproxy") + Config.getProxyString());
		}
		
		initMainDownloader();
		
		initGameDirs();
		
		initGameComponentsList();

		initListeners();
	}

	public static void exceptionReport(Throwable t) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		t.printStackTrace(new PrintStream(out));
		String str = out.toString();
		exceptionReport(str);
	}

	public static void exceptionReport(String str) {
		/*
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL("http://disqus.com/api/3.0/posts/create.json").openConnection();
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			conn.setReadTimeout(500);
			
			Map<String, String> params = new HashMap<String, String>();
			
			params.put("thread", "2708772165");
			params.put("message", "Version " + VERSION + ":\n" + str);
			params.put("api_key", "E8Uh5l5fHZ6gD8U3KycjAIAk46f68Zw7C6eW8WSjZvCLXebZ7p0r1yrYDrLilk2F");
			params.put("author_name", "Exception Report");
			params.put("author_email", "herbix@163.com");
			
			String paramStr = URLParam.mapToParamString(params);

			byte[] toSend = paramStr.getBytes("UTF-8");

			conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
			conn.addRequestProperty("Content-Length", String.valueOf(toSend.length));
			conn.setDoOutput(true);
			conn.connect();

			OutputStream os = conn.getOutputStream();
			os.write(toSend);
			os.flush();
			os.close();
			
			conn.getResponseCode();

		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
	}
	
	public static void removeShutdownHook() {
		Runtime.getRuntime().removeShutdownHook(shutdownHook);
	}

	public static void hideFrame() {
		synchronized (instance) {
			if(instance.frame != null) {
				instance.frame.setVisible(false);
			}
			instance.showFrame = false;
		}
	}

	public static void unhideFrame() {
		synchronized (instance) {
			if(instance.frame != null) {
				instance.frame.setVisible(true);
			}
			instance.showFrame = true;
		}
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		new Updater().checkUpdate();

		Runtime.getRuntime().addShutdownHook(shutdownHook = new Thread(){
			@Override
			public void run() {
				EasyFileAccess.deleteFileForce(new File(Config.TEMP_DIR));
			}
		});
		
		try {
			instance = new Launcher();
			instance.run();
		} catch (Throwable t) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			t.printStackTrace(new PrintStream(out));
			String str = out.toString();
			JOptionPane.showMessageDialog(null, Lang.getString("msg.main.error") + str,
					Lang.getString("msg.main.error.title"), JOptionPane.ERROR_MESSAGE);
			exceptionReport(str);
		}
	}

}
