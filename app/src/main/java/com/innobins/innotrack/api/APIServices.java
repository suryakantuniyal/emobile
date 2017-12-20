package com.innobins.innotrack.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.innobins.innotrack.model.VehicleList;
import com.innobins.innotrack.network.DetailResponseCallback;
import com.innobins.innotrack.network.ReportResponseCallBack;
import com.innobins.innotrack.network.ResponseCallback;
import com.innobins.innotrack.network.ResponseCallbackEvents;
import com.innobins.innotrack.network.ResponseOnlineVehicle;
import com.innobins.innotrack.network.ResponseStringCallback;
import com.innobins.innotrack.parser.TraccerParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.innobins.innotrack.utils.URLContstant;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by silence12 on 15/6/17.
 */

public final class APIServices {

    public static ArrayList<VehicleList> vehicleLists;
    public static ArrayList<VehicleList> onlineList;
    public static ArrayList<VehicleList> offlineList;
    public static VehicleList mvehicles;
    static Context mContext;
    private static APIServices ourInstance = new APIServices();
    private static String mUrl = URLContstant.LOCAL_ULR;
    private static int TIMEOUT_IN_SECONDS = 15 * 1000;
    private static int MAX_RETRIES = 2;
    private static int BACKOFF_MULT = 1;
    static SharedPreferences sharedPrefs;
    static String base;
    Context context;

    private APIServices() {

    }

    public static APIServices getInstance()
    {
        return ourInstance;
    }

    public  void GetAllVehicleList(final Context context,String userName,String password, final ResponseCallbackEvents ResponseCallback) {
//        final String requestedUrl = URLContstant.BASE_URL + "/" + URLContstant.ALL_VEHICLES + "/?" + "email=yash.bhat94%40gmail.com&password=admin";
        final String requestedUrl = URLContstant.BASE_URL + "/" + URLContstant.ALL_VEHICLES + "/?" + "email="+userName +"&password=" +password;
        Log.d("API", ":: request url :: " + requestedUrl);
        String BaseUser= userName +":"+password;
        base = Base64.encodeToString(BaseUser.toString().getBytes(),Base64.DEFAULT);
        JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (requestedUrl, new Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response != null)
                            vehicleLists = TraccerParser.parseGetVehiclesRequest(response);
                        ResponseCallback.onSuccess(vehicleLists);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", ":: Volley Error :: " + error);
//                        Toast.makeText(context, "Unable to reach our servers. Please check your internet connection.", Toast.LENGTH_SHORT).show();

                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic "+base);
                return headers;
            }
        };
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_IN_SECONDS,
                MAX_RETRIES,
                BACKOFF_MULT));
        RestapiCall.getInstance(context).addToRequestQueue(jsObjRequest);
    }
/*
    public static void AllReport(final Context context, String url, final ResponseAllReport ResponseCallback){
        final String reqstedUrl = mUrl+ url;
        final JsonArrayRequest jsObjectRequest = new JsonArrayRequest(reqstedUrl, new Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
               if (response!= null){
                   ResponseCallback.onSuccessOnline(response);
               }
            }
        });
    }
*/
public static void GetAllVehicle(final Context context,String userName,String password,final ResponseOnlineVehicle ResponseCallback){
    final String requestUrl = URLContstant.DEVICE_LIST ;
    Log.d("requrl",requestUrl);
    String BaseUser = userName + ":" +password;
    base = Base64.encodeToString(BaseUser.toString().getBytes(),Base64.DEFAULT);
    Log.d("baseVal",base);

    JsonArrayRequest jsObjectReqst = new JsonArrayRequest
            (requestUrl, new Response.Listener<JSONArray>() {

        @Override
        public void onResponse(JSONArray response) {

            Log.d("vhclRespnse","yahoo");

            Log.d("vhclRespnse",String.valueOf(response));
            if (response != null)
                ResponseCallback.onSuccessOnline(response);

                Log.d("AllNewRes", String.valueOf(response));
        }
    }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("Error", ":: Volley Error :: " + error);
        }
    }){
        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Basic "+base);
            return headers;
        }
    };
    jsObjectReqst.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_IN_SECONDS,
            MAX_RETRIES,
            BACKOFF_MULT));
    RestapiCall.getInstance(context).addToRequestQueue(jsObjectReqst);
}

    public static void GetAllOnlineVehicleList(final Context context,String userName,String password, final ResponseOnlineVehicle ResponseCallback) {
//        final String requestedUrl = URLContstant.BASE_URL + "/" + URLContstant.ALL_VEHICLES + "/?" + "email=yash.bhat94%40gmail.com&password=admin";
        final String requestedUrl = URLContstant.BASE_URL + "/" + URLContstant.ALL_VEHICLES +"/?" + "email="+userName +"&password=" + password;
        String BaseUser= userName +":"+password;
        base = Base64.encodeToString(BaseUser.toString().getBytes(),Base64.DEFAULT);
        Log.d("API", ":: request url :: " + requestedUrl);
    }

   // final String requestedUrl = URLContstant.BASE_URL+"/"+URLContstant.SUMMARY_REPORT+"/?"+"deviceId="+divId+"&from="+startTime + "&to="+endTime;
