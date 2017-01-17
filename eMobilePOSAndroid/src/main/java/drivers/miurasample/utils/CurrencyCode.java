package drivers.miurasample.utils;

/**
 * Created by mgadzala on 30/06/2016.
 */
public enum CurrencyCode {

    GBP(826, "\u00a3"),
    USD(840, "\u0024"),
    EUR(978, "\u20ac"),
    PLN(985, "PLN");

    String euro = "\u20ac";

    String pound = "\u00a3";
    private int value;
    private String sign;

    CurrencyCode(int value, String sign) {
        this.value = value;
        this.sign = sign;
    }

    public int getValue() {
        return value;
    }

    public String getSign() {
        return sign;
    }
}
