package in.gtech.gogeotrack.parser;

import android.util.Log;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import in.gtech.gogeotrack.model.VehicleList;

/**
 * Created by silence12 on 19/6/17.
 */

public final class TraccerParser {

    public static long msDiff;
    public static long daysDiff, minuteDiff, hoursDiff;

    public static ArrayList<VehicleList> parseGetVehiclesRequest(JSONArray jsonVehicles) {
        ArrayList<VehicleList> mVehiclesList = new ArrayList<VehicleList>();
        try {
            for (int i = 0; i < jsonVehicles.length(); i++) {
                VehicleList vehicles = new VehicleList();
                JSONObject jsonObject = jsonVehicles.getJSONObject(i);
                String date = jsonObject.getString("lastupdate");
                Log.d("trackerDt",String.valueOf(date));

                vehicles.id = Integer.parseInt(jsonObject.get("deviceId").toString());
                vehicles.name = jsonObject.getString("name");
                vehicles.category = jsonObject.getString("category");
                vehicles.uniqueId = jsonObject.getString("uniqueid");
                vehicles.lastUpdates = date(date);
                vehicles.time = datetime(date);
                vehicles.timeDiff = numDays(date);
                vehicles.status = jsonObject.getString("status");
                vehicles.positionId = jsonObject.getInt("positionid");
                mVehiclesList.add(vehicles);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mVehiclesList;

    }

    public static ArrayList<VehicleList> parseGeOnlinetVehiclesRequest(JSONArray jsonVehicles) {
        ArrayList<VehicleList> mVehiclesList = new ArrayList<VehicleList>();
        try {
            for (int i = 0; i < jsonVehicles.length(); i++) {
                VehicleList vehicles = new VehicleList();
                JSONObject jsonObject = jsonVehicles.getJSONObject(i);
                if (jsonObject.getString("status").equals("Online")) {
                    String date = jsonObject.getString("lastupdate");

                    vehicles.id = Integer.parseInt(jsonObject.get("id").toString());
                    vehicles.name = jsonObject.getString("name");
                    vehicles.category = jsonObject.getString("category");
                    vehicles.uniqueId = jsonObject.getString("uniqueid");
                    vehicles.lastUpdates = date(date);
                    vehicles.time = datetime(date);
                    vehicles.timeDiff = numDays(date);
                    vehicles.status = jsonObject.getString("status");
                    vehicles.positionId = jsonObject.getInt("positionid");
                    mVehiclesList.add(vehicles);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mVehiclesList;

    }


    public static ArrayList<VehicleList> parseGeOfflinetVehiclesRequest(JSONArray jsonVehicles) {
        ArrayList<VehicleList> mVehiclesList = new ArrayList<VehicleList>();
        try {
            for (int i = 0; i < jsonVehicles.length(); i++) {
                VehicleList vehicles = new VehicleList();
                JSONObject jsonObject = jsonVehicles.getJSONObject(i);
                if (jsonObject.getString("status").equals("Offline")) {
                    String date = jsonObject.getString("lastUpdate");

                    vehicles.id = Integer.parseInt(jsonObject.get("id").toString());
                    vehicles.name = jsonObject.getString("name");
                    vehicles.category = jsonObject.getString("category");
                    vehicles.uniqueId = jsonObject.getString("uniqueId");
                    vehicles.lastUpdates = date(date);
                    vehicles.time = datetime(date);
                    vehicles.timeDiff = numDays(date);
                    vehicles.status = jsonObject.getString("status");
                    vehicles.positionId = jsonObject.getInt("positionid");
                    mVehiclesList.add(vehicles);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mVehiclesList;

    }


    public static VehicleList getVehicleDetailById(JSONObject jsonObject) {

        VehicleList vehicleList = new VehicleList();

        try {
            vehicleList.address = jsonObject.getString("address");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return vehicleList;
    }

    public static String date(String str) {

        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date1 = date.parse(str);
            return date.format(date1);
        } catch (ParseException e) {
            e.printStackTrace();
            return "date";
        }

    }

       /*
        String st = str;
        Log.d("diff", String.valueOf(numDays(st)));
        String[] datesplit = str.split(" ");
        String date = datesplit[0];
        return date;*/
   // }

    public static String datetime(String str) {
       /* DateFormat dateFormat = new SimpleDateFormt("dd-MM-yyyy hh:mm:ss);
                String dateString = dateInputText + " " + timeInputText;
        Date date = dateFormat.parse(dateString);*/

        StringTokenizer token = new StringTokenizer(str);
        String date1 = token.nextToken();
        String time1 = token.nextToken();
       return time1;

       /* SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date1 = date.parse(str);
            return date.format(date1);
        } catch (ParseException e) {
            e.printStackTrace();
            return "date";
        }*/
    }
    /*    DateTimeFormatter parser = ISODateTimeFormat.dateTime();
        DateTime dt = parser.parseDateTime(str);
        DateTimeFormatter formatter = DateTimeFormat.mediumTime();
        Log.d("Dateformat",formatter.print(dt));
        String[] datesplit = str.split(" ");
        String date = datesplit[0];
        String time = datesplit[1];
        //String[] tim = date.split("T", 2);
        String result = formatter.print(dt);

//        if (tim.length > 1) {
//            result = tim[1];
//
//        } else
//            result = tim[0];
        return result;
    }*/

    public static DateTime getDate(String hh) {

        DateTime dateTime = DateTime.parse(hh);
        return dateTime;
    }
    public static String numDays(String str) {

        String time = null;
        long newMill = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            Date date = dateFormat.parse(str);
            newMill =date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        msDiff = Calendar.getInstance().getTimeInMillis() - newMill;
        minuteDiff = TimeUnit.MILLISECONDS.toMinutes(msDiff);
        hoursDiff = TimeUnit.MILLISECONDS.toHours(msDiff);
        daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff);

        if (daysDiff != 0) {
            if (daysDiff == 1)
                time = daysDiff + " Day";
            else
                time = daysDiff + " Days";
        } else if (hoursDiff != 0) {
            if (hoursDiff == 1)
                time = hoursDiff + " hour";
            else
                time = hoursDiff + " hours";
        } else {
            time = minuteDiff + " min";
        }

        return time;
    }

}
