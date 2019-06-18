package drivers.pax.utils;

/**
 * Created by Luis Camayd on 3/21/2019.
 */
public class Constant {
    public static final int REQUEST_TENDER_TYPE_CREDIT = 1; // request tender type credit
    public static final int REQUEST_TENDER_TYPE_DEBIT = 2; // request tender type debit
    public static final int TRANSACTION_TYPE_SALE = 2; // request tender type debit
    public static final int TRANSACTION_TYPE_RETURN = 3; // request tender type debit
    public static final String TRANSACTION_SUCCESS = "000000"; // transaction success
    public static final String TRANSACTION_DECLINED = "000100"; // transaction declined
    public static final String TRANSACTION_TIMEOUT = "100001"; // transaction timeout
    public static final String TRANSACTION_CANCELED = "100002"; // transaction failure
    public static final String CARD_EXPIRED = "100003"; // transaction failure
    public static final String HAS_VOIDED = "100021"; // transaction already voided
}