package com.android.emobilepos.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.dao.SalesAssociateDAO;
import com.android.emobilepos.models.SalesAssociate;

import io.realm.RealmResults;

/**
 * Created by Guarionex on 5/13/2016.
 */
public class SalesAssociateListAdapter extends BaseAdapter {

    private final RealmResults<SalesAssociate> salesAssociates;

    public SalesAssociateListAdapter() {
        salesAssociates = SalesAssociateDAO.getAll();
    }

    @Override
    public int getCount() {
        return salesAssociates.size();
    }

    @Override
    public Object getItem(int i) {
        return salesAssociates.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }
}
