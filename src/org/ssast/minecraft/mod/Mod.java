package org.ssast.minecraft.mod;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.ssast.minecraft.util.EasyZipAccess;

public class Mod {
	
	private String name = null;
	private String path = null;
	private String patten;

	List<String> containedClasses = new ArrayList<String>();
	boolean loaded;

	public Mod(String name, String path, boolean loaded, String patten, String version) throws Exception {
		this.name = name;
		this.path = path;
		generateContainedClasses();
		this.patten = patten;
		if(checkForVersion(version))
			setLoaded(loaded);
		else
			this.loaded = loaded;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPath() {
		return path;
	}
	
	public boolean isLoaded() {
		return loaded;
	}

	public boolean setLoaded(boolean value) {
		if(value) {
			if(ModManager.isClassesLoaded(containedClasses))
				return false;
			if(!loaded)
				ModManager.loadClasses(containedClasses);
		} else {
			if(loaded)
				ModManager.unloadClasses(containedClasses);
		}
		loaded = value;
		return true;
	}
	
	public boolean checkForVersion(String version) {
		return version.matches(this.patten);
	}
	
	private void generateContainedClasses() throws Exception {
		File f = new File(path);
		
		if(f.isFile()) {
			EasyZipAccess.addFileListToList(f, containedClasses);
		} else if(f.isDirectory()) {
			addFileToConstainedClasses(f, "");
		}
	}
	
	private void addFileToConstainedClasses(File dir, String prefix) {
		for(File f : dir.listFiles()) {
			if(f.isFile()) {
				String name = f.getName();
				String[] part = name.split("\\.");
				String ext = part[part.length - 1];
				if(ext.equals("class"))
					containedClasses.add(prefix + name);
			} else if(f.isDirectory()) {
				addFileToConstainedClasses(f, prefix + f.getName() + "/");
			}
		}
	}
}
