package org.ssast.minecraft.version;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class RunnableModuleInfo {

	public String id;
	public String[] minecraftArguments;
	public String time;
	public String mainClass;
	public String processArguments;
	public String releaseTime;
	public String type;
	public String incompatibilityReason;
	public List<Library> libraries;
	public List<Rule> rules;
	
	public RunnableModuleInfo(JSONObject json) {
		id = json.getString("id");
		minecraftArguments = json.getString("minecraftArguments").split(" ");
		time = json.getString("time");
		mainClass = json.getString("mainClass");
		if(json.has("processArguments"))
			processArguments = json.getString("processArguments");
		releaseTime = json.getString("releaseTime");
		type = json.getString("type");
		if(json.has("incompatibilityReason"))
			incompatibilityReason = json.getString("incompatibilityReason");

		JSONArray libs = json.getJSONArray("libraries");
		libraries = new ArrayList<Library>();
		for(int i=0; i<libs.length(); i++) {
			libraries.add(new Library(libs.getJSONObject(i)));
		}
		
		if(json.has("rules")) {
			JSONArray rls = json.getJSONArray("rules");
			rules = new ArrayList<Rule>();
			for(int i=0; i<rls.length(); i++) {
				rules.add(new Rule(rls.getJSONObject(i)));
			}
		}
	}
	
	public boolean canRunInThisOS() {
		return Rule.isAllowed(rules);
	}
}
