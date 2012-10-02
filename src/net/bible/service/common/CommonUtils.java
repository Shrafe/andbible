package net.bible.service.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import net.bible.android.BibleApplication;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.crosswire.common.util.IOUtil;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.util.Log;

public class CommonUtils {

	private static final int DEFAULT_MAX_TEXT_LENGTH = 250;
	private static final String ELLIPSIS = "...";

	private static final String TAG = "CommonUtils"; 
	static private boolean isAndroid = false;
	
	//todo have to finish implementing switchable logging here
	static {
		try {
	        if (android.os.Build.ID != null) {
	            isAndroid = true;
	        }
		} catch (Exception cnfe) {
			isAndroid = false;
		}
		System.out.println("isAndroid:"+isAndroid);
	}

	public static boolean isAndroid() {
		return isAndroid;
	}

	public static String getApplicationVersionName() {
		String versionName = null;
		try
        {
            PackageManager manager = BibleApplication.getApplication().getPackageManager();
            PackageInfo info = manager.getPackageInfo(BibleApplication.getApplication().getPackageName(), 0);
            versionName = info.versionName;
        }
        catch ( final NameNotFoundException e )
        {
            Log.e(TAG, "Error getting package name.", e);
            versionName = "Error";
        }
        return versionName;
	}
	public static int getApplicationVersionNumber() {
		int versionNumber;
		try
        {
            PackageManager manager = BibleApplication.getApplication().getPackageManager();
            PackageInfo info = manager.getPackageInfo(BibleApplication.getApplication().getPackageName(), 0);
            versionNumber = info.versionCode;
        }
        catch ( final NameNotFoundException e )
        {
            Log.e(TAG, "Error getting package name.", e);
            versionNumber = -1;
        }
        return versionNumber;
	}
	
