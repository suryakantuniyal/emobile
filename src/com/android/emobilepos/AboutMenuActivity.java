package com.android.emobilepos;





import com.android.support.MyPreferences;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class AboutMenuActivity extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.about_layout, container, false);

		MyPreferences myPref = new MyPreferences(getActivity());
		TextView acctNumber = (TextView) view.findViewById(R.id.acctNum);
		TextView employee = (TextView) view.findViewById(R.id.employeeNameID);
		TextView version = (TextView) view.findViewById(R.id.versionID);

		StringBuilder sb = new StringBuilder();
		sb.append(myPref.getEmpName()).append(" (").append(myPref.getEmpID()).append(")");
		acctNumber.setText(myPref.getAcctNumber());
		employee.setText(sb.toString());
		version.setText(myPref.getBundleVersion());
		return view;

	}
}
