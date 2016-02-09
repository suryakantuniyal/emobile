package drivers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.android.database.ClerksHandler;
import com.android.database.InvProdHandler;
import com.android.database.InvoicesHandler;
import com.android.database.MemoTextHandler;
import com.android.database.OrderProductsHandler;
import com.android.database.OrderTaxes_DB;
import com.android.database.OrdersHandler;
import com.android.database.PayMethodsHandler;
import com.android.database.PaymentsHandler;
import com.android.database.ProductsHandler;
import com.android.database.ShiftExpensesDBHandler;
import com.android.database.ShiftPeriodsDBHandler;
import com.android.database.StoredPayments_DB;
import com.android.emobilepos.R;
import com.android.emobilepos.models.DataTaxes;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.OrderProducts;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Payment;
import com.android.emobilepos.models.PaymentDetails;
import com.android.emobilepos.models.ShiftPeriods;
import com.android.emobilepos.payment.ProcessGenius_FA;
import com.android.support.ConsignmentTransaction;
import com.android.database.DBManager;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.mpowa.android.sdk.powapos.PowaPOS;
import com.partner.pt100.printer.PrinterApiContext;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

import POSSDK.POSSDK;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;

import datamaxoneil.connection.Connection_Bluetooth;
import datamaxoneil.printer.DocumentLP;
import drivers.elo.utils.PrinterAPI;
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
    PrinterAPI eloPrinterApi;

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
        saveAmount = itemDiscTotal + (anOrder.ord_discount.isEmpty() ? 0.0 : Double.parseDouble(anOrder.ord_discount));
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
                double taxAmt = Double.parseDouble(taxes.get(i).getTax_amount());
                taxAmtTotal += Double.parseDouble(taxes.get(i).getTax_amount());
                if (i == num_taxes - 1) {
                    BigDecimal rndDifference = new BigDecimal(orderTaxAmount).subtract(new BigDecimal(taxAmtTotal))
                            .setScale(2, RoundingMode.HALF_UP);
                    taxAmt += Double.parseDouble(String.valueOf(rndDifference));

                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(taxes.get(i).getTax_name(),
                            Global.getCurrencyFormat(String.valueOf(taxAmt)), lineWidth, 2));

                } else {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(taxes.get(i).getTax_name(),
                            Global.getCurrencyFormat(taxes.get(i).getTax_amount()), lineWidth, 2));
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
        if (this instanceof EMSELO) {
            eloPrinterApi.print(str);
        } else if (this instanceof EMSBluetoothStarPrinter) {
            try {
                port.writePort(str.toString().getBytes(), 0, str.toString().length());
            } catch (StarIOPortException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (this instanceof EMSPAT100) {
            printerApi.printData(str);
        } else if (this instanceof EMSBlueBambooP25) {
            byte[] header = {0x1B, 0x21, 0x01};
            byte[] lang = new byte[]{(byte) 0x1B, (byte) 0x4B, (byte) 0x31, (byte) 0x1B, (byte) 0x52, 48};

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

        if (this instanceof EMSELO) {
            eloPrinterApi.print(new String(byteArray));
        } else if (this instanceof EMSBluetoothStarPrinter) {
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
        if (this instanceof EMSELO) {
            eloPrinterApi.print(str);
        } else if (this instanceof EMSBluetoothStarPrinter) {
            try {
                port.writePort(str.getBytes(FORMAT), 0, str.length());
            } catch (UnsupportedEncodingException e) {
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

    private void printEMVSection(EMVContainer emvContainer, int lineWidth) {
        if (emvContainer != null && emvContainer.getGeniusResponse() != null) {
            StringBuffer sb = new StringBuffer();
            if (emvContainer.getGeniusResponse().getAdditionalParameters() != null &&
                    emvContainer.getGeniusResponse().getAdditionalParameters().getEMV() != null) {
                String applicationLabel = emvContainer.getGeniusResponse().getAdditionalParameters().getEMV().getApplicationInformation().getApplicationLabel();
                if (applicationLabel != null && !applicationLabel.isEmpty()) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.genius_application_label),
                            applicationLabel, lineWidth, 0));
                }
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.genius_aid),
                        emvContainer.getGeniusResponse().getAdditionalParameters().getEMV().getApplicationInformation().getAid(), lineWidth, 0));
                if (emvContainer.getGeniusResponse().getPaymentType().equalsIgnoreCase(ProcessGenius_FA.Limiters.DISCOVER.name()) ||
                        emvContainer.getGeniusResponse().getPaymentType().equalsIgnoreCase(ProcessGenius_FA.Limiters.AMEX.name()) ||
                        emvContainer.getGeniusResponse().getPaymentType().equalsIgnoreCase("EMVCo")) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.card_exp_date),
                            emvContainer.getGeniusResponse().getAdditionalParameters().getEMV().getCardInformation().getCardExpiryDate(), lineWidth, 0));
                }
                if (emvContainer.getGeniusResponse().getPaymentType().equalsIgnoreCase(ProcessGenius_FA.Limiters.AMEX.name())) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.cryptogram_type),
                            emvContainer.getGeniusResponse().getAdditionalParameters().getEMV().getApplicationCryptogram().getCryptogramType(), lineWidth, 0));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.cryptogram),
                            emvContainer.getGeniusResponse().getAdditionalParameters().getEMV().getApplicationCryptogram().getCryptogram(), lineWidth, 0));
                }
                if (!emvContainer.getGeniusResponse().getAdditionalParameters().getEMV().getPINStatement().isEmpty()) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.pin_statement),
                            emvContainer.getGeniusResponse().getAdditionalParameters().getEMV().getPINStatement(), lineWidth, 0));
                }
                sb.append("\n\n");
                print(sb.toString());
            }
        }
    }

    protected void printReceipt(String ordID, int lineWidth, boolean fromOnHold, Global.OrderType type, boolean isFromHistory, EMVContainer emvContainer) {
        try {
            setPaperWidth(lineWidth);
            printPref = myPref.getPrintingPreferences();

            OrderProductsHandler handler = new OrderProductsHandler(activity);
            OrderTaxes_DB ordTaxesDB = new OrderTaxes_DB();

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
                case ORDER: // Order
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.order) + ":", ordID,
                            lineWidth, 0));
                    break;
                case RETURN: // Return
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.return_tag) + ":", ordID,
                            lineWidth, 0));
                    break;
                case INVOICE: // Invoice
                case CONSIGNMENT_INVOICE:// Consignment Invoice
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.invoice) + ":", ordID,
                            lineWidth, 0));
                    break;
                case ESTIMATE: // Estimate
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.estimate) + ":", ordID,
                            lineWidth, 0));
                    break;
                case SALES_RECEIPT: // Sales Receipt
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
                            sb.append(textHandler.oneColumnLineWithLeftAlignedText(orders.get(m).getQty() + "x " + orders.get(m).getName(), lineWidth, 1));
