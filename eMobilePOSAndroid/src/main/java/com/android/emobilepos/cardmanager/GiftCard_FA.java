package com.android.emobilepos.cardmanager;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.emobilepos.R;
import com.android.emobilepos.adapters.GiftLoyaltyRewardLV_Adapter;
import com.android.emobilepos.adapters.GiftLoyaltyRewardLV_Adapter.ViewHolder;
import com.android.emobilepos.security.SecurityManager;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

public class GiftCard_FA extends BaseFragmentActivityActionBar implements OnItemClickListener {


    private boolean hasBeenCreated = false;
    private Global global;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gift_loyal_reward_main_layout);

        global = (Global) getApplication();
        TextView headerTitle = (TextView) findViewById(R.id.HeaderTitle);
        headerTitle.setText(getString(R.string.header_title_gift_card));

        ListView lView = (ListView) findViewById(R.id.listView);
        GiftLoyaltyRewardLV_Adapter adapter = new GiftLoyaltyRewardLV_Adapter(this, 0);
        lView.setAdapter(adapter);
        lView.setOnItemClickListener(this);
        hasBeenCreated = true;
    }


    @Override
    public void onResume() {

        if (global.isApplicationSentToBackground(this))
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
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        if (!isScreenOn)
            Global.loggedIn = false;
        global.startActivityTransitionTimer();
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long arg3) {

        Intent intent = new Intent(this, CardManager_FA.class);
        intent.putExtra("CARD_TYPE", CardManager_FA.CASE_GIFT);
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        switch (viewHolder.giftCardActions) {
            case CASE_ACTIVATE:
                intent.putExtra("PROCESS_TYPE", CardManager_FA.GiftCardActions.CASE_ACTIVATE.getCode());
                startActivity(intent);
                break;
            case CASE_ADD_BALANCE:
                intent.putExtra("PROCESS_TYPE", CardManager_FA.GiftCardActions.CASE_ADD_BALANCE.getCode());
                startActivity(intent);
                break;
            case CASE_BALANCE_INQUIRY:
                intent.putExtra("PROCESS_TYPE", CardManager_FA.GiftCardActions.CASE_BALANCE_INQUIRY.getCode());
                startActivity(intent);
                break;
            case CASE_MANUAL_ADD:
                boolean hasPermissions = SecurityManager.hasPermissions(this, SecurityManager.SecurityAction.MANUAL_ADD_BALANCE_LOYALTY);
                if (hasPermissions) {
                    intent.putExtra("PROCESS_TYPE", CardManager_FA.GiftCardActions.CASE_MANUAL_ADD.getCode());
                    promptManagerPassword(intent);
                } else {
                    Global.showPrompt(this, R.string.security_alert, getString(R.string.permission_denied));
                }
                break;
        }

    }


    private void promptManagerPassword(final Intent intent) {
        final Dialog globalDlog = new Dialog(this, R.style.Theme_TransparentTest);
        globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        globalDlog.setCancelable(true);
        globalDlog.setContentView(R.layout.dlog_field_single_layout);


        final MyPreferences myPref = new MyPreferences(this);
        final EditText viewField = (EditText) globalDlog.findViewById(R.id.dlogFieldSingle);
        viewField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        TextView viewTitle = (TextView) globalDlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) globalDlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);

        viewMsg.setText(R.string.dlog_title_enter_manager_password);
        Button btnCancel = (Button) globalDlog.findViewById(R.id.btnCancelDlogSingle);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globalDlog.dismiss();
            }
        });
        Button btnOk = (Button) globalDlog.findViewById(R.id.btnDlogSingle);
        btnOk.setText(R.string.button_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                globalDlog.dismiss();
                String pass = viewField.getText().toString();
                if (!pass.isEmpty() && myPref.loginManager(pass.trim())) {
                    startActivity(intent);
                } else {
                    promptManagerPassword(intent);
                }
            }
        });
        globalDlog.show();
    }


}
