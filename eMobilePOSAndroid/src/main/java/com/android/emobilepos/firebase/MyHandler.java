package com.android.emobilepos.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.android.database.DBManager;
import com.android.emobilepos.R;
import com.android.emobilepos.mainmenu.MainMenu_FA;
import com.android.emobilepos.models.firebase.NotificationEvent;
import com.android.support.SynchMethods;
import com.microsoft.windowsazure.notifications.NotificationsHandler;

/**
 * Created by guarionex on 11/25/16.
 */

public class MyHandler extends NotificationsHandler {
    private static final int NOTIFICATION_ID = 1;
    private Context ctx;

    @Override
    public void onReceive(Context context, Bundle bundle) {
        ctx = context;
        String eventAction = bundle.getString("gcm.notification.notificationEventAction");
        NotificationEvent.NotificationEventAction action = NotificationEvent.NotificationEventAction.valueOf(eventAction);
        sendNotification(eventAction);

//        DBManager dbManager = new DBManager(getActivity());
//        SynchMethods sm = new SynchMethods(dbManager);
    }

    private void sendNotification(String msg) {

        Intent intent = new Intent(ctx, MainMenu_FA.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        NotificationManager mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
                intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Notification Hub Demo")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setSound(defaultSoundUri)
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}