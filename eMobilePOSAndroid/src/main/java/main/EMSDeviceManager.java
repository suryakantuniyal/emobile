package main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.emobilepos.R;
import com.android.support.DeviceUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;

import drivers.EMSAsura;
import drivers.EMSBixolon;
import drivers.EMSBixolonRD;
import drivers.EMSBlueBambooP25;
import drivers.EMSBluetoothStarPrinter;
import drivers.EMSDeviceDriver;
import drivers.EMSELO;
import drivers.EMSEM100;
import drivers.EMSEM70;
import drivers.EMSHandpoint;
import drivers.EMSIngenico;
import drivers.EMSIngenicoEVO;
import drivers.EMSKDC425;
import drivers.EMSMagtekAudioCardReader;
import drivers.EMSMiura;
import drivers.EMSNomad;
import drivers.EMSOT310;
import drivers.EMSOneil4te;
import drivers.EMSPAT100;
import drivers.EMSPAT215;
import drivers.EMSPowaPOS;
import drivers.EMSZebraEM220ii;
import drivers.EMSmePOS;
import drivers.EMSsnbc;
import interfaces.EMSConnectionDelegate;
import interfaces.EMSDeviceManagerPrinterDelegate;
import interfaces.EMSPrintingDelegate;

public class EMSDeviceManager implements EMSPrintingDelegate, EMSConnectionDelegate {

    private Dialog promptDialog;
    private AlertDialog.Builder dialogBuilder;
    private Context activity;

    private EMSDeviceDriver aDevice = null;
    private EMSDeviceManager instance;
    private EMSDeviceManagerPrinterDelegate currentDevice;

    public EMSDeviceManager() {
        instance = this;
    }

    public EMSDeviceManager getManager() {
        return instance;
    }

    public void loadDrivers(Context activity, int type, PrinterInterfase interfase) {

        this.activity = activity;

        switch (type) {
            case Global.MAGTEK:
                aDevice = new EMSMagtekAudioCardReader();
                aDevice.connect(activity, -1, false, instance);
                break;
            case Global.STAR:
                aDevice = new EMSBluetoothStarPrinter();
                if (interfase == PrinterInterfase.BLUETOOTH)
                    promptTypeOfStarPrinter();
                else
                    promptStarPrinterSize(true);
                break;
            case Global.BIXOLON_RD:
                aDevice = new EMSBixolonRD(EMSBixolonRD.BixolonCountry.DOMINICAN_REPUBLIC);
                aDevice.connect(activity, -1, true, instance);
                break;
            case Global.BAMBOO:
                aDevice = new EMSBlueBambooP25();
                aDevice.connect(activity, -1, false, instance);
                break;
            case Global.BIXOLON:
                aDevice = new EMSBixolon();
                aDevice.connect(activity, -1, false, instance);
                break;
            case Global.MIURA:
                aDevice = new EMSMiura();
                aDevice.connect(activity, -1, false, instance);
                break;
            case Global.ZEBRA:
                aDevice = new EMSZebraEM220ii();
                aDevice.connect(activity, -1, false, instance);
                break;
            case Global.ONEIL:
                aDevice = new EMSOneil4te();
                aDevice.connect(activity, -1, false, instance);
                break;
            case Global.SNBC:
                aDevice = new EMSsnbc();
                aDevice.connect(activity, -1, true, instance);
                break;
            case Global.POWA:
                aDevice = new EMSPowaPOS();
                aDevice.connect(activity, -1, true, instance);
                break;
            case Global.ASURA:
                aDevice = new EMSAsura();
                aDevice.connect(activity, -1, true, instance);
                break;
            case Global.PAT100:
                aDevice = new EMSPAT100();
                aDevice.connect(activity, -1, true, instance);
                break;
            case Global.PAT215:
                aDevice = new EMSPAT215();
                aDevice.connect(activity, -1, true, instance);
                break;
            case Global.EM100:
                aDevice = new EMSEM100();
                aDevice.connect(activity, -1, false, instance);
                break;
            case Global.EM70:
                aDevice = new EMSEM70();
                aDevice.connect(activity, -1, false, instance);
                break;
            case Global.OT310:
                aDevice = new EMSOT310();
                aDevice.connect(activity, -1, false, instance);
                break;
            case Global.KDC425:
                aDevice = new EMSKDC425();
                aDevice.connect(activity, -1, false, instance);
                break;
            case Global.HANDPOINT:
                aDevice = new EMSHandpoint();
                aDevice.connect(activity, -1, false, instance);
                break;
            case Global.NOMAD:
                aDevice = new EMSNomad();
                aDevice.connect(activity, -1, false, instance);
                break;
            case Global.MEPOS:
                aDevice = new EMSmePOS();
                aDevice.connect(activity, -1, false, instance);
                break;
            case Global.ELOPAYPOINT:
                aDevice = new EMSELO();
                aDevice.connect(activity, -1, true, instance);
                break;
            case Global.ISMP:
                aDevice = new EMSIngenico();
                aDevice.connect(activity, -1, false, instance);
                break;
            case Global.ICMPEVO:
                aDevice = new EMSIngenicoEVO();
                aDevice.connect(activity, -1, false, instance);
                break;
        }
    }


