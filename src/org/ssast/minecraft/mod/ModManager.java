package org.ssast.minecraft.mod;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.ssast.minecraft.Config;
import org.ssast.minecraft.util.EasyFileAccess;
import org.ssast.minecraft.util.Lang;

public class ModManager {
	
	private static List<Mod> modList = new ArrayList<Mod>();
	private static Map<Integer, Mod> modFromListItem = new HashMap<Integer, Mod>();
	private static List<String> loadedClasses = new ArrayList<String>();

	public synchronized static void initModInfo(String dir, List<String> loaded, String version) {
		File modDir = new File(dir);
		File[] mods = modDir.listFiles();
		if(mods == null)
			return;
		modList.clear();
		modFromListItem.clear();
		for(File mod : mods) {
			if(mod.getName().equals("required"))
				continue;

			if(mod.isDirectory()) {
				String patten = EasyFileAccess.loadFile(new File(mod, "patten").getPath());
				if(patten == null)
					patten = "^" + mod.getName().replaceAll("\\.", "\\.") + "$";
				patten = patten.trim();
				for(File submod : mod.listFiles()) {
					addMod(submod, loaded, patten, version);
				}
			} else {
				addMod(mod, loaded, "^.*$", version);
			}
		}
	}
	
	private static void addMod(File mod, List<String> loaded, String patten, String version) {
		if(!mod.isFile() && !mod.isDirectory())
			return;
		String name = mod.getName();
		String[] parts = name.split("\\.");
		String ext = parts[parts.length - 1];
		if(mod.isDirectory() || ext.equals("zip") || ext.equals("jar")) {
			try {
				Mod m = new Mod(mod.getName(), mod.getAbsolutePath(), loaded.contains(mod.getName()), patten, version);
				modList.add(m);
			} catch(Exception e) {
				
			}
		}
	}
	
	public synchronized static void showMods(JTable table, String version) {
		int i = table.getSelectedRow();
		DefaultTableModel model = (DefaultTableModel)table.getModel();

		model.setRowCount(0);

		modFromListItem.clear();
		int j = 0;
		for(Mod mod : modList) {
			if(!mod.checkForVersion(version))
				continue;
			model.addRow(new String[] {mod.getName(), 
				mod.isLoaded() ? Lang.getString("ui.mod.loaded") : Lang.getString("ui.mod.notloaded")});
			modFromListItem.put(j, mod);
			j++;
		}

		if(i < table.getRowCount()) {
			table.getSelectionModel().addSelectionInterval(i, i);
		}

		ModManager.regenerateLoadedClasses(version);
	}

	public static Mod getSelectedMod(JTable table) {
		return modFromListItem.get(table.getSelectedRow());
	}
	
	public static String getModPath(String version) {
		StringBuilder sb = new StringBuilder();
		String separator = System.getProperty("path.separator");
		
		for(int i=0; i<modList.size(); i++) {
			Mod mod = modList.get(i);
			if(!Config.loadedMod.contains(mod.getName()))
				continue;
			if(!mod.checkForVersion(version))
				continue;
			sb.append(mod.getPath());
			sb.append(separator);
		}
		
		if(sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
	
	public static List<String> getLoadedMod() {
		List<String> result = new ArrayList<String>();
		for(Mod mod : modList) {
			if(mod.isLoaded()) {
				result.add(mod.getName());
			}
		}
		return result;
	}

	public static void regenerateLoadedClasses(String version) {
		loadedClasses.clear();
		for(Mod mod : modList) {
			if(mod.isLoaded() && mod.checkForVersion(version)) {
				if(!isClassesLoaded(mod.containedClasses))
					loadClasses(mod.containedClasses);
				else
					mod.loaded = false;
			}
		}
	}
	
	public static boolean isClassesLoaded(List<String> classes) {
		for(String s : classes) {
			if(loadedClasses.contains(s))
				return true;
		}
		return false;
	}
	
	static void loadClasses(List<String> classes) {
		loadedClasses.addAll(classes);
	}
	
	static void unloadClasses(List<String> classes) {
		loadedClasses.removeAll(classes);
	}
}
