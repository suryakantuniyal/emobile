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

/**
 * Created by silence12 on 19/6/17.
 */

public final class TraccerParser {

    public static ArrayList<VehicleList> parseGetVehiclesRequest (JSONArray jsonVehicles){
        ArrayList<VehicleList> mVehiclesList = new ArrayList<VehicleList>();
        try {
            for (int i = 0; i < jsonVehicles.length(); i++) {
                VehicleList vehicles = new VehicleList();
                JSONObject jsonObject = jsonVehicles.getJSONObject(i);
                String date = jsonObject.getString("lastUpdate");
                vehicles.id = Integer.parseInt(jsonObject.get("id").toString());
                vehicles.name = jsonObject.getString("name");
                vehicles.category = jsonObject.getString("category");
                vehicles.uniqueId = jsonObject.getString("uniqueId");
                vehicles.lastUpdates = date(date);
                vehicles.status = jsonObject.getString("status");
                vehicles.positionId = jsonObject.getString("positionId");
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
        String[] datesplit = str.split("T");
        String date = datesplit[0];
        return date;
    }
}
