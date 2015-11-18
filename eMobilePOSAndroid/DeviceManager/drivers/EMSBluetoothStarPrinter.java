package drivers;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.android.database.StoredPayments_DB;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.Orders;
import com.android.support.CardParser;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.DBManager;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.emobilepos.app.R;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;
import com.starmicronics.starioextension.starioextmanager.StarIoExtManager;
import com.starmicronics.starioextension.starioextmanager.StarIoExtManagerListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import main.EMSDeviceManager;
import plaintext.EMSPlainTextHelper;
import protocols.EMSCallBack;
import protocols.EMSDeviceManagerPrinterDelegate;
import util.RasterDocument;
import util.RasterDocument.RasPageEndMode;
import util.RasterDocument.RasSpeed;
import util.RasterDocument.RasTopMargin;
import util.StarBitmap;

public class EMSBluetoothStarPrinter extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {

	private int LINE_WIDTH = 32;
	private int PAPER_WIDTH;
	private String portSettings, portName;

	private StarIOPort portForCardReader;
	private byte[] outputByteBuffer = null;
	private EMSCallBack callBack, scannerCallBack;
	private StarIoExtManager mStarIoExtManager;
	private ReceiveThread receiveThread;
	private Handler handler;// = new Handler();
	private ProgressDialog myProgressDialog;
	private EMSDeviceDriver thisInstance;
	private boolean stopLoop = false;

	private String portNumber = "";
	private EMSDeviceManager edm;
	private CreditCardInfo cardManager;
	private Encrypt encrypt;

	@Override
	public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
		this.activity = activity;
		myPref = new MyPreferences(this.activity);

		cardManager = new CreditCardInfo();
		encrypt = new Encrypt(activity);
		this.isPOSPrinter = isPOSPrinter;
		this.edm = edm;
		thisInstance = this;
		LINE_WIDTH = paperSize;

		switch (LINE_WIDTH) {
		case 32:
			PAPER_WIDTH = 408;
			break;
		case 48:
			PAPER_WIDTH = 576;
			break;
		case 69:
			PAPER_WIDTH = 832;// 5400
			break;
		}

		portName = myPref.printerMACAddress(true, null);
		portNumber = myPref.getStarPort();

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
		LINE_WIDTH = paperSize;

		switch (LINE_WIDTH) {
		case 32:
			PAPER_WIDTH = 420;
			break;
		case 48:
			PAPER_WIDTH = 1600;
			break;
		case 69:
			PAPER_WIDTH = 300;// 5400
			break;
		}

		if (_portName != null || _portNumber != null) {
			portName = _portName;
			portNumber = _portNumber;
		}

