package com.android.emobilepos.print;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.StarMicronics.jasura.JAException;
import com.android.dao.AssignEmployeeDAO;
import com.android.dao.ClerkDAO;
import com.android.dao.ShiftDAO;
import com.android.dao.StoredPaymentsDAO;
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
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.orders.OrderProduct;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.models.realms.Payment;
import com.android.support.Customer;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.util.List;
import java.util.Locale;

import drivers.EMSBluetoothStarPrinter;
import drivers.star.utils.PrinterFunctions;
import plaintext.EMSPlainTextHelper;
import util.StringUtil;

/**
 * Created by Luis Camayd on 8/2/2019.
 */
public class ReceiptBuilder {
    private Context context;
    private MyPreferences myPref;
    private List<String> printPref;
    private int paperSize;

    public ReceiptBuilder(Context context, int paperSize) {
        this.context = context;
        this.paperSize = paperSize;
        myPref = new MyPreferences(context);
        printPref = myPref.getPrintingPreferences();
    }

    public Receipt getTransaction(Order order,
                                  Global.OrderType type,
                                  boolean isFromHistory,
                                  boolean isFromOnHold,
                                  EMVContainer emvContainer) {

        Receipt receipt = new Receipt();

        try {

            AssignEmployee employee = AssignEmployeeDAO.getAssignEmployee();
            Clerk clerk = ClerkDAO.getByEmpId(Integer.parseInt(myPref.getClerkID()));
            printPref = myPref.getPrintingPreferences();
            OrderProductsHandler orderProductsHandler = new OrderProductsHandler(context);
            List<DataTaxes> listOrdTaxes = order.getListOrderTaxes();
            List<OrderProduct> orderProducts = order.getOrderProducts();
            EMSPlainTextHelper textHandler = new EMSPlainTextHelper();

            boolean payWithLoyalty = false;
            StringBuilder sb = new StringBuilder();
            int size = orderProducts.size();

            File imgFile = new File(myPref.getAccountLogoPath());
            if (imgFile.exists()) {
                receipt.setMerchantLogo(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
            }

            if (printPref.contains(MyPreferences.print_header)) {
                MemoTextHandler handler = new MemoTextHandler(context);
                String[] header = handler.getHeader();

                if (!TextUtils.isEmpty(header[0]))
                    sb.append(textHandler.centeredString(header[0], paperSize));
                if (!TextUtils.isEmpty(header[1]))
                    sb.append(textHandler.centeredString(header[1], paperSize));
                if (!TextUtils.isEmpty(header[2]))
                    sb.append(textHandler.centeredString(header[2], paperSize));

                if (!TextUtils.isEmpty(sb.toString())) {
                    sb.insert(0, textHandler.newLines(1));
                    sb.append(textHandler.newLines(1));
                    receipt.setMerchantHeader(sb.toString());
                    print(sb.toString(), 0, PrinterFunctions.Alignment.Left);
                    sb.setLength(0);
                }
            }


            if (order.isVoid.equals("1")) {
                sb.append(textHandler.centeredString("*** VOID ***", paperSize)).append("\n\n");
                print(sb.toString());
                sb.setLength(0);
            }


            if (isFromOnHold) {
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                        "[" + context.getString(R.string.on_hold) + "]",
                        order.ord_HoldName, paperSize, 0));
            }

            switch (type) {
                case ORDER: // Order
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.order) + ":", order.ord_id,
                            paperSize, 0));
                    break;
                case RETURN: // Return
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.return_tag) + ":", order.ord_id,
                            paperSize, 0));
                    break;
                case INVOICE: // Invoice
                case CONSIGNMENT_INVOICE:// Consignment Invoice
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.invoice) + ":", order.ord_id,
                            paperSize, 0));
                    break;
                case ESTIMATE: // Estimate
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.estimate) + ":", order.ord_id,
                            paperSize, 0));
                    break;
                case SALES_RECEIPT: // Sales Receipt
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.sales_receipt) + ":", order.ord_id,
                            paperSize, 0));
                    break;
            }
            print(sb.toString());
            sb.setLength(0);


            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                    context.getString(R.string.receipt_date),
                    Global.formatToDisplayDate(order.ord_timeStarted, 3),
                    paperSize, 0));

            if (ShiftDAO.isShiftOpen() && myPref.isUseClerks()) {
                String clerk_id = order.clerk_id;
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                        context.getString(R.string.receipt_clerk),
                        clerk.getEmpName() + "(" + clerk_id + ")",
                        paperSize, 0));
            }

            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                    context.getString(R.string.receipt_employee),
                    employee.getEmpName() + "(" + employee.getEmpId() + ")",
                    paperSize, 0));

            String custName = getCustName(order.cust_id);
            if (custName != null && !custName.isEmpty()) {
                if (!TextUtils.isEmpty(custName)) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.receipt_customer), custName,
                            paperSize, 0));
                }
            }

