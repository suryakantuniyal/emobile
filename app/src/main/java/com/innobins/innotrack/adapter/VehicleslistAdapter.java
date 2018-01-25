package com.innobins.innotrack.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.innobins.innotrack.model.VehicleList;

import java.util.ArrayList;
import java.util.List;

import com.innobins.innotrack.R;

/**
 * Created by silence12 on 28/6/17.
 */

public class VehicleslistAdapter extends RecyclerView.Adapter<VehicleslistAdapter.MyViewHolder> implements Filterable {


    OnItemClickListener mOnItemClickListener;
    private Context mContext;
    private List<VehicleList> vehicleLists = new ArrayList<>();
    private List<VehicleList> mFilteredList;
    private OnItemClickListener cnoteClick;


    public VehicleslistAdapter(Context mContext, List<VehicleList> mVehicleList, OnItemClickListener cnoteClick) {
        this.mContext = mContext;
        this.vehicleLists = mVehicleList;
        mFilteredList = mVehicleList;
        this.cnoteClick = cnoteClick;
    }


    @Override
    public VehicleslistAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.vehicle_listview, parent, false);
        VehicleslistAdapter.MyViewHolder vh = new VehicleslistAdapter.MyViewHolder(v);
        return vh;

    }

    @Override
    public void onBindViewHolder(VehicleslistAdapter.MyViewHolder holder, int position) {

        VehicleList vehicle = mFilteredList.get(position);
        holder.name_tv.setText(vehicle.getName());
       // holder.lastupdated_tv.setText(vehicle.getLastUpdates());
        holder.lastupdated_tv.setText(vehicle.getTime());
        holder.timediff_tv.setText(vehicle.getTimeDiff());

        if (vehicle.category.equals("person")){
            holder.vehicleLstIcn.setImageResource(R.drawable.ic_punch_person);
        }
        if (vehicle.category.equals("motorcycle")){
            holder.vehicleLstIcn.setImageResource(R.drawable.motobikes);
        }
            else {
            holder.vehicleLstIcn.setImageResource(R.drawable.truckicon2);
        }

        if (vehicle.address.equals("null")) {
            holder.positionId_tv.setText("Loading...");
        } else {
            holder.positionId_tv.setText(vehicle.address);
        }
        if (vehicle.status.equals("Online")) {

            holder.online_tv.setText("Online");
            Glide.with(mContext).load(R.drawable.online_icon).asBitmap()
                    .centerCrop().placeholder(R.drawable.placeholderxx4).into(holder.status_iv);
        } else {
            holder.online_tv.setText("Offline");
            Glide.with(mContext).load(R.drawable.offline_icon).asBitmap()
                    .centerCrop().placeholder(R.drawable.placeholderxx4).into(holder.status_iv);
        }

    }

    public void setOnItemClickListener(final OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @Override
    public int getItemCount() {
        return mFilteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new MyFilter();
//            @Override
//            protected FilterResults performFiltering(CharSequence constraint) {
//                String charString = constraint.toString();
//                FilterResults filterResults = new FilterResults();
//                if (charString.isEmpty()) {
//                    mFilteredList = vehicleLists;
//                } else {
//                    List<VehicleList> filteredList = new ArrayList<>();
//                    for (VehicleList projectsModel : vehicleLists) {
//                        if (projectsModel.getName().contains(charString) || projectsModel.getStatus().contains(charString)) {
//                            filteredList.add(projectsModel);
//                        }
//                    }
//                    mFilteredList = filteredList;
//                }
//                filterResults.values = mFilteredList;
//                return filterResults;
//            }
//
//            @Override
//            protected void publishResults(CharSequence constraint, FilterResults results) {
//                mFilteredList = (List<VehicleList>) results.values;
//                notifyDataSetChanged();
//            }
//        };
    }


    private class MyFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String charString = constraint.toString();
            FilterResults filterResults = new FilterResults();
            if (charString.isEmpty()) {
                mFilteredList = vehicleLists;
            } else {
                List<VehicleList> filteredList = new ArrayList<>();
                for (VehicleList projectsModel : vehicleLists) {
                    if (projectsModel.getName().toLowerCase().contains(charString.toLowerCase()) || projectsModel.getStatus().contains(charString)) {
                        filteredList.add(projectsModel);
                    }
                }
                mFilteredList = filteredList;
            }
            filterResults.values = mFilteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredList = (List<VehicleList>) results.values;
            notifyDataSetChanged();
        }
    }

    public interface OnItemClickListener {
        public void OnItemClick(View view, int position, List<VehicleList> mFilteredList);

    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnFocusChangeListener {

        private TextView name_tv, positionId_tv, lastupdated_tv, timediff_tv, online_tv;
        private ImageView  status_iv,vehicleLstIcn;
        private LinearLayout detail, track;
        private View v;

        public MyViewHolder(View itemView) {
            super(itemView);
            v = itemView;

            track = (LinearLayout) itemView.findViewById(R.id.track_ll);
            detail = (LinearLayout) itemView.findViewById(R.id.detail_ll);
            vehicleLstIcn = (ImageView)itemView.findViewById(R.id.vehicleListIcn);
            name_tv = (TextView) itemView.findViewById(R.id.vehicle_name);
            lastupdated_tv = (TextView) itemView.findViewById(R.id.lastupdate_tv);
            status_iv = (ImageView) itemView.findViewById(R.id.status_iv);
            positionId_tv = (TextView) itemView.findViewById(R.id.address_tv);
            timediff_tv = (TextView) itemView.findViewById(R.id.timediff_tv);
            online_tv = (TextView) itemView.findViewById(R.id.onlinet_tv);
            track.setOnClickListener(this);
            detail.setOnClickListener(this);

            track.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    cnoteClick.OnItemClick(view, getAdapterPosition(), mFilteredList);
                }
            });


            detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cnoteClick.OnItemClick(view, getAdapterPosition(),mFilteredList);
                }
            });

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.OnItemClick(v, getAdapterPosition(), mFilteredList);

            } else {
                Log.d("itemclick", "OnItemClickListener is null");
            }
        }

        @Override
        public void onFocusChange(View view, boolean b) {
            detail.requestFocus();
        }
    }


}
