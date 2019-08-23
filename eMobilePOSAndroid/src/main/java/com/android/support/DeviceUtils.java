package com.android.support;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.dao.DeviceTableDAO;
import com.android.emobilepos.BuildConfig;
import com.android.emobilepos.R;
import com.android.emobilepos.mainmenu.MainMenu_FA;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.realms.Device;
import com.android.emobilepos.models.realms.RealmString;
import com.crashlytics.android.Crashlytics;
import com.elo.device.DeviceManager;
import com.elo.device.enums.EloPlatform;
import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import drivers.EMSDeviceDriver;
import drivers.EMSHPEngageOnePrimePrinter;
import drivers.EMSPowaPOS;
import drivers.EMSStar;
import drivers.EMSmePOS;
import drivers.EMSsnbc;
import io.realm.RealmList;
import main.EMSDeviceManager;

import static com.android.emobilepos.customer.ViewCustomerDetails_FA.ACTION_USB_PERMISSION;

/**
 * Created by Guarionex on 6/14/2016.
 */
public class DeviceUtils {

    private static BroadcastReceiver fingerPrintbroadcastReceiver = null;

    public static String autoConnect(final Activity activity, boolean forceReload) {
        final MyPreferences myPref = new MyPreferences(activity);
        final StringBuilder sb = new StringBuilder();
        List<Device> devices = DeviceTableDAO.getAll();
        EMSDeviceManager edm = null;
        connectStarTS650BT(activity);

        if (forceReload) {
            Global.remoteStationsPrinters = new ArrayList<>();
            HashMap<String, Integer> loadedPrinters = new HashMap<>();
            int i = 0;
            for (Device device : devices) {
                if (device.isRemoteDevice()) {
                    // these are the remote station printers (aka kitchen printers)
                    if (loadedPrinters.containsKey(device.getIpAddress())) {
                        (Global.remoteStationsPrinters.get(loadedPrinters.get(device.getIpAddress())))
                                .getRemoteStationQueue().put(device.getCategoryId(), new ArrayList<Orders>());
                    } else {
                        loadedPrinters.put(device.getIpAddress(), i);

                        edm = new EMSDeviceManager();
                        edm.getRemoteStationQueue().put(device.getCategoryId(), new ArrayList<Orders>());
                        Global.remoteStationsPrinters.add(edm);

                        if (Global.remoteStationsPrinters.get(i).loadMultiDriver(activity, Global.STAR, 48, true,
                                "TCP:" + device.getIpAddress(), device.getTcpPort()))
                            sb.append(device.getIpAddress()).append(": ").append("Connected (Remote Station)\n\r");
                        else
                            sb.append(device.getIpAddress()).append(": ").append("Failed to connect (Remote Station)\n\r");

                        i++;
                    }
                }
            }
        }

        EMSDeviceDriver usbDevice = getUSBDeviceDriver(activity);
        if (usbDevice instanceof EMSPowaPOS) {
            myPref.setIsMEPOS(false);
            myPref.setIsPOWA(true);
        }
//        if (myPref.isSNBC() && usbDevice != null) {
//            connectSNBCUSB(activity, usbDevice);
//        }
        String _portName;
        String _peripheralName;
        if (myPref.getSwiperType() != -1)
            if (Global.btSwiper == null || forceReload) {
                edm = new EMSDeviceManager();
                _portName = myPref.getSwiperMACAddress();
                _peripheralName = Global.getPeripheralName(myPref.getSwiperType());
                Global.btSwiper = edm;
                if (_peripheralName.equalsIgnoreCase(Global.getPeripheralName(Global.NOMAD))) {
                    final String final_peripheralName = _peripheralName;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (Global.btSwiper.loadMultiDriver(activity, myPref.getSwiperType(), 0, false,
                                    myPref.getSwiperMACAddress(), null))
                                sb.append(final_peripheralName).append(": ").append("Connected\n\r");
                            else
                                sb.append(final_peripheralName).append(": ").append("Failed to connect\n\r");
                        }
                    });
                    synchronized (activity) {
                        try {
                            activity.wait(30000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (Global.btSwiper.loadMultiDriver(activity, myPref.getSwiperType(), 0, false,
                            myPref.getSwiperMACAddress(), null))
                        sb.append(_peripheralName).append(": ").append("Connected\n\r");
                    else
                        sb.append(_peripheralName).append(": ").append("Failed to connect\n\r");
                }
            }
        if ((myPref.sledType(true, -2) != -1))
            if (Global.btSled == null || forceReload) {
                edm = new EMSDeviceManager();
                Global.btSled = edm.getManager();
                _peripheralName = Global.getPeripheralName(myPref.sledType(true, -2));

                if (Global.btSled.loadMultiDriver(activity, myPref.sledType(true, -2), 0, false, null, null))
                    sb.append(_peripheralName).append(": ").append("Connected\n\r");
                else
                    sb.append(_peripheralName).append(": ").append("Failed to connect\n\r");
            }
        if (myPref.isPAT215()) {
            if (Global.embededMSR == null || forceReload) {
                edm = new EMSDeviceManager();
                Global.embededMSR = edm.getManager();
                if (Global.embededMSR.loadMultiDriver(activity, Global.PAT215, 0, false, "", "")) {
                    sb.append(Global.BuildModel.PAT215.name()).append(": ").append("Connected\n\r");
                } else {
                    sb.append(Global.BuildModel.PAT215.name()).append(": ").append("Failed to connect\n\r");
                }
            }
        } else if (myPref.isESY13P1()) {
            if (myPref.getPrinterType() == -1) {
                myPref.setPrinterType(Global.ELOPAYPOINT);
            }
            if (DeviceManager.getPlatformInfo().eloPlatform == EloPlatform.PAYPOINT_2) {
                if (Global.embededMSR == null) {
                    edm = new EMSDeviceManager();
                    Global.embededMSR = edm.getManager();
                    if (Global.embededMSR.loadMultiDriver(activity, Global.MAGTEK_EMBEDDED, 0, false, "", "")) {
                        sb.append(Global.BuildModel.PayPoint_ESY13P1.name()).append(": ").append("Connected\n\r");
                    } else {
                        sb.append(Global.BuildModel.PayPoint_ESY13P1.name()).append(": ").append("Failed to connect\n\r");
                    }
                }
            } else if (Global.mainPrinterManager == null || Global.mainPrinterManager.getCurrentDevice() == null) {
                edm = new EMSDeviceManager();
                Global.mainPrinterManager = edm.getManager();
                Global.embededMSR = Global.mainPrinterManager;
                if (Global.mainPrinterManager.loadMultiDriver(activity, Global.ELOPAYPOINT, 0, true, "", "")) {
                    sb.append(Global.BuildModel.PayPoint_ESY13P1.name()).append(": ").append("Connected\n\r");
                } else {
                    sb.append(Global.BuildModel.PayPoint_ESY13P1.name()).append(": ").append("Failed to connect\n\r");
                }
            }
        } else if (MyPreferences.isTeamSable()) {
            Collection<UsbDevice> usbDevices = getUSBDevices(activity);

            boolean isMagtekConnected = false;

            for (UsbDevice device : usbDevices) {
                if (device.getProductId() == 17 &&
                        device.getVendorId() == 2049) { // "Mag-Tek Embedded"
                    isMagtekConnected = true;
                    break;
                }
            }

            if (isMagtekConnected) {
                if (Global.embededMSR == null || forceReload) {
                    Global.embededMSR = (new EMSDeviceManager()).getManager();
                    if (Global.embededMSR.loadMultiDriver(activity, Global.MAGTEK_EMBEDDED,
                            0, false, "", "")) {
                        sb.append(Global.getPeripheralName(Global.MAGTEK_EMBEDDED))
                                .append(": Connected\n\r");
                    } else {
                        sb.append(Global.getPeripheralName(Global.MAGTEK_EMBEDDED))
                                .append(": Failed to connect\n\r");
                    }
                }
            }
        } else if(MyPreferences.isAPT50()){
            connectApt50(activity);
        }else if (MyPreferences.isPaxA920()) {
            if (Global.mainPrinterManager == null || forceReload) {
                edm = new EMSDeviceManager();
                Global.mainPrinterManager = edm.getManager();

                String paxPrinter = "PAX A920 Printer";
                if (Global.mainPrinterManager.loadMultiDriver(activity, Global.PAX_A920,
                        32, true, "", "")) {
                    sb.append(paxPrinter).append(": ").append("Connected\n\r");
                    Device device = DeviceTableDAO.getByName(paxPrinter);
                    boolean deviceIsNew = false;
                    if (device == null) {
                        device = new Device();
                        deviceIsNew = true;
                    }
                    device.setId(paxPrinter);
                    device.setName(paxPrinter);
                    device.setType(String.valueOf(Global.PAX_A920));
                    device.setRemoteDevice(false);
                    device.setEmsDeviceManager(Global.mainPrinterManager);
                    devices.add(device);
                    DeviceTableDAO.insert(devices);
                    Global.printerDevices.add(device);
                    if (deviceIsNew) {
                        RealmList<RealmString> values = new RealmList<>();
                        values.add(Device.Printables.PAYMENT_RECEIPT.getRealmString());
                        values.add(Device.Printables.PAYMENT_RECEIPT_REPRINT.getRealmString());
                        values.add(Device.Printables.TRANSACTION_RECEIPT.getRealmString());
                        values.add(Device.Printables.TRANSACTION_RECEIPT_REPRINT.getRealmString());
                        values.add(Device.Printables.REPORTS.getRealmString());
                        DeviceTableDAO.remove(values);
                        device.setSelectedPritables(values);
                        DeviceTableDAO.upsert(device);
                    }
                } else {
                    sb.append(paxPrinter).append(": ").append("Failed to connect\n\r");
                }
            }
        }

        if ((myPref.getPrinterType() != -1)) {
            _peripheralName = Global.getPeripheralName(myPref.getPrinterType());
            _portName = myPref.getPrinterMACAddress();
            String _portNumber = myPref.getStarPort();
            boolean isPOS = myPref.posPrinter(true, false);
            int txtAreaSize = myPref.printerAreaSize(true, -1);
            if (myPref.getPrinterType() != Global.POWA
                    && myPref.getPrinterType() != Global.MEPOS
                    && myPref.getPrinterType() != Global.PAT215) {
                if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
                    if (!Global.mainPrinterManager.getCurrentDevice().isConnected()) {
                        forceReload = true;
                    }
                }
            }
        } else if (!TextUtils.isEmpty(myPref.getStarIPAddress())) {
            if (Global.mainPrinterManager == null || forceReload) {
                edm = new EMSDeviceManager();
                Global.mainPrinterManager = edm.getManager();

                if (Global.mainPrinterManager.loadMultiDriver(activity, Global.STAR, 48, true,
                        "TCP:" + myPref.getStarIPAddress(), myPref.getStarPort())) {
                    sb.append(myPref.getStarIPAddress()).append(": ").append("Connected\n\r");
                    Device device = DeviceTableDAO.getByName("TCP:" + myPref.getStarIPAddress());
                    if (device != null) {
                        device.setEmsDeviceManager(Global.mainPrinterManager);
                        Global.printerDevices.add(device);
                    }
                } else {
                    sb.append(myPref.getStarIPAddress()).append(": ").append("Failed to connect\n\r");
                }
            }
        }else if (myPref.isHPEOnePrime() && usbDevice instanceof EMSHPEngageOnePrimePrinter) {
            connectHPEngageOnePrimePrinter(activity,usbDevice);
        }
        ArrayList<Device> connected = new ArrayList(Global.printerDevices);