//                            sb.append(textHandler.oneColumnLineWithLeftAlignedText(orders.get(m).getQty() + "x " + orders.get(m).getName(), lineWidth, 1));
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

            addTaxesLine(listOrdTaxes, anOrder.ord_taxamount, lineWidth, sb);

            sb.append("\n\n");
            String granTotal = (anOrder.gran_total.isEmpty() ? new BigDecimal(0) : new BigDecimal(anOrder.gran_total)).subtract(new BigDecimal(itemDiscTotal)).toString();
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_grandtotal),
                    Global.formatDoubleStrToCurrency(granTotal), lineWidth, 0));

            PaymentsHandler payHandler = new PaymentsHandler(activity);
            List<PaymentDetails> detailsList = payHandler.getPaymentForPrintingTransactions(ordID);
            if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward)) {
                StoredPayments_DB dbStoredPay = new StoredPayments_DB(activity);
                detailsList.addAll(dbStoredPay.getPaymentForPrintingTransactions(ordID));
            }
            String receiptSignature;
            size = detailsList.size();

            double tempGrandTotal = Double.parseDouble(granTotal);
            double tempAmount = 0;
            if (size == 0) {
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amountpaid),
                        Global.formatDoubleToCurrency(tempAmount), lineWidth, 0));

                if (type == Global.OrderType.INVOICE) // Invoice
                {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_balance_due),
                            Global.formatDoubleToCurrency(tempGrandTotal - tempAmount), lineWidth, 0));
                }
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total_tip_paid),
                        Global.formatDoubleToCurrency(0.00), lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cash_returned),
                        Global.formatDoubleToCurrency(0.00), lineWidth, 0));
            } else {

                double paidAmount = 0;
                double tempTipAmount = 0;
                StringBuilder tempSB = new StringBuilder();
                for (int i = 0; i < size; i++) {
                    String _pay_type = detailsList.get(i).getPaymethod_name().toUpperCase(Locale.getDefault()).trim();
                    tempAmount = tempAmount + formatStrToDouble(detailsList.get(i).getPay_amount());
                    paidAmount += formatStrToDouble(detailsList.get(i).getPay_amount());
                    tempTipAmount = tempTipAmount + formatStrToDouble(detailsList.get(i).getPay_tip());
                    tempSB.append(textHandler
                            .oneColumnLineWithLeftAlignedText(Global.formatDoubleStrToCurrency(detailsList.get(i).getPay_amount())
                                    + "[" + detailsList.get(i).getPaymethod_name() + "]", lineWidth, 1));
                    if (!_pay_type.equals("CASH") && !_pay_type.equals("CHECK")) {
                        tempSB.append(textHandler.oneColumnLineWithLeftAlignedText("TransID: " + detailsList.get(i).getPay_transid(),
                                lineWidth, 1));
                        tempSB.append(textHandler.oneColumnLineWithLeftAlignedText("CC#: *" + detailsList.get(i).getCcnum_last4(),
                                lineWidth, 1));
                    }
                    if (!detailsList.get(i).getPay_signature().isEmpty())
                        receiptSignature = detailsList.get(i).getPay_signature();
                }
                if (type == Global.OrderType.ORDER) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amountreturned),
                            Global.formatDoubleToCurrency(tempAmount), lineWidth, 0));
                } else {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amountpaid),
                            Global.formatDoubleStrToCurrency(Double.toString(tempAmount)), lineWidth, 0));
                }
                sb.append(tempSB.toString());
                if (type == Global.OrderType.INVOICE) // Invoice
                {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_balance_due),
                            Global.formatDoubleToCurrency(tempGrandTotal - tempAmount), lineWidth, 0));
                }
                if (type != Global.OrderType.ORDER) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total_tip_paid),
                            Global.formatDoubleStrToCurrency(Double.toString(tempTipAmount)), lineWidth, 0));

                    if (type == Global.OrderType.RETURN) {
                        tempAmount = paidAmount;
                    } else if (tempGrandTotal >= paidAmount) {
                        tempAmount = 0.00;
                    } else {
                        if (tempGrandTotal > 0) {
                            tempAmount = paidAmount - tempGrandTotal;
                        } else {
                            tempAmount = Math.abs(tempGrandTotal);
                        }
                    }
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cash_returned),
                            Global.formatDoubleStrToCurrency(Double.toString(tempAmount)), lineWidth, 0))
                            .append("\n\n");
                }

            }
            print(sb.toString(), FORMAT);

            print(textHandler.newLines(1), FORMAT);
            if (type != Global.OrderType.ORDER)
                printYouSave(String.valueOf(saveAmount), lineWidth);
            sb.setLength(0);
            if (Global.isIvuLoto && detailsList.size() > 0) {

                if (!printPref.contains(MyPreferences.print_ivuloto_qr)) {
//                    sb.append("\n");
//                    sb.append(textHandler.centeredString(textHandler.ivuLines(2 * lineWidth / 3), lineWidth));
//                    sb.append(textHandler.centeredString("CONTROL: " + detailsList.get(0).getIvuLottoNumber(), lineWidth));
//                    sb.append(getString(R.string.enabler_prefix)+"\n");
//
//                    sb.append(textHandler.centeredString(textHandler.ivuLines(2 * lineWidth / 3), lineWidth));
//
//                    sb.append("\n");
//                    print(sb.toString().getBytes());
                    printIVULoto(detailsList.get(0).getIvuLottoNumber(), lineWidth);

//					port.writePort(sb.toString().getBytes(), 0, sb.toString().length());
                } else {
//					encodedQRCode = payArrayList.get(0)[8];
                    printImage(2);
//                    sb.append(textHandler.ivuLines(2 * lineWidth / 3)).append("\n");
//                    sb.append("\t").append("CONTROL: ").append(detailsList.get(0).getIvuLottoNumber()).append("\n");
//                    sb.append(getString(R.string.enabler_prefix)+"\n");
//
//                    sb.append(textHandler.ivuLines(2 * lineWidth / 3)).append("\n");
//                    print(sb.toString().getBytes());
                    printIVULoto(detailsList.get(0).getIvuLottoNumber(), lineWidth);
                }
                sb.setLength(0);
            }

            if (printPref.contains(MyPreferences.print_footer))
                printFooter(lineWidth);

            print(textHandler.newLines(1), FORMAT);
            receiptSignature = anOrder.ord_signature;
            if (!receiptSignature.isEmpty()) {
                encodedSignature = receiptSignature;
                printImage(1);
                sb.setLength(0);
                sb.append("x").append(textHandler.lines(lineWidth / 2)).append("\n");
                sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(1));
                print(sb.toString(), FORMAT);

            }

            if (isFromHistory) {
                sb.setLength(0);
                sb.append(textHandler.centeredString("*** Copy ***", lineWidth));
                print(sb.toString());
                print(textHandler.newLines(1));
            }
            printEnablerWebSite(lineWidth);
            cutPaper();
        } catch (StarIOPortException e) {

        } catch (JAException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
        }

    }

    private void printIVULoto(String ivuLottoNumber, int lineWidth) {
        StringBuffer sb = new StringBuffer();
        sb.append("\n");
        sb.append(textHandler.ivuLines(2 * lineWidth / 3) + "\n");
        sb.append(activity.getString(R.string.ivuloto_control_label) + ivuLottoNumber + "\n");
        sb.append(getString(R.string.enabler_prefix) + "\n");
        sb.append(getString(R.string.powered_by_enabler) + "\n");
        sb.append(textHandler.ivuLines(2 * lineWidth / 3) + "\n");
        print(sb.toString().getBytes());
    }

    private void printEnablerWebSite(int lineWidth) {
        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        sb.append(textHandler.centeredString(getString(R.string.enabler_website) + "\n\n\n", lineWidth));
        print(sb.toString());
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
            print(new byte[]{0x1b, 0x64, 0x02}); // Cut
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

            if (this instanceof EMSBluetoothStarPrinter) {

                byte[] data;

                if (isPOSPrinter) {
                    data = PrinterFunctions.createCommandsEnglishRasterModeCoupon(PAPER_WIDTH, SCBBitmapConverter.Rotation.Normal,
                            myBitmap);
                    Communication.Result result;
                    result = Communication.sendCommands(data, port, this.activity); // 10000mS!!!

//					PrinterFunctions.PrintBitmap(activity, port.getPortName(), port.getPortSettings(), myBitmap,
//							PAPER_WIDTH, false);
                } else {
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

                    MiniPrinterFunctions.PrintBitmapImage(activity, port.getPortName(), port.getPortSettings(),
                            bmp, PAPER_WIDTH, false, false);
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
            } else if (this instanceof EMSELO) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                matrix.preScale(1.0f, -1.0f);
                Bitmap rotatedBmp = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
                eloPrinterApi.print_image(activity, rotatedBmp);
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());

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
            sb.append(textHandler.newLines(3));
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


    protected void printPaymentDetailsReceipt(String payID, int type, boolean isReprint, int lineWidth, EMVContainer emvContainer) {

        try {


            EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
            printPref = myPref.getPrintingPreferences();
            PaymentsHandler payHandler = new PaymentsHandler(activity);
            PaymentDetails payArray;
            boolean isStoredFwd = false;
            long pay_count = payHandler.paymentExist(payID, true);
            if (pay_count == 0) {
                isStoredFwd = true;
                StoredPayments_DB dbStoredPay = new StoredPayments_DB(activity);
                if (emvContainer != null && emvContainer.getGeniusResponse() != null && emvContainer.getGeniusResponse().getStatus().equalsIgnoreCase("DECLINED")) {
                    type = 2;
                }
                payArray = dbStoredPay.getPrintingForPaymentDetails(payID, type);
            } else {
                payArray = payHandler.getPrintingForPaymentDetails(payID, type);
            }
            StringBuilder sb = new StringBuilder();
            boolean isCashPayment = false;
            boolean isCheckPayment = false;
            String constantValue = null;
            String creditCardFooting = "";

            if (payArray.getPaymethod_name() != null && payArray.getPaymethod_name().toUpperCase(Locale.getDefault()).trim().equals("CASH"))
                isCashPayment = true;
            else if (payArray.getPaymethod_name() != null && payArray.getPaymethod_name().toUpperCase(Locale.getDefault()).trim().equals("CHECK"))
                isCheckPayment = true;
            else {
                constantValue = getString(R.string.receipt_included_tip);
                creditCardFooting = getString(R.string.receipt_creditcard_terms);
            }

            printImage(0);

            if (printPref.contains(MyPreferences.print_header))
                printHeader(lineWidth);


            sb.append("* ").append(payArray.getPaymethod_name());
            if (payArray.getIs_refund()!=null && payArray.getIs_refund().equals("1"))
                sb.append(" Refund *\n\n\n");
            else
                sb.append(" Sale *\n\n\n");
            print(textHandler.centeredString(sb.toString(), lineWidth), FORMAT);

            sb.setLength(0);
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
                    getString(R.string.receipt_time), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(payArray.getPay_date(), payArray.getPay_timecreated(), lineWidth, 0))
                    .append("\n\n");

            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer), payArray.getCust_name(),
                    lineWidth, 0));

            if (payArray.getJob_id() != null && !payArray.getJob_id().isEmpty())
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_order_id),
                        payArray.getJob_id(), lineWidth, 0));
            else if (payArray.getInv_id() != null && !payArray.getInv_id().isEmpty()) // invoice
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_invoice_ref),
                        payArray.getInv_id(), lineWidth, 0));

            if (!isStoredFwd)
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_idnum), payID,
                        lineWidth, 0));

            if (!isCashPayment && !isCheckPayment) {
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cardnum),
                        "*" + payArray.getCcnum_last4(), lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("TransID:", payArray.getPay_transid(), lineWidth, 0));
            } else if (isCheckPayment) {
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_checknum),
                        payArray.getPay_check(), lineWidth, 0));
            }

            print(sb.toString());
            sb.setLength(0);

            printEMVSection(payArray.getEmvContainer(), lineWidth);

            String status = payArray.getEmvContainer() != null && payArray.getEmvContainer().getGeniusResponse() != null ? payArray.getEmvContainer().getGeniusResponse().getStatus() : getString(R.string.approved);
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.credit_approval_status),
                    status, lineWidth, 0));

            sb.append(textHandler.newLines(1));
            if (Global.isIvuLoto && Global.subtotalAmount > 0 && !payArray.getTax1_amount().isEmpty()
                    && !payArray.getTax2_amount().isEmpty()) {
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_subtotal),
                        Global.formatDoubleStrToCurrency(String.valueOf(Global.subtotalAmount)), lineWidth, 0));

                sb.append(textHandler.twoColumnLineWithLeftAlignedText(payArray.getTax1_name(),
                        Global.getCurrencyFormat(payArray.getTax1_amount()), lineWidth, 2));

                sb.append(textHandler.twoColumnLineWithLeftAlignedText(payArray.getTax2_name(),
                        Global.getCurrencyFormat(payArray.getTax2_amount()), lineWidth, 2));
            }
