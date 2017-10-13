package interfaces;

import com.android.emobilepos.customer.ViewCustomerDetails_FA;
import com.android.emobilepos.models.realms.BiometricFid;
import com.android.emobilepos.models.realms.EmobileBiometric;

public interface BiometricCallbacks {
    void biometricsWasRead(EmobileBiometric emobileBiometric);
    void biometricsWasEnrolled(BiometricFid biometricFid);
    void biometricsDuplicatedEnroll(BiometricFid biometricFid);
    void biometricsUnregister(ViewCustomerDetails_FA.Finger finger);

}
