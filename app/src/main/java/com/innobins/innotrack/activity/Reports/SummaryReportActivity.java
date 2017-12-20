package com.innobins.innotrack.activity.Reports;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.innobins.innotrack.network.ReportResponseCallBack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import in.innobins.innotrack.R;
import com.innobins.innotrack.ReportsAdapter.SummaryReportAdapter;
import com.innobins.innotrack.api.APIServices;
import com.innobins.innotrack.utils.URLContstant;

public class SummaryReportActivity extends AppCompatActivity {
    RecyclerView summaryList_rv;
    SummaryReportAdapter summaryReportAdapter;
    private List<ReportData>sumryReportList;
    private ReportData reportData;
    SharedPreferences mSharedPreferences;
    int divId;
    String startTime,endTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_report);

        summaryList_rv = (RecyclerView)findViewById(R.id.summaryReport);
        sumryReportList = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.luncher_icon);
        setTitle("Summary Report");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
       // sumryReportList = getSummaryReport();
        summaryList_rv.setLayoutManager(new LinearLayoutManager(this));
        summaryReport();

    }

    private void summaryReport() {

        Intent getIntent = getIntent();
        divId = getIntent.getIntExtra("divReport",-1);
        String deviceName = getIntent.getStringExtra("deviceName");
        String time1 = getIntent.getStringExtra("startTime");
        String time2 = getIntent.getStringExtra("endTime");
        String date1 = getIntent.getStringExtra("startdate");
        String date2 = getIntent.getStringExtra("endDate");
        startTime = date1+"T"+time1;
        endTime   = date2+"T"+time2;

        final String newUrl = URLContstant.SUMMARY_REPORT + "?" + "deviceId=" + divId + "&from=" + startTime + "&to=" +endTime;
        APIServices.GetReport(SummaryReportActivity.this, newUrl, new ReportResponseCallBack() {
            @Override
            public void onGetReport(JSONArray result) {
                Log.d("summryReport",String.valueOf(result));

                if (result!=null){
                    try {
                        for (int i=0;i<result.length();i++){
                            JSONObject jsonObject = result.getJSONObject(i);
                            reportData = new ReportData(jsonObject.getString("deviceName"),jsonObject.getDouble("distance"),jsonObject.getDouble("averageSpeed"),jsonObject.getDouble("maxSpeed"),jsonObject.getInt("engineHours"));

                            sumryReportList.add(reportData);
                            summaryReportAdapter = new SummaryReportAdapter(getBaseContext(),sumryReportList);
                            summaryList_rv.setAdapter(summaryReportAdapter);
                        }

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }

            }
        });

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
    }


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
