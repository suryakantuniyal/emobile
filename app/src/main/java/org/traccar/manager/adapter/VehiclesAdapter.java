package org.traccar.manager.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.traccar.manager.R;
import org.traccar.manager.activity.TrackingDevicesActivity;
import org.traccar.manager.activity.VehicleDetailActivity;
import org.traccar.manager.model.VehicleList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by silence12 on 13/6/17.
 */

public class VehiclesAdapter extends RecyclerView.Adapter<VehiclesAdapter.MyViewHolder> implements Filterable {


    private Context mContext;
    private List<VehicleList> vehicleLists = new ArrayList<>();
    private List<VehicleList> mFilteredList;


    public VehiclesAdapter(Context mContext, ArrayList<VehicleList> mVehicleList) {
        this.mContext = mContext;
        this.vehicleLists = mVehicleList;
        mFilteredList = mVehicleList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener  {

        private TextView name_tv,positionId_tv,lastupdated_tv,uniqueId_tv;
        private LinearLayout track_ll,detail_ll;
        private ImageView vehivle_iv,status_iv;
        private View v;
        public MyViewHolder(View itemView) {
            super(itemView);
            v = itemView;
            track_ll = (LinearLayout) itemView.findViewById(R.id.track_ll);
            detail_ll = (LinearLayout) itemView.findViewById(R.id.viewDetail_ll);
            name_tv = (TextView) itemView.findViewById(R.id.vehicle_name);
            lastupdated_tv = (TextView) itemView.findViewById(R.id.lastupdate_tv);
            vehivle_iv = (ImageView) itemView.findViewById(R.id.vehicle_image);
            status_iv = (ImageView) itemView.findViewById(R.id.status_iv);
            track_ll.setOnClickListener(this);
            detail_ll.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            Context context = itemView.getContext();
            switch (view.getId()){
                case  R.id.viewDetail_ll :
                  Intent intent = new Intent(context, VehicleDetailActivity.class);
                  intent.putExtra("id",mFilteredList.get(getPosition()).getId());
                  intent.putExtra("name",mFilteredList.get(getPosition()).getName());
                  intent.putExtra("pid",mFilteredList.get(getPosition()).getPositionId());
                  intent.putExtra("uid",mFilteredList.get(getPosition()).getUniqueId());
                  intent.putExtra("status",mFilteredList.get(getPosition()).getStatus());
                  intent.putExtra("category",mFilteredList.get(getPosition()).getCategory());
                  intent.putExtra("lastupdate",mFilteredList.get(getPosition()).getLastUpdates());
                  context.startActivity(intent);
                    break;
                case R.id.track_ll :
                    Intent trackIntent = new Intent(context, TrackingDevicesActivity.class);
                    trackIntent.putExtra("device_id",mFilteredList.get(getPosition()).getId());
                    trackIntent.putExtra("tname",mFilteredList.get(getPosition()).getName());
                    trackIntent.putExtra("tupdate",mFilteredList.get(getPosition()).getLastUpdates());
                    context.startActivity(trackIntent);
                    break;
            }
        }
    }

    @Override
    public VehiclesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.vehicle_listview, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;

    }

    @Override
    public void onBindViewHolder(VehiclesAdapter.MyViewHolder holder, int position) {

        VehicleList vehicle = mFilteredList.get(position);
        holder.name_tv.setText(vehicle.getName());
        holder.lastupdated_tv.setText(vehicle.getLastUpdates());
        if(vehicle.status.equals("online")){
            Glide.with(mContext).load(R.drawable.online_icon).asBitmap()
                    .centerCrop().placeholder(R.drawable.placeholderxx4).into(holder.status_iv);
        }else {
            Glide.with(mContext).load(R.drawable.offline_icon).asBitmap()
                    .centerCrop().placeholder(R.drawable.placeholderxx4).into(holder.status_iv);
        }

    }

    @Override
    public int getItemCount() {
        return mFilteredList.size();
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if(charString.isEmpty()){
                    mFilteredList = vehicleLists;
                }else {
                    List<VehicleList> filteredList = new ArrayList<>();
                    for(VehicleList projectsModel : vehicleLists){
                        if (projectsModel.getName().contains(charString)) {
                            filteredList.add(projectsModel);
                        }
                    }
                    mFilteredList = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mFilteredList = (List<VehicleList>) results.values;
                notifyDataSetChanged();
            }
        };
    }

}
