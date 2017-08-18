package com.android.emobilepos.models.orders;

import android.app.Activity;
import android.text.TextUtils;

import com.android.database.ProductsHandler;
import com.android.emobilepos.models.MixAndMatchDiscount;
import com.android.emobilepos.models.Product;
import com.android.emobilepos.models.realms.ProductAttribute;
import com.android.emobilepos.ordering.OrderingMain_FA;
import com.android.support.Global;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import util.json.JsonUtils;

public class OrderProduct implements Cloneable, Comparable<OrderProduct> {
    public String priceLevelName = "";
    public List<ProductAttribute> requiredProductAttributes = new ArrayList<ProductAttribute>();
    public List<OrderProduct> addonsProducts = new ArrayList<OrderProduct>();
    public String addon_section_name = "";
    public String addon_position = "";
    private boolean addon;
    private boolean isAdded;
    private boolean isPrinted;
    private String price_vat_exclusive = "0";
    private String item_void = "";
    private String ordprod_id = "";
    private String ord_id = "";
    private String prod_id = "";
    private String prod_sku = "";
    private String prod_upc = "";
    private String ordprod_qty = "";
    private BigDecimal overwrite_price;
    private String reason_id = "";
    @SerializedName(value = "ordprod_name", alternate = {"prod_name"})
    private String ordprod_name = "";
    private String ordprod_desc = "";
    private String ordprod_comment = "";
    private String pricelevel_id = "";
    private String prod_seq = "";
    private String uom_name = "";
    private String uom_conversion = "";
    private String uom_id = "";
    @SerializedName("prod_taxid")
    private String prod_taxId = "";
    private BigDecimal prod_taxValue;
    private String discount_id = "";
    private String discount_value = "";
    private String prod_taxcode = "";
    private String prod_istaxable = "";
    private String cat_id = "";
    private String cat_name = "";
    private String prod_price_points = "0";
    private String prod_value_points = "0";
    private String payWithPoints = "false";
    private String pricesXGroupid;
    private String itemTotalVatExclusive = "0";
    private String itemTotal = "0";
    //    private String itemSubtotal = "0";
    private String disAmount = "0";
    private String disTotal = "0";
    private String taxAmount = "0";
    private String taxTotal = "0";
    private String onHand = "0";
    private String imgURL = "";
    private String tax_position = "";
    private String discount_position = "";
    private String pricelevel_position = "";
    private String uom_position = "";
    //    private String prod_taxtype;
    private String prod_price = "";
    private String prod_type = "";
    //    private String tax_type = "";
    private String discount_is_taxable = "0";
    private String discount_is_fixed = "0";
    private Boolean hasAddons; //0 no addons, 1 it has addons
    private String prod_price_updated = "0";

    private boolean isReturned = false;
    private String assignedSeat;
    private int seatGroupId;
    private int mixMatchQtyApplied;
    private BigDecimal mixMatchOriginalPrice;
    private List<MixAndMatchDiscount> mixAndMatchDiscounts;
    private String productPriceLevelTotal;
    @SerializedName("parentAddonOrderProductId")
    private String addon_ordprod_id;
    private String prod_extradesc;
    private String consignment_qty;
    private boolean attributesCompleted;
    private boolean GC;

    public OrderProduct(Product product) {
        this.setAssignedSeat(product.getAssignedSeat());
        this.setCat_id(product.getCatId());
        this.setCat_name(product.getCategoryName());
        this.setProd_id(product.getId());
        this.setProd_sku(product.getProd_sku());
        this.setProd_upc(product.getProd_upc());
        this.setProd_price(product.getProdPrice());
        if (!TextUtils.isEmpty(product.getProdPrice())) {
            this.setMixMatchOriginalPrice(new BigDecimal(product.getProdPrice()));
        }
        this.setGC(product.isGC());
        this.setImgURL(product.getProdImgName());
        this.setProd_type(product.getProdType());
        this.setOnHand(product.getProdOnHand());
        this.setProd_istaxable(product.getProdIstaxable());
        this.setOrdprod_desc(product.getProdDesc());
        this.setProd_taxcode(product.getProdTaxCode());
        this.setProd_taxId(product.getProdTaxType());
        this.setOrdprod_name(product.getProdName());
//        this.setTax_type(product.getProdTaxType());
        this.setProd_price_points(String.valueOf(product.getProdPricePoints()));
        this.setProd_value_points(String.valueOf(product.getProdValuePoints()));
        this.setPricesXGroupid(product.getPricesXGroupid());
        this.setOrdprod_name(product.getProdName());
        this.setProd_extradesc(product.getProdExtraDesc());
        this.setOrdprod_qty(OrderingMain_FA.returnItem ? "-1" : "1");
        this.setReturned(OrderingMain_FA.returnItem);
    }