		try {

			if (!isPOSPrinter) {
				portSettings = "mini";
				port = getStarIOPort();
				enableCenter = new byte[] { 0x1b, 0x61, 0x01 };
				disableCenter = new byte[] { 0x1b, 0x61, 0x00 };
			} else {
				if (portName.contains("TCP") && !portNumber.equals("9100"))
					portSettings = portNumber;
				else
					portSettings = "";

				port = getStarIOPort();
				enableCenter = new byte[] { 0x1b, 0x1d, 0x61, 0x01 };
				disableCenter = new byte[] { 0x1b, 0x1d, 0x61, 0x00 };
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			if (port != null && port.retreiveStatus().offline == false) {
				didConnect = true;

			}

			if (didConnect) {
				this.edm.driverDidConnectToDevice(thisInstance, false);
			} else {

				this.edm.driverDidNotConnectToDevice(thisInstance, null, false);
			}

		} catch (StarIOPortException e) {
		} finally {
			// if (port != null) {
			// try {
			// StarIOPort.releasePort(port);
			// } catch (StarIOPortException e) {
			// }
			// }
			// releasePrinter();
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

				if (!isPOSPrinter) {
					portSettings = "mini";
					port = getStarIOPort();
					enableCenter = new byte[] { 0x1b, 0x61, 0x01 };
					disableCenter = new byte[] { 0x1b, 0x61, 0x00 };
				} else {
					if (portName.contains("TCP") && !portNumber.equals("9100"))
						portSettings = portNumber;
					else
						portSettings = "";

					port = getStarIOPort();
					enableCenter = new byte[] { 0x1b, 0x1d, 0x61, 0x01 };
					disableCenter = new byte[] { 0x1b, 0x1d, 0x61, 0x00 };
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}

				StarPrinterStatus status = port.retreiveStatus();

				if (status.offline == false) {
					didConnect = true;

				} else {
					msg = "Printer is offline";
					if (status.receiptPaperEmpty == true) {
						msg += "\nPaper is Empty";
					}
					if (status.coverOpen == true) {
						msg += "\nCover is Open";
					}
				}

			} catch (StarIOPortException e) {
				msg = "Failed: \n" + e.getMessage();
			} finally {
				// if (port != null) {
				// try {
				// StarIOPort.releasePort(port);
				// } catch (StarIOPortException e) {
				// }
				// }
				// releasePrinter();
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

	private void verifyConnectivity() throws StarIOPortException, InterruptedException {
		try {
			if (port == null || port.retreiveStatus() == null && port.retreiveStatus().offline)
				port = getStarIOPort();
		} catch (StarIOPortException e) {
			releasePrinter();
			Thread.sleep(500);
			port = null;// StarIOPort.getPort(portName, portSettings, 30000,
						// this.activity);
			verifyConnectivity();
		}
	}

	@Override
	public boolean printTransaction(String ordID, int type, boolean isFromHistory, boolean fromOnHold) {
		// TODO Auto-generated method stub
		try {

			// port = StarIOPort.getPort(portName, portSettings, 1000,
			// this.activity);

			// verifyConnectivity();
			port = getStarIOPort();
			Thread.sleep(500);

			if (!isPOSPrinter) {
				port.writePort(new byte[] { 0x1d, 0x57, (byte) 0x80, 0x31 }, 0, 4);
				port.writePort(new byte[] { 0x1d, 0x21, 0x00 }, 0, 3);
				port.writePort(new byte[] { 0x1b, 0x74, 0x11 }, 0, 3); // set to
																		// windows-1252
			} else {
				port.writePort(new byte[] { 0x1b, 0x1d, 0x74, 0x20 }, 0, 4);
				port.writePort(disableCenter, 0, disableCenter.length); // disable
																		// center
			}
			printReceipt(ordID, LINE_WIDTH, fromOnHold, type, isFromHistory);

		} catch (StarIOPortException e) {
			return false;

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// if (port != null) {
			// try {
			// StarIOPort.releasePort(port);
			// } catch (StarIOPortException e) {
			// }
			// }
		}
		return true;
	}

	@Override
	public boolean printPaymentDetails(String payID, int type, boolean isReprint) {
		// TODO Auto-generated method stub

		try {

			verifyConnectivity();

			Thread.sleep(1000);

			if (!isPOSPrinter) {
				port.writePort(new byte[] { 0x1d, 0x57, (byte) 0x80, 0x31 }, 0, 4);
				port.writePort(new byte[] { 0x1d, 0x21, 0x00 }, 0, 3);
				port.writePort(new byte[] { 0x1b, 0x74, 0x11 }, 0, 3); // set to
																		// windows-1252
			} else {
				// port.writePort(new byte[]{0x1b, 0x40}, 0,2);
			}

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

			this.printImage(0);

			if (printPref.contains(MyPreferences.print_header))
				this.printHeader();

			port.writePort(enableCenter, 0, enableCenter.length); // enable
																	// center

			sb.append("* ").append(payArray[0]);
			if (payArray[11].equals("1"))
				sb.append(" Refund *\n\n\n");
			else
				sb.append(" Sale *\n\n\n");

			port.writePort(sb.toString().getBytes(FORMAT), 0, sb.length());
			port.writePort(disableCenter, 0, disableCenter.length); // disable
																	// center
			sb.setLength(0);
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
					getString(R.string.receipt_time), LINE_WIDTH, 0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(payArray[1], payArray[2], LINE_WIDTH, 0))
					.append("\n\n");

			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer), payArray[3],
					LINE_WIDTH, 0));

