package com.android.emobilepos.settings;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.dao.SalesAssociateDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.adapters.SalesAssociateListAdapter;
import com.android.emobilepos.models.SalesAssociate;

import io.realm.RealmResults;

/**
 * A placeholder fragment containing a simple view.
 */
public class SalesAssociateListFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ListView list;
    RealmResults<SalesAssociate> associates = SalesAssociateDAO.getAll();
    private SalesAssociateListAdapter adapter;

    public SalesAssociateListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sales_associate_configuration, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = (ListView) view.findViewById(R.id.salesAssociatelistView);
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
//                R.layout.simple_list_item_1, android.R.id.text1, getListValues());
        list.setOnItemClickListener(this);
        adapter = new SalesAssociateListAdapter(getActivity());
        list.setAdapter(adapter);

        if (!associates.isEmpty()) {
            SalesAssociateConfiguration activity = (SalesAssociateConfiguration) getActivity();
            activity.setSelectedSalesAssociate(associates.get(0));
        }
    }

    private String[] getListValues() {
        String[] vals = new String[associates.size()];
        int i = 0;
        for (SalesAssociate associate : associates) {
            vals[i] = String.format("%s (%s)", associate.getEmp_name(), String.valueOf(associate.getEmp_id()));
            i++;
        }
        return vals;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//        view.setSelected(true);
        adapter.selectedIdx = i;
        SalesAssociate associate = SalesAssociateDAO.getByEmpId(associates.get(i).getEmp_id());
        SalesAssociateConfiguration activity = (SalesAssociateConfiguration) getActivity();
        activity.setSelectedSalesAssociate(associate);
        activity.getDinningTablesGridFragment().setSalesAssociateInfo(associate);
        adapter.notifyDataSetChanged();
    }
}
