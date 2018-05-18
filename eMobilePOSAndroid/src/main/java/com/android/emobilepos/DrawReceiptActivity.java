package com.android.emobilepos;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.emobilepos.payment.ProcessCreditCard_FA;
import com.android.support.DrawView;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.crashlytics.android.Crashlytics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DrawReceiptActivity extends Activity implements OnClickListener {
    private DrawView drawView;
    private LinearLayout layout;
    private Context context;

    private Global global;
    private boolean hasBeenCreated = false;
    private Activity activity;
    private boolean isFromPayment = false;
    private Bundle extras;
    private String cardType;
    private String payAmount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Set full screen view


        context = this;
        activity = this;
        global = (Global) getApplication();
        //drawView = new DrawView(context);
        extras = this.getIntent().getExtras();
        if (extras != null) {
            isFromPayment = extras.getBoolean("isFromPayment");
            cardType = extras.getString("card_type");
            payAmount = extras.getString("pay_amount");
        }

        setContentView(R.layout.sign_receipt_layout);

        layout = (LinearLayout) findViewById(R.id.signatureReceiptView);
        //layout.addView(drawView);

        Button cancel = (Button) findViewById(R.id.cancelBut);
        Button clear = (Button) findViewById(R.id.clearBut);
        clear.setFocusable(false);
        Button accept = (Button) findViewById(R.id.acceptBut);
        cancel.setOnClickListener(this);
        clear.setOnClickListener(this);
        accept.setOnClickListener(this);
        if (!TextUtils.isEmpty(payAmount)) {
            ((TextView) findViewById(R.id.amountSignReceipttextView)).setText(String.format("%s%s", getString(R.string.receipt_amount), Global.getCurrencyFormat(payAmount)));
            if (!TextUtils.isEmpty(cardType)) {
                ((ImageView) findViewById(R.id.cardLogoSignReceiptimageView3)).setImageResource(ProcessCreditCard_FA.getCreditLogo(cardType));
            }
        } else {
            findViewById(R.id.amountSignReceipttextView).setVisibility(View.GONE);
            findViewById(R.id.cardLogoSignReceiptimageView3).setVisibility(View.GONE);
        }

        hasBeenCreated = true;
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        drawView = new DrawView(context, layout.getWidth(), layout.getHeight());
        layout.addView(drawView);
        drawView.requestFocus();
    }


    @Override
    public void onResume() {

        if (global.isApplicationSentToBackground())
            Global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !Global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(activity);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
//        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//        boolean isScreenOn = powerManager.isScreenOn();
//        MyPreferences myPref = new MyPreferences(this);
//        if (!isScreenOn && myPref.isExpireUserSession())
//            Global.loggedIn = false;
        global.startActivityTransitionTimer();
    }


    @Override
    public void onBackPressed() {

        if (isFromPayment)
            setResult(-1);
        else {
            if (extras.getBoolean("inPortrait", false))
                setResult(Global.FROM_DRAW_RECEIPT_PORTRAIT);
            else
                setResult(Global.FROM_DRAW_RECEIPT_LANDSCAPE);
        }
        finish();
        //super.onBackPressed();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancelBut:
                if (isFromPayment)
                    setResult(-1);
                else {
                    if (extras.getBoolean("inPortrait", false))
                        setResult(Global.FROM_DRAW_RECEIPT_PORTRAIT);
                    else
                        setResult(Global.FROM_DRAW_RECEIPT_LANDSCAPE);
                }
                finish();
                break;
            case R.id.clearBut:

                layout.removeView(drawView);
                drawView = new DrawView(context, layout.getWidth(), layout.getHeight());
                layout.addView(drawView);
                drawView.requestFocus();
                break;
            case R.id.acceptBut:
                Bitmap t = drawView.getCanvasBitmap();
                OutputStream outStream;
                MyPreferences myPref = new MyPreferences(activity);

                File file = new File(myPref.getCacheDir(), "test.jpeg");
                File mediaDir = new File(myPref.getCacheDir());


                try {
                    if (!mediaDir.exists())            //validate if directory has already been created
                    {
                        mediaDir.mkdir();
                        file.createNewFile();
                    }
                    outStream = new FileOutputStream(file);
                    t.compress(Bitmap.CompressFormat.JPEG, 100, outStream);

                    outStream.flush();
                    outStream.close();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    //t.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    Bitmap temp = Bitmap.createScaledBitmap(t, t.getWidth(), t.getHeight(), false);
                    temp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] b = baos.toByteArray();
                    this.global.encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

                    if (isFromPayment)
                        setResult(-1);
                    else {
                        if (extras.getBoolean("inPortrait"))
                            setResult(Global.FROM_DRAW_RECEIPT_PORTRAIT);
                        else
                            setResult(Global.FROM_DRAW_RECEIPT_LANDSCAPE);
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                } catch (IOException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
                finish();
                break;
        }
    }
}