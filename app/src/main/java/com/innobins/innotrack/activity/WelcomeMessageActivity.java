package com.innobins.innotrack.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.innobins.innotrack.adapter.ViewPagerAdapter;
import com.innobins.innotrack.home.BaseActivity;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.Timer;
import java.util.TimerTask;

import in.innobins.innotrack.R;

/**
 * Created by silence12 on 4/7/17.
 */

public class WelcomeMessageActivity extends BaseActivity implements View.OnClickListener {
    public View rootView;
    TextView totalDevices, onlineDevices, offlineDevices;
    private RelativeLayout message_rl;
    private LinearLayout linearLayout;
    private int currentPage;
    private ViewPager circleviewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private Button mapView_button, listView_button;
    private ImageView alldevice_iv, online_iv, offline_iv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_adds_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        getSupportActionBar().setIcon(R.mipmap.innotrack_icon);
        customTitle("   "+"Home");
        setSupportActionBar(toolbar);
        init();

    }

    private void init() {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), WelcomeMessageActivity.this);
        circleviewPager = (ViewPager) findViewById(R.id.pager);
        totalDevices = (TextView) findViewById(R.id.alldevices_tv);
        onlineDevices = (TextView) findViewById(R.id.alldevices);
        offlineDevices = (TextView) findViewById(R.id.online_tv);
        listView_button = (Button) findViewById(R.id.listview_bb);
        alldevice_iv = (ImageView) findViewById(R.id.alldevices_iv);
        online_iv = (ImageView) findViewById(R.id.online_iv);
        offline_iv = (ImageView) findViewById(R.id.offline_iv);
        listView_button.setOnClickListener(this);
        circleviewPager.setAdapter(viewPagerAdapter);
        circleviewPager.setCurrentItem(0);
        alldevice_iv.setOnClickListener(this);
        online_iv.setOnClickListener(this);
        offline_iv.setOnClickListener(this);
        CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.dotindicator);
        indicator.setViewPager(circleviewPager);
        final float density = getResources().getDisplayMetrics().density;
        indicator.setRadius(3 * density);
        currentPage = 0;
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                circleviewPager.setCurrentItem(currentPage % 10, true);
                currentPage++;
            }
        };

        int delay = 500;
        int period = 1000;
        Timer swipeTimer = new Timer();
        swipeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, delay, period);

        // Pager listener over indicator
        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                currentPage = position;

            }

            @Override
            public void onPageScrolled(int pos, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int pos) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.listview_bb || view.getId() == R.id.alldevices_iv) {
            Intent intent = new Intent(WelcomeMessageActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (view.getId() == R.id.online_img) {
            Intent intent = new Intent(WelcomeMessageActivity.this, OnLineOffLineActivity.class);
            intent.putExtra("onoff", "online");
            startActivity(intent);
        } else if (view.getId() == R.id.offline_img) {
            Intent intent = new Intent(WelcomeMessageActivity.this, OnLineOffLineActivity.class);
            intent.putExtra("onoff", "offline");
            startActivity(intent);
        }
    }
}
