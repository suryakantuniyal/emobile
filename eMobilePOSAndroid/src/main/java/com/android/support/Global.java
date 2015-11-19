package com.android.support;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.multidex.MultiDexApplication;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.crashreport.ExceptionHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.holders.Locations_Holder;
import com.android.emobilepos.holders.TransferInventory_Holder;
import com.android.emobilepos.holders.TransferLocations_Holder;
import com.android.emobilepos.models.DataTaxes;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.OrderProducts;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.ordering.Catalog_FR;
import com.android.emobilepos.ordering.OrdProdAttrHolder;
import com.android.emobilepos.ordering.OrderingMain_FA;
import com.android.emobilepos.payment.ProcessCash_FA;
import com.android.emobilepos.payment.ProcessCreditCard_FA;
import com.android.support.GenerateNewID.IdType;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.zzzapi.uart.uart;

import org.springframework.util.support.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import drivers.EMSPAT100;
import main.EMSDeviceManager;

public class Global extends MultiDexApplication {

	@Override
	public void onCreate() {
		super.onCreate();
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		isIvuLoto=getPackageName().contains(getString(R.string.ivupos_packageid));
	}

	public static boolean isIvuLoto = false;
	public static boolean isForceUpload = false;
	public static boolean isEncryptSwipe = true;

	public static EMSDeviceManager btSwiper;
	public static EMSDeviceManager terminalDisplayManager;
	public static EMSDeviceManager btSled;
	public static EMSDeviceManager mainPrinterManager;
	public static HashMap<String, Integer> multiPrinterMap = new HashMap<String, Integer>();
	public static List<EMSDeviceManager> multiPrinterManager = new ArrayList<EMSDeviceManager>();

	public static final int BT_TYPE_PRINTER = 0;
	public static final int BT_TYPE_SWIPER = 1;
	public static final int BT_TYPE_SLED = 2;

	public static final int MAGTEK = 0;
	public static final int STAR = 1;
	public static final int ZEBRA = 2;
	public static final int BAMBOO = 3;
	public static final int ONEIL = 4;
	public static final int SNBC = 5;
	public static final int POWA = 6;
	public static final int ASURA = 7;
	public static final int PAT100 = 8;
	public static final int ISMP = 9;

	public static final String AUDIO_MSR_UNIMAG = "0";
	public static final String AUDIO_MSR_MAGTEK = "1";
	public static final String AUDIO_MSR_ROVER = "2";
	public static final String AUDIO_MSR_WALKER = "3";


	public final static String IS_ORDER = "0";
	public final static String IS_RETURN = "1";
	public final static String IS_INVOICE = "2";
	public final static String IS_ESTIMATE = "3";
	public final static String IS_CONSIGNMENT_FILLUP = "4";
	public final static String IS_SALES_RECEIPT = "5";
	public final static String IS_CONSIGNMENT_PICKUP = "6";
	public final static String IS_CONSIGNMENT_INVOICE = "7";
	public final static String IS_CONSIGNMENT_RETURN = "8";

	public final static int INT_ORDER = 0;
	public final static int INT_RETURN = 1;
	public final static int INT_INVOICE = 2;
	public final static int INT_ESTIMATE = 3;
	public final static int INT_CONSIGNMENT_FILLUP = 4;
	public final static int INT_SALES_RECEIPT = 5;
	public final static int INT_CONSIGNMENT_PICKUP = 6;
	public final static int INT_CONSIGNMENT_INVOICE = 7;

	public final static int S_CUSTOMERS = 1;
	public final static int S_ADDRESS = 2;
	public final static int S_CATEGORIES = 3;
	public final static int S_EMPLOYEE_INVOICES = 4;
	public final static int S_PRODUCTS_INVOICES = 5;
	public final static int S_INVOICES = 6;
	public final static int S_PAY_METHODS = 7;
	public final static int S_PRICE_LEVEL = 8;
	public final static int S_ITEM_PRICE_LEVEL = 9;
	public final static int S_PRINTERS = 10;
	public final static int S_PRODCATXREF = 11;
	public final static int S_PROD_CHAIN = 12;
	public final static int S_PROD_ADDONS = 13;
	public final static int S_PRODUCTS = 14;
	public final static int S_PROD_IMG = 15;
	public final static int S_SALES_TAX_CODE = 16;
	public final static int S_SHIP_METHODS = 17;
	public final static int S_TAXES = 18;
	public final static int S_TAX_GROUP = 19;
	public final static int S_TERMS = 20;
	public final static int S_MEMO_TXT = 21;
	public final static int S_EMPLOYEE_DATA = 22;
	public final static int S_ACCT_LOGO = 23;
	public final static int S_DEVICE_DEFAULT_VAL = 24;
	public final static int S_LAST_PAY_ID = 25;
	public final static int S_VOLUME_PRICES = 26;
	public final static int S_UOM = 27;
	public final static int S_TEMPLATES = 28;
	public final static int S_IVU_LOTTO = 29;
	public final static int S_LOCATIONS_INVENTORY = 30;
	public final static int S_CUSTOMER_INVENTORY = 31;
	public final static int S_CONSIGNMENT_TRANSACTION = 32;
	public final static int S_GET_XML_ORDERS = 33;
	public final static int S_SUBMIT_PAYMENTS = 34;
	public final static int S_SUBMIT_VOID_TRANSACTION = 35;
	public final static int S_SUBMIT_CUSTOMER = 36;
	public final static int S_SUBMIT_TEMPLATES = 37;
	public final static int S_SUBMIT_CUSTOMER_INVENTORY = 38;
	public final static int S_SUBMIT_CONSIGNMENT_TRANSACTION = 39;
	public final static int S_PRODUCTS_ATTRIBUTES = 40;
	public final static int S_TERMS_AND_CONDITIONS = 41;
	public final static int S_CLERKS = 42;
	public final static int S_ORDERS_ON_HOLD_LIST = 43;
	public final static int S_ORDERS_ON_HOLD_DETAILS = 44;
	public final static int S_SUBMIT_ON_HOLD = 45;
	public final static int S_CHECK_STATUS_ON_HOLD = 46;
	public final static int S_UPDATE_STATUS_ON_HOLD = 47;
	public final static int S_CHECKOUT_ON_HOLD = 48;
	public final static int S_GET_TIME_CLOCK = 49;
	public final static int S_SUBMIT_TIME_CLOCK = 50;
	public final static int S_SUBMIT_SHIFT = 51;

	public final static int S_SUBMIT_TUPYX = 52;
	public final static int S_SUBMIT_WALLET_RECEIPTS = 53;
	public final static int S_GET_ORDER_PRODUCTS_ATTR = 54;
	public final static int S_PRODUCT_ALIASES = 55;

	public final static int S_GET_SERVER_TIME = 56;
	public final static int S_UPDATE_SYNC_TIME = 57;
	public final static int S_LOCATIONS = 58;
	public final static int S_SUBMIT_LOCATIONS_INVENTORY = 59;
	// public final static int S_LOCATIONS_INVENTORY = 59;

