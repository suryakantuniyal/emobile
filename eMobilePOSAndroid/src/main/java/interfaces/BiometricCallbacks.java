package interfaces;

import com.android.emobilepos.models.realms.EmobileBiometric;

public interface BiometricCallbacks {
    void biometricsWasRead(EmobileBiometric emobileBiometric);
}
