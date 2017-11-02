package drivers.star.utils.sdk31.starprntsdk;

import android.content.Context;

import com.starmicronics.stario.StarIOPort;

import drivers.star.utils.sdk31.starprntsdk.functions.StarIoExtDisplayStatusFunction;

interface RequestDisplayStatusCallback {
    void onStatus(boolean result, Communication.Result communicateResult, boolean isConnect);
}

@SuppressWarnings("WeakerAccess")
public class DisplayCommunication extends Communication {

    static void requestStatus(Object lock, String portName, String portSettings, int timeout, Context context, final RequestDisplayStatusCallback callback) {

        final StarIoExtDisplayStatusFunction function = new StarIoExtDisplayStatusFunction();

        sendFunctionDoNotChecked(lock, function, portName, portSettings, timeout, context, new Communication.SendCallback() {
            @Override
            public void onStatus(boolean result, Communication.Result communicateResult) {
                callback.onStatus(result, communicateResult, function.connect);
            }
        });
    }

    static void requestStatus(Object lock, StarIOPort port, final RequestDisplayStatusCallback callback) {

        final StarIoExtDisplayStatusFunction function = new StarIoExtDisplayStatusFunction();

        sendFunctionDoNotChecked(lock, function, port, new Communication.SendCallback() {
            @Override
            public void onStatus(boolean result, Communication.Result communicateResult) {
                callback.onStatus(result, communicateResult, function.connect);
            }
        });
    }

    static void passThroughCommands(Object lock, byte[] data, String portName, String portSettings, int timeout, Context context, Communication.SendCallback callback) {
        byte l0 = (byte)(data.length % 0x0100);
        byte l1 = (byte)(data.length / 0x0100);

        byte[] command = new byte[6 + data.length];

        int index = 0;

        command[index++] = 0x1b;
        command[index++] = 0x1d;
        command[index++] = 'B';
        command[index++] = 0x40;
        command[index++] = l0;
        command[index++] = l1;

        System.arraycopy(data, 0, command, index, data.length);

        sendCommandsDoNotCheckCondition(lock, command, portName, portSettings, timeout, context, callback);
    }

    static void passThroughCommands(Object lock, byte[] data, StarIOPort port, Communication.SendCallback callback) {
        byte l0 = (byte)(data.length % 0x0100);
        byte l1 = (byte)(data.length / 0x0100);

        byte[] command = new byte[6 + data.length];

        int index = 0;

        command[index++] = 0x1b;
        command[index++] = 0x1d;
        command[index++] = 'B';
        command[index++] = 0x40;
        command[index++] = l0;
        command[index++] = l1;

        System.arraycopy(data, 0, command, index, data.length);

        sendCommandsDoNotCheckCondition(lock, command, port, callback);
    }

}
