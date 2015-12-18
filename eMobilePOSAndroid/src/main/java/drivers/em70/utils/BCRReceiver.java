package drivers.em70.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Guarionex on 12/7/2015.
 */

public class BCRReceiver extends BroadcastReceiver {

    private static String LOG_TAG = "BCRReceiver";
    public static final String SCANNER_DATA_KEY = "com.partner.barcode.data";
    public static final String EXTRA_BCR_STRING = "com.oem.barcode.string";
    public static final String EXTRA_BCR_TYPE = "com.oem.barcode.type";
    public static final String EXTRA_BCR_DATA = "com.oem.barcode.data";
    public static final String EXTRA_BCR_CHARSET = "com.oem.barcode.encoding";
    public static final String ACTION_NEW_DATA = "android.intent.action.bcr.newdata";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive: action = " + intent.getAction());
        String bcrData = intent.getStringExtra(SCANNER_DATA_KEY);
    }

}