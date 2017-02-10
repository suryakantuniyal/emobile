package com.android.emobilepos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.dao.OrderAttributesDAO;
import com.android.database.AddressHandler;
import com.android.database.ShipMethodHandler;
import com.android.database.TermsHandler;
import com.android.emobilepos.models.realms.OrderAttributes;
import com.android.support.Global;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.google.gson.Gson;

import org.w3c.dom.Attr;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import util.json.JsonUtils;

public class OrderDetailsActivity extends BaseFragmentActivityActionBar {
    private final String defaultVal = "None";
    private List<String> leftMenuList;
    private Activity activity;
    private int shipmentSelected = 0, termsSelected = 0, addressSelected = 0;

    private List<String[]> shippingMethodsDownloadedItems = new ArrayList<>();
    private String[] shipMethodItems = new String[]{};

    private List<String[]> termsDownloadedItems = new ArrayList<>();
    private String[] termsItems = new String[]{};

    private List<String[]> addressDownloadedItems = new ArrayList<>();
    private String[] addressItems = new String[]{};
    private ListView myListView;
    private int currYear, currMonth, currDay;
    static final int DATE_DIALOG_ID = 0;
    private String deliveryDate = defaultVal;
    List<OrderAttributes> orderAttributesValues;
    private String inputComment = "";
    private Global global;
    private boolean hasBeenCreated = false;
    private int selectedRowIndex;

    public enum AttributeType {
        SHIPPINT_METHODS(0), TERMS(1), DELIVERY(2), ADDRESS(3), COMMENTS(4), PO(5), OTHER(6);

        private int code;

        AttributeType(int code) {
            this.code = code;
        }

