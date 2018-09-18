package com.android.emobilepos.firebase;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.database.DBManager;
import com.android.emobilepos.BuildConfig;
import com.android.emobilepos.R;
import com.android.emobilepos.mainmenu.MainMenu_FA;
import com.android.emobilepos.models.firebase.NotificationEvent;
import com.android.emobilepos.service.SyncConfigServerService;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NetworkUtils;
import com.android.support.SynchMethods;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import util.json.JsonUtils;

/**
 * Created by anieves on 2/10/17.
 */

public class PollingNotificationService extends Service {

    public static final String MAIN_ACTION = "pollingNotificationService.action.main";
    public static final String START_ACTION = "pollingNotificationService.action.startforeground";
    public static final String STOP_ACTION = "pollingNotificationService.action.stopforeground";
    public static final String ONHOLD_BROADCAST_ACTION = "3";
    public static final String MESAS_CONFIG_BROADCAST_ACTION = "6";
    public static final String NONE_BROADCAST_ACTION = "99";
    private static final String TAG = "PollingService";
    private static final int delay = 5000; // delay for 3 sec before first start
    public static int FOREGROUND_SERVICE = 101;
    Timer autoSyncTimer;
    private Timer timer;
    private Date lastPolled;
    private String accountNumber;
    MyPreferences preferences;

    public static boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (PollingNotificationService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void start(Context context, int flags) {
        Intent startIntent = new Intent(context, PollingNotificationService.class);
        startIntent.setAction(PollingNotificationService.START_ACTION);
        startIntent.setFlags(flags);
        context.startService(startIntent);
        Log.d("Polling service started", new Date().toString());
    }

    public static void stop(Context context) {
        Intent stopIntent = new Intent(context, PollingNotificationService.class);
        stopIntent.setAction(PollingNotificationService.STOP_ACTION);
        context.startService(stopIntent);
        Log.d("Polling service stoped", new Date().toString());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = new MyPreferences(this);
        accountNumber = preferences.getAcctNumber();
        lastPolled = new Date(0);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getAction().equals(START_ACTION)) {
                Log.i(TAG, "Received Start Foreground Intent");

                Intent notificationIntent = new Intent(this, MainMenu_FA.class);
                notificationIntent.setAction(MAIN_ACTION);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                if (preferences.isPollingHoldsEnable() && !MainMenu_FA.checkPlayServices(this)) {
                    if (timer == null) {
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Log.i(TAG, "Timer");
                                if (((intent.getFlags() & PollingServicesFlag.ONHOLDS.getCode()) == PollingServicesFlag.ONHOLDS.getCode()) ||
                                        ((intent.getFlags() & PollingServicesFlag.DINING_TABLES.getCode()) == PollingServicesFlag.DINING_TABLES.getCode())) {
                                    pollNotificationEvents(PollingNotificationService.this);
                                }
                            }
                        }, delay, BuildConfig.POLLING_PERIOD);
                    }
                }
                if (autoSyncTimer == null) {
                    autoSyncTimer = new Timer();
                    autoSyncTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Log.i(TAG, "autoSyncTimer");
                            if ((intent.getFlags() & PollingServicesFlag.AUTO_SYNC.getCode()) == PollingServicesFlag.AUTO_SYNC.getCode()) {
                                DBManager dbManager = new DBManager(PollingNotificationService.this, Global.FROM_SYNCH_ACTIVITY);
                                SynchMethods sm = new SynchMethods(dbManager);
                                sm.synchSend(Global.FROM_SYNCH_ACTIVITY, true);
                            }
                        }
                    }, delay, BuildConfig.AUTOSYNC_PERIOD);
                }
                Bitmap icon = BitmapFactory.decodeResource(getResources(),
                        R.drawable.emobile_icon);

                String channelId = getString(R.string.notification_channel_id);
                String name = getString(R.string.notification_channel_name);
                String description = getString(R.string.notification_channel_description);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                    NotificationChannel channel = new NotificationChannel(channelId, name, importance);
                    channel.setDescription(description);
                    NotificationManager notificationManager = getSystemService(NotificationManager.class);
                    notificationManager.createNotificationChannel(channel);
                }
                Notification notification = new NotificationCompat.Builder(this,channelId)
                        .setContentTitle("eMobilePOS")
                        .setTicker("eMobilePOS")
                        .setContentText("Synchronizing")
                        .setSmallIcon(R.drawable.emobile_icon_notification)
                        .setLargeIcon(
                                Bitmap.createScaledBitmap(icon, 128, 128, false))
