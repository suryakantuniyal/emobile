package in.gtech.gogeotrack.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import in.gtech.gogeotrack.api.APIServices;
import in.gtech.gogeotrack.model.VehicleList;
import in.gtech.gogeotrack.network.DetailResponseCallback;
import in.gtech.gogeotrack.parser.TraccerParser;

/**
 * Created by silence12 on 19/7/17.
 */

public class   SessionHandler  {

    static SharedPreferences.Editor mEditor, editor;
    public SessionHandler(){}





    public static void updateSnessionHandler(Context mContext, final JSONArray result, final SharedPreferences mSharedPreferences) {


        final JSONObject previousData = new JSONObject();

        // Looping Transaction

        for (int i = 0; i < result.length(); i++) {
            try {
                int id = result.getJSONObject(i).getInt("positionId");
                final JSONObject innerElement = result.getJSONObject(i);
                final JSONObject vehicleDict = new JSONObject();

                //Get Value inside vehicle Dictionary
                vehicleDict.put("id", innerElement.getInt("positionId"));
                vehicleDict.put("name", innerElement.getString("name"));
                vehicleDict.put("uniqueId", innerElement.getString("uniqueId"));
                vehicleDict.put("status", innerElement.getString("status"));
                vehicleDict.put("lastUpdates", innerElement.getString("lastUpdate"));
                vehicleDict.put("category", innerElement.getString("category"));
                vehicleDict.put("positionId", innerElement.getInt("positionId"));
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





