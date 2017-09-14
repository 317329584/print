package com.android.print.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.print.demo.utils.PrintUtils;
import com.android.print.sdk.PrinterConstants.Connect;
import com.android.print.sdk.PrinterInstance;

public class MainActivity extends Activity implements OnClickListener{
	private Context mContext;
	private ImageView imageView;
	private Button btnBluetooth, btnWifi, btnUsb;
	private int offset = 0;
	private int currIndex = 0;//  0--bluetooth,1--wifi,2--usb
	private int bmpW;

	private boolean showUSB; //before android3.0 don't show usb
	private static boolean isConnected;
	private IPrinterOpertion myOpertion;
	private PrinterInstance mPrinter;

	// Intent request codes
    public static final int CONNECT_DEVICE = 1;
    public static final int ENABLE_BT = 2;

    private Button connectButton;
	private Button printImage;

	private Button printText;
	private Button printTable;

	private Button printNote;
	private Button printBarCode;

	private RadioButton paperWidth_58;
	private RadioButton paperWidth_80;

	private RadioButton printer_type_remin;
	private RadioButton printer_type_styuls;

	private boolean is58mm = true;
	private boolean isStylus = false;
	private ProgressDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		showUSB = Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1;
		mContext = this;
		InitView();
		InitImageView();
	}

	private void InitView() {
		connectButton = (Button) findViewById(R.id.connect);
		connectButton.setOnClickListener(this);

		paperWidth_58 = (RadioButton) findViewById(R.id.width_58mm);
		paperWidth_58.setOnClickListener(this);
		paperWidth_80 = (RadioButton) findViewById(R.id.width_80mm);
		paperWidth_80.setOnClickListener(this);

		printer_type_remin = (RadioButton) findViewById(R.id.type_remin);
		printer_type_remin.setOnClickListener(this);
		printer_type_styuls = (RadioButton) findViewById(R.id.type_styuls);
		printer_type_styuls.setOnClickListener(this);

		printText = (Button) findViewById(R.id.btnPrintText);
		printText.setOnClickListener(this);
		printBarCode = (Button) findViewById(R.id.btnPrintBarCode);
		printBarCode.setOnClickListener(this);
		printImage = (Button) findViewById(R.id.btnPrintImage);
		printImage.setOnClickListener(this);
		printTable = (Button) findViewById(R.id.btnPrintTable);
		printTable.setOnClickListener(this);
		printNote = (Button) findViewById(R.id.btnPrintNote);
		printNote.setOnClickListener(this);


		btnBluetooth = (Button) findViewById(R.id.btnBluetooth);
		btnBluetooth.setOnClickListener(this);

		btnWifi = (Button) findViewById(R.id.btnWifi);
		btnWifi.setOnClickListener(this);

		btnUsb = (Button) findViewById(R.id.btnUsb);
		if (showUSB) {
			btnUsb.setOnClickListener(this);
		} else {
			btnUsb.setVisibility(View.GONE);
		}

		dialog = new ProgressDialog(mContext);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setTitle("Connecting...");
		dialog.setMessage("Please Wait...");
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);

		setTitleTextColor(0);
	}

	private void updateButtonState(){
		if(!isConnected)
		{
			String connStr = getResources().getString(R.string.connect);
			switch (currIndex) {
			case 0:
				connStr = String.format(connStr, btnBluetooth.getText());
				break;
			case 1:
				connStr = String.format(connStr, btnWifi.getText());
				break;
			case 2:
				connStr = String.format(connStr, btnUsb.getText());
				break;
			default:
				break;
			}
			connectButton.setText(connStr);
		}else{
			connectButton.setText(R.string.disconnect);
		}

		btnBluetooth.setEnabled(!isConnected);
		btnWifi.setEnabled(!isConnected);
		btnUsb.setEnabled(!isConnected);

		printText.setEnabled(isConnected);
		printBarCode.setEnabled(isConnected);
		printImage.setEnabled(isConnected);
		printTable.setEnabled(isConnected);
		printNote.setEnabled(isConnected);
	}

	@Override
	protected void onActivityResult(final int requestCode, int resultCode, final Intent data) {
		switch (requestCode) {
        case CONNECT_DEVICE:
            if (resultCode == Activity.RESULT_OK) {
            	dialog.show();
            	new Thread(new Runnable(){
                    public void run() {
                    	myOpertion.open(data);
                    }
                }).start();
            }
        	break;
        case ENABLE_BT:
            if (resultCode == Activity.RESULT_OK){
            	myOpertion.chooseDevice();
            }else{
            	Toast.makeText(this, R.string.bt_not_enabled, Toast.LENGTH_SHORT).show();
            }
        }
	}

	private void InitImageView() {
		imageView = (ImageView) findViewById(R.id.cursor);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.slide1)
				.getWidth();

		offset = (screenW / (btnUsb.getVisibility() == View.VISIBLE ? 3 : 2) - bmpW - 4) / 2;// ����ƫ��,�����4��}�߱߾�
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		imageView.setImageMatrix(matrix);
	}

	public void onPageSelected(View view) {
		int index;
		if(view == btnBluetooth){
			index = 0;
		}else if(view == btnWifi){
			index = 1;
		}else{
			index = 2;
		}

		int one = offset * 2 + bmpW;

		Animation animation = new TranslateAnimation(one * currIndex, one * index, 0, 0);// ��Ȼ���Ƚϼ�ֻ࣬��һ�д��롣
		currIndex = index;
		animation.setFillAfter(true);
		animation.setDuration(300);
		imageView.startAnimation(animation);
		setTitleTextColor(index);
	}

	private void setTitleTextColor(int index) {
		switch (index) {
		case 0:
			btnBluetooth.setTextColor(Color.BLUE);
			btnWifi.setTextColor(Color.BLACK);
			btnUsb.setTextColor(Color.BLACK);
			break;
		case 1:
			btnBluetooth.setTextColor(Color.BLACK);
			btnWifi.setTextColor(Color.BLUE);
			btnUsb.setTextColor(Color.BLACK);
			break;
		case 2:
			btnBluetooth.setTextColor(Color.BLACK);
			btnWifi.setTextColor(Color.BLACK);
			btnUsb.setTextColor(Color.BLUE);
			break;

		default:
			break;
		}
		updateButtonState();
	}

	private void openConn(){
		if (!isConnected) {
			switch (currIndex) {
			case 0: // bluetooth
				myOpertion = new BluetoothOperation(MainActivity.this, mHandler);
				break;
			case 1: // wifi
				myOpertion = new WifiOperation(MainActivity.this, mHandler);
				break;
			case 2: // usb
				myOpertion = new UsbOperation(MainActivity.this, mHandler);
				break;
			default:
				break;
			}
			myOpertion.chooseDevice();
		} else {
			myOpertion.close();
			myOpertion = null;
			mPrinter = null;
		}
	}

	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Connect.SUCCESS:
				isConnected = true;
				mPrinter = myOpertion.getPrinter();
				break;
			case Connect.FAILED:
				isConnected = false;
				Toast.makeText(mContext, "connect failed...", Toast.LENGTH_SHORT).show();
				break;
			case Connect.CLOSED:
				isConnected = false;
				Toast.makeText(mContext, "connect close...", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}

			updateButtonState();

			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
		}

	};

	@Override
	public void onClick(View view) {
		if (view == connectButton) {
			openConn();
		} else if(view == btnBluetooth || view == btnWifi || view == btnUsb){
			onPageSelected(view);
		}else if (view == printText) {
			PrintUtils.printText(mContext.getResources(), mPrinter);
		} else if (view == printTable) {
			PrintUtils.printTable(mContext.getResources(), mPrinter, is58mm);
		} else if (view == printImage) {
			PrintUtils.printImage(mContext.getResources(), mPrinter, isStylus);
			PrintUtils.printCustomImage(mContext.getResources(), mPrinter, isStylus, is58mm);
		} else if (view == printNote){
			PrintUtils.printNote(mContext.getResources(), mPrinter, is58mm);
		} else if (view == printBarCode){
			PrintUtils.printBarCode(mPrinter);
		} else if (view == paperWidth_58 || view == paperWidth_80){
			is58mm = view == paperWidth_58;
			paperWidth_58.setChecked(is58mm);
			paperWidth_80.setChecked(!is58mm);
		} else if (view == printer_type_remin || view == printer_type_styuls){
			isStylus = view == printer_type_remin;
			printer_type_remin.setChecked(isStylus);
			printer_type_styuls.setChecked(!isStylus);
		}
	}

}