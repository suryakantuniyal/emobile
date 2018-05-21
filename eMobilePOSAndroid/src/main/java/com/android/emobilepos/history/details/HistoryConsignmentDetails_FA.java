package com.android.emobilepos.history.details;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.database.ConsignmentTransactionHandler;
import com.android.emobilepos.R;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.util.HashMap;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class HistoryConsignmentDetails_FA extends BaseFragmentActivityActionBar implements OnClickListener {

    private StickyListHeadersListView lView;
    private ConsignmentDetailsLV_Adapter adapter;
    private Cursor c;
    private HashMap<String, String> dataMap;
    private View lvHeaderView;
    private Bundle extras;
    private ProgressDialog myProgressDialog;
    private Activity activity;
    private boolean hasBeenCreated = false;
    private Global global;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consignment_details_layout);

        extras = this.getIntent().getExtras();
        activity = this;
        global = (Global) getApplication();
        lView = findViewById(R.id.consignmentDetailsListView);
        Button printButton = findViewById(R.id.printButton);
        printButton.setOnClickListener(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        lvHeaderView = inflater.inflate(R.layout.orddetails_lvheader_adapter, null);


        ConsignmentTransactionHandler handler = new ConsignmentTransactionHandler(this);
        c = handler.getConsignmentItemDetails(extras.getString("ConsTrans_ID"));
        dataMap = handler.getConsignmentSummaryDetails(extras.getString("ConsTrans_ID"), extras.getBoolean("isPickup"));

        setupListViewHeader();

        adapter = new ConsignmentDetailsLV_Adapter(this, c, dataMap, extras.getBoolean("isPickup"));
        lView.setAreHeadersSticky(false);
        lView.setAdapter(adapter);
        hasBeenCreated = true;
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

    private void setupListViewHeader() {

        TextView custName = lvHeaderView.findViewById(R.id.ordLVHeaderTitle);
        TextView consignDate = lvHeaderView.findViewById(R.id.ordLVHeaderSubtitle);
        ImageView consignSignature = lvHeaderView.findViewById(R.id.ordTicketImg);

        String encodedImg = dataMap.get("encoded_signature");
        dataMap.put("cust_name", extras.getString("cust_name"));
        if (!encodedImg.isEmpty()) {
            Resources resources = getResources();
            Drawable[] layers = new Drawable[2];
            layers[0] = resources.getDrawable(R.drawable.torn_paper);
            byte[] img = Base64.decode(encodedImg, Base64.DEFAULT);
            layers[1] = new BitmapDrawable(resources, BitmapFactory.decodeByteArray(img, 0, img.length));
            LayerDrawable layered = new LayerDrawable(layers);
            layered.setLayerInset(1, 100, 30, 50, 60);
            consignSignature.setImageDrawable(layered);
        }

        custName.setText(extras.getString("cust_name"));

        consignDate.setText(Global.formatToDisplayDate(extras.getString("Cons_timecreated"),  3));

        lView.addHeaderView(lvHeaderView);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.printButton:
                new printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
        }
    }


    private class printAsync extends AsyncTask<Void, Void, Void> {
        boolean printSuccessful = true;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Printing...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            if (myProgressDialog.isShowing())
                myProgressDialog.dismiss();
            myProgressDialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
                printSuccessful = Global.mainPrinterManager.getCurrentDevice().printConsignmentHistory(dataMap, c, extras.getBoolean("isPickup"));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            myProgressDialog.dismiss();
            if (!printSuccessful)
                showPrintDlg();

        }
    }


    private void showPrintDlg() {
        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = dlog.findViewById(R.id.dlogMessage);


        viewTitle.setText(R.string.dlog_title_error);
        viewMsg.setText(R.string.dlog_msg_failed_print);

        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);
        Button btnYes = dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = dlog.findViewById(R.id.btnDlogRight);
        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);

        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                new printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
            }
        });
        dlog.show();
    }
}
