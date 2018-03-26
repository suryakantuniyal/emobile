package drivers;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.StarMicronics.jasura.JAException;
import com.android.dao.AssignEmployeeDAO;
import com.android.dao.ClerkDAO;
import com.android.dao.ShiftDAO;
import com.android.dao.ShiftExpensesDAO;
import com.android.dao.StoredPaymentsDAO;
import com.android.dao.TermsNConditionsDAO;
import com.android.database.CustomersHandler;
import com.android.database.InvProdHandler;
import com.android.database.InvoicesHandler;
import com.android.database.MemoTextHandler;
import com.android.database.OrderProductsHandler;
import com.android.database.OrderTaxes_DB;
import com.android.database.OrdersHandler;
import com.android.database.PayMethodsHandler;
import com.android.database.PaymentsHandler;
import com.android.database.ProductsHandler;
import com.android.emobilepos.BuildConfig;
import com.android.emobilepos.R;
import com.android.emobilepos.models.ClockInOut;
import com.android.emobilepos.models.DataTaxes;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.PaymentDetails;
import com.android.emobilepos.models.SplittedOrder;
import com.android.emobilepos.models.Tax;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.orders.OrderProduct;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.models.realms.OrderAttributes;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.Shift;
import com.android.emobilepos.models.realms.ShiftExpense;
import com.android.emobilepos.models.realms.TermsNConditions;
import com.android.emobilepos.payment.ProcessGenius_FA;
import com.android.support.ConsignmentTransaction;
import com.android.support.Customer;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.TaxesCalculator;
import com.crashlytics.android.Crashlytics;
import com.elo.device.peripherals.Printer;
import com.miurasystems.miuralibrary.api.executor.MiuraManager;
import com.miurasystems.miuralibrary.api.listener.MiuraDefaultListener;
import com.mpowa.android.sdk.powapos.PowaPOS;
import com.partner.pt100.printer.PrinterApiContext;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.starioextension.ICommandBuilder;
import com.starmicronics.starioextension.StarIoExt;
import com.thefactoryhka.android.pa.TfhkaAndroid;
import com.uniquesecure.meposconnect.MePOS;
import com.uniquesecure.meposconnect.MePOSConnectionType;
import com.uniquesecure.meposconnect.MePOSException;
import com.uniquesecure.meposconnect.MePOSPrinterCallback;
import com.uniquesecure.meposconnect.MePOSReceipt;
import com.uniquesecure.meposconnect.MePOSReceiptImageLine;
import com.uniquesecure.meposconnect.MePOSReceiptLine;
import com.uniquesecure.meposconnect.MePOSReceiptTextLine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.Normalizer;
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
import java.util.StringTokenizer;

import POSSDK.POSSDK;
import datamaxoneil.connection.Connection_Bluetooth;
import datamaxoneil.printer.DocumentLP;
import drivers.elo.utils.PrinterAPI;
import drivers.star.utils.Communication;
import drivers.star.utils.MiniPrinterFunctions;
import drivers.star.utils.PrinterFunctions;
import drivers.star.utils.sdk31.starprntsdk.PrinterSetting;
import jpos.JposException;
import jpos.POSPrinter;
import jpos.POSPrinterConst;
import main.EMSDeviceManager;
import plaintext.EMSPlainTextHelper;
import util.StringUtil;


public class EMSDeviceDriver {
    private static final boolean PRINT_TO_LOG = BuildConfig.PRINT_TO_LOG;
    static PrinterApiContext printerApi;
    static Object printerTFHKA;
    private static int PAPER_WIDTH;
    protected final String FORMAT = "windows-1252";
    private final int ALIGN_LEFT = 0, ALIGN_CENTER = 1;
    protected EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
    protected List<String> printPref;
    protected MyPreferences myPref;
    protected Context activity;
    protected StarIOPort port;
    protected String encodedSignature;
    protected boolean isPOSPrinter = false;
    protected String encodedQRCode = "";
    protected Connection_Bluetooth device;
    byte[] enableCenter, disableCenter;
    PowaPOS powaPOS;
    MePOS mePOS;
    POSSDK pos_sdk = null;
    PrinterAPI eloPrinterApi;
    Printer eloPrinterRefresh;
    POSPrinter bixolonPrinter;
    MePOSReceipt mePOSReceipt;
    InputStream inputStream;
    OutputStream outputStream;
    Typeface typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
    private double saveAmount;
    private StarIoExt.Emulation emulation = StarIoExt.Emulation.StarGraphic;

    private static byte[] convertFromListbyteArrayTobyteArray(List<byte[]> ByteArray) {
        int dataLength = 0;
        for (int i = 0; i < ByteArray.size(); i++) {
            dataLength += ByteArray.get(i).length;
        }

        int distPosition = 0;
        byte[] byteArray = new byte[dataLength];
        for (int i = 0; i < ByteArray.size(); i++) {
            System.arraycopy(ByteArray.get(i), 0, byteArray, distPosition, ByteArray.get(i).length);
            distPosition += ByteArray.get(i).length;
        }

        return byteArray;
    }