	public final static int FROM_OPEN_INVOICES = 100;
	public final static int FROM_OPEN_INVOICES_DETAILS = 104;
	public final static int FROM_CASH_PAYMENT = 101;
	public final static int FROM_CARD_PAYMENT = 102;
	public final static int FROM_PAYMENT = 103;
	public final static int FROM_JOB_INVOICE = 105;
	public final static int FROM_DRAW_RECEIPT_PORTRAIT = 106;
	public final static int FROM_DRAW_RECEIPT_LANDSCAPE = 107;
	public final static int FROM_JOB_SALES_RECEIPT = 108;

	public final static int FROM_LOGIN_ACTIVITTY = 109;
	public final static int FROM_REGISTRATION_ACTIVITY = 110;
	public final static int FROM_SYNCH_ACTIVITY = 111;

	public final static int FROM_ADDING_ORDERS = 112;

	public final static int BLUEBAMBOO = 0;
	public final static int BLUESTAR = 1;

	public final static String TIME_OUT = "1";
	public final static String NOT_VALID_URL = "2";

	public static String APP_ID;

	public final static int IS_CONS_RACK = 0;
	public final static int IS_CONS_RETURN = 1;
	public final static int IS_CONS_FILLUP = 2;
	public final static int IS_CONS_PICKUP = 3;

	public static int consignmentType = 0;
	public static String ord_type;
	private static String empStr = "";
	public static String amountPaid = "";
	public static String tipPaid = "0";
	public static double overallPaidAmount = 0;
	public int searchType = 0;

	public String encodedImage = "";

	public int orientation;

	public static boolean isConsignment = false;
	public static boolean isInventoryTransfer = false;
	public static List<ConsignmentTransaction> consignmentTransactionList;
	public static List<HashMap<String, String>> productParentAddons;
	public static HashMap<String, Integer> productParentAddonsDictionary;
	public HashMap<String, String[]> addonSelectionType;
	public static Map<String, HashMap<String, String[]>> addonSelectionMap;
	public static HashMap<String, List<OrderProducts>> orderProductAddonsMap;

	public static Locations_Holder locationFrom, locationTo;
	public static TransferLocations_Holder transferLocation;
	public static List<TransferInventory_Holder> transferInventory = new ArrayList<TransferInventory_Holder>();

	public static String imgFrontCheck = "", imgBackCheck = "";

	// Loyalty Data
	public static String loyaltyCharge = "";
	public static String loyaltyAddAmount = "";
	public static CreditCardInfo loyaltyCardInfo;

	// Reward data
	public static BigDecimal rewardChargeAmount = new BigDecimal("0");
	public static CreditCardInfo rewardCardInfo;
	public static BigDecimal rewardAccumulableSubtotal = new BigDecimal("0");

	// For new addon views
	public List<DataTaxes> listOrderTaxes = new ArrayList<DataTaxes>();

	public HashMap<String, String> ordProdAttrPending = new HashMap<String, String>();
	public List<OrdProdAttrHolder> ordProdAttr = new ArrayList<OrdProdAttrHolder>();
	public List<OrderProducts> orderProducts = new ArrayList<OrderProducts>();
	public List<OrderProducts> orderProductsAddons = new ArrayList<OrderProducts>();
	// public static HashMap<String,List<OrderProducts>>orderProductsAddonsMap;
	public Order order;
	// public List<Orders> cur_orders = new ArrayList<Orders>();
	public HashMap<String, String> qtyCounter = new HashMap<String, String>();

	// ----- Consignment Variables
	public static List<CustomerInventory> custInventoryList;
	public static HashMap<String, String[]> custInventoryMap;
	public static List<String> custInventoryKey;
	// public static List<ConsignmentTransaction>consTransactionList;

	public static List<String> consignMapKey;
	public static HashMap<String, HashMap<String, String>> consignSummaryMap;
	public static List<OrderProducts> consignment_products = new ArrayList<OrderProducts>();
	public static Order consignment_order;
	// public static List<Orders>consignment_cur_order = new
	// ArrayList<Orders>();
	public static HashMap<String, String> consignment_qtyCounter = new HashMap<String, String>();

	public static List<OrderProducts> cons_fillup_products = new ArrayList<OrderProducts>();
	public static Order cons_fillup_order;
	// public static List<Orders>cons_fillup_cur_order = new
	// ArrayList<Orders>();
	public static HashMap<String, String> cons_fillup_qtyCounter = new HashMap<String, String>();

	public static List<OrderProducts> cons_issue_products = new ArrayList<OrderProducts>();
	public static Order cons_issue_order;
	// public static List<Orders>cons_issue_cur_order = new ArrayList<Orders>();
	public static HashMap<String, String> cons_issue_qtyCounter = new HashMap<String, String>();

	public static List<OrderProducts> cons_return_products = new ArrayList<OrderProducts>();
	public static Order cons_return_order;
	// public static List<Orders>cons_return_cur_order = new
	// ArrayList<Orders>();
	public static HashMap<String, String> cons_return_qtyCounter = new HashMap<String, String>();

	public final Map<String, String> xmlActions = createMap();
	// public static final Map<String, String> transactionType =
	// createTransactionsMap();
	public String lastInvID = "";
	public static String lastOrdID = "";
	public int lastProdOrdID = 0;

	// public int discountPos = 0, taxPos = 0;
	public static String cat_id = "0";
	public boolean isSubcategory = false, hasSubcategory = false;
	public static int sqlLimitTransaction = 1000;
	public static String taxID = "";

	public static int taxPosition = 0, discountPosition;
	public static BigDecimal taxAmount = new BigDecimal("0"), discountAmount = new BigDecimal("0");
	public static double addonTotalAmount = 0;
	public static boolean isFromOnHold = false;

	public boolean loggedIn = false;

	// ---------- Used to store order details selected info -----//
	private int selectedShippingMethod;
	private String selectedShippingMethodString;

	private int selectedTermsMethod;
	private String selectedTermsMethodString;

	private int selectedAddressMethod;
	private String selectedAddressMethodString;

	private String selectedDeliveryDate;
	private String selectedComments;
	private String selectedPO;
	// -------------------end----------------------//

	public void resetOrderDetailsValues() {
		selectedShippingMethod = -1;
		selectedShippingMethodString = empStr;
		selectedTermsMethod = -1;
		selectedTermsMethodString = empStr;
		selectedAddressMethod = -1;
		selectedAddressMethodString = empStr;
		selectedDeliveryDate = empStr;
		selectedComments = empStr;
		selectedPO = empStr;
		taxID = empStr;
		taxPosition = 0;

		ord_type = "";
		cat_id = "0";
		Catalog_FR._typeCase = -1;
		Catalog_FR.btnListID.clear();
		Catalog_FR.btnListName.clear();
		isSubcategory = false;
		hasSubcategory = false;
		Global.isFromOnHold = false;
		isConsignment = false;
		isInventoryTransfer = false;
		consignmentType = 0;
		if (productParentAddons != null)
			productParentAddons.clear();
		if (productParentAddonsDictionary != null)
			productParentAddonsDictionary.clear();
		if (addonSelectionMap != null)
			addonSelectionMap.clear();
		if (orderProductAddonsMap != null)
			orderProductAddonsMap.clear();
		loyaltyCardInfo = new CreditCardInfo();
		loyaltyAddAmount = "";
		loyaltyCharge = "";

		rewardCardInfo = new CreditCardInfo();
		rewardChargeAmount = new BigDecimal("0");
		Global.lastOrdID = "";
		encodedImage = "";
	}

