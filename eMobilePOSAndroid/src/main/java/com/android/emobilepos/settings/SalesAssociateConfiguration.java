package com.android.emobilepos.settings;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;

import com.android.emobilepos.R;
import com.android.emobilepos.models.SalesAssociate;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

public class SalesAssociateConfiguration extends BaseFragmentActivityActionBar {

    private SalesAssociate selectedSalesAssociate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_associate_configuration);
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
}
