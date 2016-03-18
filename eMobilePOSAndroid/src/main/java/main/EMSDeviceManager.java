package main;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.emobilepos.R;
import com.android.support.Global;
import com.android.support.MyPreferences;

import drivers.EMSAsura;
import drivers.EMSBlueBambooP25;
import drivers.EMSBluetoothStarPrinter;
import drivers.EMSDeviceDriver;
import drivers.EMSEM100;
import drivers.EMSHandpoint;
import drivers.EMSIngenico;
import drivers.EMSIngenicoEVO;
import drivers.EMSKDC500;
import drivers.EMSMagtekAudioCardReader;
import drivers.EMSOT310;
import drivers.EMSOneil4te;
import drivers.EMSPAT100;
import drivers.EMSPowaPOS;
import drivers.EMSZebraEM220ii;
import drivers.EMSsnbc;
import drivers.EMSEM70;
import drivers.EMSELO;
import interfaces.EMSConnectionDelegate;
import interfaces.EMSDeviceManagerPrinterDelegate;
import interfaces.EMSPrintingDelegate;

public class EMSDeviceManager implements EMSPrintingDelegate, EMSConnectionDelegate {

    private Dialog promptDialog;
    private AlertDialog.Builder dialogBuilder;
    private Activity activity;

    EMSDeviceDriver aDevice = null;
    private EMSDeviceManager instance;

	/* Singleton */
    //private static EMSDeviceManager instance = new EMSDeviceManager();

    public EMSDeviceManager() {
        instance = this;
        return;
    }


    public EMSDeviceManager getManager() {

        //instance = new EMSDeviceManager();
        return instance;
    }
    /*
	public  EMSDeviceManager getInstance()
	{
		return instance;
	}
	*/
	
	/* Generic */


    public void loadDrivers(Activity activity, int type, boolean isTCP) {

        this.activity = activity;

        switch (type) {
            case Global.MAGTEK:
                aDevice = new EMSMagtekAudioCardReader();
                aDevice.connect(activity, -1, false, instance);
                break;
            case Global.STAR:
                aDevice = new EMSBluetoothStarPrinter();
                if (!isTCP)
                    promptTypeOfStarPrinter();
                else
                    promptStarPrinterSize(true);
                break;
            case Global.BAMBOO:
                aDevice = new EMSBlueBambooP25();
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
            case Global.KDC500:
                aDevice = new EMSKDC500();
                aDevice.connect(activity, -1, false, instance);
                break;
            case Global.HANDPOINT:
                aDevice = new EMSHandpoint();
                aDevice.connect(activity, -1, false, instance);
                break;
            case Global.ESY13P1:
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
            case Global.KDC500:
                aDevice = new EMSKDC500();
                break;
            case Global.HANDPOINT:
                aDevice = new EMSHandpoint();
                break;
            case Global.ESY13P1:
                aDevice = new EMSELO();
                break;
            case Global.ICMPEVO:
                aDevice = new EMSIngenicoEVO();
                break;
        }
        if (aDevice != null)
            return aDevice.autoConnect(activity, instance, paperSize, isPOSPrinter, portName, portNumber);

        return false;
    }


    private void promptTypeOfStarPrinter() {
        ListView listViewPrinterType = new ListView(activity);
        ArrayAdapter<String> typesAdapter;
        dialogBuilder = new AlertDialog.Builder(activity);

        String[] values = new String[]{"Thermal POS Printer", "Portable Printer"};
        typesAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, values);

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
        bondedAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, values);

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


    /* Printer */
    public EMSPrintingDelegate printingDelegate;
    public EMSDeviceManagerPrinterDelegate currentDevice;

    public void printerDidFinish() {

    }

    public void printerDidDisconnect(Error err) {

    }

    public void printerDidBegin() {

    }

    public void driverDidConnectToDevice(EMSDeviceDriver theDevice, boolean showPrompt) {
        if (showPrompt) {
            Builder dialog = new AlertDialog.Builder(this.activity);
            dialog.setNegativeButton(R.string.button_ok, null);
            AlertDialog alert = dialog.create();
            alert.setTitle(R.string.dlog_title_confirm);
            alert.setMessage("Device is Online");
            alert.show();
        }

        theDevice.registerAll();
    }

    public void driverDidDisconnectFromDevice(EMSDeviceDriver theDevice, boolean showPrompt) {

    }

    public void driverDidNotConnectToDevice(EMSDeviceDriver theDevice, String err, boolean showPrompt) {

        if (showPrompt) {
            Builder dialog = new AlertDialog.Builder(this.activity);
            dialog.setNegativeButton("Ok", null);
            AlertDialog alert = dialog.create();
            alert.setTitle(R.string.dlog_title_error);
            alert.setMessage("Failed to connect device: \n" + err);
            alert.show();
        }


    }
}