//            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
//                    Global.formatDoubleStrToCurrency(payArray.getOrd_total()), lineWidth, 0));
            if (emvContainer != null && emvContainer.getGeniusResponse() != null && emvContainer.getGeniusResponse().getAmountApproved() != null) {
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amount),
                        Global.formatDoubleStrToCurrency(emvContainer.getGeniusResponse().getAmountApproved()), lineWidth, 0));
            } else {
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amount),
                        Global.formatDoubleStrToCurrency(payArray.getPay_amount()), lineWidth, 0));
            }

            String change = payArray.getChange();

            if (isCashPayment && isCheckPayment && !change.isEmpty() && change.contains(".")
                    && Double.parseDouble(change) > 0)
                change = "";

            if (constantValue != null)
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(constantValue,
                        Global.formatDoubleStrToCurrency(change), lineWidth, 0));

            sb.append("\n");
            print(sb.toString(), FORMAT);
            sb.setLength(0);

            if (!isCashPayment && !isCheckPayment) {
                if (myPref.getPreferences(MyPreferences.pref_handwritten_signature)) {
                    sb.append(textHandler.newLines(1));
                } else if (payArray.getPay_signature()!=null && !payArray.getPay_signature().isEmpty()) {
                    encodedSignature = payArray.getPay_signature();
                    printImage(1);
                }
                sb.append("x").append(textHandler.lines(lineWidth / 2)).append("\n");
                sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(1));
                print(sb.toString(), FORMAT);
                sb.setLength(0);
            }

            if (Global.isIvuLoto) {
                sb = new StringBuilder();

                if (!printPref.contains(MyPreferences.print_ivuloto_qr)) {
                    printIVULoto(payArray.getIvuLottoNumber(), lineWidth);
                } else {

                    printIVULoto(payArray.getIvuLottoNumber(), lineWidth);

                }
                sb.setLength(0);
            }

            sb.append("\n");
            print(sb.toString(), FORMAT);
            sb.setLength(0);
            printFooter(lineWidth);
            sb.append("\n");
            print(sb.toString(), FORMAT);
            sb.setLength(0);

            String temp;
            if (!isCashPayment && !isCheckPayment) {
                print(creditCardFooting.toString(), FORMAT);
                temp = textHandler.newLines(1);
                print(temp.toString(), FORMAT);
            }

            sb.setLength(0);
            if (isReprint) {
                sb.append(textHandler.centeredString("*** Copy ***", lineWidth));
                print(sb.toString(), FORMAT);
            }
            printEnablerWebSite(lineWidth);

            cutPaper();

        } catch (StarIOPortException e) {

        } catch (JAException e) {
            e.printStackTrace();
        } finally {
        }

    }

    protected void printStationPrinterReceipt(List<Orders> orders, String ordID, int lineWidth) {
        try {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            if (!isPOSPrinter) {
                port.writePort(new byte[]{0x1d, 0x57, (byte) 0x80, 0x31}, 0, 4);
                port.writePort(new byte[]{0x1d, 0x21, 0x00}, 0, 3);
                port.writePort(new byte[]{0x1b, 0x74, 0x11}, 0, 3); // set to
                // windows-1252
            } else {
                port.writePort(new byte[]{0x1b, 0x1d, 0x74, 0x20}, 0, 4);
                byte[] characterExpansion = new byte[]{0x1b, 0x69, 0x00, 0x00};
                characterExpansion[2] = (byte) (1 + '0');
                characterExpansion[3] = (byte) (1 + '0');

                port.writePort(characterExpansion, 0, characterExpansion.length);
                port.writePort(disableCenter, 0, disableCenter.length); // disable
                // center
            }

            setPaperWidth(lineWidth);

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

            if (!anOrder.cust_name.isEmpty())
                sb.append(anOrder.cust_name).append("\n");

            sb.append(getString(R.string.order)).append(": ").append(ordID).append("\n");
            sb.append(getString(R.string.receipt_started)).append(" ")
                    .append(Global.formatToDisplayDate(anOrder.ord_timecreated, activity, -1)).append("\n");

            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            sdf1.setTimeZone(Calendar.getInstance().getTimeZone());
            Date startedDate = sdf1.parse(anOrder.ord_timecreated);
            Date sentDate = new Date();

            sb.append(getString(R.string.receipt_sent_by)).append(" ").append(myPref.getEmpName()).append(" (");

            if (((float) (sentDate.getTime() - startedDate.getTime()) / 1000) > 60)
                sb.append(Global.formatToDisplayDate(sdf1.format(sentDate.getTime()), activity, -1)).append(")");
            else
                sb.append(Global.formatToDisplayDate(anOrder.ord_timecreated, activity, -1)).append(")");

            String ordComment = anOrder.ord_comment;
            if (ordComment != null && !ordComment.isEmpty()) {
                sb.append("\nComments:\n");
                sb.append(textHandler.oneColumnLineWithLeftAlignedText(ordComment, lineWidth, 3));
            }

            sb.append("\n");
            sb.append(textHandler.newDivider('=', lineWidth / 2)); //add double line divider
//            port.writePort(sb.toString().getBytes(), 0, sb.toString().length());
            print(sb.toString(), FORMAT);
            sb.setLength(0);

            int m = 0;
            for (int i = 0; i < size; i++) {

                if (orders.get(i).getHasAddon().equals("1")) {
                    m = i;
                    ordProdHandler.updateIsPrinted(orders.get(m).getOrdprodID());
                    sb.append(orders.get(m).getQty()).append("x ").append(orders.get(m).getName()).append("\n");
                    if (!orders.get(m).getAttrDesc().isEmpty())
                        sb.append("  [").append(orders.get(m).getAttrDesc()).append("]\n");
                    if (orders.get(m + 1).getAddon().equals("1")) {
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
                    }

                    if (!orders.get(m).getOrderProdComment().isEmpty())
                        sb.append("  ").append(orders.get(m).getOrderProdComment()).append("\n");

                    sb.append(textHandler.newDivider('_', lineWidth / 2)); //add line divider
//                    port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
                    print(sb.toString(), FORMAT);
                    sb.setLength(0);
                } else {
                    ordProdHandler.updateIsPrinted(orders.get(i).getOrdprodID());
                    sb.append(orders.get(i).getQty()).append("x ").append(orders.get(i).getName()).append("\n");

                    if (!orders.get(m).getOrderProdComment().isEmpty())
                        sb.append("  ").append(orders.get(m).getOrderProdComment()).append("\n");

                    sb.append(textHandler.newDivider('_', lineWidth / 2)); //add line divider
//                    port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
                    print(sb.toString(), FORMAT);
                    sb.setLength(0);
                }
            }
            sb.append(textHandler.newLines(1));
//            port.writePort(sb.toString().getBytes(), 0, sb.toString().length());

            print(sb.toString(), FORMAT);
//            printEnablerWebSite(lineWidth);

            if (isPOSPrinter) {
                byte[] characterExpansion = new byte[]{0x1b, 0x69, 0x00, 0x00};
                characterExpansion[2] = (byte) (0 + '0');
                characterExpansion[3] = (byte) (0 + '0');

                port.writePort(characterExpansion, 0, characterExpansion.length);
                cutPaper();
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
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {

        }
    }

    protected void printOpenInvoicesReceipt(String invID, int lineWidth) {
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

            setPaperWidth(lineWidth);
            printImage(0);

            if (printPref.contains(MyPreferences.print_header))
                printHeader(lineWidth);

            sb.append(textHandler.centeredString("Open Invoice Summary", lineWidth)).append("\n\n");

            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_invoice), rightInfo[1],
                    lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_invoice_ref),
                    rightInfo[2], lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer), rightInfo[0],
                    lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_PO), rightInfo[10],
                    lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_terms), rightInfo[9],
                    lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_created), rightInfo[5],
                    lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_ship), rightInfo[7],
                    lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_due), rightInfo[6],
                    lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_paid), rightInfo[8],
                    lineWidth, 0));
