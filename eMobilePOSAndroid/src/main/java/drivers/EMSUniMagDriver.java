package drivers;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.android.emobilepos.R;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import IDTech.MSR.XMLManager.StructConfigParameters;
import IDTech.MSR.uniMag.UniMagTools.uniMagReaderToolsMsg;
import IDTech.MSR.uniMag.UniMagTools.uniMagSDKTools;
import IDTech.MSR.uniMag.uniMagReader;
import IDTech.MSR.uniMag.uniMagReaderMsg;
import interfaces.EMSCallBack;
import util.CardData;


public class EMSUniMagDriver implements uniMagReaderMsg, uniMagReaderToolsMsg {


    private uniMagReader myUniMagReader = null;

    private Activity activity;
    private boolean isSwipping = false;

    private Handler handler = new Handler();
    private boolean isConnected = false;
    private CreditCardInfo cardManager;
    private EMSUniMagDriver callBack;


    public void initializeReader(Activity activity) {
        this.activity = activity;
        cardManager = new CreditCardInfo();
        callBack = this;
        initConnect();
    }

    public void release() {
        myUniMagReader.unregisterListen();
        myUniMagReader.release();
        isSwipping = false;
        isConnected = false;
    }


    private void initConnect() {

        if (myUniMagReader != null) {
            myUniMagReader.unregisterListen();
            myUniMagReader.release();
            myUniMagReader = null;
        }

        myUniMagReader = new uniMagReader(this, activity);

        myUniMagReader.setVerboseLoggingEnable(true);
        myUniMagReader.registerListen();

        // ----File Name Path was hardcoded in order to improve performance

		/*
         * fileNameWithPath = getXMLFileFromRaw();
		 * if(!isFileExist(fileNameWithPath)) { fileNameWithPath = null; }
		 */

        new Thread(new Runnable() {
            public void run() {
                String fileNameWithPath = getConfigurationFileFromRaw();
                myUniMagReader.setXMLFileNameWithPath(fileNameWithPath);
                myUniMagReader.loadingConfigurationXMLFile(true);
                synchronized (myUniMagReader) {
                    myUniMagReader.notifyAll();
                }
            }
        }).start();

        synchronized (myUniMagReader) {
            try {
                myUniMagReader.wait(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            uniMagSDKTools firmwareUpdateTool = new uniMagSDKTools(callBack, activity);
            firmwareUpdateTool.setUniMagReader(myUniMagReader);
            myUniMagReader.setSDKToolProxy(firmwareUpdateTool.getSDKToolProxy());
        }
    }

    private String getConfigurationFileFromRaw() {
        return getXMLFileFromRaw("idt_unimagcfg_default.xml");
    }

    public void startReading() {
        if (myUniMagReader != null) {
//            boolean isWaitingForCommandResult = false;
//            if (!isWaitingForCommandResult) {
            myUniMagReader.startSwipeCard();
//            }
        }
    }


    public boolean readerIsConnected() {
        return isConnected;
    }


    public boolean readerIsSwipping() {
        return isSwipping;
    }


    @Override
    public boolean getUserGrant(int type, String arg1) {
        boolean getUserGranted;
        switch (type) {
            case uniMagReaderMsg.typeToPowerupUniMag:
                //pop up dialog to get the user grant
                getUserGranted = true;
                break;
            case uniMagReaderMsg.typeToUpdateXML:
                //pop up dialog to get the user grant
                getUserGranted = true;
                break;
            case uniMagReaderMsg.typeToOverwriteXML:
                //pop up dialog to get the user grant
                getUserGranted = true;
                break;
            case uniMagReaderMsg.typeToReportToIdtech:
                //pop up dialog to get the user grant
                getUserGranted = true;
                break;
            default:
                getUserGranted = false;
                break;
        }
        return getUserGranted;
    }


    // displays data from card swiping
    private Runnable doUpdateViews = new Runnable() {
        public void run() {
            try {
                EMSCallBack callBack = (EMSCallBack) activity;
                callBack.cardWasReadSuccessfully(true, cardManager);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };


    private Runnable doUpdateDidConnect = new Runnable() {
        public void run() {
            try {
                EMSCallBack callBack = (EMSCallBack) activity;
                callBack.readerConnectedSuccessfully(true);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    @Override
    public void onReceiveMsgAutoConfigCompleted(StructConfigParameters arg0) {
    }


    @Override
    public void onReceiveMsgAutoConfigProgress(int arg0) {

    }


    @Override
    public void onReceiveMsgAutoConfigProgress(int arg0, double arg1, String arg2) {
    }


    @Override
    public void onReceiveMsgCardData(byte flagOfCardData, byte[] cardData) {

        byte flag = (byte) (flagOfCardData & 0x04);


        String strMsrData;
        if (flag == 0x00) {
            strMsrData = new String(cardData);
        }
        if (flag == 0x04) {
            //You need to decrypt the data here first.
            strMsrData = new String(cardData);
        }


        this.cardManager = parseCardData(activity, cardData);
        isSwipping = false;
        handler.post(doUpdateViews);
    }

    public static CreditCardInfo parseCardData(Activity activity, byte[] cardData) {
        CreditCardInfo cardManager = new CreditCardInfo();
        CardData cd = new CardData(cardData);
        StringBuilder tracks = new StringBuilder();

        if (cd.getT1DataAscii() == null && (cd.getT2DataAscii() == null)) {
            return cardManager;
        } else {
            if (cd.isDataEncrypted() && cd.getT2Encrypted() != null) {
                Encrypt encrypt = new Encrypt(activity);
                cardManager.setTrackDataKSN(cd.getKSN());
                cardManager.setEncryptedAESTrack1(encrypt.encryptWithAES(cd.getT1DataAscii()));
                cardManager.setEncryptedAESTrack2(encrypt.encryptWithAES(cd.getT2DataAscii()));
                StringBuilder sb = new StringBuilder();
                //cardManager.setEncryptedBlock(sb.append(cd.getT1Encrypted()).append(cd.getT2Encrypted()).toString());
                cardManager.setEncryptedTrack1(cd.getT1Encrypted());
                cardManager.setEncryptedTrack2(cd.getT2Encrypted());
                cardManager.setDeviceSerialNumber(cd.getSerialNumber());

                String tr1 = cd.getT1DataAscii() == null ? "" : cd.getT1DataAscii();
                String tr2 = cd.getT2DataAscii() == null ? "" : cd.getT2DataAscii();
                CreditCardInfo temp = Global.parseSimpleMSR(activity, tr1.replace("%*", "%").replace("?*", "?") + tr2.replace("?*", "?"));
                cardManager.setCardExpMonth(temp.getCardExpMonth());
                cardManager.setCardExpYear(temp.getCardExpYear());
                cardManager.setCardLast4(temp.getCardLast4());
                cardManager.setCardOwnerName(temp.getCardOwnerName());
                cardManager.setCardType(temp.getCardType());
                cardManager.setCardNumAESEncrypted(temp.getCardNumAESEncrypted());

            } else {
                if (cd.getT1DataAscii() != null && cd.getT1Data().length() > 0 && cd.getT2Data().length() == 0 && cd.getT1DataAscii().contains(";"))
                    tracks.append("").append(cd.getT1DataAscii());
                else if (cd.getT1DataAscii() != null && cd.getT1Data().length() > 0 && (cd.getT2Data() == null || cd.getT2Data().length() == 0)
                        && cd.getT1DataAscii().contains("%"))
                    tracks.append(cd.getT1DataAscii()).append("");
                else
                    tracks.append(cd.getT1DataAscii() == null ? "" : cd.getT1DataAscii()).append(cd.getT2DataAscii() == null ? "" : cd.getT2DataAscii());

                cardManager = Global.parseSimpleMSR(activity, tracks.toString());
            }

            return cardManager;
        }
    }

    @Override
    public void onReceiveMsgCommandResult(int arg0, byte[] arg1) {
    }


    @Override
    public void onReceiveMsgConnected() {
        isConnected = true;
        handler.post(doUpdateDidConnect);
    }


    @Override
    public void onReceiveMsgDisconnected() {
        isConnected = false;
    }


    @Override
    public void onReceiveMsgFailureInfo(int arg0, String arg1) {
    }


    @Override
    @Deprecated
    public void onReceiveMsgSDCardDFailed(String arg0) {
    }


    @Override
    public void onReceiveMsgTimeout(String arg0) {
        isConnected = false;
    }


    @Override
    public void onReceiveMsgToConnect() {
    }

    @Override
    public void onReceiveMsgProcessingCardData() {
    }

    @Override
    public void onReceiveMsgToCalibrateReader() {

    }


    @Override
    public void onReceiveMsgToSwipeCard() {
        isSwipping = true;
    }


    @Override
    public void onReceiveMsgChallengeResult(int arg0, byte[] arg1) {
    }


    @Override
    public void onReceiveMsgUpdateFirmwareProgress(int arg0) {
    }


    @Override
    public void onReceiveMsgUpdateFirmwareResult(int arg0) {
    }

    // If 'idt_unimagcfg_default.xml' file is found in the 'raw' folder, it returns the file path.
    private String getXMLFileFromRaw(String fileName) {
        //the target filename in the application path
        String fileNameWithPath = null;
        fileNameWithPath = fileName;

        try {
            InputStream in = activity.getResources().openRawResource(R.raw.idt_unimagcfg_default);
            int length = in.available();
            byte[] buffer = new byte[length];
            in.read(buffer);
            in.close();
            activity.deleteFile(fileNameWithPath);
            FileOutputStream fout = activity.openFileOutput(fileNameWithPath, Context.MODE_PRIVATE);
            fout.write(buffer);
            fout.close();

            // to refer to the application path
            File fileDir = activity.getFilesDir();
            fileNameWithPath = fileDir.getParent() + java.io.File.separator + fileDir.getName();
            fileNameWithPath += java.io.File.separator + "idt_unimagcfg_default.xml";

        } catch (Exception e) {
            e.printStackTrace();
            fileNameWithPath = null;
        }
        return fileNameWithPath;
    }

}
