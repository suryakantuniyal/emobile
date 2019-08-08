package com.android.emobilepos.print;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Base64;

import com.StarMicronics.jasura.JAException;
import com.android.dao.AssignEmployeeDAO;
import com.android.dao.ClerkDAO;
import com.android.dao.ShiftDAO;
import com.android.dao.StoredPaymentsDAO;
import com.android.dao.TermsNConditionsDAO;
import com.android.database.CustomersHandler;
import com.android.database.MemoTextHandler;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.database.ProductsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.DataTaxes;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.PaymentDetails;
import com.android.emobilepos.models.Receipt;
import com.android.emobilepos.models.Tax;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.orders.OrderProduct;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.TermsNConditions;
import com.android.emobilepos.payment.ProcessGenius_FA;
import com.android.support.Customer;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.TaxesCalculator;
import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import plaintext.EMSPlainTextHelper;
import util.StringUtil;

/**
 * Created by Luis Camayd on 8/2/2019.
 */
public class ReceiptBuilder {
    private Context context;
    private MyPreferences myPref;
    private List<String> printPref;
    private int lineWidth;

    public ReceiptBuilder(Context context, int lineWidth) {
        this.context = context;
        this.lineWidth = lineWidth;
        myPref = new MyPreferences(context);
        printPref = myPref.getPrintingPreferences();
    }

    public Receipt getTransaction(Order order, Global.OrderType type,
                                  boolean isFromHistory, boolean isFromOnHold) {

        Receipt receipt = new Receipt();

        try {
            AssignEmployee employee = AssignEmployeeDAO.getAssignEmployee();
            Clerk clerk = ClerkDAO.getByEmpId(Integer.parseInt(myPref.getClerkID()));
            printPref = myPref.getPrintingPreferences();
            OrderProductsHandler orderProductsHandler = new OrderProductsHandler(context);
            List<DataTaxes> listOrdTaxes = order.getListOrderTaxes();
            List<OrderProduct> orderProducts = order.getOrderProducts();
            EMSPlainTextHelper textHandler = new EMSPlainTextHelper();

            StringBuilder sb = new StringBuilder();
            boolean payWithLoyalty = false;

            receipt.setMerchantLogo(getMerchantLogo());

            receipt.setMerchantHeader(getMerchantHeader(textHandler));

            if (order.isVoid.equals("1")) {
                sb.append(textHandler.centeredString("*** VOID ***", lineWidth));
                sb.append(textHandler.newLines(1));
                receipt.setSpecialHeader(sb.toString());
                sb.setLength(0);
            }

            if (isFromOnHold) {
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                        "[" + context.getString(R.string.on_hold) + "]",
                        order.ord_HoldName, lineWidth, 0));
            }

