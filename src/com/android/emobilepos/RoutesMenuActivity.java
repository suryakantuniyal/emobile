package com.android.emobilepos;

import java.util.Calendar;



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

import com.android.database.ConsignmentTransactionHandler;
import com.android.database.CustomerInventoryHandler;
import com.android.database.InvoicePaymentsHandler;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.database.ShiftPeriodsDBHandler;
import com.android.database.TimeClockHandler;
import com.android.support.DBManager;

public class RoutesMenuActivity extends Fragment {

	private LinearLayout button;
	private Activity activity;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.routes_layout, container, false);
		activity = getActivity();
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
				// TODO Auto-generated method stub
				
				//getDimensions();
			
//				DBManager manag = new DBManager(getActivity());
//				manag.dbBackupDB();
//				
		        /*Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		        sharingIntent.setType("vnd.android.cursor.dir/email");      
		        sharingIntent.setType("application/octet-stream");
		        //CHANGE: using correct path:
		        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "my email subject");
		        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+manag.dbBackupDB()));
		        startActivity(Intent.createChooser(sharingIntent, "Send email"));  
				
				
				*/
				
				
				/*
				CustomerInventoryHandler custInventoryHandler = new CustomerInventoryHandler(activity);
				custInventoryHandler.emptyTable();
				ConsignmentTransactionHandler te = new ConsignmentTransactionHandler(activity);
				te.emptyTable();*/
				
				
//				OrdersHandler h1 = new OrdersHandler(activity);
//				OrderProductsHandler h2 = new OrderProductsHandler(activity);
//				PaymentsHandler h3 = new PaymentsHandler(activity);
//				TimeClockHandler h4 = new TimeClockHandler(activity);
//				ShiftPeriodsDBHandler h5 = new ShiftPeriodsDBHandler(activity);
//				InvoicePaymentsHandler h6 = new InvoicePaymentsHandler(activity);
//				h1.emptyTable();
//				h2.emptyTable();
//				h3.emptyTable();
//				h4.emptyTable();
//				h5.emptyTable();
//				h6.emptyTable();
				
				

				
				
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
			
			Toast.makeText(activity, sb.toString(), Toast.LENGTH_LONG).show();
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
