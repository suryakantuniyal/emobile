package org.traccar.manager.network;

import org.traccar.manager.model.VehicleList;

import java.util.ArrayList;

/**
 * Created by silence12 on 19/6/17.
 */

public interface ResponseCallbackEvents {
    void onSuccess(ArrayList<VehicleList> result);
}
