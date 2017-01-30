package com.android.emobilepos.models;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import util.json.JsonUtils;

/**
 * Created by Guarionex on 12/22/2015.
 */
public class Product {
    @SerializedName("prod_id")
    private String id;
    @SerializedName("prod_disc_type")
    private String prod_disc_type;
    @SerializedName("assignedSeat")
    private String assignedSeat;
    @SerializedName("prod_desc")
    private String prodDesc;
    @SerializedName("prod_name")
    private String prodName;
    @SerializedName("prodExtraDesc")
    private String prodExtraDesc;
    @SerializedName("volumePrice")
    private String volumePrice;
    @SerializedName("priceLevelPrice")
    private String priceLevelPrice;
    @SerializedName("chainPrice")
    private String chainPrice;
    @SerializedName("masterPrice")
    private String masterPrice;
    @SerializedName("localProdOnhand")
    private String localProdOnhand;
    @SerializedName("masterProdOnhand")
    private String masterProdOnhand;
    @SerializedName("locationQty")
    private String locationQty;
    @SerializedName("prodImgName")
    private String prodImgName;
    @SerializedName("prodIstaxable")
    private String prodIstaxable;
    @SerializedName("prod_type")
    private String prodType;
    @SerializedName("cat_id")
    private String catId;
    @SerializedName("prod_price_points")
    private int prodPricePoints;
    @SerializedName("prod_value_points")
    private int prodValuePoints;
    @SerializedName("prod_taxtype")
    private String prodTaxType;
    @SerializedName("prod_taxcode")
    private String prodTaxCode;
    @SerializedName("prod_onhand")
    private String prodOnHand;
    @SerializedName("prod_price")
    private String prodPrice;
    @SerializedName("prod_sku")
    private String prod_sku;
    @SerializedName("prod_upc")
    private String prod_upc;
    @SerializedName("prod_onorder")
    private String prod_onorder;
    @SerializedName("prod_update")
    private
    String prod_update;
    @SerializedName("isactive")
    private
    String isactive;
    @SerializedName("prod_showOnline")
    private
    String prod_showOnline;
    @SerializedName("prod_ispromo")
    private
    String prod_ispromo;
    @SerializedName("prod_expense")
    private
    String prod_expense;
    @SerializedName("_rowversion")
    private
    String rowversion;
    @SerializedName("GC")
    private
    boolean GC;
    @SerializedName("EBT")
    private
    boolean EBT;
    @SerializedName("PricesXGroupid")
    private String PricesXGroupid;
    @SerializedName("prod_uom")
    private String prod_uom;
    private String prod_cost;
    private String prod_glaccount;
    private String prod_mininv;
    private String prod_shipping;
    private String prod_weight;
    private String prod_disc_type_points;
    private String categoryName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProdDesc() {
        return prodDesc;
    }

    public void setProdDesc(String prodDesc) {
        this.prodDesc = prodDesc;
    }

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public String getProdExtraDesc() {
        return prodExtraDesc;
    }

    public void setProdExtraDesc(String prodExtraDesc) {
        this.prodExtraDesc = prodExtraDesc;
    }

    public String getVolumePrice() {
        return volumePrice;
    }

    public void setVolumePrice(String volumePrice) {
        this.volumePrice = volumePrice;
    }

    public String getPriceLevelPrice() {
        return priceLevelPrice;
    }

    public void setPriceLevelPrice(String priceLevelPrice) {
        this.priceLevelPrice = priceLevelPrice;
    }

    public String getChainPrice() {
        return chainPrice;
    }

    public void setChainPrice(String chainPrice) {
        this.chainPrice = chainPrice;
    }

    public String getMasterPrice() {
        return masterPrice;
    }

    public void setMasterPrice(String masterPrice) {
        this.masterPrice = masterPrice;
    }

    public String getLocalProdOnhand() {
        return localProdOnhand;
    }

    public void setLocalProdOnhand(String localProdOnhand) {
        this.localProdOnhand = localProdOnhand;
    }

    public String getMasterProdOnhand() {
        return masterProdOnhand;
    }

    public void setMasterProdOnhand(String masterProdOnhand) {
        this.masterProdOnhand = masterProdOnhand;
    }

    public String getLocationQty() {
        return locationQty;
    }

    public void setLocationQty(String locationQty) {
        this.locationQty = locationQty;
    }

    public String getProdImgName() {
        return prodImgName;
    }

    public void setProdImgName(String prodImgName) {
        this.prodImgName = prodImgName;
    }

    public String getProdIstaxable() {
        return prodIstaxable;
    }

    public void setProdIstaxable(String prodIstaxable) {
        this.prodIstaxable = prodIstaxable;
    }

    public String getProdType() {
        return prodType;
    }

    public void setProdType(String prodType) {
        this.prodType = prodType;
    }

    public String getCatId() {
        return catId;
    }

