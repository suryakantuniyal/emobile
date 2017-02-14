package com.android.emobilepos.settings;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.dao.SalesAssociateDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.adapters.SalesAssociateListAdapter;
import com.android.emobilepos.models.realms.SalesAssociate;
import com.android.support.SynchMethods;

import org.xml.sax.SAXException;

import java.io.IOException;

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

        list.setOnItemClickListener(this);
        adapter = new SalesAssociateListAdapter(getActivity());
        list.setAdapter(adapter);
        if (associates != null && !associates.isEmpty()) {
            SalesAssociateConfigurationActivity activity = (SalesAssociateConfigurationActivity) getActivity();
            activity.setSelectedSalesAssociate(associates.get(0));
        }
        new SynchDinnindTablesConfiguration().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private String[] getListValues() {
        String[] vals = new String[associates.size()];
        int i = 0;
        for (SalesAssociate associate : associates) {
            vals[i] = String.format("%s (%s)", associate.getEmpName(), String.valueOf(associate.getEmpId()));
            i++;
        }
        return vals;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        reloadGrid(i);
    }

    private void reloadGrid(int i) {
        adapter.selectedIdx = i;
        if (associates != null && !associates.isEmpty()) {
            SalesAssociate associate = SalesAssociateDAO.getByEmpId(associates.get(i).getEmpId());
            SalesAssociateConfigurationActivity activity = (SalesAssociateConfigurationActivity) getActivity();
            activity.setSelectedSalesAssociate(associate);
            activity.getDinningTablesGridFragment().refreshGrid();
            adapter.notifyDataSetChanged();
        }
    }

    private class SynchDinnindTablesConfiguration extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.sync_dload_settings));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                SynchMethods.synchSalesAssociateDinnindTablesConfiguration(getActivity());
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            reloadGrid(0);
        }
    }
}