	public static String getPeripheralName(int type) {
		String _name = "Unknown";
		switch (type) {
			case STAR:
				_name = "Star Micronics";
				break;
			case MAGTEK:
				_name = "MAGTEK";
				break;
			case BAMBOO:
				_name = "Blue Bamboo";
				break;
			case ZEBRA:
				_name = "Zebra";
				break;
			case ONEIL:
				_name = "Oneil";
				break;
			case SNBC:
				_name = "SNBC";
				break;
			case ASURA:
				_name = "ASURA";
				break;
			case PAT100:
				_name = "PAT100";
				break;
			case ISMP:
				_name = "iSMP";
				break;
		}
		return _name;

	}

	public void clearListViewData() {
		// if(this.cur_orders!=null)
		// this.cur_orders.clear();
		if (this.orderProducts != null)
			this.orderProducts.clear();
		if (this.qtyCounter != null)
			this.qtyCounter.clear();

		if (ordProdAttr != null)
			ordProdAttr.clear();
		if (ordProdAttrPending != null)
			ordProdAttrPending.clear();

		if (this.orderProductsAddons != null)
			this.orderProductsAddons.clear();

		if (this.listOrderTaxes != null)
			this.listOrderTaxes.clear();

		// this.
	}

	public static String[] getCurrLocation(Activity activity) {
		String bestProvider;
		String[] values = new String[2];
		values[0] = empStr;
		values[1] = empStr;

		LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		bestProvider = lm.getBestProvider(criteria, false);

		if (bestProvider == null)
			return values;
		Location location = lm.getLastKnownLocation(bestProvider);
		if (location == null)
			return values;
		else {
			values[0] = Double.toString(location.getLatitude());
			values[1] = Double.toString(location.getLongitude());
		}
		return values;
	}

	public static String base64QRCode(String ivuLottoNumber, String ivuLottoDrawDate) {
		String finaldata = "CONTROL: " + ivuLottoNumber + ivuLottoDrawDate;
		com.google.zxing.Writer writer = new QRCodeWriter();
		try {
			int width = 200, height = 200;
			BitMatrix bm = writer.encode(finaldata, BarcodeFormat.QR_CODE, width, height);
			Bitmap myBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					myBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
				}
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			myBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
			byte[] b = baos.toByteArray();
			return Base64.encodeBytes(b);
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return "";
	}

	public void setSelectedComments(String val) {
		this.selectedComments = val;
	}

	public String getSelectedComments() {
		if (this.selectedComments == null)
			return empStr;
		return this.selectedComments;
	}

	public void setSelectedPO(String val) {
		this.selectedPO = val;
	}

	public String getSelectedPO() {
		if (this.selectedPO == null)
			return empStr;
		return this.selectedPO;
	}

	public void setSelectedShippingMethod(int val) {
		this.selectedShippingMethod = val;
	}

	public int getSelectedShippingMethod() {
		return this.selectedShippingMethod;
	}

	public void setSelectedShippingMethodString(String val) {
		this.selectedShippingMethodString = val;
	}

	public String getSelectedShippingMethodString() {
		if (this.selectedAddressMethodString == null)
			return empStr;
		return this.selectedShippingMethodString;
	}

	public void setSelectedTermsMethod(int val) {
		this.selectedTermsMethod = val;
	}

	public int getSelectedTermsMethod() {
		return this.selectedTermsMethod;
	}

	public void setSelectedTermsMethodString(String val) {
		this.selectedTermsMethodString = val;
	}

	public String getSelectedTermsMethodsString() {
		if (this.selectedTermsMethodString == null)
			return empStr;
		return this.selectedTermsMethodString;
	}

	public void setSelectedAddress(int val) {
		this.selectedAddressMethod = val;
	}

	public int getSelectedAddressMethod() {
		return this.selectedAddressMethod;
	}

	public void setSelectedAddressString(String val) {
		this.selectedAddressMethodString = val;
	}

	public String getSelectedAddressString() {
		if (this.selectedAddressMethodString == null)
			return empStr;
		return this.selectedAddressMethodString;
	}

	public void setSelectedDeliveryDate(String val) {
		this.selectedDeliveryDate = val;
	}

	public String getSelectedDeliveryDate() {
		if (this.selectedDeliveryDate == null)
			return empStr;
		return this.selectedDeliveryDate;
	}

	// private AlertDialog adb;
	//
	//
	// public AlertDialog getPromptDialog()
	// {
	// return this.adb;
	// }

	private Dialog globalDlog;

	public Dialog getGlobalDlog() {
		return this.globalDlog;
	}

	public static String getValidString(String value) {
		if (value == null)
			return "";
		return value;
	}

	private boolean validPassword = true;

