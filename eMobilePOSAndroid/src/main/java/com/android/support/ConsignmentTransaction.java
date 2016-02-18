package com.android.support;


public class ConsignmentTransaction {

    public String Cons_ID = "";
    public String ConsTrans_ID = "";
    public String ConsEmp_ID = "";
    public String ConsCust_ID = "";
    public String ConsInvoice_ID = "";
    public String ConsReturn_ID = "";
    public String ConsPickup_ID = "";
    public String ConsDispatch_ID = "";
    public String ConsInventory_Qty = "";
    public String ConsProd_ID = "";
    public String ConsOriginal_Qty = "0";
    public String ConsStock_Qty = "0";
    public String ConsInvoice_Qty = "0";
    public String ConsReturn_Qty = "0";
    public String ConsDispatch_Qty = "0";
    public String ConsPickup_Qty = "0";
    public String ConsNew_Qty = "0";
    public String Cons_timecreated = "";
    public String is_synched = "0";
    public String invoice_total = "0";

    public ConsignmentTransaction() {
        Cons_timecreated = Global.getCurrentDate();
    }

}
