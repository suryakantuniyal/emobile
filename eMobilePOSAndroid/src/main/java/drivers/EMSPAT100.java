package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.StarMicronics.jasura.JAException;
import com.android.database.PayMethodsHandler;
import com.android.database.PaymentsHandler;
import com.android.database.StoredPayments_DB;
import com.android.emobilepos.R;
import com.android.emobilepos.models.Orders;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.partner.pt100.cashdrawer.CashDrawerApiContext;
import com.partner.pt100.cashdrawer.CashDrawerManage;
import com.partner.pt100.display.DisplayLineApiContext;
import com.partner.pt100.display.DisplayManager;
import com.partner.pt100.printer.PrinterManage;
import com.starmicronics.stario.StarIOPortException;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import main.EMSDeviceManager;
import plaintext.EMSPlainTextHelper;
import protocols.EMSCallBack;
import protocols.EMSDeviceManagerPrinterDelegate;

//import com.partner.pt100.cashdrawer.CashDrawerApiContext;
//import com.partner.pt100.cashdrawer.CashDrawerManage;

public class EMSPAT100 extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {

	CashDrawerApiContext cashDrawerApiContext = new CashDrawerManage();

	private List<String> printPref;
	private CreditCardInfo cardManager;
	private Encrypt encrypt;
	private EMSDeviceManager edm;
	private EMSDeviceDriver thisInstance;
	private ProgressDialog myProgressDialog;

	private final String FORMAT = "windows-1252";
	private final int LINE_WIDTH = 32;

	public static DisplayLineApiContext terminalDisp;
	// private CashDrawerApiContext terminalCashDrawer;

	public static DisplayLineApiContext getTerminalDisp() {
		if (terminalDisp == null) {
			terminalDisp = new DisplayManager();
			terminalDisp.open();
			terminalDisp.initialize();
		}
		return terminalDisp;
	}

	@Override
	public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
		this.activity = activity;
		myPref = new MyPreferences(this.activity);

		cardManager = new CreditCardInfo();
		encrypt = new Encrypt(activity);

		this.edm = edm;
		thisInstance = this;

