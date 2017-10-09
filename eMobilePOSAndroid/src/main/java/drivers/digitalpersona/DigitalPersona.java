package drivers.digitalpersona;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.android.dao.EmobileBiometricDAO;
import com.android.emobilepos.customer.ViewCustomerDetails_FA;
import com.android.emobilepos.models.realms.EmobileBiometric;
import com.crashlytics.android.Crashlytics;
import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbException;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbHost;

import interfaces.BiometricCallbacks;

public class DigitalPersona {

    private int spleepTime = 100;
    private Reader reader;
    private boolean stopFingerReader;
    private Context context;
    private BiometricCallbacks callbacks;

    public DigitalPersona(Context context, BiometricCallbacks callbacks) {

        this.context = context;
        this.callbacks = callbacks;
    }

    public void loadForScan() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int dpi;
                    Engine engine;
                    ReaderCollection readers;
                    Thread.sleep(spleepTime);
                    readers = UareUGlobal.GetReaderCollection(context);
                    readers.GetReaders();
                    if (readers.size() > 0) {
                        reader = readers.get(0);
                    }
                    PendingIntent mPermissionIntent;
                    mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ViewCustomerDetails_FA.ACTION_USB_PERMISSION), 0);
                    IntentFilter filter = new IntentFilter(ViewCustomerDetails_FA.ACTION_USB_PERMISSION);
//                        registerReceiver(mUsbReceiver, filter);

                    DPFPDDUsbHost.DPFPDDUsbCheckAndRequestPermissions(context, mPermissionIntent, reader.GetDescription().name);
                    reader.Open(Reader.Priority.EXCLUSIVE);
                    Reader.Status status = reader.GetStatus();
                    if (status.status == Reader.ReaderStatus.BUSY) {
                        reader.CancelCapture();
                    }
                    dpi = GetFirstDPI(reader);
                    engine = UareUGlobal.GetEngine();
                    stopFingerReader = false;
                    Fmd[] fmds = EmobileBiometricDAO.getFmds(engine);
                    if (fmds.length > 0) {
                        while (!stopFingerReader) {
                            try {
                                Reader.CaptureResult cap_result = reader.Capture(Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT, dpi, -1);
                                if (cap_result == null || cap_result.image == null) {
                                    continue;
                                }
                                Fmd fmd = engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004);
                                Engine.Candidate[] candidates = engine.Identify(fmd, 0, fmds, 100000, 2);
                                for (Engine.Candidate candidate : candidates) {
                                    int fmd_index = candidate.fmd_index;
                                    final EmobileBiometric biometric = EmobileBiometricDAO.getBiometrics(fmds[fmd_index]);
                                    if (biometric != null) {
                                        callbacks.biometricsWasRead(biometric);
                                    }
                                }
                            } catch (UareUException e) {
                                stopFingerReader = true;
                            }
                        }
                    }
                    try {
                        if (reader.GetStatus().status == Reader.ReaderStatus.BUSY) {
                            reader.CancelCapture();
                        }
                        reader.Close();
                    } catch (UareUException e) {
                        e.printStackTrace();
                    }
                } catch (UareUException e) {
                    Crashlytics.logException(e);
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Crashlytics.logException(e);
                    e.printStackTrace();
                } catch (DPFPDDUsbException e) {
                    Crashlytics.logException(e);
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private int GetFirstDPI(Reader reader) {
        Reader.Capabilities caps = reader.GetCapabilities();
        return caps.resolutions[0];
    }

    public void releaseReader() {
        stopFingerReader = true;
        try {
            if (reader.GetStatus().status == Reader.ReaderStatus.BUSY) {
                reader.CancelCapture();
            }
            reader.Close();
        } catch (UareUException e) {
            e.printStackTrace();
        }
    }
}
