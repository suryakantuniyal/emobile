package drivers.star.utils.sdk31.starprntsdk;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;

import drivers.star.utils.sdk31.starprntsdk.functions.IStarIoExtFunction;
import drivers.star.utils.sdk31.starprntsdk.functions.StarIoExtScaleStatusFunction;


interface RequestScaleStatusCallback {
    void onStatus(boolean result, Communication.Result communicateResult, boolean isConnect);
}

@SuppressWarnings("WeakerAccess")
public class ScaleCommunication extends Communication {

    static void requestStatus(Object lock, String portName, String portSettings, int timeout, Context context, final RequestScaleStatusCallback callback) {

        final StarIoExtScaleStatusFunction function = new StarIoExtScaleStatusFunction();

        sendFunctionDoNotChecked(lock, function, portName, portSettings, timeout, context, new SendCallback() {
            @Override
            public void onStatus(boolean result, Result communicateResult) {
                callback.onStatus(result, communicateResult, function.connect);
            }
        });
    }

    static void requestStatus(Object lock, StarIOPort port, final RequestScaleStatusCallback callback) {

        final StarIoExtScaleStatusFunction function = new StarIoExtScaleStatusFunction();

        sendFunctionDoNotChecked(lock, function, port, new SendCallback() {
            @Override
            public void onStatus(boolean result, Result communicateResult) {
                callback.onStatus(result, communicateResult, function.connect);
            }
        });
    }

    static void passThroughCommands(Object lock, byte[] data, String portName, String portSettings, int timeout, Context context, SendCallback callback) {
        byte l0 = (byte)(data.length % 0x0100);
        byte l1 = (byte)(data.length / 0x0100);

        byte[] command = new byte[6 + data.length];

        int index = 0;

        command[index++] = 0x1b;
        command[index++] = 0x1d;
        command[index++] = 'B';
        command[index++] = 0x50;
        command[index++] = l0;
        command[index++] = l1;

        System.arraycopy(data, 0, command, index, data.length);

        sendCommandsDoNotCheckCondition(lock, command, portName, portSettings, timeout, context, callback);
    }

    static void passThroughCommands(Object lock, byte[] data, StarIOPort port, SendCallback callback) {
        byte l0 = (byte)(data.length % 0x0100);
        byte l1 = (byte)(data.length / 0x0100);

        byte[] command = new byte[6 + data.length];

        int index = 0;

        command[index++] = 0x1b;
        command[index++] = 0x1d;
        command[index++] = 'B';
        command[index++] = 0x50;
        command[index++] = l0;
        command[index++] = l1;

        System.arraycopy(data, 0, command, index, data.length);

        sendCommandsDoNotCheckCondition(lock, command, port, callback);
    }

    static void passThroughFunction(Object lock, final IStarIoExtFunction function, StarIOPort port, final Communication.SendCallback callback ) {

        ScalePassThroughFunctionThread thread = new ScalePassThroughFunctionThread(lock, function, port, callback);
        thread.start();
    }

    static void passThroughFunction(Object lock, final IStarIoExtFunction function, String portName, String portSettings, int timeout, Context context, final Communication.SendCallback callback ) {

        ScalePassThroughFunctionThread thread = new ScalePassThroughFunctionThread(lock, function, portName, portSettings, timeout, context, callback);
        thread.start();
    }

}

class ScalePassThroughFunctionThread extends Thread {
    private final Object mLock;
    private IStarIoExtFunction         mFunction;
    private StarIOPort mPort = null;
    private Communication.SendCallback mCallback;

    private String mPortName = null;
    private String mPortSettings;
    private int     mTimeout;
    private Context mContext;

    ScalePassThroughFunctionThread(Object lock, IStarIoExtFunction function, StarIOPort port, Communication.SendCallback callback) {
        mLock     = lock;
        mFunction = function;
        mPort     = port;
        mCallback = callback;
    }

    ScalePassThroughFunctionThread(Object lock, IStarIoExtFunction function, String portName, String portSettings, int timeout, Context context, Communication.SendCallback callback) {
        mLock         = lock;
        mFunction     = function;
        mPortName     = portName;
        mPortSettings = portSettings;
        mTimeout      = timeout;
        mContext      = context;
        mCallback     = callback;
    }

