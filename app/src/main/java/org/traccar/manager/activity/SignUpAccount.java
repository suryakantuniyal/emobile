package org.traccar.manager.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import org.traccar.manager.R;
import org.traccar.manager.fragments.LoginFragment;
import org.traccar.manager.utils.UtilFunctions;

/**
 * Created by silence12 on 19/6/17.
 */

public class SignUpAccount extends AppCompatActivity {

    private static boolean activityVisible = true;
    public static boolean isDestroy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Log.d("lifecycle-loginactivity","oncreate");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(broadcastReceiver, filter);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame,new LoginFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }



    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isActivityVisible()){
                if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                    try {
                        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                        if ((networkInfo != null) && (networkInfo.getState() == NetworkInfo.State.CONNECTED)) {
                        } else {
                            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(SignUpAccount.this);
                            LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
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
                                    if (!UtilFunctions.isNetworkAvailable(getApplicationContext())) {
                                        dialog.show();
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        isDestroy = false;
        activityResumed();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("lg-cycle","onpause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("lg-cycle","onstop");
        activityPaused();
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Receiver not registered")) {

            } else {

                throw e;
            }
        }
        isDestroy = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("lg-cycle","ondestroy");
        isDestroy = true;
    }

}
