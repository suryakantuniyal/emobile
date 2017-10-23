package in.gtech.gogeotrack.activity.Reports;

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

import java.util.ArrayList;
import java.util.List;

import in.gtech.gogeotrack.R;
import in.gtech.gogeotrack.ReportsAdapter.RouteReportAdapter;
import in.gtech.gogeotrack.utils.URLContstant;

public class RouteReportActivity extends AppCompatActivity {

    RouteReportAdapter routeReportAdapter;
    RecyclerView viewList_rc;
    private List<ReportData> routereportList;
    SharedPreferences mSharedPreferences;
    int divId;
    String startTime,endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_report);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.luncher_icon);
        setTitle("Route Report");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);

        allReport();


        viewList_rc = (RecyclerView)findViewById(R.id.routeReport);
        routereportList = new ArrayList<ReportData>();
       // routereportList = getRouteReport();

        routeReportAdapter = new RouteReportAdapter(getBaseContext(),routereportList);
        viewList_rc.setLayoutManager(new LinearLayoutManager(this));
        viewList_rc.setAdapter(routeReportAdapter);


        /*generateRptIntnt.putExtra("deviceName",vhcleNoPopup_str);
        generateRptIntnt.putExtra("startdate",startDate_str);
        generateRptIntnt.putExtra("endDate",endDate_str);
        generateRptIntnt.putExtra("startTime",startTime_str);
        generateRptIntnt.putExtra("endTime",endTime_str);*/
    }

    private void allReport() {
        Intent getIntent = getIntent();
        divId = getIntent.getIntExtra("divReport",-1);
        String deviceName = getIntent.getStringExtra("deviceName");
        String time1 = getIntent.getStringExtra("startTime");
        String time2 = getIntent.getStringExtra("endTime");
        String date1 = getIntent.getStringExtra("startdate");
        String date2 = getIntent.getStringExtra("endDate");
        startTime = date1+"T"+time1;
        endTime   = date2+"T"+time2;

        Log.d("starttime",startTime);
        Log.d("divicenaya",String.valueOf(divId));

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
