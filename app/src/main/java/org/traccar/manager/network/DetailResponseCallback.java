package org.traccar.manager.network;

import org.json.JSONObject;
import org.traccar.manager.model.VehicleList;

/**
 * Created by silence12 on 23/6/17.
 */

public interface DetailResponseCallback {
    void OnResponse(VehicleList Response);
}