    public static Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        int margins = Double.valueOf(PAPER_WIDTH * .10).intValue();
        float ratio = Integer.valueOf(PAPER_WIDTH - margins).floatValue() / Integer.valueOf(b.getWidth()).floatValue();
        int width = Math.round(ratio * b.getWidth());
        int height = Math.round(ratio * b.getHeight());
        b = Bitmap.createScaledBitmap(b, width, height, true);
        return b;
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());

        return Bitmap.createScaledBitmap(realImage, width, height, filter);
    }

    public void connect(Context activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
    }

    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String portName, String portNumber) {
        return false;
    }

    public void registerAll() {
    }

    public void setPaperWidth(int lineWidth) {
        if (this instanceof EMSBluetoothStarPrinter) {
            switch (lineWidth) {
                case 32:
                    PAPER_WIDTH = 408;
                    break;
                case 44:
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
                case 42:
                    PAPER_WIDTH = 576;
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

    private void addTotalLines(Context context, Order anOrder, List<OrderProduct> orderProducts, StringBuilder sb, int lineWidth) {
        BigDecimal itemDiscTotal = new BigDecimal(0);
        for (OrderProduct orderProduct : orderProducts) {
            try {
                itemDiscTotal = itemDiscTotal.add(Global.getBigDecimalNum(orderProduct.getDiscount_value()));
            } catch (NumberFormatException e) {
                itemDiscTotal = new BigDecimal(0);
            }
        }
        saveAmount = itemDiscTotal.add(anOrder.ord_discount.isEmpty() ? new BigDecimal(0) : new BigDecimal(anOrder.ord_discount)).doubleValue();
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(context.getString(R.string.receipt_subtotal),
                Global.getCurrencyFormat(Global.getBigDecimalNum(anOrder.ord_subtotal).add(itemDiscTotal).toString()), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(context.getString(R.string.receipt_discount_line_item),
                Global.getCurrencyFormat(String.valueOf(itemDiscTotal)), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(context.getString(R.string.receipt_global_discount),
                Global.getCurrencyFormat(anOrder.ord_discount), lineWidth, 0));

        sb.append(textHandler.twoColumnLineWithLeftAlignedText(context.getString(R.string.receipt_tax),
                Global.getCurrencyFormat(anOrder.ord_taxamount), lineWidth, 0));
    }

    private void addTaxesLine(List<DataTaxes> taxes, Order order, int lineWidth, StringBuilder sb) {
        if (myPref.isRetailTaxes()) {
            HashMap<String, String[]> prodTaxes = new HashMap<>();
            for (OrderProduct product : order.getOrderProducts()) {
                if(product.getTaxes()!=null) {
                    for (Tax tax : product.getTaxes()) {
                        if (prodTaxes.containsKey(tax.getTaxRate())) {
                            BigDecimal taxAmount = new BigDecimal(prodTaxes.get(tax.getTaxRate())[1]);
                            taxAmount = taxAmount.add(TaxesCalculator.taxRounder(tax.getTaxAmount()));
                            String[] arr = new String[2];
                            arr[0] = tax.getTaxName();
                            arr[1] = String.valueOf(taxAmount);
                            prodTaxes.put(tax.getTaxRate(), arr);
                        } else {
                            BigDecimal taxAmount = TaxesCalculator.taxRounder(tax.getTaxAmount());
                            String[] arr = new String[2];
                            arr[0] = tax.getTaxName();
                            arr[1] = String.valueOf(taxAmount);
                            prodTaxes.put(tax.getTaxRate(), arr);
                        }
                    }
                }
            }
            Iterator it = prodTaxes.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String[]> pair = (Map.Entry<String, String[]>) it.next();

                sb.append(textHandler.twoColumnLineWithLeftAlignedText(pair.getValue()[0],
                        Global.getCurrencyFormat(String.valueOf(pair.getValue()[1])), lineWidth, 2));
                it.remove();
            }


        } else if (taxes != null) {
            for (DataTaxes tax : taxes) {
                BigDecimal taxAmount = new BigDecimal(0);
                List<BigDecimal> rates = new ArrayList<>();
                rates.add(new BigDecimal(tax.getTax_rate()));
                for (OrderProduct product : order.getOrderProducts()) {
                    taxAmount = taxAmount.add(TaxesCalculator.calculateTax(product.getProductPriceTaxableAmountCalculated(), rates));
                }
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(tax.getTax_name(),
                        Global.getCurrencyFormat(String.valueOf(taxAmount)), lineWidth, 2));
            }
        }
    }

    protected void releasePrinter() {
        if (this instanceof EMSBluetoothStarPrinter) {
            if (port != null) {
                try {
                    StarIOPort.releasePort(port);
                    port = null;
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
        } else if (this instanceof EMSOneil4te) {
            if (device != null && device.getIsOpen())
                device.close();
        }
    }

    protected boolean SendCmd(String cmd) {
        Log.d("BixolonCMD", cmd);
        if (printerTFHKA instanceof TfhkaAndroid) {
            return ((TfhkaAndroid) printerTFHKA).SendCmd(cmd);
        } else {
            return ((com.thefactoryhka.android.rd.TfhkaAndroid) printerTFHKA).SendCmd(cmd);
        }
    }

    protected void print(String str) {
        str = removeAccents(str);
        if (PRINT_TO_LOG) {
            Log.d("Print", str);
            return;
        }
        if (this instanceof EMSBixolonRD) {
            String[] split = str.split(("\n"));
            for (String line : split) {
                SendCmd(String.format("80*%s", line));
            }
        } else if (this instanceof EMSELO) {
            if (eloPrinterRefresh != null) {
                eloPrinterRefresh.print(str);
            } else
                eloPrinterApi.print(str);
        } else if (this instanceof EMSMiura) {
            String[] split = str.split(("\n"));
            for (String line : split) {
                String format = String.format("\u0002reset\u0003%s\u0002regular\u0003", line);
                MiuraManager.getInstance().spoolText(format, new MiuraDefaultListener() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        Toast.makeText(activity, "Miura printing error.", Toast.LENGTH_LONG).show();
                    }
                });
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } else if (this instanceof EMSmePOS) {
            mePOSReceipt.addLine(new MePOSReceiptTextLine(str, MePOS.TEXT_STYLE_NONE, MePOS.TEXT_SIZE_NORMAL, MePOS.TEXT_POSITION_LEFT));
        } else if (this instanceof EMSBluetoothStarPrinter) {
            try {
                printStar(str, false);
//                port.writePort(str.getBytes(), 0, str.length());
            } catch (StarIOPortException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
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
                e.printStackTrace();
            }
        } else if (this instanceof EMSOneil4te) {
            device.write(str);
        } else if (this instanceof EMSBixolon) {
            try {
                bixolonPrinter.open(myPref.getPrinterName());
                bixolonPrinter.claim(10000);
                bixolonPrinter.setDeviceEnabled(true);
                bixolonPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, str);
            } catch (JposException e) {
                e.printStackTrace();
            } finally {
                try {
                    bixolonPrinter.close();
                } catch (JposException e) {
                    e.printStackTrace();
                }
            }
        } else if (this instanceof EMSPowaPOS) {
            powaPOS.printText(str);
        } else if (this instanceof EMSsnbc) {
            byte[] send_buf;
            try {
                send_buf = str.getBytes("GB18030");
                pos_sdk.textPrint(send_buf, send_buf.length);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    protected void print(byte[] byteArray) {
        if (PRINT_TO_LOG) {
            Log.d("Print", new String(byteArray));
            return;
        }
        if (this instanceof EMSBixolonRD) {
            String[] split = new String(byteArray).split(("\n"));
            for (String line : split) {
                SendCmd(String.format("80*%s", line));
            }
        } else if (this instanceof EMSELO) {
            if (eloPrinterRefresh != null) {
                eloPrinterRefresh.print(new String(byteArray));
            } else {
                eloPrinterApi.print(new String(byteArray));
            }
        } else if (this instanceof EMSMiura) {
            print(new String(byteArray));
        } else if (this instanceof EMSmePOS) {
            mePOSReceipt.addLine(new MePOSReceiptTextLine(new String(byteArray), MePOS.TEXT_STYLE_NONE, MePOS.TEXT_SIZE_NORMAL, MePOS.TEXT_POSITION_LEFT));
        } else if (this instanceof EMSBluetoothStarPrinter) {
            try {
                printStar(new String(byteArray), false);
            } catch (StarIOPortException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else if (this instanceof EMSPAT100) {
            printerApi.printData(byteArray, byteArray.length);
        } else if (this instanceof EMSBlueBambooP25) {
            try {
                outputStream.write(byteArray);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (this instanceof EMSOneil4te) {
            device.write(byteArray);
        } else if (this instanceof EMSPowaPOS) {
            powaPOS.printText(new String(byteArray));
        } else if (this instanceof EMSsnbc) {
            byte[] send_buf;
            try {
                send_buf = new String(byteArray).getBytes("GB18030");
                pos_sdk.textPrint(send_buf, send_buf.length);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else if (this instanceof EMSBixolon) {
            try {
                bixolonPrinter.open(myPref.getPrinterName());
                bixolonPrinter.claim(10000);
                bixolonPrinter.setDeviceEnabled(true);
                bixolonPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, new String(byteArray));
            } catch (JposException e) {
                e.printStackTrace();
            } finally {
                try {
                    bixolonPrinter.close();
                } catch (JposException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String removeAccents(String str) {
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = str.replaceAll("[^\\p{ASCII}]", "");
        return str;
    }

    public void print(String str, String FORMAT) {
        print(str, FORMAT, false);
    }

    public void print(String str, boolean isLargeFont) {
        print(str, FORMAT, isLargeFont);
    }

    private void startReceipt() {
        if (myPref.getPrinterName().toUpperCase().contains("MPOP")) {
            emulation = StarIoExt.Emulation.StarPRNT;
        } else {
            emulation = StarIoExt.Emulation.StarGraphic;
        }
        if (this instanceof EMSmePOS) {
            mePOSReceipt = new MePOSReceipt();
        }
    }

    private void finishReceipt() {
        if (this instanceof EMSmePOS) {
            try {
                int loopCount = 0;
                while (mePOS.printerBusy()) {
                    Thread.sleep(8000);
                    loopCount++;
                    if (loopCount > 6) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            MePOSReceipt receipt = new MePOSReceipt();
            receipt.setCutType(0);

            for (MePOSReceiptLine line : mePOSReceipt.getLines()) {
                if (line instanceof MePOSReceiptTextLine) {
                    String[] split = ((MePOSReceiptTextLine) line).text.split(("\n"));
                    for (String str : split) {
                        MePOSReceiptTextLine textLine = new MePOSReceiptTextLine(str, MePOS.TEXT_STYLE_NONE, MePOS.TEXT_SIZE_NORMAL, MePOS.TEXT_POSITION_LEFT);
                        receipt.addLine(textLine);
                    }
                } else {
                    receipt.addLine(line);
                }
            }

            mePOS.print(receipt, new MePOSPrinterCallback() {
                @Override
                public void onPrinterStarted(MePOSConnectionType mePOSConnectionType, String s) {
                }

                @Override
                public void onPrinterCompleted(MePOSConnectionType mePOSConnectionType, String s) {
                    if (mePOSReceipt != null) {
                        synchronized (mePOSReceipt) {
                            mePOSReceipt.notifyAll();
                        }
                    }
                }

                @Override
                public void onPrinterError(MePOSException e) {
                    if (mePOSReceipt != null) {
                        synchronized (mePOSReceipt) {
                            mePOSReceipt.notifyAll();
                        }
                    }
                }
            });
            synchronized (mePOSReceipt) {
//                try {
//                    mePOSReceipt.wait(12000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
            mePOSReceipt = null;
        }
    }

    private void printStar(String str, boolean isLargeFont) throws StarIOPortException, UnsupportedEncodingException {
        if (port == null) {
            return;
        }
        if (!isPOSPrinter) {
            port.writePort(new byte[]{0x1d, 0x57, (byte) 0x80, 0x31}, 0, 4);
            port.writePort(new byte[]{0x1d, 0x21, 0x00}, 0, 3);
            port.writePort(new byte[]{0x1b, 0x74, 0x11}, 0, 3); // set to
            // windows-1252
            port.writePort(str.getBytes(), 0, str.length());
        } else if (isLargeFont) {
            ArrayList<byte[]> commands = new ArrayList<>();
            commands.add(new byte[]{0x1b, 0x40}); // Initialization
            byte[] characterheightExpansion = new byte[]{0x1b, 0x68, 0x00};
            characterheightExpansion[2] = 49;
            commands.add(characterheightExpansion);
            byte[] characterwidthExpansion = new byte[]{0x1b, 0x57, 0x00};
            characterwidthExpansion[2] = 49;
            commands.add(characterwidthExpansion);
            commands.add(str.getBytes());
            commands.add(new byte[]{0x0a});
            byte[] commandToSendToPrinter = convertFromListbyteArrayTobyteArray(commands);
            port.writePort(commandToSendToPrinter, 0, commandToSendToPrinter.length);
        } else {
            if (myPref.isRasterModePrint()) {
                Bitmap bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(str, 20
                        , PAPER_WIDTH, typeface);
                ICommandBuilder builder = StarIoExt.createCommandBuilder(emulation);
                builder.beginDocument();
                builder.appendBitmap(bitmapFromText, false);
                builder.endDocument();
                byte[] cmds = builder.getCommands();
                port.writePort(cmds, 0, cmds.length);
            } else {
                ArrayList<byte[]> commands = new ArrayList<>();
                commands.add(new byte[]{0x1b, 0x40}); // Initialization
                byte[] characterheightExpansion = new byte[]{0x1b, 0x68, 0x00};
                characterheightExpansion[2] = 48;
                commands.add(characterheightExpansion);
                byte[] characterwidthExpansion = new byte[]{0x1b, 0x57, 0x00};
                characterwidthExpansion[2] = 48;
                commands.add(characterwidthExpansion);
                commands.add(new byte[]{0x0a});
                byte[] commandToSendToPrinter = convertFromListbyteArrayTobyteArray(commands);
                port.writePort(commandToSendToPrinter, 0, commandToSendToPrinter.length);
                port.writePort(str.getBytes(FORMAT), 0, str.length());
            }
        }
    }

    protected void print(String str, String FORMAT, boolean isLargeFont) {
        str = removeAccents(str);
        if (PRINT_TO_LOG) {
            Log.d("Print", str);
            return;
        }
        if (this instanceof EMSBixolonRD) {
            String[] split = str.split(("\n"));
            for (String line : split) {
                if (isLargeFont) {
                    SendCmd(String.format("80>%s", str));
                } else {
                    SendCmd(String.format("80*%s", line));
                }
            }
        } else if (this instanceof EMSELO) {
            if (eloPrinterRefresh != null) {
                eloPrinterRefresh.print(str);
            } else
                eloPrinterApi.print(str);
        } else if (this instanceof EMSMiura) {
            print(str);
        } else if (this instanceof EMSmePOS) {
            mePOSReceipt.addLine(new MePOSReceiptTextLine(str, MePOS.TEXT_STYLE_NONE, MePOS.TEXT_SIZE_NORMAL, MePOS.TEXT_POSITION_LEFT));
        } else if (this instanceof EMSBluetoothStarPrinter) {
            try {
                printStar(str, isLargeFont);
            } catch (StarIOPortException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
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
                e.printStackTrace();
            }
        } else if (this instanceof EMSPowaPOS) {
            powaPOS.printText(str);
        } else if (this instanceof EMSsnbc) {
            print(str);
        } else if (this instanceof EMSBixolon) {
            try {
                bixolonPrinter.open(myPref.getPrinterName());
                bixolonPrinter.claim(10000);
                bixolonPrinter.setDeviceEnabled(true);
                bixolonPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, str);
            } catch (JposException e) {
                e.printStackTrace();
            } finally {
                try {
                    bixolonPrinter.close();
                } catch (JposException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void printEMVSection(EMVContainer emvContainer, int lineWidth) {
        if (emvContainer != null && emvContainer.getGeniusResponse() != null) {
            StringBuilder sb = new StringBuilder();
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

    public void printReceiptPreview(Bitmap bitmap, int lineWidth) throws JAException, StarIOPortException {
        startReceipt();
        setPaperWidth(lineWidth);
        printPref = myPref.getPrintingPreferences();
        printImage(bitmap);
        cutPaper();
    }

    public void printReceiptPreview(SplittedOrder splitedOrder, int lineWidth) throws JAException, StarIOPortException {
        AssignEmployee employee = AssignEmployeeDAO.getAssignEmployee(false);
        startReceipt();
        setPaperWidth(lineWidth);
        printPref = myPref.getPrintingPreferences();
        printImage(0);
        printHeader(lineWidth);
        StringBuilder sb = new StringBuilder();
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.sales_receipt) + ":", splitedOrder.ord_id,
                lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
                DateUtils.getDateAsString(new Date(), "MMM/dd/yyyy"), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee),
                String.format("%s(%s)", employee.getEmpName(), employee.getEmpId()), lineWidth, 0));
        for (OrderProduct product : splitedOrder.getOrderProducts()) {
            BigDecimal qty = Global.getBigDecimalNum(product.getOrdprod_qty());
            sb.append(textHandler.oneColumnLineWithLeftAlignedText(String.format("%sx %s", product.getOrdprod_qty(), product.getOrdprod_name()), lineWidth, 1));
            for (OrderProduct addon : product.addonsProducts) {
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("- " + addon.getOrdprod_name(),
                        Global.getCurrencyFormat(addon.getFinalPrice())
                        , lineWidth, 3));
            }
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price),
                    Global.getCurrencyFormat(product.getFinalPrice()), lineWidth, 3));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_discount),
                    Global.getCurrencyFormat(product.getDiscount_value()), lineWidth, 3));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
                    Global.getCurrencyFormat(Global.getBigDecimalNum(product.getItemTotal())
                            .multiply(qty).toString()), lineWidth, 3));
            sb.append(textHandler.oneColumnLineWithLeftAlignedText(getString(R.string.receipt_description), lineWidth, 3));
            if (product.getOrdprod_desc() != null && !product.getOrdprod_desc().isEmpty()) {
                StringTokenizer tokenizer = new StringTokenizer(product.getOrdprod_desc(), "<br/>");
                sb.append(textHandler.oneColumnLineWithLeftAlignedText(tokenizer.nextToken(), lineWidth, 3));
            } else {
                sb.append(textHandler.oneColumnLineWithLeftAlignedText("", lineWidth, 3));
            }
        }
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.subtotal_receipt),
                Global.getCurrencyFormat(Global.getBigDecimalNum(splitedOrder.ord_subtotal).toString()), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_discount_line_item),
                Global.getCurrencyFormat(Global.getBigDecimalNum(splitedOrder.ord_lineItemDiscount).toString()), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_global_discount),
                Global.getCurrencyFormat(Global.getBigDecimalNum(splitedOrder.ord_discount).toString()), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_tax),
                Global.getCurrencyFormat(Global.getBigDecimalNum(splitedOrder.ord_taxamount).toString()), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_grandtotal),
                Global.getCurrencyFormat(Global.getBigDecimalNum(splitedOrder.gran_total).toString()), lineWidth, 0));
        sb.append(textHandler.newLines(2));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_tip), textHandler.newDivider('_', lineWidth / 2), lineWidth, 0));
        sb.append(textHandler.newLines(2));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total), textHandler.newDivider('_', lineWidth / 2), lineWidth, 0));
        sb.append(textHandler.newLines(2));
        print(sb.toString());
        printFooter(lineWidth);
        cutPaper();
    }

    public String getCustName(String custId) {
        String name = "";
        if (!TextUtils.isEmpty(custId)) {
            CustomersHandler handler = new CustomersHandler(activity);
            Customer customer = handler.getCustomer(custId);
            if (customer != null) {
                if (!TextUtils.isEmpty(customer.getCust_firstName())) {
                    name = String.format("%s %s", StringUtil.nullStringToEmpty(customer.getCust_firstName())
                            , StringUtil.nullStringToEmpty(customer.getCust_lastName()));
                } else if (!TextUtils.isEmpty(customer.getCompanyName())) {
                    name = customer.getCompanyName();
                } else {
                    name = customer.getCust_name();
                }
            }
        }
        return name;
    }

    public String getCustAccount(String custId) {
        String name = "";
        if (!TextUtils.isEmpty(custId)) {
            CustomersHandler handler = new CustomersHandler(activity);
            Customer customer = handler.getCustomer(custId);
            if (customer != null) {
                return customer.getCustAccountNumber();
            }
        }
        return "";
    }

    protected void printReceipt(String ordID, int lineWidth, boolean fromOnHold, Global.OrderType type, boolean isFromHistory, EMVContainer emvContainer) {
        OrdersHandler orderHandler = new OrdersHandler(activity);
        Order anOrder = orderHandler.getPrintedOrder(ordID);
        printReceipt(anOrder, lineWidth, fromOnHold, type, isFromHistory, emvContainer);
    }

    protected void printReceipt(Order anOrder, int lineWidth, boolean fromOnHold, Global.OrderType type, boolean isFromHistory, EMVContainer emvContainer) {
        try {
            AssignEmployee employee = AssignEmployeeDAO.getAssignEmployee(false);
            Clerk clerk = ClerkDAO.getByEmpId(Integer.parseInt(myPref.getClerkID()));
            startReceipt();
            setPaperWidth(lineWidth);
            printPref = myPref.getPrintingPreferences();
            OrderProductsHandler orderProductsHandler = new OrderProductsHandler(activity);
            OrderTaxes_DB ordTaxesDB = new OrderTaxes_DB();
//            Order anOrder = orderHandler.getPrintedOrder(ordID);
            List<DataTaxes> listOrdTaxes = anOrder.getListOrderTaxes();//ordTaxesDB.getOrderTaxes(anOrder.ord_id);
            List<OrderProduct> orderProducts = anOrder.getOrderProducts();//orderProductsHandler.getOrderProducts(ordID);

            boolean payWithLoyalty = false;
            StringBuilder sb = new StringBuilder();
            int size = orderProducts.size();
            printImage(0);
            if (printPref.contains(MyPreferences.print_header))
                printHeader(lineWidth);
            if (anOrder.isVoid.equals("1"))
                sb.append(textHandler.centeredString("*** VOID ***", lineWidth)).append("\n\n");
            print(sb.toString());
            sb.setLength(0);
            print(getOrderTypeDetails(fromOnHold, anOrder, lineWidth, type));

            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
                    Global.formatToDisplayDate(anOrder.ord_timecreated, 3), lineWidth, 0));

            if (ShiftDAO.isShiftOpen() && myPref.isUseClerks()) {
                String clerk_id = anOrder.clerk_id;
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_clerk),
                        clerk.getEmpName() + "(" + clerk_id + ")", lineWidth, 0));
            }
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee),
                    employee.getEmpName() + "(" + employee.getEmpId() + ")", lineWidth, 0));
            String custName = getCustName(anOrder.cust_id);
            if (myPref.isCustSelected()) {
//                CustomersHandler handler = new CustomersHandler(activity);
//                Customer customer = handler.getCustomer(myPref.getCustID());
//                String name = getCustName();
//                if (customer != null) {
//                    custName = String.format("%s %s", customer.getCust_firstName(), customer.getCust_lastName());
//                }
                if (custName != null && !custName.isEmpty()) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer), custName,
                            lineWidth, 0));
                }
            }
            custName = getCustAccount(anOrder.cust_id);
            if (printPref.contains(MyPreferences.print_customer_id) && custName != null && !custName.isEmpty())
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer_id),
                        custName, lineWidth, 0));

            String ordComment = anOrder.ord_comment;
            if (ordComment != null && !ordComment.isEmpty()) {
                sb.append("\n\n");
                sb.append("Comments:\n");
                sb.append(textHandler.oneColumnLineWithLeftAlignedText(ordComment, lineWidth, 3)).append("\n");
            }