    @Override
    public void run() {
        boolean              result            = false;
        Communication.Result communicateResult = Communication.Result.ErrorOpenPort;

        synchronized (mLock) {
            try {
                if (mPort == null) {

                    if (mPortName == null) {
                        resultSendCallback(false, communicateResult, mCallback);
                        return;
                    } else {
                        mPort = StarIOPort.getPort(mPortName, mPortSettings, mTimeout, mContext);
                    }
                }

//              // When using USB interface with mPOP(F/W Ver 1.0.1), you need to send the following data.
//              byte[] dummy = {0x00};
//              port.writePort(dummy, 0, dummy.length);

                StarPrinterStatus status;

                communicateResult = Communication.Result.ErrorWritePort;
                status = mPort.retreiveStatus();

                if (status.rawLength == 0) {
                    throw new StarIOPortException("A printer is offline");
                }

                long start = System.currentTimeMillis();

                if (mPort.getPortName().toLowerCase().startsWith("usb")) {                  // USB Interface: Temporarily Disable ASB/NSB.

                    byte[] buffer = new byte[1024];
                    byte[] disableAsbNsbCmd = {0x1b, 0x1e, 'a', 0};
                    mPort.writePort(disableAsbNsbCmd, 0, disableAsbNsbCmd.length);

                    long lastReceiveTime = System.currentTimeMillis();
                    while (true) {

                        if (mPort.readPort(buffer, 0, buffer.length) != 0) {
                            try {
                                Thread.sleep(10);                                           // Wait 10ms
                            } catch (InterruptedException e) {
                                // do nothing
                            }
                            lastReceiveTime = System.currentTimeMillis();
                        }

                        if (100 < (System.currentTimeMillis() - lastReceiveTime)) {
                            break;
                        }

                        if (5000 < (System.currentTimeMillis() - start)) {
                            communicateResult = Communication.Result.ErrorReadPort;
                            throw new StarIOPortException("Error");
                        }
                    }
                }


                byte[] clearBuffer = {0x1b, 0x1d, 'B', 0x53};

                mPort.writePort(clearBuffer, 0, clearBuffer.length);

                byte[] data = mFunction.createCommands();

                byte l0 = (byte) (data.length % 0x0100);
                byte l1 = (byte) (data.length / 0x0100);

                byte[] command = new byte[6 + data.length];

                int index = 0;

                command[index++] = 0x1b;
                command[index++] = 0x1d;
                command[index++] = 'B';
                command[index++] = 0x50;
                command[index++] = l0;
                command[index++] = l1;

                System.arraycopy(data, 0, command, index, data.length);

                communicateResult = Communication.Result.ErrorWritePort;

                start = System.currentTimeMillis();

                boolean isScaleDataReceived = false;

                while(!isScaleDataReceived) {
                    mPort.writePort(command, 0, command.length);

                    long startRequestScaleData = System.currentTimeMillis();

                    while (!isScaleDataReceived) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            // Do nothing
                        }

                        byte[] requestData = {0x1b, 0x1d, 'B', 0x52};
                        byte[] receiveBuffer = new byte[1024];

                        mPort.writePort(requestData, 0, requestData.length);

                        int amount = 0;
                        int ignoreByte = 0;

                        long startRequestData = System.currentTimeMillis();

                        while (!isScaleDataReceived) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                // Nothing
                            }

                            int receiveSize;
                            receiveSize = mPort.readPort(receiveBuffer, amount, receiveBuffer.length - amount);

                            if (0 < receiveSize) {
                                amount += receiveSize;
                            }

                            if (6 <= amount) {
                                byte[] receiveData;
                                int length = 0;

                                boolean isResendRequest = false;

                                for (int i = ignoreByte; i <= amount - 6; i++) {
                                    if (receiveBuffer[i] == 0x1b &&
                                        receiveBuffer[i + 1] == 0x1d &&
                                        receiveBuffer[i + 2] == 'B' &&
                                        receiveBuffer[i + 3] == 0x52) {

                                        length = (receiveBuffer[i + 4] & 0xFF) + (receiveBuffer[i + 5] & 0xFF) * 0x100;

                                        if ((length != 0) && ((length + 6) <= amount)) {
                                            isScaleDataReceived = true;
                                        }
                                        else {
                                            isResendRequest = true;
                                        }
                                        break;
                                    } else {
                                        ignoreByte++;
                                    }
                                }

                                if (isResendRequest) {
                                    break;
                                }

                                if (isScaleDataReceived) {
                                    int srcStartIndex = ignoreByte + 6;

                                    receiveData = new byte[length];
                                    System.arraycopy(receiveBuffer, srcStartIndex, receiveData, 0, length);

                                    result = mFunction.onReceiveCallback(receiveData);
                                    communicateResult = Communication.Result.Success;
                                }
                            }

                            if (250 < (System.currentTimeMillis() - startRequestData)) {
                                break;
                            }
                        }

                        if (250 < (System.currentTimeMillis() - startRequestScaleData)) {
                            // Resend Scale Command
                            // Because the scale doesn't sometimes react.
                            break;
                        }
                    }

                    if (1000 < (System.currentTimeMillis() - start)) {
                        communicateResult = Communication.Result.ErrorReadPort;
                        break;
                    }
                }

            } catch (StarIOPortException e) {
                // Nothing
            }

            if (mPort != null && mPortName != null) {
                try {
                    StarIOPort.releasePort(mPort);
                } catch (StarIOPortException e) {
                    // Nothing
                }
                mPort = null;
            }

            resultSendCallback(result, communicateResult, mCallback);
        }
}

    private static void resultSendCallback(final boolean result, final Communication.Result communicationResult, final Communication.SendCallback callback) {
        if (callback != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onStatus(result, communicationResult);
                }
            });
        }
    }
}