//			port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
            print(sb.toString(), FORMAT);

            sb.setLength(0);
            int size = productInfo.size();

            for (int i = 0; i < size; i++) {

                sb.append(textHandler.oneColumnLineWithLeftAlignedText(
                        productInfo.get(i)[2] + "x " + productInfo.get(i)[0], lineWidth, 1));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price),
                        Global.getCurrencyFormat(productInfo.get(i)[3]), lineWidth, 3)).append("\n");
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
                        Global.getCurrencyFormat(productInfo.get(i)[5]), lineWidth, 3)).append("\n");

                if (printPref.contains(MyPreferences.print_descriptions)) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description), "",
                            lineWidth, 3)).append("\n");
                    sb.append(textHandler.oneColumnLineWithLeftAlignedText(productInfo.get(i)[1], lineWidth, 5))
                            .append("\n\n");
                } else
                    sb.append(textHandler.newLines(1));

//				port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
                print(sb.toString(), FORMAT);
                sb.setLength(0);
            }

            sb.append(textHandler.lines(lineWidth)).append("\n");
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_invoice_total),
                    Global.getCurrencyFormat(rightInfo[11]), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amount_collected),
                    Global.getCurrencyFormat(rightInfo[13]), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_balance_due),
                    Global.getCurrencyFormat(rightInfo[12]), lineWidth, 0)).append("\n\n\n");

            sb.append(textHandler.centeredString(getString(R.string.receipt_thankyou), lineWidth));
//			port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
            print(sb.toString(), FORMAT);
