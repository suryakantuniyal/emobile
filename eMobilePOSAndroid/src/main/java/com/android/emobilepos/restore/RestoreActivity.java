package com.android.emobilepos.restore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.database.DBManager;
import com.android.emobilepos.R;
import com.android.emobilepos.mainmenu.MainMenu_FA;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.support.MyPreferences;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import io.realm.Realm;

public class RestoreActivity extends Activity implements View.OnClickListener {

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore);
        Button startRestoreButton = (Button) findViewById(R.id.startRestoreButton);
        startRestoreButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        try {
            restore();
        } catch (IOException e) {

        }
    }

    private EditText getEditText(int id) {
        return (EditText) findViewById(id);
    }

    private void restore() throws IOException {
        File restoreDBPath = new File(Environment.getExternalStorageDirectory() + "/emobilepos.db");
        File dbTargetTPath = getApplicationContext().getDatabasePath(DBManager.DB_NAME_OLD);
//        File restoreRealmDBPath = new File(Environment.getExternalStorageDirectory() + "/emobilepos.realmdb");
//        File realmTargetPath = new File(Realm.getDefaultConfiguration().getPath());
//        Realm.removeDefaultConfiguration();
//        FileUtils.copyFile(restoreRealmDBPath, realmTargetPath);
        FileUtils.copyFile(restoreDBPath, dbTargetTPath);
//        RealmConfiguration config = new RealmConfiguration.Builder()
//                .migration(new EmobilePOSRealmMigration())
//                .modules(Realm.getDefaultModule(), new RealmModule())
//                .schemaVersion(EmobilePOSRealmMigration.REALM_SCHEMA_VERSION)
//                .build();
//        Realm.setDefaultConfiguration(config);
        DBManager dbManager = new DBManager(this);
        MyPreferences preferences = new MyPreferences(this);
        preferences.setDeviceID(getEditText(R.id.deviceIdRestore).getText().toString());
        preferences.setAcctNumber(getEditText(R.id.accountNumberRestore).getText().toString());
        preferences.setApplicationPassword(getEditText(R.id.applicationPasswordRestore).getText().toString());
        preferences.setAcctPassword(getEditText(R.id.accountPasswordRestore).getText().toString());
        preferences.setActivKey(getEditText(R.id.activationKeyRestore).getText().toString());
        AssignEmployee assignEmployee = new AssignEmployee();//AssignEmployeeDAO.getAssignEmployee(false);
        assignEmployee.setEmpId(Integer.parseInt(getEditText(R.id.employeeIdRestore).getText().toString()));
        realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.where(AssignEmployee.class).findAll().deleteAllFromRealm();
        realm.copyToRealm(assignEmployee);
        realm.commitTransaction();
        dbManager.dbRestore();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
