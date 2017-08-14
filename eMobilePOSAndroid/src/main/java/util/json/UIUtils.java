package util.json;

import android.content.Context;
import android.content.res.Resources;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;

import interfaces.BCRCallbacks;

/**
 * Created by guarionex on 12/14/16.
 */

public class UIUtils {
    private static boolean isClicked;
    private static long startTyping;

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
            }, 300);
        }
        return allowClick;
    }

    public static void startBCR(View v, final EditText search, final BCRCallbacks callbacks) {
        if (search.getText().length() <= 1) {
            startTyping = 0;
        }
        if (startTyping == 0) {
            startTyping = SystemClock.currentThreadTimeMillis();
            v.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (search.getText().length() > 5) {
                        callbacks.executeBCR();
//                        myCursor = handler.getSearchCust(search.getText().toString());
//                        if (myCursor.getCount() == 1) {
//                            selectCustomer(0);
//                        }
                    }

                }
            }, 500);
        }
    }
}