//			port.writePort(textHandler.newLines(1).getBytes(FORMAT), 0, textHandler.newLines(1).getBytes(FORMAT).length);
            print(textHandler.newLines(1), FORMAT);
            printEnablerWebSite(lineWidth);

            cutPaper();

        } catch (StarIOPortException e) {

        } catch (JAException e) {
            e.printStackTrace();
        } finally {

        }
    }


    protected void printConsignmentReceipt(List<ConsignmentTransaction> myConsignment, String encodedSig, int lineWidth) {
        try {

            encodedSignature = encodedSig;
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
            setPaperWidth(lineWidth);

            printImage(0);

            if (printPref.contains(MyPreferences.print_header))
                printHeader(lineWidth);

            sb.append(textHandler.centeredString("Consignment Summary", lineWidth)).append("\n\n");

            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer),
                    myPref.getCustName(), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee),
                    myPref.getEmpName(), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cons_trans_id),
                    myConsignment.get(0).ConsTrans_ID, lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
                    Global.formatToDisplayDate(Global.getCurrentDate(), activity, 3), lineWidth, 0));
            sb.append(textHandler.newLines(1));

            for (int i = 0; i < size; i++) {
                if (!myConsignment.get(i).ConsOriginal_Qty.equals("0")) {
                    map = productDBHandler.getProductMap(myConsignment.get(i).ConsProd_ID, true);

                    sb.append(textHandler.oneColumnLineWithLeftAlignedText(map.get("prod_name"), lineWidth, 0));

                    if (printPref.contains(MyPreferences.print_descriptions)) {
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description),
                                "", lineWidth, 3)).append("\n");
                        sb.append(textHandler.oneColumnLineWithLeftAlignedText(map.get("prod_desc"), lineWidth, 5))
                                .append("\n");
                    } else
                        sb.append(textHandler.newLines(1));

                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original Qty:",
                            myConsignment.get(i).ConsOriginal_Qty, lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Rack Qty:",
                            myConsignment.get(i).ConsStock_Qty, lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Returned Qty:",
                            myConsignment.get(i).ConsReturn_Qty, lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Sold Qty:",
                            myConsignment.get(i).ConsInvoice_Qty, lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Dispatched Qty:",
                            myConsignment.get(i).ConsDispatch_Qty, lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("New Qty:", myConsignment.get(i).ConsNew_Qty,
                            lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Product Price:",
                            Global.formatDoubleStrToCurrency(map.get("prod_price")), lineWidth, 5));

                    returnAmount = Global.formatNumFromLocale(myConsignment.get(i).ConsReturn_Qty)
                            * Global.formatNumFromLocale(map.get("prod_price"));
                    subtotalAmount = Global.formatNumFromLocale(myConsignment.get(i).invoice_total) + returnAmount;

                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Subtotal:",
                            Global.formatDoubleToCurrency(subtotalAmount), lineWidth, 5));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Credit Memo:",
                            Global.formatDoubleToCurrency(returnAmount), lineWidth, 5));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total:",
                            Global.formatDoubleStrToCurrency(myConsignment.get(i).invoice_total), lineWidth, 5))
                            .append(textHandler.newLines(1));

                    totalSold += Double.parseDouble(myConsignment.get(i).ConsInvoice_Qty);
                    totalReturned += Double.parseDouble(myConsignment.get(i).ConsReturn_Qty);
                    totalDispached += Double.parseDouble(myConsignment.get(i).ConsDispatch_Qty);
                    totalLines += 1;
                    ordTotal += Double.parseDouble(myConsignment.get(i).invoice_total);

//					port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
                    print(sb.toString(), FORMAT);
                    sb.setLength(0);
                }
            }

            sb.append(textHandler.lines(lineWidth));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Sold:", Double.toString(totalSold),
                    lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Returned",
                    Double.toString(totalReturned), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Dispatched",
                    Double.toString(totalDispached), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Line Items", Double.toString(totalLines),
                    lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Grand Total:",
                    Global.formatDoubleToCurrency(ordTotal), lineWidth, 0));
            sb.append(textHandler.newLines(1));

//			port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
            print(sb.toString(), FORMAT);
            if (printPref.contains(MyPreferences.print_descriptions))
                printFooter(lineWidth);

            //this.printImage(1);
            try {
                printImage(1);
            } catch (StarIOPortException e) {
                e.printStackTrace();
            } catch (JAException e) {
                e.printStackTrace();
            }
            printEnablerWebSite(lineWidth);

//			port.writePort(textHandler.newLines(1).getBytes(FORMAT), 0, textHandler.newLines(1).length());
            print(textHandler.newLines(1), FORMAT);
            cutPaper();

            // db.close();

        } catch (StarIOPortException e) {

        } catch (JAException e) {
            e.printStackTrace();
        } finally {
        }

    }


    protected void printConsignmentHistoryReceipt(HashMap<String, String> map, Cursor c, boolean isPickup, int lineWidth) {
        try {


            encodedSignature = map.get("encoded_signature");
            printPref = myPref.getPrintingPreferences();
            EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
            StringBuilder sb = new StringBuilder();
            String prodDesc = "";

            int size = c.getCount();
            setPaperWidth(lineWidth);
            printImage(0);

            if (printPref.contains(MyPreferences.print_header))
                printHeader(lineWidth);

            if (!isPickup)
                sb.append(textHandler.centeredString(getString(R.string.consignment_summary), lineWidth))
                        .append("\n\n");
            else
                sb.append(textHandler.centeredString(getString(R.string.consignment_pickup), lineWidth))
                        .append("\n\n");

            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer),
                    map.get("cust_name"), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee),
                    myPref.getEmpName(), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cons_trans_id),
                    map.get("ConsTrans_ID"), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
                    Global.formatToDisplayDate(Global.getCurrentDate(), activity, 3), lineWidth, 0));
            sb.append(textHandler.newLines(1));

            for (int i = 0; i < size; i++) {
                c.moveToPosition(i);

                sb.append(textHandler.oneColumnLineWithLeftAlignedText(c.getString(c.getColumnIndex("prod_name")),
                        lineWidth, 0));

                if (printPref.contains(MyPreferences.print_descriptions)) {
                    prodDesc = c.getString(c.getColumnIndex("prod_desc"));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description), "",
                            lineWidth, 3)).append("\n");
                    if (!prodDesc.isEmpty())
                        sb.append(textHandler.oneColumnLineWithLeftAlignedText(
                                c.getString(c.getColumnIndex("prod_desc")), lineWidth, 5)).append("\n");
                } else
                    sb.append(textHandler.newLines(1));

                if (!isPickup) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original Qty:",
                            c.getString(c.getColumnIndex("ConsOriginal_Qty")), lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Rack Qty:",
                            c.getString(c.getColumnIndex("ConsStock_Qty")), lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Returned Qty:",
                            c.getString(c.getColumnIndex("ConsReturn_Qty")), lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Sold Qty:",
                            c.getString(c.getColumnIndex("ConsInvoice_Qty")), lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Dispatched Qty:",
                            c.getString(c.getColumnIndex("ConsDispatch_Qty")), lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("New Qty:",
                            c.getString(c.getColumnIndex("ConsNew_Qty")), lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Product Price:",
                            Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex("price"))), lineWidth, 5));

                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Subtotal:",
                            Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex("item_subtotal"))),
                            lineWidth, 5));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Credit Memo:",
                            Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex("credit_memo"))), lineWidth,
                            5));

                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total:",
                            Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex("item_total"))), lineWidth,
                            5)).append(textHandler.newLines(1));
                } else {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original Qty:",
                            c.getString(c.getColumnIndex("ConsOriginal_Qty")), lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Picked up Qty:",
                            c.getString(c.getColumnIndex("ConsPickup_Qty")), lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("New Qty:",
                            c.getString(c.getColumnIndex("ConsNew_Qty")), lineWidth, 3)).append("\n\n\n");

                }
//				port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
                print(sb.toString(), FORMAT);
                sb.setLength(0);

            }

            sb.append(textHandler.lines(lineWidth));
            if (!isPickup) {
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Sold:", map.get("total_items_sold"),
                        lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Returned",
                        map.get("total_items_returned"), lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Dispatched",
                        map.get("total_items_dispatched"), lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Line Items", map.get("total_line_items"),
                        lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Grand Total:",
                        Global.formatDoubleStrToCurrency(map.get("total_grand_total")), lineWidth, 0));
            }
            sb.append(textHandler.newLines(1));

