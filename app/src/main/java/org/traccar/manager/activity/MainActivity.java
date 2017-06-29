package org.traccar.manager.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.design.widget.SwipeDismissBehavior;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
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

import org.traccar.manager.R;
import org.traccar.manager.adapter.VehiclesAdapter;
import org.traccar.manager.adapter.VehicleslistAdapter;
import org.traccar.manager.api.APIServices;
import org.traccar.manager.model.VehicleList;
import org.traccar.manager.network.DetailResponseCallback;
import org.traccar.manager.network.ResponseCallbackEvents;

import java.util.ArrayList;

public class MainActivity extends  AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,VehicleslistAdapter.OnItemClickListener{

    private VehicleslistAdapter vehiclesAdapter;
    private RecyclerView recyclerView;
    private ArrayList<VehicleList> listArrayList ;
    public static MenuItem  searchMenuItem;
    private static ProgressDialog progressDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Wait a moment...");
        progressDialog.setCancelable(false);
        new Async().execute();
        recyclerView = (RecyclerView) findViewById(R.id.vehicle_rv);
        listArrayList = new ArrayList<VehicleList>();
        vehiclesAdapter = new VehicleslistAdapter(getBaseContext(),listArrayList,this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(vehiclesAdapter);
        vehiclesAdapter.setOnItemClickListener(this);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void OnItemClick(View view, int position) {

       if(view.getId() == R.id.track_ll){
                Intent trackIntent = new Intent(MainActivity.this, TrackingDevicesActivity.class);
                trackIntent.putExtra("device_id",listArrayList.get(position).getPositionId());
                trackIntent.putExtra("tname",listArrayList.get(position).getName());
                trackIntent.putExtra("tupdate",listArrayList.get(position).getLastUpdates());
                trackIntent.putExtra("ttimer",listArrayList.get(position).getTime());
                trackIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(trackIntent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);

        } else if(view.getId() == R.id.viewDetail_ll){
           Intent intent = new Intent(MainActivity.this, VehicleDetailActivity.class);
           intent.putExtra("id",listArrayList.get(position).getId());
           intent.putExtra("name",listArrayList.get(position).getName());
           intent.putExtra("pid",listArrayList.get(position).getPositionId());
           intent.putExtra("uid",listArrayList.get(position).getUniqueId());
           intent.putExtra("status",listArrayList.get(position).getStatus());
           intent.putExtra("category",listArrayList.get(position).getCategory());
           intent.putExtra("lastupdate",listArrayList.get(position).getLastUpdates());
           intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
           startActivity(intent);
           overridePendingTransition(R.anim.slide_out_left,R.anim.slide_in_right);
       }


    }

    public class Async extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
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

    private void parseView()  {
        APIServices.GetAllVehicleList(MainActivity.this, new ResponseCallbackEvents() {
            @Override
            public void onSuccess(final ArrayList<VehicleList> result) {
                progressDialog.dismiss();
                for(int i=0; i< result.size() ;i++){
                    int id = result.get(i).positionId;
                    final int finalI = i;
                    APIServices.GetVehicleDetailById(MainActivity.this,id, new DetailResponseCallback() {
                        @Override
                        public void OnResponse(VehicleList Response) {
                            VehicleList vehicles = new VehicleList(result.get(finalI).id,result.get(finalI).name,result.get(finalI).uniqueId
                                    ,result.get(finalI).status,result.get(finalI).lastUpdates,result.get(finalI).category,result.get(finalI).positionId,Response.address,
                                    result.get(finalI).time);
                            listArrayList.add(vehicles);
                            vehiclesAdapter.notifyDataSetChanged();

                        }
                    });
//                    VehicleList vehicles = new VehicleList(result.get(i).id,result.get(i).name,result.get(i).uniqueId
//                    ,result.get(i).status,result.get(i).lastUpdates,result.get(i).category,result.get(i).positionId);
//                    listArrayList.add(vehicles);
//                    vehiclesAdapter.notifyDataSetChanged();
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        Intent intent = getIntent();
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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
