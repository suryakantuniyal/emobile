package com.android.emobilepos.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.database.ShiftPeriodsDBHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.OrderProducts;
import com.android.emobilepos.models.Payment;
import com.android.emobilepos.models.ShiftPeriods;
import com.android.support.Global;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class ReportEndDayAdapter extends BaseAdapter implements StickyListHeadersAdapter{

	private final int TYPE_SUMMARY = 0, TYPE_SHIFTS = 1, TYPE_ORD_TYPES = 2,TYPE_ITEMS_SOLD = 3, TYPE_ITEMS_RETURNED = 4, TYPE_DEPT_SALES = 5, TYPE_DEPT_RETURNS = 6,
			TYPE_PAYMENT = 7, TYPE_VOID = 8, TYPE_REFUND = 9, TYPE_AR_TRANS = 10;
	
	private final String S_SUMMARY = "Summary", S_SHIFTS = "Total by shifts", S_ORD_TYPES = "Total by order types", S_ITEMS_SOLD = "Items Sold", S_ITEMS_RETURNED = "Items Returned",
			S_DEPT_SALES = "Department Sales",S_DEPT_RETURNS = "Department returns", S_PAYMENT = "Payments", S_VOID = "Void", S_REFUND = "Refund", S_AR_TRANS = "A/R Transactions";
	
	private Activity activity;
	private OrdersHandler ordHandler;
	private ShiftPeriodsDBHandler shiftHandler;
	private OrderProductsHandler ordProdHandler;
	private PaymentsHandler paymentHandler;
	private String mDate = null, clerk_id = null;
	
	private List<Order> listSummary, listOrdTypes,listARTrans;
	private List<OrderProducts> listSold, listReturned,listDeptSales,listDeptReturns;
	private List<Payment>listPayment, listVoid, listRefund;
	private List<ShiftPeriods>listShifts;
	private LayoutInflater inflater;
	
	
	private int i_summary = 0, i_shifts = 0,i_ord_types = 0,i_item_sold, i_item_returned = 0,i_dept_sales = 0,i_dept_returns = 0,i_payment = 0,
			i_void = 0, i_refund = 0, i_ar_trans = 0;
	
	private int listSize = 0;
	
	public ReportEndDayAdapter(Activity activity,String date, String clerk_id)
	{
		this.activity = activity;
		inflater = LayoutInflater.from(activity);
		mDate = date;
		
		ordHandler = new OrdersHandler(activity);
		shiftHandler = new ShiftPeriodsDBHandler(activity);
		ordProdHandler = new OrderProductsHandler(activity);
		paymentHandler = new PaymentsHandler(activity);
		
		getReportData();
	}
	
	public void setNewDate(String date)
	{
		mDate = date;
		getReportData();
		notifyDataSetChanged();
	}
	
	private void getReportData()
	{
		
		getOrders();
		getShifts();
		getItems();
		getDepartments();
		getPayments();
		getARTransactions();
		
		listSize =listSummary.size()+listOrdTypes.size()+listARTrans.size()+listSold.size()+listReturned.size()+listDeptSales.size()+listDeptReturns.size()+listPayment.size()
				+listVoid.size()+listRefund.size()+listShifts.size();
	
	}
	
	private void getOrders()
	{
		listOrdTypes = ordHandler.getOrderDayReport(clerk_id,mDate);
		listSummary = new ArrayList<Order>();
		
		BigDecimal returnAmount = new BigDecimal("0");
    	BigDecimal salesAmount = new BigDecimal("0");
    	BigDecimal invoiceAmount = new BigDecimal("0");
    	
    	for(Order ord:listOrdTypes)
    	{
    		switch(Integer.parseInt(ord.ord_type))
    		{
    		case Global.INT_RETURN:
    			ord.ord_type_name = "Return";
    			returnAmount = new BigDecimal(ord.ord_total);
    			break;
    		case Global.INT_ESTIMATE:
    			ord.ord_type_name = "Estimate";
    			break;
    		case Global.INT_ORDER:
    			ord.ord_type_name = "Order";
    			break;
    		case Global.INT_SALES_RECEIPT:
    			ord.ord_type_name = "Sales Receipt";
    			salesAmount = new BigDecimal(ord.ord_total);
    			break;
    		case Global.INT_INVOICE:
    			ord.ord_type_name = "Invoice";
    			invoiceAmount = new BigDecimal(ord.ord_total);
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
    	
    	ord = new Order(activity);
    	ord.ord_total = salesAmount.add(invoiceAmount).subtract(returnAmount).toString();
    	ord.ord_type_name = "Total";
    	
    	listSummary.add(ord);
    	
    	i_summary = listSummary.size();
    	
    	
    	
	}
	
	private void getShifts()
	{
		listShifts = shiftHandler.getShiftDayReport(clerk_id,mDate);
		
		i_shifts=i_summary+listShifts.size();
		
		i_ord_types =i_shifts+listOrdTypes.size();
	}
	
	private void getItems()
	{
		listSold = ordProdHandler.getProductsDayReport(true, clerk_id,mDate);
		listReturned = ordProdHandler.getProductsDayReport(false, clerk_id,mDate);
		
		i_item_sold=i_ord_types+listSold.size();
		i_item_returned=i_item_sold+listReturned.size();
		
		
	}
	
	private void getDepartments()
	{
		listDeptSales = ordProdHandler.getDepartmentDayReport(true, clerk_id,mDate);
		listDeptReturns = ordProdHandler.getDepartmentDayReport(false, clerk_id,mDate);
		
		i_dept_sales =i_item_returned+listDeptSales.size();
		i_dept_returns=i_dept_sales+listDeptReturns.size();
	}
	
	private void getPayments()
	{
		listPayment = paymentHandler.getPaymentsGroupDayReport(0, clerk_id,mDate);
		listVoid = paymentHandler.getPaymentsGroupDayReport(1, clerk_id,mDate);
		listRefund = paymentHandler.getPaymentsGroupDayReport(2, clerk_id,mDate);

		i_payment=i_dept_returns+listPayment.size();
		i_void =i_payment+listVoid.size();
		i_refund = i_void+listRefund.size();
	}
	
	private void getARTransactions()
	{
		listARTrans = ordHandler.getARTransactionsDayReport(clerk_id,mDate);
		
		i_ar_trans =i_refund+listARTrans.size();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listSize;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getItemViewType(int position) {
		if(position<i_summary)
			return TYPE_SUMMARY;
		else if(position>=i_summary&&position<i_shifts)
			return TYPE_SHIFTS;
		else if(position>=i_shifts&&position<i_ord_types)
			return TYPE_ORD_TYPES;
		else if(position>=i_ord_types&&position<i_item_sold)
			return TYPE_ITEMS_SOLD;
		else if(position>=i_item_sold&&position<i_item_returned)
			return TYPE_ITEMS_RETURNED;
		else if(position>=i_item_returned&&position<i_dept_sales)
			return TYPE_DEPT_SALES;
		else if(position>=i_dept_sales&&position<i_dept_returns)
			return TYPE_DEPT_RETURNS;
		else if(position>=i_dept_returns&&position<i_payment)
			return TYPE_PAYMENT;
		else if(position>=i_payment&&position<i_void)
			return TYPE_VOID;
		else if(position>=i_void&&position<i_refund)
			return TYPE_REFUND;
		else if(position>=i_refund&&position<i_ar_trans)
			return TYPE_AR_TRANS;
		else
			return 0;
	}
	
	@Override
	public int getViewTypeCount() {
		return 10;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		int viewType = getItemViewType(position);
		
		if(convertView==null)
		{
			convertView = inflateView(convertView,parent,viewType);
		}
		else
			mHolder = (ViewHolder)convertView.getTag();
		
		
		populateView(position, viewType);
		return convertView; 
	}
	
	private View inflateView(View convertView,ViewGroup parent,int viewType)
	{
		mHolder = new ViewHolder();
		
		switch(viewType)
		{
		case TYPE_SUMMARY:
			convertView = inflater.inflate(R.layout.adapter_report_two_column, parent,false);
			
			mHolder.tvLeftColumn = (TextView)convertView.findViewById(R.id.tvLeftColumn);
			mHolder.tvRightColumn = (TextView)convertView.findViewById(R.id.tvRightColumn);
			break;
		case TYPE_SHIFTS:
			convertView = inflater.inflate(R.layout.adapter_report_shift, parent,false);
			mHolder.tvClerk = (TextView)convertView.findViewById(R.id.tvClerkName);
			mHolder.tvFrom = (TextView)convertView.findViewById(R.id.tvFrom);
			mHolder.tvTo = (TextView)convertView.findViewById(R.id.tvTo);
			mHolder.tvBeginningPetty = (TextView)convertView.findViewById(R.id.tvBeginningPetty);
			mHolder.tvExpenses = (TextView)convertView.findViewById(R.id.tvExpenses);
			mHolder.tvEndingPetty = (TextView)convertView.findViewById(R.id.tvEndingPetty);
			mHolder.tvTotalTrans = (TextView)convertView.findViewById(R.id.tvTotalTrans);
			mHolder.tvTotalEnding = (TextView)convertView.findViewById(R.id.tvTotalEnding);
			mHolder.tvEnteredClose = (TextView)convertView.findViewById(R.id.tvEnteredClose);

			break;
		case TYPE_ORD_TYPES:
			convertView = inflater.inflate(R.layout.adapter_report_ord_type, parent,false);
			mHolder.tvOrderType = (TextView)convertView.findViewById(R.id.tvOrderType);
			mHolder.tvOrdSubtTotal = (TextView)convertView.findViewById(R.id.tvSubTotal);
			mHolder.tvOrdTax = (TextView)convertView.findViewById(R.id.tvTaxTotal);
			mHolder.tvOrdDiscount = (TextView)convertView.findViewById(R.id.tvDiscountTotal);
			mHolder.tvOrdNetTotal = (TextView)convertView.findViewById(R.id.tvNetTotal);
			break;
		case TYPE_ITEMS_SOLD:
		case TYPE_ITEMS_RETURNED:
		case TYPE_DEPT_SALES:
		case TYPE_DEPT_RETURNS:
			convertView = inflater.inflate(R.layout.adapter_report_items, parent, false);
			mHolder.tvProdName = (TextView)convertView.findViewById(R.id.tvProdName);
			mHolder.tvProdID = (TextView)convertView.findViewById(R.id.tvProdID);
			mHolder.tvProdQty = (TextView)convertView.findViewById(R.id.tvProdQty);
			mHolder.tvProdTotal = (TextView)convertView.findViewById(R.id.tvProdTotal);
			break;
		case TYPE_PAYMENT:
		case TYPE_VOID:
		case TYPE_REFUND:
			
		case TYPE_AR_TRANS:
			convertView = inflater.inflate(R.layout.adapter_report_payment, parent, false);
			mHolder.tvPayType = (TextView)convertView.findViewById(R.id.tvPayType);
			mHolder.tvPayAmount = (TextView)convertView.findViewById(R.id.tvPayAmount);
			mHolder.tvPayTip = (TextView)convertView.findViewById(R.id.tvPayTip);
			break;
		}
		
		
		
		
		convertView.setTag(mHolder);
		return convertView;
	}

	private void populateView(int position, int viewType)
	{

		switch(viewType)
		{
		case TYPE_SUMMARY:
			mHolder.tvLeftColumn.setText(listSummary.get(position).ord_type_name);
			mHolder.tvRightColumn.setText(Global.formatDoubleStrToCurrency(listSummary.get(position).ord_total));
			break;
		case TYPE_SHIFTS:
			mHolder.tvClerk.setText(listShifts.get(position-i_summary).assignee_name);
			mHolder.tvFrom.setText(Global.formatToDisplayDate(listShifts.get(position-i_summary).startTime,activity,2));
			mHolder.tvTo.setText(Global.formatToDisplayDate(listShifts.get(position-i_summary).endTime,activity,2));
			mHolder.tvBeginningPetty.setText(Global.formatDoubleStrToCurrency(listShifts.get(position-i_summary).beginning_petty_cash));
			mHolder.tvExpenses.setText(Global.formatDoubleStrToCurrency(listShifts.get(position-i_summary).total_expenses));
			mHolder.tvEndingPetty.setText(Global.formatDoubleStrToCurrency(listShifts.get(position-i_summary).ending_petty_cash));
			mHolder.tvTotalTrans.setText(Global.formatDoubleStrToCurrency(listShifts.get(position-i_summary).total_transaction_cash));
			mHolder.tvTotalEnding.setText(Global.formatDoubleStrToCurrency(listShifts.get(position-i_summary).total_ending_cash));
			mHolder.tvEnteredClose.setText(listShifts.get(position-i_summary).entered_close_amount);
			break;
		case TYPE_ORD_TYPES:
			mHolder.tvOrderType.setText(listOrdTypes.get(position-i_shifts).ord_type_name);
			mHolder.tvOrdSubtTotal.setText(Global.formatDoubleStrToCurrency(listOrdTypes.get(position-i_shifts).ord_subtotal));
			mHolder.tvOrdTax.setText(Global.formatDoubleStrToCurrency(listOrdTypes.get(position-i_shifts).ord_taxamount));
			mHolder.tvOrdDiscount.setText(Global.formatDoubleStrToCurrency(listOrdTypes.get(position-i_shifts).ord_discount));
			mHolder.tvOrdNetTotal.setText(Global.formatDoubleStrToCurrency(listOrdTypes.get(position-i_shifts).ord_total));
			break;
		case TYPE_ITEMS_SOLD:
			mHolder.tvProdName.setText(listSold.get(position-i_ord_types).ordprod_name);
			mHolder.tvProdID.setText(listSold.get(position-i_ord_types).prod_id);
			mHolder.tvProdQty.setText(listSold.get(position-i_ord_types).ordprod_qty);
			mHolder.tvProdTotal.setText(Global.formatDoubleStrToCurrency(listSold.get(position-i_ord_types).overwrite_price));
			break;
		case TYPE_ITEMS_RETURNED:
			mHolder.tvProdName.setText(listReturned.get(position-i_item_sold).ordprod_name);
			mHolder.tvProdID.setText(listReturned.get(position-i_item_sold).prod_id);
			mHolder.tvProdQty.setText(listReturned.get(position-i_item_sold).ordprod_qty);
			mHolder.tvProdTotal.setText(Global.formatDoubleStrToCurrency(listReturned.get(position-i_item_sold).overwrite_price));
			break;
		case TYPE_DEPT_SALES:
			mHolder.tvProdName.setText(listDeptSales.get(position-i_item_returned).cat_name);
			mHolder.tvProdID.setText(listDeptSales.get(position-i_item_returned).cat_id);
			mHolder.tvProdQty.setText(listDeptSales.get(position-i_item_returned).ordprod_qty);
			mHolder.tvProdTotal.setText(Global.formatDoubleStrToCurrency(listDeptSales.get(position-i_item_returned).overwrite_price));
			break;
		case TYPE_DEPT_RETURNS:
			mHolder.tvProdName.setText(listDeptReturns.get(position-i_dept_sales).cat_name);
			mHolder.tvProdID.setText(listDeptReturns.get(position-i_dept_sales).cat_id);
			mHolder.tvProdQty.setText(listDeptReturns.get(position-i_dept_sales).ordprod_qty);
			mHolder.tvProdTotal.setText(Global.formatDoubleStrToCurrency(listDeptReturns.get(position-i_dept_sales).overwrite_price));
			break;
		case TYPE_PAYMENT:
			mHolder.tvPayType.setText(listPayment.get(position-i_dept_returns).card_type);
			mHolder.tvPayAmount.setText(Global.formatDoubleStrToCurrency(listPayment.get(position-i_dept_returns).pay_amount));
			mHolder.tvPayTip.setText(Global.formatDoubleStrToCurrency(listPayment.get(position-i_dept_returns).pay_tip));
			break;
		case TYPE_VOID:
			mHolder.tvPayType.setText(listVoid.get(position-i_payment).card_type);
			mHolder.tvPayAmount.setText(Global.formatDoubleStrToCurrency(listVoid.get(position-i_payment).pay_amount));
			mHolder.tvPayTip.setText(Global.formatDoubleStrToCurrency(listVoid.get(position-i_payment).pay_tip));
			break;
		case TYPE_REFUND:
			mHolder.tvPayType.setText(listRefund.get(position-i_void).card_type);
			mHolder.tvPayAmount.setText(Global.formatDoubleStrToCurrency(listRefund.get(position-i_void).pay_amount));
			mHolder.tvPayTip.setText(Global.formatDoubleStrToCurrency(listRefund.get(position-i_void).pay_tip));
			break;
		case TYPE_AR_TRANS:
			/*mHolder.tvPayType.setText(listPayment.get(position-i_refund).card_type);
			mHolder.tvPayAmount.setText(listPayment.get(position-i_refund).pay_amount);
			mHolder.tvPayTip.setText(listPayment.get(position-i_refund).pay_tip);*/
			break;
		}

	}
	
	
	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		if(convertView==null)
		{
			mHeaderHolder = new HeaderViewHolder();
			convertView = inflater.inflate(R.layout.adapter_day_report_header,parent, false);
			mHeaderHolder.tvHeaderTitle = (TextView)convertView.findViewById(R.id.tvHeader);
			
			convertView.setTag(mHeaderHolder);
		}
		else
			mHeaderHolder = (HeaderViewHolder)convertView.getTag();
		
		switch((int)getHeaderId(position))
		{
		case TYPE_SUMMARY:
			mHeaderHolder.tvHeaderTitle.setText("Summary");
			break;
		case TYPE_SHIFTS:
			mHeaderHolder.tvHeaderTitle.setText("Shifts");
			break;
		case TYPE_ORD_TYPES:
			mHeaderHolder.tvHeaderTitle.setText("Order Types");
			break;
		case TYPE_ITEMS_SOLD:
			mHeaderHolder.tvHeaderTitle.setText("Items Sold");
			break;
		case TYPE_ITEMS_RETURNED:
			mHeaderHolder.tvHeaderTitle.setText("Items Returned");
			break;
		case TYPE_DEPT_SALES:
			mHeaderHolder.tvHeaderTitle.setText("Dept Sales");
			break;
		case TYPE_DEPT_RETURNS:
			mHeaderHolder.tvHeaderTitle.setText("Dept Returns");
			break;
		case TYPE_PAYMENT:
			mHeaderHolder.tvHeaderTitle.setText("Payment");
			break;
		case TYPE_VOID:
			mHeaderHolder.tvHeaderTitle.setText("Void");
			break;
		case TYPE_REFUND:
			mHeaderHolder.tvHeaderTitle.setText("Refund");
			break;
		case TYPE_AR_TRANS:
			mHeaderHolder.tvHeaderTitle.setText("AR Trans");
			break;
		}
		
		return convertView;
	}

	@Override
	public long getHeaderId(int position) {
		// TODO Auto-generated method stub
		if(position<i_summary)
			return TYPE_SUMMARY;
		else if(position>=i_summary&&position<i_shifts)
			return TYPE_SHIFTS;
		else if(position>=i_shifts&&position<i_ord_types)
			return TYPE_ORD_TYPES;
		else if(position>=i_ord_types&&position<i_item_sold)
			return TYPE_ITEMS_SOLD;
		else if(position>=i_item_sold&&position<i_item_returned)
			return TYPE_ITEMS_RETURNED;
		else if(position>=i_item_returned&&position<i_dept_sales)
			return TYPE_DEPT_SALES;
		else if(position>=i_dept_sales&&position<i_dept_returns)
			return TYPE_DEPT_RETURNS;
		else if(position>=i_dept_returns&&position<i_payment)
			return TYPE_PAYMENT;
		else if(position>=i_payment&&position<i_void)
			return TYPE_VOID;
		else if(position>=i_void&&position<i_refund)
			return TYPE_REFUND;
		else if(position>=i_refund&&position<i_ar_trans)
			return TYPE_AR_TRANS;
		else
			return 0;
	}
	
	
	ViewHolder mHolder;
	HeaderViewHolder mHeaderHolder;
	private class ViewHolder{
		TextView tvLeftColumn, tvRightColumn;
		TextView tvClerk, tvFrom, tvTo, tvBeginningPetty,tvExpenses, tvEndingPetty, tvTotalTrans, tvTotalEnding, tvEnteredClose;
		TextView tvOrderType, tvOrdSubtTotal,tvOrdDiscount,tvOrdTax,tvOrdNetTotal;
		TextView tvProdName, tvProdID, tvProdQty,tvProdTotal;
		TextView tvPayType, tvPayAmount, tvPayTip;
	}
	
	private class HeaderViewHolder
	{
		TextView tvHeaderTitle;
	}

}