		new processConnectionAsync().execute(0);
	}

	@Override
	public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
			String _portName, String _portNumber) {
		this.activity = activity;
		myPref = new MyPreferences(this.activity);
		cardManager = new CreditCardInfo();
		encrypt = new Encrypt(activity);

		this.edm = edm;
		thisInstance = this;

		terminalDisp = new DisplayManager();
		terminalDisp.open();
		terminalDisp.initialize();

		// terminalCashDrawer = new CashDrawerManage();

		printerApi = new PrinterManage();
		printerApi.open();
		int res = printerApi.initPrinter();
		if (res == 0) {
			this.edm.driverDidConnectToDevice(thisInstance, false);
			return true;
		} else
			this.edm.driverDidNotConnectToDevice(thisInstance, null, false);

		return false;
	}

	public class processConnectionAsync extends AsyncTask<Integer, String, String> {

		String msg = new String();
		boolean didConnect = false;

		@Override
		protected void onPreExecute() {
			myProgressDialog = new ProgressDialog(activity);
			myProgressDialog.setMessage(activity.getString(R.string.progress_connecting_printer));
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			myProgressDialog.show();

		}

		@Override
		protected String doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			printerApi = new PrinterManage();
			printerApi.open();
			int res = printerApi.initPrinter();
			if (res == 0) {
				didConnect = true;
			}

			terminalDisp = new DisplayManager();
			terminalDisp.open();
			terminalDisp.initialize();
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

	@Override
	public void registerAll() {
		this.registerPrinter();
	}

	@Override
	public boolean printTransaction(String ordID, int type, boolean isFromHistory, boolean fromOnHold) {
		// TODO Auto-generated method stub
		printReceipt(ordID, LINE_WIDTH, fromOnHold, type, isFromHistory);
	
		return true;
	}

	@Override
	public boolean printPaymentDetails(String payID, int type, boolean isReprint) {
		// TODO Auto-generated method stub
		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		printPref = myPref.getPrintingPreferences();

		PaymentsHandler payHandler = new PaymentsHandler(activity);
		String[] payArray;
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
		} catch (StarIOPortException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (printPref.contains(MyPreferences.print_header))
			this.printHeader();

		// port.writePort(enableCenter, 0, enableCenter.length); //enable center

		sb.append("* ").append(payArray[0]);
		if (payArray[11].equals("1"))
			sb.append(" Refund *\n\n\n");
		else
			sb.append(" Sale *\n\n\n");

		printerApi.printData(textHandler.centeredString(sb.toString(), LINE_WIDTH));
		// port.writePort(sb.toString().getBytes(FORMAT), 0, sb.length());
		// port.writePort(disableCenter, 0, disableCenter.length); //disable
		// center
		sb.setLength(0);
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
				getString(R.string.receipt_time), LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(payArray[1], payArray[2], LINE_WIDTH, 0)).append("\n\n");

		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer), payArray[3],
				LINE_WIDTH, 0));

		if (payArray[17] != null && !payArray[17].isEmpty())
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_order_id), payArray[17],
					LINE_WIDTH, 0));
		else if (payArray[16] != null && !payArray[16].isEmpty()) // invoice
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
				Global.formatDoubleStrToCurrency(payArray[15]), LINE_WIDTH, 0));

		String change = payArray[6];

		if (isCashPayment && isCheckPayment && !change.isEmpty() && change.contains(".")
				&& Double.parseDouble(change) > 0)
			change = "";

		if (constantValue != null)
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(constantValue,
					Global.formatDoubleStrToCurrency(change), LINE_WIDTH, 0));

		// port.writePort(sb.toString().getBytes(FORMAT), 0,
		// sb.toString().length());
		printerApi.printData(sb.toString());

		sb.setLength(0);
		printerApi.printData(textHandler.newLines(1));
		// port.writePort(textHandler.newLines(4).getBytes(FORMAT), 0,
		// textHandler.newLines(2).length());

		if (!isCashPayment && !isCheckPayment) {
			if (myPref.getPreferences(MyPreferences.pref_handwritten_signature)) {
				sb.append(textHandler.newLines(1));
			} else if (!payArray[7].isEmpty()) {
				encodedSignature = payArray[7];
				try {
					this.printImage(1);
				} catch (StarIOPortException  e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JAException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// port.writePort(enableCenter, 0, enableCenter.length); // center
			sb.append("x").append(textHandler.lines(LINE_WIDTH / 2)).append("\n");
			sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(1));
			printerApi.printData(sb.toString());
			sb.setLength(0);
		}

		if (Global.isIvuLoto) {
			sb = new StringBuilder();
			// port.writePort(enableCenter, 0, enableCenter.length); //enable
			// center

			if (!printPref.contains(MyPreferences.print_ivuloto_qr)) {
				sb.append("\n");
				sb.append(textHandler.centeredString(textHandler.ivuLines(2 * LINE_WIDTH / 3), LINE_WIDTH));
				sb.append(textHandler.centeredString("IVULOTO: " + payArray[13], LINE_WIDTH));
				sb.append(textHandler.centeredString(payArray[12], LINE_WIDTH));
				sb.append(textHandler.centeredString(textHandler.ivuLines(2 * LINE_WIDTH / 3), LINE_WIDTH));
				sb.append("\n");

				printerApi.printData(sb.toString());
			} else {
				// encodedQRCode = payArray[14];

				try {
					this.printImage(2);
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

				printerApi.printData(sb.toString());
			}
			sb.setLength(0);
		}

		this.printFooter();
		// port.writePort(enableCenter, 0, enableCenter.length); // center
		String temp = new String();
		if (!isCashPayment && !isCheckPayment) {

			printerApi.printData(creditCardFooting);
			temp = textHandler.newLines(1);
			printerApi.printData(temp);
		}

		sb.setLength(0);
		if (isReprint) {
			sb.append(textHandler.centeredString("*** Copy ***", LINE_WIDTH));
			printerApi.printData(sb.toString());
		}
		return true;
	}

	@Override
	public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean printOpenInvoices(String invID) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setBitmap(Bitmap bmp) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean printReport(String curDate) {
		// TODO Auto-generated method stub
		try {
			// port = StarIOPort.getPort(portName, portSettings, 10000,
			// this.activity);
			// verifyConnectivity();

			// Thread.sleep(1000);
			// if(!isPOSPrinter)
			// {
			// port.writePort(new byte[] { 0x1d, 0x57, (byte) 0x80, 0x31 }, 0,
			// 4);
			// port.writePort(new byte[] { 0x1d, 0x21, 0x00 }, 0, 3);
			// port.writePort(new byte[]{0x1b,0x74,0x11}, 0,3); //set to
			// windows-1252
			// }
			// else
			// {
			// //port.writePort(new byte[]{0x1b,0x1d,0x74,0x20}, 0,2);
			// }

			PaymentsHandler paymentHandler = new PaymentsHandler(activity);
			PayMethodsHandler payMethodHandler = new PayMethodsHandler(activity);
			EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
			StringBuilder sb = new StringBuilder();
			StringBuilder sb_refunds = new StringBuilder();
			printerApi.printData(textHandler.newLines(1));

			sb.append(textHandler.centeredString("REPORT", LINE_WIDTH));
			sb.append(textHandler.centeredString(Global.formatToDisplayDate(curDate, activity, 0), LINE_WIDTH));
			sb.append(textHandler.newLines(1));
			sb.append(textHandler.oneColumnLineWithLeftAlignedText(getString(R.string.receipt_pay_summary), LINE_WIDTH,
					0));
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
			printerApi.printData(sb.toString());
			sb.setLength(0);

			for (int i = 0; i < size; i++) {
				if (paymentMap.containsKey(payMethodsNames.get(i)[0])) {
					sb.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
							Global.formatDoubleStrToCurrency(paymentMap.get(payMethodsNames.get(i)[0])), LINE_WIDTH,
							3));

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

			printerApi.printData(sb.toString());
			printerApi.printData(sb_refunds.toString());
			printerApi.printData(textHandler.newLines(1));
			// terminal.printData("".getBytes(FORMAT), "".length());

		} catch (Exception e) {

		}

		return true;
	}

	@Override
	public void registerPrinter() {
		// TODO Auto-generated method stub
		edm.currentDevice = this;
	}

	@Override
	public void unregisterPrinter() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadCardReader(EMSCallBack callBack, boolean isDebitCard) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadScanner(EMSCallBack _callBack) {
		// TODO Auto-generated method stub

	}

	@Override
	public void releaseCardReader() {
		// TODO Auto-generated method stub

	}

	@Override
	public void openCashDrawer() {
		cashDrawerApiContext.OpenCashDrawerA();
	}

	// public void printHeader() {
	//
	// EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
	// StringBuilder sb = new StringBuilder();
	//
	// MemoTextHandler handler = new MemoTextHandler(activity);
	// String[] header = handler.getHeader();
	//
	// if(header[0]!=null&&!header[0].isEmpty())
	// sb.append(textHandler.centeredString(header[0], LINE_WIDTH));
	// if(header[1]!=null&&!header[1].isEmpty())
	// sb.append(textHandler.centeredString(header[1], LINE_WIDTH));
	// if(header[2]!=null&&!header[2].isEmpty())
	// sb.append(textHandler.centeredString(header[2], LINE_WIDTH));
	//
	// if(!sb.toString().isEmpty())
	// {
	// sb.append(textHandler.newLines(2));
	// printerApi.printData(sb.toString());
	// }
	// }
	//
	// public void printFooter() {
	//
	// EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
	// StringBuilder sb = new StringBuilder();
	// MemoTextHandler handler = new MemoTextHandler(activity);
	// String[] footer = handler.getFooter();
	//
	// if(footer[0]!=null&&!footer[0].isEmpty())
	// sb.append(textHandler.centeredString(footer[0], LINE_WIDTH));
	// if(footer[1]!=null&&!footer[1].isEmpty())
	// sb.append(textHandler.centeredString(footer[1], LINE_WIDTH));
	// if(footer[2]!=null&&!footer[2].isEmpty())
	// sb.append(textHandler.centeredString(footer[2], LINE_WIDTH));
	//
	//
	// if(!sb.toString().isEmpty())
	// {
	// sb.append(textHandler.newLines(2));
	// printerApi.printData(sb.toString());
	//
	// }
	//
	// }
	@Override
	public boolean isUSBConnected() {
		// TODO Auto-generated method stub
		return false;
	}
	//
	// protected void printImage(int type) {
	// Bitmap myBitmap = null;
	// switch (type) {
	// case 0: // Logo
	// {
	// File imgFile = new File(myPref.getAccountLogoPath());
	// if (imgFile.exists()) {
	// myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
	// // int res = printerApi.downLoadImage(myBitmap);
	// // if (res == 0) {
	// // res = printerApi.printDownLoadImage(0);
	// // } else {
	// // }
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
	// case 2: {
	// // if (!encodedQRCode.isEmpty()) {
	// // byte[] img = Base64.decode(encodedQRCode, Base64.DEFAULT);
	// // myBitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
	// // }
	// break;
	// }
	// }
	//
	// if (myBitmap != null) {
	// printerApi.printImage(myBitmap, 0);
	// }
	// }

	// private double formatStrToDouble(String val) {
	// if (val == null || val.isEmpty())
	// return 0.00;
	// return Double.parseDouble(val);
	// }

	@Override
	public void printStationPrinter(List<Orders> orderProducts, String ordID) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean printOnHold(Object onHold) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void printEndOfDayReport(String date, String clerk_id) {
		// TODO Auto-generated method stub

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
