package protocols;

public interface EMSPrintingDelegate {

	public void printerDidFinish();

	public void printerDidDisconnect(Error err);

	public void printerDidBegin();

}
