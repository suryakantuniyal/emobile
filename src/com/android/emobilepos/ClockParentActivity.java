package com.android.emobilepos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.database.ClerksHandler;
import com.android.support.MyPreferences;

public class ClockParentActivity extends Fragment
{
	private Activity activity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{

		View view = inflater.inflate(R.layout.clocks_parent_layout, container, false);

		Button submitButton = (Button)view.findViewById(R.id.clockSubmitButton);
		final EditText passwordField = (EditText) view.findViewById(R.id.clockPasswordField);
		activity = getActivity();
		
		submitButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				MyPreferences myPref = new MyPreferences(activity);
				String enteredPass = passwordField.getText().toString().trim();
				ClerksHandler clerkHandler = new ClerksHandler(activity);
				String clerkID = clerkHandler.getClerkID(enteredPass);
				if(!clerkID.isEmpty())
				{
					passwordField.setText("");
					myPref.setClerkID(clerkID);
					Intent intent = new Intent(activity, ClockChildActivity.class);
					activity.startActivity(intent);
				}
				else
				{
					passwordField.setText("");
					Toast.makeText(activity, R.string.invalid_password, Toast.LENGTH_LONG).show();
				}
			}
		});
		
		return view;
	}
}
