package com.android.emobilepos.initialization;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.emobilepos.R;
import com.android.database.DBManager;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

public class SelectPassword_FA extends BaseFragmentActivityActionBar {
    private Activity activity;
    private MyPreferences myPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.initialization_register_password);


        final EditText password1 = (EditText) findViewById(R.id.regPassword1);
        password1.setTransformationMethod(PasswordTransformationMethod.getInstance());
        final EditText password2 = (EditText) findViewById(R.id.regPassword2);
        password2.setTransformationMethod(PasswordTransformationMethod.getInstance());

        Button submit = (Button) findViewById(R.id.setPasswordButton);
        activity = this;
        myPref = new MyPreferences(this);

        submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String pass1 = password1.getText().toString().trim();
                String pass2 = password2.getText().toString().trim();
                if (pass1.equals(pass2) && pass1.length() >= 5) {
                    myPref.setApplicationPassword(pass1);
                    DBManager dbManager = new DBManager(activity, Global.FROM_REGISTRATION_ACTIVITY);
                    dbManager.updateDB();
                    myPref.setCacheDir(activity.getApplicationContext().getCacheDir().getAbsolutePath());
                } else {
                    Toast.makeText(activity, R.string.wrong_password, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {

            setResult(-1);
            finish();
        }
    }
}
