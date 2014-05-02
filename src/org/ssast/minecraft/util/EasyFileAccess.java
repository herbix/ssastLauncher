package org.ssast.minecraft.util;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class EasyFileAccess {
	
	public static String loadFile(String path) {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		InputStream in = null;
	
		try {
			in = new FileInputStream(path);
			int n;
			byte[] buffer = new byte[65536];
			while((n = in.read(buffer)) >= 0) {
				out.write(buffer, 0, n);
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
		
		return new String(out.toByteArray());
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

	public static boolean copyFile(File file, File targetFile) {
		try {
			FileInputStream in = new FileInputStream(file);
			targetFile.getParentFile().mkdirs();
			FileOutputStream out = new FileOutputStream(targetFile);

			int len = 0;
			byte[] buffer = new byte[65536];
			while((len = in.read(buffer)) >= 0) {
				out.write(buffer, 0, len);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static String getDigest(File file, String algorithm, int hashLength) {
		DigestInputStream stream = null;
		try {
			stream = new DigestInputStream(new FileInputStream(file),
					MessageDigest.getInstance(algorithm));
			byte[] buffer = new byte[65536];
			int read;
			do {
				read = stream.read(buffer);
			} while (read > 0);
		} catch (Exception ignored) {
			return null;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}

		return String.format("%1$0" + hashLength + "x", new BigInteger(1, stream.getMessageDigest().digest()));
	}
	
	public static boolean doSha1Checksum(String shaFilePath, String checkedFilePath) {
		File checkedFile = new File(checkedFilePath);
		if(!checkedFile.isFile()) {
			return false;
		}
		String checksum = loadFile(shaFilePath);
		if(checksum == null) {
			return true;
		}
		String checksum2 = getDigest(checkedFile, "SHA-1", 40);
		return checksum.equalsIgnoreCase(checksum2);
	}
}
