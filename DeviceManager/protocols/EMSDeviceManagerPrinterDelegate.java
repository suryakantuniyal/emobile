package protocols;

import java.util.HashMap;
import java.util.List;

import com.android.emobilepos.models.Orders;
import com.android.support.ConsignmentTransaction;
import android.database.Cursor;
import android.graphics.Bitmap;

public interface EMSDeviceManagerPrinterDelegate {

	public boolean printTransaction(String ordID, int type, boolean isFromHistory, boolean fromOnHold);

	public boolean printPaymentDetails(String payID, int isFromMainMenu, boolean isReprint);

	public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSignature);

	public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSignature);

	public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup);

	public void printStationPrinter(List<Orders> orderProducts, String ordID);
	

	public boolean printOpenInvoices(String invID);

	public boolean printOnHold(Object onHold);

	public void setBitmap(Bitmap bmp);

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
