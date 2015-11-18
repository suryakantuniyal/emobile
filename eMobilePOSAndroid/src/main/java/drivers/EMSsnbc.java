package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.util.Base64;

import com.android.database.InvProdHandler;
import com.android.database.InvoicesHandler;
import com.android.database.MemoTextHandler;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.PayMethodsHandler;
import com.android.database.PaymentsHandler;
import com.android.database.ProductsHandler;
import com.android.database.StoredPayments_DB;
import com.android.emobilepos.R;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.Orders;
import com.android.support.ConsignmentTransaction;
import com.android.support.DBManager;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import POSAPI.POSInterfaceAPI;
import POSAPI.POSUSBAPI;
import POSSDK.POSSDK;
import main.EMSDeviceManager;
import plaintext.EMSPlainTextHelper;
import protocols.EMSCallBack;
import protocols.EMSDeviceManagerPrinterDelegate;
import util.EMSReceiptHelper;

public class EMSsnbc extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate{
	private final int LINE_WIDTH = 42;
	private String encodedSignature;
	private String encodedQRCode = "";
	private ProgressDialog myProgressDialog;
	private EMSDeviceDriver thisInstance;
	private EMSDeviceManager edm;	
	
	//Returned Value Statement
	private final int POS_SUCCESS=1000;		//success	
	private final int ERR_PROCESSING = 1001;	//processing error
	private final int ERR_PARAM = 1002;		//parameter error
	
	
	public static POSSDK pos_usb = null;
	private POSInterfaceAPI interface_usb = null;
	private int error_code = 0;
	
	@Override
	public void connect(Activity activity,int paperSize,boolean isPOSPrinter,EMSDeviceManager edm) 
	{
		this.activity = activity;
		myPref = new MyPreferences(this.activity);
		thisInstance  = this;
		this.edm = edm;
		interface_usb = new POSUSBAPI(activity);
		new processConnectionAsync().execute(0);
	}

	
	@Override
	public boolean autoConnect(Activity activity,EMSDeviceManager edm,int paperSize,boolean isPOSPrinter, String _portName, String _portNumber)
	{
		boolean didConnect = false;
		this.activity = activity;
		myPref = new MyPreferences(this.activity);
		this.edm = edm;
		thisInstance  = this;
		//isUSBConnected();
		interface_usb = new POSUSBAPI(activity);
		
		
		error_code = interface_usb.OpenDevice();
		if(error_code == POS_SUCCESS)
		{
			pos_usb = new POSSDK(interface_usb);
			pos_sdk = pos_usb;
			if(setupPrinter())
				didConnect = true;	
		}
		else if(error_code == ERR_PROCESSING)
		{
			int _time_out = 0;
			while(error_code == ERR_PROCESSING||_time_out > 10)
			{
				error_code = interface_usb.OpenDevice();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				_time_out++;
			}
			
			if(error_code == POS_SUCCESS)
			{
				pos_usb = new POSSDK(interface_usb);
				pos_sdk = pos_usb;
				if(setupPrinter())
					didConnect = true;	
			}
		}
		
		if (didConnect) {
			this.edm.driverDidConnectToDevice(thisInstance,false);
		} else {

			this.edm.driverDidNotConnectToDevice(thisInstance, null,false);
		}
		

		
		return didConnect;
	}
	
	
	
