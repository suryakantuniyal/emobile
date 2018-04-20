package com.innobins.innotrack.ReportsAdapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.innobins.innotrack.R;
import com.innobins.innotrack.activity.Reports.ReportData;

import java.util.List;

/**
 * Created by surya on 10/10/17.
 */

public class DailyRunsheetAdapter extends RecyclerView.Adapter<DailyRunsheetAdapter.ViewHolder>{
    private final Context context;
    private List<ReportData> mReportData;

    public DailyRunsheetAdapter(Context context, List<ReportData> mReportData) {
        this.context = context;
        this.mReportData = mReportData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.daily_runsheet,parent,false);
        DailyRunsheetAdapter.ViewHolder viewHolder = new DailyRunsheetAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        ReportData reportData = mReportData.get(position);

        holder.deviceeName_tv.setText(reportData.getDeviceName());
        holder.date_tv.setText(reportData.getTime());
        holder.maxSpeed_tv.setText(reportData.getMaximumSpeed().toString());
        holder.avgSpeed_tv.setText(reportData.getAverageSpeed().toString());
        holder.distance_tv.setText(reportData.getDistance().toString());
        holder.enginHr_tv.setText(String.valueOf(reportData.getEngineHours()));
        holder.spentFuel_tv.setText(reportData.getSpentFuel());
    }

    @Override
    public int getItemCount() {
        return mReportData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView deviceeName_tv,date_tv,avgSpeed_tv,maxSpeed_tv,distance_tv,enginHr_tv,spentFuel_tv;


        public ViewHolder(View itemView) {
            super(itemView);

            deviceeName_tv = (TextView)itemView.findViewById(R.id.runsht_DeviceName);
            date_tv = (TextView)itemView.findViewById(R.id.runsht_date_tv);
            avgSpeed_tv = (TextView)itemView.findViewById(R.id.runsht_avgSpd_tv);
            maxSpeed_tv = (TextView)itemView.findViewById(R.id.runsht_maxSpd_tv);
            distance_tv = (TextView)itemView.findViewById(R.id.runsht_distnce_tv);
            enginHr_tv = (TextView)itemView.findViewById(R.id.runsht_engnHr_tv);
            spentFuel_tv = (TextView)itemView.findViewById(R.id.runsht_spntFuel_tv);

        }
    }
}
