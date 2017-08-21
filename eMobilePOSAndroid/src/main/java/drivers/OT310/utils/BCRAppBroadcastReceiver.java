package drivers.OT310.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.oem.barcode.BCRIntents;

/**
 * Created by Guarionex on 12/8/2015.
 */
public class BCRAppBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(BCRIntents.ACTION_NEW_DATA)) {

            int id = intent.getIntExtra(BCRIntents.EXTRA_BCR_TYPE, -1);
            byte[] data = intent.getByteArrayExtra(BCRIntents.EXTRA_BCR_DATA);

//            final Activity activity = mActivity;
//            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//            builder.setTitle("Barcode:" + id);
//            builder.setMessage(new String(data));

//            DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int which) {
//                    switch(which) {
//                        case DialogInterface.BUTTON_NEGATIVE:
//                            break;
//                        case DialogInterface.BUTTON_POSITIVE:
//                            break;
//                    }
//                    dialog.dismiss();
//                }
//            };
//
//            builder.setPositiveButton(android.R.string.ok, onClickListener);
//            AlertDialog dialog = builder.create();
//            dialog.setCancelable(true);
//            dialog.show();

//            Toast.makeText(mActivity,new String(data), Toast.LENGTH_LONG).show();
        }
    }
}
