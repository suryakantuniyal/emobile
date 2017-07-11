package org.traccar.manager.network;

import org.traccar.manager.model.OnlineModel;
import org.traccar.manager.model.VehicleList;

import java.util.ArrayList;

/**
 * Created by silence12 on 10/7/17.
 */

public interface ResponseOnlineVehicle {
    void onSuccessOnline(ArrayList<VehicleList> result);
}
