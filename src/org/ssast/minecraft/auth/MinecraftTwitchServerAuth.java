package org.ssast.minecraft.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ssast.minecraft.util.HttpFetcher;
import org.ssast.minecraft.util.Lang;

public class MinecraftTwitchServerAuth extends ServerAuth {

	public MinecraftTwitchServerAuth(String name, String pass) {
		super(name, pass);
	}

	public void login(final AuthDoneCallback callback) {

		System.out.println(Lang.getString("msg.auth.connecting1") + "https://authserver.mojang.com/authenticate" + Lang.getString("msg.auth.connecting2"));

		String token = "SSASTLauncher" + System.nanoTime();
		
		JSONObject obj = new JSONObject();
		obj.put("agent", "Minecraft");
		obj.put("username", getName());
		obj.put("password", getPass());
		obj.put("clientToken", token);
		obj.put("requestUser", true);
		
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

		String playerName = JOptionPane.showInputDialog(null, Lang.getString("msg.twitch.inputname"), 
			"SSAST Launcher", JOptionPane.QUESTION_MESSAGE);
		
		if(playerName == null) {
			callback.authDone(this, false);
			return;
		}
		
		setPlayerName(playerName);

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

	public static String getAuthTypeName() {
		return Lang.getString("ui.auth.type.twitch");
	}
	
	public static String getAlias() {
		return "twitch";
	}

}
