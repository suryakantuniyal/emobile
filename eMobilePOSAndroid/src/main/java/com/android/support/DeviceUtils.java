package com.android.support;

import android.app.Activity;
import android.text.TextUtils;

import com.android.dao.DeviceTableDAO;
import com.android.emobilepos.models.Device;

import java.util.HashMap;

import io.realm.RealmResults;
import main.EMSDeviceManager;

/**
 * Created by Guarionex on 6/14/2016.
 */
public class DeviceUtils {

    public static String autoConnect(Activity activity, boolean forceReload) {
        MyPreferences myPref = new MyPreferences(activity);
        StringBuilder sb = new StringBuilder();
        RealmResults<Device> devices = DeviceTableDAO.getAll();
        HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
        EMSDeviceManager edm;
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
                _portName = myPref.swiperMACAddress(true, null);
                _peripheralName = Global.getPeripheralName(myPref.getSwiperType());
                Global.btSwiper = edm.getManager();
                if (Global.btSwiper.loadMultiDriver(activity, myPref.getSwiperType(), 0, false,
                        myPref.swiperMACAddress(true, null), null))
                    sb.append(_peripheralName).append(": ").append("Connected\n\r");
                else
                    sb.append(_peripheralName).append(": ").append("Failed to connect\n\r");
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
        if ((myPref.getPrinterType() != -1)) {
            _peripheralName = Global.getPeripheralName(myPref.getPrinterType());
            _portName = myPref.getPrinterMACAddress();
            String _portNumber = myPref.getStarPort();
            boolean isPOS = myPref.posPrinter(true, false);
            int txtAreaSize = myPref.printerAreaSize(true, -1);
            if (myPref.isPAT215()) {
                edm = new EMSDeviceManager();
                Global.embededMSR = edm.getManager();
                if (Global.embededMSR.loadMultiDriver(activity, Global.PAT215, 0, false, "", "")) {
                    sb.append(Global.BuildModel.PAT215.name()).append(": ").append("Connected\n\r");
                } else {
                    sb.append(Global.BuildModel.PAT215.name()).append(": ").append("Failed to connect\n\r");
                }
            }
            if (myPref.getPrinterType() != Global.POWA) {
                edm = new EMSDeviceManager();
                Global.mainPrinterManager = edm.getManager();
                if (Global.mainPrinterManager.loadMultiDriver(activity, myPref.getPrinterType(), txtAreaSize,
                        isPOS, _portName, _portNumber))
                    sb.append(_peripheralName).append(": ").append("Connected\n\r");
                else
                    sb.append(_peripheralName).append(": ").append("Failed to connect\n\r");
            }

        } else if (!TextUtils.isEmpty(myPref.getStarIPAddress())) {
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
