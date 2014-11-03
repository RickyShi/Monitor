package com.missouri.monitor.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.missouri.monitor.logger.Logger;

/**
 * information of network traffic
 *
 */
public class NetworkInfo {

	private final static Logger log = Logger.getLogger(NetworkInfo.class);

	private String uid;

	public NetworkInfo(String uid) {
		this.uid = uid;
	}

	/**
	 * get total network traffic, which is the sum of upload and download
	 * traffic.
	 *
	 * @return total traffic include received and send traffic
	 */
	public long getNetworkInfo() {
		log.i("get traffic information");
		RandomAccessFile rafRcv = null, rafSnd = null;
		String rcvPath = Utils.Network.PROC_UID_PATH + uid + Utils.Network.TCP_RCV;
		String sndPath = Utils.Network.PROC_UID_PATH + uid + Utils.Network.TCP_SND;
		long rcvTraffic = -1;
		long sndTraffic = -1;
		try {
			rafRcv = new RandomAccessFile(rcvPath, "r");
			rafSnd = new RandomAccessFile(sndPath, "r");
			rcvTraffic = Long.parseLong(rafRcv.readLine());
			sndTraffic = Long.parseLong(rafSnd.readLine());
		} catch (FileNotFoundException e) {
			rcvTraffic = -1;
			sndTraffic = -1;
		} catch (NumberFormatException e) {
			log.e( "NumberFormatException: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			log.e("IOException: " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (rafRcv != null) {
					rafRcv.close();
				}
				if (rafSnd != null) {
					rafSnd.close();
				}
			} catch (IOException e) {
				log.i("close randomAccessFile exception: " + e.getMessage());
			}
		}
		if (rcvTraffic == -1 || sndTraffic == -1) {
			return -1;
		} else {
			return rcvTraffic + sndTraffic;
		}
	}
}