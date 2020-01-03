package drivers.weightScales.weight.utils;

import android.util.Log;

import com.android.support.Global;
import com.android.support.MyPreferences;

public class weightScaleHelper {


    public static boolean checkWeightAvailability(MyPreferences myPref) {
        if (myPref.getSelectedBTweight() > -1) {
            try {
                if (Global.mainWeightScaleManager.isWeightScaleConnected()) {
                    return true;
                }else{return false;}
            }catch(Exception e){
                Log.e("Receipt_FR","Weight Scale Availability Error::"+e.toString());
                return false;
            }
        }
        return false;
    }



}
