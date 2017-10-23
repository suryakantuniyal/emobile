package in.gtech.gogeotrack.activity.Reports;

import android.app.ProgressDialog;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import in.gtech.gogeotrack.R;
import in.gtech.gogeotrack.ReportsAdapter.EventReportAdapter;
import in.gtech.gogeotrack.api.APIServices;
import in.gtech.gogeotrack.network.ReportResponseCallBack;
import in.gtech.gogeotrack.utils.URLContstant;

public class EventesReportActivity extends AppCompatActivity {
    EventReportAdapter eventReportAdapter;
    RecyclerView evntList_rv;
    private List<ReportData> eventReportList;
    private List<String> genrateRep = new ArrayList<>();
    private ReportData reportData;
    SharedPreferences mSharedPreferences;
    ProgressDialog mProgressDialog;
    int divId;
    String startTime,endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventes_report);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.luncher_icon);
        setTitle("Event Report");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mProgressDialog = new ProgressDialog(this);

        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);

        eventReportList = new ArrayList<ReportData>();
      //  eventReportList = getReport();
        String s;

        eventReport();


       /* genrateRep.add("DLX8189");
        genrateRep.add("NO TYPE");
        genrateRep.add("5:59:39");
        genrateRep.add("SEARCHING..");
*/
        //reportData = new ReportData(genrateRep.get(0),genrateRep.get(1),genrateRep.get(2),genrateRep.get(3));
       // eventReportList.add(reportData);

        evntList_rv = (RecyclerView)findViewById(R.id.eventReport);

        eventReportAdapter = new EventReportAdapter(getBaseContext(),eventReportList);

        evntList_rv.setLayoutManager(new LinearLayoutManager(this));

        evntList_rv.setAdapter(eventReportAdapter);

    }

    private List<ReportData> getReport() {
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

    }

    private void eventReport() {

        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        mProgressDialog.setMessage("Fetching Data....");

        Intent getIntent = getIntent();
        divId = getIntent.getIntExtra("divReport",-1);
        final String deviceName = getIntent.getStringExtra("deviceName");
        String time1 = getIntent.getStringExtra("startTime");
        String time2 = getIntent.getStringExtra("endTime");
        String date1 = getIntent.getStringExtra("startdate");
        String date2 = getIntent.getStringExtra("endDate");
        startTime = date1+"T"+time1;
        endTime   = date2+"T"+time2;


        Log.d("eventTime",startTime);
        Log.d("evntId",String.valueOf(divId));

        final String newUrl = URLContstant.EVENT_REPORT + "?" + "deviceId=" + divId + "&from=" + startTime + "&to=" +endTime;
        APIServices.GetReport(EventesReportActivity.this, newUrl, new ReportResponseCallBack() {
            @Override
            public void onGetReport(JSONArray result) {
                Log.d("eventReportNEw",String.valueOf(result));

                if (result!= null){
                    try {
                        for (int i = 0;i<result.length();i++){
                            JSONObject jsonObject = result.getJSONObject(i);
                            String type = result.getJSONObject(i).getString("type");
                            Log.d("evntReport",type);
                            reportData = new ReportData(deviceName,jsonObject.getString("type"),jsonObject.getString("time"),jsonObject.getInt("geofence"));
                            eventReportList.add(reportData);
                        }

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        });

/*
        APIServices.getInstance().G(EventesReportActivity.this, newUrl, new ResponseCallback() {
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



       /* APIServices.GetReport(EventesReportActivity.this, divId, startTime, endTime, new ReportResponseCallBack() {
            @Override
            public void onSuccessOnline(JSONArray result) {
                Log.d("evntResponse",String.valueOf(result));


                SessionHandler.reportHandler(getBaseContext(),result,mSharedPreferences);
            }
        });*/
        mProgressDialog.dismiss();
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
