package com.android.emobilepos.settings;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.android.dao.DinningTableDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.adapters.DinningTablesAdapter;
import com.android.emobilepos.models.DinningTable;
import com.android.emobilepos.models.SalesAssociate;

import java.util.List;

import io.realm.Realm;
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
        final List<DinningTable> dinningTables = realmResults;
        gridView = (GridView) view.findViewById(R.id.tablesGridLayout);
        adapter = new DinningTablesAdapter(getActivity(), dinningTables);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
        refreshGrid();
    }
    public void refreshGrid(){
        SalesAssociateConfiguration activity = (SalesAssociateConfiguration) getActivity();
        setSalesAssociateInfo(activity.getSelectedSalesAssociate());
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final DinningTable table = (DinningTable) adapterView.getItemAtPosition(i);
        SalesAssociateConfiguration activity = (SalesAssociateConfiguration) getActivity();
        Realm.getDefaultInstance().beginTransaction();
        boolean contains = activity.getSelectedSalesAssociate().getAssignedDinningTables().contains(table);
        if (contains) {
            activity.getSelectedSalesAssociate().getAssignedDinningTables().remove(table);
        } else {
            activity.getSelectedSalesAssociate().getAssignedDinningTables().add(table);
        }
        Realm.getDefaultInstance().commitTransaction();
        adapter.setSelectedDinningTables(activity.getSelectedSalesAssociate().getAssignedDinningTables());
        adapter.notifyDataSetChanged();
    }

    public void setSalesAssociateInfo(SalesAssociate selectedSalesAssociate) {
        TextView name = (TextView) getView().findViewById(R.id.salesAssociateNametextView16);
        if (selectedSalesAssociate != null) {
            adapter.setSelectedDinningTables(selectedSalesAssociate.getAssignedDinningTables());
            adapter.notifyDataSetChanged();
            name.setText(selectedSalesAssociate.toString());
        } else {
            name.setText("");
        }
    }
}
