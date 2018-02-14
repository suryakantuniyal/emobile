package drivers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.StarMicronics.jasura.JAException;
import com.android.database.InvProdHandler;
import com.android.database.InvoicesHandler;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.PayMethodsHandler;
import com.android.database.PaymentsHandler;
import com.android.database.ProductsHandler;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.Orders;
import com.android.support.ConsignmentTransaction;
import com.android.database.DBManager;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.emobilepos.app.R;
import com.starmicronics.stario.StarIOPortException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import datamaxoneil.connection.Connection_Bluetooth;
import main.EMSDeviceManager;
import plaintext.EMSPlainTextHelper;
import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import interfaces.EMSPrintingDelegate;

public class EMSOneil4te extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {

	public EMSPrintingDelegate printingDelegate;
	private final int LINE_WIDTH = 83;
	private final String FORMAT = "windows-1252";
	private String encodedSignature;
	private String encodedQRCode = "";
	private ProgressDialog myProgressDialog;

	private EMSDeviceDriver thisInstance;
	private EMSDeviceManager edm;
	private Resources resources;

	public void registerAll() {
		this.registerPrinter();
	}

	@Override
	public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
		this.activity = activity;
		myPref = new MyPreferences(this.activity);
		thisInstance = this;
		this.edm = edm;
		resources = this.activity.getResources();
		new processConnectionAsync().execute(0);
	}

	@Override
	public boolean autoConnect(Activity _activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
			String _portName, String _portNumber) {
		this.activity = _activity;
		myPref = new MyPreferences(this.activity);
		thisInstance = this;
		this.edm = edm;
		resources = this.activity.getResources();
		boolean didConnect = false;

		String macAddress = myPref.printerMACAddress(true, null);

		try {
			device = Connection_Bluetooth.createClient(macAddress);
			didConnect = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
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

			try {
				device = Connection_Bluetooth.createClient(macAddress);
				didConnect = true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				didConnect = false;
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

	//
	// private String getString(int id)
	// {
	// return(this.resources.getString(id));
	// }
	//

	@Override
	public boolean printTransaction(String ordID, int type, boolean isFromHistory, boolean fromOnHold) {
		// TODO Auto-generated method stub

		try {

			if (!device.getIsOpen())
				device.open();
			printReceipt(ordID, LINE_WIDTH, fromOnHold, type, isFromHistory);

			// EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
			// printPref = myPref.getPrintingPreferences();
			// OrderProductsHandler handler = new
			// OrderProductsHandler(activity);
			// List<Orders> orders = handler.getPrintOrderedProducts(ordID);
			//
			// OrdersHandler orderHandler = new OrdersHandler(activity);
			// Order anOrder = orderHandler.getPrintedOrder(ordID);
			//
			// StringBuilder sb = new StringBuilder();
			// int size = orders.size();
			//
			//
			// this.printImage(0);
			//
			// if(printPref.contains(MyPreferences.print_header))
			// this.printHeader();
			//
			//
			// if(fromOnHold)
			// {
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText("["+getString(R.string.on_hold)+"]",anOrder.ord_HoldName
			// , LINE_WIDTH, 0));
			// }
			//
			//
			// switch (type)
			// {
			// case 0: // Order
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.order)+":",
			// ordID, LINE_WIDTH, 0));
			// break;
			// case 1: // Return
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.return_tag)+":",
			// ordID, LINE_WIDTH, 0));
			// break;
			// case 2: // Invoice
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.invoice)+":",
			// ordID, LINE_WIDTH, 0));
			// break;
			// case 3: // Estimate
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.estimate)+":",
			// ordID, LINE_WIDTH, 0));
			// break;
			// case 5: // Sales Receipt
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.sales_receipt)+":",
			// ordID, LINE_WIDTH, 0));
			// break;
			// }
			//
			// sb.append("\n");
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
			// Global.formatToDisplayDate(anOrder.ord_timecreated, activity, 3),
			// LINE_WIDTH, 0));
			// sb.append("\n");
			//
			// if(!myPref.getShiftIsOpen()||myPref.getPreferences(MyPreferences.pref_use_clerks))
			// {
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_clerk),myPref.getClerkName()+
			// "("+myPref.getClerkID()+")", LINE_WIDTH,0));
			// sb.append("\n");
			// }
			//
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee),
			// myPref.getEmpName() + "(" + myPref.getEmpID() + ")", LINE_WIDTH,
			// 0));
			// sb.append("\n");
			//
			// String custName = anOrder.cust_name;
			// if (custName != null && !custName.isEmpty())
			// {
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer),
			// custName, LINE_WIDTH, 0)).append("\n\n");
			// sb.append("\n");
			// }
			// else
			// sb.append("\n\n");
			//
			// device.write(sb.toString().getBytes(FORMAT), 0,
			// sb.toString().length());
			//
			// sb.setLength(0);
			//
			//
			// if(!myPref.getPreferences(MyPreferences.pref_wholesale_printout))
			// {
			//
			// boolean isRestMode =
			// myPref.getPreferences(MyPreferences.pref_restaurant_mode);
			//
			// int m = 0;
			// for (int i = 0; i < size; i++)
			// {
			//
			// if(isRestMode)
			// {
			// if((i+1<size&&orders.get(i+1).getAddon().equals("1")))
			// {
			// m = i;
			// sb.append(textHandler.oneColumnLineWithLeftAlignedText(orders.get(m).getQty()
			// + "x " + orders.get(m).getName(), LINE_WIDTH, 1));
			// sb.append("\n");
			// for(int j = i+1;j<size;j++)
			// {
			// if(orders.get(j).getIsAdded().equals("1"))
			// {
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText("-
			// "+orders.get(j).getName(),
			// Global.getCurrencyFormat(orders.get(j).getOverwritePrice()),
			// LINE_WIDTH, 2));
			// sb.append("\n");
			// }
			// else
			// {
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText("- NO
			// "+orders.get(j).getName(),
			// Global.getCurrencyFormat(orders.get(j).getOverwritePrice()),
			// LINE_WIDTH, 2));
			// sb.append("\n");
			// }
			//
			// if((j+1<size&&orders.get(j+1).getAddon().equals("0"))||(j+1>=size))
			// {
			// i=j;
			// break;
			// }
			//
			// }
			//
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price),
			// Global.getCurrencyFormat(orders.get(m).getOverwritePrice()),
			// LINE_WIDTH, 3)).append("\n");
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
			// Global.getCurrencyFormat(orders.get(m).getTotal()), LINE_WIDTH,
			// 3))
			// .append("\n");
			//
			// if(printPref.contains(MyPreferences.print_descriptions))
			// {
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description),
			// "", LINE_WIDTH, 3)).append("\n");
			// sb.append(textHandler.oneColumnLineWithLeftAlignedText(orders.get(m).getProdDescription(),
			// LINE_WIDTH, 5)).append("\n\n");
			// }
			//
			// }
			// else
			// {
			// sb.append(textHandler.oneColumnLineWithLeftAlignedText(orders.get(i).getQty()
			// + "x " + orders.get(i).getName(), LINE_WIDTH, 1));
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price),
			// Global.getCurrencyFormat(orders.get(i).getOverwritePrice()),
			// LINE_WIDTH, 3)).append("\n");
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
			// Global.getCurrencyFormat(orders.get(i).getTotal()), LINE_WIDTH,
			// 3));
			//
			// if(printPref.contains(MyPreferences.print_descriptions))
			// {
			// sb.append("\n");
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description),
			// "", LINE_WIDTH, 3)).append("\n");
			// sb.append(textHandler.oneColumnLineWithLeftAlignedText(orders.get(i).getProdDescription(),
			// LINE_WIDTH, 5)).append("\n");
			// }
			// else
			// sb.append("\n\n");
			//
			// }
			// }
			// else
			// {
			// sb.append(textHandler.oneColumnLineWithLeftAlignedText(orders.get(i).getQty()
			// + "x " + orders.get(i).getName(), LINE_WIDTH, 1));
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price),
			// Global.getCurrencyFormat(orders.get(i).getOverwritePrice()),LINE_WIDTH,
			// 3)).append("\n");
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
			// Global.getCurrencyFormat(orders.get(i).getTotal()), LINE_WIDTH,
			// 3));
			//
			// if(printPref.contains(MyPreferences.print_descriptions))
			// {
			// sb.append("\n");
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description),
			// "", LINE_WIDTH, 3)).append("\n");
			// sb.append(textHandler.oneColumnLineWithLeftAlignedText(orders.get(i).getProdDescription(),
			// LINE_WIDTH, 5)).append("\n");
			// }
			// else
			// sb.append("\n\n");
			// }
			//
			// }
			// device.write(sb.toString().getBytes(FORMAT), 0,
			// sb.toString().length());
			// sb.setLength(0);
			// }
			// else
			// {
			// int padding = LINE_WIDTH / 4;
			// String tempor = Integer.toString(padding);
			// StringBuilder tempSB = new StringBuilder();
			// tempSB.append("%").append(tempor).append("s").append("%").append(tempor).append("s").append("%").append(tempor).append("s").append("%").append(tempor).append("s");
			//
			// sb.append(String.format(tempSB.toString(),
			// "Item","Qty","Price","Total")).append("\n\n");
			//
			// for(int i = 0; i < size; i++)
			// {
			//
			//
			// sb.append(orders.get(i).getName());
			// if(printPref.contains(MyPreferences.print_descriptions))
			// {
			// sb.append(" - ").append(orders.get(i).getProdDescription());
			// }
			//
			// sb.append("\n").append(String.format(tempSB.toString(), "
			// ",orders.get(i).getQty(),Global.getCurrencyFormat(orders.get(i).getOverwritePrice()),Global.getCurrencyFormat(orders.get(i).getTotal()))).append("\n\n");
			//
			// device.write(sb.toString().getBytes(FORMAT),0,sb.toString().length());
			// sb.setLength(0);
			//
			// }
			// }
			//
			// device.write(textHandler.lines(LINE_WIDTH).getBytes(), 0,
			// textHandler.lines(LINE_WIDTH).length());
			//
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_subtotal),Global.formatDoubleStrToCurrency(anOrder.ord_subtotal),
			// LINE_WIDTH, 0)).append("\n\n");
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_discount),
			// Global.formatDoubleStrToCurrency(anOrder.ord_discount),
			// LINE_WIDTH, 0)).append("\n");
			// sb.append(
			// textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_tax),
			// Global.formatDoubleStrToCurrency(anOrder.ord_taxamount),
			// LINE_WIDTH, 0)).append("\n\n");
			//
			// OrderTaxes_DB ordTaxesDB = new OrderTaxes_DB(activity);
			// List<DataTaxes> listOrdTaxes = ordTaxesDB.getOrderTaxes(ordID);
			//
			// addTaxesLine(listOrdTaxes, OrderTaxes_DB.tax_amount, LINE_WIDTH,
			// sb);
			//
			// String granTotal = anOrder.gran_total;
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_grandtotal),
			// Global.formatDoubleStrToCurrency(granTotal), LINE_WIDTH,
			// 0)).append("\n");
			//
			// PaymentsHandler payHandler = new PaymentsHandler(activity);
			// List<String[]> payArrayList =
			// payHandler.getPaymentForPrintingTransactions(ordID);
			// String receiptSignature = new String();
			// size = payArrayList.size();
			// if (size == 0) {
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amountpaid),
			// Global.formatDoubleToCurrency(0.00), LINE_WIDTH,
			// 0)).append("\n");
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total_tip_paid),
			// Global.formatDoubleToCurrency(0.00), LINE_WIDTH,
			// 0)).append("\n");
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cash_returned),
			// Global.formatDoubleToCurrency(0.00), LINE_WIDTH,
			// 0)).append("\n");
			// } else {
			// String _pay_type =
			// payArrayList.get(0)[1].toUpperCase(Locale.getDefault()).trim();
			// double tempAmount = formatStrToDouble(payArrayList.get(0)[9]);
			// double tempTipAmount = formatStrToDouble(payArrayList.get(0)[2]);
			// StringBuilder tempSB = new StringBuilder();
			// tempSB.append(textHandler.oneColumnLineWithLeftAlignedText(
			// Global.formatDoubleStrToCurrency(payArrayList.get(0)[9]) + "[" +
			// payArrayList.get(0)[1] + "]", LINE_WIDTH, 1)).append("\n");
			// if (!_pay_type.equals("CASH")&&!_pay_type.equals("CHECK")) {
			// tempSB.append(textHandler.oneColumnLineWithLeftAlignedText("TransID:
			// " + payArrayList.get(0)[4], LINE_WIDTH, 1)).append("\n");
			// tempSB.append(textHandler.oneColumnLineWithLeftAlignedText("CC#:
			// *" + payArrayList.get(0)[5], LINE_WIDTH, 1)).append("\n");
			// }
			// else
			// if (!payArrayList.get(0)[3].isEmpty())
			// receiptSignature = payArrayList.get(0)[3];
			//
			// for (int i = 1; i < size; i++) {
			// _pay_type =
			// payArrayList.get(i)[1].toUpperCase(Locale.getDefault()).trim();
			// tempAmount = tempAmount + formatStrToDouble(
			// payArrayList.get(i)[9]);
			// tempTipAmount = tempTipAmount +
			// formatStrToDouble(payArrayList.get(i)[2]);
			// tempSB.append(textHandler.oneColumnLineWithLeftAlignedText(
			// Global.formatDoubleStrToCurrency(payArrayList.get(i)[9]) + "[" +
			// payArrayList.get(i)[1] + "]", LINE_WIDTH, 1)).append("\n");
			// if (!_pay_type.equals("CASH")&&!_pay_type.equals("CHECK")) {
			// tempSB.append(textHandler.oneColumnLineWithLeftAlignedText("TransID:
			// " + payArrayList.get(i)[4], LINE_WIDTH, 1)).append("\n");
			// tempSB.append(textHandler.oneColumnLineWithLeftAlignedText("CC#:
			// *" + payArrayList.get(i)[5], LINE_WIDTH, 1)).append("\n");
			// }
			// if (!payArrayList.get(i)[3].isEmpty())
			// receiptSignature = payArrayList.get(i)[3];
			// }
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amountpaid),
			// Global.formatDoubleStrToCurrency(Double.toString(tempAmount)),
			// LINE_WIDTH, 0)).append("\n");
			//
			// sb.append(tempSB.toString());
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total_tip_paid),
			// Global.formatDoubleStrToCurrency(Double.toString(tempTipAmount)),
			// LINE_WIDTH, 0)).append("\n");
			//
			// tempAmount = formatStrToDouble(granTotal)-tempAmount;
			// if (tempAmount > 0)
			// tempAmount = 0.00;
			// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cash_returned),
			// Global.formatDoubleStrToCurrency(Double.toString(tempAmount)),
			// LINE_WIDTH, 0))
			// .append("\n\n");
			// }
			//
			// device.write(sb.toString().getBytes(FORMAT), 0,
			// sb.toString().length());
			// device.write(textHandler.newLines(2).getBytes(),0,textHandler.newLines(2).length());
			//
			// if(printPref.contains(MyPreferences.print_footer))
			// this.printFooter();
			//
			//
			//
			//
			// if(Global.isIvuLoto)
			// {
			// byte[] temp;
			//
			// if(isFromHistory)
			// {
			// temp = "**** Copy ****\n\n\n".getBytes(FORMAT);
			// device.write(temp,0, temp.length);
			// }
			// sb.setLength(0);
			//
			// if(!printPref.contains(MyPreferences.print_ivuloto_qr))
			// {
			// for(int i = 0 ; i < size; i++)
			// {
			//
			// sb.append(textHandler.ivuLines(2*LINE_WIDTH/3)).append("\n");
			//
			// sb.append("\t").append("IVULOTO:
			// ").append(payArrayList.get(i)[7]).append("\n");
			// sb.append(payArrayList.get(i)[6]).append("\n");
			//
			// sb.append(textHandler.ivuLines(2*LINE_WIDTH/3)).append("\n");
			// temp = sb.toString().getBytes(FORMAT);
			// device.write(temp,0, temp.length);
			// sb.setLength(0);
			//
			// }
			// }
			// else
			// {
			// for(int i = 0 ;i < size;i++)
			// {
			// encodedQRCode = payArrayList.get(i)[8];
			// this.printImage(2);
			//
			// sb.append(textHandler.ivuLines(2*LINE_WIDTH/3)).append("\n");
			//
			// sb.append("\t").append("IVULOTO:
			// ").append(payArrayList.get(i)[7]).append("\n");
			// sb.append(payArrayList.get(i)[6]).append("\n");
			//
			// sb.append(textHandler.ivuLines(2*LINE_WIDTH/3)).append("\n");
			// temp = sb.toString().getBytes(FORMAT);
			// device.write(temp,0, temp.length);
			// sb.setLength(0);
			// }
			// }
			// device.write(textHandler.newLines(2).getBytes(), 0,
			// textHandler.newLines(2).length());
			// }
			//
			//
			//
			// if (!receiptSignature.isEmpty())
			// {
			// this.encodedSignature = receiptSignature;
			// this.printImage(1);
			// try {
			// Thread.sleep(2000);
			// } catch (InterruptedException e) {
			// }
			//
			// sb.setLength(0);
			// sb.append("x").append(textHandler.lines(LINE_WIDTH /
			// 2)).append("\n");
			// sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(4));
			// device.write(sb.toString().getBytes(FORMAT));
			//
			// }
			// else
			// {
			// receiptSignature = anOrder.ord_signature;
			// if(!receiptSignature.isEmpty())
			// {
			// this.encodedSignature = receiptSignature;
			// this.printImage(1);
			// try {
			// Thread.sleep(2000);
			// } catch (InterruptedException e) {
			// }
			//
			// sb.setLength(0);
			// sb.append("x").append(textHandler.lines(LINE_WIDTH /
			// 2)).append("\n");
			// sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(4));
			// device.write(sb.toString().getBytes(FORMAT));
			// }
			// }
			//
			//
			//

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			return false;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			return false;
		} catch (Exception e) {
			return false;
		} finally {
			releasePrinter();
		}

		return true;
	}

	// private double formatStrToDouble(String val)
	// {
	// if(val==null||val.isEmpty())
	// return 0.00;
	// return Double.parseDouble(val);
	// }

	@Override
	public boolean printPaymentDetails(String payID, int type, boolean isReprint) {
		// TODO Auto-generated method stub
		try {
			if (!device.getIsOpen())
				device.open();

			EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
			printPref = myPref.getPrintingPreferences();
			PaymentsHandler payHandler = new PaymentsHandler(activity);
			String[] payArray = payHandler.getPrintingForPaymentDetails(payID, type);
			StringBuilder sb = new StringBuilder();
			boolean isCashPayment = false;
			boolean isCheckPayment = false;
			String constantValue = getString(R.string.receipt_change);
			String creditCardFooting = "";

			if (payArray[0].equals("Cash"))
				isCashPayment = true;
			else if (payArray[0].equals("Check"))
				isCheckPayment = true;
			else {
				constantValue = getString(R.string.receipt_included_tip);
				creditCardFooting = getString(R.string.receipt_creditcard_terms);
			}

			this.printImage(0);

			if (printPref.contains(MyPreferences.print_header))
				this.printHeader();

			sb.append("* ").append(payArray[0]).append(" Sale *\n\n\n");
			device.write(sb.toString().getBytes(), 0, sb.toString().length());
			sb.setLength(0);
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
					getString(R.string.receipt_time), LINE_WIDTH, 0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(payArray[1], payArray[2], LINE_WIDTH, 0))
					.append("\n\n");

			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer), payArray[3],
					LINE_WIDTH, 0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_idnum), payID, LINE_WIDTH,
					0));

			if (!isCashPayment && !isCheckPayment) {
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cardnum),
						"*" + payArray[9], LINE_WIDTH, 0));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("TransID:", payArray[8], LINE_WIDTH, 0));
			} else if (isCheckPayment) {
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_checknum),
						payArray[10], LINE_WIDTH, 0));
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

			sb.append(textHandler.twoColumnLineWithLeftAlignedText(constantValue,
					Global.formatDoubleStrToCurrency(change), LINE_WIDTH, 0));

			device.write(sb.toString().getBytes(FORMAT), 0, sb.toString().length());

			sb.setLength(0);
			device.write(textHandler.newLines(4).getBytes(), 0, textHandler.newLines(4).length());

			if (!isCashPayment && !isCheckPayment) {
				if (!payArray[7].isEmpty()) {
					encodedSignature = payArray[7];
					this.printImage(1);
				}
				sb.append("x").append(textHandler.lines(LINE_WIDTH / 2)).append("\n");
				sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(4));
				device.write(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
				sb.setLength(0);
			}

			if (printPref.contains(MyPreferences.print_footer))
				this.printFooter();
			String temp = new String();
			if (!isCashPayment && !isCheckPayment) {

				device.write(creditCardFooting.getBytes(FORMAT), 0, creditCardFooting.length());
				temp = textHandler.newLines(4);
				device.write(temp.getBytes(FORMAT), 0, temp.length());
			}
			temp = getString(R.string.receipt_cust_copy) + textHandler.newLines(4);
			device.write(temp.getBytes(FORMAT), 0, temp.length());

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StarIOPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (device != null && !device.getIsOpen())
				device.close();
		}

		return true;
	}

	@Override
	public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
		// TODO Auto-generated method stub
		try {

			if (!device.getIsOpen())
				device.open();

			this.encodedSignature = encodedSignature;
			EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
			StringBuilder sb = new StringBuilder();
			printPref = myPref.getPrintingPreferences();
			// SQLiteDatabase db = new DBManager(activity).openReadableDB();
			ProductsHandler productDBHandler = new ProductsHandler(activity);
			// String value = new String();
			HashMap<String, String> map = new HashMap<String, String>();
			double ordTotal = 0, totalSold = 0, totalReturned = 0, totalDispached = 0, totalLines = 0, returnAmount = 0,
					subtotalAmount = 0;

			int size = myConsignment.size();

			this.printImage(0);

			if (printPref.contains(MyPreferences.print_header))
				this.printHeader();

			sb.append(textHandler.centeredString("Consignment Summary", LINE_WIDTH)).append("\n\n");

			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer),
					myPref.getCustName(), LINE_WIDTH, 0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee),
					myPref.getEmpName(), LINE_WIDTH, 0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
					Global.formatToDisplayDate(Global.getCurrentDate(), activity, 3), LINE_WIDTH, 0));
			sb.append(textHandler.newLines(3));

			for (int i = 0; i < size; i++) {
				if (!myConsignment.get(i).ConsOriginal_Qty.equals("0")) {
					map = productDBHandler.getProductMap(myConsignment.get(i).ConsProd_ID, true);

					sb.append(textHandler.oneColumnLineWithLeftAlignedText(map.get("prod_name"), LINE_WIDTH, 0));

					if (printPref.contains(MyPreferences.print_descriptions)) {
						sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description),
								"", LINE_WIDTH, 3)).append("\n");
						sb.append(textHandler.oneColumnLineWithLeftAlignedText(map.get("prod_desc"), LINE_WIDTH, 5))
								.append("\n");
					} else
						sb.append(textHandler.newLines(1));

					sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original Qty:",
							myConsignment.get(i).ConsOriginal_Qty, LINE_WIDTH, 3));
					sb.append(textHandler.twoColumnLineWithLeftAlignedText("Rack Qty:",
							myConsignment.get(i).ConsStock_Qty, LINE_WIDTH, 3));
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
							.append(textHandler.newLines(2));

					totalSold += Double.parseDouble(myConsignment.get(i).ConsInvoice_Qty);
					totalReturned += Double.parseDouble(myConsignment.get(i).ConsReturn_Qty);
					totalDispached += Double.parseDouble(myConsignment.get(i).ConsDispatch_Qty);
					totalLines += 1;
					ordTotal += Double.parseDouble(myConsignment.get(i).invoice_total);

					// port.writePort(sb.toString().getBytes(FORMAT), 0,
					// sb.toString().length());
					device.write(sb.toString().getBytes(FORMAT));
					sb.setLength(0);
				}
			}

			sb.append(textHandler.lines(LINE_WIDTH));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Sold:", Double.toString(totalSold),
					LINE_WIDTH, 0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Returned",
					Double.toString(totalReturned), LINE_WIDTH, 0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Dispatched",
					Double.toString(totalDispached), LINE_WIDTH, 0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Line Items", Double.toString(totalLines),
					LINE_WIDTH, 0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Grand Total:",
					Global.formatDoubleToCurrency(ordTotal), LINE_WIDTH, 0));
			sb.append(textHandler.newLines(3));

			// port.writePort(sb.toString().getBytes(FORMAT), 0,
			// sb.toString().length());
			device.write(sb.toString().getBytes(FORMAT));

			if (printPref.contains(MyPreferences.print_footer))
				this.printFooter();

			this.printImage(1);

			// port.writePort(textHandler.newLines(3).getBytes(FORMAT), 0,
			// textHandler.newLines(3).length());
			device.write(textHandler.newLines(3).getBytes(FORMAT));

			// db.close();

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
		} catch (Exception e) {

		} finally {
			if (device != null && device.getIsOpen())
				device.close();
		}

		return true;
	}

	@Override
	public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
		// TODO Auto-generated method stub
		try {

			if (!device.getIsOpen())
				device.open();

			EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
			StringBuilder sb = new StringBuilder();
			printPref = myPref.getPrintingPreferences();
			// SQLiteDatabase db = new DBManager(activity).openReadableDB();
			ProductsHandler productDBHandler = new ProductsHandler(activity);
			HashMap<String, String> map = new HashMap<String, String>();

			int size = myConsignment.size();

			this.printImage(0);

			if (printPref.contains(MyPreferences.print_header))
				this.printHeader();

			sb.append(textHandler.centeredString("Consignment Pickup Summary", LINE_WIDTH)).append("\n\n");

			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer),
					myPref.getCustName(), LINE_WIDTH, 0)).append("\n");
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee),
					myPref.getEmpName(), LINE_WIDTH, 0)).append("\n");
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
					Global.formatToDisplayDate(Global.getCurrentDate(), activity, 3), LINE_WIDTH, 0)).append("\n");
			sb.append(textHandler.newLines(3));

			for (int i = 0; i < size; i++) {
				map = productDBHandler.getProductMap(myConsignment.get(i).ConsProd_ID, true);

				sb.append(textHandler.oneColumnLineWithLeftAlignedText(map.get("prod_name"), LINE_WIDTH, 0));

				if (printPref.contains(MyPreferences.print_descriptions)) {
					sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description), "",
							LINE_WIDTH, 3)).append("\n");
					sb.append(textHandler.oneColumnLineWithLeftAlignedText(map.get("prod_desc"), LINE_WIDTH, 5))
							.append("\n");
				}

				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original Qty:",
						myConsignment.get(i).ConsOriginal_Qty, LINE_WIDTH, 3)).append("\n");
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Picked up Qty:",
						myConsignment.get(i).ConsPickup_Qty, LINE_WIDTH, 3)).append("\n");
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("New Qty:", myConsignment.get(i).ConsNew_Qty,
						LINE_WIDTH, 3)).append("\n\n\n");

				device.write(sb.toString().getBytes(FORMAT));
				sb.setLength(0);
			}

			if (printPref.contains(MyPreferences.print_footer))
				this.printFooter();

			if (!encodedSignature.isEmpty()) {
				this.encodedSignature = encodedSignature;
				this.printImage(1);
				sb.setLength(0);
				sb.append("x").append(textHandler.lines(LINE_WIDTH / 2)).append("\n");
				sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(4));
				device.write(sb.toString().getBytes(FORMAT));
			}
			// db.close();

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
		} catch (Exception e) {
		} finally {

			if (device != null && device.getIsOpen())
				device.close();
		}

		return true;
	}

	@Override
	public void printStationPrinter(List<Orders> orders, String ordID) {
		// TODO Auto-generated method stub
		try {

			if (!device.getIsOpen())
				device.open();

			EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
			printPref = myPref.getPrintingPreferences();
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
			Date startedDate = sdf1.parse(anOrder.ord_timecreated);
			Date sentDate = new Date();

			sb.append(getString(R.string.receipt_sent_by)).append(" ").append(myPref.getEmpName()).append(" (");

			if (((float) (sentDate.getTime() - startedDate.getTime()) / 1000) > 60)
				sb.append(Global.formatToDisplayDate(sdf1.format(sentDate.getTime()), activity, 4)).append(")\n\n");
			else
				sb.append(Global.formatToDisplayDate(anOrder.ord_timecreated, activity, 4)).append(")\n\n");

			device.write(sb.toString().getBytes(FORMAT));

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

					device.write(sb.toString().getBytes(FORMAT));
					sb.setLength(0);

				} else {
					ordProdHandler.updateIsPrinted(orders.get(i).getOrdprodID());
					sb.append(orders.get(i).getQty()).append("x ").append(orders.get(i).getName()).append("\n");

					device.write(sb.toString().getBytes(FORMAT));
					sb.setLength(0);
				}
			}
			sb.append(textHandler.newLines(3));
			device.write(sb.toString().getBytes(FORMAT));

			// db.close();

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {

		} finally {
			if (device != null && device.getIsOpen())
				device.close();
		}
	}

	@Override
	public boolean printOpenInvoices(String invID) {
		// TODO Auto-generated method stub
		try {
			if (!device.getIsOpen())
				device.open();

			EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
			StringBuilder sb = new StringBuilder();
			printPref = myPref.getPrintingPreferences();
			String[] rightInfo = new String[] {};
			List<String[]> productInfo = new ArrayList<String[]>();

			InvoicesHandler invHandler = new InvoicesHandler(activity);
			rightInfo = invHandler.getSpecificInvoice(invID);

			InvProdHandler invProdHandler = new InvProdHandler(activity);
			productInfo = invProdHandler.getInvProd(invID);

			this.printImage(0);

			if (printPref.contains(MyPreferences.print_header))
				this.printHeader();

			sb.append(textHandler.centeredString("Open Invoice Summary", LINE_WIDTH)).append("\n\n");

			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_invoice), rightInfo[1],
					LINE_WIDTH, 0)).append("\n");
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_invoice_ref),
					rightInfo[2], LINE_WIDTH, 0)).append("\n");
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer), rightInfo[0],
					LINE_WIDTH, 0)).append("\n");
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_PO), rightInfo[10],
					LINE_WIDTH, 0)).append("\n");
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_terms), rightInfo[9],
					LINE_WIDTH, 0)).append("\n");
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_created), rightInfo[5],
					LINE_WIDTH, 0)).append("\n");
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_ship), rightInfo[7],
					LINE_WIDTH, 0)).append("\n");
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_due), rightInfo[6],
					LINE_WIDTH, 0)).append("\n");
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_paid), rightInfo[8],
					LINE_WIDTH, 0)).append("\n");
			device.write(sb.toString().getBytes(FORMAT), 0, sb.toString().length());

			sb.setLength(0);
			int size = productInfo.size();

			for (int i = 0; i < size; i++) {

				sb.append(textHandler.oneColumnLineWithLeftAlignedText(
						productInfo.get(i)[2] + "x " + productInfo.get(i)[0], LINE_WIDTH, 1));
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
					sb.append(textHandler.newLines(2));

				device.write(sb.toString().getBytes(FORMAT));
				sb.setLength(0);
			}

			sb.append(textHandler.centeredString(getString(R.string.receipt_thankyou), LINE_WIDTH));
			device.write(sb.toString().getBytes(FORMAT));
			device.write(textHandler.newLines(3).getBytes(FORMAT));

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StarIOPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			device.close();
		}

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
		try {

			if (!device.getIsOpen())
				device.open();

			PaymentsHandler paymentHandler = new PaymentsHandler(activity);
			PayMethodsHandler payMethodHandler = new PayMethodsHandler(activity);
			EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
			StringBuilder sb = new StringBuilder();
			StringBuilder sb_refunds = new StringBuilder();
			device.write(textHandler.newLines(3).getBytes(FORMAT));
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
			device.write(sb.toString().getBytes(FORMAT));
			sb.setLength(0);

			for (int i = 0; i < size; i++) {
				if (paymentMap.containsKey(payMethodsNames.get(i)[0])) {
					sb.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
							Global.formatDoubleStrToCurrency(paymentMap.get(payMethodsNames.get(i)[0])), LINE_WIDTH,
							3));
					sb.append("\n");
					payGranTotal += Double.parseDouble(paymentMap.get(payMethodsNames.get(i)[0]));
				} else {
					sb.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
							Global.formatDoubleToCurrency(0.00), LINE_WIDTH, 3));
					sb.append("\n");
				}

				if (refundMap.containsKey(payMethodsNames.get(i)[0])) {
					sb_refunds.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
							Global.formatDoubleStrToCurrency(refundMap.get(payMethodsNames.get(i)[0])), LINE_WIDTH, 3));
					sb_refunds.append("\n");
					refundGranTotal += Double.parseDouble(refundMap.get(payMethodsNames.get(i)[0]));
				} else {

					sb_refunds.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
							Global.formatDoubleToCurrency(0.00), LINE_WIDTH, 3));
					sb_refunds.append("\n");
				}
			}

			sb.append(textHandler.newLines(2));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
					Global.formatDoubleStrToCurrency(Double.toString(payGranTotal)), LINE_WIDTH, 4));
			sb.append(textHandler.newLines(3));

			sb_refunds.append(textHandler.newLines(2));
			sb_refunds.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
					Global.formatDoubleStrToCurrency(Double.toString(refundGranTotal)), LINE_WIDTH, 4));

			device.write(sb.toString().getBytes(FORMAT));
			device.write(sb_refunds.toString().getBytes(FORMAT));
			device.write(textHandler.newLines(5).getBytes(FORMAT));
			device.write("".getBytes(FORMAT));

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			device.close();
		}

		return true;
	}

	@Override
	public void registerPrinter() {
		// TODO Auto-generated method stub
		edm.currentDevice = this;
		this.printingDelegate = edm;
	}

	@Override
	public void unregisterPrinter() {
		// TODO Auto-generated method stub
		edm.currentDevice = null;
		this.printingDelegate = null;
	}

	@Override
	public void loadCardReader(EMSCallBack _callBack, boolean isDebitCard) {
		// TODO Auto-generated method stub

	}

	@Override
	public void releaseCardReader() {
		// TODO Auto-generated method stub

	}

	@Override
	public void openCashDrawer() {
		// TODO Auto-generated method stub

	}
	//
	//
	// @Override
	// public void printHeader() {
	// // TODO Auto-generated method stub
	// EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
	// StringBuilder sb = new StringBuilder();
	//
	// MemoTextHandler handler = new MemoTextHandler(activity);
	// String[] header = handler.getHeader();
	//
	// if(header[0]!=null && !header[0].isEmpty())
	// sb.append(textHandler.centeredString(header[0], LINE_WIDTH));
	// if(header[1]!=null && !header[1].isEmpty())
	// sb.append(textHandler.centeredString(header[1], LINE_WIDTH));
	// if(header[2]!=null && !header[2].isEmpty())
	// sb.append(textHandler.centeredString(header[2], LINE_WIDTH));
	//
	// if(!sb.toString().isEmpty())
	// {
	// sb.append(textHandler.newLines(2));
	// try {
	// device.write(sb.toString().getBytes(FORMAT));
	// } catch (UnsupportedEncodingException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }
	//
	//
	// @Override
	// public void printFooter() {
	// // TODO Auto-generated method stub
	// EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
	// StringBuilder sb = new StringBuilder();
	// MemoTextHandler handler = new MemoTextHandler(activity);
	// String[] footer = handler.getFooter();
	//
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
	//
	// try {
	// device.write(sb.toString().getBytes(FORMAT));
	// } catch (UnsupportedEncodingException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }

	//
	// protected void printImage(int type){
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
	// {
	// if (!encodedQRCode.isEmpty()) {
	// byte[] img = Base64.decode(encodedQRCode, Base64.DEFAULT);
	// myBitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
	// }
	// break;
	// }
	// }
	//
	// if (myBitmap != null)
	// {
	// //print image
	//
	// if(type == 1)
	// {
	// Bitmap bmp = myBitmap.copy(Bitmap.Config.ARGB_8888, true);
	// int w = bmp.getWidth();
	// int h = bmp.getHeight();
	// int pixel;
	// for(int x = 0 ; x < w;x++)
	// {
	// for(int y = 0; y < h;y++)
	// {
	// pixel = bmp.getPixel(x, y);
	// if(pixel==Color.TRANSPARENT)
	// bmp.setPixel(x, y, Color.WHITE);
	//
	// }
	// }
	//
	//
	// documentLP.clear();
	// documentLP.writeImage(bmp, 832);
	//
	// device.write(documentLP.getDocumentData());
	// }
	// else
	// {
	// documentLP.clear();
	// documentLP.writeImage(myBitmap, 832);
	//
	// device.write(documentLP.getDocumentData());
	// }
	// }
	// }

	@Override
	public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
		// TODO Auto-generated method stub
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