//			port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
            print(sb.toString(), FORMAT);

            if (printPref.contains(MyPreferences.print_footer))
                printFooter(lineWidth);

            printImage(1);

//			port.writePort(textHandler.newLines(1).getBytes(FORMAT), 0, textHandler.newLines(1).length());
            print(textHandler.newLines(3), FORMAT);
            printEnablerWebSite(lineWidth);

            cutPaper();

        } catch (StarIOPortException e) {

        } catch (JAException e) {
            e.printStackTrace();
        } finally {
        }
    }


    protected void printConsignmentPickupReceipt(List<ConsignmentTransaction> myConsignment, String encodedSig, int lineWidth) {
        try {

            printPref = myPref.getPrintingPreferences();
            EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
            StringBuilder sb = new StringBuilder();
            // SQLiteDatabase db = new DBManager(activity).openReadableDB();
            ProductsHandler productDBHandler = new ProductsHandler(activity);
            HashMap<String, String> map = new HashMap<String, String>();
            String prodDesc = "";

            int size = myConsignment.size();
            setPaperWidth(lineWidth);

            printImage(0);

            if (printPref.contains(MyPreferences.print_header))
                printHeader(lineWidth);

            sb.append(textHandler.centeredString("Consignment Pickup Summary", lineWidth)).append("\n\n");

            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer),
                    myPref.getCustName(), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee),
                    myPref.getEmpName(), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
                    Global.formatToDisplayDate(Global.getCurrentDate(), activity, 3), lineWidth, 0));
            sb.append(textHandler.newLines(1));

            for (int i = 0; i < size; i++) {
                map = productDBHandler.getProductMap(myConsignment.get(i).ConsProd_ID, true);

                sb.append(textHandler.oneColumnLineWithLeftAlignedText(map.get("prod_name"), lineWidth, 0));

                if (printPref.contains(MyPreferences.print_descriptions)) {
                    prodDesc = map.get("prod_desc");
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description), "",
                            lineWidth, 3)).append("\n");
                    if (!prodDesc.isEmpty())
                        sb.append(textHandler.oneColumnLineWithLeftAlignedText(prodDesc, lineWidth, 5)).append("\n");
                }

                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original Qty:",
                        myConsignment.get(i).ConsOriginal_Qty, lineWidth, 3));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Picked up Qty:",
                        myConsignment.get(i).ConsPickup_Qty, lineWidth, 3));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("New Qty:", myConsignment.get(i).ConsNew_Qty,
                        lineWidth, 3)).append("\n\n\n");

//				port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
                print(sb.toString(), FORMAT);
                sb.setLength(0);
            }

            if (printPref.contains(MyPreferences.print_footer))
                printFooter(lineWidth);

            if (!encodedSig.isEmpty()) {
                encodedSignature = encodedSig;
                printImage(1);
//                port.writePort(enableCenter, 0, enableCenter.length); // center
                sb.setLength(0);
                sb.append("x").append(textHandler.lines(lineWidth / 2)).append("\n");
                sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(1));
//				port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
                print(sb.toString(), FORMAT);
                print(textHandler.newLines(3), FORMAT);
//				port.writePort(disableCenter, 0, disableCenter.length); // disable
                // center
            }
            printEnablerWebSite(lineWidth);

            cutPaper();

            // db.close();

        } catch (StarIOPortException e) {

        } catch (JAException e) {
            e.printStackTrace();
        } finally {
        }

    }

    protected void printEndOfDayReportReceipt(String curDate, int lineWidth, boolean printDetails) {

        String mDate = Global.formatToDisplayDate(curDate, activity, 4);
        StringBuilder sb = new StringBuilder();
        EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
        StringBuilder sb_ord_types = new StringBuilder();

        OrdersHandler ordHandler = new OrdersHandler(activity);
        ShiftPeriodsDBHandler shiftHandler = new ShiftPeriodsDBHandler(activity);
        OrderProductsHandler ordProdHandler = new OrderProductsHandler(activity);
        PaymentsHandler paymentHandler = new PaymentsHandler(activity);
        boolean showTipField = false;

        //determine if we should include the tip field
        if (myPref.getPreferences(MyPreferences.pref_show_tips_for_cash)) {
            showTipField = true;
        }

        sb.append(textHandler.centeredString("End Of Day Report", lineWidth));

        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Date", Global.formatToDisplayDate(curDate, activity, 1), lineWidth, 0));

//        sb.append(textHandler.twoColumnLineWithLeftAlignedText( , ,lineWidth, 3));

        sb.append(textHandler.newLines(2));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Employee", myPref.getEmpName(), lineWidth, 0));
        sb.append(textHandler.newLines(2));

        sb.append(textHandler.centeredString("Summary", lineWidth));
        sb.append(textHandler.newLines(1));

        BigDecimal returnAmount = new BigDecimal("0");
        BigDecimal salesAmount = new BigDecimal("0");
        BigDecimal invoiceAmount = new BigDecimal("0");

        sb_ord_types.append(textHandler.centeredString("Totals By Order Types", lineWidth));
        List<Order> listOrder = ordHandler.getOrderDayReport(null, mDate);

        for (Order ord : listOrder) {

            switch (Global.OrderType.getByCode(Integer.parseInt(ord.ord_type))) {
                case RETURN:
                    sb_ord_types.append(textHandler.oneColumnLineWithLeftAlignedText("Return", lineWidth, 0));
                    returnAmount = new BigDecimal(ord.ord_total);
                    break;
                case ESTIMATE:
                    sb_ord_types.append(textHandler.oneColumnLineWithLeftAlignedText("Estimate", lineWidth, 0));
                    break;
                case ORDER:
                    sb_ord_types.append(textHandler.oneColumnLineWithLeftAlignedText("Order", lineWidth, 0));
                    break;
                case SALES_RECEIPT:
                    sb_ord_types.append(textHandler.oneColumnLineWithLeftAlignedText("Sales Receipt", lineWidth, 0));
                    salesAmount = new BigDecimal(ord.ord_total);
                    break;
                case INVOICE:
                    sb_ord_types.append(textHandler.oneColumnLineWithLeftAlignedText("Invoice", lineWidth, 0));
                    invoiceAmount = new BigDecimal(ord.ord_total);
                    break;
            }

            sb_ord_types.append(textHandler.twoColumnLineWithLeftAlignedText("SubTotal", Global.formatDoubleStrToCurrency(ord.ord_subtotal), lineWidth, 3));
            sb_ord_types.append(textHandler.twoColumnLineWithLeftAlignedText("Discount Total", Global.formatDoubleStrToCurrency(ord.ord_discount), lineWidth, 3));
            sb_ord_types.append(textHandler.twoColumnLineWithLeftAlignedText("Tax Total", Global.formatDoubleStrToCurrency(ord.ord_taxamount), lineWidth, 3));
            sb_ord_types.append(textHandler.twoColumnLineWithLeftAlignedText("Net Total", Global.formatDoubleStrToCurrency(ord.ord_total), lineWidth, 3));
        }

        listOrder.clear();

        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Return", "(" + Global.formatDoubleStrToCurrency(returnAmount.toString()) + ")", lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Sales Receipt", Global.formatDoubleStrToCurrency(salesAmount.toString()), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Invoice", Global.formatDoubleStrToCurrency(invoiceAmount.toString()), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total", Global.formatDoubleStrToCurrency(salesAmount.add(invoiceAmount).subtract(returnAmount).toString()), lineWidth, 0));

        listOrder = ordHandler.getARTransactionsDayReport(null, mDate);
        if (listOrder.size() > 0) {
            sb.append(textHandler.newLines(2));
            sb.append(textHandler.centeredString("A/R Transactions", lineWidth));
            sb.append(textHandler.threeColumnLineItem("ID", 40, "Customer", 40, "Amount", 20, lineWidth, 0));
            for (Order ord : listOrder) {
                if (ord.ord_id != null)
                    sb.append(textHandler.threeColumnLineItem(ord.ord_id, 40, ord.cust_name, 40, Global.formatDoubleStrToCurrency(ord.ord_total), 20, lineWidth, 0));
            }
            listOrder.clear();
        }

        List<ShiftPeriods> listShifts = shiftHandler.getShiftDayReport(null, mDate);
        if (listShifts.size() > 0) {
            sb.append(textHandler.newLines(2));
            sb.append(textHandler.centeredString("Totals By Shift", lineWidth));
            for (ShiftPeriods shift : listShifts) {
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Sales Clerk", shift.assignee_name, lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("From", Global.formatToDisplayDate(shift.startTime, activity, 2), lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("To", Global.formatToDisplayDate(shift.endTime, activity, 2), lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Beginning Petty Cash", Global.formatDoubleStrToCurrency(shift.beginning_petty_cash), lineWidth, 3));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Expenses", "(" + Global.formatDoubleStrToCurrency(shift.total_expenses) + ")", lineWidth, 3));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Ending Petty Cash", Global.formatDoubleStrToCurrency(shift.ending_petty_cash), lineWidth, 3));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Transactions Cash", Global.formatDoubleStrToCurrency(shift.total_transaction_cash), lineWidth, 3));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Ending Cash", Global.formatDoubleStrToCurrency(shift.total_ending_cash), lineWidth, 3));
//                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Refunds", "(" + "?????" + ")", lineWidth, 3));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Entered Close Amount", shift.entered_close_amount, lineWidth, 3));
//                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Over (Short)", shift.over_short, lineWidth, 3));
            }
            listShifts.clear();
        }

        sb.append(textHandler.newLines(2));
