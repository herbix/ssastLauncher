package org.ssast.minecraft.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class EasyFileAccess {
	
	public static String loadFile(String path) {

		StringBuilder sb = new StringBuilder();
		
		BufferedReader in = null;
	
		try {
			in = new BufferedReader(new FileReader(path));
			String line;
			while((line = in.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
		} catch(Exception e) {
			return null;
		} finally {
			try {
				if(in != null)
					in.close();
			} catch (IOException e) {
			}
		}
		
		return sb.toString();
	}
	
	public static boolean saveFile(String path, String content) {
		
		BufferedWriter out = null;
		
		try {
			out = new BufferedWriter(new FileWriter(path));
			out.write(content);
		} catch(Exception e) {
			return false;
		} finally {
			try {
				if(out != null)
					out.close();
			} catch (IOException e) {
			}
		}

		return true;
	}

	public static boolean deleteFileForce(File file) {
		boolean result = true;
		if(file.isDirectory()) {
			for(File f : file.listFiles()) {
				if(!deleteFileForce(f))
					result = false;
			}
		}
		if(file.exists())
			result &= file.delete();
		return result;
	}
}
