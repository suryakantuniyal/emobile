package com.android.emobilepos.print;

import android.content.Context;

import com.android.dao.AssignEmployeeDAO;
import com.android.dao.ClerkDAO;
import com.android.dao.ShiftDAO;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.DataTaxes;
import com.android.emobilepos.models.Report;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.orders.OrderProduct;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.Shift;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.crashlytics.android.Crashlytics;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import plaintext.EMSPlainTextHelper;

/**
 * Created by Luis Camayd on 8/19/2019.
 */
public class ReportBuilder {
    private Context context;
    private MyPreferences myPref;
    private List<String> printPref;
    private int lineWidth;

    public ReportBuilder(Context context, int lineWidth) {
        this.context = context;
        this.lineWidth = lineWidth;
        myPref = new MyPreferences(context);
        printPref = myPref.getPrintingPreferences();
    }

    public Report getEndOfDay(String currentDate, boolean printDetails) {

        Report report = new Report();

        try {
            AssignEmployee employee = AssignEmployeeDAO.getAssignEmployee();
            String mDate = Global.formatToDisplayDate(currentDate, 4);
            StringBuilder sb = new StringBuilder();
            EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
            StringBuilder sb_ord_types = new StringBuilder();

            OrdersHandler ordHandler = new OrdersHandler(context);
            OrderProductsHandler ordProdHandler = new OrderProductsHandler(context);
            PaymentsHandler paymentHandler = new PaymentsHandler(context);

            boolean showTipField = false;

            //determine if we should include the tip field
            if (myPref.getPreferences(MyPreferences.pref_show_tips_for_cash)) {
                showTipField = true;
            }

            sb.append(textHandler.centeredString("End Of Day Report", lineWidth));
            sb.append(textHandler.newLines(1));

            report.setSpecialHeader(sb.toString());
            sb.setLength(0);

            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Date",
                    Global.formatToDisplayDate(currentDate, 1), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Employee",
                    employee.getEmpName(), lineWidth, 0));
            sb.append(textHandler.newLines(2));

            report.setHeader(sb.toString());
            sb.setLength(0);

            sb.append(textHandler.centeredString("Summary", lineWidth));
            sb.append(textHandler.newLines(1));

            BigDecimal returnAmount = new BigDecimal("0");
            BigDecimal salesAmount = new BigDecimal("0");
            BigDecimal invoiceAmount = new BigDecimal("0");
            BigDecimal onHoldAmount = new BigDecimal("0");

            sb_ord_types.append(textHandler.centeredString("Totals By Order Types", lineWidth));
            sb_ord_types.append(textHandler.newLines(1));
            List<Order> listOrder = ordHandler.getOrderDayReport(null, mDate, false);
            HashMap<String, List<DataTaxes>> taxesBreakdownHashMap =
                    ordHandler.getOrderDayReportTaxesBreakdown(null, mDate);
            List<Order> listOrderHolds =
                    ordHandler.getOrderDayReport(null, mDate, true);
            for (Order ord : listOrder) {
                switch (Global.OrderType.getByCode(Integer.parseInt(ord.ord_type))) {
                    case RETURN:
                        sb_ord_types.append(textHandler.oneColumnLineWithLeftAlignedText(
                                "Return", lineWidth, 0));
                        returnAmount = new BigDecimal(ord.ord_total);
                        break;
                    case ESTIMATE:
                        sb_ord_types.append(textHandler.oneColumnLineWithLeftAlignedText(
                                "Estimate", lineWidth, 0));
                        break;
                    case ORDER:
                        sb_ord_types.append(textHandler.oneColumnLineWithLeftAlignedText(
                                "Order", lineWidth, 0));
                        break;
                    case SALES_RECEIPT:
                        sb_ord_types.append(textHandler.oneColumnLineWithLeftAlignedText(
                                "Sales Receipt", lineWidth, 0));
                        salesAmount = new BigDecimal(ord.ord_total);
                        break;
                    case INVOICE:
                        sb_ord_types.append(textHandler.oneColumnLineWithLeftAlignedText(
                                "Invoice", lineWidth, 0));
                        invoiceAmount = new BigDecimal(ord.ord_total);
                        break;
                    case CONSIGNMENT_FILLUP:
                        sb_ord_types.append(textHandler.oneColumnLineWithLeftAlignedText(
                                "Fill Up", lineWidth, 0));
                        invoiceAmount = new BigDecimal(ord.ord_total);
                        break;
                    case CONSIGNMENT_PICKUP:
                        sb_ord_types.append(textHandler.oneColumnLineWithLeftAlignedText(
                                "Pickup", lineWidth, 0));
                        invoiceAmount = new BigDecimal(ord.ord_total);
                        break;
                    case CONSIGNMENT_INVOICE:
                        sb_ord_types.append(textHandler.oneColumnLineWithLeftAlignedText(
                                "Consignment Invoice", lineWidth, 0));
                        invoiceAmount = new BigDecimal(ord.ord_total);
                        break;
                }

                sb_ord_types.append(textHandler.twoColumnLineWithLeftAlignedText(
                        "SubTotal", Global.getCurrencyFormat(ord.ord_subtotal),
                        lineWidth, 3));
                sb_ord_types.append(textHandler.twoColumnLineWithLeftAlignedText(
                        "Discounts", Global.getCurrencyFormat(ord.ord_discount),
                        lineWidth, 3));

                if (taxesBreakdownHashMap.containsKey(ord.ord_type)) {
                    sb_ord_types.append(textHandler.oneColumnLineWithLeftAlignedText(
                            "Taxes", lineWidth, 3));
                    for (DataTaxes dataTax : taxesBreakdownHashMap.get(ord.ord_type)) {
                        sb_ord_types.append(textHandler.twoColumnLineWithLeftAlignedText(
                                dataTax.getTax_name(), Global.getCurrencyFormat(
                                        dataTax.getTax_amount()), lineWidth, 6));
                    }
                } else {
                    sb_ord_types.append(textHandler.twoColumnLineWithLeftAlignedText(
                            "Taxes", "N/A", lineWidth, 3));
                }

                sb_ord_types.append(textHandler.twoColumnLineWithLeftAlignedText(
                        "Total", Global.getCurrencyFormat(ord.ord_total),
                        lineWidth, 3));
            }

