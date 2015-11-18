package protocols;

public interface EMSUniMagCallBack {
	void cardWasReadSuccessfully(boolean read);
	void readerConnectedSuccessfully(boolean value);
}
