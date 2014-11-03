package com.missouri.monitor.utils;

public class Utils {
	public static final String MONITOR_PACKAGE_NAME = "com.missouri.monitor";
	public static final String COMMA = ",";

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
		public static final String NA = "N/A";
	}
}
