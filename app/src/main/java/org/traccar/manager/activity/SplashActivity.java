package org.traccar.manager.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.gms.common.GoogleApiAvailability;

import org.traccar.manager.R;
import org.traccar.manager.utils.URLContstant;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by silence12 on 26/6/17.
 */

public class SplashActivity extends AppCompatActivity {
    LinearLayout alert;
    private static int SPLASH_TIME_OUT = 2000;
    Boolean isActive = false ;
    int Counter = 0;
    boolean Activenetwork = true;
    boolean GPS = false, networkedchecked = true;
    SharedPreferences mSharedPreferences;
    GoogleApiAvailability mGoogleApiAvailability;
    ImageView Logo;
    Timer timer;
    TimerTask timerTask;
    ProgressBar progressBar;
    Boolean flag = false ;
    boolean activityOpened = true, isShown = false, firstTime = false;
    Snackbar Alertbar,Tryingbar;
    RelativeLayout coordinatorLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ImageView img_animation = (ImageView) findViewById(R.id.imagespash);
        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME,MODE_PRIVATE);
        TranslateAnimation animation = new TranslateAnimation(0.0f, 400.0f,
                0.0f, 0.0f);          //  new TranslateAnimation(xFrom,xTo, yFrom,yTo)
        animation.setDuration(3000);  // animation duration
//        animation.setRepeatCount(5);  // animation repeat count
//        animation.setRepeatMode(2);   // repeat animation (left to right, right to left )
        //animation.setFillAfter(true);

        img_animation.startAnimation(animation);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                chooseBetweenLoginAndMainActivity();
            }
        }, SPLASH_TIME_OUT);


    }

    public void Continousservercheck(){
        final Handler handler = new Handler();
        Log.d("server", "Checking is server working or not");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("handler","called");
                Counter++;
                Log.d("count", String.valueOf(Counter));
                if (Counter < 5){
                    if (!Activenetwork){
//                        isNetworkActive();
                        handler.postDelayed(this,5000);
                    } else {
                        Tryingbar.dismiss();
                        Alertbar.dismiss();
                        if (activityOpened)
                            chooseBetweenLoginAndMainActivity();
                    }
                } else {
                    if (!isShown){
                        Tryingbar.dismiss();
                        Alertbar.show();
                        isShown = true;
                    }
                }
            }
        },10000);
    }

    void chooseBetweenLoginAndMainActivity() {
        activityOpened = false;
        Log.d("function","chooseBetweenLoginMainActivity called");
        if (mSharedPreferences.getBoolean(URLContstant.KEY_LOGGED_IN,false)){
            Intent mainactivityintent = new Intent(this, MainActivity.class);
            mainactivityintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainactivityintent);
            finish();
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        } else {
            Intent loginsignupactivityIntent = new Intent(this, SignUpAccount.class);
            loginsignupactivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginsignupactivityIntent);
            finish();
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
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