/*
        public static void GetSummaryReport(final Context context, int divId, String startTime, String endTime, final ReportResponseCallBack ResponseCallBack ){
            final String requestedUrl = URLContstant.BASE_URL + "/" + URLContstant.SUMMARY_REPORT +"/?"+"deviceId=" + divId + "&from="+startTime + "&to="+endTime;
            Log.d("sumryUrl",requestedUrl);

            JsonArrayRequest jsObjRequest = new JsonArrayRequest
                    (requestedUrl, new Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray response) {
                            if (response != null){
                                Log.d("EventREsponse", response.toString());
                                ResponseCallBack.onSuccessOnline(response);
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Error", ":: Volley Error :: " + error);
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Basic "+base);
                    return headers;
                }
            };
            jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                    TIMEOUT_IN_SECONDS,
                    MAX_RETRIES,
                    BACKOFF_MULT));
            RestapiCall.getInstance(context).addToRequestQueue(jsObjRequest);
        }
*/
        public static void GetReport(final Context context, String url, final ReportResponseCallBack ResponseCallBack ) {
            //final String requestedUrl = URLContstant.BASE_URL + "/" + URLContstant.EVENT_REPORT + "/?" + "deviceId=" + divId + "&from="+startTime + "&to="+endTime;
            final String reqstedUrl = mUrl + "/" + url;
            Log.d("reqstedEventUrl", reqstedUrl);

            JsonArrayRequest jsObjREquest = new JsonArrayRequest(reqstedUrl, new Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    if (response != null) {
                        Log.d("ReportResponse",response.toString());
                        ResponseCallBack.onGetReport(response);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                 //ResponseCallBack.onGetReport(null);
                }
            })
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Basic " + base);
                    return headers;
                }
            };
            jsObjREquest.setRetryPolicy(new DefaultRetryPolicy(
                    TIMEOUT_IN_SECONDS,
                    MAX_RETRIES,
                    BACKOFF_MULT));
            RestapiCall.getInstance(context).addToRequestQueue(jsObjREquest);
        }
 /*       *//*JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (reqstedUrl, new Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        if (response != null){
                            Log.d("EventREsponse", response.toString());
                            ResponseCallBack.onSuccessOnline(response);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", ":: Volley Error :: " + error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic "+base);
                return headers;
            }
        };

    }
*/
    public static void GetVehicleDetailById(final Context context, int id, final DetailResponseCallback ResponseCallback) {
        final String requestedUrl =  URLContstant.DEVICE_ID  + id;
        Log.d("req_url",requestedUrl);
        final JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (requestedUrl, new Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response != null){
                            Log.d("GetVehicleId_response", response.toString());
                            ResponseCallback.OnResponse(response);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", ":: Volley Error :: " + error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic "+base);
                return headers;
            }
        };
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_IN_SECONDS,
                MAX_RETRIES,
                BACKOFF_MULT));
        RestapiCall.getInstance(context).addToRequestQueue(jsObjRequest);
    }
    public static Double doubleToDecimalConverter(Double dob) {
        double speed = dob;
        DecimalFormat df = new DecimalFormat("#.##");
        speed = Double.valueOf(df.format(speed));
        return speed;
    }
    public void PostCall(Context context, String Url, JSONObject Data, final ResponseCallback ResponseCallback) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(mUrl + Url, Data, new Listener<JSONObject>() {
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

/*
    public void PostCallUrl(Context context, String Url, final ResponseCallback ResponseCallback) {
        Log.d("Response", "Inside");
        String newUrl = mUrl+"/"+Url;
        Log.d("NEWURLFOREVENT",newUrl);
        final JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (mUrl+"/"+Url,null, new Response.Listener<JSONArray>()
          {
              @Override
              public void onResponse(JSONArray response) {
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
*/
    public void GetVehicleAllList(Context context,String url,String userName,String password,final ResponseStringCallback responseCallBack){
        Log.d("newurl",url);
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String BaseUser= userName +":"+password;
        Log.d("BAseUser",BaseUser);
        base = Base64.encodeToString(BaseUser.toString().getBytes(),Base64.DEFAULT);
        //base = Base64.encodeToString(BaseUser.toString().getBytes(),Base64.DEFAULT);
       Log.d("BaseVal",base);
        StringRequest vehcleList = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                responseCallBack.OnResponse(response);
                Log.d("responseOfVhcl",response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseCallBack.OnFial(error.networkResponse.statusCode);
                Log.d("ErrorResponse",String.valueOf(error));
            }
        }){
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
        requestQueue.add(vehcleList);
    }
    public void PostProblem(Context context, String url, final ResponseStringCallback responseCallback) {
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        responseCallback.OnResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        responseCallback.OnFial(error.networkResponse.statusCode);
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
    public void PostReport(Context context, String url, final ResponseCallback responseCallback) {
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(mUrl + url, null,
                new Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", String.valueOf(response));
                        responseCallback.OnResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                       // responseCallback.OnFial(error.networkResponse.statusCode);
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
       // queue.add(postRequest);
    }


}