	public class processConnectionAsync extends
			AsyncTask<Integer, String, String> {

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

			
			
			error_code = interface_usb.OpenDevice();
			if(error_code == POS_SUCCESS)
			{
				pos_usb = new POSSDK(interface_usb);
				pos_sdk = pos_usb;
				
				if(setupPrinter())
					didConnect = true;	
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(String unused) {
			myProgressDialog.dismiss();

			if (didConnect) {
				edm.driverDidConnectToDevice(thisInstance,true);
			} else {

				edm.driverDidNotConnectToDevice(thisInstance, msg,true);
			}

		}
	}
	

	@Override
	public void registerAll() {
		this.registerPrinter();
	}
	
	
	private void printString(String s)
	{
		byte []send_buf = null;
		try {
			send_buf = s.getBytes("GB18030");
			error_code = pos_sdk.textPrint(send_buf, send_buf.length);
			send_buf = null;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean setupPrinter()
	{
		error_code = pos_sdk.textStandardModeAlignment(0);
		if(error_code != POS_SUCCESS)
			return false;
		// set the horizontal and vertical motion units
		pos_sdk.systemSetMotionUnit(100, 100);

		// set line height
		pos_sdk.textSetLineHeight(10);
		int FontStyle = 0;
		int FontType = 0;

		// set character font
		pos_sdk.textSelectFont(FontType, FontStyle);

		// set character size
		pos_sdk.textSelectFontMagnifyTimes(1, 1);
		
		return true;
	}
	
//	private String getString(int id)
//	{
//		return(activity.getResources().getString(id));
//	}
	
	

	@Override
	public boolean printTransaction(String ordID, int type,boolean isFromHistory,boolean fromOnHold) {
		// TODO Auto-generated method stub
		printReceipt(ordID, LINE_WIDTH, fromOnHold, type, isFromHistory);
		
//		printPref = myPref.getPrintingPreferences();
//		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
//		
//		OrderProductsHandler handler = new OrderProductsHandler(activity);
//		OrderTaxes_DB ordTaxesDB = new OrderTaxes_DB(activity);
//		
//		List<DataTaxes> listOrdTaxes = ordTaxesDB.getOrderTaxes(ordID);
//		List<Orders> orders = handler.getPrintOrderedProducts(ordID);
//
//		OrdersHandler orderHandler = new OrdersHandler(activity);
//		Order anOrder = orderHandler.getPrintedOrder(ordID);
//		ClerksHandler clerkHandler = new ClerksHandler(activity);
//		
//		StringBuilder sb = new StringBuilder();
//		int size = orders.size();
//
//		this.printImage(0);
//
//
//		this.printString(textHandler.newLines(2));
//		if(printPref.contains(MyPreferences.print_header))
//			this.printHeader();
//
//		if(anOrder.isVoid.equals("1"))
//			sb.append(textHandler.centeredString("*** VOID ***", LINE_WIDTH));
//		
//		if(fromOnHold)
//		{
//			sb.append(textHandler.twoColumnLineWithLeftAlignedText("["+getString(R.string.on_hold)+"]",anOrder.ord_HoldName , LINE_WIDTH, 0));
//		}
//		
//		
//		switch (type) 
//		{
//		case 0: // Order
//			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.order)+":", ordID, LINE_WIDTH, 0));
//			break;
//		case 1: // Return
//			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.return_tag)+":", ordID, LINE_WIDTH, 0));
//			break;
//		case 2: // Invoice
//			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.invoice)+":", ordID, LINE_WIDTH, 0));
//			break;
//		case 3: // Estimate
//			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.estimate)+":", ordID, LINE_WIDTH, 0));
//			break;
//		case 5: // Sales Receipt
//			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.sales_receipt)+":", ordID, LINE_WIDTH, 0));
//			break;
//		}
//
//		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date), Global.formatToDisplayDate(anOrder.ord_timecreated, activity, 3), LINE_WIDTH, 0));
//		
//		if(!myPref.getShiftIsOpen()||myPref.getPreferences(MyPreferences.pref_use_clerks))
//		{
//			String clerk_id = anOrder.clerk_id;
//			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_clerk),clerkHandler.getClerkName(clerk_id)+ "("+clerk_id+")", LINE_WIDTH,0));
//		}
//		
//		
//		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee), myPref.getEmpName() + "(" + myPref.getEmpID() + ")", LINE_WIDTH, 0));
//
//		String custName = anOrder.cust_name;
//		if (custName != null && !custName.isEmpty())
//			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer), custName, LINE_WIDTH, 0));
//		
//		
//		custName = anOrder.cust_id;
//		if(printPref.contains(MyPreferences.print_customer_id)&&custName!=null && !custName.isEmpty())
//			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer_id), custName, LINE_WIDTH, 0));
//		
//		
//		String ordComment = anOrder.ord_comment;
//		if(ordComment!=null && !ordComment.isEmpty())
//		{
//			sb.append("\n\n");
//			sb.append("Comments:\n");
//			sb.append(textHandler.oneColumnLineWithLeftAlignedText(ordComment, LINE_WIDTH, 3)).append("\n");
//		}
//		
//		sb.append("\n\n");
//		this.printString(sb.toString());
//		
//		sb.setLength(0);
//		
//					
//		if(!myPref.getPreferences(MyPreferences.pref_wholesale_printout))
//		{
//			boolean isRestMode = myPref.getPreferences(MyPreferences.pref_restaurant_mode);
//			
//			int m = 0;
//			for (int i = 0; i < size; i++) 
//			{
//
//				if(isRestMode)
//				{
//					if((i+1<size&&orders.get(i+1).getAddon().equals("1")))
//					{
//						m = i;
//						sb.append(textHandler.oneColumnLineWithLeftAlignedText(orders.get(m).getQty() + "x " + orders.get(m).getName(), LINE_WIDTH, 1));
//						for(int j = i+1;j<size;j++)
//						{
//							if(orders.get(j).getIsAdded().equals("1"))
//								sb.append(textHandler.twoColumnLineWithLeftAlignedText("- "+orders.get(j).getName(), Global.getCurrencyFormat(orders.get(j).getOverwritePrice()), LINE_WIDTH, 2));
//							else
//								sb.append(textHandler.twoColumnLineWithLeftAlignedText("- NO "+orders.get(j).getName(), Global.getCurrencyFormat(orders.get(j).getOverwritePrice()), LINE_WIDTH, 2));
//							
//							if((j+1<size&&orders.get(j+1).getAddon().equals("0"))||(j+1>=size))
//							{
//								i=j;
//								break;
//							}
//							
//						}
//						
//						sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price), Global.getCurrencyFormat(orders.get(m).getOverwritePrice()),
//										LINE_WIDTH, 3)).append("\n");
//						sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total), Global.getCurrencyFormat(orders.get(m).getTotal()), LINE_WIDTH, 3))
//								.append("\n");
//						
//						if(printPref.contains(MyPreferences.print_descriptions))
//						{
//							sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description), "", LINE_WIDTH, 3)).append("\n");
//							sb.append(textHandler.oneColumnLineWithLeftAlignedText(orders.get(m).getProdDescription(), LINE_WIDTH, 5)).append("\n");
//						}
//		
//					}
//					else
//					{
//						sb.append(textHandler.oneColumnLineWithLeftAlignedText(orders.get(i).getQty() + "x " + orders.get(i).getName(), LINE_WIDTH, 1));
//						sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price), Global.getCurrencyFormat(orders.get(i).getOverwritePrice()),
//										LINE_WIDTH, 3)).append("\n");
//						sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total), Global.getCurrencyFormat(orders.get(i).getTotal()), LINE_WIDTH, 3))
//								.append("\n");
//						
//						if(printPref.contains(MyPreferences.print_descriptions))
//						{
//							sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description), "", LINE_WIDTH, 3)).append("\n");
//							sb.append(textHandler.oneColumnLineWithLeftAlignedText(orders.get(i).getProdDescription(), LINE_WIDTH, 5)).append("\n");
//						}
//					}
//				}
//				else
//				{
//					sb.append(textHandler.oneColumnLineWithLeftAlignedText(orders.get(i).getQty() + "x " + orders.get(i).getName(), LINE_WIDTH, 1));
//					sb.append(
//							textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price), Global.getCurrencyFormat(orders.get(i).getOverwritePrice()),
//									LINE_WIDTH, 3)).append("\n");
//					
//					if(orders.get(i).getDiscountID()!=null&&!orders.get(i).getDiscountID().isEmpty())
//					{
//						sb.append(
//								textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_discount),
//										Global.getCurrencyFormat(orders.get(i).getItemDiscount()), LINE_WIDTH, 3)).append("\n");
//					}
//					
//					sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total), Global.getCurrencyFormat(orders.get(i).getTotal()), LINE_WIDTH, 3))
//							.append("\n");
//					
//					if(printPref.contains(MyPreferences.print_descriptions))
//					{
//						sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description), "", LINE_WIDTH, 3)).append("\n");
//						sb.append(textHandler.oneColumnLineWithLeftAlignedText(orders.get(i).getProdDescription(), LINE_WIDTH, 5)).append("\n");
//					}
//	
//				}
//
//			}
//		}
//		else
//		{
//			int padding = LINE_WIDTH / 4;
//			String tempor = Integer.toString(padding);
//			StringBuilder tempSB = new StringBuilder();
//			tempSB.append("%").append(tempor).append("s").append("%").append(tempor).append("s").append("%").append(tempor).append("s").append("%").append(tempor).append("s");
//			
//			
//			sb.append(String.format(tempSB.toString(), "Item","Qty","Price","Total")).append("\n\n");
//			
//			for(int i = 0; i < size; i++)
//			{
//				
//				
//				sb.append(orders.get(i).getName()).append("-").append(orders.get(i).getProdDescription()).append("\n");
//				//sb.append(textHandler.fourColumnLineWithLeftAlignedText("    ", orders.get(i).getQty(), Global.getCurrencyFormat(orders.get(i).getOverwritePrice()), Global.getCurrencyFormat(orders.get(i).getTotal()), LINE_WIDTH, 3)).append("\n\n");
//				sb.append(String.format(tempSB.toString(), "   ",orders.get(i).getQty(),Global.getCurrencyFormat(orders.get(i).getOverwritePrice()),Global.getCurrencyFormat(orders.get(i).getTotal()))).append("\n\n");
//				
//				
//				
//				//this.printString(sb.toString());
//				this.printString(sb.toString());
//				sb.setLength(0);
//				
//			}
//		}
//		this.printString(sb.toString());
//		sb.setLength(0);
//		
//		this.printString(textHandler.lines(LINE_WIDTH));
//		
//		
//		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_subtotal),
//				Global.formatDoubleStrToCurrency(anOrder.ord_subtotal), LINE_WIDTH, 0));
//		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_discount),
//				Global.formatDoubleStrToCurrency(anOrder.ord_discount), LINE_WIDTH, 0));
//		sb.append(
//				textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_tax), Global.formatDoubleStrToCurrency(anOrder.ord_taxamount),
//						LINE_WIDTH, 0));
//		addTaxesLine(listOrdTaxes, OrderTaxes_DB.tax_amount, LINE_WIDTH, sb);
////		int num_taxes = listOrdTaxes.size();
////		if(num_taxes>0)
////		{
////			for(int i = 0;i<num_taxes;i++)
////			{
////				sb.append(textHandler.twoColumnLineWithLeftAlignedText
////						(listOrdTaxes.get(i).get(OrderTaxes_DB.tax_name), 
////								listOrdTaxes.get(i).get(OrderTaxes_DB.tax_amount), LINE_WIDTH, 2));	
////			}
////		}
//
//		sb.append("\n\n");
//		
//		String granTotal = anOrder.gran_total;
//		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_grandtotal), Global.formatDoubleStrToCurrency(granTotal), LINE_WIDTH, 0));
//
//		PaymentsHandler payHandler = new PaymentsHandler(activity);
//		List<String[]> payArrayList = payHandler.getPaymentForPrintingTransactions(ordID);
//		if(myPref.getPreferences(MyPreferences.pref_use_store_and_forward))
//		{
//			StoredPayments_DB dbStoredPay = new StoredPayments_DB(activity);
//			payArrayList.addAll(dbStoredPay.getPaymentForPrintingTransactions(ordID));
//		}
//		
//		String receiptSignature = new String();
//		size = payArrayList.size();
//		
//		double tempGrandTotal = Double.parseDouble(granTotal);
//		double tempAmount = 0;
//		if (size == 0) {
//			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amountpaid), Global.formatDoubleToCurrency(0.00), LINE_WIDTH, 0));
//			if(type==2)//Invoice
//			{
//				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_balance_due), Global.formatDoubleToCurrency(tempGrandTotal-tempAmount), LINE_WIDTH, 0));
//			}
//			//sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total_tip_paid), Global.formatDoubleToCurrency(0.00), LINE_WIDTH, 0));
//			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cash_returned), Global.formatDoubleToCurrency(0.00), LINE_WIDTH, 0));
//		} else {
//			tempAmount = formatStrToDouble(payArrayList.get(0)[9]);
//			String _pay_type = payArrayList.get(0)[1].toUpperCase(Locale.getDefault()).trim();
//			double tempTipAmount = formatStrToDouble(payArrayList.get(0)[2]);
//			StringBuilder tempSB = new StringBuilder();
//			tempSB.append(textHandler.oneColumnLineWithLeftAlignedText(
//					Global.formatDoubleStrToCurrency(payArrayList.get(0)[9]) + "[" + payArrayList.get(0)[1] + "]", LINE_WIDTH, 1));
//			if (!_pay_type.equals("CASH")&&!_pay_type.equals("CHECK")) {
//				tempSB.append(textHandler.oneColumnLineWithLeftAlignedText("TransID: " + payArrayList.get(0)[4], LINE_WIDTH, 1));
//				tempSB.append(textHandler.oneColumnLineWithLeftAlignedText("CC#: *" + payArrayList.get(0)[5], LINE_WIDTH, 1));
//			}
//
//			if (!payArrayList.get(0)[3].isEmpty())
//				receiptSignature = payArrayList.get(0)[3];
//
//			for (int i = 1; i < size; i++) {
//				_pay_type = payArrayList.get(i)[1].toUpperCase(Locale.getDefault()).trim();
//				tempAmount = tempAmount + formatStrToDouble( payArrayList.get(i)[9]);
//				tempTipAmount = tempTipAmount + formatStrToDouble(payArrayList.get(i)[2]);
//				tempSB.append(textHandler.oneColumnLineWithLeftAlignedText(
//						Global.formatDoubleStrToCurrency(payArrayList.get(i)[9]) + "[" + payArrayList.get(i)[1] + "]", LINE_WIDTH, 1));
//				if (!_pay_type.equals("CASH")&&!_pay_type.equals("CHECK")) {
//					tempSB.append(textHandler.oneColumnLineWithLeftAlignedText("TransID: " + payArrayList.get(i)[4], LINE_WIDTH, 1));
//					tempSB.append(textHandler.oneColumnLineWithLeftAlignedText("CC#: *" + payArrayList.get(i)[5], LINE_WIDTH, 1));
//				}
//				if (!payArrayList.get(i)[3].isEmpty())
//					receiptSignature = payArrayList.get(i)[3];
//			}
//			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amountpaid), Global.formatDoubleStrToCurrency(Double.toString(tempAmount)), LINE_WIDTH, 0));
//
//			sb.append(tempSB.toString());
//			if(type==2)//Invoice
//			{
//				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_balance_due), Global.formatDoubleToCurrency(tempGrandTotal-tempAmount), LINE_WIDTH, 0));
//			}
//			if(tempTipAmount>0)
//				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total_tip_paid), Global.formatDoubleStrToCurrency(Double.toString(tempTipAmount)), LINE_WIDTH, 0));
//
//			tempAmount = formatStrToDouble(granTotal)-tempAmount;
//			if (tempAmount > 0)
//				tempAmount = 0.00;
//			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cash_returned), Global.formatDoubleStrToCurrency(Double.toString(tempAmount)), LINE_WIDTH, 0)).append("\n\n");
//		}
//
//		this.printString(sb.toString());
//		this.printString(textHandler.newLines(2));
//		
//		
//		if(printPref.contains(MyPreferences.print_footer))
//			this.printFooter();
//		
//				
//		this.printString(textHandler.newLines(2));
//		receiptSignature = anOrder.ord_signature;
//		if(!receiptSignature.isEmpty())
//		{
//			this.encodedSignature = receiptSignature;
//			this.printImage(1);
//			
//			sb.setLength(0);
//			sb.append("x").append(textHandler.lines(LINE_WIDTH / 2)).append("\n");
//			sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(4));
//			
//			pos_sdk.textStandardModeAlignment(ALIGN_CENTER);
//			this.printString(sb.toString());
//			pos_sdk.textStandardModeAlignment(ALIGN_LEFT);
//		}
//				
//		if (isFromHistory) {
//			this.printString(textHandler.centeredString("*** Copy ***", LINE_WIDTH));
//			this.printString(textHandler.newLines(4));
//		}
//		
//		
//		this.cutPaper();
		return true;
	}
	
	
	
	
//	private double formatStrToDouble(String val)
//	{
//		if(val==null||val.isEmpty())
//			return 0.00;
//		return Double.parseDouble(val);
//	}
//	
	
	

	@Override
	public boolean printPaymentDetails(String payID, int type, boolean isReprint) {
		// TODO Auto-generated method stub
		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		printPref = myPref.getPrintingPreferences();

		PaymentsHandler payHandler = new PaymentsHandler(activity);
		String[] payArray = null;
		boolean isStoredFwd = false;
		long pay_count = payHandler.paymentExist(payID);
		if(pay_count == 0)
		{
			isStoredFwd = true;
			StoredPayments_DB dbStoredPay = new StoredPayments_DB(activity);
			payArray = dbStoredPay.getPrintingForPaymentDetails(payID, type);
		}
		else
		{
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

		this.printImage(0);

		if(printPref.contains(MyPreferences.print_header))
			this.printHeader();
		
		sb.append("* ").append(payArray[0]);
		if(payArray[11].equals("1"))
			sb.append(" Refund *\n\n\n");
		else
			sb.append(" Sale *\n\n\n");
		
		this.printString(textHandler.centeredString(sb.toString(), LINE_WIDTH));
		sb.setLength(0);
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date), getString(R.string.receipt_time), LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(payArray[1], payArray[2], LINE_WIDTH, 0)).append("\n\n");

		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer), payArray[3], LINE_WIDTH, 0));
		
