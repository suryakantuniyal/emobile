package com.android.emobilepos.ordering;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.dao.AssignEmployeeDAO;
import com.android.database.DBManager;
import com.android.database.MemoTextHandler;
import com.android.database.OrderProductsHandler;
import com.android.database.OrderTaxes_DB;
import com.android.database.OrdersHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.adapters.OrderProductListAdapter;
import com.android.emobilepos.adapters.SplittedOrderSummaryAdapter;
import com.android.emobilepos.models.OrderSeatProduct;
import com.android.emobilepos.models.SplittedOrder;
import com.android.emobilepos.models.orders.OrderProduct;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.Device;
import com.android.emobilepos.payment.SelectPayMethod_FA;
import com.android.support.DateUtils;
import com.android.support.DeviceUtils;
import com.android.support.GenerateNewID;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.OrderProductUtils;
import com.android.support.SynchMethods;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import main.EMSDeviceManager;

/*import static com.google.android.gms.internal.zzahn.runOnUiThread;*/

/**
 * Created by Guarionex on 2/19/2016.
 */
public class SplittedOrderDetailsFR extends Fragment implements View.OnClickListener {
    public SplittedOrder restaurantSplitedOrder;
    private TextView orderId;
    private TextView subtotal;
    private MyPreferences myPref;
    private TextView globalDiscountTextView;
    private TextView lineItemDiscountTotal;
    private TextView taxTotal;
    private TextView granTotal;
    private LinearLayout productAddonsSection;
    private LinearLayout orderProductSection;
    private LayoutInflater inflater;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View detailView = inflater.inflate(R.layout.splitted_order_detail_fragment,
                container, false);
        this.inflater = inflater;
        myPref = new MyPreferences(getActivity());

        return detailView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee();
        LinearLayout previewContainer = view.findViewById(R.id.receiptPreviewContainer);
        ViewGroup.LayoutParams params = previewContainer.getLayoutParams();
        params.width = myPref.getPrintPreviewLayoutWidth();
        previewContainer.setLayoutParams(params);
        MemoTextHandler handler = new MemoTextHandler(getActivity());
        String[] header = handler.getHeader();
        String[] footer = handler.getFooter();
        Button checkoutBtn = getActivity().findViewById(R.id.checkoutbutton);
        Button printReceiptBtn = getActivity().findViewById(R.id.printReceiptbutton2);
        Button printAllReceiptBtn = getActivity().findViewById(R.id.printAllReceiptbutton3);
        printAllReceiptBtn.setOnClickListener(this);
        checkoutBtn.setOnClickListener(this);
        printReceiptBtn.setOnClickListener(this);
        File imgFile = new File(myPref.getAccountLogoPath());
        if (imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ImageView imageView = getActivity().findViewById(R.id.logoimageView);
            imageView.setImageBitmap(bitmap);
        }
        TextView header1 = getActivity().findViewById(R.id.memo_headerLine1textView);
        TextView header2 = getActivity().findViewById(R.id.memo_headerLine2textView16);
        TextView header3 = getActivity().findViewById(R.id.memo_headerLine3textView18);
        orderId = getActivity().findViewById(R.id.orderIdtextView16);
        TextView orderDate = getActivity().findViewById(R.id.orderDatetextView21);
        TextView deviceName = getActivity().findViewById(R.id.deviceNametextView23);
        subtotal = getActivity().findViewById(R.id.subtotaltextView);
        lineItemDiscountTotal = getActivity().findViewById(R.id.lineitem_discounttextView);
        globalDiscountTextView = getActivity().findViewById(R.id.globaldiscounttextView);
        taxTotal = getActivity().findViewById(R.id.taxtotaltextView14a);
        granTotal = view.findViewById(R.id.granTotaltextView16);
        TextView footer1 = getActivity().findViewById(R.id.footerLine1textView);
        TextView footer2 = getActivity().findViewById(R.id.footerLine2textView);
        TextView footer3 = getActivity().findViewById(R.id.footerLine3textView);
        orderProductSection = getActivity().findViewById(R.id.order_products_section_linearlayout);
        deviceName.setText(String.format("%s(%s)", assignEmployee.getEmpName(), String.valueOf(assignEmployee.getEmpId())));
        orderDate.setText(DateUtils.getDateAsString(new Date(), "MMM/dd/yyyy"));

