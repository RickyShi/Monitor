package com.missouri.monitor.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.missouri.monitor.logger.Logger;

/**
 * get information of running processes
 * 
 * @author Ricky
 */
public class ProcessInfo {

	private final static Logger log = Logger.getLogger(ProcessInfo.class);



	/**
	 * get information of all running processes,including package name ,process
	 * name ,icon ,pid and uid.
	 *
	 * @param context
	 * @return running processes list
	 */
	public List<Programs> getRunningProcess(Context context) {
		log.i("get running processes");

		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> run = am.getRunningAppProcesses();
		PackageManager pm = context.getPackageManager();
		List<Programs> progressList = new ArrayList<Programs>();

		for (ApplicationInfo appinfo : getPackagesInfo(context)) {
			Programs Programs = new Programs();
			if (((appinfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) || ((appinfo.processName != null) && (appinfo.processName.equals(Utils.MONITOR_PACKAGE_NAME)))) {
				continue;
			}
			for (RunningAppProcessInfo runningProcess : run) {
				if ((runningProcess.processName != null) && runningProcess.processName.equals(appinfo.processName)) {
					Programs.setPid(runningProcess.pid);
					Programs.setUid(runningProcess.uid);
					break;
				}
			}
			Programs.setPackageName(appinfo.processName);
			Programs.setProcessName(appinfo.loadLabel(pm).toString());
			Programs.setIcon(appinfo.loadIcon(pm));
			progressList.add(Programs);
		}
		Collections.sort(progressList);
		return progressList;
	}

	/**
	 * get information of all applications.
	 *
	 * @param context
	 * @return packages information of all applications
	 */
	private List<ApplicationInfo> getPackagesInfo(Context context) {
		PackageManager pm = context.getApplicationContext().getPackageManager();
		List<ApplicationInfo> appList = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		return appList;
	}
}