//            sb.append("\n\n");

            print(sb.toString());

            sb.setLength(0);
            int totalItemstQty = 0;
            if (!myPref.getPreferences(MyPreferences.pref_wholesale_printout)) {
                boolean isRestMode = myPref.isRestaurantMode();

                for (int i = 0; i < size; i++) {
                    if (!TextUtils.isEmpty(orderProducts.get(i).getProd_price_points()) && Integer.parseInt(orderProducts.get(i).getProd_price_points()) > 0) {
                        payWithLoyalty = true;
                    }
                    totalItemstQty += TextUtils.isEmpty(orderProducts.get(i).getOrdprod_qty()) ? 0 : Double.parseDouble(orderProducts.get(i).getOrdprod_qty());
                    String uomDescription = "";
                    if (!TextUtils.isEmpty(orderProducts.get(i).getUom_name())) {
                        uomDescription = orderProducts.get(i).getUom_name() + "(" + orderProducts.get(i).getUom_conversion() + ")";
                    }
                    if (isRestMode) {
                        if (!orderProducts.get(i).isAddon()) {
                            sb.append(textHandler.oneColumnLineWithLeftAlignedText(
                                    orderProducts.get(i).getOrdprod_qty() + "x " + orderProducts.get(i).getOrdprod_name()
                                            + " "
                                            + uomDescription, lineWidth, 1));
                            if (orderProducts.get(i).getHasAddons()) {
                                List<OrderProduct> addons = orderProductsHandler.getOrderProductAddons(orderProducts.get(i).getOrdprod_id());
                                for (OrderProduct addon : addons) {
                                    if (addon.isAdded()) {
                                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                                " >" + addon.getOrdprod_name(),
                                                Global.getCurrencyFormat(addon.getFinalPrice()), lineWidth, 2));
                                    } else {
                                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                                " >NO " + addon.getOrdprod_name(),
                                                Global.getCurrencyFormat(addon.getFinalPrice()), lineWidth, 2));
                                    }
                                }
                            }

                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price),
                                    Global.getCurrencyFormat(String.valueOf(orderProducts.get(i).getItemTotalCalculated())), lineWidth, 3));
                            if (orderProducts.get(i).getDiscount_id() != null && !orderProducts.get(i).getDiscount_id().isEmpty()) {
                                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_discount),
                                        Global.getCurrencyFormat(orderProducts.get(i).getDiscount_value()), lineWidth, 3));
                            }
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
                                    Global.getCurrencyFormat(orderProducts.get(i).getItemTotal()), lineWidth, 3));

                            if (printPref.contains(MyPreferences.print_descriptions)) {
                                StringTokenizer tokenizer = new StringTokenizer("orderProducts.get(i).getOrdprod_desc()", "<br/>");
                                if (tokenizer.countTokens() > 0) {
                                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                            getString(R.string.receipt_description), "", lineWidth, 3));
                                    sb.append(textHandler.oneColumnLineWithLeftAlignedText(tokenizer.nextElement().toString(), lineWidth, 5));
                                }
                            }
                        }
                    } else {
                        sb.append(textHandler.oneColumnLineWithLeftAlignedText(
                                orderProducts.get(i).getOrdprod_qty() + "x " + orderProducts.get(i).getOrdprod_name()
                                        + " "
                                        + uomDescription, lineWidth, 1));
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_price),
                                Global.getCurrencyFormat(orderProducts.get(i).getFinalPrice()), lineWidth, 3));

                        if (orderProducts.get(i).getDiscount_id() != null && !orderProducts.get(i).getDiscount_id().isEmpty()) {
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_discount),
                                    Global.getCurrencyFormat(orderProducts.get(i).getDiscount_value()), lineWidth, 3));
                        }

                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
                                Global.getCurrencyFormat(orderProducts.get(i).getItemTotal()), lineWidth, 3));

                        if (printPref.contains(MyPreferences.print_descriptions)) {
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                    getString(R.string.receipt_description), "", lineWidth, 3));
                            sb.append(textHandler.oneColumnLineWithLeftAlignedText(orderProducts.get(i).getOrdprod_desc(),
                                    lineWidth, 5));
                        }

                    }
                    print(sb.toString(), FORMAT);
                    sb.setLength(0);
                }
            } else {
                int padding = lineWidth / 4;
                String tempor = Integer.toString(padding);
                StringBuilder tempSB = new StringBuilder();
                tempSB.append("%").append(tempor).append("s").append("%").append(tempor).append("s").append("%")
                        .append(tempor).append("s").append("%").append(tempor).append("s");

                sb.append(String.format(tempSB.toString(), "Item", "Qty", "Price", "Total")).append("\n");

                for (int i = 0; i < size; i++) {
                    if (!TextUtils.isEmpty(orderProducts.get(i).getProd_price_points()) && Integer.parseInt(orderProducts.get(i).getProd_price_points()) > 0) {
                        payWithLoyalty = true;
                    }
                    totalItemstQty += TextUtils.isEmpty(orderProducts.get(i).getOrdprod_qty()) ? 0
                            : Double.parseDouble(orderProducts.get(i).getOrdprod_qty());
                    sb.append(orderProducts.get(i).getOrdprod_name()).append("-").append(orderProducts.get(i).getOrdprod_desc())
                            .append("\n");

                    sb.append(String.format("Discount %s\n", Global.getCurrencyFormat(orderProducts.get(i).getDiscountTotal().toString())));
                    sb.append(String.format(tempSB.toString(), "   ", orderProducts.get(i).getOrdprod_qty(),
                            Global.getCurrencyFormat(orderProducts.get(i).getFinalPrice()),
                            Global.getCurrencyFormat(orderProducts.get(i).getItemTotal()))).append("\n");
                    print(sb.toString(), FORMAT);
                    sb.setLength(0);

                }
            }
            print(sb.toString(), FORMAT);
            sb.setLength(0);

            print(textHandler.lines(lineWidth), FORMAT);
            addTotalLines(this.activity, anOrder, orderProducts, sb, lineWidth);

            addTaxesLine(listOrdTaxes, anOrder, lineWidth, sb);

            sb.append("\n");
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_itemsQtyTotal),
                    String.valueOf(totalItemstQty), lineWidth, 0));
            sb.append("\n");
            String granTotal = (anOrder.gran_total.isEmpty() ? new BigDecimal(0) : new BigDecimal(anOrder.gran_total)).toString();
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_grandtotal),
                    Global.getCurrencyFormat(granTotal), lineWidth, 0));
            sb.append("\n");
            PaymentsHandler payHandler = new PaymentsHandler(activity);
            List<PaymentDetails> detailsList = payHandler.getPaymentForPrintingTransactions(anOrder.ord_id);
            if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward)) {
                detailsList.addAll(StoredPaymentsDAO.getPaymentForPrintingTransactions(anOrder.ord_id));
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
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amountreturned),
                        Global.formatDoubleToCurrency(0.00), lineWidth, 0));
            } else {

                double paidAmount = 0;
                double tempTipAmount = 0;
                double totalAmountTendered = 0;

                StringBuilder tempSB = new StringBuilder();
                for (int i = 0; i < size; i++) {
                    String _pay_type = detailsList.get(i).getPaymethod_name().toUpperCase(Locale.getDefault()).trim();
                    tempAmount = tempAmount + formatStrToDouble(detailsList.get(i).getPay_amount());
                    if (Payment.PaymentType.getPaymentTypeByCode(detailsList.get(i).getPayType()) != Payment.PaymentType.VOID) {
                        paidAmount += formatStrToDouble(detailsList.get(i).getPay_amount());
                    }
                    totalAmountTendered += detailsList.get(i).getAmountTender();
                    tempTipAmount = tempTipAmount + formatStrToDouble(detailsList.get(i).getPay_tip());
                    tempSB.append(textHandler
                            .oneColumnLineWithLeftAlignedText(Global.getCurrencyFormat(detailsList.get(i).getPay_amount())
                                    + "[" + detailsList.get(i).getPaymethod_name() + "]", lineWidth, 1));
                    if (!_pay_type.equals("CASH") && !_pay_type.equals("CHECK")) {
                        tempSB.append(textHandler.oneColumnLineWithLeftAlignedText("TransID: " + StringUtil.nullStringToEmpty(detailsList.get(i).getPay_transid()),
                                lineWidth, 1));
                        tempSB.append(textHandler.oneColumnLineWithLeftAlignedText("CC#: *" + detailsList.get(i).getCcnum_last4(),
                                lineWidth, 1));
                    } else {
                        tempSB.append(textHandler
                                .oneColumnLineWithLeftAlignedText(getString(R.string.amount_tendered) + Global.formatDoubleToCurrency(detailsList.get(i).getAmountTender()), lineWidth, 1));
                        tempSB.append(textHandler
                                .oneColumnLineWithLeftAlignedText(getString(R.string.changeLbl) +
                                        Global.formatDoubleToCurrency(detailsList.get(i).getAmountTender() - formatStrToDouble(detailsList.get(i).getPay_amount())), lineWidth, 1));
                    }
                }
                if (type == Global.OrderType.ORDER) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amountreturned),
                            Global.formatDoubleToCurrency(tempAmount), lineWidth, 0));
                } else {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amountpaid),
                            Global.getCurrencyFormat(Double.toString(paidAmount)), lineWidth, 0));
                }
                sb.append(tempSB.toString());
                if (type == Global.OrderType.INVOICE) // Invoice
                {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_balance_due),
                            Global.formatDoubleToCurrency(tempGrandTotal - tempAmount), lineWidth, 0));
                }
                if (type != Global.OrderType.ORDER) {
                    if (myPref.isRestaurantMode() &&
                            myPref.getPreferences(MyPreferences.pref_enable_togo_eatin)) {
                        if (tempTipAmount == 0) {
                            sb.append("\n");
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total_tip_paid),
                                    textHandler.lines(lineWidth / 2), lineWidth, 0));
                            sb.append("\n");
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
                                    textHandler.lines(lineWidth / 2), lineWidth, 0));
                        } else {
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total_tip_paid),
                                    Global.getCurrencyFormat(Double.toString(tempTipAmount)), lineWidth, 0));
                        }

                    } else {
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total_tip_paid),
                                Global.getCurrencyFormat(Double.toString(tempTipAmount)), lineWidth, 0));
                    }

                    if (type == Global.OrderType.RETURN) {
                        tempAmount = paidAmount;
                    } else if (tempGrandTotal >= totalAmountTendered) {
                        tempAmount = 0.00;
                    } else {
                        if (tempGrandTotal > 0) {
                            tempAmount = totalAmountTendered - tempGrandTotal;
                        } else {
                            tempAmount = Math.abs(tempGrandTotal);
                        }
                    }
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amountreturned),
                            Global.getCurrencyFormat(Double.toString(tempAmount)), lineWidth, 0))
                            .append("\n");
                }

            }
            print(sb.toString(), FORMAT);

            print(textHandler.newLines(1), FORMAT);
            if (type != Global.OrderType.ORDER && saveAmount > 0)
                printYouSave(String.valueOf(saveAmount), lineWidth);
            sb.setLength(0);
            if (Global.isIvuLoto && detailsList.size() > 0) {
                if (!printPref.contains(MyPreferences.print_ivuloto_qr)) {
                    printIVULoto(detailsList.get(0).getIvuLottoNumber(), lineWidth);
                } else {
                    printImage(2);
                    printIVULoto(detailsList.get(0).getIvuLottoNumber(), lineWidth);
                }
                sb.setLength(0);
            }
