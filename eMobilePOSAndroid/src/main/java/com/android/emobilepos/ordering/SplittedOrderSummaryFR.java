package com.android.emobilepos.ordering;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.android.emobilepos.R;
import com.android.emobilepos.adapters.SplittedOrderSummaryAdapter;
import com.android.emobilepos.models.SplitedOrder;

/**
 * Created by Guarionex on 2/19/2016.
 */
public class SplittedOrderSummaryFR extends Fragment implements AdapterView.OnItemClickListener {

    private GridView gridView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.splitted_order_summary_fragment,
                container, false);
        setGridView((GridView) view.findViewById(R.id.splitedOrderSummarygridView));
        getGridView().setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public GridView getGridView() {
        return gridView;
    }

    public void setGridView(GridView gridView) {
        this.gridView = gridView;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SplitedOrder splitedOrder = (SplitedOrder) gridView.getItemAtPosition(position);
        SplittedOrderSummaryAdapter adapter = (SplittedOrderSummaryAdapter) gridView.getAdapter();
        adapter.setSelectedIndex(position);
        adapter.notifyDataSetChanged();
    }
}
