package com.missouri.monitor.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;

import com.missouri.monitor.logger.Logger;

public class MemoryInfo {

	private final static Logger log = Logger.getLogger(MemoryInfo.class);

	/**
	 * get total memory of certain device.
	 *
	 * @return total memory of device
	 */
	public long getTotalMemory() {
		String readTemp = "";
		String memTotal = "";
		long memory = 0;
		try {
			FileReader fr = new FileReader(Utils.Memory.MEMORY_INFO_PATH);
			BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
			while ((readTemp = localBufferedReader.readLine()) != null) {
				// file looks like
				// MemTotal: 94096 kB
				if (readTemp.contains("MemTotal")) {
					String[] total = readTemp.split(":");
					memTotal = total[1].trim();
				}
			}
			localBufferedReader.close();
			String[] memKb = memTotal.split(" ");
			memTotal = memKb[0].trim();
			log.d("memTotal: " + memTotal);
			memory = Long.parseLong(memTotal);
		} catch (IOException e) {
			log.e("IOException: " + e.getMessage());
		}
		return memory;
	}

	/**
	 * get free memory.(MB)
	 *
	 * @return free memory of device
	 *
	 */
	public long getFreeMemorySize(Context context) {
		ActivityManager.MemoryInfo freeMemInfo = new ActivityManager.MemoryInfo();
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		am.getMemoryInfo(freeMemInfo);
		long avaliMem = freeMemInfo.availMem;
		log.d("freeMem: " + avaliMem);
		return avaliMem / 1024;
	}

	/**
	 * get the memory of process with certain pid.
	 *
	 * @param pid
	 *            pid of process
	 * @param context
	 *            context of certain activity
	 * @return memory usage of certain process
	 */
	public int getPidMemorySize(int pid, Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		int[] myMempid = new int[] { pid };
		Debug.MemoryInfo[] memoryInfo = am.getProcessMemoryInfo(myMempid);
		memoryInfo[0].getTotalSharedDirty();
		int memSize = memoryInfo[0].getTotalPss();
		log.d("pidMem: " + memSize);
		return memSize;
	}

}
