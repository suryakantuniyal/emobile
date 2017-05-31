package com.android.emobilepos.initialization;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.database.DBManager;
import com.android.emobilepos.R;
import com.android.emobilepos.mainmenu.MainMenu_FA;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.SynchMethods;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

public class SelectPassword_FA extends BaseFragmentActivityActionBar {
    private Activity activity;
    private MyPreferences myPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
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
                    new SyncReceiveTask().execute(dbManager);
//                    SynchMethods sm = new SynchMethods(dbManager);
//                    sm.synchReceive(Global.FROM_REGISTRATION_ACTIVITY, activity);
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

    public class SyncReceiveTask extends AsyncTask<DBManager, Void, Boolean> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(SelectPassword_FA.this);
            dialog.setTitle(R.string.sync_title);
            dialog.setIndeterminate(true);
            dialog.setMessage(getString(R.string.sync_inprogress));
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(DBManager... params) {
            DBManager dbManager = params[0];
            SynchMethods sm = new SynchMethods(dbManager);
            return sm.syncReceive();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            boolean isDestroyed = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (activity.isDestroyed()) {
                    isDestroyed = true;
                }
            }
            if (!activity.isFinishing() && !isDestroyed && dialog.isShowing()) {
                dialog.dismiss();
            }

            if (!result) {
                Global.showPrompt(SelectPassword_FA.this, R.string.sync_title, getString(R.string.sync_fail));
            } else {
                Intent intent = new Intent(SelectPassword_FA.this, MainMenu_FA.class);
                activity.setResult(-1);
                startActivity(intent);
                activity.finish();
            }
        }
    }
}
