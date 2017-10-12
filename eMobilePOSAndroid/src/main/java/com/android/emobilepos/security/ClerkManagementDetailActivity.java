package com.android.emobilepos.security;

import android.app.Activity;
import android.os.Bundle;

import com.android.dao.ClerkDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.models.realms.EmobileBiometric;

import drivers.digitalpersona.DigitalPersona;
import interfaces.BiometricCallbacks;


public class ClerkManagementDetailActivity extends Activity implements BiometricCallbacks {
    int clerkId;
    private Clerk clerk;
    DigitalPersona digitalPersona;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clerk_management_detail);
        Bundle extras = getIntent().getExtras();
        clerkId = extras.getInt("clerkId", 0);
        clerk = ClerkDAO.getByEmpId(clerkId);
        digitalPersona = new DigitalPersona(this, this);
        digitalPersona.loadForEnrollment();
    }

    @Override
    public void biometricsWasRead(EmobileBiometric emobileBiometric) {

    }
}
