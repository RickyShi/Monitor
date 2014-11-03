package com.missouri.monitor.utils;

import android.graphics.drawable.Drawable;

/**
 * details of installed processes ,including
 * icon,packagename,pid,uid,processname
 *
 * @author Ricky
 *
 */
public class Programs implements Comparable<Programs> {
	// private final static Logger log = Logger.getLogger(Programs.class);
	private Drawable icon;
	private String processName;
	private String packageName;
	private int pid;
	private int uid;

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {

		this.processName = processName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

		@Override
	public int compareTo(Programs another) {
		// TODO Auto-generated method stub
		return (this.getProcessName().compareTo(another.getProcessName()));
		}
}
