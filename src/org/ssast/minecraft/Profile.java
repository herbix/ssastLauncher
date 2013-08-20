package org.ssast.minecraft;

public class Profile {
	
	public String profileName;

	public String user = "";
	public String pass = "";
	public boolean savePass = false;
	
	public String authType = "minecraft";
	
	public String gameVersion = "";

	public String runPath = Config.gamePath;

	public Profile(String name, String saved) {
		profileName = name;
		
		String[] split = saved.split(";");
		if(split.length < 6) {
			return;
		}
		
		user = split[0];
		pass = split[1];
		savePass = split[2].toLowerCase().equals("true");
		authType = split[3];
		gameVersion = split[4];
		runPath = split[5];
	}
	
	public String toSavedString() {
		StringBuilder sb = new StringBuilder();
		sb.append(user);
		sb.append(';');
		sb.append(pass);
		sb.append(';');
		sb.append(savePass);
		sb.append(';');
		sb.append(authType);
		sb.append(';');
		sb.append(gameVersion);
		sb.append(';');
		sb.append(runPath);
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return profileName;
	}
	
}
