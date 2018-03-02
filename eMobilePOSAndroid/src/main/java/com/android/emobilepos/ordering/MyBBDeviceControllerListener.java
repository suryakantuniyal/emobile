package com.android.emobilepos.ordering;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.widget.Toast;

import com.bbpos.bbdevice.BBDeviceController;
import com.bbpos.bbdevice.BBDeviceController.BBDeviceControllerListener;
import com.bbpos.bbdevice.CAPK;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by Luis Camayd on 3/2/2018.
 */

public class MyBBDeviceControllerListener implements BBDeviceControllerListener {

    private Context context;
    private BarcodeCallback callback;

    public MyBBDeviceControllerListener(Context context, BarcodeCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public interface BarcodeCallback {
        void onDataScanned(String data);
    }

    @Override
    public void onBarcodeReaderConnected() {
        toastIt("@@@@@@@@ CONNECTED!");
    }

    @Override
    public void onBarcodeReaderDisconnected() {
        toastIt("@@@@@@@@ DISCONNECTED!");
    }

    @Override
    public void onReturnBarcode(String s) {
        toastIt("@@@@@@@@ DATA: " + s);
        callback.onDataScanned(s);
    }

    @Override
    public void onError(BBDeviceController.Error error, String s) {
        Toast.makeText(context, "@@@@@@@@ ERROR: " + s, Toast.LENGTH_LONG).show();
    }

    private void toastIt(String message) {
//        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onWaitingForCard(BBDeviceController.CheckCardMode checkCardMode) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onWaitingReprintOrPrintNext() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onBTReturnScanResults(List<BluetoothDevice> list) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onBTScanTimeout() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onBTScanStopped() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onBTConnected(BluetoothDevice bluetoothDevice) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onBTDisconnected() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onUsbConnected() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onUsbDisconnected() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onSerialConnected() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onSerialDisconnected() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnCheckCardResult(BBDeviceController.CheckCardResult checkCardResult, Hashtable<String, String> hashtable) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnCancelCheckCardResult(boolean b) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnDeviceInfo(Hashtable<String, String> hashtable) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnTransactionResult(BBDeviceController.TransactionResult transactionResult) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnBatchData(String s) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnReversalData(String s) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnAmountConfirmResult(boolean b) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnPinEntryResult(BBDeviceController.PinEntryResult pinEntryResult, Hashtable<String, String> hashtable) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnPrintResult(BBDeviceController.PrintResult printResult) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnAccountSelectionResult(BBDeviceController.AccountSelectionResult accountSelectionResult, int i) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnAmount(Hashtable<String, String> hashtable) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnUpdateAIDResult(Hashtable<String, BBDeviceController.TerminalSettingStatus> hashtable) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnUpdateGprsSettingsResult(boolean b, Hashtable<String, BBDeviceController.TerminalSettingStatus> hashtable) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnUpdateTerminalSettingResult(BBDeviceController.TerminalSettingStatus terminalSettingStatus) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnUpdateWiFiSettingsResult(boolean b, Hashtable<String, BBDeviceController.TerminalSettingStatus> hashtable) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnReadAIDResult(Hashtable<String, Object> hashtable) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnReadGprsSettingsResult(boolean b, Hashtable<String, Object> hashtable) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnReadTerminalSettingResult(Hashtable<String, Object> hashtable) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnReadWiFiSettingsResult(boolean b, Hashtable<String, Object> hashtable) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnEnableAccountSelectionResult(boolean b) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnEnableInputAmountResult(boolean b) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnCAPKList(List<CAPK> list) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnCAPKDetail(CAPK capk) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnCAPKLocation(String s) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnUpdateCAPKResult(boolean b) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnRemoveCAPKResult(boolean b) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnEmvReportList(Hashtable<String, String> hashtable) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnEmvReport(String s) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnDisableAccountSelectionResult(boolean b) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnDisableInputAmountResult(boolean b) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnPhoneNumber(BBDeviceController.PhoneEntryResult phoneEntryResult, String s) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnEmvCardDataResult(boolean b, String s) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnEmvCardNumber(boolean b, String s) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnEncryptPinResult(boolean b, Hashtable<String, String> hashtable) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnEncryptDataResult(boolean b, Hashtable<String, String> hashtable) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnInjectSessionKeyResult(boolean b, Hashtable<String, String> hashtable) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnPowerOnIccResult(boolean b, String s, String s1, int i) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnPowerOffIccResult(boolean b) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnApduResult(boolean b, Hashtable<String, Object> hashtable) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onRequestSelectApplication(ArrayList<String> arrayList) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onRequestSetAmount() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onRequestPinEntry(BBDeviceController.PinEntrySource pinEntrySource) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onRequestOnlineProcess(String s) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onRequestTerminalTime() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onRequestDisplayText(BBDeviceController.DisplayText displayText) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onRequestDisplayAsterisk(int i) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onRequestDisplayLEDIndicator(BBDeviceController.ContactlessStatus contactlessStatus) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onRequestProduceAudioTone(BBDeviceController.ContactlessStatusTone contactlessStatusTone) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onRequestClearDisplay() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onRequestFinalConfirm() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onRequestPrintData(int i, boolean b) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onPrintDataCancelled() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onPrintDataEnd() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onBatteryLow(BBDeviceController.BatteryStatus batteryStatus) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onAudioDevicePlugged() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onAudioDeviceUnplugged() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onSessionInitialized() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onSessionError(BBDeviceController.SessionError sessionError, String s) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onAudioAutoConfigProgressUpdate(double v) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onAudioAutoConfigCompleted(boolean b, String s) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onAudioAutoConfigError(BBDeviceController.AudioAutoConfigError audioAutoConfigError) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onNoAudioDeviceDetected() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onDeviceHere(boolean b) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onPowerDown() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onPowerButtonPressed() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onDeviceReset() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onEnterStandbyMode() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnNfcDataExchangeResult(boolean b, Hashtable<String, String> hashtable) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnNfcDetectCardResult(BBDeviceController.NfcDetectCardResult nfcDetectCardResult, Hashtable<String, Object> hashtable) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnControlLEDResult(boolean b, String s) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnVasResult(BBDeviceController.VASResult vasResult, Hashtable<String, Object> hashtable) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onRequestStartEmv() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onDeviceDisplayingPrompt() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onRequestKeypadResponse() {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReturnDisplayPromptResult(BBDeviceController.DisplayPromptResult displayPromptResult) {
        toastIt(Thread.currentThread().getStackTrace()[2].getMethodName());
    }
}