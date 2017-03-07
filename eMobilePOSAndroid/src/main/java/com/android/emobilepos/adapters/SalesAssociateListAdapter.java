package com.android.emobilepos.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.dao.ClerkDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.models.realms.Clerk;

import io.realm.RealmResults;

/**
 * Created by Guarionex on 5/13/2016.
 */
public class SalesAssociateListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    public int selectedIdx = 0;
    private final RealmResults<Clerk> clerks;

    public SalesAssociateListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        clerks = ClerkDAO.getAll();
    }

    @Override
    public int getCount() {
        return clerks.size();
    }

    @Override
    public Object getItem(int i) {
        return clerks.get(i);
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
        holder.textView.setText(clerks.get(i).toString());
        return convertView;
    }

    public class ViewHolder {
        TextView textView;
    }
}
