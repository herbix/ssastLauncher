package org.ssast.minecraft.process;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
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
		cp += module.getClassPath(!cp.equals(""));

		params.add(cp);
		
		params.add("-Djava.library.path=" + module.getNativePath());
		
		if(Config.d64)
			params.add("-d64");
		if(Config.d32)
			params.add("-d32");
		
		params.add(module.getMainClass());
		
		Map<String, String> valueMap = new HashMap<String, String>();
		
		valueMap.put("auth_access_token", auth.getAccessToken());
		valueMap.put("user_properties", new JSONObject(auth.getUserProperties()).toString());

		valueMap.put("auth_session", auth.getSession());

		valueMap.put("auth_player_name", auth.getPlayerName());
		valueMap.put("auth_uuid", auth.getUuid());
		valueMap.put("user_type", auth.getUserType());
		
		valueMap.put("profile_name", Config.currentProfile.profileName);
		valueMap.put("version_name", module.getName());

		valueMap.put("game_directory", Config.currentProfile.runPath);
		valueMap.put("game_assets", Config.gamePath + Config.MINECRAFT_VIRTUAL_PATH);

		valueMap.put("assets_root", Config.gamePath + Config.MINECRAFT_ASSET_PATH);
		valueMap.put("assets_index_name", module.getAssetsIndex());
		
		String[] gameParams = module.getRunningParams();
		if(!replaceParams(gameParams, valueMap)) {
			return false;
		}
		
		if(module.isAssetsVirtual() && !module.copyAssetsToVirtual()) {
			System.out.println(Lang.getString("msg.assets.cannotload"));
			return false;
		}
		
		params.addAll(Arrays.asList(gameParams));
		
		return true;
	}
	
	private static final Pattern paramPattern = Pattern.compile("\\$\\{([a-zA-Z0-9_]*)\\}");
	
	private boolean replaceParams(String[] gameParams, Map<String, String> map) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<gameParams.length; i++) {
			String param = gameParams[i];
			Matcher m = paramPattern.matcher(param);
			int lastend = 0;
			sb.setLength(0);
			while(m.find()) {
				sb.append(param, lastend, m.start());
				String key = m.group(1);
				String value = map.get(key);
				if(value == null) {
					System.out.println(Lang.getString("msg.run.unknownparam1") +
							key + Lang.getString("msg.run.unknownparam2"));
					return false;
				}
				sb.append(value);
				lastend = m.end();
			}
			sb.append(param, lastend, param.length());
			gameParams[i] = sb.toString();
		}
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
			
			if(name == null)
				return "";
			
			String url = (String)auth.getMethod("getRequiredModUrl", String.class).invoke(null, Config.currentProfile.version);
			
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
