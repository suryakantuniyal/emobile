package drivers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.StarMicronics.jasura.JAException;
import com.android.database.ClerksHandler;
import com.android.database.MemoTextHandler;
import com.android.database.OrderProductsHandler;
import com.android.database.OrderTaxes_DB;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.database.StoredPayments_DB;
import com.android.emobilepos.R;
import com.android.emobilepos.models.DataTaxes;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.Orders;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.mpowa.android.sdk.powapos.PowaPOS;
import com.partner.pt100.printer.PrinterApiContext;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

import POSSDK.POSSDK;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Base64;
import android.util.Log;
import datamaxoneil.connection.Connection_Bluetooth;
import datamaxoneil.printer.DocumentLP;
import drivers.star.utils.Communication;
import drivers.star.utils.MiniPrinterFunctions;
import drivers.star.utils.PrinterFunctions;
import main.EMSDeviceManager;
import plaintext.EMSPlainTextHelper;
import util.RasterDocument;
import util.RasterDocument.RasPageEndMode;
import util.RasterDocument.RasSpeed;
import util.RasterDocument.RasTopMargin;
import com.starmicronics.starioextension.commandbuilder.Bitmap.SCBBitmapConverter;


public class EMSDeviceDriver {
	public static final boolean PRINT_TO_LOG = false;
	protected EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
	protected double itemDiscTotal = 0;
	protected double saveAmount;
	protected List<String> printPref;
	protected MyPreferences myPref;
	protected Activity activity;
	protected StarIOPort port;
	protected final String FORMAT = "windows-1252";
	protected String encodedSignature;
	protected byte[] enableCenter, disableCenter;
	protected boolean isPOSPrinter = false;
	protected String encodedQRCode = "";
	protected static PrinterApiContext printerApi;
	protected Connection_Bluetooth device;
	protected PowaPOS powaPOS;
	protected POSSDK pos_sdk = null;
	protected final int ALIGN_LEFT = 0, ALIGN_CENTER = 1;

	protected InputStream inputStream;
	protected OutputStream outputStream;
	private static int PAPER_WIDTH;

