package drivers.pax.utils;

/**
 * Created by Luis Camayd on 3/21/2019.
 */
public class Constant {
    public static final int REQUEST_TENDER_TYPE_CREDIT = 1;
    public static final int REQUEST_TENDER_TYPE_DEBIT = 2;
    public static final int REQUEST_TRANSACTION_TYPE_SALE = 2;
    public static final int REQUEST_TRANSACTION_TYPE_RETURN = 3;
    public static final int REQUEST_TRANSACTION_TYPE_VOID = 4;
    public static final String TRANSACTION_SUCCESS = "000000";
    public static final String TRANSACTION_DECLINED = "000100";
    public static final String TRANSACTION_TIMEOUT = "100001";
    public static final String TRANSACTION_CANCELED = "100002";
    public static final String CARD_EXPIRED = "100003";
    public static final String HAS_VOIDED = "100021";
    public static final String TRANS_NOT_FOUND = "100023";
}