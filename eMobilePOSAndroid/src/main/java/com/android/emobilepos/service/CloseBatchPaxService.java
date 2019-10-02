package com.android.emobilepos.service;

import android.app.ProgressDialog;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentResolver;
import android.content.Context;

import com.android.support.MyPreferences;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import drivers.pax.utils.BatchProcessing;


public class CloseBatchPaxService extends JobService implements BatchProcessing.OnBatchProcessedCallback {
    private ProgressDialog myProgressDialog;


    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        DateFormat fmt = new SimpleDateFormat("MM/dd/yyyy");
        DateFormat fmtHHmmAMPM = new SimpleDateFormat("MM/dd/yyyy hh:mm a");

        Calendar calNow     = Calendar.getInstance();
        Calendar calStart   = Calendar.getInstance();
        Calendar calEnd     = Calendar.getInstance();
        calStart.add(Calendar.MINUTE,-7);
        calEnd.add(Calendar.MINUTE, 7);
        try{
            Context context = this;
            MyPreferences myPref = new MyPreferences(context);
            String hour = myPref.getPreferencesValue(MyPreferences.pref_pax_close_batch_hour);
            if(hour != null ){
                hour = hour.trim();
                if(hour.equals("")){
                    hour = "12:00 AM";
                }
            }
            String processDateString = fmt.format(calNow.getTime()) + " " + hour ;
            Date runDate   = fmtHHmmAMPM.parse(processDateString);
            Date startDate   = calStart.getTime();
            Date endDate     = calEnd.getTime();
            if(runDate.compareTo(startDate) >= 0 && runDate.compareTo(endDate) <= 0){
                BatchProcessing batchProcessing = new BatchProcessing(this, this.getApplicationContext() );
                batchProcessing.close();
            }
        }catch (Exception x){
            x.printStackTrace();
        }
	    return false;
    }
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
    @Override
    public void onBatchProcessedDone(String result) {
        CloseBatchPaxServiceHelper helper = new CloseBatchPaxServiceHelper();
        helper.insertIntoResults(getContentResolver(), result);
        getResults(getContentResolver());
    }
    private List<CloseBatchPaxResult> getResults(ContentResolver contentResolver){
        return new CloseBatchPaxServiceHelper().getResults(contentResolver, "DESC");
    }
}