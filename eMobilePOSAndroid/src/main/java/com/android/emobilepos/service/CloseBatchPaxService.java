package com.android.emobilepos.service;

import android.app.ProgressDialog;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import com.android.emobilepos.R;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
        calStart.add(Calendar.MINUTE,-5);
        calEnd.add(Calendar.MINUTE, 5);
        try{
            Context context = this;
            MyPreferences myPref = new MyPreferences(context);
            String processDateString = fmt.format(calNow.getTime()) + " " + myPref.getPreferencesValue(MyPreferences.pref_pax_close_batch_hour);
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
        System.out.println("/********************************************************/");
        System.out.println(" onBatchProcessedDone: "+result+" <<<<<<<<<<<");
        System.out.println("/********************************************************/");
    }
}