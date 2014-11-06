package com.missouri.monitor.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utils {
	public static final String MONITOR_PACKAGE_NAME = "com.missouri.monitor";

	public static final String ACTION_INTENT_BATTERY_CHANGED = "android.intent.action.BATTERY_CHANGED";

	public static final String COMMA = ",";
	public static final String NA = "N/A";
	public static final String BLANK_STRING = "";

	public static final String PROC_PATH = "/proc/";
	public static final String STAT_PATH = "/stat";
	public static final String FILE_PATH = "sdcard/monitor/";

	public static final int TIMEOUT = 20000;
	// default MONITOR_INTERVAL is set to 5s
	public static final int MONITOR_INTERVAL = 5000;

	public static final String UPLOAD_ADDRESS = "http://dslsrv8.cs.missouri.edu/~rs79c/monitor/writeArrayToFile.php";

	public class Memory {
		public static final String MEMORY_INFO_PATH = "/proc/meminfo";
	}

	public class Current {
		public static final String CURRENT_NOW_PATH = "/sys/class/power_supply/battery/current_now";
		public static final String BATT_CURRENT_PATH = "/sys/class/power_supply/battery/batt_current";
		public static final String SMEM_TEXT_PATH = "/sys/class/power_supply/battery/smem_text";
		public static final String BATT_CURRENT_ADC_PATH = "/sys/class/power_supply/battery/batt_current_adc";
		public static final String CURRENT_AVG_PATH = "/sys/class/power_supply/battery/current_avg";
		public static final String I_MBAT = "I_MBAT: ";
	}

	public class Network {
		public static final String PROC_UID_PATH = "/proc/uid_stat/";
		public static final String TCP_RCV_PATH = "/tcp_rcv";
		public static final String TCP_SND_PATH = "/tcp_snd";
	}

	public class CPU {
		public static final String INTEL_CPU_NAME = "model name";
		public static final String CPU_DIR_PATH = "/sys/devices/system/cpu/";
		public static final String CPU_X86 = "x86";
		public static final String CPU_INFO_PATH = "/proc/cpuinfo";
		public static final String CPU_STAT_PATH = "/proc/stat";
	}

	/**
	 * get the sdk version of phone.
	 *
	 * @return sdk version
	 */
	public static String getSDKVersion() {
		return android.os.Build.VERSION.RELEASE;
	}

	/**
	 * get phone type.
	 *
	 * @return phone type
	 */
	public static String getPhoneType() {
		return android.os.Build.MODEL;
	}

	public static boolean checkDataConnectivity(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static void writeToFile(String fileName, String toWrite) throws IOException {
		File dir = new File(FILE_PATH);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File f = new File(FILE_PATH, fileName);
		FileWriter fw = new FileWriter(f, true);
		fw.write(toWrite + '\n');
		fw.flush();
		fw.close();
		f = null;
	}

}
