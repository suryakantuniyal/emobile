package in.gtech.gogeotrack.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import in.gtech.gogeotrack.R;
import in.gtech.gogeotrack.model.VehicleList;
import in.gtech.gogeotrack.services.UpdateListViewService;
import in.gtech.gogeotrack.utils.URLContstant;

/**
 * Created by silence12 on 5/7/17.
 */

public class MapViewActivity extends AppCompatActivity {

    public static MapViewActivity mapViewActivity;

    public static MenuItem searchMenuItem;
    GoogleMap googleMap;
    SharedPreferences sharedPreferences;
    int locationCount = 0;
    ArrayList<VehicleList> result;
    MarkerOptions markerOptions;
    private List<VehicleList> mFilteredList;
    private CameraPosition cameraPosition;
    SharedPreferences mSharedPreferences;
    String userName,password;
    private static ProgressDialog progressDialog;

    PendingIntent pintent;
    AlarmManager alarm;
    Intent updateListViewService;
    String address,name;

    TextView name_tv,address_tv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapview);

        mapViewActivity = this;
        updateListViewService = new Intent(getBaseContext(), UpdateListViewService.class);
        startService(updateListViewService);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.luncher_icon);
        setTitle("MapView");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Map...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
        userName = mSharedPreferences.getString(URLContstant.KEY_USERNAME, "");
        password = mSharedPreferences.getString(URLContstant.KEY_PASSWORD,"");
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        // Showing statusen
        if (status != ConnectionResult.SUCCESS) {

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        } else {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            reloadMap();
            }

        pintent = PendingIntent.getService(MapViewActivity.this, 0, updateListViewService,0);
        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar cal = Calendar.getInstance();
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 6 * 1000, pintent);
    }

    public void reloadMap() {



        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setPadding(10,10,10,20);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(28.644800, 77.216721)));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
        JSONObject previousData = null;
        try {
            previousData = new JSONObject(mSharedPreferences.getString("deviceData","{}"));
            Log.d("tt", String.valueOf(previousData));
            JSONArray vehicleList = previousData.getJSONArray("totalList");
            Log.d("to", String.valueOf(vehicleList));
            Double lat = 0.0;
            Double lng = 0.0;
            progressDialog.dismiss();
            for (int i = 0; i < vehicleList.length(); i++) {
                JSONObject jsonObject = vehicleList.getJSONObject(i);
                lat = jsonObject.getDouble("latitude");
                lng = jsonObject.getDouble("longitude");

                name = jsonObject.getString("name");

                if (jsonObject.getString("address").equals("null")) {
                    address = "Loading...";
                } else {
                    address = jsonObject.getString("address");
                }
               // title = name+","+ address;

                drawMarker(new LatLng(lat, lng),jsonObject.getString("status"),name,address,jsonObject.getString("category"));


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void drawMarker( LatLng point, String str, final String add, final String devicename, String category) {


        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point).title(add);
        markerOptions.position(point).snippet(devicename);

        if(category.equals("person")){
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_punch_person));
        }else {
            if (str.equals("Online")) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.greentruck));
            } else if (str.equals("Offline")) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.redtruck));
            } else {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_truck_med));
            }
        }
        googleMap.addMarker(markerOptions);

/*
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }
            @Override
            public View getInfoContents(Marker marker) {

                View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);
                // Getting the position from the marker
                // LatLng latLng = marker.getPosition();
                // Getting reference to the TextView to set latitude
                name_tv = (TextView) v.findViewById(R.id.name);
                // Getting reference to the TextView to set longitude
                address_tv = (TextView) v.findViewById(R.id.address);
                // Setting the latitude
                name_tv.setText("Name:" + add);
                // Setting the longitude
                address_tv.setText("Address:"+ devicename);

                // Returning the view containing InfoWindow contents
                return v;

            }
        });
*/


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
//        searchMenuItem = menu.findItem(R.id.action_search);
//        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem menuItem) {
//                SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
//                searchVehicle(searchView);
//                return true;
//            }
//
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
//                return true;
//            }
//        });
        return super.onCreateOptionsMenu(menu);
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
    public void onDestroy() {

        // Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        getBaseContext().stopService(updateListViewService);
        pintent.cancel();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        getBaseContext().stopService(updateListViewService);
        pintent.cancel();
        super.onBackPressed();
    }

}

