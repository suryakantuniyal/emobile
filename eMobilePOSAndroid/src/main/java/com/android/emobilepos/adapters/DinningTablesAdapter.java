package com.android.emobilepos.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.emobilepos.R;
import com.android.emobilepos.models.DinningTable;

import java.util.List;

public class DinningTablesAdapter extends BaseAdapter implements Filterable {
    private LayoutInflater mInflater;
    private List<DinningTable> dinningTables;


    public DinningTablesAdapter(Activity activity, List<DinningTable> dinningTables) {
        mInflater = LayoutInflater.from(activity);
        this.dinningTables = dinningTables;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.main_menu_listviewadapter, null);
            holder = new ViewHolder();
            holder.textLine = (TextView) convertView.findViewById(R.id.salesText);
            holder.iconLine = (ImageView) convertView.findViewById(R.id.salesIcon);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.iconLine.setImageResource(R.drawable.dinning_table);
        holder.textLine.setText(String.valueOf(dinningTables.get(position).getNumber()));
        holder.dinningTable = dinningTables.get(position);
        return convertView;
    }


    public class ViewHolder {
        TextView textLine;
        ImageView iconLine;
        public DinningTable dinningTable;
    }


    @Override
    public Filter getFilter() {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return dinningTables.size();
    }

    @Override
    public Object getItem(int position) {
        return dinningTables.get(position);
    }

}