package org.traccar.manager.parser;

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.traccar.manager.model.VehicleList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by silence12 on 19/6/17.
 */

public final class TraccerParser {

    public static long msDiff;
    public static long daysDiff,minuteDiff,hoursDiff;

    public static ArrayList<VehicleList> parseGetVehiclesRequest (JSONArray jsonVehicles){
        ArrayList<VehicleList> mVehiclesList = new ArrayList<VehicleList>();
        try {
            for (int i = 0; i < jsonVehicles.length(); i++) {
                VehicleList vehicles = new VehicleList();
                JSONObject jsonObject = jsonVehicles.getJSONObject(i);
                String date = jsonObject.getString("lastUpdate");
                Log.d("Date",date);
                vehicles.id = Integer.parseInt(jsonObject.get("id").toString());
                vehicles.name = jsonObject.getString("name");
                vehicles.category = jsonObject.getString("category");
                vehicles.uniqueId = jsonObject.getString("uniqueId");
                vehicles.lastUpdates = date(date);
                vehicles.time = datetime(date);
                vehicles.timeDiff = numDays(date);
                vehicles.status = jsonObject.getString("status");
                vehicles.positionId = jsonObject.getInt("positionId");
                mVehiclesList.add(vehicles);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mVehiclesList;

    }

    public static VehicleList getVehicleDetailById(JSONObject jsonObject){

        VehicleList vehicleList = new VehicleList();

        try {
                vehicleList.address = jsonObject.getString("address");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return vehicleList ;
    }

    public static String date(String str){
        String st = str;
        Log.d("diff", String.valueOf(numDays(st)));
        String[] datesplit = str.split("T");
        String date = datesplit[0];
        return date;
    }

    public static String datetime(String str) {
        String[] datesplit = str.split("\\.");
        String date = datesplit[0];
        String[] tim = date.split("T",2);
        String result;
        if(tim.length>1)
        {
            result = tim[1];

        } else
            result = tim[0];
        return result;
    }

    public static DateTime getDate(String hh) {
        DateTime dateTime = DateTime.parse(hh);
        return dateTime;
    }

    public static String numDays(String str){
        String time = null;
        msDiff = Calendar.getInstance().getTimeInMillis() - getDate(str).getMillis();
        minuteDiff = TimeUnit.MILLISECONDS.toMinutes(msDiff);
        hoursDiff = TimeUnit.MILLISECONDS.toHours(msDiff);
        daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff);
        if(daysDiff!=0) {
            if(daysDiff==1)
            time = daysDiff + " Day";
            else
                time = daysDiff + " Days";
        }
        else if(hoursDiff!=0) {
            if(hoursDiff==1)
            time = hoursDiff + " hour";
            else
                time = hoursDiff+" hours";
        }else {
            time = minuteDiff+" min";
        }

        return time;
    }

}
