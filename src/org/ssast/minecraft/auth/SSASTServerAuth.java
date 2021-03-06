package org.ssast.minecraft.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.ssast.minecraft.util.HttpFetcher;
import org.ssast.minecraft.util.Lang;

public class SSASTServerAuth extends ServerAuth {

	private static final String loginUrl = "http://minecraft.ssast.org/login.php";
	private static final String logoutUrl = "http://minecraft.ssast.org/login.php?action=logout";
	private static final String logoutMsgPatten = "name=${name}&session=${session}";
	
	public SSASTServerAuth(String name, String pass) {
		super(name, pass);
	}
	
	public void login(final AuthDoneCallback callback) {
		System.out.println(Lang.getString("msg.auth.connecting1") + loginUrl + Lang.getString("msg.auth.connecting2"));

		String salt = HttpFetcher.fetch("http://minecraft.ssast.org/login.php?action=salt&name=" + getName());

		String hash = "";
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			hash = getBase16Str(md5.digest((getName() + getPass() + salt).getBytes()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		String result = HttpFetcher.fetchUsePostMethod("http://minecraft.ssast.org/login.php", "name=" + getName() + "&hash=" + hash);
		
		if(result == null) {
			callback.authDone(SSASTServerAuth.this, false);
			return;
		}

		result = result.trim();
		if(!result.matches("^[a-fA-F0-9]{32}$")) {
			callback.authDone(SSASTServerAuth.this, false);
			return;
		}

		setAccessToken(result.trim());
		setUuid(UUID.nameUUIDFromBytes(("SSAST:" + getPlayerName()).getBytes()).toString().replace("-", ""));
		callback.authDone(SSASTServerAuth.this, true);
	}

	private static final char[] digits = 
		{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	private static String getBase16Str(byte[] digest) {
		StringBuilder result = new StringBuilder();
		for(int i=0; i<16; i++) {
			result.append(digits[(digest[i] & 0xff) >>> 4]);
			result.append(digits[digest[i] & 0xf]);
		}
		return result.toString();
	}

	public static String getAuthTypeName() {
		return Lang.getString("ui.auth.type.ssast");
	}
	
	public static String getAlias() {
		return "ssast";
	}
}