	public static boolean isFroyoPlus() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	}

	public static boolean isGingerBreadPlus() {
		return Build.VERSION.SDK_INT >= 9;
	}

	public static boolean isIceCreamSandwichPlus() {
		return Build.VERSION.SDK_INT >= 14;
	}
	public static boolean isJellyBeanPlus() {
		return Build.VERSION.SDK_INT >= 16;
	}

	public static long getSDCardMegsFree() {
		long bytesAvailable = getFreeSpace(Environment.getExternalStorageDirectory().getPath());
		long megAvailable = bytesAvailable / 1048576;
		Log.d(TAG, "Megs available on SD card :"+megAvailable);
		return megAvailable;
	}
	public static long getFreeSpace(String path) {
		StatFs stat = new StatFs(path);
		long bytesAvailable = (long)stat.getBlockSize() * (long)stat.getAvailableBlocks();
		Log.d(TAG, "Free space :"+bytesAvailable);
		return bytesAvailable;
	}
	
	/** shorten text for display in lists etc.
	 * 
	 * @param text
	 * @return
	 */
	public static String limitTextLength(String text) {
		return limitTextLength(text, DEFAULT_MAX_TEXT_LENGTH);
	}
	public static String limitTextLength(String text, int maxLength) {
		return limitTextLength(text, maxLength, false);
	}
	public static String limitTextLength(String text, int maxLength, boolean singleLine) {
		if (text!=null) {
			int origLength = text.length();
			
			if (singleLine) {
				// get first line but limit length in case there are no line breaks
				text = StringUtils.substringBefore(text,"\n");
			}
			
			if (text.length()>maxLength) {
				// break on a space rather than mid-word
				int cutPoint = text.indexOf(" ", maxLength);
				if (cutPoint >= maxLength) {
					text = text.substring(0, cutPoint+1);
				}
			}
			
			if (text.length() != origLength) {
				text += ELLIPSIS;
			}
		}
		return text;
	}
	
    public static boolean isInternetAvailable() {
    	String testUrl = "http://www.crosswire.org/ftpmirror/pub/sword/packages/rawzip/";
    	return CommonUtils.isHttpUrlAvailable(testUrl);
    }

    public static boolean isHttpUrlAvailable(String urlString) {
 	    HttpURLConnection connection = null;
    	try {
    		// might as well test for the url we need to access
	 	    URL url = new URL(urlString);
	 	         
	 	    Log.d(TAG, "Opening test connection");
	 	    connection = (HttpURLConnection)url.openConnection();
	 	    connection.setConnectTimeout(3000);
	 	    Log.d(TAG, "Connecting to test internet connection");
	 	    connection.connect();
	 	    boolean success = (connection.getResponseCode() == HttpURLConnection.HTTP_OK);
	 	    Log.d(TAG, "Url test result for:"+urlString+" is "+success);
	 	    return success;
    	} catch (IOException e) {
    		Log.i(TAG, "No internet connection");
    		return false;
    	} finally {
    		if (connection!=null) {
    			connection.disconnect();
    		}
    	}
    }

	public static void ensureDirExists(File dir) {
		if (!dir.exists() || !dir.isDirectory()) {
			dir.mkdirs();
		}
	}

	public static boolean deleteDirectory(File path) {
		Log.d(TAG, "Deleting directory:"+path.getAbsolutePath());
		if (path.exists()) {
			if (path.isDirectory()) {
				File[] files = path.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						deleteDirectory(files[i]);
					} else {
						files[i].delete();
						Log.d(TAG, "Deleted "+files[i]);
					}
				}
			}
			boolean deleted = path.delete();
			if (!deleted) {
				Log.w(TAG, "Failed to delete:"+path.getAbsolutePath());
			}
			return deleted;
		}
		return false;
	}

	public static Properties loadProperties(File propertiesFile) {
		Properties properties = new Properties();
		if (propertiesFile.exists()) {
			FileInputStream in = null;
			try {
            	in = new FileInputStream(propertiesFile);
            	properties.load(in);
			} catch (Exception e) {
				Log.e(TAG, "Error loading properties", e);
			} finally {
            	IOUtil.close(in);
			}
		}
		return properties;
	}
	
    public static void pause(int seconds) {
    	try {
    		Thread.sleep(seconds*1000);
    	} catch (Exception e) {
    		Log.e(TAG, "error sleeping", e);
    	}
    }
    
    public static boolean isPortrait() {
    	return BibleApplication.getApplication().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public static String getLocalePref() {
    	return getSharedPreferences().getString("locale_pref", "");
    }
    
	/** get preferences used by User Prefs screen
	 * 
	 * @return
	 */
	public static SharedPreferences getSharedPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(BibleApplication.getApplication().getApplicationContext());
	}
	
	public static String getResourceString(int resourceId) {
		return BibleApplication.getApplication().getResources().getString(resourceId);
	}

	public static int getResourceInteger(int resourceId) {
		return BibleApplication.getApplication().getResources().getInteger(resourceId);
	}
	
	/**
	 * convert dip measurements to pixels
	 */
	public static int convertDipsToPx(int dips) {
		// Converts 14 dip into its equivalent px
		float scale = BibleApplication.getApplication().getResources().getDisplayMetrics().density;
		return (int) ( dips * scale + 0.5f );
	}
	
	/**
	 * convert dip measurements to pixels
	 */
	public static int convertPxToDips(int px) {
		float scale = BibleApplication.getApplication().getResources().getDisplayMetrics().density;
		return Math.round(px/scale);
	}

	/**
	 * StringUtils methods only compare with a single char and hence create lots
	 * of temporary Strings This method compares with all chars and just creates
	 * one new string for each original string. This is to minimise memory
	 * overhead & gc.
	 * 
	 * @param str
	 * @param removeChars
	 * @return
	 */
	public static String remove(String str, char[] removeChars) {
		if (StringUtils.isEmpty(str)
				|| !StringUtils.containsAny(str, removeChars)) {
			return str;
		}

		StringBuilder r = new StringBuilder(str.length());
		// for all chars in string
		for (int i = 0; i < str.length(); i++) {
			char strCur = str.charAt(i);

			// compare with all chars to be removed
			boolean matched = false;
			for (int j = 0; j < removeChars.length && !matched; j++) {
				if (removeChars[j] == strCur) {
					matched = true;
				}
			}
			// if current char does not match any in the list then add it to the
			if (!matched) {
				r.append(strCur);
			}
		}
		return r.toString();
	}
	
	public static Date getTruncatedDate() {
		return DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
	}
}
