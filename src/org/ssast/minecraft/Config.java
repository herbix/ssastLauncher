package org.ssast.minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import org.ssast.minecraft.mod.ModManager;
import org.ssast.minecraft.auth.AuthType;

import net.minecraft.bootstrap.Util;

public class Config {
	
	public static final String CONFIG_FILE = "Launcher.properties";

	public static final String MINECRAFT_DOWNLOAD_BASE = 
		"https://s3.amazonaws.com/Minecraft.Download";
	public static final String MINECRAFT_RESOURCE_BASE =
		"https://s3.amazonaws.com/Minecraft.Resources";
	public static final String MINECRAFT_VERSION_FILE = 
		"/versions/versions.json";
	public static final String MINECRAFT_VERSION_PATH = 
		"/versions";
	public static final String MINECRAFT_ASSET_PATH =
		"/assets";
	public static final String MINECRAFT_VERSION_FORMAT =
		"/versions/%s/%s.json";
	public static final String MINECRAFT_VERSION_GAME_FORMAT =
		"/versions/%s/%s.jar";
	public static final String MINECRAFT_VERSION_GAME_RUN_FORMAT =
		"/versions/%s/%s_run.jar";
	public static final String MINECRAFT_VERSION_GAME_EXTRACT_TEMP_FORMAT =
		"/versions/%s/%s_temp/";
	public static final String MINECRAFT_VERSION_GAME_EXTRACT_FORMAT =
		"/versions/%s/%s/";
	public static final String MINECRAFT_LIBRARY_FORMAT =
		"/libraries/%s/%s/%s/%s-%s.jar";
	public static final String MINECRAFT_LIBRARY_NATIVE_FORMAT =
		"/libraries/%s/%s/%s/%s-%s-%s.jar";
	public static final String MINECRAFT_RESOURCE_FILE =
		"/assets/assets.json";

	public static final String MOD_DIR = "mods";
	public static final String TEMP_DIR = new File(new File(System.getProperty("java.io.tmpdir")), "SSASTLauncher").getPath();

	public static String user = "";
	public static String pass = "";
	public static boolean savePass = false;
	public static String authType = "minecraft";
	public static String version = "";
	public static List<String> loadedMod = new ArrayList<String>();
	public static String jrePath = System.getProperty("java.home");
	public static boolean d64 = false;
	public static boolean d32 = false;
	public static int memory = 1024;
	public static String gamePath = Util.getWorkingDirectory().getPath();
	public static long lastUpdate = Long.MIN_VALUE;
	public static long dontUpdateUntil = Long.MIN_VALUE;
	
	private static final byte[] magic = {123, 32, 4, 12, 5, 86, 2, 12};
	
	private static String encodePass(String pass) {
		byte[] str = pass.getBytes();
		
		for(int i=0; i<str.length; i++) {
			str[i] ^= magic[i % magic.length];
		}
		
		String result = new BASE64Encoder().encode(str);
		int equalIndex = result.indexOf('=');
		if(equalIndex > 0) {
			result = result.substring(0, equalIndex);
		}
		return result;
	}
	
	private static String decodePass(String encoded) {
		while(encoded.length() % 4 != 0)
			encoded += "=";

		byte[] str;

		try {
			str = new BASE64Decoder().decodeBuffer(encoded);
		} catch (IOException e) {
			return "";
		}

		for(int i=0; i<str.length; i++) {
			str[i] ^= magic[i % magic.length];
		}
		
		return new String(str);
	}

