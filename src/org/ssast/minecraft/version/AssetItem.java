package org.ssast.minecraft.version;

import org.json.JSONObject;
import org.ssast.minecraft.Config;

import java.io.File;

public class AssetItem {
	
	private String name;
	private String hash;
	private int size;
	
	public AssetItem(JSONObject json, String name) {
		this.name = name;
		this.hash = json.getString("hash");
		this.size = json.getInt("size");
	}

	public String getName() {
		return name;
	}
	
	public int getSize() {
		return size;
	}
	
	public String getKey() {
		return hash.substring(0, 2) + "/" + hash;
	}

	public String getTempFilePath() {
		return Config.TEMP_DIR + Config.MINECRAFT_OBJECTS_PATH + "/" + getKey();
	}

	public String getRealFilePath() {
		return Config.gamePath + Config.MINECRAFT_OBJECTS_PATH + "/" + getKey();
	}

	public String getFullUrl() {
		return Config.MINECRAFT_RESOURCE_BASE + "/" + getKey();
	}

	public String getVirtualPath() {
		return Config.gamePath + Config.MINECRAFT_VIRTUAL_PATH + "/" + getName();
	}

	public boolean downloaded() {
		return new File(getRealFilePath()).isFile();
	}
}
