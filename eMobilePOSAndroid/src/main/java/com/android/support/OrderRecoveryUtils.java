package com.android.support;

import android.content.Context;
import android.content.Intent;

import com.android.database.CustomersHandler;
import com.android.database.OrdersHandler;
import com.android.database.SalesTaxCodesHandler;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.ordering.OrderingMain_FA;

import java.util.HashMap;

/**
 * Created by Luis Camayd on 7/30/2019.
 */
public class OrderRecoveryUtils {
    private Context context;
    private MyPreferences myPref;

    public OrderRecoveryUtils(Context context) {
        this.context = context;
        myPref = new MyPreferences(context);
    }

    public Intent getRecoveryIntent(String orderId) {
        Intent intent = null;
        if (orderId != null) {
            OrdersHandler ordersHandler = new OrdersHandler(context);
            Order order = ordersHandler.getOrder(orderId);
            if (order != null) {
                selectCustomer(order.cust_id);
                intent = new Intent(context, OrderingMain_FA.class);
                String assignedTable = order.assignedTable;
                intent.putExtra("selectedDinningTableNumber", assignedTable);
                intent.putExtra("recoveryOrderJson", order.toJson());
                intent.putExtra("openFromRecovery", true);
                intent.putExtra("recoveryOrderId", orderId);
                if (assignedTable != null && !assignedTable.isEmpty()) {
                    intent.putExtra("RestaurantSaleType", Global.RestaurantSaleType.EAT_IN);
                } else {
                    intent.putExtra("RestaurantSaleType", Global.RestaurantSaleType.TO_GO);
                }
                intent.putExtra("option_number", Global.TransactionType.SALE_RECEIPT);
                intent.putExtra("associateId", order.associateID);
                Global.isFromOnHold = true;
            }
        }

        return intent;
    }

    private void selectCustomer(String custID) {
        if (custID != null && !custID.isEmpty()) {
            CustomersHandler customersHandler = new CustomersHandler(context);
            HashMap<String, String> customerInfo = customersHandler.getCustomerInfo(custID);
            SalesTaxCodesHandler taxHandler = new SalesTaxCodesHandler(context);
            SalesTaxCodesHandler.TaxableCode taxable =
                    taxHandler.checkIfCustTaxable(customerInfo.get("cust_taxable"));
            myPref.setCustTaxCode(taxable, customerInfo.get("cust_salestaxcode"));
            myPref.setCustID(customerInfo.get("cust_id"));
            myPref.setCustName(customerInfo.get("cust_name"));
            myPref.setCustIDKey(customerInfo.get("custidkey"));
            myPref.setCustSelected(true);
            myPref.setCustPriceLevel(customerInfo.get("pricelevel_id"));
            myPref.setCustEmail(customerInfo.get("cust_email"));
        }
    }
}
