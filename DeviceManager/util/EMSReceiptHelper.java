package util;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.database.ShiftPeriodsDBHandler;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.OrderProducts;
import com.android.emobilepos.models.Payment;
import com.android.emobilepos.models.ShiftPeriods;
import com.android.support.Global;

import android.app.Activity;

public class EMSReceiptHelper {
	private int LINE_WIDTH = 0;
	private final String empStr = "";
	private Activity activity;

    public EMSReceiptHelper(Activity activity,int _line_width)
    {
        LINE_WIDTH = _line_width;
        this.activity = activity;
    }



//    public String getHeader(MemoText obj)
//    {
//        StringBuilder sb = new StringBuilder();
//
//        if (String.IsNullOrEmpty(obj.memo_headerLine1) == false)
//        {
//            sb.append(centerText( obj.memo_headerLine1.toStringCheck()));
//            sb.append("\n");
//        }
//
//        if (String.IsNullOrEmpty(obj.memo_headerLine2) == false)
//        {
//            sb.append(centerText(obj.memo_headerLine2.toStringCheck()));
//            sb.append("\n");
//
//        }
//
//        if (String.IsNullOrEmpty(obj.memo_headerLine3) == false)
//        {
//            sb.append(centerText(obj.memo_headerLine3.toStringCheck()));
//        }
//
//        return sb.toString();
//    }
//
//    public String getFooter(MemoText obj)
//    {
//        StringBuilder sb = new StringBuilder();
//
//        
//            sb.append(centerText(obj.memo_footerLine1.toStringCheck()));
//            sb.append("\n");
//        
//
//        
//            sb.append(centerText(obj.memo_footerLine2.toStringCheck()));
//            sb.append("\n");
//       
//            sb.append(centerText(obj.memo_footerLine3.toStringCheck()));
//
//          
//
//        return sb.toString();
//
//    }
//
//    public String getTransactionReceipt(MemoText objMemo,Order order, List<OrderProduct> ordProd, List<Payments> payments)
//    {
//
//        StringBuilder sb = new StringBuilder();
//
//        sb.append("\n\n");
//        //TO-DO print header
//        sb.append(getHeader(objMemo));
//        sb.append("\n\n");
//
//        //print order details
//        int ord_type = order.ord_type;
//        switch (ord_type)
//        {
//            case 0:
//                break;
//            case 1:
//                break;
//            case 2:
//            case 7:
//                break;
//            case 3:
//                break;
//            case 5://SALES RECEIPT
//                sb.append(twoColumn("Sales Receipt" + ":", order.ord_id, 0));
//                break;
//        }
//
//        sb.append(twoColumn("Date:", order.ord_timecreated.toString("MMM/dd/yyyy"), 0));
//        sb.append(twoColumn("Employee:", App.Settings.LoadSetting("emp_name") + " (" + App.Settings.LoadSetting("empid") + ")", 0));
//
//        if (!String.IsNullOrEmpty(order.cust_id))
//            sb.append(twoColumn("Customer:", order.Customer.cust_name, 0));
//
//        if (!String.IsNullOrEmpty(order.ord_comment))
//        {
//            sb.append("\n\nComments:\n");
//            sb.append(oneColumn(order.ord_comment, 3));
//        }
//
//
//        sb.append("\n\n");
//
//
//        //Generate order products
//        int prod_size = ordProd.Count;
//        for (int i = 0; i < prod_size; i++)
//        {
//
//            sb.append(oneColumn(ordProd.ElementAt(i).ordprod_qty + "x " + ordProd.ElementAt(i).ordprod_name, 1));
//            //TODO Add currency format
//            sb.append(twoColumn("Price", ordProd.ElementAt(i).price.toString("C"), 3));
//            if (!String.IsNullOrEmpty(ordProd.ElementAt(i).prod_discountId))
//                sb.append(twoColumn("Discount", ordProd.ElementAt(i).prod_discountValue.toString("C"), 3));
//
//            sb.append(twoColumn("Total", ordProd.ElementAt(i).itemTotal.toString("C"), 3));
//
//            sb.append(oneColumn("Description",3));
//            if(!String.IsNullOrEmpty(ordProd.ElementAt(i).ordprod_desc))
//                sb.append(oneColumn(ordProd.ElementAt(i).ordprod_desc, 3));
//
//            sb.append("\n");
//        }
//
//
//        //Generate subtotal,total,discoutns,taxes, and payments
//        sb.append(lines(LINE_WIDTH));
//        sb.append(twoColumn("Sub Total:", order.ord_subtotal.toString("C"), 0));
//        sb.append(twoColumn("Discount:", order.ord_discount.toString("C"), 0));
//        sb.append(twoColumn("Tax:", order.ord_taxamount.toString("C"), 0)).append("\n");
//        //TODO Add Tax Group
//        sb.append(twoColumn("Grand Total:", order.ord_total.toString("C"), 0));
//
//        StringBuilder sb2 = new StringBuilder();
//        Decimal total_tip = new Decimal(0.0);
//        Decimal total_amount_paid = new Decimal(0.0);
//        Decimal? CashReturned = 0.00m;
//        foreach (Payments payment in payments)
//        {
//            total_tip += payment.pay_tip;
//            total_amount_paid += payment.pay_amount;
//
//            //Note need to add the payment method name when returning the payment
//            //Also need to determine the code for the pay type = Cash, Credit Card, Check
//            sb2.append(oneColumn(payment.pay_amount.toString("C")+"["+payment.payMethod.paymethod_name+"]",1));
//            if(!payment.pay_type.Equals(0)&&!payment.pay_type.Equals(1))
//            {
//                sb2.append(oneColumn("TransID: " + payment.pay_transid, 1));
//                sb2.append(oneColumn("CC#: *" + payment.ccnum_last4,1));
//            }
//
//            if(payment.payMethod.paymentmethod_type == "Cash")
//            {
//
//                CashReturned += payment.ReturnMoneyAmount;
//            }
//
//        }
//        sb.append(twoColumn("Amount Paid:", total_amount_paid.toString("C"), 0));
//        sb.append(sb2.toString());
//        sb.append(twoColumn("Tip Paid:", total_tip.toString("C"), 0));
//
//        if (CashReturned > 0)
//        sb.append(twoColumn("Cash Returned:",String.Format("{0:C}",CashReturned), 0));
//
//
//        return sb.toString();
//
//    }


//    public String getPaymentReceipt(MemoText obj, Payments payment,Customers customer)
//    {
//        StringBuilder sb = new StringBuilder();
//
//        //Generate Header
//        sb.append(getHeader(obj));
//        
//        if (payment.is_refund)
//            sb.append(centerText("* Refund *"));
//        else
//            sb.append(centerText(payment.payMethod.paymethod_name + " Sale"));
//
//
//        //Generate main body of receipt
//        sb.append("\n\n\n");
//        sb.append(twoColumn("Date",payment.pay_timecreated.toString("MMM/dd/yyyy"),0));
//        sb.append(twoColumn("Time", payment.pay_timecreated.toString("HH:mm"), 0));
//        
//        if(customer != null)
//        sb.append(twoColumn("Customer:", customer.cust_name , 0));
//
//        if (String.IsNullOrEmpty(payment.job_id))
//            sb.append(twoColumn("Order ID:", payment.job_id, 0));
//
//        sb.append(twoColumn("Payent ID:", payment.payMethod.paymethod_name, 0));
//
//
//        bool isCash = false, isCheck = false;
//
//        if (payment.pay_type == 0)
//            isCash = true;
//        else if(payment.pay_type==1)
//            isCheck = true;
//
//        if (!isCash && !isCheck)
//        {
//            sb.append(twoColumn("Card Num:", "*" + payment.ccnum_last4, 0));
//            sb.append(twoColumn("TransID:", payment.pay_transid, 0));
//        }
//        else if(isCheck)
//            sb.append(twoColumn("Check Num:", payment.pay_check, 0));
//
//        sb.append("\n\n");
//        sb.append(twoColumn("Total:",payment.pay_amount.toString("C"),0));
//        sb.append(twoColumn("Paid:", payment.pay_amount.toString("C"), 0));
//
//        sb.append(twoColumn("Tip:", payment.pay_tip.toString("C"), 0));
//
//
//        //if(!isCheck && !isCash)
//        //{
//        //    sb.append("\n\n\n\n");
//
//        //    //check if handwritten signature if not print signature image
//        //    //if(handwritten)
//        //    //    sb.append("\n\n\n")
//        //    //else
//        //    //  print image
//        //    sb.append(centerText("x"+lines(LINE_WIDTH / 2)));
//        //    sb.append(centerText("Signature"));
//        //    sb.append("\n\n\n");
//        //}
//
//
//        ////TO-DO Ivu Loto
//
//
//
//        ////Print Footer
//        //sb.append(getFooter(obj));
//        //if(!isCash&&!isCheck)
//        //{
//        //    sb.append(ccTerms);
//        //}
//        return sb.toString();
//    }
    
    
    public String getEndOfDayReportReceipt(String clerk_id, String date)
    {
    	String mDate = Global.formatToDisplayDate(date, activity, 4);
    	StringBuilder sb = new StringBuilder();
    	StringBuilder sb_ord_types = new StringBuilder();
    	
    	OrdersHandler ordHandler = new OrdersHandler(activity);
    	ShiftPeriodsDBHandler shiftHandler = new ShiftPeriodsDBHandler(activity);
    	OrderProductsHandler ordProdHandler = new OrderProductsHandler(activity);
    	PaymentsHandler paymentHandler = new PaymentsHandler(activity);
    	
    	sb.append(centerText("End Of Day Report"));
    	
    	sb.append(twoColumn("Date",Global.formatToDisplayDate(date, activity, 1),0));
    	sb.append(newLines(2));
    	
    	sb.append(centerText("Summary"));
    	sb.append(newLines(1));
    	
    	BigDecimal returnAmount = new BigDecimal("0");
    	BigDecimal salesAmount = new BigDecimal("0");
    	BigDecimal invoiceAmount = new BigDecimal("0");
    	
    	sb_ord_types.append(centerText("Totals By Order Types"));
    	List<Order> listOrder = ordHandler.getOrderDayReport(null,mDate);
    	for(Order ord:listOrder)
    	{
    		switch(Integer.parseInt(ord.ord_type))
    		{
    		case Global.INT_RETURN:
    			sb_ord_types.append(oneColumn("Return",0));
    			returnAmount = new BigDecimal(ord.ord_total);
    			break;
    		case Global.INT_ESTIMATE:
    			sb_ord_types.append(oneColumn("Estimate",0));
    			break;
    		case Global.INT_ORDER:
    			sb_ord_types.append(oneColumn("Order",0));
    			break;
    		case Global.INT_SALES_RECEIPT:
    			sb_ord_types.append(oneColumn("Sales Receipt",0));
    			salesAmount = new BigDecimal(ord.ord_total);
    			break;
    		case Global.INT_INVOICE:
    			sb_ord_types.append(oneColumn("Invoice",0));
    			invoiceAmount = new BigDecimal(ord.ord_total);
    			break;
    		}
    		
    		sb_ord_types.append(twoColumn("SubTotal",Global.formatDoubleStrToCurrency(ord.ord_subtotal),2));
			sb_ord_types.append(twoColumn("Discount Total",Global.formatDoubleStrToCurrency(ord.ord_discount),2));
			sb_ord_types.append(twoColumn("Tax Total",Global.formatDoubleStrToCurrency(ord.ord_taxamount),2));
			sb_ord_types.append(twoColumn("Net Total",Global.formatDoubleStrToCurrency(ord.ord_total),2));
    	}
    	
    	listOrder.clear();
    	
    	
    	sb.append(twoColumn("Return",Global.formatDoubleStrToCurrency(returnAmount.toString()),0));
    	sb.append(twoColumn("Sales Receipt",Global.formatDoubleStrToCurrency(salesAmount.toString()),0));
    	sb.append(twoColumn("Invoice",Global.formatDoubleStrToCurrency(invoiceAmount.toString()),0));
    	sb.append(twoColumn("Total",Global.formatDoubleStrToCurrency(salesAmount.add(invoiceAmount).subtract(returnAmount).toString()),0));
    	
    	sb.append(newLines(2));
    	
    	List<ShiftPeriods>listShifts = shiftHandler.getShiftDayReport(null,mDate);
    	if(listShifts.size()>0)
    	{
	    	sb.append(centerText("Totals By Shift"));
	    	for(ShiftPeriods shift:listShifts)
	    	{
	    		sb.append(twoColumn("Sales Clerk",shift.assignee_name,0));
	    		sb.append(twoColumn("From",Global.formatToDisplayDate(shift.startTime,activity,2),0));
	    		sb.append(twoColumn("To",Global.formatToDisplayDate(shift.endTime,activity,2),0));
	    		sb.append(twoColumn("Beginning Petty Cash",Global.formatDoubleStrToCurrency(shift.beginning_petty_cash),2));
	    		sb.append(twoColumn("Total Expenses",Global.formatDoubleStrToCurrency(shift.total_expenses),2));
	    		sb.append(twoColumn("Ending Petty Cash",Global.formatDoubleStrToCurrency(shift.ending_petty_cash),2));
	    		sb.append(twoColumn("Total Transactions Cash",Global.formatDoubleStrToCurrency(shift.total_transaction_cash),2));
	    		sb.append(twoColumn("Entered Close Amount",shift.entered_close_amount,2));
	    	}
	    	listShifts.clear();
    	}
    	
    	sb.append(newLines(2));
    	
    	sb.append(sb_ord_types);
    	
    	sb.append(newLines(2));
    	
    	List<OrderProducts>listProd = ordProdHandler.getProductsDayReport(true, null,mDate);
    	if(listProd.size()>0)
    	{
	    	sb.append(centerText("Items Sold"));
	    	sb.append(fourColumn("Name","ID","Qty","Total",0));
	    	
	    	for(OrderProducts prod:listProd)
	    	{
	    		sb.append(fourColumn(prod.ordprod_name,prod.prod_id,prod.ordprod_qty,Global.formatDoubleStrToCurrency(prod.overwrite_price),0));
	    	}
	    	listProd.clear();
    	}
    	
    	sb.append(newLines(2));
    	
    	listProd = ordProdHandler.getProductsDayReport(false, null,mDate);
    	if(listProd.size()>0)
    	{
	    	sb.append(centerText("Items Returned"));
	    	sb.append(fourColumn("Name","ID","Qty","Total",0));
	    	for(OrderProducts prod:listProd)
	    	{
	    		sb.append(fourColumn(prod.ordprod_name,prod.prod_id,prod.ordprod_qty,Global.formatDoubleStrToCurrency(prod.overwrite_price),0));
	    	}
	    	listProd.clear();
    	}
    	
    	sb.append(newLines(2));
    	
    	listProd = ordProdHandler.getDepartmentDayReport(true, null,mDate);
    	if(listProd.size()>0)
    	{
	    	sb.append(centerText("Department Sales"));
	    	sb.append(fourColumn("Name","ID","Qty","Total",0));
	    	for(OrderProducts prod:listProd)
	    	{
	    		sb.append(fourColumn(prod.cat_name,prod.cat_id,prod.ordprod_qty,Global.formatDoubleStrToCurrency(prod.overwrite_price),0));
	    	}
	    	listProd.clear();
    	}
    	
    	sb.append(newLines(2));
    	
    	listProd = ordProdHandler.getDepartmentDayReport(true, null,mDate);
    	if(listProd.size()>0)
    	{
	    	sb.append(centerText("Department Returns"));
	    	sb.append(fourColumn("Name","ID","Qty","Total",0));
	    	for(OrderProducts prod:listProd)
	    	{
	    		sb.append(fourColumn(prod.cat_name,prod.cat_id,prod.ordprod_qty,Global.formatDoubleStrToCurrency(prod.overwrite_price),0));
	    	}
	    	listProd.clear();
    	}
    	
    	sb.append(newLines(2));
    	List<Payment>listPayments = paymentHandler.getPaymentsDayReport(0, null,mDate);
    	if(listPayments.size()>0)
    	{
	    	sb.append(centerText("Payment"));
	    	for(Payment payment:listPayments)
	    	{
	    		sb.append(oneColumn(payment.card_type,0));
	    		sb.append(twoColumn("Amount",Global.formatDoubleStrToCurrency(payment.pay_amount),2));
	    		sb.append(twoColumn("Tip",Global.formatDoubleStrToCurrency(payment.pay_tip),2));
	    		
	    		sb.append(oneColumn("Details",3));
	    		sb.append(twoColumn("ID",payment.pay_id,4));
	    		sb.append(twoColumn("Amount",Global.formatDoubleStrToCurrency(payment.pay_amount),4));
	    		sb.append(twoColumn("Invoice",payment.job_id,4));
	    		sb.append(newLines(1));
	    	}
	    	listPayments.clear();
    	}
    	
    	sb.append(newLines(2));
    	
    	listPayments = paymentHandler.getPaymentsDayReport(1, null,mDate);
    	if(listPayments.size()>0)
    	{
	    	sb.append(centerText("Void"));
	    	for(Payment payment:listPayments)
	    	{
	    		sb.append(oneColumn(payment.card_type,0));
	    		sb.append(twoColumn("Amount",Global.formatDoubleStrToCurrency(payment.pay_amount),2));
	    		sb.append(twoColumn("Tip",Global.formatDoubleStrToCurrency(payment.pay_tip),2));
	    		
	    		sb.append(oneColumn("Details",3));
	    		sb.append(twoColumn("ID",payment.pay_id,4));
	    		sb.append(twoColumn("Amount",Global.formatDoubleStrToCurrency(payment.pay_amount),4));
	    		sb.append(twoColumn("Invoice",payment.job_id,4));
	    		sb.append(newLines(1));
	    	}
	    	listPayments.clear();
    	}
    	
    	
    	sb.append(newLines(2));
    	
    	listPayments = paymentHandler.getPaymentsDayReport(2, null,mDate);
    	if(listPayments.size()>0)
    	{
	    	sb.append(centerText("Refund"));
	    	for(Payment payment:listPayments)
	    	{
	    		sb.append(oneColumn(payment.card_type,0));
	    		sb.append(twoColumn("Amount",Global.formatDoubleStrToCurrency(payment.pay_amount),2));
	    		sb.append(twoColumn("Tip",Global.formatDoubleStrToCurrency(payment.pay_tip),2));
	    		
	    		sb.append(oneColumn("Details",3));
	    		sb.append(twoColumn("ID",payment.pay_id,4));
	    		sb.append(twoColumn("Amount",Global.formatDoubleStrToCurrency(payment.pay_amount),4));
	    		sb.append(twoColumn("Invoice",payment.job_id,4));
	    		sb.append(newLines(1));
	    	}
	    	listPayments.clear();
    	}
    	
    	sb.append(newLines(2));
    	
    	listOrder = ordHandler.getARTransactionsDayReport(null,mDate);
    	if(listOrder.size()>0)
    	{
	    	sb.append(centerText("A/R Transactions"));
	    	sb.append(threeColumn("ID","Customer","Amount",0));
	    	for(Order ord:listOrder)
	    	{
	    		if(ord.ord_id!=null)
	    			sb.append(threeColumn(ord.ord_id,ord.cust_name,Global.formatDoubleStrToCurrency(ord.ord_total),0));
	    	}
	    	listOrder.clear();
    	}
    	
    	return sb.toString();
    }














