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
        implements NavigationView.OnNavigationItemSelectedListener, VehicleslistAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private VehicleslistAdapter vehiclesAdapter;
    private RecyclerView recyclerView;
    private ArrayList<VehicleList> listArrayList;
    public static ArrayList<VehicleList> onLineList;
    public static ArrayList<VehicleList>offlineList;
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
        getSupportActionBar().setIcon(R.mipmap.luncher_icon);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Wait a moment...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setOnRefreshListener(this);
        new Async().execute();
        recyclerView = (RecyclerView) findViewById(R.id.vehicle_rv);
        listArrayList = new ArrayList<VehicleList>();
        onLineList = new ArrayList<VehicleList>();
        offlineList = new ArrayList<VehicleList>();
        vehiclesAdapter = new VehicleslistAdapter(getBaseContext(), listArrayList, this);
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

//                username = (TextView) findViewById(R.id.profile_name_text);
//        username.setText(mSharedPreferences.getString(URLContstant.KEY_USERNAME,""));


    }

    @Override
    public void OnItemClick(View view, int position) {
        if (view.getId() == R.id.detail_btn) {
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

        } else  if (view.getId() == R.id.track_tv) {
            Intent trackIntent = new Intent(MainActivity.this, TrackingDevicesActivity.class);
            trackIntent.putExtra("device_id", listArrayList.get(position).getPositionId());
            trackIntent.putExtra("tname", listArrayList.get(position).getName());
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
        APIServices.GetAllVehicleList(MainActivity.this, new ResponseCallbackEvents() {
            @Override
            public void onSuccess(final ArrayList<VehicleList> result) {
                progressDialog.dismiss();
                AllSize = result.size();
                swipeRefreshLayout.setRefreshing(false);
                for (int i = 0; i < result.size(); i++) {
                    int id = result.get(i).positionId;
                    final int finalI = i;
                    if(result.get(finalI).status.equals("online")){
                           onLineList.add(result.get(i));
                    }
                    if(result.get(finalI).status.equals("offline")){
                        offlineList.add(result.get(i));
                    }
                    APIServices.GetVehicleDetailById(MainActivity.this, id, new DetailResponseCallback() {
                        @Override
                        public void OnResponse(VehicleList Response) {
                            VehicleList vehicles = new VehicleList(result.get(finalI).id, result.get(finalI).name, result.get(finalI).uniqueId
                                    , result.get(finalI).status, result.get(finalI).lastUpdates, result.get(finalI).category, result.get(finalI).positionId, Response.address,
                                    result.get(finalI).time, result.get(finalI).timeDiff);
                            listArrayList.add(vehicles);
                            vehiclesAdapter.notifyDataSetChanged();

                        }
                    });
                }
                offlinesize = offlineList.size();
                onlinesize = onLineList.size();

            }
        });
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

        if (id == R.id.nav_home) {
            Intent intent = new Intent(MainActivity.this,WelcomeMessageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);

        } else if (id == R.id.nav_trac) {

        } else if (id == R.id.nav_detail) {
//            Intent intent = new Intent(MainActivity.this, MainActivity.class);
//            startActivity(intent);
        } else if (id == R.id.nav_report) {
            Intent intent = new Intent(MainActivity.this,ReportsActivity.class);
                startActivity(intent);
        } else if (id == R.id.nav_contact) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:09999095036"));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
//                return TODO;
            }
            startActivity(callIntent);

        } else if (id == R.id.nav_online) {

        }else if (id == R.id.nav_offline) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
