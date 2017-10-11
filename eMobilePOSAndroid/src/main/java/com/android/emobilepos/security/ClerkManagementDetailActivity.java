package com.android.emobilepos.security;

import android.app.Activity;
import android.os.Bundle;

import com.android.dao.ClerkDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.models.realms.Clerk;


public class ClerkManagementDetailActivity extends Activity {
    int clerkId;
    private Clerk clerk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clerk_management_detail);
        Bundle extras = getIntent().getExtras();
        clerkId = extras.getInt("clerkId", 0);
        clerk = ClerkDAO.getByEmpId(clerkId);

    }

}
