package interfaces;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.View;

import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.SplitedOrder;
import com.android.emobilepos.models.realms.Payment;
import com.android.support.ConsignmentTransaction;
import com.android.support.Global;

import java.util.HashMap;
import java.util.List;

public interface EMSDeviceManagerPrinterDelegate {

    boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer);

    boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold);

    boolean printPaymentDetails(String payID, int isFromMainMenu, boolean isReprint, EMVContainer emvContainer);

    boolean printBalanceInquiry(HashMap<String, String> values);

    boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSignature);

    boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSignature);

    boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup);

    String printStationPrinter(List<Orders> orderProducts, String ordID, boolean cutPaper, boolean printHeader);

    boolean printOpenInvoices(String invID);

    boolean printOnHold(Object onHold);

    void setBitmap(Bitmap bmp);

    void playSound();

    void cutPaper();

    boolean printReport(String curDate);

    public void printShiftDetailsReport(String shiftID);

    public void printEndOfDayReport(String date, String clerk_id, boolean printDetails);

    public void registerPrinter();

    void unregisterPrinter();

    void loadCardReader(EMSCallBack callBack, boolean isDebitCard);

    void loadScanner(EMSCallBack _callBack);

    void releaseCardReader();

    void openCashDrawer();

    void printHeader();

    void printFooter();

    boolean isUSBConnected();

    void toggleBarcodeReader();

//    void printReceiptPreview(View view);

    void printReceiptPreview(SplitedOrder splitedOrder);

    void salePayment(Payment payment);

    void saleReversal(Payment payment, String originalTransactionId);

    void refund(Payment payment);

    void refundReversal(Payment payment, String originalTransactionId);

    void printEMVReceipt(String text);

    void sendEmailLog();

    void updateFirmware();

    void submitSignature();

    boolean isConnected();
}
