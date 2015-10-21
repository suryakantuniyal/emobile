package protocols;

import com.android.support.CreditCardInfo;
import com.mpowa.android.sdk.powapos.common.dataobjects.PowaDeviceObject;

public interface EMSCallBack {
	void cardWasReadSuccessfully(boolean read,CreditCardInfo cardManager);
	void readerConnectedSuccessfully(boolean value);
	void scannerWasRead(String data); 

}

