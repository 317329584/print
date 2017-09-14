package com.android.print.demo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.android.print.sdk.PrinterInstance;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class UsbOperation implements IPrinterOpertion {
	private final static String TAG = "UsbOpertion";
	private Context mContext;
	private Handler mHandler;
	private PrinterInstance mPrinter;
	private UsbDevice mDevice;
	private boolean hasRegDisconnectReceiver;
	private IntentFilter filter;

	public UsbOperation(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
		hasRegDisconnectReceiver = false;

		filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
	}

	public void open(Intent data) {
		mDevice = data.getParcelableExtra(UsbManager.EXTRA_DEVICE);
		mPrinter = new PrinterInstance(mContext, mDevice, mHandler);
		// default is gbk...
		// mPrinter.setEncoding("gbk");
		mPrinter.openConnection();
	}

	public void close() {
		if (mPrinter != null) {
			mPrinter.closeConnection();
			mPrinter = null;
		}
		if (hasRegDisconnectReceiver) {
			mContext.unregisterReceiver(myReceiver);
			hasRegDisconnectReceiver = false;
		}
	}

	private final BroadcastReceiver myReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i(TAG, "receiver is: " + action);
			if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
				// xxxxx
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				UsbDevice device = (UsbDevice) intent
						.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				if (device != null && mPrinter != null
						&& mPrinter.isConnected() && device.equals(mDevice)) {
					close();
				}
			}
		}
	};

	public PrinterInstance getPrinter() {
		if (mPrinter != null && mPrinter.isConnected()) {
			if (!hasRegDisconnectReceiver) {
				mContext.registerReceiver(myReceiver, filter);
				hasRegDisconnectReceiver = true;
			}
		}
		return mPrinter;
	}

	@Override
	public void chooseDevice() {
		Intent intent = new Intent(mContext, UsbDeviceList.class);
		((Activity) mContext).startActivityForResult(intent,
				MainActivity.CONNECT_DEVICE);
	}
}
