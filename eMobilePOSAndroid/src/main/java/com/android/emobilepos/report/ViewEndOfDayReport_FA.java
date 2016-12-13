package com.android.emobilepos.report;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.android.emobilepos.R;
import com.android.emobilepos.adapters.ReportEndDayAdapter;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class ViewEndOfDayReport_FA extends BaseFragmentActivityActionBar implements OnClickListener {


    private static String curDate, mDate;
    private Activity activity;
    private static Button btnDate;
    private Global global;
    private ProgressDialog myProgressDialog;
    private static ReportEndDayAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_end_day_layout);
        activity = this;
        global = (Global) activity.getApplication();
        curDate = DateUtils.getDateAsString(new Date(), DateUtils.DATE_yyyy_MM_ddTHH_mm_ss);
        btnDate = (Button) findViewById(R.id.btnDate);
        Button btnPrint = (Button) findViewById(R.id.btnPrint);
        btnDate.setOnClickListener(this);
        btnPrint.setOnClickListener(this);
        mDate = Global.formatToDisplayDate(curDate, activity, 0);
        btnDate.setText(mDate);
        StickyListHeadersListView myListview = (StickyListHeadersListView) findViewById(R.id.listView);
        myListview.setAreHeadersSticky(false);
        adapter = new ReportEndDayAdapter(this, Global.formatToDisplayDate(curDate, activity, 4), null);
        myListview.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        if (global.isApplicationSentToBackground(this))
            global.loggedIn = false;
        global.stopActivityTransitionTimer();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        if (!isScreenOn)
            global.loggedIn = false;
        global.startActivityTransitionTimer();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnDate:
                DialogFragment newFrag = new DateDialog();
                FragmentManager fm = getSupportFragmentManager();
                newFrag.show(fm, "dialog");
                break;
            case R.id.btnPrint:
                showPrintDetailsDlg();
                break;
        }
    }

    private void showPrintDlg() {
        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(true);
        dlog.setCanceledOnTouchOutside(true);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);
        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        viewTitle.setText(R.string.dlog_title_error);
        viewMsg.setText(R.string.dlog_msg_failed_print);
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);
        Button btnYes = (Button) dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = (Button) dlog.findViewById(R.id.btnDlogRight);
        Button btnCancel = (Button) dlog.findViewById(R.id.btnDlogCancel);
        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);

        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                new printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlog.dismiss();
            }
        });
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dlog.dismiss();
            }
        });
        dlog.show();
    }

    private void showPrintDetailsDlg() {
        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);
        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_print_details);
        viewMsg.setText(R.string.dlog_msg_print_details);
        Button btnYes = (Button) dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = (Button) dlog.findViewById(R.id.btnDlogRight);
        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);

        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                new printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                new printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false);
            }
        });
        dlog.show();
    }

    private class printAsync extends AsyncTask<Boolean, Void, Void> {
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
        protected Void doInBackground(Boolean... params) {
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null)
                Global.mainPrinterManager.getCurrentDevice().printEndOfDayReport(curDate, null, params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            myProgressDialog.dismiss();
            if (!printSuccessful)
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
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            // Do something after user selects the date...
            StringBuilder sb = new StringBuilder();
            sb.append(Integer.toString(year)).append(Integer.toString(monthOfYear + 1)).append(Integer.toString(dayOfMonth));
            Calendar cal = Calendar.getInstance();
            cal.set(year, monthOfYear, dayOfMonth);
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            curDate = sdf2.format(cal.getTime());
            adapter.setNewDate(Global.formatToDisplayDate(curDate, getActivity(), 4));
            mDate = Global.formatToDisplayDate(curDate, getActivity(), 0);
            btnDate.setText(mDate);
        }
    }

}
