package org.traccar.manager.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.design.widget.SwipeDismissBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.api.Result;

import org.traccar.manager.R;
import org.traccar.manager.adapter.VehiclesAdapter;
import org.traccar.manager.adapter.VehicleslistAdapter;
import org.traccar.manager.api.APIServices;
import org.traccar.manager.model.VehicleList;
import org.traccar.manager.network.DetailResponseCallback;
import org.traccar.manager.network.ResponseCallbackEvents;
import org.traccar.manager.utils.URLContstant;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements VehicleslistAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private VehicleslistAdapter vehiclesAdapter;
    private RecyclerView recyclerView;
    private ArrayList<VehicleList> listArrayList;
    public static ArrayList<VehicleList> onLineList;
    public static ArrayList<VehicleList>offlineList;
    public static ArrayList<VehicleList>latlongList;
    public static MenuItem searchMenuItem;
    private static ProgressDialog progressDialog;
    private TextView username;
    SharedPreferences mSharedPreferences;
    private SwipeRefreshLayout swipeRefreshLayout;
    public static int AllSize,onlinesize,offlinesize ;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.luncher_icon);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Wait a moment...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(this);
        new Async().execute();
        recyclerView = (RecyclerView) findViewById(R.id.vehicle_rv);
        listArrayList = new ArrayList<VehicleList>();
        onLineList = new ArrayList<VehicleList>();
        offlineList = new ArrayList<VehicleList>();
        latlongList = new ArrayList<VehicleList>();
        vehiclesAdapter = new VehicleslistAdapter(getBaseContext(), listArrayList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(vehiclesAdapter);
        vehiclesAdapter.setOnItemClickListener(this);
    }

    @Override
    public void OnItemClick(View view, int position) {

        if (view.getId() == R.id.detail_ll) {
            
            Intent intent = new Intent(MainActivity.this, VehicleDetailActivity.class);
            intent.putExtra("id", listArrayList.get(position).getId());
            intent.putExtra("name", listArrayList.get(position).getName());
            intent.putExtra("pid", listArrayList.get(position).getPositionId());
            intent.putExtra("uid", listArrayList.get(position).getUniqueId());
            intent.putExtra("status", listArrayList.get(position).getStatus());
            intent.putExtra("category", listArrayList.get(position).getCategory());
            intent.putExtra("lastupdate", listArrayList.get(position).getLastUpdates());
            intent.putExtra("diff", listArrayList.get(position).getTimeDiff());
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        } else  if (view.getId() == R.id.track_ll) {
            Intent trackIntent = new Intent(MainActivity.this, TrackingDevicesActivity.class);
            trackIntent.putExtra("device_id", listArrayList.get(position).getPositionId());
            trackIntent.putExtra("tname", listArrayList.get(position).getName());
            trackIntent.putExtra("status",listArrayList.get(position).getStatus());
            trackIntent.putExtra("tupdate", listArrayList.get(position).getLastUpdates());
            trackIntent.putExtra("ttimer", listArrayList.get(position).getTime());
            trackIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(trackIntent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        }


    }

    @Override
    public void onRefresh() {
        new Async().execute();
    }

    public class Async extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            parseView();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private void parseView() {
        ArrayList<VehicleList> result = Main2Activity.listArrayList;
        progressDialog.dismiss();
        swipeRefreshLayout.setRefreshing(false);
        for (int i = 0; i < result.size(); i++) {
            final int finalI = i;
            VehicleList vehicles = new VehicleList(result.get(finalI).id, result.get(finalI).name, result.get(finalI).uniqueId
                    , result.get(finalI).status, result.get(finalI).lastUpdates, result.get(finalI).category, result.get(finalI).positionId, result.get(finalI).address,
                    result.get(finalI).time, result.get(finalI).timeDiff);
            listArrayList.add(vehicles);
        }

    }

//    private void parseView() {
//        APIServices.GetAllVehicleList(MainActivity.this, new ResponseCallbackEvents() {
//            @Override
//            public void onSuccess(final ArrayList<VehicleList> result) {
//                progressDialog.dismiss();
//                AllSize = result.size();
//                swipeRefreshLayout.setRefreshing(false);
//                for (int i = 0; i < result.size(); i++) {
//                    int id = result.get(i).positionId;
//                    final int finalI = i;
//                    APIServices.GetVehicleDetailById(MainActivity.this, id, new DetailResponseCallback() {
//                        @Override
//                        public void OnResponse(VehicleList Response) {
//                            VehicleList vehicles = new VehicleList(result.get(finalI).id, result.get(finalI).name, result.get(finalI).uniqueId
//                                    , result.get(finalI).status, result.get(finalI).lastUpdates, result.get(finalI).category, result.get(finalI).positionId, Response.address,
//                                    result.get(finalI).time, result.get(finalI).timeDiff);
//                            listArrayList.add(vehicles);
//                            if(result.get(finalI).status.equals("online")){
//                                onLineList.add(vehicles);
//                            }
//                            if(result.get(finalI).status.equals("offline")){
//                                offlineList.add(vehicles);
//                            }
//
//                            VehicleList latlong = new VehicleList(result.get(finalI).id,result.get(finalI).status,Response.latitute,Response.longitute);
//                            latlongList.add(latlong);
//                            vehiclesAdapter.notifyDataSetChanged();
//
//                        }
//                    });
//                }
//                offlinesize = offlineList.size();
//                onlinesize = onLineList.size();
//
//            }
//        });
//    }

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
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
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
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
