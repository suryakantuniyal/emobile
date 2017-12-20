package in.gtech.gogeotrack.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import in.gtech.gogeotrack.activity.MainActivity;
import in.gtech.gogeotrack.activity.MapViewActivity;
import in.gtech.gogeotrack.activity.OnLineOffLineActivity;
import in.gtech.gogeotrack.activity.TrackingDevicesActivity;
import in.gtech.gogeotrack.activity.VehicleDetailActivity;

/**
 * Created by surya on 27/9/17.
 */

public class UpdateListViewService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate(){
       // Toast.makeText(getBaseContext(), "ONCREATEEEEEEEEEEEEEee..", Toast.LENGTH_SHORT).show();

    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId){

       // Toast.makeText(getBaseContext(), "Service has been Started..", Toast.LENGTH_SHORT).show();
        MainActivity mainActivity = MainActivity.instance;
       // VehicleDetailActivity vehicleDetailActivity = VehicleDetailActivity.instance;
        VehicleDetailActivity vehicleDetailActivity = VehicleDetailActivity.vehicleDetailActivity;
        TrackingDevicesActivity trackingDevicesActivity = TrackingDevicesActivity.trackingDevicesActivity;
        MapViewActivity mapViewActivity = MapViewActivity.mapViewActivity;
      //  mainActivity.uploadNewData();

        OnLineOffLineActivity onLineOffLineActivity = OnLineOffLineActivity.onLineInstance;

        if (mainActivity!=null){
          //  Toast.makeText(getBaseContext(), "NULL ACTIVITY MAIN..", Toast.LENGTH_SHORT).show();
            mainActivity.uploadNewData();
        }
        if (trackingDevicesActivity!=null){
            trackingDevicesActivity.uploadIndividualData();
        }

        if (onLineOffLineActivity!=null){

            onLineOffLineActivity.uploadNewData();
        }
       /* if (vehicleDetailActivity!= null){
            vehicleDetailActivity.individualDetail();

        }*/
        if (vehicleDetailActivity!=null){
            vehicleDetailActivity.uploadIndividualData();
        }
        if (mapViewActivity!=null){
            mapViewActivity.reloadMap();
        }


        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy(){

        stopSelf();
        super.onDestroy();
       // Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }
}
