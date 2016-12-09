package com.android.emobilepos.shifts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.database.TimeClockHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.TimeClock;
import com.android.saxhandler.SAXPostHandler;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NetworkUtils;
import com.android.support.Post;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class ClockInOut_FA extends FragmentActivity implements OnClickListener {
    private Global global;
    private Activity activity;
    private boolean hasBeenCreated = false;
    private ProgressDialog myProgressDialog;
    private SAXPostHandler handler;
    private TextView clerkName, clockType, clockDateTime;
    private Button clockIn, clockOut;
    private boolean clockOutOn = false;
    private boolean clockInOn = false;
    private TimeClockHandler timeClockHandler;
    private MyPreferences myPref;
    private long hourDifference = 0;
    private List<TimeClock> listTimeClock;
    private AlertDialog.Builder adb;
    private Dialog promptDialog;
    private boolean validPassword = true;
    private String mClerkName = "", mClerkID = "";
    private boolean fromOnClick = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.clock_child_layout);

        activity = this;
        myPref = new MyPreferences(activity);
        global = (Global) getApplication();

        Bundle extras = getIntent().getExtras();
        mClerkName = extras.getString("clerk_name");
        mClerkID = extras.getString("clerk_id");

        listTimeClock = new ArrayList<TimeClock>();
        timeClockHandler = new TimeClockHandler(activity);

        TextView clockTodayDate = (TextView) findViewById(R.id.topDate);
        clockTodayDate.setText(Global.formatToDisplayDate(DateUtils.getDateAsString(new Date(), DateUtils.DATE_yyyy_MM_ddTHH_mm_ss),  0));
        clerkName = (TextView) findViewById(R.id.clockClerkNameID);
        clockType = (TextView) findViewById(R.id.clockType);
        clockDateTime = (TextView) findViewById(R.id.clockDateTime);
        clockIn = (Button) findViewById(R.id.clockIn);
        clockOut = (Button) findViewById(R.id.clockOut);

        StringBuilder sb = new StringBuilder();
        sb.append(mClerkName).append(" (").append(mClerkID).append(")");

        clerkName.setText(sb.toString());

        hasBeenCreated = true;

        clockOut.setOnClickListener(this);
        clockIn.setOnClickListener(this);

        new sendUnsyncTimeClock().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);

        // if(Global.isConnectedToInternet(activity))
        // {
        //
        // new sendUnsyncTimeClock().execute(true);
        // }
        // else
        // {
        // new getLastUpdate().execute();
        // }
    }

    @Override
    public void onResume() {

        if (global.isApplicationSentToBackground(activity))
            global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(activity);
        }
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
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.clockIn:
                if (clockInOn) {
                    // myPref.setClerkID(mClerkID);
                    // myPref.setClerkName(mClerkName);
                    switchButton(false);
                    listTimeClock.add(createTimeClock(false, DateUtils.getDateAsString(new Date(), DateUtils.DATE_yyyy_MM_ddTHH_mm_ss)));
                    timeClockHandler.insert(listTimeClock, false);
                    listTimeClock.clear();
                    new sendUnsyncTimeClock().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false);
                }

                break;
            case R.id.clockOut:
                if (clockOutOn && hourDifference <= 12) {
                    // myPref.setClerkID("");
                    // myPref.setClerkName("");
                    switchButton(true);
                    listTimeClock.add(createTimeClock(true,DateUtils.getDateAsString(new Date(), DateUtils.DATE_yyyy_MM_ddTHH_mm_ss)));
                    timeClockHandler.insert(listTimeClock, false);
                    listTimeClock.clear();
                    new sendUnsyncTimeClock().execute(false);
                } else {
                    promptDateTime();
                }
                break;
        }
    }

    private TimeClock createTimeClock(boolean isOut, String date) {
        TimeClock tempTC = new TimeClock();

        String randomUUIDString = UUID.randomUUID().toString();
        tempTC.timeclockid = randomUUIDString;
        tempTC.emp_id = mClerkID;
        tempTC.punchtime = date;
        if (isOut)
            tempTC.status = "OUT";
        else
            tempTC.status = "IN";

        return tempTC;
    }

    public class sendUnsyncTimeClock extends AsyncTask<Boolean, Void, Void> {
        boolean receiveTimeClock = false;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Sending...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected Void doInBackground(Boolean... params) {
            // TODO Auto-generated method stub

            // DBManager dbManager = new DBManager(activity);
            // SQLiteDatabase db = dbManager.openWritableDB();
            receiveTimeClock = params[0];
            if (timeClockHandler.getNumUnsyncTimeClock() > 0 && NetworkUtils.isConnectedToInternet(activity)) {
                publishProgress();
                Post httpClient = new Post();
                SAXParserFactory spf = SAXParserFactory.newInstance();
                handler = new SAXPostHandler();
                try {

                    String xml = httpClient.postData(Global.S_SUBMIT_TIME_CLOCK, activity, null);

                    InputSource inSource = new InputSource(new StringReader(xml));

                    SAXParser sp = spf.newSAXParser();
                    XMLReader xr = sp.getXMLReader();
                    xr.setContentHandler(handler);
                    xr.parse(inSource);

                    int size = handler.getSize();
                    if (size > 0) {

                        for (int i = 0; i < size; i++) {
                            if (!handler.getData("timeclockid", i).isEmpty())
                                timeClockHandler.updateIsSync(handler.getData("timeclockid", i),
                                        handler.getData("status", i));
                        }

                        // db.close();
                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
//					Tracker tracker = EasyTracker.getInstance(activity);
//					tracker.send(MapBuilder.createException(e.getStackTrace().toString(), false).build());
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void n) {
            myProgressDialog.dismiss();
            if (receiveTimeClock)
                new getLastUpdate().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                activity.finish();
        }

    }

    public class getLastUpdate extends AsyncTask<Void, String, String> {
        String errorMsg = "";
        boolean timedOut = false;
        boolean isOut = false;
        boolean isValid = false;
        String outStringDate = "", inStringDate = "";
        boolean statusOut = false, statusIn = false;
        Date dateOutIn[];

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Downloading...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO Auto-generated method stub

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            sdf2.setTimeZone(TimeZone.getDefault());
            // SimpleDateFormat sdf3 = new
            // SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz",Locale.getDefault());
            // sdf3.setTimeZone(TimeZone.getDefault());
            List<TimeClock> listTC = new ArrayList<TimeClock>();
            dateOutIn = new Date[2];
            if (NetworkUtils.isConnectedToInternet(activity)) {
                Post httpClient = new Post();

                SAXParserFactory spf = SAXParserFactory.newInstance();
                handler = new SAXPostHandler();

                try {

                    String xml = httpClient.postData(Global.S_GET_TIME_CLOCK, activity, null);

                    if (xml.equals(Global.TIME_OUT)) {
                        errorMsg = "TIME OUT, would you like to try again?";
                        timedOut = true;
                    } else if (xml.equals(Global.NOT_VALID_URL)) {
                        errorMsg = "Can not proceed...";
                    } else {
                        InputSource inSource = new InputSource(new StringReader(xml));

                        SAXParser sp = spf.newSAXParser();
                        XMLReader xr = sp.getXMLReader();
                        xr.setContentHandler(handler);
                        xr.parse(inSource);
                        int size = handler.getSize();
                        if (size > 0) {
                            isValid = true;

                            TimeClock tempTC = new TimeClock();

                            for (int i = 0; i < size; i++) {
                                tempTC.timeclockid = handler.getData("timeclockid", i);
                                tempTC.emp_id = handler.getData("emp_id", i);
                                tempTC.status = handler.getData("status", i);
                                tempTC.punchtime = handler.getData("punchtime", i);
                                tempTC.updated = handler.getData("updated", i);
                                tempTC.issync = "1";

                                if (handler.getData("status", i).equals("OUT")) {
                                    statusOut = true;

                                    if (!handler.getData("punchtime", i).contains("Z")) {
                                        outStringDate = handler.getData("punchtime", i).substring(0, 19);
                                        dateOutIn[0] = sdf2.parse(outStringDate);
                                    } else {
                                        outStringDate = handler.getData("punchtime", i).replace("Z", "");
                                        dateOutIn[0] = sdf.parse(outStringDate);
                                        outStringDate = sdf2.format(dateOutIn[0]);
                                    }

                                    // outStringDate =
                                    // handler.getData("punchtime",
                                    // i).replace("Z", "");
                                    // dateOutIn[0] = sdf.parse(outStringDate);
                                    // outStringDate =
                                    // sdf2.format(dateOutIn[0]);
                                } else {
                                    statusIn = true;

                                    if (!handler.getData("punchtime", i).contains("Z")) {
                                        inStringDate = handler.getData("punchtime", i).substring(0, 19);
                                        dateOutIn[1] = sdf2.parse(inStringDate);
                                    } else {
                                        inStringDate = handler.getData("punchtime", i).replace("Z", "");
                                        dateOutIn[1] = sdf.parse(inStringDate);
                                        inStringDate = sdf2.format(dateOutIn[1]);
                                    }

                                    // inStringDate =
                                    // handler.getData("punchtime",
                                    // i).replace("Z", "");
                                    // dateOutIn[1] = sdf.parse(inStringDate);
                                    // inStringDate = sdf2.format(dateOutIn[1]);
                                }

                                listTC.add(tempTC);
                                tempTC = new TimeClock();

                            }
                            timeClockHandler.insert(listTC, true);

                            if (statusOut && statusIn) // there were both an out
                            // and in time
                            {
                                if (dateOutIn[0].after(dateOutIn[1]))
                                    isOut = true;
                            } else if (statusOut) // there was only an out date,
                                isOut = true;
                        }

                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
//					Tracker tracker = EasyTracker.getInstance(activity);
//					tracker.send(MapBuilder.createException(e.getStackTrace().toString(), false).build());
                }
            } else {
                listTC = timeClockHandler.getEmployeeTimeClock(mClerkID);
                int size = listTC.size();

                if (size > 0) {
                    isValid = true;

                    for (int i = 0; i < size; i++) {
                        try {
                            if (listTC.get(i).status.equals("OUT")) {
                                statusOut = true;
                                if (!listTC.get(i).punchtime.contains("Z")) {
                                    outStringDate = listTC.get(i).punchtime.substring(0, 19);
                                    dateOutIn[0] = sdf2.parse(outStringDate);
                                } else {
                                    outStringDate = listTC.get(i).punchtime.replace("Z", "");
                                    dateOutIn[0] = sdf.parse(outStringDate);

                                    outStringDate = sdf2.format(dateOutIn[0]);
                                }

                            } else {

                                statusIn = true;

                                if (!listTC.get(i).punchtime.contains("Z")) {
                                    inStringDate = listTC.get(i).punchtime.substring(0, 19);
                                    dateOutIn[1] = sdf2.parse(inStringDate);
                                } else {
                                    inStringDate = listTC.get(i).punchtime.replace("Z", "");
                                    dateOutIn[1] = sdf.parse(inStringDate);
                                    inStringDate = sdf2.format(dateOutIn[1]);
                                }

                            }
                        } catch (ParseException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    if (statusOut && statusIn) // there were both an out and in
                    // time
                    {
                        if (dateOutIn[0].after(dateOutIn[1])) {
                            isOut = true;
                        }
                    } else if (statusOut) // there was only an in date,
                    {
                        isOut = true;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {

            if (isValid) {
                if (isOut) {
                    clockType.setText("OUT");
                    clockDateTime.setText(Global.formatToDisplayDate(outStringDate,  3));

                    switchButton(true);
                } else {
                    long secs = ((new Date()).getTime() - dateOutIn[1].getTime()) / 1000;
                    hourDifference = secs / 3600;

                    clockType.setText("IN");
                    clockDateTime.setText(Global.formatToDisplayDate(inStringDate,  3));

                    switchButton(false);
                }
            } else {
                switchButton(true);
            }
            myProgressDialog.dismiss();

        }

    }

    private void switchButton(boolean isOut) {
        if (isOut) {
            clockOutOn = false;
            clockInOn = true;
        } else {
            clockOutOn = true;
            clockInOn = false;
        }

        clockOut.setEnabled(clockOutOn);
        clockIn.setEnabled(clockInOn);
    }

    private void promptDateTime() {
        View view = activity.getLayoutInflater().inflate(R.layout.date_time_picker_layout, null);
        final TimePicker tp = (TimePicker) view.findViewById(R.id.timePicker);
        final DatePicker dp = (DatePicker) view.findViewById(R.id.datePicker);

        adb = new AlertDialog.Builder(activity);
        adb.setView(view);
        adb.setTitle(R.string.pick_date_time);
        adb.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();
                GregorianCalendar date = new GregorianCalendar(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(),
                        tp.getCurrentHour(), tp.getCurrentMinute(), 0);

                listTimeClock.add(createTimeClock(true, formatDate(date)));
                askForAdminPassDlg(Global.formatToDisplayDate(formatDate(date),  3));

            }
        });
        adb.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        });
        promptDialog = adb.create();

        tp.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {

            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                // TODO Auto-generated method stub

            }
        });
        promptDialog.show();
    }

    private String formatDate(GregorianCalendar calendar) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        SimpleDateFormat sdfTZ = new SimpleDateFormat("Z", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        sdfTZ.setTimeZone(tz);
        // sdf.setCalendar(calendar);

        String cur_date = sdf.format(calendar.getTime());

        String TimeZone = sdfTZ.format(calendar.getTime());

        String ending = TimeZone.substring(TimeZone.length() - 2, TimeZone.length());
        String begining = TimeZone.substring(0, TimeZone.length() - 2);
        StringBuilder sb = new StringBuilder().append(cur_date).append(begining).append(":").append(ending);

        return sb.toString();
    }

    private void askForAdminPassDlg(final String date) {

        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.clock_in_on)).append("\n").append(clockDateTime.getText().toString())
                .append("\n\n");
        sb.append(getString(R.string.confirm_clock_out)).append("\n").append(date).append("\n");

        final Dialog globalDlog = new Dialog(activity, R.style.Theme_TransparentTest);
        globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        globalDlog.setCancelable(false);
        globalDlog.setContentView(R.layout.dlog_field_single_two_btn);

        final EditText viewField = (EditText) globalDlog.findViewById(R.id.dlogFieldSingle);
        viewField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        TextView viewTitle = (TextView) globalDlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) globalDlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        if (!validPassword)
            viewTitle.setText(R.string.invalid_password);
        else
            viewTitle.setText(R.string.enter_password);

        viewMsg.setText(sb.toString());

        Button btnLeft = (Button) globalDlog.findViewById(R.id.btnDlogLeft);
        btnLeft.setText(R.string.button_ok);
        Button btnRight = (Button) globalDlog.findViewById(R.id.btnDlogRight);
        btnRight.setText(R.string.button_cancel);
        btnLeft.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String value = viewField.getText().toString();

                if (value.equals(myPref.getPOSAdminPass())) // validate admin
                // password
                {
                    validPassword = true;
                    globalDlog.dismiss();

                    timeClockHandler.insert(listTimeClock, false);
                    listTimeClock.clear();
                    new sendUnsyncTimeClock().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false);
                } else {
                    globalDlog.dismiss();
                    validPassword = false;
                    askForAdminPassDlg(date);
                }
            }
        });
        btnRight.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                globalDlog.dismiss();
            }
        });
        globalDlog.show();
    }

    public String getClerkID() {
        return mClerkID;
    }
}
