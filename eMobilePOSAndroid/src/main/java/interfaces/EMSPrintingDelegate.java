package interfaces;

public interface EMSPrintingDelegate {

	void printerDidFinish();

	void printerDidDisconnect(Error err);

	void printerDidBegin();

}
