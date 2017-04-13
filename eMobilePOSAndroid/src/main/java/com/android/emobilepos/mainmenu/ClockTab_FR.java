package com.android.emobilepos.mainmenu;

import android.app.Activity;
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
import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.security.SecurityManager;
import com.android.emobilepos.shifts.ClockInOut_FA;
import com.android.support.MyPreferences;

public class ClockTab_FR extends Fragment implements OnClickListener {
    private Activity activity;
    private EditText fieldPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.clocks_parent_layout, container, false);
        Button submitButton = (Button) view.findViewById(R.id.clockSubmitButton);
        fieldPassword = (EditText) view.findViewById(R.id.clockPasswordField);
        activity = getActivity();
        submitButton.setOnClickListener(this);
        boolean hasPermissions = SecurityManager.hasPermissions(getActivity(), SecurityManager.SecurityAction.TIME_CLOCK);
        submitButton.setEnabled(hasPermissions);
        return view;
    }

    @Override
    public void onClick(View v) {
        String enteredPass = fieldPassword.getText().toString().trim();
        Clerk clerk = ClerkDAO.login(enteredPass, new MyPreferences(getActivity()));
        fieldPassword.setText("");
        if (clerk != null) {
            Intent intent = new Intent(activity, ClockInOut_FA.class);
            intent.putExtra("clerk_id", clerk.getEmpId());
            intent.putExtra("clerk_name", clerk.getEmpName());
            activity.startActivity(intent);
        } else {
            Toast.makeText(activity, R.string.invalid_password, Toast.LENGTH_LONG).show();
        }
    }
}
