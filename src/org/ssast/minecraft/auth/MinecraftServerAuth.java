package org.ssast.minecraft.auth;

import java.util.HashMap;

import org.ssast.minecraft.util.HttpFetcher;
import org.ssast.minecraft.util.Lang;

public class MinecraftServerAuth extends ServerAuth {
	
	private static final int VERSION = 14;

	public MinecraftServerAuth(String name, String pass) {
		super(name, pass);
	}

	public void login(final AuthDoneCallback callback) {

		System.out.println(Lang.getString("msg.auth.connecting1") + "https://login.minecraft.net" + Lang.getString("msg.auth.connecting2"));

		HashMap<String, String> args = new HashMap<String, String>();

		args.put("user", getName());
		args.put("password", getPass());
		args.put("version", String.valueOf(VERSION));

		String respond = HttpFetcher.fetchUsePostMethod("https://login.minecraft.net", args);

		if(respond == null) {
			callback.authDone(this, false);
			return;
		}

		String[] split = respond.split(":");
		
		if(split.length != 5) {
			System.out.println(respond);
			callback.authDone(this, false);
			return;
		}

		setAccessToken(split[3]);
		setUuid(split[4]);
		setPlayerName(split[2]);
		setUserType("legacy");
		callback.authDone(this, true);

	}

	public void logout() {

	}

	public static String getAuthTypeName() {
		return Lang.getString("ui.auth.type.minecraft");
	}
	
	public static String getAlias() {
		return "minecraft";
	}

}
