package com.innobins.innotrack.activity;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.GoogleApiAvailability;
import com.innobins.innotrack.home.HomeActivity;
import com.innobins.innotrack.utils.UtilsFunctions;
import com.rampo.updatechecker.UpdateChecker;
import com.rampo.updatechecker.notice.Notice;

import in.innobins.innotrack.R;
import com.innobins.innotrack.utils.URLContstant;


/**
 * Created by silence12 on 26/6/17.
 */

public class   SplashActivity extends AppCompatActivity  {
    Boolean isActive = false;
    private static int SPLASH_TIME_OUT = 5000;
    int Counter = 0;
    boolean Activenetwork = true;
    boolean GPS = false, networkedchecked = true;
    SharedPreferences mSharedPreferences;
    Boolean flag = false;
    boolean activityOpened = true, isShown = false, firstTime = false;
    Snackbar Alertbar, Tryingbar;
    RelativeLayout coordinatorLayout;
    SharedPreferences sharedPrefs, sharedPreferences;
    String userName,password;
    GoogleApiAvailability mGoogleApiAvailability;
    ProgressBar progressBar;
    MyCustomTextView animated_textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        coordinatorLayout = (RelativeLayout) findViewById(R.id.coordinatorLayout);
        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, MODE_PRIVATE);
        sharedPrefs = getSharedPreferences("ArrayList", Context.MODE_PRIVATE);
        sharedPreferences = getSharedPreferences("OfflineList", Context.MODE_PRIVATE);
        userName = mSharedPreferences.getString(URLContstant.KEY_USERNAME, "");
        password = mSharedPreferences.getString(URLContstant.KEY_PASSWORD,"");
        mGoogleApiAvailability = GoogleApiAvailability.getInstance();

        animated_textView = (MyCustomTextView)findViewById(R.id.animated_tv);
        animated_textView.animateText("Enterprise Facilitator");
        animated_textView.setCharacterDelay(200);

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
            startAnimation();
        }

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
                      //isNetworkActive();
                        handler.postDelayed(this, 2000);
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
        }, 2000);
    }

    void chooseBetweenLoginAndMainActivity() {
        activityOpened = false;
        Log.d("function", "chooseBetweenLoginMainActivity called");
        if (mSharedPreferences.getBoolean(URLContstant.KEY_LOGGED_IN, false)) {
            Intent mainactivityintent = new Intent(this, HomeActivity.class);
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
    public void startAnimation(){

//        progressBar.setVisibility(View.VISIBLE);
        final Context context = getApplicationContext() ;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("run","after 2 sec");

                if (UtilsFunctions.isNetworkAvailable(context)){
                    chooseBetweenLoginAndMainActivity();
                   // allOnlineVehicle();
                }
                else {
                    if (!UtilsFunctions.isNetworkAvailable(SplashActivity.this)){
                        final AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                        LayoutInflater layoutInflater = LayoutInflater.from(SplashActivity.this);
                        View view = layoutInflater.inflate(R.layout.disconnected_network, null);
                        builder.setView(view);
                        Button Tryagain = (Button)view.findViewById(R.id.tryagain);
                        builder.setCancelable(false);
                        final Dialog dialog = builder.create();
                        dialog.show();
                        Tryagain.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                                if (!UtilsFunctions.isNetworkAvailable(getApplicationContext())){
                                    dialog.show();
                                } else{
                                    chooseBetweenLoginAndMainActivity();
                                   //allOnlineVehicle();
                                }
                            }
                        });
                    }
                }
            }
        }, SPLASH_TIME_OUT);
        Tryingbar = Snackbar.make(coordinatorLayout, "Trying to connect server.....",Snackbar.LENGTH_INDEFINITE);
        Alertbar = Snackbar
                .make(coordinatorLayout, "Unable to connect the server!", Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Alertbar.dismiss();
                        View sbView = Tryingbar.getView();
                        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(Color.GREEN);
                        Tryingbar.show();
                        Continousservercheck();
                        Counter = 0;
                        isShown = false;
                    }
                });

        Alertbar.setActionTextColor(Color.RED);
        View sbView = Alertbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
    }

}