//        custName = getCustAccount(anOrder.cust_id);
//        if (printPref.contains(MyPreferences.print_customer_id) &&
//                custName != null && !TextUtils.isEmpty(custName)) {
//            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
//                    context.getString(R.string.receipt_customer_id),
//                    custName, paperSize, 0));
//        }

            String ordComment = order.ord_comment;
            if (!TextUtils.isEmpty(ordComment)) {
                sb.append("\n\n");
                sb.append("Comments:\n");
                sb.append(textHandler.oneColumnLineWithLeftAlignedText(
                        ordComment, paperSize, 3)).append("\n");
            }

            print(sb.toString());
            sb.setLength(0);

            int totalItemstQty = 0;
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
                                            uomDescription, paperSize, 1));
                            if (orderProducts.get(i).getHasAddons()) {
                                List<OrderProduct> addons = orderProductsHandler.getOrderProductAddons(
                                        orderProducts.get(i).getOrdprod_id());
                                for (OrderProduct addon : addons) {
                                    if (addon.isAdded()) {
                                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                                " >" + addon.getOrdprod_name(),
                                                Global.getCurrencyFormat(addon.getFinalPrice()),
                                                paperSize, 2));
                                    } else {
                                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                                " >NO " + addon.getOrdprod_name(),
                                                Global.getCurrencyFormat(addon.getFinalPrice()),
                                                paperSize, 2));
                                    }
                                }
                            }

                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                    context.getString(R.string.receipt_price),
                                    Global.getCurrencyFormat(String.valueOf(orderProducts.get(i)
                                            .getItemTotalCalculated())), paperSize, 3));
                            if (orderProducts.get(i).getDiscount_id() != null &&
                                    !TextUtils.isEmpty(orderProducts.get(i).getDiscount_id())) {
                                ProductsHandler productDBHandler = new ProductsHandler(context);
                                String discountName = productDBHandler.getDiscountName(
                                        orderProducts.get(i).getDiscount_id());
                                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                        context.getString(R.string.receipt_discount) +
                                                " " + discountName,
                                        Global.getCurrencyFormat(orderProducts.get(i)
                                                .getDiscount_value()), paperSize, 3));
                            }
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                    context.getString(R.string.receipt_total),
                                    Global.getCurrencyFormat(orderProducts.get(i).getItemTotal()),
                                    paperSize, 3));

                            List<OrderProduct> giftcardvalues = orderProductsHandler
                                    .getOrdProdGiftCardNumber(orderProducts.get(i).getOrdprod_id());
                            for (OrderProduct giftCard : giftcardvalues) {
                                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                        giftCard.getGiftcardName() + ":",
                                        giftCard.getGiftcardNumber(), paperSize, 3));
                            }

                            if (printPref.contains(MyPreferences.print_descriptions)) {
                                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                        context.getString(R.string.receipt_description),
                                        "", paperSize, 3));
                                sb.append(textHandler.oneColumnLineWithLeftAlignedText(
                                        orderProducts.get(i).getOrdprod_desc(),
                                        paperSize, 5));
                            }
                        }
                    } else {
                        sb.append(textHandler.oneColumnLineWithLeftAlignedText(
                                orderProducts.get(i).getOrdprod_qty()
                                        + "x " + orderProducts.get(i).getOrdprod_name()
                                        + " " + uomDescription, paperSize, 1));
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                context.getString(R.string.receipt_price),
                                Global.getCurrencyFormat(orderProducts.get(i).getFinalPrice()),
                                paperSize, 3));

                        if (orderProducts.get(i).getDiscount_id() != null && !TextUtils.isEmpty(
                                orderProducts.get(i).getDiscount_id())) {
                            ProductsHandler productDBHandler = new ProductsHandler(context);
                            String discountName = productDBHandler.getDiscountName(
                                    orderProducts.get(i).getDiscount_id());
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                    context.getString(R.string.receipt_discount) +
                                            " " + discountName,
                                    Global.getCurrencyFormat(orderProducts.get(i).getDiscount_value()),
                                    paperSize, 3));
                        }

                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                context.getString(R.string.receipt_total),
                                Global.getCurrencyFormat(orderProducts.get(i).getItemTotal()),
                                paperSize, 3));


                        List<OrderProduct> giftcardvalues = orderProductsHandler
                                .getOrdProdGiftCardNumber(orderProducts.get(i).getOrdprod_id());
                        for (OrderProduct giftCard : giftcardvalues) {
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                    giftCard.getGiftcardName() + ":",
                                    giftCard.getGiftcardNumber(), paperSize, 3));
                        }


                        if (printPref.contains(MyPreferences.print_descriptions)) {
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                    context.getString(R.string.receipt_description),
                                    "", paperSize, 3));
                            sb.append(textHandler.oneColumnLineWithLeftAlignedText(
                                    orderProducts.get(i).getOrdprod_desc(),
                                    paperSize, 5));
                        }

                    }
                    print(sb.toString());
                    sb.setLength(0);
                }
            } else {
                int padding = paperSize / 4;
                String tempor = Integer.toString(padding);
                StringBuilder tempSB = new StringBuilder();
                tempSB.append("%").append(tempor).append("s").append("%").append(tempor).append("s")
                        .append("%").append(tempor).append("s").append("%").append(tempor).append("s");

                sb.append(String.format(tempSB.toString(), "Item", "Qty", "Price", "Total"))
                        .append("\n");

                for (int i = 0; i < size; i++) {
                    if (!TextUtils.isEmpty(orderProducts.get(i).getProd_price_points()) &&
                            Integer.parseInt(orderProducts.get(i).getProd_price_points()) > 0) {
                        payWithLoyalty = true;
                    }
                    totalItemstQty += TextUtils.isEmpty(orderProducts.get(i).getOrdprod_qty()) ? 0
                            : Double.parseDouble(orderProducts.get(i).getOrdprod_qty());
                    sb.append(orderProducts.get(i).getOrdprod_name()).append("-")
                            .append(orderProducts.get(i).getOrdprod_desc()).append("\n");

                    sb.append(String.format("Discount %s\n", Global.getCurrencyFormat(
                            orderProducts.get(i).getDiscountTotal().toString())));
                    sb.append(String.format(tempSB.toString(), "   ",
                            orderProducts.get(i).getOrdprod_qty(),
                            Global.getCurrencyFormat(orderProducts.get(i).getFinalPrice()),
                            Global.getCurrencyFormat(orderProducts.get(i).getItemTotal())))
                            .append("\n");
                    print(sb.toString());
                    sb.setLength(0);
                }
            }
            print(sb.toString());
            sb.setLength(0);

            print(textHandler.lines(paperSize));

            addTotalLines(context, order, orderProducts, sb, paperSize);

            addTaxesLine(listOrdTaxes, order, paperSize, sb);

            sb.append("\n");
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                    context.getString(R.string.receipt_itemsQtyTotal),
                    String.valueOf(totalItemstQty), paperSize, 0));
            sb.append("\n");
            String granTotal = "0";
            if (!TextUtils.isEmpty(order.gran_total)) {
                granTotal = order.gran_total;
            } else if (!TextUtils.isEmpty(order.ord_total)) {
                granTotal = order.ord_total;
            }
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                    context.getString(R.string.receipt_grandtotal),
                    Global.getCurrencyFormat(granTotal), paperSize, 0));
            sb.append("\n");
            PaymentsHandler payHandler = new PaymentsHandler(context);
            List<PaymentDetails> detailsList = payHandler
                    .getPaymentForPrintingTransactions(order.ord_id);
            if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward)) {
                detailsList.addAll(StoredPaymentsDAO.getPaymentForPrintingTransactions(order.ord_id));
            }
            String receiptSignature;
            size = detailsList.size();

            double tempGrandTotal = Double.parseDouble(granTotal);
            double tempAmount = 0;
            if (size == 0) {
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                        context.getString(R.string.receipt_amountpaid),
                        Global.formatDoubleToCurrency(tempAmount), paperSize, 0));

                if (type == Global.OrderType.INVOICE) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.receipt_balance_due),
                            Global.formatDoubleToCurrency(tempGrandTotal - tempAmount),
                            paperSize, 0));
                }
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                        context.getString(R.string.receipt_total_tip_paid),
                        Global.formatDoubleToCurrency(0.00),
                        paperSize, 0));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                        context.getString(R.string.receipt_amountreturned),
                        Global.formatDoubleToCurrency(0.00), paperSize, 0));
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
                                    paperSize, 1));
                    if (!_pay_type.equals("CASH") && !_pay_type.equals("CHECK")) {
                        tempSB.append(textHandler.oneColumnLineWithLeftAlignedText(
                                "TransID: " + StringUtil.nullStringToEmpty(
                                        detailsList.get(i).getPay_transid()),
                                paperSize, 1));
                        tempSB.append(textHandler.oneColumnLineWithLeftAlignedText(
                                "CC#: *" + detailsList.get(i).getCcnum_last4(),
                                paperSize, 1));
                    } else {
                        tempSB.append(textHandler
                                .oneColumnLineWithLeftAlignedText(
                                        context.getString(R.string.amount_tendered) +
                                                Global.formatDoubleToCurrency(
                                                        detailsList.get(i).getAmountTender()),
                                        paperSize, 1));
                        tempSB.append(textHandler
                                .oneColumnLineWithLeftAlignedText(
                                        context.getString(R.string.changeLbl) +
                                        Global.formatDoubleToCurrency(
                                                detailsList.get(i).getAmountTender() -
                                                        formatStrToDouble(detailsList.get(i)
                                                                .getPay_amount())),
                                        paperSize, 1));
                    }
                }
                if (type == Global.OrderType.ORDER) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.receipt_amountreturned),
                            Global.formatDoubleToCurrency(tempAmount), paperSize, 0));
                } else {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.receipt_amountpaid),
                            Global.getCurrencyFormat(Double.toString(paidAmount)),
                            paperSize, 0));
                }
                sb.append(tempSB.toString());
                if (type == Global.OrderType.INVOICE) // Invoice
                {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                            context.getString(R.string.receipt_balance_due),
                            Global.formatDoubleToCurrency(tempGrandTotal - tempAmount),
                            paperSize, 0));
                }
                if (type != Global.OrderType.ORDER) {
                    if (myPref.isRestaurantMode() &&
                            myPref.getPreferences(MyPreferences.pref_enable_togo_eatin)) {
                        if (tempTipAmount == 0) {
                            sb.append("\n");
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                    context.getString(R.string.receipt_total_tip_paid),
                                    textHandler.lines(paperSize / 2),
                                    paperSize, 0));
                            sb.append("\n");
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                    context.getString(R.string.receipt_total),
                                    textHandler.lines(paperSize / 2),
                                    paperSize, 0));
                        } else {
                            sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                    context.getString(R.string.receipt_total_tip_paid),
                                    Global.getCurrencyFormat(Double.toString(tempTipAmount)),
                                    paperSize, 0));
                        }
                    } else {
                        sb.append(textHandler.twoColumnLineWithLeftAlignedText(
                                context.getString(R.string.receipt_total_tip_paid),
                                Global.getCurrencyFormat(Double.toString(tempTipAmount)),
                                paperSize, 0));
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
                            paperSize, 0))
                            .append("\n");
                }

            }
            print(sb.toString());

            print(textHandler.newLines(1));

            if (type != Global.OrderType.ORDER && saveAmount > 0)
                printYouSave(String.valueOf(saveAmount), paperSize);
            sb.setLength(0);

            if (Global.isIvuLoto && detailsList.size() > 0) {
                if (!printPref.contains(MyPreferences.print_ivuloto_qr)) {
                    printIVULoto(detailsList.get(0).getIvuLottoNumber(), paperSize);
                } else {
                    printImage(2);
                    printIVULoto(detailsList.get(0).getIvuLottoNumber(), paperSize);
                }
                sb.setLength(0);
            }
