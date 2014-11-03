package com.missouri.monitor.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Locale;

import android.os.Build;

import com.missouri.monitor.logger.Logger;

public class CurrentInfo {
	private final static Logger log = Logger.getLogger(CurrentInfo.class);

	private static final String BUILD_MODEL = Build.MODEL.toLowerCase(Locale.ENGLISH);


	/**
	 * read system file to get current value
	 *
	 * @return current value
	 */
	public Long getCurrentValue() {
		File f = null;
		log.d(BUILD_MODEL);
		// galaxy s4,oppo find,samgsung note2
		if (BUILD_MODEL.contains("sgh-i337") || BUILD_MODEL.contains("gt-i9505") || BUILD_MODEL.contains("sch-i545")
				|| BUILD_MODEL.contains("find 5") || BUILD_MODEL.contains("sgh-m919") || BUILD_MODEL.contains("sgh-i537")
				|| BUILD_MODEL.contains("x907") || BUILD_MODEL.contains("gt-n7100")) {
			f = new File(Utils.Current.CURRENT_NOW_PATH);
			if (f.exists()) {
				return getCurrentValue(f, false);
			}
		}

		// samsung galaxy
		if (BUILD_MODEL.contains("gt-p31") || BUILD_MODEL.contains("gt-p51")) {
			f = new File(Utils.Current.CURRENT_AVG_PATH);
			if (f.exists()) {
				return getCurrentValue(f, false);
			}
		}

		// htc desire hd ,desire z
		if (BUILD_MODEL.contains("desire hd") || BUILD_MODEL.contains("desire z")) {
			f = new File(Utils.Current.BATT_CURRENT_PATH);
			if (f.exists()) {
				return getCurrentValue(f, false);
			}
		}

		// htc sensation z710e
		f = new File(Utils.Current.BATT_CURRENT_PATH);
		if (f.exists()) {
			return getCurrentValue(f, false);
		}

		// htc one V
		f = new File(Utils.Current.SMEM_TEXT_PATH);
		if (f.exists()) {
			return getSMemValue();
		}

		// nexus one,meizu
		f = new File(Utils.Current.CURRENT_NOW_PATH);
		if (f.exists()) {
			return getCurrentValue(f, true);
		}

		// galaxy note, galaxy s2
		f = new File(Utils.Current.BATT_CURRENT_ADC_PATH);
		if (f.exists()) {
			return getCurrentValue(f, false);
		}

		// acer V360
		f = new File("/sys/class/power_supply/battery/BatteryAverageCurrent");
		if (f.exists()) {
			return getCurrentValue(f, false);
		}

		// moto milestone,moto mb526
		f = new File("/sys/devices/platform/cpcap_battery/power_supply/usb/current_now");
		if (f.exists()) {
			return getCurrentValue(f, false);
		}

		return null;
	}

	/**
	 * get current value from smem_text
	 *
	 * @return current value
	 */
	public Long getSMemValue() {
		boolean success = false;
		String text = null;
		Long value = null;
		try {
			FileReader fr = new FileReader(Utils.Current.SMEM_TEXT_PATH);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while (line != null) {
				if (line.contains(Utils.Current.I_MBAT)) {
					text = line.substring(line.indexOf(Utils.Current.I_MBAT) + 8);
					success = true;
					break;
				}
				line = br.readLine();
			}
			fr.close();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (success) {
			try {
				value = Long.parseLong(text);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				value = null;
			}
		}
		return value;
	}

	/**
	 * read system file to get current value
	 *
	 * @param file
	 * @param convertToMillis
	 * @return current value
	 */
	public Long getCurrentValue(File file, boolean convertToMillis) {
		log.d("*** getCurrentValue ***");
		log.d("*** " + convertToMillis + " ***");
		String line = null;
		Long value = null;
		FileInputStream fs = null;
		DataInputStream ds = null;
		try {
			fs = new FileInputStream(file);
			ds = new DataInputStream(fs);
			line = ds.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fs.close();
				ds.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (line != null) {
			try {
				value = Long.parseLong(line);
			} catch (NumberFormatException nfe) {
				value = null;
			}
			if (convertToMillis) {
				value = value / 1000;
			}
		}
		return value;
	}
}
