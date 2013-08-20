package org.ssast.minecraft.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtil {

	public static Class<?>[] getClassesFromPackage(String packageName, boolean searchJre) {
		
		String classPathString = System.getProperty("java.class.path");
		
		if(searchJre) {
			classPathString += System.getProperty("path.separator") + System.getProperty("sun.boot.class.path");
		}

		String[] classPaths = classPathString.split(System.getProperty("path.separator"));

		List<Class<?>> results = new ArrayList<Class<?>>();
		
		String packagePath = packageName.replaceAll("\\.", "/");
		
		for(String classPath : classPaths) {
			File cpFile = new File(classPath);

			if(cpFile.isDirectory()) {
				File pFile = new File(cpFile, packagePath);

				if(pFile.isDirectory()) {
					
					for(File cFile : pFile.listFiles()) {
						String cFileName = cFile.getName();
	
						if(cFile.isFile() && cFileName.endsWith(".class") && !cFileName.contains("$")) {
							try {
								results.add(Class.forName(packageName + "." + cFileName.substring(0, cFileName.length() - 6)));
							} catch (ClassNotFoundException e) {
							}
						}
					}
				}
			} else if(cpFile.isFile()) {
				try {
					JarFile jFile = new JarFile(cpFile);
					
					Enumeration<JarEntry> jEntrys = jFile.entries();
					
					while(jEntrys.hasMoreElements()) {
						JarEntry jClass = jEntrys.nextElement();
						String jClassPath = jClass.getName();
						
						if(jClassPath.startsWith(packagePath)) {
							String jClassName = jClassPath.substring(packagePath.length() + 1);
							if(!jClassName.contains("/") && jClassName.endsWith(".class") && !jClassName.contains("$")) {
								try {
									results.add(Class.forName(packageName + "." + jClassName.substring(0, jClassName.length() - 6)));
								} catch (ClassNotFoundException e) {
								}
							}
						}
					}
				} catch (IOException e) {
				}
			}
		}
		
		return results.toArray(new Class<?>[results.size()]);
	}
}
