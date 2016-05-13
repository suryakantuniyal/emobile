package com.android.emobilepos.settings;

import android.os.Bundle;
import android.app.Activity;

import com.android.emobilepos.R;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

public class SalesAssociateConfiguration extends BaseFragmentActivityActionBar {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_associate_configuration);
    }

}