    public boolean loadMultiDriver(Activity activity, int type, int paperSize, boolean isPOSPrinter, String portName, String portNumber) {
        this.activity = activity;
        switch (type) {
            case Global.MAGTEK:
                aDevice = new EMSMagtekAudioCardReader();
                break;
            case Global.BAMBOO:
                aDevice = new EMSBlueBambooP25();
                break;
            case Global.BIXOLON:
                aDevice = new EMSBixolon();
                break;
            case Global.BIXOLON_RD:
                aDevice = new EMSBixolonRD(EMSBixolonRD.BixolonCountry.DOMINICAN_REPUBLIC);
                break;
            case Global.ZEBRA:
                aDevice = new EMSZebraEM220ii();
                break;
            case Global.ONEIL:
                aDevice = new EMSOneil4te();
                break;
            case Global.SNBC:
                aDevice = new EMSsnbc();
                break;
            case Global.POWA:
                aDevice = new EMSPowaPOS();
                break;
            case Global.EM100:
                aDevice = new EMSEM100();
                break;
            case Global.ASURA:
                aDevice = new EMSAsura();
                break;
            case Global.PAT100:
                aDevice = new EMSPAT100();
                break;
            case Global.PAT215:
                aDevice = new EMSPAT215();
                break;
            case Global.ISMP:
                aDevice = new EMSIngenico();
                break;
            case Global.STAR:
                aDevice = new EMSBluetoothStarPrinter();
                break;
            case Global.EM70:
                aDevice = new EMSEM70();
                break;
            case Global.OT310:
                aDevice = new EMSOT310();
                break;
            case Global.KDC425:
                aDevice = new EMSKDC425();
                break;
            case Global.HANDPOINT:
                aDevice = new EMSHandpoint();
                break;
            case Global.MEPOS:
                aDevice = new EMSmePOS();
                break;
            case Global.NOMAD:
                aDevice = new EMSNomad();
                break;
            case Global.ELOPAYPOINT:
                aDevice = new EMSELO();
                break;
            case Global.ICMPEVO:
                aDevice = new EMSIngenicoEVO();
                break;
            case Global.MIURA:
                aDevice = new EMSMiura();
                break;
        }
        return aDevice != null && aDevice.autoConnect(activity, instance, paperSize, isPOSPrinter, portName, portNumber);

    }


    private void promptTypeOfStarPrinter() {
        ListView listViewPrinterType = new ListView(activity);
        ArrayAdapter<String> typesAdapter;
        dialogBuilder = new AlertDialog.Builder(activity);

        String[] values = new String[]{"Thermal POS Printer", "Portable Printer"};
        typesAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, values);

