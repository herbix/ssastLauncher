package org.ssast.minecraft.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.ssast.minecraft.util.HttpFetcher;
import org.ssast.minecraft.util.Lang;

public class MineLoginServerAuth extends ServerAuth {

	public MineLoginServerAuth(String name, String pass) {
		super(name, pass);
	}

	public void login(final AuthDoneCallback callback) {

		System.out.println(Lang.getString("msg.auth.connecting1") + "http://www.minelogin.cc/ml/login.php" + Lang.getString("msg.auth.connecting2"));

		HashMap<String, String> args = new HashMap<String, String>();

		String hashed = "-";

		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			hashed = getBase16Str(md5.digest(getPass().getBytes()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		args.put("username", getName());
		args.put("hash", hashed);
		args.put("launcher", "SSASTLauncher");

		String respond = HttpFetcher.fetchUsePostMethod("http://www.minelogin.cc/ml/login.php", args);
		
		if(respond == null) {
			callback.authDone(this, false);
			return;
		}

		respond = respond.trim();
		if(respond.equals("100")) {
			callback.authDone(this, true);
		} else {
			System.out.println(Lang.getString("msg.auth.minelogin.failed." + respond));
			callback.authDone(this, false);
		}
	}

	public void logout() {

	}

	private static final char[] digits = 
		{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	private static String getBase16Str(byte[] digest) {
		String result = "";
		for(int i=0; i<16; i++) {
			result += digits[(digest[i] & 0xff) >>> 4];
			result += digits[digest[i] & 0xf];
		}
		return result;
	}

	public static String getAuthTypeName() {
		return Lang.getString("ui.auth.type.minelogin");
	}
	
	public static String getAlias() {
		return "minelogin";
	}
}
