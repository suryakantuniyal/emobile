package com.android.emobilepos.adapters;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.dao.ClerkDAO;
import com.android.dao.ShiftDAO;
import com.android.dao.ShiftExpensesDAO;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.orders.OrderProduct;
import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.Shift;
import com.android.emobilepos.models.realms.ShiftExpense;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class ReportEndDayAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private final int TYPE_SUMMARY = 0, TYPE_SHIFTS = 1, TYPE_ORD_TYPES = 2, TYPE_ITEMS_SOLD = 3, TYPE_ITEMS_RETURNED = 4, TYPE_DEPT_SALES = 5, TYPE_DEPT_RETURNS = 6,
            TYPE_PAYMENT = 7, TYPE_VOID = 8, TYPE_REFUND = 9, TYPE_AR_TRANS = 10;

    private final String S_SUMMARY = "Summary", S_SHIFTS = "Total by shifts", S_ORD_TYPES = "Total by order types", S_ITEMS_SOLD = "Items Sold", S_ITEMS_RETURNED = "Items Returned",
            S_DEPT_SALES = "Department Sales", S_DEPT_RETURNS = "Department returns", S_PAYMENT = "Payments", S_VOID = "Void", S_REFUND = "Refund", S_AR_TRANS = "A/R Transactions";
    private final MyPreferences preferences;
    ViewHolder mHolder;
    HeaderViewHolder mHeaderHolder;
    private Activity activity;
    private OrdersHandler ordHandler;
    //    private ShiftPeriodsDBHandler shiftHandler;
    private OrderProductsHandler ordProdHandler;
    private PaymentsHandler paymentHandler;
    private String mDate = null, clerk_id = null;
    private List<Order> listSummary, listOrdTypes, listARTrans;
    private List<OrderProduct> listSold, listReturned, listDeptSales, listDeptReturns;
    private List<Payment> listPayment, listVoid, listRefund;
    private List<Shift> listShifts;
    private LayoutInflater inflater;
    private int i_summary = 0, i_shifts = 0, i_ord_types = 0, i_item_sold, i_item_returned = 0, i_dept_sales = 0, i_dept_returns = 0, i_payment = 0,
            i_void = 0, i_refund = 0, i_ar_trans = 0;
    private int listSize = 0;

    public ReportEndDayAdapter(Activity activity, String date, String clerk_id) {
        this.activity = activity;
        inflater = LayoutInflater.from(activity);
        mDate = date;
        preferences = new MyPreferences(activity);
        ordHandler = new OrdersHandler(activity);
//        shiftHandler = new ShiftPeriodsDBHandler(activity);
        ordProdHandler = new OrderProductsHandler(activity);
        paymentHandler = new PaymentsHandler(activity);
        this.clerk_id = clerk_id;

        getReportData();
    }

    public void setNewDate(String date) {
        mDate = date;
        getReportData();
        notifyDataSetChanged();
    }

    private void getReportData() {

        getOrders();
        getShifts();
        getItems();
        getDepartments();
        getPayments();
        getARTransactions();

        listSize = listSummary.size() + listOrdTypes.size() + listARTrans.size() + listSold.size() +
                listReturned.size() + listDeptSales.size() + listDeptReturns.size() + listPayment.size()
                + listVoid.size() + listRefund.size() + listShifts.size();

    }

    private void getOrders() {
        listOrdTypes = ordHandler.getOrderDayReport(clerk_id, mDate, false);
        listSummary = new ArrayList<>();
        List<Order> listOrdOnHoldTypes = ordHandler.getOrderDayReport(clerk_id, mDate, true);

        BigDecimal returnAmount = new BigDecimal("0");
        BigDecimal salesAmount = new BigDecimal("0");
        BigDecimal invoiceAmount = new BigDecimal("0");

        for (Order ord : listOrdTypes) {
            switch (Global.OrderType.getByCode(TextUtils.isEmpty(ord.ord_type) ? Global.OrderType.ORDER.getCode()
                    : Integer.parseInt(ord.ord_type))) {
                case RETURN:
                    ord.ord_type_name = activity.getString(R.string.eod_report_return);
                    returnAmount = new BigDecimal(ord.ord_total);
                    break;
                case ESTIMATE:
                    ord.ord_type_name = activity.getString(R.string.eod_report_estimate);
                    break;
                case ORDER:
                    ord.ord_type_name = activity.getString(R.string.eod_report_order);
                    break;
                case SALES_RECEIPT:
                    ord.ord_type_name = activity.getString(R.string.eod_report_salesreceipt);
                    salesAmount = new BigDecimal(ord.ord_total);
                    break;
                case INVOICE:
                    ord.ord_type_name = activity.getString(R.string.eod_report_invoice);
                    invoiceAmount = new BigDecimal(ord.ord_total);
                    break;
                case CONSIGNMENT_FILLUP:
                    ord.ord_type_name = activity.getString(R.string.eod_report_consignment_fillup);
                    break;
                case CONSIGNMENT_PICKUP:
                    ord.ord_type_name = activity.getString(R.string.eod_report_consignment_pickup);
                    break;
                case CONSIGNMENT_INVOICE:
                    ord.ord_type_name = activity.getString(R.string.eod_report_consignment_invoice);
                    break;
                case CONSIGNMENT_RETURN:
                    ord.ord_type_name = activity.getString(R.string.eod_report_consignment_return);
                    break;
            }
        }
        Order ord = new Order(activity);
        ord.ord_total = returnAmount.toString();
        ord.ord_type_name = "Return";

        listSummary.add(ord);


        ord = new Order(activity);
        ord.ord_total = salesAmount.toString();
        ord.ord_type_name = "Sales Receipt";

        listSummary.add(ord);

        ord = new Order(activity);
        ord.ord_total = invoiceAmount.toString();
        ord.ord_type_name = "Invoice";

        listSummary.add(ord);

        if (listOrdOnHoldTypes != null && !listOrdOnHoldTypes.isEmpty()) {
            ord = new Order(activity);
            ord.ord_total = listOrdOnHoldTypes.get(0).ord_total;
            ord.ord_type_name = "On Holds";
            listSummary.add(ord);
        }

        ord = new Order(activity);
        ord.ord_total = salesAmount.add(invoiceAmount).subtract(returnAmount).toString();
        ord.ord_type_name = "Total";

        listSummary.add(ord);

        i_summary = listSummary.size();


    }

    private void getShifts() {
        if (TextUtils.isEmpty(clerk_id)) {
            listShifts = ShiftDAO.getShift(DateUtils.getDateStringAsDate(mDate, DateUtils.DATE_yyyy_MM_dd));
        } else {
            listShifts = ShiftDAO.getShift(DateUtils.getDateStringAsDate(mDate, DateUtils.DATE_yyyy_MM_dd)
                    , Integer.parseInt(clerk_id));
        }
        i_shifts = i_summary + listShifts.size();
        i_ord_types = i_shifts + listOrdTypes.size();
    }

    private void getItems() {
        listSold = ordProdHandler.getProductsDayReport(true, clerk_id, mDate);
        listReturned = ordProdHandler.getProductsDayReport(false, clerk_id, mDate);

        i_item_sold = i_ord_types + listSold.size();
        i_item_returned = i_item_sold + listReturned.size();


    }

    private void getDepartments() {
        listDeptSales = ordProdHandler.getDepartmentDayReport(true, clerk_id, mDate);
        listDeptReturns = ordProdHandler.getDepartmentDayReport(false, clerk_id, mDate);

        i_dept_sales = i_item_returned + listDeptSales.size();
        i_dept_returns = i_dept_sales + listDeptReturns.size();
    }

    private void getPayments() {
        listPayment = paymentHandler.getPaymentsGroupDayReport(0, clerk_id, mDate);
        listVoid = paymentHandler.getPaymentsGroupDayReport(1, clerk_id, mDate);
        listRefund = paymentHandler.getPaymentsGroupDayReport(2, clerk_id, mDate);

        i_payment = i_dept_returns + listPayment.size();
        i_void = i_payment + listVoid.size();
        i_refund = i_void + listRefund.size();
    }

    private void getARTransactions() {
        listARTrans = ordHandler.getARTransactionsDayReport(clerk_id, mDate);
        i_ar_trans = i_refund + listARTrans.size();
    }

    @Override
    public int getCount() {
        return listSize;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < i_summary)
            return TYPE_SUMMARY;
        else if (position >= i_summary && position < i_shifts)
            return TYPE_SHIFTS;
        else if (position >= i_shifts && position < i_ord_types)
            return TYPE_ORD_TYPES;
        else if (position >= i_ord_types && position < i_item_sold)
            return TYPE_ITEMS_SOLD;
        else if (position >= i_item_sold && position < i_item_returned)
            return TYPE_ITEMS_RETURNED;
        else if (position >= i_item_returned && position < i_dept_sales)
            return TYPE_DEPT_SALES;
        else if (position >= i_dept_sales && position < i_dept_returns)
            return TYPE_DEPT_RETURNS;
        else if (position >= i_dept_returns && position < i_payment)
            return TYPE_PAYMENT;
        else if (position >= i_payment && position < i_void)
            return TYPE_VOID;
        else if (position >= i_void && position < i_refund)
            return TYPE_REFUND;
        else if (position >= i_refund)
            return TYPE_AR_TRANS;
        else
            return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 11;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);

        if (convertView == null) {
            convertView = inflateView(convertView, parent, viewType);
        } else
            mHolder = (ViewHolder) convertView.getTag();


        populateView(position, viewType);
        return convertView;
    }

    private View inflateView(View convertView, ViewGroup parent, int viewType) {
        mHolder = new ViewHolder();

        switch (viewType) {
            case TYPE_SUMMARY:
                convertView = inflater.inflate(R.layout.adapter_report_two_column, parent, false);

                mHolder.tvLeftColumn = convertView.findViewById(R.id.tvLeftColumn);
                mHolder.tvRightColumn = convertView.findViewById(R.id.tvRightColumn);
                break;
            case TYPE_SHIFTS:
                convertView = inflater.inflate(R.layout.adapter_report_shift, parent, false);
                mHolder.tvClerk = convertView.findViewById(R.id.tvClerkName);
                mHolder.tvFrom = convertView.findViewById(R.id.tvFrom);
                mHolder.tvTo = convertView.findViewById(R.id.tvTo);
                mHolder.tvBeginningPetty = convertView.findViewById(R.id.tvBeginningPetty);
                mHolder.tvExpenses = convertView.findViewById(R.id.tvExpenses);
//                mHolder.tvEndingPetty = (TextView) convertView.findViewById(R.id.tvEndingPetty);
                mHolder.tvTotalTrans = convertView.findViewById(R.id.tvTotalTrans);
                mHolder.tvTotalEnding = convertView.findViewById(R.id.tvTotalEnding);
                mHolder.tvEnteredClose = convertView.findViewById(R.id.tvEnteredClose);

                mHolder.tvSafeDrop = convertView.findViewById(R.id.safeDropExpensestextView);
                mHolder.tvCashDrop = convertView.findViewById(R.id.cashDropExpensestextView2);
                mHolder.tvCashIn = convertView.findViewById(R.id.cashInExpensestextView4);
                mHolder.tvBuyGoods = convertView.findViewById(R.id.buyGoodsServicesExpensestextView6);
                mHolder.tvNonCashGratuity = convertView.findViewById(R.id.nonCashGratuityExpensestextVie8);
                mHolder.tvTotalExpenses = convertView.findViewById(R.id.totalExpensestextView26);

                break;
            case TYPE_ORD_TYPES:
                convertView = inflater.inflate(R.layout.adapter_report_ord_type, parent, false);
                mHolder.tvOrderType = convertView.findViewById(R.id.tvOrderType);
                mHolder.tvOrdSubtTotal = convertView.findViewById(R.id.tvSubTotal);
                mHolder.tvOrdTax = convertView.findViewById(R.id.tvTaxTotal);
                mHolder.tvOrdDiscount = convertView.findViewById(R.id.tvDiscountTotal);
                mHolder.tvOrdNetTotal = convertView.findViewById(R.id.tvNetTotal);
                break;
            case TYPE_ITEMS_SOLD:
            case TYPE_ITEMS_RETURNED:
            case TYPE_DEPT_SALES:
            case TYPE_DEPT_RETURNS:
                convertView = inflater.inflate(R.layout.adapter_report_items, parent, false);
                mHolder.tvProdName = convertView.findViewById(R.id.tvProdName);
                mHolder.tvProdID = convertView.findViewById(R.id.tvProdID);
                mHolder.tvProdQty = convertView.findViewById(R.id.tvProdQty);
                mHolder.tvProdTotal = convertView.findViewById(R.id.tvProdTotal);
                break;
            case TYPE_PAYMENT:
            case TYPE_VOID:
            case TYPE_REFUND:
            case TYPE_AR_TRANS:
                convertView = inflater.inflate(R.layout.adapter_report_payment, parent, false);
                mHolder.tvPayType = convertView.findViewById(R.id.tvPayType);
                mHolder.tvPayAmount = convertView.findViewById(R.id.tvPayAmount);
                mHolder.tvPayTip = convertView.findViewById(R.id.tvPayTip);
                break;
        }


        convertView.setTag(mHolder);
        return convertView;
    }

    private void populateView(int position, int viewType) {

        switch (viewType) {
            case TYPE_SUMMARY:
                mHolder.tvLeftColumn.setText(listSummary.get(position).ord_type_name);
                mHolder.tvRightColumn.setText(Global.getCurrencyFormat(listSummary.get(position).ord_total));
                break;
            case TYPE_SHIFTS:
                String name;
                String shiftId = listShifts.get(position - i_summary).getShiftId();
                Clerk clerk = ClerkDAO.getByEmpId(listShifts.get(position - i_summary).getClerkId());
                name = clerk == null ? activity.getString(R.string.unknown) : clerk.getEmpName();
//                if (preferences.isUseClerks()) {
//                    name = ClerkDAO.getByEmpId(listShifts.get(position - i_summary).getClerkId(), false).getEmpName();
//                } else {
//                    name = ClerkDAO.getByEmpId(listShifts.get(position - i_summary).getClerkId(), false).getEmpName();
//                }
                BigDecimal totalExpenses = ShiftExpensesDAO.getShiftTotalExpenses(shiftId);
                BigDecimal safeDropTotal = ShiftExpensesDAO.getShiftTotalExpenses(shiftId, ShiftExpense.ExpenseProductId.SAFE_DROP);
                BigDecimal cashDropTotal = ShiftExpensesDAO.getShiftTotalExpenses(shiftId, ShiftExpense.ExpenseProductId.CASH_DROP);
                BigDecimal cashInTotal = ShiftExpensesDAO.getShiftTotalExpenses(shiftId, ShiftExpense.ExpenseProductId.CASH_IN);
                BigDecimal buyGoodsTotal = ShiftExpensesDAO.getShiftTotalExpenses(shiftId, ShiftExpense.ExpenseProductId.BUY_GOODS_SERVICES);
                BigDecimal nonCashGratuityTotal = ShiftExpensesDAO.getShiftTotalExpenses(shiftId, ShiftExpense.ExpenseProductId.NON_CASH_GRATUITY);

                mHolder.tvSafeDrop.setText(Global.getCurrencyFormat(String.valueOf(safeDropTotal)));
                mHolder.tvCashDrop.setText(Global.getCurrencyFormat(String.valueOf(cashDropTotal)));
                mHolder.tvCashIn.setText(Global.getCurrencyFormat(String.valueOf(cashInTotal)));
                mHolder.tvBuyGoods.setText(Global.getCurrencyFormat(String.valueOf(buyGoodsTotal)));
                mHolder.tvNonCashGratuity.setText(Global.getCurrencyFormat(String.valueOf(nonCashGratuityTotal)));
                mHolder.tvTotalExpenses.setText(Global.getCurrencyFormat(String.valueOf(totalExpenses)));
                mHolder.tvClerk.setText(name);
                mHolder.tvFrom.setText(DateUtils.getDateAsString(listShifts.get(position - i_summary).getStartTime(), DateUtils.DATE_yyyy_MM_dd));
                mHolder.tvTo.setText(DateUtils.getDateAsString(listShifts.get(position - i_summary).getEndTime(), DateUtils.DATE_yyyy_MM_dd));
                mHolder.tvBeginningPetty.setText(Global.getCurrencyFormat(listShifts.get(position - i_summary).getBeginningPettyCash()));
                mHolder.tvExpenses.setText(Global.getCurrencyFormat(listShifts.get(position - i_summary).getTotalExpenses()));
//                mHolder.tvEndingPetty.setText(Global.getCurrencyFormat(listShifts.get(position - i_summary).getEndingPettyCash()));
                mHolder.tvTotalTrans.setText(Global.getCurrencyFormat(listShifts.get(position - i_summary).getTotalTransactionsCash()));
                mHolder.tvTotalEnding.setText(Global.getCurrencyFormat(listShifts.get(position - i_summary).getTotal_ending_cash()));
                mHolder.tvEnteredClose.setText(listShifts.get(position - i_summary).getEnteredCloseAmount());
                break;
            case TYPE_ORD_TYPES:
                mHolder.tvOrderType.setText(listOrdTypes.get(position - i_shifts).ord_type_name);
                mHolder.tvOrdSubtTotal.setText(Global.getCurrencyFormat(listOrdTypes.get(position - i_shifts).ord_subtotal));
                mHolder.tvOrdTax.setText(Global.getCurrencyFormat(listOrdTypes.get(position - i_shifts).ord_taxamount));
                mHolder.tvOrdDiscount.setText(Global.getCurrencyFormat(listOrdTypes.get(position - i_shifts).ord_discount));
                mHolder.tvOrdNetTotal.setText(Global.getCurrencyFormat(listOrdTypes.get(position - i_shifts).ord_total));
                break;
            case TYPE_ITEMS_SOLD:
                mHolder.tvProdName.setText(listSold.get(position - i_ord_types).getOrdprod_name());
                mHolder.tvProdID.setText(listSold.get(position - i_ord_types).getProd_id());
                mHolder.tvProdQty.setText(listSold.get(position - i_ord_types).getOrdprod_qty());
                mHolder.tvProdTotal.setText(Global.getCurrencyFormat(listSold.get(position - i_ord_types).getFinalPrice()));
                break;
            case TYPE_ITEMS_RETURNED:
                mHolder.tvProdName.setText(listReturned.get(position - i_item_sold).getOrdprod_name());
                mHolder.tvProdID.setText(listReturned.get(position - i_item_sold).getProd_id());
                mHolder.tvProdQty.setText(listReturned.get(position - i_item_sold).getOrdprod_qty());
                mHolder.tvProdTotal.setText(Global.getCurrencyFormat(listReturned.get(position - i_item_sold).getFinalPrice()));
                break;
            case TYPE_DEPT_SALES:
                mHolder.tvProdName.setText(listDeptSales.get(position - i_item_returned).getCat_name());
                mHolder.tvProdID.setText(listDeptSales.get(position - i_item_returned).getCat_id());
                mHolder.tvProdQty.setText(listDeptSales.get(position - i_item_returned).getOrdprod_qty());
                mHolder.tvProdTotal.setText(Global.getCurrencyFormat(listDeptSales.get(position - i_item_returned).getFinalPrice()));
                break;
            case TYPE_DEPT_RETURNS:
                mHolder.tvProdName.setText(listDeptReturns.get(position - i_dept_sales).getCat_name());
                mHolder.tvProdID.setText(listDeptReturns.get(position - i_dept_sales).getCat_id());
                mHolder.tvProdQty.setText(listDeptReturns.get(position - i_dept_sales).getOrdprod_qty());
                mHolder.tvProdTotal.setText(Global.getCurrencyFormat(listDeptReturns.get(position - i_dept_sales).getFinalPrice()));
                break;
            case TYPE_PAYMENT:
                mHolder.tvPayType.setText(listPayment.get(position - i_dept_returns).getCard_type());
                mHolder.tvPayAmount.setText(Global.getCurrencyFormat(listPayment.get(position - i_dept_returns).getPay_amount()));
                mHolder.tvPayTip.setText(Global.getCurrencyFormat(listPayment.get(position - i_dept_returns).getPay_tip()));
                break;
            case TYPE_VOID:
                mHolder.tvPayType.setText(listVoid.get(position - i_payment).getCard_type());
                mHolder.tvPayAmount.setText(Global.getCurrencyFormat(listVoid.get(position - i_payment).getPay_amount()));
                mHolder.tvPayTip.setText(Global.getCurrencyFormat(listVoid.get(position - i_payment).getPay_tip()));
                break;
            case TYPE_REFUND:
                mHolder.tvPayType.setText(listRefund.get(position - i_void).getCard_type());
                mHolder.tvPayAmount.setText(Global.getCurrencyFormat(listRefund.get(position - i_void).getPay_amount()));
                mHolder.tvPayTip.setText(Global.getCurrencyFormat(listRefund.get(position - i_void).getPay_tip()));
                break;
            case TYPE_AR_TRANS:
                mHolder.tvPayType.setText(listARTrans.get(position - i_refund).ord_timecreated);
                mHolder.tvPayAmount.setText(listARTrans.get(position - i_refund).cust_name);
                mHolder.tvPayTip.setText(Global.getCurrencyFormat(listARTrans.get(position - i_refund).ord_total));
                break;
        }

    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            mHeaderHolder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.adapter_day_report_header, parent, false);
            mHeaderHolder.tvHeaderTitle = convertView.findViewById(R.id.tvHeader);

            convertView.setTag(mHeaderHolder);
        } else
            mHeaderHolder = (HeaderViewHolder) convertView.getTag();

        switch ((int) getHeaderId(position)) {
            case TYPE_SUMMARY:
                mHeaderHolder.tvHeaderTitle.setText(R.string.eod_report_summary);
                break;
            case TYPE_SHIFTS:
                mHeaderHolder.tvHeaderTitle.setText(R.string.eod_report_shifts);
                break;
            case TYPE_ORD_TYPES:
                mHeaderHolder.tvHeaderTitle.setText(R.string.eod_report_ordertypes);
                break;
            case TYPE_ITEMS_SOLD:
                mHeaderHolder.tvHeaderTitle.setText(R.string.eod_report_itemsold);
                break;
            case TYPE_ITEMS_RETURNED:
                mHeaderHolder.tvHeaderTitle.setText(R.string.eod_report_itemsreturned);
                break;
            case TYPE_DEPT_SALES:
                mHeaderHolder.tvHeaderTitle.setText(R.string.eod_report_dept_sales);
                break;
            case TYPE_DEPT_RETURNS:
                mHeaderHolder.tvHeaderTitle.setText(R.string.eod_report_dept_returns);
                break;
            case TYPE_PAYMENT:
                mHeaderHolder.tvHeaderTitle.setText(R.string.eod_report_payment);
                break;
            case TYPE_VOID:
                mHeaderHolder.tvHeaderTitle.setText(R.string.eod_report_void);
                break;
            case TYPE_REFUND:
                mHeaderHolder.tvHeaderTitle.setText(R.string.eod_report_refund);
                break;
            case TYPE_AR_TRANS:
                mHeaderHolder.tvHeaderTitle.setText(R.string.eod_report_ar_trans);
                break;
        }

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        if (position < i_summary)
            return TYPE_SUMMARY;
        else if (position >= i_summary && position < i_shifts)
            return TYPE_SHIFTS;
        else if (position >= i_shifts && position < i_ord_types)
            return TYPE_ORD_TYPES;
        else if (position >= i_ord_types && position < i_item_sold)
            return TYPE_ITEMS_SOLD;
        else if (position >= i_item_sold && position < i_item_returned)
            return TYPE_ITEMS_RETURNED;
        else if (position >= i_item_returned && position < i_dept_sales)
            return TYPE_DEPT_SALES;
        else if (position >= i_dept_sales && position < i_dept_returns)
            return TYPE_DEPT_RETURNS;
        else if (position >= i_dept_returns && position < i_payment)
            return TYPE_PAYMENT;
        else if (position >= i_payment && position < i_void)
            return TYPE_VOID;
        else if (position >= i_void && position < i_refund)
            return TYPE_REFUND;
        else if (position >= i_refund && position < i_ar_trans)
            return TYPE_AR_TRANS;
        else
            return 0;
    }

    private class ViewHolder {
        TextView tvLeftColumn, tvRightColumn;
        TextView tvClerk, tvFrom, tvTo, tvBeginningPetty, tvExpenses, tvTotalTrans, tvTotalEnding, tvEnteredClose;
        TextView tvSafeDrop, tvCashIn, tvCashDrop, tvBuyGoods, tvNonCashGratuity, tvTotalExpenses;
        TextView tvOrderType, tvOrdSubtTotal, tvOrdDiscount, tvOrdTax, tvOrdNetTotal;
        TextView tvProdName, tvProdID, tvProdQty, tvProdTotal;
        TextView tvPayType, tvPayAmount, tvPayTip;
    }

    private class HeaderViewHolder {
        TextView tvHeaderTitle;
    }

}
