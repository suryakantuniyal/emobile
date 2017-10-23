package in.gtech.gogeotrack.activity.Reports;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import in.gtech.gogeotrack.R;
import in.gtech.gogeotrack.ReportsAdapter.TripReportAdapter;

public class TripsReportActiviy extends AppCompatActivity {
    TripReportAdapter tripReportAdapter;
    RecyclerView tripList_rv;
    private List<ReportData> tripReportList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips_report_activiy);

        tripList_rv = (RecyclerView)findViewById(R.id.tripReport);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.luncher_icon);
        setTitle("Trips Report");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tripReportList = new ArrayList<>();

        tripReportAdapter = new TripReportAdapter(getBaseContext(),tripReportList);
        tripList_rv.setLayoutManager(new LinearLayoutManager(this));
        tripList_rv.setAdapter(tripReportAdapter);
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
