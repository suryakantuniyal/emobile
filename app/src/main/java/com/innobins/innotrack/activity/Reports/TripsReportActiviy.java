package com.innobins.innotrack.activity.Reports;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.innobins.innotrack.R;
import com.innobins.innotrack.ReportsAdapter.TripReportAdapter;
import com.innobins.innotrack.activity.GoGeoDataProDialog;
import com.innobins.innotrack.home.BaseActivity;
import com.innobins.innotrack.network.ResponseCallback;
import com.innobins.innotrack.network.WebserviceHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TripsReportActiviy extends BaseActivity {
    TripReportAdapter tripReportAdapter;
    RecyclerView tripList_rv;
    private List<ReportData> tripReportList;
    ReportData reportData;
    int divId;
    String startTime,endTime;
    GoGeoDataProDialog goGeoDataProDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips_report_activiy);

        tripList_rv = (RecyclerView)findViewById(R.id.tripReport);
        tripReportList = new ArrayList<>();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.innotrack_icon);
        customTitle("   "+"Trips Report");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        goGeoDataProDialog = new GoGeoDataProDialog(this);
        tripList_rv.setLayoutManager(new LinearLayoutManager(this));

        tripReport();

    }
    private void tripReport() {
        goGeoDataProDialog.show();
        Intent getIntent = getIntent();
       // divId = getIntent.getIntExtra("divReport",-1);
        String divId = getIntent.getStringExtra("divReport");
        final String deviceName = getIntent.getStringExtra("deviceName");
        String reportType = getIntent.getStringExtra("reportType");
        String time1 = getIntent.getStringExtra("startTime");
        String time2 = getIntent.getStringExtra("endTime");
        String date1 = getIntent.getStringExtra("startdate");
        String date2 = getIntent.getStringExtra("endDate");
        startTime = date1+" "+time1;
        endTime   = date2+" "+time2;
        Log.d("starttme", startTime);

        String mUrl = "https://mtrack-api.appspot.com/api/report/trips/";
        final JSONObject jsonObject = new JSONObject();
        try{

            jsonObject.put("startDate",startTime);
            jsonObject.put("endDate",endTime);
            jsonObject.put("deviceLst",divId);
            /*jsonObject.put("reportType",reportType);
            jsonObject.put("dList",divId);//[divId]
            jsonObject.put("dName",deviceName);*/

            WebserviceHelper.getInstance().PostCall(TripsReportActiviy.this, mUrl, jsonObject, new ResponseCallback() {
                @Override
                public void OnResponse(JSONObject Response) {
                    if (Response!=null){
                        try {
                            JSONArray jsonArray = Response.getJSONArray("reportData");
                            for (int i= 0;i<jsonArray.length();i++){
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                                reportData = new ReportData(jsonObject1.getString("deviceName"),jsonObject1.getString("startTime"),jsonObject1.getString("endTime"),jsonObject1.getInt("duration"),
                                        jsonObject1.getString("startAddress"),jsonObject1.getString("endAddress"),jsonObject1.getString("spentFuel"),jsonObject1.getDouble("distance"),jsonObject1.getDouble("averageSpeed"),
                                        jsonObject1.getDouble("maxSpeed"));

                                tripReportList.add(reportData);
                                tripReportAdapter = new TripReportAdapter(TripsReportActiviy.this,tripReportList);
                                tripList_rv.setAdapter(tripReportAdapter);
                                goGeoDataProDialog.dismiss();
                            }
                            Log.d("trprespns",String.valueOf(jsonArray));


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        }catch (JSONException e){
            e.printStackTrace();
        }

       /* final String newUrl = URLContstant.TRIP_REPORT + "?" + "&deviceLst=" + divId + "&startDate=" + startTime + "&endDate=" +endTime +
        "&reportType=" + reportType +"&dName="+deviceName + "&dList=" + deviceName;

        Log.d("tripurl",newUrl);*/
/*
        APIServices.GetReport(TripsReportActiviy.this, newUrl, new ReportResponseCallBack() {
            @Override
            public void onGetReport(JSONArray result) {
                Log.d("TrPReport",String.valueOf(result));

                if (result!=null){
                    try{
                        for (int i=0;i<result.length();i++){

                            JSONObject jsonObject = result.getJSONObject(i);
                            reportData = new ReportData(jsonObject.getString("deviceName"),jsonObject.getString("startTime"),jsonObject.getString("endTime")
                            ,jsonObject.getInt("duration"),jsonObject.getString("startAddress"),jsonObject.getString("endAddress"),jsonObject.getString("spentFuel")
                            ,jsonObject.getDouble("distance"),jsonObject.getDouble("averageSpeed"),jsonObject.getDouble("maxSpeed"));

                            tripReportList.add(reportData);
                            tripReportAdapter = new TripReportAdapter(getBaseContext(),tripReportList);
                            tripList_rv.setAdapter(tripReportAdapter);

                        }

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }

            }
        });
*/


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