        if (header[0] != null && !header[0].isEmpty()) {
            header1.setText(header[0]);
        } else {
            header1.setVisibility(View.GONE);
        }
        if (header[1] != null && !header[1].isEmpty()) {
            header2.setText(header[1]);
        } else {
            header2.setVisibility(View.GONE);
        }
        if (header[2] != null && !header[2].isEmpty()) {
            header3.setText(header[2]);
        } else {
            header3.setVisibility(View.GONE);
        }

        if (footer[0] != null && !footer[0].isEmpty()) {
            footer1.setText(footer[0]);
        } else {
            footer1.setVisibility(View.GONE);
        }
        if (footer[1] != null && !footer[1].isEmpty()) {
            footer2.setText(footer[1]);
        } else {
            footer2.setVisibility(View.GONE);
        }
        if (footer[2] != null && !footer[2].isEmpty()) {
            footer3.setText(footer[2]);
        } else {
            footer3.setVisibility(View.GONE);
        }
    }

    private void addProductLine(String leftString, String rightString, int indentedTabs) {
        LinearLayout itemLL;
        switch (indentedTabs) {
            default:
                itemLL = (LinearLayout) inflater.inflate(R.layout.twocols_leftweight_layout_item, null, false);
                break;
            case 2:
                itemLL = (LinearLayout) inflater.inflate(R.layout.twocols_leftweight_margin1_layout_item, null, false);
                break;
            case 3:
                itemLL = (LinearLayout) inflater.inflate(R.layout.twocols_leftweight_margin2_layout_item, null, false);
                break;
        }
        TextView leftText = itemLL.findViewById(R.id.lefttextView);
        TextView rightText = itemLL.findViewById(R.id.righttextView);
        if (rightString == null) {
            rightText.setVisibility(View.GONE);
        } else {
            rightText.setVisibility(View.VISIBLE);
            rightText.setText(rightString);
        }
        if (leftString == null) {
            leftText.setVisibility(View.GONE);
        } else {
            leftText.setVisibility(View.VISIBLE);
            leftText.setText(leftString);
        }
        productAddonsSection.addView(itemLL);
    }

    private void applyPreviewCalculations(SplittedOrder splitedOrder) {
        SplittedOrderSummary_FA orderSummaryFa = (SplittedOrderSummary_FA) getActivity();
        BigDecimal orderSubtotal = new BigDecimal(0);
        BigDecimal orderTaxes = new BigDecimal(0);
        BigDecimal orderGranTotal = new BigDecimal(0);
        BigDecimal itemDiscountTotal = new BigDecimal(0);
        BigDecimal globalDiscountTotal = new BigDecimal(0);
        List<OrderProduct> products = splitedOrder.getOrderProducts();
        for (OrderProduct product : products) {
            BigDecimal qty = Global.getBigDecimalNum(product.getOrdprod_qty());
            orderSubtotal = orderSubtotal.add(product.getAddonsTotalPrice()).add(Global.getBigDecimalNum(product.getFinalPrice()).multiply(qty));
            globalDiscountTotal = globalDiscountTotal.add(Global.getBigDecimalNum(product.getFinalPrice()).setScale(4, RoundingMode.HALF_UP)
                    .multiply(orderSummaryFa.getGlobalDiscountPercentge().setScale(6, RoundingMode.HALF_UP)));
            itemDiscountTotal = itemDiscountTotal.add(Global.getBigDecimalNum(product.getDiscount_value()));
            if (orderSummaryFa.getTax() != null) {
//                TaxesCalculator taxesCalculator = new TaxesCalculator(getActivity(), product, splitedOrder.tax_id,
//                        orderSummaryFa.getTax(), orderSummaryFa.getDiscount(), Global.getBigDecimalNum(splitedOrder.ord_subtotal),
//                        Global.getBigDecimalNum(splitedOrder.ord_discount), getSplittedOrderSummaryFa().transType);
                orderTaxes = orderTaxes.add(product.getProd_taxValue());
                splitedOrder.setListOrderTaxes(splitedOrder.getListOrderTaxes());

            }
        }
        orderGranTotal = orderSubtotal.subtract(itemDiscountTotal).setScale(6, RoundingMode.HALF_UP)
                .subtract(globalDiscountTotal).setScale(6, RoundingMode.HALF_UP).add(orderTaxes)
                .setScale(6, RoundingMode.HALF_UP);
        splitedOrder.ord_total = orderGranTotal.toString();
        splitedOrder.gran_total = orderGranTotal.toString();
        splitedOrder.ord_subtotal = orderSubtotal.toString();
        splitedOrder.ord_taxamount = orderTaxes.toString();
        splitedOrder.ord_discount = globalDiscountTotal.toString();
        splitedOrder.ord_lineItemDiscount = itemDiscountTotal.toString();
    }

    public void setReceiptOrder(SplittedOrder splitedOrder) {
        restaurantSplitedOrder = splitedOrder;
        final List<OrderProduct> products = splitedOrder.getOrderProducts();
        if (orderProductSection.getChildCount() > 0) {
            orderProductSection.removeAllViewsInLayout();
        }

        new Thread() {
            public void run() { //temporary fix, needs refactoring
                try {
                    List<OrderProduct> addons;
                    BigDecimal qty;
                    for (OrderProduct product : products) {
                        final LinearLayout productSectionLL = (LinearLayout) View.inflate(getActivity(), R.layout.receipt_product_layout_item, null);
                        addons = product.addonsProducts;
                        qty = Global.getBigDecimalNum(product.getOrdprod_qty());
                        ((TextView) productSectionLL.findViewById(R.id.productNametextView)).setText(String.format("%sx %s", product.getOrdprod_qty(), product.getOrdprod_name()));
                        productAddonsSection = productSectionLL.findViewById(R.id.productAddonSectionLinearLayout);

                        for (OrderProduct addon : addons) {
                            addProductLine("- " + addon.getOrdprod_name(),
                                    Global.getCurrencyFormat(addon.getFinalPrice()), 3);
                        }

                        ((TextView) productSectionLL.findViewById(R.id.productPricetextView)).setText(Global.getCurrencyFormat(product.getFinalPrice()));
                        ((TextView) productSectionLL.findViewById(R.id.productDiscounttextView)).setText(Global.getCurrencyFormat(product.getDiscount_value()));
                        ((TextView) productSectionLL.findViewById(R.id.productTotaltextView)).setText(Global.getCurrencyFormat(Global.getBigDecimalNum(product.getItemTotal())
                                .multiply(qty).toString()));
                        if (product.getOrdprod_desc() != null && !product.getOrdprod_desc().isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            StringTokenizer tokenizer = new StringTokenizer(product.getOrdprod_desc(), "<br/>");
                            while (tokenizer.hasMoreElements()) {
                                sb.append(tokenizer.nextToken());
                            }
                            ((TextView) productSectionLL.findViewById(R.id.productDescriptiontextView)).setText(sb.toString());
                        } else {
                            ((TextView) productSectionLL.findViewById(R.id.productDescriptiontextView)).setText("");
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                orderProductSection.addView(productSectionLL);
                            }
                        });
                    }
                } catch (Exception e) {
                    // error generating receipt preview
                }
            }
        }.start();

        subtotal.setText(Global.formatDoubleToCurrency(Double.parseDouble(splitedOrder.ord_subtotal)));
        lineItemDiscountTotal.setText(Global.formatDoubleToCurrency(Double.parseDouble(splitedOrder.ord_lineItemDiscount)));
        taxTotal.setText(Global.formatDoubleToCurrency(Double.parseDouble(splitedOrder.ord_taxamount)));
        granTotal.setText(Global.formatDoubleToCurrency(Double.parseDouble(splitedOrder.gran_total)));
        orderId.setText(splitedOrder.ord_id);
        globalDiscountTextView.setText(Global.formatDoubleToCurrency(Double.parseDouble(splitedOrder.ord_discount)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.checkoutbutton: {
                saveHoldOrder(restaurantSplitedOrder);
                break;
            }
            case R.id.printReceiptbutton2: {
                if (Global.mainPrinterManager != null
                        && Global.mainPrinterManager.getCurrentDevice() != null) {
                    new PrintPreview().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, restaurantSplitedOrder);
                }
                break;
            }
            case R.id.printAllReceiptbutton3: {
                if (Global.mainPrinterManager != null
                        && Global.mainPrinterManager.getCurrentDevice() != null) {
//                    final int[] count = {0};
                    SplittedOrderSummary_FA orderSummaryFa = (SplittedOrderSummary_FA) getActivity();
                    final SplittedOrderSummaryAdapter adapter = (SplittedOrderSummaryAdapter) orderSummaryFa.getOrderSummaryFR().getGridView().getAdapter();
//                    ViewTreeObserver vto = receiptPreview.getViewTreeObserver();
//                    vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                        @Override
//                        public void onGlobalLayout() {
//                            Global.mainPrinterManager.getCurrentDevice().printReceiptPreview((SplittedOrder) adapter.getItem(count[0]));
//                            count[0]++;
//                            if (count[0] < adapter.getCount()) {
//                                setReceiptOrder((SplittedOrder) adapter.getItem(count[0]));
//                            } else {
//                            } else {
//                                receiptPreview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                            }
//                        }
//                    });
                    for (int i = 0; i < adapter.getCount(); i++) {
                        applyPreviewCalculations((SplittedOrder) adapter.getItem(i));
                    }
                    new PrintPreview().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, adapter.getItems());
                }
            }
        }
    }

    private void saveHoldOrder(SplittedOrder splitedOrder) {
        OrdersHandler ordersHandler = new OrdersHandler(getActivity());
        OrderTaxes_DB ordTaxesDB = new OrderTaxes_DB();
        Global global = (Global) getActivity().getApplication();
        OrderProductsHandler productsHandler = new OrderProductsHandler(getActivity());
        SplittedOrderSummary_FA summaryFa = (SplittedOrderSummary_FA) getActivity();
        if (summaryFa.getOrderSummaryFR().getGridView().getAdapter().getCount() > 1) {
            GenerateNewID newID = new GenerateNewID(getActivity());
            splitedOrder.ord_id = newID.getNextID(GenerateNewID.IdType.ORDER_ID);
            for (OrderProduct product : splitedOrder.getOrderProducts()) {
                if (global.order.getOrderProducts().contains(product)) {
                    global.order.getOrderProducts().remove(product);
                }
            }
            if (summaryFa.splitType != SplittedOrderSummary_FA.SalesReceiptSplitTypes.SPLIT_EQUALLY) {
                global.order.ord_subtotal = Global.getBigDecimalNum(global.order.ord_subtotal)
                        .subtract(Global.getBigDecimalNum(splitedOrder.ord_subtotal)).toString();

                global.order.ord_total = Global.getBigDecimalNum(global.order.ord_total)
                        .subtract(Global.getBigDecimalNum(splitedOrder.ord_total)).toString();

                global.order.gran_total = Global.getBigDecimalNum(global.order.gran_total)
                        .subtract(Global.getBigDecimalNum(splitedOrder.gran_total)).toString();
            }
            global.order.isOnHold = "1";
            if (global.order.ord_HoldName == null || global.order.ord_HoldName.isEmpty()) {
                global.order.ord_HoldName = "Table " + global.order.assignedTable + " " + DateUtils.getDateAsString(new Date(), "MMM/dd/yy hh:mm");
            }
            global.order.processed = "10";
        } else {
            if (global.order.ord_id != null) {
                splitedOrder.ord_id = global.order.ord_id;
            }
        }

        if (splitedOrder.getOrderProducts().size() > 0) {
            splitedOrder.resetTimeCreated(); // reset time created for an accurate Z-Report (Shifts)
            if (splitedOrder.ord_id != null) {
                ordTaxesDB.insert(splitedOrder.getListOrderTaxes(), splitedOrder.ord_id);
            }
            if (summaryFa.getOrderSummaryFR().getGridView().getAdapter().getCount() == 1) {
                splitedOrder.processed = "10";
                splitedOrder.isOnHold = "0";
                global.order.isOnHold = "0";
                global.order.processed = "10";
                splitedOrder.total_lines = String.valueOf(splitedOrder.getTotalLines());
                global.order.total_lines = String.valueOf(splitedOrder.getTotalLines());
                if (summaryFa.splitType == SplittedOrderSummary_FA.SalesReceiptSplitTypes.SPLIT_EQUALLY) {
                    for (OrderSeatProduct seatProduct : summaryFa.orderSeatProducts) {
                        if (seatProduct.rowType == OrderProductListAdapter.RowType.TYPE_ITEM && seatProduct.orderProduct != null) {
                            splitedOrder.getOrderProducts().add(seatProduct.orderProduct);
                        }
                    }
                    splitedOrder.syncOrderProductIds();
                    ordersHandler.insert(splitedOrder);
                } else {
                    splitedOrder.total_lines = String.valueOf(splitedOrder.getTotalLines());
                    splitedOrder.syncOrderProductIds();
                    ordersHandler.insert(splitedOrder);
                }
                global.encodedImage = "";
                productsHandler.insert(splitedOrder.getOrderProducts());
            } else if (summaryFa.splitType == SplittedOrderSummary_FA.SalesReceiptSplitTypes.SPLIT_EQUALLY) {
                splitedOrder.processed = "10";
                splitedOrder.isOnHold = "0";
                splitedOrder.syncOrderProductIds();
                ordersHandler.insert(splitedOrder);
                productsHandler.insert(splitedOrder.getOrderProducts());
            } else {
                splitedOrder.processed = "10";
                splitedOrder.isOnHold = "0";
                splitedOrder.syncOrderProductIds();
                ordersHandler.insert(splitedOrder);
                productsHandler.insert(splitedOrder.getOrderProducts());
            }
            Receipt_FR.updateLocalInventory(getActivity(), splitedOrder.getOrderProducts(), false);
            if (Global.getBigDecimalNum(splitedOrder.gran_total).compareTo(new BigDecimal(0)) != -1) {
                Receipt_FR.updateLocalInventory(getActivity(), splitedOrder.getOrderProducts(), false);
                isSalesReceipt(splitedOrder);
            }
        }
    }

    private void isSalesReceipt(SplittedOrder order) {
        Intent intent = new Intent(getActivity(), SelectPayMethod_FA.class);
        intent.putExtra("typeOfProcedure", Global.TransactionType.SALE_RECEIPT);
        intent.putExtra("salesreceipt", true);
        intent.putExtra("amount", String.valueOf(Global.getRoundBigDecimal(Global.getBigDecimalNum(order.gran_total)
                .compareTo(new BigDecimal(0)) < 0 ? Global.getBigDecimalNum(order.gran_total)
                .negate() : Global.getBigDecimalNum(order.gran_total))));
        intent.putExtra("paid", "0.00");
        intent.putExtra("is_receipt", true);
        intent.putExtra("job_id", order.ord_id);
        intent.putExtra("ord_subtotal", order.ord_subtotal);
        intent.putExtra("ord_taxID", order.tax_id);
        intent.putExtra("ord_type", Global.OrderType.SALES_RECEIPT);
        intent.putExtra("ord_email", "");
        intent.putExtra("subTotal", order.ord_subtotal);
        SplittedOrderSummary_FA summaryFa = (SplittedOrderSummary_FA) getActivity();
        if (summaryFa != null) {
            intent.putExtra("splitPaymentsCount", summaryFa.getOrderSummaryFR().getGridView().getAdapter().getCount());
        }
        if (myPref.isCustSelected()) {
            intent.putExtra("cust_id", myPref.getCustID());
            intent.putExtra("custidkey", myPref.getCustIDKey());
        }
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SplittedOrderSummary_FA summaryFa = (SplittedOrderSummary_FA) getActivity();
        Global global = (Global) getActivity().getApplication();
        summaryFa.checkoutCount++;
        if (resultCode == SplittedOrderSummary_FA.NavigationResult.PAYMENT_SELECTION_VOID.getCode()) {
            summaryFa.voidTransaction(false, restaurantSplitedOrder.ord_id);
            removeCheckoutOrder(summaryFa);
        } else if (summaryFa.splitType == SplittedOrderSummary_FA.SalesReceiptSplitTypes.SPLIT_EQUALLY
                && resultCode != SplittedOrderSummary_FA.NavigationResult.BACK_SELECT_PAYMENT.getCode()) {
            removeCheckoutOrder(summaryFa);
            summaryFa.findViewById(R.id.splitTypesSpinner).setEnabled(false);
            if (summaryFa.getOrderSummaryFR().getGridView().getAdapter().getCount() == 0) {
                getActivity().setResult(-1);
                getActivity().finish();
            } else {
                summaryFa.getOrderDetailsFR().setReceiptOrder((SplittedOrder) summaryFa.getOrderSummaryFR().getGridView().getAdapter().getItem(0));
            }
        } else if (resultCode == SplittedOrderSummary_FA.NavigationResult.PAYMENT_COMPLETED.getCode()) {
            removeCheckoutOrder(summaryFa);
            if (summaryFa.getOrderSummaryFR().getGridView().getAdapter().getCount() == 0) {
//                DBManager dbManager = new DBManager(getActivity());
//                SynchMethods sm = new SynchMethods(dbManager);
//                sm.synchSendOnHold(false, true, getActivity(), restaurantSplitedOrder.ord_id);
                new SyncOnHolds().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);
//                getActivity().setResult(-1);
//                getActivity().finish();
            } else {
                summaryFa.getOrderDetailsFR().setReceiptOrder((SplittedOrder) summaryFa.getOrderSummaryFR().getGridView().getAdapter().getItem(0));
            }
        } else {//Rollback order checkout
            for (OrderProduct product : restaurantSplitedOrder.getOrderProducts()) {
                product.setOrd_id(global.order.ord_id);
                OrderProductUtils.removeOrderProductsByOrderProductId(global.order.getOrderProducts(), product.getOrdprod_id());
                global.order.getOrderProducts().add(product);
                if (summaryFa.splitType != SplittedOrderSummary_FA.SalesReceiptSplitTypes.SPLIT_EQUALLY) {
                    global.order.ord_subtotal = Global.getBigDecimalNum(global.order.ord_subtotal)
                            .add(Global.getBigDecimalNum(restaurantSplitedOrder.ord_subtotal)).toString();
                    global.order.ord_total = Global.getBigDecimalNum(global.order.ord_total)
                            .add(Global.getBigDecimalNum(restaurantSplitedOrder.ord_total)).toString();
                    global.order.gran_total = Global.getBigDecimalNum(global.order.gran_total)
                            .add(Global.getBigDecimalNum(restaurantSplitedOrder.gran_total)).toString();
                }
            }

            OrdersHandler ordersHandler = new OrdersHandler(getActivity());
            OrderTaxes_DB orderTaxesDB = new OrderTaxes_DB();
            OrderProductsHandler productsHandler = new OrderProductsHandler(getActivity());
            ordersHandler.insert(global.order);
            productsHandler.insert(restaurantSplitedOrder.getOrderProducts());
            orderTaxesDB.insert(global.order.getListOrderTaxes(), global.order.ord_id);
            ordersHandler.deleteOrder(restaurantSplitedOrder.ord_id);

        }
    }

    private void removeCheckoutOrder(SplittedOrderSummary_FA summaryFa) {
        SplittedOrderSummaryAdapter adapter = (SplittedOrderSummaryAdapter) summaryFa.getOrderSummaryFR().getGridView().getAdapter();
        adapter.removeOrder(restaurantSplitedOrder);
        removeTicket(restaurantSplitedOrder);
        if (!adapter.isEmpty()) {
            adapter.setSelectedIndex(0);
            restaurantSplitedOrder = (SplittedOrder) adapter.getItem(0);
            summaryFa.getOrderDetailsFR().setReceiptOrder(restaurantSplitedOrder);
        }
    }

    private void removeTicket(SplittedOrder splitedOrder) {
        SplittedOrderSummary_FA summaryFa = (SplittedOrderSummary_FA) getActivity();
        List<OrderSeatProduct> seatProducts = new ArrayList<OrderSeatProduct>(summaryFa.orderSeatProducts);
        List<OrderProduct> products = splitedOrder.getOrderProducts();
        for (OrderProduct product : products) {
            for (OrderSeatProduct seatProduct : seatProducts) {
                if (seatProduct.rowType == OrderProductListAdapter.RowType.TYPE_ITEM &&
                        seatProduct.orderProduct.getOrdprod_id().equalsIgnoreCase(product.getOrdprod_id())) {
                    summaryFa.orderSeatProducts.remove(seatProduct);
                }
            }
        }
    }

    public class PrintPreview extends AsyncTask<SplittedOrder, Void, Void> {
        private ProgressDialog myProgressDialog;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(getActivity());
            myProgressDialog.setMessage("Printing...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
        }

        @Override
        protected Void doInBackground(SplittedOrder... params) {
            EMSDeviceManager emsDeviceManager = DeviceUtils.getEmsDeviceManager(Device.Printables.PAYMENT_RECEIPT_REPRINT, Global.printerDevices);
            for (SplittedOrder order : params) {
                emsDeviceManager.getCurrentDevice().printReceiptPreview(order);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void none) {
            myProgressDialog.dismiss();
        }
    }


    private class SyncOnHolds extends AsyncTask<Boolean, Void, Boolean> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Global.lockOrientation(getActivity());
            dialog = new ProgressDialog(getActivity());
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.sync_sending_orders));
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Boolean... isCheckout) {
            DBManager dbManager = new DBManager(getActivity());
            SynchMethods sm = new SynchMethods(dbManager);
            return sm.synchSendOnHold(false, isCheckout[0], getActivity(), restaurantSplitedOrder.ord_id);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Global.releaseOrientation(getActivity());
            Global.dismissDialog(getActivity(), dialog);
            getActivity().setResult(-1);
            getActivity().finish();
        }
    }
}
