package com.android.emobilepos.recovery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.emobilepos.R;
import com.android.emobilepos.holders.Recoveries_Holder;

import java.util.List;

/**
 * Created by Luis Camayd on 8/30/2019.
 */
public class RecoveriesLV_Adapter extends BaseAdapter {
    private Context context;
    List<Recoveries_Holder> recoveries;
    LayoutInflater inflater;

    public RecoveriesLV_Adapter(Context context, List<Recoveries_Holder> recoveries) {
        super();
        this.context = context;
        this.recoveries = recoveries;
        inflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return recoveries != null ? recoveries.size() : 0;
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    /**
     * Return row for each recovery
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View cellView = convertView;
        RecoveriesLV_Adapter.Cell cell;
        Recoveries_Holder recovery = recoveries.get(position);

        if (convertView == null) {
            cell = new RecoveriesLV_Adapter.Cell();
            cellView = inflater.inflate(R.layout.country_picker_adapter, null);
            cell.textView = cellView.findViewById(R.id.row_title);
            cellView.setTag(cell);
        } else {
            cell = (RecoveriesLV_Adapter.Cell) cellView.getTag();
        }

        cell.textView.setText(recovery.getRec_name());
        return cellView;
    }

    /**
     * Holder for the cell
     */
    static class Cell {
        public TextView textView;
    }
}
