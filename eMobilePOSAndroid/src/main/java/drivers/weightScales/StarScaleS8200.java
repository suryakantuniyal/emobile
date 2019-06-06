package drivers.weightScales;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.starmicronics.starmgsio.ConnectionInfo;
import com.starmicronics.starmgsio.Scale;
import com.starmicronics.starmgsio.ScaleCallback;
import com.starmicronics.starmgsio.ScaleData;
import com.starmicronics.starmgsio.ScaleSetting;
import com.starmicronics.starmgsio.StarDeviceManager;
import com.starmicronics.starmgsio.StarDeviceManagerCallback;

import java.util.Locale;

import interfaces.EMSDeviceManagerWeightDelegate;

public class StarScaleS8200 extends WSDeviceDriver implements EMSDeviceManagerWeightDelegate {

    private StarDeviceManager mStarDeviceManager;
    private final Context context;
    private final boolean autoConnect;

    private Scale mScale;
    private String macAddress, weightUnit, weightStringResult, name;
    private double weightResult;
    private boolean connection;

    public StarScaleS8200(Context context, WSDeviceManager wsdm, boolean autoConnect, String macAddress) {
        this.context = context;
        this.wsdm = wsdm;
        this.autoConnect = autoConnect;
        this.macAddress = macAddress;
    }

    @Override
    public void connect() {
        if (autoConnect) {
            startScale(macAddress, true);
        } else if (!isConnected()) {
            scanForStarScaleDevice();
        }
    }

    @Override
    public void disconnect() {
        if (isConnected()) {
            mScale.disconnect();
        }
    }

    @Override
    public boolean isConnected() {
        setConnectionDetails(connection, name, macAddress);
        return connection;
    }

    @Override
    public void setDevice() {
        this.setWeightScaleDevice();
    }

    private void scanForStarScaleDevice() {
        if (mStarDeviceManager == null) {
            mStarDeviceManager = new StarDeviceManager(context);
        }
        mStarDeviceManager.scanForScales(mStarDeviceManagerCallback);
    }

    private void startScale(String macAddress, boolean auto) {
        if (auto || mStarDeviceManager == null) {
            mStarDeviceManager = new StarDeviceManager(context);
        }
        if (mScale == null) {
            ConnectionInfo connectionInfo = new ConnectionInfo.Builder()
                    .setBleInfo(macAddress)
                    .build();

            mScale = mStarDeviceManager.createScale(connectionInfo);
        }
        mScale.connect(mScaleCallback);
        wsdm.WeightDeviceDidConnect(this);
    }

