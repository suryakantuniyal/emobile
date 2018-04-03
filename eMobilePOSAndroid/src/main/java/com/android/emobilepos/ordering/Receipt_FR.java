package com.android.emobilepos.ordering;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
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
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dao.AssignEmployeeDAO;
import com.android.dao.DinningTableOrderDAO;
import com.android.dao.ShiftDAO;
import com.android.database.CustomersHandler;
import com.android.database.DBManager;
import com.android.database.EmpInvHandler;
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
import com.android.emobilepos.adapters.OrderProductListAdapter;
import com.android.emobilepos.consignment.ConsignmentCheckout_FA;
import com.android.emobilepos.customer.ViewCustomers_FA;
import com.android.emobilepos.holders.TransferInventory_Holder;
import com.android.emobilepos.holders.TransferLocations_Holder;
import com.android.emobilepos.models.BCRMacro;
import com.android.emobilepos.models.DataTaxes;
import com.android.emobilepos.models.OrderSeatProduct;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Product;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.orders.OrderProduct;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.OrderAttributes;
import com.android.emobilepos.models.salesassociates.Template;
import com.android.emobilepos.payment.SelectPayMethod_FA;
import com.android.emobilepos.security.SecurityManager;
import com.android.support.Customer;
import com.android.support.CustomerInventory;
import com.android.support.GenerateNewID;
import com.android.support.GenerateNewID.IdType;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NetworkUtils;
import com.android.support.NumberUtils;
import com.android.support.OnHoldsManager;
import com.android.support.OrderProductUtils;
import com.android.support.SemiClosedSlidingDrawer;
import com.android.support.SemiClosedSlidingDrawer.OnDrawerCloseListener;
import com.android.support.SemiClosedSlidingDrawer.OnDrawerOpenListener;
import com.android.support.SynchMethods;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.viewpagerindicator.CirclePageIndicator;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import drivers.EMSBluetoothStarPrinter;
import interfaces.PayWithLoyalty;
import util.StringUtil;
import util.json.JsonUtils;

