package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;

import com.StarMicronics.jasura.JAException;
import com.android.database.PayMethodsHandler;
import com.android.database.PaymentsHandler;
import com.android.database.ProductsHandler;
import com.android.database.StoredPayments_DB;
import com.android.emobilepos.R;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.payment.ProcessCreditCard_FA;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.starmicronics.stario.StarIOPortException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Vector;

import main.EMSDeviceManager;
import plaintext.EMSPlainTextHelper;
import protocols.EMSCallBack;
import protocols.EMSDeviceManagerPrinterDelegate;
import protocols.EMSPrintingDelegate;
import util.NumberUtil;
import util.PocketPos;
import util.StringUtil;

public class EMSBlueBambooP25 extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {

	public EMSPrintingDelegate printingDelegate;
	private BluetoothAdapter mBtAdapter;


	private final int LINE_WIDTH = 32;

	public static BluetoothSocket socket;
	public static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	public static final String LOAD_CARD_P25 = "C0 48 32 30 30 30 32 20 20 20 20 C1"; // 20
																						// second
																						// time-out
	public static final int START_FRAME = -64;
	public static final int END_FRAME = -63;

	private String[] cardValues = new String[3];

	public byte[] btBuf;
	public Vector<Byte> packdata = new Vector<Byte>(2048);
	public static boolean isreceive = false;
	public ReceiveThread receivethread;

	private Bitmap bitmap;
	
	private Handler handler;// = new Handler();
	private ProgressDialog myProgressDialog;
	private EMSDeviceDriver thisInstance;
	private EMSDeviceManager edm;
	private CreditCardInfo cardManager;
	private Encrypt encrypt;
	private BambooHandlerCallback bambooHandler;


	private EMSCallBack callBack;

	public EMSBlueBambooP25() {

		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		cardManager = new CreditCardInfo();

	}

	public void registerAll() {
		this.registerPrinter();
	}

	@Override
	public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
		this.activity = activity;
		myPref = new MyPreferences(this.activity);