            if (listOrderHolds != null && !listOrderHolds.isEmpty()) {
                onHoldAmount = new BigDecimal(listOrderHolds.get(0).ord_total);
            }

            listOrder.clear();
            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Return", "(" +
                    Global.getCurrencyFormat(returnAmount.toString()) + ")", lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Sales Receipt",
                    Global.getCurrencyFormat(salesAmount.toString()), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Invoice",
                    Global.getCurrencyFormat(invoiceAmount.toString()), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText("On Holds",
                    Global.getCurrencyFormat(onHoldAmount.toString()), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total",
                    Global.getCurrencyFormat(salesAmount.add(invoiceAmount).subtract(returnAmount)
                            .toString()), lineWidth, 0));

            report.setSummary(sb.toString());
            sb.setLength(0);

            listOrder = ordHandler.getARTransactionsDayReport(null, mDate);
            if (listOrder.size() > 0) {
                sb.append(textHandler.newLines(2));
                sb.append(textHandler.centeredString("A/R Transactions", lineWidth));
                sb.append(textHandler.threeColumnLineItem("ID", 40,
                        "Customer", 40, "Amount", 20,
                        lineWidth, 0));
                for (Order ord : listOrder) {
                    if (ord.ord_id != null)
                        sb.append(textHandler.threeColumnLineItem(ord.ord_id, 40,
                                ord.cust_name, 40, Global.getCurrencyFormat(
                                        ord.ord_total), 20, lineWidth, 0));
                }
                listOrder.clear();

                sb.append(textHandler.newLines(1));
                report.setArTransactions(sb.toString());
                sb.setLength(0);
            }

