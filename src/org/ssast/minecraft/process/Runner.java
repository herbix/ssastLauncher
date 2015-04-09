package org.ssast.minecraft.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.json.JSONObject;
import org.ssast.minecraft.Config;
import org.ssast.minecraft.Launcher;
import org.ssast.minecraft.auth.ServerAuth;
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

		String javaraw = java;
		
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
		
		params.add(java);
		
		params.add("-Xmx" + Config.memory + "M");
		params.add("-Xms" + Config.memory + "M");
		
		params.add("-cp");
		String cp = "";
		cp += module.getClassPath();

		params.add(cp);

		String arch = isJre64Bit(javaraw) ? "64" : "32";

		if(Config.d64)
			arch = "64";
		if(Config.d32)
			arch = "32";

		params.add("-Djava.library.path=" + module.getNativePath(arch));
		
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

			Process p = pb.start();
			boolean windowOpened = false;

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("last.log")));
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			out.write("Running with params : " + params + "\r\n");
			
			String line;
			while((line = in.readLine()) != null) {
				if(line.toLowerCase().contains("lwjgl version")) {
					if(!Config.showDebugInfo) {
						out.close();
						return;
					}
					windowOpened = true;
				}
				out.write(line);
				System.out.println(line);
			}
			out.close();
			
			if(!windowOpened) {
				JOptionPane.showMessageDialog(null, Lang.getString("msg.run.failed"), "SSAST Launcher", JOptionPane.ERROR_MESSAGE);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			Launcher.exceptionReport(e);
		}

	}
	
	private boolean isJre64Bit(String java) {
		try {
			ProcessBuilder pb = new ProcessBuilder(java, "-version");
			pb.redirectErrorStream(true);
			Process p = pb.start();

			InputStream in = p.getInputStream();
			StringBuilder sb = new StringBuilder();
			byte[] buffer = new byte[1024];
			int n;

			while((n = in.read(buffer)) >= 0) {
				sb.append(new String(buffer, 0, n));
			}
			
			return sb.toString().toLowerCase().contains("64-bit");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
