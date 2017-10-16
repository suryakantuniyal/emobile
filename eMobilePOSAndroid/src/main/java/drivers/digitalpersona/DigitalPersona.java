package drivers.digitalpersona;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.usb.UsbDevice;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dao.EmobileBiometricDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.customer.ViewCustomerDetails_FA;
import com.android.emobilepos.models.realms.BiometricFid;
import com.android.emobilepos.models.realms.EmobileBiometric;
import com.android.support.DeviceUtils;
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

import java.util.Collection;

import interfaces.BiometricCallbacks;

import static com.android.emobilepos.R.id.fingerPrintimageView;
import static com.android.emobilepos.R.id.unregisterFingerprintbutton2;

public class DigitalPersona {

    private int spleepTime = 500;
    private Reader reader;
    private boolean stopFingerReader;
    private Context context;
    private BiometricCallbacks callbacks;
    private int dpi;
    private Engine engine;
    private boolean m_reset;
    private int m_current_fmds_count;
    private Reader.CaptureResult cap_result;
    private String m_enginError;
    private String m_text_conclusionString;
    private Fmd m_enrollment_fmd;
    private boolean m_first;
    private boolean m_success;
    private int m_templateSize;
    private EnrollmentCallback enrollThread;
    private int progress;
    private ImageView fingerPrintimage;
    private ProgressBar progressBar;
    private TextView fingerPrintScanningNotesTextView;

    public DigitalPersona(Context context, BiometricCallbacks callbacks) {

        this.context = context;
        this.callbacks = callbacks;
    }

