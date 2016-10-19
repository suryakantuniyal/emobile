package com.android.emobilepos.settings;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;

import com.android.dao.SalesAssociateDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.models.realms.SalesAssociate;
import com.android.support.Global;
import com.android.support.SynchMethods;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.util.List;

import io.realm.Realm;

public class SalesAssociateConfigurationActivity extends BaseFragmentActivityActionBar {

    private SalesAssociate selectedSalesAssociate;
    private boolean hasBeenCreated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_associate_configuration);
        hasBeenCreated = true;
    }

    protected DinningTablesGridFragment getDinningTablesGridFragment() {
        return (DinningTablesGridFragment) getFragmentManager().findFragmentById(R.id.dinningTablesGridfragment);
    }

    protected SalesAssociateListFragment getSalesAssociateListFragment() {
        return (SalesAssociateListFragment) getFragmentManager().findFragmentById(R.id.associateListfragment);
    }

    public void setSelectedSalesAssociate(SalesAssociate selectedSalesAssociate) {
        this.selectedSalesAssociate = selectedSalesAssociate;
    }

    public SalesAssociate getSelectedSalesAssociate() {
        return selectedSalesAssociate;
    }

    @Override
    public void onResume() {
        Global global = (Global) getApplication();
        if (global.isApplicationSentToBackground(this))
            global.loggedIn = false;

        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(this);
        }
        super.onResume();
    }


    @Override
    public void onPause() {
        Global global = (Global) getApplication();
        super.onPause();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        if (!isScreenOn)
            global.loggedIn = false;
        global.startActivityTransitionTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        new SaveConfigurationTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class SaveConfigurationTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(SalesAssociateConfigurationActivity.this);
            progressDialog.setMessage(getString(R.string.sync_saving_settings));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Realm realm = Realm.getDefaultInstance();
            List<SalesAssociate> assosiates = SalesAssociateDAO.getAll();// realm.where(SalesAssociate.class).findAll();
            try {
                SynchMethods.postSalesAssociatesConfiguration(SalesAssociateConfigurationActivity.this, realm.copyFromRealm(assosiates));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            finish();
        }
    }

}
