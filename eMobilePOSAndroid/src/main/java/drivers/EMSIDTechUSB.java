package drivers;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.support.CardParser;
import com.android.support.CreditCardInfo;

import java.util.HashMap;
import java.util.Iterator;

import interfaces.EMSCallBack;

public class EMSIDTechUSB {
    private Activity _activity;
    private static int productId = Integer.parseInt("2010", 16);
    private static int vendorId = Integer.parseInt("0ACD", 16);

    private static final String ACTION_USB_PERMISSION =
            "com.example.company.app.testhid.USB_PERMISSION";

    // Locker object that is responsible for locking read/write thread.
    private Object _locker = new Object();
    private EMSCallBack _callBack;

    private UsbManager _usbManager;
    private UsbDevice _usbDevice;
    private boolean isDeviceOpen = false;
    private boolean startReading = false;
    private IntentFilter filter;

    // The queue that contains the read data.
    //private Queue<byte[]> _receivedQueue;

    /**
     * Creates a hid bridge to the dongle. Should be created once.
     *
     * @param activity  is the UI context of Android.
     * @param callBack interface used to update the view when done card swipe.
     */
    public EMSIDTechUSB(Activity activity, EMSCallBack callBack) {
        _activity = activity;
        _callBack = callBack;
        //_receivedQueue = new LinkedList<byte[]>();
        startReading = false;
    }

    public static boolean isUSBConnected(Context context){
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if (device.getProductId() == productId && device.getVendorId() == vendorId) {
                return true;
            }
        }

        return false;
    }
    /**
     * Searches for the device and opens it if successful
     *
     * @return true, if connection was successful
     */
    public boolean OpenDevice() {
        _usbManager = (UsbManager) _activity.getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> deviceList = _usbManager.getDeviceList();

        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        _usbDevice = null;

        // Iterate all the available devices and find ours.
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if (device.getProductId() == productId && device.getVendorId() == vendorId) {
                _usbDevice = device;
            }
        }

        if (_usbDevice == null) {
            Log("Cannot find the device. Did you forgot to plug it?");
            Log(String.format("\t I search for VendorId: %s and ProductId: %s", vendorId, productId));
            return false;
        }

        // Create and intent and request a permission.
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(_activity, 0, new Intent(ACTION_USB_PERMISSION), 0);
        filter = new IntentFilter(ACTION_USB_PERMISSION);
        _activity.registerReceiver(mUsbReceiver, filter);

        _usbManager.requestPermission(_usbDevice, mPermissionIntent);
        Log("Found the device");
        isDeviceOpen = true;
        return true;
    }

    /**
     * Closes the reading thread of the device.
     */
    public void CloseTheDevice() {
        _activity.unregisterReceiver(mUsbReceiver);
        isDeviceOpen = false;
        startReading = false;
        //StopReadingThread();
    }

    public boolean isDeviceOpen() {
        return isDeviceOpen;
    }

