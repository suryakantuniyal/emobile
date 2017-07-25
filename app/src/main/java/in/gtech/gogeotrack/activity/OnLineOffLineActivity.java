package in.gtech.gogeotrack.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import in.gtech.gogeotrack.R;
import in.gtech.gogeotrack.adapter.VehicleslistAdapter;
import in.gtech.gogeotrack.model.VehicleList;
import in.gtech.gogeotrack.parser.TraccerParser;
import in.gtech.gogeotrack.utils.URLContstant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by silence12 on 5/7/17.
 */

public class OnLineOffLineActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, VehicleslistAdapter.OnItemClickListener {
    public static MenuItem searchMenuItem;
    private static ProgressDialog progressDialog;
    SharedPreferences sharedPrefs;
    ArrayList<VehicleList> arrayList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private VehicleslistAdapter vehiclesAdapter;
    private RecyclerView recyclerView;
    private List<VehicleList> listArrayList;
    SharedPreferences mSharedPreferences;
    private String onnOff;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onlineoffline);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.luncher_icon);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Wait a moment...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        setTitle("Online Devices");
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh_online);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(this);
        listArrayList = new ArrayList<VehicleList>();
        recyclerView = (RecyclerView) findViewById(R.id.onlineoffline_rv);
        listArrayList = parseView();
        if(listArrayList.size() == 0){
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(),"No online Devices",Toast.LENGTH_SHORT).show();
        }
        vehiclesAdapter = new VehicleslistAdapter(getBaseContext(), listArrayList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(vehiclesAdapter);
        vehiclesAdapter.setOnItemClickListener(this);
    }


    private List<VehicleList> parseView() {

        JSONObject previousData = null;
        try {
            previousData = new JSONObject(mSharedPreferences.getString("deviceData","{}"));
            Log.d("tt", String.valueOf(previousData.length()));
            JSONArray vehicleList = previousData.getJSONArray("onlineLst");
            Log.d("to", String.valueOf(vehicleList.length()));
            progressDialog.dismiss();
            swipeRefreshLayout.setRefreshing(false);
            for (int i = 0; i < vehicleList.length(); i++) {
                JSONObject jsonObject = vehicleList.getJSONObject(i);
                VehicleList vehicleList1 = new VehicleList(jsonObject.getInt("id"),jsonObject.getString("name"),jsonObject.getString("uniqueId"),jsonObject.getString("status"),
                        jsonObject.getString("date"),jsonObject.getString("category"),jsonObject.getInt("positionId"),jsonObject.getString("address"),jsonObject.getString("time"),jsonObject.getString("timeDiff")
                        ,jsonObject.getDouble("speed"),jsonObject.getDouble("latitude"),jsonObject.getDouble("longitude"),jsonObject.getDouble("distance"));
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

            Intent intent = new Intent(OnLineOffLineActivity.this, VehicleDetailActivity.class);
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
            trackIntent.putExtra("tname", mFilteredList.get(position).getName());
            trackIntent.putExtra("status", mFilteredList.get(position).getStatus());
            trackIntent.putExtra("tupdate", mFilteredList.get(position).getLastUpdates());
            trackIntent.putExtra("ttimer", mFilteredList.get(position).getTime());
            trackIntent.putExtra("address",mFilteredList.get(position).getAddress());
            trackIntent.putExtra("speed",mFilteredList.get(position).getSpeed());
            trackIntent.putExtra("lat",mFilteredList.get(position).getLatitute());
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

}
