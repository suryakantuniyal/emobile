package org.traccar.manager.FCM;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;
import org.traccar.manager.utils.URLContstant;

/**
 * Created by silence12 on 20/6/17.
 */

public class MessagingHandler extends FirebaseMessagingService {

    private static final String TAG = "MessagingHandler";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
//        Log.d("From: ", remoteMessage.getFrom());
//        if (remoteMessage.getFrom().equals(URLContstant.INTERCITY_ORDERS_TOPICS)){
//            Intent intent = new Intent(this, DatabaseQueryService.class);
//            intent.putExtra(URLContstant.KEY_NOTIFICATION_DATA,remoteMessage.getData().get("message"));
//            startService(intent);
//            Intent newOrder = new Intent(URLContstant.KEY_NEW_ORDER);
//            try {
//                JSONObject mJsonObject = new JSONObject(remoteMessage.getData().get("message"));
//                if (mJsonObject.getString("action").equals("bidPrice") || mJsonObject.getString("action").equals("updateBidPrice")){
//                    newOrder.putExtra(URLContstant.KEY_NOTIFICATION_DATA,remoteMessage.getData().get("message"));
//                    sendBroadcast(newOrder);
//                }
//            } catch (JSONException e) {
//                Log.d(TAG,e.toString());
//            }
//        } else if (remoteMessage.getFrom().equals("747358378481")){
//            Intent intent = new Intent(this, DatabaseQueryService.class);
//            intent.putExtra(URLContstant.KEY_NOTIFICATION_DATA,remoteMessage.getData().get("message"));
//            startService(intent);
//        }
    }
}

