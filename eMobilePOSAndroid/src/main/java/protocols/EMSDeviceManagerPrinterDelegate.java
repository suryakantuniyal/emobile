package protocols;

import android.database.Cursor;
import android.graphics.Bitmap;

import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Payment;
import com.android.support.ConsignmentTransaction;
import com.android.support.Global;

import java.util.HashMap;
import java.util.List;

public interface EMSDeviceManagerPrinterDelegate {

    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer);

    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold);

    public boolean printPaymentDetails(String payID, int isFromMainMenu, boolean isReprint);

    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSignature);

    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSignature);

    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup);

    public void printStationPrinter(List<Orders> orderProducts, String ordID);


    public boolean printOpenInvoices(String invID);

    public boolean printOnHold(Object onHold);

    public void setBitmap(Bitmap bmp);

    public void playSound();

    public boolean printReport(String curDate);

    public void printEndOfDayReport(String date, String clerk_id);

    public void registerPrinter();

    public void unregisterPrinter();

    public void loadCardReader(EMSCallBack callBack, boolean isDebitCard);

    public void loadScanner(EMSCallBack _callBack);

    public void releaseCardReader();

    public void openCashDrawer();

    public void printHeader();

    public void printFooter();

    public boolean isUSBConnected();

}
