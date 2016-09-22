package com.android.emobilepos.mainmenu.restaurant;


import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupMenu;

import com.android.dao.DinningTableDAO;
import com.android.dao.DinningTableOrderDAO;
import com.android.dao.SalesAssociateDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.adapters.DinningTablesAdapter;
import com.android.emobilepos.models.DinningTable;
import com.android.emobilepos.models.DinningTableOrder;
import com.android.emobilepos.models.SalesAssociate;
import com.android.emobilepos.ordering.SplittedOrderSummary_FA;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class TablesGridFragment extends Fragment implements AdapterView.OnItemLongClickListener {

    private GridView gridView;
    private DinningTablesAdapter adapter;
    private SalesAssociate associate;

    public TablesGridFragment() {
    }
    private DinningTablesActivity getDinningTablesActivity() {
        return (DinningTablesActivity) getActivity();
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
        RealmResults<DinningTable> realmResults = DinningTableDAO.getAll();
        realmResults.sort("number");
        final List<DinningTable> dinningTables = realmResults;
        gridView = (GridView) view.findViewById(R.id.tablesGridLayout);
        adapter = new DinningTablesAdapter(getActivity(), dinningTables);
        MyPreferences preferences = new MyPreferences(getActivity());
        if (!TextUtils.isEmpty(getDinningTablesActivity().associateId)) {
            associate = SalesAssociateDAO.getByEmpId(Integer.parseInt(getDinningTablesActivity().associateId));
        }
        if (associate != null) {
            adapter.setSelectedDinningTables(associate.getAssignedDinningTables());
        }
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DinningTable table = (DinningTable) parent.getItemAtPosition(position);
                if (associate != null && associate.getAssignedDinningTables().contains(table)) {
                    DinningTableOrder tableOrder = DinningTableOrderDAO.getByNumber(table.getNumber());
                    if (tableOrder != null) {
                        Realm realm = Realm.getDefaultInstance();
                        getDinningTablesActivity().new OpenOnHoldOrderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR
                                , realm.copyFromRealm(tableOrder), realm.copyFromRealm(table));
                    } else {
                        Intent result = new Intent();
                        result.putExtra("tableId", table.getId());
                        getActivity().setResult(SplittedOrderSummary_FA.NavigationResult.TABLE_SELECTION.getCode(), result);
                        getActivity().finish();
                    }

//                    DinningTableOrder tableOrder = DinningTableOrderDAO.getByNumber(table.getNumber());
//                    if (tableOrder == null) {
//                        Intent result = new Intent();
//                        result.putExtra("tableId", table.getId());
//                        getActivity().setResult(SplittedOrderSummary_FA.NavigationResult.TABLE_SELECTION.getCode(), result);
//                        getActivity().finish();
//                    }
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
