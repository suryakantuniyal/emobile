package util;

import java.math.BigDecimal;

/**
 * Created by Luis Camayd on 12/14/2018.
 */
public class MoneyUtils {

    public static int convertDollarsToCents(String amount) {
        try {
            return new BigDecimal(amount).movePointRight(2).intValueExact();
        } catch (Exception e) {
            // TODO: validate scenario
            return 0;
        }
    }

    public static int convertDollarsToCents(BigDecimal amount) {
        try {
            return convertDollarsToCents(String.valueOf(amount));
        } catch (Exception e) {
            // TODO: validate scenario
            return 0;
        }
    }

    public static BigDecimal convertCentsToDollars(String amount) {
        try {
            return new BigDecimal(amount).movePointLeft(2);
        } catch (Exception e) {
            // TODO: validate scenario
            return BigDecimal.ZERO;
        }
    }

    public static BigDecimal convertCentsToDollars(int amount) {
        try {
            return convertCentsToDollars(String.valueOf(new BigDecimal(amount)));
        } catch (Exception e) {
            // TODO: validate scenario
            return BigDecimal.ZERO;
        }
    }
}