package com.android.emobilepos.payment;

import android.app.Activity;
import android.os.Bundle;

import com.android.emobilepos.R;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

public class TipAdjustmentFA extends BaseFragmentActivityActionBar {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tip_adjustment_fa);
    }
}
