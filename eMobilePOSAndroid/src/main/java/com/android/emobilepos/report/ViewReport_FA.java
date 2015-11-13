package com.android.emobilepos.report;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.android.emobilepos.R;
import com.android.emobilepos.adapters.ReportsMenuAdapter;
import com.android.emobilepos.adapters.ReportsShiftAdapter;
import com.android.emobilepos.shifts.ShiftReportDetails_FA;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ViewReport_FA extends FragmentActivity {
	
	private static ReportsMenuAdapter mainAdapter;
	private static ReportsShiftAdapter shiftAdapter;
	private ProgressDialog myProgressDialog;
	private static ListView myListview;
	private static String curDate;
	private static Activity activity;
	private static String [] dates = new String[2];
	private Button dateBut,printBut;
	private static boolean isShiftReport = false;
	private Global global;
	private boolean hasBeenCreated = false;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.report_layout);
		activity = this;
		
		global = (Global)activity.getApplication();
		final Bundle extras =activity.getIntent().getExtras();
		
		isShiftReport = extras.getBoolean("isShiftReport",false);
		
		dateBut = (Button)findViewById(R.id.changeDateButton);
		printBut = (Button)findViewById(R.id.reportPrintButton);
		myListview = (ListView) findViewById(R.id.reportListView);
		TextView headerTitle = (TextView) findViewById(R.id.headerTitle);
		
		dateBut.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DialogFragment newFrag = new DateDialog();
				FragmentManager fm = getSupportFragmentManager();
				newFrag.show(fm, "dialog");
				/*EMSReceiptHelper em = new EMSReceiptHelper(activity,42);
				String t = em.getEndOfDayReportReceipt(null,Global.getCurrentDate());
				t = "";*/
			}
		});
		
		
		if(isShiftReport)
		{
			headerTitle.setText(R.string.report_per_shift);
			myListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
						long arg3) {
					// TODO Auto-generated method stub
					
					if(pos>=2)
					{
					Intent intent = new Intent(activity,ShiftReportDetails_FA.class);
					intent.putExtra("shift_id", shiftAdapter.getShiftID(pos));
					startActivity(intent);
					}
				}
			});
		}
		else
		{
			headerTitle.setText(R.string.report_title);
		}
		
		hasBeenCreated = true;
		new initViewAsync().execute();
	}

	
	@Override
	public void onResume() {

		if(global.isApplicationSentToBackground(this))
			global.loggedIn = false;
		global.stopActivityTransitionTimer();
		
		if(hasBeenCreated&&!global.loggedIn)
		{
			if(global.getGlobalDlog()!=null)
				global.getGlobalDlog().dismiss();
			global.promptForMandatoryLogin(this);
		}
		super.onResume();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
		boolean isScreenOn = powerManager.isScreenOn();
		if(!isScreenOn)
			global.loggedIn = false;
		global.startActivityTransitionTimer();
	}
	
	
	private class initViewAsync extends AsyncTask<Void, String, String> 
	{
		@Override
		protected void onPreExecute() {
			
			myProgressDialog = new ProgressDialog(activity);
			myProgressDialog.setMessage("Creating Report...");
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			myProgressDialog.show();

		}

		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub

			curDate = Global.getCurrentDate();
						
			dates[0]=Global.formatToDisplayDate(curDate, activity, 0);
			dates[1] = Global.formatToDisplayDate(curDate, activity, 4);
			
			if(!isShiftReport)
				mainAdapter = new ReportsMenuAdapter(activity, dates);
			else
				shiftAdapter = new ReportsShiftAdapter(activity,dates);
			
			return null;
		}

		@Override
		protected void onPostExecute(String unused) {
			myProgressDialog.dismiss();
			if(!isShiftReport)
				myListview.setAdapter(mainAdapter);
			else
				myListview.setAdapter(shiftAdapter);
			
			MyPreferences myPref = new MyPreferences(activity);
			if(myPref.getPreferences(MyPreferences.pref_enable_printing))
			{
				printBut.setBackgroundResource(R.drawable.tab_button_selector);
				printBut.setOnClickListener(new View.OnClickListener() {
		
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if(!isShiftReport)
							new printAsync().execute();
					}
				});
			}
			else
			{
				printBut.setBackgroundResource(R.drawable.tab_disabled_button_selector);
			}
		}
	}
	
	private void showPrintDlg() {
		final Dialog dlog = new Dialog(activity,R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setCancelable(false);
		dlog.setContentView(R.layout.dlog_btn_left_right_layout);
		
		TextView viewTitle = (TextView)dlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)dlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(R.string.dlog_title_confirm);

		viewTitle.setText(R.string.dlog_title_error);
		viewMsg.setText(R.string.dlog_msg_failed_print);

		
		Button btnYes = (Button)dlog.findViewById(R.id.btnDlogLeft);
		Button btnNo = (Button)dlog.findViewById(R.id.btnDlogRight);
		btnYes.setText(R.string.button_yes);
		btnNo.setText(R.string.button_no);
		
		btnYes.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				new printAsync().execute();
			}
		});
		btnNo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
			}
		});
		dlog.show();
	}
	
	private class printAsync extends AsyncTask<Void, Void, Void> 
	{
		private boolean printSuccessful = true;
		@Override
		protected void onPreExecute() {
			myProgressDialog = new ProgressDialog(activity);
			myProgressDialog.setMessage("Printing...");
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			myProgressDialog.show();

		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub

			if(Global.mainPrinterManager!=null&&Global.mainPrinterManager.currentDevice!=null)
				printSuccessful = Global.mainPrinterManager.currentDevice.printReport(curDate);
			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			myProgressDialog.dismiss();
			if(!printSuccessful)
				showPrintDlg();
			
		}
	}
	
	
	public static class DateDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {

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

			return new DatePickerDialog(activity, this, year, month, day);

		}

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

			// Do something after user selects the date...
			StringBuilder sb = new StringBuilder();
			sb.append(Integer.toString(year)).append(Integer.toString(monthOfYear+1)).append(Integer.toString(dayOfMonth));
			Calendar cal = Calendar.getInstance();
			cal.set(year, monthOfYear, dayOfMonth);
			
			
			
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.getDefault());
			
			curDate = sdf2.format(cal.getTime());
			
			dates[0]=Global.formatToDisplayDate(curDate, activity, 0);
			dates[1] = Global.formatToDisplayDate(curDate, activity, 4);
			if(activity!=null)
			{
				if(!isShiftReport)
				{
					mainAdapter = new ReportsMenuAdapter(activity, dates);
					myListview.setAdapter(mainAdapter);
				}
				else
				{
					shiftAdapter = new ReportsShiftAdapter(activity, dates);
					myListview.setAdapter(shiftAdapter);
				}
			}
			
		}
	}

}
