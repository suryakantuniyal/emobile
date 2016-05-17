package com.android.emobilepos.mainmenu.restaurant;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.android.dao.DinningTableDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.adapters.DinningTablesAdapter;
import com.android.emobilepos.models.DinningTable;
import com.android.emobilepos.ordering.SplittedOrderSummary_FA;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TablesGridFragment extends Fragment {

    public TablesGridFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dlog_ask_table_grid_layout, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final List<DinningTable> dinningTables = DinningTableDAO.getAll();
        GridView gridView = (GridView) view.findViewById(R.id.tablesGridLayout);
        final DinningTablesAdapter adapter = new DinningTablesAdapter(getActivity(), dinningTables);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DinningTable table = (DinningTable) parent.getItemAtPosition(position);
                Intent result = new Intent();
                result.putExtra("tableId", table.getId());
                getActivity().setResult(SplittedOrderSummary_FA.NavigationResult.TABLE_SELECTION.getCode(), result);
                getActivity().finish();
            }
        });
    }
}
