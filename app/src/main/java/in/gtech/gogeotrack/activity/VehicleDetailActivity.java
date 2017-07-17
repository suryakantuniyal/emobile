package in.gtech.gogeotrack.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import in.gtech.gogeotrack.R;
import in.gtech.gogeotrack.api.APIServices;
import in.gtech.gogeotrack.model.VehicleList;
import in.gtech.gogeotrack.network.DetailResponseCallback;

/**
 * Created by silence12 on 21/6/17.
 */

public class VehicleDetailActivity extends AppCompatActivity {

    private static final String TAG = VehicleDetailActivity.class.getSimpleName();
    private static String address;
    private static Double speed;
    GoogleMap googleMap;
    private TextView name_tv, positionId_tv, uniqueId_tv, status_tv, lastUpdate_tv, category_tv, contact_tv,
            speed_tv, distance_tv, timedated;
    private ImageView projImageView;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ProgressDialog progressDialog;
    private String nameString, positionIdString, uniqueIdString, lastUpdatetString, categoryString, statusString,
            contactString, diffString;
    private int id, positionId;
    private Button homeButton;
    private MarkerOptions markerOptions;
    private CameraPosition cameraPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_vehicle);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("DeviceDetail");
        initViews();
        getIntentFileds();
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(nameString);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Wait a moment...");
        progressDialog.show();
        final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if (state.name().equals(State.COLLAPSED.name())) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                } else if (state.name().equals(State.IDLE.name())) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }
        });
        new Async().execute();
    }

    private void getIntentFileds() {
        Intent getIntent = getIntent();
        id = getIntent.getIntExtra("id", -1);
        nameString = getIntent.getStringExtra("name");
        uniqueIdString = getIntent.getStringExtra("uid");
        positionId = getIntent.getIntExtra("pid", -1);
        lastUpdatetString = getIntent.getStringExtra("lastupdate");
        statusString = getIntent.getStringExtra("status");
        categoryString = getIntent.getStringExtra("category");
        contactString = getIntent.getStringExtra("contact");
        diffString = getIntent.getStringExtra("diff");
        Log.d("Diff", diffString);
    }

    public void callDetailRequest(int id) {
        APIServices.GetVehicleDetailById(VehicleDetailActivity.this, id, new DetailResponseCallback() {
            @Override
            public void OnResponse(VehicleList Response) {
                progressDialog.dismiss();
                name_tv.setText(nameString);
                String speed = String.valueOf(Response.speed);
                String distance = String.valueOf(Response.distance_travelled);
                if (Response.address.equals("null")) {
                    positionId_tv.setText("Loading...");
                } else {
                    positionId_tv.setText(Response.address);
                }
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map2)).getMap();
                CameraUpdate point = CameraUpdateFactory.newLatLng(new LatLng(20.5937, 78.9629));
                googleMap.moveCamera(point);
                markerOptions = new MarkerOptions().position(new LatLng(Response.latitute, Response.longitute)).title(Response.address);
                cameraPosition = new CameraPosition.Builder().target(new LatLng(Response.latitute, Response.longitute)).zoom(14).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                if (statusString.equals("online")) {
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.greentruck));
                } else if (statusString.equals("offline")) {
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.redtruck));
                } else {
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_truck_med));
                }
                googleMap.addMarker(markerOptions);
                uniqueId_tv.setText(uniqueIdString);
                lastUpdate_tv.setText(lastUpdatetString);
                status_tv.setText(statusString);
                category_tv.setText(categoryString);
                speed_tv.setText(speed);
                timedated.setText(diffString);
                distance_tv.setText(distance);
            }
        });
    }

    private void initViews() {
//        projImageView = (ImageView) findViewById(R.id.category_image_iv);
        name_tv = (TextView) findViewById(R.id.project_name_tv);
        positionId_tv = (TextView) findViewById(R.id.positionId_tv);
        uniqueId_tv = (TextView) findViewById(R.id.uniqueid_tv);
        lastUpdate_tv = (TextView) findViewById(R.id.date_tv);
        status_tv = (TextView) findViewById(R.id.status_tv);
        category_tv = (TextView) findViewById(R.id.category_tv);
        speed_tv = (TextView) findViewById(R.id.speed_tv);
        distance_tv = (TextView) findViewById(R.id.distancecover_tv);
        timedated = (TextView) findViewById(R.id.diff_tv);
        homeButton = (Button) findViewById(R.id.gohome_btn);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

//        Glide.with(this).load(R.drawable.ic_truck).asBitmap()
//                .centerCrop().placeholder(R.drawable.placeholderxx4).into(projImageView);


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
        new Async().execute();

    }

    public abstract static class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {

        private State mCurrentState = State.IDLE;

        @Override
        public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
            if (i == 0) {
                if (mCurrentState != State.EXPANDED) {
                    onStateChanged(appBarLayout, State.EXPANDED);
                }
                mCurrentState = State.EXPANDED;
            } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
                if (mCurrentState != State.COLLAPSED) {
                    onStateChanged(appBarLayout, State.COLLAPSED);
                }
                mCurrentState = State.COLLAPSED;
            } else {
                if (mCurrentState != State.IDLE) {
                    onStateChanged(appBarLayout, State.IDLE);
                }
                mCurrentState = State.IDLE;
            }
        }

        public abstract void onStateChanged(AppBarLayout appBarLayout, State state);

        public enum State {
            EXPANDED,
            COLLAPSED,
            IDLE
        }
    }

    public class Async extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            callDetailRequest(positionId);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
