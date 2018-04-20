package com.innobins.innotrack.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.innobins.innotrack.R;
import com.innobins.innotrack.adapter.VehicleslistAdapter;
import com.innobins.innotrack.home.BaseActivity;
import com.innobins.innotrack.model.VehicleList;
import com.innobins.innotrack.network.ResponseCallback;
import com.innobins.innotrack.network.WebserviceHelper;
import com.innobins.innotrack.services.UpdateListViewService;
import com.innobins.innotrack.utils.URLContstant;
import com.innobins.innotrack.vehicleonmap.VehicleOnMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by silence12 on 5/7/17.
 */

public class OnLineOffLineActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, VehicleslistAdapter.OnItemClickListener {
    public static MenuItem searchMenuItem;
    private static ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private VehicleslistAdapter vehiclesAdapter;
    private RecyclerView recyclerView;
    private List<VehicleList> listArrayList;
    SharedPreferences mSharedPreferences;
    private LinearLayout no_data_ll;
    public static OnLineOffLineActivity onLineInstance;
    PendingIntent pintent;
    AlarmManager alarm;
    Intent updateListViewService;
    String userName,password;
    RelativeLayout coordinatorLayout ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onlineoffline);
        onLineInstance = this;
        updateListViewService = new Intent(getBaseContext(), UpdateListViewService.class);
        startService(updateListViewService);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.innotrack_icon);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
        userName = mSharedPreferences.getString(URLContstant.KEY_USERNAME, "");
        password = mSharedPreferences.getString(URLContstant.KEY_PASSWORD,"");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Wait a moment...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        customTitle(" "+"Online Devices");
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh_online);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(this);
        no_data_ll = (LinearLayout) findViewById(R.id.no_vehicle_ll);
        listArrayList = new ArrayList<VehicleList>();
        recyclerView = (RecyclerView) findViewById(R.id.onlineoffline_rv);
        listArrayList = parseView();
        if(listArrayList.size() == 0){
            progressDialog.dismiss();
            no_data_ll.setVisibility(View.VISIBLE);
        }else {
            no_data_ll.setVisibility(View.GONE);
        }
        vehiclesAdapter = new VehicleslistAdapter(getBaseContext(),listArrayList,this);
        coordinatorLayout = (RelativeLayout)findViewById(R.id.main_rl);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(vehiclesAdapter);

        vehiclesAdapter.setOnItemClickListener(this);

        /*-----------------------Call Service----------------------------*/
        pintent = PendingIntent.getService(OnLineOffLineActivity.this, 0, updateListViewService,0);
        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar cal = Calendar.getInstance();
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 12 * 1000, pintent);

    }
    public void uploadNewData() {

        listArrayList.clear();
        String mUrl = "https://mtrack-api.appspot.com/api/get/devices/byuser/" ;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userid",mSharedPreferences.getInt(URLContstant.KEY_LOGEDIN_USERID,-1));
            WebserviceHelper.getInstance().PostCall(OnLineOffLineActivity.this, mUrl, jsonObject, new ResponseCallback() {
                @Override
                public void OnResponse(JSONObject Response) {
                    if(Response!=null) {
                        try {
                            JSONArray jsonArray = Response.getJSONArray("deviceData");
                            SessionHandler.updateSnessionHandler(getBaseContext(), jsonArray, mSharedPreferences);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {

                        Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "Check your internet connectivity.", Snackbar.LENGTH_LONG);
                        snackbar1.show();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        parseView();
    }

    private List<VehicleList> parseView() {

        JSONObject previousData = null;
        try {
            previousData = new JSONObject(mSharedPreferences.getString("deviceData","{}"));
            Log.d("tt", String.valueOf(previousData));
            JSONArray vehicleList = previousData.getJSONArray("OnLineList");
            Log.d("to", String.valueOf(vehicleList.length()));
            progressDialog.dismiss();
            swipeRefreshLayout.setRefreshing(false);
            for (int i=0;i<vehicleList.length();i++){
                JSONObject jsonObject = vehicleList.getJSONObject(i);
                VehicleList vehicleList1 = new VehicleList(jsonObject.getInt("deviceId"),jsonObject.getString("name"),jsonObject.getString("uniqueid")
                ,jsonObject.getString("status"),jsonObject.getString("date"),jsonObject.getString("category"),jsonObject.getInt("positionid"),
                        jsonObject.getString("address"),jsonObject.getString("time"),jsonObject.getDouble("speed"),jsonObject.getDouble("latitude"),
                        jsonObject.getDouble("longitude"),jsonObject.getString("timeDiff"),jsonObject.getInt("groupid"));

                listArrayList.add(vehicleList1);
            }
            return listArrayList;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return listArrayList;
    }
    @Override
    public void onRefresh() {
        listArrayList.clear();
        parseView();
    }
    @Override
    public void OnItemClick(View view, int position, List<VehicleList> mFilteredList) {
        if (view.getId() == R.id.detail_ll) {
            Intent intent = new Intent(OnLineOffLineActivity.this, VehicleOnMap.class);
            Log.d("Position", String.valueOf(position));
            intent.putExtra("id", mFilteredList.get(position).getId());
            intent.putExtra("name", mFilteredList.get(position).getName());
            intent.putExtra("pid", mFilteredList.get(position).getPositionId());
            intent.putExtra("uid", mFilteredList.get(position).getUniqueId());
            intent.putExtra("status", mFilteredList.get(position).getStatus());
            intent.putExtra("category", mFilteredList.get(position).getCategory());
            intent.putExtra("lastupdate", mFilteredList.get(position).getLastUpdates());
            intent.putExtra("diff", mFilteredList.get(position).getTimeDiff());
            intent.putExtra("address",mFilteredList.get(position).getAddress());
            intent.putExtra("speed",mFilteredList.get(position).getSpeed());
            intent.putExtra("lat",mFilteredList.get(position).getLatitute());
            intent.putExtra("long",mFilteredList.get(position).getLongitute());
            intent.putExtra("distance",mFilteredList.get(position).getDistance_travelled());
            Log.d("latit", String.valueOf(mFilteredList.get(position).latitute));
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        } else if (view.getId() == R.id.track_ll) {
            Intent trackIntent = new Intent(OnLineOffLineActivity.this, TrackingDevicesActivity.class);
            trackIntent.putExtra("device_id", mFilteredList.get(position).getPositionId());
            trackIntent.putExtra("uid",mFilteredList.get(position).getUniqueId());
            trackIntent.putExtra("tname", mFilteredList.get(position).getName());
            trackIntent.putExtra("status", mFilteredList.get(position).getStatus());
            trackIntent.putExtra("tupdate", mFilteredList.get(position).getLastUpdates());
            trackIntent.putExtra("ttimer", mFilteredList.get(position).getTime());
            trackIntent.putExtra("category", mFilteredList.get(position).getCategory());
            trackIntent.putExtra("address",mFilteredList.get(position).getAddress());
            trackIntent.putExtra("speed",mFilteredList.get(position).getSpeed());
            trackIntent.putExtra("lat",mFilteredList.get(position).getLatitute());
            trackIntent.putExtra("id", mFilteredList.get(position).getId());
            trackIntent.putExtra("long",mFilteredList.get(position).getLongitute());
            trackIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(trackIntent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        searchMenuItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
                searchVehicle(searchView);
                Log.d("CollapseValue","Expands");

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                Log.d("CollapseValue","Collapse");
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void searchVehicle(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                vehiclesAdapter.getFilter().filter(newText);
                return true;
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
        getBaseContext().stopService(updateListViewService);
        pintent.cancel();
        super.onBackPressed();
    }

}