	public void promptForMandatoryLogin(final Activity activity) {
		if (!loggedIn) {
			globalDlog = new Dialog(activity, R.style.Theme_TransparentTest);
			globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			globalDlog.setCancelable(false);
			globalDlog.setContentView(R.layout.dlog_field_single_layout);

			final EditText viewField = (EditText) globalDlog.findViewById(R.id.dlogFieldSingle);
			viewField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
			TextView viewTitle = (TextView) globalDlog.findViewById(R.id.dlogTitle);
			TextView viewMsg = (TextView) globalDlog.findViewById(R.id.dlogMessage);
			viewTitle.setText(R.string.dlog_title_confirm);
			if (!validPassword)
				viewMsg.setText(R.string.invalid_password);
			else
				viewMsg.setText(R.string.enter_password);

			Button btnOk = (Button) globalDlog.findViewById(R.id.btnDlogSingle);
			btnOk.setText(R.string.button_ok);
			btnOk.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					globalDlog.dismiss();
					MyPreferences myPref = new MyPreferences(activity);
					String enteredPass = viewField.getText().toString().trim();
					if (enteredPass.equals(myPref.getApplicationPassword())) {
						loggedIn = true;
						validPassword = true;
					} else {
						validPassword = false;
						promptForMandatoryLogin(activity);
					}
				}
			});
			globalDlog.show();
		}
	}

	private static Dialog popDlog;

	public static void showPrompt(Activity activity, int title, String msg) {
		if (popDlog == null)
			popDlog = new Dialog(activity, R.style.Theme_TransparentTest);
		else {
			popDlog.dismiss();
			popDlog = new Dialog(activity, R.style.Theme_TransparentTest);
		}
		popDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		popDlog.setCancelable(true);
		popDlog.setCanceledOnTouchOutside(false);
		popDlog.setContentView(R.layout.dlog_btn_single_layout);

		TextView viewTitle = (TextView) popDlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView) popDlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(title);
		viewMsg.setText(msg);
		Button btnOk = (Button) popDlog.findViewById(R.id.btnDlogSingle);
		btnOk.setText(R.string.button_ok);
		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				popDlog.dismiss();
			}
		});
		popDlog.show();
	}

	public static Map<String, String> paymentIconsMap = paymentIconMap();

	private static final Map<String, String> paymentIconMap() {
		HashMap<String, String> result = new HashMap<String, String>();

		result.put("AmericanExpress", "amex");
		result.put("Cash", "cash");
		result.put("Check", "debit");
		result.put("Discover", "discover");
		result.put("MasterCard", "mastercard");
		result.put("Visa", "visa");
		result.put("DebitCard", "debit");
		result.put("GiftCard", "debit");
		result.put("ECheck", "debit");
		result.put("Genius", "ic_cayan");
		result.put("Tupyx", "tupyx");
		result.put("Wallet", "tupyx");
		result.put("Boloro", "ic_boloro");

		return Collections.unmodifiableMap(result);
	}

	public static String formatToDisplayDate(String date, Activity activity, int type) {
		Calendar cal = Calendar.getInstance();
		TimeZone tz = cal.getTimeZone();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
		sdf1.setTimeZone(tz);
		SimpleDateFormat sdf2;
		if (type == 0)
			sdf2 = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
		else if (type == 1)
			sdf2 = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
		else if (type == 2)
			sdf2 = new SimpleDateFormat("h:mm:ss a", Locale.getDefault());
		else if (type == 3) // 3
			sdf2 = new SimpleDateFormat("MMM/dd/yyyy h:mm a", Locale.getDefault());
		else if (type == 4)
			sdf2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		else
			sdf2 = new SimpleDateFormat("h:mm a", Locale.getDefault());

		String formatedDate = new String();
		try {
			formatedDate = sdf2.format(sdf1.parse(date));

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [")
					.append("com.android.support.Global (at Class.formatToDisplayDate) ]");
//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
		}
		return formatedDate;
	}

	public static String getCurrentDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
		SimpleDateFormat sdfTZ = new SimpleDateFormat("Z", Locale.getDefault());
		Calendar cal = Calendar.getInstance();
		TimeZone tz = cal.getTimeZone();
		sdfTZ.setTimeZone(tz);
		Date date = new Date();
		String cur_date = sdf.format(date);
		String TimeZone = sdfTZ.format(date);

		String ending = TimeZone.substring(TimeZone.length() - 2, TimeZone.length());
		String begining = TimeZone.substring(0, TimeZone.length() - 2);
		StringBuilder sb = new StringBuilder().append(cur_date).append(begining).append(":").append(ending);

		return sb.toString();
	}

	public boolean isApplicationSentToBackground(final Context context) {
		// ActivityManager am = (ActivityManager)
		// context.getSystemService(Context.ACTIVITY_SERVICE);
		// List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		// if (!tasks.isEmpty()) {
		// ComponentName topActivity = tasks.get(0).topActivity;
		// if (!topActivity.getPackageName().equals(context.getPackageName())) {
		// return true;
		// }
		// }
		//
		// return false;
		return wasInBackground;
		// return !MyLifecycleHandler.isApplicationInForeground();
	}

	public static String formatLocaleToCurrency(String value) {
		double val = formatNumFromLocale(value);
		return formatDoubleToCurrency(val);
	}

	public static String getCurrencyFormat(String value)// received as #
	{
		if (value == null || value.isEmpty())
			return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(0.00);
		/*
		 * else if(value.contains(".")) return
		 * NumberFormat.getCurrencyInstance(Locale.US).format(Double.parseDouble
		 * (value)); return
		 * NumberFormat.getCurrencyInstance(Locale.US).format((double)Integer.
		 * parseInt(value));
		 */
		return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(formatNumFromLocale(value));
	}

	public static String formatDoubleToCurrency(double value)// received as #.#
	{
		NumberFormat numFormater = NumberFormat.getNumberInstance(Locale.getDefault());
		numFormater.setParseIntegerOnly(false);
		// Double val = Double.parseDouble(numFormater.format(value));
		return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(value);
	}

	public static String formatDoubleStrToCurrency(String val) {
		if (val == null || val.isEmpty())
			return (Global.getCurrencyFormat(Global.formatNumToLocale(0.00)));
		return (Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(val))));

	}

	public static double formatNumFromLocale(String val)// received as #,##
	// instead of #.##
	{
		double frmt = 0.0;
		try {
			NumberFormat numFormater = NumberFormat.getNumberInstance(Locale.getDefault());
			numFormater.setParseIntegerOnly(false);

			// Number number =
			// NumberFormat.getNumberInstance(Locale.getDefault()).setParseIntegerOnly(false).parse(val);
			Number number = numFormater.parse(val);
			frmt = number.doubleValue();
		} catch (ParseException e) {

		}
		return frmt;
	}

	public static double formatNumWithCurrFromLocale(String val) {
		NumberFormat cf = NumberFormat.getCurrencyInstance(Locale.getDefault());

		Number number = 0.0;
		try {
			number = cf.parse(val);
		} catch (ParseException e) {

		}
		return number.doubleValue();
	}

	public static String formatNumToLocale(double val) {

		NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
		;
		nf.setParseIntegerOnly(false);
		DecimalFormat df = (DecimalFormat) nf;
		return df.format(val);
	}

	public static void generateDebugFile(String sBody) {
		try {
			File root = new File(Environment.getExternalStorageDirectory(), "Notes");
			if (!root.exists()) {
				root.mkdirs();
			}
			File gpxfile = new File(root, "test.txt");
			FileWriter writer = new FileWriter(gpxfile);
			writer.append(sBody);
			writer.flush();
			writer.close();

		} catch (IOException e) {
		}
	}

	private static Map<String, String> createMap() {
		HashMap<String, String> result = new HashMap<String, String>();

		result.put("Address", "getXMLAddress.aspx");
		result.put("Categories", "getXMLCategories.aspx");
		result.put("Customers", "getXMLCustomers.aspx");
		result.put("EmpInv", "getXMLInventory.aspx");
		result.put("Employees", "RequestEmployees.aspx");
		result.put("InvProducts", "getXMLInvoicesDetails.aspx");
		result.put("Invoices", "getXMLInvoices.aspx");
		// result.put("OrderProducts", "getXMLOrdersOnHoldDetail.ashx");
		// Orders
		result.put("PayMethods", "getXMLPayMethods.aspx");
		result.put("PriceLevel", "getXMLPriceLevel.aspx");
		result.put("PriceLevelItems", "getXMLPriceLevelItems.aspx");
		result.put("Printers", "getXMLPrinters.ashx");
		result.put("ProdCatXref", "getxmlproductcategories.aspx");
		result.put("ProductChainXRef", "getXMLProductChains.aspx");
		result.put("Product_addons", "getXMLProductAddons.ashx");
		result.put("Products", "getXMLProducts.aspx");
		result.put("ProductAliases", "getXMLProductAliases.ashx");
		result.put("Products_Images", "getXMLProductsImages.aspx");

		result.put("SalesTaxCodes", "getXMLSalesTaxCodes.aspx");
		result.put("ShipMethod", "getXMLShipMethods.aspx");
		result.put("Taxes", "getXMLTaxes.aspx");
		result.put("Taxes_Group", "getXMLTaxesGroup.aspx");
		result.put("Terms", "getXMLTerms.aspx");
		// deviceDefaultValues
		result.put("DeviceDefaultValues", "getXMLDeviceDefaultValues.aspx");
		result.put("memotext", "getPrintHeader.aspx");

		result.put("VoidTransactions", "submitVoidTrans.aspx");
		// products_attrs not needed

		result.put("GetLogo", "getLogo.aspx");
		result.put("submitCustomer", "submitCustomer.aspx");
		result.put("UoM", "getXMLUOMItems.aspx");
		result.put("VolumePrices", "getXMLVolumePrices.ashx");
		result.put("Templates", "getXMLCustomertemplates.aspx");
		result.put("ivudrawdates", "ivudrawdates.ashx");
		result.put("LocationInventory", "getXMLInventory.aspx");

		result.put("PostConsignmentTransaction", "submitConsignmentTransaction.aspx");
		result.put("GetConsignmentTransaction", "getXMLConsignmentTransaction.aspx");

		result.put("PostCustomerInventory", "submitCustomerInventory.aspx");
		result.put("GetCustomerInventory", "getXMLCustomerInventory.aspx");
		result.put("ProductsAttr", "getXMLProductsAttr.aspx");
		result.put("Clerks", "getXMLClerks.aspx");
		result.put("TermsAndConditions", "getXMLTermAndConditions.ashx");

		result.put("submitOrdersOnHold", "submitOrdersOnHold.aspx");
		result.put("GetOrdersOnHoldList", "getXMLOrdersOnHoldList.ashx");
		result.put("GetOrdersOnHoldDetail", "getXMLOrdersOnHoldDetail.ashx");

		result.put("GetOrderProductsAttr", "getXMLOrderProductsAttr.aspx");
		result.put("GetLocations", "getXMLLocations.aspx");
		result.put("GetLocationsInventory", "getXMLLocationsInventory.aspx");

		return Collections.unmodifiableMap(result);
	}

	public static String addSubsStrings(boolean isAdd, String value1, String value2) {
		double amount1 = 0.00, amount2 = 0.00;
		value1 = value1.replace("$", "");
		value2 = value2.replace("$", "");
		if (value1 != null && !value1.isEmpty()) {
			amount1 = Global.formatNumFromLocale(value1);
		}
		if (value2 != null && !value2.isEmpty()) {
			amount2 = Global.formatNumFromLocale(value2);
		}

		DecimalFormat frmt = new DecimalFormat("0.00");

		double total = 0.00;
		if (isAdd)
			total = amount1 + amount2;
		else
			total = amount1 - amount2;

		return frmt.format(total);
	}

	public static String formatNumber(boolean isDecimal, double val) {
		String returnedVal = new String();
		if (isDecimal) {
			returnedVal = Double.toString(val);
		} else {
			returnedVal = Integer.toString((int) val);
		}
		return returnedVal;
	}

	public int checkIfGroupBySKU(Activity activity, String prodID, String pickedQty) {
		int orderIndex = -1;
		MyPreferences myPref = new MyPreferences(activity);
		int size = this.orderProducts.size();
		boolean found = false;

		for (int i = size - 1; i >= 0; i--) {
			if (this.orderProducts.get(i).prod_id.equals(prodID) && !orderProducts.get(i).isReturned) {
				orderIndex = i;
				found = true;
				break;
			}
		}

		if (found && !OrderingMain_FA.returnItem) {
			String value = this.qtyCounter.get(prodID);
			double previousQty = 0.0;
			if (value != null && !value.isEmpty())
				previousQty = Double.parseDouble(value);
			double sum = Double.parseDouble(pickedQty) + previousQty;
			sum = OrderingMain_FA.returnItem ? sum * -1 : sum;

			value = new String();
			if (myPref.getPreferences(MyPreferences.pref_allow_decimal_quantities)) {
				value = Global.formatNumber(true, sum);
				this.orderProducts.get(orderIndex).ordprod_qty = value;
				// this.cur_orders.get(0).setQty(value);
				this.qtyCounter.put(prodID, Double.toString(sum));
			} else {
				value = Global.formatNumber(false, sum);
				this.orderProducts.get(orderIndex).ordprod_qty = value;
				// this.cur_orders.get(0).setQty(value);
				this.qtyCounter.put(prodID, Integer.toString((int) sum));
			}
		}
		return orderIndex;
	}

	public void refreshParticularOrder(MyPreferences myPref, int position, String[] data) {
		OrderProducts orderedProducts = this.orderProducts.get(position);

		String newPickedOrders = orderedProducts.ordprod_qty;
		double sum = 1.0;

		if (myPref.getPreferences(MyPreferences.pref_allow_decimal_quantities))
			sum = Double.parseDouble(newPickedOrders);
		else
			sum = Integer.parseInt(newPickedOrders);

		String prLevTotal = data[2];// orderedProducts.getSetData("overwrite_price",
		// true, null);

		double total = 0;// = sum*Double.parseDouble(prLevTotal);

		try {
			total = sum * Double.parseDouble(prLevTotal);
		} catch (NumberFormatException e) {
		}

		double itemTotal = total;

		if (itemTotal < 0)
			itemTotal = 0.00;

		orderedProducts.itemSubtotal = Double.toString(total);
		double discountRate = 0;
		if (orderedProducts.discount_is_fixed.equals("1")) {
			discountRate = Double.parseDouble(orderedProducts.discount_value);
		} else {
			double val = total * Global.formatNumFromLocale(orderedProducts.discount_value);
			discountRate = (val / 100);
		}

		orderedProducts.itemTotal = Double.toString(total - discountRate);
		orderedProducts.overwrite_price = prLevTotal;
		orderedProducts.prod_price_updated = "0";

		if (myPref.isSam4s(true, true)) {
			StringBuilder sb = new StringBuilder();
			String row1 = data[1];
			String row2 = sb.append(Global.formatDoubleStrToCurrency(data[2])).toString();
			uart uart_tool = new uart();
			uart_tool.config(3, 9600, 8, 1);
			uart_tool.write(3, Global.emptySpaces(40, 0, false));
			uart_tool.write(3, Global.formatSam4sCDT(row1, row2));
		} else if (myPref.isPAT100(true, true)) {
			StringBuilder sb = new StringBuilder();
			String row1 = data[1];
			String row2 = sb.append(Global.formatDoubleStrToCurrency(data[2])).toString();
			EMSPAT100.getTerminalDisp().clearText();
			EMSPAT100.getTerminalDisp().displayText(Global.formatSam4sCDT(row1.toString(), row2.toString()));
		}

	}

	public static CreditCardInfo parseSimpleMSR(Activity activity, String data) {
		CreditCardInfo cardManager = new CreditCardInfo();
		Encrypt encrypt = new Encrypt(activity);
		String card_number;
		String name_on_card = "";
		String exp_year = "";
		String exp_month = "";

		data = data.replaceAll("\\r", "");
		String[] tracks = data.split(";");

		if (tracks.length >= 2) // two tracks
		{
			if (!tracks[0].isEmpty()) {

				int startIndex = tracks[0].indexOf("^") + 1;
				int endIndex = tracks[0].indexOf("^", startIndex);
				if (startIndex >= 0 && endIndex >= 0)
					name_on_card = tracks[0].substring(startIndex, endIndex);

				cardManager.setEncryptedAESTrack1(encrypt.encryptWithAES(tracks[0]));
				cardManager.setCardOwnerName(name_on_card);

				if (tracks[1] == null || tracks[1].isEmpty()) // track 2 N/A
				// retrieve PAN
				// from track 1
				{

					card_number = tracks[0].replace("B", "").substring(1, startIndex - 2);

					cardManager.setCardType(ProcessCreditCard_FA.cardType(card_number));

					if (card_number.length() > 4) {
						int temp = card_number.length();
						String last4Digits = (String) card_number.subSequence(temp - 4, temp);
						cardManager.setCardLast4(last4Digits);
					}

					cardManager.setCardNumAESEncrypted(encrypt.encryptWithAES(card_number));
					if (!isEncryptSwipe)
						cardManager.setCardNumUnencrypted(card_number);
				}
			}

			if (tracks[1] != null && !tracks[1].isEmpty()) {

				String[] track2Split = tracks[1].split("=");
				card_number = track2Split[0].replace("?", "");// contains card
				// number
				tracks[1] = new StringBuilder().append(";").append(tracks[1]).toString();
				if (track2Split.length > 1 && track2Split[1].length() > 4) {
					if (track2Split[1].length() > 0) {
						exp_year = track2Split[1].trim().substring(0, 2);
						exp_month = track2Split[1].trim().substring(2, 4);
					}
					cardManager.setCardExpMonth(exp_month);
					cardManager.setCardExpYear(exp_year);
				}
				cardManager.setEncryptedAESTrack2(encrypt.encryptWithAES(tracks[1]));
				cardManager.setCardType(ProcessCreditCard_FA.cardType(card_number));

				if (!track2Split[0].isEmpty()) {

					if (card_number.length() > 4) {
						int temp = card_number.length();
						String last4Digits = (String) card_number.subSequence(temp - 4, temp);
						cardManager.setCardLast4(last4Digits);
					}

					cardManager.setCardNumAESEncrypted(encrypt.encryptWithAES(card_number));
					if (!isEncryptSwipe)
						cardManager.setCardNumUnencrypted(card_number);
				}
			}
		} else {
			int size = tracks.length;
			if (size == 1 && tracks[0].contains("^")) // tack 1
			{

				int startIndex = tracks[0].indexOf("^") + 1;
				int endIndex = tracks[0].indexOf("^", startIndex);
				if (startIndex >= 0 && endIndex >= 0)
					name_on_card = tracks[0].substring(startIndex, endIndex);

				cardManager.setCardOwnerName(name_on_card);
				cardManager.setEncryptedAESTrack1(encrypt.encryptWithAES(tracks[0]));

				card_number = tracks[0].replace("B", "").substring(1, startIndex - 2);

				cardManager.setCardNumAESEncrypted(encrypt.encryptWithAES(card_number));
				if (!isEncryptSwipe)
					cardManager.setCardNumUnencrypted(card_number);

			} else if (tracks[0].contains("=")) // track 2
			{
				String[] track2Split = tracks[0].split("=");
				card_number = track2Split[0];// contains card number
				tracks[0] = new StringBuilder().append(";").append(tracks[0]).toString();

				if (track2Split.length > 1) {
					if (track2Split[1].length() > 0) {
						exp_year = track2Split[1].trim().substring(0, 2);
						exp_month = track2Split[1].trim().substring(2, 4);

						cardManager.setCardExpMonth(exp_month);
						cardManager.setCardExpYear(exp_year);
					}
				}

				cardManager.setCardType(ProcessCreditCard_FA.cardType(card_number));

				if (isEncryptSwipe)
					cardManager.setCardNumAESEncrypted(encrypt.encryptWithAES(card_number));
				else
					cardManager.setCardNumAESEncrypted(card_number);

				cardManager.setEncryptedAESTrack2(encrypt.encryptWithAES(tracks[0]));
				if (card_number.length() > 4) {
					int temp = card_number.length();
					String last4Digits = (String) card_number.subSequence(temp - 4, temp);
					cardManager.setCardLast4(last4Digits);
				}
			}

		}

		// if (tracks.length >= 2) {
		// String[] track2Split = tracks[1].split("=");
		// card_number = track2Split[0];// contains card number
		// tracks[1] = new
		// StringBuilder().append(";").append(tracks[1]).toString();
		// int startIndex = tracks[0].indexOf("^") + 1;
		// int endIndex = tracks[0].indexOf("^", startIndex);
		// if (startIndex >= 0 && endIndex >= 0)
		// name_on_card = tracks[0].substring(startIndex, endIndex);
		//
		// if(track2Split.length>1)
		// {
		// if (track2Split[1].length() > 0) {
		// exp_year = track2Split[1].trim().substring(0, 2);
		// exp_month = track2Split[1].trim().substring(2, 4);
		// }
		//
		// Encrypt encrypt = new Encrypt(activity);
		// cardManager.setCardType(ProcessCardMenuActivity.cardType(card_number));
		//
		// if(isEncryptSwipe)
		// cardManager.setCardNumAESEncrypted(encrypt.encryptWithAES(card_number));
		// else
		// cardManager.setCardNumAESEncrypted(card_number);
		//
		// cardManager.setCardExpMonth(exp_month);
		// cardManager.setCardExpYear(exp_year);
		// cardManager.setCardOwnerName(name_on_card);
		// cardManager.setEncryptedAESTrack1(encrypt.encryptWithAES(tracks[0]));
		// cardManager.setEncryptedAESTrack2(encrypt.encryptWithAES(tracks[1]));
		// if (card_number.length() > 10) {
		// int temp = card_number.length();
		// String last4Digits = (String) card_number.subSequence(temp - 4,
		// temp);
		// cardManager.setCardLast4(last4Digits);
		// }
		// }
		// }
		return cardManager;
	}

	public void automaticAddOrder(Activity activity, boolean isFromAddon, Global global, String[] data) {
		Orders order = new Orders();
		OrderProducts ord = new OrderProducts();

		int sum = 0;
		if (this.qtyCounter.containsKey(data[0]))
			sum = Integer.parseInt(this.qtyCounter.get(data[0]));

		if (!OrderingMain_FA.returnItem)
			global.qtyCounter.put(data[0], Integer.toString(sum + 1));
		else
			global.qtyCounter.put(data[0], Integer.toString(sum - 1));
		if (OrderingMain_FA.returnItem)
			ord.isReturned = true;

		order.setName(data[1]);
		order.setValue(data[2]);
		order.setProdID(data[0]);
		order.setDiscount("0.00");
		order.setTax("0.00");
		order.setDistQty("0");
		order.setTaxQty("0");

		String val = data[2];
		if (val.isEmpty() || val == null)
			val = "0.00";

		BigDecimal total = Global.getBigDecimalNum(Global.formatNumToLocale(Double.parseDouble(val)));
		// double total = 1 * Double.parseDouble(val);
		if (isFromAddon) {
			total = total.add(Global.getBigDecimalNum(Global.formatNumToLocale(Global.addonTotalAmount)));
			// total+=Global.addonTotalAmount;
		}

		ord.overwrite_price = total.toString();
		ord.prod_price = total.toString();

		total = total.multiply(OrderingMain_FA.returnItem ? new BigDecimal(-1) : new BigDecimal(1));

		DecimalFormat frmt = new DecimalFormat("0.00");
		order.setTotal(frmt.format(total));

		ord.prod_istaxable = data[6];
		ord.prod_taxtype = data[11];
		ord.prod_taxcode = data[12];

		// add order to db
		ord.ordprod_qty = OrderingMain_FA.returnItem ? "-1" : "1";
		ord.ordprod_name = data[1];
		ord.ordprod_desc = data[3];
		ord.prod_id = data[0];

		ord.onHand = data[4];
		ord.imgURL = data[5];
		ord.cat_id = data[8];
		try {
			ord.prod_price_points = data[9];
			ord.prod_value_points = data[10];
		} catch (Exception e) {

		}

		// Still need to do add the appropriate tax/discount value
		ord.prod_taxValue = "0.00";
		ord.discount_value = "0.00";

		ord.taxAmount = "0";
		ord.taxTotal = "0.00";
		ord.disAmount = "0";
		ord.disTotal = "0.00";
		ord.itemTotal = total.toString();
		ord.itemSubtotal = total.toString();

		ord.tax_position = "0";
		ord.discount_position = "0";
		ord.pricelevel_position = "0";
		ord.uom_position = "0";

		ord.prod_price_updated = "0";
		// OrdersHandler handler = new OrdersHandler(activity);

		GenerateNewID generator = new GenerateNewID(activity);

		MyPreferences myPref = new MyPreferences(activity);
		// myPref.setLastOrdID(generator.getNextID(myPref.getLastOrdID()));

		// if(!Global.isFromOnHold)
		// {
		// if (handler.getDBSize() == 0)
		// Global.lastOrdID = generator.generate("",0);
		// else
		// Global.lastOrdID = generator.generate(handler.getLastOrdID(),0);
		// }

		if (!Global.isFromOnHold && Global.lastOrdID.isEmpty()) {
			Global.lastOrdID = generator.getNextID(IdType.ORDER_ID);
		}

		ord.ord_id = Global.lastOrdID;

		if (global.orderProducts == null) {
			global.orderProducts = new ArrayList<OrderProducts>();
		}

		UUID uuid = UUID.randomUUID();
		String randomUUIDString = uuid.toString();

		ord.ordprod_id = randomUUIDString;

		if (isFromAddon) {
			Global.addonTotalAmount = 0;

			if (Global.addonSelectionMap == null)
				Global.addonSelectionMap = new HashMap<String, HashMap<String, String[]>>();
			if (Global.orderProductAddonsMap == null)
				Global.orderProductAddonsMap = new HashMap<String, List<OrderProducts>>();

			if (global.addonSelectionType.size() > 0) {
				StringBuilder sb = new StringBuilder();
				Global.addonSelectionMap.put(randomUUIDString, global.addonSelectionType);
				Global.orderProductAddonsMap.put(randomUUIDString, global.orderProductsAddons);

				sb.append(ord.ordprod_desc);
				int tempSize = global.orderProductsAddons.size();
				for (int i = 0; i < tempSize; i++) {

					sb.append("<br/>");
					if (global.orderProductsAddons.get(i).isAdded.equals("0")) // Not
						// added
						sb.append("[NO ").append(global.orderProductsAddons.get(i).ordprod_name).append("]");
					else
						sb.append("[").append(global.orderProductsAddons.get(i).ordprod_name).append("]");

				}
				ord.ordprod_desc = sb.toString();
				ord.hasAddons = "1";

				global.orderProductsAddons = new ArrayList<OrderProducts>();

			}
		}
		global.orderProducts.add(ord);
	}

	public static boolean isConnectedToInternet(Activity activity) {
		ConnectivityManager connManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo myWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo myMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo myEthernet = connManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
		if ((myWifi != null && myWifi.isAvailable() && myWifi.isConnected())
				|| (myMobile != null && myMobile.isAvailable() && myMobile.isConnected())
				|| (myEthernet != null && myEthernet.isAvailable() && myEthernet.isConnected())) {
			InetAddress inetAddress;
			Socket socket = null;
			try {
				inetAddress = InetAddress.getByName("sync.enablermobile.com");

				socket = new Socket();
				SocketAddress socketAddress = new InetSocketAddress(inetAddress, 443);
				socket.connect(socketAddress, 5000);// try for 3 seconds

				// Process p = Runtime.getRuntime().exec("/system/bin/ping -c 1
				// www.google.com");
				// Thread.sleep(1000);
				// int i = p.exitValue();
				// if(i==0)
				// return true;
				// return false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				return false;
			} finally {
				if (socket != null && socket.isConnected()) {
					try {
						socket.close();
					} catch (IOException e) {
						return false;
					}
				} else
					return false;
			}
		} else
			return false;
		return true;
	}

	public static Object getFormatedNumber(boolean isDecimal, String val) {
		Object returnedVal = new Object();
		if (isDecimal) {
			returnedVal = Double.parseDouble(val);
		} else {
			returnedVal = Integer.parseInt(val);
		}

		return returnedVal;
	}

	// public void recalculateOrderProduct(int position)
	// {
	// double prodPrice =
	// getDouble(orderProducts.get(position).getSetData("overwrite_price", true,
	// null));
	// double qty =
	// getDouble(orderProducts.get(position).getSetData("ordprod_qty", true,
	// null));
	//
	// double granTotal = prodPrice*qty;
	// orderProducts.get(position).getSetData("itemTotal", false,
	// Double.toString(granTotal));
	// }

	public double getDouble(String val) {
		double ans = 0.00;
		if (!val.isEmpty()) {
			ans = Double.parseDouble(val);
		}
		return ans;
	}

	public List<HashMap<String, Integer>> dictionary;

	public static OnTouchListener opaqueImageOnClick() {
		return (new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN: {
						ImageView view = (ImageView) v;
						// overlay is black with transparency of 0x77 (119)
						if (view.getDrawable() != null) {
							view.getDrawable().setColorFilter(0x77000000, Mode.SRC_ATOP);
							view.invalidate();
						}
						break;
					}
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL: {
						ImageView view = (ImageView) v;
						// clear the overlay
						if (view.getDrawable() != null) {
							view.getDrawable().clearColorFilter();
							view.invalidate();
						}
						break;
					}
				}

				return false;
			}
		});
	}

