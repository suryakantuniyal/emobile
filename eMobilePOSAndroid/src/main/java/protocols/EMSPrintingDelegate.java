package protocols;

public interface EMSPrintingDelegate {

	void printerDidFinish();

	void printerDidDisconnect(Error err);

	void printerDidBegin();

}
