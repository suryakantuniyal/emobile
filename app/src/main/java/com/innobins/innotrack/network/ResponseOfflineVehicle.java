package com.innobins.innotrack.network;

import com.innobins.innotrack.model.VehicleList;

import java.util.ArrayList;

/**
 * Created by silence12 on 10/7/17.
 */

public interface  ResponseOfflineVehicle  {
    void onSuccessOffline(ArrayList<VehicleList> result);

}
