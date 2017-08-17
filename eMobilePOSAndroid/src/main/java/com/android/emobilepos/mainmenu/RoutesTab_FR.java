package com.android.emobilepos.mainmenu;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.emobilepos.R;

import java.util.Calendar;

public class RoutesTab_FR extends Fragment {

	private LinearLayout button;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.routes_layout, container, false);
		button = (LinearLayout) view.findViewById(R.id.dateButton);
		return view;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		button.setOnClickListener(new View.OnClickListener() 
		{

			@Override
			public void onClick(View v) {
											
				DialogFragment newFrag = new DateDialog();
				newFrag.show(getFragmentManager(), "dialog");
			}
		});

	}

	

	private void getDimensions()
	{
		 int density= getResources().getDisplayMetrics().densityDpi;

		 StringBuilder sb = new StringBuilder();
		 switch(density)
		 {
  		 case DisplayMetrics.DENSITY_LOW:
			 sb.append("ldpi - ");
		    
		     break;
		 case DisplayMetrics.DENSITY_MEDIUM:
		    sb.append("mdpi - ");
		     break;
		 case DisplayMetrics.DENSITY_HIGH:
		     sb.append("hdpi - ");
		     break;
		 case DisplayMetrics.DENSITY_XHIGH:
		      sb.append("xhpdi - ");
		     break;
		 }
		 int screenSize = getResources().getConfiguration().screenLayout &Configuration.SCREENLAYOUT_SIZE_MASK;

			switch(screenSize) {
			case Configuration.SCREENLAYOUT_SIZE_XLARGE:
				sb.append("xlarge");
				break;
			    case Configuration.SCREENLAYOUT_SIZE_LARGE:
			        sb.append("large");
			        break;
			    case Configuration.SCREENLAYOUT_SIZE_NORMAL:
			       sb.append("normal");
			        break;
			    case Configuration.SCREENLAYOUT_SIZE_SMALL:
			        sb.append("small");
			        break;
			    default:
			       sb.append("undefined");
			}
			
			Toast.makeText(getActivity(), sb.toString(), Toast.LENGTH_LONG).show();
	}
	
	
	public static class DateDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {
		public DateDialog() {

		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			return new DatePickerDialog(getActivity(), this, year, month, day);

		}

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

			// Do something after user selects the date...

		}
	}
}
