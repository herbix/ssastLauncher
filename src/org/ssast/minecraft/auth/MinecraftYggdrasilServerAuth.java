package org.ssast.minecraft.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ssast.minecraft.util.HttpFetcher;
import org.ssast.minecraft.util.Lang;

public class MinecraftYggdrasilServerAuth extends ServerAuth {

	public MinecraftYggdrasilServerAuth(String name, String pass) {
		super(name, pass);
	}

	public void login(final AuthDoneCallback callback) {

		System.out.println(Lang.getString("msg.auth.connecting1") + "https://authserver.mojang.com/authenticate" + Lang.getString("msg.auth.connecting2"));

		String token = "SSASTLauncher" + System.nanoTime();
		
		JSONObject obj = new JSONObject();
		obj.accumulate("agent", "Minecraft");
		obj.accumulate("username", getName());
		obj.accumulate("password", getPass());
		obj.accumulate("clientToken", token);
		obj.accumulate("requestUser", true);
		
		String result = HttpFetcher.fetchUsePostMethod("https://authserver.mojang.com/authenticate", obj);
		
		if(result == null) {
			callback.authDone(this, false);
			return;
		}
		
		JSONObject resultObj = new JSONObject(result);
		
		if(resultObj.has("clientToken") && !resultObj.getString("clientToken").equals(token)) {
			callback.authDone(this, false);
			return;
		}
		
		JSONObject profile = null;

		/*
		// selectedProfile settings
		if(resultObj.has("selectedProfile")) {
			profile = resultObj.getJSONObject("selectedProfile");
		} else
		*/
		
		if(resultObj.getJSONArray("availableProfiles").length() > 0) {
			JSONArray profiles = resultObj.getJSONArray("availableProfiles");

			List<Object> list = new ArrayList<Object>();
			Map<String, JSONObject> map = new HashMap<String, JSONObject>();

			for(int i=0; i<profiles.length(); i++) {
				JSONObject item = profiles.getJSONObject(i);
				String name = item.getString("name");
				list.add(name);
				map.put(name, item);
			}

			profile = map.get((String)selectFrom(list));
		}
		
		if(profile == null) {
			callback.authDone(this, false);
			return;
		}
		
		setPlayerName(profile.getString("name"));
		setUuid(profile.getString("id"));
		
		setAccessToken(resultObj.getString("accessToken"));

		setUserType("mojang");
		
		if(resultObj.has("user")) {
			Map<String, Collection<Object>> properties = getUserProperties();
			JSONObject user = resultObj.getJSONObject("user");
			
			if(user.has("properties")) {
				JSONArray arr = user.getJSONArray("properties");
				
				for(int i=0; i<arr.length(); i++) {
					JSONObject item = arr.getJSONObject(i);
					String name = item.getString("name");
					Object value = item.get("value");
					
					Collection<Object> list = properties.get(name);
					if(list == null) {
						list = new ArrayList<Object>();
						properties.put(name, list);
					}
					
					list.add(value);
				}
			}
		}
		
		callback.authDone(this, true);
	}

	public void logout() {

	}

	public static String getAuthTypeName() {
		return Lang.getString("ui.auth.type.yggdrasil");
	}
	
	public static String getAlias() {
		return "yggdrasil";
	}

}
