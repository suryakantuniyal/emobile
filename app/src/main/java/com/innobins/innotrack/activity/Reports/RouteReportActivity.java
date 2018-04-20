package com.innobins.innotrack.activity.Reports;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.innobins.innotrack.R;
import com.innobins.innotrack.ReportsAdapter.RouteReportAdapter;
import com.innobins.innotrack.activity.GoGeoDataProDialog;
import com.innobins.innotrack.home.BaseActivity;
import com.innobins.innotrack.network.ResponseCallback;
import com.innobins.innotrack.network.WebserviceHelper;
import com.innobins.innotrack.utils.URLContstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RouteReportActivity extends BaseActivity {

    RouteReportAdapter routeReportAdapter;
    RecyclerView viewList_rc;
    ReportData reportData;
    private List<ReportData> routereportList;
    SharedPreferences mSharedPreferences;
    int divId;
    String startTime,endTime;
    GoGeoDataProDialog goGeoDataProDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_report);
        routereportList = new ArrayList<ReportData>();
        viewList_rc = (RecyclerView)findViewById(R.id.routeReport);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.innotrack_icon);
        customTitle("   "+"Route Report");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        goGeoDataProDialog = new GoGeoDataProDialog(this);

        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
        viewList_rc.setLayoutManager(new LinearLayoutManager(this));
        allReport();
    }

    private void allReport() {
        goGeoDataProDialog.show();
        Intent getIntent = getIntent();
        // divId = getIntent.getIntExtra("divReport",-1);
        String newName = null;
        String divId = getIntent.getStringExtra("divReport");
        Log.d("divid",divId);
        String deviceName = getIntent.getStringExtra("deviceName");
        String reportType = getIntent.getStringExtra("reportType");
        String time1 = getIntent.getStringExtra("startTime");
        String time2 = getIntent.getStringExtra("endTime");
        String date1 = getIntent.getStringExtra("startdate");
        String date2 = getIntent.getStringExtra("endDate");
        startTime = date1+" "+time1;
        endTime   = date2+" "+time2;
       Log.d("starttme", deviceName);
        if (deviceName.endsWith(",")) {
             newName = deviceName.substring(0, deviceName.length() - 1);
        }

        String mUrl = "https://mtrack-api.appspot.com/api/report/view/";
        final JSONObject jsonObject = new JSONObject();
        try{

            jsonObject.put("startDate",startTime);
            jsonObject.put("endDate",endTime);
            jsonObject.put("deviceLst",divId);
            jsonObject.put("reportType",reportType);
            /*jsonObject.put("reportType",reportType);
            jsonObject.put("dList",divId);//[divId]
            jsonObject.put("dName",deviceName);*/

            final String device_Name = newName;
            Log.d("finaldevce",device_Name);
            WebserviceHelper.getInstance().PostCall(RouteReportActivity.this, mUrl, jsonObject, new ResponseCallback() {
                @Override
                public void OnResponse(JSONObject Response) {
                    if (Response!=null){
                        try {
                            JSONArray jsonArray = Response.getJSONArray("reportData");
                            Log.d("routejson",String.valueOf(jsonArray));
                            for (int i= 0;i<jsonArray.length();i++){
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                                reportData = new ReportData(device_Name,jsonObject1.getString("valid"),jsonObject1.getString("servertime"),jsonObject1.getDouble("latitude"),
                                        jsonObject1.getDouble("longitude"),jsonObject1.getDouble("altitude"),jsonObject1.getString("speed"),jsonObject1.getString("address"));

                                routereportList.add(reportData);
                                routeReportAdapter = new RouteReportAdapter(RouteReportActivity.this,routereportList);
                                viewList_rc.setAdapter(routeReportAdapter);
                                goGeoDataProDialog.dismiss();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        }catch (JSONException e){
            e.printStackTrace();
        }

       /* APIServices.GetReport(RouteReportActivity.this, divId, startTime, endTime, new ReportResponseCallBack() {
            @Override
            public void OnResponse(JSONArray result) {
                Log.d("Result", String.valueOf(result));
                Toast.makeText(getBaseContext(),"reportAPI",Toast.LENGTH_LONG).show();
            }
        });*/
    }

/*    private List<ReportData> getRouteReport() {

        JSONObject previousReport = null;
        try {
            previousReport = new JSONObject(mSharedPreferences.getString("reportData","{}"));
            Log.d("reportPart",String.valueOf(previousReport));
            JSONArray reportlist = previousReport.getJSONArray("totalreport");
            for (int i = 0;i<reportlist.length();i++){
                JSONObject jsonObject = reportlist.getJSONObject(i);

                ReportData reportData1 = new ReportData(jsonObject.getString("type"),jsonObject.getString("time"),jsonObject.getString("deviceName"),jsonObject.getString("geofence"));
                routereportList.add(reportData1);
            }
            return routereportList;

        }catch (JSONException e){
            e.printStackTrace();
        }
        return routereportList;

    }*/

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
