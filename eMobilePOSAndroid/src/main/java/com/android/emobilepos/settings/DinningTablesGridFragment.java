package com.android.emobilepos.settings;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.android.dao.ClerkDAO;
import com.android.dao.DinningTableDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.adapters.DinningTablesAdapter;
import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.models.realms.DinningTable;

import io.realm.RealmResults;

/**
 * A placeholder fragment containing a simple view.
 */
public class DinningTablesGridFragment extends Fragment implements AdapterView.OnItemClickListener {


    private GridView gridView;
    private DinningTablesAdapter adapter;

    public DinningTablesGridFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.salesassociates_dinningtable_selection_grid_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RealmResults<DinningTable> realmResults = DinningTableDAO.getAll();
        realmResults.sort("number");
        gridView = (GridView) view.findViewById(R.id.tablesGridLayout);
        adapter = new DinningTablesAdapter(getActivity(), realmResults);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
        refreshGrid();
    }

    public void refreshGrid() {
        SalesAssociateConfigurationActivity activity = (SalesAssociateConfigurationActivity) getActivity();
        setSalesAssociateInfo(activity.getSelectedClerk());
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final DinningTable table = (DinningTable) adapterView.getItemAtPosition(i);
        SalesAssociateConfigurationActivity activity = (SalesAssociateConfigurationActivity) getActivity();
        boolean contains = activity.getSelectedClerk().getAssignedDinningTables().contains(table);
        if (contains) {
            ClerkDAO.removeAssignedTable(activity.getSelectedClerk(), table);
        } else {
            ClerkDAO.addAssignedTable(activity.getSelectedClerk(), table);
        }
        activity.setSelectedClerk(ClerkDAO.getByEmpId(activity.getSelectedClerk().getEmpId(), false));
        adapter.setSelectedDinningTables(activity.getSelectedClerk().getAssignedDinningTables());
        adapter.notifyDataSetChanged();
    }

    public void setSalesAssociateInfo(Clerk selectedClerk) {
        TextView name = (TextView) getView().findViewById(R.id.salesAssociateNametextView16);
        if (selectedClerk != null) {
            adapter.setSelectedDinningTables(selectedClerk.getAssignedDinningTables());
            adapter.notifyDataSetChanged();
            name.setText(selectedClerk.toString());
        } else {
            name.setText("");
        }
    }
}
