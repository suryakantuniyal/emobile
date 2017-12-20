package com.innobins.innotrack.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.innobins.innotrack.parser.TraccerParser;

/**
 * Created by silence12 on 19/7/17.
 */
public class   SessionHandler  {

    static SharedPreferences.Editor mEditor, editor;
    public SessionHandler(){}

    public static void updateSnessionHandler(final Context mContext, final JSONArray result, final SharedPreferences mSharedPreferences) {

        final JSONObject previousData = new JSONObject();
        Log.d("resultof",String.valueOf(result.length()));
        // Looping Transaction
        for (int i = 0; i < result.length(); i++) {
            try {
                final JSONObject innerElement = result.getJSONObject(i);

                final JSONObject vehicleDict = new JSONObject();
                Log.d("innrelm",String.valueOf(innerElement));

                if ((innerElement.isNull("positionid")||innerElement.isNull("groupid"))==true ){
                    innerElement.put("positionid",0);
                    innerElement.put("groupid",0);
                }

                if ((innerElement.isNull("longitude")||innerElement.isNull("latitude")||innerElement.isNull("speed"))==true){
                    innerElement.put("longitude",00.00);
                    innerElement.put("latitude",00.00);
                    innerElement.put("speed",0.0);
                }
               /* Integer positionId = innerElement.getInt("positionid");
                Log.d("positionIdInt",String.valueOf(positionId));
                int positionid = positionId.intValue();*/

                vehicleDict.put("status",innerElement.getString("status"));
                vehicleDict.put("positionid",innerElement.getInt("positionid"));
                vehicleDict.put("name",innerElement.getString("name"));
                vehicleDict.put("longitude",innerElement.getDouble("longitude"));
                vehicleDict.put("latitude",innerElement.getDouble("latitude"));
                vehicleDict.put("deviceId",innerElement.getInt("deviceId"));
                vehicleDict.put("uniqueid",innerElement.getString("uniqueid"));
                vehicleDict.put("address",innerElement.getString("address"));
                vehicleDict.put("category",innerElement.getString("category"));
                vehicleDict.put("speed",innerElement.getDouble("speed"));
                vehicleDict.put("groupid",innerElement.getInt("groupid"));
                vehicleDict.put("lastUpdates", innerElement.getString("lastupdate"));

                try {
                    String lastupdation = innerElement.getString("lastupdate");
                    String time = TraccerParser.datetime(innerElement.getString("lastupdate"));
                    String newDate = TraccerParser.date(innerElement.getString("lastupdate"));
                    String timeDiff = TraccerParser.numDays(innerElement.getString("lastupdate"));
                    Log.d("newTimediif",timeDiff);
/*                    String timeDiff = TraccerParser.numDays(innerElement.getString("lastupdate"));
                    Log.d("timediffofTrcker",timeDiff);*/

                    vehicleDict.put("time",time);
                    vehicleDict.put("date",newDate);
                    vehicleDict.put("timeDiff",timeDiff);
                }catch (Exception e){
                    String time = "";
                    String timeDiff = "";
                    String parseDate = "";
                    vehicleDict.put("time",time);
                    vehicleDict.put("timeDiff",timeDiff);
                    vehicleDict.put("date",parseDate);
                }
                Log.d("devicetotallst",String.valueOf(vehicleDict));

                if (previousData.has("totalList")){
                    JSONArray deviceTotalLst = previousData.getJSONArray("totalList");
                    deviceTotalLst.put(vehicleDict);
                    Log.d("totallistvehicleDict",String.valueOf(deviceTotalLst));
                    previousData.put("totalList",deviceTotalLst);

                }else {
                    JSONArray deviceTotalLst = new JSONArray();
                    deviceTotalLst.put(vehicleDict);
                    previousData.put("totalList",deviceTotalLst);
                    Log.d("totllistoftrck",String.valueOf(deviceTotalLst));
                }

                if (innerElement.getString("status").equals("Online")){

                    if (previousData.has("OnLineList")){
                        JSONArray totalOnLineList = previousData.getJSONArray("OnLineList");
                        Log.d("onlinelist",String.valueOf(totalOnLineList));
                        totalOnLineList.put(vehicleDict);
                        Log.d("OnLineDevices",String.valueOf(totalOnLineList.length()));
                        previousData.put("OnLineList",totalOnLineList);
                    }else {
                        JSONArray totalOnlineList = new JSONArray();
                        totalOnlineList.put(vehicleDict);
                        previousData.put("OnLineList",totalOnlineList);
                    }
                }else {
                    if (previousData.has("OffLineList")){
                        JSONArray totalOffLineList = previousData.getJSONArray("OffLineList");
                        totalOffLineList.put(vehicleDict);
                        Log.d("OffLineDevices",String.valueOf(totalOffLineList.length()));
                        previousData.put("OffLineList",totalOffLineList);
                    }else {
                        JSONArray totalOffLIneList = new JSONArray();
                        totalOffLIneList.put(vehicleDict);
                        previousData.put("OffLineList",totalOffLIneList);
                    }
                }
                mEditor = mSharedPreferences.edit();
                mEditor.putString("deviceData", String.valueOf(previousData));
                Log.d("deviceData",String.valueOf(previousData));
                mEditor.apply();

        } catch (JSONException e) {
                e.printStackTrace();
        }



    }
}
        //End Here



}





