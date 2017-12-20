package in.gtech.gogeotrack.activity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;

import in.gtech.gogeotrack.R;

/**
 * Created by surya on 13/11/17.
 */

public class GoGeoDataProDialog extends Dialog {
    private com.victor.loading.rotate.RotateLoading rotateLoading;
    public GoGeoDataProDialog(@NonNull Context context) {
        super(context);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        setContentView(R.layout.gogeo_pro_dialog);
        this.rotateLoading = (com.victor.loading.rotate.RotateLoading) findViewById(R.id.loading_spinner);
        this.rotateLoading.start();
    }
}