    private final ScaleCallback mScaleCallback = new ScaleCallback() {
        @Override
        public void onConnect(Scale scale, int status) {
            switch (status) {
                default:
                case Scale.CONNECT_SUCCESS:
                    connection = true;
                    Toast.makeText(context, "Connect success.", Toast.LENGTH_SHORT).show();
                    Log.e("StarScale-DRIVER","Connect Success");
                    break;

                case Scale.CONNECT_NOT_AVAILABLE:
                    connection = false;
//                    Toast.makeText(context, "Failed to connect. (Not available)", Toast.LENGTH_SHORT).show();
                    Log.e("StarScale-DRIVER","Failed to connect. (Not available)");
                    break;

                case Scale.CONNECT_ALREADY_CONNECTED:
                    connection = true;
//                    Toast.makeText(context, "Failed to connect. (Already connected)", Toast.LENGTH_SHORT).show();
                    Log.e("StarScale-DRIVER","Failed to connect. (Already connected)");
                    break;

                case Scale.CONNECT_TIMEOUT:
                    connection = false;
                    Toast.makeText(context, "Failed to connect. (Timeout)", Toast.LENGTH_SHORT).show();
                    Log.e("StarScale-DRIVER","Failed to connect. (Timeout)");
                    break;

                case Scale.CONNECT_NOT_SUPPORTED:
                    connection = false;
//                    Toast.makeText(context, "Failed to connect. (Not supported device)", Toast.LENGTH_SHORT).show();
                    Log.e("StarScale-DRIVER","Failed to connect. (Not supported device)");
                    break;

                case Scale.CONNECT_UNEXPECTED_ERROR:
                    connection = false;
//                    Toast.makeText(context, "Failed to connect. (Unexpected error)", Toast.LENGTH_SHORT).show();
                    Log.e("StarScale-DRIVER","Failed to connect. (Unexpected error)");
                    break;
            }

            if (!connection) {
                mScale = null;
            }
        }

        @Override
        public void onDisconnect(Scale scale, int status) {
            mScale = null;

            switch (status) {
                default:
                case Scale.DISCONNECT_SUCCESS:
                    connection = false;
                    Toast.makeText(context, "Disconnect success.", Toast.LENGTH_SHORT).show();
                    break;

                case Scale.DISCONNECT_NOT_CONNECTED:
                    connection = false;
                    Toast.makeText(context, "Failed to disconnect. (Not connected)", Toast.LENGTH_SHORT).show();
                    break;

                case Scale.DISCONNECT_TIMEOUT:
                    connection = false;
                    Toast.makeText(context, "Failed to disconnect. (Timeout)", Toast.LENGTH_SHORT).show();
                    break;

                case Scale.DISCONNECT_UNEXPECTED_ERROR:
                    connection = false;
                    Toast.makeText(context, "Failed to disconnect. (Unexpected error)", Toast.LENGTH_SHORT).show();
                    break;

                case Scale.DISCONNECT_UNEXPECTED_DISCONNECTION:
                    connection = false;
                    Toast.makeText(context, "Unexpected disconnection.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        public void onReadScaleData(Scale scale, ScaleData scaleData) {
            if (scaleData.getStatus() == ScaleData.Status.ERROR) { // Error
                weightResult = 0;
//                weightResult = "Status ERROR! Data Type INVALID";
            } else {
                String weight = String.format(Locale.US, "%." + scaleData.getNumberOfDecimalPlaces() + "f", scaleData.getWeight());
                weightResult = Double.parseDouble(weight);
                String unit = scaleData.getUnit().toString();
                weightUnit = unit;
                weightStringResult = weight + " [" + unit + "]";

//                String statusStr =
//                        "Status: " + scaleData.getStatus()   + "\n" +
//                                "Data Type: " + scaleData.getDataType() + "\n" +
//                                "Comparator Result: " + scaleData.getComparatorResult();
            }
        }

        @Override
        public void onUpdateSetting(Scale scale, ScaleSetting scaleSetting, int status) {
            if (scaleSetting == ScaleSetting.ZeroPointAdjustment) {
                switch (status) {
                    default:
                    case Scale.UPDATE_SETTING_SUCCESS:
                        Toast.makeText(context, "Succeeded.", Toast.LENGTH_SHORT).show();
                        break;

                    case Scale.UPDATE_SETTING_NOT_CONNECTED:
                        Toast.makeText(context, "Failed. (Not connected)", Toast.LENGTH_SHORT).show();
                        break;

                    case Scale.UPDATE_SETTING_REQUEST_REJECTED:
                        Toast.makeText(context, "Failed. (Request rejected)", Toast.LENGTH_SHORT).show();
                        break;

                    case Scale.UPDATE_SETTING_TIMEOUT:
                        Toast.makeText(context, "Failed. (Timeout)", Toast.LENGTH_SHORT).show();
                        break;

                    case Scale.UPDATE_SETTING_UNEXPECTED_ERROR:
                        Toast.makeText(context, "Failed. (Unexpected error)", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };

    private final StarDeviceManagerCallback mStarDeviceManagerCallback = new StarDeviceManagerCallback() {
        @Override
        public void onDiscoverScale(@NonNull ConnectionInfo connectionInfo) {
            if (connectionInfo.getDeviceName().contains("MG-S8200")) {
                macAddress = connectionInfo.getMacAddress();
                name = connectionInfo.getDeviceName();
                mStarDeviceManager.stopScan();
                startScale(macAddress,false);
            }
        }
    };

    @Override
    public void setWeightScaleDevice() {
        wsdm.setCurrentWeightDevice(this);
    }

    @Override
    public void setMacAddress(String mac) {
        macAddress = mac;
    }

    @Override
    public double getScaleWeight() {
        return weightResult;
    }

    @Override
    public String getFormatedScaleWeight() {
        return weightStringResult;
    }

    @Override
    public String getScaleUnit() {
        return weightUnit;
    }

    @Override
    public String getMacAddress() {
        return macAddress;
    }

    @Override
    public String getName() {
        return name;
    }

}
