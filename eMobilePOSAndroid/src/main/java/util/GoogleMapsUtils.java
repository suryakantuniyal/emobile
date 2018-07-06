package util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

/**
 * Created by Luis Camayd on 7/5/2018.
 */
public class GoogleMapsUtils {
    private static final String GOOGLE_MAPS_PACKAGE_NAME = "com.google.android.apps.maps";

    public static void openAndSearchAddress(Context context, String query) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + query);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage(GOOGLE_MAPS_PACKAGE_NAME);
        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        }
    }

    public static boolean isInstalled(Context context) {
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(GOOGLE_MAPS_PACKAGE_NAME, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}