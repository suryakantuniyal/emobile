package com.android.support;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.dao.DeviceTableDAO;
import com.android.emobilepos.models.realms.Device;
import com.crashlytics.android.Crashlytics;
import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import drivers.EMSBluetoothStarPrinter;
import drivers.EMSDeviceDriver;
import drivers.EMSPowaPOS;
import drivers.EMSmePOS;
import main.EMSDeviceManager;

/**
 * Created by Guarionex on 6/14/2016.
 */
public class DeviceUtils {

    private static BroadcastReceiver fingerPrintbroadcastReceiver = null;

    public static String autoConnect(final Activity activity, boolean forceReload) {
        final MyPreferences myPref = new MyPreferences(activity);
        final StringBuilder sb = new StringBuilder();
        List<Device> devices = DeviceTableDAO.getAll();
        HashMap<String, Integer> tempMap = new HashMap<>();
        EMSDeviceManager edm = null;
        connectStarTS650BT(activity);
        if (forceReload || Global.multiPrinterMap.size() != devices.size()) {
            int i = 0;
            for (Device device : devices) {
                if (tempMap.containsKey(device.getId())) {
                    Global.multiPrinterMap.put(device.getCategoryId(), tempMap.get(device.getId()));
                } else {
                    tempMap.put(device.getId(), i);
                    Global.multiPrinterMap.put(device.getCategoryId(), i);

                    edm = new EMSDeviceManager();
                    Global.multiPrinterManager.add(edm);

                    if (Global.multiPrinterManager.get(i).loadMultiDriver(activity, Global.STAR, 48, true,
                            "TCP:" + device.getIpAddress(), device.getTcpPort()))
                        sb.append(device.getIpAddress()).append(": ").append("Connected\n\r");
                    else
                        sb.append(device.getIpAddress()).append(": ").append("Failed to connectTFHKA\n\r");

                    i++;
                }
            }
        }
        EMSDeviceDriver usbDevice = getUSBDeviceDriver(activity);
        if (usbDevice instanceof EMSPowaPOS) {
            myPref.setIsMEPOS(false);
            myPref.setIsPOWA(true);
        }
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
                                sb.append(final_peripheralName).append(": ").append("Failed to connectTFHKA\n\r");
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
                        sb.append(_peripheralName).append(": ").append("Failed to connectTFHKA\n\r");
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
                    sb.append(_peripheralName).append(": ").append("Failed to connectTFHKA\n\r");
            }
        if (myPref.isPAT215()) {
            if (Global.embededMSR == null || forceReload) {
                edm = new EMSDeviceManager();
                Global.embededMSR = edm.getManager();
                if (Global.embededMSR.loadMultiDriver(activity, Global.PAT215, 0, false, "", "")) {
                    sb.append(Global.BuildModel.PAT215.name()).append(": ").append("Connected\n\r");
                } else {
                    sb.append(Global.BuildModel.PAT215.name()).append(": ").append("Failed to connectTFHKA\n\r");
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
                    && myPref.getPrinterType() != Global.MIURA
                    && myPref.getPrinterType() != Global.ELOPAYPOINT
                    && myPref.getPrinterType() != Global.PAT215) {
                if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
                    if (!Global.mainPrinterManager.getCurrentDevice().isConnected()) {
                        forceReload = true;
                    }
                }
                if (Global.mainPrinterManager == null || Global.mainPrinterManager.getCurrentDevice() == null
                        || forceReload) {
                    if (Global.mainPrinterManager == null) {
                        edm = new EMSDeviceManager();
                        Global.mainPrinterManager = edm.getManager();
                    }
                    if (Global.mainPrinterManager.loadMultiDriver(activity, myPref.getPrinterType(), txtAreaSize,
                            isPOS, _portName, _portNumber))
                        sb.append(_peripheralName).append(": ").append("Connected\n\r");
                    else
                        sb.append(_peripheralName).append(": ").append("Failed to connectTFHKA\n\r");
                    Global.multiPrinterManager.add(edm);
                }
            }
        } else if (!TextUtils.isEmpty(myPref.getStarIPAddress()))
            if (Global.mainPrinterManager == null || forceReload) {
                edm = new EMSDeviceManager();
                Global.mainPrinterManager = edm.getManager();

                if (Global.mainPrinterManager.loadMultiDriver(activity, Global.STAR, 48, true,
                        "TCP:" + myPref.getStarIPAddress(), myPref.getStarPort()))
                    sb.append(myPref.getStarIPAddress()).append(": ").append("Connected\n\r");
                else
                    sb.append(myPref.getStarIPAddress()).append(": ").append("Failed to connectTFHKA\n\r");
            }
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
                case 22321:
                case 21541:
                    Log.d("USB POWA detected:", "true");
                    preferences.setIsPOWA(true);
                    preferences.setPrinterType(Global.POWA);
                    return new EMSPowaPOS();
                case 9220:
                    preferences.setIsMEPOS(true);
                    return new EMSmePOS();
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

         fingerPrintbroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean connected;
                if (intent.getAction().contains("ATTACHED")) {
                    Toast.makeText(context, "USB connected", Toast.LENGTH_SHORT).show();
                    connected = true;
                } else if (intent.getAction().contains("DETACHED")) {
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    connected = false;
                }
            }
        };
        context.getApplicationContext().registerReceiver(fingerPrintbroadcastReceiver, intentFilter);
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
                preferences.setPrinterType(Global.STAR);
                preferences.setPrinterName(mPortList.get(0).getPortName());
                preferences.setPrinterMACAddress(mPortList.get(0).getPortName());
                preferences.posPrinter(false, true);
                preferences.printerAreaSize(false, 48);
                EMSBluetoothStarPrinter aDevice = new EMSBluetoothStarPrinter();
                Global.mainPrinterManager = edm.getManager();
                aDevice.autoConnect((Activity) context, edm, 48, true, preferences.getPrinterMACAddress(), "");
//                Global.mainPrinterManager.loadDrivers(context, Global.STAR, EMSDeviceManager.PrinterInterfase.USB);
            }
        } catch (StarIOPortException e) {
            e.printStackTrace();
        }
    }
}
