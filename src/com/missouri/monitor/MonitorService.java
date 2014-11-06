package com.missouri.monitor;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.missouri.monitor.logger.Logger;
import com.missouri.monitor.utils.CpuInfo;
import com.missouri.monitor.utils.CurrentInfo;
import com.missouri.monitor.utils.MemoryInfo;
import com.missouri.monitor.utils.Utils;

/**
 * Service running in background
 *
 * @author Ricky
 */
public class MonitorService extends Service {

	private final static Logger log = Logger.getLogger(MonitorService.class);

	private WindowManager windowManager = null;
	private WindowManager.LayoutParams wmParams = null;
	private View viFloatingWindow;
	private DecimalFormat fomart;
	private MemoryInfo memoryInfo;
	private Handler handler = new Handler();
	private CpuInfo cpuInfo;
	private boolean isFloating;
	private String processName, packageName, startActivity;
	private int pid, uid;
	private boolean isServiceStop = false;

	public static boolean isStop = false;
	public static String fileName = "default";

	private String totalBatt;
	private String temperature;
	private String voltage;
	private CurrentInfo currentInfo;
	private BatteryInfoBroadcastReceiver batteryBroadcast = null;

	// get start time
	private static final int MAX_START_TIME_COUNT = 5;
	private static final String START_TIME = "#startTime";
	private int getStartTimeCount = 0;
	private boolean isGetStartTime = true;
	private String startTime = "";
	public static final String SERVICE_ACTION = "com.missouri.action.monitorService";

	@Override
	public void onCreate() {
		log.i("service onCreate");
		super.onCreate();
		isServiceStop = false;
		isStop = false;
		memoryInfo = new MemoryInfo();
		fomart = new DecimalFormat();
		fomart.setMaximumFractionDigits(2);
		fomart.setMinimumFractionDigits(0);
		currentInfo = new CurrentInfo();
		batteryBroadcast = new BatteryInfoBroadcastReceiver();
		registerReceiver(batteryBroadcast, new IntentFilter(Utils.ACTION_INTENT_BATTERY_CHANGED));
	}

