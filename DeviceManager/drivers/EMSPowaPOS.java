package drivers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.StarMicronics.jasura.JAException;
import com.android.database.InvProdHandler;
import com.android.database.InvoicesHandler;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.PayMethodsHandler;
import com.android.database.PaymentsHandler;
import com.android.database.ProductsHandler;
import com.android.emobilepos.mainmenu.SalesTab_FR;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.Orders;
import com.android.support.ConsignmentTransaction;
import com.android.support.DBManager;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.emobilepos.app.R;

import com.mpowa.android.sdk.powapos.PowaPOS;
import com.mpowa.android.sdk.powapos.common.base.PowaEnums;
import com.mpowa.android.sdk.powapos.common.dataobjects.PowaDeviceObject;
import com.mpowa.android.sdk.powapos.core.PowaPOSEnums;
import com.mpowa.android.sdk.powapos.core.PowaPOSEnums.BootloaderUpdateError;
import com.mpowa.android.sdk.powapos.core.PowaPOSEnums.PowaUSBCOMPort;
import com.mpowa.android.sdk.powapos.core.callbacks.PowaPOSCallback;
import com.mpowa.android.sdk.powapos.core.callbacks.PowaPOSCallbackBase;
import com.mpowa.android.sdk.powapos.drivers.s10.PowaS10Scanner;
import com.mpowa.android.sdk.powapos.drivers.tseries.PowaTSeries;
import com.starmicronics.stario.StarIOPortException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import main.EMSDeviceManager;
import plaintext.EMSPlainTextHelper;
import protocols.EMSCallBack;
import protocols.EMSDeviceManagerPrinterDelegate;
import com.mpowa.android.sdk.powapos.core.callbacks.PowaPOSCallbackBase;
//com.mpowa.android.sdk.powapos.core.callbacks.PowaPOSCallbackBas
public class EMSPowaPOS extends EMSDeviceDriver
		implements EMSDeviceManagerPrinterDelegate {
	private int LINE_WIDTH = 48;

	private Handler handler;
	private ProgressDialog myProgressDialog;
	private EMSDeviceDriver thisInstance;
	private EMSDeviceManager edm;
	private EMSCallBack callBack, _scannerCallBack;

	private boolean isAutoConnect = false;
	private Global global;

	@Override
	public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
		this.activity = activity;
		myPref = new MyPreferences(this.activity);

		this.edm = edm;
		thisInstance = this;

		if (isUSBConnected()) {
			myProgressDialog = new ProgressDialog(activity);
			myProgressDialog.setMessage(activity.getString(R.string.progress_connecting_printer));
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			myProgressDialog.show();
			try {

//				powaPOS = new PowaPOS(this.activity, peripheralCallback);

//				PowaTSeries mcu = new PowaTSeries(this.activity);
//				powaPOS.addPeripheral(mcu);
//
//				PowaS10Scanner scanner = new PowaS10Scanner(activity);
//				powaPOS.addPeripheral(scanner);
				
				powaPOS = new PowaPOS(this.activity, peripheralCallback);
				powaPOS.initializeMCU();
				powaPOS.initializeScanner();

			} catch (Exception e) {
			}
		}
	}

	@Override
	public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
			String _portName, String _portNumber) {

		this.activity = activity;
		myPref = new MyPreferences(this.activity);

		this.edm = edm;
		thisInstance = this;

		isAutoConnect = true;
		global = (Global) activity.getApplication();

		if (isUSBConnected()) {
			if (global.getGlobalDlog() != null)
				global.getGlobalDlog().dismiss();
			myProgressDialog = new ProgressDialog(activity);
			myProgressDialog.setMessage(activity.getString(R.string.progress_connecting_printer));
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			myProgressDialog.show();
			try {
				new Thread(new Runnable() {
					@Override
					public void run() {
						Looper.prepare();
						powaPOS = new PowaPOS(EMSPowaPOS.this.activity, peripheralCallback);
						powaPOS.initializeMCU();
						powaPOS.initializeScanner();

//						powaPOS = new PowaPOS(EMSPowaPOS.this.activity, peripheralCallback);
//						PowaTSeries mcu = new PowaTSeries(EMSPowaPOS.this.activity);
//						powaPOS.addPeripheral(mcu);
//						PowaS10Scanner scanner = new PowaS10Scanner(EMSPowaPOS.this.activity);
//						powaPOS.addPeripheral(scanner);
						myProgressDialog.dismiss();
						Looper.loop();
					}
				}).start();

			} catch (Exception e) {
			}
		} else {
			global.promptForMandatoryLogin(activity);
		}

		return true;
	}

	@Override
	public boolean isUSBConnected() {
		UsbManager manager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);

		HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		if (!deviceList.isEmpty()) {
			return true;
		}
		// while (deviceIterator.hasNext()) {
		//
		// /*UsbDevice device = deviceIterator.next();
		// if((device.getVendorId()==1240&&device.getProductId()==516)||(device.getVendorId()==1155&&device.getProductId()==22321))
		// return true;*/
		//
		// return true;
		//
		// }
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

			try {

				powaPOS = new PowaPOS(activity, peripheralCallback);

				PowaTSeries mcu = new PowaTSeries(activity);
				powaPOS.addPeripheral(mcu);

				// PowaS10Scanner scanner = new PowaS10Scanner(activity);
				// powaPOS.addPeripheral(scanner);

				didConnect = true;
			} catch (Exception e) {
			}

			return null;
		}

		@Override
		protected void onPostExecute(String unused) {
			myProgressDialog.dismiss();

			Toast.makeText(activity, "finished connection", Toast.LENGTH_LONG).show();
			// if (didConnect) {
			// edm.driverDidConnectToDevice(thisInstance,true);
			// } else {
			//
			// edm.driverDidNotConnectToDevice(thisInstance, msg,true);
			// }

		}
	}

	@Override
	public void registerAll() {
		this.registerPrinter();
	}

	// private String getString(int id) {
	// return (activity.getResources().getString(id));
	// }

	@Override
	public boolean printTransaction(String ordID, int type, boolean isFromHistory, boolean fromOnHold) {

		printReceipt(ordID, LINE_WIDTH, fromOnHold, type, isFromHistory);
	

		return true;
	}



	@Override
	public boolean printPaymentDetails(String payID, int type, boolean isReprint) {
		// TODO Auto-generated method stub

		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		printPref = myPref.getPrintingPreferences();

		PaymentsHandler payHandler = new PaymentsHandler(activity);
		String[] payArray = payHandler.getPrintingForPaymentDetails(payID, type);
		StringBuilder sb = new StringBuilder();
		boolean isCashPayment = false;
		boolean isCheckPayment = false;
		String constantValue = getString(R.string.receipt_change);
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
			this.printImage(0);
		} catch (StarIOPortException | JAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		powaPOS.printText("\n\n");
		if (printPref.contains(MyPreferences.print_header))
			this.printHeader();

		sb.append("* ").append(payArray[0]);
		if (payArray[11].equals("1"))
			sb.append(" Refund *");
		else
			sb.append(" Sale *");

		powaPOS.printText(textHandler.centeredString(sb.toString(), LINE_WIDTH));
		powaPOS.printText("\n");

		sb.setLength(0);
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

		sb.append(
				textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_idnum), payID, LINE_WIDTH, 0));

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

		sb.append(textHandler.twoColumnLineWithLeftAlignedText(constantValue, Global.formatDoubleStrToCurrency(change),
				LINE_WIDTH, 0));

		powaPOS.printText(sb.toString());

		sb.setLength(0);
		powaPOS.printText("\n\n");

		if (!isCashPayment && !isCheckPayment) {
			if (myPref.getPreferences(MyPreferences.pref_handwritten_signature)) {
				sb.append(textHandler.newLines(1));
			} else if (!payArray[7].isEmpty()) {
				encodedSignature = payArray[7];
				try {
					this.printImage(1);
				} catch (StarIOPortException | JAException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			sb.append("x").append(textHandler.lines(LINE_WIDTH / 2)).append("\n");
			sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(1));
			powaPOS.printText(sb.toString());
			sb.setLength(0);
		}

		if (Global.isIvuLoto) {
			sb.setLength(0);

			if (!printPref.contains(MyPreferences.print_ivuloto_qr)) {
				powaPOS.printText("\n");
				powaPOS.printText(textHandler.centeredString(textHandler.ivuLines(2 * LINE_WIDTH / 3), LINE_WIDTH));
				powaPOS.printText(textHandler.centeredString("IVULOTO: " + payArray[13], LINE_WIDTH));
				powaPOS.printText(textHandler.centeredString(payArray[12], LINE_WIDTH));
				powaPOS.printText(textHandler.centeredString(textHandler.ivuLines(2 * LINE_WIDTH / 3), LINE_WIDTH));
				powaPOS.printText("\n");
			} else {
				encodedQRCode = payArray[14];

				try {
					this.printImage(2);
				} catch (StarIOPortException | JAException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				sb.append(textHandler.ivuLines(2 * LINE_WIDTH / 3)).append("\n");
				sb.append("\t").append("IVULOTO: ").append(payArray[13]).append("\n");
				sb.append(payArray[12]).append("\n");
				sb.append(textHandler.ivuLines(2 * LINE_WIDTH / 3)).append("\n");

				powaPOS.printText(sb.toString());
			}
			sb.setLength(0);
		}

		this.printFooter();

		if (!isCashPayment && !isCheckPayment) {

			powaPOS.printText(creditCardFooting);
			powaPOS.printText(textHandler.newLines(1));
		}

		if (isReprint) {
			powaPOS.printText(textHandler.centeredString("*** Copy ***", LINE_WIDTH));
		}

		powaPOS.printText("\n\n");

		return true;
	}


	@Override
	public boolean printOnHold(Object onHold) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void setBitmap(Bitmap bmp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void printEndOfDayReport(String date, String clerk_id) {

	}

	@Override
	public boolean printReport(String curDate) {
		// TODO Auto-generated method stub

		PaymentsHandler paymentHandler = new PaymentsHandler(activity);
		PayMethodsHandler payMethodHandler = new PayMethodsHandler(activity);
		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		StringBuilder sb = new StringBuilder();
		StringBuilder sb_refunds = new StringBuilder();
		// port.writePort(textHandler.newLines(3).getBytes(FORMAT), 0,
		// textHandler.newLines(3).length());
		powaPOS.printText(textHandler.newLines(1));
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
		// port.writePort(sb.toString().getBytes(FORMAT), 0,
		// sb.toString().length());
		powaPOS.printText(sb.toString());
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

		powaPOS.printText(sb.toString());
		powaPOS.printText(sb_refunds.toString());
		powaPOS.printText(textHandler.newLines(1));
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
		edm.currentDevice = null;
	}



	@Override
	public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSig) {
		// TODO Auto-generated method stub

		this.encodedSignature = encodedSig;
		printPref = myPref.getPrintingPreferences();
		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		StringBuilder sb = new StringBuilder();
		// SQLiteDatabase db = new DBManager(activity).openReadableDB();
		ProductsHandler productDBHandler = new ProductsHandler(activity);
		// String value = new String();
		HashMap<String, String> map = new HashMap<String, String>();
		double ordTotal = 0, totalSold = 0, totalReturned = 0, totalDispached = 0, totalLines = 0, returnAmount = 0,
				subtotalAmount = 0;

		int size = myConsignment.size();

		try {
			this.printImage(0);
		} catch (StarIOPortException | JAException e) {
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
			if (!myConsignment.get(i).ConsOriginal_Qty.equals("0")) {
				map = productDBHandler.getProductMap(myConsignment.get(i).ConsProd_ID, true);

				sb.append(textHandler.oneColumnLineWithLeftAlignedText(map.get("prod_name"), LINE_WIDTH, 0));

				if (printPref.contains(MyPreferences.print_descriptions)) {
					sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description), "",
							LINE_WIDTH, 3)).append("\n");
					sb.append(textHandler.oneColumnLineWithLeftAlignedText(map.get("prod_desc"), LINE_WIDTH, 5))
							.append("\n");
				} else
					sb.append(textHandler.newLines(1));

				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original Qty:",
						myConsignment.get(i).ConsOriginal_Qty, LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Rack Qty:", myConsignment.get(i).ConsStock_Qty,
						LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Returned Qty:",
						myConsignment.get(i).ConsReturn_Qty, LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Sold Qty:",
						myConsignment.get(i).ConsInvoice_Qty, LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Dispatched Qty:",
						myConsignment.get(i).ConsDispatch_Qty, LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("New Qty:", myConsignment.get(i).ConsNew_Qty,
						LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Product Price:",
						Global.formatDoubleStrToCurrency(map.get("prod_price")), LINE_WIDTH, 5));

				returnAmount = Global.formatNumFromLocale(myConsignment.get(i).ConsReturn_Qty)
						* Global.formatNumFromLocale(map.get("prod_price"));
				subtotalAmount = Global.formatNumFromLocale(myConsignment.get(i).invoice_total) + returnAmount;

				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Subtotal:",
						Global.formatDoubleToCurrency(subtotalAmount), LINE_WIDTH, 5));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Credit Memo:",
						Global.formatDoubleToCurrency(returnAmount), LINE_WIDTH, 5));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total:",
						Global.formatDoubleStrToCurrency(myConsignment.get(i).invoice_total), LINE_WIDTH, 5))
						.append(textHandler.newLines(1));

				totalSold += Double.parseDouble(myConsignment.get(i).ConsInvoice_Qty);
				totalReturned += Double.parseDouble(myConsignment.get(i).ConsReturn_Qty);
				totalDispached += Double.parseDouble(myConsignment.get(i).ConsDispatch_Qty);
				totalLines += 1;
				ordTotal += Double.parseDouble(myConsignment.get(i).invoice_total);

				powaPOS.printText(sb.toString());
				sb.setLength(0);
			}
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

		powaPOS.printText(sb.toString());

		if (printPref.contains(MyPreferences.print_descriptions))
			this.printFooter();

		try {
			this.printImage(1);
		} catch (StarIOPortException | JAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		powaPOS.printText(textHandler.newLines(1));

		// db.close();

		return true;
	}

	@Override
	public void releaseCardReader() {
		// TODO Auto-generated method stu
		callBack = null;
	}

	@Override
	public void loadCardReader(EMSCallBack _callBack, boolean isDebitCard) {
		// TODO Auto-generated method stub
		callBack = _callBack;
	}

	@Override
	public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSig) {
		// TODO Auto-generated method stub

		printPref = myPref.getPrintingPreferences();
		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		StringBuilder sb = new StringBuilder();
		// SQLiteDatabase db = new DBManager(activity).openReadableDB();
		ProductsHandler productDBHandler = new ProductsHandler(activity);
		HashMap<String, String> map = new HashMap<String, String>();
		String prodDesc = "";

		int size = myConsignment.size();

		try {
			this.printImage(0);
		} catch (StarIOPortException | JAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (printPref.contains(MyPreferences.print_header))
			this.printHeader();

		sb.append(textHandler.centeredString("Consignment Pickup Summary", LINE_WIDTH)).append("\n\n");

		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer),
				myPref.getCustName(), LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee),
				myPref.getEmpName(), LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
				Global.formatToDisplayDate(Global.getCurrentDate(), activity, 3), LINE_WIDTH, 0));
		sb.append(textHandler.newLines(1));

		for (int i = 0; i < size; i++) {
			map = productDBHandler.getProductMap(myConsignment.get(i).ConsProd_ID, true);

			sb.append(textHandler.oneColumnLineWithLeftAlignedText(map.get("prod_name"), LINE_WIDTH, 0));

			if (printPref.contains(MyPreferences.print_descriptions)) {
				prodDesc = map.get("prod_desc");
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description), "",
						LINE_WIDTH, 3)).append("\n");
				if (prodDesc != null && !prodDesc.isEmpty())
					sb.append(textHandler.oneColumnLineWithLeftAlignedText(prodDesc, LINE_WIDTH, 5)).append("\n");
			}

			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original Qty:",
					myConsignment.get(i).ConsOriginal_Qty, LINE_WIDTH, 3));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Picked up Qty:",
					myConsignment.get(i).ConsPickup_Qty, LINE_WIDTH, 3));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("New Qty:", myConsignment.get(i).ConsNew_Qty,
					LINE_WIDTH, 3)).append("\n\n\n");

			powaPOS.printText(sb.toString());
			sb.setLength(0);
		}

		if (printPref.contains(MyPreferences.print_footer))
			this.printFooter();

		if (!encodedSig.isEmpty()) {
			this.encodedSignature = encodedSig;
			try {
				this.printImage(1);
			} catch (StarIOPortException | JAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// port.writePort(enableCenter, 0, enableCenter.length); // center
			sb.setLength(0);
			sb.append("x").append(textHandler.lines(LINE_WIDTH / 2)).append("\n");
			sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(1));
			// port.writePort(sb.toString().getBytes(FORMAT), 0,
			// sb.toString().length());
			// port.writePort(disableCenter, 0, disableCenter.length); //
			// disable center
			powaPOS.printText(sb.toString());
		}

		// db.close();

		return true;
	}

	@Override
	public boolean printOpenInvoices(String invID) {
		// TODO Auto-generated method stub

		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		StringBuilder sb = new StringBuilder();
		String[] rightInfo = new String[] {};
		List<String[]> productInfo = new ArrayList<String[]>();
		printPref = myPref.getPrintingPreferences();

		InvoicesHandler invHandler = new InvoicesHandler(activity);
		rightInfo = invHandler.getSpecificInvoice(invID);

		InvProdHandler invProdHandler = new InvProdHandler(activity);
		productInfo = invProdHandler.getInvProd(invID);

		try {
			this.printImage(0);
		} catch (StarIOPortException | JAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (printPref.contains(MyPreferences.print_header))
			this.printHeader();

		sb.append(textHandler.centeredString("Open Invoice Summary", LINE_WIDTH)).append("\n\n");

		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_invoice), rightInfo[1],
				LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_invoice_ref), rightInfo[2],
				LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer), rightInfo[0],
				LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_PO), rightInfo[10],
				LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_terms), rightInfo[9],
				LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_created), rightInfo[5],
				LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_ship), rightInfo[7],
				LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_due), rightInfo[6],
				LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_paid), rightInfo[8],
				LINE_WIDTH, 0));
		powaPOS.printText(sb.toString());

		sb.setLength(0);
		int size = productInfo.size();

		for (int i = 0; i < size; i++) {

			sb.append(textHandler.oneColumnLineWithLeftAlignedText(productInfo.get(i)[2] + "x " + productInfo.get(i)[0],
					LINE_WIDTH, 1));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price),
					Global.getCurrencyFormat(productInfo.get(i)[3]), LINE_WIDTH, 3)).append("\n");
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
					Global.getCurrencyFormat(productInfo.get(i)[5]), LINE_WIDTH, 3)).append("\n");

			if (printPref.contains(MyPreferences.print_descriptions)) {
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description), "",
						LINE_WIDTH, 3)).append("\n");
				sb.append(textHandler.oneColumnLineWithLeftAlignedText(productInfo.get(i)[1], LINE_WIDTH, 5))
						.append("\n\n");
			} else
				sb.append(textHandler.newLines(1));

			powaPOS.printText(sb.toString());
			sb.setLength(0);
		}

		sb.append(textHandler.centeredString(getString(R.string.receipt_thankyou), LINE_WIDTH));
		powaPOS.printText(sb.toString());
		powaPOS.printText(textHandler.newLines(1));
		return true;
	}

	@Override
	public void printStationPrinter(List<Orders> orders, String ordID) {
		// TODO Auto-generated method stub

		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();

		OrdersHandler orderHandler = new OrdersHandler(activity);
		OrderProductsHandler ordProdHandler = new OrderProductsHandler(activity);
		DBManager dbManager = new DBManager(activity);
		// SQLiteDatabase db = dbManager.openWritableDB();
		Order anOrder = orderHandler.getPrintedOrder(ordID);

		StringBuilder sb = new StringBuilder();
		int size = orders.size();

		if (!anOrder.ord_HoldName.isEmpty())
			sb.append(getString(R.string.receipt_name)).append(anOrder.ord_HoldName).append("\n");

		sb.append(getString(R.string.order)).append(": ").append(ordID).append("\n");
		sb.append(getString(R.string.receipt_started)).append(" ")
				.append(Global.formatToDisplayDate(anOrder.ord_timecreated, activity, 4)).append("\n");

		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
		sdf1.setTimeZone(Calendar.getInstance().getTimeZone());
		Date startedDate = new Date();
		try {
			startedDate = sdf1.parse(anOrder.ord_timecreated);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date sentDate = new Date();

		sb.append(getString(R.string.receipt_sent_by)).append(" ").append(myPref.getEmpName()).append(" (");

		if (((float) (sentDate.getTime() - startedDate.getTime()) / 1000) > 60)
			sb.append(Global.formatToDisplayDate(sdf1.format(sentDate.getTime()), activity, 4)).append(")\n\n");
		else
			sb.append(Global.formatToDisplayDate(anOrder.ord_timecreated, activity, 4)).append(")\n\n");

		powaPOS.printText(sb.toString());

		sb.setLength(0);

		int m = 0;
		for (int i = 0; i < size; i++) {

			if (orders.get(i).getHasAddon().equals("1")) {
				m = i;
				ordProdHandler.updateIsPrinted(orders.get(m).getOrdprodID());
				sb.append(orders.get(m).getQty()).append("x ").append(orders.get(m).getName()).append("\n");
				if (!orders.get(m).getAttrDesc().isEmpty())
					sb.append("  [").append(orders.get(m).getAttrDesc()).append("]\n");
				for (int j = i + 1; j < size; j++) {
					ordProdHandler.updateIsPrinted(orders.get(j).getOrdprodID());
					if (orders.get(j).getIsAdded().equals("1"))
						sb.append("  ").append(orders.get(j).getName()).append("\n");
					else
						sb.append("  NO ").append(orders.get(j).getName()).append("\n");

					if ((j + 1 < size && orders.get(j + 1).getAddon().equals("0")) || (j + 1 >= size)) {
						i = j;
						break;
					}
				}

				powaPOS.printText(sb.toString());
				sb.setLength(0);

			} else {
				ordProdHandler.updateIsPrinted(orders.get(i).getOrdprodID());
				sb.append(orders.get(i).getQty()).append("x ").append(orders.get(i).getName()).append("\n");

				powaPOS.printText(sb.toString());
				sb.setLength(0);
			}
		}
		sb.append(textHandler.newLines(1));
		powaPOS.printText(sb.toString());

		// db.close();
	}

	@Override
	public void openCashDrawer() {
		// TODO Auto-generated method stub

		new Thread(new Runnable() {
			public void run() {
				powaPOS.openCashDrawer();
			}
		}).start();
	}

	@Override
	public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
		// TODO Auto-generated method stub

		this.encodedSignature = map.get("encoded_signature");
		printPref = myPref.getPrintingPreferences();
		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		StringBuilder sb = new StringBuilder();
		String prodDesc = "";

		int size = c.getCount();
		try {
			this.printImage(0);
		} catch (StarIOPortException | JAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		powaPOS.printText("\n\n");

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

			sb.append(textHandler.oneColumnLineWithLeftAlignedText(c.getString(c.getColumnIndex("prod_name")),
					LINE_WIDTH, 0));

			if (printPref.contains(MyPreferences.print_descriptions)) {
				prodDesc = c.getString(c.getColumnIndex("prod_desc"));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description), "",
						LINE_WIDTH, 3)).append("\n");
				if (prodDesc != null && !prodDesc.isEmpty())
					sb.append(textHandler.oneColumnLineWithLeftAlignedText(prodDesc, LINE_WIDTH, 5)).append("\n");
			} else
				sb.append(textHandler.newLines(1));

			if (!isPickup) {
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

				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Subtotal:",
						Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex("item_subtotal"))), LINE_WIDTH,
						5));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Credit Memo:",
						Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex("credit_memo"))), LINE_WIDTH, 5));

				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total:",
						Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex("item_total"))), LINE_WIDTH, 5))
						.append(textHandler.newLines(1));
			} else {
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original Qty:",
						c.getString(c.getColumnIndex("ConsOriginal_Qty")), LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Picked up Qty:",
						c.getString(c.getColumnIndex("ConsPickup_Qty")), LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("New Qty:",
						c.getString(c.getColumnIndex("ConsNew_Qty")), LINE_WIDTH, 3)).append("\n\n\n");

			}
			powaPOS.printText(sb.toString());
			sb.setLength(0);

		}

		sb.append(textHandler.lines(LINE_WIDTH));
		if (!isPickup) {
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Sold:", map.get("total_items_sold"),
					LINE_WIDTH, 0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Returned",
					map.get("total_items_returned"), LINE_WIDTH, 0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Dispatched",
					map.get("total_items_dispatched"), LINE_WIDTH, 0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Line Items", map.get("total_line_items"),
					LINE_WIDTH, 0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Grand Total:",
					Global.formatDoubleStrToCurrency(map.get("total_grand_total")), LINE_WIDTH, 0));
		}
		sb.append(textHandler.newLines(1));

		powaPOS.printText(sb.toString());

		if (printPref.contains(MyPreferences.print_footer))
			this.printFooter();

		try {
			this.printImage(1);
		} catch (StarIOPortException | JAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		powaPOS.printText(textHandler.newLines(1));
		return true;
	}

	private PowaPOSCallback peripheralCallback = new PowaPOSCallback() {
		@Override
		public void onMCUInitialized(PowaPOSEnums.InitializedResult result) {

			if (myProgressDialog != null)
				myProgressDialog.dismiss();

			if (isAutoConnect && !global.loggedIn) {
				if (global.getGlobalDlog() != null)
					global.getGlobalDlog().dismiss();
				global.promptForMandatoryLogin(activity);
				SalesTab_FR.startDefault(activity, myPref.getPreferencesValue(MyPreferences.pref_default_transaction));
			}

			if (result.equals(PowaPOSEnums.InitializedResult.SUCCESSFUL))
				edm.driverDidConnectToDevice(thisInstance, !isAutoConnect);
			else
				edm.driverDidNotConnectToDevice(thisInstance, "Failed to connect to MCU", !isAutoConnect);
		}

		@Override
		public void onMCUFirmwareUpdateStarted() {
			// Toast.makeText(activity, "mcu firmware update started",
			// Toast.LENGTH_LONG).show();
		}

		@Override
		public void onScannerConnectionStateChanged(PowaEnums.ConnectionState newState) {
			if (newState.equals(PowaEnums.ConnectionState.CONNECTED)) {
				powaPOS.getScanner().scannerBeep(PowaPOSEnums.PowaScannerBeep.SHORT_2_BEEP_HIGH);
			}
		}
		
		@Override
		public void onScannerInitialized(final PowaPOSEnums.InitializedResult result) {
				if (result.equals(PowaPOSEnums.InitializedResult.SUCCESSFUL)) {

				} else {
					// scannerReconnect();
				}
			
		}
		
		@Override
		public void onMCUFirmwareUpdateProgress(int progress) {
			// if(mcuFragment != null){
			// mcuFragment.updateBarDialog(progress);
			// }
			// Toast.makeText(activity, "mcu firmware update progress",
			// Toast.LENGTH_LONG).show();
		}

		@Override
		public void onMCUFirmwareUpdateFinished() {
			// if(mcuFragment != null){
			// mcuFragment.closeBarDialog();
			// }
			// Toast.makeText(activity, "firmware update finished",
			// Toast.LENGTH_LONG).show();
		}

		@Override
		public void onCashDrawerStatus(PowaPOSEnums.CashDrawerStatus status) {
			// Dialog dialog = createAlertDialog("The cash drawer status is: " +
			// status.toString());
			// dialog.show();
			// Toast.makeText(activity, "cash drawer status",
			// Toast.LENGTH_LONG).show();
		}

		@Override
		public void onRotationSensorStatus(PowaPOSEnums.RotationSensorStatus status) {
			// Dialog dialog = createAlertDialog("The rotation sensor status is:
			// " + status.toString());
			// dialog.show();
			// Toast.makeText(activity, "rotation sensor status",
			// Toast.LENGTH_LONG).show();
		}

	
		// @Override
		// public void onPrintJobCompleted(PowaPOSEnums.PrintJobResult result) {
		// // Toast.makeText(activity, "print job completed",
		// // Toast.LENGTH_LONG).show();
		// }

		@Override
		public void onScannerRead(String data) {
			// if(scannerFragment != null){
			// scannerFragment.scannerRead(data);
			// }
			// Toast.makeText(activity, "scanner read",
			// Toast.LENGTH_LONG).show();
			// Toast.makeText(activity, "Data: "+data,
			// Toast.LENGTH_LONG).show();
			// if(_scannerCallBack!=null)
			// {
			// Toast.makeText(activity, "Callback: "+data,
			// Toast.LENGTH_LONG).show();
			// _scannerCallBack.scannerWasRead(data);
			// }

			scannedData = data;
			handler.post(runnableScannedData);
		}

		@Override
		public void onMCUSystemConfiguration(Map<String, String> configuration) {
			// String str = "";
			// for(Map.Entry<String, String> data: configuration.entrySet()){
			// str += data.getKey() + ": " + data.getValue() + "\n";
			// }
			// Dialog dialog = createAlertDialog(str);
			// dialog.show();
			// Toast.makeText(activity, "MCU Sys Config",
			// Toast.LENGTH_LONG).show();
		}

		// @Override
		// public void onMCUDeviceAttached(PowaMsgHeader.DeviceType type) {
		//
		// //Toast.makeText(activity, "MCU Device Attached",
		// Toast.LENGTH_LONG).show();
		// }
		//
		// @Override
		// public void onMCUDeviceDetached(PowaMsgHeader.DeviceType type) {
		// //Toast.makeText(activity, "MCU Device Detached",
		// Toast.LENGTH_LONG).show();
		// }
		//
		// @Override
		// public void onMCUBootloaderUpdateFailed(BootloaderUpdateError arg0) {
		// // TODO Auto-generated method stub
		//
		// }

		@Override
		public void onMCUBootloaderUpdateFinished() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMCUBootloaderUpdateProgress(int arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMCUBootloaderUpdateStarted() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUSBDeviceAttached(PowaUSBCOMPort arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUSBDeviceDetached(PowaUSBCOMPort arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUSBReceivedData(PowaUSBCOMPort arg0, byte[] arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMCUBootloaderUpdateFailed(BootloaderUpdateError arg0) {
			// TODO Auto-generated method stub

		}
	};

	@Override
	public void loadScanner(EMSCallBack _callBack) {
		// TODO Auto-generated method stub
		_scannerCallBack = _callBack;
		if (handler == null)
			handler = new Handler();
		if (_callBack != null) {

			List<PowaDeviceObject> availScanners = powaPOS.getAvailableScanners();
			if (availScanners.size() > 0) {
				powaPOS.getScanner().selectScanner(availScanners.get(0));
				// powaPOS.selectScanner(availScanners.get(0));
			}
		}
	}



	String scannedData = "";

	private Runnable runnableScannedData = new Runnable() {
		public void run() {
			try {
				if (_scannerCallBack != null)
					_scannerCallBack.scannerWasRead(scannedData);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};

	@Override
	public void printHeader() {
		super.printHeader(LINE_WIDTH);
	}

	@Override
	public void printFooter() {
		super.printFooter(LINE_WIDTH);
	}


}
