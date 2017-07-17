package in.gtech.gogeotrack.network;

import in.gtech.gogeotrack.model.VehicleList;

import java.util.ArrayList;

/**
 * Created by silence12 on 10/7/17.
 */

public interface ResponseOnlineVehicle {
    void onSuccessOnline(ArrayList<VehicleList> result);
}
