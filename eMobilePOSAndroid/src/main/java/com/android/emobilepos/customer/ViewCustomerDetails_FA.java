package com.android.emobilepos.customer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import com.android.database.AddressHandler;
import com.android.database.CustomersHandler;
import com.android.emobilepos.R;
import com.android.support.Customer;
import com.android.support.Global;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewCustomerDetails_FA extends BaseFragmentActivityActionBar {

    private final int CASE_BILLING = 0, CASE_SHIPPING = 1;
    private List<String> allInfoLeft;
    private List<String> allInfoRight = new ArrayList<String>();
    private List<String> allFinancialLeft = Arrays.asList("Balance", "Limit", "Taxable", "Tax ID");
    private List<String> allFinancialRight = new ArrayList<>();
    private Global global;
    private boolean hasBeenCreated = false;
    private Activity activity;
    private String cust_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_moreinfo_layout);

        activity = this;
        global = (Global) getApplication();

        allInfoLeft = Arrays.asList(getString(R.string.cust_detail_name), getString(R.string.cust_detail_contact),
                getString(R.string.cust_detail_phone), getString(R.string.cust_detail_email), getString(R.string.cust_detail_company));
        allFinancialLeft = Arrays.asList(getString(R.string.cust_detail_balance), getString(R.string.cust_detail_limit),
                getString(R.string.cust_detail_taxable), getString(R.string.cust_detail_tax_id));
        Bundle extras = getIntent().getExtras();
        CustomersHandler custHandler = new CustomersHandler(this);
        cust_id = extras.getString("cust_id");
        Customer customer = custHandler.getCustomer(cust_id);
        List<String> data = custHandler.getCustDetails(cust_id);
        int size = data.size();
        for (int i = 0; i < size; i++) {
            if (i < 5) {
                allInfoRight.add(data.get(i));
            } else if (i >= 5 && i < 9) {
                allFinancialRight.add(data.get(i));
            }
        }
