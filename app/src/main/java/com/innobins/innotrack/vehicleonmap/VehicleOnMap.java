package com.innobins.innotrack.vehicleonmap;

import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
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
import com.innobins.innotrack.activity.VehicleDetailActivity;
import com.innobins.innotrack.home.BaseActivity;
import com.innobins.innotrack.network.ResponseCallback;
import com.innobins.innotrack.network.WebserviceHelper;
import com.innobins.innotrack.parser.TraccerParser;
import com.innobins.innotrack.services.UpdateListViewService;
import com.innobins.innotrack.utils.URLContstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Created by silence12 on 23/1/18.
 */

public class VehicleOnMap extends BaseActivity implements View.OnClickListener {

    private static final String TAG = VehicleDetailActivity.class.getSimpleName();

    public static VehicleOnMap vehicleOnMap;
    GoogleMap googleMap;
    private TextView name_tv, positionId_tv, uniqueId_tv, status_tv, lastUpdate_tv, category_tv, contact_tv,
            speed_tv, distance_tv, timedated;
    private ProgressDialog progressDialog;
    private String nameString, uniqueIdString, lastUpdatetString, categoryString, statusString,address;
    private int id;
    private MarkerOptions markerOptions;
    Double speed;
    Double origin_latitute,origin_longitute;
    Double destiny_latitude,destiny_longitude;
    Double newLatitude,newLongitude;
    Double distance_trav;
    private CameraPosition cameraPosition;
    SharedPreferences mSharedPreferences;
    Intent updateListViewService;
    PendingIntent pintent;
    AlarmManager alarm;
    private List<LatLng> polyLineList;
    private int index, next;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyline, greyPolyLine;
    private Marker marker;
    private Handler handler;
    private LatLng startPosition, endPosition;
    private float v;
    private static View myView;
    boolean isUp;
    ArrayList<LatLng> lineStringArray = new ArrayList<LatLng>();
    LinearLayout coordinatorLayout ;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    ///start latlong static and then start = new latlong.....

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicleon_map);
        vehicleOnMap = this;
        updateListViewService = new Intent(getBaseContext(), UpdateListViewService.class);
        startService(updateListViewService);
        polyLineList = new ArrayList<>();
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        customTitle("   "+"Device Details");
        setPintent();
        initViews();
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
//        collapsingToolbarLayout.setTitle("Device Details");
//        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.BLACK);
//        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if (state.name().equals(State.COLLAPSED.name())) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                } else if (state.name().equals(State.IDLE.name())) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }
        });
        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Wait a moment...");
        progressDialog.show();
        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map25)).getMap();
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setPadding(10,10,10,20);
        Log.d("orignLat",String.valueOf(origin_latitute));
        CameraUpdate point = CameraUpdateFactory.newLatLng(new LatLng(origin_latitute, origin_longitute));
        googleMap.moveCamera(point);
        markerOptions = new MarkerOptions().position(new LatLng(origin_latitute,origin_longitute)).title(address);//set current position lat,long
        cameraPosition = new CameraPosition.Builder().target(new LatLng(origin_latitute,origin_longitute)).zoom(17).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.carnew_icon));
        marker=googleMap.addMarker(markerOptions);

//        uploadIndividualData();

        pintent = PendingIntent.getService(VehicleOnMap.this, 0, updateListViewService,0);
        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar cal = Calendar.getInstance();
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 6 * 1000, pintent);
    }

    private void initViews() {
        name_tv = (TextView) findViewById(R.id.project_name_tv);
        positionId_tv = (TextView) findViewById(R.id.positionId_tv);
        uniqueId_tv = (TextView) findViewById(R.id.uniqueid_tv);
        lastUpdate_tv = (TextView) findViewById(R.id.date_tv);
        status_tv = (TextView) findViewById(R.id.status_tv);
        category_tv = (TextView) findViewById(R.id.category_tv);
        speed_tv = (TextView) findViewById(R.id.speed_tv);
        distance_tv = (TextView) findViewById(R.id.distancecover_tv);
        timedated = (TextView) findViewById(R.id.diff_tv);
        coordinatorLayout = (LinearLayout)findViewById(R.id.mainswipe_ll);
//        myView = findViewById(R.id.shadoview_vv);
////        myView.setOnTouchListener(this);
//        myView.setOnClickListener(this);
//        myView.setVisibility(View.VISIBLE);
        isUp = false;
    }
