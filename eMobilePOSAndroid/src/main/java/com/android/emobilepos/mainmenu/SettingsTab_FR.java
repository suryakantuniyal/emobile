package com.android.emobilepos.mainmenu;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.emobilepos.R;
import com.android.emobilepos.security.SecurityManager;
import com.android.emobilepos.settings.SettingListActivity;
import com.android.support.MyPreferences;

public class SettingsTab_FR extends Fragment implements OnClickListener {

    public final static int CASE_ADMIN = 0, CASE_MANAGER = 1, CASE_GENERAL = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_layout, container, false);
        boolean hasPermissions = SecurityManager.hasPermissions(getActivity(), SecurityManager.SecurityAction.SYSTEM_SETTINGS);
        Button btnAdmin = (Button) view.findViewById(R.id.btnAdminSetting);
        Button btnManager = (Button) view.findViewById(R.id.btnManagerSetting);
        Button btnGeneral = (Button) view.findViewById(R.id.btnGeneralSetting);
        btnAdmin.setOnClickListener(this);
        btnManager.setOnClickListener(this);
        btnGeneral.setOnClickListener(this);
        btnAdmin.setEnabled(hasPermissions);
        btnManager.setEnabled(hasPermissions);
        btnGeneral.setEnabled(hasPermissions);
        if (!hasPermissions) {
            Toast.makeText(getActivity(), R.string.permission_denied, Toast.LENGTH_LONG);
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAdminSetting:
                promptPassword(CASE_ADMIN);
                break;
            case R.id.btnManagerSetting:
                promptPassword(CASE_MANAGER);
                break;
            case R.id.btnGeneralSetting:
                Intent intent = new Intent(getActivity(), SettingListActivity.class);
                intent.putExtra("settings_type", CASE_GENERAL);
                startActivity(intent);
                break;
        }

    }

    private void promptPassword(final int type) {


        final Dialog globalDlog = new Dialog(getActivity(), R.style.Theme_TransparentTest);
        final MyPreferences myPref = new MyPreferences(getActivity());
        globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        globalDlog.setCancelable(true);
        globalDlog.setCanceledOnTouchOutside(true);
        globalDlog.setContentView(R.layout.dlog_field_single_layout);

        final EditText viewField = (EditText) globalDlog.findViewById(R.id.dlogFieldSingle);
        viewField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        TextView viewTitle = (TextView) globalDlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) globalDlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        if (type == CASE_ADMIN)
            viewMsg.setText(R.string.dlog_title_enter_admin_password);
        else
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
                if (!pass.isEmpty()) {
                    Intent intent = new Intent(getActivity(), SettingListActivity.class);
                    if (type == CASE_ADMIN && myPref.getPOSAdminPass().equals(pass.trim())) {
                        intent.putExtra("settings_type", CASE_ADMIN);
                        startActivity(intent);

                    } else if (type == CASE_MANAGER && myPref.getPosManagerPass().equals(pass.trim())) {
                        intent.putExtra("settings_type", CASE_MANAGER);
                        startActivity(intent);
                    } else {
                        promptPassword(type);
                    }
                }
            }
        });
        globalDlog.show();
    }

}
