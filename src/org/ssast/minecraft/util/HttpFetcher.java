package org.ssast.minecraft.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
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

	private static HttpURLConnection createConnection(String url, String method, int downloaded, int len, String type)
			throws 	IOException {
		HttpURLConnection conn;
		URL console = new URL(url);
		conn = (HttpURLConnection) console.openConnection();
		conn.setRequestMethod(method);
		if(downloaded > 0) {
			conn.addRequestProperty("Range", downloaded + "-");
		}
		if(len > 0) {
			conn.addRequestProperty("Content-Type", type + "; charset=utf-8");
			conn.addRequestProperty("Content-Length", String.valueOf(len));
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
		}
		return conn;
	}
	
	private static void pipeStream(InputStream in, OutputStream out) throws IOException {
		pipeStream(in, out, 0, -1);
	}
	
	private static int pipeStream(InputStream in, OutputStream out, int downloaded, int length) throws IOException {
		byte[] buffer = new byte[4096];
		int count;

		while ((count = in.read(buffer)) >= 0) {
			downloaded += count;
			if (count > 0) {
				out.write(buffer, 0, count);
				if(length > 0) {
					int n = (downloaded * 80 / length) - (downloaded - count) * 80 / length;
					for(int i=0; i<n; i++)
						System.out.print(".");
				}
			}
		}
		if(length > 0) {
			System.out.print("\n");
		}
		return downloaded;
	}

	/**
	 * Get content from the url.
	 * @param url The url
	 * @return The content. If exception occurs, <i>null</i> will be returned.
	 */
	public static String fetch(String url) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		boolean failed = fetchToStream(url, out);

		if(failed)
			return null;
		return new String(out.toByteArray());
	}

	/**
	 * Get content from the url and save to a local file.
	 * Be sure <b>param file</b> is a file that doesn't exist.
	 * @param url The url
	 * @param file Local filename to save
	 * @return Whether network access is available.
	 * @throws IOException if the file can't be created
	 */
	public static boolean fetchAndSave(String url, String file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		
		boolean failed = fetchToStream(url, out);

		out.close();
		
		if(failed) {
			new File(file).delete();
			return false;
		}
		return true;
	}
	
	/**
	 * Get content from the url and output to a stream.
	 * 
	 * @param url The url
	 * @param out Stream to output
	 * @return Whether the operation succeed
	 */
	public static boolean fetchToStream(String url, OutputStream out) {
		boolean failed;
		int tryCount = 0;
		int downloaded = 0;
		int length = -1;
		do {
			tryCount++;
			HttpURLConnection conn = null;
			failed = false;
			try {
				conn = createConnection(url, "GET", downloaded, 0, "application/x-www-form-urlencoded");
				conn.connect();
				if(length == -1) {
					String lenStr = conn.getHeaderField("Content-Length");
					if(lenStr == null)
						length = -2;
					else
						length = Integer.valueOf(lenStr);
				}
				InputStream in = conn.getInputStream();
				pipeStream(in, out, downloaded, length);
				in.close();
			} catch (Exception e) {
				failed = true;
			} finally {
				if (conn != null)
					conn.disconnect();
			}
		} while (failed && tryCount < 10);
		
		return failed;
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
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		boolean failed;
		int tryCount = 0;
		do {
			tryCount++;
			HttpURLConnection conn = null;
			failed = false;
			try {
				byte[] toSend = params.getBytes("UTF-8");
				conn = createConnection(url, "POST", 0, toSend.length, type);
				conn.connect();
				
				OutputStream os = conn.getOutputStream();
				os.write(toSend);
				os.flush();
				os.close();

				InputStream in = conn.getInputStream();
				pipeStream(in, out);
				in.close();
			} catch (Exception e) {
				failed = true;
			} finally {
				if (conn != null)
					conn.disconnect();
			}
		} while (failed && tryCount < 10);

		if(failed)
			return null;
		return new String(out.toByteArray());
	}
}