package com.android.emobilepos.ordering;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.database.EmpInvHandler;
import com.android.database.Locations_DB;
import com.android.database.OrderProductsAttr_DB;
import com.android.database.OrderProductsHandler;
import com.android.database.OrderTaxes_DB;
import com.android.database.OrdersHandler;
import com.android.database.ProductsHandler;
import com.android.database.TemplateHandler;
import com.android.database.TransferInventory_DB;
import com.android.database.TransferLocations_DB;
import com.android.emobilepos.DrawReceiptActivity;
import com.android.emobilepos.OrderDetailsActivity;
import com.android.emobilepos.R;
import com.android.emobilepos.consignment.ConsignmentCheckout_FA;
import com.android.emobilepos.customer.ViewCustomers_FA;
import com.android.emobilepos.holders.TransferInventory_Holder;
import com.android.emobilepos.holders.TransferLocations_Holder;
import com.android.emobilepos.mainmenu.SalesTab_FR;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.OrderProducts;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.payment.SelectPayMethod_FA;
import com.android.support.CustomerInventory;
import com.android.database.DBManager;
import com.android.support.GenerateNewID;
import com.android.support.GenerateNewID.IdType;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NumberUtils;
import com.android.support.Post;
import com.android.support.SemiClosedSlidingDrawer;
import com.android.support.SemiClosedSlidingDrawer.OnDrawerCloseListener;
import com.android.support.SemiClosedSlidingDrawer.OnDrawerOpenListener;
import com.viewpagerindicator.CirclePageIndicator;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Receipt_FR extends Fragment implements OnClickListener,
        OnItemClickListener, OnDrawerOpenListener, OnDrawerCloseListener {

    private AddProductBtnCallback callBackAddProd;

    public interface AddProductBtnCallback {
        void addProductServices();
    }

    private final int REMOVE_ITEM = 0, OVERWRITE_PRICE = 1,
            UPDATE_HOLD_STATUS = 1, CHECK_OUT_HOLD = 2;
    private Activity activity;
    private SemiClosedSlidingDrawer slidingDrawer;
    public TextView custName;
    public static ReceiptMainLV_Adapter mainLVAdapter;
    private ReceiptRestLV_Adapter restLVAdapter;
    // private ListViewAdapter myAdapter;
    public static ListView receiptListView;
    // private DragSortListView receiptListGuari123
    // View;

    private Global.TransactionType caseSelected = Global.TransactionType.SALE_RECEIPT;
    private boolean custSelected;
    private Global.OrderType consignmentType;
    private Global global;
    private Global.TransactionType typeOfProcedure = Global.TransactionType.SALE_RECEIPT;
    private MyPreferences myPref;

    private int orientation = 0;

    private boolean validPassword = true;
    private ProductsHandler prodHandler;

    private String ord_HoldName = "";
    private boolean voidOnHold = false;

    private ProgressDialog myProgressDialog;

    private Button btnTemplate, btnHold, btnDetails, btnSign, btnReturn;
    private ImageButton btnScrollRight, btnScrollLeft;

    private MyPagerAdapter pagerAdapter;
    private RecalculateCallback callBackRecalculate;
    private UpdateHeaderTitleCallback callBackUpdateHeaderTitle;
    public static Receipt_FR fragInstance;

    private String order_email = "";
    private NumberUtils numberUtils = new NumberUtils();


    public interface RecalculateCallback {
        void recalculateTotal();
    }

    public interface UpdateHeaderTitleCallback {
        void updateHeaderTitle(String val);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_receipt_main_layout,
                container, false);
        global = (Global) getActivity().getApplication();
        activity = getActivity();

        fragInstance = this;

        myPref = new MyPreferences(activity);
        global.order = new Order(activity);
        final Bundle extras = activity.getIntent().getExtras();
        typeOfProcedure = (Global.TransactionType) extras.get("option_number");

        prodHandler = new ProductsHandler(activity);
        callBackAddProd = (AddProductBtnCallback) activity;
        callBackUpdateHeaderTitle = (UpdateHeaderTitleCallback) activity;

        custName = (TextView) view.findViewById(R.id.membersField);
        receiptListView = (ListView) view.findViewById(R.id.receiptListView);
        slidingDrawer = (SemiClosedSlidingDrawer) view
                .findViewById(R.id.slideDrawer);

        ViewPager viewPager = (ViewPager) view.findViewById(R.id.orderViewPager);
        pagerAdapter = new MyPagerAdapter(getFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        CirclePageIndicator pagerIndicator = (CirclePageIndicator) view
                .findViewById(R.id.indicator);
        pagerIndicator.setViewPager(viewPager);
        pagerIndicator.setCurrentItem(0);

        orientation = getResources().getConfiguration().orientation;

        Button addProd = (Button) view.findViewById(R.id.addProdButton);
        addProd.setOnClickListener(this);
        if (myPref.getIsTablet()
                && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            addProd.setVisibility(View.GONE);
        }

        ImageView plusBut = (ImageView) view.findViewById(R.id.plusButton);
        plusBut.setOnClickListener(this);

        btnTemplate = (Button) view.findViewById(R.id.templateButton);
        btnTemplate.setOnClickListener(this);

        btnHold = (Button) view.findViewById(R.id.holdButton);
        btnHold.setOnClickListener(this);

        btnDetails = (Button) view.findViewById(R.id.detailsButton);
        btnDetails.setOnClickListener(this);

        btnSign = (Button) view.findViewById(R.id.signButton);
        btnSign.setOnClickListener(this);

        btnReturn = (Button) view.findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(this);

        btnScrollLeft = (ImageButton) view.findViewById(R.id.btnScrollLeft);
        btnScrollLeft.setOnClickListener(this);

        btnScrollRight = (ImageButton) view.findViewById(R.id.btnScrollRight);
        btnScrollRight.setOnClickListener(this);

        if (typeOfProcedure != Global.TransactionType.SALE_RECEIPT) {
            LinearLayout.LayoutParams params = (LayoutParams) btnTemplate
                    .getLayoutParams();
            params.width = 0;
            params.weight = 1.0f;
            btnTemplate.setLayoutParams(params);
            btnTemplate.setVisibility(View.VISIBLE);
            btnScrollLeft.setVisibility(View.GONE);
            btnScrollRight.setVisibility(View.GONE);
            btnReturn.setVisibility(View.GONE);
        } else
            btnTemplate.setEms(7);

        ord_HoldName = extras.getString("ord_HoldName");
        custSelected = myPref.isCustSelected();

        if (custSelected) {
            caseSelected = typeOfProcedure;
            switch (typeOfProcedure) {
                case SALE_RECEIPT: {
                    // title.setText("Sales Receipt");
                    custName.setText(myPref.getCustName());
                    Global.ord_type = Global.OrderType.SALES_RECEIPT;
                    break;
                }
                case ORDERS: {
                    // title.setText("Order");
                    custName.setText(myPref.getCustName());
                    Global.ord_type = Global.OrderType.ORDER;
                    break;
                }
                case RETURN: {
                    // title.setText("Return");
                    custName.setText(myPref.getCustName());
                    Global.ord_type = Global.OrderType.RETURN;
                    break;
                }
                case INVOICE: {
                    // title.setText("Invoice");
                    custName.setText(myPref.getCustName());
                    Global.ord_type = Global.OrderType.INVOICE;
                    break;
                }
                case ESTIMATE: {
                    // title.setText("Estimate");
                    custName.setText(myPref.getCustName());
                    Global.ord_type = Global.OrderType.ESTIMATE;
                    break;
                }
                case REFUND: {
                    custName.setText(myPref.getCustName());
                    plusBut.setVisibility(View.INVISIBLE);
                    btnTemplate
                            .setBackgroundResource(R.drawable.disabled_gloss_button_selector);
                    btnTemplate.setOnClickListener(null);
                    btnHold.setBackgroundResource(R.drawable.disabled_gloss_button_selector);
                    btnHold.setOnClickListener(null);
                    btnSign.setBackgroundResource(R.drawable.disabled_gloss_button_selector);
                    btnSign.setOnClickListener(null);

                    consignmentType = Global.consignmentType;
                    switch (Global.consignmentType) {
                        case ORDER:
                            // title.setText("Rack");
                            break;
                        case CONSIGNMENT_RETURN:
                            // title.setText("Return");
                            Global.ord_type = Global.OrderType.RETURN;
                            break;
                        case CONSIGNMENT_FILLUP:
                            // title.setText("Fill-up");
                            Global.ord_type = Global.OrderType.CONSIGNMENT_FILLUP;
                            break;
                        case CONSIGNMENT_PICKUP:
                            Global.ord_type = Global.OrderType.CONSIGNMENT_PICKUP;
                            // title.setText("Pick-up");
                            break;
                    }
                    break;
                }
            }
        } else {
            switch ((Global.TransactionType) extras.get("option_number")) {
                case SALE_RECEIPT: {
                    caseSelected = Global.TransactionType.SALE_RECEIPT;
                    Global.ord_type = Global.OrderType.SALES_RECEIPT;
                    break;
                }
                case RETURN: {
                    caseSelected = Global.TransactionType.RETURN;
                    Global.ord_type = Global.OrderType.RETURN;
                    break;
                }
                case INVOICE: {
                    caseSelected = Global.TransactionType.INVOICE;
                    getActivity().setResult(2);
                    Global.ord_type = Global.OrderType.INVOICE;
                    break;
                }
            }
        }

        receiptListView.setOnItemClickListener(this);

        setupListView();

        runJustBeforeBeingDrawn(slidingDrawer, new Runnable() {
            @Override
            public void run() {
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) receiptListView
                        .getLayoutParams();
                mlp.setMargins(mlp.leftMargin, 0, mlp.rightMargin,
                        slidingDrawer.getHeight());
                receiptListView.invalidateViews();
                reCalculate();
            }
        });
        slidingDrawer.setOnDrawerOpenListener(this);
        slidingDrawer.setOnDrawerCloseListener(this);
        slidingDrawer.open();

        return view;
    }

    // @Override
    // public void onResume() {
    //
    // if (receiptListView != null) {
    // global = (Global) getActivity().getApplication();
    // receiptListView.invalidateViews();
    //
    // if(callBackRecalculate!=null)
    // callBackRecalculate.recalculateTotal();
    //
    // }
    // super.onResume();
    // }

    // @Override
    // public void onDestroy()
    // {
    // super.onDestroy();
    // String t = "";
    // }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment frag;
            switch (position) {
                case 0: // Fragment # 0 - This will show image
                    if (OrderTotalDetails_FR.getFrag() == null) {
                        frag = OrderTotalDetails_FR.init(position);
                    } else
                        frag = OrderTotalDetails_FR.getFrag();
                    callBackRecalculate = (RecalculateCallback) frag;
                    return frag;
                case 1: // Fragment # 1 - This will show image
                    return OrderLoyalty_FR.init(position);
                default:// Fragment # 2-9 - Will show list
                    return OrderRewards_FR.init(position);

            }
        }
    }

    private void setupListView() {
        mainLVAdapter = new ReceiptMainLV_Adapter(activity);
        receiptListView.setAdapter(mainLVAdapter);
//		reCalculate();
    }

    private void overridePrice(final int position) {
        final EditText input = new EditText(activity);
        final HashMap<String, String> map = prodHandler
                .getDiscountDetail(global.orderProducts.get(position).discount_id);

        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        input.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                numberUtils.parseInputedCurrency(s, input);
            }
        });

        new AlertDialog.Builder(activity)
                .setTitle(getString(R.string.enter_price))
                .setView(input)
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface thisDialog,
                                                int which) {
                                // TODO Auto-generated method stub
                                String value = NumberUtils.cleanCurrencyFormatedNumber(input);
                                if (!value.isEmpty()) {
                                    BigDecimal new_price = Global.getBigDecimalNum(Double.toString((Global.formatNumFromLocale(value))));
                                    BigDecimal prod_qty = new BigDecimal("0");
                                    BigDecimal new_subtotal = new BigDecimal("0");
                                    try {
                                        prod_qty = new BigDecimal(
                                                global.orderProducts
                                                        .get(position).ordprod_qty);
                                    } catch (Exception e) {
                                        prod_qty = new BigDecimal("0");
                                    }

                                    String temp = Double.toString(Global
                                            .formatNumFromLocale(value));
                                    if (!map.isEmpty()) {
                                        if (map.get("discount_type")
                                                .toUpperCase(
                                                        Locale.getDefault())
                                                .trim().equals("FIXED")) {
                                            new_subtotal = new_price
                                                    .multiply(prod_qty)
                                                    .subtract(
                                                            new BigDecimal(
                                                                    map.get("discount_price")));
                                            // global.orderProducts.get(position).getSetData("disAmount",
                                            // false, new
                                            // BigDecimal(map.get("discount_price")));
                                            // global.orderProducts.get(position).getSetData("disTotal",
                                            // false, new
                                            // BigDecimal(map.get("discount_price")));
                                        } else {
                                            BigDecimal rate = new BigDecimal(
                                                    map.get("discount_price"))
                                                    .divide(new BigDecimal(
                                                            "100"));
                                            rate = rate.multiply(new_price
                                                    .multiply(prod_qty));

                                            new_subtotal = new_price.multiply(
                                                    prod_qty).subtract(rate);
                                            // global.orderProducts.get(position).getSetData("disAmount",
                                            // false,
                                            // Global.getRoundBigDecimal(rate));
                                            global.orderProducts.get(position).disTotal = Global
                                                    .getRoundBigDecimal(rate);
                                            global.orderProducts.get(position).discount_value = Global
                                                    .getRoundBigDecimal(rate);

                                        }
                                    } else
                                        new_subtotal = new_price
                                                .multiply(prod_qty);
                                    // double
                                    // global.orderProducts.get(position).getSetData("ordprod_qty",
                                    // true, temp);

                                    global.orderProducts.get(position).overwrite_price = temp;
                                    global.orderProducts.get(position).prod_price = temp;
                                    global.orderProducts.get(position).itemSubtotal = Global
                                            .getRoundBigDecimal(new_subtotal);
                                    global.orderProducts.get(position).itemTotal = Global
                                            .getRoundBigDecimal(new_subtotal);
                                    global.orderProducts.get(position).pricelevel_id = "";
                                    global.orderProducts.get(position).prod_price_updated = "0";

                                    // global.recalculateOrderProduct(position);
                                    receiptListView.invalidateViews();
                                    reCalculate();
                                }
                                thisDialog.dismiss();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface thisDialog,
                                                int which) {
                                // TODO Auto-generated method stub
                                thisDialog.dismiss();
                            }
                        }).show();

    }

    public static long lastClickTime = 0;

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent;
        if (SystemClock.elapsedRealtime() - lastClickTime < 500) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();
        switch (v.getId()) {

            case R.id.plusButton:
                intent = new Intent(getActivity(), ViewCustomers_FA.class);
                startActivityForResult(intent, 0);
                break;
            case R.id.addProdButton:
                callBackAddProd.addProductServices();
                break;
            case R.id.templateButton:
                loadSaveTemplate();
                break;
            case R.id.holdButton:

                if (global.orderProducts != null && global.orderProducts.size() > 0) {
                    processOrder("", true);

                } else
                    Toast.makeText(activity,
                            getString(R.string.warning_empty_products),
                            Toast.LENGTH_SHORT).show();
                break;
            case R.id.detailsButton:
                intent = new Intent(getActivity(), OrderDetailsActivity.class);
                startActivity(intent);
                break;
            case R.id.signButton:
                orientation = getResources().getConfiguration().orientation;
                intent = new Intent(getActivity(), DrawReceiptActivity.class);
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    intent.putExtra("inPortrait", true);
                else
                    intent.putExtra("inPortrait", false);
                activity.startActivityForResult(intent, 0);
                break;
            case R.id.btnReturn:
                OrderingMain_FA.returnItem = !OrderingMain_FA.returnItem;
                OrderingMain_FA.switchHeaderTitle(OrderingMain_FA.returnItem,
                        getString(R.string.return_title));
                break;
            case R.id.btnScrollLeft:
                btnScrollLeft.setVisibility(View.GONE);
                btnScrollRight.setVisibility(View.VISIBLE);

                btnReturn.setVisibility(View.VISIBLE);
                btnHold.setVisibility(View.VISIBLE);
                btnDetails.setVisibility(View.VISIBLE);
                btnSign.setVisibility(View.VISIBLE);

                btnTemplate.setVisibility(View.GONE);

                break;
            case R.id.btnScrollRight:
                btnScrollLeft.setVisibility(View.VISIBLE);
                btnScrollRight.setVisibility(View.GONE);

                btnReturn.setVisibility(View.GONE);
                btnHold.setVisibility(View.GONE);
                btnDetails.setVisibility(View.GONE);
                btnSign.setVisibility(View.GONE);

                btnTemplate.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        // TODO Auto-generated method stub
        final int removePos = position;
        if (restLVAdapter != null)
            position = restLVAdapter.dataPosition(position);
        String isVoidedItem = global.orderProducts.get(position).item_void;

        if (!isVoidedItem.equals("1")) {
            final Dialog dialog = new Dialog(activity,
                    R.style.Theme_TransparentTest);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setContentView(R.layout.picked_item_dialog);

            TextView itemName = (TextView) dialog.findViewById(R.id.itemName);
            itemName.setText(global.orderProducts.get(position).ordprod_name);

            Button remove = (Button) dialog.findViewById(R.id.removeButton);
            Button cancel = (Button) dialog.findViewById(R.id.cancelButton);
            Button modify = (Button) dialog.findViewById(R.id.modifyButton);
            Button overridePrice = (Button) dialog
                    .findViewById(R.id.overridePriceButton);
            Button payWithLoyalty = (Button) dialog
                    .findViewById(R.id.btnPayWithLoyalty);

            final int pos = position;
            remove.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (myPref
                            .getPreferences(MyPreferences.pref_require_password_to_remove_void)) {
                        showPromptManagerPassword(REMOVE_ITEM, pos, removePos);
                    } else {
                        proceedToRemove(pos, removePos);
                    }

                    dialog.dismiss();
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();

                }
            });

            modify.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent(getActivity(),
                            PickerProduct_FA.class);
                    intent.putExtra("isModify", true);
                    intent.putExtra("modify_position", pos);
                    startActivityForResult(intent, 0);

                    dialog.dismiss();
                }
            });

            overridePrice.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    dialog.dismiss();

                    if (myPref
                            .getPreferences(MyPreferences.pref_skip_manager_price_override)) {
                        overridePrice(pos);
                    } else {
                        showPromptManagerPassword(OVERWRITE_PRICE, pos, pos);
                    }
                }
            });

            payWithLoyalty.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();
                    if (!Boolean.parseBoolean(global.orderProducts.get(pos).payWithPoints)) {
                        String price = global.orderProducts.get(pos).prod_price_points;
                        if (OrderLoyalty_FR.isValidPointClaim(price)) {
                            global.orderProducts.get(pos).overwrite_price = "0.00";
                            global.orderProducts.get(pos).itemTotal = "0.00";
                            global.orderProducts.get(pos).itemSubtotal = "0.00";
                            global.orderProducts.get(pos).payWithPoints = "true";
                            refreshView();
                        } else
                            Global.showPrompt(activity,
                                    R.string.dlog_title_error,
                                    "Not enough points available");
                    } else {
                        Global.showPrompt(activity, R.string.dlog_title_error,
                                "Points claimed");
                    }
                }
            });

            dialog.show();
        }
    }

    public void checkoutOrder() {
        if (receiptListView.getCount() == 0 && caseSelected != Global.TransactionType.REFUND) {
            Toast.makeText(activity,
                    getString(R.string.warning_empty_products),
                    Toast.LENGTH_SHORT).show();
        } else if (Global.isInventoryTransfer) {
            processInventoryTransfer();
        } else if (myPref
                .getPreferences(MyPreferences.pref_signature_required_mode)
                && global.encodedImage.isEmpty()) {
            Toast.makeText(activity, R.string.warning_signature_required,
                    Toast.LENGTH_LONG).show();
        } else if (myPref.getPreferences(MyPreferences.pref_require_address)
                && global.getSelectedAddressString().isEmpty()) {
            Toast.makeText(activity, R.string.warning_ship_address_required,
                    Toast.LENGTH_LONG).show();
        } else {

            if (myPref
                    .getPreferences(MyPreferences.pref_skip_want_add_more_products)) {
                if (myPref.getPreferences(MyPreferences.pref_skip_email_phone))
                    processOrder("", false);
                else
                    showEmailDlog();
            } else {
                showAddMoreProductsDlg();
            }
        }
    }



    public void showEmailDlog() {

        final Dialog dialog = new Dialog(activity,
                R.style.Theme_TransparentTest);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.checkout_dialog_layout);
        final EditText input = (EditText) dialog.findViewById(R.id.emailTxt);
        final EditText phoneNum = (EditText) dialog
                .findViewById(R.id.phoneNumField);
        Button done = (Button) dialog.findViewById(R.id.OKButton);

        if (myPref.isCustSelected())
            input.setText(myPref.getCustEmail());

        done.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dialog.dismiss();

                if (!input.getText().toString().isEmpty()) {
                    if (checkEmail(input.getText().toString()))
                        processOrder(input.getText().toString(), false);
                    else
                        Toast.makeText(activity,
                                getString(R.string.warning_email_invalid),
                                Toast.LENGTH_LONG).show();
                } else
                    processOrder(input.getText().toString(), false);
            }
        });
        dialog.show();
    }

    private void showAddMoreProductsDlg() {

        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(true);
        dlog.setCanceledOnTouchOutside(true);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        viewMsg.setText(R.string.dlog_msg_add_more_products);
        Button btnYes = (Button) dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = (Button) dlog.findViewById(R.id.btnDlogRight);
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);

        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);

        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlog.dismiss();
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlog.dismiss();

                if (myPref.getPreferences(MyPreferences.pref_skip_email_phone))
                    processOrder("", false);
                else
                    showEmailDlog();
            }
        });
        dlog.show();
    }

    private boolean checkEmail(String paramString) {
        String[] emails = paramString.split(";");

        for (String email : emails) {
            if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches())
                return false;
        }
        return true;
    }

    private void processOrder(String emailHolder,
                              boolean buttonOnHold) {

        OrdersHandler handler = new OrdersHandler(activity);
        OrderTaxes_DB ordTaxesDB = new OrderTaxes_DB(activity);
        global.order = buildOrder(activity, global, myPref, emailHolder);

        order_email = emailHolder;

        OrderProductsHandler handler2 = new OrderProductsHandler(activity);
        OrderProductsAttr_DB handler3 = new OrderProductsAttr_DB(activity);
        if (caseSelected != Global.TransactionType.REFUND) {
            if (buttonOnHold) {
                global.order.isOnHold = "1";

                if (Global.isFromOnHold) {
                    handler.updateFinishOnHold(Global.lastOrdID);
                    // global.order.getSetData("ord_timecreated", false,
                    // handler.updateFinishOnHold(Global.lastOrdID));
                    global.order.ord_HoldName = ord_HoldName;
                    global.order.processed = "10";
                    handler.insert(global.order);
                    global.encodedImage = "";
                    handler2.insert(global.orderProducts);
                    handler3.insert(global.ordProdAttr);

                    if (myPref
                            .getPreferences(MyPreferences.pref_restaurant_mode))
                        new printAsync().execute(true);

                    DBManager dbManager = new DBManager(activity);
                    dbManager.synchSendOrdersOnHold(false, false);
                } else
                    showOnHoldPromptName(handler, handler2);
            } else if (Global.isFromOnHold) {
                if (!voidOnHold) {
                    handler.updateFinishOnHold(Global.lastOrdID);
                    // global.order.getSetData("ord_timecreated",
                    // false,handler.updateFinishOnHold(Global.lastOrdID) );
                    global.order.processed = "10";
                    global.order.isOnHold = "0";
                    handler.insert(global.order);
                    global.encodedImage = "";
                    handler2.insert(global.orderProducts);
                    handler3.insert(global.ordProdAttr);

                    if (global.listOrderTaxes != null
                            && global.listOrderTaxes.size() > 0
                            && typeOfProcedure != Global.TransactionType.REFUND)
                        ordTaxesDB.insert(global.listOrderTaxes,
                                global.order.ord_id);

                    if (myPref
                            .getPreferences(MyPreferences.pref_restaurant_mode))
                        new printAsync().execute(true);

                    DBManager dbManager = new DBManager(activity);
                    dbManager.synchSendOrdersOnHold(false, true);
                } else {
                    handler.updateFinishOnHold(Global.lastOrdID);
                    // global.order.getSetData("ord_timecreated", false,
                    // handler.updateFinishOnHold(Global.lastOrdID));
                    global.order.isVoid = "1";
                    global.order.processed = "9";
                    handler.insert(global.order);
                    global.encodedImage = "";
                    handler2.insert(global.orderProducts);
                    handler3.insert(global.ordProdAttr);

                    if (global.listOrderTaxes != null
                            && global.listOrderTaxes.size() > 0
                            && typeOfProcedure != Global.TransactionType.REFUND)
                        ordTaxesDB.insert(global.listOrderTaxes,
                                global.order.ord_id);

                    new onHoldAsync().execute(CHECK_OUT_HOLD);

                }
            } else {
                handler.insert(global.order);

                handler2.insert(global.orderProducts);
                handler3.insert(global.ordProdAttr);

                if (global.listOrderTaxes != null
                        && global.listOrderTaxes.size() > 0
                        && typeOfProcedure != Global.TransactionType.REFUND)
                    ordTaxesDB.insert(global.listOrderTaxes,
                            global.order.ord_id);

                if (myPref.getPreferences(MyPreferences.pref_restaurant_mode))
                    new printAsync().execute(true);

            }
        }

        if (!buttonOnHold && !voidOnHold) {
            switch (caseSelected) {
                case SALE_RECEIPT: // is Sales Receipt
                {
                    this.updateLocalInventory(false);
                    typeOfProcedure = Global.TransactionType.PAYMENT;
                    if (OrderTotalDetails_FR.gran_total
                            .compareTo(new BigDecimal(0)) == -1) {
                        this.updateLocalInventory(true);
                        proceedToRefund();
                    } else {
                        this.updateLocalInventory(false);
                        isSalesReceipt();
                    }
                    break;
                }
                case ORDERS: {
                    if (custSelected) // is Order
                    {
                        typeOfProcedure = Global.TransactionType.ORDERS;
                        handler.updateIsProcessed(Global.lastOrdID, "1");
                        if (myPref
                                .getPreferences(MyPreferences.pref_enable_printing)) {
                            if (myPref
                                    .getPreferences(MyPreferences.pref_automatic_printing)) {
                                new printAsync().execute(false);
                            } else
                                showPrintDlg(false);
                        } else {
                            reloadDefaultTransaction();
                        }
                    } else// is Return
                    {
                        this.updateLocalInventory(true);
                        typeOfProcedure = Global.TransactionType.RETURN;
                        if (myPref
                                .getPreferences(MyPreferences.pref_return_require_refund))
                            proceedToRefund();
                        else {
                            handler.updateIsProcessed(Global.lastOrdID, "1");
                            showRefundDlg();
                        }
                    }
                    break;
                }
                case RETURN: {
                    if (custSelected) // Return
                    {
                        this.updateLocalInventory(true);
                        typeOfProcedure = Global.TransactionType.ORDERS;
                        if (myPref
                                .getPreferences(MyPreferences.pref_return_require_refund))
                            proceedToRefund();
                        else {
                            handler.updateIsProcessed(Global.lastOrdID, "1");
                            showRefundDlg();
                        }
                    } else {
                        proceedToRefund();
                    }
                    break;
                }
                case INVOICE: {
                    if (custSelected) // Invoice
                    {
                        handler.updateIsProcessed(Global.lastOrdID, "1");
                        this.updateLocalInventory(false);
                        typeOfProcedure = Global.TransactionType.RETURN;
                        showPaymentDlg();
                    }
                    break;
                }
                case ESTIMATE: {
                    if (custSelected) // Estimate
                    {
                        handler.updateIsProcessed(Global.lastOrdID, "1");
                        typeOfProcedure = Global.TransactionType.INVOICE;
                        if (myPref
                                .getPreferences(MyPreferences.pref_enable_printing)) {
                            if (myPref
                                    .getPreferences(MyPreferences.pref_automatic_printing)) {
                                new printAsync().execute(false);
                            } else
                                showPrintDlg(false);
                        } else
                            reloadDefaultTransaction();
                    }

                    break;
                }
                case CONSIGNMENT: // Consignment
                {

                    if (consignmentType == Global.OrderType.CONSIGNMENT_PICKUP || consignmentType == Global.OrderType.CONSIGNMENT_FILLUP) {
                        processConsignment();
                        Intent intent = new Intent(activity,
                                ConsignmentCheckout_FA.class);
                        intent.putExtra("consignmentType", consignmentType);
                        intent.putExtra("ord_signature", global.encodedImage);
                        global.encodedImage = "";
                        startActivity(intent);
                        activity.finish();
                    } else {
                        updateConsignmentType(true);
                    }
                    break;
                }
            }
            // global.orderProducts = new ArrayList<OrderProducts>();
            // global.qtyCounter.clear();
        }
    }

    public static Order buildOrder(Activity activity, Global global,
                                   MyPreferences myPref, String _email) {
        Order order = new Order(activity);

        order.ord_total = Global
                .getRoundBigDecimal(OrderTotalDetails_FR.gran_total);
        order.ord_subtotal = Global
                .getRoundBigDecimal(OrderTotalDetails_FR.sub_total.subtract(OrderTotalDetails_FR.itemsDiscountTotal));

        order.ord_id = Global.lastOrdID;
        order.ord_signature = global.encodedImage;
        order.qbord_id = Global.lastOrdID.replace("-", "");

        order.c_email = _email;

        order.cust_id = myPref.getCustID();
        order.custidkey = myPref.getCustIDKey();

        order.ord_type = Global.ord_type.getCodeString();

        order.tax_id = OrderTotalDetails_FR.taxID;
        order.ord_discount_id = OrderTotalDetails_FR.discountID;

        if (myPref.getIsVAT()) {
            order.VAT = "1";
        }

        int totalLines = global.orderProducts.size();
        for (OrderProducts orderProduct : global.orderProducts) {
            order.ord_lineItemDiscount += orderProduct.discount_value;
        }
        if (myPref.getPreferences(MyPreferences.pref_restaurant_mode)
                && Global.orderProductAddonsMap != null
                && Global.orderProductAddonsMap.size() > 0) {
            String[] keys = Global.orderProductAddonsMap.keySet().toArray(
                    new String[Global.orderProductAddonsMap.size()]);
            for (String key : keys) {

                totalLines += Global.orderProductAddonsMap.get(key).size();

            }
        }

        if (!myPref.getShiftIsOpen())
            order.clerk_id = myPref.getShiftClerkID();
        else if (myPref.getPreferences(MyPreferences.pref_use_clerks))
            order.clerk_id = myPref.getClerkID();

        order.total_lines = Integer.toString(totalLines);
        order.ord_taxamount = Global
                .getRoundBigDecimal(OrderTotalDetails_FR.tax_amount);
        order.ord_discount = Global
                .getRoundBigDecimal(OrderTotalDetails_FR.discount_amount);

        order.ord_shipvia = global.getSelectedShippingMethodString();
        order.ord_delivery = global.getSelectedDeliveryDate();
        order.ord_terms = global.getSelectedTermsMethodsString();
        order.ord_shipto = global.getSelectedAddressString();
        order.ord_comment = global.getSelectedComments();
        order.ord_po = global.getSelectedPO();

        String[] location = Global.getCurrLocation(activity);
        order.ord_latitude = location[0];
        order.ord_longitude = location[1];

        return order;
    }

    private void processInventoryTransfer() {
        TransferLocations_DB dbLocations = new TransferLocations_DB(activity);
        TransferInventory_DB dbInventory = new TransferInventory_DB(activity);
        Global.transferLocation = new TransferLocations_Holder(activity);
        GenerateNewID generateID = new GenerateNewID(activity);
        String _temp_id = generateID.getNextID(IdType.ORDER_ID);

        Global.transferLocation.set(TransferLocations_DB.trans_id, _temp_id);
        Global.transferLocation.set(TransferLocations_DB.loc_key_from,
                Global.locationFrom.get(Locations_DB.loc_key));
        Global.transferLocation.set(TransferLocations_DB.loc_key_to,
                Global.locationTo.get(Locations_DB.loc_key));

        int size = global.orderProducts.size();
        for (int i = 0; i < size; i++) {
            TransferInventory_Holder inventory = new TransferInventory_Holder();
            inventory.set(TransferInventory_DB.prod_id,
                    global.orderProducts.get(i).prod_id);
            inventory.set(TransferInventory_DB.prod_qty,
                    global.orderProducts.get(i).ordprod_qty);
            inventory.set(TransferInventory_DB.trans_id,
                    Global.transferLocation.get(TransferLocations_DB.trans_id));
            Global.transferInventory.add(inventory);
        }

        dbLocations.insert(Global.transferLocation);
        dbInventory.insert(Global.transferInventory);

        reloadDefaultTransaction();
    }

    private void processConsignment() {
        GenerateNewID idGenerator = new GenerateNewID(activity);
        HashMap<String, HashMap<String, String>> summaryMap = new HashMap<String, HashMap<String, String>>();
        HashMap<String, String> tempMap = new HashMap<String, String>();
        CustomerInventory custInventory;
        int size, size2;
        String[] temp;

        switch (consignmentType) {

            case ORDER:// Rack
                Global.consignment_order = global.order;
                Global.consignment_products = global.orderProducts;
                Global.consignment_qtyCounter = new HashMap<String, String>(
                        global.qtyCounter);

                size2 = Global.custInventoryKey.size();
                size = Global.consignment_products.size();

                double sold = 0;
                for (int i = 0; i < size; i++) {
                    Global.consignMapKey
                            .add(Global.consignment_products.get(i).prod_id);
                    tempMap.put("rack",
                            Global.consignment_products.get(i).ordprod_qty);
                    tempMap.put("rack_index", Integer.toString(i));

                    temp = Global.custInventoryMap.get(Global.consignment_products
                            .get(i).prod_id);
                    if (temp != null) {
                        sold = Double.parseDouble(temp[2])
                                - Double.parseDouble(tempMap.get("rack"));
                        tempMap.put("original_qty", temp[2]);
                        tempMap.put("invoice", Double.toString(sold));

                    }

                    tempMap.put("prod_id",
                            Global.consignment_products.get(i).prod_id);
                    tempMap.put("ordprod_name",
                            Global.consignment_products.get(i).ordprod_name);

                    tempMap.put(
                            "invoice_total",
                            Double.toString(sold
                                    * Double.parseDouble(Global.consignment_products
                                    .get(i).overwrite_price)));

                    tempMap.put("prod_price",
                            Global.consignment_products.get(i).overwrite_price);
                    summaryMap.put(Global.consignment_products.get(i).prod_id,
                            tempMap);
                    tempMap = new HashMap<String, String>();
                }

                for (int i = 0; i < size2; i++) {
                    if (!Global.consignMapKey.contains(Global.custInventoryKey
                            .get(i))) {
                        temp = Global.custInventoryMap.get(Global.custInventoryKey
                                .get(i));
                        if (temp[3] == null || temp[3].isEmpty())
                            temp[3] = prodHandler
                                    .getProductPrice(Global.consignment_products
                                            .get(i).prod_id);
                        tempMap.put("invoice", temp[2]);
                        tempMap.put(
                                "invoice_total",
                                Double.toString(Double.parseDouble(temp[2])
                                        * Double.parseDouble(temp[3])));
                        tempMap.put("original_qty", temp[2]);
                        tempMap.put("prod_id", temp[0]);
                        tempMap.put("ordprod_name", temp[1]);

                        tempMap.put("prod_price", temp[3]);
                        summaryMap.put(temp[0], tempMap);
                        Global.consignMapKey.add(temp[0]);
                        tempMap = new HashMap<String, String>();
                    }
                }

                Global.consignSummaryMap = summaryMap;
                Global.lastOrdID = "";

                break;

            case CONSIGNMENT_RETURN:// Returns
                Global.cons_return_order = global.order;
                Global.cons_return_products = global.orderProducts;
                Global.cons_return_qtyCounter = global.qtyCounter;

                size = Global.cons_return_products.size();
                double invoiceTotal,
                        invoiceQty;

                for (int i = 0; i < size; i++) {
                    tempMap = Global.consignSummaryMap
                            .get(Global.cons_return_products.get(i).prod_id);
                    if (tempMap == null) {
                        tempMap = new HashMap<String, String>();
                        Global.consignMapKey
                                .add(Global.cons_return_products.get(i).prod_id);
                        tempMap.put("prod_id",
                                Global.cons_return_products.get(i).prod_id);
                        tempMap.put("ordprod_name",
                                Global.cons_return_products.get(i).ordprod_name);
                        tempMap.put("prod_price",
                                Global.cons_return_products.get(i).overwrite_price);
                    } else {
                        invoiceTotal = Double.parseDouble(tempMap
                                .get("invoice_total"));
                        invoiceQty = Double.parseDouble(tempMap.get("invoice"));
                        invoiceTotal -= Double.parseDouble(tempMap
                                .get("prod_price"))
                                * Double.parseDouble(Global.cons_return_products
                                .get(i).ordprod_qty);
                        invoiceQty -= Double
                                .parseDouble(Global.cons_return_qtyCounter
                                        .get(Global.cons_return_products.get(i).prod_id));
                        tempMap.put("invoice", Double.toString(invoiceQty));
                        tempMap.put("invoice_total", Double.toString(invoiceTotal));
                    }
                    tempMap.put("return",
                            Global.cons_return_products.get(i).ordprod_qty);
                    tempMap.put("return_index", Integer.toString(i));

                    Global.consignSummaryMap.put(
                            Global.cons_return_products.get(i).prod_id, tempMap);
                }

                break;

            case CONSIGNMENT_FILLUP:// Fill-up
                Global.cons_fillup_order = global.order;
                Global.cons_fillup_products = global.orderProducts;
                Global.cons_fillup_qtyCounter = global.qtyCounter;

                Global.custInventoryList = new ArrayList<CustomerInventory>();
                custInventory = new CustomerInventory();
                double invoiceTotalTemp;
                size = Global.cons_fillup_products.size();

                if (Global.cons_return_products.size() > 0 && size > 0) {
                    Global.lastOrdID = idGenerator.getNextID(IdType.ORDER_ID);
                }

                Global.cons_fillup_order.ord_id = Global.lastOrdID;

                for (int i = 0; i < size; i++) {
                    tempMap = Global.consignSummaryMap
                            .get(Global.cons_fillup_products.get(i).prod_id);
                    if (tempMap == null) {
                        tempMap = new HashMap<String, String>();
                        Global.consignMapKey
                                .add(Global.cons_fillup_products.get(i).prod_id);
                        tempMap.put("prod_id",
                                Global.cons_fillup_products.get(i).prod_id);
                        tempMap.put("ordprod_name",
                                Global.cons_fillup_products.get(i).ordprod_name);
                        // tempMap.put("prod_price",
                        // Global.cons_fillup_products.get(i).getSetData("overwrite_price",
                        // true, null));
                    }

                    tempMap.put("fillup",
                            Global.cons_fillup_products.get(i).ordprod_qty);
                    tempMap.put("prod_price",
                            Global.cons_fillup_products.get(i).overwrite_price);
                    if (tempMap.get("invoice") != null)
                        invoiceTotalTemp = Double.parseDouble(tempMap
                                .get("invoice"))
                                * Double.parseDouble(tempMap.get("prod_price"));
                    else
                        invoiceTotalTemp = 0;

                    tempMap.put("invoice_total", Double.toString(invoiceTotalTemp));
                    tempMap.put("fillup_index", Integer.toString(i));

                    Global.cons_fillup_products.get(i).ord_id = Global.lastOrdID;
                    Global.consignSummaryMap.put(
                            Global.cons_fillup_products.get(i).prod_id, tempMap);
                }

                String tempProdID;
                String fillUpQty;
                String rackQty;
                double newStockQty;
                size2 = Global.consignMapKey.size();
                for (int i = 0; i < size2; i++) {
                    tempProdID = Global.consignMapKey.get(i);
                    custInventory.prod_id = tempProdID;
                    custInventory.prod_name = Global.consignSummaryMap.get(
                            tempProdID).get("ordprod_name");
                    custInventory.price = Global.consignSummaryMap.get(tempProdID)
                            .get("prod_price");
                    custInventory.cust_id = myPref.getCustID();

                    if (Global.consignSummaryMap.get(tempProdID).get("fillup") != null
                            || Global.consignSummaryMap.get(tempProdID).get("rack") != null) {
                        fillUpQty = Global.consignSummaryMap.get(tempProdID).get(
                                "fillup");
                        if (fillUpQty == null)
                            fillUpQty = "0";
                        rackQty = Global.consignSummaryMap.get(tempProdID).get(
                                "rack");
                        if (rackQty == null)
                            rackQty = "0";

                        newStockQty = Double.parseDouble(fillUpQty)
                                + Double.parseDouble(rackQty);
                        custInventory.qty = Double.toString(newStockQty);
                    } else {
                        custInventory.qty = "0";
                    }

                    Global.custInventoryList.add(custInventory);
                    custInventory = new CustomerInventory();
                }
                break;

            case CONSIGNMENT_PICKUP:// pickup
                Global.consignment_order = global.order;
                Global.consignment_products = global.orderProducts;
                Global.consignment_qtyCounter = global.qtyCounter;

                size = Global.consignment_products.size();

                Global.custInventoryList = new ArrayList<CustomerInventory>();
                custInventory = new CustomerInventory();

                for (int i = 0; i < size; i++) {
                    Global.consignMapKey
                            .add(Global.consignment_products.get(i).prod_id);
                    tempMap.put("pickup",
                            Global.consignment_products.get(i).ordprod_qty);
                    tempMap.put("prod_id",
                            Global.consignment_products.get(i).prod_id);
                    tempMap.put("ordprod_name",
                            Global.consignment_products.get(i).ordprod_name);
                    tempMap.put("prod_price",
                            Global.consignment_products.get(i).overwrite_price);

                    temp = Global.custInventoryMap.get(Global.consignment_products
                            .get(i).prod_id);
                    if (temp != null) {
                        tempMap.put("original_qty", temp[2]);
                        custInventory.prod_id = tempMap.get("prod_id");
                        custInventory.prod_name = tempMap.get("ordprod_name");
                        custInventory.price = tempMap.get("prod_price");
                        custInventory.cust_id = myPref.getCustID();
                        custInventory.qty = Double.toString(Double
                                .parseDouble(tempMap.get("original_qty"))
                                - Double.parseDouble(tempMap.get("pickup")));

                        Global.custInventoryList.add(custInventory);
                        custInventory = new CustomerInventory();
                    }

                    summaryMap.put(Global.consignment_products.get(i).prod_id,
                            tempMap);

                    tempMap = new HashMap<String, String>();
                }

                Global.consignSummaryMap = summaryMap;
                break;
        }
    }

    private void updateConsignmentType(boolean shouldProcess) {
        if (shouldProcess)
            processConsignment();

        consignmentType = Global.OrderType.getByCode(consignmentType.getCode() + 1);

        Global.consignmentType = consignmentType;
        String title = "";
        switch (consignmentType) {
            case ORDER:
                // title.setText("Rack");
                title = activity.getString(R.string.consignment_stacked);
                break;
            case CONSIGNMENT_RETURN:
                // title.setText("Return");
                Global.ord_type = Global.OrderType.RETURN;
                title = activity.getString(R.string.consignment_returned);
                break;
            case CONSIGNMENT_FILLUP:
                // title.setText("Fill-up");
                Global.ord_type = Global.OrderType.CONSIGNMENT_FILLUP;
                title = activity.getString(R.string.consignment_filledup);
                break;
            case CONSIGNMENT_PICKUP:
                // title.setText("Pick-up");
                Global.ord_type = Global.OrderType.CONSIGNMENT_PICKUP;
                title = activity.getString(R.string.consignment_pickup);
                break;
        }

        global.orderProducts = new ArrayList<OrderProducts>();
        global.qtyCounter = new HashMap<String, String>();
        if (mainLVAdapter != null)
            mainLVAdapter.notifyDataSetChanged();
        else
            restLVAdapter.notifyDataSetChanged();
        callBackUpdateHeaderTitle.updateHeaderTitle(title);

        reCalculate();
    }

    private void isSalesReceipt() {
        Intent intent = new Intent(activity, SelectPayMethod_FA.class);
        intent.putExtra("typeOfProcedure", typeOfProcedure);
        intent.putExtra("salesreceipt", true);
        intent.putExtra(
                "amount",
                Global.getRoundBigDecimal(OrderTotalDetails_FR.gran_total
                        .compareTo(new BigDecimal(0)) == -1 ? OrderTotalDetails_FR.gran_total
                        .negate() : OrderTotalDetails_FR.gran_total));
        intent.putExtra("paid", "0.00");
        intent.putExtra("is_receipt", true);
        intent.putExtra("job_id", global.order.ord_id);
        intent.putExtra("ord_subtotal", global.order.ord_subtotal);
        intent.putExtra("ord_taxID", OrderTotalDetails_FR.taxID);
        intent.putExtra("ord_type", Global.ord_type);
        intent.putExtra("ord_email", order_email);

        if (myPref.isCustSelected()) {
            intent.putExtra("cust_id", myPref.getCustID());
            intent.putExtra("custidkey", myPref.getCustIDKey());
        }
        startActivityForResult(intent, 0);
    }

    private void updateLocalInventory(boolean isIncrement) {
        EmpInvHandler eiHandler = new EmpInvHandler(activity);
        int size = global.orderProducts.size();
        for (int i = 0; i < size; i++) {
            eiHandler.updateOnHand(global.orderProducts.get(i).prod_id,
                    global.orderProducts.get(i).ordprod_qty, isIncrement);
        }
    }

    private void showPromptManagerPassword(final int type, final int position,
                                           final int removePos) {

        final Dialog globalDlog = new Dialog(activity,
                R.style.Theme_TransparentTest);
        globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        globalDlog.setCancelable(true);
        globalDlog.setContentView(R.layout.dlog_field_single_two_btn);

        final EditText viewField = (EditText) globalDlog
                .findViewById(R.id.dlogFieldSingle);
        viewField.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        TextView viewTitle = (TextView) globalDlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) globalDlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        if (!validPassword)
            viewMsg.setText(R.string.invalid_password);
        else
            viewMsg.setText(R.string.dlog_title_enter_manager_password);
        Button cancelBtn = (Button) globalDlog.findViewById(R.id.btnDlogRight);
        cancelBtn.setText(R.string.button_cancel);
        cancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                globalDlog.dismiss();
            }
        });

        Button btnOk = (Button) globalDlog.findViewById(R.id.btnDlogLeft);
        btnOk.setText(R.string.button_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                globalDlog.dismiss();
                String value = viewField.getText().toString().trim();
                if (value.equals(myPref.posManagerPass(true, null))) // validate
                // admin
                // password
                {
                    validPassword = true;
                    switch (type) {
                        case REMOVE_ITEM:
                            proceedToRemove(position, removePos);
                            break;
                        case OVERWRITE_PRICE:
                            overridePrice(position);
                            break;
                    }

                } else {
                    globalDlog.dismiss();
                    validPassword = false;
                    showPromptManagerPassword(type, position, removePos);
                }
            }
        });
        globalDlog.show();
    }

    private void showOnHoldPromptName(final OrdersHandler handler,
                                      final OrderProductsHandler handler2) {
        final Dialog globalDlog = new Dialog(activity,
                R.style.Theme_TransparentTest);
        globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        globalDlog.setCancelable(false);
        globalDlog.setContentView(R.layout.dlog_field_single_layout);

        final EditText viewField = (EditText) globalDlog
                .findViewById(R.id.dlogFieldSingle);
        viewField.setInputType(InputType.TYPE_CLASS_TEXT);
        TextView viewTitle = (TextView) globalDlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) globalDlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        viewMsg.setText(R.string.enter_name);
        Button btnCancel = (Button) globalDlog.findViewById(R.id.btnCancelDlogSingle);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globalDlog.dismiss();
            }
        });
        Button btnOk = (Button) globalDlog.findViewById(R.id.btnDlogSingle);
        btnOk.setText(R.string.button_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                globalDlog.dismiss();
                String value = viewField.getText().toString().trim();
                if (!value.isEmpty()) {
                    global.order.ord_HoldName = value;
                    global.order.processed = "10";
                    handler.insert(global.order);
                    global.encodedImage = "";
                    handler2.insert(global.orderProducts);

                    new printAsync().execute(true);

                    global.orderProducts = new ArrayList<OrderProducts>();
                    global.qtyCounter.clear();
                    global.resetOrderDetailsValues();

                    DBManager dbManager = new DBManager(activity);
                    dbManager.synchSendOrdersOnHold(false, false);
                } else {
                    showOnHoldPromptName(handler, handler2);
                }
            }
        });
        globalDlog.show();
    }

    public void voidCancelOnHold(int type) {
        switch (type) {
            case 1:// void hold
                voidOnHold = true;
                processOrder("", false);
                break;
            case 2:// cancel hold
                voidOnHold = false;
                processOrder("", true);
                break;
        }
    }

    private class onHoldAsync extends AsyncTask<Integer, Integer, String> {
        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(getActivity());
            myProgressDialog.setMessage("Sending...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            if (myProgressDialog.isShowing())
                myProgressDialog.dismiss();

        }

        @Override
        protected String doInBackground(Integer... arg0) {
            // TODO Auto-generated method stub

            if (Global.isConnectedToInternet(activity)) {
                Post httpClient = new Post();
                switch (arg0[0]) {
                    case UPDATE_HOLD_STATUS:
                        httpClient.postData(Global.S_UPDATE_STATUS_ON_HOLD,
                                activity, Global.lastOrdID);
                        break;
                    case CHECK_OUT_HOLD:
                        httpClient.postData(Global.S_CHECKOUT_ON_HOLD, activity,
                                Global.lastOrdID);
                        break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();
            if (voidOnHold)
                getActivity().finish();
        }

    }

    private void showPrintDlg(boolean isRetry) {

        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
        if (isRetry) {
            viewTitle.setText(R.string.dlog_title_error);
            viewMsg.setText(R.string.dlog_msg_failed_print);
        } else {
            viewTitle.setText(R.string.dlog_title_confirm);
            viewMsg.setText(R.string.dlog_msg_want_to_print);
        }
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);

        Button btnYes = (Button) dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = (Button) dlog.findViewById(R.id.btnDlogRight);
        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);

        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlog.dismiss();
                new printAsync().execute(false);

            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlog.dismiss();
                reloadDefaultTransaction();
            }
        });
        dlog.show();

    }

    private class printAsync extends AsyncTask<Boolean, Integer, String> {
        boolean isPrintStationPrinter = false;
        boolean printSuccessful = true;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(getActivity());
            myProgressDialog.setMessage("Printing...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            if (myProgressDialog.isShowing())
                myProgressDialog.dismiss();

            if (OrderingMain_FA.instance._msrUsbSams != null
                    && OrderingMain_FA.instance._msrUsbSams.isDeviceOpen()) {
                OrderingMain_FA.instance._msrUsbSams.CloseTheDevice();
            }

        }

        @Override
        protected void onProgressUpdate(Integer... params) {
            if (!myProgressDialog.isShowing())
                myProgressDialog.show();
        }

        @Override
        protected String doInBackground(Boolean... params) {
            // TODO Auto-generated method stub

            isPrintStationPrinter = params[0];
            if (!isPrintStationPrinter) {
                publishProgress();
                Global.OrderType type = Global.ord_type;
                if (Global.mainPrinterManager != null
                        && Global.mainPrinterManager.currentDevice != null) {
                    printSuccessful = Global.mainPrinterManager.currentDevice
                            .printTransaction(global.order.ord_id, type, false,
                                    false);
                }
            } else {
                OrderProductsHandler handler2 = new OrderProductsHandler(
                        activity);
                HashMap<String, List<Orders>> temp = handler2
                        .getStationPrinterProducts(global.order.ord_id);

                String[] sArr = temp.keySet().toArray(
                        new String[temp.keySet().size()]);
                int printMap;
                for (String aSArr : sArr) {
                    if (Global.multiPrinterMap.containsKey(aSArr)) {
                        printMap = Global.multiPrinterMap.get(aSArr);
//                        Global.multiPrinterManager.get(printMap).currentDevice = Global.mainPrinterManager.currentDevice;
                        if (Global.multiPrinterManager.get(printMap) != null
                                && Global.multiPrinterManager.get(printMap).currentDevice != null)
                            Global.multiPrinterManager.get(printMap).currentDevice
                                    .printStationPrinter(temp.get(aSArr),
                                            global.order.ord_id);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();
            if (!isPrintStationPrinter) {
                if (printSuccessful) {
                    reloadDefaultTransaction();
                } else {
                    showPrintDlg(true);
                }
            }
            // if(printSuccessful && !isPrintStationPrinter)
            // {
            // reloadDefaultTransaction();
            // }
            // else if(!printSuccessful && !isPrintStationPrinter)
            // {
            // showPrintDlg(true);
            // }

            // if(!isPrintStationPrinter)
            // {
            // myProgressDialog.dismiss();
            // reloadDefaultTransaction();
            // }

        }
    }

    private void proceedToRefund() {
        Intent intent = new Intent(activity, SelectPayMethod_FA.class);
        intent.putExtra("typeOfProcedure", typeOfProcedure);
        intent.putExtra("salesrefund", true);
        intent.putExtra(
                "amount",
                Global.getRoundBigDecimal(OrderTotalDetails_FR.gran_total
                        .compareTo(new BigDecimal(0)) == -1 ? OrderTotalDetails_FR.gran_total
                        .negate() : OrderTotalDetails_FR.gran_total));
        intent.putExtra("paid", "0.00");
        intent.putExtra("job_id", global.order.ord_id);
        intent.putExtra("ord_type", Global.ord_type);
        intent.putExtra("ord_email", order_email);

        if (myPref.isCustSelected()) {
            intent.putExtra("cust_id", myPref.getCustID());
            intent.putExtra("custidkey", myPref.getCustIDKey());
        }

        startActivityForResult(intent, 0);
    }

    private void showRefundDlg() {
        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        viewMsg.setText(R.string.dlog_msg_want_to_make_refund);
        Button btnYes = (Button) dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = (Button) dlog.findViewById(R.id.btnDlogRight);
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);
        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);

        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlog.dismiss();
                proceedToRefund();

            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlog.dismiss();

                if (myPref.getPreferences(MyPreferences.pref_enable_printing)) {
                    if (myPref
                            .getPreferences(MyPreferences.pref_automatic_printing)) {
                        new printAsync().execute(false);
                    } else
                        showPrintDlg(false);
                } else {
                    reloadDefaultTransaction();
                }
            }
        });
        dlog.show();
    }

    private void showPaymentDlg() {

        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        viewMsg.setText(R.string.take_payment_now);
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);
        Button btnYes = (Button) dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = (Button) dlog.findViewById(R.id.btnDlogRight);
        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);

        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlog.dismiss();
                Intent intent = new Intent(activity, SelectPayMethod_FA.class);
                intent.putExtra("typeOfProcedure", typeOfProcedure);
                intent.putExtra("salesinvoice", true);
                intent.putExtra("ord_subtotal", global.order.ord_subtotal);
                intent.putExtra("ord_taxID", OrderTotalDetails_FR.taxID);
                intent.putExtra("amount", global.order.ord_total);
                intent.putExtra("paid", "0.00");
                intent.putExtra("job_id", global.order.ord_id);
                intent.putExtra("ord_type", Global.ord_type);
                intent.putExtra("ord_email", order_email);

                if (myPref.isCustSelected()) {
                    intent.putExtra("cust_id", myPref.getCustID());
                    intent.putExtra("custidkey", myPref.getCustIDKey());
                }

                startActivityForResult(intent, 0);

            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlog.dismiss();
                if (myPref.getPreferences(MyPreferences.pref_enable_printing)) {
                    if (myPref
                            .getPreferences(MyPreferences.pref_automatic_printing)) {
                        new printAsync().execute(false);
                    } else
                        showPrintDlg(false);
                } else
                    reloadDefaultTransaction();
            }
        });
        dlog.show();
    }

    private void proceedToRemove(int pos, int removePos) {
        // String quant = global.orderProducts.get(pos).ordprod_qty;
        // String prodID = global.orderProducts.get(pos).prod_id;
        OrderProducts product = global.orderProducts.get(pos);
        if (myPref.getPreferences(MyPreferences.pref_allow_decimal_quantities)) {
            double totalQty = (Double) Global.getFormatedNumber(true,
                    global.qtyCounter.get(product.prod_id));
            double qty = Double.parseDouble(product.ordprod_qty);
            double sum = totalQty - qty;
            global.qtyCounter.put(product.prod_id, Double.toString(sum));
        } else {
            int totalQty = (Integer) Global.getFormatedNumber(false,
                    global.qtyCounter.get(product.prod_id));
            int qty = Integer.parseInt(product.ordprod_qty);
            int sum = totalQty - qty;

            global.qtyCounter.put(product.prod_id, Integer.toString(sum));
        }

        if (myPref
                .getPreferences(MyPreferences.pref_show_removed_void_items_in_printout)) {
            product.item_void = "1";
            String val = global.orderProducts.get(pos).ordprod_name;

            product.ordprod_name = val + " [VOIDED]";
            product.overwrite_price = "0";
        } else {
            OrderProductsHandler ordProdDB = new OrderProductsHandler(activity);
            ordProdDB.deleteOrderProduct(product.ordprod_id);

            if (Global.addonSelectionMap != null)
                Global.addonSelectionMap.remove(product.ordprod_id);
            if (Global.orderProductAddonsMap != null
                    && !Global.orderProductAddonsMap.isEmpty()) {
                if (Global.orderProductAddonsMap.get(product.ordprod_id) != null)
                    for (OrderProducts op : Global.orderProductAddonsMap
                            .get(product.ordprod_id)) {
                        ordProdDB.deleteOrderProduct(op.ordprod_id);
                    }

                Global.orderProductAddonsMap.remove(product.ordprod_id);
            }
            global.orderProducts.remove(pos);
        }

        receiptListView.invalidateViews();
        reCalculate();
        Catalog_FR.instance.refreshListView();

        if (restLVAdapter != null) {
            restLVAdapter.updateDivisionPos(removePos);
        }
    }

