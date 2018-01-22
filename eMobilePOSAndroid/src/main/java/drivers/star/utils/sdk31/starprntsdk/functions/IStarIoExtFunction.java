package drivers.star.utils.sdk31.starprntsdk.functions;

public interface IStarIoExtFunction {
    byte[] createCommands();
    boolean onReceiveCallback(byte[] data);
}
