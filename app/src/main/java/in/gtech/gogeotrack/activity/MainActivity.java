package in.gtech.gogeotrack.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import in.gtech.gogeotrack.R;
import in.gtech.gogeotrack.adapter.VehicleslistAdapter;
import in.gtech.gogeotrack.api.APIServices;
import in.gtech.gogeotrack.model.VehicleList;
import in.gtech.gogeotrack.network.ResponseOnlineVehicle;
import in.gtech.gogeotrack.services.UpdateListViewService;
import in.gtech.gogeotrack.utils.URLContstant;

public class MainActivity extends AppCompatActivity
        implements VehicleslistAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    // private static final String TAG = "MainActivity";

    public static MainActivity instance;
     SharedPreferences.Editor mEditor;

    private static final String TAG = "BroadcastTest";
    private Intent intent;

    private Intent locationService;
    private Intent gpsTrackerService;
   // UpdateLocationService updateLocationService;
    //    UpdateListViewService updateListViewService;
    public static MenuItem searchMenuItem;
    private static ProgressDialog progressDialog;
    SharedPreferences mSharedPreferences;
    private VehicleslistAdapter vehiclesAdapter;
    private RecyclerView recyclerView;
    private List<VehicleList> listArrayList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout no_data_ll;

    PendingIntent pintent;
    AlarmManager alarm;
    private boolean bound = false;

   // private LocalService mBoundService;
    Intent updateListViewService;

    String userName,password;
    int vhcleId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;

        updateListViewService = new Intent(getBaseContext(), UpdateListViewService.class);
        startService(updateListViewService);

        // intent = new Intent(getBaseContext(), BroadcastService.class);
        /*locationService = new Intent(getBaseContext(),UpdateLocationService.class);
         startService(locationService);*/
        // gpsTrackerService = new Intent(this, GPSTracker.class);
        // updateLocationService = new UpdateLocationService();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("ListView");
        getSupportActionBar().setIcon(R.mipmap.luncher_icon);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Wait a moment...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);

        userName = mSharedPreferences.getString(URLContstant.KEY_USERNAME, "");
        password = mSharedPreferences.getString(URLContstant.KEY_PASSWORD,"");
       // vhcleId  = mSharedPreferences.getInt("", URLContstant.VEHICLE_ID);


        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(this);
        no_data_ll = (LinearLayout) findViewById(R.id.no_vehicle_ll);
        listArrayList = new ArrayList<VehicleList>();
        recyclerView = (RecyclerView) findViewById(R.id.vehicle_rv);

        listArrayList = parseView();

        if (listArrayList.size() == 0) {
            progressDialog.dismiss();
            no_data_ll.setVisibility(View.VISIBLE);
        } else {
            no_data_ll.setVisibility(View.GONE);
        }
        vehiclesAdapter = new VehicleslistAdapter(getBaseContext(), listArrayList, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(vehiclesAdapter);

//        doBindService();
        // startService(locationService);
        //vehiclesAdapter.notifyDataSetChanged();

        // startService(locationService);

        pintent = PendingIntent.getService(MainActivity.this, 0, updateListViewService,0);
        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar cal = Calendar.getInstance();
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 30 * 1000, pintent);
        recyclerView.getRecycledViewPool().clear();

       /* swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                recyclerView.getRecycledViewPool().clear();
                parseView();
            }
        });*/
        vehiclesAdapter.setOnItemClickListener(this);

        // updateCard();
    }


   /* @Override
    protected void onStart() {
        super.onStart();
        // bind to Service
        Intent intent = new Intent(this, UpdateLocationService.class);
        bindService(intent, (ServiceConnection) serviceConnection, Context.BIND_AUTO_CREATE);
    }*/


    /* private ServiceConnection serviceConnection = new ServiceConnection() {

         @Override
         public void onServiceConnected(ComponentName className, IBinder service) {
             // cast the IBinder and get UpdateListViewService instance
             UpdateLocationService.LocalBinder binder = (UpdateLocationService.LocalBinder) service;
             updateLocationService = binder.getService();
             bound = true;
             updateLocationService.setCallbacks(MainActivity.this); // register
         }

         @Override
         public void onServiceDisconnected(ComponentName arg0) {
             bound = false;
         }
     };*/

    public void uploadNewData() {

       // Toast.makeText(getBaseContext(), "main activvty new", Toast.LENGTH_LONG).show();
        listArrayList.clear();
       // swipeRefreshLayout.setRefreshing(true);

        APIServices.GetAllOnlineVehicleList(MainActivity.this,userName,password, new ResponseOnlineVehicle() {
            @Override
            public void onSuccessOnline(JSONArray result) {
                Log.d("Result", String.valueOf(result));
                SessionHandler.updateSnessionHandler(getBaseContext(), result, mSharedPreferences);
               // recyclerView.getRecycledViewPool().clear();
                vehiclesAdapter.notifyDataSetChanged();
                //8189 1.8.23
                //6958 1.6.27
            }
        });
        parseView();
    /*    listArrayList.clear();
        parseView();
        vehiclesAdapter.notifyDataSetChanged();*/

       /* swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                vehiclesAdapter.notifyDataSetChanged();
               *//* swipeRefreshLayout.setRefreshing(true);
                vehiclesAdapter.notifyDataSetChanged();
                Toast.makeText(getBaseContext(),"SWIPE REFRESH LAYOUT",Toast.LENGTH_LONG).show();
                parseView();
*//*
            }
        });*/
        // listArrayList.clear();

        //return 1;

       // Toast.makeText(getBaseContext(), "UPLOAD NEW DATA", Toast.LENGTH_LONG).show();
    }

    /*private ServiceConnection mConnection  = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBoundService = ((LocalService.LocalBinder)service).getService();

            Toast.makeText(getApplicationContext(), R.string.local_service_connected,
                    Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

            mBoundService = null;
            Toast.makeText(getApplicationContext(), R.string.local_service_disconnected,
                    Toast.LENGTH_SHORT).show();


        }
    };*/

   /* void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(MainActivity.this,
                LocalService.class), mConnection, Context.BIND_AUTO_CREATE);
        bound = true;
    }

    void doUnbindService() {
        if (bound) {
            // Detach our existing connection.
            unbindService(mConnection);
            bound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }*/

   /*{
        @Override
        protected ServiceConnection initialValue() {
            return new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
               // UpdateLocationService.LocalBinder binder = (in.gtech.gogeotrack.services.UpdateLocationService.LocalBinder) updateLocationService;
                    in.gtech.gogeotrack.services.UpdateLocationService.LocalBinder binder = (in.gtech.gogeotrack.services.UpdateLocationService.LocalBinder) service;
                   updateLocationService = service
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };
        }
    };*/
  /*  private void updateCard() {
       // startService(locationService);
        listArrayList.clear();
        parseView();
        vehiclesAdapter.notifyDataSetChanged();

    }
*/
   /* private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent);
        }
    };*/

   /* private void updateUI(Intent intent) {

        String counter = intent.getStringExtra("counter");
        String time = intent.getStringExtra("time");
        Log.d(TAG, counter);
        Log.d(TAG, time);
    }
*/

    @Override
    public void OnItemClick(View view, int position, List<VehicleList> mFilteredList) {

        if (view.getId() == R.id.detail_ll) {
            //  activitystart();
            // startService(updateListViewService);
            // startService(locationService);
            Intent intent = new Intent(MainActivity.this, VehicleDetailActivity.class);
            Log.d("Position", String.valueOf(position));
            int id = mFilteredList.get(position).getId();

           // intent.putExtra("deviceId",mFilteredList.get(position).getDeviceId());

            intent.putExtra("id", mFilteredList.get(position).getId());
            intent.putExtra("name", mFilteredList.get(position).getName());
            intent.putExtra("pid", mFilteredList.get(position).getPositionId());
            intent.putExtra("uid", mFilteredList.get(position).getUniqueId());

           /* String uniqid = mFilteredList.get(position).getUniqueId();
            Log.d("uniqly",String.valueOf(uniqid));

            int uniId = Integer.parseInt(uniqid);
            Log.d("intuniq",String.valueOf(uniId));*/

            intent.putExtra("status", mFilteredList.get(position).getStatus());
            intent.putExtra("category", mFilteredList.get(position).getCategory());
            intent.putExtra("lastupdate", mFilteredList.get(position).getLastUpdates());
            intent.putExtra("diff", mFilteredList.get(position).getTimeDiff());
            intent.putExtra("address", mFilteredList.get(position).getAddress());
            intent.putExtra("speed", mFilteredList.get(position).getSpeed());
            intent.putExtra("time",mFilteredList.get(position).getTime());

            intent.putExtra("lat", mFilteredList.get(position).getLatitute());
            intent.putExtra("long", mFilteredList.get(position).getLongitute());

            intent.putExtra("distance", mFilteredList.get(position).getDistance_travelled());
            Log.d("latit", String.valueOf(mFilteredList.get(position).latitute));
            // intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        } else if (view.getId() == R.id.track_ll) {
            Intent trackIntent = new Intent(MainActivity.this, TrackingDevicesActivity.class);

            trackIntent.putExtra("device_id", mFilteredList.get(position).getPositionId());
            trackIntent.putExtra("uid",mFilteredList.get(position).getUniqueId());
            trackIntent.putExtra("tname", mFilteredList.get(position).getName());
            trackIntent.putExtra("status", mFilteredList.get(position).getStatus());
            trackIntent.putExtra("tupdate", mFilteredList.get(position).getLastUpdates());
            trackIntent.putExtra("category", mFilteredList.get(position).getCategory());
            trackIntent.putExtra("ttimer", mFilteredList.get(position).getTime());
            Log.d("TimeCheck", mFilteredList.get(position).getTime());
            trackIntent.putExtra("address", mFilteredList.get(position).getAddress());
            trackIntent.putExtra("speed", mFilteredList.get(position).getSpeed());
            trackIntent.putExtra("lat", mFilteredList.get(position).getLatitute());
            trackIntent.putExtra("long", mFilteredList.get(position).getLongitute());
            trackIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(trackIntent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }



    @Override
    public void onRefresh() {
        recyclerView.getRecycledViewPool().clear();
        listArrayList.clear();
        parseView();

       /* Toast.makeText(getApplicationContext(),"ON REFERESH OF MAIN", Toast.LENGTH_SHORT).show();
        vehiclesAdapter.notifyDataSetChanged();*/
    }

    private List<VehicleList> parseView() {

        JSONObject previousData = null;
        try {
            previousData = new JSONObject(mSharedPreferences.getString("deviceData", "{}"));
            Log.d("tt", String.valueOf(previousData));
            JSONArray vehicleList = previousData.getJSONArray("totalLst");
            Log.d("to", String.valueOf(vehicleList));
            progressDialog.dismiss();
            swipeRefreshLayout.setRefreshing(false);
            for (int i = 0; i < vehicleList.length(); i++) {
                JSONObject jsonObject = vehicleList.getJSONObject(i);
                VehicleList vehicleList1 = new VehicleList(jsonObject.getInt("id"), jsonObject.getString("name"), jsonObject.getString("uniqueId"), jsonObject.getString("status"),
                        jsonObject.getString("date"), jsonObject.getString("category"), jsonObject.getInt("positionId"), jsonObject.getString("address"), jsonObject.getString("time"), jsonObject.getString("timeDiff")
                        , jsonObject.getDouble("speed"), jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude"), jsonObject.getDouble("distance"));
                listArrayList.add(vehicleList1);
               /* int id = jsonObject.getInt("id");
                mEditor = mSharedPreferences.edit();
                mEditor.putInt(String.valueOf(URLContstant.VEHICLE_ID),id);
                mEditor.apply();*/
            }
            return listArrayList;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return listArrayList;

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
        getBaseContext().stopService(updateListViewService);
        pintent.cancel();
        //stopService(updateListViewService);

        super.onBackPressed();

    }

    @Override
    protected void onResume() {
        // startService(locationService);
        super.onResume();
        //startService(locationService);
        // startService(intent);
        // registerReceiver(broadcastReceiver, new IntentFilter(BroadcastService.BROADCAST_ACTION));
    }

    @Override
    public void onDestroy() {
       // Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        getBaseContext().stopService(updateListViewService);
        pintent.cancel();
        super.onDestroy();
        updateListViewService = null;

    }

}