//    private class SectionController extends DragSortController {
//
//        private ReceiptRestLV_Adapter mAdapter;
//
//        DragSortListView mDslv;
//
//        public SectionController(DragSortListView dslv,
//                                 ReceiptRestLV_Adapter adapter) {
//            super(dslv, R.id.dragDropIcon, DragSortController.ON_DOWN, 0);
//            setRemoveEnabled(false);
//            mDslv = dslv;
//            mAdapter = adapter;
//        }
//
//        @Override
//        public int startDragPosition(MotionEvent ev) {
//            int res = super.dragHandleHitPosition(ev);
//            int width = mDslv.getWidth();
//
//            if ((int) ev.getX() > width / 2) {
//                return res;
//            } else {
//                return DragSortController.MISS;
//            }
//        }
//
//        @Override
//        public View onCreateFloatView(int position) {
//            View v = mAdapter.getView(position, null, mDslv);
//            v.setBackgroundResource(R.drawable.bg_handle_drag_selection);
//            v.getBackground().setLevel(10000);
//            return v;
//        }
//
//        private int origHeight = -1;
//
//        @Override
//        public void onDragFloatView(View floatView, Point floatPoint,
//                                    Point touchPoint) {
//            if (origHeight == -1) {
//                origHeight = floatView.getHeight();
//            }
//
//            if (touchPoint.x > mDslv.getWidth() / 2) {
//                float scale = touchPoint.x - mDslv.getWidth() / 2;
//                scale /= (float) (mDslv.getWidth() / 5);
//                ViewGroup.LayoutParams lp = floatView.getLayoutParams();
//                lp.height = Math.max(origHeight, (int) (scale * origHeight));
//                floatView.setLayoutParams(lp);
//            }
//        }
//
//        @Override
//        public void onDestroyFloatView(View floatView) {
//            // do nothing; block super from crashing
//        }
//    }

    public void reCalculate() {
        pagerAdapter.getItem(0);
        if (callBackRecalculate != null) {
            callBackRecalculate.recalculateTotal();
            pagerAdapter.notifyDataSetChanged();
        }
    }

    private void loadSaveTemplate() {
        if (myPref.isCustSelected()) {
            final TemplateHandler handleTemplate = new TemplateHandler(activity);
            final Dialog dlog = new Dialog(activity,
                    R.style.Theme_TransparentTest);
            dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dlog.setCanceledOnTouchOutside(true);
            dlog.setContentView(R.layout.dlog_btn_left_right_layout);

            TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
            TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
            viewTitle.setText(R.string.dlog_title_choose_action);
            // viewMsg.setText(R.string.dlog_msg_want_to_make_refund);
            viewMsg.setVisibility(View.GONE);
            Button btnSave = (Button) dlog.findViewById(R.id.btnDlogLeft);
            Button btnLoad = (Button) dlog.findViewById(R.id.btnDlogRight);
            btnSave.setText(R.string.button_save);
            btnLoad.setText(R.string.button_load);
            Button btnCancel = (Button) dlog.findViewById(R.id.btnDlogCancel);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dlog.dismiss();
                }
            });
            btnSave.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    handleTemplate.insert(myPref.getCustID());
                    dlog.dismiss();
                    Global.showPrompt(
                            activity,
                            R.string.dlog_title_confirm,
                            activity.getString(R.string.dlog_msg_template_saved));
                }
            });
            btnLoad.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    // TODO Auto-generated method stub
                    List<HashMap<String, String>> mapList = handleTemplate
                            .getTemplate(myPref.getCustID());
                    int size = mapList.size();
                    global.orderProducts = new ArrayList<OrderProducts>();

                    OrderProducts ordProd = new OrderProducts();
                    Orders anOrder = new Orders();
                    for (int i = 0; i < size; i++) {
                        ordProd.ordprod_id = mapList.get(i).get("ordprod_id");
                        ordProd.prod_id = mapList.get(i).get("prod_id");
                        ordProd.ordprod_name = mapList.get(i).get("prod_name");
                        ordProd.overwrite_price = mapList.get(i).get(
                                "overwrite_price");
                        ordProd.ordprod_qty = mapList.get(i).get("ordprod_qty");
                        ordProd.itemTotal = mapList.get(i).get("itemTotal");
                        ordProd.itemSubtotal = mapList.get(i).get(
                                "itemSubtotal");
                        ordProd.ord_id = mapList.get(i).get("ord_id");
                        ordProd.ordprod_desc = mapList.get(i).get(
                                "ordprod_desc");
                        ordProd.prod_istaxable = mapList.get(i).get(
                                "prod_istaxable");

                        anOrder.setValue(mapList.get(i).get("prod_price"));
                        anOrder.setQty(mapList.get(i).get("ordprod_qty"));

                        ordProd.tax_position = "0";
                        ordProd.discount_position = "0";
                        ordProd.pricelevel_position = "0";
                        ordProd.uom_position = "0";

                        global.orderProducts.add(ordProd);
                        global.qtyCounter.put(mapList.get(i).get("prod_id"),
                                mapList.get(i).get("ordprod_qty"));

                        ordProd = new OrderProducts();
                        anOrder = new Orders();
                    }
                    receiptListView.invalidateViews();
                    reCalculate();
                    dlog.dismiss();
                }
            });
            dlog.show();
        } else
            Toast.makeText(activity,
                    getString(R.string.warning_no_customer_selected),
                    Toast.LENGTH_LONG).show();

    }

    @Override
    public void onDrawerClosed() {
        // TODO Auto-generated method stub
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) receiptListView
                .getLayoutParams();

        int dp = (int) (getResources().getDimension(
                R.dimen.add_orders_slider_semiclosed_size) / getResources()
                .getDisplayMetrics().density);
        mlp.setMargins(mlp.leftMargin, 0, mlp.rightMargin, dp);
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (20 * scale + 0.5f);
        receiptListView.setPadding(0, 0, 0, dpAsPixels);
        receiptListView.invalidateViews();
    }

    @Override
    public void onDrawerOpened() {
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) receiptListView
                .getLayoutParams();
        mlp.setMargins(mlp.leftMargin, 0, mlp.rightMargin,
                slidingDrawer.getHeight());
        receiptListView.invalidateViews();
    }

    public void refreshView() {
        if (mainLVAdapter != null)
            mainLVAdapter.notifyDataSetChanged();
        else
            restLVAdapter.notifyDataSetChanged();
        reCalculate();
    }

    private void reloadDefaultTransaction() {
        String type = myPref
                .getPreferencesValue(MyPreferences.pref_default_transaction);
        int transType = -1;
        global.order = new Order(activity);
        try {
            if (type == null || type.isEmpty())
                type = "-1";
            transType = Integer.parseInt(type);
        } catch (NumberFormatException e) {
            transType = -1;
        }

        if (transType != -1) {
            Intent intent = null;
            switch (transType) {
                case 0:// sales receipt
                    intent = new Intent(SalesTab_FR.activity, OrderingMain_FA.class);
                    intent.putExtra("option_number", Global.TransactionType.SALE_RECEIPT);
                    OrderTotalDetails_FR.resetView();
                    activity.finish();
                    SalesTab_FR.activity.startActivityForResult(intent, 0);
                    break;
                case 2:// return
                    intent = new Intent(SalesTab_FR.activity, OrderingMain_FA.class);
                    intent.putExtra("option_number", Global.TransactionType.RETURN);
                    OrderTotalDetails_FR.resetView();
                    activity.finish();
                    SalesTab_FR.activity.startActivityForResult(intent, 0);
                    break;

                default:
                    activity.finish();
                    break;
            }
        } else {
            activity.finish();
        }
    }

    private void runJustBeforeBeingDrawn(final View view,
                                         final Runnable runnable) {
        final OnPreDrawListener preDrawListener = new OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                runnable.run();
                return true;
            }
        };
        view.getViewTreeObserver().addOnPreDrawListener(preDrawListener);
    }

}
