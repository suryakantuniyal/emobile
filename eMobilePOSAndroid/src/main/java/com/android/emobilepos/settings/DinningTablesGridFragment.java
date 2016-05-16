package com.android.emobilepos.settings;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.dao.DinningTableDAO;
import com.android.dao.DinningTableOrderDAO;
import com.android.dao.SalesAssociateDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.adapters.DinningTablesAdapter;
import com.android.emobilepos.models.DinningTable;
import com.android.emobilepos.models.DinningTableOrder;
import com.android.emobilepos.models.SalesAssociate;

import java.util.List;

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
        SalesAssociateConfiguration activity = (SalesAssociateConfiguration) getActivity();
        setSalesAssociateInfo(activity.getSelectedSalesAssociate());
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final DinningTable table = (DinningTable) adapterView.getItemAtPosition(i);
        DinningTableOrder tableOrder = DinningTableOrderDAO.getByNumber(table.getNumber());

    }

    public void setSalesAssociateInfo(SalesAssociate selectedSalesAssociate) {
        TextView name = (TextView) getView().findViewById(R.id.salesAssociateNametextView16);
        if (selectedSalesAssociate != null) {
            name.setText(selectedSalesAssociate.toString());
        } else {
            name.setText("");
        }
    }
}
