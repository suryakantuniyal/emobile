package interfaces;

import android.database.Cursor;
import android.graphics.Bitmap;

import com.android.emobilepos.models.ClockInOut;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.SplittedOrder;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.ShiftExpense;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Global;

import java.util.HashMap;
import java.util.List;

public interface EMSDeviceManagerPrinterDelegate {

    boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer);

    boolean printTransaction(Order order, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer);

    boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold);

    boolean printTransaction(Order order, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold);

    boolean printGiftReceipt(Order order, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold);

    boolean printPaymentDetails(String payID, int isFromMainMenu, boolean isReprint, EMVContainer emvContainer);

    boolean printBalanceInquiry(HashMap<String, String> values);

    boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSignature);

    boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSignature);

    boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup);

    boolean printRemoteStation(List<Orders> orderProducts, String ordID);

    boolean printOpenInvoices(String invID);

    boolean printOnHold(Object onHold);

    void setBitmap(Bitmap bmp);

    void playSound();

    void turnOnBCR();

    void turnOffBCR();

    void cutPaper();

    boolean printReport(String curDate);

    void printShiftDetailsReport(String shiftID);

    void printEndOfDayReport(String date, String clerk_id, boolean printDetails);

    void registerPrinter();

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

    void printReceiptPreview(SplittedOrder splitedOrder);

    void salePayment(Payment payment, CreditCardInfo creditCardInfo);

    void saleReversal(Payment payment, String originalTransactionId, CreditCardInfo creditCardInfo);

    void refund(Payment payment, CreditCardInfo creditCardInfo);

    void refundReversal(Payment payment, String originalTransactionId, CreditCardInfo creditCardInfo);

    void printEMVReceipt(String text);

    void sendEmailLog();

    void updateFirmware();

    void submitSignature();

    boolean isConnected();

    void printClockInOut(List<ClockInOut> clockInOuts, String clerkID);

    void printExpenseReceipt(ShiftExpense expense);
}
