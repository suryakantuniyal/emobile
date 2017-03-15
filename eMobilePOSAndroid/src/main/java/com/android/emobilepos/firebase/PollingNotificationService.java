package com.android.emobilepos.firebase;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.emobilepos.BuildConfig;
import com.android.emobilepos.R;
import com.android.emobilepos.mainmenu.MainMenu_FA;
import com.android.support.DateUtils;
import com.android.support.HttpClient;
import com.android.support.MyPreferences;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
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

    public static int FOREGROUND_SERVICE = 101;
    public static final String MAIN_ACTION = "pollingNotificationService.action.main";
    public static final String START_ACTION = "pollingNotificationService.action.startforeground";
    public static final String STOP_ACTION = "pollingNotificationService.action.stopforeground";

    public static final String ONHOLD_BROADCAST_ACTION = "3";
    public static final String MESAS_CONFIG_BROADCAST_ACTION = "6";
    public static final String NONE_BROADCAST_ACTION = "99";

    private static final String TAG = "PollingService";
    private Timer timer;
    private static final int delay = 3000; // delay for 3 sec before first start
    private Date lastPolled;
    private String accountNumber;


    @Override
    public void onCreate() {
        super.onCreate();
        accountNumber = new MyPreferences(this).getAcctNumber();
        lastPolled = new Date(0);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null) {
            if (intent.getAction().equals(START_ACTION)) {
                Log.i(TAG, "Received Start Foreground Intent");

                Intent notificationIntent = new Intent(this, MainMenu_FA.class);
                notificationIntent.setAction(MAIN_ACTION);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                        notificationIntent, 0);

                if (timer == null) {
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Timer");
                            pollNotificationEvents(PollingNotificationService.this);
//                        updateMainActivity("3");
                        }
                    }, delay, BuildConfig.POLLING_PERIOD);
                }

                Bitmap icon = BitmapFactory.decodeResource(getResources(),
                        R.drawable.emobile_icon);

                Notification notification = new NotificationCompat.Builder(this)
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
    }

    public void pollNotificationEvents(Context context) {
        try {
            HttpClient client = new HttpClient();
            Gson gson = JsonUtils.getInstance();

            StringBuilder sb = new StringBuilder(context.getString(R.string.sync_enablermobile_deviceasxmltrans));
            sb.append("pollnotification.ashx?RegID=").append(URLEncoder.encode(accountNumber, "utf-8"));
            sb.append("&fromdate=").append(URLEncoder.encode(DateUtils.getDateAsString(lastPolled), "utf-8"));

            InputStream inputStream = client.httpInputStreamRequest(sb.toString());
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            reader.beginArray();

            List<PollNotification> notifications = new ArrayList();
            while (reader.hasNext()) {
                PollNotification notification = gson.fromJson(reader, PollNotification.class);
                notifications.add(notification);
            }

            for (PollNotification pn : notifications) {
                String message = NONE_BROADCAST_ACTION;
                if (pn.isAvailable()) {
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

            lastPolled = new Date();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class PollNotification {
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
