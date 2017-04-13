package com.android.emobilepos.shifts;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.dao.ClerkDAO;
import com.android.dao.ShiftDAO;
import com.android.database.DBManager;
import com.android.emobilepos.R;
import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.models.realms.Shift;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NetworkUtils;
import com.android.support.NumberUtils;
import com.android.support.SynchMethods;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.Date;

public class ShiftsActivity extends BaseFragmentActivityActionBar implements View.OnClickListener, TextWatcher {

    private EditText oneDollarEditText;
    private EditText fiveDollarEditText;
    private EditText tenDollarEditText;
    private EditText twentyDollarEditText;
    private EditText fiftyDollarEditText;
    private EditText hundredDollarEditText;
    private EditText oneCentEditText;
    private EditText fiveCentsEditText;
    private EditText tenCentsEditText;
    private EditText quarterCentsEditText;

    private TextView oneDollarTextView;
    private TextView fiveDollarTextView;
    private TextView tenDollarTextView;
    private TextView twentyDollarTextView;
    private TextView fiftyDollarTextView;
    private TextView hundredDollarTextView;
    private TextView oneCentTextView;
    private TextView fiveCentsTextView;
    private TextView tenCentsTextView;
    private TextView quarterCentsTextView;

    private int oneCent;
    private int fiveCents;
    private int tenCents;
    private int quarterCents;
    private int oneDollar;
    private int fiveDollars;
    private int tenDollars;
    private int twentyDollars;
    private int fiftyDollars;
    private int hundredDollars;
    private TextView totalAmountEditText;
    private Shift shift;
    private Button submitShiftbutton;
    private TextView openOnLbl;
    private TextView openOnDate;
    private TextView closeAmountLbl;
    private TextView pettyCashLbl;
    private TextView pettyCash;
    private MyPreferences preferences;
    Global global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = new MyPreferences(this);
        global = (Global) this.getApplication();
        new GetShiftTask().execute();
    }

    private void setShiftUI() {
        if (shift == null || shift.getShiftStatus() == Shift.ShiftStatus.CLOSED) {
            enableCurrencies(true);
            submitShiftbutton.setText(getString(R.string.admin_open_shift));
            openOnLbl.setVisibility(View.INVISIBLE);
            openOnDate.setVisibility(View.INVISIBLE);
            pettyCashLbl.setVisibility(View.INVISIBLE);
            pettyCash.setVisibility(View.INVISIBLE);
            closeAmountLbl.setText(getString(R.string.entered_open_amount));
        } else if (shift.getShiftStatus() == Shift.ShiftStatus.OPEN) {
            enableCurrencies(false);
            submitShiftbutton.setText(getString(R.string.shift_count_down_shift));
            openOnLbl.setVisibility(View.VISIBLE);
            openOnDate.setVisibility(View.VISIBLE);
            pettyCashLbl.setVisibility(View.VISIBLE);
            pettyCash.setVisibility(View.VISIBLE);
            closeAmountLbl.setText(getString(R.string.entered_count_down_amount));
            openOnDate.setText(DateUtils.getDateAsString(shift.getCreationDate(), DateUtils.DATE_MMM_dd_yyyy_h_mm_a));
            pettyCash.setText(Global.formatDoubleStrToCurrency(shift.getBeginningPettyCash()));
        } else if (shift.getShiftStatus() == Shift.ShiftStatus.PENDING) {
            enableCurrencies(true);
            submitShiftbutton.setText(getString(R.string.shift_close_shift));
            openOnLbl.setVisibility(View.VISIBLE);
            openOnDate.setVisibility(View.VISIBLE);
            pettyCashLbl.setVisibility(View.VISIBLE);
            pettyCash.setVisibility(View.VISIBLE);
            closeAmountLbl.setText(getString(R.string.entered_close_amount));
            openOnDate.setText(DateUtils.getDateAsString(shift.getCreationDate(), DateUtils.DATE_MMM_dd_yyyy_h_mm_a));
            pettyCash.setText(Global.formatDoubleStrToCurrency(shift.getBeginningPettyCash()));
        }

    }

    private void enableCurrencies(boolean enable) {
        LinearLayout currenciesLl = (LinearLayout) findViewById(R.id.currencieslinearLayout4);
        ArrayList<View> allChildren = getAllChildren(currenciesLl);
        for (View child : allChildren) {
            if (child instanceof Button || child instanceof EditText) {
                child.setEnabled(enable);
            }
        }
    }

    private ArrayList<View> getAllChildren(View v) {
        if (!(v instanceof ViewGroup)) {
            ArrayList<View> viewArrayList = new ArrayList<>();
            viewArrayList.add(v);
            return viewArrayList;
        }
        ArrayList<View> result = new ArrayList<>();
        ViewGroup vg = (ViewGroup) v;
        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            ArrayList<View> viewArrayList = new ArrayList<>();
            viewArrayList.add(v);
            viewArrayList.addAll(getAllChildren(child));
            result.addAll(viewArrayList);
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submitShiftbutton:
                if (shift == null || shift.getShiftStatus() == Shift.ShiftStatus.CLOSED) {
                    openShift();
                } else if (shift.getShiftStatus() == Shift.ShiftStatus.OPEN) {
                    startCountDownShift();
                } else if (shift.getShiftStatus() == Shift.ShiftStatus.PENDING) {
                    closeShift();
                }
                break;
            case R.id.oneCentMinusbutton:
                if (oneCent > 0) {
                    oneCent--;
                }
                oneCentEditText.setText(String.valueOf(oneCent));
                break;
            case R.id.fiveCentsMinusbutton:
                if (fiveCents > 0) {
                    fiveCents--;
                }
                fiveCentsEditText.setText(String.valueOf(fiveCents));
                break;
            case R.id.tenCentsMinusbutton:
                if (tenCents > 0) {
                    tenCents--;
                }
                tenCentsEditText.setText(String.valueOf(tenCents));
                break;
            case R.id.quartesCentsMinusbutton:
                if (quarterCents > 0) {
                    quarterCents--;
                }
                quarterCentsEditText.setText(String.valueOf(quarterCents));
                break;
            case R.id.oneDollarMinusbutton:
                if (oneDollar > 0) {
                    oneDollar--;
                }
                oneDollarEditText.setText(String.valueOf(oneDollar));
                break;
            case R.id.fiveDollarMinusbutton:
                if (fiveDollars > 0) {
                    fiveDollars--;
                }
                fiveDollarEditText.setText(String.valueOf(fiveDollars));
                break;
            case R.id.tenDollarMinusbutton:
                if (tenDollars > 0) {
                    tenDollars--;
                }
                tenDollarEditText.setText(String.valueOf(tenDollars));
                break;
            case R.id.twentyDollarMinusbutton:
                if (twentyDollars > 0) {
                    twentyDollars--;
                }
                twentyDollarEditText.setText(String.valueOf(twentyDollars));
                break;
            case R.id.fiftyDollarMinusbutton:
                if (fiftyDollars > 0) {
                    fiftyDollars--;
                }
                fiftyDollarEditText.setText(String.valueOf(fiftyDollars));
                break;
            case R.id.hundredDollarMinusbutton:
                if (hundredDollars > 0) {
                    hundredDollars--;
                }
                hundredDollarEditText.setText(String.valueOf(hundredDollars));
                break;
            case R.id.oneCentPlusbutton:
                oneCent++;
                oneCentEditText.setText(String.valueOf(oneCent));
                break;
            case R.id.fiveCentsPlusbutton:
                fiveCents++;
                fiveCentsEditText.setText(String.valueOf(fiveCents));
                break;
            case R.id.tenCentsPlusbutton:
                tenCents++;
                tenCentsEditText.setText(String.valueOf(tenCents));
                break;
            case R.id.quartesCentsPlusbutton:
                quarterCents++;
                quarterCentsEditText.setText(String.valueOf(quarterCents));
                break;
            case R.id.oneDollarPlusbutton:
                oneDollar++;
                oneDollarEditText.setText(String.valueOf(oneDollar));
                break;
            case R.id.fiveDollarPlusbutton:
                fiveDollars++;
                fiveDollarEditText.setText(String.valueOf(fiveDollars));
                break;
            case R.id.tenDollarPlusbutton:
                tenDollars++;
                tenDollarEditText.setText(String.valueOf(tenDollars));
                break;
            case R.id.twentyDollarPlusbutton:
                twentyDollars++;
                twentyDollarEditText.setText(String.valueOf(twentyDollars));
                break;
            case R.id.fiftyDollarPlusbutton:
                fiftyDollars++;
                fiftyDollarEditText.setText(String.valueOf(fiftyDollars));
                break;
            case R.id.hundredDollarPlusbutton:
                hundredDollars++;
                hundredDollarEditText.setText(String.valueOf(hundredDollars));
                break;
        }
        recalculate();
    }

    private void closeShift() {
        Date now = new Date();
        shift.setEnteredCloseAmount(NumberUtils.cleanCurrencyFormatedNumber(totalAmountEditText.getText().toString()));
        shift.setEndTime(now);
        shift.setEndTimeLocal(now);
        shift.setShiftStatus(Shift.ShiftStatus.CLOSED);
        ShiftDAO.insertOrUpdate(shift);
        new SendShiftTask().execute();
    }

    private void startCountDownShift() {
        shift.setShiftStatus(Shift.ShiftStatus.PENDING);
        ShiftDAO.insertOrUpdate(shift);
        setShiftUI();
        new SendShiftTask().execute();
    }

    private void openShift() {
        Date now = new Date();
        shift = new Shift();
        shift.setShiftStatus(Shift.ShiftStatus.OPEN);
        shift.setAssigneeId(Integer.parseInt(preferences.getClerkID()));
        shift.setAssigneeName(preferences.getClerkName());
        shift.setBeginningPettyCash(NumberUtils.cleanCurrencyFormatedNumber(totalAmountEditText.getText().toString()));
        shift.setCreationDate(now);
        shift.setStartTime(now);
        shift.setStartTimeLocal(now);

        //set the ending petty cash equal to the beginning petty cash, decrease the ending petty cash every time there is an expense
        shift.setEndingPettyCash(NumberUtils.cleanCurrencyFormatedNumber(totalAmountEditText.getText().toString()));
        shift.setTotal_ending_cash("0");
        ShiftDAO.insertOrUpdate(shift);
        finish();
    }

    private void recalculate() {
        Double total = (oneCent * .01) + (fiveCents * 5 * .01) + (tenCents * 10 * .01) + (quarterCents * 25 * .01) +
                oneDollar + (fiveDollars * 5) + (tenDollars * 10) + (twentyDollars * 20) +
                (fiftyDollars * 50) + (hundredDollars * 100);
        oneCentTextView.setText(Global.getCurrencyFormat(String.valueOf(oneCent * .01)));
        fiveCentsTextView.setText(Global.getCurrencyFormat(String.valueOf(fiveCents * 5 * .01)));
        tenCentsTextView.setText(Global.getCurrencyFormat(String.valueOf(tenCents * 10 * .01)));
        quarterCentsTextView.setText(Global.getCurrencyFormat(String.valueOf(quarterCents * 25 * .01)));
        oneDollarTextView.setText(Global.getCurrencyFormat(String.valueOf(oneDollar)));
        fiveDollarTextView.setText(Global.getCurrencyFormat(String.valueOf(fiveDollars * 5)));
        tenDollarTextView.setText(Global.getCurrencyFormat(String.valueOf(tenDollars * 10)));
        twentyDollarTextView.setText(Global.getCurrencyFormat(String.valueOf(twentyDollars * 20)));
        fiftyDollarTextView.setText(Global.getCurrencyFormat(String.valueOf(fiftyDollars * 50)));
        hundredDollarTextView.setText(Global.getCurrencyFormat(String.valueOf(hundredDollars * 100)));
        totalAmountEditText.setText(Global.getCurrencyFormat(total.toString()));
    }

    @Override
    public void onResume() {
        if (global.isApplicationSentToBackground(this))
            global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (!global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(this);
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
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() == 0) {
            s.append("0");
        }
        int hashCode = s.hashCode();
        if (hashCode == oneCentEditText.getText().hashCode()) {
            oneCent = Integer.parseInt(oneCentEditText.getText().toString());
        } else if (hashCode == fiveCentsEditText.getText().hashCode()) {
            fiveCents = Integer.parseInt(fiveCentsEditText.getText().toString());
        } else if (hashCode == tenCentsEditText.getText().hashCode()) {
            tenCents = Integer.parseInt(tenCentsEditText.getText().toString());
        } else if (hashCode == quarterCentsEditText.getText().hashCode()) {
            quarterCents = Integer.parseInt(quarterCentsEditText.getText().toString());
        } else if (hashCode == oneDollarEditText.getText().hashCode()) {
            oneDollar = Integer.parseInt(oneDollarEditText.getText().toString());
        } else if (hashCode == fiveDollarEditText.getText().hashCode()) {
            fiveDollars = Integer.parseInt(fiveDollarEditText.getText().toString());
        } else if (hashCode == tenDollarEditText.getText().hashCode()) {
            tenDollars = Integer.parseInt(tenDollarEditText.getText().toString());
        } else if (hashCode == twentyDollarEditText.getText().hashCode()) {
            twentyDollars = Integer.parseInt(twentyDollarEditText.getText().toString());
        } else if (hashCode == fiftyDollarEditText.getText().hashCode()) {
            fiftyDollars = Integer.parseInt(fiftyDollarEditText.getText().toString());
        } else if (hashCode == hundredDollarEditText.getText().hashCode()) {
            hundredDollars = Integer.parseInt(hundredDollarEditText.getText().toString());
        }
        recalculate();
    }

    private class GetShiftTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(ShiftsActivity.this);
            dialog.setTitle(getString(R.string.shift_title));
            dialog.setMessage(getString(R.string.sync_dload_shifts));
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            DBManager dbManager = new DBManager(ShiftsActivity.this);
            SynchMethods sm = new SynchMethods(dbManager);
            if (NetworkUtils.isConnectedToInternet(ShiftsActivity.this)) {
                try {
                    sm.synchShifts();
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            openUI();
        }
    }

    private void openUI() {
        setContentView(R.layout.activity_shifts);
        Clerk clerk = ClerkDAO.getByEmpId(Integer.parseInt(preferences.getClerkID()), true);
        TextView clerkName = (TextView) findViewById(R.id.clerkNameShifttextView);
        clerkName.setText(clerk.getEmpName());
        shift = ShiftDAO.getCurrentShift(Integer.parseInt(preferences.getClerkID()));
        totalAmountEditText = (TextView) findViewById(R.id.totalAmounteditText);
        openOnLbl = (TextView) findViewById(R.id.openOnLbltextView25);
        openOnDate = (TextView) findViewById(R.id.openOnDatetextView26);
        closeAmountLbl = (TextView) findViewById(R.id.closeAmountLbltextView21);
        pettyCashLbl = (TextView) findViewById(R.id.pettyCashLbltextView27);
        pettyCash = (TextView) findViewById(R.id.beginningPettyCashtextView);

        oneDollarEditText = (EditText) findViewById(R.id.oneDollareditText);
        fiveDollarEditText = (EditText) findViewById(R.id.fiveDollareditText);
        tenDollarEditText = (EditText) findViewById(R.id.tenDollareditText);
        twentyDollarEditText = (EditText) findViewById(R.id.twentyDollareditText);
        fiftyDollarEditText = (EditText) findViewById(R.id.fiftyDollareditText);
        hundredDollarEditText = (EditText) findViewById(R.id.hundredDollareditText);
        oneCentEditText = (EditText) findViewById(R.id.oneCenteditText);
        fiveCentsEditText = (EditText) findViewById(R.id.fiveCentseditText);
        tenCentsEditText = (EditText) findViewById(R.id.tenCentseditText);
        quarterCentsEditText = (EditText) findViewById(R.id.quartesCentseditText);

        oneDollarTextView = (TextView) findViewById(R.id.oneDollarTotaltextView35);
        fiveDollarTextView = (TextView) findViewById(R.id.fiveDollarTotaltextView35);
        tenDollarTextView = (TextView) findViewById(R.id.tenDollarTotaltextView35);
        twentyDollarTextView = (TextView) findViewById(R.id.twentyDollarTotaltextView35);
        fiftyDollarTextView = (TextView) findViewById(R.id.fiftyDollarTotaltextView35);
        hundredDollarTextView = (TextView) findViewById(R.id.hundredDollarTotaltextView35);
        oneCentTextView = (TextView) findViewById(R.id.oneCentTotaltextView35);
        fiveCentsTextView = (TextView) findViewById(R.id.fiveCentsTotaltextView35);
        tenCentsTextView = (TextView) findViewById(R.id.tenCentsTotaltextView35);
        quarterCentsTextView = (TextView) findViewById(R.id.quartesCentsTotaltextView35);

        Button minusOneDollar = (Button) findViewById(R.id.oneDollarMinusbutton);
        Button minusFiveDollar = (Button) findViewById(R.id.fiveDollarMinusbutton);
        Button minusTenDollar = (Button) findViewById(R.id.tenDollarMinusbutton);
        Button minusTwentyDollar = (Button) findViewById(R.id.twentyDollarMinusbutton);
        Button minusFiftyDollar = (Button) findViewById(R.id.fiftyDollarMinusbutton);
        Button minusHundredDollar = (Button) findViewById(R.id.hundredDollarMinusbutton);
        Button minusOneCent = (Button) findViewById(R.id.oneCentMinusbutton);
        Button minusFiveCent = (Button) findViewById(R.id.fiveCentsMinusbutton);
        Button minusTenCent = (Button) findViewById(R.id.tenCentsMinusbutton);
        Button minusQuarterCent = (Button) findViewById(R.id.quartesCentsMinusbutton);
        Button plusOneDollar = (Button) findViewById(R.id.oneDollarPlusbutton);
        Button plusFiveDollar = (Button) findViewById(R.id.fiveDollarPlusbutton);
        Button plusTenDollar = (Button) findViewById(R.id.tenDollarPlusbutton);
        Button plusTwentyDollar = (Button) findViewById(R.id.twentyDollarPlusbutton);
        Button plusFiftyDollar = (Button) findViewById(R.id.fiftyDollarPlusbutton);
        Button plusHundredDollar = (Button) findViewById(R.id.hundredDollarPlusbutton);
        Button plusOneCent = (Button) findViewById(R.id.oneCentPlusbutton);
        Button plusFiveCent = (Button) findViewById(R.id.fiveCentsPlusbutton);
        Button plusTenCent = (Button) findViewById(R.id.tenCentsPlusbutton);
        Button plusQuarterCent = (Button) findViewById(R.id.quartesCentsPlusbutton);
        submitShiftbutton = (Button) findViewById(R.id.submitShiftbutton);
        submitShiftbutton.setOnClickListener(this);
        minusOneCent.setOnClickListener(this);
        minusFiveCent.setOnClickListener(this);
        minusTenCent.setOnClickListener(this);
        minusQuarterCent.setOnClickListener(this);
        minusOneDollar.setOnClickListener(this);
        minusFiveDollar.setOnClickListener(this);
        minusTenDollar.setOnClickListener(this);
        minusTwentyDollar.setOnClickListener(this);
        minusFiftyDollar.setOnClickListener(this);
        minusHundredDollar.setOnClickListener(this);
        plusOneCent.setOnClickListener(this);
        plusFiveCent.setOnClickListener(this);
        plusTenCent.setOnClickListener(this);
        plusQuarterCent.setOnClickListener(this);
        plusOneDollar.setOnClickListener(this);
        plusFiveDollar.setOnClickListener(this);
        plusTenDollar.setOnClickListener(this);
        plusTwentyDollar.setOnClickListener(this);
        plusFiftyDollar.setOnClickListener(this);
        plusHundredDollar.setOnClickListener(this);
        oneCentEditText.addTextChangedListener(this);
        fiveCentsEditText.addTextChangedListener(this);
        tenCentsEditText.addTextChangedListener(this);
        quarterCentsEditText.addTextChangedListener(this);
        oneDollarEditText.addTextChangedListener(this);
        fiveDollarEditText.addTextChangedListener(this);
        tenDollarEditText.addTextChangedListener(this);
        twentyDollarEditText.addTextChangedListener(this);
        fiftyDollarEditText.addTextChangedListener(this);
        hundredDollarEditText.addTextChangedListener(this);
        setShiftUI();
    }

    private class SendShiftTask extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(ShiftsActivity.this);
            dialog.setTitle(getString(R.string.shift_title));
            dialog.setMessage(getString(R.string.sync_sending_shifts));
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            DBManager dbManager = new DBManager(ShiftsActivity.this);
            SynchMethods sm = new SynchMethods(dbManager);
            if (NetworkUtils.isConnectedToInternet(ShiftsActivity.this)) {
                try {
                    sm.postShift(ShiftsActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            dialog.dismiss();
            if (result) {
                finish();
            } else {
                Global.showPrompt(ShiftsActivity.this, R.string.dlog_title_error, getString(R.string.error_sync_closed_shift));
            }
        }
    }
}