	public static void saveConfig() {
		Properties p = new Properties();
		p.setProperty("user", user);
		p.setProperty("pass", encodePass(pass));
		p.setProperty("save-pass", String.valueOf(savePass));
		p.setProperty("auth-type", String.valueOf(authType));
		p.setProperty("version", version);
		p.setProperty("jre-path", jrePath);
		p.setProperty("d64", String.valueOf(d64));
		p.setProperty("d32", String.valueOf(d32));
		p.setProperty("memory", String.valueOf(memory));
		p.setProperty("game-path", gamePath);
		p.setProperty("last-update", String.valueOf(lastUpdate));
		p.setProperty("dont-update-until", String.valueOf(dontUpdateUntil));
		String modarr = "";
		for(int i=0; i<loadedMod.size(); i++) {
			if(i != 0)
				modarr += ",";
			modarr += loadedMod.get(i);
		}
		p.setProperty("loaded-mod", modarr);
		try {
			FileOutputStream out = new FileOutputStream(CONFIG_FILE);
			p.store(out, "Created by SSAST Launcher");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void loadConfig() {
		InputStream in;
		Properties p = new Properties();
		try {
			in = new FileInputStream(CONFIG_FILE);
			p.load(in);
			user = p.getProperty("user", "");
			pass = decodePass(p.getProperty("pass", ""));
			try {
				savePass = Boolean.valueOf(p.getProperty("save-pass", "false"));
			} catch (Exception e) {	}
			authType = p.getProperty("auth-type", "minecraft");
			version = p.getProperty("version", "");
			jrePath = p.getProperty("jre-path", System.getProperty("java.home"));
			if(jrePath.equals("")) {
				jrePath = System.getProperty("java.home");
			}
			try {
				d64 = Boolean.valueOf(p.getProperty("d64", "false"));
			} catch (Exception e) {	}
			try {
				d32 = Boolean.valueOf(p.getProperty("d32", "false"));
			} catch (Exception e) {	}
			try {
				memory = Integer.valueOf(p.getProperty("memory", "1024"));
			} catch (Exception e) {	}
			gamePath = p.getProperty("game-path", Util.getWorkingDirectory().getPath());
			gamePath = new File(gamePath).getAbsolutePath();
			try {
				lastUpdate = Long.valueOf(p.getProperty("last-update", String.valueOf(Long.MIN_VALUE)));
			} catch (Exception e) {	}
			try {
				dontUpdateUntil = Long.valueOf(p.getProperty("dont-update-until", String.valueOf(Long.MIN_VALUE)));
			} catch (Exception e) {	}
			String modstr = p.getProperty("loaded-mod", "");
			String[] mods = modstr.split(",");
			for(int i=0; i<mods.length; i++) {
				mods[i] = mods[i].trim();
				if(!mods[i].equals(""))
					loadedMod.add(mods[i].trim());
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateToFrame(LauncherFrame frame) {
		frame.user.setText(user);
		frame.pass.setText(pass);
		frame.savePass.setSelected(savePass);
		frame.authType.setSelectedItem(AuthType.valueOf(authType));
		frame.gameVersion.setSelectedItem(version);
		frame.jrePath.setText(jrePath);
		frame.memorySizeSlider.setValue(memory);
		if(!d32 && !d64)
			frame.runningModeDefault.setSelected(true);
		if(d32)
			frame.runningMode32.setSelected(true);
		if(d64)
			frame.runningMode64.setSelected(true);
		frame.memorySize.setText(String.valueOf(memory));
	}

	public static void updateFromFrame(LauncherFrame frame) {
		user = frame.user.getText();
		savePass = frame.savePass.isSelected();
		if(savePass)
			pass = frame.pass.getText();
		else
			pass = "";
		authType = ((AuthType)frame.authType.getSelectedItem()).value();
		if(frame.gameVersion.getSelectedItem() != null)
			version = frame.gameVersion.getSelectedItem().toString();
		jrePath = frame.jrePath.getText();
		if(jrePath.equals("")) {
			jrePath = System.getProperty("java.home");
		}
		d64 = frame.runningMode64.isSelected();
		d32 = frame.runningMode32.isSelected();
		try {
			memory = Integer.valueOf(frame.memorySize.getText());
		} catch (Exception e) {	}
		if(version == null)
			version = "";
		loadedMod = ModManager.getLoadedMod();
	}

	static {
		loadConfig();
	}
}
