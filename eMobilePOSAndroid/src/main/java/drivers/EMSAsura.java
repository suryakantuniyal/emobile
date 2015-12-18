package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextPaint;
import android.util.Base64;

import com.StarMicronics.jasura.IBarcodeListener;
import com.StarMicronics.jasura.IMSRListener;
import com.StarMicronics.jasura.JABarcodeMode;
import com.StarMicronics.jasura.JABarcodeReader;
import com.StarMicronics.jasura.JAException;
import com.StarMicronics.jasura.JAMagstripeReader;
import com.StarMicronics.jasura.JAPrinter;
import com.StarMicronics.jasura.JAPrinter.JAPrintCashDrawer;
import com.StarMicronics.jasura.JAPrinterStatus;
import com.android.database.InvProdHandler;
import com.android.database.InvoicesHandler;
import com.android.database.MemoTextHandler;
import com.android.database.PayMethodsHandler;
import com.android.database.PaymentsHandler;
import com.android.database.ProductsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.PaymentDetails;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import drivers.EMSBluetoothStarPrinter.ReceiveThread;
import main.EMSDeviceManager;
import plaintext.EMSPlainTextHelper;
import protocols.EMSCallBack;
import protocols.EMSDeviceManagerPrinterDelegate;

public class EMSAsura extends EMSDeviceDriver
        implements EMSDeviceManagerPrinterDelegate, IMSRListener, IBarcodeListener {

    public enum Align {
        ALIGN_LEFT, ALIGN_CENTER, ALIGN_RIGHT
	};

    private int LINE_WIDTH = 40;
    private int PRINT_TXT_SIZE = 24;
    private int PAPER_WIDTH = 576;
    private String portSettings, portName;
    private Activity activity;
    private EMSAsura thisClassInstance;

    private EMSCallBack callBack, _scannerCallBack;
    private String encodedSignature;
    private final String FORMAT = "windows-1252";
    private String encodedQRCode = "";
    private ReceiveThread receiveThread;
    private Handler handler;// = new Handler();
    private ProgressDialog myProgressDialog;
    private EMSDeviceDriver thisInstance;
    private boolean stopLoop = false;
    private boolean isPOSPrinter = false;
    private byte[] enableCenter, disableCenter;
    private String portNumber = "";
    private EMSDeviceManager edm;
    private CreditCardInfo cardManager;
    private Encrypt encrypt;
    private List<String> printPref;
    private MyPreferences myPref;

    private JAPrinter printer;
    private JAMagstripeReader msr;
    private JABarcodeReader barcodeReader;
    private JAPrinterStatus status;
    private JAPrintCashDrawer cashDrawer;

    @Override
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);

        cardManager = new CreditCardInfo();
        encrypt = new Encrypt(activity);
        this.edm = edm;
        thisInstance = this;
        thisClassInstance = this;

        msr = new JAMagstripeReader();
        barcodeReader = new JABarcodeReader();

        new processConnectionAsync().execute(0);
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        boolean didConnect = false;
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        cardManager = new CreditCardInfo();
        encrypt = new Encrypt(activity);
        this.isPOSPrinter = isPOSPrinter;
        this.edm = edm;
        thisInstance = this;
        thisClassInstance = this;

        try {

            printer = new JAPrinter();
            msr = new JAMagstripeReader();
            barcodeReader = new JABarcodeReader();

            // try
            // {
            // synchronized(printerLock)
            // {
            // printer.claim();
            // printerLock.
            // }
            // }

            status = printer.status();

            if (!status.offline || (status.offline && (status.receiptPaperEmpty || status.receiptPaperLow)))
                didConnect = true;

            if (didConnect) {
                this.edm.driverDidConnectToDevice(thisInstance, false);
            } else {

                this.edm.driverDidNotConnectToDevice(thisInstance, null, false);
            }

        } catch (JAException e) {
            // TODO Auto-generated catch block
        } finally {

        }

        return didConnect;
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

                printer = new JAPrinter();
                // try
                // {
                // synchronized(printerLock)
                // {
                // printer.claim();
                // printerLock.
                // }
                // }

                status = printer.status();

                if (status.offline)
                    msg = "Printer is offline";
                else
                    didConnect = true;

            } catch (JAException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {

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

    @Override
    public void registerAll() {
        this.registerPrinter();
    }

    // private String getString(int id)
    // {
    // return(activity.getResources().getString(id));
    // }
    //

    private Bitmap addLineTextImage(Bitmap dstBitmap, String lineText, int fontSize, Align align) {
        int alignment = 0;
        Bitmap expandedBitmap;
        String text = lineText.replace("\n", "");

        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.BLACK);
        textPaint.setTypeface(Typeface.MONOSPACE);
        textPaint.setTextSize(fontSize);

        FontMetrics fontMetrics = textPaint.getFontMetrics();

        int textWidth = (int) Math.ceil(textPaint.measureText(text));
        int textHeight = (int) Math
                .ceil(Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent) + Math.abs(fontMetrics.leading));

        switch (align) {
            case ALIGN_RIGHT:
                if (textWidth < PAPER_WIDTH) {
                    alignment = PAPER_WIDTH - textWidth;
                }
                break;

            case ALIGN_CENTER:
                if (textWidth < PAPER_WIDTH) {
                    alignment = (PAPER_WIDTH - textWidth) / 2;
                }
                break;

            default: // case ALIGN_LEFT:
                alignment = 0;
                break;
        }

        if (dstBitmap == null) {
            expandedBitmap = Bitmap.createBitmap(textWidth + alignment, textHeight, Bitmap.Config.RGB_565);
        } else {
            if (textWidth + alignment < dstBitmap.getWidth()) {
                textWidth = dstBitmap.getWidth();
            } else {
                textWidth += alignment;
            }
            expandedBitmap = Bitmap.createBitmap(textWidth, textHeight + dstBitmap.getHeight(), Bitmap.Config.RGB_565);
        }

        Canvas canvasText = new Canvas(expandedBitmap);
        canvasText.drawColor(Color.WHITE);

        if (dstBitmap == null) {
            canvasText.drawText(text, alignment, Math.abs(textPaint.getFontMetrics().ascent), textPaint);
        } else {
            canvasText.drawBitmap(dstBitmap, 0, 0, textPaint);
            canvasText.drawText(text, alignment, dstBitmap.getHeight() + Math.abs(textPaint.getFontMetrics().ascent),
                    textPaint);
        }

        return expandedBitmap;
    }

    // private void verifyConnectivity() throws StarIOPortException,
    // InterruptedException
    // {
    // try {
    // if(port==null||port.retreiveStatus()==null&&port.retreiveStatus().offline)
    // port = StarIOPort.getPort(portName, portSettings, 1000, this.activity);
    // }
    // catch(StarIOPortException e)
    // {
    // StarIOPort.releasePort(port);
    // Thread.sleep(1000);
    // port = StarIOPort.getPort(portName, portSettings, 1000, this.activity);
    // }
    // }

	@Override
	public boolean printTransaction(String ordID, Global.OrderType type, boolean isFromHistory, boolean fromOnHold) {
		printReceipt(ordID, LINE_WIDTH, fromOnHold, type, isFromHistory);
		return true;
//		// TODO Auto-generated method stub
//		try {
//
//			// port = StarIOPort.getPort(portName, portSettings, 1000,
//			// this.activity);
//
//			Bitmap textBitmap = null;
//
//			printPref = myPref.getPrintingPreferences();
//			EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
//
//			OrderProductsHandler handler = new OrderProductsHandler(activity);
//			OrderTaxes_DB ordTaxesDB = new OrderTaxes_DB(activity);
//
//			List<DataTaxes> listOrdTaxes = ordTaxesDB.getOrderTaxes(ordID);
//			List<Orders> orders = handler.getPrintOrderedProducts(ordID);
//
//			OrdersHandler orderHandler = new OrdersHandler(activity);
//			Order anOrder = orderHandler.getPrintedOrder(ordID);
//			ClerksHandler clerkHandler = new ClerksHandler(activity);
//
//			StringBuilder sb = new StringBuilder();
//			int size = orders.size();
//
//			this.printImage(0);
//
//			if (printPref.contains(MyPreferences.print_header))
//				this.printHeader();
//
//			if (fromOnHold) {
//				textBitmap = addLineTextImage(textBitmap,
//						textHandler.twoColumnLineWithLeftAlignedText("[" + getString(R.string.on_hold) + "]",
//								anOrder.ord_HoldName, LINE_WIDTH, 0),
//						PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//			}
//
//			switch (type) {
//			case 0: // Order
//				textBitmap = addLineTextImage(textBitmap, textHandler.twoColumnLineWithLeftAlignedText(
//						getString(R.string.order) + ":", ordID, LINE_WIDTH, 0), PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//				break;
//			case 1: // Return
//				textBitmap = addLineTextImage(textBitmap, textHandler.twoColumnLineWithLeftAlignedText(
//						getString(R.string.order) + ":", ordID, LINE_WIDTH, 0), PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//				break;
//			case 2: // Invoice
//				textBitmap = addLineTextImage(textBitmap, textHandler.twoColumnLineWithLeftAlignedText(
//						getString(R.string.invoice) + ":", ordID, LINE_WIDTH, 0), PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//				break;
//			case 3: // Estimate
//				textBitmap = addLineTextImage(textBitmap, textHandler.twoColumnLineWithLeftAlignedText(
//						getString(R.string.estimate) + ":", ordID, LINE_WIDTH, 0), PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//				break;
//			case 5: // Sales Receipt
//				textBitmap = addLineTextImage(textBitmap,
//						textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.sales_receipt) + ":", ordID,
//								LINE_WIDTH, 0),
//						PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//				break;
//			}
//
//			textBitmap = addLineTextImage(textBitmap,
//					textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
//							Global.formatToDisplayDate(anOrder.ord_timecreated, activity, 3), LINE_WIDTH, 0),
//					PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//			if (!myPref.getShiftIsOpen() || myPref.getPreferences(MyPreferences.pref_use_clerks)) {
//				String clerk_id = anOrder.clerk_id;
//				textBitmap = addLineTextImage(textBitmap,
//						textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_clerk),
//								clerkHandler.getClerkName(clerk_id) + "(" + clerk_id + ")", LINE_WIDTH, 0),
//						PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//			}
//
//			textBitmap = addLineTextImage(textBitmap,
//					textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee),
//							myPref.getEmpName() + "(" + myPref.getEmpID() + ")", LINE_WIDTH, 0),
//					PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//
//			String custName = anOrder.cust_name;
//			if (custName != null && !custName.isEmpty()) {
//				textBitmap = addLineTextImage(textBitmap,
//						textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer), custName,
//								LINE_WIDTH, 0),
//						PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//			}
//
//			custName = anOrder.cust_id;
//			if (printPref.contains(MyPreferences.print_customer_id) && custName != null && !custName.isEmpty())
//				textBitmap = addLineTextImage(textBitmap,
//						textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer_id), custName,
//								LINE_WIDTH, 0),
//						PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//
//			String ordComment = anOrder.ord_comment;
//			if (ordComment != null && !ordComment.isEmpty()) {
//				textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 4, Align.ALIGN_LEFT);
//				textBitmap = addLineTextImage(textBitmap, "Comments", PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//				textBitmap = addLineTextImage(textBitmap,
//						textHandler.oneColumnLineWithLeftAlignedText(ordComment, LINE_WIDTH, 3), PRINT_TXT_SIZE + 4,
//						Align.ALIGN_LEFT);
//				// textBitmap =
//				// addLineTextImage(textBitmap,"",PRINT_TXT_SIZE,Align.ALIGN_LEFT);
//			}
//
//			textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 4, Align.ALIGN_LEFT);
//
//			// printer.printBitmapImage(textBitmap);
//			// port.writePort(sb.toString().getBytes(), 0,
//			// sb.toString().length());
//
//			// sb.setLength(0);
//
//			printer.printBitmapImage(textBitmap);
//			textBitmap.recycle();
//			textBitmap = null;
//
//			if (!myPref.getPreferences(MyPreferences.pref_wholesale_printout)) {
//				boolean isRestMode = myPref.getPreferences(MyPreferences.pref_restaurant_mode);
//
//				int m = 0;
//				for (int i = 0; i < size; i++) {
//
//					if (isRestMode) {
//						if ((i + 1 < size && orders.get(i + 1).getAddon().equals("1"))) {
//							m = i;
//							textBitmap = addLineTextImage(textBitmap,
//									textHandler.oneColumnLineWithLeftAlignedText(
//											orders.get(m).getQty() + "x " + orders.get(m).getName(), LINE_WIDTH, 1),
//									PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//							for (int j = i + 1; j < size; j++) {
//								if (orders.get(j).getIsAdded().equals("1"))
//									textBitmap = addLineTextImage(textBitmap,
//											textHandler.twoColumnLineWithLeftAlignedText("- " + orders.get(j).getName(),
//													Global.getCurrencyFormat(orders.get(j).getOverwritePrice()),
//													LINE_WIDTH, 2),
//											PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//								else
//									textBitmap = addLineTextImage(textBitmap,
//											textHandler.twoColumnLineWithLeftAlignedText(
//													"- NO " + orders.get(j).getName(),
//													Global.getCurrencyFormat(orders.get(j).getOverwritePrice()),
//													LINE_WIDTH, 2),
//											PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//
//								if ((j + 1 < size && orders.get(j + 1).getAddon().equals("0")) || (j + 1 >= size)) {
//									i = j;
//									break;
//								}
//
//							}
//
//							textBitmap = addLineTextImage(textBitmap,
//									textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price),
//											Global.getCurrencyFormat(orders.get(m).getOverwritePrice()), LINE_WIDTH, 3),
//									PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//							textBitmap = addLineTextImage(textBitmap,
//									textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
//											Global.getCurrencyFormat(orders.get(m).getTotal()), LINE_WIDTH, 3),
//									PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//
//							if (printPref.contains(MyPreferences.print_descriptions)) {
//								textBitmap = addLineTextImage(textBitmap,
//										textHandler.twoColumnLineWithLeftAlignedText(
//												getString(R.string.receipt_description), "", LINE_WIDTH, 3),
//										PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//
//								String[] temp = textHandler.oneColumnLineWithLeftAlignedText(
//										orders.get(m).getProdDescription(), LINE_WIDTH, 5).split("\n");
//								for (String value : temp) {
//									if (!value.equals("\n"))
//										textBitmap = addLineTextImage(textBitmap, value, PRINT_TXT_SIZE,
//												Align.ALIGN_LEFT);
//								}
//								textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 4, Align.ALIGN_LEFT);
//							}
//
//						} else {
//							textBitmap = addLineTextImage(textBitmap,
//									textHandler.oneColumnLineWithLeftAlignedText(
//											orders.get(i).getQty() + "x " + orders.get(i).getName(), LINE_WIDTH, 1),
//									PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//							textBitmap = addLineTextImage(textBitmap,
//									textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price),
//											Global.getCurrencyFormat(orders.get(i).getOverwritePrice()), LINE_WIDTH, 3),
//									PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//							textBitmap = addLineTextImage(textBitmap,
//									textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
//											Global.getCurrencyFormat(orders.get(i).getTotal()), LINE_WIDTH, 3),
//									PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//
//							if (printPref.contains(MyPreferences.print_descriptions)) {
//								textBitmap = addLineTextImage(textBitmap,
//										textHandler.twoColumnLineWithLeftAlignedText(
//												getString(R.string.receipt_description), "", LINE_WIDTH, 3),
//										PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//								// textBitmap =
//								// addLineTextImage(textBitmap,textHandler.oneColumnLineWithLeftAlignedText(orders.get(i).getProdDescription(),
//								// LINE_WIDTH,
//								// 5),PRINT_TXT_SIZE,Align.ALIGN_LEFT);
//
//								String[] temp = textHandler.oneColumnLineWithLeftAlignedText(
//										orders.get(i).getProdDescription(), LINE_WIDTH, 5).split("\n");
//								for (String value : temp) {
//									if (!value.equals("\n"))
//										textBitmap = addLineTextImage(textBitmap, value, PRINT_TXT_SIZE,
//												Align.ALIGN_LEFT);
//								}
//								textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 4, Align.ALIGN_LEFT);
//							}
//						}
//					} else {
//						textBitmap = addLineTextImage(textBitmap,
//								textHandler.oneColumnLineWithLeftAlignedText(
//										orders.get(i).getQty() + "x " + orders.get(i).getName(), LINE_WIDTH, 1),
//								PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//						textBitmap = addLineTextImage(textBitmap,
//								textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price),
//										Global.getCurrencyFormat(orders.get(i).getOverwritePrice()), LINE_WIDTH, 3),
//								PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//						textBitmap = addLineTextImage(textBitmap,
//								textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
//										Global.getCurrencyFormat(orders.get(i).getTotal()), LINE_WIDTH, 3),
//								PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//
//						if (printPref.contains(MyPreferences.print_descriptions)) {
//							textBitmap = addLineTextImage(textBitmap,
//									textHandler.twoColumnLineWithLeftAlignedText(
//											getString(R.string.receipt_description), "", LINE_WIDTH, 3),
//									PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//							// textBitmap =
//							// addLineTextImage(textBitmap,textHandler.oneColumnLineWithLeftAlignedText(orders.get(i).getProdDescription(),
//							// LINE_WIDTH, 5),PRINT_TXT_SIZE,Align.ALIGN_LEFT);
//
//							String[] temp = textHandler
//									.oneColumnLineWithLeftAlignedText(orders.get(i).getProdDescription(), LINE_WIDTH, 5)
//									.split("\n");
//							for (String value : temp) {
//								if (!value.equals("\n"))
//									textBitmap = addLineTextImage(textBitmap, value, PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//							}
//							textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 4, Align.ALIGN_LEFT);
//						}
//					}
//
//					printer.printBitmapImage(textBitmap);
//					textBitmap.recycle();
//					textBitmap = null;
//				}
//			} else {
//				int padding = LINE_WIDTH / 4;
//				String tempor = Integer.toString(padding);
//				StringBuilder tempSB = new StringBuilder();
//				tempSB.append("%").append(tempor).append("s").append("%").append(tempor).append("s").append("%")
//						.append(tempor).append("s").append("%").append(tempor).append("s");
//
//				textBitmap = addLineTextImage(textBitmap,
//						String.format(tempSB.toString(), "Item", "Qty", "Price", "Total"), PRINT_TXT_SIZE,
//						Align.ALIGN_LEFT);
//				textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 4, Align.ALIGN_LEFT);
//
//				for (int i = 0; i < size; i++) {
//
//					textBitmap = addLineTextImage(textBitmap,
//							orders.get(i).getName() + "-" + orders.get(i).getProdDescription(), PRINT_TXT_SIZE,
//							Align.ALIGN_LEFT);
//					textBitmap = addLineTextImage(textBitmap,
//							String.format(tempSB.toString(), "   ", orders.get(i).getQty(),
//									Global.getCurrencyFormat(orders.get(i).getOverwritePrice()),
//									Global.getCurrencyFormat(orders.get(i).getTotal())),
//							PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//					textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 4, Align.ALIGN_LEFT);
//					// sb.append(textHandler.fourColumnLineWithLeftAlignedText("
//					// ", orders.get(i).getQty(),
//					// Global.getCurrencyFormat(orders.get(i).getOverwritePrice()),
//					// Global.getCurrencyFormat(orders.get(i).getTotal()),
//					// LINE_WIDTH, 3)).append("\n\n");
//
//					// printer.printBitmapImage(addLineTextImage(null,sb.toString(),PRINT_TXT_SIZE,Align.ALIGN_LEFT));
//					// sb.setLength(0);
//
//					printer.printBitmapImage(textBitmap);
//					textBitmap.recycle();
//					textBitmap = null;
//				}
//			}
//			// printer.printBitmapImage(addLineTextImage(null,sb.toString(),PRINT_TXT_SIZE,Align.ALIGN_LEFT));
//			// sb.setLength(0);
//			// printer.printBitmapImage(addLineTextImage(null,textHandler.lines(LINE_WIDTH),PRINT_TXT_SIZE,Align.ALIGN_LEFT));
//
//			textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 4, Align.ALIGN_CENTER);
//			textBitmap = addLineTextImage(textBitmap, textHandler.lines(LINE_WIDTH), PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//
//			textBitmap = addLineTextImage(textBitmap,
//					textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_subtotal),
//							Global.formatDoubleStrToCurrency(anOrder.ord_subtotal), LINE_WIDTH, 0),
//					PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//			textBitmap = addLineTextImage(textBitmap,
//					textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_discount),
//							Global.formatDoubleStrToCurrency(anOrder.ord_discount), LINE_WIDTH, 0),
//					PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//			textBitmap = addLineTextImage(textBitmap,
//					textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_tax),
//							Global.formatDoubleStrToCurrency(anOrder.ord_taxamount), LINE_WIDTH, 0),
//					PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//
//			int num_taxes = listOrdTaxes.size();
//			if (num_taxes > 0) {
//				for (int i = 0; i < num_taxes; i++) {
//					textBitmap = addLineTextImage(textBitmap,
//							textHandler.twoColumnLineWithLeftAlignedText(
//									listOrdTaxes.get(i).get(OrderTaxes_DB.tax_name),
//									listOrdTaxes.get(i).get(OrderTaxes_DB.tax_amount), LINE_WIDTH, 2),
//							PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//				}
//			}
//
//			textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 4, Align.ALIGN_LEFT);
//
//			String granTotal = anOrder.gran_total;
//
//			textBitmap = addLineTextImage(textBitmap,
//					textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_grandtotal),
//							Global.formatDoubleStrToCurrency(granTotal), LINE_WIDTH, 0),
//					PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//
//			PaymentsHandler payHandler = new PaymentsHandler(activity);
//			List<String[]> payArrayList = payHandler.getPaymentForPrintingTransactions(ordID);
//			String receiptSignature = new String();
//			size = payArrayList.size();
//
//			double tempGrandTotal = Double.parseDouble(granTotal);
//			double tempAmount = 0;
//			if (size == 0) {
//				textBitmap = addLineTextImage(textBitmap,
//						textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amountpaid),
//								Global.formatDoubleToCurrency(tempAmount), LINE_WIDTH, 0),
//						PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//				if (type == 2) // Invoice
//				{
//					textBitmap = addLineTextImage(textBitmap,
//							textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_balance_due),
//									Global.formatDoubleToCurrency(tempGrandTotal - tempAmount), LINE_WIDTH, 0),
//							PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//				}
//				textBitmap = addLineTextImage(textBitmap,
//						textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total_tip_paid),
//								Global.formatDoubleToCurrency(0.00), LINE_WIDTH, 0),
//						PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//				textBitmap = addLineTextImage(textBitmap,
//						textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cash_returned),
//								Global.formatDoubleToCurrency(0.00), LINE_WIDTH, 0),
//						PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//
//				printer.printBitmapImage(textBitmap);
//			} else {
//				Bitmap textBitmap2 = null;
//
//				tempAmount = formatStrToDouble(payArrayList.get(0)[9]);
//				String _pay_type = payArrayList.get(0)[1].toUpperCase(Locale.getDefault()).trim();
//				double tempTipAmount = formatStrToDouble(payArrayList.get(0)[2]);
//				StringBuilder tempSB = new StringBuilder();
//				textBitmap2 = addLineTextImage(textBitmap2, textHandler.oneColumnLineWithLeftAlignedText(
//						Global.formatDoubleStrToCurrency(payArrayList.get(0)[9]) + "[" + payArrayList.get(0)[1] + "]",
//						LINE_WIDTH, 1), PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//
//				if (!_pay_type.equals("CASH") && !_pay_type.equals("CHECK")) {
//					textBitmap2 = addLineTextImage(textBitmap2, textHandler.oneColumnLineWithLeftAlignedText(
//							"TransID: " + payArrayList.get(0)[4], LINE_WIDTH, 1), PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//					textBitmap2 = addLineTextImage(textBitmap2, textHandler.oneColumnLineWithLeftAlignedText(
//							"CC#: *" + payArrayList.get(0)[5], LINE_WIDTH, 1), PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//				}
//				if (!payArrayList.get(0)[3].isEmpty())
//					receiptSignature = payArrayList.get(0)[3];
//
//				for (int i = 1; i < size; i++) {
//					_pay_type = payArrayList.get(i)[1].toUpperCase(Locale.getDefault()).trim();
//					tempAmount = tempAmount + formatStrToDouble(payArrayList.get(i)[9]);
//					tempTipAmount = tempTipAmount + formatStrToDouble(payArrayList.get(i)[2]);
//					textBitmap2 = addLineTextImage(textBitmap2,
//							textHandler.oneColumnLineWithLeftAlignedText(
//									Global.formatDoubleStrToCurrency(payArrayList.get(i)[9]) + "["
//											+ payArrayList.get(i)[1] + "]",
//									LINE_WIDTH, 1),
//							PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//					if (!_pay_type.equals("CASH") && !_pay_type.equals("CHECK")) {
//						textBitmap2 = addLineTextImage(textBitmap2, textHandler.oneColumnLineWithLeftAlignedText(
//								"TransID: " + payArrayList.get(i)[4], LINE_WIDTH, 1), PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//						textBitmap2 = addLineTextImage(textBitmap2, textHandler.oneColumnLineWithLeftAlignedText(
//								"CC#: *" + payArrayList.get(i)[5], LINE_WIDTH, 1), PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//					}
//					if (!payArrayList.get(i)[3].isEmpty())
//						receiptSignature = payArrayList.get(i)[3];
//				}
//				textBitmap = addLineTextImage(textBitmap,
//						textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amountpaid),
//								Global.formatDoubleStrToCurrency(Double.toString(tempAmount)), LINE_WIDTH, 0),
//						PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//
//				printer.printBitmapImage(textBitmap);
//				printer.printBitmapImage(textBitmap2);
//				textBitmap = null;
//
//				if (type == 2) // Invoice
//				{
//					textBitmap = addLineTextImage(textBitmap,
//							textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_balance_due),
//									Global.formatDoubleToCurrency(tempGrandTotal - tempAmount), LINE_WIDTH, 0),
//							PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//				}
//				textBitmap = addLineTextImage(textBitmap,
//						textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total_tip_paid),
//								Global.formatDoubleStrToCurrency(Double.toString(tempTipAmount)), LINE_WIDTH, 0),
//						PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//
//				tempAmount = formatStrToDouble(granTotal) - tempAmount;
//				if (tempAmount > 0)
//					tempAmount = 0.00;
//
//				textBitmap = addLineTextImage(textBitmap,
//						textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cash_returned),
//								Global.formatDoubleStrToCurrency(Double.toString(tempAmount)), LINE_WIDTH, 0),
//						PRINT_TXT_SIZE, Align.ALIGN_LEFT);
//
//				// printer.printBitmapImage(addLineTextImage(null,"",PRINT_TXT_SIZE+4,Align.ALIGN_LEFT));
//				// port.writePort(sb.toString().getBytes(FORMAT), 0,
//				// sb.toString().length());
//				printer.printBitmapImage(textBitmap);
//			}
//
//			// printer.printBitmapImage(addLineTextImage(null,sb.toString(),PRINT_TXT_SIZE,Align.ALIGN_LEFT));
//
//			printer.printBitmapImage(addLineTextImage(null, " ", PRINT_TXT_SIZE + 8, Align.ALIGN_LEFT));
//
//			if (printPref.contains(MyPreferences.print_footer))
//				this.printFooter();
//
//			// printer.printBitmapImage(addLineTextImage(null,"",PRINT_TXT_SIZE+4,Align.ALIGN_LEFT));
//			// printer.printBitmapImage(addLineTextImage(null,textHandler.newLines(2),PRINT_TXT_SIZE,Align.ALIGN_LEFT));
//			// port.writePort(textHandler.newLines(2).getBytes(FORMAT), 0,
//			// textHandler.newLines(2).length());
//
//			textBitmap = null;
//			receiptSignature = anOrder.ord_signature;
//			if (!receiptSignature.isEmpty()) {
//				this.encodedSignature = receiptSignature;
//				this.printImage(1);
//
//				textBitmap = addLineTextImage(textBitmap, "x" + textHandler.lines(LINE_WIDTH / 2), PRINT_TXT_SIZE,
//						Align.ALIGN_CENTER);
//				textBitmap = addLineTextImage(textBitmap, getString(R.string.receipt_signature), PRINT_TXT_SIZE,
//						Align.ALIGN_CENTER);
//				textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 4, Align.ALIGN_LEFT);
//				printer.printBitmapImage(textBitmap);
//			}
//
//			if (isFromHistory) {
//				sb.setLength(0);
//
//				textBitmap = null;
//				textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 4, Align.ALIGN_CENTER);
//				textBitmap = addLineTextImage(textBitmap, "*** Copy ***", PRINT_TXT_SIZE, Align.ALIGN_CENTER);
//				textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 4, Align.ALIGN_CENTER);
//				printer.printBitmapImage(textBitmap);
//			}
//
//			printer.feedMM(5);
//			printer.cut(true);
//
//		} catch (JAException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return true;
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

            Bitmap textBitmap = null;
            EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
            printPref = myPref.getPrintingPreferences();

            PaymentsHandler payHandler = new PaymentsHandler(activity);
			PaymentDetails paymentDetails = payHandler.getPrintingForPaymentDetails(payID, type);
            StringBuilder sb = new StringBuilder();
            boolean isCashPayment = false;
            boolean isCheckPayment = false;
            String constantValue = null;
            String creditCardFooting = "";

			if (paymentDetails.getPaymethod_name().toUpperCase(Locale.getDefault()).trim().equals("CASH"))
                isCashPayment = true;
			else if (paymentDetails.getPaymethod_name().toUpperCase(Locale.getDefault()).trim().equals("CHECK"))
                isCheckPayment = true;
            else {
                constantValue = getString(R.string.receipt_included_tip);
                creditCardFooting = getString(R.string.receipt_creditcard_terms);
            }

            this.printImage(0);

            if (printPref.contains(MyPreferences.print_header))
                this.printHeader();

			sb.append("* ").append(paymentDetails.getPaymethod_name());
			if (paymentDetails.getIs_refund().equals("1"))
                sb.append(" Refund *");
            else
                sb.append(" Sale *");

            textBitmap = addLineTextImage(textBitmap, sb.toString(), PRINT_TXT_SIZE, Align.ALIGN_CENTER);
            // printer.printBitmapImage(addLineTextImage(null,sb.toString(),PRINT_TXT_SIZE,Align.ALIGN_CENTER));

            sb.setLength(0);
            textBitmap = addLineTextImage(textBitmap,
                    textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
                            getString(R.string.receipt_time), LINE_WIDTH, 0),
                    PRINT_TXT_SIZE, Align.ALIGN_LEFT);
            textBitmap = addLineTextImage(textBitmap,
					textHandler.twoColumnLineWithLeftAlignedText(paymentDetails.getPay_date(), paymentDetails.getPay_timecreated(), LINE_WIDTH, 0),
                    PRINT_TXT_SIZE, Align.ALIGN_LEFT);
            textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 4, Align.ALIGN_LEFT);

            textBitmap = addLineTextImage(textBitmap, textHandler
					.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer), paymentDetails.getCust_name(), LINE_WIDTH, 0),
                    PRINT_TXT_SIZE, Align.ALIGN_LEFT);
            textBitmap = addLineTextImage(textBitmap, textHandler.twoColumnLineWithLeftAlignedText(
                    getString(R.string.receipt_idnum), payID, LINE_WIDTH, 0), PRINT_TXT_SIZE, Align.ALIGN_LEFT);

            if (!isCashPayment && !isCheckPayment) {
                textBitmap = addLineTextImage(textBitmap,
                        textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cardnum),
								"*" + paymentDetails.getCcnum_last4(), LINE_WIDTH, 0),
                        PRINT_TXT_SIZE, Align.ALIGN_LEFT);
                textBitmap = addLineTextImage(textBitmap,
						textHandler.twoColumnLineWithLeftAlignedText("TransID:", paymentDetails.getPay_transid(), LINE_WIDTH, 0),
                        PRINT_TXT_SIZE, Align.ALIGN_LEFT);
            } else if (isCheckPayment) {
                textBitmap = addLineTextImage(textBitmap,
						textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_checknum), paymentDetails.getPay_check(),
                                LINE_WIDTH, 0),
                        PRINT_TXT_SIZE, Align.ALIGN_LEFT);
            }

            textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 4, Align.ALIGN_LEFT);

            textBitmap = addLineTextImage(textBitmap,
                    textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
							Global.formatDoubleStrToCurrency(paymentDetails.getOrd_total()), LINE_WIDTH, 0),
                    PRINT_TXT_SIZE, Align.ALIGN_LEFT);
            textBitmap = addLineTextImage(textBitmap,
                    textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_paid),
							Global.formatDoubleStrToCurrency(paymentDetails.getPay_amount()), LINE_WIDTH, 0),
                    PRINT_TXT_SIZE, Align.ALIGN_LEFT);

			String change = paymentDetails.getChange();

            if (isCashPayment && isCheckPayment && !change.isEmpty() && change.contains(".")
                    && Double.parseDouble(change) > 0)
                change = "";

            sb.setLength(0);
            if (constantValue != null)
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(constantValue,
                        Global.formatDoubleStrToCurrency(change), LINE_WIDTH, 0));

            textBitmap = addLineTextImage(textBitmap, sb.toString(), PRINT_TXT_SIZE, Align.ALIGN_LEFT);

            textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 4, Align.ALIGN_LEFT);

            printer.printBitmapImage(textBitmap);
            textBitmap.recycle();
            textBitmap = null;

            if (!isCashPayment && !isCheckPayment) {
                if (myPref.getPreferences(MyPreferences.pref_handwritten_signature)) {
                    textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 4, Align.ALIGN_LEFT);
				} else if (!paymentDetails.getPay_signature().isEmpty()) {
					encodedSignature = paymentDetails.getPay_signature();
                    this.printImage(1);
                }

                textBitmap = addLineTextImage(textBitmap, "x" + textHandler.lines(LINE_WIDTH / 2), PRINT_TXT_SIZE,
                        Align.ALIGN_CENTER);
                textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 4, Align.ALIGN_LEFT);
            }

            printer.printBitmapImage(textBitmap);
            textBitmap.recycle();
            textBitmap = null;

            if (Global.isIvuLoto) {
                sb = new StringBuilder();

                if (!printPref.contains(MyPreferences.print_ivuloto_qr)) {
                    textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 2, Align.ALIGN_LEFT);
                    textBitmap = addLineTextImage(textBitmap,
                            textHandler.centeredString(textHandler.ivuLines(2 * LINE_WIDTH / 3), LINE_WIDTH),
                            PRINT_TXT_SIZE, Align.ALIGN_LEFT);
                    textBitmap = addLineTextImage(textBitmap,
							textHandler.centeredString("CONTROL: " + paymentDetails.getIvuLottoNumber(), LINE_WIDTH), PRINT_TXT_SIZE,
                            Align.ALIGN_LEFT);
//					textBitmap = addLineTextImage(textBitmap, textHandler.centeredString(payArray[12], LINE_WIDTH),
//							PRINT_TXT_SIZE, Align.ALIGN_LEFT);
                    textBitmap = addLineTextImage(textBitmap,
                            textHandler.centeredString(textHandler.ivuLines(2 * LINE_WIDTH / 3), LINE_WIDTH),
                            PRINT_TXT_SIZE, Align.ALIGN_LEFT);
                    textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 2, Align.ALIGN_LEFT);

                    // printer.printBitmapImage(addLineTextImage(null,sb.toString(),PRINT_TXT_SIZE,Align.ALIGN_LEFT));
                } else {
//					encodedQRCode = payArray[14];

//					this.printImage(2);
                    textBitmap = addLineTextImage(textBitmap, textHandler.ivuLines(2 * LINE_WIDTH / 3), PRINT_TXT_SIZE,
                            Align.ALIGN_CENTER);
					textBitmap = addLineTextImage(textBitmap, "CONTROL: " + paymentDetails.getIvuLottoNumber(), PRINT_TXT_SIZE,
                            Align.ALIGN_CENTER);
//					textBitmap = addLineTextImage(textBitmap, payArray[12], PRINT_TXT_SIZE, Align.ALIGN_CENTER);
                    textBitmap = addLineTextImage(textBitmap, textHandler.ivuLines(2 * LINE_WIDTH / 3), PRINT_TXT_SIZE,
                            Align.ALIGN_CENTER);

                }
            }
            this.printFooter();

            if (!isCashPayment && !isCheckPayment) {
                String[] temp = textHandler.oneColumnLineWithLeftAlignedText(creditCardFooting, LINE_WIDTH, 0)
                        .split("\n");
                for (String value : temp) {
                    if (!value.equals("\n"))
                        textBitmap = addLineTextImage(textBitmap, value, PRINT_TXT_SIZE, Align.ALIGN_CENTER);
                }

                textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 4, Align.ALIGN_LEFT);
                // printer.printBitmapImage(textBitmap);

            }

            if (isReprint) {
                textBitmap = addLineTextImage(textBitmap, "*** Copy ***", PRINT_TXT_SIZE, Align.ALIGN_CENTER);
                // printer.printBitmapImage(textBitmap);
            }

            printer.printBitmapImage(textBitmap);

            printer.cut(true);

        } catch (JAException e) {
            // TODO Auto-generated catch block
            return false;
        }
        return true;
    }

    protected void printImage(int type) throws JAException {
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
                    // myBitmap =
                    // getResizedBitmap(myBitmap,myBitmap.getHeight()/2,myBitmap.getWidth()/2);
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
            printer.printBitmapImage(myBitmap);
            myBitmap.recycle();
            myBitmap = null;
            Bitmap textBitmap = addLineTextImage(null, " ", PRINT_TXT_SIZE + 4, Align.ALIGN_CENTER);
            printer.printBitmapImage(textBitmap);
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
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

            PaymentsHandler paymentHandler = new PaymentsHandler(activity);
            PayMethodsHandler payMethodHandler = new PayMethodsHandler(activity);
            EMSPlainTextHelper textHandler = new EMSPlainTextHelper();

            Bitmap textBitmap = addLineTextImage(null, "REPORT", PRINT_TXT_SIZE, Align.ALIGN_CENTER);
            textBitmap = addLineTextImage(textBitmap, Global.formatToDisplayDate(curDate, activity, 0), PRINT_TXT_SIZE,
                    Align.ALIGN_CENTER);
            textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + (2 * 2), Align.ALIGN_LEFT);
            textBitmap = addLineTextImage(textBitmap, getString(R.string.receipt_pay_summary), PRINT_TXT_SIZE,
                    Align.ALIGN_LEFT);

            Bitmap refundBitmap = addLineTextImage(null, getString(R.string.receipt_refund_summmary), PRINT_TXT_SIZE,
                    Align.ALIGN_LEFT);

            HashMap<String, String> paymentMap = paymentHandler
                    .getPaymentsRefundsForReportPrinting(Global.formatToDisplayDate(curDate, activity, 4), 0);
            HashMap<String, String> refundMap = paymentHandler
                    .getPaymentsRefundsForReportPrinting(Global.formatToDisplayDate(curDate, activity, 4), 1);
            List<String[]> payMethodsNames = payMethodHandler.getPayMethodsName();
            int size = payMethodsNames.size();
            double payGranTotal = 0.00;
            double refundGranTotal = 0.00;

            for (int i = 0; i < size; i++) {
                if (paymentMap.containsKey(payMethodsNames.get(i)[0])) {
                    // sb.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
                    // Global.formatDoubleStrToCurrency(paymentMap.get(payMethodsNames.get(i)[0])),
                    // LINE_WIDTH, 3));
                    textBitmap = addLineTextImage(textBitmap,
                            textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
                                    Global.formatDoubleStrToCurrency(paymentMap.get(payMethodsNames.get(i)[0])),
                                    LINE_WIDTH, 3),
                            PRINT_TXT_SIZE, Align.ALIGN_LEFT);
                    payGranTotal += Double.parseDouble(paymentMap.get(payMethodsNames.get(i)[0]));

                } else {
                    // sb.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
                    // Global.formatDoubleToCurrency(0.00), LINE_WIDTH, 3));
                    textBitmap = addLineTextImage(textBitmap,
                            textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
                                    Global.formatDoubleToCurrency(0.00), LINE_WIDTH, 3),
                            PRINT_TXT_SIZE, Align.ALIGN_LEFT);
                }

                if (refundMap.containsKey(payMethodsNames.get(i)[0])) {
                    refundBitmap = addLineTextImage(refundBitmap,
                            textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
                                    Global.formatDoubleStrToCurrency(refundMap.get(payMethodsNames.get(i)[0])),
                                    LINE_WIDTH, 3),
                            PRINT_TXT_SIZE, Align.ALIGN_LEFT);
                    // sb_refunds.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
                    // Global.formatDoubleStrToCurrency(refundMap.get(payMethodsNames.get(i)[0])),
                    // LINE_WIDTH, 3));
                    refundGranTotal += Double.parseDouble(refundMap.get(payMethodsNames.get(i)[0]));
                } else {
                    refundBitmap = addLineTextImage(refundBitmap,
                            textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
                                    Global.formatDoubleToCurrency(0.00), LINE_WIDTH, 3),
                            PRINT_TXT_SIZE, Align.ALIGN_LEFT);
                    // sb_refunds.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
                    // Global.formatDoubleToCurrency(0.00), LINE_WIDTH, 3));
                }
            }

            textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + (2 * 2), Align.ALIGN_LEFT);
            textBitmap = addLineTextImage(textBitmap,
                    textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
                            Global.formatDoubleStrToCurrency(Double.toString(payGranTotal)), LINE_WIDTH, 4),
                    PRINT_TXT_SIZE, Align.ALIGN_LEFT);
            textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + (2 * 3), Align.ALIGN_LEFT);

            refundBitmap = addLineTextImage(refundBitmap, " ", PRINT_TXT_SIZE + (2 * 2), Align.ALIGN_LEFT);
            refundBitmap = addLineTextImage(refundBitmap,
                    textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
                            Global.formatDoubleStrToCurrency(Double.toString(refundGranTotal)), LINE_WIDTH, 4),
                    PRINT_TXT_SIZE, Align.ALIGN_LEFT);

            printer.printBitmapImage(textBitmap);
            printer.printBitmapImage(refundBitmap);
            printer.feedMM(5);
            printer.cut(true);

            textBitmap.recycle();
            refundBitmap.recycle();

        } catch (JAException e) {
            // TODO Auto-generated catch block
            return true;
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
        edm.currentDevice = null;
    }

    public void printHeader() {

        EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
        StringBuilder sb = new StringBuilder();

        MemoTextHandler handler = new MemoTextHandler(activity);
        String[] header = handler.getHeader();
        Bitmap textBitmap = null;

        if (header[0] != null && !header[0].isEmpty())
            textBitmap = addLineTextImage(textBitmap, textHandler.formatLongString(header[0], LINE_WIDTH),
                    PRINT_TXT_SIZE, Align.ALIGN_CENTER);
        if (header[1] != null && !header[1].isEmpty())
            textBitmap = addLineTextImage(textBitmap, textHandler.formatLongString(header[1], LINE_WIDTH),
                    PRINT_TXT_SIZE, Align.ALIGN_CENTER);
        if (header[2] != null && !header[2].isEmpty())
            textBitmap = addLineTextImage(textBitmap, textHandler.formatLongString(header[2], LINE_WIDTH),
                    PRINT_TXT_SIZE, Align.ALIGN_CENTER);

        textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + 4, Align.ALIGN_LEFT);
        try {
            printer.printBitmapImage(textBitmap);
            textBitmap.recycle();
        } catch (JAException e) {
            // TODO Auto-generated catch block
        }
    }

    public void printFooter() {

        EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
        StringBuilder sb = new StringBuilder();
        MemoTextHandler handler = new MemoTextHandler(activity);
        String[] footer = handler.getFooter();
        Bitmap textBitmap = null;

        if (footer[0] != null && !footer[0].isEmpty())
            textBitmap = addLineTextImage(textBitmap, textHandler.formatLongString(footer[0], LINE_WIDTH),
                    PRINT_TXT_SIZE, Align.ALIGN_CENTER);
        if (footer[1] != null && !footer[1].isEmpty())
            textBitmap = addLineTextImage(textBitmap, textHandler.formatLongString(footer[1], LINE_WIDTH),
                    PRINT_TXT_SIZE, Align.ALIGN_CENTER);
        if (footer[2] != null && !footer[2].isEmpty())
            textBitmap = addLineTextImage(textBitmap, textHandler.formatLongString(footer[2], LINE_WIDTH),
                    PRINT_TXT_SIZE, Align.ALIGN_CENTER);

        textBitmap = addLineTextImage(textBitmap, " ", PRINT_TXT_SIZE + (2 * 2), Align.ALIGN_LEFT);
        try {
            printer.printBitmapImage(textBitmap);
        } catch (JAException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
/*
    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSig) {

        try {
            // port = StarIOPort.getPort(portName, portSettings, 10000,
            // this.activity);

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

            this.printImage(0);

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

                    // printer.printBitmapImage(addLineTextImage(null,sb.toString(),PRINT_TXT_SIZE,Align.ALIGN_LEFT));
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

            // printer.printBitmapImage(addLineTextImage(null,sb.toString(),PRINT_TXT_SIZE,Align.ALIGN_LEFT));

            if (printPref.contains(MyPreferences.print_descriptions))
                this.printFooter();

            this.printImage(1);

            // printer.printBitmapImage(addLineTextImage(null,textHandler.newLines(3),PRINT_TXT_SIZE,Align.ALIGN_LEFT));

            printer.cut(true);

            // db.close();

        } catch (JAException e) {

            return true;
        }
        return true;
    }
*/
	@Override
	public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSig) {

		printConsignmentReceipt(myConsignment, encodedSig, LINE_WIDTH);


		return true;
	}

    @Override
    public void releaseCardReader() {
        // TODO Auto-generated method stub
        try {
            msr.release();
        } catch (JAException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void loadCardReader(EMSCallBack _callBack, boolean isDebitCard) {
        // TODO Auto-generated method stub

        callBack = _callBack;
        if (handler == null)
            handler = new Handler();

        try {
            msr.claim();
            msr.addListener(this);
            handler.post(doUpdateDidConnect);
        } catch (JAException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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
    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSig) {
        // TODO Auto-generated method stub
        try {
            // port = StarIOPort.getPort(portName, portSettings, 10000,
            // this.activity);

            printPref = myPref.getPrintingPreferences();
            EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
            StringBuilder sb = new StringBuilder();
            // SQLiteDatabase db = new DBManager(activity).openReadableDB();
            ProductsHandler productDBHandler = new ProductsHandler(activity);
            HashMap<String, String> map = new HashMap<String, String>();
            String prodDesc = "";

            int size = myConsignment.size();

            this.printImage(0);

            if (printPref.contains(MyPreferences.print_header))
                this.printHeader();

            sb.append(textHandler.centeredString("Consignment Pickup Summary", LINE_WIDTH)).append("\n\n");

            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer),
                    myPref.getCustName(), LINE_WIDTH, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee),
                    myPref.getEmpName(), LINE_WIDTH, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
                    Global.formatToDisplayDate(Global.getCurrentDate(), activity, 3), LINE_WIDTH, 0));
            sb.append(textHandler.newLines(3));

            for (int i = 0; i < size; i++) {
                map = productDBHandler.getProductMap(myConsignment.get(i).ConsProd_ID, true);

                sb.append(textHandler.oneColumnLineWithLeftAlignedText(map.get("prod_name"), LINE_WIDTH, 0));

                if (printPref.contains(MyPreferences.print_descriptions)) {
                    prodDesc = map.get("prod_desc");
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description), "",
                            LINE_WIDTH, 3)).append("\n");
                    if (!prodDesc.isEmpty())
                        sb.append(textHandler.oneColumnLineWithLeftAlignedText(prodDesc, LINE_WIDTH, 5)).append("\n");
                }

                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original Qty:",
                        myConsignment.get(i).ConsOriginal_Qty, LINE_WIDTH, 3));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Picked up Qty:",
                        myConsignment.get(i).ConsPickup_Qty, LINE_WIDTH, 3));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("New Qty:", myConsignment.get(i).ConsNew_Qty,
                        LINE_WIDTH, 3)).append("\n\n\n");

                // printer.printBitmapImage(addLineTextImage(null,sb.toString(),PRINT_TXT_SIZE,Align.ALIGN_LEFT));
                sb.setLength(0);
            }

            if (printPref.contains(MyPreferences.print_footer))
                this.printFooter();

            if (!encodedSig.isEmpty()) {
                this.encodedSignature = encodedSig;
                this.printImage(1);

                sb.setLength(0);
                sb.append("x").append(textHandler.lines(LINE_WIDTH / 2)).append("\n");
                sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(4));

                // printer.printBitmapImage(addLineTextImage(null,sb.toString(),PRINT_TXT_SIZE,Align.ALIGN_CENTER));
            }

            printer.cut(true);

            // db.close();

        } catch (JAException e) {
            // TODO Auto-generated catch block
            return true;
        }
        return true;
    }

    @Override
    public boolean printOpenInvoices(String invID) {
        // TODO Auto-generated method stub
        try {

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

            if (printPref.contains(MyPreferences.print_header))
                this.printHeader();

            sb.append(textHandler.centeredString("Open Invoice Summary", LINE_WIDTH)).append("\n\n");

            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_invoice), rightInfo[1],
                    LINE_WIDTH, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_invoice_ref),
                    rightInfo[2], LINE_WIDTH, 0));
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

            // printer.printBitmapImage(addLineTextImage(null,sb.toString(),PRINT_TXT_SIZE,Align.ALIGN_LEFT));

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

                // printer.printBitmapImage(addLineTextImage(null,sb.toString(),PRINT_TXT_SIZE,Align.ALIGN_LEFT));
                sb.setLength(0);
            }

            sb.append(textHandler.centeredString(getString(R.string.receipt_thankyou), LINE_WIDTH));
            // printer.printBitmapImage(addLineTextImage(null,sb.toString(),PRINT_TXT_SIZE,Align.ALIGN_LEFT));
            // printer.printBitmapImage(addLineTextImage(null,textHandler.newLines(3),PRINT_TXT_SIZE,Align.ALIGN_LEFT));

            printer.cut(true);

        } catch (JAException e) {
            // TODO Auto-generated catch block
            return true;
        }
        return true;
    }

    @Override
    public void printStationPrinter(List<Orders> orders, String ordID) {
        // TODO Auto-generated method stub
    }

    @Override
    public void openCashDrawer() {
        // TODO Auto-generated method stub

        new Thread(new Runnable() {
            public void run() {
                try {
                    printer.openCashDrawer(JAPrinter.JAPrintCashDrawer.JAPRINT_CASHDRAWER_1);
                    printer.openCashDrawer(JAPrinter.JAPrintCashDrawer.JAPRINT_CASHDRAWER_2);
                } catch (JAException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
        // TODO Auto-generated method stub
        try {
            // port = StarIOPort.getPort(portName, portSettings, 10000,
            // this.activity);

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
                sb.append(textHandler.centeredString(getString(R.string.consignment_summary), LINE_WIDTH))
                        .append("\n\n");
            else
                sb.append(textHandler.centeredString(getString(R.string.consignment_pickup), LINE_WIDTH))
                        .append("\n\n");

            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer),
                    map.get("cust_name"), LINE_WIDTH, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee),
                    myPref.getEmpName(), LINE_WIDTH, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cons_trans_id),
                    map.get("ConsTrans_ID"), LINE_WIDTH, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
                    Global.formatToDisplayDate(Global.getCurrentDate(), activity, 3), LINE_WIDTH, 0));
            sb.append(textHandler.newLines(3));

            for (int i = 0; i < size; i++) {
                c.moveToPosition(i);

                sb.append(textHandler.oneColumnLineWithLeftAlignedText(c.getString(c.getColumnIndex("prod_name")),
                        LINE_WIDTH, 0));

                if (printPref.contains(MyPreferences.print_descriptions)) {
                    prodDesc = c.getString(c.getColumnIndex("prod_desc"));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description), "",
                            LINE_WIDTH, 3)).append("\n");
                    if (!prodDesc.isEmpty())
                        sb.append(textHandler.oneColumnLineWithLeftAlignedText(
                                c.getString(c.getColumnIndex("prod_desc")), LINE_WIDTH, 5)).append("\n");
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
                            Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex("item_subtotal"))),
                            LINE_WIDTH, 5));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Credit Memo:",
                            Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex("credit_memo"))), LINE_WIDTH,
                            5));

                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total:",
                            Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex("item_total"))), LINE_WIDTH,
                            5)).append(textHandler.newLines(2));
                } else {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original Qty:",
                            c.getString(c.getColumnIndex("ConsOriginal_Qty")), LINE_WIDTH, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Picked up Qty:",
                            c.getString(c.getColumnIndex("ConsPickup_Qty")), LINE_WIDTH, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("New Qty:",
                            c.getString(c.getColumnIndex("ConsNew_Qty")), LINE_WIDTH, 3)).append("\n\n\n");

                }
                // printer.printBitmapImage(addLineTextImage(null,sb.toString(),PRINT_TXT_SIZE,Align.ALIGN_LEFT));
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
            sb.append(textHandler.newLines(3));

            // printer.printBitmapImage(addLineTextImage(null,sb.toString(),PRINT_TXT_SIZE,Align.ALIGN_LEFT));

            if (printPref.contains(MyPreferences.print_footer))
                this.printFooter();

            this.printImage(1);

            // printer.printBitmapImage(addLineTextImage(null,textHandler.newLines(3),PRINT_TXT_SIZE,Align.ALIGN_LEFT));

            printer.cut(true);

        } catch (JAException e) {
            // TODO Auto-generated catch block
            return false;
        }

        return true;
    }

    @Override
    public void loadScanner(EMSCallBack _callBack) {
        // TODO Auto-generated method stub
        try {
            _scannerCallBack = _callBack;
            if (handler == null)
                handler = new Handler();
            if (_callBack != null) {
                new Thread(new Runnable() {
                    public void run() {

                        try {
                            barcodeReader.claim();
                            barcodeReader.setMode(JABarcodeMode.JABARCODE_MODE_AUTO);
                            barcodeReader.addBarCodeListener(thisClassInstance);
                        } catch (JAException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                }).start();

            } else
                barcodeReader.release();
        } catch (JAException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public boolean isUSBConnected() {
        return false;
    }

    @Override
    public void msrError() {
        // TODO Auto-generated method stub

    }

    private StringBuilder msr_data = new StringBuilder();
    private int count = 0;

    @Override
    public void msrRead() {
        // TODO Auto-generated method stub
        ArrayList<byte[]> msrData = msr.getAllMSRData();
        int size = msrData.size();

        for (int i = 0; i < size; i++) {
            msr_data.append(new String(msrData.get(i)));
        }

        count++;

        if (count >= 3) {
            this.cardManager = Global.parseSimpleMSR(activity, msr_data.toString().replace("\r", ""));
            handler.post(doUpdateViews);
            count = 0;
            msr_data.setLength(0);
        }
    }

    @Override
    public void barcodeScanned() {
        // TODO Auto-generated method stub
        try {
            scannedData = barcodeReader.getNextBarcodeString();
            handler.post(runnableScannedData);
        } catch (JAException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void barcodeScannerError() {
        // TODO Auto-generated method stub

    }

    private String scannedData = "";

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

}
