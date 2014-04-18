package org.ssast.minecraft.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.net.HttpURLConnection;

import org.json.JSONObject;

/**
 * This class contains methods to easy access http contents.
 * 
 * @author Chaos
 * @since SSAST Launcher 1.3.2
 */
public final class HttpFetcher {

	private static HttpURLConnection connect(String url, String method, int downloaded, int len)
			throws KeyManagementException, NoSuchAlgorithmException, IOException {
		return connect(url, method, downloaded, len, "application/x-www-form-urlencoded");
	}

	private static HttpURLConnection connect(String url, String method, int downloaded, int len, String type)
			throws KeyManagementException, NoSuchAlgorithmException,
			IOException {
		HttpURLConnection conn;
		URL console = new URL(url);
		conn = (HttpURLConnection) console.openConnection();
		conn.setRequestMethod(method);
		if(downloaded > 0)
			conn.addRequestProperty("Range", downloaded + "-");
		if(len > 0) {
			conn.addRequestProperty("Content-Type", type + "; charset=utf-8");
			conn.addRequestProperty("Content-Length", String.valueOf(len));
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
		}
		conn.connect();
		return conn;
	}

	/**
	 * Get content from the url.
	 * @param url The url
	 * @return The content. If exception occurs, <i>null</i> will be returned.
	 */
	public static String fetch(String url) {
		byte[] buffer = new byte[4096];
		ByteArrayBuilder ab = new ByteArrayBuilder();
		
		boolean failed;
		int tryCount = 0;
		int downloaded = 0;
		int length = -1;
		do {
			tryCount++;
			HttpURLConnection conn = null;
			failed = false;
			try {
				conn = connect(url, "GET", downloaded, 0);
				if(length == -1) {
					String lenStr = conn.getHeaderField("Content-Length");
					if(lenStr == null)
						length = -2;
					else
						length = Integer.valueOf(lenStr);
				}
				InputStream is = conn.getInputStream();
				DataInputStream indata = new DataInputStream(is);
				int count = 1;
				while (count >= 0) {
					count = indata.read(buffer);
					downloaded += count;
					if (count > 0) {
						ab.append(buffer, 0, count);
						if(length > 0) {
							int n = (downloaded * 80 / length) - (downloaded - count) * 80 / length;
							for(int i=0; i<n; i++)
								System.out.print(".");
						}
					}
				}
				System.out.print("\n");
				indata.close();
			} catch (Exception e) {
				failed = true;
			} finally {
				if (conn != null)
					conn.disconnect();
			}
		} while (failed && tryCount < 10);

		if(failed)
			return null;
		return new String(ab.toArray());
	}

	/**
	 * Get content from the url and save to a local file.
	 * Be sure <b>param file</b> is a file that doesn't exist.
	 * @param url The url
	 * @param file Local filename to save.
	 * @return Whether network access is available.
	 * @throws IOException if the file can't be created
	 */
	public static boolean fetchAndSave(String url, String file) throws IOException {
		byte[] buffer = new byte[4096];

		boolean failed;
		int tryCount = 0;
		int downloaded = 0;
		int length = -1;
		DataOutputStream fout = new DataOutputStream(new FileOutputStream(file));
		do {
			tryCount++;
			HttpURLConnection conn = null;
			failed = false;
			try {
				conn = connect(url, "GET", downloaded, 0);
				if(length == -1) {
					String lenStr = conn.getHeaderField("Content-Length");
					if(lenStr == null)
						length = -2;
					else
						length = Integer.valueOf(lenStr);
				}
				InputStream is = conn.getInputStream();
				DataInputStream indata = new DataInputStream(is);
				int count = 1;
				while (count >= 0) {
					count = indata.read(buffer);
					downloaded += count;
					if (count > 0) {
						fout.write(buffer, 0, count);
						if(length > 0) {
							int n = (downloaded * 80 / length) - (downloaded - count) * 80 / length;
							for(int i=0; i<n; i++)
								System.out.print(".");
						}
					}
				}
				System.out.print("\n");
				indata.close();
			} catch (Exception e) {
				failed = true;
			} finally {
				if (conn != null)
					conn.disconnect();
			}
		} while (failed && tryCount < 10);

		fout.close();
		
		if(failed) {
			new File(file).delete();
			return false;
		}
		return true;
	}
	
	/**
	 * Get content from the url, using POST method.
	 * @param url The url
	 * @param params The map contains post params
	 * @return The content. If exception occurs, <i>null</i> will be returned.
	 */
	public static String fetchUsePostMethod(String url, Map<String, String> params) {
		return fetchUsePostMethod(url, URLParam.mapToParamString(params), "application/x-www-form-urlencoded");
	}

	/**
	 * Get content from the url, using POST method.
	 * @param url The url
	 * @param json The JSON object to send
	 * @return The content. If exception occurs, <i>null</i> will be returned.
	 */
	public static String fetchUsePostMethod(String url, JSONObject json) {
		return fetchUsePostMethod(url, json.toString(), "application/json");
	}

	/**
	 * Get content from the url, using POST method.
	 * @param url The url
	 * @param params The string contains post params
	 * @return The content. If exception occurs, <i>null</i> will be returned.
	 */
	public static String fetchUsePostMethod(String url, String params) {
		return fetchUsePostMethod(url, params, "application/x-www-form-urlencoded");
	}

	/**
	 * Get content from the url, using POST method.
	 * @param url The url
	 * @param params The string contains post params
	 * @param type The param mime type
	 * @return The content. If exception occurs, <i>null</i> will be returned.
	 */
	public static String fetchUsePostMethod(String url, String params, String type) {
		byte[] buffer = new byte[4096];
		ByteArrayBuilder ab = new ByteArrayBuilder();
		
		boolean failed;
		int tryCount = 0;
		int downloaded = 0;
		do {
			tryCount++;
			HttpURLConnection conn = null;
			failed = false;
			try {
				byte[] toSend = params.getBytes("UTF-8");
				conn = connect(url, "POST", downloaded, toSend.length, type);
				
				DataOutputStream os = new DataOutputStream(conn.getOutputStream());
				os.write(toSend);
				os.flush();
				os.close();

				InputStream is = conn.getInputStream();
				DataInputStream indata = new DataInputStream(is);
				int count = 1;
				while (count >= 0) {
					count = indata.read(buffer);
					downloaded += count;
					if (count > 0) {
						ab.append(buffer, 0, count);
					}
				}
				indata.close();
			} catch (Exception e) {
				failed = true;
			} finally {
				if (conn != null)
					conn.disconnect();
			}
		} while (failed && tryCount < 10);

		if(failed)
			return null;
		return new String(ab.toArray());
	}
}