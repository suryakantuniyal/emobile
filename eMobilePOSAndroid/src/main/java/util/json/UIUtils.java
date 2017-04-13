package util.json;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Created by guarionex on 12/14/16.
 */

public class UIUtils {
    private static boolean isClicked;

    public static float convertPixelsToDp(Context context, int px) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static boolean singleOnClick(View v) {
        boolean allowClick = false;
        if (!isClicked) {
            allowClick = true;
            isClicked = true;
            v.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isClicked = false;
                }
            }, 1000);
        }
        return allowClick;
    }
}
