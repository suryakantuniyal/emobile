package interfaces;

import com.android.emobilepos.models.OrderSeatProduct;

/**
 * Created by guarionex on 7/18/17.
 */

public interface PayWithLoyalty {
    void processPayWithLoyalty(OrderSeatProduct orderSeatProduct);
}
