package org.ssast.minecraft.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.ssast.minecraft.Config;
import org.ssast.minecraft.CrashDialog;
import org.ssast.minecraft.auth.ServerAuth;
import org.ssast.minecraft.mod.ModManager;
import org.ssast.minecraft.util.EasyFileAccess;
import org.ssast.minecraft.util.EasyZipAccess;
import org.ssast.minecraft.util.HttpFetcher;
import org.ssast.minecraft.util.Lang;
import org.ssast.minecraft.util.OS;
import org.ssast.minecraft.version.RunnableModule;

public class Runner {
	
	private RunnableModule module;
	private List<String> params = new ArrayList<String>();
	private ServerAuth auth;
	
	public Runner(RunnableModule module, ServerAuth auth) {
		this.module = module;
		this.auth = auth;
	}
	
	public boolean prepare() {
		
		String java = Config.jrePath + "/bin/java";
		
		java = java.replace('/', System.getProperty("file.separator").charAt(0));
		
		if(OS.getCurrentPlatform() == OS.WINDOWS) {
			if(new File(java + "w.exe").exists()) {
				java += "w.exe";
			} else {
				if(!new File(java + ".exe").exists()) {
					System.out.println(Lang.getString("msg.jrepath.error"));
					return false;
				}
			}
		} else {
			if(!new File(java).exists()) {
				System.out.println(Lang.getString("msg.jrepath.error"));
				return false;
			}
		}
		
		String requiredMod = tryDownloadRequiredMod(auth.getClass(), Config.version);
		if(requiredMod == null) {
			System.out.println(Lang.getString("msg.mod.required.error"));
			return false;
		} else if(!requiredMod.equals("")) {
			ArrayList<String> classes = new ArrayList<String>();
			try {
				EasyZipAccess.addFileListToList(new File(requiredMod), classes);
				if(ModManager.isClassesLoaded(classes)) {
					System.out.println(Lang.getString("msg.mod.required.conflict"));
					return false;
				}
			} catch(Exception e) {
				
			}
			requiredMod += System.getProperty("path.separator");
		}
		
		params.add(java);
		
		params.add("-Xmx" + Config.memory + "M");
		params.add("-Xms" + Config.memory + "M");
		
		params.add("-cp");
		String cp = requiredMod;
		cp += ModManager.getModPath(module.getName());
		if(!cp.equals(""))
			cp += System.getProperty("path.separator");
		cp += module.getClassPath();

		params.add(cp);
		
		params.add("-Djava.library.path=" + module.getNativePath());
		
		if(Config.d64)
			params.add("-d64");
		if(Config.d32)
			params.add("-d32");
		
		params.add(module.getMainClass());
		
		String[] gameParams = module.getRunningParams();
		for(int i=0; i<gameParams.length; i++) {
			if(gameParams[i].contains("${auth_player_name}")) {
				gameParams[i] = auth.getPlayerName();
			} else if(gameParams[i].contains("${auth_session}")) {
				gameParams[i] = auth.getSession();
			} else if(gameParams[i].contains("${version_name}")) {
				gameParams[i] = module.getName();
			} else if(gameParams[i].contains("${game_directory}")) {
				gameParams[i] = Config.gamePath;
			} else if(gameParams[i].contains("${game_assets}")) {
				gameParams[i] = Config.gamePath + Config.MINECRAFT_ASSET_PATH;
			} else if(gameParams[i].contains("${auth_uuid}")) {
				gameParams[i] = auth.getUuid();
			} else if(gameParams[i].contains("${auth_username}")) {
				gameParams[i] = auth.getName();
			}
		}
		
		params.addAll(Arrays.asList(gameParams));
		
		return true;
	}
	
	public void start() {

		try {
			ProcessBuilder pb = new ProcessBuilder(params.toArray(new String[params.size()]));
			pb.directory(new File(Config.gamePath));
			pb.redirectErrorStream(true);
			Process process = pb.start();

			/*
			BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while((line = r.readLine()) != null) {
				System.out.println(line);
				sb.append(line);
				sb.append('\n');
			}
			r.close();
			int reply = process.waitFor();

			//if(reply != 0) {
				CrashDialog cd;
				long current = new Date().getTime();

				File crashFolder = new File(Config.gamePath + "/crash-reports");
				if(crashFolder.isDirectory()) {
					for(File elem : crashFolder.listFiles()) {
						long modified = elem.lastModified();
						if(current - modified < 10000) {
							String crashReport = EasyFileAccess.loadFile(elem.getPath());

							if(crashReport != null) {
								cd = new CrashDialog();
								cd.setReport(crashReport);
								cd.setVisible(true);
								return;
							} 
						}
					}
				}
				cd = new CrashDialog();
				cd.setReport(Lang.getString("ui.crash.default") + sb.toString());
				cd.setVisible(true);
			//}
			//*/
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static String tryDownloadRequiredMod(Class<? extends ServerAuth> auth, String version) {

		try {
			String name = (String)auth.getMethod("getRequiredModName", String.class).invoke(null, Config.version);
			String url = (String)auth.getMethod("getRequiredModUrl", String.class).invoke(null, Config.version);
			
			if(name == null)
				return "";
			
			String modFileStr = "mods/required/" + (String)auth.getDeclaredMethod("getAlias").invoke(null) + "/" + name;
			File modFile = new File(modFileStr);
			
			if(modFile.isFile()) {
				return modFile.getAbsolutePath();
			}
			
			System.out.println(Lang.getString("msg.download.start") + url);
			
			modFile.getParentFile().mkdirs();
			
			if(HttpFetcher.fetchAndSave(url, modFileStr)) {
				System.out.println(Lang.getString("msg.download.succeeded") + url);
				return modFile.getAbsolutePath();
			}

			System.out.println(Lang.getString("msg.download.failed") + url);
			
		} catch (Exception e) {
			
		}

		return null;
	}
}
