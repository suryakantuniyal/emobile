package in.gtech.gogeotrack.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import in.gtech.gogeotrack.R;
import in.gtech.gogeotrack.model.VehicleList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by silence12 on 5/7/17.
 */

public class MapViewActivity extends AppCompatActivity {

    public static MenuItem searchMenuItem;
    GoogleMap googleMap;
    SharedPreferences sharedPreferences;
    int locationCount = 0;
    ArrayList<VehicleList> result;
    MarkerOptions markerOptions;
    private List<VehicleList> mFilteredList;
    private CameraPosition cameraPosition;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.luncher_icon);
        setTitle("MapView");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        result = SplashActivity.latlongList;
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        // Showing status
        if (status != ConnectionResult.SUCCESS) {

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        } else {

            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(28.644800, 77.216721)));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            googleMap.setMyLocationEnabled(true);
            if (result.size() != 0) {
                Double lat = 0.0;
                Double lng = 0.0;
                for (int i = 0; i < result.size(); i++) {
                    lat = result.get(i).getLatitute();
                    lng = result.get(i).getLongitute();
                    drawMarker(new LatLng(lat, lng), result.get(i).status);
                }
            }
        }

    }

    private void drawMarker(LatLng point, String str) {

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        if (str.equals("online")) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.greentruck));
        } else if (str.equals("offline")) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.redtruck));
        } else {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_truck_med));
        }
        googleMap.addMarker(markerOptions);
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
                for (int i = 0; i < result.size(); i++) {
//                    if(result.get(i).getName().contains(query)){
//                        cameraPosition = new CameraPosition.Builder().target(new LatLng(result.get(i).latitute,result.get(i).longitute)).zoom(14).build();
//                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//                    }
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
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