        listViewPrinterType.setAdapter(typesAdapter);
        listViewPrinterType.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                                    long arg3) {
                promptDialog.dismiss();
                if (pos == 0)
                    promptStarPrinterSize(true);
                else
                    promptStarPrinterSize(false);
            }
        });

        dialogBuilder.setView(listViewPrinterType);

        dialogBuilder.setView(listViewPrinterType);
        dialogBuilder.setTitle("Choose Printer Type");
        promptDialog = dialogBuilder.create();

        promptDialog.show();
    }


    public void promptStarPrinterSize(final boolean isPOSPrinter) {
        ListView listViewPaperSizes = new ListView(activity);
        ArrayAdapter<String> bondedAdapter;
        dialogBuilder = new AlertDialog.Builder(activity);

        String[] values = new String[]{"2inch (58mm)", "3inch (78mm)", "4inch (112mm)"};
        final int[] paperSize = new int[]{32, 48, 69};
        bondedAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, values);

        listViewPaperSizes.setAdapter(bondedAdapter);

        listViewPaperSizes.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                promptDialog.dismiss();
                MyPreferences myPref = new MyPreferences(activity);
                myPref.posPrinter(false, isPOSPrinter);
                myPref.printerAreaSize(false, paperSize[position]);
                aDevice.connect(activity, paperSize[position], isPOSPrinter, instance);

            }
        });

        dialogBuilder.setView(listViewPaperSizes);

        dialogBuilder.setView(listViewPaperSizes);
        dialogBuilder.setTitle("Choose Printable Area Size");
        promptDialog = dialogBuilder.create();

        promptDialog.show();
    }

    public EMSDeviceManagerPrinterDelegate getCurrentDevice() {
        return currentDevice;
    }

    public void setCurrentDevice(EMSDeviceManagerPrinterDelegate currentDevice) {
        this.currentDevice = currentDevice;
    }

    public void printerDidFinish() {

    }

    public void printerDidDisconnect(Error err) {

    }

    public void printerDidBegin() {

    }

    public void driverDidConnectToDevice(EMSDeviceDriver theDevice, boolean showPrompt) {
        boolean isDestroyed = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity != null && this.activity instanceof Activity) {
                if (((Activity) this.activity).isDestroyed()) {
                    isDestroyed = true;
                }
            }
        }
        if (activity != null && this.activity instanceof Activity) {
            if (showPrompt && !((Activity) this.activity).isFinishing() && !isDestroyed) {
                Builder dialog = new Builder(this.activity);
                dialog.setNegativeButton(R.string.button_ok, null);
                AlertDialog alert = dialog.create();
                alert.setTitle(R.string.dlog_title_confirm);
                alert.setMessage("Device is Online");
                alert.show();
            }
        }
        if (activity != null) {
            DeviceUtils.sendBroadcastDeviceConnected(activity);
        }
        theDevice.registerAll();

    }

    public void driverDidDisconnectFromDevice(EMSDeviceDriver theDevice, boolean showPrompt) {

    }

    public void driverDidNotConnectToDevice(EMSDeviceDriver theDevice, String err, boolean showPrompt) {

        boolean isDestroyed = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (this.activity instanceof Activity) {
                if (((Activity) this.activity).isDestroyed()) {
                    isDestroyed = true;
                }
            }
        }
        if (this.activity instanceof Activity) {
            if (showPrompt && !((Activity) this.activity).isFinishing() && !isDestroyed) {
                Builder dialog = new Builder(this.activity);
                dialog.setNegativeButton("Ok", null);
                AlertDialog alert = dialog.create();
                alert.setTitle(R.string.dlog_title_error);
                alert.setMessage("Failed to connectTFHKA device: \n" + err);
                alert.show();
            }
        }
    }

    public enum PrinterInterfase {
        USB, BLUETOOTH, TCP
    }
}
