package com.android.print.demo;

import com.android.print.sdk.PrinterInstance;

import android.content.Intent;

public interface IPrinterOpertion {
	public void open(Intent data);
	public void close();
	public void chooseDevice();
	public PrinterInstance getPrinter();
}
