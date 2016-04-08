package com.android.emobilepos.mainmenu.restaurant;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.emobilepos.R;
import com.android.emobilepos.models.DinningTable;
import com.android.proxies.DinningTablesProxy;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TablesMapFragment extends Fragment {

    private List<DinningTable> dinningTables;

    public TablesMapFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dlog_ask_table_map_layout, container, false);
        dinningTables = DinningTablesProxy.getDinningTables(getActivity());
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RelativeLayout.LayoutParams params;
        params = new RelativeLayout.LayoutParams(40, 40);

        RelativeLayout map = (RelativeLayout) view.findViewById(R.id.dinningTableMap);
        for (DinningTable table : dinningTables) {
            ImageView tableImageView = new ImageView(getActivity());
            tableImageView.setImageResource(R.drawable.dinning_table);
            params.leftMargin = table.getLocation().getPositionX();
            params.topMargin = table.getLocation().getPositionY();
            map.addView(tableImageView, params);
        }
    }
}