    /*
     * The classes below are used to format the String for the receipt printout
     */ 
    public String centerText(String _data)
    {
    	if(_data==null)
    		_data = empStr;
		int theStringLength = _data.length();
		StringBuilder sb = new StringBuilder();

		if (theStringLength < (LINE_WIDTH - 2)) {
			try {
				sb.append(this.spaces((LINE_WIDTH - theStringLength) / 2));
				sb.append(_data);
				sb.append(this.spaces((LINE_WIDTH - theStringLength) / 2));
			} catch (Exception ex) {
				sb.append("\n");
			}
		} else {
			try {

				sb.append(_data.substring(0, LINE_WIDTH - 2));
			} catch (Exception ex) {
				sb.append("\n");
			}
		}
		return sb.append("\n").toString();
    }

    private String spaces(int numSpaces)
    {
        StringBuilder sb = new StringBuilder();
        if (numSpaces > 0)
        {
            for (int i = 0; i < numSpaces; i++)
            {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String oneColumn(String columnText, int indent)
    {

    	if(columnText==null)
			columnText = empStr;
		
		int leftCharCount = columnText.length();

		int numSpaces = LINE_WIDTH - leftCharCount;
		StringBuilder sb = new StringBuilder();

		if (numSpaces > 0) {
			sb.append(this.spaces(indent));
			sb.append(columnText);
		} else // line exceeds the theLineWidth
		{
			int maxCharCount = LINE_WIDTH - indent;
			
			int size = 0;
			String [] tempArray;
			tempArray = this.formatLongStringArray(columnText, maxCharCount);
			size = tempArray.length;
			for(int i = 0 ; i < size && i < 10; i++)
			{
				sb.append(this.spaces(indent)).append(tempArray[i]).append("\n");
			}
		}

		return sb.append("\n").toString();
    }

    private String twoColumn(String first, String second, int theIndentation)
    {
//    	int numSpaces = 0;
//		
//		if(leftText == null)
//			leftText = empStr;
//		if(rightText == null)
//			rightText = empStr;
//		
//		if(leftText!=null&&rightText!=null)
//		{
//			int leftCharCount = leftText.length();
//			int rightCharCount = rightText.length();
//			numSpaces = LINE_WIDTH - leftCharCount - rightCharCount;
//		}
//		
//		StringBuilder sb = new StringBuilder();
//
//		if (numSpaces > 0) 
//		{
//			sb.append(this.spaces(theIndentation));
//			sb.append(leftText);
//			sb.append(this.spaces(numSpaces - theIndentation));
//			sb.append(rightText);
//		} else {
//			sb.append(this.spaces(theIndentation));
//			sb.append(leftText).append("\n");
//			sb.append(" ");
//			sb.append(rightText).append("\n");
//		}
//
//		return sb.append("\n").toString();
    	int max_col_length = (LINE_WIDTH / 2);
    	int length_1 = max_col_length;
    	int length_2 = max_col_length-1;
    	
		if(first.length()+theIndentation>max_col_length)
		{
			
			if(second.length()+theIndentation>max_col_length-1)
			{
				first = first.substring(0, max_col_length-3-theIndentation)+"...";
			}
			else
			{
				length_1 = first.length()+theIndentation;
			}
    		
		}
		else
		{
			length_1 = first.length()+theIndentation;
		}
		
    	if(second.length()+theIndentation>max_col_length-1)
    	{
    		second = second.substring(0,max_col_length-4-theIndentation)+"...";
    	}
    	else
    	{
    		length_2 = LINE_WIDTH-length_1-1;
    	}
    	
    	first = spaces(theIndentation)+first;
    	second = spaces(theIndentation)+second;
    	
    	StringBuilder formater = new StringBuilder();
    	String length1 = Integer.toString(length_1);
    	String length2 = Integer.toString(length_2);
    	formater.append("%-").append(length1).append("s");
    	formater.append("%").append(length2).append("s");
    	formater.append("%n");
    	
    	return String.format(formater.toString(), first,second);
    }

    private String threeColumn(String first, String second, String third, int theIndentation)
    {
//    	int numSpaces = 0;
//		
//		if(first == null)
//			first = empStr;
//		if(second == null)
//			second = empStr;
//		if(third == null)
//			third = empStr;
//		
//
//			int firstCharCount = first.length();
//			int secondCharCount = second.length();
//			int thirdCharCount = third.length();
//			numSpaces = LINE_WIDTH - (firstCharCount + secondCharCount+thirdCharCount);
//			numSpaces = numSpaces/3;
//		
//		StringBuilder sb = new StringBuilder();
//
//		if (numSpaces > 0) {
//			sb.append(this.spaces(numSpaces - theIndentation));
//			sb.append(first);
//			sb.append(this.spaces(numSpaces - theIndentation));
//			sb.append(second);
//			sb.append(this.spaces(numSpaces - theIndentation));
//			sb.append(third);
//		} else {
//			sb.append(this.spaces(theIndentation));
//			sb.append(first).append("\n");
//			sb.append(" ");
//			sb.append(second).append("\n");
//			sb.append(" ");
//			sb.append(third).append("\n");
//		}
//
//		return sb.append("\n").toString();
    	int max_col_length = (LINE_WIDTH / 3);
		if(first.length()>max_col_length)
    		first = first.substring(0, max_col_length-5)+"...";
    	if(second.length()>max_col_length)
    		second = second.substring(0,max_col_length-5)+"...";
    	if(third.length()>max_col_length)
    		third = third.substring(0, max_col_length-6)+"...";
    	
    	
    	StringBuilder formater = new StringBuilder();
    	String length = Integer.toString(max_col_length);
    	String length3 = Integer.toString(max_col_length-1);
    	formater.append("%-").append(length).append("s");
    	formater.append("%").append(length).append("s");
    	formater.append("%").append(length3).append("s");
    	
    	return String.format(formater.toString(), first,second,third)+"\n";
    }

    private String fourColumn(String first, String second, String third, String fourth, int theIndentation)
    {
//    	int numSpaces = 0;
//		
//		if(first == null)
//			first = empStr;
//		if(second == null)
//			second = empStr;
//		if(third == null)
//			third = empStr;
//		if(fourth == null)
//			fourth = empStr;
//		
//
//			int firstCharCount = first.length();
//			int secondCharCount = second.length();
//			int thirdCharCount = third.length();
//			int fourthCharCount = fourth.length();
//			numSpaces = LINE_WIDTH - (firstCharCount + secondCharCount+thirdCharCount+fourthCharCount);
//			numSpaces = numSpaces/4;
//		
//		StringBuilder sb = new StringBuilder();
//
//		if (numSpaces > 0) {
//			sb.append(this.spaces(numSpaces - theIndentation));
//			sb.append(first);
//			sb.append(this.spaces(numSpaces - theIndentation));
//			sb.append(second);
//			sb.append(this.spaces(numSpaces - theIndentation));
//			sb.append(third);
//			sb.append(this.spaces(numSpaces - theIndentation));
//			sb.append(fourth);
//		} else {
//			sb.append(this.spaces(theIndentation));
//			int max_length = LINE_WIDTH/4;
//			
//			if(first.length()>max_length)
//			{
//				first = first.substring(0, max_length-5);
//				first = first+"...";
//				sb.append(first);
//				numSpaces = max_length - first.length();
//				sb.append(spaces(numSpaces));
//			}
//			else
//			{
//				numSpaces = max_length - first.length();
//				sb.append(first);
//				sb.append(spaces(numSpaces));
//			}
//			
//			if(second.length()>max_length)
//			{
//				second = second.substring(0, max_length-5);
//				second = second+"...";
//				sb.append(second);
//				numSpaces = max_length - second.length();
//				sb.append(spaces(numSpaces));
//			}
//			else
//			{
//				numSpaces = max_length - second.length();
//				sb.append(second);
//				sb.append(spaces(numSpaces));
//			}
//			
//			if(third.length()>max_length)
//			{
//				third = third.substring(0, max_length-5);
//				third = third+"...";
//				sb.append(third);
//				numSpaces = max_length - third.length();
//				sb.append(spaces(numSpaces));
//			}
//			else
//			{
//				numSpaces = max_length - third.length();
//				sb.append(third);
//				sb.append(spaces(numSpaces));
//			}
//			
//			if(fourth.length()>max_length)
//			{
//				fourth = fourth.substring(0, max_length-5);
//				fourth = fourth+"...";
//				sb.append(fourth);
//				numSpaces = max_length - fourth.length();
//				sb.append(spaces(numSpaces));
//			}
//			else
//			{
//				numSpaces = max_length - fourth.length();
//				sb.append(fourth);
//				sb.append(spaces(numSpaces));
//			}
//			
//			
////			sb.append(first).append("\n");
////			sb.append(" ");
////			sb.append(second).append("\n");
////			sb.append(" ");
////			sb.append(third).append("\n");
////			sb.append(" ");
////			sb.append(fourth).append("\n");
//		}
//
//		return sb.append("\n").toString();
    	
    	int max_col_length = LINE_WIDTH / 4;
    	if(first.length()>max_col_length)
    		first = first.substring(0, max_col_length-5)+"...";
    	if(second.length()>max_col_length)
    		second = second.substring(0,max_col_length-5)+"...";
    	if(third.length()>max_col_length)
    		third = third.substring(0, max_col_length-5)+"...";
    	if(fourth.length()>max_col_length)
    		fourth = fourth.substring(0,max_col_length-5)+"...";
    	
    	StringBuilder formater = new StringBuilder();
    	
    	String length = Integer.toString(max_col_length);
    	formater.append("%-").append(length).append("s");
    	formater.append("%").append(length).append("s");
    	formater.append("%").append(length).append("s");
    	formater.append("%").append(length).append("s");
    	
    	return String.format(formater.toString(), first,second,third,fourth)+"\n";
    }

    private String longStringFormatter(String input, int maxCharInLine)
    {
        if (input == null)
            input = "";

        maxCharInLine = maxCharInLine - 2;
        StringBuilder output = new StringBuilder(input.length());
        String[] words = input.split(" ");
        int lineLen = 0;
        for (String word : words)
        {
            String temp_word = word;
            while (word.length() > maxCharInLine)
            {
                output.append(temp_word.substring(0, maxCharInLine - lineLen) + "\n");
                temp_word = temp_word.substring(maxCharInLine - lineLen);
                lineLen = 0;
            }

            if (lineLen + temp_word.length() > maxCharInLine)
            {
                output.append("\n");
                lineLen = 0;
            }

            output.append(temp_word + " ");
            lineLen += temp_word.length() + 1;
        }

        return output.toString();
    }

    private String formatLongString(String input, int maxCharInLine)
    {
        return longStringFormatter(input, maxCharInLine);
    }

    private String[] formatLongStringArray(String input, int maxCharInLine)
    {
        return longStringFormatter(input,maxCharInLine).toString().split("\n");
    }

    public String lines(int numLines)
    {
        StringBuilder sb = new StringBuilder();

        if (numLines > 0)
        {
            for (int i = 0; i < numLines; i++)
                sb.append("_");
        }

        return sb.toString();
    }
    
    
    
    public String newLines(int numNewLines)
	{
		StringBuilder sb = new StringBuilder();
		if(numNewLines>0)
			for(int i = 0 ; i < numNewLines; i++)
				sb.append("\n");
		return sb.toString();
	}
}
