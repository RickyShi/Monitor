package com.missouri.monitor.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Build;

import com.missouri.monitor.MonitorService;
import com.missouri.monitor.R;
import com.missouri.monitor.logger.Logger;

public class CpuInfo {
	private final static Logger log = Logger.getLogger(CpuInfo.class);

	private Context context;
	private long processCpu;
	private ArrayList<Long> idleCpu = new ArrayList<Long>();
	private ArrayList<Long> totalCpu = new ArrayList<Long>();
	private boolean isInitialStatics = true;
	private SimpleDateFormat formatterFile;
	private MemoryInfo mi;
	private long totalMemorySize;
	private long initialNetwork;
	private long lastestNetwork;
	private long Network;
	private NetworkInfo NetworkInfo;
	private ArrayList<String> cpuUsedRatio = new ArrayList<String>();
	private ArrayList<Long> totalCpu2 = new ArrayList<Long>();
	private long processCpu2;
	private ArrayList<Long> idleCpu2 = new ArrayList<Long>();
	private String processCpuRatio = "";
	private ArrayList<String> totalCpuRatio = new ArrayList<String>();
	private int pid;



	public CpuInfo(Context context, int pid, String uid) {
		this.pid = pid;
		this.context = context;
		NetworkInfo = new NetworkInfo(uid);
		formatterFile = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		mi = new MemoryInfo();
		totalMemorySize = mi.getTotalMemory();
		cpuUsedRatio = new ArrayList<String>();
	}