//???? what is this doing???
        sb.append(sb_ord_types);


        List<OrderProducts> listProd = ordProdHandler.getProductsDayReport(true, null, mDate);
        if (listProd.size() > 0) {
            sb.append(textHandler.newLines(2));
            sb.append(textHandler.centeredString("Items Sold", lineWidth));

            sb.append(textHandler.threeColumnLineItem("Name", 60, "Qty", 20, "Total", 20, lineWidth, 0));

            for (OrderProducts prod : listProd) {
                sb.append(textHandler.threeColumnLineItem(prod.ordprod_name, 60, prod.ordprod_qty, 20, Global.formatDoubleStrToCurrency(prod.overwrite_price), 20, lineWidth, 0));
                if (printDetails) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("UPC:" + prod.prod_upc, "", lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("SKU:" + prod.prod_sku, "", lineWidth, 3));
                }
            }

            listProd.clear();
        }

        List<Payment> listPayments = paymentHandler.getPaymentsDayReport(0, null, mDate);
        if (listPayments.size() > 0) {
            sb.append(textHandler.newLines(2));
            sb.append(textHandler.centeredString("Payments", lineWidth));
            for (Payment payment : listPayments) {
                sb.append(textHandler.oneColumnLineWithLeftAlignedText(payment.card_type, lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Amount", Global.formatDoubleStrToCurrency(payment.pay_amount), lineWidth, 2));

                if (printDetails) {
                    //check if tip should be printed
                    if (showTipField) {
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Tip", Global.formatDoubleStrToCurrency(payment.pay_tip), lineWidth, 2));
                    }
                    sb.append(textHandler.oneColumnLineWithLeftAlignedText("Details", lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("ID", payment.pay_id, lineWidth, 4));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Amount", Global.formatDoubleStrToCurrency(payment.pay_amount), lineWidth, 4));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Invoice", payment.job_id, lineWidth, 4));
                    sb.append(textHandler.newLines(1));
                }
            }
            listPayments.clear();
        }


        listPayments = paymentHandler.getPaymentsDayReport(1, null, mDate);
        if (listPayments.size() > 0) {
            sb.append(textHandler.newLines(2));
            sb.append(textHandler.centeredString("Void", lineWidth));
            for (Payment payment : listPayments) {
                sb.append(textHandler.oneColumnLineWithLeftAlignedText(payment.card_type, lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Amount", Global.formatDoubleStrToCurrency(payment.pay_amount), lineWidth, 2));

                if (printDetails) {
                    //check if tip should be printed
                    if (showTipField) {
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Tip", Global.formatDoubleStrToCurrency(payment.pay_tip), lineWidth, 2));
                    }
                    sb.append(textHandler.oneColumnLineWithLeftAlignedText("Details", lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("ID", payment.pay_id, lineWidth, 4));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Amount", Global.formatDoubleStrToCurrency(payment.pay_amount), lineWidth, 4));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Invoice", payment.job_id, lineWidth, 4));
                    sb.append(textHandler.newLines(1));
                }
            }
            listPayments.clear();
        }

        listPayments = paymentHandler.getPaymentsDayReport(2, null, mDate);
        if (listPayments.size() > 0) {
            sb.append(textHandler.newLines(2));
            sb.append(textHandler.centeredString("Refund", lineWidth));
            for (Payment payment : listPayments) {
                sb.append(textHandler.oneColumnLineWithLeftAlignedText(payment.card_type, lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Amount", Global.formatDoubleStrToCurrency(payment.pay_amount), lineWidth, 2));

                if (printDetails) {
                    //check if tip should be printed
                    if (showTipField) {
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Tip", Global.formatDoubleStrToCurrency(payment.pay_tip), lineWidth, 2));
                    }
                    sb.append(textHandler.oneColumnLineWithLeftAlignedText("Details", lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("ID", payment.pay_id, lineWidth, 4));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Amount", Global.formatDoubleStrToCurrency(payment.pay_amount), lineWidth, 4));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Invoice", payment.job_id, lineWidth, 4));
                    sb.append(textHandler.newLines(1));
                }
            }
            listPayments.clear();
        }


        listProd = ordProdHandler.getProductsDayReport(false, null, mDate);
        if (listProd.size() > 0) {
            sb.append(textHandler.newLines(2));
            sb.append(textHandler.centeredString("Items Returned", lineWidth));
            sb.append(textHandler.threeColumnLineItem("Name", 60, "Qty", 20, "Total", 20, lineWidth, 0));
            for (OrderProducts prod : listProd) {
                sb.append(textHandler.threeColumnLineItem(prod.ordprod_name, 60, prod.ordprod_qty, 20, Global.formatDoubleStrToCurrency(prod.overwrite_price), 20, lineWidth, 0));
                if (printDetails) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("UPC:" + prod.prod_upc, "", lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("SKU:" + prod.prod_sku, "", lineWidth, 3));
                }
            }
            listProd.clear();
        }

        listProd = ordProdHandler.getDepartmentDayReport(true, null, mDate);
        if (listProd.size() > 0) {
            sb.append(textHandler.newLines(2));
            sb.append(textHandler.centeredString("Department Sales", lineWidth));
            sb.append(textHandler.threeColumnLineItem("Name", 60, "Qty", 20, "Total", 20, lineWidth, 0));
            for (OrderProducts prod : listProd) {
                sb.append(textHandler.threeColumnLineItem(prod.cat_name, 60, prod.ordprod_qty, 20, Global.formatDoubleStrToCurrency(prod.overwrite_price), 20, lineWidth, 0));
            }
            listProd.clear();
        }

        listProd = ordProdHandler.getDepartmentDayReport(true, null, mDate);
        if (listProd.size() > 0) {
            sb.append(textHandler.newLines(2));
            sb.append(textHandler.centeredString("Department Returns", lineWidth));
            sb.append(textHandler.threeColumnLineItem("Name", 60, "Qty", 20, "Total", 20, lineWidth, 0));
            for (OrderProducts prod : listProd) {
                sb.append(textHandler.threeColumnLineItem(prod.cat_name, 60, prod.ordprod_qty, 20, Global.formatDoubleStrToCurrency(prod.overwrite_price), 20, lineWidth, 0));
            }
            listProd.clear();
        }


        sb.append(textHandler.centeredString("** End of report **", lineWidth));
        sb.append(textHandler.newLines(4));
        print(sb.toString(), FORMAT);
        cutPaper();
    }

    protected void printShiftDetailsReceipt(int lineWidth, String shiftID) {
        StringBuilder sb = new StringBuilder();
        EMSPlainTextHelper textHandler = new EMSPlainTextHelper();

        ShiftPeriodsDBHandler shiftHandler = new ShiftPeriodsDBHandler(activity);
        SparseArray<String> shift = shiftHandler.getShiftDetails(shiftID);

        sb.append(textHandler.centeredString("Shift Details", lineWidth));

        sb.append(textHandler.newLines(2));

        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Sales Clerk:", shift.get(0), lineWidth, 0));
        MyPreferences myPreferences = new MyPreferences(activity);
        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Employee: ", myPreferences.getEmpName(), lineWidth, 0));
        sb.append(textHandler.newLines(2));
        sb.append("From: " + shift.get(7)); //startTime
        sb.append(textHandler.newLines(1));
        if (shift.get(8).isEmpty()) {
            sb.append("To: " + shift.get(9)); //display Open
        } else {
            sb.append("To: " + shift.get(8)); //show endTime

        }
        sb.append(textHandler.newLines(2));

        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Beginning Petty Cash", shift.get(1), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Expenses", "(" + shift.get(2) + ")", lineWidth, 0));

        ShiftExpensesDBHandler shiftExpensesDBHandler = new ShiftExpensesDBHandler(activity);
        Cursor expensesByShift;
        expensesByShift = shiftExpensesDBHandler.getShiftExpenses(shiftID);

        int i = 0;
        while (!expensesByShift.isAfterLast()) {
            // expName = expensesByShift.getString(0); //get the expense id
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(expensesByShift.getString(4), Global.formatDoubleStrToCurrency(expensesByShift.getString(2)), lineWidth, 3));
            //theSpinnerNames[i] = productExpensesCursor.getString(2); //get if expense
            i++;
            expensesByShift.moveToNext();
        }

        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Ending Petty Cash", shift.get(3), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Transactions Cash", shift.get(4), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Ending Cash", shift.get(5), lineWidth, 0));