//            printOrderAttributes(lineWidth, anOrder);
            if (printPref.contains(MyPreferences.print_footer))
                printFooter(lineWidth);

            print(textHandler.newLines(1), FORMAT);
            if (payWithLoyalty && Global.loyaltyCardInfo != null && !TextUtils.isEmpty(Global.loyaltyCardInfo.getCardNumAESEncrypted())
                    && !TextUtils.isEmpty(Global.loyaltyCardInfo.getCardLast4())) {
                print(String.format("%s *%s\n", getString(R.string.receipt_cardnum), Global.loyaltyCardInfo.getCardLast4()));
                print(String.format("%s %s %s\n", getString(R.string.receipt_point_used), Global.loyaltyCharge, getString(R.string.points)));
                print(String.format("%s %s %s\n", getString(R.string.receipt_reward_balance), Global.loyaltyPointsAvailable, getString(R.string.points)));
                print(textHandler.newLines(1), FORMAT);
            }
            if (Global.rewardCardInfo != null && !TextUtils.isEmpty(Global.rewardCardInfo.getCardNumAESEncrypted())
                    && !TextUtils.isEmpty(Global.rewardCardInfo.getCardLast4())) {
                print(String.format("%s *%s\n", getString(R.string.receipt_cardnum), Global.rewardCardInfo.getCardLast4()));
                print(String.format("%s %s %s\n", getString(R.string.receipt_reward_balance), Global.rewardCardInfo.getOriginalTotalAmount(), getString(R.string.points)));
                print(textHandler.newLines(1), FORMAT);
            }
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
            printTermsNConds();
            printEnablerWebSite(lineWidth);
            cutPaper();
        } catch (StarIOPortException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        } catch (JAException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    public String getOrderTypeDetails(boolean fromOnHold, Order anOrder, int lineWidth, Global.OrderType type) {
        StringBuilder sb = new StringBuilder();
        if (fromOnHold) {
            sb.append(textHandler.twoColumnLineWithLeftAlignedText("[" + getString(R.string.on_hold) + "]",
                    anOrder.ord_HoldName, lineWidth, 0));
        }

        switch (type) {
            case ORDER: // Order
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.order) + ":", anOrder.ord_id,
                        lineWidth, 0));
                break;
            case RETURN: // Return
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.return_tag) + ":", anOrder.ord_id,
                        lineWidth, 0));
                break;
            case INVOICE: // Invoice
            case CONSIGNMENT_INVOICE:// Consignment Invoice
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.invoice) + ":", anOrder.ord_id,
                        lineWidth, 0));
                break;
            case ESTIMATE: // Estimate
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.estimate) + ":", anOrder.ord_id,
                        lineWidth, 0));
                break;
            case SALES_RECEIPT: // Sales Receipt
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.sales_receipt) + ":", anOrder.ord_id,
                        lineWidth, 0));
                break;
        }
        return sb.toString();
    }

    private void printOrderAttributes(int lineWidth, Order order) {
        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        for (OrderAttributes attr : order.orderAttributes) {
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(attr.getOrdAttrName(), attr.getInputValue(), lineWidth, 0));
        }
        print(sb.toString());
    }

    private void printIVULoto(String ivuLottoNumber, int lineWidth) {
        print((textHandler.ivuLines(2 * lineWidth / 3) + "\n" + activity.getString(R.string.ivuloto_control_label) + ivuLottoNumber + "\n" + getString(R.string.enabler_prefix) + "\n" + getString(R.string.powered_by_enabler) + "\n" + textHandler.ivuLines(2 * lineWidth / 3)).getBytes());
    }

    private void printEnablerWebSite(int lineWidth) {
        if (myPref.isPrintWebSiteFooterEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.setLength(0);
            sb.append(textHandler.centeredString(getString(R.string.enabler_website) + "\n\n", lineWidth));
            print(sb.toString());
        }
    }

    public void cutPaper() {
        if (PRINT_TO_LOG) {
            Log.d("Cut", "Paper Cut");
            return;
        }
        if (this instanceof EMSBixolonRD) {
            SendCmd(String.format("81*%s", " "));
        } else if (this instanceof EMSsnbc) {
            // ******************************************************************************************
            // print in page mode
            pos_sdk.pageModePrint();
            pos_sdk.systemCutPaper(66, 0);

            // *****************************************************************************************
            // clear buffer in page mode
            pos_sdk.pageModeClearBuffer();
        } else if (this instanceof EMSMiura) {
            print(textHandler.newLines(4));
            MiuraManager.getInstance().setConnectionDelegate((EMSMiura) this);
            MiuraManager.getInstance().spoolPrint(new MiuraDefaultListener() {
                @Override
                public void onSuccess() {
                    Log.d("Print Receipt", "Printed");
                }

                @Override
                public void onError() {
                    Log.d("Print Receipt", "Fail");
                }
            });
        } else if (isPOSPrinter) {
            ICommandBuilder builder = StarIoExt.createCommandBuilder(emulation);
            builder.beginDocument();
            builder.appendCutPaper(ICommandBuilder.CutPaperAction.PartialCutWithFeed);
            builder.endDocument();
            byte[] cmds = builder.getCommands();
            try {
                port.writePort(cmds, 0, cmds.length);
            } catch (StarIOPortException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
//            print(new byte[]{0x1b, 0x64, 0x02}); // Cut
        } else if (this instanceof EMSmePOS) {
            finishReceipt();
        }
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
                    data = PrinterFunctions.createCommandsEnglishRasterModeCoupon(PAPER_WIDTH,
                            ICommandBuilder.BitmapConverterRotation.Normal,
                            myBitmap, emulation);
                    Communication.sendCommands(data, port, this.activity); // 10000mS!!!
                } else {
                    Bitmap bmp = myBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    int w = bmp.getWidth();
                    int h = bmp.getHeight();
                    int pixel;
                    try {
                        for (int x = 0; x < w; x++) {
                            for (int y = 0; y < h; y++) {
                                pixel = bmp.getPixel(x, y);
                                if (pixel == Color.TRANSPARENT)
                                    bmp.setPixel(x, y, Color.WHITE);
                            }
                        }
                        MiniPrinterFunctions.PrintBitmapImage(activity, port.getPortName(), port.getPortSettings(),
                                bmp, PAPER_WIDTH, false, false);
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                    }
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
                        byteArray[i] = arrayList.get(i);
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
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
                powaPOS.printImage(myBitmap);
            } else if (this instanceof EMSmePOS) {
                float ratio = Integer.valueOf(PAPER_WIDTH).floatValue() / Integer.valueOf(myBitmap.getWidth()).floatValue();
                int width = Math.round(ratio * myBitmap.getWidth());
                int height = Math.round(ratio * myBitmap.getHeight());
                Bitmap bmpMonochrome = Bitmap.createBitmap(width / 2, height / 2, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bmpMonochrome);
                ColorMatrix ma = new ColorMatrix();
                ma.setSaturation(0);
                Paint paint = new Paint();
                paint.setColorFilter(new ColorMatrixColorFilter(ma));
                canvas.drawBitmap(myBitmap, 0, 0, paint);
                mePOSReceipt.addLine(new MePOSReceiptImageLine(bmpMonochrome));
            } else if (this instanceof EMSsnbc) {
                int PrinterWidth = 640;
                pos_sdk.textStandardModeAlignment(ALIGN_CENTER);
                pos_sdk.imageStandardModeRasterPrint(myBitmap, PrinterWidth);
                pos_sdk.textStandardModeAlignment(ALIGN_LEFT);
            } else if (this instanceof EMSELO) {
                if (eloPrinterRefresh == null) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    matrix.preScale(1.0f, -1.0f);
                    Bitmap rotatedBmp = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
                    eloPrinterApi.print_image(activity, rotatedBmp);
                }
            } else if (this instanceof EMSMiura) {
//                try {
//                    InputStream inputStream = activity.getAssets().open("image.bmp");
//
//                    int size = inputStream.available();
//                    byte[] buffer = new byte[size];
//                    inputStream.read(buffer);
//                    inputStream.close();
//                    MiuraManager.getInstance().uploadBinary(buffer, "image.bmp", new MiuraDefaultListener() {
//                        @Override
//                        public void onSuccess() {
//                            MiuraManager.getInstance().hardReset(new MiuraDefaultListener() {
//                                @Override
//                                public void onSuccess() {
//                                    MiuraManager.getInstance().spoolImage("image.bmp", new MiuraDefaultListener() {
//                                        @Override
//                                        public void onSuccess() {
////                                            MiuraManager.getInstance().spoolPrint(new MiuraDefaultListener() {
////                                                @Override
////                                                public void onSuccess() {
////                                                    Log.d("Print Image", "Success");
////                                                }
////
////                                                @Override
////                                                public void onError() {
////                                                }
////                                            });
//                                        }
//
//                                        @Override
//                                        public void onError() {
//                                        }
//                                    });
//                                }
//
//                                @Override
//                                public void onError() {
//                                }
//                            });
//                        }
//
//                        @Override
//                        public void onError() {
//                        }
//                    });
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            } else if (this instanceof EMSBixolon) {
                ByteBuffer buffer = ByteBuffer.allocate(4);
                buffer.put((byte) POSPrinterConst.PTR_S_RECEIPT);
                buffer.put((byte) 50);
                buffer.put((byte) 0x00);
                buffer.put((byte) 0x00);
                try {
                    bixolonPrinter.open(myPref.getPrinterName());
                    bixolonPrinter.claim(10000);
                    bixolonPrinter.setDeviceEnabled(true);
                    bixolonPrinter.printBitmap(buffer.getInt(0), myBitmap,
                            bixolonPrinter.getRecLineWidth(), POSPrinterConst.PTR_BM_LEFT);
                } catch (JposException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        bixolonPrinter.close();
                    } catch (JposException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }

    }

    protected void printImage(Bitmap bitmap) throws StarIOPortException, JAException {
        if (PRINT_TO_LOG) {
            Log.d("Print", "*******Image Print***********");
            return;
        }
        if (bitmap != null) {

            if (this instanceof EMSBluetoothStarPrinter) {
                PrinterSetting setting = new PrinterSetting(activity);
                StarIoExt.Emulation emulation = setting.getEmulation();
                byte[] data;

                if (isPOSPrinter) {
                    data = drivers.star.utils.sdk31.starprntsdk.functions.PrinterFunctions
                            .createRasterData(emulation, bitmap, PAPER_WIDTH, true);

//                    data = PrinterFunctions.createCommandsEnglishRasterModeCoupon(PAPER_WIDTH, SCBBitmapConverter.Rotation.Normal,
//                            bitmap);
                    Communication.sendCommands(data, port, this.activity); // 10000mS!!!

                } else {
                    Bitmap bmp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
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
                printerApi.printImage(bitmap, 0);
            } else if (this instanceof EMSmePOS) {
                mePOSReceipt.addLine(new MePOSReceiptImageLine(bitmap));
            } else if (this instanceof EMSBlueBambooP25) {
                EMSBambooImageLoader loader = new EMSBambooImageLoader();
                ArrayList<ArrayList<Byte>> arrayListList = loader.bambooDataWithAlignment(0, bitmap);
                for (ArrayList<Byte> arrayList : arrayListList) {
                    byte[] byteArray = new byte[arrayList.size()];
                    int size = arrayList.size();
                    for (int i = 0; i < size; i++) {
                        byteArray[i] = arrayList.get(i);
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    print(byteArray);
                }
            } else if (this instanceof EMSOneil4te) {
                // print image
                DocumentLP documentLP = new DocumentLP("$");
                documentLP.clear();
                documentLP.writeImage(bitmap, 832);
                device.write(documentLP.getDocumentData());
            } else if (this instanceof EMSPowaPOS) {
                powaPOS.printImage(bitmap);
            } else if (this instanceof EMSsnbc) {
                int PrinterWidth = 640;
                // download bitmap
                pos_sdk.textStandardModeAlignment(ALIGN_CENTER);
                pos_sdk.imageStandardModeRasterPrint(bitmap, PrinterWidth);
                pos_sdk.textStandardModeAlignment(ALIGN_LEFT);
            } else if (this instanceof EMSELO) {
                if (eloPrinterRefresh == null) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    matrix.preScale(1.0f, -1.0f);
                    Bitmap rotatedBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    eloPrinterApi.print_image(activity, rotatedBmp);
                }
                print("\n\n\n\n");

            } else if (this instanceof EMSBixolon) {
                ByteBuffer buffer = ByteBuffer.allocate(4);
                buffer.put((byte) POSPrinterConst.PTR_S_RECEIPT);
                buffer.put((byte) 50);
                buffer.put((byte) 1);
                buffer.put((byte) 0x00);
                try {
                    bixolonPrinter.open(myPref.getPrinterName());
                    bixolonPrinter.claim(10000);
                    bixolonPrinter.setDeviceEnabled(true);
                    bixolonPrinter.printBitmap(buffer.getInt(0), bitmap,
                            bixolonPrinter.getRecLineWidth(), POSPrinterConst.PTR_BM_CENTER);
                } catch (JposException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        bixolonPrinter.close();
                    } catch (JposException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        matrix.preScale(1.0f, -1.0f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
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

    private void printYouSave(String saveAmount, int lineWidth) {
        EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
        StringBuilder sb = new StringBuilder(saveAmount);

        print(textHandler.ivuLines(lineWidth), FORMAT);
        sb.setLength(0);
        sb.append(textHandler.newLines(1));

        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_youSave),
                Global.getCurrencyFormat(saveAmount), lineWidth, 0));

        sb.append(textHandler.newLines(1));
        print(sb.toString());
        print(textHandler.ivuLines(lineWidth), FORMAT);

    }

    public void printTermsNConds() {
        printPref = myPref.getPrintingPreferences();
        if (printPref.contains(MyPreferences.print_terms_conditions)) {
            List<TermsNConditions> termsNConditions = TermsNConditionsDAO.getTermsNConds();
            if (termsNConditions != null) {
                for (TermsNConditions terms : termsNConditions) {
                    print(terms.getTcTerm());
                }
                print("\n");
            }
        }
    }

    public void printClockInOut(List<ClockInOut> timeClocks, int lineWidth, String clerkID) {
        EMSPlainTextHelper textHelper = new EMSPlainTextHelper();
        Clerk clerk = ClerkDAO.getByEmpId(Integer.parseInt(clerkID));
        StringBuilder str = new StringBuilder();
        str.append(textHelper.centeredString(activity.getString(R.string.clockReceipt), lineWidth));
        str.append(textHelper.newLines(1));
        str.append(textHelper.twoColumnLineWithLeftAlignedText(DateUtils.getDateAsString(new Date(), DateUtils.DATE_yyyy_MM_dd_h_mm_a),
                String.format("%s%s", activity.getString(R.string.receipt_employee), clerk.getEmpName()), lineWidth, 0));
        str.append(textHelper.newDivider('-', lineWidth));
        str.append(textHelper.fourColumnLineWithLeftAlignedTextPercentWidth(activity.getString(R.string.date), 25,
                activity.getString(R.string.clock_in), 25, activity.getString(R.string.clock_out), 25, activity.getString(R.string.hours), 25,
                lineWidth, 0));
        int minsTotal = 0;
        for (ClockInOut clock : timeClocks) {
            Date in = DateUtils.getDateStringAsDate(clock.getClockIn(), DateUtils.DATE_PATTERN);
            Date out = null;
            int hours = 0;
            int mins = 0;
            if (clock.getClockOut() != null) {
                out = DateUtils.getDateStringAsDate(clock.getClockOut(), DateUtils.DATE_PATTERN);
                hours = clock.getMinutesPeriod() / 60;
                mins = clock.getMinutesPeriod() - (hours * 60);
            }
            str.append(textHelper.fourColumnLineWithLeftAlignedTextPercentWidth(DateUtils.getDateAsString(in, DateUtils.DATE_MM_DD), 25,
                    DateUtils.getDateAsString(in, DateUtils.DATE_h_mm_a), 25,
                    DateUtils.getDateAsString(out, DateUtils.DATE_h_mm_a), 25, String.format(Locale.getDefault(), "%d.%d", hours, mins), 25,
                    lineWidth, 0));
            minsTotal += clock.getMinutesPeriod();
        }
        str.append(textHelper.newLines(1));
        int hours = minsTotal / 60;
        int mins = minsTotal - (hours * 60);
        str.append(textHelper.centeredString(String.format(Locale.getDefault(), "%s  %d.%d",
                activity.getString(R.string.total_hours_worked), hours, mins), lineWidth));
        str.append(textHelper.newLines(4));
        print(str.toString());
        cutPaper();
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

    private double formatStrToDouble(String val) {
        if (val == null || val.isEmpty())
            return 0.00;
        return Double.parseDouble(val);
    }

    protected String getString(int id) {
        return (activity.getResources().getString(id));
    }

    protected boolean printBalanceInquiry(HashMap<String, String> values, int lineWidth) {
        try {
            startReceipt();
            printPref = myPref.getPrintingPreferences();
            StringBuilder sb = new StringBuilder();
            printImage(0);
            if (myPref.isCustSelected()) {
                sb.append(textHandler.centeredString(myPref.getCustName(), lineWidth));
            }
            if (printPref.contains(MyPreferences.print_header))
                printHeader(lineWidth);
            if (values.containsKey("amountAdded")) {
                sb.append("* ").append(getString(R.string.add_balance));
            } else {
                sb.append("* ").append(getString(R.string.balance_inquiry));
            }
            sb.append(" *\n");
            print(textHandler.centeredString(sb.toString(), lineWidth), FORMAT);
            sb.setLength(0);
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
                    getString(R.string.receipt_time), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(DateUtils.getDateAsString(new Date(), "MMM/dd/yyyy"), DateUtils.getDateAsString(new Date(), "hh:mm:ss"), lineWidth, 0))
                    .append("\n");

            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.card_number),
                    "*" + values.get("pay_maccount"), lineWidth, 0));
            if (values.containsKey("amountAdded")) {
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.status),
                        values.get("epayStatusCode"), lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.amount_added),
                        values.get("amountAdded"), lineWidth, 0));
            }

            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.balanceAmount),
                    Global.getCurrencyFormat(values.get("CardBalance")), lineWidth, 0));

            print(sb.toString());

            sb.setLength(0);
            printFooter(lineWidth);
            sb.append("\n");
            print(sb.toString(), FORMAT);
            sb.setLength(0);
            printEnablerWebSite(lineWidth);
            cutPaper();
        } catch (StarIOPortException ignored) {

        } catch (JAException e) {
            e.printStackTrace();
        }
        return true;
    }

    void printPaymentDetailsReceipt(String payID, int type, boolean isReprint, int lineWidth, EMVContainer emvContainer) {
        try {
            startReceipt();
            EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
            printPref = myPref.getPrintingPreferences();
            PaymentsHandler payHandler = new PaymentsHandler(activity);
            Spanned fromHtml = null;
            if (emvContainer != null && emvContainer.getHandpointResponse() != null && emvContainer.getHandpointResponse().getCustomerReceipt() != null) {
                fromHtml = Html.fromHtml(emvContainer.getHandpointResponse().getCustomerReceipt());
            }
            PaymentDetails payArray;
            boolean isStoredFwd = false;
            long pay_count = payHandler.paymentExist(payID, true);
            if (pay_count == 0) {
                isStoredFwd = true;
                if (emvContainer != null && emvContainer.getGeniusResponse() != null && emvContainer.getGeniusResponse().getStatus().equalsIgnoreCase("DECLINED")) {
                    type = 2;
                }
                payArray = StoredPaymentsDAO.getPrintingForPaymentDetails(payID, type);
            } else {
                payArray = payHandler.getPrintingForPaymentDetails(payID, type);
            }
            StringBuilder sb = new StringBuilder();
            boolean isCashPayment = false;
            boolean isCheckPayment = false;
            String includedTip = null;
            String creditCardFooting = "";
            if (payArray.getPaymethod_name() != null && payArray.getPaymethod_name().toUpperCase(Locale.getDefault()).trim().equals("CASH"))
                isCashPayment = true;
            else if (payArray.getPaymethod_name() != null && payArray.getPaymethod_name().toUpperCase(Locale.getDefault()).trim().equals("CHECK"))
                isCheckPayment = true;
            else {
                includedTip = getString(R.string.receipt_included_tip);
                creditCardFooting = getString(R.string.receipt_creditcard_terms);
            }
            printImage(0);

            if (fromHtml == null) {
                if (printPref.contains(MyPreferences.print_header))
                    printHeader(lineWidth);
                sb.append("* ").append(payArray.getPaymethod_name());
                if (payArray.getIs_refund() != null && payArray.getIs_refund().equals("1"))
                    sb.append(" Refund *\n");
                else
                    sb.append(" Sale *\n");
                print(textHandler.centeredString(sb.toString(), lineWidth), FORMAT);

                sb.setLength(0);
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
                        getString(R.string.receipt_time), lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(payArray.getPay_date(), payArray.getPay_timecreated(), lineWidth, 0))
                        .append("\n");

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
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("TransID:", payArray.getPay_transid(), lineWidth, 0)).append("\n");
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
                            Global.getCurrencyFormat(String.valueOf(Global.subtotalAmount)), lineWidth, 0));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(payArray.getTax1_name(),
                            Global.getCurrencyFormat(payArray.getTax1_amount()), lineWidth, 2));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(payArray.getTax2_name(),
                            Global.getCurrencyFormat(payArray.getTax2_amount()), lineWidth, 2));
                }

                if (emvContainer != null && emvContainer.getGeniusResponse() != null && emvContainer.getGeniusResponse().getAmountApproved() != null) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amount),
                            Global.getCurrencyFormat(emvContainer.getGeniusResponse().getAmountApproved()), lineWidth, 0));
                } else {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_amount),
                            Global.getCurrencyFormat(payArray.getPay_amount()), lineWidth, 0));
                }
                String change = payArray.getChange();
                if (isCashPayment && isCheckPayment && !change.isEmpty() && change.contains(".")
                        && Double.parseDouble(change) > 0) {
                    change = "";
                }
                sb.append("\n");
                print(sb.toString(), FORMAT);
                sb.setLength(0);
                if (includedTip != null) {
                    if (Double.parseDouble(change) > 0) {
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(includedTip,
                                Global.getCurrencyFormat(change), lineWidth, 0));
                    } else if (myPref.isRestaurantMode() &&
                            myPref.getPreferences(MyPreferences.pref_enable_togo_eatin)) {
                        print(textHandler.newLines(1), FORMAT);
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_tip),
                                textHandler.lines(lineWidth / 2), lineWidth, 0))
                                .append("\n");
                        print(sb.toString(), FORMAT);
                        sb.setLength(0);
                        print(textHandler.newLines(1), FORMAT);
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
                                textHandler.lines(lineWidth / 2), lineWidth, 0))
                                .append("\n");
                        print(sb.toString(), FORMAT);
                        sb.setLength(0);
                    }
                }
                sb.append("\n");
                print(sb.toString(), FORMAT);
            } else {
                sb.setLength(0);
                sb.append("\n\n");
                sb.append(fromHtml.toString());
                print(sb.toString(), FORMAT);
            }
            sb.setLength(0);
            if (!isCashPayment && !isCheckPayment) {
                if (myPref.getPreferences(MyPreferences.pref_handwritten_signature)) {
                    sb.append(textHandler.newLines(1));
                } else if (payArray.getPay_signature() != null && !payArray.getPay_signature().isEmpty()) {
                    encodedSignature = payArray.getPay_signature();
                    printImage(1);
                }
                sb.append("\n\nx").append(textHandler.lines(lineWidth / 2)).append("\n");
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
//            sb.append("\n");
//            print(sb.toString(), FORMAT);
            sb.setLength(0);
            printFooter(lineWidth);
//            sb.append("\n");
//            print(sb.toString(), FORMAT);
//            sb.setLength(0);

            if (fromHtml == null) {
                if (!isCashPayment && !isCheckPayment) {
                    print(creditCardFooting, FORMAT);
                    String temp = textHandler.newLines(1);
                    print(temp, FORMAT);
                }
            }

            sb.setLength(0);
            if (isReprint) {
                sb.append(textHandler.centeredString("*** Copy ***", lineWidth));
                print(sb.toString(), FORMAT);
            }
            printTermsNConds();
            printEnablerWebSite(lineWidth);
            cutPaper();
        } catch (StarIOPortException ignored) {

        } catch (JAException e) {
            e.printStackTrace();
        }
    }

    String printStationPrinterReceipt(List<Orders> orders, String ordID, int lineWidth, boolean cutPaper, boolean printheader) {
        try {
            AssignEmployee employee = AssignEmployeeDAO.getAssignEmployee(false);
            setPaperWidth(lineWidth);
            EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
            OrdersHandler orderHandler = new OrdersHandler(activity);
            OrderProductsHandler ordProdHandler = new OrderProductsHandler(activity);
            Order anOrder = orderHandler.getPrintedOrder(ordID);
            StringBuilder sb = new StringBuilder();
            int size = orders.size();
            if (printheader) {
                if (!anOrder.ord_HoldName.isEmpty())
                    sb.append(getString(R.string.receipt_name)).append(anOrder.ord_HoldName).append("\n");
                if (!anOrder.cust_name.isEmpty())
                    sb.append(anOrder.cust_name).append("\n");
                sb.append(getString(R.string.order)).append(": ").append(ordID).append("\n");
                sb.append(getString(R.string.receipt_started)).append(" ")
                        .append(Global.formatToDisplayDate(anOrder.ord_timecreated, -1)).append("\n");

                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                sdf1.setTimeZone(Calendar.getInstance().getTimeZone());
                Date startedDate = sdf1.parse(anOrder.ord_timecreated);
                Date sentDate = new Date();

                sb.append(getString(R.string.receipt_sent_by)).append(" ").append(employee.getEmpName()).append(" (");

                if (((float) (sentDate.getTime() - startedDate.getTime()) / 1000) > 60)
                    sb.append(Global.formatToDisplayDate(sdf1.format(sentDate.getTime()), -1)).append(")");
                else
                    sb.append(Global.formatToDisplayDate(anOrder.ord_timecreated, -1)).append(")");

                String ordComment = anOrder.ord_comment;
                if (ordComment != null && !ordComment.isEmpty()) {
                    sb.append("\nComments:\n");
                    sb.append(textHandler.oneColumnLineWithLeftAlignedText(ordComment, lineWidth, 3));
                }
                sb.append("\n");
                sb.append(textHandler.newDivider('=', lineWidth / 2)); //add double line divider
                sb.append("\n");
            }

            int m;
            for (int i = 0; i < size; i++) {
                if (orders.get(i).hasAddon()) {
                    m = i;
                    ordProdHandler.updateIsPrinted(orders.get(m).getOrdprodID());
                    sb.append(orders.get(m).getQty()).append("x ").append(orders.get(m).getName()).append("\n");
                    if (!orders.get(m).getAttrDesc().isEmpty())
                        sb.append("  [").append(orders.get(m).getAttrDesc()).append("]\n");
                    if ((m + 1) < size && orders.get(m + 1).isAddon()) {
                        for (int j = i + 1; j < size; j++) {
                            ordProdHandler.updateIsPrinted(orders.get(j).getOrdprodID());
                            if (orders.get(j).isAdded())
                                sb.append("  >").append(orders.get(j).getName()).append("\n");
                            else
                                sb.append("  >NO ").append(orders.get(j).getName()).append("\n");

                            if ((j + 1 < size && !orders.get(j + 1).isAddon()) || (j + 1 >= size)) {
                                i = j;
                                break;
                            }
                        }
                    }

                    if (!orders.get(m).getOrderProdComment().isEmpty())
                        sb.append("  ").append(orders.get(m).getOrderProdComment()).append("\n");
                    sb.append(textHandler.newDivider('_', lineWidth / 2)); //add line divider
                    sb.append("\n");

                } else {
                    ordProdHandler.updateIsPrinted(orders.get(i).getOrdprodID());
                    sb.append(orders.get(i).getQty()).append("x ").append(orders.get(i).getName()).append("\n");
                    if (!orders.get(i).getOrderProdComment().isEmpty())
                        sb.append("  ").append(orders.get(i).getOrderProdComment()).append("\n");
                    sb.append(textHandler.newDivider('_', lineWidth / 2)); //add line divider
                    sb.append("\n");

                }
            }
            sb.append(textHandler.newLines(1));
//
            return sb.toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    void printOpenInvoicesReceipt(String invID, int lineWidth) {
        try {
            startReceipt();
            EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
            StringBuilder sb = new StringBuilder();
            String[] rightInfo;
            List<String[]> productInfo;
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
            print(sb.toString(), FORMAT);
            print(textHandler.newLines(1), FORMAT);
            printEnablerWebSite(lineWidth);
            cutPaper();
        } catch (StarIOPortException e) {
            Crashlytics.logException(e);
        } catch (JAException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }
    }

    protected void printConsignmentReceipt(List<ConsignmentTransaction> myConsignment, String encodedSig, int lineWidth) {
        try {
            AssignEmployee employee = AssignEmployeeDAO.getAssignEmployee(false);
            startReceipt();
            encodedSignature = encodedSig;
            printPref = myPref.getPrintingPreferences();
            EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
            StringBuilder sb = new StringBuilder();
            ProductsHandler productDBHandler = new ProductsHandler(activity);
            HashMap<String, String> map;
            double ordTotal = 0, totalSold = 0, totalReturned = 0, totalDispached = 0, totalLines = 0, returnAmount,
                    subtotalAmount;
            int size = myConsignment.size();
            setPaperWidth(lineWidth);
            printImage(0);
            if (printPref.contains(MyPreferences.print_header))
                printHeader(lineWidth);
            sb.append(textHandler.centeredString("Consignment Summary", lineWidth)).append("\n\n");
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer),
                    myPref.getCustName(), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee),
                    employee.getEmpName(), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cons_trans_id),
                    myConsignment.get(0).ConsTrans_ID, lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
                    Global.formatToDisplayDate(DateUtils.getDateAsString(new Date(), DateUtils.DATE_yyyy_MM_ddTHH_mm_ss), 3), lineWidth, 0));
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
                            Global.getCurrencyFormat(map.get("prod_price")), lineWidth, 5));
                    returnAmount = Global.formatNumFromLocale(myConsignment.get(i).ConsReturn_Qty)
                            * Global.formatNumFromLocale(map.get("prod_price"));
                    subtotalAmount = Global.formatNumFromLocale(myConsignment.get(i).invoice_total) + returnAmount;
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Subtotal:",
                            Global.formatDoubleToCurrency(subtotalAmount), lineWidth, 5));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Credit Memo:",
                            Global.formatDoubleToCurrency(returnAmount), lineWidth, 5));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total:",
                            Global.getCurrencyFormat(myConsignment.get(i).invoice_total), lineWidth, 5))
                            .append(textHandler.newLines(1));
                    totalSold += Double.parseDouble(myConsignment.get(i).ConsInvoice_Qty);
                    totalReturned += Double.parseDouble(myConsignment.get(i).ConsReturn_Qty);
                    totalDispached += Double.parseDouble(myConsignment.get(i).ConsDispatch_Qty);
                    totalLines += 1;
                    ordTotal += Double.parseDouble(myConsignment.get(i).invoice_total);
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
            print(sb.toString(), FORMAT);
            if (printPref.contains(MyPreferences.print_descriptions))
                printFooter(lineWidth);
            try {
                printImage(1);
            } catch (StarIOPortException e) {
                e.printStackTrace();
            } catch (JAException e) {
                e.printStackTrace();
            }
            printEnablerWebSite(lineWidth);
            print(textHandler.newLines(1), FORMAT);
            cutPaper();
        } catch (StarIOPortException ignored) {

        } catch (JAException e) {
            e.printStackTrace();
        }
    }

    void printConsignmentHistoryReceipt(HashMap<String, String> map, Cursor c, boolean isPickup, int lineWidth) {
        try {
            AssignEmployee employee = AssignEmployeeDAO.getAssignEmployee(false);
            startReceipt();
            encodedSignature = map.get("encoded_signature");
            printPref = myPref.getPrintingPreferences();
            EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
            StringBuilder sb = new StringBuilder();
            String prodDesc;
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
                    employee.getEmpName(), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cons_trans_id),
                    map.get("ConsTrans_ID"), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
                    Global.formatToDisplayDate(DateUtils.getDateAsString(new Date(), DateUtils.DATE_yyyy_MM_ddTHH_mm_ss), 3), lineWidth, 0));
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
                            Global.getCurrencyFormat(c.getString(c.getColumnIndex("price"))), lineWidth, 5));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Subtotal:",
                            Global.getCurrencyFormat(c.getString(c.getColumnIndex("item_subtotal"))),
                            lineWidth, 5));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Credit Memo:",
                            Global.getCurrencyFormat(c.getString(c.getColumnIndex("credit_memo"))), lineWidth,
                            5));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total:",
                            Global.getCurrencyFormat(c.getString(c.getColumnIndex("item_total"))), lineWidth,
                            5)).append(textHandler.newLines(1));
                } else {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original Qty:",
                            c.getString(c.getColumnIndex("ConsOriginal_Qty")), lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Picked up Qty:",
                            c.getString(c.getColumnIndex("ConsPickup_Qty")), lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("New Qty:",
                            c.getString(c.getColumnIndex("ConsNew_Qty")), lineWidth, 3)).append("\n\n\n");
                }
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
                        Global.getCurrencyFormat(map.get("total_grand_total")), lineWidth, 0));
            }
            sb.append(textHandler.newLines(1));
            print(sb.toString(), FORMAT);
            if (printPref.contains(MyPreferences.print_footer))
                printFooter(lineWidth);
            printImage(1);
            print(textHandler.newLines(3), FORMAT);
            printEnablerWebSite(lineWidth);
            cutPaper();
        } catch (StarIOPortException ignored) {
        } catch (JAException e) {
            e.printStackTrace();
        }
    }

    void printConsignmentPickupReceipt(List<ConsignmentTransaction> myConsignment, String encodedSig, int lineWidth) {
        try {
            AssignEmployee employee = AssignEmployeeDAO.getAssignEmployee(false);
            startReceipt();
            printPref = myPref.getPrintingPreferences();
            EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
            StringBuilder sb = new StringBuilder();
            ProductsHandler productDBHandler = new ProductsHandler(activity);
            HashMap<String, String> map;
            String prodDesc;
            int size = myConsignment.size();
            setPaperWidth(lineWidth);
            printImage(0);
            if (printPref.contains(MyPreferences.print_header))
                printHeader(lineWidth);
            sb.append(textHandler.centeredString("Consignment Pickup Summary", lineWidth)).append("\n\n");
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer),
                    myPref.getCustName(), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee),
                    employee.getEmpName(), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
                    Global.formatToDisplayDate(DateUtils.getDateAsString(new Date(), DateUtils.DATE_yyyy_MM_ddTHH_mm_ss), 3), lineWidth, 0));
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
                print(sb.toString(), FORMAT);
                sb.setLength(0);
            }

            if (printPref.contains(MyPreferences.print_footer))
                printFooter(lineWidth);
            if (!encodedSig.isEmpty()) {
                encodedSignature = encodedSig;
                printImage(1);
                sb.setLength(0);
                sb.append("x").append(textHandler.lines(lineWidth / 2)).append("\n");
                sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(1));
                print(sb.toString(), FORMAT);
                print(textHandler.newLines(1), FORMAT);
            }
            printEnablerWebSite(lineWidth);
            cutPaper();
        } catch (StarIOPortException ignored) {
        } catch (JAException e) {
            e.printStackTrace();
        }
    }

    protected void printEndOfDayReportReceipt(String curDate, int lineWidth, boolean printDetails) {
        AssignEmployee employee = AssignEmployeeDAO.getAssignEmployee(false);
        startReceipt();
        String mDate = Global.formatToDisplayDate(curDate, 4);
        StringBuilder sb = new StringBuilder();
        EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
        StringBuilder sb_ord_types = new StringBuilder();
        OrdersHandler ordHandler = new OrdersHandler(activity);
        OrderProductsHandler ordProdHandler = new OrderProductsHandler(activity);
        PaymentsHandler paymentHandler = new PaymentsHandler(activity);
        boolean showTipField = false;

        //determine if we should include the tip field
        if (myPref.getPreferences(MyPreferences.pref_show_tips_for_cash)) {
            showTipField = true;
        }
        sb.append(textHandler.centeredString("End Of Day Report", lineWidth));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Date", Global.formatToDisplayDate(curDate, 1), lineWidth, 0));
        sb.append(textHandler.newLines(1));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Employee", employee.getEmpName(), lineWidth, 0));
        sb.append(textHandler.newLines(1));
        sb.append(textHandler.centeredString("Summary", lineWidth));
        sb.append(textHandler.newLines(1));
        BigDecimal returnAmount = new BigDecimal("0");
        BigDecimal salesAmount = new BigDecimal("0");
        BigDecimal invoiceAmount = new BigDecimal("0");
        BigDecimal onHoldAmount = new BigDecimal("0");

        sb_ord_types.append(textHandler.centeredString("Totals By Order Types", lineWidth));
        List<Order> listOrder = ordHandler.getOrderDayReport(null, mDate, false);
        List<Order> listOrderHolds = ordHandler.getOrderDayReport(null, mDate, true);
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

            sb_ord_types.append(textHandler.twoColumnLineWithLeftAlignedText("SubTotal", Global.getCurrencyFormat(ord.ord_subtotal), lineWidth, 3));
            sb_ord_types.append(textHandler.twoColumnLineWithLeftAlignedText("Discount Total", Global.getCurrencyFormat(ord.ord_discount), lineWidth, 3));
            sb_ord_types.append(textHandler.twoColumnLineWithLeftAlignedText("Tax Total", Global.getCurrencyFormat(ord.ord_taxamount), lineWidth, 3));
            sb_ord_types.append(textHandler.twoColumnLineWithLeftAlignedText("Net Total", Global.getCurrencyFormat(ord.ord_total), lineWidth, 3));
        }
        if (listOrderHolds != null && !listOrderHolds.isEmpty()) {
            onHoldAmount = new BigDecimal(listOrderHolds.get(0).ord_total);
        }
        print(sb.toString());
        sb.setLength(0);
        listOrder.clear();
        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Return", "(" + Global.getCurrencyFormat(returnAmount.toString()) + ")", lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Sales Receipt", Global.getCurrencyFormat(salesAmount.toString()), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Invoice", Global.getCurrencyFormat(invoiceAmount.toString()), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText("On Holds", Global.getCurrencyFormat(onHoldAmount.toString()), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total", Global.getCurrencyFormat(salesAmount.add(invoiceAmount).subtract(returnAmount).toString()), lineWidth, 0));
        listOrder = ordHandler.getARTransactionsDayReport(null, mDate);
        if (listOrder.size() > 0) {
            sb.append(textHandler.newLines(1));
            sb.append(textHandler.centeredString("A/R Transactions", lineWidth));
            sb.append(textHandler.threeColumnLineItem("ID", 40, "Customer", 40, "Amount", 20, lineWidth, 0));
            for (Order ord : listOrder) {
                if (ord.ord_id != null)
                    sb.append(textHandler.threeColumnLineItem(ord.ord_id, 40, ord.cust_name, 40, Global.getCurrencyFormat(ord.ord_total), 20, lineWidth, 0));
            }
            listOrder.clear();
        }
        print(sb.toString());
        sb.setLength(0);
        List<Shift> listShifts = ShiftDAO.getShift(DateUtils.getDateStringAsDate(curDate, DateUtils.DATE_yyyy_MM_dd));
        if (listShifts.size() > 0) {
            sb.append(textHandler.newLines(1));
            sb.append(textHandler.centeredString("Totals By Shift", lineWidth));
            for (Shift shift : listShifts) {
                Clerk clerk = ClerkDAO.getByEmpId(shift.getClerkId());
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Sales Clerk", clerk.getEmpName(), lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("From", DateUtils.getDateAsString(shift.getStartTime(), DateUtils.DATE_yyyy_MM_dd), lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("To", DateUtils.getDateAsString(shift.getEndTime(), DateUtils.DATE_yyyy_MM_dd), lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.begging_petty_cash), Global.getCurrencyFormat(shift.getBeginningPettyCash()), lineWidth, 3));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.total_expenses), "(" + Global.getCurrencyFormat(shift.getTotalExpenses()) + ")", lineWidth, 3));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.ending_petty_cash), Global.getCurrencyFormat(shift.getEndingPettyCash()), lineWidth, 3));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Transactions Cash", Global.getCurrencyFormat(shift.getTotalTransactionsCash()), lineWidth, 3));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Ending Cash", Global.getCurrencyFormat(shift.getTotal_ending_cash()), lineWidth, 3));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Entered Close Amount", Global.getCurrencyFormat(shift.getEnteredCloseAmount()), lineWidth, 3));
                sb.append(textHandler.newLines(1));
            }
            listShifts.clear();
        }
        print(sb.toString());
        sb.setLength(0);
        sb.append(textHandler.newLines(1));
        sb.append(sb_ord_types);
        List<OrderProduct> listProd = ordProdHandler.getProductsDayReport(true, null, mDate);
        if (listProd.size() > 0) {
            sb.append(textHandler.newLines(1));
            sb.append(textHandler.centeredString("Items Sold", lineWidth));
            sb.append(textHandler.threeColumnLineItem("Name", 60, "Qty", 20, "Total", 20, lineWidth, 0));

            for (OrderProduct prod : listProd) {
                String calc;
                if (new BigDecimal(prod.getOrdprod_qty()).compareTo(new BigDecimal(0)) != 0) {
                    calc = Global.getCurrencyFormat(prod.getItemTotal());//Global.getCurrencyFormat(String.valueOf(new BigDecimal(prod.getItemTotal())
//                            .divide(new BigDecimal(prod.getOrdprod_qty()), 2, RoundingMode.HALF_UP)));
                } else {
                    calc = Global.formatDoubleToCurrency(0);
                }

                sb.append(textHandler.threeColumnLineItem(prod.getOrdprod_name(), 60,
                        prod.getOrdprod_qty(), 20,
                        calc,
                        20, lineWidth, 0));
                if (printDetails) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("UPC:" + prod.getProd_upc(), "", lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("SKU:" + prod.getProd_sku(), "", lineWidth, 3));
                }
            }
            listProd.clear();
        }
        print(sb.toString());
        sb.setLength(0);
        listProd = ordProdHandler.getDepartmentDayReport(true, null, mDate);
        if (listProd.size() > 0) {
            sb.append(textHandler.newLines(1));
            sb.append(textHandler.centeredString("Department Sales", lineWidth));
            sb.append(textHandler.threeColumnLineItem("Name", 60, "Qty", 20, "Total", 20, lineWidth, 0));
            for (OrderProduct prod : listProd) {
                sb.append(textHandler.threeColumnLineItem(prod.getCat_name(), 60, prod.getOrdprod_qty(), 20, Global.getCurrencyFormat(prod.getFinalPrice()), 20, lineWidth, 0));
            }
            listProd.clear();
        }
        print(sb.toString());
        sb.setLength(0);
        listProd = ordProdHandler.getDepartmentDayReport(false, null, mDate);
        if (listProd.size() > 0) {
            sb.append(textHandler.newLines(1));
            sb.append(textHandler.centeredString("Department Returns", lineWidth));
            sb.append(textHandler.threeColumnLineItem("Name", 60, "Qty", 20, "Total", 20, lineWidth, 0));
            for (OrderProduct prod : listProd) {
                sb.append(textHandler.threeColumnLineItem(prod.getCat_name(), 60, prod.getOrdprod_qty(), 20, Global.getCurrencyFormat(prod.getFinalPrice()), 20, lineWidth, 0));
            }
            listProd.clear();
        }
        print(sb.toString());
        sb.setLength(0);
        List<Payment> listPayments = paymentHandler.getPaymentsGroupDayReport(0, null, mDate);
        if (listPayments.size() > 0) {
            sb.append(textHandler.newLines(1));
            sb.append(textHandler.centeredString("Payments", lineWidth));
            for (Payment payment : listPayments) {
                sb.append(textHandler.oneColumnLineWithLeftAlignedText(payment.getCard_type(), lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Amount", Global.getCurrencyFormat(payment.getPay_amount()), lineWidth, 2));
                if (printDetails) {
                    //check if tip should be printed
                    if (showTipField) {
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Tip", Global.getCurrencyFormat(payment.getPay_tip()), lineWidth, 2));
                    }
                    sb.append(textHandler.oneColumnLineWithLeftAlignedText("Details", lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("ID", payment.getPay_id(), lineWidth, 4));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Amount", Global.getCurrencyFormat(payment.getPay_amount()), lineWidth, 4));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Invoice", payment.getJob_id(), lineWidth, 4));
                    sb.append(textHandler.newLines(1));
                    print(sb.toString());
                    sb.setLength(0);
                }
            }
            listPayments.clear();
        }

        listPayments = paymentHandler.getPaymentsGroupDayReport(1, null, mDate);
        if (listPayments.size() > 0) {
            sb.append(textHandler.newLines(1));
            sb.append(textHandler.centeredString("Void", lineWidth));
            for (Payment payment : listPayments) {
                sb.append(textHandler.oneColumnLineWithLeftAlignedText(payment.getCard_type(), lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Amount", Global.getCurrencyFormat(payment.getPay_amount()), lineWidth, 2));
                if (printDetails) {
                    //check if tip should be printed
                    if (showTipField) {
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Tip", Global.getCurrencyFormat(payment.getPay_tip()), lineWidth, 2));
                    }
                    sb.append(textHandler.oneColumnLineWithLeftAlignedText("Details", lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("ID", payment.getPay_id(), lineWidth, 4));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Amount", Global.getCurrencyFormat(payment.getPay_amount()), lineWidth, 4));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Invoice", payment.getJob_id(), lineWidth, 4));
                    sb.append(textHandler.newLines(1));
                    print(sb.toString());
                    sb.setLength(0);
                }
            }
            listPayments.clear();
        }

        listPayments = paymentHandler.getPaymentsGroupDayReport(2, null, mDate);
        if (listPayments.size() > 0) {
            sb.append(textHandler.newLines(1));
            sb.append(textHandler.centeredString("Refund", lineWidth));
            for (Payment payment : listPayments) {
                sb.append(textHandler.oneColumnLineWithLeftAlignedText(payment.getCard_type(), lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Amount", Global.getCurrencyFormat(payment.getPay_amount()), lineWidth, 2));

                if (printDetails) {
                    //check if tip should be printed
                    if (showTipField) {
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Tip", Global.getCurrencyFormat(payment.getPay_tip()), lineWidth, 2));
                    }
                    sb.append(textHandler.oneColumnLineWithLeftAlignedText("Details", lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("ID", payment.getPay_id(), lineWidth, 4));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Amount", Global.getCurrencyFormat(payment.getPay_amount()), lineWidth, 4));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Invoice", payment.getJob_id(), lineWidth, 4));
                    sb.append(textHandler.newLines(1));
                    print(sb.toString());
                    sb.setLength(0);
                }
            }
            listPayments.clear();
        }

        listProd = ordProdHandler.getProductsDayReport(false, null, mDate);
        if (listProd.size() > 0) {
            sb.append(textHandler.newLines(1));
            sb.append(textHandler.centeredString("Items Returned", lineWidth));
            sb.append(textHandler.threeColumnLineItem("Name", 60, "Qty", 20, "Total", 20, lineWidth, 0));
            for (OrderProduct prod : listProd) {
                sb.append(textHandler.threeColumnLineItem(prod.getOrdprod_name(), 60, prod.getOrdprod_qty(), 20, Global.getCurrencyFormat(prod.getFinalPrice()), 20, lineWidth, 0));
                if (printDetails) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("UPC:" + prod.getProd_upc(), "", lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("SKU:" + prod.getProd_sku(), "", lineWidth, 3));
                }
            }
            listProd.clear();
        }
        print(sb.toString());

        sb.setLength(0);
        sb.append(textHandler.centeredString("** End of report **", lineWidth));
        sb.append(textHandler.newLines(4));
        print(sb.toString(), FORMAT);
        cutPaper();
    }

    protected void printShiftDetailsReceipt(int lineWidth, String shiftID) {
        AssignEmployee employee = AssignEmployeeDAO.getAssignEmployee(false);
        startReceipt();
        StringBuilder sb = new StringBuilder();
        EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
        sb.append(textHandler.centeredString(activity.getString(R.string.shift_details), lineWidth));
        Shift shift = ShiftDAO.getShift(shiftID);
        Clerk clerk = ClerkDAO.getByEmpId(shift.getClerkId());
        sb.append(textHandler.newLines(1));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(activity.getString(R.string.sales_clerk), clerk == null ?
                shift.getAssigneeName() : clerk.getEmpName(), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(activity.getString(R.string.receipt_employee), employee.getEmpName(), lineWidth, 0));
        sb.append(textHandler.newLines(1));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(activity.getString(R.string.from), DateUtils.getDateAsString(shift.getStartTime()), lineWidth, 0));

        sb.append(textHandler.newLines(1));
        if (shift.getShiftStatus() == Shift.ShiftStatus.OPEN) {
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(activity.getString(R.string.to), Shift.ShiftStatus.OPEN.name(), lineWidth, 0));
        } else {
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(activity.getString(R.string.to), DateUtils.getDateAsString(shift.getEndTime()), lineWidth, 0));
        }
        sb.append(textHandler.newLines(1));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(activity.getString(R.string.begging_petty_cash), Global.getCurrencyFormat(shift.getBeginningPettyCash()), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(activity.getString(R.string.total_expenses), Global.getCurrencyFormat(shift.getTotalExpenses()), lineWidth, 0));
        List<ShiftExpense> shiftExpenses = ShiftExpensesDAO.getShiftExpenses(shiftID);
        if (shiftExpenses != null) {
            for (ShiftExpense expense : shiftExpenses) {
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(expense.getProductName(),
                        Global.getCurrencyFormat(expense.getCashAmount()), lineWidth, 3));
            }
        }
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(activity.getString(R.string.total_transactions_cash), Global.getCurrencyFormat(shift.getTotalTransactionsCash()), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(activity.getString(R.string.total_ending_cash), Global.getCurrencyFormat(shift.getTotal_ending_cash()), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(activity.getString(R.string.entered_close_amount), Global.getCurrencyFormat(shift.getEnteredCloseAmount()), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(activity.getString(R.string.shortage_overage_amount), Global.getCurrencyFormat(shift.getOver_short()), lineWidth, 0));
        sb.append(textHandler.newLines(1));

        OrderProductsHandler orderProductsHandler = new OrderProductsHandler(activity);
        String date = DateUtils.getDateAsString(shift.getCreationDate(), "yyyy-MM-dd");
        List<OrderProduct> listDeptSales = orderProductsHandler.getDepartmentDayReport(true,
                String.valueOf(shift.getClerkId()), date);
        List<OrderProduct> listDeptReturns = orderProductsHandler.getDepartmentDayReport(false,
                String.valueOf(shift.getClerkId()), date);
        if (!listDeptSales.isEmpty()) {
            sb.append(textHandler.centeredString(activity.getString(R.string.eod_report_dept_sales), lineWidth));
            for (OrderProduct product : listDeptSales) {
                sb.append(textHandler.threeColumnLineItem(product.getCat_name(), 60,
                        product.getOrdprod_qty(), 20,
                        Global.getCurrencyFormat(product.getFinalPrice()),
                        20, lineWidth, 0));
            }
        }
        sb.append(textHandler.newLines(1));
        if (!listDeptReturns.isEmpty()) {
            sb.append(textHandler.centeredString(activity.getString(R.string.eod_report_return), lineWidth));
            for (OrderProduct product : listDeptReturns) {
                sb.append(textHandler.threeColumnLineItem(product.getCat_name(), 60,
                        product.getOrdprod_qty(), 20,
                        Global.getCurrencyFormat(product.getFinalPrice()),
                        20, lineWidth, 0));
            }
        }
        sb.append(textHandler.newLines(1));
        sb.append(textHandler.centeredString(activity.getString(R.string.endShiftReport), lineWidth));
        sb.append(textHandler.newLines(4));
        print(sb.toString(), FORMAT);
        cutPaper();
    }


    protected void printExpenseReceipt(int lineWidth, ShiftExpense expense) {
        AssignEmployee employee = AssignEmployeeDAO.getAssignEmployee(false);
        startReceipt();
        StringBuilder sb = new StringBuilder();
        EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
        sb.append(textHandler.centeredString(activity.getString(R.string.shift_expense), lineWidth));
        Shift shift = ShiftDAO.getShift(expense.getShiftId());
        Clerk clerk = ClerkDAO.getByEmpId(shift.getClerkId());
        sb.append(textHandler.newLines(1));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(activity.getString(R.string.sales_clerk), clerk == null ?
                shift.getAssigneeName() : clerk.getEmpName(), lineWidth, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(activity.getString(R.string.receipt_employee), employee.getEmpName(), lineWidth, 0));
        sb.append(textHandler.newLines(1));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(activity.getString(R.string.date), DateUtils.getDateAsString(expense.getCreationDate()), lineWidth, 0));

        sb.append(textHandler.newLines(1));

        sb.append(textHandler.twoColumnLineWithLeftAlignedText(expense.getProductName(),
                Global.getCurrencyFormat(expense.getCashAmount()), lineWidth, 3));
        sb.append(textHandler.centeredString(activity.getString(R.string.receipt_description), lineWidth));
        sb.append(textHandler.oneColumnLineWithLeftAlignedText(expense.getProductDescription(), lineWidth, 0));
        sb.append(textHandler.newLines(4));
        print(sb.toString(), FORMAT);
        cutPaper();
    }

    void printReportReceipt(String curDate, int lineWidth) {
        try {
            AssignEmployee employee = AssignEmployeeDAO.getAssignEmployee(false);
            startReceipt();
            PaymentsHandler paymentHandler = new PaymentsHandler(activity);
            PayMethodsHandler payMethodHandler = new PayMethodsHandler(activity);
            EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
            StringBuilder sb = new StringBuilder();
            StringBuilder sb_refunds = new StringBuilder();
            print(textHandler.newLines(1), FORMAT);
            sb.append(textHandler.centeredString("REPORT", lineWidth));
            sb.append(textHandler.centeredString(Global.formatToDisplayDate(curDate, 0), lineWidth));
            sb.append(textHandler.newLines(1));
            sb.append(textHandler.oneColumnLineWithLeftAlignedText(getString(R.string.receipt_pay_summary), lineWidth,
                    0));
            sb.append(textHandler.newLines(1));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Employee", employee.getEmpName(), lineWidth, 0));
            sb.append(textHandler.newLines(1));
            sb_refunds.append(textHandler.oneColumnLineWithLeftAlignedText(getString(R.string.receipt_refund_summmary),
                    lineWidth, 0));
            HashMap<String, String> paymentMap = paymentHandler
                    .getPaymentsRefundsForReportPrinting(Global.formatToDisplayDate(curDate, 4), 0);
            HashMap<String, String> refundMap = paymentHandler
                    .getPaymentsRefundsForReportPrinting(Global.formatToDisplayDate(curDate, 4), 1);
            List<String[]> payMethodsNames = payMethodHandler.getPayMethodsName();
            int size = payMethodsNames.size();
            double payGranTotal = 0.00;
            double refundGranTotal = 0.00;
            print(sb.toString(), FORMAT);
            sb.setLength(0);
            for (int i = 0; i < size; i++) {
                if (paymentMap.containsKey(payMethodsNames.get(i)[0])) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
                            Global.getCurrencyFormat(paymentMap.get(payMethodsNames.get(i)[0])), lineWidth,
                            3));
                    payGranTotal += Double.parseDouble(paymentMap.get(payMethodsNames.get(i)[0]));
                } else
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
                            Global.formatDoubleToCurrency(0.00), lineWidth, 3));
                if (refundMap.containsKey(payMethodsNames.get(i)[0])) {
                    sb_refunds.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
                            Global.getCurrencyFormat(refundMap.get(payMethodsNames.get(i)[0])), lineWidth, 3));
                    refundGranTotal += Double.parseDouble(refundMap.get(payMethodsNames.get(i)[0]));
                } else
                    sb_refunds.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
                            Global.formatDoubleToCurrency(0.00), lineWidth, 3));
            }
            sb.append(textHandler.newLines(1));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
                    Global.getCurrencyFormat(Double.toString(payGranTotal)), lineWidth, 4));
            sb.append(textHandler.newLines(1));
            sb_refunds.append(textHandler.newLines(1));
            sb_refunds.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
                    Global.getCurrencyFormat(Double.toString(refundGranTotal)), lineWidth, 4));
            //print earnings
            print(sb.toString(), FORMAT);
            print(textHandler.newLines(1), FORMAT);
            //print refunds
            print(sb_refunds.toString(), FORMAT);
            print(textHandler.newLines(5), FORMAT);

            printEnablerWebSite(lineWidth);
            if (isPOSPrinter) {
                port.writePort(new byte[]{0x1b, 0x64, 0x02}, 0, 3); // Cut
            }
            cutPaper();
        } catch (StarIOPortException ignored) {
        }
    }

}