            List<Shift> listShifts = ShiftDAO.getShift(DateUtils.getDateStringAsDate(currentDate,
                    DateUtils.DATE_yyyy_MM_dd));
            if (listShifts.size() > 0) {
                sb.append(textHandler.newLines(2));
                sb.append(textHandler.centeredString("Totals By Shift", lineWidth));
                sb.append(textHandler.newLines(1));
                for (Shift shift : listShifts) {
                    Clerk clerk = ClerkDAO.getByEmpId(shift.getClerkId());
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Sales Clerk",
                            clerk.getEmpName(), lineWidth, 0));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("From",
                            DateUtils.getDateAsString(shift.getStartTime(), DateUtils.DATE_yyyy_MM_dd),
                            lineWidth, 0));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("To",
                            DateUtils.getDateAsString(shift.getEndTime(), DateUtils.DATE_yyyy_MM_dd),
                            lineWidth, 0));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(context.getString(R.string.begging_petty_cash),
                            Global.getCurrencyFormat(shift.getBeginningPettyCash()),
                            lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(context.getString(R.string.total_expenses),
                            "(" + Global.getCurrencyFormat(shift.getTotalExpenses()) + ")",
                            lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(context.getString(R.string.ending_petty_cash),
                            Global.getCurrencyFormat(shift.getEndingPettyCash()),
                            lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Transactions Cash",
                            Global.getCurrencyFormat(shift.getTotalTransactionsCash()),
                            lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Ending Cash",
                            Global.getCurrencyFormat(shift.getTotal_ending_cash()),
                            lineWidth, 3));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Entered Close Amount",
                            Global.getCurrencyFormat(shift.getEnteredCloseAmount()),
                            lineWidth, 3));
                    sb.append(textHandler.newLines(1));
                }
                listShifts.clear();

                report.setTotalsByShifts(sb.toString());
                sb.setLength(0);
            }

            sb.append(textHandler.newLines(1));
            sb.append(sb_ord_types);

            report.setTotalsByTypes(sb.toString());
            sb.setLength(0);

            List<OrderProduct> listProd = ordProdHandler.getProductsDayReport(
                    true, null, mDate);
            if (listProd.size() > 0) {
                sb.append(textHandler.newLines(2));
                sb.append(textHandler.centeredString("Items Sold", lineWidth));
                sb.append(textHandler.threeColumnLineItem("Name", 60,
                        "Qty", 20, "Total", 20,
                        lineWidth, 0));

                for (OrderProduct prod : listProd) {
                    String calc;
                    if (new BigDecimal(prod.getOrdprod_qty()).compareTo(new BigDecimal(0)) != 0) {
                        calc = Global.getCurrencyFormat(prod.getFinalPrice());
                    } else {
                        calc = Global.formatDoubleToCurrency(0);
                    }

                    sb.append(textHandler.threeColumnLineItem(prod.getOrdprod_name(), 60,
                            prod.getOrdprod_qty(), 20,
                            calc,
                            20, lineWidth, 0));
                    if (printDetails) {
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText("UPC:" +
                                prod.getProd_upc(), "", lineWidth, 3));
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText("SKU:" +
                                prod.getProd_sku(), "", lineWidth, 3));
                    }
                }
                listProd.clear();

                sb.append(textHandler.newLines(1));
                report.setItemsSold(sb.toString());
                sb.setLength(0);
            }

            listProd = ordProdHandler.getDepartmentDayReport(true, null, mDate);
            if (listProd.size() > 0) {
                sb.append(textHandler.newLines(2));
                sb.append(textHandler.centeredString("Department Sales", lineWidth));
                sb.append(textHandler.threeColumnLineItem("Name", 60, "Qty",
                        20, "Total", 20, lineWidth, 0));
                for (OrderProduct prod : listProd) {
                    sb.append(textHandler.threeColumnLineItem(prod.getCat_name(), 60,
                            prod.getOrdprod_qty(), 20, Global.getCurrencyFormat(
                                    prod.getFinalPrice()), 20, lineWidth, 0));
                }
                listProd.clear();

                sb.append(textHandler.newLines(1));
                report.setDepartmentSales(sb.toString());
                sb.setLength(0);
            }

            listProd = ordProdHandler.getDepartmentDayReport(false, null, mDate);
            if (listProd.size() > 0) {
                sb.append(textHandler.newLines(2));
                sb.append(textHandler.centeredString("Department Returns", lineWidth));
                sb.append(textHandler.threeColumnLineItem("Name", 60, "Qty",
                        20, "Total", 20, lineWidth, 0));
                for (OrderProduct prod : listProd) {
                    sb.append(textHandler.threeColumnLineItem(prod.getCat_name(), 60,
                            prod.getOrdprod_qty(), 20, Global.getCurrencyFormat(
                                    prod.getFinalPrice()), 20, lineWidth, 0));
                }
                listProd.clear();

                sb.append(textHandler.newLines(1));
                report.setDepartmentReturns(sb.toString());
                sb.setLength(0);
            }

            List<Payment> listPayments = paymentHandler.getPaymentsGroupDayReport(
                    0, null, mDate);
            if (listPayments.size() > 0) {
                sb.append(textHandler.newLines(2));
                sb.append(textHandler.centeredString("Payments", lineWidth));
                sb.append(textHandler.newLines(1));
                for (Payment payment : listPayments) {
                    sb.append(textHandler.oneColumnLineWithLeftAlignedText(
                            payment.getCard_type(), lineWidth, 0));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            "Amount", Global.getCurrencyFormat(payment.getPay_amount()),
                            lineWidth, 2));
                    if (printDetails) {
                        //check if tip should be printed
                        if (showTipField) {
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Tip",
                                    Global.getCurrencyFormat(payment.getPay_tip()),
                                    lineWidth, 2));
                        }
                        sb.append(textHandler.oneColumnLineWithLeftAlignedText("Details",
                                lineWidth, 3));
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText("ID",
                                payment.getPay_id(), lineWidth, 4));
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Amount",
                                Global.getCurrencyFormat(payment.getPay_amount()),
                                lineWidth, 4));
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Invoice",
                                payment.getJob_id(), lineWidth, 4));
                        sb.append(textHandler.newLines(1));

                        report.setPayments(sb.toString());
                        sb.setLength(0);
                    }
                }
                listPayments.clear();
            }

            listPayments = paymentHandler.getPaymentsGroupDayReport(1, null, mDate);
            if (listPayments.size() > 0) {
                sb.append(textHandler.newLines(2));
                sb.append(textHandler.centeredString("Voids", lineWidth));
                sb.append(textHandler.newLines(1));
                for (Payment payment : listPayments) {
                    sb.append(textHandler.oneColumnLineWithLeftAlignedText(payment.getCard_type(),
                            lineWidth, 0));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Amount",
                            Global.getCurrencyFormat(payment.getPay_amount()),
                            lineWidth, 2));
                    if (printDetails) {
                        //check if tip should be printed
                        if (showTipField) {
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Tip",
                                    Global.getCurrencyFormat(payment.getPay_tip()),
                                    lineWidth, 2));
                        }
                        sb.append(textHandler.oneColumnLineWithLeftAlignedText("Details",
                                lineWidth, 3));
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText("ID",
                                payment.getPay_id(), lineWidth, 4));
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Amount",
                                Global.getCurrencyFormat(payment.getPay_amount()),
                                lineWidth, 4));
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Invoice",
                                payment.getJob_id(), lineWidth, 4));
                        sb.append(textHandler.newLines(1));
                    }
                }

                report.setVoids(sb.toString());
                sb.setLength(0);

                listPayments.clear();
            }

            listPayments = paymentHandler.getPaymentsGroupDayReport(2, null, mDate);
            if (listPayments.size() > 0) {
                sb.append(textHandler.newLines(2));
                sb.append(textHandler.centeredString("Refunds", lineWidth));
                sb.append(textHandler.newLines(1));
                for (Payment payment : listPayments) {
                    sb.append(textHandler.oneColumnLineWithLeftAlignedText(payment.getCard_type(),
                            lineWidth, 0));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText("Amount",
                            Global.getCurrencyFormat(payment.getPay_amount()),
                            lineWidth, 2));

                    if (printDetails) {
                        //check if tip should be printed
                        if (showTipField) {
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Tip",
                                    Global.getCurrencyFormat(payment.getPay_tip()),
                                    lineWidth, 2));
                        }
                        sb.append(textHandler.oneColumnLineWithLeftAlignedText("Details",
                                lineWidth, 3));
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText("ID",
                                payment.getPay_id(), lineWidth, 4));
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Amount",
                                Global.getCurrencyFormat(payment.getPay_amount()),
                                lineWidth, 4));
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText("Invoice",
                                payment.getJob_id(), lineWidth, 4));
                        sb.append(textHandler.newLines(1));
                    }
                }

                report.setRefunds(sb.toString());
                sb.setLength(0);

                listPayments.clear();
            }

            listProd = ordProdHandler.getProductsDayReport(false, null, mDate);
            if (listProd.size() > 0) {
                sb.append(textHandler.newLines(2));
                sb.append(textHandler.centeredString("Items Returned", lineWidth));
                sb.append(textHandler.threeColumnLineItem("Name", 60,
                        "Qty", 20, "Total", 20,
                        lineWidth, 0));
                for (OrderProduct prod : listProd) {
                    sb.append(textHandler.threeColumnLineItem(prod.getOrdprod_name(),
                            60, prod.getOrdprod_qty(), 20,
                            Global.getCurrencyFormat(prod.getFinalPrice()), 20,
                            lineWidth, 0));
                    if (printDetails) {
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText("UPC:" +
                                prod.getProd_upc(), "", lineWidth, 3));
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText("SKU:" +
                                prod.getProd_sku(), "", lineWidth, 3));
                    }
                }

                sb.append(textHandler.newLines(1));
                report.setItemsReturned(sb.toString());
                sb.setLength(0);

                listProd.clear();
            }

            sb.append(textHandler.newLines(1));
            sb.append(textHandler.centeredString("** End of report **", lineWidth));
            sb.append(textHandler.newLines(4));
            report.setSpecialFooter(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        return report;
    }

    
}
