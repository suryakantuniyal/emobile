package com.android.emobilepos.ordering;

import android.os.Bundle;
import android.os.Parcelable;
import android.widget.GridView;

import com.android.emobilepos.R;
import com.android.emobilepos.adapters.SplittedOrderSummaryAdapter;
import com.android.emobilepos.models.SplitedOrder;
import com.android.support.Global;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.util.ArrayList;

/**
 * Created by Guarionex on 2/8/2016.
 */
public class SplittedOrderSummary_FA extends BaseFragmentActivityActionBar {

    private Global global;
    private ArrayList<Parcelable> splittedOrder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splited_order_summary);
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            splittedOrder = extras.getParcelableArrayList("SplittedOrder");
        }
        global = (Global) getApplication();
        GridView gridView = (GridView) findViewById(R.id.splitedOrderSummarygridView);
        SplittedOrderSummaryAdapter summaryAdapter = new SplittedOrderSummaryAdapter(this, new ArrayList<SplitedOrder>());
        gridView.setAdapter(summaryAdapter);
    }
}
