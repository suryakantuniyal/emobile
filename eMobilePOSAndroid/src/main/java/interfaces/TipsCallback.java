package interfaces;

import android.app.AlertDialog;
import android.widget.TextView;

public interface TipsCallback {

    void noneTipGratuityWasPressed(TextView totalAmountView, TextView dlogGrandTotal, double subTotal);

    void cancelTipGratuityWasPressed(AlertDialog dialog);

    void saveTipGratuityWasPressed(AlertDialog dialog, double amountToTip);

}