//	public static TextWatcher amountTextWatcher(final EditText editText) {
//
//		return new TextWatcher() {
//			@Override
//			public void afterTextChanged(Editable s) {
//
//			}
//
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//			}
//
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//				ProcessCash_FA.parseInputedCurrency(s, editText);
//			}
//		};
//	}

//	public static void parseInputedCurrency(EditText field, CharSequence s) {
//		DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(Locale.getDefault());
//		DecimalFormatSymbols sym = format.getDecimalFormatSymbols();
//		StringBuilder sb = new StringBuilder();
//		// sb.append("^\\").append(sym.getCurrencySymbol()).append("\\s(\\d{1,3}(\\").append(sym.getGroupingSeparator())
//		// .append("\\d{3})*|(\\d+))(");
//		// sb.append(sym.getDecimalSeparator()).append("\\d{2})?$");
//
//		sb.append("^(\\d{1,3}(\\").append(sym.getGroupingSeparator()).append("\\d{3})*|(\\d+))(");
//		sb.append(sym.getDecimalSeparator()).append("\\d{2})?$");
//
//		if (!s.toString().matches(sb.toString())
//				|| !s.toString().contains(Character.toString(sym.getDecimalSeparator()))) {
//			String userInput = "" + s.toString().replaceAll("[^\\d]", "");
//			StringBuilder cashAmountBuilder = new StringBuilder(userInput);
//
//			while (cashAmountBuilder.length() > 3 && cashAmountBuilder.charAt(0) == '0') {
//				cashAmountBuilder.deleteCharAt(0);
//			}
//			while (cashAmountBuilder.length() < 3) {
//				cashAmountBuilder.insert(0, '0');
//			}
//
//			cashAmountBuilder.insert(cashAmountBuilder.length() - 2, sym.getDecimalSeparator());
//			// cashAmountBuilder.insert(0, sym.getCurrencySymbol() + " ");
//
//			field.setText(cashAmountBuilder.toString());
//		}
//
//		Selection.setSelection(field.getText(), field.getText().length());
//	}

	public static BigDecimal getBigDecimalNum(String val) {
		if (val == null || val.isEmpty())
			val = "0";
		try {
			double valDbl = Global.formatNumFromLocale(val.toString().replaceAll("[^\\d\\,\\.]", "").trim());
			DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.getDefault());
			df.setParseBigDecimal(true);
			df.setMaximumFractionDigits(4);
			df.setMinimumFractionDigits(2);
			return (BigDecimal) df.parseObject(String.valueOf(valDbl));
		} catch (ParseException e) {
			return new BigDecimal("0");
		}
	}

	public static String getRoundBigDecimal(BigDecimal val) {
		return val.setScale(2, RoundingMode.HALF_UP).toString();
	}

	public static String getCurrencyFrmt(String value) {
		NumberFormat frmt = NumberFormat.getCurrencyInstance(Locale.getDefault());
		if (value == null || value.isEmpty())
			value = "0";
		frmt.setMinimumFractionDigits(2);
		frmt.setMaximumFractionDigits(2);
		return frmt.format(Double.parseDouble(value));
		// System.out.println( usdCostFormat.format(displayVal.doubleValue()) );
	}

	public static boolean isPortrait(Context context) {
		int ot = context.getResources().getConfiguration().orientation;
		switch (ot) {

			case Configuration.ORIENTATION_LANDSCAPE:
				return false;
			case Configuration.ORIENTATION_PORTRAIT:
				return true;

		}
		return false;
	}

	public static String encodeBitmapToBase64(Bitmap image) {
		Bitmap immagex = image;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] b = baos.toByteArray();
		String imageEncoded = Base64.encodeBytes(b);

		return imageEncoded;
	}

	public static Bitmap decodeBase64Bitmap(String input) {

		byte[] decodedByte = null;
		try {
			decodedByte = Base64.decode(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
	}

	public static String formatSam4sCDT(String row1, String row2) {
		StringBuilder sb = new StringBuilder();
		int limiter = 20;

		if (row1.length() >= limiter) {
			sb.append(row1.substring(0, limiter));
		} else {
			sb.append(row1).append(emptySpaces(limiter, row1.length(), false));
		}

		if (row2.length() >= limiter) {
			sb.append(row2.substring(0, limiter));
		} else {
			sb.append(row2).append(emptySpaces(limiter, row2.length(), false));
		}

		return sb.toString();
	}

	public static String emptySpaces(int maxLength, int currLength, boolean center) {

		int size = maxLength - currLength;
		if (center)
			size = size / 2;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(" ");
		}
		return sb.toString();
	}

	public static void showCDTDefault(Activity activity) {
		MyPreferences myPref = new MyPreferences(activity);
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		uart uart_tool = new uart();
		uart_tool.config(3, 9600, 8, 1);
		uart_tool.write(3, Global.emptySpaces(40, 0, false));

		String msg1 = myPref.cdtLine1(true, "");
		String msg2 = myPref.cdtLine2(true, "");
		sb1.append(Global.emptySpaces(20, msg1.length(), true)).append(msg1);
		sb2.append(Global.emptySpaces(20, msg2.length(), true)).append(msg2);
		if (myPref.isSam4s(true, true)) {

			uart_tool.config(3, 9600, 8, 1);
			uart_tool.write(3, Global.emptySpaces(40, 0, false));
			uart_tool.write(3, Global.formatSam4sCDT(sb1.toString(), sb2.toString()));
		} else if (myPref.isPAT100(true, true)) {
			EMSPAT100.getTerminalDisp().clearText();
			EMSPAT100.getTerminalDisp().displayText(Global.formatSam4sCDT(sb1.toString(), sb2.toString()));
		}
	}

	public static boolean deviceHasMSR(int _printer_type) {
		return (_printer_type == Global.ISMP || _printer_type == Global.STAR || _printer_type == Global.BAMBOO
				|| _printer_type == Global.ZEBRA || _printer_type == Global.ASURA);
	}

	public static boolean deviceHasBarcodeScanner(int _device_type) {
		return (_device_type == Global.ISMP || _device_type == Global.POWA || _device_type == Global.ASURA
				|| _device_type == Global.STAR);
	}

	// Handle application transition for background
	private Timer mActivityTransitionTimer;
	private TimerTask mActivityTransitionTimerTask;
	private boolean wasInBackground;
	private final long MAX_ACTIVITY_TRANSITION_TIME_MS = 3000;

	public void startActivityTransitionTimer() {
		this.mActivityTransitionTimer = new Timer();
		this.mActivityTransitionTimerTask = new TimerTask() {
			public void run() {
				wasInBackground = true;
			}
		};

		mActivityTransitionTimer.schedule(mActivityTransitionTimerTask, MAX_ACTIVITY_TRANSITION_TIME_MS);
	}

	public void stopActivityTransitionTimer() {
		if (this.mActivityTransitionTimerTask != null) {
			this.mActivityTransitionTimerTask.cancel();
		}

		if (this.mActivityTransitionTimer != null) {
			this.mActivityTransitionTimer.cancel();
		}

		this.wasInBackground = false;
	}

	public static boolean isIpAvailable(String ip, int port) {
		boolean exists = false;
		Socket sock;
		try {
			SocketAddress sockaddr = new InetSocketAddress(ip, port);
			// Create an unbound socket
			sock = new Socket();
			int timeoutMs = 2000; // 2 seconds
			sock.connect(sockaddr, timeoutMs);
			sock.close();
			exists = true;
		} catch (Exception e) {
		}

		return exists;
	}
}
