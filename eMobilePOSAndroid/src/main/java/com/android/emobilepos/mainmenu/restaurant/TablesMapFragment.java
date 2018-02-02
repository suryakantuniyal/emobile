package com.android.emobilepos.mainmenu.restaurant;


import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.dao.ClerkDAO;
import com.android.dao.DinningTableDAO;
import com.android.dao.DinningTableOrderDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.models.realms.DinningTable;
import com.android.emobilepos.models.realms.DinningTableOrder;
import com.android.emobilepos.ordering.SplittedOrderSummary_FA;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TablesMapFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    MyPreferences preferences;
    private HashMap<String, List<Clerk>> tableAssignedClerks;

    public TablesMapFragment() {
    }

    private DinningTablesActivity getDinningTablesActivity() {
        return (DinningTablesActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dlog_ask_table_map_layout, container, false);
        preferences = new MyPreferences(getActivity());
        tableAssignedClerks = DinningTableDAO.getTableAssignedClerks();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        drawDinningTableMap(view);
    }

    private void drawDinningTableMap(View view) {
        final RelativeLayout.LayoutParams[] params = new RelativeLayout.LayoutParams[1];

        final RelativeLayout map = (RelativeLayout) view.findViewById(R.id.dinningTableMap);
        final View mapFloor = map.findViewById(R.id.dinningTableMap);
        ViewTreeObserver observer = map.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                map.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                List<DinningTable> dinningTables = getDinningTablesActivity().dinningTables;
                for (DinningTable table : dinningTables) {
                    params[0] = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    LinearLayout tableItem = (LinearLayout) View.inflate(getActivity(), R.layout.dinning_table_map_item, null);

                    if (table.getPosition() != null && table.getPosition().getPositionY() > 0 && table.getPosition().getPositionX() > 0) {
                        ImageView img;
                        switch (Integer.parseInt(table.getStyle())) {
                            case 0: {
                                img = (ImageView) tableItem.findViewById(R.id.dinningtableimageView3);
                                img.setImageResource(getSquareTableBySize(table.getDimensions().getWidth()));
                                break;
                            }
                            default: {
                                img = (ImageView) tableItem.findViewById(R.id.dinningtableimageView3);
                                img.setImageResource(getRoundTableBySize(table.getDimensions().getWidth()));
                            }
                        }
                        DinningTableOrder dinningTableOrder = DinningTableOrderDAO.getByNumber(table.getNumber());
                        params[0].leftMargin = (int) convertPixelsToDp(table.getPosition().getPositionX(), mapFloor);
                        params[0].topMargin = (int) convertPixelsToDp(table.getPosition().getPositionY(), mapFloor);
                        String label = getActivity().getString(R.string.table_label_map) + " " + table.getNumber();
                        List<Clerk> clerks = tableAssignedClerks.get(table.getId());
                        ((TextView) tableItem.findViewById(R.id.tableNumbertextView)).setText(label);
                        map.addView(tableItem, params[0]);
                        tableItem.findViewById(R.id.table_map_container).setOnClickListener(TablesMapFragment.this);
                        tableItem.findViewById(R.id.table_map_container).setTag(table);
                        TextView timeTxt = (TextView) tableItem.findViewById(R.id.timetextView21);
                        TextView clerkName = (TextView) tableItem.findViewById(R.id.clerkNametextView23);
                        clerkName.setText(clerks != null && !clerks.isEmpty() ? clerks.get(0).getEmpName() : "");
                        ImageView isSelectedCheckBox = (ImageView) tableItem.findViewById(R.id.selectedCheckboximageView);
                        TextView guestsTxt = (TextView) tableItem.findViewById(R.id.gueststextView16);
                        TextView amountxt = (TextView) tableItem.findViewById(R.id.amounttextView23);
                        if ((getDinningTablesActivity().associate != null && getDinningTablesActivity().associate.getAssignedDinningTables().contains(table))) {
                            isSelectedCheckBox.setVisibility(View.VISIBLE);
                        } else {
                            isSelectedCheckBox.setVisibility(View.GONE);
                        }
                        if (dinningTableOrder != null) {
                            timeTxt.setBackgroundResource(R.color.seat7);
                            guestsTxt.setBackgroundResource(R.color.seat7);
                            amountxt.setBackgroundResource(R.color.seat7);
                            timeTxt.setVisibility(View.VISIBLE);
                            guestsTxt.setVisibility(View.VISIBLE);
                            amountxt.setVisibility(View.VISIBLE);
                            timeTxt.setText(dinningTableOrder.getElapsedTime());
                            guestsTxt.setText(String.format("%d/%d", dinningTableOrder.getNumberOfGuest(), table.getSeats()));
                            Order order = dinningTableOrder.getOrder(getActivity());
                            amountxt.setText(Global.getCurrencyFormat(order.ord_subtotal));
                            tableItem.findViewById(R.id.table_map_container).setOnLongClickListener(TablesMapFragment.this);
                        } else {
                            timeTxt.setBackgroundResource(R.color.seat12);
                            guestsTxt.setBackgroundResource(R.color.seat12);
                            amountxt.setBackgroundResource(R.color.seat12);
                            timeTxt.setVisibility(View.GONE);
                            guestsTxt.setText(String.format("%d/%d", 0, table.getSeats()));
                            amountxt.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
    }


    private int getRoundTableBySize(int width) {
        switch (width) {
            case 40:
                return R.drawable.table_round_sm;
            case 60:
                return R.drawable.table_round_md;
            case 80:
                return R.drawable.table_round_lg;
            default:
                return R.drawable.table_round_md;
        }
    }

    private int getSquareTableBySize(int width) {
        switch (width) {
            case 40:
                return R.drawable.table_square_sm;
            case 60:
                return R.drawable.table_square_md;
            case 80:
                return R.drawable.table_square_lg;
            default:
                return R.drawable.table_square_md;
        }
    }


    public static float convertPixelsToDp(float px, View view) {
        float ratio = (Float.valueOf(String.valueOf(view.getHeight())) / 600f);
        px = px * ratio;
        return px;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.table_map_container: {
                DinningTable table = (DinningTable) v.getTag();
                if ((getDinningTablesActivity().associate != null && getDinningTablesActivity().associate.getAssignedDinningTables().contains(table))) {
                    DinningTableOrder tableOrder = DinningTableOrderDAO.getByNumber(table.getNumber());
                    if (tableOrder != null) {
                        getDinningTablesActivity().new OpenOnHoldOrderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR
                                , tableOrder, table);
                    } else {
                        Intent result = new Intent();
                        result.putExtra("tableId", table.getId());
                        getActivity().setResult(SplittedOrderSummary_FA.NavigationResult.TABLE_SELECTION.getCode(), result);
                        getActivity().finish();
                    }
                } else {
                    Global.showPrompt(getActivity(), R.string.title_activity_dinning_tables, getActivity().getString(R.string.dinningtablenotassigned));
                }
                break;
            }
        }
    }

    @Override
    public boolean onLongClick(final View v) {
        switch (v.getId()) {
            case R.id.table_map_container: {
                final DinningTable table = (DinningTable) v.getTag();
                if (getDinningTablesActivity().associate.getAssignedDinningTables().contains(table)) {
                    PopupMenu popup = new PopupMenu(getActivity(), v);
                    popup.getMenuInflater().inflate(R.menu.dinning_table_map_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            DinningTableOrderDAO.deleteByNumber(table.getNumber());
                            DinningTablesActivity activity = (DinningTablesActivity) getActivity();
                            activity.refresh(0);
                            return true;
                        }
                    });
                    popup.show();
                }
                break;
            }
        }
        return false;
    }


}
