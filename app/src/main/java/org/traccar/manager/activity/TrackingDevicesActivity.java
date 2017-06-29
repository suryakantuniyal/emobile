package org.traccar.manager.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.traccar.manager.R;
import org.traccar.manager.api.APIServices;
import org.traccar.manager.model.VehicleList;
import org.traccar.manager.network.DetailResponseCallback;

import java.util.Map;

/**
 * Created by silence12 on 22/6/17.
 */

public class TrackingDevicesActivity extends AppCompatActivity {

    private GoogleMap googleMap;
    private MarkerOptions markerOptions;
    private CameraPosition cameraPosition;
    private TextView address_tv, speed_tv, vehicleName_tv, lastupdate_tv, travelled_tv;
    private ImageView imageView,currentLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devicestracking);
        initView();
    }


    private void initView() {

        address_tv = (TextView) findViewById(R.id.vehicle_location);
        imageView = (ImageView) findViewById(R.id.back_button);
        vehicleName_tv = (TextView) findViewById(R.id.vehiclename_tv);
        speed_tv = (TextView) findViewById(R.id.vehiclespeed_tv);
        lastupdate_tv = (TextView) findViewById(R.id.update_tv);
        travelled_tv = (TextView) findViewById(R.id.travelled_tv);
        currentLocation = (ImageView)findViewById(R.id.location);
        Intent getIntent = getIntent();
        int id = getIntent.getIntExtra("device_id", -1);
        String update = getIntent.getStringExtra("tupdate");
        String name = getIntent.getStringExtra("tname");
        String time = getIntent.getStringExtra("ttimer");
        vehicleName_tv.setText(name);
        lastupdate_tv.setText(update);
//        Log.e("Timmer",time);
        travelled_tv.setText(time);
        Log.e("idTrack", String.valueOf(id));
        trackOnMap();
        callDetailRequest(id);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TrackingDevicesActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_out_left,R.anim.slide_in_right);
                finish();

            }
        });
    }

    private void trackOnMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
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

            if(googleMap == null){
                Toast.makeText(getApplicationContext(),"Unable to create Map",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void callDetailRequest(int id) {
        APIServices.GetVehicleDetailById(TrackingDevicesActivity.this, id, new DetailResponseCallback() {
            @Override
            public void OnResponse(final VehicleList Response) {
                if(Response.address.equals("null")){
                    address_tv.setText("Loading...");
                }else {
                    address_tv.setText(Response.address);
                }
                String speed = String.valueOf(Response.speed);
                speed_tv.setText(speed);
                cameraPosition(Response);
                currentLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                      cameraPosition(Response);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        trackOnMap();
    }

    public void cameraPosition(VehicleList Response){
        markerOptions = new MarkerOptions().position(new LatLng(Response.latitute,Response.longitute)).title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_truck_med));
        googleMap.addMarker(markerOptions);
        cameraPosition = new CameraPosition.Builder().target(new LatLng(Response.latitute,Response.longitute)).zoom(14).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
