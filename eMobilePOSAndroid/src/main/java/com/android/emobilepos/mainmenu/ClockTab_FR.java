package com.android.emobilepos.mainmenu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.dao.ClerkDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.customer.ViewCustomerDetails_FA;
import com.android.emobilepos.models.realms.BiometricFid;
import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.models.realms.EmobileBiometric;
import com.android.emobilepos.security.SecurityManager;
import com.android.emobilepos.shifts.ClockInOut_FA;
import com.android.support.MyPreferences;

import drivers.digitalpersona.DigitalPersona;
import interfaces.BiometricCallbacks;

public class ClockTab_FR extends Fragment implements OnClickListener, BiometricCallbacks {
    DigitalPersona digitalPersona;
    private EditText fieldPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.clocks_parent_layout, container, false);
        Button submitButton = (Button) view.findViewById(R.id.clockSubmitButton);
        fieldPassword = (EditText) view.findViewById(R.id.clockPasswordField);
        submitButton.setOnClickListener(this);
        boolean hasPermissions = SecurityManager.hasPermissions(getActivity(), SecurityManager.SecurityAction.TIME_CLOCK);
        submitButton.setEnabled(hasPermissions);
        digitalPersona = new DigitalPersona(getActivity(), this, EmobileBiometric.UserType.CLERK);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        digitalPersona.loadForScan();
    }

    @Override
    public void onClick(View v) {
        String enteredPass = fieldPassword.getText().toString().trim();
        Clerk clerk = ClerkDAO.login(enteredPass, new MyPreferences(getActivity()), false);
        fieldPassword.setText("");
        if (clerk != null) {
            Intent intent = new Intent(getActivity(), ClockInOut_FA.class);
            intent.putExtra("clerk_id", clerk.getEmpId());
            intent.putExtra("clerk_name", clerk.getEmpName());
            getActivity().startActivity(intent);
        } else {
            Toast.makeText(getActivity(), R.string.invalid_password, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void biometricsWasRead(EmobileBiometric emobileBiometric) {
        Clerk clerk = ClerkDAO.getByEmpId(Integer.parseInt(emobileBiometric.getEntityid()));
        if (clerk != null) {
            clerk = ClerkDAO.login(clerk.getEmpPwd(), new MyPreferences(getActivity()), false);
            if (clerk != null) {
                Intent intent = new Intent(getActivity(), ClockInOut_FA.class);
                intent.putExtra("clerk_id", clerk.getEmpId());
                intent.putExtra("clerk_name", clerk.getEmpName());
                getActivity().startActivity(intent);
            } else {
                Toast.makeText(getActivity(), R.string.invalid_password, Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        digitalPersona.releaseReader();
    }

    @Override
    public void biometricsReadNotFound() {

    }

    @Override
    public void biometricsWasEnrolled(BiometricFid biometricFid) {

    }

    @Override
    public void biometricsDuplicatedEnroll(EmobileBiometric emobileBiometric, BiometricFid biometricFid) {

    }

    @Override
    public void biometricsUnregister(ViewCustomerDetails_FA.Finger finger) {

    }
}