	/**
	 * read the status of CPU.
	 *
	 * @throws FileNotFoundException
	 */
	public void readCpuStat() {
		String processPid = Integer.toString(pid);
		String cpuStatPath = Utils.PROC_PATH + processPid + Utils.STAT_PATH;
		try {
			// monitor cpu stat of certain process
			RandomAccessFile processCpuInfo = new RandomAccessFile(cpuStatPath, "r");
			String line = "";
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.setLength(0);
			while ((line = processCpuInfo.readLine()) != null) {
				stringBuffer.append(line + "\n");
			}
			String[] tok = stringBuffer.toString().split(" ");
			processCpu = Long.parseLong(tok[13]) + Long.parseLong(tok[14]);
			processCpuInfo.close();
		} catch (FileNotFoundException e) {
			log.e("FileNotFoundException: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		readTotalCpuStat();
	}

	/**
	 * read stat of each CPU cores
	 */
	private void readTotalCpuStat() {
		try {
			// monitor total and idle cpu stat of certain process
			RandomAccessFile cpuInfo = new RandomAccessFile(Utils.CPU.CPU_STAT_PATH, "r");
			String line = "";
			while ((null != (line = cpuInfo.readLine())) && line.startsWith("cpu")) {
				String[] toks = line.split("\\s+");
				idleCpu.add(Long.parseLong(toks[4]));
				totalCpu.add(Long.parseLong(toks[1]) + Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
						+ Long.parseLong(toks[5]) + Long.parseLong(toks[6]) + Long.parseLong(toks[7]));
			}
			cpuInfo.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * get CPU name.
	 *
	 * @return CPU name
	 */
	public String getCpuName() {
		try {
			RandomAccessFile cpuStat = new RandomAccessFile(Utils.CPU.CPU_INFO_PATH, "r");
			// check cpu type
			if (Build.CPU_ABI.equalsIgnoreCase(Utils.CPU.CPU_X86)) {
				String line;
				while (null != (line = cpuStat.readLine())) {
					String[] values = line.split(":");
					if (values[0].contains(Utils.CPU.INTEL_CPU_NAME)) {
						cpuStat.close();
						return values[1];
					}
				}
			} else {
				String[] cpu = cpuStat.readLine().split(":"); // cpu信息的前一段是含有processor字符串，此处替换为不显示
				cpuStat.close();
				return cpu[1];
			}
		} catch (IOException e) {
			log.e("IOException: " + e.getMessage());
		}
		return "";
	}

	/**
	 * display directories naming with "cpu*"
	 *
	 * @author andrewleo
	 */
	class CpuFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			// Check if filename matchs "cpu[0-9]"
			if (Pattern.matches("cpu[0-9]", pathname.getName())) {
				return true;
			}
			return false;
		}
	}

	/**
	 * get CPU core numbers
	 *
	 * @return cpu core numbers
	 */
	public int getCpuNum() {
		try {
			// Get directory containing CPU info
			File dir = new File(Utils.CPU.CPU_DIR_PATH);
			// Filter to only list the devices we care about
			File[] files = dir.listFiles(new CpuFilter());
			return files.length;
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}

	/**
	 * get CPU core list (name)
	 *
	 * @return cpu core list (name)
	 */
	public ArrayList<String> getCpuList() {
		ArrayList<String> cpuList = new ArrayList<String>();
		try {
			// Get directory containing CPU info
			File dir = new File(Utils.CPU.CPU_DIR_PATH);
			// Filter to only list the devices we care about
			File[] files = dir.listFiles(new CpuFilter());
			for (int i = 0; i < files.length; i++) {
				cpuList.add(files[i].getName());
			}
			return cpuList;
		} catch (Exception e) {
			e.printStackTrace();
			cpuList.add("cpu0");
			return cpuList;
		}
	}

	/**
	 * reserve used ratio of process CPU and total CPU, meanwhile collect
	 * network Network.
	 *
	 * @return network Network ,used ratio of process CPU and total CPU in
	 *         certain interval
	 */
	public ArrayList<String> getCpuRatioInfo(String totalBatt, String currentBatt, String temperature, String voltage) {

		DecimalFormat format = new DecimalFormat();
		format.setGroupingUsed(false);
		format.setMaximumFractionDigits(2);
		format.setMinimumFractionDigits(2);

		cpuUsedRatio.clear();
		idleCpu.clear();
		totalCpu.clear();
		totalCpuRatio.clear();
		readCpuStat();

		try {
			String mDateTime2;
			Calendar cal = Calendar.getInstance();
			if ((Build.MODEL.equals("sdk")) || (Build.MODEL.equals("google_sdk"))) {
				mDateTime2 = formatterFile.format(cal.getTime().getTime() + 8 * 60 * 60 * 1000);
				totalBatt = Utils.NA;
				currentBatt = Utils.NA;
				temperature = Utils.NA;
				voltage = Utils.NA;
			} else {
				mDateTime2 = formatterFile.format(cal.getTime().getTime());
			}
			if (isInitialStatics) {
				initialNetwork = NetworkInfo.getNetworkInfo();
				isInitialStatics = false;
			} else {
				lastestNetwork = NetworkInfo.getNetworkInfo();
				if (initialNetwork == -1) {
					Network = -1;
				} else {
					// byte to kb
					Network = (lastestNetwork - initialNetwork + 1023) / 1024;
				}
				StringBuffer totalCpuBuffer = new StringBuffer();
				if (null != totalCpu2 && totalCpu2.size() > 0) {
					processCpuRatio = format.format(100 * ((double) (processCpu - processCpu2) / ((double) (totalCpu.get(0) - totalCpu2.get(0)))));
					for (int i = 0; i < (totalCpu.size() > totalCpu2.size() ? totalCpu2.size() : totalCpu.size()); i++) {
						String cpuRatio = "0.00";
						if (totalCpu.get(i) - totalCpu2.get(i) > 0) {
							cpuRatio = format
									.format(100 * ((double) ((totalCpu.get(i) - idleCpu.get(i)) - (totalCpu2.get(i) - idleCpu2.get(i))) / (double) (totalCpu
											.get(i) - totalCpu2.get(i))));
						}
						totalCpuRatio.add(cpuRatio);
						totalCpuBuffer.append(cpuRatio + Utils.COMMA);
					}
				} else {
					processCpuRatio = "0";
					totalCpuRatio.add("0");
					totalCpuBuffer.append("0,");
					totalCpu2 = (ArrayList<Long>) totalCpu.clone();
					processCpu2 = processCpu;
					idleCpu2 = (ArrayList<Long>) idleCpu.clone();
				}
				// 多核cpu的值写入csv文件中
				for (int i = 0; i < getCpuNum() - totalCpuRatio.size() + 1; i++) {
					totalCpuBuffer.append("0.00,");
				}
				long pidMemory = mi.getPidMemorySize(pid, context);
				String pMemory = format.format((double) pidMemory / 1024);
				long freeMemory = mi.getFreeMemorySize(context);
				String fMemory = format.format((double) freeMemory / 1024);
				String percent = context.getString(R.string.stat_error);
				if (totalMemorySize != 0) {
					percent = format.format(((double) pidMemory / (double) totalMemorySize) * 100);
				}

				if (isPositive(processCpuRatio) && isPositive(totalCpuRatio.get(0))) {
					String trafValue;
					// whether certain device supports Network statics or not
					if (Network == -1) {
						trafValue = Utils.NA;
					} else {
						trafValue = String.valueOf(Network);
					}
					MonitorService.bw.write(mDateTime2 + Utils.COMMA + pMemory + Utils.COMMA + percent + Utils.COMMA + fMemory + Utils.COMMA + processCpuRatio + Utils.COMMA
							+ totalCpuBuffer.toString() + trafValue + Utils.COMMA + totalBatt + Utils.COMMA + currentBatt + Utils.COMMA + temperature + Utils.COMMA + voltage
							+ "\r\n");
					totalCpu2 = (ArrayList<Long>) totalCpu.clone();
					processCpu2 = processCpu;
					idleCpu2 = (ArrayList<Long>) idleCpu.clone();
					cpuUsedRatio.add(processCpuRatio);
					cpuUsedRatio.add(totalCpuRatio.get(0));
					cpuUsedRatio.add(String.valueOf(Network));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cpuUsedRatio;
	}

	/**
	 * is text a positive number
	 *
	 * @param text
	 * @return
	 */
	private boolean isPositive(String text) {
		Double num;
		try {
			num = Double.parseDouble(text);
		} catch (NumberFormatException e) {
			return false;
		}
		return num >= 0;
	}
}
