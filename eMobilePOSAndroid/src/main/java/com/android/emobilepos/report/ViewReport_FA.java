package com.android.emobilepos.report;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.DialogFragment;
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
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ViewReport_FA extends BaseFragmentActivityActionBar {

    private ReportsMenuAdapter mainAdapter;
    private ReportsShiftAdapter shiftAdapter;
    private ProgressDialog myProgressDialog;
    ListView myListview;
    private static String curDate;
    private static String[] dates = new String[2];
    private Button printBut;
    private static boolean isShiftReport = false;
    private Global global;
    private boolean hasBeenCreated = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_layout);
        global = (Global) this.getApplication();
        final Bundle extras = this.getIntent().getExtras();
        isShiftReport = extras.getBoolean("isShiftReport", false);
        Button dateBut = findViewById(R.id.changeDateButton);
        printBut = findViewById(R.id.reportPrintButton);
        myListview = findViewById(R.id.reportListView);
        TextView headerTitle = findViewById(R.id.headerTitle);
        dateBut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DialogFragment newFrag = new DateDialog();
                FragmentManager fm = getSupportFragmentManager();
                newFrag.show(fm, "dialog");
            }
        });
        if (isShiftReport) {
            headerTitle.setText(R.string.report_per_shift);
            myListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                                        long arg3) {
                    if (pos >= 2) {
                        Intent intent = new Intent(ViewReport_FA.this, ShiftReportDetails_FA.class);
                        intent.putExtra("shift_id", shiftAdapter.getShiftID(pos));
                        startActivity(intent);
                    }
                }
            });
        } else {
            headerTitle.setText(R.string.report_title);
        }
        hasBeenCreated = true;
        new initViewAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onResume() {

        if (global.isApplicationSentToBackground())
            Global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !Global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(this);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
//        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//        boolean isScreenOn = powerManager.isScreenOn();
//        MyPreferences myPref = new MyPreferences(this);
//        if (!isScreenOn && myPref.isExpireUserSession())
//            Global.loggedIn = false;
        global.startActivityTransitionTimer();
    }


    private class initViewAsync extends AsyncTask<Void, String, String> {
        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(ViewReport_FA.this);
            myProgressDialog.setMessage("Creating Report...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            curDate = DateUtils.getDateAsString(new Date(), DateUtils.DATE_yyyy_MM_ddTHH_mm_ss);
            dates[0] = Global.formatToDisplayDate(curDate, 0);
            dates[1] = Global.formatToDisplayDate(curDate, 4);
            if (isShiftReport) {
                shiftAdapter = new ReportsShiftAdapter(ViewReport_FA.this, dates);
            } else {
                mainAdapter = new ReportsMenuAdapter(ViewReport_FA.this, dates);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();
            if (!isShiftReport)
                myListview.setAdapter(mainAdapter);
            else
                myListview.setAdapter(shiftAdapter);
            if (isShiftReport) {
                printBut.setVisibility(View.GONE);
            } else {
                printBut.setVisibility(View.VISIBLE);
            }
            MyPreferences myPref = new MyPreferences(ViewReport_FA.this);
            if (myPref.getPreferences(MyPreferences.pref_enable_printing)) {
                printBut.setEnabled(true);
//                printBut.setBackgroundResource(R.drawable.tab_button_selector);
                printBut.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (!isShiftReport)
                            new printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                });
            } else {
                printBut.setEnabled(false);
//                printBut.setBackgroundResource(R.drawable.tab_disabled_button_selector);
            }
        }
    }

    private void showPrintDlg() {
        final Dialog dlog = new Dialog(this, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);

        viewTitle.setText(R.string.dlog_title_error);
        viewMsg.setText(R.string.dlog_msg_failed_print);

        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);

        Button btnYes = dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = dlog.findViewById(R.id.btnDlogRight);
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
        dlog.show();
    }

    private class printAsync extends AsyncTask<Void, Void, Void> {
        private boolean printSuccessful = true;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(ViewReport_FA.this);
            myProgressDialog.setMessage("Printing...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null)
                printSuccessful = Global.mainPrinterManager.getCurrentDevice().printReport(curDate);
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

        private ViewReport_FA context;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onAttach(Context context) {
            this.context = (ViewReport_FA) context;
            super.onAttach(context);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(context, this, year, month, day);

        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar cal = Calendar.getInstance();
            cal.set(year, monthOfYear, dayOfMonth);
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            curDate = sdf2.format(cal.getTime());
            dates[0] = Global.formatToDisplayDate(curDate, 0);
            dates[1] = Global.formatToDisplayDate(curDate, 4);
            if (!isShiftReport) {
                context.mainAdapter = new ReportsMenuAdapter(context, dates);
                context.myListview.setAdapter(context.mainAdapter);
            } else {
                context.shiftAdapter = new ReportsShiftAdapter(context, dates);
                context.myListview.setAdapter(context.shiftAdapter);
            }

        }
    }

}
