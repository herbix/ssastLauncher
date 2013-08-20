package org.ssast.minecraft.auth;

import java.security.MessageDigest;

import org.ssast.minecraft.util.HttpFetcher;
import org.ssast.minecraft.util.Lang;


public class SkinMeServerAuth extends ServerAuth {

	public SkinMeServerAuth(String name, String pass) {
		super(name, pass);
	}

	public void login(AuthDoneCallback callback) {
		
		System.out.println(Lang.getString("msg.auth.connecting1") + "http://www.skinme.cc/api/login.php" + Lang.getString("msg.auth.connecting2"));
		
		String[] namesplit = getName().split(":");
		String s2 = getName();
		String playerName = null;
		
		if(namesplit.length == 2) {
			s2 = namesplit[1];
			playerName = namesplit[0];
		}
		
		String s1 = getPass();
		String s4 = s1;
        s4 = (new StringBuilder()).append(b("SHA1", s4)).append(s1).toString();
        s4 = (new StringBuilder()).append(b("MD5", s4)).append(b("SHA1", s2.toLowerCase())).toString();
        s4 = b("SHA1", s4);
        
        String respond = HttpFetcher.fetch("http://www.skinme.cc/api/login.php?user=" + s2 + "&hash=" + s4);
  
		if(respond == null) {
			callback.authDone(this, false);
			return;
		}
		
		String[] split = respond.split(":");
		if(split.length < 2) {
			callback.authDone(this, false);
			return;
		}
		
		if("0".equals(split[0])) {
			System.out.println(split[1]);
			callback.authDone(this, false);
			
		} else if("1".equals(split[0])) {
			split = split[1].split(",");
			
			if(split.length < 2) {
				callback.authDone(this, false);
			} else {
				if(playerName != null && !split[0].equals(playerName)) {
					System.out.println(Lang.getString("msg.auth.skinme.donthavechar"));
					callback.authDone(this, false);
				} else {
					setPlayerName(split[0]);
					setSession(split[1]);
					callback.authDone(this, true);
				}
			}
			
		} else if("2".equals(split[0])) {
			split = split[1].split(";");

			if(split.length < 1) {
				callback.authDone(this, false);
			} else {
				
				if(playerName == null) {
					String chars = "";
					for(int i=0; i<split.length; i++) {
						String[] tmpSplit = split[i].split(",");
						if(tmpSplit.length > 1) {
							if(i != 0)
								chars += "£¬";
							chars += tmpSplit[1];
						}
					}
					System.out.println(Lang.getString("msg.auth.skinme.selectone1") + chars + Lang.getString("msg.auth.skinme.selectone2"));
					
					callback.authDone(this, false);
					return;
				}
				
				boolean find = false;
				for(int i=0; i<split.length; i++) {
					String[] tmpSplit = split[i].split(",");
					if(tmpSplit.length > 1 && tmpSplit[1].equals(playerName)) {
						split = tmpSplit;
						find = true;
						break;
					}
				}
				if(!find) {
					System.out.println(Lang.getString("msg.auth.skinme.donthavechar"));
					callback.authDone(this, false);
					return;
				}
				if(split.length < 1) {
					callback.authDone(this, false);
				} else {
					respond = HttpFetcher.fetch("http://www.skinme.cc/api/login.php?user=" + s2 + "&hash=" + s4 + "&char=" + split[0]);
				
					split = respond.split(":");
					if(split.length < 2) {
						callback.authDone(this, false);
						return;
					}
					
					if("0".equals(split[0])) {
						System.out.println(split[1]);
						callback.authDone(this, false);
						
					} else if("1".equals(split[0])) {
						split = split[1].split(",");
						
						if(split.length < 2) {
							callback.authDone(this, false);
						} else {
							setPlayerName(split[0]);
							setSession(split[1]);
							callback.authDone(this, true);
						}

					} else {
						System.out.println(respond);
						callback.authDone(this, false);
					}
				}
			}
			
			callback.authDone(this, false);
		} else {
			System.out.println(respond);
			callback.authDone(this, false);
		}
	}

	public void logout() {
		
	}
	
	public static String getAuthTypeName() {
		return "SkinMe";
	}
	
	public static String getAlias() {
		return "skinme";
	}
	
	private String b(String s1, String s2)
    {
        if(s2 == null)
            return null;
        try
        {
        	MessageDigest md = MessageDigest.getInstance(s1);
        	md.update(s2.getBytes());
        	byte[] result = md.digest();
            int l = result.length;
            StringBuilder stringbuilder = new StringBuilder(l << 1);
            for(int i1 = 0; i1 < l; i1++)
            {
                stringbuilder.append(e[result[i1] >> 4 & 0xf]);
                stringbuilder.append(e[result[i1] & 0xf]);
            }

            return stringbuilder.toString();
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static final char e[] = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
        'a', 'b', 'c', 'd', 'e', 'f'
    };

}
