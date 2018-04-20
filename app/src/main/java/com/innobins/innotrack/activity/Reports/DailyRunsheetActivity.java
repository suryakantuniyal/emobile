package com.innobins.innotrack.activity.Reports;

import android.app.ProgressDialog;
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
import com.innobins.innotrack.ReportsAdapter.DailyRunsheetAdapter;
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

public class DailyRunsheetActivity extends BaseActivity {
    DailyRunsheetAdapter dailyRunsheetAdapter;
    RecyclerView runSheetList_rv;
    private List<ReportData> runSheetReportList;
    private List<String> genrateRep = new ArrayList<>();
    private ReportData reportData;
    SharedPreferences mSharedPreferences;
    ProgressDialog mProgressDialog;
    int divId;
    String startTime,endTime;
    ArrayList<Integer> selectedItem1 = new ArrayList<>();
    GoGeoDataProDialog goGeoDataProDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventes_report);

        runSheetList_rv = (RecyclerView)findViewById(R.id.dailyrunSheet_rv);
        runSheetReportList = new ArrayList<ReportData>();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.innotrack_icon);
        customTitle("   RunSheet Report");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        goGeoDataProDialog = new GoGeoDataProDialog(this);
     /*   mProgressDialog = new ProgressDialog(this);
        mProgressDialog.show();*/

        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
        //eventReportList = getReport();
        runSheetList_rv.setLayoutManager(new LinearLayoutManager(this));
        runSheetReort();
        //getReport();
       /* dailyRunsheetAdapter = new DailyRunsheetAdapter(getBaseContext(),eventReportList);
        evntList_rv.setAdapter(dailyRunsheetAdapter);*/

    }


 /*   private List<ReportData> getReport() {
        Intent getIntent = getIntent();
        String deviceName = getIntent.getStringExtra("deviceName");
        Log.d("dvceName",deviceName);
        JSONObject previousReport = null;
        try {
            previousReport = new JSONObject(mSharedPreferences.getString("reportData","{}"));
            Log.d("totalReportEvent",String.valueOf(previousReport));
            JSONArray eventReport = previousReport.getJSONArray("totalReport");
            for (int i =0;i<eventReport.length();i++){
                JSONObject jsonObject = eventReport.getJSONObject(i);
                reportData = new ReportData(deviceName,jsonObject.getString("type"),jsonObject.getString("time"),jsonObject.getInt("geofence"));
                Log.d("rportdata",String.valueOf(reportData));
                eventReportList.add(reportData);

                mProgressDialog.dismiss();
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
        return eventReportList;

    }*/

    private void runSheetReort() {
        goGeoDataProDialog.show();

       // mProgressDialog.setCancelable(false);
       /* mProgressDialog.show();
        mProgressDialog.setMessage("Fetching Data....");*/

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

        String mUrl = "https://mtrack-api.appspot.com/api/report/daily/runsheet/";
        final JSONObject jsonObject = new JSONObject();

        try{

            jsonObject.put("startDate",startTime);
            jsonObject.put("endDate",endTime);
            jsonObject.put("deviceLst",divId);
            /*jsonObject.put("reportType",reportType);
            jsonObject.put("dList",divId);//[divId]
            jsonObject.put("dName",deviceName);*/

            WebserviceHelper.getInstance().PostCall(DailyRunsheetActivity.this, mUrl, jsonObject, new ResponseCallback() {
                @Override
                public void OnResponse(JSONObject Response) {
                    if (Response!=null){
                        try {
                            JSONArray jsonArray = Response.getJSONArray("reportData");
                            Log.d("sumryreprt",String.valueOf(jsonArray));
                            for (int i= 0;i<jsonArray.length();i++){
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                //String deviceName,Double distance,Double averageSpeed,Double maximumSpeed,int engineHours,String spentFuel
                                reportData = new ReportData(jsonObject1.getString("deviceName"),jsonObject1.getString("date"),jsonObject1.getDouble("maxSpeed"),jsonObject1.getDouble("averageSpeed")
                                        ,jsonObject1.getDouble("distance"),jsonObject1.getInt("engineHours"),jsonObject1.getString("spentFuel") );

                                runSheetReportList.add(reportData);
                                dailyRunsheetAdapter = new DailyRunsheetAdapter(DailyRunsheetActivity.this,runSheetReportList);
                                runSheetList_rv.setAdapter(dailyRunsheetAdapter);
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


/*
        APIServices.getInstance().G(DailyRunsheetActivity.this, newUrl, new ResponseCallback() {
            @Override
            public void OnResponse(JSONObject Response) {

                Log.d("TAG",Response.toString());
                if (Response!=null){
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(String.valueOf(Response));
                        for (int i =0;i<jsonArray.length();i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String type = jsonObject.getString("type");
                            Toast.makeText(getBaseContext(),"value of type"+type,Toast.LENGTH_LONG).show();
                            Log.d("evntTypeVlaue",type);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();

                    }


                }


            }

        });
*/



       /* APIServices.GetReport(DailyRunsheetActivity.this, divId, startTime, endTime, new ReportResponseCallBack() {
            @Override
            public void onSuccessOnline(JSONArray result) {
                Log.d("evntResponse",String.valueOf(result));


                SessionHandler.reportHandler(getBaseContext(),result,mSharedPreferences);
            }
        });*/
//        mProgressDialog.dismiss();
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
