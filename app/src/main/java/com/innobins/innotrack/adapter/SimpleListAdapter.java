package com.innobins.innotrack.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.innobins.innotrack.R;

/**
 * Created by surya on 11/10/17.
 */

public class SimpleListAdapter extends BaseAdapter {

    Context mContext;
    List<String> mList = new ArrayList<>();

    public SimpleListAdapter(Context context1, int simple_list_item_multiple_choice, List<String> List){
        mContext = context1;
        mList = List;
    }

 /*   public SimpleListAdapter(Context context, List<String> deviceName) {
        mContext = context;
        mList = deviceName;
    }*/

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null){
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            view = layoutInflater.inflate(R.layout.simple_items,null);
        } else {
            view = convertView;
        }
   /*     ListView listView = (ListView)view.findViewById(R.id.lists);
        listView.setAdapter(notifyDataSetChanged());*/
        TextView textView = (TextView)view.findViewById(R.id.text_i);
        textView.setText(mList.get(position));
        return view;
    }
}

