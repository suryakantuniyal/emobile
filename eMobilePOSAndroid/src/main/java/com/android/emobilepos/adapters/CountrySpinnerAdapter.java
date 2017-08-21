package com.android.emobilepos.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.android.dao.DinningTableDAO;
import com.android.dao.DinningTableOrderDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.consignment.ConsignmentCheckout_FA;
import com.android.emobilepos.models.Country;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.models.realms.DinningTable;
import com.android.emobilepos.models.realms.DinningTableOrder;
import com.android.support.Global;

import java.util.HashMap;
import java.util.List;

import io.realm.RealmList;

public class CountrySpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

    private Activity context;
    private List<Country> countries;
    private int selectedPosition;

    public CountrySpinnerAdapter(Activity context, List<Country> countries) {
        this.context = context;
        this.countries = countries;
    }

    @Override
    public int getCount() {
        return countries.size();
    }

    @Override
    public Object getItem(int position) {
        return countries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.spinner_layout, null);
        }
        selectedPosition = position;
        Country country = countries.get(position);
        TextView text = (TextView) convertView.findViewById(R.id.taxName);
        text.setTextColor(Color.BLACK);// choose your color
        text.setPadding(35, 0, 0, 0);
        TextView valueText = (TextView) convertView.findViewById(R.id.taxValue);
        text.setText(country.getName());
        valueText.setText(country.getIsoCode());
        convertView.findViewById(R.id.checkMark).setVisibility(View.INVISIBLE);
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.spinner_layout, parent, false);
        }
        Country country = countries.get(position);
        TextView leftView = (TextView) convertView.findViewById(R.id.taxName);
        TextView rightView = (TextView) convertView.findViewById(R.id.taxValue);
        ImageView checked = (ImageView) convertView.findViewById(R.id.checkMark);
        leftView.setText(country.getIsoCode());
        rightView.setText(country.getName());
        if (position == selectedPosition) {
            checked.setVisibility(View.VISIBLE);
        } else {
            checked.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    class ViewHolder {

    }
}