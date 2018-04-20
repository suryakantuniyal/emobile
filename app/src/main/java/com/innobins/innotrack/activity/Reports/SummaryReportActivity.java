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
import com.innobins.innotrack.ReportsAdapter.SummaryReportAdapter;
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

public class SummaryReportActivity extends BaseActivity {
    RecyclerView summaryList_rv;
    SummaryReportAdapter summaryReportAdapter;
    private List<ReportData>sumryReportList;
    private ReportData reportData;
    SharedPreferences mSharedPreferences;
    int divId;
    String startTime,endTime;
    GoGeoDataProDialog goGeoDataProDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_report);

        summaryList_rv = (RecyclerView)findViewById(R.id.summaryReport);
        sumryReportList = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.innotrack_icon);
        customTitle("   "+"Summery Report");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        goGeoDataProDialog = new GoGeoDataProDialog(this);

        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
       // sumryReportList = getSummaryReport();
        summaryList_rv.setLayoutManager(new LinearLayoutManager(this));
        summaryReport();

    }

    private void summaryReport() {
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
        Log.d("starttme", String.valueOf(divId));

        String mUrl = "https://mtrack-api.appspot.com/api/report/summary/";
        final JSONObject jsonObject = new JSONObject();
        try{

            jsonObject.put("startDate",startTime);
            jsonObject.put("endDate",endTime);
            jsonObject.put("deviceLst",divId);
            /*jsonObject.put("reportType",reportType);
            jsonObject.put("dList",divId);//[divId]
            jsonObject.put("dName",deviceName);*/

            WebserviceHelper.getInstance().PostCall(SummaryReportActivity.this, mUrl, jsonObject, new ResponseCallback() {
                @Override
                public void OnResponse(JSONObject Response) {
                    if (Response!=null){
                        try {
                            JSONArray jsonArray = Response.getJSONArray("reportData");
                            Log.d("sumryreprt",String.valueOf(jsonArray));
                            for (int i= 0;i<jsonArray.length();i++){
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                              //String deviceName,Double distance,Double averageSpeed,Double maximumSpeed,int engineHours,String spentFuel
                                reportData = new ReportData(jsonObject1.getString("deviceName"),jsonObject1.getDouble("distance"),jsonObject1.getDouble("averageSpeed")
                                        ,jsonObject1.getDouble("maxSpeed"),jsonObject1.getInt("engineHours"),jsonObject1.getString("spentFuel") );

                                sumryReportList.add(reportData);
                                summaryReportAdapter = new SummaryReportAdapter(SummaryReportActivity.this,sumryReportList);
                                summaryList_rv.setAdapter(summaryReportAdapter);
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
    }


        /*try {
            jsonObject.put("deviceId",divId);
            jsonObject.put("&from",startTime);
            jsonObject.put("&to",endTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

/*
        APIServices.getInstance().PostCall(SummaryReportActivity.this, "http://35.189.74.0/api/reports/summary", jsonObject, new ResponseCallback() {
            @Override
            public void OnResponse(JSONObject Response) {
                if (Response!=null){
                    try {
                        JSONArray sumryReport = Response.getJSONArray(String.valueOf(Response));
                        Log.d("newsummryReport", String.valueOf(sumryReport));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
*/

        /*APIServices.GetSummaryReport(SummaryReportActivity.this, divId, startTime, endTime, new ReportResponseCallBack() {
            @Override
            public void onSuccessOnline(JSONArray result) {
                Log.d("sumryResponse",String.valueOf(result));
                SessionHandler.reportHandler(getBaseContext(),result,mSharedPreferences);
            }
        });*/



    private List<ReportData> getSummaryReport() {

        JSONObject sumryPrevious = null;
        try{
            sumryPrevious = new JSONObject(mSharedPreferences.getString("summaryData","{}"));
            Log.d("sumaryReport",String.valueOf(sumryPrevious));
            JSONArray sumryReport = sumryPrevious.getJSONArray("summaryReport");

            for (int i =0;i<sumryReport.length();i++){
                JSONObject jsonObject = sumryReport.getJSONObject(i);

            }

        }catch (JSONException e){
            e.printStackTrace();
        }

        return sumryReportList;
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
