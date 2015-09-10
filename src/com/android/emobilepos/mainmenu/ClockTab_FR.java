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

import com.android.database.ClerksHandler;
import com.emobilepos.app.R;
import com.android.emobilepos.shifts.ClockInOut_FA;
import com.android.support.MyPreferences;

public class ClockTab_FR extends Fragment implements OnClickListener
{
	private Activity activity;
	private EditText fieldPassword;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{

		View view = inflater.inflate(R.layout.clocks_parent_layout, container, false);

		Button submitButton = (Button)view.findViewById(R.id.clockSubmitButton);
		fieldPassword = (EditText) view.findViewById(R.id.clockPasswordField);
		activity = getActivity();
		submitButton.setOnClickListener(this);
		
		
		return view;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		MyPreferences myPref = new MyPreferences(activity);
		String enteredPass = fieldPassword.getText().toString().trim();
		ClerksHandler clerkHandler = new ClerksHandler(activity);
		String[] clerkData = clerkHandler.getClerkID(enteredPass);
		fieldPassword.setText("");
		if(clerkData!=null && !clerkData[0].isEmpty())
		{
			
			Intent intent = new Intent(activity, ClockInOut_FA.class);
			intent.putExtra("clerk_id", clerkData[0]);
			intent.putExtra("clerk_name", clerkData[1]);
			activity.startActivity(intent);
		}
		else
		{
			Toast.makeText(activity, R.string.invalid_password, Toast.LENGTH_LONG).show();
		}
	}
}
