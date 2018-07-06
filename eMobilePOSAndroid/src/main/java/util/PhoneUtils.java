package util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by Luis Camayd on 7/5/2018.
 */
public class PhoneUtils {

    public static boolean isCallingSupported(Context context) {
        return context.getPackageManager()
                .queryIntentActivities(new Intent(Intent.ACTION_DIAL), 0).size() > 0;
    }

    public static void dialPhoneNumber(Context context, String phone) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:" + phone));
        context.startActivity(dialIntent);
    }
}