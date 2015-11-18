package com.android.emobilepos.report;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.android.emobilepos.adapters.ReportEndDayAdapter;
import com.android.support.Global;
import com.emobilepos.app.R;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class ViewEndOfDayReport_FA  extends FragmentActivity implements OnClickListener{
	

	private StickyListHeadersListView myListview;
	private String curDate, mDate;
	private Activity activity;
	
	private Button btnDate,btnPrint;

	private Global global;
	private boolean hasBeenCreated = false;
	private ProgressDialog myProgressDialog;
	private ReportEndDayAdapter adapter;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.report_end_day_layout);
		activity = this;
		global = (Global)activity.getApplication();
		curDate = Global.getCurrentDate();
		btnDate = (Button)findViewById(R.id.btnDate);
		btnPrint = (Button)findViewById(R.id.btnPrint);
		btnDate.setOnClickListener(this);
		btnPrint.setOnClickListener(this);
		
		mDate = Global.formatToDisplayDate(curDate, activity, 0);
		btnDate.setText(mDate);
		myListview = (StickyListHeadersListView) findViewById(R.id.listView);
		myListview.setAreHeadersSticky(false);
		adapter = new ReportEndDayAdapter(this,Global.formatToDisplayDate(curDate, activity, 4),null);
		myListview.setAdapter(adapter);
		
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


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.btnDate:
			DialogFragment newFrag = new DateDialog();
			FragmentManager fm = getSupportFragmentManager();
			newFrag.show(fm, "dialog");
			break;
		case R.id.btnPrint:
			new printAsync().execute();
			break;
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
				Global.mainPrinterManager.currentDevice.printEndOfDayReport(curDate, null);
			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			myProgressDialog.dismiss();
			if(!printSuccessful)
				showPrintDlg();
			
		}
	}
	
	
	public  class DateDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {

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
			adapter.setNewDate(Global.formatToDisplayDate(curDate, activity, 4));
			mDate = Global.formatToDisplayDate(curDate, activity, 0);
			btnDate.setText(mDate);
			
//			dates[0]=Global.formatToDisplayDate(curDate, activity, 0);
//			dates[1] = Global.formatToDisplayDate(curDate, activity, 4);
//			if(activity!=null)
//			{
//				if(!isShiftReport)
//				{
//					mainAdapter = new ReportsMenuAdapter(activity, dates);
//					myListview.setAdapter(mainAdapter);
//				}
//				else
//				{
//					shiftAdapter = new ReportsShiftAdapter(activity, dates);
//					myListview.setAdapter(shiftAdapter);
//				}
//			}
			
		}
	}
	
}