            switch (type) {
                case ORDER: // Order
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.order) + ":", order.ord_id,
                            lineWidth, 0));
                    break;
                case RETURN: // Return
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.return_tag) + ":", order.ord_id,
                            lineWidth, 0));
                    break;
                case INVOICE: // Invoice
                case CONSIGNMENT_INVOICE:// Consignment Invoice
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.invoice) + ":", order.ord_id,
                            lineWidth, 0));
                    break;
                case ESTIMATE: // Estimate
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.estimate) + ":", order.ord_id,
                            lineWidth, 0));
                    break;
                case SALES_RECEIPT: // Sales Receipt
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.sales_receipt) + ":", order.ord_id,
                            lineWidth, 0));
                    break;
            }

            sb.append(textHandler.newLines(1));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                    context.getString(R.string.receipt_date),
                    Global.formatToDisplayDate(order.ord_timeStarted, 3),
                    lineWidth, 0));

            if (ShiftDAO.isShiftOpen() && myPref.isUseClerks()) {
                String clerk_id = order.clerk_id;
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                        context.getString(R.string.receipt_clerk),
                        clerk.getEmpName() + "(" + clerk_id + ")",
                        lineWidth, 0));
            }

            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                    context.getString(R.string.receipt_employee),
                    employee.getEmpName() + "(" + employee.getEmpId() + ")",
                    lineWidth, 0));

            String custName = getCustName(order.cust_id);
            if (custName != null && !custName.isEmpty()) {
                if (!TextUtils.isEmpty(custName)) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.receipt_customer), custName,
                            lineWidth, 0));
                }
            }

            custName = getCustAccount(order.cust_id);
            if (printPref.contains(MyPreferences.print_customer_id) &&
                    custName != null && !TextUtils.isEmpty(custName)) {
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                        context.getString(R.string.receipt_customer_id),
                        custName, lineWidth, 0));
            }

            String ordComment = order.ord_comment;
            if (!TextUtils.isEmpty(ordComment)) {
                sb.append(textHandler.newLines(2));
                sb.append("Comments:");
                sb.append(textHandler.newLines(1));
                sb.append(textHandler.oneColumnLineWithLeftAlignedText(
                        ordComment, lineWidth, 3));
            }

            receipt.setHeader(sb.toString());
            sb.setLength(0);

            int totalItemstQty = 0;
            int size = orderProducts.size();
            if (!myPref.getPreferences(MyPreferences.pref_wholesale_printout)) {
                boolean isRestMode = myPref.isRestaurantMode();

                for (int i = 0; i < size; i++) {
                    if (!TextUtils.isEmpty(orderProducts.get(i).getProd_price_points()) &&
                            Integer.parseInt(orderProducts.get(i).getProd_price_points()) > 0) {
                        payWithLoyalty = true;
                    }
                    totalItemstQty += TextUtils.isEmpty(orderProducts.get(i).getOrdprod_qty()) ?
                            0 : Double.parseDouble(orderProducts.get(i).getOrdprod_qty());
                    String uomDescription = "";
                    if (!TextUtils.isEmpty(orderProducts.get(i).getUom_name())) {
                        uomDescription = orderProducts.get(i).getUom_name() +
                                "(" + orderProducts.get(i).getUom_conversion() + ")";
                    }
                    if (isRestMode) {
                        if (!orderProducts.get(i).isAddon()) {
                            sb.append(textHandler.oneColumnLineWithLeftAlignedText(
                                    orderProducts.get(i).getOrdprod_qty() + "x " +
                                            orderProducts.get(i).getOrdprod_name() + " " +
                                            uomDescription, lineWidth, 1));
                            if (orderProducts.get(i).getHasAddons()) {
                                List<OrderProduct> addons = orderProductsHandler
                                        .getOrderProductAddons(orderProducts.get(i)
                                                .getOrdprod_id());
                                for (OrderProduct addon : addons) {
                                    if (addon.isAdded()) {
                                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                                " >" + addon.getOrdprod_name(),
                                                Global.getCurrencyFormat(addon.getFinalPrice()),
                                                lineWidth, 2));
                                    } else {
                                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                                " >NO " + addon.getOrdprod_name(),
                                                Global.getCurrencyFormat(addon.getFinalPrice()),
                                                lineWidth, 2));
                                    }
                                }
                            }

                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                    context.getString(R.string.receipt_price),
                                    Global.getCurrencyFormat(String.valueOf(orderProducts.get(i)
                                            .getItemTotalCalculated())), lineWidth, 3));
                            if (orderProducts.get(i).getDiscount_id() != null &&
                                    !TextUtils.isEmpty(orderProducts.get(i).getDiscount_id())) {
                                ProductsHandler productDBHandler = new ProductsHandler(context);
                                String discountName = productDBHandler.getDiscountName(
                                        orderProducts.get(i).getDiscount_id());
                                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                        context.getString(R.string.receipt_discount) +
                                                " " + discountName,
                                        Global.getCurrencyFormat(orderProducts.get(i)
                                                .getDiscount_value()), lineWidth, 3));
                            }
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                    context.getString(R.string.receipt_total),
                                    Global.getCurrencyFormat(orderProducts.get(i).getItemTotal()),
                                    lineWidth, 3));

                            List<OrderProduct> giftcardvalues = orderProductsHandler
                                    .getOrdProdGiftCardNumber(orderProducts.get(i).getOrdprod_id());
                            for (OrderProduct giftCard : giftcardvalues) {
                                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                        giftCard.getGiftcardName() + ":",
                                        giftCard.getGiftcardNumber(), lineWidth, 3));
                            }

                            if (printPref.contains(MyPreferences.print_descriptions)) {
                                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                        context.getString(R.string.receipt_description),
                                        "", lineWidth, 3));
                                sb.append(textHandler.oneColumnLineWithLeftAlignedText(
                                        orderProducts.get(i).getOrdprod_desc(),
                                        lineWidth, 5));
                            }
                        }
                    } else {
                        sb.append(textHandler.oneColumnLineWithLeftAlignedText(
                                orderProducts.get(i).getOrdprod_qty()
                                        + "x " + orderProducts.get(i).getOrdprod_name()
                                        + " " + uomDescription, lineWidth, 1));
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                context.getString(R.string.receipt_price),
                                Global.getCurrencyFormat(orderProducts.get(i).getFinalPrice()),
                                lineWidth, 3));

                        if (orderProducts.get(i).getDiscount_id() != null && !TextUtils.isEmpty(
                                orderProducts.get(i).getDiscount_id())) {
                            ProductsHandler productDBHandler = new ProductsHandler(context);
                            String discountName = productDBHandler.getDiscountName(
                                    orderProducts.get(i).getDiscount_id());
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                    context.getString(R.string.receipt_discount) +
                                            " " + discountName,
                                    Global.getCurrencyFormat(orderProducts.get(i)
                                            .getDiscount_value()),
                                    lineWidth, 3));
                        }

                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                context.getString(R.string.receipt_total),
                                Global.getCurrencyFormat(orderProducts.get(i).getItemTotal()),
                                lineWidth, 3));

                        List<OrderProduct> giftcardvalues = orderProductsHandler
                                .getOrdProdGiftCardNumber(orderProducts.get(i).getOrdprod_id());
                        for (OrderProduct giftCard : giftcardvalues) {
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                    giftCard.getGiftcardName() + ":",
                                    giftCard.getGiftcardNumber(), lineWidth, 3));
                        }

                        if (printPref.contains(MyPreferences.print_descriptions)) {
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                    context.getString(R.string.receipt_description),
                                    "", lineWidth, 3));
                            sb.append(textHandler.oneColumnLineWithLeftAlignedText(
                                    orderProducts.get(i).getOrdprod_desc(),
                                    lineWidth, 5));
                        }
                    }
                    receipt.getItems().add((sb.toString()));
                    sb.setLength(0);
                }
            } else {
                int padding = lineWidth / 4;
                String tempor = Integer.toString(padding);
                StringBuilder tempSB = new StringBuilder();
                tempSB.append("%").append(tempor).append("s").append("%").append(tempor)
                        .append("s").append("%").append(tempor).append("s").append("%")
                        .append(tempor).append("s");
                sb.append(String.format(tempSB.toString(), "Item", "Qty", "Price", "Total"));
                sb.append(textHandler.newLines(1));

                for (int i = 0; i < size; i++) {
                    if (!TextUtils.isEmpty(orderProducts.get(i).getProd_price_points()) &&
                            Integer.parseInt(orderProducts.get(i).getProd_price_points()) > 0) {
                        payWithLoyalty = true;
                    }
                    totalItemstQty += TextUtils.isEmpty(orderProducts.get(i).getOrdprod_qty()) ? 0
                            : Double.parseDouble(orderProducts.get(i).getOrdprod_qty());
                    sb.append(orderProducts.get(i).getOrdprod_name()).append("-")
                            .append(orderProducts.get(i).getOrdprod_desc());
                    sb.append(textHandler.newLines(1));

                    sb.append(String.format("Discount %s", Global.getCurrencyFormat(
                            orderProducts.get(i).getDiscountTotal().toString())));
                    sb.append(textHandler.newLines(1));
                    sb.append(String.format(tempSB.toString(), "   ",
                            orderProducts.get(i).getOrdprod_qty(),
                            Global.getCurrencyFormat(orderProducts.get(i).getFinalPrice()),
                            Global.getCurrencyFormat(orderProducts.get(i).getItemTotal())));
                    sb.append(textHandler.newLines(1));
                    receipt.getItems().add((sb.toString()));
                    sb.setLength(0);
                }
            }

            sb.append(textHandler.lines(lineWidth));
            sb.append(textHandler.newLines(2));
            receipt.setSeparator(sb.toString());
            sb.setLength(0);

            BigDecimal itemDiscTotal = new BigDecimal(0);
            for (OrderProduct orderProduct : orderProducts) {
                try {
                    itemDiscTotal = itemDiscTotal.add(Global.getBigDecimalNum(
                            orderProduct.getDiscount_value()));
                } catch (NumberFormatException e) {
                    itemDiscTotal = new BigDecimal(0);
                }
            }
            double saveAmount = itemDiscTotal.add(TextUtils.isEmpty(order.ord_discount) ?
                    new BigDecimal(0) : new BigDecimal(order.ord_discount)).doubleValue();
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(context.getString(
                    R.string.receipt_subtotal),
                    Global.getCurrencyFormat(Global.getBigDecimalNum(order.ord_subtotal)
                            .add(itemDiscTotal).toString()), lineWidth, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(context.getString(
                    R.string.receipt_discount_line_item),
                    Global.getCurrencyFormat(String.valueOf(itemDiscTotal)),
                    lineWidth, 0));

            String discountName = "";
            if (order.ord_discount_id != null && !order.ord_discount_id.isEmpty()) {
                ProductsHandler productDBHandler = new ProductsHandler(context);
                discountName = productDBHandler.getDiscountName(order.ord_discount_id);
            }
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                    context.getString(R.string.receipt_global_discount) +
                            " " + discountName,
                    Global.getCurrencyFormat(order.ord_discount), lineWidth, 0));

            sb.append(textHandler.twoColumnLineWithLeftAlignedText(context.getString(
                    R.string.receipt_tax),
                    Global.getCurrencyFormat(order.ord_taxamount), lineWidth, 0));

            receipt.setTotals(sb.toString());
            sb.setLength(0);

            if (myPref.getPreferences(MyPreferences.pref_print_taxes_breakdown)) {
                if (myPref.isRetailTaxes()) {
                    HashMap<String, String[]> prodTaxes = new HashMap<>();
                    for (OrderProduct product : order.getOrderProducts()) {
                        if (product.getTaxes() != null) {
                            for (Tax tax : product.getTaxes()) {
                                if (prodTaxes.containsKey(tax.getTaxRate())) {
                                    BigDecimal taxAmount = new BigDecimal(
                                            prodTaxes.get(tax.getTaxRate())[1]);
                                    taxAmount = taxAmount.add(TaxesCalculator.taxRounder(
                                            tax.getTaxAmount()));
                                    String[] arr = new String[2];
                                    arr[0] = tax.getTaxName();
                                    arr[1] = String.valueOf(taxAmount);
                                    prodTaxes.put(tax.getTaxRate(), arr);
                                } else {
                                    BigDecimal taxAmount = TaxesCalculator.taxRounder(
                                            tax.getTaxAmount());
                                    String[] arr = new String[2];
                                    arr[0] = tax.getTaxName();
                                    arr[1] = String.valueOf(taxAmount);
                                    prodTaxes.put(tax.getTaxRate(), arr);
                                }
                            }
                        }
                    }
                    Iterator it = prodTaxes.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, String[]> pair = (Map.Entry<String, String[]>) it.next();

                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(pair.getValue()[0],
                                Global.getCurrencyFormat(String.valueOf(pair.getValue()[1])),
                                lineWidth, 2));
                        it.remove();
                    }
                } else if (listOrdTaxes != null) {
                    for (DataTaxes tax : listOrdTaxes) {
                        BigDecimal taxAmount = new BigDecimal(0);
                        List<BigDecimal> rates = new ArrayList<>();
                        rates.add(new BigDecimal(tax.getTax_rate()));
                        for (OrderProduct product : order.getOrderProducts()) {
                            taxAmount = taxAmount.add(TaxesCalculator.calculateTax(
                                    product.getProductPriceTaxableAmountCalculated(), rates));
                        }
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(tax.getTax_name(),
                                Global.getCurrencyFormat(String.valueOf(taxAmount)),
                                lineWidth, 2));
                    }
                }

                receipt.setTaxes(sb.toString());
                sb.setLength(0);
            }

            sb.append(textHandler.newLines(1));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                    context.getString(R.string.receipt_itemsQtyTotal),
                    String.valueOf(totalItemstQty), lineWidth, 0));
            sb.append(textHandler.newLines(1));

            receipt.setTotalItems(sb.toString());
            sb.setLength(0);

            String granTotal = "0";
            if (!TextUtils.isEmpty(order.gran_total)) {
                granTotal = order.gran_total;
            } else if (!TextUtils.isEmpty(order.ord_total)) {
                granTotal = order.ord_total;
            }
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                    context.getString(R.string.receipt_grandtotal),
                    Global.getCurrencyFormat(granTotal), lineWidth, 0));
            sb.append(textHandler.newLines(1));

            receipt.setGrandTotal(sb.toString());
            sb.setLength(0);

            PaymentsHandler payHandler = new PaymentsHandler(context);
            List<PaymentDetails> detailsList = payHandler
                    .getPaymentForPrintingTransactions(order.ord_id);
            if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward)) {
                detailsList.addAll(StoredPaymentsDAO
                        .getPaymentForPrintingTransactions(order.ord_id));
            }
            size = detailsList.size();

            double tempGrandTotal = Double.parseDouble(granTotal);
            double tempAmount = 0;
            if (size == 0) {
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                        context.getString(R.string.receipt_amountpaid),
                        Global.formatDoubleToCurrency(tempAmount), lineWidth, 0));

                if (type == Global.OrderType.INVOICE) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.receipt_balance_due),
                            Global.formatDoubleToCurrency(tempGrandTotal - tempAmount),
                            lineWidth, 0));
                }
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                        context.getString(R.string.receipt_total_tip_paid),
                        Global.formatDoubleToCurrency(0.00),
                        lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                        context.getString(R.string.receipt_amountreturned),
                        Global.formatDoubleToCurrency(0.00), lineWidth, 0));
            } else {
                double paidAmount = 0;
                double tempTipAmount = 0;
                double totalAmountTendered = 0;

                StringBuilder tempSB = new StringBuilder();
                for (int i = 0; i < size; i++) {
                    String _pay_type = detailsList.get(i).getPaymethod_name().toUpperCase(
                            Locale.getDefault()).trim();
                    tempAmount = tempAmount + formatStrToDouble(
                            detailsList.get(i).getPay_amount());
                    if (Payment.PaymentType.getPaymentTypeByCode(
                            detailsList.get(i).getPayType()) != Payment.PaymentType.VOID) {
                        paidAmount += formatStrToDouble(detailsList.get(i).getPay_amount());
                    }
                    totalAmountTendered += detailsList.get(i).getAmountTender();
                    tempTipAmount = tempTipAmount + formatStrToDouble(
                            detailsList.get(i).getPay_tip());
                    tempSB.append(textHandler
                            .oneColumnLineWithLeftAlignedText(
                                    Global.getCurrencyFormat(
                                            detailsList.get(i).getPay_amount())
                                            + "[" + detailsList.get(i).getPaymethod_name() + "]",
                                    lineWidth, 1));
                    if (!_pay_type.equals("CASH") && !_pay_type.equals("CHECK")) {
                        tempSB.append(textHandler.oneColumnLineWithLeftAlignedText(
                                "TransID: " + StringUtil.nullStringToEmpty(
                                        detailsList.get(i).getPay_transid()),
                                lineWidth, 1));
                        tempSB.append(textHandler.oneColumnLineWithLeftAlignedText(
                                "CC#: *" + detailsList.get(i).getCcnum_last4(),
                                lineWidth, 1));
                    } else {
                        tempSB.append(textHandler
                                .oneColumnLineWithLeftAlignedText(
                                        context.getString(R.string.amount_tendered) +
                                                Global.formatDoubleToCurrency(
                                                        detailsList.get(i).getAmountTender()),
                                        lineWidth, 1));
                        tempSB.append(textHandler
                                .oneColumnLineWithLeftAlignedText(
                                        context.getString(R.string.changeLbl) +
                                                Global.formatDoubleToCurrency(
                                                        detailsList.get(i).getAmountTender() -
                                                                formatStrToDouble(detailsList.get(i)
                                                                        .getPay_amount())),
                                        lineWidth, 1));
                    }
                }
                if (type == Global.OrderType.ORDER) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.receipt_amountreturned),
                            Global.formatDoubleToCurrency(tempAmount), lineWidth, 0));
                } else {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.receipt_amountpaid),
                            Global.getCurrencyFormat(Double.toString(paidAmount)),
                            lineWidth, 0));
                }
                sb.append(tempSB.toString());
                if (type == Global.OrderType.INVOICE) // Invoice
                {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.receipt_balance_due),
                            Global.formatDoubleToCurrency(tempGrandTotal - tempAmount),
                            lineWidth, 0));
                }
                if (type != Global.OrderType.ORDER) {
                    if (myPref.isRestaurantMode() &&
                            myPref.getPreferences(MyPreferences.pref_enable_togo_eatin)) {
                        if (tempTipAmount == 0) {
                            sb.append(textHandler.newLines(1));
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                    context.getString(R.string.receipt_total_tip_paid),
                                    textHandler.lines(lineWidth / 2),
                                    lineWidth, 0));
                            sb.append(textHandler.newLines(1));
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                    context.getString(R.string.receipt_total),
                                    textHandler.lines(lineWidth / 2),
                                    lineWidth, 0));
                        } else {
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                    context.getString(R.string.receipt_total_tip_paid),
                                    Global.getCurrencyFormat(Double.toString(tempTipAmount)),
                                    lineWidth, 0));
                        }
                    } else {
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                context.getString(R.string.receipt_total_tip_paid),
                                Global.getCurrencyFormat(Double.toString(tempTipAmount)),
                                lineWidth, 0));
                    }

                    if (type == Global.OrderType.RETURN) {
                        tempAmount = paidAmount;
                    } else if (tempGrandTotal >= totalAmountTendered) {
                        tempAmount = 0.00;
                    } else {
                        if (tempGrandTotal > 0) {
                            tempAmount = totalAmountTendered - tempGrandTotal;
                        } else {
                            tempAmount = Math.abs(tempGrandTotal);
                        }
                    }
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.receipt_amountreturned),
                            Global.getCurrencyFormat(Double.toString(tempAmount)),
                            lineWidth, 0));
                    sb.append(textHandler.newLines(1));
                }
            }

            sb.append(textHandler.newLines(1));
            receipt.setPaymentsDetails(sb.toString());
            sb.setLength(0);

            if (type != Global.OrderType.ORDER && saveAmount > 0) {
                sb.append(textHandler.ivuLines(lineWidth));
                sb.append(textHandler.newLines(1));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                        context.getString(R.string.receipt_youSave),
                        Global.getCurrencyFormat(String.valueOf(saveAmount)),
                        lineWidth, 0));
                sb.append(textHandler.newLines(1));
                sb.append(textHandler.ivuLines(lineWidth));
                receipt.setYouSave(sb.toString());
                sb.setLength(0);
            }

            if (Global.isIvuLoto && detailsList.size() > 0) {
                sb.append(textHandler.ivuLines(2 * lineWidth / 3));
                sb.append(textHandler.newLines(1));
                sb.append(context.getString(R.string.ivuloto_control_label));
                sb.append(detailsList.get(0).getIvuLottoNumber());
                sb.append(textHandler.newLines(1));
                sb.append(context.getString(R.string.enabler_prefix));
                sb.append(textHandler.newLines(1));
                sb.append(context.getString(R.string.powered_by_enabler));
                sb.append(textHandler.newLines(1));
                sb.append(textHandler.ivuLines(2 * lineWidth / 3));
                sb.append(textHandler.newLines(2));

                receipt.setIvuLoto(sb.toString());
                sb.setLength(0);
            }

            if (printPref.contains(MyPreferences.print_footer)) {
                MemoTextHandler handler = new MemoTextHandler(context);
                String[] footer = handler.getFooter();

                if (!TextUtils.isEmpty(footer[0]))
                    sb.append(textHandler.centeredString(footer[0], lineWidth));
                if (!TextUtils.isEmpty(footer[1]))
                    sb.append(textHandler.centeredString(footer[1], lineWidth));
                if (!TextUtils.isEmpty(footer[2]))
                    sb.append(textHandler.centeredString(footer[2], lineWidth));

                if (!TextUtils.isEmpty(sb.toString())) {
                    sb.append(textHandler.newLines(1));
                    receipt.setMerchantFooter(sb.toString());
                    sb.setLength(0);
                }
            }

            sb.append(textHandler.newLines(1));

            if (payWithLoyalty && Global.loyaltyCardInfo != null &&
                    !TextUtils.isEmpty(Global.loyaltyCardInfo.getCardNumAESEncrypted())
                    && !TextUtils.isEmpty(Global.loyaltyCardInfo.getCardLast4())) {
                sb.append(String.format("%s *%s", context.getString(R.string.receipt_cardnum),
                        Global.loyaltyCardInfo.getCardLast4()));
                sb.append(textHandler.newLines(1));
                sb.append(String.format("%s %s %s", context.getString(R.string.receipt_point_used),
                        Global.loyaltyCharge, context.getString(R.string.points)));
                sb.append(textHandler.newLines(1));
                sb.append(String.format("%s %s %s", context.getString(R.string.receipt_reward_balance),
                        Global.loyaltyPointsAvailable, context.getString(R.string.points)));
                sb.append(textHandler.newLines(2));
                receipt.setLoyaltyDetails(sb.toString());
                sb.setLength(0);
            }

            if (Global.rewardCardInfo != null &&
                    !TextUtils.isEmpty(Global.rewardCardInfo.getCardNumAESEncrypted())
                    && !TextUtils.isEmpty(Global.rewardCardInfo.getCardLast4())) {
                sb.append(String.format("%s *%s", context.getString(R.string.receipt_cardnum),
                        Global.rewardCardInfo.getCardLast4()));
                sb.append(textHandler.newLines(1));
                sb.append(String.format("%s %s %s", context.getString(R.string.receipt_reward_balance),
                        Global.rewardCardInfo.getOriginalTotalAmount(), context.getString(R.string.points)));
                sb.append(textHandler.newLines(2));
                receipt.setRewardsDetails(sb.toString());
                sb.setLength(0);
            }

            String receiptSignature = order.ord_signature;
            if (!TextUtils.isEmpty(receiptSignature)) {
                if (!TextUtils.isEmpty(receiptSignature)) {
                    byte[] img = Base64.decode(receiptSignature, Base64.DEFAULT);
                    receipt.setSignatureImage(
                            BitmapFactory.decodeByteArray(img, 0, img.length));
                }
                sb.append("x").append(textHandler.lines(lineWidth / 2));
                sb.append(textHandler.newLines(1));
                sb.append(context.getString(R.string.receipt_signature)).append(textHandler.newLines(1));
                receipt.setSignature(sb.toString());
                sb.setLength(0);
            }

            if (isFromHistory) {
                sb.setLength(0);
                sb.append(textHandler.centeredString("*** Copy ***", lineWidth));
                sb.append(textHandler.newLines(1));
                receipt.setSpecialFooter(sb.toString());
                sb.setLength(0);
            }

            if (printPref.contains(MyPreferences.print_terms_conditions)) {
                List<TermsNConditions> termsNConditions = TermsNConditionsDAO.getTermsNConds();
                if (termsNConditions != null) {
                    for (TermsNConditions terms : termsNConditions) {
                        sb.append(terms.getTcTerm());
                    }
                    sb.append(textHandler.newLines(1));
                    receipt.setTermsAndConditions(sb.toString());
                    sb.setLength(0);
                }
            }

            if (myPref.isPrintWebSiteFooterEnabled()) {
                sb.append(textHandler.newLines(1));
                sb.append(textHandler.centeredString(
                        context.getString(R.string.enabler_website), lineWidth));
                sb.append(textHandler.newLines(4));
                receipt.setEnablerWebsite(sb.toString());
                sb.setLength(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        return receipt;
    }

    public Receipt getTransaction(String orderId, Global.OrderType type,
                                  boolean isFromHistory, boolean isFromOnHold) {

        OrdersHandler orderHandler = new OrdersHandler(context);
        Order order = orderHandler.getPrintedOrder(orderId);

        return getTransaction(order, type, isFromHistory, isFromOnHold);
    }

    public Receipt getPaymentDetails(String paymentId, int type,
                                     boolean isReprint, EMVContainer emvContainer) {

        Receipt receipt = new Receipt();

        try {
            EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
            PaymentsHandler payHandler = new PaymentsHandler(context);
            Spanned fromHtml = null;
            if (emvContainer != null && emvContainer.getHandpointResponse() != null &&
                    emvContainer.getHandpointResponse().getCustomerReceipt() != null) {
                fromHtml = Html.fromHtml(emvContainer.getHandpointResponse().getCustomerReceipt());
            }
            PaymentDetails payArray;
            boolean isStoredFwd = false;
            long pay_count = payHandler.paymentExist(paymentId, true);
            if (pay_count == 0) {
                isStoredFwd = true;
                if (emvContainer != null && emvContainer.getGeniusResponse() != null &&
                        emvContainer.getGeniusResponse().getStatus()
                                .equalsIgnoreCase("DECLINED")) {
                    type = 2;
                }
                payArray = StoredPaymentsDAO.getPrintingForPaymentDetails(paymentId, type);
            } else {
                payArray = payHandler.getPrintingForPaymentDetails(paymentId, type);
            }
            StringBuilder sb = new StringBuilder();
            boolean isCashPayment = false;
            boolean isCheckPayment = false;
            String includedTip = null;
            String creditCardFooting = "";
            if (payArray.getPaymethod_name() != null &&
                    payArray.getPaymethod_name().toUpperCase(
                            Locale.getDefault()).trim().equals("CASH"))
                isCashPayment = true;
            else if (payArray.getPaymethod_name() != null &&
                    payArray.getPaymethod_name().toUpperCase(
                            Locale.getDefault()).trim().equals("CHECK"))
                isCheckPayment = true;
            else {
                includedTip = context.getString(R.string.receipt_included_tip);
                creditCardFooting = context.getString(R.string.receipt_creditcard_terms);
            }

            receipt.setMerchantLogo(getMerchantLogo());

            if (fromHtml == null) {
                receipt.setMerchantHeader(getMerchantHeader(textHandler));
                sb.append("* ").append(payArray.getPaymethod_name());
                if (payArray.getIs_refund() != null && payArray.getIs_refund().equals("1"))
                    sb.append(" Refund *\n");
                else
                    sb.append(" Sale *\n");
//                print(textHandler.centeredString(sb.toString(), lineWidth));
//
//                sb.setLength(0);
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                        context.getString(R.string.receipt_date),
                        context.getString(R.string.receipt_time), lineWidth, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(payArray.getPay_date(),
                        payArray.getPay_timecreated(), lineWidth, 0))
                        .append("\n");

                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                        context.getString(R.string.receipt_customer),
                        getCustName(payArray.getCustomerId()),
                        lineWidth, 0));

                if (payArray.getJob_id() != null && !TextUtils.isEmpty(payArray.getJob_id()))
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.receipt_order_id),
                            payArray.getJob_id(), lineWidth, 0));
                else if (payArray.getInv_id() != null && !TextUtils.isEmpty(payArray.getInv_id()))
                    // invoice
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.receipt_invoice_ref),
                            payArray.getInv_id(), lineWidth, 0));

                if (!isStoredFwd)
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.receipt_idnum), paymentId,
                            lineWidth, 0));

                if (!isCashPayment && !isCheckPayment) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.receipt_cardnum),
                            "*" + payArray.getCcnum_last4(), lineWidth, 0));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            "TransID:", payArray.getPay_transid(),
                            lineWidth, 0)).append("\n");
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            "Auth Code:", payArray.getAuthcode(),
                            lineWidth, 0)).append("\n");
                } else if (isCheckPayment) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.receipt_checknum),
                            payArray.getPay_check(), lineWidth, 0));
                }

                receipt.setHeader(sb.toString());
                sb.setLength(0);

                receipt.setEmvDetails(getEmvDetails(payArray.getEmvContainer(), textHandler));

                String status = payArray.getEmvContainer() != null &&
                        payArray.getEmvContainer().getGeniusResponse() != null ?
                        payArray.getEmvContainer().getGeniusResponse().getStatus() :
                        context.getString(R.string.approved);
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                        context.getString(R.string.credit_approval_status),
                        status, lineWidth, 0));
                sb.append(textHandler.newLines(1));
                if (Global.isIvuLoto && Global.subtotalAmount > 0 &&
                        !TextUtils.isEmpty(payArray.getTax1_amount())
                        && !TextUtils.isEmpty(payArray.getTax2_amount())) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.receipt_subtotal),
                            Global.getCurrencyFormat(String.valueOf(Global.subtotalAmount)),
                            lineWidth, 0));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(payArray.getTax1_name(),
                            Global.getCurrencyFormat(payArray.getTax1_amount()),
                            lineWidth, 2));
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(payArray.getTax2_name(),
                            Global.getCurrencyFormat(payArray.getTax2_amount()),
                            lineWidth, 2));
                }

                if (emvContainer != null && emvContainer.getGeniusResponse() != null &&
                        emvContainer.getGeniusResponse().getAmountApproved() != null) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.receipt_amount),
                            Global.getCurrencyFormat(emvContainer.getGeniusResponse()
                                    .getAmountApproved()), lineWidth, 0));
                } else {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.receipt_amount),
                            Global.getCurrencyFormat(payArray.getPay_amount()),
                            lineWidth, 0));
                }
                String change = payArray.getChange();
                if (isCashPayment && isCheckPayment && !TextUtils.isEmpty(change) &&
                        change.contains(".") && Double.parseDouble(change) > 0) {
                    change = "";
                }

                sb.append("\n");

                if (includedTip != null) {
                    if (Double.parseDouble(change) > 0) {
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(includedTip,
                                Global.getCurrencyFormat(change), lineWidth, 0));
                    } else if (myPref.isRestaurantMode() &&
                            myPref.getPreferences(MyPreferences.pref_enable_togo_eatin)) {
                        sb.append(textHandler.newLines(1));
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                context.getString(R.string.receipt_tip),
                                textHandler.lines(lineWidth / 2),
                                lineWidth, 0)).append("\n");
