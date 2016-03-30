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
import com.android.support.MyPreferences;

import java.util.List;

public class DinningTableSeatsAdapter extends BaseAdapter implements Filterable {
    private LayoutInflater mInflater;
    private int[] dinningTableSeats;


    public DinningTableSeatsAdapter(Activity activity, int[] dinningTableSeats) {
        mInflater = LayoutInflater.from(activity);
        this.dinningTableSeats = dinningTableSeats;
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

        holder.iconLine.setImageResource(R.drawable.ic_group_black_48dp);
        holder.textLine.setText(String.valueOf(dinningTableSeats[position]));
        return convertView;
    }


    public class ViewHolder {
        TextView textLine;
        ImageView iconLine;
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
        return dinningTableSeats.length;
    }

    @Override
    public Object getItem(int position) {
        return dinningTableSeats[position];
    }

}