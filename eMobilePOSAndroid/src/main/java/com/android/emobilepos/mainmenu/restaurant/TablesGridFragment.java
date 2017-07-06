package com.android.emobilepos.mainmenu.restaurant;


import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupMenu;

import com.android.dao.DinningTableOrderDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.adapters.DinningTablesAdapter;
import com.android.emobilepos.models.realms.DinningTable;
import com.android.emobilepos.models.realms.DinningTableOrder;
import com.android.emobilepos.ordering.SplittedOrderSummary_FA;
import com.android.support.Global;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TablesGridFragment extends Fragment implements AdapterView.OnItemLongClickListener {

    private DinningTablesAdapter adapter;

    public TablesGridFragment() {
    }

    private DinningTablesActivity getDinningTablesActivity() {
        return (DinningTablesActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dlog_ask_table_grid_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GridView gridView = (GridView) view.findViewById(R.id.tablesGridLayout);
        List<DinningTable> dinningTables = getDinningTablesActivity().dinningTables;
        adapter = new DinningTablesAdapter(getActivity(), dinningTables);

        if (getDinningTablesActivity().associate != null) {
            adapter.setSelectedDinningTables(getDinningTablesActivity().associate.getAssignedDinningTables());
        }
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DinningTable table = (DinningTable) parent.getItemAtPosition(position);
                if (getDinningTablesActivity().associate != null && getDinningTablesActivity().associate.getAssignedDinningTables().contains(table)) {
                    DinningTableOrder tableOrder = DinningTableOrderDAO.getByNumber(table.getNumber());
                    if (tableOrder != null) {
                        getDinningTablesActivity().new OpenOnHoldOrderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR
                                , tableOrder, table);
                    } else {
                        Intent result = new Intent();
                        result.putExtra("tableId", table.getId());
                        getActivity().setResult(SplittedOrderSummary_FA.NavigationResult.TABLE_SELECTION.getCode(), result);
                        getActivity().finish();
                    }
                } else {
                    Global.showPrompt(getActivity(), R.string.title_activity_dinning_tables, getActivity().getString(R.string.dinningtablenotassigned));

                }
            }
        });
        gridView.setOnItemLongClickListener(this);
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> adapterView, View view, int i, long l) {
        final DinningTable table = (DinningTable) adapterView.getItemAtPosition(i);
        DinningTableOrder tableOrder = DinningTableOrderDAO.getByNumber(table.getNumber());
        if (tableOrder != null) {
            PopupMenu popup = new PopupMenu(getActivity(), view);
            popup.getMenuInflater().inflate(R.menu.dinning_table_map_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    DinningTableOrderDAO.deleteByNumber(table.getNumber());
                    DinningTablesActivity activity = (DinningTablesActivity) getActivity();
                    adapter.notifyDataSetChanged();
                    activity.refresh(1);
                    return true;
                }
            });
            popup.show();
        }
        return true;
    }
}
