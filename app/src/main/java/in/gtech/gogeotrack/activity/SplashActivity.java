package in.gtech.gogeotrack.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import in.gtech.gogeotrack.R;
import in.gtech.gogeotrack.api.APIServices;
import in.gtech.gogeotrack.model.VehicleList;
import in.gtech.gogeotrack.network.DetailResponseCallback;
import in.gtech.gogeotrack.network.ResponseCallbackEvents;
import in.gtech.gogeotrack.network.ResponseOfflineVehicle;
import in.gtech.gogeotrack.network.ResponseOnlineVehicle;
import in.gtech.gogeotrack.utils.URLContstant;

import java.util.ArrayList;

/**
 * Created by silence12 on 26/6/17.
 */

public class SplashActivity extends AppCompatActivity {
    public static ArrayList<VehicleList> listArrayList;
    public static ArrayList<VehicleList> latlongList;
    Boolean isActive = false;
    int Counter = 0;
    boolean Activenetwork = true;
    boolean GPS = false, networkedchecked = true;
    SharedPreferences mSharedPreferences;
    Boolean flag = false;
    boolean activityOpened = true, isShown = false, firstTime = false;
    Snackbar Alertbar, Tryingbar;
    RelativeLayout coordinatorLayout;
    SharedPreferences sharedPrefs, sharedPreferences;
    SharedPreferences.Editor arrayEditor, editor;
    String userName,password;
    GoogleApiAvailability mGoogleApiAvailability;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, MODE_PRIVATE);
        sharedPrefs = getSharedPreferences("ArrayList", Context.MODE_PRIVATE);
        sharedPreferences = getSharedPreferences("OfflineList", Context.MODE_PRIVATE);
        userName = mSharedPreferences.getString(URLContstant.KEY_USERNAME, "");
        password = mSharedPreferences.getString(URLContstant.KEY_PASSWORD,"");
        mGoogleApiAvailability = GoogleApiAvailability.getInstance();
        try {
            int status = mGoogleApiAvailability.isGooglePlayServicesAvailable(getApplicationContext());
            if (status != ConnectionResult.SUCCESS) {
//                GOOGLE_PLAY_SERVICE_UPDATE_CODE
                if(mGoogleApiAvailability.isUserResolvableError(status)) {
                    mGoogleApiAvailability.getErrorDialog(this, status,0
                            , new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                                    builder.setMessage("Are you want to update ?")
                                            .setTitle("Google play service Out of date.Google play services need to be Updated, tou are unable to use this app without updating your google services.")
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                    finish();
                                                }
                                            })
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                    Intent termcondlink = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=in.gtech.gogeotrack&hl=en"));
                                                    startActivity(termcondlink);
                                                }
                                            }).show();
                                }
                            }).show();
                }
            }
        } catch (Exception e){
            Log.d("error",e.toString());
        }
        if(userName ==null && password==null){
            Intent loginsignupactivityIntent = new Intent(this, SignUpAccount.class);
            loginsignupactivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginsignupactivityIntent);
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }else if(userName.equals("")&& password.equals("")){
            Intent loginsignupactivityIntent = new Intent(this, SignUpAccount.class);
            loginsignupactivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginsignupactivityIntent);
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }else {
            allOnlineVehicle();
        }
    }

    public void allOnlineVehicle() {

        APIServices.GetAllOnlineVehicleList(SplashActivity.this,userName,password, new ResponseOnlineVehicle() {
            @Override
            public void onSuccessOnline(JSONArray result) {
                Log.d("Result", String.valueOf(result));
                SessionHandler.updateSnessionHandler(getBaseContext(), result, mSharedPreferences);
                chooseBetweenLoginAndMainActivity();
                }
        });

        };


    public void Continousservercheck() {
        final Handler handler = new Handler();
        Log.d("server", "Checking is server working or not");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("handler", "called");
                Counter++;
                Log.d("count", String.valueOf(Counter));
                if (Counter < 5) {
                    if (!Activenetwork) {
//                        isNetworkActive();
                        handler.postDelayed(this, 5000);
                    } else {
                        Tryingbar.dismiss();
                        Alertbar.dismiss();
                        if (activityOpened)
                            chooseBetweenLoginAndMainActivity();
                    }
                } else {
                    if (!isShown) {
                        Tryingbar.dismiss();
                        Alertbar.show();
                        isShown = true;
                    }
                }
            }
        }, 10000);
    }

    void chooseBetweenLoginAndMainActivity() {
        activityOpened = false;
        Log.d("function", "chooseBetweenLoginMainActivity called");
        if (mSharedPreferences.getBoolean(URLContstant.KEY_LOGGED_IN, false)) {
            Intent mainactivityintent = new Intent(this, Main2Activity.class);
            mainactivityintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainactivityintent);
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            Intent loginsignupactivityIntent = new Intent(this, SignUpAccount.class);
            loginsignupactivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginsignupactivityIntent);
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActive = false;
    }


}
