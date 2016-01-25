package com.android.emobilepos.models;

import java.math.BigDecimal;


public class OrderProduct {
	private String empt = "";

	public String addon = "0";
	public String isAdded = "0";
	public String isPrinted = "0";
	public String price_vat_exclusive = "0";
	public String item_void = empt;
	public String ordprod_id = empt;
	public String ord_id = empt;
	public String prod_id = empt;
	public String prod_sku = empt;
	public String prod_upc = empt;
	public String ordprod_qty = empt;
	public String overwrite_price = empt;
	public String reason_id = empt;
	public String ordprod_name = empt;
	public String ordprod_desc = empt;
	public String ordprod_comment = empt;
	public String pricelevel_id = empt;
	public String prod_seq = empt;
	public String uom_name = empt;
	public String uom_conversion = empt;
	public String uom_id = empt;
	public String prod_taxId = empt;
	public String prod_taxValue = empt;
	public String discount_id = empt;
	public String discount_value = empt;
	public String prod_taxcode = empt;
	public String prod_istaxable = empt;
	public String global_taxamount = empt;
	public String cat_id = empt;
	public String cat_name = empt;
	public String prod_price_points = "0";
	public String prod_value_points = "0";
	public String payWithPoints = "false";

	public String itemTotalVatExclusive = "0";
	public String itemTotal = "0";
	public String itemSubtotal = "0";
	public String disAmount = "0", disTotal = "0";
	public String taxAmount = "0", taxTotal = "0";
	public String onHand = "0";
	public String imgURL = empt;
	
	public String tax_position = empt;
	public String discount_position = empt;
	public String pricelevel_position = empt;
	public String uom_position = empt;
	public String prod_price = empt;
	public String prod_type = empt;
	public String tax_type = empt;
	public String discount_is_taxable = "0";
	public String discount_is_fixed = "0";
	public String prod_taxtype;
	
	public String priceLevelName = empt;
	
	
	public String hasAddons = "0"; //0 no addons, 1 it has addons
	public String addon_section_name = empt;
	public String addon_position = empt;
	
	public String prod_price_updated = "0";
	
	public boolean isReturned = false;
	public String assignedSeat;
	public BigDecimal global_discount_total = new BigDecimal("0");
	

}