    public OrderProduct() {

    }

    public static OrderProduct getInstance(String ordprod_id) {
        OrderProduct product = new OrderProduct();
        product.setOrdprod_id(ordprod_id);
        return product;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Object clone = super.clone();
        List<OrderProduct> cloneAddons = new ArrayList<>();
        for (OrderProduct addon : addonsProducts) {
            cloneAddons.add((OrderProduct) addon.clone());
        }
        ((OrderProduct) clone).addonsProducts = cloneAddons;
        return clone;
    }

    public boolean isVoid() {
        return getItem_void().equals("1");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof String) {
            return this.getOrdprod_id().equalsIgnoreCase((String) o);
        } else {
            return this.getOrdprod_id().equalsIgnoreCase(((OrderProduct) o).getOrdprod_id());
        }
    }

    @Override
    public int compareTo(OrderProduct another) {
        return this.getOrdprod_id().compareTo(another.getOrdprod_id());
    }

    public void setOverwritePrice(BigDecimal overwriteAmount, Activity activity) {
        ProductsHandler productsHandler = new ProductsHandler(activity);
        HashMap<String, String> map = productsHandler
                .getDiscountDetail(getDiscount_id());
        BigDecimal prod_qty;
        BigDecimal new_subtotal;
        try {
            prod_qty = new BigDecimal(
                    getOrdprod_qty());
        } catch (Exception e) {
            prod_qty = new BigDecimal("0");
        }

        if (!map.isEmpty()) {
            if (map.get("discount_type")
                    .toUpperCase(
                            Locale.getDefault())
                    .trim().equals("FIXED")) {
                new_subtotal = overwriteAmount
                        .multiply(prod_qty)
                        .subtract(
                                new BigDecimal(
                                        map.get("discount_price")));

            } else {
                BigDecimal rate = new BigDecimal(
                        map.get("discount_price"))
                        .divide(new BigDecimal(
                                "100"));
                rate = rate.multiply(overwriteAmount
                        .multiply(prod_qty));

                new_subtotal = overwriteAmount.multiply(
                        prod_qty).subtract(rate);

                setDisTotal(String.valueOf(Global
                        .getRoundBigDecimal(rate)));
                setDiscount_value(String.valueOf(Global
                        .getRoundBigDecimal(rate)));

            }
        } else
            new_subtotal = overwriteAmount
                    .multiply(prod_qty);

        setOverwrite_price(overwriteAmount);
        setProd_price(String.valueOf(Global.getRoundBigDecimal(overwriteAmount)));
//        setItemSubtotal(Global
//                .getRoundBigDecimal(new_subtotal));
        setItemTotal(String.valueOf(Global
                .getRoundBigDecimal(new_subtotal)));
        setPricelevel_id("");
        setProd_price_updated("0");
    }

    public String getPricesXGroupid() {
        return pricesXGroupid;
    }

    public void setPricesXGroupid(String pricesXGroupid) {
        this.pricesXGroupid = pricesXGroupid;
    }

    public String getFinalPrice() {
        if (getOverwrite_price() != null) {
            return getOverwrite_price().toString();
        } else if (!TextUtils.isEmpty(getPrice_vat_exclusive()) && !getItemTotalVatExclusive().equalsIgnoreCase("0")) {
            return getItemTotalVatExclusive();
        } else {
            if (!TextUtils.isEmpty(getProd_price())) {
                return getProd_price();
            } else {
                return "0";
            }
        }
    }

    public void setPrices(String prod_price, String ordprod_qty) {
        this.setProd_price(prod_price);
        this.setItemTotal(Global.getBigDecimalNum(prod_price).multiply(new BigDecimal(ordprod_qty)).toString());
//        this.setItemSubtotal(Global.getBigDecimalNum(prod_price).multiply(new BigDecimal(ordprod_qty)).toString());
    }

    public String getPrice_vat_exclusive() {
        return price_vat_exclusive;
    }

    public void setPrice_vat_exclusive(String price_vat_exclusive) {
        this.price_vat_exclusive = price_vat_exclusive;
    }

    public String getItem_void() {
        return item_void;
    }

    public void setItem_void(String item_void) {
        this.item_void = item_void;
    }

    public String getOrdprod_id() {
        return ordprod_id;
    }

    public void setOrdprod_id(String ordprod_id) {
        this.ordprod_id = ordprod_id;
        if (addonsProducts != null && !addonsProducts.isEmpty()) {
            for (OrderProduct addon : addonsProducts) {
                addon.setAddon_ordprod_id(ordprod_id);
            }
        }
    }

    public String getOrd_id() {
        return ord_id;
    }

    public void setOrd_id(String ord_id) {
        this.ord_id = ord_id;
    }

    public String getProd_id() {
        return prod_id;
    }

    public void setProd_id(String prod_id) {
        this.prod_id = prod_id;
    }

    public String getProd_sku() {
        return prod_sku;
    }

    public void setProd_sku(String prod_sku) {
        this.prod_sku = prod_sku;
    }

    public String getProd_upc() {
        return prod_upc;
    }

    public void setProd_upc(String prod_upc) {
        this.prod_upc = prod_upc;
    }

    public String getOrdprod_qty() {
        return ordprod_qty;
    }

    public void setOrdprod_qty(String ordprod_qty) {
        this.ordprod_qty = ordprod_qty;
    }

    public BigDecimal getOverwrite_price() {
        return overwrite_price;
    }

    public void setOverwrite_price(BigDecimal overwrite_price) {
        this.overwrite_price = overwrite_price;
    }

    public String getReason_id() {
        return reason_id;
    }

    public void setReason_id(String reason_id) {
        this.reason_id = reason_id;
    }

    public String getOrdprod_name() {
        return ordprod_name;
    }

    public void setOrdprod_name(String ordprod_name) {
        this.ordprod_name = ordprod_name;
    }

    public String getOrdprod_desc() {
        return ordprod_desc;
    }

    public void setOrdprod_desc(String ordprod_desc) {
        this.ordprod_desc = ordprod_desc;
    }

    public String getOrdprod_comment() {
        return ordprod_comment;
    }

    public void setOrdprod_comment(String ordprod_comment) {
        this.ordprod_comment = ordprod_comment;
    }

    public String getPricelevel_id() {
        return pricelevel_id;
    }

    public void setPricelevel_id(String pricelevel_id) {
        this.pricelevel_id = pricelevel_id;
    }

    public String getProd_seq() {
        return prod_seq;
    }

    public void setProd_seq(String prod_seq) {
        this.prod_seq = prod_seq;
    }

    public String getUom_name() {
        return uom_name;
    }

    public void setUom_name(String uom_name) {
        this.uom_name = uom_name;
    }

    public String getUom_conversion() {
        return uom_conversion;
    }

    public void setUom_conversion(String uom_conversion) {
        this.uom_conversion = uom_conversion;
    }

    public String getUom_id() {
        return uom_id;
    }

    public void setUom_id(String uom_id) {
        this.uom_id = uom_id;
    }

    public String getProd_taxId() {
        return prod_taxId;
    }

    public void setProd_taxId(String prod_taxId) {
        this.prod_taxId = prod_taxId;
    }

    public BigDecimal getProd_taxValue() {
        return prod_taxValue;
    }

    public void setProd_taxValue(BigDecimal prod_taxValue) {
        this.prod_taxValue = prod_taxValue;
    }

    public String getDiscount_id() {
        return discount_id;
    }

    public void setDiscount_id(String discount_id) {
        this.discount_id = discount_id;
    }

    public String getDiscount_value() {
        return discount_value;
    }

    public void setDiscount_value(String discount_value) {
        this.discount_value = discount_value;
    }

    public String getProd_taxcode() {
        return prod_taxcode;
    }

    public void setProd_taxcode(String prod_taxcode) {
        this.prod_taxcode = prod_taxcode;
    }

    public String getProd_istaxable() {
        return prod_istaxable;
    }

    public void setProd_istaxable(String prod_istaxable) {
        this.prod_istaxable = prod_istaxable;
    }

    public String getCat_id() {
        return cat_id;
    }

    public void setCat_id(String cat_id) {
        this.cat_id = cat_id;
    }

    public String getCat_name() {
        return cat_name;
    }

    public void setCat_name(String cat_name) {
        this.cat_name = cat_name;
    }

    public String getProd_price_points() {
        return prod_price_points;
    }

    public void setProd_price_points(String prod_price_points) {
        this.prod_price_points = prod_price_points;
    }

    public String getProd_value_points() {
        return prod_value_points;
    }

    public void setProd_value_points(String prod_value_points) {
        this.prod_value_points = prod_value_points;
    }

    public String getPayWithPoints() {
        return payWithPoints;
    }

    public String getItemTotalVatExclusive() {
        return itemTotalVatExclusive;
    }

    public void setItemTotalVatExclusive(String itemTotalVatExclusive) {
        this.itemTotalVatExclusive = itemTotalVatExclusive;
    }

    public String getItemTotal() {
        return itemTotal;
    }

    public void setItemTotal(String itemTotal) {
        this.itemTotal = itemTotal;
    }

    public String getDisAmount() {
        return disAmount;
    }

