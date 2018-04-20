package com.innobins.innotrack.activity.Reports;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.animation.LinearInterpolator;

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
import com.innobins.innotrack.activity.GoGeoDataProDialog;
import com.innobins.innotrack.home.BaseActivity;
import com.innobins.innotrack.network.ResponseCallback;
import com.innobins.innotrack.network.WebserviceHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RouteMapActivity extends BaseActivity {
    GoogleMap googleMap;
    GoGeoDataProDialog goGeoDataProDialog;
    String startTime,endTime;
    Double lat,lng;
    private MarkerOptions markerOptions;
    private CameraPosition cameraPosition;
    private Marker marker;
    Double origin_latitute=0.0,origin_longitute=0.0;
    Double destiny_latitude,destiny_longitude;
    ArrayList<LatLng> lineStringArray ;
    private List<LatLng> polyLineList;
    private int index, next;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyline, greyPolyLine;
    private float v;
    private Handler handler;
    private LatLng startPosition, endPosition;
    Double newLatitude,newLongitude;
    PolylineOptions poly;
    Polyline polyline;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.innotrack_icon);
        customTitle("   "+"RouteMap Report");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        goGeoDataProDialog = new GoGeoDataProDialog(this);
        lineStringArray = new ArrayList<LatLng>();



        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.routemap)).getMap();
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setPadding(10,10,10,20);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(28.644800, 77.216721)));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(8));
       // googleMap.addMarker(new MarkerOptions().position(new LatLng(origin_latitute,origin_longitute)));

        mapReportData();
      /*  googleMap.addPolyline(new PolylineOptions().add(new LatLng(32.33203,77.200445),
                new LatLng(32.3308566667,77.1992616667),
                new LatLng(28.673585,77.0911283333),
                new LatLng(28.6736083333,77.0911283333))
                .width(10).color(Color.RED));*/
        //reloadMap();
    }

    private void mapReportData() {

        goGeoDataProDialog.show();
        Intent getIntent = getIntent();
        // divId = getIntent.getIntExtra("divReport",-1);
        String newName = null;
        String divId = getIntent.getStringExtra("divReport");
        Log.d("divid",divId);
        String deviceName = getIntent.getStringExtra("deviceName");
        String reportType = getIntent.getStringExtra("reportType");
        String time1 = getIntent.getStringExtra("startTime");
        String time2 = getIntent.getStringExtra("endTime");
        String date1 = getIntent.getStringExtra("startdate");
        String date2 = getIntent.getStringExtra("endDate");
        startTime = date1+" "+time1;
        endTime   = date2+" "+time2;
        Log.d("starttme", deviceName);
        if (deviceName.endsWith(",")) {
            newName = deviceName.substring(0, deviceName.length() - 1);
        }

        String mUrl = "https://mtrack-api.appspot.com/api/report/view/";
        final JSONObject jsonObject = new JSONObject();
        try{

            jsonObject.put("startDate",startTime);
            jsonObject.put("endDate",endTime);
            jsonObject.put("deviceLst",divId);
            jsonObject.put("reportType",reportType);

            final String device_Name = newName;
            Log.d("finaldevce",device_Name);
            WebserviceHelper.getInstance().PostCall(RouteMapActivity.this, mUrl, jsonObject, new ResponseCallback() {
                @Override
                public void OnResponse(JSONObject Response) {
                    if (Response!=null){
                        try {
                            PolylineOptions polylineOptions = new PolylineOptions();
                            JSONArray jsonArray = Response.getJSONArray("reportData");
                            Log.d("routejson",String.valueOf(jsonArray));
                            for (int i= 0;i<jsonArray.length();i++){
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                origin_latitute = jsonArray.getJSONObject(0).getDouble("latitude");
                                origin_longitute=jsonArray.getJSONObject(0).getDouble("longitude");
                                destiny_latitude = jsonObject1.getDouble("latitude");
                                destiny_longitude = jsonObject1.getDouble("longitude");

                                polylineOptions.color(Color.BLUE);
                                polylineOptions.width(10);
                                polylineOptions.add(new LatLng(destiny_latitude, destiny_longitude));
                                goGeoDataProDialog.dismiss();
                            }
                            polyline = googleMap.addPolyline(polylineOptions);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void drawPolyLineOnMap() {

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

    private List<LatLng> decodePoly(String encoded) {{
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

    }

    private void reloadMap() {

     /*   MarkerOptions markerOptions = new MarkerOptions();
        googleMap.addMarker(markerOptions);
*/

        //markerOptions.position(point).snippet(devicename);
        //drawMarker(new LatLng(lat, lng),jsonObject.getString("status"),name,address,jsonObject.getString("category"));


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
