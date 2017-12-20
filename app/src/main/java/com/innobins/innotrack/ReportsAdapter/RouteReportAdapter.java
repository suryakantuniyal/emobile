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

import in.innobins.innotrack.R;

/**
 * Created by surya on 10/10/17.
 */
public class RouteReportAdapter extends RecyclerView.Adapter<RouteReportAdapter.ViewHolder>{
    private final Context context;
    private List<ReportData> reportDatas = new ArrayList<>();
    private List<ReportData>mReortData;
    public RouteReportAdapter(Context context,List<ReportData>mReortData) {
        this.context = context;
        this.reportDatas = mReortData;
        this.mReortData = mReortData;
    }
    @Override
    public RouteReportAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_report,parent,false);
        RouteReportAdapter.ViewHolder viewHolder = new RouteReportAdapter.ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RouteReportAdapter.ViewHolder holder, int position) {

/*        ReportData reportData = mReortData.get(position);

        holder.deviceName_tv.setText(reportData.getDeviceName());
        holder.valid_tv.setText(reportData.getValid());
        holder.time_tv.setText(reportData.getTime());
        holder.speed_tv.setText(reportData.getSpeed().toString());
        holder.address_tv.setText(reportData.getAddress());
        holder.latitude_tv.setText(reportData.getLatitude().toString());
        holder.longitude_tv.setText(reportData.getLongitude().toString());
        holder.altitude_tv.setText(reportData.getAltitude().toString());*/
    }

    @Override
    public int getItemCount() {
        return mReortData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView deviceName_tv,valid_tv,time_tv,speed_tv,address_tv,latitude_tv,longitude_tv,altitude_tv;
        public ViewHolder(View itemView) {
            super(itemView);

            deviceName_tv = (TextView)itemView.findViewById(R.id.routeDeviceName);
            valid_tv = (TextView)itemView.findViewById(R.id.routeValid_tv);
            time_tv = (TextView)itemView.findViewById(R.id.routeTime_tv);
            speed_tv = (TextView)itemView.findViewById(R.id.routeSpeed_tv);
            address_tv = (TextView)itemView.findViewById(R.id.routeAddres_tv);
            latitude_tv = (TextView)itemView.findViewById(R.id.routeLatitude_tv);
            longitude_tv = (TextView)itemView.findViewById(R.id.routeLongitude_tv);
            altitude_tv = (TextView)itemView.findViewById(R.id.routeAltitude_tv);
        }
    }
}
