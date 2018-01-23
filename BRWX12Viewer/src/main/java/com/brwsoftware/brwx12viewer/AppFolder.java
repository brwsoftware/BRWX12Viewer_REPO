package com.brwsoftware.brwx12viewer;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.ShlObj;
import com.sun.jna.platform.win32.W32Errors;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT.HRESULT;

public class AppFolder {

	public static String getAppFolder(String appName, String appVersion, String appAuthor) {
	    String theFolder;
	    String os = System.getProperty("os.name").toLowerCase();
	    if (os.startsWith("mac os x")) {
	    	theFolder = buildPath(home(), "/Library/Application Support", appName, appVersion);
	    } else if (os.startsWith("windows")) {
	    	theFolder = buildPath(windowsAppData(), appAuthor == null ? appName : appAuthor, appName, appVersion);
	    } else {
	        String dir = System.getProperty("XDG_DATA_HOME", buildPath(home(), "/.local/share"));
	        theFolder = buildPath(dir, appName, appVersion);
	    }
	    return theFolder;
	}

	protected static String home() {
		return System.getProperty("user.home");
	}

	protected static String buildPath(String... elems) {
		String separator = System.getProperty("file.separator");
		StringBuilder buffer = new StringBuilder();
		String lastElem = null;
		for (String elem : elems) {
			if (elem == null) {
				continue;
			}

			if (lastElem == null) {
				buffer.append(elem);
			} else if (lastElem.endsWith(separator)) {
				buffer.append(elem.startsWith(separator) ? elem.substring(1) : elem);
			} else {
				if (!elem.startsWith(separator)) {
					buffer.append(separator);
				}
				buffer.append(elem);
			}
			lastElem = elem;
		}
		return buffer.toString();
	}

	protected static String windowsAppData() {
		char[] pszPath = new char[WinDef.MAX_PATH];
		HRESULT result = Shell32.INSTANCE.SHGetFolderPath(null, ShlObj.CSIDL_APPDATA, null, null, pszPath);
		if (W32Errors.S_OK.equals(result)) {
			return Native.toString(pszPath);
		}
		throw new RuntimeException("SHGetFolderPath returns an error: " + result.intValue());
	}
}
