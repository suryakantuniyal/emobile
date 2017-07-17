package in.gtech.gogeotrack.FCM;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import in.gtech.gogeotrack.utils.URLContstant;

/**
 * Created by silence12 on 20/6/17.
 */

public class RegistrationTokenHandler extends FirebaseInstanceIdService {
    static SharedPreferences mSharedprefrences;
    static SharedPreferences.Editor mEditor;

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("Refreshed token: ", refreshedToken);
        // TODO: Implement this method to send any registration to your app's servers.
        mSharedprefrences = getSharedPreferences(URLContstant.PREFERENCE_NAME, MODE_PRIVATE);
        mEditor = mSharedprefrences.edit();
        mEditor.putString(URLContstant.FCM_TOKEN, refreshedToken);
        mEditor.apply();
        if (mSharedprefrences.getBoolean(URLContstant.KEY_LOGGED_IN, false)) {
            Intent sendTokenservice = new Intent(this, SendRegistrationTokentoServer.class);
            startService(sendTokenservice);
        }
    }
}
