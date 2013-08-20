package org.ssast.minecraft.process;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ssast.minecraft.Config;
import org.ssast.minecraft.auth.ServerAuth;
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
		
		String requiredMod = tryDownloadRequiredMod(auth.getClass(), Config.currentProfile.version);
		if(requiredMod == null) {
			System.out.println(Lang.getString("msg.mod.required.error"));
			return false;
		}
		
		params.add(java);
		
		params.add("-Xmx" + Config.memory + "M");
		params.add("-Xms" + Config.memory + "M");
		
		params.add("-cp");
		String cp = requiredMod;
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
				gameParams[i] = Config.currentProfile.runPath;
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
			pb.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static String tryDownloadRequiredMod(Class<? extends ServerAuth> auth, String version) {

		try {
			String name = (String)auth.getMethod("getRequiredModName", String.class).invoke(null, Config.currentProfile.version);
			String url = (String)auth.getMethod("getRequiredModUrl", String.class).invoke(null, Config.currentProfile.version);
			
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