//                    .setContentIntent(pendingIntent)
                        .setOngoing(true).build();

                startForeground(FOREGROUND_SERVICE,
                        notification);
            } else if (intent.getAction().equals(STOP_ACTION)) {
                Log.i(TAG, "Received Stop Foreground Intent");
                stopForeground(true);
                stopSelf();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Destroy polling service");
    }

    void broadcastMessage(String message) {
        Intent intent = new Intent(MAIN_ACTION);
        intent.putExtra("action", message);
        sendBroadcast(intent);
        if (message.equalsIgnoreCase(ONHOLD_BROADCAST_ACTION)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        SynchMethods.synchOrdersOnHoldList(getApplicationContext());
                        Intent i = new Intent(MainMenu_FA.NOTIFICATION_RECEIVED);
                        i.putExtra(MainMenu_FA.NOTIFICATION_MESSAGE, String.valueOf(NotificationEvent.NotificationEventAction.SYNC_HOLDS.getCode()));
                        sendBroadcast(i);
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (KeyManagementException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void pollNotificationEvents(Context context) {
        try {
            if ((preferences.isUse_syncplus_services() && NetworkUtils.isConnectedToLAN(context))
                    || NetworkUtils.isConnectedToInternet(context)) {
                Date tempPollDate = new Date();
                Gson gson = JsonUtils.getInstance();
                String baseUrl;
                String pattern;
                if (preferences.isUse_syncplus_services()) {
                    baseUrl = SyncConfigServerService.getUrl(context.getString(R.string.sync_enablermobile_local_polling), context);
                    pattern = "%spollnotification?RegID=%s&fromdate=%s";
                } else {
                    baseUrl = context.getString(R.string.sync_enablermobile_deviceasxmltrans);
                    pattern = "%spollnotification.ashx?RegID=%s&fromdate=%s";
                }
                String sb = String.format(pattern,
                        baseUrl,
                        URLEncoder.encode(accountNumber, "utf-8"),
                        URLEncoder.encode(DateUtils.getDateAsString(lastPolled), "utf-8"));

                InputStream inputStream = oauthclient.HttpClient.get(sb, null, true);
                JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
                reader.beginArray();

                List<PollNotification> notifications = new ArrayList<>();
                while (reader.hasNext()) {
                    PollNotification notification = gson.fromJson(reader, PollNotification.class);
                    notifications.add(notification);
                }

                for (PollNotification pn : notifications) {
                    String message = NONE_BROADCAST_ACTION;
                    if (pn.isAvailable()) {
                        lastPolled = tempPollDate;
                        if (pn.notificationtype.equalsIgnoreCase("onhold")) {
                            message = ONHOLD_BROADCAST_ACTION;
                        } else if (pn.notificationtype.equalsIgnoreCase("mesas")) {
                            message = MESAS_CONFIG_BROADCAST_ACTION;
                        }
                        broadcastMessage(message);
                    }
                }

                reader.endArray();
                reader.close();
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    public enum PollingServicesFlag {
        ONHOLDS(2), DINING_TABLES(4), AUTO_SYNC(8);

        private int code;

        PollingServicesFlag(int code) {

            this.code = code;
        }

        public final int getCode() {
            return this.code;
        }
    }

    private class PollNotification {
        private String notificationtype;
        private boolean isAvailable;

        public String getNotificationtype() {
            return notificationtype;
        }

        public void setNotificationtype(String notificationtype) {
            this.notificationtype = notificationtype;
        }

        public boolean isAvailable() {
            return isAvailable;
        }

        public void setAvailable(boolean available) {
            isAvailable = available;
        }
    }
}
