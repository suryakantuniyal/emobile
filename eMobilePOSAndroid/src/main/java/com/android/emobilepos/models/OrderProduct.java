package com.android.emobilepos.models;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import io.realm.RealmObject;


public class OrderProduct implements Cloneable, Comparable<OrderProduct> {
    public String addon = "0";
    public String isAdded = "0";
    public String isPrinted = "0";
    public String price_vat_exclusive = "0";
    public String item_void = "";
    public String ordprod_id = "";
    public String ord_id = "";
    public String prod_id = "";
    public String prod_sku = "";
    public String prod_upc = "";
    public String ordprod_qty = "";
    public String overwrite_price = "";
    public String reason_id = "";
    public String ordprod_name = "";
    public String ordprod_desc = "";
    public String ordprod_comment = "";
    public String pricelevel_id = "";
    public String prod_seq = "";
    public String uom_name = "";
    public String uom_conversion = "";
    public String uom_id = "";
    public String prod_taxId = "";
    public String prod_taxValue = "";
    public String discount_id = "";
    public String discount_value = "";
    public String prod_taxcode = "";
    public String prod_istaxable = "";
    public String cat_id = "";
    public String cat_name = "";
    public String prod_price_points = "0";
    public String prod_value_points = "0";
    public String payWithPoints = "false";
    public String pricesXGroupid;

    public String itemTotalVatExclusive = "0";
    public String itemTotal = "0";
    public String itemSubtotal = "0";
    public String disAmount = "0", disTotal = "0";
    public String taxAmount = "0", taxTotal = "0";
    public String onHand = "0";
    public String imgURL = "";

    public String tax_position = "";
    public String discount_position = "";
    public String pricelevel_position = "";
    public String uom_position = "";
    public String prod_price = "";
    public String prod_type = "";
    public String tax_type = "";
    public String discount_is_taxable = "0";
    public String discount_is_fixed = "0";
    public String prod_taxtype;

    public String priceLevelName = "";


    public String hasAddons = "0"; //0 no addons, 1 it has addons
    public String addon_section_name = "";
    public String addon_position = "";

    public String prod_price_updated = "0";

    public boolean isReturned = false;
    public String assignedSeat;
    public int seatGroupId;
    public int mixMatchQtyApplied;
    public BigDecimal mixMatchOriginalPrice;
    public List<MixAndMatchDiscount> mixAndMatchDiscounts;
    public String consignment_qty;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public boolean isVoid() {
        return item_void.equals("1");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        return this.ordprod_id.equalsIgnoreCase(((OrderProduct) o).ordprod_id);
    }

    @Override
    public int compareTo(OrderProduct another) {
        return this.ordprod_id.compareTo(another.ordprod_id);
    }

    public void setPricesXGroupid(String pricesXGroupid) {
        this.pricesXGroupid = pricesXGroupid;
    }

    public String getPricesXGroupid() {
        return pricesXGroupid;
    }
}