//            printOrderAttributes(lineWidth, anOrder);

            if (printPref.contains(MyPreferences.print_footer)) {
                printFooter(paperSize);
            }

            print(textHandler.newLines(1));

            if (payWithLoyalty && Global.loyaltyCardInfo != null &&
                    !TextUtils.isEmpty(Global.loyaltyCardInfo.getCardNumAESEncrypted())
                    && !TextUtils.isEmpty(Global.loyaltyCardInfo.getCardLast4())) {
                print(String.format("%s *%s\n", context.getString(R.string.receipt_cardnum),
                        Global.loyaltyCardInfo.getCardLast4()));
                print(String.format("%s %s %s\n", context.getString(R.string.receipt_point_used),
                        Global.loyaltyCharge, context.getString(R.string.points)));
                print(String.format("%s %s %s\n", context.getString(R.string.receipt_reward_balance),
                        Global.loyaltyPointsAvailable, context.getString(R.string.points)));
                print(textHandler.newLines(1));
            }
            if (Global.rewardCardInfo != null &&
                    !TextUtils.isEmpty(Global.rewardCardInfo.getCardNumAESEncrypted())
                    && !TextUtils.isEmpty(Global.rewardCardInfo.getCardLast4())) {
                print(String.format("%s *%s\n", context.getString(R.string.receipt_cardnum),
                        Global.rewardCardInfo.getCardLast4()));
                print(String.format("%s %s %s\n", context.getString(R.string.receipt_reward_balance),
                        Global.rewardCardInfo.getOriginalTotalAmount(),
                        context.getString(R.string.points)));
                print(textHandler.newLines(1));
            }
            receiptSignature = order.ord_signature;
            if (!TextUtils.isEmpty(receiptSignature)) {
                encodedSignature = receiptSignature;
                printImage(1);
                sb.setLength(0);
                sb.append("x").append(textHandler.lines(paperSize / 2)).append("\n");
                sb.append(context.getString(R.string.receipt_signature))
                        .append(textHandler.newLines(1));
                print(sb.toString());

            }

            if (isFromHistory) {
                sb.setLength(0);
                sb.append(textHandler.centeredString("*** Copy ***", paperSize));
                print(sb.toString());
                print(textHandler.newLines(1));
            }
            printTermsNConds();
            printEnablerWebSite(lineWidth);
            cutPaper();
        } catch (
                JAException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        } catch (
                Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }


        return receipt;
    }

    public Receipt getTransaction(String orderId,
                                  Global.OrderType type,
                                  boolean isFromHistory,
                                  boolean isFromOnHold,
                                  EMVContainer emvContainer) {

        OrdersHandler orderHandler = new OrdersHandler(context);
        Order order = orderHandler.getPrintedOrder(orderId);

        return getTransaction(order, type, isFromHistory, isFromOnHold, emvContainer);
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
                        name = String.format("%s %s", StringUtil.nullStringToEmpty(customer.getCust_firstName())
                                , StringUtil.nullStringToEmpty(customer.getCust_lastName()));
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

}
