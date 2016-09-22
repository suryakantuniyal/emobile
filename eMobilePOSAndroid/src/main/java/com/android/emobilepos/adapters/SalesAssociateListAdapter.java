package com.android.emobilepos.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.dao.SalesAssociateDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.models.SalesAssociate;

import io.realm.RealmResults;

/**
 * Created by Guarionex on 5/13/2016.
 */
public class SalesAssociateListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    public int selectedIdx = 0;
    private final RealmResults<SalesAssociate> salesAssociates;

    public SalesAssociateListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
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
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.simple_list_item_1, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        convertView.setActivated(selectedIdx == i);
        holder.textView.setText(salesAssociates.get(i).toString());
        return convertView;
    }

    public class ViewHolder {
        TextView textView;
    }
}
