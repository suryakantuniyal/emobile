package org.traccar.manager.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.viewpagerindicator.CirclePageIndicator;
import org.traccar.manager.R;
import org.traccar.manager.adapter.ViewPagerAdapter;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by silence12 on 4/7/17.
 */

public class WelcomeMessageActivity extends AppCompatActivity {
   private RelativeLayout message_rl;
    private LinearLayout linearLayout;
    public View rootView;
    private  int currentPage ;
    private ViewPager circleviewPager;
    private ViewPagerAdapter viewPagerAdapter;
    TextView totalDevices,onlineDevices,offlineDevices;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_adds_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();

    }

    private void init(){
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),WelcomeMessageActivity.this);
        circleviewPager = (ViewPager)findViewById(R.id.pager);
        totalDevices = (TextView)findViewById(R.id.alldevices_tv);
        onlineDevices = (TextView)findViewById(R.id.allonline_tv);
        offlineDevices = (TextView)findViewById(R.id.alloffline_tv);
        totalDevices.setText(String.valueOf(MainActivity.AllSize));
        onlineDevices.setText(String.valueOf(MainActivity.onlinesize));
        offlineDevices.setText(String.valueOf(MainActivity.offlinesize));
        circleviewPager.setAdapter(viewPagerAdapter);
        circleviewPager.setCurrentItem(0);
        CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.dotindicator);
        indicator.setViewPager(circleviewPager);
        final float density = getResources().getDisplayMetrics().density;
        indicator.setRadius(3 * density);
        currentPage=0;
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
        },delay, period);

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
        Intent intent = new Intent(WelcomeMessageActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_out_right,R.anim.slide_in_left);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
