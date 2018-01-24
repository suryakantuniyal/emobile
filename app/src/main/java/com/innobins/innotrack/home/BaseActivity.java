package com.innobins.innotrack.home;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.innobins.innotrack.utils.SharedUtils;

import in.innobins.innotrack.R;

/**
 * Created by silence12 on 24/1/18.
 */

public class BaseActivity extends AppCompatActivity {


    BroadcastReceiver mNetworkReceiver;
    public static boolean isNetwork = false ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mNetworkReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                    if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                        isNetwork = true ;
                    } else if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
                        LayoutInflater layoutInflater = LayoutInflater.from(BaseActivity.this);
                        isNetwork = false ;
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
                                if (!SharedUtils.isNetworkAvailable(getApplicationContext())){
                                    dialog.show();
                                }
                            }
                        });
                    }
                }
            }
        };
        IntentFilter mIntentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(mNetworkReceiver,mIntentFilter);
        setVisible(true);
    }


    public void customTitle(String title){
        SpannableString s = new SpannableString(title);
        s.setSpan(new TypefaceSpan("fonts/Serif.ttf"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setTitle(s);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setVisible(false);
        unregisterReceiver(mNetworkReceiver);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

}