	public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
	}

	public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
							   String portName, String portNumber) {
		return false;
	}

	public void disconnect() {
	}

	public void registerAll() {
	}

	public void setPaperWidth(int lineWidth) {
		if (this instanceof EMSBluetoothStarPrinter) {
			switch (lineWidth) {
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
		} else {
			switch (lineWidth) {
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
		}
	}

	protected void addTotalLines(Context context, Order anOrder, List<Orders> orders, StringBuilder sb, int lineWidth) {
		itemDiscTotal = 0;
		for (Orders order : orders) {
			try {
				itemDiscTotal += Double.parseDouble(order.getItemDiscount());
			} catch (NumberFormatException e) {
				itemDiscTotal = 0;
			}
		}
		saveAmount = itemDiscTotal + Double.parseDouble(anOrder.ord_discount);
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(context.getString(R.string.receipt_subtotal),
				Global.formatDoubleStrToCurrency(anOrder.ord_subtotal), lineWidth, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(context.getString(R.string.receipt_discount_line_item),
				Global.formatDoubleStrToCurrency(String.valueOf(itemDiscTotal)), lineWidth, 0));
		sb.append(textHandler.twoColumnLineWithLeftAlignedText(context.getString(R.string.receipt_global_discount),
				Global.formatDoubleStrToCurrency(anOrder.ord_discount), lineWidth, 0));

		sb.append(textHandler.twoColumnLineWithLeftAlignedText(context.getString(R.string.receipt_tax),
				Global.formatDoubleStrToCurrency(anOrder.ord_taxamount), lineWidth, 0));
	}

	protected void addTaxesLine(List<DataTaxes> taxes, String orderTaxAmount, int lineWidth, StringBuilder sb) {

		int num_taxes = taxes.size();
		double taxAmtTotal = 0;
		if (num_taxes > 0) {
			for (int i = 0; i < num_taxes; i++) {
				double taxAmt = Double.parseDouble(taxes.get(i).get(OrderTaxes_DB.tax_amount));
				taxAmtTotal += Double.parseDouble(taxes.get(i).get(OrderTaxes_DB.tax_amount));
				if (i == num_taxes - 1) {
					BigDecimal rndDifference = new BigDecimal(orderTaxAmount).subtract(new BigDecimal(taxAmtTotal))
							.setScale(2, RoundingMode.HALF_UP);
					taxAmt += Double.parseDouble(String.valueOf(rndDifference));

					sb.append(textHandler.twoColumnLineWithLeftAlignedText(taxes.get(i).get(OrderTaxes_DB.tax_name),
							Global.getCurrencyFormat(String.valueOf(taxAmt)), lineWidth, 2));

				} else {
					sb.append(textHandler.twoColumnLineWithLeftAlignedText(taxes.get(i).get(OrderTaxes_DB.tax_name),
							Global.getCurrencyFormat(taxes.get(i).get(OrderTaxes_DB.tax_amount)), lineWidth, 2));
				}
			}
		}
	}

	protected void releasePrinter() {
		if (this instanceof EMSBluetoothStarPrinter) {
			if (port != null) {
				try {
					StarIOPort.releasePort(port);
				} catch (StarIOPortException e) {
				}
			}
		} else if (this instanceof EMSOneil4te) {
			if (device != null && device.getIsOpen())
				device.close();
		}
	}

	protected void print(String str) {
		if (PRINT_TO_LOG) {
			Log.d("Print", str);
			return;
		}
		if (this instanceof EMSBluetoothStarPrinter) {
			try {
				port.writePort(str.toString().getBytes(), 0, str.toString().length());
			} catch (StarIOPortException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (this instanceof EMSPAT100) {
			printerApi.printData(str);
		} else if (this instanceof EMSBlueBambooP25) {
			byte[] header = { 0x1B, 0x21, 0x01 };
			byte[] lang = new byte[] { (byte) 0x1B, (byte) 0x4B, (byte) 0x31, (byte) 0x1B, (byte) 0x52, 48 };

			try {
				this.outputStream.write(header);
				this.outputStream.write(lang);
				this.outputStream.write(str.getBytes("UTF-8"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (this instanceof EMSOneil4te) {
			device.write(str);
		} else if (this instanceof EMSPowaPOS) {
			powaPOS.printText(str);
		} else if (this instanceof EMSsnbc) {
			byte[] send_buf = null;
			try {
				send_buf = str.getBytes("GB18030");
				pos_sdk.textPrint(send_buf, send_buf.length);
				send_buf = null;
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected void print(byte[] byteArray) {
		if (PRINT_TO_LOG) {
			Log.d("Print", new String(byteArray));
			return;
		}
		if (this instanceof EMSBluetoothStarPrinter) {
			try {
				port.writePort(byteArray, 0, byteArray.length);
			} catch (StarIOPortException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (this instanceof EMSPAT100) {
			printerApi.printData(byteArray, byteArray.length);
		} else if (this instanceof EMSBlueBambooP25) {
			try {
				outputStream.write(byteArray);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (this instanceof EMSOneil4te) {
			device.write(byteArray);
		} else if (this instanceof EMSPowaPOS) {
			powaPOS.printText(new String(byteArray));
		} else if (this instanceof EMSsnbc) {
			byte[] send_buf = null;
			try {
				send_buf = new String(byteArray).getBytes("GB18030");
				pos_sdk.textPrint(send_buf, send_buf.length);
				send_buf = null;
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	protected void print(String str, String FORMAT) {
		if (PRINT_TO_LOG) {
			Log.d("Print", str);
			return;
		}
		if (this instanceof EMSBluetoothStarPrinter) {
			try {
				port.writePort(str.getBytes(FORMAT), 0, str.length());
			} catch (UnsupportedEncodingException  e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (StarIOPortException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (this instanceof EMSPAT100) {
			printerApi.printData(str);
		} else if (this instanceof EMSBlueBambooP25) {
			print(str);
		} else if (this instanceof EMSOneil4te) {
			try {
				device.write(str.getBytes(FORMAT));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (this instanceof EMSPowaPOS) {
			powaPOS.printText(str);
		} else if (this instanceof EMSsnbc) {
			print(str);
		}

	}

	protected void printReceipt(String ordID, int lineWidth, boolean fromOnHold, int type, boolean isFromHistory) {
		try {
			setPaperWidth(lineWidth);
			printPref = myPref.getPrintingPreferences();

			OrderProductsHandler handler = new OrderProductsHandler(activity);
			OrderTaxes_DB ordTaxesDB = new OrderTaxes_DB(activity);

			List<DataTaxes> listOrdTaxes = ordTaxesDB.getOrderTaxes(ordID);
			List<Orders> orders = handler.getPrintOrderedProducts(ordID);

			OrdersHandler orderHandler = new OrdersHandler(activity);
			Order anOrder = orderHandler.getPrintedOrder(ordID);
			ClerksHandler clerkHandler = new ClerksHandler(activity);

			StringBuilder sb = new StringBuilder();
			int size = orders.size();
			printImage(0);
			if (printPref.contains(MyPreferences.print_header))
				printHeader(lineWidth);

			if (anOrder.isVoid.equals("1"))
				sb.append(textHandler.centeredString("*** VOID ***", lineWidth)).append("\n\n");

			if (fromOnHold) {
				sb.append(textHandler.twoColumnLineWithLeftAlignedText("[" + getString(R.string.on_hold) + "]",
						anOrder.ord_HoldName, lineWidth, 0));
			}

			switch (type) {
				case 0: // Order
					sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.order) + ":", ordID,
							lineWidth, 0));
					break;
				case 1: // Return
					sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.return_tag) + ":", ordID,
							lineWidth, 0));
					break;
				case 2: // Invoice
				case 7:// Consignment Invoice
					sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.invoice) + ":", ordID,
							lineWidth, 0));
					break;
				case 3: // Estimate
					sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.estimate) + ":", ordID,
							lineWidth, 0));
					break;
				case 5: // Sales Receipt
					sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.sales_receipt) + ":", ordID,
							lineWidth, 0));
					break;
			}

			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
					Global.formatToDisplayDate(anOrder.ord_timecreated, activity, 3), lineWidth, 0));

			if (!myPref.getShiftIsOpen() || myPref.getPreferences(MyPreferences.pref_use_clerks)) {
				String clerk_id = anOrder.clerk_id;
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_clerk),
						clerkHandler.getClerkName(clerk_id) + "(" + clerk_id + ")", lineWidth, 0));
			}

			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee),
					myPref.getEmpName() + "(" + myPref.getEmpID() + ")", lineWidth, 0));

			String custName = anOrder.cust_name;
			if (custName != null && !custName.isEmpty())
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer), custName,
						lineWidth, 0));

			custName = anOrder.cust_id;
			if (printPref.contains(MyPreferences.print_customer_id) && custName != null && !custName.isEmpty())
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer_id),
						custName, lineWidth, 0));

			String ordComment = anOrder.ord_comment;
			if (ordComment != null && !ordComment.isEmpty()) {
				sb.append("\n\n");
				sb.append("Comments:\n");
				sb.append(textHandler.oneColumnLineWithLeftAlignedText(ordComment, lineWidth, 3)).append("\n");
			}

			sb.append("\n\n");

			print(sb.toString());

			sb.setLength(0);

			if (!myPref.getPreferences(MyPreferences.pref_wholesale_printout)) {
				boolean isRestMode = myPref.getPreferences(MyPreferences.pref_restaurant_mode);

				int m = 0;
				for (int i = 0; i < size; i++) {

					if (isRestMode) {
						if ((i + 1 < size && orders.get(i + 1).getAddon().equals("1"))) {
							m = i;
							sb.append(textHandler.oneColumnLineWithLeftAlignedText(
									orders.get(m).getQty() + "x " + orders.get(m).getName(), lineWidth, 1));
							for (int j = i + 1; j < size; j++) {
								if (orders.get(j).getIsAdded().equals("1"))
									sb.append(textHandler.twoColumnLineWithLeftAlignedText(
											"- " + orders.get(j).getName(),
											Global.getCurrencyFormat(orders.get(j).getOverwritePrice()), lineWidth, 2));
								else
									sb.append(textHandler.twoColumnLineWithLeftAlignedText(
											"- NO " + orders.get(j).getName(),
											Global.getCurrencyFormat(orders.get(j).getOverwritePrice()), lineWidth, 2));

								if ((j + 1 < size && orders.get(j + 1).getAddon().equals("0")) || (j + 1 >= size)) {
									i = j;
									break;
								}

							}

							sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price),
									Global.getCurrencyFormat(orders.get(m).getOverwritePrice()), lineWidth, 3))
									.append("\n");
							sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
									Global.getCurrencyFormat(orders.get(m).getTotal()), lineWidth, 3)).append("\n");

							if (printPref.contains(MyPreferences.print_descriptions)) {
								sb.append(textHandler.twoColumnLineWithLeftAlignedText(
										getString(R.string.receipt_description), "", lineWidth, 3)).append("\n");
								sb.append(textHandler.oneColumnLineWithLeftAlignedText(
										orders.get(m).getProdDescription(), lineWidth, 5)).append("\n");
							}

						} else {
							sb.append(textHandler.oneColumnLineWithLeftAlignedText(
									orders.get(i).getQty() + "x " + orders.get(i).getName(), lineWidth, 1));
							sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price),
									Global.getCurrencyFormat(orders.get(i).getOverwritePrice()), lineWidth, 3))
									.append("\n");
							sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
									Global.getCurrencyFormat(orders.get(i).getTotal()), lineWidth, 3)).append("\n");

							if (printPref.contains(MyPreferences.print_descriptions)) {
								sb.append(textHandler.twoColumnLineWithLeftAlignedText(
										getString(R.string.receipt_description), "", lineWidth, 3)).append("\n");
								sb.append(textHandler.oneColumnLineWithLeftAlignedText(
										orders.get(i).getProdDescription(), lineWidth, 5)).append("\n");
							}
						}
					} else {
						sb.append(textHandler.oneColumnLineWithLeftAlignedText(
								orders.get(i).getQty() + "x " + orders.get(i).getName(), lineWidth, 1));
						sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price),
								Global.getCurrencyFormat(orders.get(i).getOverwritePrice()), lineWidth, 3))
								.append("\n");

						if (orders.get(i).getDiscountID() != null && !orders.get(i).getDiscountID().isEmpty()) {
							sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_discount),
									Global.getCurrencyFormat(orders.get(i).getItemDiscount()), lineWidth, 3))
									.append("\n");
						}

						sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
								Global.getCurrencyFormat(orders.get(i).getTotal()), lineWidth, 3)).append("\n");

						if (printPref.contains(MyPreferences.print_descriptions)) {
							sb.append(textHandler.twoColumnLineWithLeftAlignedText(
									getString(R.string.receipt_description), "", lineWidth, 3)).append("\n");
							sb.append(textHandler.oneColumnLineWithLeftAlignedText(orders.get(i).getProdDescription(),
									lineWidth, 5)).append("\n");
						}

					}

				}
			} else {
				int padding = lineWidth / 4;
				String tempor = Integer.toString(padding);
				StringBuilder tempSB = new StringBuilder();
				tempSB.append("%").append(tempor).append("s").append("%").append(tempor).append("s").append("%")
						.append(tempor).append("s").append("%").append(tempor).append("s");

				sb.append(String.format(tempSB.toString(), "Item", "Qty", "Price", "Total")).append("\n\n");

				for (int i = 0; i < size; i++) {

					sb.append(orders.get(i).getName()).append("-").append(orders.get(i).getProdDescription())
							.append("\n");

					sb.append(String.format(tempSB.toString(), "   ", orders.get(i).getQty(),
							Global.getCurrencyFormat(orders.get(i).getOverwritePrice()),
							Global.getCurrencyFormat(orders.get(i).getTotal()))).append("\n\n");
					print(sb.toString(), FORMAT);
					sb.setLength(0);

				}
			}
			print(sb.toString(), FORMAT);
			sb.setLength(0);

			print(textHandler.lines(lineWidth), FORMAT);
			addTotalLines(this.activity, anOrder, orders, sb, lineWidth);

			int num_taxes = listOrdTaxes.size();
			addTaxesLine(listOrdTaxes, anOrder.ord_taxamount, lineWidth, sb);

			sb.append("\n\n");
			String granTotal = new BigDecimal(anOrder.gran_total).subtract(new BigDecimal(itemDiscTotal)).toString();
			sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_grandtotal),
					Global.formatDoubleStrToCurrency(granTotal), lineWidth, 0));

			PaymentsHandler payHandler = new PaymentsHandler(activity);
			List<String[]> payArrayList = payHandler.getPaymentForPrintingTransactions(ordID);
			if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward)) {
				StoredPayments_DB dbStoredPay = new StoredPayments_DB(activity);
				payArrayList.addAll(dbStoredPay.getPaymentForPrintingTransactions(ordID));
			}
			String receiptSignature = new String();
			size = payArrayList.size();

			double tempGrandTotal = Double.parseDouble(granTotal);
			double tempAmount = 0;
			if (size == 0) {
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amountpaid),
						Global.formatDoubleToCurrency(tempAmount), lineWidth, 0));

				if (type == 2) // Invoice
				{
					sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_balance_due),
							Global.formatDoubleToCurrency(tempGrandTotal - tempAmount), lineWidth, 0));
				}
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total_tip_paid),
						Global.formatDoubleToCurrency(0.00), lineWidth, 0));
				sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cash_returned),
						Global.formatDoubleToCurrency(0.00), lineWidth, 0));
				;
			} else {
				tempAmount += formatStrToDouble(payArrayList.get(0)[0]);
				String _pay_type = payArrayList.get(0)[1].toUpperCase(Locale.getDefault()).trim();
				double tempTipAmount = formatStrToDouble(payArrayList.get(0)[2]);
				StringBuilder tempSB = new StringBuilder();
				tempSB.append(textHandler.oneColumnLineWithLeftAlignedText(
						Global.formatDoubleStrToCurrency(payArrayList.get(0)[0]) + "[" + payArrayList.get(0)[1] + "]",
						lineWidth, 1));
				if (!_pay_type.equals("CASH") && !_pay_type.equals("CHECK")) {
					tempSB.append(textHandler.oneColumnLineWithLeftAlignedText("TransID: " + payArrayList.get(0)[4],
							lineWidth, 1));
					tempSB.append(textHandler.oneColumnLineWithLeftAlignedText("CC#: *" + payArrayList.get(0)[5],
							lineWidth, 1));
				}
				if (!payArrayList.get(0)[3].isEmpty())
					receiptSignature = payArrayList.get(0)[3];

				for (int i = 1; i < size; i++) {
					_pay_type = payArrayList.get(i)[1].toUpperCase(Locale.getDefault()).trim();
					tempAmount = tempAmount + formatStrToDouble(payArrayList.get(i)[0]);
					tempTipAmount = tempTipAmount + formatStrToDouble(payArrayList.get(i)[2]);
					tempSB.append(textHandler
							.oneColumnLineWithLeftAlignedText(Global.formatDoubleStrToCurrency(payArrayList.get(i)[0])
									+ "[" + payArrayList.get(i)[1] + "]", lineWidth, 1));
					if (!_pay_type.equals("CASH") && !_pay_type.equals("CHECK")) {
						tempSB.append(textHandler.oneColumnLineWithLeftAlignedText("TransID: " + payArrayList.get(i)[4],
								lineWidth, 1));
						tempSB.append(textHandler.oneColumnLineWithLeftAlignedText("CC#: *" + payArrayList.get(i)[5],
								lineWidth, 1));
					}
					if (!payArrayList.get(i)[3].isEmpty())
						receiptSignature = payArrayList.get(i)[3];
				}
				if (type == 1) {
					sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amountreturned),
							Global.formatDoubleToCurrency(tempAmount), lineWidth, 0));
				} else {
					sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amountpaid),
							Global.formatDoubleStrToCurrency(Double.toString(tempAmount)), lineWidth, 0));
				}
				sb.append(tempSB.toString());
				if (type == 2) // Invoice
				{
					sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_balance_due),
							Global.formatDoubleToCurrency(tempGrandTotal - tempAmount), lineWidth, 0));
				}
				if (type != 1) {
					sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total_tip_paid),
							Global.formatDoubleStrToCurrency(Double.toString(tempTipAmount)), lineWidth, 0));

					tempAmount = Math.abs(formatStrToDouble(granTotal)) - tempAmount;
					if (tempAmount > 0)
						tempAmount = 0.00;
					sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cash_returned),
							Global.formatDoubleStrToCurrency(Double.toString(tempAmount)), lineWidth, 0))
							.append("\n\n");
				}

			}
			print(sb.toString(), FORMAT);

			print(textHandler.newLines(1), FORMAT);
			if (type != 1)
				printYouSave(String.valueOf(saveAmount), lineWidth);
			if (printPref.contains(MyPreferences.print_footer))
				printFooter(lineWidth);

			print(textHandler.newLines(1), FORMAT);
			receiptSignature = anOrder.ord_signature;
			if (!receiptSignature.isEmpty()) {
				encodedSignature = receiptSignature;
				this.printImage(1);
				// print(enableCenter); // center
				sb.setLength(0);
				sb.append("x").append(textHandler.lines(lineWidth / 2)).append("\n");
				sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(1));
				print(sb.toString(), FORMAT);
				// print(disableCenter); // disable
				// center
			}

			if (isFromHistory) {
				sb.setLength(0);
				sb.append(textHandler.centeredString("*** Copy ***", lineWidth));
				print(sb.toString());
				print(textHandler.newLines(1));
			}

			cutPaper();
		} catch (StarIOPortException e) {

		} catch (JAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

//			releasePrinter();
		}

	}

	public void cutPaper() {
		if (this instanceof EMSsnbc) {
			// ******************************************************************************************
			// print in page mode
			int error_code = pos_sdk.pageModePrint();

			error_code = pos_sdk.systemCutPaper(66, 0);

			// *****************************************************************************************
			// clear buffer in page mode
			error_code = pos_sdk.pageModeClearBuffer();
		} else if (isPOSPrinter)
			print(new byte[] { 0x1b, 0x64, 0x02 }); // Cut
	}

	private void CopyArray(byte[] srcArray, Byte[] cpyArray) {
		for (int index = 0; index < cpyArray.length; index++) {
			cpyArray[index] = srcArray[index];
		}
	}

	public void PrintBitmapImage(Bitmap tempBitmap, boolean compressionEnable, int lineWidth)
			throws StarIOPortException {

		if (PRINT_TO_LOG) {
			Log.d("Print", "*******Image Print***********");
			return;
		}

		ArrayList<Byte> commands = new ArrayList<Byte>();
		Byte[] tempList;

		RasterDocument rasterDoc = new RasterDocument(RasSpeed.Medium, RasPageEndMode.None, RasPageEndMode.None,
				RasTopMargin.Standard, 0, lineWidth / 3, 0);
		// Bitmap bm = BitmapFactory.decodeResource(res, source);
		util.StarBitmap starbitmap = new util.StarBitmap(tempBitmap, false, 350, PAPER_WIDTH);

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

	private static byte[] convertFromListByteArrayTobyteArray(List<Byte> ByteArray) {
		byte[] byteArray = new byte[ByteArray.size()];
		for (int index = 0; index < byteArray.length; index++) {
			byteArray[index] = ByteArray.get(index);
		}

		return byteArray;
	}

	protected void printImage(int type) throws StarIOPortException, JAException {
		if (PRINT_TO_LOG) {
			Log.d("Print", "*******Image Print***********");
			return;
		}
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
			// myBitmap = BitmapFactory.decodeResource(activity.getResources(),
			// R.drawable.companylogo);
			if (this instanceof EMSBluetoothStarPrinter) {
//				float diff = PAPER_WIDTH - myBitmap.getWidth();
//				float percentage = diff / myBitmap.getWidth();
//				int w = myBitmap.getWidth() + (int) (myBitmap.getWidth() * percentage);
//				int h = myBitmap.getHeight();
//				Bitmap canvasBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//				Canvas canvas = new Canvas(canvasBmp);
//				int centreX = (w - myBitmap.getWidth()) / 2;
//				canvas.drawColor(Color.WHITE);
//				canvas.drawBitmap(myBitmap, centreX, 0, null);
//				myBitmap = canvasBmp;
				byte[] data;

				if (isPOSPrinter) {
					data = PrinterFunctions.createCommandsEnglishRasterModeCoupon(PAPER_WIDTH, SCBBitmapConverter.Rotation.Normal,
							myBitmap);
					Communication.Result result;
					result = Communication.sendCommands(data, port, this.activity); // 10000mS!!!

//					PrinterFunctions.PrintBitmap(activity, port.getPortName(), port.getPortSettings(), myBitmap,
//							PAPER_WIDTH, false);
				} else {
					MiniPrinterFunctions.PrintBitmapImage(activity, port.getPortName(), port.getPortSettings(),
							myBitmap, PAPER_WIDTH, false, false);
				}

			} else if (this instanceof EMSPAT100) {
				printerApi.printImage(myBitmap, 0);
			} else if (this instanceof EMSBlueBambooP25) {
				EMSBambooImageLoader loader = new EMSBambooImageLoader();
				ArrayList<ArrayList<Byte>> arrayListList = loader.bambooDataWithAlignment(0, myBitmap);

				for (ArrayList<Byte> arrayList : arrayListList) {

					byte[] byteArray = new byte[arrayList.size()];
					int size = arrayList.size();
					for (int i = 0; i < size; i++) {

						byteArray[i] = arrayList.get(i).byteValue();

					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					print(byteArray);
				}
			} else if (this instanceof EMSOneil4te) {

				// print image
				DocumentLP documentLP = new DocumentLP("$");

				if (type == 1) {
					Bitmap bmp = myBitmap.copy(Bitmap.Config.ARGB_8888, true);
					int w = bmp.getWidth();
					int h = bmp.getHeight();
					int pixel;
					for (int x = 0; x < w; x++) {
						for (int y = 0; y < h; y++) {
							pixel = bmp.getPixel(x, y);
							if (pixel == Color.TRANSPARENT)
								bmp.setPixel(x, y, Color.WHITE);

						}
					}

					documentLP.clear();
					documentLP.writeImage(bmp, 832);

					device.write(documentLP.getDocumentData());
				} else {
					documentLP.clear();
					documentLP.writeImage(myBitmap, 832);

					device.write(documentLP.getDocumentData());
				}

			} else if (this instanceof EMSPowaPOS) {
				// powaPOS.printImage(scaleDown(myBitmap, 300, false));
				powaPOS.printImage(myBitmap);
			} else if (this instanceof EMSsnbc) {
				int PrinterWidth = 640;

				// download bitmap
				pos_sdk.textStandardModeAlignment(ALIGN_CENTER);
				pos_sdk.imageStandardModeRasterPrint(myBitmap, PrinterWidth);
				pos_sdk.textStandardModeAlignment(ALIGN_LEFT);
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
	}

	public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
		float ratio = Math.min((float) maxImageSize / realImage.getWidth(),
				(float) maxImageSize / realImage.getHeight());
		int width = Math.round((float) ratio * realImage.getWidth());
		int height = Math.round((float) ratio * realImage.getHeight());

		Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width, height, filter);
		return newBitmap;
	}

	public void printHeader(int lineWidth) {

		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		StringBuilder sb = new StringBuilder();

		MemoTextHandler handler = new MemoTextHandler(activity);
		String[] header = handler.getHeader();

		if (header[0] != null && !header[0].isEmpty())
			sb.append(textHandler.centeredString(header[0], lineWidth));
		if (header[1] != null && !header[1].isEmpty())
			sb.append(textHandler.centeredString(header[1], lineWidth));
		if (header[2] != null && !header[2].isEmpty())
			sb.append(textHandler.centeredString(header[2], lineWidth));

		if (!sb.toString().isEmpty()) {
			sb.append(textHandler.newLines(1));
			print(sb.toString());

		}
	}

	public void printYouSave(String saveAmount, int lineWidth) {
		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		StringBuilder sb = new StringBuilder(saveAmount);

		print(textHandler.ivuLines(lineWidth), FORMAT);
		sb.setLength(0);
		sb.append(textHandler.newLines(1));

		sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_youSave),
				Global.formatDoubleStrToCurrency(saveAmount), lineWidth, 0));

		sb.append(textHandler.newLines(1));
		print(sb.toString());
		print(textHandler.ivuLines(lineWidth), FORMAT);

	}

	public void printFooter(int lineWidth) {

		EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
		StringBuilder sb = new StringBuilder();
		MemoTextHandler handler = new MemoTextHandler(activity);
		String[] footer = handler.getFooter();

		if (footer[0] != null && !footer[0].isEmpty())
			sb.append(textHandler.centeredString(footer[0], lineWidth));
		if (footer[1] != null && !footer[1].isEmpty())
			sb.append(textHandler.centeredString(footer[1], lineWidth));
		if (footer[2] != null && !footer[2].isEmpty())
			sb.append(textHandler.centeredString(footer[2], lineWidth));

		if (!sb.toString().isEmpty()) {
			sb.append(textHandler.newLines(1));
			print(sb.toString());

		}
	}

	protected double formatStrToDouble(String val) {
		if (val == null || val.isEmpty())
			return 0.00;
		return Double.parseDouble(val);
	}

	protected String getString(int id) {
		return (activity.getResources().getString(id));
	}

}
