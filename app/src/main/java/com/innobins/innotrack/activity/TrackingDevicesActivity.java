package com.innobins.innotrack.activity;

import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.innobins.innotrack.R;
import com.innobins.innotrack.home.BaseActivity;
import com.innobins.innotrack.network.ResponseCallback;
import com.innobins.innotrack.network.WebserviceHelper;
import com.innobins.innotrack.parser.TraccerParser;
import com.innobins.innotrack.services.UpdateListViewService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by silence12 on 22/6/17.
 */

public class TrackingDevicesActivity extends BaseActivity {

    public static TrackingDevicesActivity trackingDevicesActivity;
    private static ProgressDialog progressDialog;
    String statsString,address,categoryString,uniqId,name;
    Double latitute,longitute,speed;
    private GoogleMap googleMap;
    Double origin_latitute,origin_longitute;
    Double destiny_latitude,destiny_longitude;
    MarkerOptions markerOptions;
    CameraPosition cameraPosition;
    private TextView address_tv, speed_tv, vehicleName_tv, lastupdate_tv, travelled_tv;
    private ImageView currentLocation,trackDeviceIcn;
    private int id;
    String update;
    Double newLatitude,newLongitude;
    private List<LatLng> polyLineList;
    private int index, next;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyline, greyPolyLine;
    private Marker marker;
    private Handler handler;
    private LatLng startPosition, endPosition;
    private float v;
    PendingIntent pintent;
    AlarmManager alarm;
    Intent updateListViewService;
    RelativeLayout coordinatorLayout ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devicestracking);
        trackingDevicesActivity = this;
        updateListViewService = new Intent(getBaseContext(), UpdateListViewService.class);
        startService(updateListViewService);
        polyLineList = new ArrayList<>();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        customTitle("   "+"Tracking");
        getSupportActionBar().setIcon(R.mipmap.innotrack_icon);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Wait a moment...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        initView();
        setPintent();
        uploadIndividualData();

        pintent = PendingIntent.getService(TrackingDevicesActivity.this, 0, updateListViewService,0);
        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar cal = Calendar.getInstance();
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 6 * 1000, pintent);

    }

    public void uploadIndividualData() {

        String urlStr = "https://mtrack-api.appspot.com/api/get/devices/deviceid/" ;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deviceId",id);
            WebserviceHelper.getInstance().PostCall(TrackingDevicesActivity.this, urlStr, jsonObject, new ResponseCallback() {
                @Override
                public void OnResponse(JSONObject Response) {
                    if(Response!=null) {
                        progressDialog.dismiss();
                        Log.d("TrckingDetails", String.valueOf(Response));
                        try {
                            JSONArray result = Response.getJSONArray("deviceData");
                            update = result.getJSONObject(0).getString("lastUpdate");
                            name = result.getJSONObject(0).getString("name");
                            uniqId = result.getJSONObject(0).getString("uniqueId");
                            statsString = result.getJSONObject(0).getString("status");
                            categoryString = result.getJSONObject(0).getString("category");
                            address = result.getJSONObject(0).getString("address");
                            latitute = result.getJSONObject(0).getDouble("latitude");
                            longitute = result.getJSONObject(0).getDouble("longitude");
                            destiny_latitude = latitute ;
                            destiny_longitude = longitute ;
                            speed = result.getJSONObject(0).getDouble("speed");
                            String time = TraccerParser.datetime(update);
                            String timeDiff = TraccerParser.numDays(update);
                            String newDate = TraccerParser.date(update);
                            Log.d("updatetime", update);
                            lastupdate_tv.setText(newDate);
                            vehicleName_tv.setText(name);
                            travelled_tv.setText(time);
                            if (address.equals("null")) {
                                address_tv.setText("Loading...");
                            } else {
                                address_tv.setText(address);
                            }
                            double d = speed;
                            String formattedData = String.format("%.02f", d);
                            speed_tv.setText(formattedData);
                            cameraPosition(address, latitute, longitute);
                            currentLocation.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    cameraPosition(address, latitute, longitute);
                                }
                            });

                            if (Double.compare(origin_latitute, destiny_latitude) != 0) {
                                animationFunc();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "Check your internet connectivity.", Snackbar.LENGTH_LONG);
                        snackbar1.show();
                    }
                }
            });
        } catch (JSONException e) {
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
        coordinatorLayout = (RelativeLayout)findViewById(R.id.main_rl);
        trackOnMap();

    }
    private void trackOnMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.setPadding(0,10,10,200);
            progressDialog.dismiss();
            Intent getIntent = getIntent();
            id = getIntent.getIntExtra("id", -1);
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(), "Unable to create Map", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void animationFunc() {

        String requestUrl = null;
        try {
            requestUrl = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&"
                    + "transit_routing_preference=less_driving&"
                    + "origin=" + origin_latitute + "," + origin_longitute + "&"
                    + "destination=" + destiny_latitude+","+destiny_longitude + "&"
                    + "key=" + getResources().getString(R.string.google_directions_key);
            Log.d("Orgin-Desti",origin_latitute+","+destiny_latitude);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                    requestUrl, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray jsonArray = response.getJSONArray("routes");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject route = jsonArray.getJSONObject(i);
                                    JSONObject poly = route.getJSONObject("overview_polyline");
                                    String polyline = poly.getString("points");
                                    polyLineList = decodePoly(polyline);
                                }

                                polylineOptions = new PolylineOptions();
                                polylineOptions.color(Color.GRAY);
                                polylineOptions.width(5);
                                polylineOptions.addAll(polyLineList);
                                greyPolyLine = googleMap.addPolyline(polylineOptions);
                                blackPolylineOptions = new PolylineOptions();
                                blackPolylineOptions.width(5);
                                blackPolylineOptions.color(Color.BLACK);
                                blackPolyline = googleMap.addPolyline(blackPolylineOptions);
                                ValueAnimator polylineAnimator = ValueAnimator.ofInt(0, 100);
                                polylineAnimator.setDuration(5000);
                                polylineAnimator.setInterpolator(new LinearInterpolator());
                                polylineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                        List<LatLng> points = greyPolyLine.getPoints();
                                        int percentValue = (int) valueAnimator.getAnimatedValue();
                                        int size = points.size();
                                        int newPoints = (int) (size * (percentValue / 100.0f));
                                        List<LatLng> p = points.subList(0, newPoints);
                                        blackPolyline.setPoints(p);
                                    }
                                });
                                googleMap.clear();
                                marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(destiny_latitude,destiny_longitude))
                                        .flat(true)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.carnew_icon)));

                                handler = new Handler();
                                index = -1;
                                next = 1;
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (index < polyLineList.size() - 1) {
                                            index++;
                                            next = index + 1;
                                        }
                                        if (index < polyLineList.size() - 1) {
                                            startPosition = polyLineList.get(index);
                                            endPosition = polyLineList.get(next);
                                        }
                                        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                                        valueAnimator.setDuration(6000);
                                        valueAnimator.setInterpolator(new LinearInterpolator());
                                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                            @Override
                                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                                try {
                                                    v = valueAnimator.getAnimatedFraction();
                                                    Log.d("ValueAnimator", String.valueOf(v));
                                                    newLongitude = v * endPosition.longitude + (1 - v)
                                                            * startPosition.longitude;
                                                    newLatitude = v * endPosition.latitude + (1 - v)
                                                            * startPosition.latitude;

                                                    LatLng newPos = new LatLng(newLatitude, newLongitude);
                                                    marker.setPosition(newPos);
                                                    marker.setAnchor(0.5f, 0.5f);
                                                    marker.setRotation(getBearing(startPosition, newPos));
                                                    googleMap.moveCamera(CameraUpdateFactory
                                                            .newCameraPosition
                                                                    (new CameraPosition.Builder()
                                                                            .target(newPos)
                                                                            .zoom(17)
                                                                            .build()));
                                                }catch (Exception e){

                                                }
                                            }
                                        });
                                        if(Double.compare(origin_latitute,destiny_latitude)!=0) {
                                            valueAnimator.start();
//                                        handler.postDelayed(this, 6000);
                                            origin_latitute = destiny_latitude;
                                            origin_longitute = destiny_longitude;
                                        }else {
                                            valueAnimator.end();
                                        }
                                    }
                                }, 500);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    private float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }


    @Override
    protected void onResume() {
        super.onResume();
        trackOnMap();
    }

    public void cameraPosition(String add,Double latitute,Double longitute) {
        //String address;
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
        if (categoryString.equals("motorcycle")){
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.motobikes));
            trackDeviceIcn.setImageResource(R.drawable.motobikes);
        }
        else {
            if (statsString.equals("Online")) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.carnew_icon));
            } else if (statsString.equals("Offline")) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car_red));
            } else {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_truck_med));
            }
        }
        googleMap.clear();
        googleMap.addMarker(markerOptions);
        cameraPosition = new CameraPosition.Builder().target(new LatLng(latitute,longitute)).zoom(15).build();
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
    public void onDestroy() {
        // Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        getBaseContext().stopService(updateListViewService);
        pintent.cancel();
        super.onDestroy();
        updateListViewService = null;

    }

    @Override
    public void onBackPressed() {
        getBaseContext().stopService(updateListViewService);
        pintent.cancel();
        super.onBackPressed();
    }

    private void setPintent(){
        Intent getIntent = getIntent();
        id = getIntent.getIntExtra("id", -1);
        latitute = getIntent.getDoubleExtra("lat",0.0);
        longitute = getIntent.getDoubleExtra("long",0.0);
        origin_latitute = latitute ;
        origin_longitute = longitute ;
        name = getIntent.getStringExtra("tname");
        uniqId = getIntent.getStringExtra("uid");
        update = getIntent.getStringExtra("tupdate");
        statsString = getIntent.getStringExtra("status");
        categoryString = getIntent.getStringExtra("category");
        address = getIntent.getStringExtra("address");
        progressDialog.dismiss();

    }


}