//	public void unregisterReceiver()
//	{
//		_activity.unregisterReceiver(mUsbReceiver);
//	}
//	public void registerReceiver()
//	{
//		_activity.registerReceiver(mUsbReceiver, filter);
//	}

    /**
     * Starts the thread that continuously reads the data from the device.
     * Should be called in order to be able to talk with the device.
     */

    public boolean isDeviceReading() {
        return startReading;
    }


    public void StartReadingThread() {
        startReading = true;
        new getSwipeDataAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Stops the thread that continuously reads the data from the device.
     * If it is stopped - talking to the device would be impossible.
     */
    public void StopReadingThread() {

        startReading = false;
    }

    /**
     * Write data to the usb hid. Data is written as-is, so calling method is responsible for adding header data.
     *
     * @param bytes is the data to be written.
     * @return true if succeed.
     */
    public boolean WriteData(byte[] bytes) {
        try {
            // Lock that is common for read/write methods.
            synchronized (_locker) {
                UsbInterface writeIntf = _usbDevice.getInterface(0);
                UsbEndpoint writeEp = writeIntf.getEndpoint(1);
                UsbDeviceConnection writeConnection = _usbManager.openDevice(_usbDevice);

                // Lock the usb interface.
                writeConnection.claimInterface(writeIntf, true);

                // Write the data as a bulk transfer with defined data length.
                int r = writeConnection.bulkTransfer(writeEp, bytes, bytes.length, 0);
                if (r != -1) {
                    Log(String.format("Written %s bytes to the dongle. Data written: %s", r, composeString(bytes)));
                } else {
                    Log("Error happened while writing data. No ACK");
                }

                // Release the usb interface.
                writeConnection.releaseInterface(writeIntf);
                writeConnection.close();
            }

        } catch (NullPointerException e) {
            Log("Error happend while writing. Could not connect to the device or interface is busy?");
            Log.e("HidBridge", Log.getStackTraceString(e));
            return false;
        }
        return true;
    }

//	/**
//	 * @return true if there are any data in the queue to be read.
//	 */
//	public boolean IsThereAnyReceivedData() {
//		synchronized(_locker) {
//			return !_receivedQueue.isEmpty();
//		}
//	}
//	
//	/**
//	 * Queue the data from the read queue.
//	 * @return queued data.
//	 */
//	public byte[] GetReceivedDataFromQueue() {
//		synchronized(_locker) {
//			return _receivedQueue.poll();
//		}
//	}


    private class getSwipeDataAsync extends AsyncTask<Void, Void, Void> {

        private UsbEndpoint readEp;
        private UsbDeviceConnection readConnection = null;
        private UsbInterface readIntf = null;

        int packetSize = 1024;
        byte[] bytes = new byte[packetSize];

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            readIntf = _usbDevice.getInterface(0);
            readEp = readIntf.getEndpoint(0);
            while (startReading) {
                try {
                    if (readConnection == null) {
                        try {

                            readConnection = _usbManager.openDevice(_usbDevice);

                            if (readConnection == null) {
                                Log("Cannot start reader because the user didn't gave me permissions or the device is not present. Retrying in 2 sec...");
                                Sleep(2000);
                                continue;
                            }

                            // Claim and lock the interface in the android system.
                            readConnection.claimInterface(readIntf, true);
                        } catch (SecurityException e) {
                            Log("Cannot start reader because the user didn't gave me permissions. Retrying in 2 sec...");

                            Sleep(2000);
                            continue;
                        }
                    }
                    if (readConnection.bulkTransfer(readEp, bytes, packetSize, 500) >= 0) {
                        int size = bytes.length - 1;
                        int index = 0;
                        for (int i = size; i > -1; i--) {
                            if (bytes[i] != 0) {
                                index = i;
                                break;
                            }
                        }
                        final byte[] arrayOfByte = new byte[index + 1];
                        System.arraycopy(bytes, 0, arrayOfByte, 0, index + 1);
                        _activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CreditCardInfo creditCardInfo;
                                if (arrayOfByte.length > 3 && checkLRCAndChecksum(arrayOfByte)) {
                                    creditCardInfo = CardParser.parseIDTechOriginal(_activity, arrayOfByte);
                                    _callBack.cardWasReadSuccessfully(true, creditCardInfo);
                                } else
                                    Toast.makeText(_activity, "Card data error, bad swipe", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else
                        bytes = new byte[packetSize];
                } catch (NullPointerException e) {
                    Log("Error happened while reading. No device or the connection is busy");
                    Log.e("HidBridge", Log.getStackTraceString(e));
                } catch (ThreadDeath e) {
                    if (readConnection != null) {
                        readConnection.releaseInterface(readIntf);
                        readConnection.close();
                    }

                    throw e;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
        }
    }


    private byte calculateCheckSum(byte[] paramArrayOfByte) {
        int i = 0;
        for (int j = 3; ; j++) {
            if (j >= -3 + paramArrayOfByte.length) {
                return (byte) (i & 0xFF);
            }
            i = (byte) (i + paramArrayOfByte[j]);
        }
    }

    private byte calculateLRC(byte[] paramArrayOfByte) {
        int i = 0;
        for (int j = 3; ; j++) {
            if (j >= -3 + paramArrayOfByte.length) {
                return (byte) (i & 0xFF);
            }
            i = (byte) (i ^ paramArrayOfByte[j]);
        }
    }

    private boolean checkLRCAndChecksum(byte[] paramArrayOfByte) {
        return (paramArrayOfByte[(-3 + paramArrayOfByte.length)] == calculateLRC(paramArrayOfByte)) && (paramArrayOfByte[(-2 + paramArrayOfByte.length)] == calculateCheckSum(paramArrayOfByte));
    }


    private void Sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            //call method to set up device communication
                        }
                    } else {
                        Log.d("TAG", "permission denied for the device " + device);
                    }
                }
            }
        }
    };

    /**
     * Logs the message from HidBridge.
     *
     * @param message to log.
     */
    private void Log(String message) {
        //LogHandler logHandler = LogHandler.getInstance();
        //logHandler.WriteMessage("HidBridge: " + message, LogHandler.GetNormalColor());
        //Log.println(1, "USB Log", message);
        //System.out.println(message);
    }

    /**
     * Composes a string from byte array.
     */
    private String composeString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(b);
            builder.append(" ");
        }

        return builder.toString();
    }
}