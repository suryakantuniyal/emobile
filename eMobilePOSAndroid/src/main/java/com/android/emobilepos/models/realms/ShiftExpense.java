package com.android.emobilepos.models.realms;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by guarionex on 02-11-17.
 */

public class ShiftExpense extends RealmObject {
    public enum ExpenseProductId {
        SAFE_DROP(1), CASH_DROP(2), CASH_IN(3), BUY_GOODS_SERVICES(4), NON_CASH_GRATUITY(5);

        private int code;

        ExpenseProductId(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    @PrimaryKey
    private String expenseId;
    @Index
    private String shiftId;
    private Date creationDate;
    private String cashAmount;
    private int productId;
    private String productOption;
    private String productDescription;
    private String productName;

    public String getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(String expenseId) {
        this.expenseId = expenseId;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getCashAmount() {
        return cashAmount;
    }

    public void setCashAmount(String cashAmount) {
        this.cashAmount = cashAmount;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductOption() {
        return productOption;
    }

    public void setProductOption(String productOption) {
        this.productOption = productOption;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
