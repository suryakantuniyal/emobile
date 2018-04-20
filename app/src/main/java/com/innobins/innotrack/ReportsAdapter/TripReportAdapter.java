package com.innobins.innotrack.ReportsAdapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.innobins.innotrack.R;
import com.innobins.innotrack.activity.Reports.ReportData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by surya on 10/10/17.
 */

public class TripReportAdapter extends RecyclerView.Adapter<TripReportAdapter.ViewHolder> {
    private final Context context;
    private List<ReportData>reportDatas = new ArrayList<>();
    private List<ReportData>mReportData;

    public TripReportAdapter(Context context,List<ReportData>mReportData) {
        this.context = context;
        this.mReportData = mReportData;
       // this.mReportData = mReportData;
    }

    @Override
    public TripReportAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.trips_report,parent,false);
        TripReportAdapter.ViewHolder viewHolder = new TripReportAdapter.ViewHolder(view);
        return  viewHolder;

    }

    @Override
    public void onBindViewHolder(TripReportAdapter.ViewHolder holder, int position) {

        ReportData reportData = mReportData.get(position);

        holder.deviceName_tv.setText(reportData.getDeviceName());
        holder.startTime_tv.setText(reportData.getStartTime());
        holder.endTime_tv.setText(reportData.getEndTime().toString());
        holder.duration_tv.setText(String.valueOf(reportData.getDuration()));
        holder.startAddres_tv.setText(reportData.getStartAddress());
        holder.endAddres_tv.setText(reportData.getEndAddress());
        holder.spentFuel_tv.setText(reportData.getSpentFuel());
        holder.distance_tv.setText(reportData.getDistance().toString());
        holder.avgspeed_tv.setText(reportData.getAverageSpeed().toString());
        holder.maxSpeed_tv.setText(reportData.getMaximumSpeed().toString());

    }

    @Override
    public int getItemCount() {
        return mReportData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView deviceName_tv,startTime_tv,endTime_tv,duration_tv,startAddres_tv,endAddres_tv,spentFuel_tv,distance_tv,avgspeed_tv,maxSpeed_tv;
        public ViewHolder(View itemView) {
            super(itemView);

            deviceName_tv = (TextView)itemView.findViewById(R.id.tripDeviceName);
            startTime_tv = (TextView)itemView.findViewById(R.id.tripStrtTime);
            endTime_tv = (TextView)itemView.findViewById(R.id.tripEndTime);
            duration_tv = (TextView)itemView.findViewById(R.id.tripDuration);
            startAddres_tv = (TextView)itemView.findViewById(R.id.tripStartAddrs);
            endAddres_tv = (TextView)itemView.findViewById(R.id.tripEndAddrs);
            spentFuel_tv = (TextView)itemView.findViewById(R.id.tripSpntFuel);
            distance_tv = (TextView)itemView.findViewById(R.id.tripDistnce);
            avgspeed_tv = (TextView)itemView.findViewById(R.id.tripAvgSpeed);
            maxSpeed_tv = (TextView)itemView.findViewById(R.id.tripMaxSpeed);
        }
    }
}
