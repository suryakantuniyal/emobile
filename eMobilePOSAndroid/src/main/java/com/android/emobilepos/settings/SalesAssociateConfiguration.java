package com.android.emobilepos.settings;

import android.os.Bundle;
import android.os.PowerManager;

import com.android.emobilepos.R;
import com.android.emobilepos.models.SalesAssociate;
import com.android.support.Global;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

public class SalesAssociateConfiguration extends BaseFragmentActivityActionBar {

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
}