    public void loadForEnrollment() {
        ReaderCollection readers;
        try {
            readers = UareUGlobal.GetReaderCollection(context);
            readers.GetReaders();
            if (readers.size() > 0) {
                this.reader = readers.get(0);
            } else {
                return;
            }
            PendingIntent mPermissionIntent;
            mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ViewCustomerDetails_FA.ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ViewCustomerDetails_FA.ACTION_USB_PERMISSION);
//                registerReceiver(mUsbReceiver, filter);

            DPFPDDUsbHost.DPFPDDUsbCheckAndRequestPermissions(context, mPermissionIntent, reader.GetDescription().name);
            reader.Open(Reader.Priority.EXCLUSIVE);
            Reader.Status status = reader.GetStatus();
            if (status.status == Reader.ReaderStatus.BUSY) {
                reader.CancelCapture();
            }
            dpi = GetFirstDPI(reader);
            engine = UareUGlobal.GetEngine();
        } catch (UareUException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        } catch (DPFPDDUsbException e) {

        }
    }

    public void loadForScan() {
        Collection<UsbDevice> usbDevices = DeviceUtils.getUSBDevices(context);
        boolean isReaderConnected = usbDevices.size() > 0;
        if (isReaderConnected) {
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
                        } else {
                            return;
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
    }


    private int GetFirstDPI(Reader reader) {
        Reader.Capabilities caps = reader.GetCapabilities();
        return caps.resolutions[0];
    }

    public void releaseReader() {
        stopFingerReader = true;
        try {
            if (reader != null) {
                if (reader.GetStatus().status == Reader.ReaderStatus.BUSY) {
                    reader.CancelCapture();
                }
                reader.Close();
            }

        } catch (UareUException e) {
            e.printStackTrace();
        }
    }


    public void startFingerPrintEnrollment(final ViewCustomerDetails_FA.Finger finger) {
        try {
            loadForEnrollment();
            if (reader != null) {
                final Dialog scanningDialog = showScanningDialog(finger);
                m_reset = false;
                // loop capture on a separate thread to avoid freezing the UI
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            m_current_fmds_count = 0;
                            m_reset = false;
                            enrollThread = new EnrollmentCallback(reader, engine, finger);
                            while (!m_reset) {
                                Log.d("Engine", "Engine Enrollment progress");
                                try {
                                    m_enrollment_fmd = engine.CreateEnrollmentFmd(Fmd.Format.ANSI_378_2004, enrollThread);
                                    if (m_success = (m_enrollment_fmd != null)) {
                                        m_templateSize = m_enrollment_fmd.getData().length;
                                        m_current_fmds_count = 0;    // reset count on success
                                    }
                                } catch (Exception e) {
                                    // template creation failed, reset count
                                    m_current_fmds_count = 0;
                                }
                            }
                            progress = 0;
                            releaseReader();
                            scanningDialog.dismiss();
//                            ViewCustomerDetails_FA.this.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    setFingerPrintUI();
//                                }
//                            });
                        } catch (Exception e) {
                            if (!m_reset) {
                                Log.w("UareUSampleJava", "error during capture");
//                                onBackPressed();
                            }
                        }
                    }
                }).start();
            } else {
                Toast.makeText(context, context.getString(R.string.fingerreadernotfound), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Dialog showScanningDialog(final ViewCustomerDetails_FA.Finger finger) {
        final Dialog dialog = new Dialog(context, R.style.DialogLargeArea);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.fingerprint_scanning_layout);
        fingerPrintimage = (ImageView) dialog.findViewById(fingerPrintimageView);
        Button fingerPrintCancelButton = (Button) dialog.findViewById(R.id.cancelScanningButton);
        Button unregisterButton = (Button) dialog.findViewById(unregisterFingerprintbutton2);
        fingerPrintCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_reset = true;
                try {
                    if (reader.GetStatus().status == Reader.ReaderStatus.BUSY) {
                        reader.CancelCapture();
                    }
                } catch (UareUException e) {

                }
                dialog.dismiss();
            }
        });

        unregisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_reset = true;
                try {
                    if (reader.GetStatus().status == Reader.ReaderStatus.BUSY) {
                        reader.CancelCapture();
                    }
                } catch (UareUException e) {

                }
                dialog.dismiss();
                callbacks.biometricsUnregister(finger);
            }
        });


        dialog.show();
        startAnimation(fingerPrintimage, 0);
        progressBar = (ProgressBar) dialog.findViewById(R.id.fingerprintScanningprogressBar3);
        progressBar.setMax(5);
        progressBar.setProgress(progress);
        fingerPrintScanningNotesTextView = (TextView) dialog.findViewById(R.id.fingerPrintNotestextView);
        fingerPrintScanningNotesTextView.setText(R.string.fingerprint_enrollment);
        return dialog;
    }

    private void startAnimation(final ImageView imageView, int step) {
        android.os.Handler handler = new android.os.Handler();
        switch (step) {
            case 1:
                fingerPrintimage.setBackgroundResource(R.drawable.fingertscanner_ok);
                AnimationDrawable animation = (AnimationDrawable) imageView.getBackground();
                animation.start();
                fingerPrintScanningNotesTextView.setText(R.string.processing);
                fingerPrintScanningNotesTextView.setTextColor(Color.RED);
                startAnimation(imageView, 2);
                break;
            case 2:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fingerPrintScanningNotesTextView.setText(R.string.rescan_fingerprint);
                        fingerPrintScanningNotesTextView.setTextColor(Color.RED);
                        fingerPrintimage.setBackgroundResource(R.drawable.fingertscanner_scanning);
                        AnimationDrawable animation = (AnimationDrawable) imageView.getBackground();
                        animation.start();
                    }
                }, 500);
                break;
        }

    }

    public class EnrollmentCallback
            extends Thread
            implements Engine.EnrollmentCallback {
        public int m_current_index = 0;

        private Reader m_reader = null;
        private Engine m_engine = null;
        private ViewCustomerDetails_FA.Finger finger;

        public EnrollmentCallback(Reader reader, Engine engine, ViewCustomerDetails_FA.Finger finger) {
            m_reader = reader;
            m_engine = engine;
            this.finger = finger;
        }

        // callback function is called by dp sdk to retrieve fmds until a null is returned
        @Override
        public Engine.PreEnrollmentFmd GetFmd(Fmd.Format format) {
            Engine.PreEnrollmentFmd result = null;


            while (!m_reset) {
                Log.d("Enrollment", "Enrollment Capture in progress");
                try {
                    cap_result = m_reader.Capture(Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT, dpi, -1);
                } catch (Exception e) {
                    Log.w("UareUSampleJava", "error during capture: " + e.toString());
                }

                // an error occurred
                if (cap_result == null || cap_result.image == null) continue;
                try {
                    m_enginError = "";
                    Engine.PreEnrollmentFmd prefmd = new Engine.PreEnrollmentFmd();

                    prefmd.fmd = m_engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004);
                    prefmd.view_index = 0;
                    m_current_fmds_count++;
                    progress++;
                    progressBar.setProgress(progress);

                    result = prefmd;
                    break;
                } catch (Exception e) {
                    m_enginError = e.toString();
                    Log.w("UareUSampleJava", "Engine error: " + e.toString());
                }
            }

            m_text_conclusionString = QualityToString(cap_result);

            if (!TextUtils.isEmpty(m_enginError)) {
                m_text_conclusionString = "Engine: " + m_enginError;
            }

            if (m_enrollment_fmd != null || m_current_fmds_count == 0) {
                if (!m_first) {
                    if (m_text_conclusionString.length() == 0) {
                        try {
                            Fmd[] fmds = EmobileBiometricDAO.getFmds(engine);
                            Engine.Candidate[] candidates = new Engine.Candidate[0];
                            if (fmds.length > 0) {
                                candidates = engine.Identify(m_enrollment_fmd, 0, fmds, 100000, 2);
                            }
                            m_reset = true;
                            BiometricFid biometricFid = new BiometricFid(engine, cap_result.image, finger);
                            if (candidates.length == 0) {
                                callbacks.biometricsWasEnrolled(biometricFid);
                            } else {
                                for (Engine.Candidate candidate : candidates) {
                                    int fmd_index = candidate.fmd_index;
                                    final EmobileBiometric biometric = EmobileBiometricDAO.getBiometrics(fmds[fmd_index]);
                                    callbacks.biometricsDuplicatedEnroll(biometric, biometricFid);
//                                    if (biometric != null) {
//                                        callbacks.biometricsWasRead(biometric);
                                }
                            }
                        } catch (UareUException e) {
                            e.printStackTrace();
                        }
                        m_text_conclusionString = m_success ? "Enrollment template created, size: " + m_templateSize : "Enrollment template failed. Please try again";
                    }
                }
                m_enrollment_fmd = null;
            } else {
                m_first = false;
                m_success = false;
                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startAnimation(fingerPrintimage, 1);
                        }
                    });
                }
            }

            return result;
        }
    }

    public static String QualityToString(Reader.CaptureResult result) {
        if (result == null) {
            return "";
        }
        if (result.quality == null) {
            return "An error occurred";
        }
        switch (result.quality) {
            case FAKE_FINGER:
                return "Fake finger";
            case NO_FINGER:
                return "No finger";
            case CANCELED:
                return "Capture cancelled";
            case TIMED_OUT:
                return "Capture timed out";
            case FINGER_TOO_LEFT:
                return "Finger too left";
            case FINGER_TOO_RIGHT:
                return "Finger too right";
            case FINGER_TOO_HIGH:
                return "Finger too high";
            case FINGER_TOO_LOW:
                return "Finger too low";
            case FINGER_OFF_CENTER:
                return "Finger off center";
            case SCAN_SKEWED:
                return "Scan skewed";
            case SCAN_TOO_SHORT:
                return "Scan too short";
            case SCAN_TOO_LONG:
                return "Scan too long";
            case SCAN_TOO_SLOW:
                return "Scan too slow";
            case SCAN_TOO_FAST:
                return "Scan too fast";
            case SCAN_WRONG_DIRECTION:
                return "Wrong direction";
            case READER_DIRTY:
                return "Reader dirty";
            case GOOD:
                return "";
            default:
                return "An error occurred";
        }
    }
}