		thisInstance = this;
		this.edm = edm;
		encrypt = new Encrypt(activity);
		new processConnectionAsync().execute(0);
	}

	@Override
	public boolean autoConnect(Activity _activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
			String _portName, String _portNumber) {
		this.activity = _activity;
		myPref = new MyPreferences(this.activity);

		thisInstance = this;
		this.edm = edm;
		encrypt = new Encrypt(activity);
		boolean didConnect = false;

		String macAddress = myPref.printerMACAddress(true, null);
		BluetoothDevice btDev = mBtAdapter.getRemoteDevice(macAddress);
		try {
			socket = btDev.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
			socket.connect();

			if (socket != null) {
				inputStream = socket.getInputStream();
				outputStream = socket.getOutputStream();

				didConnect = true;
			}
		} catch (IOException e) {
		}

		if (didConnect) {
			edm.driverDidConnectToDevice(thisInstance, false);
		} else {

			edm.driverDidNotConnectToDevice(thisInstance, "", false);
		}

		return didConnect;
	}

	public class processConnectionAsync extends AsyncTask<Integer, String, String> {

		String msg = new String("Failed to connect");
		boolean didConnect = false;

		@Override
		protected void onPreExecute() {
			myProgressDialog = new ProgressDialog(activity);
			myProgressDialog.setMessage("Connecting Printer...");
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			myProgressDialog.show();

		}

		@Override
		protected String doInBackground(Integer... params) {
			// TODO Auto-generated method stub

			String macAddress = myPref.printerMACAddress(true, null);
			BluetoothDevice btDev = mBtAdapter.getRemoteDevice(macAddress);
			try {
				socket = btDev.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
				socket.connect();

				if (socket != null) {
					inputStream = socket.getInputStream();
					outputStream = socket.getOutputStream();

					didConnect = true;
				}
			} catch (IOException e) {

				msg = "Failed to connect: \n" + e.getMessage();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String unused) {
			myProgressDialog.dismiss();

			if (didConnect) {
				edm.driverDidConnectToDevice(thisInstance, true);
			} else {

				edm.driverDidNotConnectToDevice(thisInstance, msg, true);
			}

		}
	}

//	public void printString(String theString) {
//		byte[] header = { 0x1B, 0x21, 0x01 };
//		byte[] lang = new byte[] { (byte) 0x1B, (byte) 0x4B, (byte) 0x31, (byte) 0x1B, (byte) 0x52, 48 };
//
//		try {
//			this.outputStream.write(header);
//			this.outputStream.write(lang);
//			this.outputStream.write(theString.getBytes("UTF-8"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	public void loadCardReader(EMSCallBack _callBack, boolean isDebitCard) {

		if (handler == null)
			handler = new Handler();

		if (!isreceive) {
			callBack = _callBack;
			startReceiveThread();
		}
		byte[] commandcancel = StringUtil.hexStringToBytes(LOAD_CARD_P25);
		this.printByteArray(commandcancel);
		handler.post(doUpdateDidConnect);
	}

	public void printByteArray(byte[] byteArray) {
		try {
			this.outputStream.write(byteArray);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setBitmap(Bitmap bmp) {
		this.bitmap = bmp;
	}

	// public void printHeader() {
	//
	// EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
	// StringBuilder sb = new StringBuilder();
	//
	// MemoTextHandler handler = new MemoTextHandler(activity);
	// String[] header = handler.getHeader();
	//
	// if (header[0] != null && !header[0].isEmpty())
	// sb.append(textHandler.centeredString(header[0], LINE_WIDTH));
	// if (header[1] != null && !header[1].isEmpty())
	// sb.append(textHandler.centeredString(header[1], LINE_WIDTH));
	// if (header[2] != null && !header[2].isEmpty())
	// sb.append(textHandler.centeredString(header[2], LINE_WIDTH));
	// if (!sb.toString().isEmpty()) {
	// sb.append(textHandler.newLines(3));
	// this.printString("\n" + sb.toString());
	// }
	// }

	// public void printFooter() {
	//
	// EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
	// StringBuilder sb = new StringBuilder();
	// MemoTextHandler handler = new MemoTextHandler(activity);
	// String[] footer = handler.getFooter();
	//
	// if (footer[0] != null && !footer[0].isEmpty())
	// sb.append(textHandler.centeredString(footer[0], LINE_WIDTH));
	// if (footer[1] != null && !footer[1].isEmpty())
	// sb.append(textHandler.centeredString(footer[1], LINE_WIDTH));
	// if (footer[2] != null && !footer[2].isEmpty())
	// sb.append(textHandler.centeredString(footer[2], LINE_WIDTH));
	//
	// if (!sb.toString().isEmpty()) {
	// this.printString(textHandler.newLines(3) + sb.toString() + "\n");
	// }
	//
	// }

	// private String getString(int id) {
	// return activity.getResources().getString(id);
	// }

	@Override
	public boolean printTransaction(String ordID, Global.OrderType type, boolean isFromHistory, boolean fromOnHold) {
		printReceipt(ordID, LINE_WIDTH, fromOnHold, type, isFromHistory);

		return true;
	}

	@Override
	public boolean printPaymentDetails(String payID, int type, boolean isReprint) {

		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		printPref = myPref.getPrintingPreferences();
		PaymentsHandler payHandler = new PaymentsHandler(activity);
		String[] payArray = null;
		boolean isStoredFwd = false;
		long pay_count = payHandler.paymentExist(payID);
		if (pay_count == 0) {
			isStoredFwd = true;
			StoredPayments_DB dbStoredPay = new StoredPayments_DB(activity);
			payArray = dbStoredPay.getPrintingForPaymentDetails(payID, type);
		} else {
			payArray = payHandler.getPrintingForPaymentDetails(payID, type);
		}

		StringBuilder sb = new StringBuilder();
		boolean isCashPayment = false;
		boolean isCheckPayment = false;
		String constantValue = null;
		String creditCardFooting = "";

		if (payArray[0].toUpperCase(Locale.getDefault()).trim().equals("CASH"))
			isCashPayment = true;
		else if (payArray[0].toUpperCase(Locale.getDefault()).trim().equals("CHECK"))
			isCheckPayment = true;
		else {
			constantValue = getString(R.string.receipt_included_tip);
			creditCardFooting = getString(R.string.receipt_creditcard_terms);
		}

		try {
			printImage(0);
		} catch (StarIOPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.printHeader();

		print("\n");
		StringBuilder tempSB = new StringBuilder();

		tempSB.append("* ").append(payArray[0]);
		if (payArray[11].equals("1"))
			tempSB.append(" Refund *");
		else
			tempSB.append(" Sale *");

		sb.append(textHandler.centeredString(tempSB.toString(), LINE_WIDTH)).append("\n\n\n");
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
				getString(R.string.receipt_time), LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(payArray[1], payArray[2], LINE_WIDTH, 0)).append("\n\n");

		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer), payArray[3],
				LINE_WIDTH, 0));

		if (payArray[17] != null && !payArray[17].isEmpty())
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_order_id), payArray[17],
					LINE_WIDTH, 0));
		else if (payArray[16] != null && !payArray[16].isEmpty())
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_invoice_ref),
					payArray[16], LINE_WIDTH, 0));

		if (!isStoredFwd)
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_idnum), payID, LINE_WIDTH,
					0));

		if (!isCashPayment && !isCheckPayment) {
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cardnum),
					"*" + payArray[9], LINE_WIDTH, 0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("TransID:", payArray[8], LINE_WIDTH, 0));
		} else if (isCheckPayment) {
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_checknum), payArray[10],
					LINE_WIDTH, 0));
		}

		sb.append(textHandler.newLines(1));

		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
				Global.formatDoubleStrToCurrency(payArray[4]), LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_paid),
				Global.formatDoubleStrToCurrency(payArray[5]), LINE_WIDTH, 0));

		String change = payArray[6];

		if (isCashPayment && isCheckPayment && !change.isEmpty() && change.contains(".")
				&& Double.parseDouble(change) > 0)
			change = "";

		if (constantValue != null)
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(constantValue,
					Global.formatDoubleStrToCurrency(change), LINE_WIDTH, 0));

		print(sb.toString());

		sb.setLength(0);
		print(textHandler.newLines(1));

		if (!isCashPayment && !isCheckPayment) {
			if (myPref.getPreferences(MyPreferences.pref_handwritten_signature)) {
				sb.append(textHandler.newLines(1));
			} else if (!payArray[7].isEmpty()) {
				encodedSignature = payArray[7];
				try {
					printImage(1);
				} catch (StarIOPortException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JAException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			sb.append(textHandler.centeredString("x" + textHandler.lines(LINE_WIDTH / 2), LINE_WIDTH));
			sb.append(textHandler.centeredString(getString(R.string.receipt_signature), LINE_WIDTH));
			print(sb.toString());
			print(textHandler.newLines(1));
			sb.setLength(0);
		}

		if (Global.isIvuLoto) {
			sb = new StringBuilder();

			if (!printPref.contains(MyPreferences.print_ivuloto_qr)) {
				print("\n");
				print(textHandler.centeredString(textHandler.ivuLines(2 * LINE_WIDTH / 3), LINE_WIDTH));
				print(textHandler.centeredString("IVULOTO: " + payArray[13], LINE_WIDTH));
				print(textHandler.centeredString(payArray[12], LINE_WIDTH));
				print(textHandler.centeredString(textHandler.ivuLines(2 * LINE_WIDTH / 3), LINE_WIDTH));
				print("\n");
			} else {
				encodedQRCode = payArray[14];

				try {
					printImage(2);
				} catch (StarIOPortException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JAException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				sb.append(textHandler.ivuLines(2 * LINE_WIDTH / 3)).append("\n");
				sb.append("\t").append("IVULOTO: ").append(payArray[13]).append("\n");
				sb.append(payArray[12]).append("\n");
				sb.append(textHandler.ivuLines(2 * LINE_WIDTH / 3)).append("\n");

				print(sb.toString());
			}
			sb.setLength(0);
		}

		this.printFooter();
		print(textHandler.newLines(1));

		if (!isCashPayment && !isCheckPayment) {
			sb.append(textHandler.oneColumnLineWithLeftAlignedText(creditCardFooting, LINE_WIDTH, 0));
			sb.append(textHandler.newLines(1));
		}

		if (isReprint) {
			sb.append(textHandler.centeredString("*** Copy ***", LINE_WIDTH));
		}
		print(sb.toString());
		print(textHandler.newLines(1));

		return true;
	}

	// private double formatStrToDouble(String val) {
	// if (val == null || val.isEmpty())
	// return 0.00;
	// return Double.parseDouble(val);
	// }

	// private void printBambooImage(int type) {
	// Bitmap myBitmap = null;
	// switch (type) {
	// case 0: // Logo
	// {
	// File imgFile = new File(myPref.getAccountLogoPath());
	// if (imgFile.exists()) {
	// myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
	//
	// }
	// break;
	// }
	// case 1: // signature
	// {
	// if (!encodedSignature.isEmpty()) {
	// byte[] img = Base64.decode(encodedSignature, Base64.DEFAULT);
	// myBitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
	// }
	// break;
	// }
	// case 2:
	// if (!encodedQRCode.isEmpty()) {
	// byte[] img = Base64.decode(encodedQRCode, Base64.DEFAULT);
	// myBitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
	// }
	// break;
	// }
	//
	// if (myBitmap != null) {
	// EMSBambooImageLoader loader = new EMSBambooImageLoader();
	// ArrayList<ArrayList<Byte>> arrayListList =
	// loader.bambooDataWithAlignment(0, myBitmap);
	//
	// for (ArrayList<Byte> arrayList : arrayListList) {
	//
	// byte[] byteArray = new byte[arrayList.size()];
	// int size = arrayList.size();
	// for (int i = 0; i < size; i++) {
	//
	// byteArray[i] = arrayList.get(i).byteValue();
	//
	// }
	// try {
	// Thread.sleep(800);
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// this.printByteArray(byteArray);
	// }
	// }
	// }

	public boolean printOnHold(Object onHold) {
		EMSBambooImageLoader loader = new EMSBambooImageLoader();
		ArrayList<ArrayList<Byte>> arrayListList = loader.bambooDataWithAlignment(0, bitmap);

		for (ArrayList<Byte> arrayList : arrayListList) {

			byte[] byteArray = new byte[arrayList.size()];
			int size = arrayList.size();
			for (int i = 0; i < size; i++) {
				byteArray[i] = arrayList.get(i).byteValue();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.printByteArray(byteArray);
		}

		return true;
	}

	@Override
	public void printEndOfDayReport(String date, String clerk_id) {

	}

	@Override
	public boolean printReport(String curDate) {

		PaymentsHandler paymentHandler = new PaymentsHandler(activity);
		PayMethodsHandler payMethodHandler = new PayMethodsHandler(activity);
		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		StringBuilder sb = new StringBuilder();
		StringBuilder sb_refunds = new StringBuilder();
		print(textHandler.newLines(1));
		sb.append(textHandler.centeredString("REPORT", LINE_WIDTH));
		sb.append(textHandler.centeredString(Global.formatToDisplayDate(curDate, activity, 0), LINE_WIDTH));
		sb.append(textHandler.centeredString("Device: " + myPref.getEmpName() + "(" + myPref.getEmpID() + ")",
				LINE_WIDTH));
		sb.append(textHandler.newLines(1));
		sb.append(textHandler.oneColumnLineWithLeftAlignedText(getString(R.string.receipt_pay_summary), LINE_WIDTH, 0));
		sb_refunds.append(textHandler.oneColumnLineWithLeftAlignedText(getString(R.string.receipt_refund_summmary),
				LINE_WIDTH, 0));

		HashMap<String, String> paymentMap = paymentHandler
				.getPaymentsRefundsForReportPrinting(Global.formatToDisplayDate(curDate, activity, 4), 0);
		HashMap<String, String> refundMap = paymentHandler
				.getPaymentsRefundsForReportPrinting(Global.formatToDisplayDate(curDate, activity, 4), 1);
		List<String[]> payMethodsNames = payMethodHandler.getPayMethodsName();
		int size = payMethodsNames.size();
		double payGranTotal = 0.00;
		double refundGranTotal = 0.00;

		print(sb.toString());
		sb.setLength(0);
		for (int i = 0; i < size; i++) {

			if (paymentMap.containsKey(payMethodsNames.get(i)[0])) {
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
						Global.formatDoubleStrToCurrency(paymentMap.get(payMethodsNames.get(i)[0])), LINE_WIDTH, 3));
				payGranTotal += Double.parseDouble(paymentMap.get(payMethodsNames.get(i)[0]));
			} else
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
						Global.formatDoubleToCurrency(0.00), LINE_WIDTH, 3));

			if (refundMap.containsKey(payMethodsNames.get(i)[0])) {
				sb_refunds.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
						Global.formatDoubleStrToCurrency(refundMap.get(payMethodsNames.get(i)[0])), LINE_WIDTH, 3));

				refundGranTotal += Double.parseDouble(refundMap.get(payMethodsNames.get(i)[0]));
			} else
				sb_refunds.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
						Global.formatDoubleToCurrency(0.00), LINE_WIDTH, 3));

		}
		sb.append(textHandler.newLines(1));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
				Global.formatDoubleStrToCurrency(Double.toString(payGranTotal)), LINE_WIDTH, 4));
		sb.append(textHandler.newLines(1));

		sb_refunds.append(textHandler.newLines(1));
		sb_refunds.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
				Global.formatDoubleStrToCurrency(Double.toString(refundGranTotal)), LINE_WIDTH, 4));

		print(sb.toString());
		print(sb_refunds.toString());
		print(textHandler.newLines(1));
		print("");

		return true;
	}

	public void registerPrinter() {
		// TODO Auto-generated method stub
		edm.currentDevice = this;
		this.printingDelegate = edm;
	}

	public void unregisterPrinter() {
		// TODO Auto-generated method stub
		edm.currentDevice = null;
		this.printingDelegate = null;
	}

	private void sendMsg(int flag) {

		Message msg = new Message();
		msg.what = flag;
		if (bambooHandler == null)
			bambooHandler = new BambooHandlerCallback();

		bambooHandler.handleMessage(msg);

		// bambooHandler.sendMessage(msg);
	}

	public void startReceiveThread() {
		isreceive = true;
		if (receivethread == null) {
			receivethread = new ReceiveThread();
			receivethread.start();
		}

	}

	@Override
	public void releaseCardReader() {
		isreceive = false;
		callBack = null;
	}

	private class BambooHandlerCallback implements Callback {
		@Override
		public boolean handleMessage(Message msg) {
			if (isreceive) {
				switch (msg.what) {
				case 3:
					int size = packdata.size();
					byte[] buffer = null;
					buffer = new byte[size];

					for (int i = 0; i < size; i++) {
						buffer[i] = packdata.get(i);
					}

					if (buffer != null) {
						// handle the message
						byte[] content = PocketPos.FrameUnpack(buffer, 0, buffer.length);
						if (content != null) {
							if (content[0] == PocketPos.FRAME_MSR) {

								String trackone = "";
								String tracktwo = "";
								String trackthree = "";

								byte tracknumber;
								int offset = 1;
								byte[] tracknumberlength = new byte[4];

								Log.i("content length = ", content.length + "");

								do {

									if (offset < content.length - 2) {
										tracknumber = content[offset];// track
																		// number
									} else {
										return false;
									}

									if (StringUtil.toHexChar(tracknumber) == (byte) 0x31) // track
																							// one
									{
										// Log.i("tracknumber = " ,
										// StringUtil.toHexString(tracknumber));
										tracknumberlength[0] = content[offset + 1];
										tracknumberlength[1] = content[offset + 2];
										tracknumberlength[2] = content[offset + 3];
										tracknumberlength[3] = content[offset + 4];

										offset += 4;
										int tracklength = NumberUtil.parseInt(tracknumberlength, 0,
												tracknumberlength.length, 10);
										byte[] trackcontent = new byte[tracklength];
										System.arraycopy(content, offset + 1, trackcontent, 0, tracklength);

										try {
											trackone = new String(trackcontent, "utf-8");
										} catch (UnsupportedEncodingException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}

										// Global.cardTrackOne = trackone;
										cardManager.setEncryptedAESTrack1(encrypt.encryptWithAES(trackone));
										offset += tracklength;
										offset++;
									}

									if (StringUtil.toHexChar(tracknumber) == (byte) 0x32) // track
																							// two
									{
										// Log.i("tracknumber = ",
										// StringUtil.toHexString(tracknumber));
										tracknumberlength[0] = content[offset + 1];
										tracknumberlength[1] = content[offset + 2];
										tracknumberlength[2] = content[offset + 3];
										tracknumberlength[3] = content[offset + 4];

										offset += 4;
										int tracklength = NumberUtil.parseInt(tracknumberlength, 0,
												tracknumberlength.length, 10);
										byte[] trackcontent = new byte[tracklength];
										System.arraycopy(content, offset + 1, trackcontent, 0, tracklength);

										try {
											tracktwo = new String(trackcontent, "utf-8");
										} catch (UnsupportedEncodingException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}

										// Global.cardTrackTwo = tracktwo;
										cardManager.setEncryptedAESTrack2(encrypt.encryptWithAES(tracktwo));
										offset += tracklength;
										offset++;
									}

									if (StringUtil.toHexChar(tracknumber) == (byte) 0x33) // track
																							// three
									{
										// Log.i("tracknumber = ",
										// StringUtil.toHexString(tracknumber));
										tracknumberlength[0] = content[offset + 1];
										tracknumberlength[1] = content[offset + 2];
										tracknumberlength[2] = content[offset + 3];
										tracknumberlength[3] = content[offset + 4];

										offset += 4;
										int tracklength = NumberUtil.parseInt(tracknumberlength, 0,
												tracknumberlength.length, 10);
										byte[] trackcontent = new byte[tracklength];
										System.arraycopy(content, offset + 1, trackcontent, 0, tracklength);

										try {
											trackthree = new String(trackcontent, "utf-8");
										} catch (UnsupportedEncodingException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										// Log.i("track three content=",
										// trackthree);

										offset += tracklength;
									}

									// Log.i("offset = ", offset + "");
								} while (offset <= content.length - 2);

								cardValues[0] = trackone;
								cardValues[1] = tracktwo;
								cardValues[2] = trackthree;

								// Toast.makeText(activity,
								// cardValues[0]+cardValues[1]+cardValues[2],
								// Toast.LENGTH_LONG).show();

								cardValues[0].trim();
								String[] firstTrack = cardValues[0].split("\\^");
								if (firstTrack.length > 1)
									firstTrack = firstTrack[1].split("/");

								boolean isDateAvail = false;
								String[] secondTrack;
								String expYear = "";
								String expDate = "";
								String cardNumber = "";
								if (cardValues[1].contains("=")) {
									secondTrack = cardValues[1].split("=");
									// tempCardNum[1] should contain the card
									// number
									String[] tempCardNum = secondTrack[0].split(";");
									if (tempCardNum.length > 1) {
										expYear = secondTrack[1].substring(0, 2);
										expDate = secondTrack[1].substring(2, 4);
									}
									cardNumber = tempCardNum[1];

								} else {
									String[] tempCardNum = cardValues[1].split(";");
									cardNumber = tempCardNum[1].replace("?", "");
								}
								// String[] secondTrack =
								// cardValues[1].split("=");
								// String[] cardNumber =
								// secondTrack[0].split(";"); // cardNumber[1]
								// // contains
								// // card
								// // number

								StringBuilder sb = new StringBuilder();
								for (int i = 0; i < firstTrack.length; i++)
									sb.append(firstTrack[i].trim()).append(" ");
								
								cardManager.setCardOwnerName(sb.toString());
								if (cardNumber.length() > 1) {
									int temp = cardNumber.length();
									String last4Digits = (String) cardNumber.subSequence(temp - 4, temp);
									cardManager.setCardLast4(last4Digits);

									cardManager.setCardNumAESEncrypted(encrypt.encryptWithAES(cardNumber));
									cardManager.setCardType(ProcessCreditCard_FA.cardType(cardNumber));
									
									if (!Global.isEncryptSwipe)
										cardManager.setCardNumUnencrypted(cardNumber);
									// if(Global.isEncryptSwipe)
									// cardManager.setCardNumAESEncrypted(encrypt.encryptWithAES(cardNumber));
									// else
									// {
									// cardManager.setCardNumUnencrypted(cardNumber);
									// }
								}
								cardManager.setCardExpMonth(expDate);
								cardManager.setCardExpYear(expYear);

								handler.post(doUpdateViews);
							} else if (content[0] == PocketPos.FRAME_TOF_PWD) {
								if (content.length > 1) {
								}
							}
						}
					}

					break;

				case 4:

					break;
				case 5:
					break;

				case -1:
					break;
				}
			}
			return false;
		}
	}

	class ReceiveThread extends Thread {
		public void run() {

			while (isreceive) {
				if (inputStream != null) {
					try {
						byte[] temp = new byte[2048];// 2k

						int len = inputStream.read(temp);
						if (len > 0) {
							btBuf = new byte[len];

							System.arraycopy(temp, 0, btBuf, 0, btBuf.length);

							// Log.i("receive message", btBuf[0] + "");
							//
							// Log.i("receive message package data",
							// btBuf.length + "");
							// for (int i = 0; i < btBuf.length; i++) {
							// Log.i("receive message package data log",
							// btBuf[i] + "" + "_" + i);
							// }

							if (btBuf[0] == START_FRAME && btBuf[btBuf.length - 1] == END_FRAME) {
								// Log.i("receive message", "put whole data");
								packdata.clear();
								for (int i = 0; i < btBuf.length; i++) {
									packdata.add(btBuf[i]);
								}
								sendMsg(3);
							}

							else if ((btBuf[0] == START_FRAME) && (btBuf[btBuf.length - 1] != END_FRAME)) {
								// Log.i("receive message package data", "No1");
								if (packdata != null && packdata.size() > 0) {
									packdata.clear();

								}
								for (int i = 0; i < btBuf.length; i++) {
									packdata.add(btBuf[i]);
								}
							}

							else if ((btBuf[0] != START_FRAME) && (btBuf[btBuf.length - 1] == END_FRAME)) {
								// Log.i("receive message package data", "No2");
								if (packdata != null) {
									if (packdata.get(0) == START_FRAME) {

										if (packdata.size() <= 2048) {
											for (int i = 0; i < btBuf.length; i++) {
												packdata.add(btBuf[i]);
											}
											sendMsg(3);
										} else {
											packdata.clear();
											// Log.i("receive message package
											// data",
											// "Clear 2");
										}
									}
								}
							} else if (btBuf[0] != START_FRAME && btBuf[btBuf.length - 1] != END_FRAME) {
								if (packdata != null && packdata.size() > 0) {
									if (packdata.get(0) == START_FRAME) {
										// Log.i("receive message package
										// data","No3");
										for (int i = 0; i < btBuf.length; i++) {
											packdata.add(btBuf[i]);
										}
									}
								}
							}

						}

					} catch (Exception e) {
						e.printStackTrace();
						isreceive = false;
					}
				}
			}
		}
	}

	@Override
	public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSig) {
		// TODO Auto-generated method stub
		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		printPref = myPref.getPrintingPreferences();
		StringBuilder sb = new StringBuilder();
		// SQLiteDatabase db = new DBManager(activity).openReadableDB();
		ProductsHandler productDBHandler = new ProductsHandler(activity);
		this.encodedSignature = encodedSig;
		HashMap<String, String> map = new HashMap<String, String>();
		double ordTotal = 0, totalSold = 0, totalReturned = 0, totalDispached = 0, totalLines = 0;

		int size = myConsignment.size();

		try {
			printImage(0);
		} catch (StarIOPortException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (printPref.contains(MyPreferences.print_header))
			this.printHeader();

		sb.append(textHandler.centeredString("Consignment Summary", LINE_WIDTH)).append("\n\n");

		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer),
				myPref.getCustName(), LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee),
				myPref.getEmpName(), LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cons_trans_id),
				myConsignment.get(0).ConsTrans_ID, LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
				Global.formatToDisplayDate(Global.getCurrentDate(), activity, 3), LINE_WIDTH, 0));
		sb.append(textHandler.newLines(1));

		for (int i = 0; i < size; i++) {
			map = productDBHandler.getProductMap(myConsignment.get(i).ConsProd_ID, true);

			sb.append(textHandler.oneColumnLineWithLeftAlignedText(map.get("prod_name"), LINE_WIDTH, 0));

			if (printPref.contains(MyPreferences.print_descriptions)) {
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description), "",
						LINE_WIDTH, 3)).append("\n");
				sb.append(textHandler.oneColumnLineWithLeftAlignedText(map.get("prod_desc"), LINE_WIDTH, 5))
						.append("\n");
			} else
				sb.append(textHandler.newLines(1));

			// sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original
			// Qty",
			// rightText, theLineWidth, theIndentation))

			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original Qty:",
					myConsignment.get(i).ConsOriginal_Qty, LINE_WIDTH, 3));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Rack Qty:", myConsignment.get(i).ConsStock_Qty,
					LINE_WIDTH, 3));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Returned Qty:", myConsignment.get(i).ConsReturn_Qty,
					LINE_WIDTH, 3));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Sold Qty:", myConsignment.get(i).ConsInvoice_Qty,
					LINE_WIDTH, 3));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Dispatched Qty:",
					myConsignment.get(i).ConsDispatch_Qty, LINE_WIDTH, 3));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("New Qty:", myConsignment.get(i).ConsNew_Qty,
					LINE_WIDTH, 3));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Product Price:",
					Global.formatDoubleStrToCurrency(map.get("prod_price")), LINE_WIDTH, 5));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total:",
					Global.formatDoubleStrToCurrency(myConsignment.get(i).invoice_total), LINE_WIDTH, 5))
					.append(textHandler.newLines(1));

			totalSold += Double.parseDouble(myConsignment.get(i).ConsInvoice_Qty);
			totalReturned += Double.parseDouble(myConsignment.get(i).ConsReturn_Qty);
			totalDispached += Double.parseDouble(myConsignment.get(i).ConsDispatch_Qty);
			totalLines += 1;
			ordTotal += Double.parseDouble(myConsignment.get(i).invoice_total);

			print(sb.toString());
			sb.setLength(0);
		}

		sb.append(textHandler.lines(LINE_WIDTH));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Sold:", Double.toString(totalSold),
				LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Returned", Double.toString(totalReturned),
				LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Dispatched",
				Double.toString(totalDispached), LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Line Items", Double.toString(totalLines),
				LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText("Grand Total:", Global.formatDoubleToCurrency(ordTotal),
				LINE_WIDTH, 0));
		sb.append(textHandler.newLines(1));

		print(sb.toString());

		if (printPref.contains(MyPreferences.print_footer))
			this.printFooter();

		try {
			printImage(1);
		} catch (StarIOPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		print(textHandler.newLines(1));
		// db.close();

		return true;
	}

	// displays data from card swiping
	private Runnable doUpdateViews = new Runnable() {
		public void run() {
			try {
				if (callBack != null)
					callBack.cardWasReadSuccessfully(true, cardManager);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};

	private Runnable doUpdateDidConnect = new Runnable() {
		public void run() {
			try {
				if (callBack != null)
					callBack.readerConnectedSuccessfully(true);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};

	@Override
	public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean printOpenInvoices(String invID) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void printStationPrinter(List<Orders> orderProducts, String ordID) {
		// TODO Auto-generated method stub

	}

	@Override
	public void openCashDrawer() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
		// TODO Auto-generated method stub

		this.encodedSignature = map.get("encoded_signature");
		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		StringBuilder sb = new StringBuilder();
		printPref = myPref.getPrintingPreferences();

		int size = c.getCount();
		try {
			printImage(0);
		} catch (StarIOPortException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (printPref.contains(MyPreferences.print_header))
			this.printHeader();

		if (!isPickup)
			sb.append(textHandler.centeredString(getString(R.string.consignment_summary), LINE_WIDTH)).append("\n\n");
		else
			sb.append(textHandler.centeredString(getString(R.string.consignment_pickup), LINE_WIDTH)).append("\n\n");

		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer),
				map.get("cust_name"), LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee),
				myPref.getEmpName(), LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cons_trans_id),
				map.get("ConsTrans_ID"), LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
				Global.formatToDisplayDate(Global.getCurrentDate(), activity, 3), LINE_WIDTH, 0));
		sb.append(textHandler.newLines(1));

		for (int i = 0; i < size; i++) {
			c.moveToPosition(i);
			if (!c.getString(c.getColumnIndex("ConsOriginal_Qty")).equals("0")) {

				sb.append(textHandler.oneColumnLineWithLeftAlignedText(c.getString(c.getColumnIndex("prod_name")),
						LINE_WIDTH, 0));

				if (printPref.contains(MyPreferences.print_descriptions)) {
					sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description), "",
							LINE_WIDTH, 3)).append("\n");
					sb.append(textHandler.oneColumnLineWithLeftAlignedText(c.getString(c.getColumnIndex("prod_desc")),
							LINE_WIDTH, 5)).append("\n");
				} else
					sb.append(textHandler.newLines(1));

				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original Qty:",
						c.getString(c.getColumnIndex("ConsOriginal_Qty")), LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Rack Qty:",
						c.getString(c.getColumnIndex("ConsStock_Qty")), LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Returned Qty:",
						c.getString(c.getColumnIndex("ConsReturn_Qty")), LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Sold Qty:",
						c.getString(c.getColumnIndex("ConsInvoice_Qty")), LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Dispatched Qty:",
						c.getString(c.getColumnIndex("ConsDispatch_Qty")), LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("New Qty:",
						c.getString(c.getColumnIndex("ConsNew_Qty")), LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Product Price:",
						Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex("price"))), LINE_WIDTH, 5));
				if (!isPickup)
					sb.append(textHandler.twoColumnLineWithLeftAlignedText("Subtotal:",
							Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex("item_subtotal"))),
							LINE_WIDTH, 5));
				else
					sb.append(textHandler.twoColumnLineWithLeftAlignedText("Subtotal:",
							Global.formatDoubleStrToCurrency("0"), LINE_WIDTH, 5));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Credit Memo:",
						Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex("credit_memo"))), LINE_WIDTH, 5));
				if (!isPickup)
					sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total:",
							Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex("item_total"))), LINE_WIDTH,
							5)).append(textHandler.newLines(1));
				else
					sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total:",
							Global.formatDoubleStrToCurrency("0"), LINE_WIDTH, 5)).append(textHandler.newLines(1));

				print(sb.toString());
				sb.setLength(0);
			}
		}

		sb.append(textHandler.lines(LINE_WIDTH));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Sold:", map.get("total_items_sold"),
				LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Returned", map.get("total_items_returned"),
				LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Dispatched",
				map.get("total_items_dispatched"), LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Line Items", map.get("total_line_items"),
				LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText("Grand Total:",
				Global.formatDoubleStrToCurrency(map.get("total_grand_total")), LINE_WIDTH, 0));
		sb.append(textHandler.newLines(1));

		print(sb.toString());

		if (printPref.contains(MyPreferences.print_footer))
			this.printFooter();

		try {
			printImage(1);
		} catch (StarIOPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		print(textHandler.newLines(1));

		return true;
	}

	@Override
	public void loadScanner(EMSCallBack _callBack) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isUSBConnected() {
		return false;
	}

	@Override
	public void printHeader() {
		super.printHeader(LINE_WIDTH);
	}

	@Override
	public void printFooter() {
		super.printFooter(LINE_WIDTH);
	}
}
