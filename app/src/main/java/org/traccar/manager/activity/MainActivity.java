package org.traccar.manager.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import org.traccar.manager.R;
import org.traccar.manager.adapter.VehiclesAdapter;
import org.traccar.manager.api.APIServices;
import org.traccar.manager.model.VehicleList;
import org.traccar.manager.network.ResponseCallbackEvents;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView textView ,mToolbar;
    private VehiclesAdapter vehiclesAdapter;
    private RecyclerView recyclerView;
    private ArrayList<VehicleList> listArrayList ;
    public static MenuItem  searchMenuItem;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(" ");
        mToolbar = (TextView) findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Hold on for a moment.");
        progressDialog.show();
        setProgressBarIndeterminateVisibility(true);
        new Async().execute();
        recyclerView = (RecyclerView) findViewById(R.id.vehicle_rv);
        listArrayList = new ArrayList<VehicleList>();
        vehiclesAdapter = new VehiclesAdapter(this,listArrayList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(vehiclesAdapter);
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
            public void onSuccess(ArrayList<VehicleList> result) {
                progressDialog.dismiss();
                for(int i=0; i< result.size() ;i++){
                    VehicleList vehicles = new VehicleList(result.get(i).id,result.get(i).name,result.get(i).uniqueId
                    ,result.get(i).status,result.get(i).lastUpdates,result.get(i).category,result.get(i).positionId);
                    listArrayList.add(vehicles);
                    vehiclesAdapter.notifyDataSetChanged();
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
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_search:
                mToolbar.setHint("Search Vehicle...");
                mToolbar.setHintTextColor(Color.WHITE);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