//                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Refunds", "(" + "?????" + ")", lineWidth, 3));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Entered Close Amount", shift.get(6), lineWidth, 0));
//                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Over (Short)", shift.over_short, lineWidth, 3));

        sb.append(textHandler.newLines(2));

        sb.append(textHandler.centeredString("** End of shift report **", lineWidth));
        sb.append(textHandler.newLines(4));
        print(sb.toString(), FORMAT);
        cutPaper();
    }

    protected void printReportReceipt(String curDate, int lineWidth) {

        try {

            PaymentsHandler paymentHandler = new PaymentsHandler(activity);
            PayMethodsHandler payMethodHandler = new PayMethodsHandler(activity);
            EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
            StringBuilder sb = new StringBuilder();
            StringBuilder sb_refunds = new StringBuilder();
//            port.writePort(textHandler.newLines(1).getBytes(FORMAT), 0, textHandler.newLines(1).length());
            print(textHandler.newLines(1), FORMAT);
            sb.append(textHandler.centeredString("REPORT", lineWidth));
            sb.append(textHandler.centeredString(Global.formatToDisplayDate(curDate, activity, 0), lineWidth));
            sb.append(textHandler.newLines(1));
            sb.append(textHandler.oneColumnLineWithLeftAlignedText(getString(R.string.receipt_pay_summary), lineWidth,
                    0));

            sb.append(textHandler.newLines(2));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Employee", myPref.getEmpName(), lineWidth, 0));
            sb.append(textHandler.newLines(2));

            sb_refunds.append(textHandler.oneColumnLineWithLeftAlignedText(getString(R.string.receipt_refund_summmary),
                    lineWidth, 0));

            HashMap<String, String> paymentMap = paymentHandler
                    .getPaymentsRefundsForReportPrinting(Global.formatToDisplayDate(curDate, activity, 4), 0);
            HashMap<String, String> refundMap = paymentHandler
                    .getPaymentsRefundsForReportPrinting(Global.formatToDisplayDate(curDate, activity, 4), 1);
            List<String[]> payMethodsNames = payMethodHandler.getPayMethodsName();
            int size = payMethodsNames.size();
            double payGranTotal = 0.00;
            double refundGranTotal = 0.00;
//            port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
            print(sb.toString(), FORMAT);
            sb.setLength(0);



            for (int i = 0; i < size; i++) {
                if (paymentMap.containsKey(payMethodsNames.get(i)[0])) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
                            Global.formatDoubleStrToCurrency(paymentMap.get(payMethodsNames.get(i)[0])), lineWidth,
                            3));

                    payGranTotal += Double.parseDouble(paymentMap.get(payMethodsNames.get(i)[0]));
                } else
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
                            Global.formatDoubleToCurrency(0.00), lineWidth, 3));

                if (refundMap.containsKey(payMethodsNames.get(i)[0])) {
                    sb_refunds.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
                            Global.formatDoubleStrToCurrency(refundMap.get(payMethodsNames.get(i)[0])), lineWidth, 3));
                    refundGranTotal += Double.parseDouble(refundMap.get(payMethodsNames.get(i)[0]));
                } else
                    sb_refunds.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
                            Global.formatDoubleToCurrency(0.00), lineWidth, 3));
            }

            sb.append(textHandler.newLines(1));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
                    Global.formatDoubleStrToCurrency(Double.toString(payGranTotal)), lineWidth, 4));
            sb.append(textHandler.newLines(1));

            sb_refunds.append(textHandler.newLines(1));
            sb_refunds.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
                    Global.formatDoubleStrToCurrency(Double.toString(refundGranTotal)), lineWidth, 4));

//            port.writePort(sb.toString().getBytes(FORMAT), 0, sb.toString().length());
            //print earnings
            print(sb.toString(), FORMAT);
            print(textHandler.newLines(2), FORMAT);
//            port.writePort(sb_refunds.toString().getBytes(FORMAT), 0, sb_refunds.toString().length());
            //print refunds
            print(sb_refunds.toString(), FORMAT);
//            port.writePort(textHandler.newLines(1).getBytes(FORMAT), 0, textHandler.newLines(1).length());
            print(textHandler.newLines(5), FORMAT);
            printEnablerWebSite(lineWidth);

            if (isPOSPrinter) {
                port.writePort(new byte[]{0x1b, 0x64, 0x02}, 0, 3); // Cut
            }
            cutPaper();

        } catch (StarIOPortException e) {
        } finally {
        }
    }

}
