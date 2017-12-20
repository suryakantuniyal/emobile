package in.gtech.gogeotrack.activity.Reports;

import android.content.Intent;
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
import in.gtech.gogeotrack.ReportsAdapter.TripReportAdapter;
import in.gtech.gogeotrack.api.APIServices;
import in.gtech.gogeotrack.network.ReportResponseCallBack;
import in.gtech.gogeotrack.utils.URLContstant;

public class TripsReportActiviy extends AppCompatActivity {
    TripReportAdapter tripReportAdapter;
    RecyclerView tripList_rv;
    private List<ReportData> tripReportList;
    ReportData reportData;
    int divId;
    String startTime,endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips_report_activiy);

        tripList_rv = (RecyclerView)findViewById(R.id.tripReport);
        tripReportList = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.luncher_icon);
        setTitle("Trips Report");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        tripList_rv.setLayoutManager(new LinearLayoutManager(this));
        tripReport();


    }

    private void tripReport() {
        Intent getIntent = getIntent();
        divId = getIntent.getIntExtra("divReport",-1);
        final String deviceName = getIntent.getStringExtra("deviceName");
        String time1 = getIntent.getStringExtra("startTime");
        String time2 = getIntent.getStringExtra("endTime");
        String date1 = getIntent.getStringExtra("startdate");
        String date2 = getIntent.getStringExtra("endDate");
        startTime = date1+"T"+time1;
        endTime   = date2+"T"+time2;

        final String newUrl = URLContstant.TRIP_REPORT + "?" + "deviceId=" + divId + "&from=" + startTime + "&to=" +endTime;
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
