package in.gtech.gogeotrack.network;

import org.json.JSONArray;

import in.gtech.gogeotrack.model.VehicleList;

/**
 * Created by silence12 on 23/6/17.
 */

public interface DetailResponseCallback {
    void OnResponse(JSONArray Response);
}
