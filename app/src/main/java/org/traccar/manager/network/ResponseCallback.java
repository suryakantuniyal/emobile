package org.traccar.manager.network;

import org.json.JSONObject;

/**
 * Created by silence12 on 15/6/17.
 */

public interface ResponseCallback {
    void OnResponse(JSONObject Response);
}
