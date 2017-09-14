package com.android.print.demo;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;

import com.android.print.sdk.PrinterInstance;

public class WifiOperation implements IPrinterOpertion{
	private Context mContext;
	private Handler mHandler;
	private PrinterInstance mPrinter;
	private WifiManager wifiManager;
	private Timer timer;
	private int errorNumber;
	public WifiOperation(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
		wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		timer = new Timer();
	}

	public void close() {
		if (mPrinter != null) {
			mPrinter.closeConnection();
			mPrinter = null;
		}
	}

	//每隔10s检查一次状态。能检查到就证明连接正常。否则再查一次。还不行就断开连接
	private TimerTask myTask = new TimerTask() {
		byte[] writeData = new byte[]{0x10, 0x04, 0x01};
		@Override
		public void run() {
			if (mPrinter == null) {
				timer.cancel();
				return;
			}
			mPrinter.sendByteData(writeData);

			if (mPrinter.read() != null) {
				System.out.println("wifi connection is alive..");
			}else{
				errorNumber++;
				if (errorNumber == 2) {
					errorNumber = 0;
					close();
				}
			}
		}
	};

	public PrinterInstance getPrinter() {
		if (mPrinter != null && mPrinter.isConnected()) {
			timer.schedule(myTask, 5000, 10000);
		}
		return mPrinter;
	}

	public void open(Intent data) {
		String ipAddress = data.getStringExtra("ip_address");
		mPrinter = new PrinterInstance(ipAddress, 9100, mHandler);
		//default is gbk...
		//mPrinter.setEncoding("gbk");
		mPrinter.openConnection();
	}

	@Override
	public void chooseDevice() {
		if (!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}
		Intent intent = new Intent();
    	intent.setClass(mContext, IpAddressEdit.class);
    	((Activity)mContext).startActivityForResult(intent, MainActivity.CONNECT_DEVICE);
	}
}
