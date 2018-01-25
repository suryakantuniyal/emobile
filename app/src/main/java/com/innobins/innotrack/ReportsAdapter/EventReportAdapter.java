package com.innobins.innotrack.ReportsAdapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.innobins.innotrack.activity.Reports.ReportData;

import java.util.ArrayList;
import java.util.List;

import com.innobins.innotrack.R;

/**
 * Created by surya on 10/10/17.
 */

public class EventReportAdapter extends RecyclerView.Adapter<EventReportAdapter.ViewHolder>{
    private final Context context;
    private List<ReportData> reportDatas = new ArrayList<>();
    private List<ReportData> mReportData;

    public EventReportAdapter(Context context,List<ReportData> mReportData) {
        this.context = context;
       this.reportDatas = mReportData;
       this.mReportData = mReportData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_report,parent,false);
        EventReportAdapter.ViewHolder viewHolder = new EventReportAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        ReportData reportData = mReportData.get(position);

        holder.deviceeName_tv.setText(reportData.getDeviceName());
        holder.type_tv.setText(reportData.getType());
        holder.time_tv.setText(reportData.getTime());
       // holder.geofence_tv.setText(reportData.getGeo());
        holder.geofence_tv.setText(String.valueOf(reportData.getGeofence()));
//        holder.geofence_tv.setText(reportData.getGeofence());

    }

    @Override
    public int getItemCount() {
        return mReportData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView deviceeName_tv,type_tv,time_tv,geofence_tv;


        public ViewHolder(View itemView) {
            super(itemView);

            deviceeName_tv = (TextView)itemView.findViewById(R.id.eventDevice_name);
            type_tv = (TextView)itemView.findViewById(R.id.eventType_tv);
            time_tv = (TextView)itemView.findViewById(R.id.eventTime_tv);
            geofence_tv = (TextView)itemView.findViewById(R.id.eventGeofence_tv);
        }
    }
}