public class Receipt_FR extends Fragment implements OnClickListener,
        OnItemClickListener, OnDrawerOpenListener, OnDrawerCloseListener, PayWithLoyalty {

    public static long lastClickTime = 0;
    private final int REMOVE_ITEM = 0, OVERWRITE_PRICE = 1,
            UPDATE_HOLD_STATUS = 1, CHECK_OUT_HOLD = 2;
    public ListView receiptListView;
    public TextView custName;
    public OrderProductListAdapter mainLVAdapter;
    public OrderTotalDetails_FR orderTotalDetailsFr;
    public OrderRewards_FR orderRewardsFr;
    private AddProductBtnCallback callBackAddProd;
    private boolean isToGo;
    private Order onHoldOrder;
    private SemiClosedSlidingDrawer slidingDrawer;
    private Global.TransactionType caseSelected = Global.TransactionType.SALE_RECEIPT;
    private boolean custSelected;
    private Global.OrderType consignmentType;
    private Global.TransactionType typeOfProcedure = Global.TransactionType.SALE_RECEIPT;
    private MyPreferences myPref;
    private int orientation = 0;
    private boolean validPassword = true;
    private ProductsHandler prodHandler;
    private String ord_HoldName = "";
    private ProgressDialog myProgressDialog;
    //    private boolean voidOnHold = false;
    private Button btnTemplate;
    private Button btnHold;
    private Button btnDetails;
    private Button btnSign;
    private Button btnReturn;
    private ImageButton btnScrollRight;
    private ImageButton btnScrollLeft;
    private ReceiptPagerAdapter pagerAdapter;
    private RecalculateCallback callBackRecalculate;
    private UpdateHeaderTitleCallback callBackUpdateHeaderTitle;
    private String order_email = "";
    private Bundle extras;

    public Receipt_FR() {

    }

    public static Receipt_FR getInstance(Order onHoldOrder) {
        Receipt_FR receipt_fr = new Receipt_FR();
        receipt_fr.onHoldOrder = onHoldOrder;
        return receipt_fr;
    }

    public static Order buildOrder(Activity activity, Global global,
                                   String _email, String ord_HoldName, String assignedTable, String associateId,
                                   List<OrderAttributes> orderAttributes, List<DataTaxes> orderTaxes, List<OrderProduct> orderProducts) {
        OrderingMain_FA orderingMainFa = (OrderingMain_FA) activity;
        orderingMainFa.buildOrderStarted = true;
        MyPreferences myPref = new MyPreferences(activity);
        AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee(false);

        Order order = new Order(activity);
        order.setOrderProducts(orderProducts);
        order.assignedTable = assignedTable;
        order.associateID = associateId;
        order.ord_total = String.valueOf(Global
                .getRoundBigDecimal(OrderTotalDetails_FR.gran_total));
        order.ord_subtotal = String.valueOf(Global.getRoundBigDecimal(OrderTotalDetails_FR.sub_total));
        if (Global.lastOrdID == null || Global.lastOrdID.isEmpty()) {
            GenerateNewID generator = new GenerateNewID(activity);
            Global.lastOrdID = generator.getNextID(IdType.ORDER_ID);
        }
        order.setListOrderTaxes(orderTaxes);
        order.ord_id = Global.lastOrdID;
        order.ord_signature = global.encodedImage;
        order.qbord_id = GenerateNewID.getQBOrderId(Global.lastOrdID);
        order.ord_HoldName = ord_HoldName;
        order.c_email = _email;
        order.cust_id = myPref.getCustID();
        order.custidkey = myPref.getCustIDKey();
        order.ord_type = Global.ord_type == null ? "" : Global.ord_type.getCodeString();
        order.tax_id = OrderTotalDetails_FR.taxID;
        order.ord_discount_id = OrderTotalDetails_FR.discountID;
        if (global.order != null) {
            order.ord_timecreated = global.order.ord_timecreated;
        }
        if (assignEmployee.isVAT()) {
            order.VAT = "1";
        }
        int totalLines = global.order.getOrderProducts().size();
        for (OrderProduct orderProduct : global.order.getOrderProducts()) {
            order.ord_lineItemDiscount = String.valueOf(Global.getBigDecimalNum(order.ord_lineItemDiscount)
                    .add(Global.getBigDecimalNum(orderProduct.getDiscount_value())));
        }
        if (myPref.isUseClerks()) {
            order.clerk_id = myPref.getClerkID();
        } else if (ShiftDAO.isShiftOpen()) {
            order.clerk_id = String.valueOf(ShiftDAO.getOpenShift().getClerkId());
        }

        order.total_lines = Integer.toString(totalLines);
        order.ord_taxamount = String.valueOf(Global
                .getRoundBigDecimal(OrderTotalDetails_FR.tax_amount));
        order.ord_discount = String.valueOf(Global
                .getRoundBigDecimal(OrderTotalDetails_FR.discount_amount));
        order.ord_shipvia = global.getSelectedShippingMethodString();
        order.ord_delivery = global.getSelectedDeliveryDate();
        order.ord_terms = global.getSelectedTermsMethodsString();
        order.ord_shipto = global.getSelectedAddressString();
        order.ord_comment = global.getSelectedComments();
        order.ord_po = global.getSelectedPO();
        Location currLocation = Global.getCurrLocation(activity, false);
        order.ord_latitude = String.valueOf(currLocation.getLatitude());
        order.ord_longitude = String.valueOf(currLocation.getLongitude());
        order.orderAttributes = orderAttributes;
        return order;
    }

    public static void updateLocalInventory(Activity activity, List<OrderProduct> orderProducts, boolean isIncrement) {
        EmpInvHandler eiHandler = new EmpInvHandler(activity);
//        int size = orderProducts.size();
//        for (OrderProduct product : orderProducts) {
        eiHandler.updateOnHand(orderProducts);
//        }
//        for (int i = 0; i < size; i++) {
//            eiHandler.updateOnHand(orderProducts.get(i).getProd_id(), orderProducts.get(i).getOrdprod_qty(), isIncrement);
//        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_receipt_main_layout,
                container, false);

        myPref = new MyPreferences(getActivity());
        if (savedInstanceState == null) {
            if (onHoldOrder == null) {
                getOrderingMainFa().global.order = new Order(getActivity());
            } else {
                getOrderingMainFa().global.order = onHoldOrder;
            }
        } else if (getOrderingMainFa().global.order == null) {
            Log.d("Ordering Main", "Restore OrderingMain NULL order global. Activity finished.");
            getActivity().finish();
            return null;
        }
        extras = getActivity().getIntent().getExtras();
        typeOfProcedure = (Global.TransactionType) extras.get("option_number");
        isToGo = ((OrderingMain_FA) getActivity()).isToGo;
        prodHandler = new ProductsHandler(getActivity());
        callBackAddProd = (AddProductBtnCallback) getActivity();
        callBackUpdateHeaderTitle = (UpdateHeaderTitleCallback) getActivity();

        custName = view.findViewById(R.id.membersField);
        receiptListView = view.findViewById(R.id.receiptListView);
        slidingDrawer = view
                .findViewById(R.id.slideDrawer);

        ViewPager viewPager = view.findViewById(R.id.orderViewPager);
        pagerAdapter = new ReceiptPagerAdapter(getFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        CirclePageIndicator pagerIndicator = view
                .findViewById(R.id.indicator);
        pagerIndicator.setViewPager(viewPager);
        pagerIndicator.setCurrentItem(0);

        orientation = getResources().getConfiguration().orientation;

        Button addProd = view.findViewById(R.id.addProdButton);
        addProd.setOnClickListener(this);
        if (myPref.isTablet()
                && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            addProd.setVisibility(View.GONE);
        }

        Button addSeatButton = view.findViewById(R.id.addSeatButton);
        addSeatButton.setOnClickListener(this);
        if (!myPref.isRestaurantMode() || !myPref.getPreferences(MyPreferences.pref_enable_togo_eatin)) {
            addSeatButton.setVisibility(View.GONE);
        }
        ImageView plusBut = view.findViewById(R.id.plusButton);
//        plusBut.setOnClickListener(this);

        LinearLayout customerLinearLayout = view.findViewById(R.id.customerLinearLayout);
        customerLinearLayout.setOnClickListener(this);

        btnTemplate = view.findViewById(R.id.templateButton);
        btnTemplate.setOnClickListener(this);

        btnHold = view.findViewById(R.id.holdButton);
        btnHold.setOnClickListener(this);
        if (myPref.isRestaurantMode()) {
            btnHold.setText(getString(R.string.button_send));
        } else {
            btnHold.setText(getString(R.string.button_hold));
        }

        btnDetails = view.findViewById(R.id.detailsButton);
        btnDetails.setOnClickListener(this);

        btnSign = view.findViewById(R.id.signButton);
        btnSign.setOnClickListener(this);

        btnReturn = view.findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(this);

        btnScrollLeft = view.findViewById(R.id.btnScrollLeft);
        btnScrollLeft.setOnClickListener(this);

        btnScrollRight = view.findViewById(R.id.btnScrollRight);
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
                    setCustName();
                    Global.ord_type = Global.OrderType.SALES_RECEIPT;
                    break;
                }
                case ORDERS: {
                    // title.setText("Order");
                    setCustName();
                    Global.ord_type = Global.OrderType.ORDER;
                    break;
                }
                case RETURN: {
                    // title.setText("Return");
                    setCustName();
                    Global.ord_type = Global.OrderType.RETURN;
                    break;
                }
                case INVOICE: {
                    // title.setText("Invoice");
                    setCustName();
                    Global.ord_type = Global.OrderType.INVOICE;
                    break;
                }
                case ESTIMATE: {
                    // title.setText("Estimate");
                    setCustName();
                    Global.ord_type = Global.OrderType.ESTIMATE;
                    break;
                }
                case CONSIGNMENT: {
                    setCustName();
                    plusBut.setVisibility(View.INVISIBLE);
                    customerLinearLayout.setOnClickListener(null);
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
                            Global.ord_type = Global.OrderType.CONSIGNMENT_INVOICE;
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
            if (extras.get("option_number") != null) {
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
                        caseSelected = Global.TransactionType.SALE_RECEIPT;
                        getActivity().setResult(2);
                        Global.ord_type = Global.OrderType.SALES_RECEIPT;
                        break;
                    }
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
//                reCalculate();
            }
        });
        slidingDrawer.setOnDrawerOpenListener(this);
        slidingDrawer.setOnDrawerCloseListener(this);
        slidingDrawer.open();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (extras.containsKey("BCRMacro")) {
            String json = extras.getString("BCRMacro");
            Gson gson = JsonUtils.getInstance();
            BCRMacro bcrMacro = gson.fromJson(json, BCRMacro.class);
            if (bcrMacro.getBcrMacroParams().isLoadTemplate()) {
                loadCustomerTemplate();
            }
        }
    }

    private void setupListView() {
        OrderingMain_FA orderingMain_fa = (OrderingMain_FA) getActivity();
        mainLVAdapter = new OrderProductListAdapter(getActivity(), getOrderingMainFa().global.order.getOrderProducts(), orderingMain_fa);
        receiptListView.setAdapter(mainLVAdapter);
        if (orderingMain_fa.openFromHold && !orderingMain_fa.isToGo) {
            addHoldOrderSeats();
        }
        mainLVAdapter.notifyDataSetChanged();
    }

    private void addHoldOrderSeats() {
        for (OrderProduct product : getOrderingMainFa().global.order.getOrderProducts()) {
            mainLVAdapter.addSeat(product);
        }
    }

    private void overridePrice(final int position) {
        final EditText input = new EditText(getActivity());

        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        input.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                NumberUtils.parseInputedCurrency(s, input);
            }
        });

        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.enter_price))
                .setView(input)
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface thisDialog,
                                                int which) {
                                String value = NumberUtils.cleanCurrencyFormatedNumber(input);
                                if (!value.isEmpty()) {
                                    getOrderingMainFa().global.order.getOrderProducts().get(position).setOverwritePrice(Global.getBigDecimalNum(value), getActivity());

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
                                thisDialog.dismiss();
                            }
                        }).show();

    }

    @Override
    public void onClick(View v) {
        Intent intent;
        if (SystemClock.elapsedRealtime() - lastClickTime < 500) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();
        switch (v.getId()) {
            case R.id.addSeatButton:
                mainLVAdapter.addSeat();
                if (isToGo) {
                    isToGo = false;
                    ((OrderingMain_FA) getActivity()).isToGo = false;
                    ((OrderingMain_FA) getActivity()).setRestaurantSaleType(Global.RestaurantSaleType.EAT_IN);
                    String firstSeat = mainLVAdapter.getFirstSeat();
                    ((OrderingMain_FA) getActivity()).setSelectedSeatNumber(firstSeat);
//                    ((OrderingMain_FA) getActivity()).setSelectedDinningTableNumber("1");
                    mainLVAdapter.moveSeatItems(getOrderingMainFa().global.order.getOrderProducts(), firstSeat);
                    mainLVAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.customerLinearLayout:
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
                ((OrderingMain_FA) getActivity()).orderingAction = OrderingMain_FA.OrderingAction.HOLD;
                if (getOrderingMainFa().global.order.getOrderProducts() != null && getOrderingMainFa().global.order.getOrderProducts().size() > 0) {
                    Order order = buildOrder(getActivity(), getOrderingMainFa().global, "", ord_HoldName,
                            ((OrderingMain_FA) getActivity()).getSelectedDinningTableNumber(),
                            ((OrderingMain_FA) getActivity()).getAssociateId(), ((OrderingMain_FA) getActivity()).getOrderAttributes(),
                            ((OrderingMain_FA) getActivity()).getListOrderTaxes(), getOrderingMainFa().global.order.getOrderProducts());
                    processOrder(order, "", OrderingMain_FA.OrderingAction.HOLD, Global.isFromOnHold, false);

                } else
                    Toast.makeText(getActivity(),
                            getString(R.string.warning_empty_products),
                            Toast.LENGTH_SHORT).show();
                break;
            case R.id.detailsButton:
                intent = new Intent(getActivity(), OrderDetailsActivity.class);
                List<OrderAttributes> orderAttributes = ((OrderingMain_FA) getActivity()).getOrderAttributes();
                if (orderAttributes != null) {
                    Gson gson = JsonUtils.getInstance();
                    intent.putExtra("orderAttributes", gson.toJson(orderAttributes));
                }
                startActivityForResult(intent, 0);
                break;
            case R.id.signButton:
                orientation = getResources().getConfiguration().orientation;
                intent = new Intent(getActivity(), DrawReceiptActivity.class);
                getActivity().setRequestedOrientation(orientation);
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    intent.putExtra("inPortrait", true);
                else
                    intent.putExtra("inPortrait", false);
                getActivity().startActivityForResult(intent, 0);
                break;
            case R.id.btnReturn:
                OrderingMain_FA.returnItem = !OrderingMain_FA.returnItem;
                getOrderingMainFa().switchHeaderTitle(OrderingMain_FA.returnItem,
                        getString(R.string.return_title), getOrderingMainFa().mTransType);
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
        final OrderSeatProduct orderSeatProduct = (OrderSeatProduct) mainLVAdapter.getItem(position);
        final int orderProductIdx = orderSeatProduct.rowType == OrderProductListAdapter.RowType.TYPE_ITEM ? getOrderingMainFa().global.order.getOrderProducts().indexOf(orderSeatProduct.orderProduct) : 0;
        if (orderSeatProduct.rowType == OrderProductListAdapter.RowType.TYPE_HEADER) {
            ((OrderingMain_FA) getActivity()).setSelectedSeatNumber(orderSeatProduct.seatNumber);
            mainLVAdapter.notifyDataSetChanged();
        } else {
            final boolean hasRemoveItemPermission = SecurityManager.hasPermissions(getActivity(), SecurityManager.SecurityAction.REMOVE_ITEM);
            final boolean hasOverwritePermission = SecurityManager.hasPermissions(getActivity(), SecurityManager.SecurityAction.CHANGE_PRICE);

            String isVoidedItem = orderSeatProduct.orderProduct.getItem_void();
            final HashMap<Integer, String> subMenus = new HashMap<>();
            if (!isVoidedItem.equals("1")) {
                PopupMenu popup = new PopupMenu(getActivity(), view);
                popup.getMenuInflater().inflate(R.menu.receiptlist_product_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.modifyProduct:
                                Intent intent = new Intent(getActivity(),
                                        PickerProduct_FA.class);
                                Gson gson = JsonUtils.getInstance();
                                Product product = prodHandler.getProductDetails(orderSeatProduct.orderProduct.getProd_id());
                                if (onHoldOrder != null) {
                                    orderSeatProduct.orderProduct.setProd_price(product.getFinalPrice());
                                }
                                intent.putExtra("orderProduct", gson.toJson(orderSeatProduct.orderProduct));
                                intent.putExtra("isModify", true);
                                intent.putExtra("isFromAddon", onHoldOrder != null);
                                intent.putExtra("modify_position", orderProductIdx);
                                intent.putExtra("transType", getOrderingMainFa().mTransType);

                                startActivityForResult(intent, 0);
                                break;
                            case R.id.removeProduct:
                                if (hasRemoveItemPermission) {
                                    boolean printed = orderSeatProduct.orderProduct.isPrinted();
                                    if (printed || myPref
                                            .getPreferences(MyPreferences.pref_require_password_to_remove_void)) {
                                        showPromptManagerPassword(REMOVE_ITEM, orderProductIdx, orderProductIdx);
                                    } else {
                                        proceedToRemove(orderProductIdx);
                                    }
                                } else {
                                    Global.showPrompt(getActivity(), R.string.security_alert, getString(R.string.permission_denied));
                                }
                                break;
                            case R.id.moveProductSeat:
                                int i = 0;
                                for (OrderSeatProduct seatProduct : mainLVAdapter.orderSeatProductList) {
                                    if (seatProduct.rowType == OrderProductListAdapter.RowType.TYPE_HEADER) {
                                        if (!seatProduct.seatNumber.equalsIgnoreCase(orderSeatProduct.orderProduct.getAssignedSeat())) {
                                            item.getSubMenu().add(0, i, SubMenu.NONE, "Move items to seat " + seatProduct.seatNumber);
                                            subMenus.put(i, seatProduct.seatNumber);
                                            i++;
                                        }
                                    }
                                }
                                break;
                            case R.id.viewVariations:
                                break;
                            case R.id.payWithLoyalty:
                                processPayWithLoyalty(orderSeatProduct);
                                break;
                            case R.id.overridePrice:
                                if (hasOverwritePermission) {
                                    if (myPref.getPreferences(MyPreferences.pref_skip_manager_price_override)) {
                                        overridePrice(orderProductIdx);
                                    } else {
                                        showPromptManagerPassword(OVERWRITE_PRICE, orderProductIdx, orderProductIdx);
                                    }
                                } else {
                                    Global.showPrompt(getActivity(), R.string.security_alert, getString(R.string.permission_denied));
                                }
                                break;
                            case R.id.cancel:
                                break;
                            default:
                                if (subMenus.containsKey(item.getItemId())) {
                                    String targetSeat = subMenus.get(item.getItemId());
                                    ((OrderingMain_FA) getActivity()).setSelectedSeatNumber(targetSeat);
                                    orderSeatProduct.orderProduct.setAssignedSeat(targetSeat);
                                    orderSeatProduct.setSeatGroupId(mainLVAdapter.getSeat(targetSeat).getSeatGroupId());
                                    mainLVAdapter.notifyDataSetChanged();
                                }
                                break;
                        }
                        return true;
                    }
                });
                popup.getMenu().findItem(R.id.overridePrice).setEnabled(hasOverwritePermission);
                popup.getMenu().findItem(R.id.removeProduct).setEnabled(hasRemoveItemPermission);
                popup.getMenu().findItem(R.id.payWithLoyalty).setEnabled(Double.parseDouble(orderSeatProduct.orderProduct.getProd_price_points()) > 0);
                popup.show();
            }
        }
        receiptListView.smoothScrollToPosition(position);

    }


    @Override
    public void processPayWithLoyalty(OrderSeatProduct orderSeatProduct) {
        if (!Boolean.parseBoolean(orderSeatProduct.orderProduct.getPayWithPoints())) {
            String price = orderSeatProduct.orderProduct.getProd_price_points();
            if (getOrderingMainFa().getLoyaltyFragment().isValidPointClaim(price)) {
                orderSeatProduct.orderProduct.setOverwrite_price(null);
                orderSeatProduct.orderProduct.setItemTotal("0.00");
                orderSeatProduct.orderProduct.setProd_price("0.00");
//                                        orderSeatProduct.orderProduct.setItemSubtotal("0.00");
                orderSeatProduct.orderProduct.setPayWithPoints("true");
                refreshView();
            } else
                Global.showPrompt(getActivity(),
                        R.string.dlog_title_error,
                        "Not enough points available");
        } else {
            Global.showPrompt(getActivity(), R.string.dlog_title_error,
                    "Points claimed");
        }
    }

    public void checkoutOrder() {
        if (!getOrderingMainFa().global.order.isAllProductsRequiredAttrsCompleted()) {
            Global.showPrompt(getActivity(), R.string.dlog_title_error, getActivity().getString(R.string.dlog_msg_required_attributes));
        } else if (receiptListView.getCount() == 0 &&
                (caseSelected != Global.TransactionType.CONSIGNMENT ||
                        (caseSelected == Global.TransactionType.CONSIGNMENT && consignmentType == Global.OrderType.CONSIGNMENT_PICKUP))) {
            Toast.makeText(getActivity(),
                    getString(R.string.warning_empty_products),
                    Toast.LENGTH_SHORT).show();
        } else if (Global.isInventoryTransfer) {
            processInventoryTransfer();
        } else if (myPref
                .getPreferences(MyPreferences.pref_signature_required_mode)
                && getOrderingMainFa().global.encodedImage.isEmpty()) {
            Toast.makeText(getActivity(), R.string.warning_signature_required,
                    Toast.LENGTH_LONG).show();
        } else if (myPref.getPreferences(MyPreferences.pref_require_address)
                && getOrderingMainFa().global.getSelectedAddressString().isEmpty()) {
            Toast.makeText(getActivity(), R.string.warning_ship_address_required,
                    Toast.LENGTH_LONG).show();
        } else {

            if (myPref.getPreferences(MyPreferences.pref_skip_want_add_more_products)) {
                if (myPref.isSkipEmailPhone() && !myPref.getPreferences(MyPreferences.pref_ask_order_comments)) {
                    String email = "";
                    if (myPref.isCustSelected()) {
                        email = myPref.getCustEmail();
                    }
                    Order order = buildOrder(getActivity(), getOrderingMainFa().global, email, ord_HoldName,
                            ((OrderingMain_FA) getActivity()).getSelectedDinningTableNumber(),
                            ((OrderingMain_FA) getActivity()).getAssociateId(), ((OrderingMain_FA) getActivity()).getOrderAttributes(),
                            ((OrderingMain_FA) getActivity()).getListOrderTaxes(), getOrderingMainFa().global.order.getOrderProducts());
                    if (isToGo) {
                        processOrder(order, "", OrderingMain_FA.OrderingAction.CHECKOUT, Global.isFromOnHold, false);
                    } else {
                        if (getOrderingMainFa().global.order.getOrderProducts() != null && getOrderingMainFa().global.order.getOrderProducts().size() > 0) {
                            processOrder(order, "", OrderingMain_FA.OrderingAction.HOLD, Global.isFromOnHold, false);
                        } else {
                            getOrderingMainFa().buildOrderStarted = false;
                            Toast.makeText(getActivity(),
                                    getString(R.string.warning_empty_products),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                } else
                    showEmailDlog();
            } else {
                showAddMoreProductsDlg();
            }
        }
    }

    public void showEmailDlog() {
        final Dialog dialog = new Dialog(getActivity(),
                R.style.Theme_TransparentTest);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.checkout_dialog_layout);
        //edit text comments of dialog box
        final EditText editTextDialogComments = dialog.findViewById(R.id.fieldComments);
        //set the comments to previously entered comments in details section
        if (!getOrderingMainFa().global.getSelectedComments().isEmpty()) {
            editTextDialogComments.setText(getOrderingMainFa().global.getSelectedComments());
        }
        final EditText emailInput = dialog.findViewById(R.id.emailTxt);
        final EditText phoneNum = dialog
                .findViewById(R.id.phoneNumField);
        Button done = dialog.findViewById(R.id.OKButton);
        //if skip email phone enabled then hide fields
        if (myPref.isSkipEmailPhone()) {
            emailInput.setVisibility(View.GONE);
            phoneNum.setVisibility(View.GONE);
        }
        //if not asking for order comments then hide field
        if (!myPref.getPreferences(MyPreferences.pref_ask_order_comments)) {
            editTextDialogComments.setVisibility(View.GONE);
        }
        if (myPref.isCustSelected())
            emailInput.setText(myPref.getCustEmail());
        done.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //if we have comments set the global comments field
                if (!editTextDialogComments.getText().toString().isEmpty()) {
                    getOrderingMainFa().global.setSelectedComments(editTextDialogComments.getText().toString());
                }
                if (!emailInput.getText().toString().isEmpty()) {
                    if (checkEmail(emailInput.getText().toString())) {
                        if (isToGo) {
                            Order order = buildOrder(getActivity(), getOrderingMainFa().global, emailInput.getText().toString(), ord_HoldName,
                                    ((OrderingMain_FA) getActivity()).getSelectedDinningTableNumber(),
                                    ((OrderingMain_FA) getActivity()).getAssociateId(), ((OrderingMain_FA) getActivity()).getOrderAttributes(),
                                    ((OrderingMain_FA) getActivity()).getListOrderTaxes(), getOrderingMainFa().global.order.getOrderProducts());
                            processOrder(order, emailInput.getText().toString(), OrderingMain_FA.OrderingAction.CHECKOUT,
                                    Global.isFromOnHold, false);
                        } else {
                            if (getOrderingMainFa().global.order.getOrderProducts() != null && getOrderingMainFa().global.order.getOrderProducts().size() > 0) {
                                Order order = buildOrder(getActivity(), getOrderingMainFa().global, emailInput.getText().toString(), ord_HoldName,
                                        ((OrderingMain_FA) getActivity()).getSelectedDinningTableNumber(),
                                        ((OrderingMain_FA) getActivity()).getAssociateId(), ((OrderingMain_FA) getActivity()).getOrderAttributes(),
                                        ((OrderingMain_FA) getActivity()).getListOrderTaxes(), getOrderingMainFa().global.order.getOrderProducts());
                                processOrder(order, emailInput.getText().toString(), OrderingMain_FA.OrderingAction.HOLD,
                                        Global.isFromOnHold, false);

                            } else {
                                getOrderingMainFa().buildOrderStarted = false;
                                Toast.makeText(getActivity(),
                                        getString(R.string.warning_empty_products),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else
                        Toast.makeText(getActivity(),
                                getString(R.string.warning_email_invalid),
                                Toast.LENGTH_LONG).show();
                } else {
                    if (isToGo) {
                        Order order = buildOrder(getActivity(), getOrderingMainFa().global, emailInput.getText().toString(), ord_HoldName,
                                ((OrderingMain_FA) getActivity()).getSelectedDinningTableNumber(),
                                ((OrderingMain_FA) getActivity()).getAssociateId(), ((OrderingMain_FA) getActivity()).getOrderAttributes(),
                                ((OrderingMain_FA) getActivity()).getListOrderTaxes(), getOrderingMainFa().global.order.getOrderProducts());
                        processOrder(order, emailInput.getText().toString(), OrderingMain_FA.OrderingAction.CHECKOUT, Global.isFromOnHold, false);
                    } else {
                        if (getOrderingMainFa().global.order.getOrderProducts() != null && getOrderingMainFa().global.order.getOrderProducts().size() > 0) {
                            Order order = buildOrder(getActivity(), getOrderingMainFa().global, "", ord_HoldName,
                                    ((OrderingMain_FA) getActivity()).getSelectedDinningTableNumber(),
                                    ((OrderingMain_FA) getActivity()).getAssociateId(), ((OrderingMain_FA) getActivity()).getOrderAttributes(),
                                    ((OrderingMain_FA) getActivity()).getListOrderTaxes(), getOrderingMainFa().global.order.getOrderProducts());
                            processOrder(order, "", OrderingMain_FA.OrderingAction.HOLD, Global.isFromOnHold, false);

                        } else {
                            getOrderingMainFa().buildOrderStarted = false;
                            Toast.makeText(getActivity(),
                                    getString(R.string.warning_empty_products),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        dialog.show();
    }

    private void showAddMoreProductsDlg() {
        final Dialog dlog = new Dialog(getActivity(), R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(true);
        dlog.setCanceledOnTouchOutside(true);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);
        TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        viewMsg.setText(R.string.dlog_msg_add_more_products);
        Button btnYes = dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = dlog.findViewById(R.id.btnDlogRight);
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);
        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);
        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                if (myPref.isSkipEmailPhone() && !myPref.getPreferences(MyPreferences.pref_ask_order_comments)) {
                    String email = "";
                    if (myPref.isCustSelected()) {
                        email = myPref.getCustEmail();
                    }
                    Order order = buildOrder(getActivity(), getOrderingMainFa().global, email, ord_HoldName,
                            ((OrderingMain_FA) getActivity()).getSelectedDinningTableNumber(),
                            ((OrderingMain_FA) getActivity()).getAssociateId(), ((OrderingMain_FA) getActivity()).getOrderAttributes(),
                            ((OrderingMain_FA) getActivity()).getListOrderTaxes(), getOrderingMainFa().global.order.getOrderProducts());
                    if (isToGo) {
                        processOrder(order, "", OrderingMain_FA.OrderingAction.CHECKOUT, Global.isFromOnHold, false);
                    } else {
                        if (getOrderingMainFa().global.order.getOrderProducts() != null && getOrderingMainFa().global.order.getOrderProducts().size() > 0) {
                            processOrder(order, "", OrderingMain_FA.OrderingAction.HOLD, Global.isFromOnHold, false);
                        } else {
                            getOrderingMainFa().buildOrderStarted = false;
                            Toast.makeText(getActivity(),
                                    getString(R.string.warning_empty_products),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                } else
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

    private void processOrder(Order order, String emailHolder, OrderingMain_FA.OrderingAction orderingAction,
                              boolean isFromOnHold, boolean voidOnHold) {

        OrdersHandler ordersHandler = new OrdersHandler(getActivity());
        OrderTaxes_DB ordTaxesDB = new OrderTaxes_DB();
        getOrderingMainFa().global.order = order;
        order_email = emailHolder;
        OrderProductsHandler orderProductsHandler = new OrderProductsHandler(getActivity());
        OrderProductsAttr_DB productsAttrDb = new OrderProductsAttr_DB(getActivity());
        if (caseSelected != Global.TransactionType.REFUND) {
            if ((Global.loyaltyCardInfo != null && !Global.loyaltyCardInfo.getCardNumUnencrypted().isEmpty()) ||
                    (Global.rewardCardInfo != null && !Global.rewardCardInfo.getCardNumUnencrypted().isEmpty())) {
                BigDecimal total = Global.getBigDecimalNum(order.ord_total);
                if (total.compareTo(new BigDecimal(0)) == 0) {
                    order.processed = "1";
                }
            }
            if (orderingAction == OrderingMain_FA.OrderingAction.HOLD) {
                getOrderingMainFa().global.order.isOnHold = "1";

                if (isFromOnHold && isToGo) {
                    ordersHandler.updateFinishOnHold(Global.lastOrdID);
                    getOrderingMainFa().global.order.ord_HoldName = ord_HoldName;
                    getOrderingMainFa().global.order.processed = "10";
                    ordersHandler.insert(getOrderingMainFa().global.order);
                    getOrderingMainFa().global.encodedImage = "";
                    orderProductsHandler.insert(order.getOrderProducts());
                    productsAttrDb.insert(getOrderingMainFa().global.ordProdAttr);
                    if (myPref.isRestaurantMode()) {
                        new PrintAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);
                    }
                    new SyncOnHolds().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                    DBManager dbManager = new DBManager(getActivity());
//                    SynchMethods sm = new SynchMethods(dbManager);
//                    sm.synchSendOnHold(false, false, getActivity(), null);
                } else {
                    if (getOrderingMainFa().global.order.ord_HoldName == null || getOrderingMainFa().global.order.ord_HoldName.isEmpty()) {
                        showOnHoldPromptName(ordersHandler, orderProductsHandler);
                    } else {
                        setOrderAsHold(getOrderingMainFa().global.order.ord_HoldName, ordersHandler, orderProductsHandler);
                    }
                }
            } else if (Global.isFromOnHold) {
                if (!voidOnHold) {
                    ordersHandler.updateFinishOnHold(Global.lastOrdID);
                    getOrderingMainFa().global.order.processed = "10";
                    getOrderingMainFa().global.order.isOnHold = "1";
                    ordersHandler.insert(getOrderingMainFa().global.order);
                    getOrderingMainFa().global.encodedImage = "";
                    orderProductsHandler.insert(order.getOrderProducts());
                    productsAttrDb.insert(getOrderingMainFa().global.ordProdAttr);

                    if (getOrderingMainFa().global.order.getListOrderTaxes() != null
                            && getOrderingMainFa().global.order.getListOrderTaxes().size() > 0
                            && typeOfProcedure != Global.TransactionType.REFUND)
                        ordTaxesDB.insert(getOrderingMainFa().global.order.getListOrderTaxes(),
                                getOrderingMainFa().global.order.ord_id);
                    if (myPref.isRestaurantMode())
                        new PrintAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);
//                    DBManager dbManager = new DBManager(getActivity());
//                    SynchMethods sm = new SynchMethods(dbManager);
//                    sm.synchSendOnHold(false, true, getActivity(), global.order.ord_id);
                    new OnHoldAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, CHECK_OUT_HOLD, voidOnHold);
                } else {
                    ordersHandler.updateFinishOnHold(Global.lastOrdID);
                    getOrderingMainFa().global.order.isVoid = "1";
                    getOrderingMainFa().global.order.processed = "9";
                    ordersHandler.insert(getOrderingMainFa().global.order);
                    getOrderingMainFa().global.encodedImage = "";
                    orderProductsHandler.insert(order.getOrderProducts());
                    productsAttrDb.insert(getOrderingMainFa().global.ordProdAttr);

                    if (getOrderingMainFa().global.order.getListOrderTaxes() != null
                            && getOrderingMainFa().global.order.getListOrderTaxes().size() > 0
                            && typeOfProcedure != Global.TransactionType.REFUND)
                        ordTaxesDB.insert(getOrderingMainFa().global.order.getListOrderTaxes(),
                                getOrderingMainFa().global.order.ord_id);
                    new OnHoldAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, CHECK_OUT_HOLD, voidOnHold);
                }
            } else {
                if (getOrderingMainFa().global.order.getOrderProducts().size() > 0 ||
                        !getOrderingMainFa().global.order.ord_type.equalsIgnoreCase(Global.OrderType.CONSIGNMENT_RETURN.getCodeString())) {
                    if (!getOrderingMainFa().global.order.ord_type.equalsIgnoreCase(Global.OrderType.CONSIGNMENT_INVOICE.getCodeString()) &&
                            !getOrderingMainFa().global.order.ord_type.equalsIgnoreCase(Global.OrderType.CONSIGNMENT_RETURN.getCodeString()) &&
                            !getOrderingMainFa().global.order.ord_type.equalsIgnoreCase(Global.OrderType.CONSIGNMENT_FILLUP.getCodeString())) {
                        ordersHandler.insert(getOrderingMainFa().global.order);
                    }
                }
                if (getOrderingMainFa().global.order.getOrderProducts().size() > 0) {
                    if (!getOrderingMainFa().global.order.ord_type.equalsIgnoreCase(Global.OrderType.CONSIGNMENT_INVOICE.getCodeString())) {
                        if (!getOrderingMainFa().global.order.ord_type.equalsIgnoreCase(Global.OrderType.CONSIGNMENT_INVOICE.getCodeString()) &&
                                !getOrderingMainFa().global.order.ord_type.equalsIgnoreCase(Global.OrderType.CONSIGNMENT_RETURN.getCodeString()) &&
                                !getOrderingMainFa().global.order.ord_type.equalsIgnoreCase(Global.OrderType.CONSIGNMENT_FILLUP.getCodeString())) {
                            orderProductsHandler.insert(getOrderingMainFa().global.order.getOrderProducts());
                        }
                        productsAttrDb.insert(getOrderingMainFa().global.ordProdAttr);
                        if (getOrderingMainFa().global.order.getListOrderTaxes() != null
                                && getOrderingMainFa().global.order.getListOrderTaxes().size() > 0
                                && typeOfProcedure != Global.TransactionType.REFUND) {
                            ordTaxesDB.insert(getOrderingMainFa().global.order.getListOrderTaxes(),
                                    getOrderingMainFa().global.order.ord_id);
                        }
                    }
                }
                if (myPref.isRestaurantMode())
                    new PrintAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);

            }
        }

        if (orderingAction != OrderingMain_FA.OrderingAction.HOLD && !voidOnHold) {
            switch (caseSelected) {
                case SALE_RECEIPT: // is Sales Receipt
                {
                    updateLocalInventory(getActivity(), getOrderingMainFa().global.order.getOrderProducts(), false);
                    typeOfProcedure = Global.TransactionType.PAYMENT;
                    if (OrderTotalDetails_FR.gran_total
                            .compareTo(new BigDecimal(0)) == -1) {
//                        updateLocalInventory(getActivity(), global.order.getOrderProducts(), true);
                        proceedToRefund();
                    } else {
//                        updateLocalInventory(getActivity(), global.order.getOrderProducts(), false);
                        isSalesReceipt();
                    }
                    break;
                }
                case ORDERS: {
                    if (custSelected) // is Order
                    {
                        typeOfProcedure = Global.TransactionType.ORDERS;
                        ordersHandler.updateIsProcessed(Global.lastOrdID, "1");
                        if (myPref
                                .getPreferences(MyPreferences.pref_enable_printing)) {
                            if (myPref
                                    .getPreferences(MyPreferences.pref_automatic_printing)) {
                                new PrintAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false);
                            } else
                                showPrintDlg(false);
                        } else {
                            reloadDefaultTransaction();
                        }
                    } else// is Return
                    {
                        updateLocalInventory(getActivity(), getOrderingMainFa().global.order.getOrderProducts(), true);
                        typeOfProcedure = Global.TransactionType.RETURN;
                        if (myPref
                                .getPreferences(MyPreferences.pref_return_require_refund))
                            proceedToRefund();
                        else {
                            ordersHandler.updateIsProcessed(Global.lastOrdID, "1");
                            showRefundDlg();
                        }
                    }
                    break;
                }
                case RETURN: {
                    if (custSelected) // Return
                    {
                        updateLocalInventory(getActivity(), getOrderingMainFa().global.order.getOrderProducts(), true);
                        typeOfProcedure = Global.TransactionType.ORDERS;
                        if (myPref
                                .getPreferences(MyPreferences.pref_return_require_refund))
                            proceedToRefund();
                        else {
                            ordersHandler.updateIsProcessed(Global.lastOrdID, "1");
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
                        ordersHandler.updateIsProcessed(Global.lastOrdID, "1");
                        updateLocalInventory(getActivity(), getOrderingMainFa().global.order.getOrderProducts(), false);
                        typeOfProcedure = Global.TransactionType.RETURN;
                        if (myPref.isInvoiceRequirePayment()) {
                            openInvoicePaymentSelection();
                        } else {
                            showPaymentDlg();
                        }
                    }
                    break;
                }
                case ESTIMATE: {
                    if (custSelected) // Estimate
                    {
                        ordersHandler.updateIsProcessed(Global.lastOrdID, "1");
                        typeOfProcedure = Global.TransactionType.INVOICE;
                        if (myPref
                                .getPreferences(MyPreferences.pref_enable_printing)) {
                            if (myPref
                                    .getPreferences(MyPreferences.pref_automatic_printing)) {
                                new PrintAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false);
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
                        getOrderingMainFa().global.order.setOrderProducts(new ArrayList<OrderProduct>());
                        Intent intent = new Intent(getActivity(),
                                ConsignmentCheckout_FA.class);
                        intent.putExtra("consignmentType", consignmentType);
                        intent.putExtra("ord_signature", getOrderingMainFa().global.encodedImage);
                        getOrderingMainFa().global.encodedImage = "";
                        startActivity(intent);
                        getActivity().finish();
                    } else {
                        updateConsignmentType(true);
                    }
                    getOrderingMainFa().buildOrderStarted = false;
                    break;
                }
            }
        }
    }

    private void processInventoryTransfer() {
        TransferLocations_DB dbLocations = new TransferLocations_DB(getActivity());
        TransferInventory_DB dbInventory = new TransferInventory_DB();
        Global.transferLocation = new TransferLocations_Holder(getActivity());
        GenerateNewID generateID = new GenerateNewID(getActivity());
        String _temp_id = generateID.getNextID(IdType.INVENTORY_TRANSFER_ID);
        Global.transferLocation.setTrans_id(_temp_id);
        Global.transferLocation.setLoc_key_from(Global.locationFrom.getLoc_key());
        Global.transferLocation.setLoc_key_to(Global.locationTo.getLoc_key());
        int size = getOrderingMainFa().global.order.getOrderProducts().size();
        for (int i = 0; i < size; i++) {
            TransferInventory_Holder inventory = new TransferInventory_Holder();
            inventory.setProd_id(getOrderingMainFa().global.order.getOrderProducts().get(i).getProd_id());
            inventory.setProd_qty(getOrderingMainFa().global.order.getOrderProducts().get(i).getOrdprod_qty());
            inventory.setTrans_id(Global.transferLocation.getTrans_id());
            Global.transferInventory.add(inventory);
        }
        dbLocations.insert(Global.transferLocation);
        dbInventory.insert(Global.transferInventory);
        reloadDefaultTransaction();
    }

    private void processConsignment() {
        HashMap<String, HashMap<String, String>> summaryMap = new HashMap<>();
        HashMap<String, String> tempMap = new HashMap<>();
        CustomerInventory custInventory;
        int size, size2;
        String[] temp;
        switch (consignmentType) {
            case ORDER:// Rack
                try {
                    Global.consignment_order = (Order) getOrderingMainFa().global.order.clone();
                } catch (CloneNotSupportedException e) {

                }
                Global.consignment_products = getOrderingMainFa().global.order.getOrderProducts();
                Global.consignment_qtyCounter = OrderProductUtils.getProductQtyHashMap(getOrderingMainFa().global.order.getOrderProducts());//new HashMap<String, String>(global.qtyCounter);
                size2 = Global.custInventoryKey.size();
                size = Global.consignment_products.size();

                double sold = 0;
                for (int i = 0; i < size; i++) {
                    Global.consignMapKey
                            .add(Global.consignment_products.get(i).getProd_id());
                    tempMap.put("rack",
                            Global.consignment_products.get(i).getOrdprod_qty());
                    tempMap.put("rack_index", Integer.toString(i));
                    temp = Global.custInventoryMap.get(Global.consignment_products
                            .get(i).getProd_id());
                    if (temp != null) {
                        sold = Double.parseDouble(temp[2])
                                - Double.parseDouble(tempMap.get("rack"));
                        tempMap.put("original_qty", temp[2]);
                        tempMap.put("invoice", Double.toString(sold));

                    }
                    tempMap.put("prod_id",
                            Global.consignment_products.get(i).getProd_id());
                    tempMap.put("ordprod_name",
                            Global.consignment_products.get(i).getOrdprod_name());
                    tempMap.put(
                            "invoice_total",
                            Double.toString(sold
                                    * Double.parseDouble(Global.consignment_products
                                    .get(i).getFinalPrice())));
                    tempMap.put("prod_price",
                            Global.consignment_products.get(i).getFinalPrice());
                    summaryMap.put(Global.consignment_products.get(i).getProd_id(),
                            tempMap);
                    tempMap = new HashMap<>();
                }

                for (int i = 0; i < size2; i++) {
                    if (!Global.consignMapKey.contains(Global.custInventoryKey
                            .get(i))) {
                        temp = Global.custInventoryMap.get(Global.custInventoryKey
                                .get(i));
                        if (temp[3] == null || temp[3].isEmpty())
                            temp[3] = prodHandler
                                    .getProductPrice(Global.consignment_products
                                            .get(i).getProd_id());
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
                        tempMap = new HashMap<>();
                    }
                }
                Global.consignSummaryMap = summaryMap;
                Global.lastOrdID = "";
                break;

            case CONSIGNMENT_RETURN:// Returns
                Global.cons_return_order = getOrderingMainFa().global.order;
                Global.cons_return_products = getOrderingMainFa().global.order.getOrderProducts();
                Global.cons_return_qtyCounter = OrderProductUtils.getProductQtyHashMap(getOrderingMainFa().global.order.getOrderProducts());//global.qtyCounter;
                size = Global.cons_return_products.size();
                double invoiceTotal,
                        invoiceQty;
                for (int i = 0; i < size; i++) {
                    tempMap = Global.consignSummaryMap
                            .get(Global.cons_return_products.get(i).getProd_id());
                    if (tempMap == null) {
                        tempMap = new HashMap<>();
                        Global.consignMapKey
                                .add(Global.cons_return_products.get(i).getProd_id());
                        tempMap.put("prod_id",
                                Global.cons_return_products.get(i).getProd_id());
                        tempMap.put("ordprod_name",
                                Global.cons_return_products.get(i).getOrdprod_name());
                        tempMap.put("prod_price",
                                Global.cons_return_products.get(i).getFinalPrice());
                    } else {
                        invoiceTotal = Double.parseDouble(tempMap
                                .get("invoice_total"));
                        invoiceQty = Double.parseDouble(tempMap.get("invoice"));
                        invoiceTotal -= Double.parseDouble(tempMap
                                .get("prod_price"))
                                * Double.parseDouble(Global.cons_return_products
                                .get(i).getOrdprod_qty());
                        invoiceQty -= Double
                                .parseDouble(Global.cons_return_qtyCounter
                                        .get(Global.cons_return_products.get(i).getProd_id()));
                        tempMap.put("invoice", Double.toString(invoiceQty));
                        tempMap.put("invoice_total", Double.toString(invoiceTotal));
                    }
                    tempMap.put("return",
                            Global.cons_return_products.get(i).getOrdprod_qty());
                    tempMap.put("return_index", Integer.toString(i));

                    Global.consignSummaryMap.put(
                            Global.cons_return_products.get(i).getProd_id(), tempMap);
                }
                Global.lastOrdID = "";
                break;

            case CONSIGNMENT_FILLUP:// Fill-up
                Global.cons_fillup_order = getOrderingMainFa().global.order;
                Global.cons_fillup_products = getOrderingMainFa().global.order.getOrderProducts();
                Global.cons_fillup_qtyCounter = OrderProductUtils.getProductQtyHashMap(getOrderingMainFa().global.order.getOrderProducts());//global.qtyCounter;

                Global.custInventoryList = new ArrayList<>();
                custInventory = new CustomerInventory();
                double invoiceTotalTemp;
                size = Global.cons_fillup_products.size();
                for (int i = 0; i < size; i++) {
                    tempMap = Global.consignSummaryMap
                            .get(Global.cons_fillup_products.get(i).getProd_id());
                    if (tempMap == null) {
                        tempMap = new HashMap<>();
                        Global.consignMapKey
                                .add(Global.cons_fillup_products.get(i).getProd_id());
                        tempMap.put("prod_id",
                                Global.cons_fillup_products.get(i).getProd_id());
                        tempMap.put("ordprod_name",
                                Global.cons_fillup_products.get(i).getOrdprod_name());
                    }
                    tempMap.put("fillup",
                            Global.cons_fillup_products.get(i).getOrdprod_qty());
                    tempMap.put("prod_price",
                            Global.cons_fillup_products.get(i).getFinalPrice());
                    if (tempMap.get("invoice") != null)
                        invoiceTotalTemp = Double.parseDouble(tempMap
                                .get("invoice"))
                                * Double.parseDouble(tempMap.get("prod_price"));
                    else
                        invoiceTotalTemp = 0;
                    tempMap.put("invoice_total", Double.toString(invoiceTotalTemp));
                    tempMap.put("fillup_index", Integer.toString(i));
                    Global.cons_fillup_products.get(i).setOrd_id(Global.lastOrdID);
                    Global.consignSummaryMap.put(
                            Global.cons_fillup_products.get(i).getProd_id(), tempMap);
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
                Global.consignment_order = getOrderingMainFa().global.order;
                Global.consignment_products = getOrderingMainFa().global.order.getOrderProducts();
                Global.consignment_qtyCounter = OrderProductUtils.getProductQtyHashMap(getOrderingMainFa().global.order.getOrderProducts());//global.qtyCounter;
                size = Global.consignment_products.size();
                Global.custInventoryList = new ArrayList<>();
                custInventory = new CustomerInventory();
                for (int i = 0; i < size; i++) {
                    Global.consignMapKey
                            .add(Global.consignment_products.get(i).getProd_id());
                    tempMap.put("pickup",
                            Global.consignment_products.get(i).getOrdprod_qty());
                    tempMap.put("prod_id",
                            Global.consignment_products.get(i).getProd_id());
                    tempMap.put("ordprod_name",
                            Global.consignment_products.get(i).getOrdprod_name());
                    tempMap.put("prod_price",
                            Global.consignment_products.get(i).getFinalPrice());

                    temp = Global.custInventoryMap.get(Global.consignment_products
                            .get(i).getProd_id());
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
                    summaryMap.put(Global.consignment_products.get(i).getProd_id(),
                            tempMap);
                    tempMap = new HashMap<>();
                }
                Global.consignSummaryMap = summaryMap;
                break;
        }
    }

    private Global.OrderType getNextConsignmentType(Global.OrderType consignmentType) {
        switch (consignmentType) {
            case ORDER:
                return Global.OrderType.CONSIGNMENT_RETURN;
            case CONSIGNMENT_RETURN:
                return Global.OrderType.CONSIGNMENT_FILLUP;
            case CONSIGNMENT_FILLUP:
                return Global.OrderType.CONSIGNMENT_PICKUP;
            default:
                return consignmentType;
        }
    }

    private void updateConsignmentType(boolean shouldProcess) {
        if (shouldProcess)
            processConsignment();
        consignmentType = getNextConsignmentType(consignmentType);
        Global.consignmentType = consignmentType;
        String title = "";
        switch (consignmentType) {
            case ORDER:
                // title.setText("Rack");
                title = getActivity().getString(R.string.consignment_stacked);
                break;
            case CONSIGNMENT_RETURN:
                // title.setText("Return");
                Global.ord_type = Global.OrderType.CONSIGNMENT_RETURN;
                title = getActivity().getString(R.string.consignment_returned);
                break;
            case CONSIGNMENT_FILLUP:
                // title.setText("Fill-up");
                Global.ord_type = Global.OrderType.CONSIGNMENT_FILLUP;
                title = getActivity().getString(R.string.consignment_filledup);
                break;
            case CONSIGNMENT_PICKUP:
                // title.setText("Pick-up");
                Global.ord_type = Global.OrderType.CONSIGNMENT_PICKUP;
                title = getActivity().getString(R.string.consignment_pickup);
                break;
        }

        getOrderingMainFa().global.order.setOrderProducts(new ArrayList<OrderProduct>());
        if (mainLVAdapter != null)
            mainLVAdapter.notifyDataSetChanged();
        callBackUpdateHeaderTitle.updateHeaderTitle(title);
        setupListView();
        reCalculate();
    }

    private void showSplitedOrderPreview() {
        Gson gson = JsonUtils.getInstance();
        Type listType = new TypeToken<List<OrderSeatProduct>>() {
        }.getType();
        Intent intent = new Intent(getActivity(), SplittedOrderSummary_FA.class);
        Bundle b = new Bundle();
        String json = gson.toJson(mainLVAdapter.orderSeatProductList, listType);
        b.putString("orderSeatProductList", json);
        b.putString("tableNumber", ((OrderingMain_FA) getActivity()).getSelectedDinningTableNumber());
        b.putString("taxID", Global.taxID);
        b.putString("orderTaxId", getOrderingMainFa().global.order.tax_id);
        b.putInt("discountSelected", Global.discountPosition - 1);

        intent.putExtras(b);
        intent.putExtra("transType", getOrderingMainFa().mTransType);

        startActivityForResult(intent, 0);
    }

    private void isSalesReceipt() {
        Intent intent = new Intent(getActivity(), SelectPayMethod_FA.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("typeOfProcedure", typeOfProcedure);
        intent.putExtra("salesreceipt", true);

        intent.putExtra(
                "amount",
                String.valueOf(Global.getRoundBigDecimal(OrderTotalDetails_FR.gran_total
                        .compareTo(new BigDecimal(0)) == -1 ? OrderTotalDetails_FR.gran_total
                        .negate() : OrderTotalDetails_FR.gran_total, 2)));
        intent.putExtra("paid", "0.00");
        intent.putExtra("is_receipt", true);
        intent.putExtra("job_id", getOrderingMainFa().global.order.ord_id);
        intent.putExtra("ord_subtotal", getOrderingMainFa().global.order.ord_subtotal);
        intent.putExtra("ord_taxID", OrderTotalDetails_FR.taxID);
        intent.putExtra("ord_type", Global.ord_type);
        intent.putExtra("ord_email", order_email);

        if (myPref.isCustSelected()) {
            intent.putExtra("cust_id", myPref.getCustID());
            intent.putExtra("custidkey", myPref.getCustIDKey());
        }
        startActivityForResult(intent, 0);
    }

    private void showPromptManagerPassword(final int type, final int position,
                                           final int removePos) {
        final Dialog globalDlog = new Dialog(getActivity(),
                R.style.Theme_TransparentTest);
        globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        globalDlog.setCancelable(true);
        globalDlog.setContentView(R.layout.dlog_field_single_two_btn);
        final EditText viewField = globalDlog
                .findViewById(R.id.dlogFieldSingle);
        viewField.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        TextView viewTitle = globalDlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = globalDlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        if (!validPassword)
            viewMsg.setText(R.string.invalid_password);
        else
            viewMsg.setText(R.string.dlog_title_enter_manager_password);
        Button cancelBtn = globalDlog.findViewById(R.id.btnDlogRight);
        cancelBtn.setText(R.string.button_cancel);
        cancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                globalDlog.dismiss();
            }
        });

        Button btnOk = globalDlog.findViewById(R.id.btnDlogLeft);
        btnOk.setText(R.string.button_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                globalDlog.dismiss();
                String value = viewField.getText().toString().trim();
                if (myPref.loginManager(value)) {
                    validPassword = true;
                    switch (type) {
                        case REMOVE_ITEM:
                            proceedToRemove(removePos);
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

    private void showOnHoldPromptName(final OrdersHandler ordersHandler,
                                      final OrderProductsHandler orderProductsHandler) {
        final Dialog globalDlog = new Dialog(getActivity(),
                R.style.Theme_TransparentTest);
        globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        globalDlog.setCancelable(false);
        globalDlog.setContentView(R.layout.dlog_field_single_layout);
        final EditText viewField = globalDlog
                .findViewById(R.id.dlogFieldSingle);
        viewField.setInputType(InputType.TYPE_CLASS_TEXT);
        TextView viewTitle = globalDlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = globalDlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        viewMsg.setText(R.string.enter_name);
        if (!isToGo && getOrderingMainFa().getSelectedDinningTableNumber() != null) {
            viewField.setText(String.format("%s %s", getString(R.string.restaurant_table), getOrderingMainFa().getSelectedDinningTableNumber()));
        }
        Button btnCancel = globalDlog.findViewById(R.id.btnCancelDlogSingle);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOrderingMainFa().buildOrderStarted = false;
                globalDlog.dismiss();
            }
        });
        Button btnOk = globalDlog.findViewById(R.id.btnDlogSingle);
        btnOk.setText(R.string.button_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                globalDlog.dismiss();
                String value = viewField.getText().toString().trim();
                ord_HoldName = value;
                if (!value.isEmpty()) {
                    setOrderAsHold(value, ordersHandler, orderProductsHandler);
                } else {
                    showOnHoldPromptName(ordersHandler, orderProductsHandler);
                }
            }
        });
        globalDlog.show();
    }

    private void setOrderAsHold(String holdName, OrdersHandler ordersHandler, OrderProductsHandler orderProductsHandler) {
        getOrderingMainFa().global.order.ord_HoldName = holdName;
        getOrderingMainFa().global.order.processed = "10";
        getOrderingMainFa().global.order.isOnHold = "1";
        getOrderingMainFa().global.order.numberOfSeats = mainLVAdapter.getSeatsAmount();
        ordersHandler.insert(getOrderingMainFa().global.order);
        getOrderingMainFa().global.encodedImage = "";
        orderProductsHandler.insert(getOrderingMainFa().global.order.getOrderProducts());
        new PrintAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);
        if (((OrderingMain_FA) getActivity()).orderingAction != OrderingMain_FA.OrderingAction.CHECKOUT
                || ((OrderingMain_FA) getActivity()).orderingAction == OrderingMain_FA.OrderingAction.BACK_PRESSED) {
            getOrderingMainFa().global.order.setOrderProducts(new ArrayList<OrderProduct>());
            getOrderingMainFa().global.resetOrderDetailsValues();
        }
        new SyncOnHolds().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        DBManager dbManager = new DBManager(getActivity());
//        SynchMethods sm = new SynchMethods(dbManager);
//        sm.synchSendOnHold(false, false, getActivity(), null);
//
//        if (!isToGo && ((OrderingMain_FA) getActivity()).orderingAction != OrderingMain_FA.OrderingAction.HOLD
//                && (((OrderingMain_FA) getActivity()).orderingAction == OrderingMain_FA.OrderingAction.CHECKOUT ||
//                ((OrderingMain_FA) getActivity()).orderingAction != OrderingMain_FA.OrderingAction.BACK_PRESSED)) {
//            showSplitedOrderPreview();
//        } else {
//            getActivity().finish();
//        }
    }

    public void voidCancelOnHold(int type) {
        switch (type) {
            case 1:// void hold
                processOrder(getOrderingMainFa().global.order, "", OrderingMain_FA.OrderingAction.NONE, Global.isFromOnHold, true);
                DinningTableOrderDAO.deleteByNumber(((OrderingMain_FA) getActivity()).getSelectedDinningTableNumber());
                break;
            case 2:// cancel hold
                processOrder(getOrderingMainFa().global.order, "", OrderingMain_FA.OrderingAction.HOLD, Global.isFromOnHold, false);
                break;
        }
    }

    private void showPrintDlg(boolean isRetry) {
        final Dialog dlog = new Dialog(getActivity(), R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);
        TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = dlog.findViewById(R.id.dlogMessage);
        if (isRetry) {
            viewTitle.setText(R.string.dlog_title_error);
            viewMsg.setText(R.string.dlog_msg_failed_print);
        } else {
            viewTitle.setText(R.string.dlog_title_confirm);
            viewMsg.setText(R.string.dlog_msg_want_to_print);
        }
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);
        Button btnYes = dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = dlog.findViewById(R.id.btnDlogRight);
        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);
        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                new PrintAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false);

            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                reloadDefaultTransaction();
            }
        });
        dlog.show();
    }

    private OrderingMain_FA getOrderingMainFa() {
        return (OrderingMain_FA) getActivity();
    }

    private void proceedToRefund() {
        Intent intent = new Intent(getActivity(), SelectPayMethod_FA.class);
        intent.putExtra("typeOfProcedure", typeOfProcedure);
        intent.putExtra("salesrefund", true);
        intent.putExtra("amount",
                String.valueOf(Global.getRoundBigDecimal(OrderTotalDetails_FR.gran_total
                        .compareTo(new BigDecimal(0)) == -1 ? OrderTotalDetails_FR.gran_total
                        .negate() : OrderTotalDetails_FR.gran_total)));
        intent.putExtra("paid", "0.00");
        intent.putExtra("job_id", getOrderingMainFa().global.order.ord_id);
        intent.putExtra("ord_type", Global.ord_type);
        intent.putExtra("ord_email", order_email);

        if (myPref.isCustSelected()) {
            intent.putExtra("cust_id", myPref.getCustID());
            intent.putExtra("custidkey", myPref.getCustIDKey());
        }
        intent.putExtra(
                "amount",
                String.valueOf(Global.getRoundBigDecimal(OrderTotalDetails_FR.gran_total
                        .compareTo(new BigDecimal(0)) == -1 ? OrderTotalDetails_FR.gran_total
                        .negate() : OrderTotalDetails_FR.gran_total, 2)));
        intent.putExtra("paid", "0.00");
        intent.putExtra("ord_subtotal", getOrderingMainFa().global.order.ord_subtotal);
        intent.putExtra("ord_taxID", OrderTotalDetails_FR.taxID);
        intent.putExtra("ord_email", order_email);
        startActivityForResult(intent, 0);
    }

    private void showRefundDlg() {
        final Dialog dlog = new Dialog(getActivity(), R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        viewMsg.setText(R.string.dlog_msg_want_to_make_refund);
        Button btnYes = dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = dlog.findViewById(R.id.btnDlogRight);
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);
        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);

        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                proceedToRefund();

            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();

                if (myPref.getPreferences(MyPreferences.pref_enable_printing)) {
                    if (myPref
                            .getPreferences(MyPreferences.pref_automatic_printing)) {
                        new PrintAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false);
                    } else
                        showPrintDlg(false);
                } else {
                    reloadDefaultTransaction();
                }
            }
        });
        dlog.show();
    }

    private void openInvoicePaymentSelection() {
        Intent intent = new Intent(getActivity(), SelectPayMethod_FA.class);
        intent.putExtra("typeOfProcedure", typeOfProcedure);
        intent.putExtra("salesinvoice", true);
        intent.putExtra("ord_subtotal", getOrderingMainFa().global.order.ord_subtotal);
        intent.putExtra("ord_taxID", OrderTotalDetails_FR.taxID);
        intent.putExtra("amount", getOrderingMainFa().global.order.ord_total);
        intent.putExtra("paid", "0.00");
        intent.putExtra("job_id", getOrderingMainFa().global.order.ord_id);
        intent.putExtra("ord_type", Global.ord_type);
        intent.putExtra("ord_email", order_email);
        if (myPref.isCustSelected()) {
            intent.putExtra("cust_id", myPref.getCustID());
            intent.putExtra("custidkey", myPref.getCustIDKey());
        }
        startActivityForResult(intent, 0);
    }

    private void showPaymentDlg() {
        final Dialog dlog = new Dialog(getActivity(), R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);
        TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        viewMsg.setText(R.string.take_payment_now);
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);
        Button btnYes = dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = dlog.findViewById(R.id.btnDlogRight);
        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);

        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                openInvoicePaymentSelection();
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                if (myPref.getPreferences(MyPreferences.pref_enable_printing)) {
                    if (myPref
                            .getPreferences(MyPreferences.pref_automatic_printing)) {
                        new PrintAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false);
                    } else
                        showPrintDlg(false);
                } else
                    reloadDefaultTransaction();
            }
        });
        dlog.show();
    }

    private void proceedToRemove(int removePos) {
        if (removePos < 0 || getOrderingMainFa().global.order.getOrderProducts().isEmpty()) {
            return;
        }
        OrderProduct product = getOrderingMainFa().global.order.getOrderProducts().get(removePos);
        if (myPref
                .getPreferences(MyPreferences.pref_show_removed_void_items_in_printout)) {
            product.setItem_void("1");
            String val = product.getOrdprod_name();

            product.setOrdprod_name(val + " [VOIDED]");
            product.setOverwrite_price(null);
        } else {
            OrderProductsHandler ordProdDB = new OrderProductsHandler(getActivity());
            ordProdDB.deleteOrderProduct(product.getOrdprod_id());
            getOrderingMainFa().global.order.getOrderProducts().remove(product);
        }
        receiptListView.invalidateViews();
        reCalculate();
//        Catalog_FR.instance.refreshListView();
//        refreshView();
    }

    public void reCalculate() {
        pagerAdapter.getItem(0);
        if (callBackRecalculate != null) {
//            Message msg=((OrderingMain_FA) getActivity()).receiptListHandler.obtainMessage();
//            msg.what=1;
//            ((OrderingMain_FA) getActivity()).receiptListHandler.sendMessage(msg);
            callBackRecalculate.recalculateTotal();
            pagerAdapter.notifyDataSetChanged();
        }
    }


    private void loadSaveTemplate() {
        if (myPref.isCustSelected()) {
            final TemplateHandler handleTemplate = new TemplateHandler(getActivity());
            final Dialog dlog = new Dialog(getActivity(),
                    R.style.Theme_TransparentTest);
            dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dlog.setCanceledOnTouchOutside(true);
            dlog.setContentView(R.layout.dlog_btn_left_right_layout);

            TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
            TextView viewMsg = dlog.findViewById(R.id.dlogMessage);
            viewTitle.setText(R.string.dlog_title_choose_action);
            viewMsg.setVisibility(View.GONE);
            Button btnSave = dlog.findViewById(R.id.btnDlogLeft);
            Button btnLoad = dlog.findViewById(R.id.btnDlogRight);
            btnSave.setText(R.string.button_save);
            btnLoad.setText(R.string.button_load);
            Button btnCancel = dlog.findViewById(R.id.btnDlogCancel);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dlog.dismiss();
                }
            });
            btnSave.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    handleTemplate.insert(myPref.getCustID(), getOrderingMainFa().global.order.getOrderProducts());
                    dlog.dismiss();
                    Global.showPrompt(
                            getActivity(),
                            R.string.dlog_title_confirm,
                            getActivity().getString(R.string.dlog_msg_template_saved));
                }
            });
            btnLoad.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    loadCustomerTemplate();
                    dlog.dismiss();
                }
            });
            dlog.show();
        } else
            Toast.makeText(getActivity(),
                    getString(R.string.warning_no_customer_selected),
                    Toast.LENGTH_LONG).show();
    }

    private void loadCustomerTemplate() {
        TemplateHandler handleTemplate = new TemplateHandler(getActivity());
        List<Template> templates = handleTemplate
                .getTemplate(myPref.getCustID());
        getOrderingMainFa().global.order.setOrderProducts(new ArrayList<OrderProduct>());
        OrderProduct ordProd = new OrderProduct();
        Orders anOrder = new Orders();
        for (Template template : templates) {
            ordProd.setOrdprod_id(template.getOrdProductId());
            ordProd.setProd_id(template.getProductId());
            ordProd.setProd_price(template.getProductPrice());
            ordProd.setOrdprod_name(template.getProductName());
            ordProd.setOverwrite_price(TextUtils.isEmpty(template.getOveritePrice()) ? null : new BigDecimal(template.getOveritePrice()));
            ordProd.setOrdprod_qty(template.getOrdProductQty());
            ordProd.setItemTotal(String.valueOf(template.getItemTotal()));
            ordProd.setOrd_id(template.getOrderId());
            ordProd.setOrdprod_desc(template.getOrdProductDescription());
            ordProd.setProd_istaxable(template.getProductIsTaxable());
            anOrder.setValue(template.getProductPrice());
            anOrder.setQty(template.getOrdProductQty());
            ordProd.setTax_position("0");
            ordProd.setDiscount_position("0");
            ordProd.setPricelevel_position("0");
            ordProd.setUom_position("0");
            ordProd.setAttributesCompleted(true);
            getOrderingMainFa().global.order.getOrderProducts().add(ordProd);
            ordProd = new OrderProduct();
            anOrder = new Orders();
        }
        setupListView();
//        reCalculate();
    }

    @Override
    public void onDrawerClosed() {
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
//        receiptListView.post(new Runnable() {
//            @Override
//            public void run() {
        reCalculate();
        if (((OrderingMain_FA) getActivity()).isToGo && !mainLVAdapter.isEmpty()) {
            mainLVAdapter.selectedPosition = mainLVAdapter.getCount();
        }
        if (mainLVAdapter != null) {
            mainLVAdapter.notifyDataSetChanged();
            receiptListView.setSelection(mainLVAdapter.selectedPosition);
//            receiptListView.smoothScrollToPosition(mainLVAdapter.selectedPosition);
        }

//            }
//        });
    }


    private void reloadDefaultTransaction() {
        String type = myPref
                .getPreferencesValue(MyPreferences.pref_default_transaction);
        int transType;
        getOrderingMainFa().global.order = new Order(getActivity());
        if (type == null || type.isEmpty())
            type = "-1";
        transType = Integer.parseInt(type);
        if (transType != -1) {
            Intent intent;
            switch (transType) {
                case 0:// sales receipt
                    intent = new Intent(getActivity(), OrderingMain_FA.class);
                    intent.putExtra("option_number", Global.TransactionType.SALE_RECEIPT);
//                    orderTotalDetailsFr.resetView();
                    getActivity().finish();
                    getActivity().startActivityForResult(intent, 0);
                    break;
                case 2:// return
                    intent = new Intent(getActivity(), OrderingMain_FA.class);
                    intent.putExtra("option_number", Global.TransactionType.RETURN);
//                    OrderTotalDetails_FR.resetView();
                    getActivity().finish();
                    getActivity().startActivityForResult(intent, 0);
                    break;

                default:
                    getActivity().finish();
                    break;
            }
        } else {
            if (!Global.isFromOnHold) {
                getActivity().finish();
            }
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

    public BigDecimal applyRewardDiscount(BigDecimal rewardAmount, List<OrderProduct> orderProducts) {
        BigDecimal discountedAmount = new BigDecimal(0);
        for (OrderProduct product : orderProducts) {
            BigDecimal price = Global.getBigDecimalNum(product.getProd_price());
            BigDecimal newPrice;
            if (rewardAmount.compareTo(price) < 0) {
                newPrice = price.subtract(rewardAmount);
                discountedAmount = discountedAmount.add(rewardAmount);
                rewardAmount = new BigDecimal(0);
            } else {
                newPrice = new BigDecimal(0);
                rewardAmount = rewardAmount.subtract(price);
                discountedAmount = discountedAmount.add(price);
            }
            product.setOverwritePrice(newPrice, getActivity());
            if (rewardAmount.doubleValue() <= 0) {
                break;
            }
        }
        refreshView();
        return discountedAmount;
    }

    public void setCustName() {
        if (myPref.isCustSelected()) {
            CustomersHandler handler = new CustomersHandler(getActivity());
            Customer customer = handler.getCustomer(myPref.getCustID());
            if (customer != null) {
                if (!TextUtils.isEmpty(customer.getCust_firstName())) {
                    custName.setText(String.format("%s %s", StringUtil.nullStringToEmpty(customer.getCust_firstName())
                            , StringUtil.nullStringToEmpty(customer.getCust_lastName())));
                } else if (!TextUtils.isEmpty(customer.getCompanyName())) {
                    custName.setText(customer.getCompanyName());
                } else {
                    custName.setText(customer.getCust_name());
                }
            }
        }
    }

    public interface AddProductBtnCallback {
        void addProductServices();
    }

    public interface RecalculateCallback {
        void recalculateTotal();
    }

    public interface UpdateHeaderTitleCallback {
        void updateHeaderTitle(String val);
    }

    private class ReceiptPagerAdapter extends FragmentPagerAdapter {
        public ReceiptPagerAdapter(FragmentManager fragmentManager) {
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
                case 0:
                    if (orderTotalDetailsFr == null) {
                        frag = OrderTotalDetails_FR.init(position);
                    } else
                        frag = orderTotalDetailsFr;
                    callBackRecalculate = (RecalculateCallback) frag;
                    orderTotalDetailsFr = (OrderTotalDetails_FR) frag;
                    return frag;
                case 1:
                    OrderLoyalty_FR loyaltyFr = OrderLoyalty_FR.init(position);
                    OrderingMain_FA mainFa = (OrderingMain_FA) getActivity();
                    mainFa.setLoyaltyFragment(loyaltyFr);
                    return loyaltyFr;
                default:
                    orderRewardsFr = OrderRewards_FR.init(position);
                    return orderRewardsFr;

            }
        }
    }

    public class OnHoldAsync extends AsyncTask<Object, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(getActivity());
            myProgressDialog.setMessage(getString(R.string.sending));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            if (myProgressDialog.isShowing())
                myProgressDialog.dismiss();

        }

        @Override
        protected Boolean doInBackground(Object... arg0) {

            if (NetworkUtils.isConnectedToInternet(getActivity())) {
//                Post httpClient = new Post(getActivity());
                switch ((Integer) arg0[0]) {
                    case UPDATE_HOLD_STATUS:
                        try {
                            OnHoldsManager.updateStatusOnHold(Global.lastOrdID, getActivity());
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
//                        httpClient.postData(Global.S_UPDATE_STATUS_ON_HOLD,
//                                Global.lastOrdID);
                        break;
                    case CHECK_OUT_HOLD:
                        try {
                            OnHoldsManager.checkoutOnHold(Global.lastOrdID, getActivity());
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
//                        httpClient.postData(Global.S_CHECKOUT_ON_HOLD,
//                                Global.lastOrdID);
                        break;
                }
            }
            return (Boolean) arg0[1];
        }

        @Override
        protected void onPostExecute(Boolean voidOnHold) {
            Global.dismissDialog(getActivity(), myProgressDialog);
//            if (voidOnHold)
            if (caseSelected != Global.TransactionType.INVOICE) {
                getActivity().finish();
            }
        }

    }

    private class PrintAsync extends AsyncTask<Boolean, Integer, String> {
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

            if (getOrderingMainFa()._msrUsbSams != null
                    && getOrderingMainFa()._msrUsbSams.isDeviceOpen()) {
                getOrderingMainFa()._msrUsbSams.CloseTheDevice();
            }

        }

        @Override
        protected void onProgressUpdate(Integer... params) {
            if (!myProgressDialog.isShowing())
                myProgressDialog.show();
        }

        @Override
        protected String doInBackground(Boolean... params) {
            isPrintStationPrinter = params[0];
            if (!isPrintStationPrinter) {
                publishProgress();
                Global.OrderType type = Global.ord_type;
                if (Global.mainPrinterManager != null
                        && Global.mainPrinterManager.getCurrentDevice() != null) {
                    printSuccessful = Global.mainPrinterManager.getCurrentDevice()
                            .printTransaction(getOrderingMainFa().global.order.ord_id, type, false,
                                    false);
                }
            } else {
                OrderProductsHandler orderProductsHandler = new OrderProductsHandler(
                        getActivity());
                HashMap<String, List<Orders>> temp = orderProductsHandler
                        .getStationPrinterProducts(getOrderingMainFa().global.order.ord_id);

                String[] sArr = temp.keySet().toArray(
                        new String[temp.keySet().size()]);
                int printMap;
                boolean splitByCat = myPref.getPreferences(MyPreferences.pref_split_stationprint_by_categories);
                EMSBluetoothStarPrinter currentDevice = null;
                boolean printHeader = true;
                StringBuffer receipt = new StringBuffer();
                String currentPrinterName = null;
                for (String aSArr : sArr) {
                    if (Global.multiPrinterMap.containsKey(aSArr)) {
                        printMap = Global.multiPrinterMap.get(aSArr);
                        if (Global.multiPrinterManager.get(printMap) != null
                                && Global.multiPrinterManager.get(printMap).getCurrentDevice() != null) {
                            if (currentPrinterName == null || !currentPrinterName.equalsIgnoreCase(((EMSBluetoothStarPrinter)
                                    Global.multiPrinterManager.get(printMap).getCurrentDevice()).getPortName())) {
                                printHeader = true;
                                if (currentDevice != null) {
                                    currentDevice.print(receipt.toString(), true);
                                    receipt.setLength(0);
                                    currentDevice.cutPaper();
                                }
                            }
                            currentDevice = (EMSBluetoothStarPrinter) Global.multiPrinterManager.get(printMap).getCurrentDevice();
                            receipt.append(currentDevice.printStationPrinter(temp.get(aSArr),
                                    getOrderingMainFa().global.order.ord_id, splitByCat, printHeader));
                            printHeader = splitByCat;
                            currentPrinterName = currentDevice.getPortName();
                            if (splitByCat) {
                                currentDevice.print(receipt.toString(), true);
                                receipt.setLength(0);
                                currentDevice.cutPaper();
                            }
                        }
                    }
                }
                if (currentDevice != null && !TextUtils.isEmpty(receipt)) {
                    currentDevice.print(receipt.toString(), true);
                    receipt.setLength(0);
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
        }
    }

    private class SyncOnHolds extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Global.lockOrientation(getActivity());
            dialog = new ProgressDialog(getActivity());
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.sync_sending_orders));
            if (Global.isActivityDestroyed(getActivity())) {
                dialog.show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            DBManager dbManager = new DBManager(getActivity());
            SynchMethods sm = new SynchMethods(dbManager);
            return sm.synchSendOnHold(false, false, getActivity(), null);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Global.dismissDialog(getActivity(), dialog);
            getOrderingMainFa().buildOrderStarted = false;
            if (getActivity() != null) {
                if (!isToGo && ((OrderingMain_FA) getActivity()).orderingAction != OrderingMain_FA.OrderingAction.HOLD
                        && (((OrderingMain_FA) getActivity()).orderingAction == OrderingMain_FA.OrderingAction.CHECKOUT ||
                        ((OrderingMain_FA) getActivity()).orderingAction != OrderingMain_FA.OrderingAction.BACK_PRESSED)) {
                    showSplitedOrderPreview();
                } else if (getOrderingMainFa().orderingAction != OrderingMain_FA.OrderingAction.CHECKOUT) {
                    getActivity().finish();
                }
            }
//            Global.releaseOrientation(getActivity());
        }
    }


}
