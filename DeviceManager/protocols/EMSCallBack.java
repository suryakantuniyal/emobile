package protocols;

import com.android.support.CreditCardInfo;

public interface EMSCallBack {
	void cardWasReadSuccessfully(boolean read,CreditCardInfo cardManager);
	void readerConnectedSuccessfully(boolean value);
	void scannerWasRead(String data); 
}

