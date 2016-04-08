package com.android.emobilepos.mainmenu.restaurant;


import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
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

        RelativeLayout map = (RelativeLayout) view.findViewById(R.id.dinningTableMap);

        for (DinningTable table : dinningTables) {
            params = new RelativeLayout.LayoutParams((int) convertPixelsToDp(60f, getActivity()),
                    (int) convertPixelsToDp(60f, getActivity()));
            ImageView tableImageView = new ImageView(getActivity());
            tableImageView.setImageResource(R.drawable.dinning_table);
            params.leftMargin = (int) convertPixelsToDp(table.getPosition().getPositionX(), getActivity());
            params.topMargin = (int) convertPixelsToDp(table.getPosition().getPositionY(), getActivity());
            Log.d("Table add:", "X:" + params.leftMargin + " Y:" + params.topMargin);
            map.addView(tableImageView, params);
        }
    }


    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();

        DisplayMetrics metrics = resources.getDisplayMetrics();
        float ratio = (Float.valueOf(String.valueOf(metrics.heightPixels)) / 600f);
        px = px * ratio;
//        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }
}
