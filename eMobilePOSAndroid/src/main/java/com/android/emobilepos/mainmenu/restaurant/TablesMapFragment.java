package com.android.emobilepos.mainmenu.restaurant;


import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
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

import com.android.dao.DinningTableDAO;
import com.android.dao.DinningTableOrderDAO;
import com.android.dao.SalesAssociateDAO;
import com.android.database.OrderProductsHandler;
import com.android.emobilepos.OnHoldActivity;
import com.android.emobilepos.R;
import com.android.emobilepos.models.DinningTable;
import com.android.emobilepos.models.DinningTableOrder;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.models.SalesAssociate;
import com.android.emobilepos.ordering.OrderingMain_FA;
import com.android.emobilepos.ordering.SplittedOrderSummary_FA;
import com.android.saxhandler.SAXdownloadHandler;
import com.android.support.Global;
import com.android.support.HttpClient;
import com.android.support.MyPreferences;
import com.android.support.OnHoldsManager;
import com.android.support.Post;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import io.realm.Realm;

/**
 * A simple {@link Fragment} subclass.
 */
public class TablesMapFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    private List<DinningTable> dinningTables;
    private SalesAssociate associate;

    public TablesMapFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dlog_ask_table_map_layout, container, false);
        DinningTablesActivity activity = (DinningTablesActivity) getActivity();
        if (!TextUtils.isEmpty(activity.associateId)) {
            associate = SalesAssociateDAO.getByEmpId(Integer.parseInt(activity.associateId));
        }
        dinningTables = DinningTableDAO.getAll();//DinningTablesProxy.getDinningTables(getActivity());

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
                        ((TextView) tableItem.findViewById(R.id.tableNumbertextView)).setText(label);
                        map.addView(tableItem, params[0]);
                        tableItem.findViewById(R.id.table_map_container).setOnClickListener(TablesMapFragment.this);
                        tableItem.findViewById(R.id.table_map_container).setTag(table);
                        TextView timeTxt = (TextView) tableItem.findViewById(R.id.timetextView21);
                        ImageView isSelectedCheckBox = (ImageView) tableItem.findViewById(R.id.selectedCheckboximageView);
                        TextView guestsTxt = (TextView) tableItem.findViewById(R.id.gueststextView16);
                        TextView amountxt = (TextView) tableItem.findViewById(R.id.amounttextView23);
                        if (associate != null && associate.getAssignedDinningTables().contains(table)) {
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
                            amountxt.setText(Global.formatDoubleStrToCurrency(order.ord_subtotal));
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
                if (associate != null && associate.getAssignedDinningTables().contains(table)) {
                    DinningTableOrder tableOrder = DinningTableOrderDAO.getByNumber(table.getNumber());
                    if (tableOrder != null) {
                        Realm realm = Realm.getDefaultInstance();
                        new OpenOnHoldOrderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR
                                , realm.copyFromRealm(tableOrder), realm.copyFromRealm(table));
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
                if (associate.getAssignedDinningTables().contains(table)) {
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

    public class OpenOnHoldOrderTask extends AsyncTask<Object, Void, Boolean> {

        private DinningTable table;
        DinningTableOrder tableOrder;

        @Override
        protected Boolean doInBackground(Object... params) {
            tableOrder = (DinningTableOrder) params[0];
            table = (DinningTable) params[1];
            boolean claimRequired = OnHoldsManager.isOnHoldAdminClaimRequired(tableOrder.getCurrentOrderId(), getActivity());
            if (claimRequired) {
                return false;
            } else {
                try {
                    OnHoldsManager.synchOrdersOnHoldDetails(getActivity(), tableOrder.getCurrentOrderId());
                    OrderProductsHandler orderProdHandler = new OrderProductsHandler(getActivity());
                    Cursor c = orderProdHandler.getOrderProductsOnHold(tableOrder.getCurrentOrderId());
                    Global global = (Global) getActivity().getApplication();
                    global.orderProducts = new ArrayList<>();
                    OnHoldActivity.addOrderProducts(getActivity(), c);
                    Global.isFromOnHold = true;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean willOpen) {
            if (willOpen) {
                if (Global.isFromOnHold) {
                    openOrderingMain();
                } else {
                    Intent result = new Intent();
                    result.putExtra("tableId", table.getId());
                    getActivity().setResult(SplittedOrderSummary_FA.NavigationResult.TABLE_SELECTION.getCode(), result);
                    getActivity().finish();
                }
            } else {
                Global.showPrompt(getActivity(), R.string.dlog_title_claimed_hold, getString(R.string.dlog_msg_cant_open_claimed_hold));
            }
        }

        private void openOrderingMain() {
            Global.lastOrdID=tableOrder.getCurrentOrderId();
            Order order = tableOrder.getOrder(getActivity());
            Intent intent = new Intent(getActivity(), OrderingMain_FA.class);
            intent.putExtra("selectedDinningTableNumber", table.getNumber());
            intent.putExtra("onHoldOrderJson", order.toJson());
            intent.putExtra("openFromHold", true);
            intent.putExtra("RestaurantSaleType", Global.RestaurantSaleType.EAT_IN);
            intent.putExtra("option_number", Global.TransactionType.SALE_RECEIPT);
            intent.putExtra("ord_HoldName", order.ord_HoldName);
            intent.putExtra("associateId", order.associateID);
            startActivityForResult(intent, 0);
            getActivity().finish();
        }
    }
}
