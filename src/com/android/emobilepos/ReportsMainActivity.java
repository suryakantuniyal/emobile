package com.android.emobilepos;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;





public class ReportsMainActivity extends Fragment implements OnClickListener
{
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.reports_main_layout, container, false);
		Button btnDaySummary = (Button)view.findViewById(R.id.btnReportDaySummary);
		Button btnPerShift = (Button ) view.findViewById(R.id.btnReportPerShift);
		btnDaySummary.setOnClickListener(this);
		btnPerShift.setOnClickListener(this);
		return view;

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent= new Intent(getActivity(),ReportsMenuActivity.class);
		switch(v.getId())
		{
		case R.id.btnReportDaySummary:
			intent.putExtra("isShiftReport", false);
			break;
		case R.id.btnReportPerShift:
			intent.putExtra("isShiftReport", true);
			break;
		}
		startActivity(intent);
	}
}
