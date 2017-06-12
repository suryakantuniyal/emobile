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

import com.android.emobilepos.R;
import com.android.emobilepos.security.SecurityManager;
import com.android.emobilepos.settings.SettingListActivity;
import com.android.support.Global;
import com.android.support.MyPreferences;

public class SettingsTab_FR extends Fragment implements OnClickListener {
    public enum SettingsRoles {
        ADMIN, MANAGER, GENERAL
    }

    MyPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_layout, container, false);
//        boolean hasAdminPermissions = SecurityManager.hasPermissions(getActivity(), SecurityManager.SecurityAction.SYSTEM_SETTINGS);
//        boolean hasManagerPermissions = SecurityManager.hasPermissions(getActivity(), SecurityManager.SecurityAction.MANAGE_SHIFT);
        Button btnAdmin = (Button) view.findViewById(R.id.btnAdminSetting);
        Button btnManager = (Button) view.findViewById(R.id.btnManagerSetting);
        Button btnGeneral = (Button) view.findViewById(R.id.btnGeneralSetting);
        btnAdmin.setOnClickListener(this);
        btnManager.setOnClickListener(this);
        btnGeneral.setOnClickListener(this);
//        btnAdmin.setEnabled(hasAdminPermissions);
//        btnManager.setEnabled(hasManagerPermissions);
        preferences = new MyPreferences(getActivity());
        return view;
    }

    @Override
    public void onClick(View v) {
        boolean hasAdminPermissions = SecurityManager.hasPermissions(getActivity(), SecurityManager.SecurityAction.SYSTEM_SETTINGS);
        boolean hasManagerPermissions = SecurityManager.hasPermissions(getActivity(), SecurityManager.SecurityAction.MANAGE_SHIFT);
        switch (v.getId()) {
            case R.id.btnAdminSetting:
                if (preferences.isUseClerks()) {
                    if (hasAdminPermissions) {
                        openSettings(SettingsRoles.ADMIN);
                    } else {
                        Global.showPrompt(getActivity(), R.string.security_alert, getString(R.string.permission_denied));
                    }
                } else {
                    promptPassword(SettingsRoles.ADMIN);
                }
                break;
            case R.id.btnManagerSetting:
                if (preferences.isUseClerks()) {
                    if (hasManagerPermissions) {
                        openSettings(SettingsRoles.MANAGER);
                    } else {
                        Global.showPrompt(getActivity(), R.string.security_alert, getString(R.string.permission_denied));
                    }
                } else {
                    promptPassword(SettingsRoles.MANAGER);
                }
                break;
            case R.id.btnGeneralSetting:
                openSettings(SettingsRoles.GENERAL);
//                Intent intent = new Intent(getActivity(), SettingListActivity.class);
//                intent.putExtra("settings_type", SettingsRoles.GENERAL);
//                startActivity(intent);
                break;
        }

    }

    private void promptPassword(final SettingsRoles role) {
        final Dialog globalDlog = new Dialog(getActivity(), R.style.Theme_TransparentTest);
        globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        globalDlog.setCancelable(true);
        globalDlog.setCanceledOnTouchOutside(true);
        globalDlog.setContentView(R.layout.dlog_field_single_layout);

        final EditText viewField = (EditText) globalDlog.findViewById(R.id.dlogFieldSingle);
        viewField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        TextView viewTitle = (TextView) globalDlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) globalDlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        if (role == SettingsRoles.ADMIN)
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
                    if (role == SettingsRoles.ADMIN && preferences.loginAdmin(pass.trim())) {
                        openSettings(role);
                    } else if (role == SettingsRoles.MANAGER && preferences.loginManager(pass.trim())) {
                        openSettings(role);
                    } else {
                        promptPassword(role);
                    }
                }
            }
        });
        globalDlog.show();
    }

    private void openSettings(SettingsRoles role) {
        Intent intent = new Intent(getActivity(), SettingListActivity.class);
        intent.putExtra("settings_type", role);
        startActivity(intent);
    }
}
