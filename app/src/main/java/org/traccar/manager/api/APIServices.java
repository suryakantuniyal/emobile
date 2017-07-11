package org.traccar.manager.api;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.traccar.manager.model.VehicleList;
import org.traccar.manager.network.DetailResponseCallback;
import org.traccar.manager.network.ResponseOfflineVehicle;
import org.traccar.manager.network.ResponseOnlineVehicle;
import org.traccar.manager.network.ResponseStringCallback;
import org.traccar.manager.parser.TraccerParser;
import org.traccar.manager.network.ResponseCallback;
import org.traccar.manager.network.ResponseCallbackEvents;
import org.traccar.manager.utils.URLContstant;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by silence12 on 15/6/17.
 */

public  final class APIServices {

    static Context mContext;
    private static APIServices ourInstance = new APIServices();
    private static String mUrl = URLContstant.LOCAL_ULR;

    public static APIServices getInstance() {
        return ourInstance;
    }

    private static int TIMEOUT_IN_SECONDS = 15 * 1000;
    private static int MAX_RETRIES = 2;
    private static int BACKOFF_MULT = 1;
    public static ArrayList<VehicleList> vehicleLists;
    public static ArrayList<VehicleList> onlineList;

    public static ArrayList<VehicleList> offlineList;
    public static VehicleList mvehicles;

    private APIServices() {
    }

    public static void GetAllVehicleList(final Context context, final ResponseCallbackEvents ResponseCallback) {
        final String requestedUrl = URLContstant.BASE_URL + "/" + URLContstant.ALL_VEHICLES +"/?"+"email=yash.bhat94%40gmail.com&password=admin";
        Log.d("API", ":: request url :: " + requestedUrl);
        JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (requestedUrl, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        if (response != null)
                            Log.d("Restful response", response.toString());
                        vehicleLists = TraccerParser.parseGetVehiclesRequest(response);
                        ResponseCallback.onSuccess(vehicleLists);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", ":: Volley Error :: " + error);
                        Toast.makeText(context, "Unable to reach our servers. Please check your internet connection.", Toast.LENGTH_SHORT).show();

                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic eWFzaC5iaGF0OTRAZ21haWwuY29tOmFkbWlu");
                return headers;
            }
        };
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_IN_SECONDS,
                MAX_RETRIES,
                BACKOFF_MULT));
        RestapiCall.getInstance(context).addToRequestQueue(jsObjRequest);
    }

    public static void GetAllOnlineVehicleList(final Context context, final ResponseOnlineVehicle ResponseCallback) {
        final String requestedUrl = URLContstant.BASE_URL + "/" + URLContstant.ALL_VEHICLES +"/?"+"email=yash.bhat94%40gmail.com&password=admin";
        Log.d("API", ":: request url :: " + requestedUrl);
        JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (requestedUrl, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        if (response != null)
                            Log.d("Restful response", response.toString());
                        onlineList = TraccerParser.parseGeOnlinetVehiclesRequest(response);
                        ResponseCallback.onSuccessOnline(onlineList);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", ":: Volley Error :: " + error);
                        Toast.makeText(context, "Unable to reach our servers. Please check your internet connection.", Toast.LENGTH_SHORT).show();

                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic eWFzaC5iaGF0OTRAZ21haWwuY29tOmFkbWlu");
                return headers;
            }
        };
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_IN_SECONDS,
                MAX_RETRIES,
                BACKOFF_MULT));
        RestapiCall.getInstance(context).addToRequestQueue(jsObjRequest);
    }


    public static void GetAllOfflineVehicleList(final Context context, final ResponseOfflineVehicle ResponseCallback) {
        final String requestedUrl = URLContstant.BASE_URL + "/" + URLContstant.ALL_VEHICLES +"/?"+"email=yash.bhat94%40gmail.com&password=admin";
        Log.d("API", ":: request url :: " + requestedUrl);
        JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (requestedUrl, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        if (response != null)
                            Log.d("Restful response", response.toString());
                        offlineList = TraccerParser.parseGeOfflinetVehiclesRequest(response);
                        ResponseCallback.onSuccessOffline(offlineList);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", ":: Volley Error :: " + error);
                        Toast.makeText(context, "Unable to reach our servers. Please check your internet connection.", Toast.LENGTH_SHORT).show();

                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic eWFzaC5iaGF0OTRAZ21haWwuY29tOmFkbWlu");
                return headers;
            }
        };
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_IN_SECONDS,
                MAX_RETRIES,
                BACKOFF_MULT));
        RestapiCall.getInstance(context).addToRequestQueue(jsObjRequest);
    }

    public void PostCall(Context context, String Url, JSONObject Data, final ResponseCallback ResponseCallback) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(mUrl + Url, Data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response != null)
                    Log.d("Restful response", response.toString());
                ResponseCallback.OnResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (null != error.networkResponse) {
                    Log.d(TAG + ": ", "Error Response code: " + error.networkResponse.statusCode);
                    //listener.getResult(false);
                }
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_IN_SECONDS,
                MAX_RETRIES,
                BACKOFF_MULT));

        RestapiCall.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public void PostCallUrl(Context context, String Url, final ResponseCallback ResponseCallback) {
        Log.d("Response", "Inside");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(mUrl + Url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Restful response", response.toString());
                        if (response != null)
                            Log.d("Restful response", response.toString());
                        ResponseCallback.OnResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (null != error.networkResponse) {
                    Log.d(TAG + ": ", "Error Response code: " + error.networkResponse.statusCode);
                    //listener.getResult(false);
                }
            }
        }) {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_IN_SECONDS,
                MAX_RETRIES,
                BACKOFF_MULT));

        RestapiCall.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }


    public void PostProblem(Context context,String url,final ResponseStringCallback responseCallback){
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        responseCallback.OnResponse(response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", String.valueOf(error));
                    }
                }
        ) {


            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };
        queue.add(postRequest);
    }


    public static void GetVehicleDetailById(final Context context,int id, final DetailResponseCallback ResponseCallback) {
        final String requestedUrl = URLContstant.BASE_URL + "/" + "api" +"/" + "positions" + "/?" +"id=" + id;
        Log.d("API", ":: request url :: " + requestedUrl);
        final JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (requestedUrl,new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        if (response != null)
                            Log.d("Detail response", response.toString());

                        try {
                            JSONObject jsonObject = response.getJSONObject(0);
                            mvehicles = TraccerParser.getVehicleDetailById(jsonObject);
                            mvehicles.latitute = jsonObject.getDouble("latitude");
                            mvehicles.longitute = jsonObject.getDouble("longitude");
                            mvehicles.speed =  doubleToDecimalConverter(jsonObject.getDouble("speed"));
                            mvehicles.distance_travelled = jsonObject.getJSONObject("attributes").getDouble("distance");
                            ResponseCallback.OnResponse(mvehicles);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", ":: Volley Error :: " + error);
                        Toast.makeText(context, "Unable to reach our servers. Please check your internet connection.", Toast.LENGTH_SHORT).show();

                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic eWFzaC5iaGF0OTRAZ21haWwuY29tOmFkbWlu");
                return headers;
            }
        };
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_IN_SECONDS,
                MAX_RETRIES,
                BACKOFF_MULT));
        RestapiCall.getInstance(context).addToRequestQueue(jsObjRequest);

    }
    public static Double doubleToDecimalConverter(Double dob){
        double speed = dob;
        DecimalFormat df = new DecimalFormat("#.##");
        speed = Double.valueOf(df.format(speed));
        return  speed;
    }

}
