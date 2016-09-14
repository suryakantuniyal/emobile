package com.android.emobilepos.models;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Guarionex on 3/8/2016.
 */
public class SalesAssociate extends RealmObject {
    private int emp_id;
    private String zone_id;
    private String emp_name;
    private String emp_init;
    private String emp_pcs;
    private String emp_lastlogin;
    private int emp_pos;
    private String qb_emp_id;
    private String qb_salesrep_id;
    private int isactive;
    private String tax_default;
    private boolean loc_items;
    private String _rowversion;
    private String lastSync;
    private boolean TupyWalletDevice;
    private boolean VAT;
    private RealmList<DinningTable> assignedDinningTables;


    public int getEmp_id() {
        return emp_id;
    }

    public void setEmp_id(int emp_id) {
        this.emp_id = emp_id;
    }

    public String getZone_id() {
        return zone_id;
    }

    public void setZone_id(String zone_id) {
        this.zone_id = zone_id;
    }

    public String getEmp_name() {
        return emp_name;
    }

    public void setEmp_name(String emp_name) {
        this.emp_name = emp_name;
    }

    public String getEmp_init() {
        return emp_init;
    }

    public void setEmp_init(String emp_init) {
        this.emp_init = emp_init;
    }

    public String getEmp_pcs() {
        return emp_pcs;
    }

    public void setEmp_pcs(String emp_pcs) {
        this.emp_pcs = emp_pcs;
    }

    public String getEmp_lastlogin() {
        return emp_lastlogin;
    }

    public void setEmp_lastlogin(String emp_lastlogin) {
        this.emp_lastlogin = emp_lastlogin;
    }

    public int getEmp_pos() {
        return emp_pos;
    }

    public void setEmp_pos(int emp_pos) {
        this.emp_pos = emp_pos;
    }

    public String getQb_emp_id() {
        return qb_emp_id;
    }

    public void setQb_emp_id(String qb_emp_id) {
        this.qb_emp_id = qb_emp_id;
    }

    public String getQb_salesrep_id() {
        return qb_salesrep_id;
    }

    public void setQb_salesrep_id(String qb_salesrep_id) {
        this.qb_salesrep_id = qb_salesrep_id;
    }

    public int isactive() {
        return isactive;
    }

    public boolean isActive() {
        return isactive == 1;
    }


    public void setIsactive(int isactive) {
        this.isactive = isactive;
    }

    public String getTax_default() {
        return tax_default;
    }

    public void setTax_default(String tax_default) {
        this.tax_default = tax_default;
    }

    public boolean isLoc_items() {
        return loc_items;
    }

    public void setLoc_items(boolean loc_items) {
        this.loc_items = loc_items;
    }

    public String get_rowversion() {
        return _rowversion;
    }

    public void set_rowversion(String _rowversion) {
        this._rowversion = _rowversion;
    }

    public String getLastSync() {
        return lastSync;
    }

    public void setLastSync(String lastSync) {
        this.lastSync = lastSync;
    }

    public boolean isTupyWalletDevice() {
        return TupyWalletDevice;
    }

    public void setTupyWalletDevice(boolean tupyWalletDevice) {
        TupyWalletDevice = tupyWalletDevice;
    }

    public boolean isVAT() {
        return VAT;
    }

    public void setVAT(boolean VAT) {
        this.VAT = VAT;
    }


    @Override
    public String toString() {
        return String.format("%s (%s)", getEmp_name(), String.valueOf(getEmp_id()));
    }

    public RealmList<DinningTable> getAssignedDinningTables() {
        if(assignedDinningTables==null){
            assignedDinningTables = new RealmList<>();
        }
        return assignedDinningTables;
    }

    public void setAssignedDinningTables(RealmList<DinningTable> assignedDinningTables) {
        this.assignedDinningTables = assignedDinningTables;
    }
}