	/**
	 * Battery Broadcast
	 *
	 * @see google online doc Example
	 *
	 * @author Ricky
	 *
	 */
	public class BatteryInfoBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
				int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				totalBatt = String.valueOf(level * 100 / scale);
				voltage = String.valueOf(intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) * 1.0 / 1000);
				temperature = String.valueOf(intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) * 1.0 / 10);
				log.d("BatteryStat: " + totalBatt + "," + voltage + "," + temperature);
			}

		}

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		log.i("service onStart");
		PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(this, MainActivity.class), 0);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentIntent(contentIntent).setSmallIcon(R.drawable.ic_launcher).setWhen(System.currentTimeMillis()).setAutoCancel(true)
				.setContentTitle("Monitor").setContentText(getString(R.string.noti_text));
		startForeground(startId, builder.build());

		pid = intent.getExtras().getInt("pid");
		uid = intent.getExtras().getInt("uid");
		processName = intent.getExtras().getString("processName");
		packageName = intent.getExtras().getString("packageName");
		startActivity = intent.getExtras().getString("startActivity");

		cpuInfo = new CpuInfo(getBaseContext(), pid, Integer.toString(uid));
		createResultCsv();
		handler.postDelayed(monitorTask, 1000);
		return START_NOT_STICKY;
	}

	/**
	 * write the test result to csv format report.
	 */
	private void createResultCsv() {
		log.d("createResultCsv() is started");
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String mDateTime;
		if ((Build.MODEL.equals("sdk")) || (Build.MODEL.equals("google_sdk"))) {
			mDateTime = formatter.format(cal.getTime().getTime() + 8 * 60 * 60 * 1000);
		} else {
			mDateTime = formatter.format(cal.getTime().getTime());
		}
		fileName = processName + "_" + mDateTime + ".txt";
		long totalMemorySize = memoryInfo.getTotalMemory();
		String totalMemory = fomart.format((double) totalMemorySize / 1024);
		String multiCpuTitle = Utils.BLANK_STRING;
		// titles of multiple cpu cores
		ArrayList<String> cpuList = cpuInfo.getCpuList();
		for (int i = 0; i < cpuList.size(); i++) {
			multiCpuTitle += "," + cpuList.get(i) + getString(R.string.total_usage);
		}
		String overallStat = getString(R.string.process_package) + ": " + packageName + "\r\n" + getString(R.string.process_name) + ": " + processName
				+ "\r\n" + getString(R.string.process_pid) + ": " + pid + "\r\n" + getString(R.string.mem_size) + "： ," + totalMemory + "MB\r\n"
				+ getString(R.string.cpu_type) + ": " + cpuInfo.getCpuName() + "\r\n" + getString(R.string.android_system_version) + ": "
				+ Utils.getSDKVersion() + "\r\n" + getString(R.string.mobile_type) + ": " + Utils.getPhoneType() + "\r\n" + "UID"
				+ ": " + uid + "\r\n";
		String title = getString(R.string.timestamp) + "," + getString(R.string.traffic) + ","
				+ getString(R.string.battery) + "," + getString(R.string.current) + ","
				+ getString(R.string.temperature) + "," + getString(R.string.voltage) + ","
				+ getString(R.string.used_mem_PSS) + "," + getString(R.string.used_mem_ratio) + ","
				+ getString(R.string.mobile_free_mem) + "," + getString(R.string.app_used_cpu_ratio) + ","
				+ getString(R.string.total_used_cpu_ratio)
				+ multiCpuTitle + "\r\n";
		try {
			Utils.writeToFile(fileName, overallStat + title);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private Runnable monitorTask = new Runnable() {

		@Override
		public void run() {
			if (!isServiceStop) {
				dataRefresh();
				handler.postDelayed(this, Utils.MONITOR_INTERVAL);
				// get app start time from logcat on every task running
				getStartTimeFromLogcat();
			} else {
				Intent intent = new Intent();
				intent.putExtra("isServiceStop", true);
				intent.setAction(SERVICE_ACTION);
				sendBroadcast(intent);
				stopSelf();
			}
		}
	};

	/**
	 * Try to get start time from logcat.
	 */
	private void getStartTimeFromLogcat() {
		if (!isGetStartTime || getStartTimeCount >= MAX_START_TIME_COUNT) {
			return;
		}
		try {
			// filter logcat by Tag:ActivityManager and Level:Info
			String logcatCommand = "logcat -v time -d ActivityManager:I *:S";
			Process process = Runtime.getRuntime().exec(logcatCommand);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder strBuilder = new StringBuilder();
			String line = Utils.BLANK_STRING;

			while ((line = bufferedReader.readLine()) != null) {
				strBuilder.append(line);
				strBuilder.append("\r\n");
				String regex = ".*Displayed.*" + startActivity + ".*\\+(.*)ms.*";
				if (line.matches(regex)) {
					Log.w("my logs", line);
					if (line.contains("total")) {
						line = line.substring(0, line.indexOf("total"));
					}
					startTime = line.substring(line.lastIndexOf("+") + 1, line.lastIndexOf("ms") + 2);
					Toast.makeText(MonitorService.this, getString(R.string.start_time) + startTime, Toast.LENGTH_LONG).show();
					isGetStartTime = false;
					break;
				}
			}
			getStartTimeCount++;
		} catch (IOException e) {
			log.d(e.getMessage());
		}
	}

	/**
	 * refresh the performance data
	 *
	 * @throws FileNotFoundException
	 *
	 * @throws IOException
	 */
	private void dataRefresh() {
		String currentBatt = String.valueOf(currentInfo.getCurrentValue());
		// 异常数据过滤
		// try {
		// if (Math.abs(Double.parseDouble(currentBatt)) >= 500) {
		// currentBatt = "N/A";
		// }
		// } catch (Exception e) {
		// currentBatt = "N/A";
		// }
		cpuInfo.getCpuRatioInfo(totalBatt, currentBatt, temperature, voltage);
	}

	/**
	 * Above JellyBean, we cannot grant READ_LOGS permission...
	 *
	 * @return
	 */
	private boolean isGrantedReadLogsPermission() {
		int permissionState = getPackageManager().checkPermission(android.Manifest.permission.READ_LOGS, getPackageName());
		return permissionState == PackageManager.PERMISSION_GRANTED;
	}


	/**
	 * close all opened stream.
	 */
	public void closeOpenedStream() {
		String comments = getString(R.string.comment1) + "\r\n" + "\r\n" + getString(R.string.comment2) + "\r\n";
		try {
			Utils.writeToFile(fileName, comments);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void onDestroy() {
		log.i("service onDestroy");
		if (windowManager != null) {
			windowManager.removeView(viFloatingWindow);
		}
		handler.removeCallbacks(monitorTask);
		closeOpenedStream();
		// replace the start time in file
		// if (isGrantedReadLogsPermission()) {
		// if (!Utils.BLANK_STRING.equals(startTime)) {
		// replaceFileString(resultFilePath, START_TIME,
		// getString(R.string.start_time) + startTime + "\r\n");
		// } else {
		// replaceFileString(resultFilePath, START_TIME, Utils.BLANK_STRING);
		// }
		// }
		isStop = true;
		unregisterReceiver(batteryBroadcast);
		super.onDestroy();
		stopForeground(true);
	}

	// /**
	// * Replaces all matches for replaceType within this replaceString in file
	// on
	// * the filePath
	// *
	// * @param filePath
	// * @param replaceType
	// * @param replaceString
	// */
	// private void replaceFileString(String filePath, String replaceType,
	// String replaceString) {
	// try {
	// File file = new File(filePath);
	// BufferedReader reader = new BufferedReader(new FileReader(file));
	// String line = Utils.BLANK_STRING;
	// String oldtext = Utils.BLANK_STRING;
	// while ((line = reader.readLine()) != null) {
	// oldtext += line + "\r\n";
	// }
	// reader.close();
	// // replace a word in a file
	// String newtext = oldtext.replaceAll(replaceType, replaceString);
	// BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new
	// FileOutputStream(filePath), "UTF-8"));
	// writer.write(newtext);
	// writer.close();
	// } catch (IOException e) {
	// log.d(e.getMessage());
	// }
	// }

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
