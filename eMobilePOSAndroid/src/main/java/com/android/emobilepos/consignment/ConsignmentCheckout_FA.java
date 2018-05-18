package com.android.emobilepos.consignment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.android.database.OrdersHandler;
import com.android.emobilepos.R;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

public class ConsignmentCheckout_FA extends BaseFragmentActivityActionBar {
    private Global global;
    private boolean hasBeenCreated = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getIntent().getExtras();
        Global.OrderType consignmentType = (Global.OrderType) extras.get("consignmentType");
        if (consignmentType == Global.OrderType.CONSIGNMENT_FILLUP)
            setContentView(R.layout.consign_fragment_container);
        else
            setContentView(R.layout.consign_pickup_fragment_container);

        global = (Global) getApplication();
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
            global.promptForMandatoryLogin(this);
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
        showExitDlog();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) {
            ConsignmentVisit_FR fragment = (ConsignmentVisit_FR) getSupportFragmentManager().findFragmentByTag("Consign");
            if (fragment != null) {
                fragment.notifyListViewChange();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }


    private void showExitDlog() {
        final Dialog dlog = new Dialog(this, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setCanceledOnTouchOutside(false);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);


        Button btnLeft = (Button) dlog.findViewById(R.id.btnDlogLeft);
        Button btnRight = (Button) dlog.findViewById(R.id.btnDlogRight);
        dlog.findViewById(R.id.btnDlogRight).setVisibility(View.GONE);
        viewTitle.setText(R.string.warning_title);
        viewMsg.setText(R.string.warning_exit_now);
        btnLeft.setText(R.string.button_yes);
        btnRight.setText(R.string.button_no);

        btnLeft.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                if (Global.consignment_order != null && !Global.consignment_order.ord_id.isEmpty()) {
                    OrdersHandler.deleteTransaction(ConsignmentCheckout_FA.this, Global.consignment_order.ord_id);
                }
                if (Global.cons_return_order != null && !Global.cons_return_order.ord_id.isEmpty()) {
                    OrdersHandler.deleteTransaction(ConsignmentCheckout_FA.this, Global.cons_return_order.ord_id);
                }
                if (Global.cons_fillup_order != null && !Global.cons_fillup_order.ord_id.isEmpty()) {
                    OrdersHandler.deleteTransaction(ConsignmentCheckout_FA.this, Global.cons_fillup_order.ord_id);
                }
                if (Global.consignment_order != null && !Global.consignment_order.ord_id.isEmpty()) {
                    OrdersHandler.deleteTransaction(ConsignmentCheckout_FA.this, Global.consignment_order.ord_id);
                }
                finish();
            }
        });
        btnRight.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
            }
        });
        dlog.show();
    }

}