			if (payArray[17] != null && !payArray[17].isEmpty())
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_order_id),
						payArray[17], LINE_WIDTH, 0));
			else if (payArray[16] != null && !payArray[16].isEmpty()) // invoice
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_invoice_ref),
						payArray[16], LINE_WIDTH, 0));

			if (!isStoredFwd)
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_idnum), payID,
						LINE_WIDTH, 0));

			if (!isCashPayment && !isCheckPayment) {
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cardnum),
						"*" + payArray[9], LINE_WIDTH, 0));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("TransID:", payArray[8], LINE_WIDTH, 0));
			} else if (isCheckPayment) {
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_checknum),
						payArray[10], LINE_WIDTH, 0));
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

			port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());

			sb.setLength(0);
			port.writePort(textHandler.newLines(1).getBytes(FORMAT), 0, textHandler.newLines(1).length());

			if (!isCashPayment && !isCheckPayment) {
				if (myPref.getPreferences(MyPreferences.pref_handwritten_signature)) {
					sb.append(textHandler.newLines(1));
				} else if (!payArray[7].isEmpty()) {
					encodedSignature = payArray[7];
					this.printImage(1);
				}
				port.writePort(enableCenter, 0, enableCenter.length); // center
				sb.append("x").append(textHandler.lines(LINE_WIDTH / 2)).append("\n");
				sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(1));
				port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
				sb.setLength(0);
			}

			if (Global.isIvuLoto) {
				sb = new StringBuilder();
				port.writePort(enableCenter, 0, enableCenter.length); // enable
																		// center

				if (!printPref.contains(MyPreferences.print_ivuloto_qr)) {
					sb.append("\n");
					sb.append(textHandler.centeredString(textHandler.ivuLines(2 * LINE_WIDTH / 3), LINE_WIDTH));
					sb.append(textHandler.centeredString("IVULOTO: " + payArray[13], LINE_WIDTH));
					sb.append(textHandler.centeredString(payArray[12], LINE_WIDTH));
					sb.append(textHandler.centeredString(textHandler.ivuLines(2 * LINE_WIDTH / 3), LINE_WIDTH));
					sb.append("\n");

					port.writePort(sb.toString().getBytes(), 0, sb.toString().length());
				} else {
					encodedQRCode = payArray[14];

					this.printImage(2);

					sb.append(textHandler.ivuLines(2 * LINE_WIDTH / 3)).append("\n");
					sb.append("\t").append("IVULOTO: ").append(payArray[13]).append("\n");
					sb.append(payArray[12]).append("\n");
					sb.append(textHandler.ivuLines(2 * LINE_WIDTH / 3)).append("\n");

					port.writePort(sb.toString().getBytes(), 0, sb.toString().length());
				}
				sb.setLength(0);
			}

			this.printFooter();
			port.writePort(enableCenter, 0, enableCenter.length); // center
			String temp = new String();
			if (!isCashPayment && !isCheckPayment) {

				port.writePort(creditCardFooting.getBytes(FORMAT), 0, creditCardFooting.length());
				temp = textHandler.newLines(1);
				port.writePort(temp.getBytes(FORMAT), 0, temp.length());
			}

			sb.setLength(0);
			if (isReprint) {
				sb.append(textHandler.centeredString("*** Copy ***", LINE_WIDTH));
				port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
			}

			if (isPOSPrinter) {
				port.writePort(new byte[] { 0x1b, 0x64, 0x02 }, 0, 3); // Cut
			}

		} catch (StarIOPortException e) {
			return false;
		} catch (UnsupportedEncodingException e) {
			return false;
			// TODO Auto-generated catch block
		} catch (InterruptedException e) {
			return false;
			// TODO Auto-generated catch block
		} catch (JAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// if (port != null) {
			// try {
			// StarIOPort.releasePort(port);
			// } catch (StarIOPortException e) {
			// }
			// }
		}
		return true;
	}

	// private void printImage(int type) throws StarIOPortException {
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
	// case 2: {
	// if (!encodedQRCode.isEmpty()) {
	// byte[] img = Base64.decode(encodedQRCode, Base64.DEFAULT);
	// myBitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
	// }
	// break;
	// }
	// }
	//
	// if (myBitmap != null) {
	//
	// byte[] data;
	// data = PrinterFunctions.createCommandsEnglishRasterModeCoupon(384,
	// SCBBitmapConverter.Rotation.Normal,
	// myBitmap);
	// Communication.Result result;
	// result = Communication.sendCommands(data, port, this.activity); //
	// 10000mS!!!
	// try {
	// Thread.sleep(2000);
	// } catch (InterruptedException e) {
	// }
	// }
	// }

	public void PrintBitmapImage(Bitmap tempBitmap, boolean compressionEnable) throws StarIOPortException {
		ArrayList<Byte> commands = new ArrayList<Byte>();
		Byte[] tempList;

		RasterDocument rasterDoc = new RasterDocument(RasSpeed.Medium, RasPageEndMode.None, RasPageEndMode.None,
				RasTopMargin.Standard, 0, LINE_WIDTH / 3, 0);
		// Bitmap bm = BitmapFactory.decodeResource(res, source);
		StarBitmap starbitmap = new StarBitmap(tempBitmap, false, 350, PAPER_WIDTH);

		byte[] command = rasterDoc.BeginDocumentCommandData();
		tempList = new Byte[command.length];
		CopyArray(command, tempList);
		commands.addAll(Arrays.asList(tempList));

		command = starbitmap.getImageRasterDataForPrinting();
		tempList = new Byte[command.length];
		CopyArray(command, tempList);
		commands.addAll(Arrays.asList(tempList));

		command = rasterDoc.EndDocumentCommandData();
		tempList = new Byte[command.length];
		CopyArray(command, tempList);
		commands.addAll(Arrays.asList(tempList));

		byte[] commandToSendToPrinter = convertFromListByteArrayTobyteArray(commands);
		port.writePort(commandToSendToPrinter, 0, commandToSendToPrinter.length);
	}

	private void CopyArray(byte[] srcArray, Byte[] cpyArray) {
		for (int index = 0; index < cpyArray.length; index++) {
			cpyArray[index] = srcArray[index];
		}
	}

	private static byte[] convertFromListByteArrayTobyteArray(List<Byte> ByteArray) {
		byte[] byteArray = new byte[ByteArray.size()];
		for (int index = 0; index < byteArray.length; index++) {
			byteArray[index] = ByteArray.get(index);
		}

		return byteArray;
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
			// port = StarIOPort.getPort(portName, portSettings, 10000,
			// this.activity);
			verifyConnectivity();

			Thread.sleep(1000);
			if (!isPOSPrinter) {
				port.writePort(new byte[] { 0x1d, 0x57, (byte) 0x80, 0x31 }, 0, 4);
				port.writePort(new byte[] { 0x1d, 0x21, 0x00 }, 0, 3);
				port.writePort(new byte[] { 0x1b, 0x74, 0x11 }, 0, 3); // set to
																		// windows-1252
			} else {
				// port.writePort(new byte[]{0x1b,0x1d,0x74,0x20}, 0,2);
			}

			PaymentsHandler paymentHandler = new PaymentsHandler(activity);
			PayMethodsHandler payMethodHandler = new PayMethodsHandler(activity);
			EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
			StringBuilder sb = new StringBuilder();
			StringBuilder sb_refunds = new StringBuilder();
			port.writePort(textHandler.newLines(1).getBytes(FORMAT), 0, textHandler.newLines(1).length());
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
			port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
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

			port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
			port.writePort(sb_refunds.toString().getBytes(FORMAT), 0, sb_refunds.toString().length());
			port.writePort(textHandler.newLines(1).getBytes(FORMAT), 0, textHandler.newLines(1).length());
			port.writePort("".getBytes(FORMAT), 0, "".length());

			if (isPOSPrinter) {
				port.writePort(new byte[] { 0x1b, 0x64, 0x02 }, 0, 3); // Cut
			}

		} catch (StarIOPortException e) {
			return false;
		} catch (UnsupportedEncodingException e) {
			return false;
			// TODO Auto-generated catch block
		} catch (InterruptedException e) {
			return false;
			// TODO Auto-generated catch block
		} finally {
			// if (port != null) {
			// try {
			// StarIOPort.releasePort(port);
			// } catch (StarIOPortException e) {
			// }
			// }
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

	// public void printHeader() {
	//
	// EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
	// StringBuilder sb = new StringBuilder();
	//
	// MemoTextHandler handler = new MemoTextHandler(activity);
	// String[] header = handler.getHeader();
	//
	// if (header[0] != null && !header[0].isEmpty())
	// sb.append(textHandler.formatLongString(header[0],
	// LINE_WIDTH)).append("\n");
	// if (header[1] != null && !header[1].isEmpty())
	// sb.append(textHandler.formatLongString(header[1],
	// LINE_WIDTH)).append("\n");
	// if (header[2] != null && !header[2].isEmpty())
	// sb.append(textHandler.formatLongString(header[2],
	// LINE_WIDTH)).append("\n");
	//
	// if (!sb.toString().isEmpty()) {
	// sb.append(textHandler.newLines(2));
	// outputByteBuffer = sb.toString().getBytes();
	// try {
	// port.writePort(enableCenter, 0, enableCenter.length);
	// port.writePort(outputByteBuffer, 0, outputByteBuffer.length);
	// port.writePort(disableCenter, 0, disableCenter.length);
	// } catch (StarIOPortException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// }
	// }
	//
	// public void printYouSave(String saveAmount) {
	// EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
	// StringBuilder sb = new StringBuilder(saveAmount);
	//
	// try {
	// port.writePort(textHandler.ivuLines(LINE_WIDTH).getBytes(FORMAT), 0,
	// textHandler.ivuLines(LINE_WIDTH).length());
	// sb.setLength(0);
	// sb.append(textHandler.newLines(2));
	// // port.writePort(sb.toString().getBytes(), 0,
	// // sb.toString().length());
	// // sb.setLength(0);
	// sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer_id),
	// saveAmount,
	// LINE_WIDTH, 0));
	// // port.writePort(sb.toString().getBytes(), 0,
	// // sb.toString().length());
	// sb.append(textHandler.newLines(2));
	// port.writePort(sb.toString().getBytes(), 0, sb.toString().length());
	// port.writePort(textHandler.ivuLines(LINE_WIDTH).getBytes(FORMAT), 0,
	// textHandler.ivuLines(LINE_WIDTH).length());
	//
	// } catch (UnsupportedEncodingException | StarIOPortException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// }
	//
	// public void printFooter() {
	//
	// EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
	// StringBuilder sb = new StringBuilder();
	// MemoTextHandler handler = new MemoTextHandler(activity);
	// String[] footer = handler.getFooter();
	//
	// if (footer[0] != null && !footer[0].isEmpty())
	// sb.append(textHandler.formatLongString(footer[0],
	// LINE_WIDTH)).append("\n");
	// if (footer[1] != null && !footer[1].isEmpty())
	// sb.append(textHandler.formatLongString(footer[1],
	// LINE_WIDTH)).append("\n");
	// if (footer[2] != null && !footer[2].isEmpty())
	// sb.append(textHandler.formatLongString(footer[2],
	// LINE_WIDTH)).append("\n");
	//
	// if (!sb.toString().isEmpty()) {
	// sb.append(textHandler.newLines(2));
	// outputByteBuffer = sb.toString().getBytes();
	// try {
	// port.writePort(enableCenter, 0, enableCenter.length);
	// port.writePort(outputByteBuffer, 0, outputByteBuffer.length);
	// port.writePort(disableCenter, 0, disableCenter.length);
	// } catch (StarIOPortException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// }
	//
	// }

	@Override
	public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSig) {
		// TODO Auto-generated method stub
		try {
			// port = StarIOPort.getPort(portName, portSettings, 10000,
			// this.activity);
			verifyConnectivity();

			Thread.sleep(1000);

			if (!isPOSPrinter) {
				port.writePort(new byte[] { 0x1d, 0x57, (byte) 0x80, 0x31 }, 0, 4);
				port.writePort(new byte[] { 0x1d, 0x21, 0x00 }, 0, 3);
				port.writePort(new byte[] { 0x1b, 0x74, 0x11 }, 0, 3); // set to
																		// windows-1252
			} else {
				// port.writePort(new byte[]{0x1b, 0x40}, 0,2);
			}

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
			sb.append(textHandler.newLines(1));

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
							.append(textHandler.newLines(1));

					totalSold += Double.parseDouble(myConsignment.get(i).ConsInvoice_Qty);
					totalReturned += Double.parseDouble(myConsignment.get(i).ConsReturn_Qty);
					totalDispached += Double.parseDouble(myConsignment.get(i).ConsDispatch_Qty);
					totalLines += 1;
					ordTotal += Double.parseDouble(myConsignment.get(i).invoice_total);

					port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
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
			sb.append(textHandler.newLines(1));

			port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());

			if (printPref.contains(MyPreferences.print_descriptions))
				this.printFooter();

			this.printImage(1);

			port.writePort(textHandler.newLines(1).getBytes(FORMAT), 0, textHandler.newLines(1).length());

			if (isPOSPrinter) {
				port.writePort(new byte[] { 0x1b, 0x64, 0x02 }, 0, 3); // Cut
			}

			// db.close();

		} catch (StarIOPortException e) {
			return false;
		} catch (UnsupportedEncodingException e) {
			return false;
			// TODO Auto-generated catch block
		} catch (InterruptedException e) {
			return false;
			// TODO Auto-generated catch block
		} catch (JAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// if (port != null) {
			// try {
			// StarIOPort.releasePort(port);
			// } catch (StarIOPortException e) {
			// }
			// }
		}
		return true;
	}

	@Override
	public void releaseCardReader() {
		// TODO Auto-generated method stub
		if (!isPOSPrinter) {
			callBack = null;
			try {
				if (portForCardReader != null) {
					portForCardReader.writePort(new byte[] { 0x04 }, 0, 1);
					stopLoop = true;
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
				}
			} catch (StarIOPortException e) {

			} finally {
				if (portForCardReader != null) {
					try {
						StarIOPort.releasePort(portForCardReader);
						portForCardReader = null;
					} catch (StarIOPortException e1) {
					}
				}
			}
		}
	}

	@Override
	public void loadCardReader(EMSCallBack _callBack, boolean isDebitCard) {
		// TODO Auto-generated method stub

		callBack = _callBack;
		if (handler == null)
			handler = new Handler();
		if (!isPOSPrinter) {
			StartCardReaderThread temp = new StartCardReaderThread();
			temp.start();
		}
	}

	class StartCardReaderThread extends Thread {
		public void run() {

			try {
				if (portForCardReader == null) {
					stopLoop = false;
					portForCardReader = getStarIOPort();

					receiveThread = new ReceiveThread();
					receiveThread.start();
					portForCardReader.writePort(new byte[] { 0x1b, 0x4d, 0x45 }, 0, 3);
					handler.post(doUpdateDidConnect);
				}

			} catch (StarIOPortException e) {
			} finally {

			}
		}
	}

	class ReceiveThread extends Thread {
		public void run() {

			byte[] mcrData1 = new byte[1];
			int track = 1;
			try {

				StringBuilder tr1 = new StringBuilder();
				StringBuilder tr2 = new StringBuilder();
				List<String> listTrack = new ArrayList<String>();
				String t = "";
				boolean doneParsing = false;
				int countNameLimiter = 0;

				while (!stopLoop) {

					if (portForCardReader.readPort(mcrData1, 0, 1) > 0) {

						if (!doneParsing) {
							t = new String(mcrData1, "windows-1252");
							if (t.equals("\r") || t.equals("\n")) {
								for (String data : listTrack) {
									if (data.contains("B")) {
										if (!data.startsWith("%"))
											tr1.append("%");

										tr1.append(data);

										if (!data.endsWith("?"))
											tr1.append("?");
										// tr1.append("%").append(data).append("?");
									} else if (data.contains("=")) {
										if (!data.startsWith(";"))
											tr1.append(";");

										tr1.append(data);

										if (!data.endsWith("?"))
											tr1.append("?");
										// tr1.append(";").append(data).append("?");
									}

								}
								// tr2.append("%").append(tr1.toString()).append("?");
								cardManager = new CreditCardInfo();
								CardParser.parseCreditCard(activity, tr1.toString(), cardManager);
								doneParsing = true;
								handler.post(doUpdateViews);
								tr1.setLength(0);

							} else if (mcrData1 != null && mcrData1[0] == 28 && tr2.length() > 0) {

								listTrack.add(tr2.toString());
								tr2.setLength(0);
							} else {
								tr2.append(t.trim());
							}

						}
						// if((!t.isEmpty()||(t.isEmpty()&&countNameLimiter==1))&&track==1)
						// {
						// doneParsing = false;
						// if(t.equals("B"))
						// {
						// tr1.setLength(0);
						// tr1.append("%");
						// }
						//
						// if(t.equals("^"))
						// countNameLimiter++;
						//
						// tr1.append(t);
						// }
						// else
						// if(t.isEmpty()&&tr1.toString().length()>10&&track==1)
						// {
						// track=2;
						//
						// }
						// else if(!t.isEmpty()&&track==2)
						// {
						// if(tr2.toString().isEmpty())
						// tr2.append(";");
						//
						// tr2.append(t);
						// }
						// else
						// if(t.isEmpty()&&tr2.toString().length()>0&&!doneParsing)
						// {
						// //portForCardReader.writePort(new byte[] { 0x1b,
						// 0x4d, 0x45 }, 0, 3);
						// track=1;
						// doneParsing = true;
						// countNameLimiter = 0;
						// tr1.append("?");
						// tr2.append("?");
						//
						//
						//
						// //if(tr1.toString().matches("^%B[^\\^\\W]{0,19}\\^[^\\^]{2,26}\\^\\d{4}\\w{3}[^?]+\\?\\w?$")&&
						// tr2.toString().matches(";\\d{0,19}=\\d{7}\\w*\\?"))
						// if(tr1.toString().matches("^%B[^\\^\\W]{0,19}\\^[^\\^]{0,26}\\^\\d{4}\\w{3}[^?]+\\?\\w?$")
						// &&
						// tr2.toString().matches(";\\d{0,19}=\\d{7}\\w*\\?"))
						// {
						// String[] cardValues = new String[2];
						//
						// cardValues[0] = tr1.toString();
						// cardValues[1] = tr2.toString();
						//
						//
						// cardValues[0].trim();
						// String[] firstTrack = cardValues[0].split("\\^");
						// if (firstTrack.length > 1)
						// firstTrack = firstTrack[1].split("/");
						// String[] secondTrack = cardValues[1].split("=");
						// String[] cardNumber = secondTrack[0].split(";");
						//
						// String expYear = "";
						// String expDate = "";
						//
						// if (cardNumber.length > 1) {
						// expYear = secondTrack[1].substring(0, 2);
						// expDate = secondTrack[1].substring(2, 4);
						// }
						// StringBuilder sb = new StringBuilder();
						// for (int i = 0; i < firstTrack.length; i++)
						// sb.append(firstTrack[i].trim()).append(" ");
						//
						//
						// cardManager.setEncryptedAESTrack1(encrypt.encryptWithAES(cardValues[0]));
						// cardManager.setEncryptedAESTrack2(encrypt.encryptWithAES(cardValues[1]));
						// cardManager.setCardOwnerName(sb.toString());
						//
						// if(cardNumber.length>1)
						// {
						// int temp = cardNumber[1].length();
						// String last4Digits = (String)
						// cardNumber[1].subSequence(temp-4, temp);
						// cardManager.setCardLast4(last4Digits);
						// if(Global.isEncryptSwipe)
						// cardManager.setCardNumAESEncrypted(encrypt.encryptWithAES(cardNumber[1]));
						// else
						// cardManager.setCardNumAESEncrypted(cardNumber[1]);
						// }
						// cardManager.setCardExpMonth(expDate);
						// cardManager.setCardExpYear(expYear);
						//
						//
						// handler.post(doUpdateViews);
						// }
						// tr1.setLength(0);
						// tr2.setLength(0);
						//
						// }
					}
				}
			} catch (StarIOPortException e) {
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
			}
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
			verifyConnectivity();

			Thread.sleep(1000);
			if (!isPOSPrinter) {
				port.writePort(new byte[] { 0x1d, 0x57, (byte) 0x80, 0x31 }, 0, 4);
				port.writePort(new byte[] { 0x1d, 0x21, 0x00 }, 0, 3);
				port.writePort(new byte[] { 0x1b, 0x74, 0x11 }, 0, 3); // set to
																		// windows-1252
			} else {
				// port.writePort(new byte[]{0x1b, 0x40}, 0,2);
			}

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
			sb.append(textHandler.newLines(1));

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

				port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
				sb.setLength(0);
			}

			if (printPref.contains(MyPreferences.print_footer))
				this.printFooter();

			if (!encodedSig.isEmpty()) {
				this.encodedSignature = encodedSig;
				this.printImage(1);
				port.writePort(enableCenter, 0, enableCenter.length); // center
				sb.setLength(0);
				sb.append("x").append(textHandler.lines(LINE_WIDTH / 2)).append("\n");
				sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(1));
				port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
				port.writePort(disableCenter, 0, disableCenter.length); // disable
																		// center
			}

			if (isPOSPrinter) {
				port.writePort(new byte[] { 0x1b, 0x64, 0x02 }, 0, 3); // Cut
			}

			// db.close();

		} catch (StarIOPortException e) {
			return false;
		} catch (UnsupportedEncodingException e) {
			return false;
			// TODO Auto-generated catch block
		} catch (InterruptedException e) {
			return false;
			// TODO Auto-generated catch block
		} catch (JAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// if (port != null) {
			// try {
			// StarIOPort.releasePort(port);
			// } catch (StarIOPortException e) {
			// }
			// }
		}
		return true;
	}

	@Override
	public boolean printOpenInvoices(String invID) {
		// TODO Auto-generated method stub
		try {
			verifyConnectivity();

			Thread.sleep(1000);

			if (!isPOSPrinter) {
				port.writePort(new byte[] { 0x1d, 0x57, (byte) 0x80, 0x31 }, 0, 4);
				port.writePort(new byte[] { 0x1d, 0x21, 0x00 }, 0, 3);
				port.writePort(new byte[] { 0x1b, 0x74, 0x11 }, 0, 3); // set to
																		// windows-1252
			} else {
				// port.writePort(new byte[]{0x1b, 0x40}, 0,2);
			}

			EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
			StringBuilder sb = new StringBuilder();
			String[] rightInfo = new String[] {};
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
			port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());

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
					sb.append(textHandler.newLines(1));

				port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
				sb.setLength(0);
			}

			sb.append(textHandler.lines(LINE_WIDTH)).append("\n");
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_invoice_total),
					Global.getCurrencyFormat(rightInfo[11]), LINE_WIDTH, 0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amount_collected),
					Global.getCurrencyFormat(rightInfo[13]), LINE_WIDTH, 0));
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_balance_due),
					Global.getCurrencyFormat(rightInfo[12]), LINE_WIDTH, 0)).append("\n\n\n");

			sb.append(textHandler.centeredString(getString(R.string.receipt_thankyou), LINE_WIDTH));
			port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
			port.writePort(textHandler.newLines(1).getBytes(FORMAT), 0,
					textHandler.newLines(1).getBytes(FORMAT).length);

			if (isPOSPrinter) {
				port.writePort(new byte[] { 0x1b, 0x64, 0x02 }, 0, 3); // Cut
			}

		} catch (StarIOPortException e) {
			return false;
		} catch (UnsupportedEncodingException e) {
			return false;
			// TODO Auto-generated catch block
		} catch (InterruptedException e) {
			return false;
			// TODO Auto-generated catch block
		} catch (JAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// if (port != null) {
			// try {
			// StarIOPort.releasePort(port);
			// } catch (StarIOPortException e) {
			// }
			// }
		}
		return true;
	}

	@Override
	public void printStationPrinter(List<Orders> orders, String ordID) {
		// TODO Auto-generated method stub
		try {
			// if (port != null) {
			// StarIOPort.releasePort(port);
			// try {
			// Thread.sleep(1000);
			// } catch (InterruptedException e) {
			// }
			// }
			//
			//
			port = getStarIOPort();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			if (!isPOSPrinter) {
				port.writePort(new byte[] { 0x1d, 0x57, (byte) 0x80, 0x31 }, 0, 4);
				port.writePort(new byte[] { 0x1d, 0x21, 0x00 }, 0, 3);
				port.writePort(new byte[] { 0x1b, 0x74, 0x11 }, 0, 3); // set to
																		// windows-1252
			} else {
				port.writePort(new byte[] { 0x1b, 0x1d, 0x74, 0x20 }, 0, 4);
				byte[] characterExpansion = new byte[] { 0x1b, 0x69, 0x00, 0x00 };
				characterExpansion[2] = (byte) (1 + '0');
				characterExpansion[3] = (byte) (1 + '0');

				port.writePort(characterExpansion, 0, characterExpansion.length);
				port.writePort(disableCenter, 0, disableCenter.length); // disable
																		// center
			}

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
			Date startedDate = sdf1.parse(anOrder.ord_timecreated);
			Date sentDate = new Date();

			sb.append(getString(R.string.receipt_sent_by)).append(" ").append(myPref.getEmpName()).append(" (");

			if (((float) (sentDate.getTime() - startedDate.getTime()) / 1000) > 60)
				sb.append(Global.formatToDisplayDate(sdf1.format(sentDate.getTime()), activity, 4)).append(")");
			else
				sb.append(Global.formatToDisplayDate(anOrder.ord_timecreated, activity, 4)).append(")");

			String ordComment = anOrder.ord_comment;
			if (ordComment != null && !ordComment.isEmpty()) {
				sb.append("\nComments:\n");
				sb.append(textHandler.oneColumnLineWithLeftAlignedText(ordComment, LINE_WIDTH, 3)).append("\n");
			}

			sb.append("\n");

			port.writePort(sb.toString().getBytes(), 0, sb.toString().length());

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

					if (!orders.get(m).getOrderProdComment().isEmpty())
						sb.append("  ").append(orders.get(m).getOrderProdComment()).append("\n");
					port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
					sb.setLength(0);
				} else {
					ordProdHandler.updateIsPrinted(orders.get(i).getOrdprodID());
					sb.append(orders.get(i).getQty()).append("x ").append(orders.get(i).getName()).append("\n");

					if (!orders.get(m).getOrderProdComment().isEmpty())
						sb.append("  ").append(orders.get(m).getOrderProdComment()).append("\n");
					port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
					sb.setLength(0);
				}
			}
			sb.append(textHandler.newLines(1));
			port.writePort(sb.toString().getBytes(), 0, sb.toString().length());
			if (isPOSPrinter) {
				byte[] characterExpansion = new byte[] { 0x1b, 0x69, 0x00, 0x00 };
				characterExpansion[2] = (byte) (0 + '0');
				characterExpansion[3] = (byte) (0 + '0');

				port.writePort(characterExpansion, 0, characterExpansion.length);
				port.writePort(new byte[] { 0x1b, 0x64, 0x02 }, 0, 3); // Cut
			}

			// db.close();
		} catch (StarIOPortException e) {
			/*
			 * Builder dialog = new AlertDialog.Builder(this.activity);
			 * dialog.setNegativeButton("Ok", null); AlertDialog alert =
			 * dialog.create(); alert.setTitle("Failure"); alert.setMessage(
			 * "Failed to connect to printer"); alert.show();
			 */
			// Toast.makeText(activity, e.getMessage(),
			// Toast.LENGTH_LONG).show();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// if (port != null) {
			// try {
			// StarIOPort.releasePort(port);
			// // Thread.sleep(1000);
			// } catch (StarIOPortException e) {
			// }
			// }
		}
	}

	@Override
	public void openCashDrawer() {

		new Thread(new Runnable() {
			public void run() {

				try {

					if (isPOSPrinter) {
						port.writePort(new byte[] { 0x07 }, 0, 1); // Kick cash
						releasePrinter(); // drawer
					}

				} catch (StarIOPortException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();

				} finally {

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
			verifyConnectivity();

			Thread.sleep(1000);
			if (!isPOSPrinter) {
				port.writePort(new byte[] { 0x1d, 0x57, (byte) 0x80, 0x31 }, 0, 4);
				port.writePort(new byte[] { 0x1d, 0x21, 0x00 }, 0, 3);
				port.writePort(new byte[] { 0x1b, 0x74, 0x11 }, 0, 3); // set to
																		// windows-1252
			} else {
				// port.writePort(new byte[]{0x1b, 0x40}, 0,2);
			}

			this.encodedSignature = map.get("encoded_signature");
			printPref = myPref.getPrintingPreferences();
			EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
			StringBuilder sb = new StringBuilder();
			String prodDesc = "";

			int size = c.getCount();
			this.setPaperWidth(LINE_WIDTH);
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
			sb.append(textHandler.newLines(1));

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
							5)).append(textHandler.newLines(1));
				} else {
					sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original Qty:",
							c.getString(c.getColumnIndex("ConsOriginal_Qty")), LINE_WIDTH, 3));
					sb.append(textHandler.twoColumnLineWithLeftAlignedText("Picked up Qty:",
							c.getString(c.getColumnIndex("ConsPickup_Qty")), LINE_WIDTH, 3));
					sb.append(textHandler.twoColumnLineWithLeftAlignedText("New Qty:",
							c.getString(c.getColumnIndex("ConsNew_Qty")), LINE_WIDTH, 3)).append("\n\n\n");

				}
				port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
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

			port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());

			if (printPref.contains(MyPreferences.print_footer))
				this.printFooter();

			this.printImage(1);

			port.writePort(textHandler.newLines(1).getBytes(FORMAT), 0, textHandler.newLines(1).length());

			if (isPOSPrinter) {
				port.writePort(new byte[] { 0x1b, 0x64, 0x02 }, 0, 3); // Cut
			}

		} catch (StarIOPortException e) {
			return false;
		} catch (UnsupportedEncodingException e) {
			return false;
			// TODO Auto-generated catch block
		} catch (InterruptedException e) {
			return false;
			// TODO Auto-generated catch block
		} catch (JAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// if (port != null) {
			// try {
			// StarIOPort.releasePort(port);
			// } catch (StarIOPortException e) {
			// }
			// }
		}
		return true;
	}

	@Override
	public void loadScanner(EMSCallBack _callBack) {
		scannerCallBack = _callBack;
		if (handler == null)
			handler = new Handler();
		if (_callBack != null) {
			mStarIoExtManager = new StarIoExtManager(StarIoExtManager.Type.OnlyBarcodeReader, portName, "", 10000,
					this.activity); // 10000mS!!!
			mStarIoExtManager.setListener(mStarIoExtManagerListener);
			// mStarIoExtManager.disconnect();
			// mStarIoExtManager.connect();
			starIoExtManagerConnect();
		} else {
			if (mStarIoExtManager != null) {
				mStarIoExtManager.disconnect();
				mStarIoExtManager = null;
			}
		}
	}

	@Override
	public boolean isUSBConnected() {
		return false;
	}

	private void starIoExtManagerConnect() {
		final Dialog mProgressDialog = new ProgressDialog(EMSBluetoothStarPrinter.this.activity);
		AsyncTask<Void, Void, Boolean> asyncTask = new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected void onPreExecute() {
				if (!EMSBluetoothStarPrinter.this.activity.isFinishing())
					mProgressDialog.show();
			}

			@Override
			protected Boolean doInBackground(Void... params) {
				mStarIoExtManager.disconnect();
				return mStarIoExtManager.connect();
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (!EMSBluetoothStarPrinter.this.activity.isFinishing()) {
					mProgressDialog.dismiss();
					if (!result) {
						AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
								EMSBluetoothStarPrinter.this.activity);

						dialogBuilder.setTitle("Communication Result");
						dialogBuilder.setMessage("failure.\nPrinter is offline.");
						dialogBuilder.setPositiveButton("OK", null);
						dialogBuilder.show();
					}
				}
			}
		};

		asyncTask.execute();
	}

	StarIoExtManagerListener mStarIoExtManagerListener = new StarIoExtManagerListener() {
		@Override
		public void didBarcodeDataReceive(byte[] data) {
			String[] barcodeDataArray = new String(data).split("\r\n");
			for (String barcodeData : barcodeDataArray) {
				scannedData = barcodeData;
				handler.post(runnableScannedData);
			}
			// scannedData = new String(data);
			// handler.post(runnableScannedData);
		}
	};

	String scannedData = "";

	private Runnable runnableScannedData = new Runnable() {
		public void run() {
			try {
				if (scannerCallBack != null)
					scannerCallBack.scannerWasRead(scannedData);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};

	@Override
	public void printFooter() {
		super.printFooter(LINE_WIDTH);
	}

	@Override
	public void printHeader() {
		super.printHeader(LINE_WIDTH);

	}

	private StarIOPort getStarIOPort() throws StarIOPortException {
		releasePrinter();
		port = null;
		if (port == null || port.retreiveStatus() == null || port.retreiveStatus().offline) {
			if (portName.toUpperCase().contains("TCP")) {
				String ip = portName.replace("TCP:", "");
				int port=80;
				try {
					port = TextUtils.isEmpty(portSettings) ? 80 : Integer.parseInt(portSettings);
				} catch (NumberFormatException e) {					
					e.printStackTrace();
					
				}
				if (!Global.isIpAvailable(ip, port)) {
					throw new StarIOPortException("Host not reachable.");
				}
			}
			port = StarIOPort.getPort(portName, portSettings, 10000, activity);
		}
		return port;
	}
}
