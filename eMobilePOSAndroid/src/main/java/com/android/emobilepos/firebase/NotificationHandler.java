package com.android.emobilepos.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.emobilepos.R;
import com.android.emobilepos.mainmenu.MainMenu_FA;
import com.android.emobilepos.models.firebase.NotificationEvent;
import com.android.support.NetworkUtils;
import com.android.support.SynchMethods;
import com.crashlytics.android.Crashlytics;
import com.microsoft.windowsazure.notifications.NotificationsHandler;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by guarionex on 11/25/16.
 */

public class NotificationHandler extends NotificationsHandler {
    private static final int NOTIFICATION_ID = 1;
    private Context ctx;

    @Override
    public void onReceive(final Context context, Bundle bundle) {
        ctx = context;
        final String eventAction = bundle.getString("action");
        if (eventAction != null) {
            if (NetworkUtils.isConnectedToInternet(context)) {
                NotificationEvent.NotificationEventAction action = NotificationEvent.NotificationEventAction.getNotificationEventByCode(Integer.parseInt(eventAction));
                switch (action) {
                    case SYNC_HOLDS:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Log.d("NotificationHandler", "synchOrdersOnHoldList");
                                    SynchMethods.synchOrdersOnHoldList(context);
                                    updateMainActivity(context, eventAction);
                                } catch (SAXException e) {
                                    e.printStackTrace();
                                    Crashlytics.logException(e);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Crashlytics.logException(e);
                                } catch (NoSuchAlgorithmException e) {
                                    e.printStackTrace();
                                    Crashlytics.logException(e);
                                } catch (KeyManagementException e) {
                                    e.printStackTrace();
                                    Crashlytics.logException(e);
                                }
                            }
                        }).start();
                        break;
                    case SYNC_MESAS_CONFIG:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Log.d("NotificationHandler", "synchSalesAssociateDinnindTablesConfiguration");
                                    SynchMethods.synchSalesAssociateDinnindTablesConfiguration(context);
//                                    updateMainActivity(context, eventAction);
                                } catch (SAXException e) {
                                    e.printStackTrace();
                                    Crashlytics.logException(e);
                                }
                            }
                        }).start();
                        break;
                }
            }
        }
    }

    static void updateMainActivity(Context context, String message) {
        Intent intent = new Intent(MainMenu_FA.NOTIFICATION_RECEIVED);
        intent.putExtra(MainMenu_FA.NOTIFICATION_MESSAGE, message);
        context.sendBroadcast(intent);
        Log.d("NotificationHandler", "sendBroadcast");
    }

    private void sendNotification(String msg) {

        Intent intent = new Intent(ctx, MainMenu_FA.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        NotificationManager mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
                intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String channelId = ctx.getString(R.string.notification_channel_id);
        String name = ctx.getString(R.string.notification_channel_name);
        String description = ctx.getString(R.string.notification_channel_description);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx, channelId)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Notification Hub Demo")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setSound(defaultSoundUri)
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}