    public void setCatId(String catId) {
        this.catId = catId;
    }

    public int getProdPricePoints() {
        return prodPricePoints;
    }

    public void setProdPricePoints(int prodPricePoints) {
        this.prodPricePoints = prodPricePoints;
    }

    public int getProdValuePoints() {
        return prodValuePoints;
    }

    public void setProdValuePoints(int prodValuePoints) {
        this.prodValuePoints = prodValuePoints;
    }

    public String getProdTaxType() {
        return prodTaxType;
    }

    public void setProdTaxType(String prodTaxType) {
        this.prodTaxType = prodTaxType;
    }

    public String getProdTaxCode() {
        return prodTaxCode;
    }

    public void setProdTaxCode(String prodTaxCode) {
        this.prodTaxCode = prodTaxCode;
    }

    public String getProdOnHand() {
        return prodOnHand;
    }

    public void setProdOnHand(String prodOnHand) {
        this.prodOnHand = prodOnHand;
    }

    public String getProdPrice() {
        return prodPrice;
    }

    public void setProdPrice(String prodPrice) {
        this.prodPrice = prodPrice;
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

    public String getAssignedSeat() {
        return assignedSeat;
    }

    public void setAssignedSeat(String seatNumber) {
        this.assignedSeat = seatNumber;
    }

    public String getProd_disc_type() {
        return prod_disc_type;
    }

    public void setProd_disc_type(String prod_disc_type) {
        this.prod_disc_type = prod_disc_type;
    }

    public String getProd_onorder() {
        return prod_onorder;
    }

    public void setProd_onorder(String prod_onorder) {
        this.prod_onorder = prod_onorder;
    }

    public String getProd_update() {
        return prod_update;
    }

    public void setProd_update(String prod_update) {
        this.prod_update = prod_update;
    }

    public String getIsactive() {
        return isactive;
    }

    public void setIsactive(String isactive) {
        this.isactive = isactive;
    }

    public String getProd_showOnline() {
        return prod_showOnline;
    }

    public void setProd_showOnline(String prod_showOnline) {
        this.prod_showOnline = prod_showOnline;
    }

    public String getProd_ispromo() {
        return prod_ispromo;
    }

    public void setProd_ispromo(String prod_ispromo) {
        this.prod_ispromo = prod_ispromo;
    }

    public String getProd_expense() {
        return prod_expense;
    }

    public void setProd_expense(String prod_expense) {
        this.prod_expense = prod_expense;
    }

    public String getRowversion() {
        return rowversion;
    }

    public void setRowversion(String rowversion) {
        this.rowversion = rowversion;
    }

    public boolean isGC() {
        return GC;
    }

    public void setGC(boolean GC) {
        this.GC = GC;
    }

    public boolean isEBT() {
        return EBT;
    }

    public void setEBT(boolean EBT) {
        this.EBT = EBT;
    }

    public String getPricesXGroupid() {
        return PricesXGroupid;
    }

    public void setPricesXGroupid(String pricesXGroupid) {
        PricesXGroupid = pricesXGroupid;
    }

    public String getProd_uom() {
        return prod_uom;
    }

    public void setProd_uom(String prod_uom) {
        this.prod_uom = prod_uom;
    }

    public String getProd_cost() {
        return prod_cost;
    }

    public void setProd_cost(String prod_cost) {
        this.prod_cost = prod_cost;
    }

    public String getProd_glaccount() {
        return prod_glaccount;
    }

    public void setProd_glaccount(String prod_glaccount) {
        this.prod_glaccount = prod_glaccount;
    }

    public String getProd_mininv() {
        return prod_mininv;
    }

    public void setProd_mininv(String prod_mininv) {
        this.prod_mininv = prod_mininv;
    }

    public String getProd_shipping() {
        return prod_shipping;
    }

    public void setProd_shipping(String prod_shipping) {
        this.prod_shipping = prod_shipping;
    }

    public String getProd_weight() {
        return prod_weight;
    }

    public void setProd_weight(String prod_weight) {
        this.prod_weight = prod_weight;
    }

    public String getProd_disc_type_points() {
        return prod_disc_type_points;
    }

    public void setProd_disc_type_points(String prod_disc_type_points) {
        this.prod_disc_type_points = prod_disc_type_points;
    }

    public String toJson() {
        Gson gson = JsonUtils.getInstance();
        return gson.toJson(this);
    }

    public String getFinalPrice() {
        if (!TextUtils.isEmpty(volumePrice)) {
            return volumePrice;
        } else if (!TextUtils.isEmpty(priceLevelPrice)) {
            return priceLevelPrice;
        } else if (!TextUtils.isEmpty(chainPrice)) {
            return chainPrice;
        } else if (!TextUtils.isEmpty(masterPrice)) {
            return masterPrice;
        } else {
            return "0";
        }
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }
}