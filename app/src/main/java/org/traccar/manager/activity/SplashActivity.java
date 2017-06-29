package org.traccar.manager.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import org.traccar.manager.R;

/**
 * Created by silence12 on 26/6/17.
 */

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ImageView img_animation = (ImageView) findViewById(R.id.imagespash);
        TranslateAnimation animation = new TranslateAnimation(0.0f, 400.0f,
                0.0f, 0.0f);          //  new TranslateAnimation(xFrom,xTo, yFrom,yTo)
        animation.setDuration(2995);  // animation duration
//        animation.setRepeatCount(5);  // animation repeat count
//        animation.setRepeatMode(2);   // repeat animation (left to right, right to left )
        //animation.setFillAfter(true);

        img_animation.startAnimation(animation);
        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

}