		if(payArray[17]!=null&&!payArray[17].isEmpty())
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_order_id), payArray[17], LINE_WIDTH, 0));
		else if(payArray[16]!=null&&!payArray[16].isEmpty())
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_invoice_ref), payArray[16], LINE_WIDTH, 0));
		
		if(!isStoredFwd)
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_idnum), payID, LINE_WIDTH, 0));

		if (!isCashPayment && !isCheckPayment) {
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cardnum), "*" + payArray[9], LINE_WIDTH, 0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("TransID:", payArray[8], LINE_WIDTH, 0));
		} else if (isCheckPayment) {
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_checknum), payArray[10], LINE_WIDTH, 0));
		}

		sb.append(textHandler.newLines(2));

		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total), Global.formatDoubleStrToCurrency(payArray[4]), LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_paid), Global.formatDoubleStrToCurrency(payArray[15]), LINE_WIDTH, 0));

		String change = payArray[6];
		double tempChange = 0;
		try
		{
			tempChange = Double.parseDouble(change);
		}
		catch(Exception e)
		{
			tempChange = 0;
		}
		if (isCashPayment && isCheckPayment && !change.isEmpty() && change.contains(".") && Double.parseDouble(change) > 0)
			change = "";

		
		if(constantValue!=null&&(constantValue.toUpperCase(Locale.getDefault()).contains("TIP")&&tempChange<=0))
			constantValue = null;
		
		if(constantValue!=null)
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(constantValue, Global.formatDoubleStrToCurrency(change), LINE_WIDTH, 0));

		this.printString(sb.toString());

		sb.setLength(0);
		this.printString(textHandler.newLines(4));

		if (!isCashPayment && !isCheckPayment) {
			pos_sdk.textStandardModeAlignment(ALIGN_CENTER);
			if(myPref.getPreferences(MyPreferences.pref_handwritten_signature))
			{
				sb.append(textHandler.newLines(4));
			}
			else if (!payArray[7].isEmpty()) {
				encodedSignature = payArray[7];
				this.printImage(1);
			}
			sb.append("x").append(textHandler.lines(LINE_WIDTH / 2)).append("\n");
			sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(4));
			this.printString(sb.toString());
			pos_sdk.textStandardModeAlignment(ALIGN_LEFT);
			sb.setLength(0);
		}

		
		if (Global.isIvuLoto) 
		{
			sb.setLength(0);

			if (!printPref.contains(MyPreferences.print_ivuloto_qr)) {
				this.printString("\n");
				this.printString(textHandler.centeredString(textHandler.ivuLines(2 * LINE_WIDTH / 3), LINE_WIDTH));
				this.printString(textHandler.centeredString("IVULOTO: " + payArray[13], LINE_WIDTH));
				this.printString(textHandler.centeredString(payArray[12], LINE_WIDTH));
				this.printString(textHandler.centeredString(textHandler.ivuLines(2 * LINE_WIDTH / 3), LINE_WIDTH));
				this.printString("\n");
			} else {
				encodedQRCode = payArray[14];

				this.printImage(2);

				sb.append(textHandler.ivuLines(2 * LINE_WIDTH / 3)).append("\n");
				sb.append("\t").append("IVULOTO: ").append(payArray[13]).append("\n");
				sb.append(payArray[12]).append("\n");
				sb.append(textHandler.ivuLines(2 * LINE_WIDTH / 3)).append("\n");

				this.printString(sb.toString());

				sb.setLength(0);
			}
		}
		
		this.printFooter();

		
		if (!isCashPayment && !isCheckPayment) {

			pos_sdk.textStandardModeAlignment(ALIGN_CENTER);
			this.printString((creditCardFooting));
			pos_sdk.textStandardModeAlignment(ALIGN_LEFT);
			this.printString(textHandler.newLines(4));
		}
		
		if (isReprint) {
			sb.append(textHandler.centeredString("*** Copy ***", LINE_WIDTH));
			this.printString(sb.toString());
		}
		this.cutPaper();
		
		return true;
	}

	
	protected void printImage(int type) {

		Bitmap myBitmap = null;
		switch (type) {
		case 0: // Logo
		{
			File imgFile = new File(myPref.getAccountLogoPath());
			if (imgFile.exists()) {
				myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
				
			}
			break;
		}
		case 1: // signature
		{
			if (!encodedSignature.isEmpty()) {
				byte[] img = Base64.decode(encodedSignature, Base64.DEFAULT);
				myBitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
			}
			break;
		}
		case 2: {
			if (!encodedQRCode.isEmpty()) {
				byte[] img = Base64.decode(encodedQRCode, Base64.DEFAULT);
				myBitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
			}
			break;
		}
		}

		if (myBitmap != null) {
			int PrinterWidth = 640;

			// download bitmap
			pos_sdk.textStandardModeAlignment(ALIGN_CENTER);
			pos_sdk.imageStandardModeRasterPrint(myBitmap, PrinterWidth);
			pos_sdk.textStandardModeAlignment(ALIGN_LEFT);
		}

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
	public void printEndOfDayReport(String date, String clerk_id)
	{
		EMSReceiptHelper em = new EMSReceiptHelper(activity,42);
		String t = em.getEndOfDayReportReceipt(clerk_id,Global.getCurrentDate());
		this.printString(t);
		pos_sdk.systemFeedLine(5);
		cutPaper();
	}
	
	
	@Override
	public boolean printReport(String curDate) {
		// TODO Auto-generated method stub

		PaymentsHandler paymentHandler = new PaymentsHandler(activity);
		PayMethodsHandler payMethodHandler = new PayMethodsHandler(activity);
		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		StringBuilder sb = new StringBuilder();
		StringBuilder sb_refunds = new StringBuilder();
		sb.append(textHandler.centeredString("REPORT", LINE_WIDTH));
		sb.append(textHandler.centeredString(Global.formatToDisplayDate(curDate, activity, 0), LINE_WIDTH));
		sb.append(textHandler.centeredString("Device: "+myPref.getEmpName()+"("+myPref.getEmpID()+")", LINE_WIDTH));
		sb.append(textHandler.newLines(2));
		sb.append(textHandler.oneColumnLineWithLeftAlignedText(getString(R.string.receipt_pay_summary), LINE_WIDTH, 0));
		sb_refunds.append(textHandler.oneColumnLineWithLeftAlignedText(getString(R.string.receipt_refund_summmary), LINE_WIDTH, 0));

		HashMap<String, String> paymentMap = paymentHandler.getPaymentsRefundsForReportPrinting(
				Global.formatToDisplayDate(curDate, activity, 4), 0);
		HashMap<String, String> refundMap = paymentHandler.getPaymentsRefundsForReportPrinting(
				Global.formatToDisplayDate(curDate, activity, 4), 1);
		List<String[]> payMethodsNames = payMethodHandler.getPayMethodsName();
		int size = payMethodsNames.size();
		double payGranTotal = 0.00;
		double refundGranTotal = 0.00;

		this.printString(sb.toString());

		sb.setLength(0);

		for (int i = 0; i < size; i++) {
			if (paymentMap.containsKey(payMethodsNames.get(i)[0])) {
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
						Global.formatDoubleStrToCurrency(paymentMap.get(payMethodsNames.get(i)[0])), LINE_WIDTH, 3));

				payGranTotal += Double.parseDouble(paymentMap.get(payMethodsNames.get(i)[0]));
			} else
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1], Global.formatDoubleToCurrency(0.00),
						LINE_WIDTH, 3));

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

		
		this.printString(sb.toString());
		this.printString(sb_refunds.toString());

		// feed line
		pos_sdk.systemFeedLine(5);
		cutPaper();
		
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
		edm.currentDevice= null;
	}


	public void printHeader() {

		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		StringBuilder sb = new StringBuilder();

		MemoTextHandler handler = new MemoTextHandler(activity);
		String[] header = handler.getHeader();

		if(header[0]!=null&&!header[0].isEmpty())
			sb.append(textHandler.formatLongString(header[0], LINE_WIDTH)).append("\n");
		if(header[1]!=null&&!header[1].isEmpty())
			sb.append(textHandler.formatLongString(header[1], LINE_WIDTH)).append("\n");
		if(header[2]!=null&&!header[2].isEmpty())
			sb.append(textHandler.formatLongString(header[2], LINE_WIDTH)).append("\n");
		
		
		if(!sb.toString().isEmpty())
		{
			sb.append(textHandler.newLines(2));
			pos_sdk.textStandardModeAlignment(ALIGN_CENTER);
			this.printString(sb.toString());
			pos_sdk.textStandardModeAlignment(ALIGN_LEFT);
		}
	}
	

	public void printFooter() {

		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		StringBuilder sb = new StringBuilder();
		MemoTextHandler handler = new MemoTextHandler(activity);
		String[] footer = handler.getFooter();
		
		if(footer[0]!=null&&!footer[0].isEmpty())
			sb.append(textHandler.formatLongString(footer[0], LINE_WIDTH)).append("\n");
		if(footer[1]!=null&&!footer[1].isEmpty())
			sb.append(textHandler.formatLongString(footer[1], LINE_WIDTH)).append("\n");
		if(footer[2]!=null&&!footer[2].isEmpty())
			sb.append(textHandler.formatLongString(footer[2], LINE_WIDTH)).append("\n");
		

		if(!sb.toString().isEmpty())
		{
			sb.append(textHandler.newLines(2));
			pos_sdk.textStandardModeAlignment(ALIGN_CENTER);
			this.printString(sb.toString());
			pos_sdk.textStandardModeAlignment(ALIGN_LEFT);
		}
		
	}
	
	public void cutPaper() {

		// ******************************************************************************************
		// print in page mode
		error_code = pos_sdk.pageModePrint();

		error_code = pos_sdk.systemCutPaper(66, 0);

		// *****************************************************************************************
		// clear buffer in page mode
		error_code = pos_sdk.pageModeClearBuffer();
	}

	@Override
	public boolean printConsignment(List<ConsignmentTransaction> myConsignment,String encodedSig) 
	{
		this.encodedSignature = encodedSig;
		printPref = myPref.getPrintingPreferences();
		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		StringBuilder sb = new StringBuilder();
		//SQLiteDatabase db = new DBManager(activity).openReadableDB();
		ProductsHandler productDBHandler = new ProductsHandler(activity);
		//String value = new String();
		HashMap<String,String>map = new HashMap<String,String>();
		double ordTotal = 0,totalSold = 0,totalReturned = 0,totalDispached = 0,totalLines = 0,returnAmount=0,subtotalAmount=0;
		
		int size = myConsignment.size();
		
		this.printImage(0);
		
		if(printPref.contains(MyPreferences.print_header))
			this.printHeader();
		
		
		sb.append(textHandler.centeredString("Consignment Summary", LINE_WIDTH)).append("\n\n");
		
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer), myPref.getCustName(), LINE_WIDTH,0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee), myPref.getEmpName(), LINE_WIDTH,0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cons_trans_id), myConsignment.get(0).ConsTrans_ID, LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),Global.formatToDisplayDate(Global.getCurrentDate(),activity,3), LINE_WIDTH,0));
		sb.append(textHandler.newLines(3));
		
		for(int i = 0 ; i < size;i++)
		{
			if(!myConsignment.get(i).ConsOriginal_Qty.equals("0"))
			{
				map = productDBHandler.getProductMap(myConsignment.get(i).ConsProd_ID,true);
				
				sb.append(textHandler.oneColumnLineWithLeftAlignedText(map.get("prod_name"), LINE_WIDTH, 0));
				
				if(printPref.contains(MyPreferences.print_descriptions))
				{
					sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description), "", LINE_WIDTH, 3)).append("\n");
					sb.append(textHandler.oneColumnLineWithLeftAlignedText(map.get("prod_desc"), LINE_WIDTH, 5)).append("\n");
				}
				else
					sb.append(textHandler.newLines(1));
				

				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original Qty:", myConsignment.get(i).ConsOriginal_Qty, LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Rack Qty:",myConsignment.get(i).ConsStock_Qty, LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Returned Qty:", myConsignment.get(i).ConsReturn_Qty, LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Sold Qty:", myConsignment.get(i).ConsInvoice_Qty, LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Dispatched Qty:", myConsignment.get(i).ConsDispatch_Qty, LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("New Qty:", myConsignment.get(i).ConsNew_Qty, LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Product Price:", Global.formatDoubleStrToCurrency(map.get("prod_price")), LINE_WIDTH, 5));
				
				returnAmount = Global.formatNumFromLocale(myConsignment.get(i).ConsReturn_Qty)*Global.formatNumFromLocale(map.get("prod_price"));
				subtotalAmount = Global.formatNumFromLocale(myConsignment.get(i).invoice_total)+returnAmount;
				
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Subtotal:", Global.formatDoubleToCurrency(subtotalAmount),LINE_WIDTH,5));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Credit Memo:", Global.formatDoubleToCurrency(returnAmount),LINE_WIDTH,5));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total:", Global.formatDoubleStrToCurrency(myConsignment.get(i).invoice_total), LINE_WIDTH, 5)).append(textHandler.newLines(2));
				
				totalSold += Double.parseDouble( myConsignment.get(i).ConsInvoice_Qty);
				totalReturned+=Double.parseDouble(myConsignment.get(i).ConsReturn_Qty);
				totalDispached+=Double.parseDouble(myConsignment.get(i).ConsDispatch_Qty);
				totalLines+=1;
				ordTotal += Double.parseDouble(myConsignment.get(i).invoice_total);
				
				
				this.printString(sb.toString());
				sb.setLength(0);
			}
		}
		
		
		sb.append(textHandler.lines(LINE_WIDTH));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Sold:", Double.toString(totalSold), LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Returned", Double.toString(totalReturned), LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Dispatched", Double.toString(totalDispached), LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Line Items", Double.toString(totalLines), LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText("Grand Total:", Global.formatDoubleToCurrency(ordTotal), LINE_WIDTH, 0));
		sb.append(textHandler.newLines(3));
		
		this.printString(sb.toString());
		
		if(printPref.contains(MyPreferences.print_descriptions))
			this.printFooter();
		
		this.printImage(1);
		this.printString(textHandler.newLines(3));
		
		this.cutPaper();
		
		
		//db.close();
		
		return true;
	}

	
	
	@Override
	public void releaseCardReader() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void loadCardReader(EMSCallBack _callBack, boolean isDebitCard) {
		// TODO Auto-generated method stub
	}
	
	
	
	
	@Override
	public boolean printConsignmentPickup(
			List<ConsignmentTransaction> myConsignment, String encodedSig) {
		printPref = myPref.getPrintingPreferences();
		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		StringBuilder sb = new StringBuilder();
		//SQLiteDatabase db = new DBManager(activity).openReadableDB();
		ProductsHandler productDBHandler = new ProductsHandler(activity);
		HashMap<String,String>map = new HashMap<String,String>();
		String prodDesc = "";
		
		
		int size = myConsignment.size();
		
		
		this.printImage(0);
		
		
		if(printPref.contains(MyPreferences.print_header))
			this.printHeader();
		
		
		sb.append(textHandler.centeredString("Consignment Pickup Summary", LINE_WIDTH)).append("\n\n");
		
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer), myPref.getCustName(), LINE_WIDTH,0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee), myPref.getEmpName(), LINE_WIDTH,0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),Global.formatToDisplayDate(Global.getCurrentDate(),activity,3), LINE_WIDTH,0));
		sb.append(textHandler.newLines(3));
		
		for(int i = 0 ; i < size;i++)
		{
			map = productDBHandler.getProductMap(myConsignment.get(i).ConsProd_ID,true);
			
			sb.append(textHandler.oneColumnLineWithLeftAlignedText(map.get("prod_name"), LINE_WIDTH, 0));
			
			if(printPref.contains(MyPreferences.print_descriptions))
			{
				prodDesc = map.get("prod_desc");
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description), "", LINE_WIDTH, 3)).append("\n");
				if(!prodDesc.isEmpty())
					sb.append(textHandler.oneColumnLineWithLeftAlignedText(prodDesc, LINE_WIDTH, 5)).append("\n");
			}

			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original Qty:", myConsignment.get(i).ConsOriginal_Qty, LINE_WIDTH, 3));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Picked up Qty:", myConsignment.get(i).ConsPickup_Qty, LINE_WIDTH, 3));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("New Qty:", myConsignment.get(i).ConsNew_Qty, LINE_WIDTH, 3)).append("\n\n\n");

			//port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
			this.printString(sb.toString());
			sb.setLength(0);
		}
		
		
		if(printPref.contains(MyPreferences.print_footer))
			this.printFooter();
		
		if(!encodedSig.isEmpty())
		{
			this.encodedSignature = encodedSig;
			this.printImage(1);
			pos_sdk.textStandardModeAlignment(ALIGN_CENTER);
			sb.setLength(0);
			sb.append("x").append(textHandler.lines(LINE_WIDTH / 2)).append("\n");
			sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(4));
			this.printString(sb.toString());
			pos_sdk.textStandardModeAlignment(ALIGN_LEFT);
		}
		
		this.cutPaper();
		//db.close();
		
		return true;
	}

	@Override
	public boolean printOpenInvoices(String invID) {
		// TODO Auto-generated method stub
		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		StringBuilder sb = new StringBuilder();
		String[] rightInfo = new String[]{};
		List<String[]> productInfo = new ArrayList<String[]>();
		printPref = myPref.getPrintingPreferences();


		InvoicesHandler invHandler = new InvoicesHandler(activity);
		rightInfo = invHandler.getSpecificInvoice(invID);
		
		InvProdHandler invProdHandler = new InvProdHandler(activity);
		productInfo = invProdHandler.getInvProd(invID);
		
		
		this.printImage(0);
		
		
		if(printPref.contains(MyPreferences.print_header))
			this.printHeader();
		
		
		sb.append(textHandler.centeredString("Open Invoice Summary", LINE_WIDTH)).append("\n\n");
		
		
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_invoice), rightInfo[1], LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_invoice_ref), rightInfo[2], LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer), rightInfo[0], LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_PO), rightInfo[10], LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_terms), rightInfo[9], LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_created), rightInfo[5], LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_ship), rightInfo[7], LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_due), rightInfo[6], LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_paid), rightInfo[8], LINE_WIDTH, 0));
		this.printString(sb.toString());
		
		sb.setLength(0);
		int size = productInfo.size();
		
		
		for (int i = 0; i < size; i++) 
		{

			sb.append(textHandler.oneColumnLineWithLeftAlignedText(productInfo.get(i)[2]+ "x " + productInfo.get(i)[0], LINE_WIDTH, 1));
			sb.append(
					textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price), Global.getCurrencyFormat(productInfo.get(i)[3]),
							LINE_WIDTH, 3)).append("\n");
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total), Global.getCurrencyFormat(productInfo.get(i)[5]), LINE_WIDTH, 3))
					.append("\n");
			
			if(printPref.contains(MyPreferences.print_descriptions))
			{
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description), "", LINE_WIDTH, 3)).append("\n");
				sb.append(textHandler.oneColumnLineWithLeftAlignedText(productInfo.get(i)[1], LINE_WIDTH, 5)).append("\n\n");
			}
			else
				sb.append(textHandler.newLines(2));

			
			this.printString(sb.toString());
			sb.setLength(0);
		}
	
		
		sb.append(textHandler.centeredString(getString(R.string.receipt_thankyou), LINE_WIDTH));
		this.printString(sb.toString());
		this.printString(textHandler.newLines(3));
		
		this.cutPaper();
		
		return true;
	}

	
	
	
	@Override
	public void printStationPrinter(List<Orders>orders,String ordID) 
	{
		// TODO Auto-generated method stub
		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();

		OrdersHandler orderHandler = new OrdersHandler(activity);
		OrderProductsHandler ordProdHandler = new OrderProductsHandler(activity);
		DBManager dbManager = new DBManager(activity);
		//SQLiteDatabase db = dbManager.openWritableDB();
		Order anOrder = orderHandler.getPrintedOrder(ordID);

		StringBuilder sb = new StringBuilder();
		int size = orders.size();



		if(!anOrder.ord_HoldName.isEmpty())
			sb.append(getString(R.string.receipt_name)).append(anOrder.ord_HoldName).append("\n");
		
		sb.append(getString(R.string.order)).append(": ").append(ordID).append("\n");
		sb.append(getString(R.string.receipt_started)).append(" ").append(Global.formatToDisplayDate(anOrder.ord_timecreated, activity, 4)).append("\n");
		
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.getDefault());
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
		
		if(((float)(sentDate.getTime()-startedDate.getTime())/1000)>60)
			sb.append(Global.formatToDisplayDate(sdf1.format(sentDate.getTime()), activity, 4)).append(")");
		else
			sb.append(Global.formatToDisplayDate(anOrder.ord_timecreated, activity, 4)).append(")");

		
		String ordComment = anOrder.ord_comment;
		if(ordComment!=null && !ordComment.isEmpty())
		{
			sb.append("Comments:\n");
			sb.append(textHandler.oneColumnLineWithLeftAlignedText(ordComment, LINE_WIDTH, 3)).append("\n");
		}
		
		sb.append("\n\n");
		
		this.printString(sb.toString());
		
		sb.setLength(0);
		
		
		int m = 0;
		for (int i = 0; i < size; i++) 
		{

				if(orders.get(i).getHasAddon().equals("1"))
				{
					m = i;
					ordProdHandler.updateIsPrinted(orders.get(m).getOrdprodID());
					sb.append(orders.get(m).getQty()).append("x ").append(orders.get(m).getName()).append("\n");
					if(!orders.get(m).getAttrDesc().isEmpty())
						sb.append("  [").append(orders.get(m).getAttrDesc()).append("]\n");
					for(int j = i+1;j<size;j++)
					{
						ordProdHandler.updateIsPrinted(orders.get(j).getOrdprodID());
						if(orders.get(j).getIsAdded().equals("1"))
							sb.append("  ").append(orders.get(j).getName()).append("\n");
						else
							sb.append("  NO ").append(orders.get(j).getName()).append("\n");
							
						if((j+1<size&&orders.get(j+1).getAddon().equals("0"))||(j+1>=size))
						{
							i=j;
							break;
						}
					}
					this.printString(sb.toString());
					sb.setLength(0);
				}
				else
				{
					ordProdHandler.updateIsPrinted(orders.get(i).getOrdprodID());
					sb.append(orders.get(i).getQty()).append("x ").append(orders.get(i).getName()).append("\n");
					
					this.printString(sb.toString());
					sb.setLength(0);
				}
		}
		sb.append(textHandler.newLines(3));
		this.printString(sb.toString());
		
		this.cutPaper();
		//db.close();
	}

	@Override
	public void openCashDrawer() {
		// TODO Auto-generated method stub
		
		
		new Thread(new Runnable() {
			   public void run() {
				   pos_sdk.cashdrawerOpen(0, 100, 100);
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
		this.printImage(0);

		if (printPref.contains(MyPreferences.print_header))
			this.printHeader();

		if (!isPickup)
			sb.append(textHandler.centeredString(getString(R.string.consignment_summary), LINE_WIDTH)).append("\n\n");
		else
			sb.append(textHandler.centeredString(getString(R.string.consignment_pickup), LINE_WIDTH)).append("\n\n");

		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer), map.get("cust_name"), LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee), myPref.getEmpName(), LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cons_trans_id), map.get("ConsTrans_ID"),
				LINE_WIDTH, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
				Global.formatToDisplayDate(Global.getCurrentDate(), activity, 3), LINE_WIDTH, 0));
		sb.append(textHandler.newLines(3));

		for (int i = 0; i < size; i++) {
			c.moveToPosition(i);

			sb.append(textHandler.oneColumnLineWithLeftAlignedText(c.getString(c.getColumnIndex("prod_name")), LINE_WIDTH, 0));

			if (printPref.contains(MyPreferences.print_descriptions)) {
				prodDesc = c.getString(c.getColumnIndex("prod_desc"));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description), "", LINE_WIDTH, 3)).append(
						"\n");
				if (!prodDesc.isEmpty())
					sb.append(textHandler.oneColumnLineWithLeftAlignedText(c.getString(c.getColumnIndex("prod_desc")), LINE_WIDTH, 5))
							.append("\n");
			} else
				sb.append(textHandler.newLines(1));

			if (!isPickup) {
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original Qty:", c.getString(c.getColumnIndex("ConsOriginal_Qty")),
						LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Rack Qty:", c.getString(c.getColumnIndex("ConsStock_Qty")),
						LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Returned Qty:", c.getString(c.getColumnIndex("ConsReturn_Qty")),
						LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Sold Qty:", c.getString(c.getColumnIndex("ConsInvoice_Qty")),
						LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Dispatched Qty:",
						c.getString(c.getColumnIndex("ConsDispatch_Qty")), LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("New Qty:", c.getString(c.getColumnIndex("ConsNew_Qty")),
						LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Product Price:",
						Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex("price"))), LINE_WIDTH, 5));

				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Subtotal:",
						Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex("item_subtotal"))), LINE_WIDTH, 5));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Credit Memo:",
						Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex("credit_memo"))), LINE_WIDTH, 5));

				sb.append(
						textHandler.twoColumnLineWithLeftAlignedText("Total:",
								Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex("item_total"))), LINE_WIDTH, 5)).append(
						textHandler.newLines(2));
			} else {
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original Qty:", c.getString(c.getColumnIndex("ConsOriginal_Qty")),
						LINE_WIDTH, 3));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("Picked up Qty:", c.getString(c.getColumnIndex("ConsPickup_Qty")),
						LINE_WIDTH, 3));
				sb.append(
						textHandler.twoColumnLineWithLeftAlignedText("New Qty:", c.getString(c.getColumnIndex("ConsNew_Qty")), LINE_WIDTH,
								3)).append("\n\n\n");

			}
			this.printString(sb.toString());
			sb.setLength(0);

		}

		sb.append(textHandler.lines(LINE_WIDTH));
		if (!isPickup) {
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Sold:", map.get("total_items_sold"), LINE_WIDTH, 0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Returned", map.get("total_items_returned"), LINE_WIDTH, 0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Dispatched", map.get("total_items_dispatched"), LINE_WIDTH,
					0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Line Items", map.get("total_line_items"), LINE_WIDTH, 0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText("Grand Total:",
					Global.formatDoubleStrToCurrency(map.get("total_grand_total")), LINE_WIDTH, 0));
		}
		sb.append(textHandler.newLines(3));

		this.printString(sb.toString());

		if (printPref.contains(MyPreferences.print_footer))
			this.printFooter();

		this.printImage(1);

		this.printString(textHandler.newLines(3));

		this.cutPaper();
		
		return true;
	}
	
	@Override
	public void loadScanner(EMSCallBack _callBack) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public boolean isUSBConnected()
	{
		UsbManager manager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);

		HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

		while (deviceIterator.hasNext()) {

			UsbDevice device = deviceIterator.next();
			if((device.getVendorId()==7306&&device.getProductId()==515))
				return true;

		}
		return false;
	}
}
