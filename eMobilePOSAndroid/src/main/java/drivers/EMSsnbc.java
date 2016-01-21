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

import com.android.database.MemoTextHandler;
import com.android.database.ProductsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.Orders;
import com.android.support.ConsignmentTransaction;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import POSAPI.POSInterfaceAPI;
import POSAPI.POSUSBAPI;
import POSSDK.POSSDK;
import main.EMSDeviceManager;
import plaintext.EMSPlainTextHelper;
import protocols.EMSCallBack;
import protocols.EMSDeviceManagerPrinterDelegate;

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
	public boolean printTransaction(String ordID, Global.OrderType type,boolean isFromHistory,boolean fromOnHold) {
		// TODO Auto-generated method stub
		printReceipt(ordID, LINE_WIDTH, fromOnHold, type, isFromHistory);
		
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
		printPaymentDetailsReceipt(payID,type, isReprint, LINE_WIDTH);

		
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
	public void playSound() {

	}

//	@Override
//	public void printEndOfDayReport(String date, String clerk_id)
//	{
//		EMSReceiptHelper em = new EMSReceiptHelper(activity,42);
//		String t = em.getEndOfDayReportReceipt(clerk_id,Global.getCurrentDate());
//		this.printString(t);
//		pos_sdk.systemFeedLine(5);
//		cutPaper();
//	}

	@Override
	public void printEndOfDayReport(String curDate, String clerk_id, boolean printDetails) {
		printEndOfDayReportReceipt(curDate, LINE_WIDTH, printDetails);
	}

	@Override
	public void printShiftDetailsReport(String shiftID) {
		printShiftDetailsReceipt(LINE_WIDTH, shiftID);
	}


	@Override
	public boolean printReport(String curDate) {
		// TODO Auto-generated method stub

		printReportReceipt(curDate, LINE_WIDTH);

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
	public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSig) {

		printConsignmentReceipt(myConsignment, encodedSig, LINE_WIDTH);


		return true;
	}

	/*
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
*/

	
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
		printOpenInvoicesReceipt(invID, LINE_WIDTH);

		return true;
	}

	
	
	
	@Override
	public void printStationPrinter(List<Orders>orders,String ordID) 
	{
		printStationPrinterReceipt(orders, ordID,LINE_WIDTH);

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

		printConsignmentHistoryReceipt(map, c, isPickup, LINE_WIDTH);

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
