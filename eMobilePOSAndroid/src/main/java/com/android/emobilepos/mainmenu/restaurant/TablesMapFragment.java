package com.android.emobilepos.mainmenu.restaurant;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.dao.DinningTableDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.models.DinningTable;
import com.android.emobilepos.ordering.SplittedOrderSummary_FA;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TablesMapFragment extends Fragment implements View.OnClickListener {

    private List<DinningTable> dinningTables;

    public TablesMapFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dlog_ask_table_map_layout, container, false);
        dinningTables = DinningTableDAO.getAll();//DinningTablesProxy.getDinningTables(getActivity());
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final RelativeLayout.LayoutParams[] params = new RelativeLayout.LayoutParams[1];

        final RelativeLayout map = (RelativeLayout) view.findViewById(R.id.dinningTableMap);
        final View mapFloor = map.findViewById(R.id.map_floorimageView4);
        ViewTreeObserver observer = map.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                map.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                for (DinningTable table : dinningTables) {
                    params[0] = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    RelativeLayout tableItem = (RelativeLayout) View.inflate(getActivity(), R.layout.dinning_table_map_item, null);
//            ImageView tableImageView = new ImageView(getActivity());
//            tableImageView.setImageResource(R.drawable.dinning_table);
//            tableItem.setLeft((int) convertPixelsToDp(table.getPosition().getPositionX(), getActivity()));
//            tableItem.setTop((int) convertPixelsToDp(table.getPosition().getPositionY(), getActivity()));
                    if (table.getPosition() != null && table.getPosition().getPositionY() > 0 && table.getPosition().getPositionX() > 0) {
                        params[0].leftMargin = (int) convertPixelsToDp(table.getPosition().getPositionX(), mapFloor);
                        params[0].topMargin = (int) convertPixelsToDp(table.getPosition().getPositionY(), mapFloor);
                        Log.d("Table add:", "X:" + params[0].leftMargin + " Y:" + params[0].topMargin);
                        String label = getActivity().getString(R.string.table_label_map) + " " + table.getNumber();
                        ((TextView) tableItem.findViewById(R.id.tableNumbertextView)).setText(label);
                        map.addView(tableItem, params[0]);
                        tableItem.findViewById(R.id.table_map_container).setOnClickListener(TablesMapFragment.this);
                        tableItem.findViewById(R.id.table_map_container).setTag(table);
                    }
                }
            }
        });


    }


    public static float convertPixelsToDp(float px, View view) {
        float ratio = (Float.valueOf(String.valueOf(view.getHeight())) / 600f);
        px = px * ratio;
//        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.table_map_container: {
                DinningTable table = (DinningTable) v.getTag();
                Intent result = new Intent();
                result.putExtra("tableId", table.getId());
                getActivity().setResult(SplittedOrderSummary_FA.NavigationResult.TABLE_SELECTION.getCode(), result);
                getActivity().finish();
                break;
            }
        }
    }
}
