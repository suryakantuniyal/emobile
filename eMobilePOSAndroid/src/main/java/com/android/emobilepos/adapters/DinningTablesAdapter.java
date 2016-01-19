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
import com.android.support.MyPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DinningTablesAdapter extends BaseAdapter implements Filterable {
    private LayoutInflater mInflater;
    private List<DinningTable> dinningTables;


    public DinningTablesAdapter(Activity activity) {
        mInflater = LayoutInflater.from(activity);
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<DinningTable>>() {
        }.getType();
        dinningTables = gson.fromJson(activity.getResources().getString(R.string.dinningTables), listType);
        MyPreferences myPref = new MyPreferences(activity);
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
        holder.textLine.setText(dinningTables.get(position).getNumber());
        holder.dinningTable=dinningTables.get(position);
        return convertView;
    }


    public class ViewHolder {
        TextView textLine;
        ImageView iconLine;
        DinningTable dinningTable;
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