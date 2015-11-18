package com.android.emobilepos.mainmenu;

import com.android.emobilepos.report.ViewEndOfDayReport_FA;
import com.android.emobilepos.report.ViewReport_FA;
import com.emobilepos.app.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class ReportTab_FR extends Fragment implements OnClickListener
{
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.reports_main_layout, container, false);
		Button btnDaySummary = (Button)view.findViewById(R.id.btnReportDaySummary);
		Button btnPerShift = (Button ) view.findViewById(R.id.btnReportPerShift);
		Button btnEndOfDay = (Button)view.findViewById(R.id.btnEndOfDay);
		btnDaySummary.setOnClickListener(this);
		btnPerShift.setOnClickListener(this);
		btnEndOfDay.setOnClickListener(this);
		return view;

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent= new Intent(getActivity(),ViewReport_FA.class);
		switch(v.getId())
		{
		case R.id.btnReportDaySummary:
			intent.putExtra("isShiftReport", false);
			break;
		case R.id.btnReportPerShift:
			intent.putExtra("isShiftReport", true);
			break;
		case R.id.btnEndOfDay:
			intent = new Intent(getActivity(),ViewEndOfDayReport_FA.class);
			
			break;
		}
		startActivity(intent);
	}
}
