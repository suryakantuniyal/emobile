package in.gtech.gogeotrack.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.viewpagerindicator.CirclePageIndicator;

import in.gtech.gogeotrack.R;
import in.gtech.gogeotrack.adapter.ViewPagerAdapter;
import in.gtech.gogeotrack.model.VehicleList;
import in.gtech.gogeotrack.network.ResponseOnlineVehicle;
import in.gtech.gogeotrack.utils.URLContstant;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, ResponseOnlineVehicle {

    public static ArrayList<VehicleList> listArrayList;
    public static int AllSize, onlinesize, offlinesize;
    private static ProgressDialog progressDialog;
    TextView totalDevices, onlineDevices, offlineDevices;
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor, editor;
    SharedPreferences sharedPrefs;
    private int currentPage;
    private Button mapview_bb, listview_bb;
    private ViewPager circleviewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private CardView mapView_button, listView_button;
    private TextView alldevice_iv, online_iv, offline_iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.luncher_icon);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Wait a moment...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, MODE_PRIVATE);
        sharedPrefs = getSharedPreferences("ArrayList", MODE_PRIVATE);
        setSupportActionBar(toolbar);
        init();
        listArrayList = new ArrayList<VehicleList>();
        parseView();
        Bundle b = getIntent().getExtras();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        TextView username = (TextView) header.findViewById(R.id.username_tv);
        String userName = mSharedPreferences.getString(URLContstant.KEY_USERNAME, "");
        username.setText(userName);
    }

    private void init() {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), Main2Activity.this);
        circleviewPager = (ViewPager) findViewById(R.id.pager);
        totalDevices = (TextView) findViewById(R.id.all_count);
        onlineDevices = (TextView) findViewById(R.id.online_count);
        offlineDevices = (TextView) findViewById(R.id.offline_count);
        listView_button = (CardView) findViewById(R.id.listview_cv);
        mapView_button = (CardView) findViewById(R.id.mapview_cv);
        alldevice_iv = (TextView) findViewById(R.id.alldevices_view);
        online_iv = (TextView) findViewById(R.id.online_img);
        offline_iv = (TextView) findViewById(R.id.offline_img);
        mapview_bb = (Button) findViewById(R.id.mapview_bb);
        listview_bb = (Button) findViewById(R.id.listview_bb);
        listView_button.setOnClickListener(this);
        circleviewPager.setAdapter(viewPagerAdapter);
        circleviewPager.setCurrentItem(0);
        alldevice_iv.setOnClickListener(this);
        online_iv.setOnClickListener(this);
        offline_iv.setOnClickListener(this);
        mapView_button.setOnClickListener(this);
        mapview_bb.setOnClickListener(this);
        listview_bb.setOnClickListener(this);
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
    public void onSuccessOnline(ArrayList<VehicleList> result) {
        Log.d("Response size", String.valueOf(result.size()));
    }

    private void parseView() {
        Log.d("MainSize", String.valueOf(SplashActivity.onlinesize));
        progressDialog.dismiss();
        listArrayList = SplashActivity.listArrayList;
        String json = sharedPrefs.getString("onlinesize", null);

        int total = SplashActivity.AllSize - Integer.parseInt(json);
        Log.d("Kab ka bhai", String.valueOf(total));
        totalDevices.setText(String.valueOf(SplashActivity.AllSize));
        onlineDevices.setText(json);
        offlineDevices.setText(String.valueOf(total));

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_mapview) {

            Intent intent = new Intent(Main2Activity.this, MapViewActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_listview) {
            Intent intent = new Intent(Main2Activity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        } else if (id == R.id.nav_reports) {

            Intent intent = new Intent(Main2Activity.this, ReportsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        } else if (id == R.id.nav_contact) {

            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:09999095036"));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
//                return TODO;
            }
            startActivity(callIntent);

        } else if (id == R.id.nav_online) {
            Intent intent = new Intent(Main2Activity.this, OnLineOffLineActivity.class);
            intent.putExtra("onoff", "online");
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);


        } else if (id == R.id.nav_offline) {
            Intent intent = new Intent(Main2Activity.this, OfflineActivity.class);
            intent.putExtra("onoff", "offline");
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        } else if (id == R.id.nav_logout) {
            mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
            mEditor = mSharedPreferences.edit();
            mEditor.putBoolean(URLContstant.KEY_LOGGED_IN, false);
            mEditor.putString(URLContstant.KEY_USERNAME,null);
            mEditor.putString(URLContstant.KEY_PASSWORD,null);
            Intent intent = new Intent(Main2Activity.this, SignUpAccount.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.listview_bb || view.getId() == R.id.alldevices_view) {
            Intent intent = new Intent(Main2Activity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (view.getId() == R.id.online_img) {
            Intent intent = new Intent(Main2Activity.this, OnLineOffLineActivity.class);
            intent.putExtra("onoff", "online");
            startActivity(intent);
        } else if (view.getId() == R.id.offline_img) {
            Intent intent = new Intent(Main2Activity.this, OfflineActivity.class);
            intent.putExtra("onoff", "offline");
            startActivity(intent);
        } else if (view.getId() == R.id.mapview_bb) {
            Intent intent = new Intent(Main2Activity.this, MapViewActivity.class);
            startActivity(intent);
        }
    }
}