        for (Device device : devices) {
            int i = connected.indexOf(device);
            if (i == -1 || (i > -1 && (connected.get(i).getEmsDeviceManager() == null && !connected.get(i).isRemoteDevice()))) {
                try {
                    EMSDeviceManager deviceManager = new EMSDeviceManager();
                    if (deviceManager.loadMultiDriver(activity, Integer.parseInt(device.getType()), device.getTextAreaSize(),
                            device.isPOS(), device.getMacAddress(), device.getTcpPort())) {
                        device.setEmsDeviceManager(deviceManager);
                    }
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
            } else {
                device.setEmsDeviceManager(connected.get(i).getEmsDeviceManager());
            }
        }
        Global.printerDevices.clear();
        Global.printerDevices.addAll(devices);
//        sendBroadcast(activity);
        return sb.toString();
    }

    public static Collection<UsbDevice> getUSBDevices(Context context) {
        HashMap<String, UsbDevice> deviceList = new HashMap<>();
        try {
            UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            deviceList = manager.getDeviceList();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        return deviceList.values();
    }

    public static EMSDeviceDriver getUSBDeviceDriver(Activity context) {
        Collection<UsbDevice> usbDevices = getUSBDevices(context);
        MyPreferences preferences = new MyPreferences(context);
        for (UsbDevice device : usbDevices) {
            Log.d("USB product ID:", String.valueOf(device.getProductId()));
            int productId = device.getProductId();
            switch (productId) {
                case 3690:
                    preferences.setHPEOnePrime(true);
                    return new EMSHPEngageOnePrimePrinter();
                case 22321:
                case 21541:
                    Log.d("USB POWA detected:", "true");
                    preferences.setIsPOWA(true);
                    preferences.setPrinterType(Global.POWA);
                    return new EMSPowaPOS();
                case 9220:
                    preferences.setIsMEPOS(true);
                    return new EMSmePOS();
                case 5455:
                    preferences.setSNBC(true);
                    return new EMSsnbc();
            }
        }
        return null;
    }


    public static void unregisterFingerPrintReader(Context context) {
        context.getApplicationContext().unregisterReceiver(fingerPrintbroadcastReceiver);
    }

    public static void registerFingerPrintReader(Context context) {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
        intentFilter.addAction("com.android.example.USB_PERMISSION");

        final Context activity = context;

        fingerPrintbroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean connected;
                MyPreferences preferences = new MyPreferences(context);
                if (intent.getAction().contains("ATTACHED")) {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(context, "USB connected", Toast.LENGTH_SHORT).show();
                    }
                    UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
                    UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    // Create and intent and request a permission.
                    PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                    context.registerReceiver(fingerPrintbroadcastReceiver, filter);
                    manager.requestPermission(usbDevice, mPermissionIntent);
                    connected = true;
                    if (activity != null &&
                            activity instanceof Activity &&
                            ((preferences.getPrinterType() == Global.STAR && preferences.getPrinterName().toUpperCase().startsWith("USB")) ||
                                    (preferences.isSNBC()) || (preferences.isHPEOnePrime())
                            )) {

                        if(preferences.isHPEOnePrime()){
                            EMSDeviceDriver usbDeviceDriver = getUSBDeviceDriver((Activity) activity);
                            if(usbDeviceDriver != null){
                                connectHPEngageOnePrimePrinter(activity,usbDeviceDriver);
                            }
                        }

                        else if (preferences.isSNBC()) {
//                            DeviceUtils.connectStarTS650BT(activity);
                            EMSDeviceDriver usbDeviceDriver = getUSBDeviceDriver((Activity) activity);
                            if (usbDeviceDriver != null) {
                                connectSNBCUSB(activity, usbDeviceDriver);
                            }
                        }
//                        if (Global.mainPrinterManager == null || Global.mainPrinterManager.getCurrentDevice() == null) {
                        new ReconnectUSBPrinterTask((Activity) activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                        } else {
//                            DeviceUtils.connectStarTS650BT(activity);
//                        }
                    }
                } else if (intent.getAction().contains("DETACHED")) {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    }
                    connected = false;
                    if (preferences.getPrinterType() == Global.STAR && preferences.getPrinterName().toUpperCase().startsWith("USB")) {
                        Toast.makeText(context, context.getString(R.string.usb_disconnected), Toast.LENGTH_SHORT).show();
                    }
                    if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null &&
                            Global.mainPrinterManager.getCurrentDevice() instanceof EMSsnbc) {
//                        ((EMSsnbc) Global.mainPrinterManager.getCurrentDevice()).closeUsbInterface();
                    }
                }
            }
        };
        context.getApplicationContext().registerReceiver(fingerPrintbroadcastReceiver, intentFilter);
    }

    public static void connectApt50(Activity activity){
            EMSDeviceManager edm = new EMSDeviceManager();
            Global.mainPrinterManager = edm.getManager();

            String aptPrinter = "APT50 Printer";
            if (Global.mainPrinterManager.loadMultiDriver(activity, Global.APT_50,
                    32, true, "", "")) {
                Log.e("APT50","Connection Successfull");
                Device device = DeviceTableDAO.getByName(aptPrinter);
                List<Device> devices = new ArrayList<>();
                boolean deviceIsNew = false;
                if (device == null) {
                    device = new Device();
                    deviceIsNew = true;
                }
                device.setId(aptPrinter);
                device.setName(aptPrinter);
                device.setPOS(true);
                device.setType(String.valueOf(Global.APT_50));
                device.setRemoteDevice(false);
                device.setEmsDeviceManager(Global.mainPrinterManager);
                device.setTextAreaSize(32);
                devices.add(device);
                DeviceTableDAO.insert(devices);
                Global.printerDevices.add(device);
                if (deviceIsNew) {
                    RealmList<RealmString> values = new RealmList<>();
                    values.add(Device.Printables.PAYMENT_RECEIPT.getRealmString());
                    values.add(Device.Printables.PAYMENT_RECEIPT_REPRINT.getRealmString());
                    values.add(Device.Printables.TRANSACTION_RECEIPT.getRealmString());
                    values.add(Device.Printables.TRANSACTION_RECEIPT_REPRINT.getRealmString());
                    values.add(Device.Printables.REPORTS.getRealmString());
                    DeviceTableDAO.remove(values);
                    device.setSelectedPritables(values);
                    DeviceTableDAO.upsert(device);
                }
            } else {
                Log.e("APT50","Failed to connect....");
            }
    }

    public static void connectStarTS650BT(Context context) {
        try {
            Collection<UsbDevice> usbDevices = getUSBDevices(context);
            if (usbDevices.isEmpty()) {
                return;
            }
            EMSDeviceManager edm = new EMSDeviceManager();
            ArrayList<PortInfo> mPortList = StarIOPort.searchPrinter("USB:", context);
            MyPreferences preferences = new MyPreferences(context);
            if (!mPortList.isEmpty()) {
                String portName = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? "USB:" : mPortList.get(0).getPortName();
                preferences.setPrinterType(Global.STAR);
                preferences.setPrinterName(portName);
                preferences.setPrinterMACAddress(portName);
                preferences.posPrinter(false, true);
                preferences.printerAreaSize(false, 48);
                EMSStar aDevice = new EMSStar();
                Global.mainPrinterManager = edm.getManager();
                aDevice.autoConnect((Activity) context, edm, 48, true, preferences.getPrinterMACAddress(), "");
                List<Device> devices = new ArrayList<>();
                Device device = DeviceTableDAO.getByName(portName);
                if (device == null) {
                    device = new Device();
                }
                device.setTextAreaSize(48);
                device.setEmsDeviceManager(Global.mainPrinterManager);
                device.setId(portName);
                device.setName(portName);
                device.setType(String.valueOf(Global.STAR));
                device.setRemoteDevice(false);
                device.setMacAddress(portName);
                devices.add(device);
                DeviceTableDAO.insert(devices);
                Global.printerDevices.add(device);
            }
        } catch (StarIOPortException e) {
            e.printStackTrace();
        }
    }

    public static EMSDeviceManager getEmsDeviceManager(Device.Printables printables, Set<Device> printerDevices) {
        Device device = DeviceTableDAO.getByPrintable(printables);
        ArrayList<Device> devices = new ArrayList(printerDevices);
        int i = devices.indexOf(device);
        EMSDeviceManager emsDeviceManager = null;
        if (i > -1) {
            emsDeviceManager = devices.get(i).getEmsDeviceManager();
        }
        return emsDeviceManager;
    }

    public static void connectSNBCUSB(Context context, EMSDeviceDriver usbDevice) {
        try {
//            autoConnect((Activity) context, true);
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
                EMSsnbc ssnbc = (EMSsnbc) usbDevice;
                ssnbc.openUsbInterface();
            } else {
                MyPreferences preferences = new MyPreferences(context);
                preferences.setPrinterType(Global.SNBC);
                preferences.posPrinter(false, true);
                preferences.printerAreaSize(false, 48);
                EMSDeviceManager edm = new EMSDeviceManager();
                Global.mainPrinterManager = edm.getManager();
                Global.mainPrinterManager.loadDrivers(context, Global.SNBC, EMSDeviceManager.PrinterInterfase.USB);
                List<Device> devices = new ArrayList<>();
                Device device = DeviceTableDAO.getByName("SNBC");
                if (device == null) {
                    device = new Device();
                }
                device.setTextAreaSize(48);
                device.setName("SNBC");
                device.setId("SNBC");
                device.setType(String.valueOf(Global.SNBC));
                device.setRemoteDevice(false);
                devices.add(device);
                DeviceTableDAO.insert(devices);
                Global.printerDevices.add(device);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void connectHPEngageOnePrimePrinter(Context context, EMSDeviceDriver usbDevice) {
        try {
//            autoConnect((Activity) context, true);
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
                EMSHPEngageOnePrimePrinter hp1Printer = (EMSHPEngageOnePrimePrinter) usbDevice;
//                hp1Printer.initializePrinter();
            } else {
                MyPreferences preferences = new MyPreferences(context);
                preferences.setPrinterType(Global.HP_EONEPRIME);
                preferences.posPrinter(false, true);
                preferences.printerAreaSize(false, 58);
                EMSDeviceManager edm = new EMSDeviceManager();
                Global.mainPrinterManager = edm.getManager();
                Global.mainPrinterManager.loadDrivers(context, Global.HP_EONEPRIME, EMSDeviceManager.PrinterInterfase.USB);
                List<Device> devices = new ArrayList<>();
                Device device = DeviceTableDAO.getByName("HP Engage One Prime Printer");
                if (device == null) {
                    device = new Device();
                }
                device.setTextAreaSize(58);
                device.setName("HP Engage One Prime Printer");
                device.setId("HP_E1PP");
                device.setType(String.valueOf(Global.HP_EONEPRIME));
                device.setRemoteDevice(false);
                devices.add(device);
                DeviceTableDAO.insert(devices);
                Global.printerDevices.add(device);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendBroadcastDeviceConnected(Context context) {
        Intent intent = new Intent(MainMenu_FA.NOTIFICATION_DEVICES_LOADED);
        context.sendBroadcast(intent);
    }

    public static EMSDeviceManager getEmsDeviceManager(int type, Set<Device> printerDevices) {
        Device device = DeviceTableDAO.getByPrintableType(type);
        ArrayList<Device> devices = new ArrayList(printerDevices);
        int i = devices.indexOf(device);
        EMSDeviceManager emsDeviceManager = null;
        if (i > -1) {
            emsDeviceManager = devices.get(i).getEmsDeviceManager();
        }
        return emsDeviceManager;
    }

    private static class ReconnectUSBPrinterTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;
        private Activity activity;

        public ReconnectUSBPrinterTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            if (!Global.isActivityDestroyed(activity)) {
                progressDialog = new ProgressDialog(activity);
                progressDialog.setTitle(R.string.connecting_devices);
                progressDialog.setMessage(activity.getString(R.string.connecting_devices));
                progressDialog.setCanceledOnTouchOutside(true);
                progressDialog.setCancelable(true);
                progressDialog.show();
            }
        }

        @Override
        protected Void doInBackground(Void... none) {
//            autoConnect(activity,true);
            DeviceUtils.connectStarTS650BT(activity);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            Global.dismissDialog(activity, progressDialog);
        }
    }
}