//                        print(sb.toString());
//                        sb.setLength(0);
                        sb.append(textHandler.newLines(1));
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                context.getString(R.string.receipt_total),
                                textHandler.lines(lineWidth / 2),
                                lineWidth, 0)).append("\n");
//                        print(sb.toString());
//                        sb.setLength(0);
                    }
                }
                sb.append("\n");

                receipt.setPaymentsDetails(sb.toString());
                sb.setLength(0);
            } else {
                sb.setLength(0);
                sb.append("\n\n");
                sb.append(fromHtml.toString());

                receipt.setPaymentsDetails(sb.toString());
                sb.setLength(0);
            }

//            sb.setLength(0);
            if (!isCashPayment && !isCheckPayment) {
                if (myPref.getPreferences(MyPreferences.pref_handwritten_signature)) {
                    sb.append(textHandler.newLines(1));
                } else if (payArray.getPay_signature() != null &&
                        !TextUtils.isEmpty(payArray.getPay_signature())) {
                    encodedSignature = payArray.getPay_signature();
                    printImage(1);
                }
                sb.append("\n\nx").append(textHandler.lines(lineWidth / 2)).append("\n");
                sb.append(context.getString(R.string.receipt_signature))
                        .append(textHandler.newLines(1));
                print(sb.toString());
                sb.setLength(0);
            }
            if (Global.isIvuLoto) {
                sb = new StringBuilder();

                if (!printPref.contains(MyPreferences.print_ivuloto_qr)) {
                    printIVULoto(payArray.getIvuLottoNumber(), lineWidth);
                } else {

                    printIVULoto(payArray.getIvuLottoNumber(), lineWidth);

                }
                sb.setLength(0);
            }

            sb.setLength(0);
            printFooter(lineWidth);

            if (fromHtml == null) {
                if (!isCashPayment && !isCheckPayment) {
                    print(creditCardFooting);
                    String temp = textHandler.newLines(1);
                    print(temp);
                }
            }

            sb.setLength(0);
            if (isReprint) {
                sb.append(textHandler.centeredString("*** Copy ***", lineWidth));
                print(sb.toString());
            }
            printTermsNConds();
            printEnablerWebSite(lineWidth);
            cutPaper();
        } catch (JAException e) {
            e.printStackTrace();
        }

        return receipt;
    }

    private Bitmap getMerchantLogo() {
        Bitmap merchantLogo = null;
        File imgFile = new File(myPref.getAccountLogoPath());
        if (imgFile.exists()) {
            merchantLogo = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        }
        return merchantLogo;
    }

    private String getMerchantHeader(EMSPlainTextHelper emsPlainTextHelper) {
        String merchantHeader = null;
        if (printPref.contains(MyPreferences.print_header)) {
            MemoTextHandler handler = new MemoTextHandler(context);
            String[] header = handler.getHeader();
            StringBuilder stringBuilder = new StringBuilder();

            if (!TextUtils.isEmpty(header[0]))
                stringBuilder.append(emsPlainTextHelper.centeredString(header[0], lineWidth));
            if (!TextUtils.isEmpty(header[1]))
                stringBuilder.append(emsPlainTextHelper.centeredString(header[1], lineWidth));
            if (!TextUtils.isEmpty(header[2]))
                stringBuilder.append(emsPlainTextHelper.centeredString(header[2], lineWidth));

            if (!TextUtils.isEmpty(stringBuilder.toString())) {
                stringBuilder.insert(0, emsPlainTextHelper.newLines(1));
                stringBuilder.append(emsPlainTextHelper.newLines(1));
                merchantHeader = stringBuilder.toString();
            }
        }

        return merchantHeader;
    }

    private String getEmvDetails(EMVContainer emvContainer, EMSPlainTextHelper emsPlainTextHelper) {
        String emvDetails = null;

        if (emvContainer != null && emvContainer.getGeniusResponse() != null) {
            if (emvContainer.getGeniusResponse().getAdditionalParameters() != null &&
                    emvContainer.getGeniusResponse()
                            .getAdditionalParameters().getEMV() != null) {

                StringBuilder stringBuilder = new StringBuilder();

                // Entry Method
                String entryMethod = emvContainer.getGeniusResponse()
                        .getAdditionalParameters().getEMV().getEntryModeMessage();
                if (!TextUtils.isEmpty(entryMethod)) {
                    stringBuilder.append(emsPlainTextHelper.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.pax_entry_method),
                            entryMethod, lineWidth, 0));
                }

                // Application Label
                String applicationLabel = emvContainer.getGeniusResponse()
                        .getAdditionalParameters()
                        .getEMV().getApplicationInformation().getApplicationLabel();
                if (!TextUtils.isEmpty(applicationLabel)) {
                    stringBuilder.append(emsPlainTextHelper.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.genius_application_label),
                            applicationLabel, lineWidth, 0));
                }

                // AID
                String aid = emvContainer.getGeniusResponse().getAdditionalParameters()
                        .getEMV().getApplicationInformation().getAid();
                if (!TextUtils.isEmpty(aid)) {
                    stringBuilder.append(emsPlainTextHelper.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.genius_aid),
                            aid, lineWidth, 0));
                }

                if (emvContainer.getGeniusResponse().getPaymentType()
                        .equalsIgnoreCase(ProcessGenius_FA.Limiters.DISCOVER.name()) ||
                        emvContainer.getGeniusResponse().getPaymentType()
                                .equalsIgnoreCase(ProcessGenius_FA.Limiters.AMEX.name()) ||
                        emvContainer.getGeniusResponse().getPaymentType()
                                .equalsIgnoreCase("EMVCo")) {
                    stringBuilder.append(emsPlainTextHelper.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.card_exp_date),
                            emvContainer.getGeniusResponse().getAdditionalParameters()
                                    .getEMV().getCardInformation().getCardExpiryDate(),
                            lineWidth, 0));
                }
                if (emvContainer.getGeniusResponse().getPaymentType()
                        .equalsIgnoreCase(ProcessGenius_FA.Limiters.AMEX.name())) {
                    stringBuilder.append(emsPlainTextHelper.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.cryptogram_type),
                            emvContainer.getGeniusResponse().getAdditionalParameters()
                                    .getEMV().getApplicationCryptogram()
                                    .getCryptogramType(), lineWidth, 0));
                    stringBuilder.append(emsPlainTextHelper.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.cryptogram),
                            emvContainer.getGeniusResponse().getAdditionalParameters()
                                    .getEMV().getApplicationCryptogram().getCryptogram(),
                            lineWidth, 0));
                }

                // PIN Statement
                String pinStatement = emvContainer.getGeniusResponse()
                        .getAdditionalParameters().getEMV().getPINStatement();
                if (!TextUtils.isEmpty(pinStatement)) {
                    stringBuilder.append(emsPlainTextHelper.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.pin_statement),
                            pinStatement, lineWidth, 0));
                }

                // TVR
                String tvr = emvContainer.getGeniusResponse().getAdditionalParameters()
                        .getEMV().getTVR();
                if (!TextUtils.isEmpty(tvr)) {
                    stringBuilder.append(emsPlainTextHelper.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.pax_tvr),
                            tvr, lineWidth, 0));
                }

                // IAD
                String iad = emvContainer.getGeniusResponse().getAdditionalParameters()
                        .getEMV().getIAD();
                if (!TextUtils.isEmpty(iad)) {
                    stringBuilder.append(emsPlainTextHelper.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.pax_iad),
                            iad, lineWidth, 0));
                }

                // TSI
                String tsi = emvContainer.getGeniusResponse().getAdditionalParameters()
                        .getEMV().getTSI();
                if (!TextUtils.isEmpty(tsi)) {
                    stringBuilder.append(emsPlainTextHelper.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.pax_tsi_atc),
                            tsi, lineWidth, 0));
                }

                // AC
                String ac = emvContainer.getGeniusResponse().getAdditionalParameters()
                        .getEMV().getAC();
                if (!TextUtils.isEmpty(ac)) {
                    stringBuilder.append(emsPlainTextHelper.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.pax_ac),
                            ac, lineWidth, 0));
                }

                stringBuilder.append("\n\n");
                emvDetails = stringBuilder.toString();
            }
        }

        return emvDetails;
    }

    private String getCustName(String custId) {
        String name = "";
        if (!TextUtils.isEmpty(custId)) {
            CustomersHandler handler = new CustomersHandler(context);
            Customer customer = handler.getCustomer(custId);
            if (customer != null) {
                String displayName = myPref.getCustomerDisplayName();
                switch (displayName) {
                    case "cust_name":
                        name = customer.getCust_name();
                        break;
                    case "fullName":
                        name = String.format("%s %s", StringUtil.nullStringToEmpty(
                                customer.getCust_firstName()),
                                StringUtil.nullStringToEmpty(customer.getCust_lastName()));
                        break;
                    case "CompanyName":
                        name = customer.getCompanyName();
                        break;
                    default:
                        name = customer.getCust_name();
                }
            }
        }
        return name;
    }

    private String getCustAccount(String custId) {
        String name = "";
        if (!TextUtils.isEmpty(custId)) {
            CustomersHandler handler = new CustomersHandler(context);
            Customer customer = handler.getCustomer(custId);
            if (customer != null) {
                return customer.getCustAccountNumber();
            }
        }
        return name;
    }

    private double formatStrToDouble(String val) {
        if (TextUtils.isEmpty(val))
            return 0.00;
        return Double.parseDouble(val);
    }
}
