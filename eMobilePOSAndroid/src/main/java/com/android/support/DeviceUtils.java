package com.android.support;

import android.app.Activity;
import android.text.TextUtils;

import com.android.dao.DeviceTableDAO;
import com.android.emobilepos.models.realms.Device;

import java.util.HashMap;

import io.realm.RealmResults;
import main.EMSDeviceManager;

/**
 * Created by Guarionex on 6/14/2016.
 */
public class DeviceUtils {

    public static String autoConnect(final Activity activity, boolean forceReload) {
        final MyPreferences myPref = new MyPreferences(activity);
        final StringBuilder sb = new StringBuilder();
        RealmResults<Device> devices = DeviceTableDAO.getAll();
        HashMap<String, Integer> tempMap = new HashMap<>();
        EMSDeviceManager edm = null;
        if (forceReload) {
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
                        sb.append(device.getIpAddress()).append(": ").append("Failed to connect\n\r");

                    i++;
                }
            }
        }

        String _portName;
        String _peripheralName;
        if (myPref.getSwiperType() != -1)
            if (Global.btSwiper == null || forceReload) {
                edm = new EMSDeviceManager();
                _portName = myPref.getSwiperMACAddress();
                _peripheralName = Global.getPeripheralName(myPref.getSwiperType());
                Global.btSwiper = edm.getManager();
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
//                if (myPref.getPrinterName().toUpperCase().contains("MPOP") && Global.mainPrinterManager != null) {
//                    EMSBluetoothStarPrinter mpop = (EMSBluetoothStarPrinter) Global.mainPrinterManager.getCurrentDevice();
//                    try {
//                        if (mpop.getPort().retreiveStatus().offline) {
//                            forceReload = true;
//                        }
//                    } catch (StarIOPortException e) {
//                        e.printStackTrace();
//                        forceReload = true;
//                    }
//                }
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
                        sb.append(_peripheralName).append(": ").append("Failed to connect\n\r");
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
                    sb.append(myPref.getStarIPAddress()).append(": ").append("Failed to connect\n\r");
            }

        return sb.toString();
    }
}
