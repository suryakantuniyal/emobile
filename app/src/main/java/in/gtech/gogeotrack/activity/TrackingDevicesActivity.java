package in.gtech.gogeotrack.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.test.mock.MockPackageManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import in.gtech.gogeotrack.R;
import in.gtech.gogeotrack.services.GPSTracker;

/**
 * Created by silence12 on 22/6/17.
 */

public class   TrackingDevicesActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 2;
    private static ProgressDialog progressDialog;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    GPSTracker gps;
    String statsString,address,categoryString,uniqId;
    Double latitute,longitute,speed;
    private GoogleMap googleMap;
    private MarkerOptions markerOptions;
    private CameraPosition cameraPosition;
    private TextView address_tv, speed_tv, vehicleName_tv, lastupdate_tv, travelled_tv;
    private ImageView imageView, currentLocation,trackDeviceIcn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devicestracking);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Tracking");
        getSupportActionBar().setIcon(R.mipmap.luncher_icon);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Wait a moment...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        initView();
        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission)
                    != MockPackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{mPermission},
                        REQUEST_CODE_PERMISSION);
                gps = new GPSTracker(TrackingDevicesActivity.this);

                // check if GPS enabled
                if (gps.canGetLocation()) {

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

//                    // \n is for new line
//                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: "
//                            + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void initView() {

        address_tv = (TextView) findViewById(R.id.vehicle_location);
        vehicleName_tv = (TextView) findViewById(R.id.vehiclename_tv);
        speed_tv = (TextView) findViewById(R.id.vehiclespeed_tv);
        lastupdate_tv = (TextView) findViewById(R.id.update_tv);
        travelled_tv = (TextView) findViewById(R.id.travelled_tv);
        currentLocation = (ImageView) findViewById(R.id.location);
        trackDeviceIcn = (ImageView)findViewById(R.id.trackDeviceIcn);
        trackOnMap();

    }
    private void trackOnMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.setPadding(0,10,10,200);
            progressDialog.dismiss();
            Intent getIntent = getIntent();
            String update = getIntent.getStringExtra("tupdate");
            String name = getIntent.getStringExtra("tname");
            String time = getIntent.getStringExtra("ttimer");
            uniqId = getIntent.getStringExtra("uid");
            statsString = getIntent.getStringExtra("status");
            categoryString = getIntent.getStringExtra("category");
            address = getIntent.getStringExtra("address");
            latitute = getIntent.getDoubleExtra("lat",0.0);
            longitute = getIntent.getDoubleExtra("long",0.0);
            speed = getIntent.getDoubleExtra("speed",0.0);
            vehicleName_tv.setText(name);
            lastupdate_tv.setText(update);
            travelled_tv.setText(time);
            if (address.equals("null")) {
                address_tv.setText("Loading...");
            } else {
                address_tv.setText(address);
            }
            double d = speed;
            String formattedData = String.format("%.02f", d);
            speed_tv.setText(formattedData);
            cameraPosition(address,latitute,longitute);
            currentLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cameraPosition(address,latitute,longitute);
                }
            });
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

            if (googleMap == null) {
                Toast.makeText(getApplicationContext(), "Unable to create Map", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        trackOnMap();
    }

    public void cameraPosition(String add,Double latitute,Double longitute) {
        String address;
        if (add.equals("null")) {
            address = "Loading...";
        } else {
            address = add;
        }
        Log.d("Latlong", latitute + "  " + longitute);
        markerOptions = new MarkerOptions().position(new LatLng(latitute, longitute)).title(address);

        if(categoryString.equals("person")){
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_punch_person));
            trackDeviceIcn.setImageResource(R.drawable.ic_punch_person);
        }
        if (uniqId.equals("007835051035")){
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.motobikes));
            trackDeviceIcn.setImageResource(R.drawable.motobikes);
        }
        else {
            if (statsString.equals("online")) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.greentruck));
            } else if (statsString.equals("offline")) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.redtruck));
            } else {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_truck_med));
            }
        }
        googleMap.addMarker(markerOptions);
        cameraPosition = new CameraPosition.Builder().target(new LatLng(latitute,longitute)).zoom(14).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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

}