//    public String getItemSubtotal() {
//        return itemSubtotal;
//    }
//
//    public void setItemSubtotal(String itemSubtotal) {
//        this.itemSubtotal = itemSubtotal;
//    }

    public void setDisAmount(String disAmount) {
        this.disAmount = disAmount;
    }

    public String getDisTotal() {
        return disTotal;
    }

    public void setDisTotal(String disTotal) {
        this.disTotal = disTotal;
    }

    public String getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(String taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getTaxTotal() {
        return taxTotal;
    }

    public void setTaxTotal(String taxTotal) {
        this.taxTotal = taxTotal;
    }

    public String getOnHand() {
        return onHand;
    }

    public void setOnHand(String onHand) {
        this.onHand = onHand;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    public String getTax_position() {
        return tax_position;
    }

    public void setTax_position(String tax_position) {
        this.tax_position = tax_position;
    }

    public String getDiscount_position() {
        return discount_position;
    }

    public void setDiscount_position(String discount_position) {
        this.discount_position = discount_position;
    }

    public String getPricelevel_position() {
        return pricelevel_position;
    }

    public void setPricelevel_position(String pricelevel_position) {
        this.pricelevel_position = pricelevel_position;
    }

    public String getUom_position() {
        return uom_position;
    }

    public void setUom_position(String uom_position) {
        this.uom_position = uom_position;
    }

    public String getProd_price() {
        if (TextUtils.isEmpty(prod_price) || prod_price.equalsIgnoreCase("null")) {
            prod_price = "0";
        }
        return prod_price;
    }

    public void setProd_price(String prod_price) {
        this.prod_price = prod_price;
    }

    public String getProd_type() {
        return prod_type;
    }

    public void setProd_type(String prod_type) {
        this.prod_type = prod_type;
    }

//    public String getTax_type() {
//        return tax_type;
//    }
//
//    public void setTax_type(String tax_type) {
//        this.tax_type = tax_type;
//    }

    public String getDiscount_is_taxable() {
        return discount_is_taxable;
    }

    public void setDiscount_is_taxable(String discount_is_taxable) {
        this.discount_is_taxable = discount_is_taxable;
    }

    public String getDiscount_is_fixed() {
        return discount_is_fixed;
    }

    public void setDiscount_is_fixed(String discount_is_fixed) {
        this.discount_is_fixed = discount_is_fixed;
    }

    public String getPriceLevelName() {
        return priceLevelName;
    }

//    public String getProd_taxtype() {
//        return prod_taxtype;
//    }
//
//    public void setProd_taxtype(String prod_taxtype) {
//        this.prod_taxtype = prod_taxtype;
//    }

    public void setPriceLevelName(String priceLevelName) {
        this.priceLevelName = priceLevelName;
    }

    public List<ProductAttribute> getRequiredProductAttributes() {
        return requiredProductAttributes;
    }

    public void setRequiredProductAttributes(List<ProductAttribute> requiredProductAttributes) {
        this.requiredProductAttributes = requiredProductAttributes;
    }

    public boolean getHasAddons() {
        return addonsProducts != null && !addonsProducts.isEmpty();
    }

    public String getAddon_section_name() {
        return addon_section_name;
    }

    public void setAddon_section_name(String addon_section_name) {
        this.addon_section_name = addon_section_name;
    }

    public String getAddon_position() {
        return addon_position;
    }

    public void setAddon_position(String addon_position) {
        this.addon_position = addon_position;
    }

    public String getProd_price_updated() {
        return prod_price_updated;
    }

    public void setProd_price_updated(String prod_price_updated) {
        this.prod_price_updated = prod_price_updated;
    }

    public boolean isReturned() {
        return isReturned;
    }

    public void setReturned(boolean returned) {
        isReturned = returned;
    }

    public String getAssignedSeat() {
        return assignedSeat;
    }

    public void setAssignedSeat(String assignedSeat) {
        this.assignedSeat = assignedSeat;
    }

    public int getSeatGroupId() {
        return seatGroupId;
    }

    public void setSeatGroupId(int seatGroupId) {
        this.seatGroupId = seatGroupId;
    }

    public int getMixMatchQtyApplied() {
        return mixMatchQtyApplied;
    }

    public void setMixMatchQtyApplied(int mixMatchQtyApplied) {
        this.mixMatchQtyApplied = mixMatchQtyApplied;
    }

    public BigDecimal getMixMatchOriginalPrice() {
        return mixMatchOriginalPrice;
    }

    public void setMixMatchOriginalPrice(BigDecimal mixMatchOriginalPrice) {
        this.mixMatchOriginalPrice = mixMatchOriginalPrice;
    }

    public List<MixAndMatchDiscount> getMixAndMatchDiscounts() {
        return mixAndMatchDiscounts;
    }

    public void setMixAndMatchDiscounts(List<MixAndMatchDiscount> mixAndMatchDiscounts) {
        this.mixAndMatchDiscounts = mixAndMatchDiscounts;
    }

    public String getConsignment_qty() {
        return consignment_qty;
    }

    public void setConsignment_qty(String consignment_qty) {
        this.consignment_qty = consignment_qty;
    }

    public String getProductPriceLevelTotal() {
        return productPriceLevelTotal;
    }

    public void setProductPriceLevelTotal(String productPriceLevelTotal) {
        this.productPriceLevelTotal = productPriceLevelTotal;
    }

    public void resetMixMatch() {
        this.setMixAndMatchDiscounts(null);
        this.setMixMatchQtyApplied(0);
        this.setProd_price(String.valueOf(getMixMatchOriginalPrice()));
    }

    public String getProd_extradesc() {
        return prod_extradesc;
    }

    public void setProd_extradesc(String prod_extradesc) {
        this.prod_extradesc = prod_extradesc;
    }

    @Override
    public String toString() {
        return getProd_id() + "-" + getOrdprod_name();
    }

    public String getAddon_ordprod_id() {
        return addon_ordprod_id;
    }

    public void setAddon_ordprod_id(String addon_ordprod_id) {
        this.addon_ordprod_id = addon_ordprod_id;
    }

    public boolean isAddon() {
        return addon;
    }

    public void setAddon(boolean addon) {
        this.addon = addon;
    }

    public boolean isAdded() {
        return isAdded;
    }

    public void setAdded(boolean added) {
        isAdded = added;
    }

    public boolean isPrinted() {
        return isPrinted;
    }

    public void setPrinted(boolean printed) {
        isPrinted = printed;
    }

    public String toJson() {
        Gson gson = JsonUtils.getInstance();
        return gson.toJson(this);
    }

    public BigDecimal getAddonsTotalPrice() {
        BigDecimal price = new BigDecimal(0);
        for (OrderProduct addon : addonsProducts) {
            price = price.add(new BigDecimal(addon.getFinalPrice()));
        }
        return price;
    }

//    public BigDecimal getTaxAmountCalculated() {
//        BigDecimal taxAmount = getProductPriceTaxableAmountCalculated()
//                .multiply(Global.getBigDecimalNum(getTaxAmount()).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
//                .setScale(4, RoundingMode.HALF_UP);
//        setProd_taxValue(taxAmount);
//        return taxAmount;
//    }

    public BigDecimal getItemSubtotalCalculated() {
        BigDecimal subtotal;
        BigDecimal addonsTotalPrice = getAddonsTotalPrice();
        BigDecimal finalPrice = new BigDecimal(getFinalPrice()).multiply(new BigDecimal(getOrdprod_qty()));
        if (isVAT()) {
            finalPrice = finalPrice.add(getProd_taxValue());
        }
        BigDecimal discount = getDiscountTotal();
        subtotal = finalPrice.subtract(discount).add(addonsTotalPrice).setScale(6, RoundingMode.HALF_UP);
        return subtotal;
    }

    public BigDecimal getItemTotalCalculated() {
        BigDecimal subtotal;
        BigDecimal addonsTotalPrice = getAddonsTotalPrice();
        BigDecimal finalPrice = new BigDecimal(getFinalPrice()).multiply(new BigDecimal(getOrdprod_qty()));
        if (isVAT()) {
            finalPrice = finalPrice.add(getProd_taxValue());
        }
        subtotal = finalPrice.add(addonsTotalPrice).setScale(6, RoundingMode.HALF_UP);
        return subtotal;
    }

    public BigDecimal getGranTotalCalculated() {
        BigDecimal taxAmount = isVAT() ? new BigDecimal(0) : getProd_taxValue();
        BigDecimal subtotalCalculated = getItemSubtotalCalculated();
        BigDecimal granTotal = subtotalCalculated.add(taxAmount)
                .setScale(6, RoundingMode.HALF_UP);
        return granTotal;
    }

//    public BigDecimal getDiscountTotal() {
//        return Global.getBigDecimalNum(getDisTotal());
//}

    public BigDecimal getDiscountTotal() {
        if (isReturned()) {
            return new BigDecimal(0);
        }
        BigDecimal calculatedDiscount;
        BigDecimal disAmount = Global.getBigDecimalNum(getDisAmount());
        if (isDiscountFixed()) {
            calculatedDiscount = disAmount;
        } else {
            calculatedDiscount = getItemTotalCalculated().multiply(disAmount).divide(new BigDecimal(100)).setScale(6, RoundingMode.HALF_UP);
        }
        if (getItemTotalCalculated().compareTo(calculatedDiscount) < 1) {
            calculatedDiscount = getItemTotalCalculated();
        }
        setDisTotal(String.valueOf(Global.getRoundBigDecimal(calculatedDiscount)));
        return Global.getRoundBigDecimal(calculatedDiscount);
    }

    public BigDecimal getProductPriceTaxableAmountCalculated() {
        BigDecimal taxableAmount;
        if (isTaxable()) {
            taxableAmount = (new BigDecimal(getFinalPrice()).multiply(new BigDecimal(getOrdprod_qty())))
                    .add(getAddonsTotalPrice())
                    .setScale(6, RoundingMode.HALF_UP);
            if (isDiscountTaxable()) {
                taxableAmount = taxableAmount
                        .subtract(getDiscountTotal())
                        .setScale(6, RoundingMode.HALF_UP);
            }
        } else {
            taxableAmount = BigDecimal.valueOf(0);
        }
        return taxableAmount;
    }

    public boolean isTaxable() {
        return prod_istaxable.equals("1");
    }

    public boolean isDiscountTaxable() {
        return discount_is_taxable.equals("1");
    }

    public boolean isDiscountFixed() {
        return discount_is_fixed != null && discount_is_fixed.equals("1");
    }

    public boolean isVAT() {
        return !TextUtils.isEmpty(getPrice_vat_exclusive()) && !getPrice_vat_exclusive().equals("0");
    }

    public boolean isAttributesCompleted() {
        return attributesCompleted;
    }

    public void setAttributesCompleted(boolean attributesCompleted) {
        this.attributesCompleted = attributesCompleted;
    }

    public boolean isPayWithPoints() {
        return !TextUtils.isEmpty(getProd_price_points()) && Double.parseDouble(getProd_price_points()) > 0;
    }

    public void setPayWithPoints(String payWithPoints) {
        this.payWithPoints = payWithPoints;
    }

    public boolean isGC() {
        return GC;
    }

    public void setGC(boolean GC) {
        this.GC = GC;
    }
}