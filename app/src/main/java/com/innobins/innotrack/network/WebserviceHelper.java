package com.innobins.innotrack.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.innobins.innotrack.api.RestapiCall;

import org.json.JSONObject;

/**
 * Created by surya on 21/9/17.
 */

public class WebserviceHelper {

    private static WebserviceHelper ourInstance = new WebserviceHelper();
    private static String mUrl = "http://app.asap-9ff41.appspot.com/";
    public static WebserviceHelper getInstance() {
        return ourInstance;
    }
    private static int TIMEOUT_IN_SECONDS = 15*1000;
    private static int MAX_RETRIES = 2;
    private static int BACKOFF_MULT = 1;
    private WebserviceHelper() {
    }

    public void PostCall(Context context, String Url, JSONObject Data, final ResponseCallback ResponseCallback){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Url, Data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response != null)
                    Log.d("Restful response",response.toString());
                ResponseCallback.OnResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error", error.toString());
                ResponseCallback.OnResponse(null);
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_IN_SECONDS,
                MAX_RETRIES,
                BACKOFF_MULT));
        RestapiCall.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public void PostCallThird(Context context, String Url, JSONObject Data, final ResponseCallback ResponseCallback){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Url, Data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response != null)
                    Log.d("Restful response",response.toString());
                ResponseCallback.OnResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error", error.toString());
                ResponseCallback.OnResponse(null);
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_IN_SECONDS,
                MAX_RETRIES,
                BACKOFF_MULT));
        RestapiCall.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public void GetCall(Context context, String Url, final ResponseCallback ResponseCallback){
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, Url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if (response != null)
                            Log.d("Restful response",response.toString());
                        ResponseCallback.OnResponse(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ResponseCallback.OnResponse(null);
                    }
                });
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_IN_SECONDS,
                MAX_RETRIES,
                BACKOFF_MULT));
        RestapiCall.getInstance(context).addToRequestQueue(jsObjRequest);
    }
}

