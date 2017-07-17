package in.gtech.gogeotrack.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.gson.Gson;

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
    public static ArrayList<VehicleList> onLineList;
    public static ArrayList<VehicleList> offlineList;
    public static int AllSize, onlinesize, offlinesize;
    private static int SPLASH_TIME_OUT = 2000;
    LinearLayout alert;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
//        ImageView img_animation = (ImageView) findViewById(R.id.imagespash);
        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, MODE_PRIVATE);
        sharedPrefs = getSharedPreferences("ArrayList", Context.MODE_PRIVATE);
        sharedPreferences = getSharedPreferences("OfflineList", Context.MODE_PRIVATE);
        userName = mSharedPreferences.getString(URLContstant.KEY_USERNAME, "");
        password = mSharedPreferences.getString(URLContstant.KEY_PASSWORD,"");
        Log.d("Auth11",userName + password);
        listArrayList = new ArrayList<VehicleList>();
        onLineList = new ArrayList<VehicleList>();
        offlineList = new ArrayList<VehicleList>();
        latlongList = new ArrayList<VehicleList>();
        if(userName ==null && password==null){
            Intent loginsignupactivityIntent = new Intent(this, SignUpAccount.class);
            loginsignupactivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginsignupactivityIntent);
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }else {
            parseView();
            allOnlineVehicle();
            allOfflineVehicle();
        }
    }

    private void parseView() {
        APIServices.GetAllVehicleList(SplashActivity.this,userName,password, new ResponseCallbackEvents() {
                    @Override
                    public void onSuccess(final ArrayList<VehicleList> result) {
                        for (int i = 0; i < result.size(); i++) {

                            AllSize = result.size();
                            int id = result.get(i).positionId;
                            final int finalI = i;
                            APIServices.GetVehicleDetailById(SplashActivity.this, id, new DetailResponseCallback() {
                                @Override
                                public void OnResponse(VehicleList Response) {
                                    VehicleList vehicles = new VehicleList(result.get(finalI).id, result.get(finalI).name, result.get(finalI).uniqueId
                                            , result.get(finalI).status, result.get(finalI).lastUpdates, result.get(finalI).category, result.get(finalI).positionId, Response.address,
                                            result.get(finalI).time, result.get(finalI).timeDiff);
                                    listArrayList.add(vehicles);
//                            if(result.get(finalI).status.equals("online")){
//                                onLineList.add(vehicles);
//                            }
//                            if(result.get(finalI).status.equals("offline")){
//                                offlineList.add(vehicles);
//                            }
                                    VehicleList latlong = new VehicleList(result.get(finalI).id, result.get(finalI).name, result.get(finalI).status, Response.latitute, Response.longitute);
                                    latlongList.add(latlong);
                                }
                            });
                        }

//                Log.d("Onsize", String.valueOf(onLineList.size()));
                        chooseBetweenLoginAndMainActivity();
                    }
                }

        );
    }


    public void allOnlineVehicle() {
        APIServices.GetAllOnlineVehicleList(SplashActivity.this,userName,password, new ResponseOnlineVehicle() {
            @Override
            public void onSuccessOnline(final ArrayList<VehicleList> result) {
                final ArrayList<VehicleList> arrayList = new ArrayList<VehicleList>();
                for (int i = 0; i < result.size(); i++) {
                    onlinesize = result.size();
                    int id = result.get(i).positionId;
                    final int finalI = i;
                    APIServices.GetVehicleDetailById(SplashActivity.this, id, new DetailResponseCallback() {
                        @Override
                        public void OnResponse(VehicleList Response) {
                            VehicleList vehicles = new VehicleList(result.get(finalI).id, result.get(finalI).name, result.get(finalI).uniqueId
                                    , result.get(finalI).status, result.get(finalI).lastUpdates, result.get(finalI).category, result.get(finalI).positionId, Response.address,
                                    result.get(finalI).time, result.get(finalI).timeDiff);
                            arrayList.add(vehicles);
                            arrayEditor = sharedPrefs.edit();
                            arrayEditor.clear();
                            Gson gson = new Gson();
                            String json = gson.toJson(arrayList);
                            arrayEditor.putString("onlist", json);
                            arrayEditor.putString("onlinesize", String.valueOf(onlinesize));
                            arrayEditor.commit();
                        }
                    });
                }
            }

        });
    }

    public void allOfflineVehicle() {
        APIServices.GetAllOfflineVehicleList(SplashActivity.this,userName,password, new ResponseOfflineVehicle() {
            @Override
            public void onSuccessOffline(final ArrayList<VehicleList> result) {
                final ArrayList<VehicleList> arrayLi = new ArrayList<VehicleList>();
                for (int i = 0; i < result.size(); i++) {
                    offlinesize = result.size();
                    Log.d("onlineSize", String.valueOf(offlinesize));
                    int id = result.get(i).positionId;
                    final int finalI = i;
                    APIServices.GetVehicleDetailById(SplashActivity.this, id, new DetailResponseCallback() {
                        @Override
                        public void OnResponse(VehicleList Response) {
                            VehicleList vehicles = new VehicleList(result.get(finalI).id, result.get(finalI).name, result.get(finalI).uniqueId
                                    , result.get(finalI).status, result.get(finalI).lastUpdates, result.get(finalI).category, result.get(finalI).positionId, Response.address,
                                    result.get(finalI).time, result.get(finalI).timeDiff);
                            arrayLi.add(vehicles);
                            editor = sharedPreferences.edit();
                            editor.clear();
                            Gson gson = new Gson();
                            String json = gson.toJson(arrayLi);
                            editor.putString("off", json);
                            editor.commit();
                        }
                    });
                }

            }
        });
    }


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
