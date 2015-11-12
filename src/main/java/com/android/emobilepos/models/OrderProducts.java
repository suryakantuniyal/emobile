package com.android.emobilepos.models;

import java.math.BigDecimal;


public class OrderProducts {
	private String empt = "";

	public String addon = "0";
	public String isAdded = "0";
	public String isPrinted = "0";
	public String price_vat_exclusive = "0";
	public String item_void = empt;
	public String ordprod_id = empt;
	public String ord_id = empt;
	public String prod_id = empt;
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
	public BigDecimal global_discount_total = new BigDecimal("0");
	
	
	
//	public enum Limiters 
//	{
//		addon, isAdded, isPrinted, item_void, ordprod_id, ord_id, prod_id, ordprod_qty, overwrite_price, reason_id, ordprod_name, ordprod_desc, 
//		pricelevel_id, prod_seq, uom_name, uom_conversion,uom_id, prod_taxId, prod_taxValue, discount_id, discount_value, prod_taxcode,
//
//		itemTotal, disAmount, disTotal, taxAmount, taxTotal,onHand,imgURL,prod_istaxable,global_taxamount,
//		tax_position,discount_position,pricelevel_position,uom_position,prod_price,priceLevelName,prod_type,tax_type,
//		discount_is_taxable,itemSubtotal,discount_is_fixed,addon_section_name,addon_position,hasAddons,cat_id,
//		prod_price_points,prod_value_points,payWithPoints,prod_taxtype,ordprod_comment,prod_price_updated,price_vat_exclusive,
//		itemTotalVatExclusive;
//
//		public static Limiters toLimit(String str) {
//			try {
//				return valueOf(str);
//			} catch (Exception ex) {
//				return null;
//			}
//		}
//	}
//
//	
//	
//	public String getSetData(String attribute, boolean get, String value) {
//		Limiters test = Limiters.toLimit(attribute);
//		String returnedVal = "";
//		
//		if(value==null)
//			value = empt;
//		
//		if (test != null) {
//			switch (test) {
//			case prod_taxtype:
//				if(get)
//					returnedVal = this.prod_taxtype;
//				else
//					this.prod_taxtype = value;
//				break;
//			case addon:
//				if (get)
//					returnedVal = this.addon;
//				else
//					this.addon = value;
//				break;
//			case isAdded:
//				if (get)
//					returnedVal = this.isAdded;
//				else
//					this.isAdded = value;
//				break;
//			case isPrinted:
//				if (get)
//					returnedVal = this.isPrinted;
//				else
//					this.isPrinted = value;
//				break;
//			case item_void:
//				if (get)
//					returnedVal = this.item_void;
//				else
//					this.item_void = value;
//				break;
//			case ordprod_id:
//				if (get)
//					returnedVal = this.ordprod_id;
//				else
//					this.ordprod_id = value;
//				break;
//			case ord_id:
//				if (get)
//					returnedVal = this.ord_id;
//				else
//					this.ord_id = value;
//				break;
//			case prod_id:
//				if (get)
//					returnedVal = this.prod_id;
//				else
//					this.prod_id = value;
//				break;
//			case ordprod_qty:
//				if (get)
//					returnedVal = this.ordprod_qty;
//				else
//					this.ordprod_qty = value;
//				break;
//			case overwrite_price:
//				if (get)
//				{
//					returnedVal = this.overwrite_price;
//					if(returnedVal ==null||returnedVal.isEmpty())
//						returnedVal = Global.formatNumToLocale(0.0);
//				}
//				else
//					this.overwrite_price = value;
//				break;
//			case ordprod_comment:
//				if(get)
//					returnedVal = this.ordprod_comment;
//				else
//					this.ordprod_comment = value;
//				break;
//			case reason_id:
//				if (get)
//					returnedVal = this.reason_id;
//				else
//					this.reason_id = value;
//				break;
//			case ordprod_name:
//				if (get)
//					returnedVal = this.ordprod_name;
//				else
//					this.ordprod_name = value;
//				break;
//			case ordprod_desc:
//				if (get)
//					returnedVal = this.ordprod_desc;
//				else
//					this.ordprod_desc = value;
//				break;
//			case pricelevel_id:
//				if (get)
//					returnedVal = this.pricelevel_id;
//				else
//					this.pricelevel_id = value;
//				break;
//			case prod_seq:
//				if (get)
//					returnedVal = this.prod_seq;
//				else
//					this.prod_seq = value;
//				break;
//			case uom_name:
//				if (get)
//					returnedVal = this.uom_name;
//				else
//					this.uom_name = value;
//				break;
//			case uom_conversion:
//				if (get)
//					returnedVal = this.uom_conversion;
//				else
//					this.uom_conversion = value;
//				break;
//			case uom_id:
//				if(get)
//					returnedVal = this.uom_id;
//				else
//					this.uom_id = value;
//				break;
//			case prod_taxId:
//				if (get)
//					returnedVal = this.prod_taxId;
//				else
//					this.prod_taxId = value;
//				break;
//			case prod_taxValue:
//				if (get)
//					returnedVal = this.prod_taxValue;
//				else
//					this.prod_taxValue = value;
//				break;
//			case discount_id:
//				if (get)
//					returnedVal = this.discount_id;
//				else
//					this.discount_id = value;
//				break;
//			case discount_value:
//				if (get)
//					returnedVal = this.discount_value;
//				else
//					this.discount_value = value;
//				break;
//			case prod_taxcode:
//				if (get)
//					returnedVal = this.prod_taxcode;
//				else
//					this.prod_taxcode = value;
//				break;
//			case itemTotal:
//				if (get)
//				{
//					returnedVal = this.itemTotal;
//					if(returnedVal ==null||returnedVal.isEmpty())
//						returnedVal = Global.formatNumToLocale(0.0);
//				}
//				else
//					this.itemTotal = value;
//				break;
//			case disAmount:
//				if (get)
//					returnedVal = this.disAmount;
//				else
//					this.disAmount = value;
//				break;
//			case disTotal:
//				if (get)
//					returnedVal = this.disTotal;
//				else
//					this.disTotal = value;
//				break;
//			case taxAmount:
//				if (get)
//					returnedVal = this.taxAmount;
//				else
//					this.taxAmount = value;
//				break;
//			case taxTotal:
//				if (get)
//					returnedVal = this.taxTotal;
//				else
//					this.taxTotal = value;
//				break;
//				
//			case onHand:
//				if(get)
//					returnedVal = this.onHand;
//				else
//				{
//					if(value.isEmpty())
//						value = "0";
//					this.onHand = value;
//				}
//				break;
//			case imgURL:
//				if(get)
//					returnedVal = this.imgURL;
//				else
//					this.imgURL = value;
//				break;
//			case prod_istaxable:
//				if(get)
//					returnedVal = this.prod_istaxable;
//				else
//					this.prod_istaxable = value;
//				break;
//			case global_taxamount:
//				if(get)
//					returnedVal = this.global_taxamount;
//				else
//					this.global_taxamount = value;
//				break;
//			case tax_position:
//				if(get)
//					returnedVal = this.tax_position;
//				else
//					this.tax_position = value;
//				break;
//			case discount_position:
//				if(get)
//					returnedVal = this.discount_position;
//				else
//					this.discount_position = value;
//				break;
//			case pricelevel_position:
//				if(get)
//					returnedVal = this.pricelevel_position;
//				else
//					this.pricelevel_position = value;
//				break;
//			case uom_position:
//				if(get)
//					returnedVal = this.uom_position;
//				else
//					this.uom_position = value;
//				break;
//			case prod_price:
//				if(get)
//					returnedVal = this.prod_price;
//				else
//					this.prod_price = value;
//				break;
//			case priceLevelName:
//				if(get)
//					returnedVal = this.priceLevelName;
//				else
//					this.priceLevelName = value;
//				break;
//			case prod_type:
//				if(get)
//					returnedVal = this.prod_type;
//				else
//					this.prod_type = value;
//				break;
//			case tax_type:
//				if(get)
//					returnedVal = this.tax_type;
//				else
//					this.tax_type = value;
//				break;
//			case discount_is_taxable:
//				if(get)
//					returnedVal = this.discount_is_taxable;
//				else
//					this.discount_is_taxable = value;
//				break;
//			case itemSubtotal:
//				if(get)
//					returnedVal = this.itemSubtotal;
//				else
//					this.itemSubtotal = value;
//				break;
//			case discount_is_fixed:
//				if(get)
//					returnedVal = this.discount_is_fixed;
//				else
//					this.discount_is_fixed = value;
//				break;
//			case addon_position:
//				if(get)
//					returnedVal = this.addon_position;
//				else
//					this.addon_position = value;
//				break;
//			case addon_section_name:
//				if(get)
//					returnedVal = this.addon_section_name;
//				else
//					this.addon_section_name = value;
//				break;
//			case hasAddons:
//				if(get)
//					returnedVal = this.hasAddons;
//				else
//					this.hasAddons = value;
//				break;
//			case cat_id:
//				if(get)
//					returnedVal = this.cat_id;
//				else
//					this.cat_id = value;
//				break;
//			case prod_price_points:
//				if(get)
//					returnedVal = this.prod_price_points;
//				else
//					this.prod_price_points = value;
//				break;
//			case prod_value_points:
//				if(get)
//					returnedVal = this.prod_value_points;
//				else
//					this.prod_value_points = value;
//				break;
//			case payWithPoints:
//				if(get)
//					returnedVal = this.payWithPoints;
//				else
//					this.payWithPoints = value;
//				break;
//			case prod_price_updated:
//				if(get)
//					returnedVal = this.prod_price_updated;
//				else
//					this.prod_price_updated = value;
//				break;
//			case price_vat_exclusive:
//				if(get)
//					returnedVal = this.price_vat_exclusive;
//				else
//					this.price_vat_exclusive = value;
//				break;
//			case itemTotalVatExclusive:
//				if(get)
//					returnedVal = this.itemTotalVatExclusive;
//				else
//					this.itemTotalVatExclusive = value;
//				break;
//			}
//		}
//		if(returnedVal==null)
//			returnedVal = "";
//		return returnedVal;
//	}
}
