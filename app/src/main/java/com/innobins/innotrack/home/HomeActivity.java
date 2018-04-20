package com.innobins.innotrack.home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.innobins.innotrack.R;
import com.innobins.innotrack.activity.GoGeoDataProDialog;
import com.innobins.innotrack.activity.MainActivity;
import com.innobins.innotrack.activity.MapViewActivity;
import com.innobins.innotrack.activity.OfflineActivity;
import com.innobins.innotrack.activity.OnLineOffLineActivity;
import com.innobins.innotrack.activity.Reports.ReportsActivity;
import com.innobins.innotrack.activity.SessionHandler;
import com.innobins.innotrack.activity.SignUpAccount;
import com.innobins.innotrack.adapter.ViewPagerAdapter;
import com.innobins.innotrack.network.ResponseCallback;
import com.innobins.innotrack.network.WebserviceHelper;
import com.innobins.innotrack.running.RunningActivity;
import com.innobins.innotrack.unknownActivity.UnknownActivity;
import com.innobins.innotrack.utils.URLContstant;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by silence12 on 22/1/18.
 */

public class HomeActivity extends BaseActivity implements  NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {


    TextView totalDevices, onlineDevices, offlineDevices;
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;
    private int currentPage;
    private ViewPager circleviewPager;
    private ViewPagerAdapter viewPagerAdapter;
    GoGeoDataProDialog goGeoDataProDialog;
    TextView online_tv,offline_tv,running_tv,nodata_tv ;
    public static final int MULTIPLE_PERMISSIONS = 10;
    String[] permissions= new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CALL_PHONE,};

    ProgressBar online_prog,offline_prog,running_prog,nodata_prog;
    LinearLayout mapView_ll,listView_ll,coordinatorLayout,online_ll,offline_ll,running_ll,nodata_ll ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.innotrack_icon);
        customTitle("   "+"InnoTrack");
        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, MODE_PRIVATE);
        goGeoDataProDialog = new GoGeoDataProDialog(this);
        goGeoDataProDialog.show();
        int permission_call = PermissionChecker.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        int permission_location = PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        init();
        getData();
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        TextView username = (TextView) header.findViewById(R.id.username_tv);
        String userName = mSharedPreferences.getString(URLContstant.KEY_USERNAME, "");
        String upperString = userName.substring(0,1).toUpperCase() + userName.substring(1);
        username.setText(upperString);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permission_call == PermissionChecker.PERMISSION_GRANTED && permission_location == PermissionChecker.PERMISSION_GRANTED) {


            } else if(checkPermissions()) {
//                checkPermission();
            }
        }else {
            CheckGPS();
        }
        vehicleStatusData();
    }

    private void getData() {
        goGeoDataProDialog.show();
        String mUrl = "https://mtrack-api.appspot.com/api/get/devices/byuser/" ;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userid",mSharedPreferences.getInt(URLContstant.KEY_LOGEDIN_USERID,-1));
            WebserviceHelper.getInstance().PostCall(HomeActivity.this, mUrl, jsonObject, new ResponseCallback() {
                @Override
                public void OnResponse(JSONObject Response) {
                    if(Response!=null) {
                        Log.d("HomeResponse", String.valueOf(Response) + "," + mSharedPreferences.getInt(URLContstant.KEY_LOGEDIN_USERID, -1));
                        try {
                            JSONArray jsonArray = Response.getJSONArray("deviceData");
                            Log.d("DevicesDataSize", String.valueOf(jsonArray.length()));
                            SessionHandler.updateSnessionHandler(getBaseContext(), jsonArray, mSharedPreferences);
                            goGeoDataProDialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        goGeoDataProDialog.dismiss();
                        Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "Check your internet connectivity.", Snackbar.LENGTH_LONG);
                        snackbar1.show();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void init() {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), HomeActivity.this);
        circleviewPager =  findViewById(R.id.pager);
        totalDevices =  findViewById(R.id.all_count);
        onlineDevices =  findViewById(R.id.online_count);
        offlineDevices =  findViewById(R.id.offline_count);
        mapView_ll = findViewById(R.id.mapview_ll);
        listView_ll = findViewById(R.id.listview_ll);
        coordinatorLayout = findViewById(R.id.mail_ll);
        online_tv = findViewById(R.id.progress_circle_text_on);
        offline_tv = findViewById(R.id.progress_circle_text_off);
        running_tv = findViewById(R.id.progress_circle_text_run);
        nodata_tv = findViewById(R.id.progress_circle_text_nodata);
        online_prog = findViewById(R.id.progress_online);
        offline_prog = findViewById(R.id.progress_offline);
        running_prog = findViewById(R.id.progress_running);
        nodata_prog = findViewById(R.id.progress_nodata);
        online_ll = findViewById(R.id.onlinem_ll);
        offline_ll = findViewById(R.id.offlinem_ll);
        running_ll = findViewById(R.id.running_ll);
        nodata_ll = findViewById(R.id.nodata_ll);
        mapView_ll.setOnClickListener(this);
        listView_ll.setOnClickListener(this);
        online_ll.setOnClickListener(this);
        offline_ll.setOnClickListener(this);
        running_ll.setOnClickListener(this);
        nodata_ll.setOnClickListener(this);
        circleviewPager.setAdapter(viewPagerAdapter);
        circleviewPager.setCurrentItem(0);
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


    private void vehicleStatusData() {
        String mUrl = "https://mtrack-api.appspot.com/api/get/summary/byuser/" ;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userid",mSharedPreferences.getInt(URLContstant.KEY_LOGEDIN_USERID,-1));
            Log.d("UserId", String.valueOf(mSharedPreferences.getInt(URLContstant.KEY_LOGEDIN_USERID,-1)));
            WebserviceHelper.getInstance().PostCall(HomeActivity.this, mUrl, jsonObject, new ResponseCallback() {
                @Override
                public void OnResponse(JSONObject Response) {
                    goGeoDataProDialog.dismiss();
                    Log.d("SummeryResponse", String.valueOf(Response));
                    try {
                        JSONArray jsonArray = Response.getJSONArray("summaryData");
                        JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                        int onnline  = jsonObject1.getInt("online_vehicle");
                        int offline = jsonObject1.getInt("offline_vehicle");
                        int running = jsonObject1.getInt("running_vehicle");
                        int nodata = jsonObject1.getInt("unknown_vehicle");
                        int totalvalue = onnline+offline+running+nodata ;
                        online_tv.setText(String.valueOf(onnline));
                        offline_tv.setText(String.valueOf(offline));
                        running_tv.setText(String.valueOf(running));
                        nodata_tv.setText(String.valueOf(nodata));
                        online_prog.setProgress(progress(totalvalue,onnline));
                        offline_prog.setProgress(progress(totalvalue,offline));
                        running_prog.setProgress(progress(totalvalue,running));
                        nodata_prog.setProgress(progress(totalvalue,nodata));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

            Intent intent = new Intent(HomeActivity.this, MapViewActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_listview) {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        } else if (id == R.id.nav_reports) {

            Intent intent = new Intent(HomeActivity.this, ReportsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        } else if (id == R.id.nav_contact) {

//            Intent callIntent = new Intent(Intent.ACTION_CALL);
//            callIntent.setData(Uri.parse("tel:09999095036"));
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
////                return TODO;
//            }
//            startActivity(callIntent);

            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        Integer.parseInt("123"));
            } else {
                startActivity(new Intent(Intent.ACTION_CALL).setData(Uri.parse("tel:09999095036")));
            }

        } else if (id == R.id.nav_online) {
            Intent intent = new Intent(HomeActivity.this, OnLineOffLineActivity.class);
            intent.putExtra("onoff", "online");
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);


        } else if (id == R.id.nav_offline) {
            Intent intent = new Intent(HomeActivity.this, OfflineActivity.class);
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
            mEditor.putString("deviceData","{}");
            mEditor.apply();
            Intent intent = new Intent(HomeActivity.this, SignUpAccount.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override

    public void onClick(View view) {
        if (view.getId() == R.id.listview_ll || view.getId() == R.id.alldevices_view) {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (view.getId() == R.id.mapview_ll ) {
            Intent intent = new Intent(HomeActivity.this, MapViewActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }else if(view.getId() == R.id.onlinem_ll){
            Intent intent = new Intent(HomeActivity.this, OnLineOffLineActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }else if(view.getId() == R.id.offlinem_ll){
            Intent intent = new Intent(HomeActivity.this, OfflineActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }else if(view.getId() == R.id.running_ll){
            Intent intent = new Intent(HomeActivity.this, RunningActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }else if(view.getId() == R.id.nodata_ll){
            Intent intent = new Intent(HomeActivity.this, UnknownActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    public void CheckGPS(){

        if (!isGPSenabled(HomeActivity.this)){

            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setMessage("GPS is Disable.Enbale it for better results")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {

        }
    }

    public boolean isGPSenabled(Context context) {
        LocationManager lm = null;
        boolean gps_enabled = false,network_enabled=false;
        if(lm==null)
            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try{
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){}
        try{
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch(Exception ex){}
        if (gps_enabled&&network_enabled){
            return true;
        }else {
            return false;
        }
    }


    private int progress(int max,int value){
        int result = (value *100)/max ;
        return result ;
    }
    //  <======= RunTime Permission Checking above api 23   ===============>

    private  boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(HomeActivity.this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // permissions granted.
                } else {
//                    String permissionss = "";
//                    for (String per : permissionsList) {
//                        permissionss += "\n" + per;
//                    }
                    // permissions list of don't granted permission
                }
                return;
            }
        }
    }





}

