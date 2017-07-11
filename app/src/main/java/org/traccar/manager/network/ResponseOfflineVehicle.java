package org.traccar.manager.network;

import org.traccar.manager.model.OfflineModel;
import org.traccar.manager.model.OnlineModel;
import org.traccar.manager.model.VehicleList;

import java.util.ArrayList;

/**
 * Created by silence12 on 10/7/17.
 */

public interface ResponseOfflineVehicle {
    void onSuccessOffline(ArrayList<VehicleList> result);

}
