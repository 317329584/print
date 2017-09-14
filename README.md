# print
安卓手机打印demo

链接usb打印机

private void connectPrinter() {
		
		myOpertion = new UsbOperation(this, mHandler);
		//myOpertion.chooseDevice();
	
		UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> devices = manager.getDeviceList();
		ArrayList<UsbDevice>	deviceList = new ArrayList<UsbDevice>();
		for (UsbDevice device : devices.values()) {
			if (USBPort.isUsbPrinter(device)) {
				deviceList.add(device);
			}
		}
		if(deviceList.size() > 0){
			final Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putParcelable(UsbManager.EXTRA_DEVICE, deviceList.get(0));
			intent.putExtras(bundle);
			new Thread(new Runnable() {
				public void run() {
					myOpertion.open(intent);
				}
			}).start();
		}
	}




	/**
	 * 打印二维码
	 * 
	 * @param mPrinter
	 * @param value
	 */
	public static void printQRCode(PrinterInstance mPrinter, String value) {
		mPrinter.init();
		mPrinter.setCharacterMultiple(0, 0);
		mPrinter.setLeftMargin(15, 0);
		Barcode qrcode = new Barcode(BarcodeType.QRCODE, 2, 3, 6, value);
		mPrinter.printBarCode(qrcode);
	}

	public static void printQRCodeBitmap(Context con, PrinterInstance mPrinter,
			String value) {
		Bitmap bitmap = createQRCodeBitmap(value);
		mPrinter.init();
		mPrinter.setCharacterMultiple(0, 0);
		mPrinter.setLeftMargin(500, 0);
		mPrinter.printImage(bitmap);
	}

	/**
	 * 换行
	 * 
	 * @param mPrinter
	 */
	public static void changeLine(PrinterInstance mPrinter, int num) {
		mPrinter.setPrinter(Command.PRINT_AND_WAKE_PAPER_BY_LINE, num); // 换1行
	}

	/**
	 * 打印文字
	 * 
	 * @param mPrinter
	 * @param str
	 */
	public static void printText(PrinterInstance mPrinter, String str) {
		mPrinter.printText(str);
	}

	/**
	 * 切纸
	 */
	public static void cutPaper(PrinterInstance mPrinter) {
		mPrinter.cutPaper();
	}

	/**
	 * 设置打印模式
	 */
	public static void setMode(PrinterInstance mPrinter, boolean isBold,
			boolean isDoubleHeight, boolean isDoubleWidth, boolean isUnderLine) {
		mPrinter.setPrintModel(isBold, isDoubleHeight, isDoubleWidth,
				isUnderLine);
	}

	public static void showToast(Context con, String txt) {
		Toast toa = Toast.makeText(con, txt, Toast.LENGTH_SHORT);
		toa.setGravity(Gravity.CENTER, 0, 0);
		toa.show();
	}

	// 生成二维码
	public static Bitmap createQRCodeBitmap(String text) {
		int QR_WIDTH = 300, QR_HEIGHT = 300;
		try {
			// 需要引入core包
			if (text == null || "".equals(text) || text.length() < 1) {
				return null;
			}
			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			// 比特矩阵
			BitMatrix bitMatrix = new QRCodeWriter().encode(text,
					BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
			int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
			// 比特矩阵转颜色数组
			for (int y = 0; y < QR_HEIGHT; y++) {
				for (int x = 0; x < QR_WIDTH; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * QR_WIDTH + x] = 0xff000000;// 黑点
					} else {
						pixels[y * QR_WIDTH + x] = 0xffffffff;// 透明点,白点为0xffffffff
					}

				}
			}
			// 解析颜色数组,其他的java平台可以选择其他的API
			Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH+100, QR_HEIGHT,
					Bitmap.Config.ARGB_8888);
			Canvas can = new Canvas(bitmap);
			Paint pa = new Paint();
			pa.setColor(Color.WHITE);
			can.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), pa);
			bitmap.setPixels(pixels, 0, QR_WIDTH, 50, 0, QR_WIDTH, QR_HEIGHT);
			return bitmap;
		} catch (WriterException e) {
			e.printStackTrace();
			return null;
		}
	}
  
  整理的比较乱，请多担待