        public static AttributeType valueOf(int code) {
            switch (code) {
                case 0:
                    return SHIPPINT_METHODS;
                case 1:
                    return TERMS;
                case 2:
                    return DELIVERY;
                case 3:
                    return ADDRESS;
                case 4:
                    return COMMENTS;
                case 5:
                    return PO;
                default:
                    return OTHER;
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_details_layout);
        activity = this;
        global = (Global) this.getApplication();
        leftMenuList = new ArrayList<>(Arrays.asList(getString(R.string.details_shipping), getString(R.string.details_terms),
                getString(R.string.details_delivery), getString(R.string.details_address), getString(R.string.details_comments),
                getString(R.string.details_po)));
        List<String> orderAttributeNames = OrderAttributesDAO.getOrderAttributeNames();
        leftMenuList.addAll(orderAttributeNames);
        orderAttributesValues = new ArrayList<>();
        initAttributeValues();
        if (global.getSelectedAddressMethod() != -1)
            addressSelected = global.getSelectedAddressMethod();
        if (global.getSelectedShippingMethod() != -1)
            shipmentSelected = global.getSelectedShippingMethod();
        if (global.getSelectedTermsMethod() != -1)
            termsSelected = global.getSelectedTermsMethod();
        if (!global.getSelectedDeliveryDate().isEmpty())
            deliveryDate = global.getSelectedDeliveryDate();
        if (!global.getSelectedComments().isEmpty())
            inputComment = global.getSelectedComments();
//        if (!global.getSelectedPO().isEmpty())
//            inputPO = global.getSelectedPO();
        initAllMenuValues();
        myListView = (ListView) findViewById(R.id.orderDetailsListView);
        CustomAdapter adapter = new CustomAdapter(this);
        myListView.setAdapter(adapter);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                selectedRowIndex = position;
                executeItemAction(position);
            }
        });
        hasBeenCreated = true;
    }

    private void initAttributeValues() {
        int i = 0;
        for (String attr : leftMenuList) {
            if (i < 6) {
                orderAttributesValues.add(new OrderAttributes(attr));
            }
            i++;
        }
        List<OrderAttributes> orderAttributes = OrderAttributesDAO.getOrderAttributes(false);
        orderAttributesValues.addAll(orderAttributes);
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            currYear = year;
            currMonth = monthOfYear + 1;
            currDay = dayOfMonth;
            deliveryDate = Integer.toString(currMonth) + "/" + Integer.toString(currDay) + "/" + Integer.toString(currYear);
            global.setSelectedDeliveryDate(deliveryDate);
            orderAttributesValues.get(selectedRowIndex).setInputValue(deliveryDate);
            myListView.invalidateViews();
        }
    };

    @Override
    public void onBackPressed() {
        Gson gson = JsonUtils.getInstance();
        Intent data = getIntent();
        data.putExtra("orderAttributesValue", gson.toJson(orderAttributesValues.subList(6, orderAttributesValues.size())));
        setResult(Global.FROM_ORDER_ATTRIBUTES_ACTIVITY, data);
        finish();
    }

    private void executeItemAction(int pos) {
        switch (AttributeType.valueOf(pos)) {
            case SHIPPINT_METHODS:
                showPickerDialogBox(pos);
                break;
            case TERMS:
                showPickerDialogBox(pos);
                break;
            case DELIVERY:
                showDialog(DATE_DIALOG_ID);
                break;
            case ADDRESS:
                showPickerDialogBox(pos);
                break;
            case COMMENTS:
                showEditTextDialogBox(pos);
                break;
            default:
                showEditTextDialogBox(pos);
                break;
        }
    }

    @Override
    public void onResume() {
        if (global.isApplicationSentToBackground(this))
            global.loggedIn = false;
        global.stopActivityTransitionTimer();
        if (hasBeenCreated && !global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(this);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        if (!isScreenOn)
            global.loggedIn = false;
        global.startActivityTransitionTimer();
    }


    private void initAllMenuValues() {
        // initialize Shipment Methods Items from database
        ShipMethodHandler shipMethodHandler = new ShipMethodHandler(activity);
        shippingMethodsDownloadedItems = shipMethodHandler.getShipmentMethods();
        int size = shippingMethodsDownloadedItems.size();
        shipMethodItems = new String[size + 1];
        shipMethodItems[0] = defaultVal;
        for (int i = 0; i < size; i++) {
            shipMethodItems[i + 1] = shippingMethodsDownloadedItems.get(i)[0];
        }

        // initialize Terms items from database
        TermsHandler termsHandler = new TermsHandler(activity);
        termsDownloadedItems = termsHandler.getAllTerms();
        size = termsDownloadedItems.size();
        termsItems = new String[size + 1];
        termsItems[0] = defaultVal;
        for (int i = 0; i < size; i++) {
            termsItems[i + 1] = termsDownloadedItems.get(i)[0];
        }

        // initialize Dates
        final Calendar cal = Calendar.getInstance();
        currYear = cal.get(Calendar.YEAR);
        currMonth = cal.get(Calendar.MONTH);
        currDay = cal.get(Calendar.DAY_OF_MONTH);

        //initialize Address from database
        AddressHandler addressHandler = new AddressHandler(activity);
        addressDownloadedItems = addressHandler.getAddress();
        size = addressDownloadedItems.size();
        addressItems = new String[size + 1];
        addressItems[0] = defaultVal;
        StringBuilder sb = new StringBuilder();
        String temp;
        for (int i = 0; i < size; i++) {
            temp = addressDownloadedItems.get(i)[1];
            if (!temp.isEmpty())                            //address 1
                sb.append(temp).append(" ");
            temp = addressDownloadedItems.get(i)[2];
            if (!temp.isEmpty())                            //address 2
                sb.append(temp).append(" ");
            temp = addressDownloadedItems.get(i)[3];
            if (!temp.isEmpty())                            //address 3
                sb.append(temp).append("\t\t");
            temp = addressDownloadedItems.get(i)[4];
            if (!temp.isEmpty())                            //address country
                sb.append(temp).append(" ");
            temp = addressDownloadedItems.get(i)[5];
            if (!temp.isEmpty())                            //address city
                sb.append(temp).append(",");
            temp = addressDownloadedItems.get(i)[6];        //address state
            if (!temp.isEmpty())
                sb.append(temp).append(" ");

            temp = addressDownloadedItems.get(i)[7];        //address zip code
            if (!temp.isEmpty())
                sb.append(temp);

            addressItems[i + 1] = sb.toString();
            sb.setLength(0);
        }
    }

    private void showPickerDialogBox(final int type) {
        String dialogTitle = "No Items";
        String[] menuItems = new String[]{};
        int selectedItem = 0;
        switch (AttributeType.valueOf(type)) {
            case SHIPPINT_METHODS:
                if (shipMethodItems.length > 0) {
                    menuItems = shipMethodItems;
                    dialogTitle = "Shipment Methods";
                    selectedItem = shipmentSelected;
                }
                break;

            case TERMS:
                if (termsItems.length > 0) {
                    menuItems = termsItems;
                    dialogTitle = "Terms";
                    selectedItem = termsSelected;
                }
                break;

            case ADDRESS:
                if (addressItems.length > 0) {
                    menuItems = addressItems;
                    dialogTitle = "Select Address";
                    selectedItem = addressSelected;
                }
                break;
        }


        AlertDialog.Builder adb = new AlertDialog.Builder(activity);
        adb.setSingleChoiceItems(menuItems, selectedItem, new OnClickListener() {

            @Override
            public void onClick(DialogInterface d, int position) {

                switch (AttributeType.valueOf(type)) {
                    case SHIPPINT_METHODS:
                        shipmentSelected = position;
                        global.setSelectedShippingMethod(shipmentSelected);
                        if (position != 0) {
                            orderAttributesValues.get(type).setInputValue(shippingMethodsDownloadedItems.get(position - 1)[0]);
                            global.setSelectedShippingMethodString(shippingMethodsDownloadedItems.get(position - 1)[1]);
                        }
                        break;
                    case TERMS:
                        termsSelected = position;
                        global.setSelectedTermsMethod(termsSelected);
                        if (position != 0) {
                            orderAttributesValues.get(type).setInputValue(termsDownloadedItems.get(position - 1)[0]);
                            global.setSelectedTermsMethodString(termsDownloadedItems.get(position - 1)[1]);
                        }
                        break;
                    case ADDRESS:
                        addressSelected = position;
                        global.setSelectedAddress(addressSelected);
                        if (position != 0) {
                            orderAttributesValues.get(type).setInputValue(addressDownloadedItems.get(position - 1)[0]);
                            global.setSelectedAddressString(addressDownloadedItems.get(position - 1)[0]);
                        }
                        break;
                }
                myListView.invalidateViews();
                d.dismiss();
            }
        });

        adb.setNegativeButton("Cancel", null);
        adb.setTitle(dialogTitle);
        adb.show();
    }

    private void showEditTextDialogBox(final int type) {
        String dialogTitle = "";
        final EditText editTextField = new EditText(activity);
        int orientation = getResources().getConfiguration().orientation;
        editTextField.setSingleLine(false);
        editTextField.setGravity(Gravity.TOP);
        dialogTitle = leftMenuList.get(type);
        if (orderAttributesValues.get(type).getInputValue() != null
                && !orderAttributesValues.get(type).getInputValue().equals(defaultVal)) {
            editTextField.setText(orderAttributesValues.get(type).getInputValue());
            editTextField.setSelection(orderAttributesValues.get(type).getInputValue().length());
        }
//        switch (AttributeType.valueOf(type)) {
//            case COMMENTS:
//                dialogTitle = "Comments";
//                if (!inputComment.equals(defaultVal)) {
//                    editTextField.setText(inputComment);
//                    editTextField.setSelection(inputComment.length());
//                }
//                break;
//            case PO:
//                dialogTitle = "PO";
//                if (!inputPO.equals(defaultVal)) {
//                    editTextField.setText(inputPO);
//                    editTextField.setSelection(inputPO.length());
//                }
//                break;
//        }
        AlertDialog.Builder adb = new AlertDialog.Builder(activity);
        adb.setView(editTextField);
        adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                orderAttributesValues.get(type).setInputValue( editTextField.getText().toString());
                switch (AttributeType.valueOf(type)) {
                    case COMMENTS:
                        inputComment = editTextField.getText().toString();
                        if (inputComment.trim().length() > 0) {
                            global.setSelectedComments(inputComment.trim());
                        }
                        break;
                    default:
//                        inputPO = editTextField.getText().toString();
//                        if (inputPO.trim().length() > 0) {
//                            global.setSelectedPO(inputPO.trim());
//                        }
                        break;
                }

                myListView.invalidateViews();
                dialog.dismiss();
            }
        });
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        adb.setTitle(dialogTitle);

        adb.show();
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        datePickerListener,
                        currYear, currMonth, currDay);
        }
        return null;
    }

    private class CustomAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public CustomAdapter(Activity activity) {
            inflater = LayoutInflater.from(activity);
        }

        @Override
        public int getViewTypeCount() {
            return leftMenuList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
//			if(position == 0)
//				return 0;
//			else if(position == 1)
//				return 1;
//			else if(position == 2)
//				return 2;
//			else if(position == 3)
//				return 3;
//			else if(position == 4)
//				return 4;
//			return 5;
        }

        @Override
        public int getCount() {
            return leftMenuList.size();
        }

        @Override
        public Object getItem(int pos) {
            return leftMenuList.get(pos);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            int type = getItemViewType(position);

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.order_details_lvadapter, null);
                holder.leftText = (TextView) convertView.findViewById(R.id.orderDetailsLVText);
                holder.rightText = (TextView) convertView.findViewById(R.id.orderDetailsLVRightText);
                holder.leftText.setText(leftMenuList.get(position));
                setHolderValues(type, holder);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.leftText.setText(leftMenuList.get(position));
                setHolderValues(position, holder);
            }
            return convertView;
        }

        private void setHolderValues(int type, ViewHolder holder) {
            if (!TextUtils.isEmpty(orderAttributesValues.get(type).getInputValue()))
                holder.rightText.setText(orderAttributesValues.get(type).getInputValue());
            else
                holder.rightText.setText(defaultVal);

//            switch (AttributeType.valueOf(type)) {
//                case SHIPPINT_METHODS:
//                    if (shipMethodItems.length > 0)
//                        holder.rightText.setText(shipMethodItems[shipmentSelected]);
//                    break;
//                case TERMS:
//                    if (termsItems.length > 0)
//                        holder.rightText.setText(termsItems[termsSelected]);
//                    break;
//                case DELIVERY:
//                    holder.rightText.setText(deliveryDate);
//                    break;
//                case ADDRESS:
//                    holder.rightText.setText(addressItems[addressSelected]);
//                    break;
//                case COMMENTS:
//                    if (!inputComment.isEmpty())
//                        holder.rightText.setText(inputComment);
//                    else
//                        holder.rightText.setText(defaultVal);
//
//                    break;
//                case PO:
////                    if (!inputPO.isEmpty())
////                        holder.rightText.setText(inputPO);
////                    else
////                        holder.rightText.setText(defaultVal);
//                    break;
//                default:
//                    holder.rightText.setText(defaultVal);
//            }
        }

        public class ViewHolder {
            TextView leftText;
            TextView rightText;
        }
    }
}
