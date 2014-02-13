package org.ssast.minecraft.version;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ssast.minecraft.Config;
import org.ssast.minecraft.util.OS;

public class Library {
	private String name;
	private boolean extract;
	private List<String> extractExclude;
	private JSONObject nativesMap;
	private List<Rule> rules;
	private String key;
	private String url;

	public Library(JSONObject json) {
		name = json.getString("name");
		extract = json.has("extract");
		if(extract) {
			JSONArray exls = json.getJSONObject("extract").getJSONArray("exclude");
			extractExclude = new ArrayList<String>();
			for(int i=0; i<exls.length(); i++) {
				extractExclude.add(exls.getString(i));
			}
		}
		if(json.has("natives")) {
			nativesMap = json.getJSONObject("natives");
		}
		if(json.has("rules")) {
			JSONArray rls = json.getJSONArray("rules");
			rules = new ArrayList<Rule>();
			for(int i=0; i<rls.length(); i++) {
				rules.add(new Rule(rls.getJSONObject(i)));
			}
		}
		if(json.has("url")) {
			url = json.getString("url");
			if(!url.endsWith("/"))
				url += "/";
		}
	}

	public String getKey() {
		if(key != null) {
			return key;
		}
		String result = "";
		String[] part = name.split(":");
		result += part[0].replace('.', '/') + "/" + part[1] + "/" + part[2] + "/";
		result += part[1] + "-" + part[2];

		if(nativesMap != null) {
			String osName = OS.getCurrentPlatform().getName();
			String arch = System.getProperty("os.arch").equals("x86") ? "32" : "64";
			result += "-" + nativesMap.getString(osName).replaceAll("\\$\\{arch\\}", arch);
		}

		result += ".jar";
		key = result;
		return result;
	}
	
	public String getTempFilePath() {
		return Config.TEMP_DIR + "/libraries/" + getKey();
	}
	
	public String getFullUrl() {
		if(url != null)
			return url + getKey();
		else
			return Config.MINECRAFT_DOWNLOAD_LIBRARY + "/" + getKey();
	}
	
	public String getRealFilePath() {
		return Config.gamePath + "/libraries/" + getKey();
	}
	
	public String getNativeExtractedPath() {
		if(extract) {
			String key = getRealFilePath();
			return key.substring(0, key.length() - 4);
		}
		return null;
	}
	
	public String getExtractTempPath() {
		String key = getTempFilePath();
		return key.substring(0, key.length() - 4);
	}

	public boolean needDownloadInOS() {
		return Rule.isAllowed(rules);
	}

	public boolean needExtract() {
		return extract;
	}

	public List<String> getExtractExclude() {
		return extractExclude;
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof Library) ? ((Library)o).name.equals(name) : false;
	}
}
