package in.gtech.gogeotrack.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import org.json.JSONArray;

import in.gtech.gogeotrack.R;
import in.gtech.gogeotrack.api.APIServices;
import in.gtech.gogeotrack.network.ResponseOnlineVehicle;
import in.gtech.gogeotrack.utils.URLContstant;
import io.netopen.hotbitmapgg.library.view.RingProgressBar;

public class CircularActivity extends AppCompatActivity {

    RingProgressBar  mRingProgressBar;
    private int progress = 0;
    private String message = "data loading ";

    String userName,password;
    SharedPreferences sharedPreferences,sharedPrefs,mSharedPreferences;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 0) {
                if (progress < 100) {
                    progress++;

                   allOnlineVehicle();
                    mRingProgressBar.setProgress(progress);

                    mRingProgressBar.setOnProgressListener(new RingProgressBar.OnProgressListener() {

                        @Override
                        public void progressToComplete() {
                            Intent mainactivityintent = new Intent(CircularActivity.this, Main2Activity.class);
                            mainactivityintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainactivityintent);
                            finish();

                        }
                    });
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circular);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = 100 / 100.0f;
        getWindow().setAttributes(lp);

        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, MODE_PRIVATE);
        sharedPrefs = getSharedPreferences("ArrayList", Context.MODE_PRIVATE);
        sharedPreferences = getSharedPreferences("OfflineList", Context.MODE_PRIVATE);
        userName = mSharedPreferences.getString(URLContstant.KEY_USERNAME, "");
        password = mSharedPreferences.getString(URLContstant.KEY_PASSWORD,"");

        mRingProgressBar = (RingProgressBar) findViewById(R.id.progress_bar);

        mRingProgressBar.setProgress(progress);

       /* mRingProgressBar.setOnProgressListener(new RingProgressBar.OnProgressListener()
        {

            @Override
            public void progressToComplete()
            {
                Intent mainactivityintent = new Intent(CircularActivity.this, Main2Activity.class);
                mainactivityintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainactivityintent);
                finish();
                // Progress reaches the maximum callback default Max value is 100

            }
        });*/

        new Thread(new Runnable() {

            @Override
            public void run() {

                for (int i = 0; i < 100; i++) {
                    try {
                        Thread.sleep(300);
                       // allOnlineVehicle();
                        mHandler.sendEmptyMessage(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void allOnlineVehicle() {

        APIServices.GetAllOnlineVehicleList(CircularActivity.this,userName,password, new ResponseOnlineVehicle() {
            @Override
            public void onSuccessOnline(JSONArray result) {
                Log.d("Result", String.valueOf(result));
                SessionHandler.updateSnessionHandler(getBaseContext(), result, mSharedPreferences);
                //chooseBetweenLoginAndMainActivity();
            }
        });

    };
    @Override
    protected void onDestroy() {

        super.onDestroy();

        mHandler.removeCallbacksAndMessages(null);
    }




       /* CircleProgressbar circleProgressbar = (CircleProgressbar)findViewById(R.id.circularprogressbar);
        circleProgressbar.setForegroundProgressColor(Color.GRAY);
        circleProgressbar.setBackgroundProgressWidth(15);
        circleProgressbar.setForegroundProgressWidth(20);
        circleProgressbar.enabledTouch(true);
        circleProgressbar.setRoundedCorner(true);
        circleProgressbar.setClockwise(true);
        int animationDuration = 5500; // 2500ms = 2,5s
        circleProgressbar.setProgressWithAnimation(65, animationDuration); // Default duration = 1500ms*/

}