//28.271915
//04-04 17:30:56.878 7883-7883/com.innobins.innotrack D/orignLat: 28.207438333333332
    public void uploadIndividualData(){
        String urlStr = "https://mtrack-api.appspot.com/api/get/devices/deviceid/" ;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deviceId",id);
            WebserviceHelper.getInstance().PostCall(VehicleOnMap.this, urlStr, jsonObject, new ResponseCallback() {
                @Override
                public void OnResponse(JSONObject Response) {
                    if(Response!=null) {
                        progressDialog.dismiss();
                        try {

                            JSONArray result = Response.getJSONArray("deviceData");
                            nameString = result.getJSONObject(0).getString("name");
                            uniqueIdString = result.getJSONObject(0).getString("uniqueId");
                            lastUpdatetString = result.getJSONObject(0).getString("lastUpdate");
                            statusString = result.getJSONObject(0).getString("status");
                            categoryString = result.getJSONObject(0).getString("category");
                            address = result.getJSONObject(0).getString("address");
                            destiny_latitude = result.getJSONObject(0).getDouble("latitude");
                            destiny_longitude = result.getJSONObject(0).getDouble("longitude");
                            speed = result.getJSONObject(0).getDouble("speed");
                            lastUpdate_tv.setText(result.getJSONObject(0).getString("latitude"));
                            String time = TraccerParser.datetime(lastUpdatetString);
                            String timeDiff = TraccerParser.numDays(lastUpdatetString);
                            progressDialog.dismiss();
                            name_tv.setText(nameString);
                            Log.d("VehicleMap", nameString);

                            if (address.equals("null")) {
                                address = "Loading...";
                            }
                            positionId_tv.setText(address);
//                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car));
//                        markerOptions.anchor(0.5f,0.5f)
//                                .flat(true);
//                        marker=googleMap.addMarker(markerOptions);
                            uniqueId_tv.setText(uniqueIdString);
                            lastUpdate_tv.setText(time);
                            status_tv.setText(statusString);
                            category_tv.setText(categoryString);
                            speed_tv.setText(String.valueOf(speed));
                            timedated.setText(timeDiff);
                            distance_tv.setText(String.valueOf(distance_trav));

                            lineStringArray.add(new LatLng(origin_latitute, origin_longitute));

                            if (Double.compare(origin_latitute, destiny_latitude) != 0) {
                                animationFunc();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        progressDialog.dismiss();
                        Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "Check your internet connectivity.", Snackbar.LENGTH_LONG);
                        snackbar1.show();                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
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
                                //Adjusting bounds
//                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
//                                for (LatLng latLng : polyLineList) {
//                                    builder.include(latLng);
//                                }
//                                LatLngBounds bounds = builder.build();
//                                CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
//                                googleMap.animateCamera(mCameraUpdate);

                                polylineOptions = new PolylineOptions();
                                polylineOptions.color(Color.GRAY);
                                polylineOptions.width(5);

                             /*   polylineOptions.startCap(new SquareCap());
                                polylineOptions.endCap(new SquareCap());
                                polylineOptions.jointType(ROUND);*/
                                polylineOptions.addAll(polyLineList);
                                greyPolyLine = googleMap.addPolyline(polylineOptions);

                                blackPolylineOptions = new PolylineOptions();
                                blackPolylineOptions.width(5);
                                blackPolylineOptions.color(Color.BLACK);
                                /*blackPolylineOptions.startCap(new SquareCap());
                                blackPolylineOptions.endCap(new SquareCap());
                                blackPolylineOptions.jointType(ROUND);*/
                                blackPolyline = googleMap.addPolyline(blackPolylineOptions);

                                /*googleMap.addMarker(new MarkerOptions()
                                        .position(polyLineList.get(polyLineList.size() - 1)));
*/
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

        getBaseContext().stopService(updateListViewService);
        pintent.cancel();
        super.onBackPressed();
        finish();

    }
    @Override
    protected void onResume() {
        super.onResume();
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

    @Override
    public void onDestroy() {
        getBaseContext().stopService(updateListViewService);
        pintent.cancel();
        super.onDestroy();
        updateListViewService = null;
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
    public void onClick(View view) {
        if(view.getId()==R.id.mainswipe_ll)
            Log.d("ClickedToHo", String.valueOf(view.getId()));
    }

    private void setPintent(){
        Intent getIntent = getIntent();
        id = getIntent.getIntExtra("id", -1);
        origin_latitute = getIntent.getDoubleExtra("lat",0.0);
        origin_longitute = getIntent.getDoubleExtra("long",0.0);
        nameString = getIntent.getStringExtra("name");
        uniqueIdString = getIntent.getStringExtra("uid");
        lastUpdatetString = getIntent.getStringExtra("lastupdate");
        statusString = getIntent.getStringExtra("status");
        categoryString = getIntent.getStringExtra("category");
        address = getIntent.getStringExtra("address");

    }

    public abstract static class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {

        private State mCurrentState = State.IDLE;

        @Override
        public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
            if(Math.abs(i)>=170){
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                myView.setLayoutParams(layoutParams);
            }else if(Math.abs(i)<=170){
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
                myView.setLayoutParams(layoutParams);
            }
            if (i == 0) {
                if (mCurrentState != State.EXPANDED) {
                    onStateChanged(appBarLayout, State.EXPANDED);
                }
                mCurrentState = State.EXPANDED;
            } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
                if (mCurrentState != State.COLLAPSED) {
                    onStateChanged(appBarLayout, State.COLLAPSED);
                }
                mCurrentState = State.COLLAPSED;
            } else {
                if (mCurrentState != State.IDLE) {
                    onStateChanged(appBarLayout, State.IDLE);
                }
                mCurrentState = State.IDLE;
            }
        }

        public abstract void onStateChanged(AppBarLayout appBarLayout, State state);

        public enum State {
            EXPANDED,
            COLLAPSED,
            IDLE
        }
    }

}

