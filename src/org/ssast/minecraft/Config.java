package org.ssast.minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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

	public static Profile currentProfile = null;
	public static Map<String, Profile> profiles = new HashMap<String, Profile>();
	public static String jrePath = System.getProperty("java.home");
	public static boolean d64 = false;
	public static boolean d32 = false;
	public static int memory = 1024;
	public static String gamePath = Util.getWorkingDirectory().getPath();
	public static long lastUpdate = Long.MIN_VALUE;
	public static long dontUpdateUntil = Long.MIN_VALUE;

	public static void saveConfig() {
		Properties p = new Properties();
		p.setProperty("jre-path", jrePath);
		p.setProperty("d64", String.valueOf(d64));
		p.setProperty("d32", String.valueOf(d32));
		p.setProperty("memory", String.valueOf(memory));
		p.setProperty("game-path", gamePath);
		p.setProperty("last-update", String.valueOf(lastUpdate));
		p.setProperty("dont-update-until", String.valueOf(dontUpdateUntil));
		String profileList = "";
		for(String profileName : profiles.keySet()) {
			profileList += profileName + ";";
			p.setProperty("profile-" + profileName, profiles.get(profileName).toSavedString());
		}
		p.setProperty("profiles", profileList);
		p.setProperty("current-profile", currentProfile.profileName);
		
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

			profiles.clear();
			String profileList = p.getProperty("profiles", "");
			String[] split = profileList.split(";");
			for(String profileName : split) {
				if(profileName.equals(""))
					continue;
				Profile profile = new Profile(profileName, p.getProperty("profile-" + profileName, null));
				profiles.put(profileName, profile);
			}
			if(!profiles.containsKey("(Default)"))
				profiles.put("(Default)", new Profile("(Default)", null));
			
			String current = p.getProperty("current-profile", "(Default)");
			currentProfile = profiles.get(current);
			if(currentProfile == null)
				currentProfile = profiles.get("(Default)");
			
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateToFrame(LauncherFrame frame) {
		frame.profiles.removeAllItems();
		for(Profile profile : profiles.values()) {
			frame.profiles.addItem(profile);
		}
		frame.profiles.setSelectedItem(currentProfile);
		currentProfile.updateToFrame(frame);
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
		profiles.clear();
		for(int i=0; i<frame.profiles.getItemCount(); i++) {
			Profile profile = (Profile)frame.profiles.getItemAt(i);
			profiles.put(profile.profileName, profile);
		}
		currentProfile = (Profile)frame.profiles.getSelectedItem();
		if(currentProfile == null)
			currentProfile = profiles.get("(Default)");
		currentProfile.updateFromFrame(frame);
		jrePath = frame.jrePath.getText();
		if(jrePath.equals("")) {
			jrePath = System.getProperty("java.home");
		}
		d64 = frame.runningMode64.isSelected();
		d32 = frame.runningMode32.isSelected();
		try {
			memory = Integer.valueOf(frame.memorySize.getText());
		} catch (Exception e) {	}
	}

	static {
		loadConfig();
	}
}
