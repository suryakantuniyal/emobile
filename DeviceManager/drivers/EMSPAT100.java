package drivers;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.StarMicronics.jasura.JAException;
import com.android.database.MemoTextHandler;
import com.android.database.PayMethodsHandler;
import com.android.database.PaymentsHandler;
import com.android.database.StoredPayments_DB;
import com.android.emobilepos.models.Orders;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.emobilepos.app.R;
import com.partner.pt100.cashdrawer.CashDrawerApiContext;
import com.partner.pt100.cashdrawer.CashDrawerManage;
//import com.partner.pt100.cashdrawer.CashDrawerApiContext;
//import com.partner.pt100.cashdrawer.CashDrawerManage;
import com.partner.pt100.display.DisplayLineApiContext;
import com.partner.pt100.display.DisplayManager;
import com.partner.pt100.printer.PrinterManage;
import com.starmicronics.stario.StarIOPortException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import main.EMSDeviceManager;
import plaintext.EMSPlainTextHelper;
import protocols.EMSCallBack;
import protocols.EMSDeviceManagerPrinterDelegate;

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
		// printPref = myPref.getPrintingPreferences();
		// EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		//
		// OrderProductsHandler handler = new OrderProductsHandler(activity);
		// OrderTaxes_DB ordTaxesDB = new OrderTaxes_DB(activity);
		//
		// List<DataTaxes> listOrdTaxes = ordTaxesDB.getOrderTaxes(ordID);
		// List<Orders> orders = handler.getPrintOrderedProducts(ordID);
		//
		// OrdersHandler orderHandler = new OrdersHandler(activity);
		// Order anOrder = orderHandler.getPrintedOrder(ordID);
		// ClerksHandler clerkHandler = new ClerksHandler(activity);
		//
		// StringBuilder sb = new StringBuilder();
		// int size = orders.size();
		//
		// this.printImage(0);
		//
		// if (printPref.contains(MyPreferences.print_header))
		// this.printHeader();
		//
		// if (anOrder.isVoid.equals("1"))
		// sb.append(textHandler.centeredString("*** VOID ***",
		// LINE_WIDTH)).append("\n\n");
		//
		// if (fromOnHold) {
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText("[" +
		// getString(R.string.on_hold) + "]",
		// anOrder.ord_HoldName, LINE_WIDTH, 0));
		// }
		//
		// switch (type) {
		// case 0: // Order
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.order)
		// + ":", ordID, LINE_WIDTH,
		// 0));
		// break;
		// case 1: // Return
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.return_tag)
		// + ":", ordID,
		// LINE_WIDTH, 0));
		// break;
		// case 2: // Invoice
		// case 7:// Consignment Invoice
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.invoice)
		// + ":", ordID, LINE_WIDTH,
		// 0));
		// break;
		// case 3: // Estimate
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.estimate)
		// + ":", ordID,
		// LINE_WIDTH, 0));
		// break;
		// case 5: // Sales Receipt
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.sales_receipt)
		// + ":", ordID,
		// LINE_WIDTH, 0));
		// break;
		// }
		//
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
		// Global.formatToDisplayDate(anOrder.ord_timecreated, activity, 3),
		// LINE_WIDTH,
		// 0));
		//
		// if (!myPref.getShiftIsOpen() ||
		// myPref.getPreferences(MyPreferences.pref_use_clerks)) {
		// String clerk_id = anOrder.clerk_id;
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_clerk),
		// clerkHandler.getClerkName(clerk_id) + "(" + clerk_id + ")",
		// LINE_WIDTH, 0));
		// }
		//
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee),
		// myPref.getEmpName() + "(" + myPref.getEmpID() + ")", LINE_WIDTH, 0));
		//
		// String custName = anOrder.cust_name;
		// if (custName != null && !custName.isEmpty())
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer),
		// custName,
		// LINE_WIDTH, 0));
		//
		// custName = anOrder.cust_id;
		// if (printPref.contains(MyPreferences.print_customer_id) && custName
		// != null && !custName.isEmpty())
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer_id),
		// custName,
		// LINE_WIDTH, 0));
		//
		// String ordComment = anOrder.ord_comment;
		// if (ordComment != null && !ordComment.isEmpty()) {
		// sb.append("\n\n");
		// sb.append("Comments:\n");
		// sb.append(textHandler.oneColumnLineWithLeftAlignedText(ordComment,
		// LINE_WIDTH, 3)).append("\n");
		// }
		//
		// sb.append("\n\n");
		//
		// // port.writePort(sb.toString().getBytes(), 0,
		// sb.toString().length());
		// printerApi.printData(sb.toString());
		//
		// sb.setLength(0);
		//
		// if (!myPref.getPreferences(MyPreferences.pref_wholesale_printout)) {
		// boolean isRestMode =
		// myPref.getPreferences(MyPreferences.pref_restaurant_mode);
		//
		// int m = 0;
		// for (int i = 0; i < size; i++) {
		//
		// if (isRestMode) {
		// if ((i + 1 < size && orders.get(i + 1).getAddon().equals("1"))) {
		// m = i;
		// sb.append(textHandler.oneColumnLineWithLeftAlignedText(
		// orders.get(m).getQty() + "x " + orders.get(m).getName(), LINE_WIDTH,
		// 1));
		// for (int j = i + 1; j < size; j++) {
		// if (orders.get(j).getIsAdded().equals("1"))
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText("- " +
		// orders.get(j).getName(),
		// Global.getCurrencyFormat(orders.get(j).getOverwritePrice()),
		// LINE_WIDTH, 2));
		// else
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(
		// "- NO " + orders.get(j).getName(),
		// Global.getCurrencyFormat(orders.get(j).getOverwritePrice()),
		// LINE_WIDTH, 2));
		//
		// if ((j + 1 < size && orders.get(j + 1).getAddon().equals("0")) || (j
		// + 1 >= size)) {
		// i = j;
		// break;
		// }
		//
		// }
		//
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price),
		// Global.getCurrencyFormat(orders.get(m).getOverwritePrice()),
		// LINE_WIDTH, 3))
		// .append("\n");
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
		// Global.getCurrencyFormat(orders.get(m).getTotal()), LINE_WIDTH,
		// 3)).append("\n");
		//
		// if (printPref.contains(MyPreferences.print_descriptions)) {
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(
		// getString(R.string.receipt_description), "", LINE_WIDTH,
		// 3)).append("\n");
		// sb.append(textHandler.oneColumnLineWithLeftAlignedText(orders.get(m).getProdDescription(),
		// LINE_WIDTH, 5)).append("\n");
		// }
		//
		// } else {
		// sb.append(textHandler.oneColumnLineWithLeftAlignedText(
		// orders.get(i).getQty() + "x " + orders.get(i).getName(), LINE_WIDTH,
		// 1));
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price),
		// Global.getCurrencyFormat(orders.get(i).getOverwritePrice()),
		// LINE_WIDTH, 3))
		// .append("\n");
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
		// Global.getCurrencyFormat(orders.get(i).getTotal()), LINE_WIDTH,
		// 3)).append("\n");
		//
		// if (printPref.contains(MyPreferences.print_descriptions)) {
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(
		// getString(R.string.receipt_description), "", LINE_WIDTH,
		// 3)).append("\n");
		// sb.append(textHandler.oneColumnLineWithLeftAlignedText(orders.get(i).getProdDescription(),
		// LINE_WIDTH, 5)).append("\n");
		// }
		// }
		// } else {
		// sb.append(textHandler.oneColumnLineWithLeftAlignedText(
		// orders.get(i).getQty() + "x " + orders.get(i).getName(), LINE_WIDTH,
		// 1));
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price),
		// Global.getCurrencyFormat(orders.get(i).getOverwritePrice()),
		// LINE_WIDTH, 3)).append("\n");
		//
		// if (orders.get(i).getDiscountID() != null &&
		// !orders.get(i).getDiscountID().isEmpty()) {
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_discount),
		// Global.getCurrencyFormat(orders.get(i).getItemDiscount()),
		// LINE_WIDTH, 3)).append("\n");
		// }
		//
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
		// Global.getCurrencyFormat(orders.get(i).getTotal()), LINE_WIDTH,
		// 3)).append("\n");
		//
		// if (printPref.contains(MyPreferences.print_descriptions)) {
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description),
		// "", LINE_WIDTH, 3)).append("\n");
		// sb.append(textHandler.oneColumnLineWithLeftAlignedText(orders.get(i).getProdDescription(),
		// LINE_WIDTH, 5)).append("\n");
		// }
		//
		// }
		//
		// }
		// } else {
		// int padding = LINE_WIDTH / 4;
		// String tempor = Integer.toString(padding);
		// StringBuilder tempSB = new StringBuilder();
		// tempSB.append("%").append(tempor).append("s").append("%").append(tempor).append("s").append("%")
		// .append(tempor).append("s").append("%").append(tempor).append("s");
		//
		// sb.append(String.format(tempSB.toString(), "Item", "Qty", "Price",
		// "Total")).append("\n\n");
		//
		// for (int i = 0; i < size; i++) {
		//
		// sb.append(orders.get(i).getName()).append("-").append(orders.get(i).getProdDescription()).append("\n");
		// // sb.append(textHandler.fourColumnLineWithLeftAlignedText(" ",
		// // orders.get(i).getQty(),
		// // Global.getCurrencyFormat(orders.get(i).getOverwritePrice()),
		// // Global.getCurrencyFormat(orders.get(i).getTotal()),
		// // LINE_WIDTH, 3)).append("\n\n");
		// sb.append(String.format(tempSB.toString(), " ",
		// orders.get(i).getQty(),
		// Global.getCurrencyFormat(orders.get(i).getOverwritePrice()),
		// Global.getCurrencyFormat(orders.get(i).getTotal()))).append("\n\n");
		//
		// // this.printString(sb.toString());
		// printerApi.printData(sb.toString());
		// sb.setLength(0);
		//
		// }
		// }
		// printerApi.printData(sb.toString());
		// sb.setLength(0);
		// printerApi.printData(textHandler.lines(LINE_WIDTH));
		//
		//// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_subtotal),
		//// Global.formatDoubleStrToCurrency(anOrder.ord_subtotal), LINE_WIDTH,
		// 0));
		//// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_discount),
		//// Global.formatDoubleStrToCurrency(anOrder.ord_discount), LINE_WIDTH,
		// 0));
		//// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_tax),
		//// Global.formatDoubleStrToCurrency(anOrder.ord_taxamount),
		// LINE_WIDTH, 0));
		////
		//// int num_taxes = listOrdTaxes.size();
		//// if (num_taxes > 0) {
		//// for (int i = 0; i < num_taxes; i++) {
		//// sb.append(textHandler.twoColumnLineWithLeftAlignedText(listOrdTaxes.get(i).get(OrderTaxes_DB.tax_name),
		//// Global.getCurrencyFormat(listOrdTaxes.get(i).get(OrderTaxes_DB.tax_amount)),
		// LINE_WIDTH, 2));
		//// }
		//// }
		//
		//
		// addTotalLines(this.activity, anOrder, orders, sb, LINE_WIDTH);
		// int num_taxes = listOrdTaxes.size();
		// addTaxesLine(listOrdTaxes, anOrder.ord_taxamount, LINE_WIDTH, sb);
		//
		// sb.append("\n\n");
		//
		// String granTotal = anOrder.gran_total;
		//
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_grandtotal),
		// Global.formatDoubleStrToCurrency(granTotal), LINE_WIDTH, 0));
		//
		// PaymentsHandler payHandler = new PaymentsHandler(activity);
		// List<String[]> payArrayList =
		// payHandler.getPaymentForPrintingTransactions(ordID);
		// if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward))
		// {
		// StoredPayments_DB dbStoredPay = new StoredPayments_DB(activity);
		// payArrayList.addAll(dbStoredPay.getPaymentForPrintingTransactions(ordID));
		// }
		// String receiptSignature = new String();
		// size = payArrayList.size();
		//
		// double tempGrandTotal = Double.parseDouble(granTotal);
		// double tempAmount = 0;
		// if (size == 0) {
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amountpaid),
		// Global.formatDoubleToCurrency(tempAmount), LINE_WIDTH, 0));
		// if (type == 2) // Invoice
		// {
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_balance_due),
		// Global.formatDoubleToCurrency(tempGrandTotal - tempAmount),
		// LINE_WIDTH, 0));
		// }
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total_tip_paid),
		// Global.formatDoubleToCurrency(0.00), LINE_WIDTH, 0));
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cash_returned),
		// Global.formatDoubleToCurrency(0.00), LINE_WIDTH, 0));
		// // port.writePort(sb.toString().getBytes(FORMAT), 0,
		// // sb.toString().length());
		// } else {
		// tempAmount = formatStrToDouble(payArrayList.get(0)[9]);
		// String _pay_type =
		// payArrayList.get(0)[1].toUpperCase(Locale.getDefault()).trim();
		// double tempTipAmount = formatStrToDouble(payArrayList.get(0)[2]);
		// StringBuilder tempSB = new StringBuilder();
		// tempSB.append(textHandler.oneColumnLineWithLeftAlignedText(
		// Global.formatDoubleStrToCurrency(payArrayList.get(0)[9]) + "[" +
		// payArrayList.get(0)[1] + "]",
		// LINE_WIDTH, 1));
		// if (!_pay_type.equals("CASH") && !_pay_type.equals("CHECK")) {
		// tempSB.append(textHandler.oneColumnLineWithLeftAlignedText("TransID:
		// " + payArrayList.get(0)[4],
		// LINE_WIDTH, 1));
		// tempSB.append(
		// textHandler.oneColumnLineWithLeftAlignedText("CC#: *" +
		// payArrayList.get(0)[5], LINE_WIDTH, 1));
		// }
		// if (!payArrayList.get(0)[3].isEmpty())
		// receiptSignature = payArrayList.get(0)[3];
		//
		// for (int i = 1; i < size; i++) {
		// _pay_type =
		// payArrayList.get(i)[1].toUpperCase(Locale.getDefault()).trim();
		// tempAmount = tempAmount + formatStrToDouble(payArrayList.get(i)[9]);
		// tempTipAmount = tempTipAmount +
		// formatStrToDouble(payArrayList.get(i)[2]);
		// tempSB.append(textHandler.oneColumnLineWithLeftAlignedText(
		// Global.formatDoubleStrToCurrency(payArrayList.get(i)[9]) + "[" +
		// payArrayList.get(i)[1] + "]",
		// LINE_WIDTH, 1));
		// if (!_pay_type.equals("CASH") && !_pay_type.equals("CHECK")) {
		// tempSB.append(textHandler.oneColumnLineWithLeftAlignedText("TransID:
		// " + payArrayList.get(i)[4],
		// LINE_WIDTH, 1));
		// tempSB.append(textHandler.oneColumnLineWithLeftAlignedText("CC#: *" +
		// payArrayList.get(i)[5],
		// LINE_WIDTH, 1));
		// }
		// if (!payArrayList.get(i)[3].isEmpty())
		// receiptSignature = payArrayList.get(i)[3];
		// }
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amountpaid),
		// Global.formatDoubleStrToCurrency(Double.toString(tempAmount)),
		// LINE_WIDTH, 0));
		// sb.append(tempSB.toString());
		// if (type == 2) // Invoice
		// {
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_balance_due),
		// Global.formatDoubleToCurrency(tempGrandTotal - tempAmount),
		// LINE_WIDTH, 0));
		// }
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total_tip_paid),
		// Global.formatDoubleStrToCurrency(Double.toString(tempTipAmount)),
		// LINE_WIDTH, 0));
		//
		// tempAmount = formatStrToDouble(granTotal) - tempAmount;
		// if (tempAmount > 0)
		// tempAmount = 0.00;
		// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cash_returned),
		// Global.formatDoubleStrToCurrency(Double.toString(tempAmount)),
		// LINE_WIDTH, 0)).append("\n\n");
		// // port.writePort(sb.toString().getBytes(FORMAT), 0,
		// // sb.toString().length());
		// }
		//
		// printerApi.printData(sb.toString());
		// printerApi.printData(textHandler.newLines(2));
		//
		// if (printPref.contains(MyPreferences.print_footer))
		// this.printFooter();
		//
		// printerApi.printData(textHandler.newLines(2));
		//
		// receiptSignature = anOrder.ord_signature;
		// if (!receiptSignature.isEmpty()) {
		// this.encodedSignature = receiptSignature;
		// this.printImage(1);
		// // port.writePort(enableCenter, 0, enableCenter.length); // center
		// sb.setLength(0);
		// sb.append("x").append(textHandler.lines(LINE_WIDTH /
		// 2)).append("\n");
		// sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(4));
		// printerApi.printData(sb.toString());
		// // port.writePort(disableCenter, 0, disableCenter.length); //
		// // disable center
		// }
		//
		// if (isFromHistory) {
		// sb.setLength(0);
		// sb.append(textHandler.centeredString("*** Copy ***", LINE_WIDTH));
		// printerApi.printData(sb.toString());
		// printerApi.printData(textHandler.newLines(4));
		// }
		return true;
	}

	@Override
	public boolean printPaymentDetails(String payID, int type, boolean isReprint) {
		// TODO Auto-generated method stub
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
		} catch (StarIOPortException | JAException e) {
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

		sb.append(textHandler.newLines(2));

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
		printerApi.printData(textHandler.newLines(4));
		// port.writePort(textHandler.newLines(4).getBytes(FORMAT), 0,
		// textHandler.newLines(2).length());

		if (!isCashPayment && !isCheckPayment) {
			if (myPref.getPreferences(MyPreferences.pref_handwritten_signature)) {
				sb.append(textHandler.newLines(4));
			} else if (!payArray[7].isEmpty()) {
				encodedSignature = payArray[7];
				try {
					this.printImage(1);
				} catch (StarIOPortException | JAException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// port.writePort(enableCenter, 0, enableCenter.length); // center
			sb.append("x").append(textHandler.lines(LINE_WIDTH / 2)).append("\n");
			sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(4));
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
				} catch (StarIOPortException | JAException e) {
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
			temp = textHandler.newLines(4);
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
			printerApi.printData(textHandler.newLines(3));

			sb.append(textHandler.centeredString("REPORT", LINE_WIDTH));
			sb.append(textHandler.centeredString(Global.formatToDisplayDate(curDate, activity, 0), LINE_WIDTH));
			sb.append(textHandler.newLines(2));
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

			sb.append(textHandler.newLines(2));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
					Global.formatDoubleStrToCurrency(Double.toString(payGranTotal)), LINE_WIDTH, 4));
			sb.append(textHandler.newLines(3));

			sb_refunds.append(textHandler.newLines(2));
			sb_refunds.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
					Global.formatDoubleStrToCurrency(Double.toString(refundGranTotal)), LINE_WIDTH, 4));

			printerApi.printData(sb.toString());
			printerApi.printData(sb_refunds.toString());
			printerApi.printData(textHandler.newLines(5));
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
	public void loadCardReader(EMSCallBack callBack) {
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
