package com.missouri.monitor.utils;

public class Utils {
	public static final String MONITOR_PACKAGE_NAME = "com.missouri.monitor";

	public class Memory {
		public static final String MEMORY_INFO_PATH = "/proc/meminfo";
	}

	public class Current {
		public static final String CURRENT_NOW = "/sys/class/power_supply/battery/current_now";
		public static final String BATT_CURRENT = "/sys/class/power_supply/battery/batt_current";
		public static final String SMEM_TEXT = "/sys/class/power_supply/battery/smem_text";
		public static final String BATT_CURRENT_ADC = "/sys/class/power_supply/battery/batt_current_adc";
		public static final String CURRENT_AVG = "/sys/class/power_supply/battery/current_avg";
		public static final String I_MBAT = "I_MBAT: ";
	}

	public class Network {
		public static final String PROC_UID_PATH = "/proc/uid_stat/";
		public static final String TCP_RCV = "/tcp_rcv";
		public static final String TCP_SND = "/tcp_snd";
	}
}