//        ListView myListview = (ListView) findViewById(R.id.custMoreInfoLV);
//        ListViewAdapter myAdapter = new ListViewAdapter(this);
//        myListview.setAdapter(myAdapter);
//        myListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
//                int offset = allInfoLeft.size() + 3 + allFinancialLeft.size();
//                if (pos == offset)                    //BILLING
//                {
//                    showAddressDialog(CASE_BILLING);
//                } else if (pos == offset + 1)        //SHIPPING
//                {
//                    showAddressDialog(CASE_SHIPPING);
//                }
//            }
//        });
        hasBeenCreated = true;
    }


    @Override
    public void onResume() {

        if (global.isApplicationSentToBackground())
            Global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !Global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(activity);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        if (!isScreenOn)
            Global.loggedIn = false;
        global.startActivityTransitionTimer();
    }


    private void showAddressDialog(int type) {
        AddressHandler addressHandler = new AddressHandler(activity);
        List<String[]> addressDownloadedItems = new ArrayList<String[]>();

        AlertDialog.Builder adb = new AlertDialog.Builder(activity);
        String dialogTitle = new String();


        switch (type) {
            case CASE_BILLING:
                addressDownloadedItems = addressHandler.getSpecificAddress(cust_id, CASE_BILLING);
                dialogTitle = "Billing Address";
                break;
            case CASE_SHIPPING:
                addressDownloadedItems = addressHandler.getSpecificAddress(cust_id, CASE_SHIPPING);
                dialogTitle = "Shipping Address";
                break;
        }

        int size = addressDownloadedItems.size();
        String[] addressItems = new String[size];
        //addressItems[0] = "None";

        StringBuilder sb = new StringBuilder();
        String temp = "";

        for (int i = 0; i < size; i++) {
            temp = addressDownloadedItems.get(i)[0];
            if (!temp.isEmpty())                            //address 1
                sb.append(temp).append(" ");
            temp = addressDownloadedItems.get(i)[1];
            if (!temp.isEmpty())                            //address 2
                sb.append(temp).append(" ");
            temp = addressDownloadedItems.get(i)[2];
            if (!temp.isEmpty())                            //address 3
                sb.append(temp).append("\t\t");
            temp = addressDownloadedItems.get(i)[3];
            if (!temp.isEmpty())                            //address country
                sb.append(temp).append(" ");
            temp = addressDownloadedItems.get(i)[4];
            if (!temp.isEmpty())                            //address city
                sb.append(temp).append(" ");
            temp = addressDownloadedItems.get(i)[5];        //address state
            if (!temp.isEmpty())
                sb.append(temp).append(" ");
            temp = addressDownloadedItems.get(i)[6];    //address zipcode
            if (!temp.isEmpty())
                sb.append(temp).append(" ");
            addressItems[i] = sb.toString();
            sb.setLength(0);
        }

        adb.setItems(addressItems, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dialog.dismiss();

            }
        });

        adb.setNegativeButton("OK", null);
        adb.setTitle(dialogTitle);
        adb.show();
    }


    public class ListViewAdapter extends BaseAdapter implements Filterable {
        private LayoutInflater myInflater;


        public ListViewAdapter(Context context) {

            myInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            //+3 for the dividers +2 for the actual address
            return allInfoLeft.size() + allFinancialLeft.size() + 3 + 2;
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            int type = getItemViewType(position);

            if (convertView == null) {
                holder = new ViewHolder();

                switch (type) {
                    case 0: {
                        convertView = myInflater.inflate(R.layout.orddetails_lvdivider_adapter, null);
                        holder.left = (TextView) convertView.findViewById(R.id.orderDivLeft);
                        holder.right = (TextView) convertView.findViewById(R.id.orderDivRight);

                        if (position == 0) {
                            holder.left.setText(getString(R.string.cust_detail_info));
                        } else if (position == (allInfoLeft.size() + 1)) {
                            holder.left.setText(getString(R.string.cust_detail_financial_info));
                        } else {
                            holder.left.setText(getString(R.string.cust_detail_address));
                        }
                        break;
                    }
                    case 1: {
                        convertView = myInflater.inflate(R.layout.orddetails_lvinfo_adapter, null);
                        holder.left = (TextView) convertView.findViewById(R.id.ordInfoLeft);
                        holder.right = (TextView) convertView.findViewById(R.id.ordInfoRight);

                        int length2 = allInfoLeft.size() + 2 + allFinancialLeft.size();
                        if (position > 0 && position <= allInfoLeft.size()) {
                            holder.left.setText(allInfoLeft.get(position - 1));
                            holder.right.setText(allInfoRight.get(position - 1));
                        } else if (position > allInfoLeft.size() + 1 && position < length2) {
                            int ind = position - allInfoLeft.size() - 2;
                            holder.left.setText(allFinancialLeft.get(ind));
                            holder.right.setText(allFinancialRight.get(ind));
                        } else if (position == allFinancialLeft.size() + allInfoLeft.size() + 3)                //Billing Address
                        {
                            holder.left.setText(getString(R.string.cust_detail_bill));
                            holder.right.setText("");
                        } else                                                                            //Shipping Address
                        {
                            holder.left.setText(getString(R.string.cust_detail_ship));
                            holder.right.setSingleLine(true);
                            holder.right.setText("");
                        }
                        break;
                    }
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (type == 0) {
                if (position == 0) {
                    holder.left.setText(getString(R.string.cust_detail_info));
                } else if (position == (allInfoLeft.size() + 1)) {
                    holder.left.setText(getString(R.string.cust_detail_financial_info));
                } else {
                    holder.left.setText(getString(R.string.cust_detail_address));
                }
            } else {
                int length2 = allInfoLeft.size() + 2 + allFinancialLeft.size();
                if (position > 0 && position <= allInfoLeft.size()) {
                    holder.left.setText(allInfoLeft.get(position - 1));
                    holder.right.setText(allInfoRight.get(position - 1));
                } else if (position > allInfoLeft.size() + 1 && position < length2) {
                    int ind = position - allInfoLeft.size() - 2;
                    holder.left.setText(allFinancialLeft.get(ind));
                    holder.right.setText(allFinancialRight.get(ind));
                } else if (position == length2 + 1)                        //Billing Address
                {
                    holder.left.setText(getString(R.string.cust_detail_bill));
                    holder.right.setText("");
                } else                                                                                        //Shipping Address
                {
                    holder.left.setText(getString(R.string.cust_detail_ship));
                    holder.right.setSingleLine(true);
                    holder.right.setText("");
                }
            }
            return convertView;
        }

        @Override
        public Filter getFilter() {
            return null;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0 || (position == (allInfoLeft.size() + 1)) || (position == (allFinancialLeft.size() + allInfoLeft.size() + 2))) {
                return 0;
            }

            return 1;

        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        public class ViewHolder {
            TextView left;
            TextView right;
        }

    }
}
