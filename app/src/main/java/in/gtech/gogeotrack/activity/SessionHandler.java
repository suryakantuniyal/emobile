package in.gtech.gogeotrack.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import in.gtech.gogeotrack.api.APIServices;
import in.gtech.gogeotrack.network.DetailResponseCallback;
import in.gtech.gogeotrack.parser.TraccerParser;

/**
 * Created by silence12 on 19/7/17.
 */

public class   SessionHandler  {

    static SharedPreferences.Editor mEditor, editor;
    public SessionHandler(){}

    public static void reportHandler(Context context,final JSONArray result,final SharedPreferences mSharedPreferences){

        final JSONObject previousReport = new JSONObject();
        final JSONObject sumryPrevious = new JSONObject();

        for (int i = 0;i<result.length();i++){
            try {
                final JSONObject innerElement = result.getJSONObject(i);
                final JSONObject reportDict = new JSONObject();
                final JSONObject sumryDict  = new JSONObject();

                reportDict.put("id",innerElement.getInt("deviceId"));
                reportDict.put("time",innerElement.getString("serverTime"));
                reportDict.put("type",innerElement.getString("type"));
                reportDict.put("geofence",innerElement.getInt("geofenceId"));

                sumryDict.put("deviceName",innerElement.getString("deviceName"));
                sumryDict.put("distance",innerElement.getDouble("distance"));
                sumryDict.put("averageSpeed",innerElement.getDouble("averageSpeed"));
                sumryDict.put("maxSpeed",innerElement.getDouble("maxSpeed"));
                sumryDict.put("engineHours",innerElement.getInt("engineHours"));


                if (previousReport.has("totalReport")){
                    JSONArray evntTotalLst = previousReport.getJSONArray("totalReport");
                    Log.d("evntTotalLst",String.valueOf(evntTotalLst));
                    evntTotalLst.put(reportDict);
                    previousReport.put("totalReport",evntTotalLst);
                }else {
                    JSONArray evntTotalLst = new JSONArray();
                    evntTotalLst.put(reportDict);
                    previousReport.put("totalReport",evntTotalLst);
                    Log.d("prvsReport",String.valueOf(previousReport));
                }

                if (sumryPrevious.has("summaryReport")){
                    JSONArray sumryTotalLst = sumryPrevious.getJSONArray("summaryReport");
                    sumryTotalLst.put(sumryDict);
                    sumryPrevious.put("summaryReport",sumryTotalLst);

                }else {
                    JSONArray sumryTotalLst = new JSONArray();
                    sumryTotalLst.put(sumryDict);
                    sumryPrevious.put("summaryReport",sumryTotalLst);

                }

            }catch (JSONException e){
                e.printStackTrace();
            }
            mEditor = mSharedPreferences.edit();
            mEditor.putString("reportData", String.valueOf(previousReport));
            mEditor.putString("summaryData",String.valueOf(sumryPrevious));
            mEditor.apply();
        }
    }

    public static void updateSnessionHandler(Context mContext, final JSONArray result, final SharedPreferences mSharedPreferences) {

        final JSONObject previousData = new JSONObject();
        // Looping Transaction

        for (int i = 0; i < result.length(); i++) {
            try {
               // int uid = result.getJSONObject(i).getInt("id");
                int id = result.getJSONObject(i).getInt("positionId");

                Log.d("newgetnrate", String.valueOf(id));

                final JSONObject innerElement = result.getJSONObject(i);
                final JSONObject vehicleDict = new JSONObject();

                //Get Value inside vehicle Dictionary
                vehicleDict.put("id", innerElement.getInt("positionId"));
                vehicleDict.put("divId",innerElement.getInt("id"));

                vehicleDict.put("name", innerElement.getString("name"));
                vehicleDict.put("uniqueId", innerElement.getString("uniqueId"));
                vehicleDict.put("status", innerElement.getString("status"));
                vehicleDict.put("lastUpdates", innerElement.getString("lastUpdate"));
                vehicleDict.put("category", innerElement.getString("category"));
                vehicleDict.put("positionId", innerElement.getInt("positionId"));
                vehicleDict.put("groupId",innerElement.getInt("groupId"));
                try {
                    String time = TraccerParser.datetime(innerElement.getString("lastUpdate"));
                    String timeDiff = TraccerParser.numDays(innerElement.getString("lastUpdate"));
                    String parseDate = TraccerParser.date(innerElement.getString("lastUpdate"));
                    vehicleDict.put("time", time);
                    vehicleDict.put("timeDiff", timeDiff);
                    vehicleDict.put("date",parseDate);
                } catch (Exception e) {
                    String time = "";
                    String timeDiff = "";
                    String parseDate = "";
                    vehicleDict.put("time", time);
                    vehicleDict.put("timeDiff", timeDiff);
                    vehicleDict.put("date",parseDate);
                }

                APIServices.GetVehicleDetailById(mContext, id, new DetailResponseCallback() {
                    @Override
                    public void OnResponse(JSONArray Response) {
                        try {
                            vehicleDict.put("id",Response.getJSONObject(0).getInt("id"));

                            Log.d("NEWID",String.valueOf(Response.getJSONObject(0).getInt("deviceId")));

                            vehicleDict.put("address", Response.getJSONObject(0).getString("address"));
                            vehicleDict.put("latitude",Response.getJSONObject(0).getDouble("latitude"));
                            vehicleDict.put("longitude",Response.getJSONObject(0).getDouble("longitude"));
                            vehicleDict.put("speed",Response.getJSONObject(0).getDouble("speed"));
                            vehicleDict.put("distance",Response.getJSONObject(0).getJSONObject("attributes").getString("distance"));

                            //Adding Value to existing Datasets
                            if (previousData.has("totalLst")) {
                                JSONArray deviceTotalLst = previousData.getJSONArray("totalLst");
                                deviceTotalLst.put(vehicleDict);
                                previousData.put("totalLst", deviceTotalLst);
                            } else {
                                JSONArray deviceTotalLst = new JSONArray();
                                deviceTotalLst.put(vehicleDict);
                                previousData.put("totalLst", deviceTotalLst);
                            }

                            if (innerElement.getString("status").equals("online")) {
                                if (previousData.has("onlineLst")) {
                                    JSONArray deviceTotalLst = previousData.getJSONArray("onlineLst");
                                    deviceTotalLst.put(vehicleDict);
                                    previousData.put("onlineLst", deviceTotalLst);
                                } else {
                                    JSONArray deviceTotalLst = new JSONArray();
                                    deviceTotalLst.put(vehicleDict);
                                    previousData.put("onlineLst", deviceTotalLst);
                                }
                            } else {
                                if (previousData.has("offlineLst")) {
                                    JSONArray deviceTotalLst = previousData.getJSONArray("offlineLst");
                                    deviceTotalLst.put(vehicleDict);
                                    previousData.put("offlineLst", deviceTotalLst);
                                } else {
                                    JSONArray deviceTotalLst = new JSONArray();
                                    deviceTotalLst.put(vehicleDict);
                                    previousData.put("offlineLst", deviceTotalLst);
                                }
                            }
                            mEditor = mSharedPreferences.edit();
                            mEditor.putString("deviceData", String.valueOf(previousData));
                            mEditor.apply();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        //End Here


    }
}





