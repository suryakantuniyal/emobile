package com.android.emobilepos.shifts;

import android.app.ProgressDialog;
import android.graphics.Color;
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

import com.android.dao.AssignEmployeeDAO;
import com.android.dao.ClerkDAO;
import com.android.dao.ShiftDAO;
import com.android.database.DBManager;
import com.android.emobilepos.R;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.models.realms.Shift;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NetworkUtils;
import com.android.support.NumberUtils;
import com.android.support.SynchMethods;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.android.support.textwatcher.NumberFieldsTextWatcher;
import com.crashlytics.android.Crashlytics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;


public class ShiftsActivity extends BaseFragmentActivityActionBar implements View.OnClickListener, TextWatcher, View.OnFocusChangeListener {

    Global global;
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
    private TextView shortOverStatusTextView;
    private Shift shift;
    private Button submitShiftbutton;
    private TextView openOnLbl;
    private TextView openOnDate;
    private TextView closeAmountLbl;
    private TextView pettyCashLbl;
    private TextView pettyCash;
    private MyPreferences preferences;
    private TextView endingCashAmounteditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = new MyPreferences(this);
        global = (Global) this.getApplication();
//        if (preferences.isUseClerks()) {
        new GetShiftTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        }
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
            pettyCash.setText(Global.getCurrencyFormat(shift.getBeginningPettyCash()));
        } else if (shift.getShiftStatus() == Shift.ShiftStatus.PENDING) {
            enableCurrencies(true);
            submitShiftbutton.setText(getString(R.string.shift_close_shift));
            openOnLbl.setVisibility(View.VISIBLE);
            openOnDate.setVisibility(View.VISIBLE);
            pettyCashLbl.setVisibility(View.VISIBLE);
            pettyCash.setVisibility(View.VISIBLE);
            closeAmountLbl.setText(getString(R.string.entered_close_amount));
            openOnDate.setText(DateUtils.getDateAsString(shift.getCreationDate(), DateUtils.DATE_MMM_dd_yyyy_h_mm_a));
            pettyCash.setText(Global.getCurrencyFormat(shift.getBeginningPettyCash()));
            endingCashAmounteditText.setText(Global.getCurrencyFormat(shift.getTotal_ending_cash()));
        }

    }

    private void enableCurrencies(boolean enable) {
        LinearLayout currenciesLl = findViewById(R.id.currencieslinearLayout4);
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
        Double total = (oneCent * .01) + (fiveCents * 5 * .01) + (tenCents * 10 * .01) + (quarterCents * 25 * .01) +
                oneDollar + (fiveDollars * 5) + (tenDollars * 10) + (twentyDollars * 20) +
                (fiftyDollars * 50) + (hundredDollars * 100);
        Date now = new Date();
        shift.setEnteredCloseAmount(NumberUtils.cleanCurrencyFormatedNumber(totalAmountEditText.getText().toString()));
        shift.setEndTime(now);
        shift.setEndTimeLocal(now);
        shift.setShiftStatus(Shift.ShiftStatus.CLOSED);
        shift.setOver_short(String.valueOf(Double.parseDouble(shift.getTotal_ending_cash()) - total));
        ShiftDAO.insertOrUpdate(shift);
        new SendShiftTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void startCountDownShift() {
        shift.setShiftStatus(Shift.ShiftStatus.PENDING);
        ShiftDAO.insertOrUpdate(shift);
        setShiftUI();
        if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Global.mainPrinterManager.getCurrentDevice().openCashDrawer();
                }
            }).start();
        }
        new SendShiftTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void openShift() {
        if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Global.mainPrinterManager.getCurrentDevice().openCashDrawer();
                }
            }).start();
        }
        Date now = new Date();
        shift = new Shift();
        AssignEmployee employee = AssignEmployeeDAO.getAssignEmployee(false);
        shift.setShiftStatus(Shift.ShiftStatus.OPEN);
        shift.setAssigneeId(employee.getEmpId());
        shift.setAssigneeName(employee.getEmpName());
        shift.setClerkId(Integer.parseInt(preferences.getClerkID()));
        shift.setBeginningPettyCash(NumberUtils.cleanCurrencyFormatedNumber(totalAmountEditText.getText().toString()));
        shift.setTotal_ending_cash(shift.getBeginningPettyCash());
        shift.setCreationDate(now);
        shift.setStartTime(now);
        shift.setStartTimeLocal(now);

        //set the ending petty cash equal to the beginning petty cash, decrease the ending petty cash every time there is an expense
        shift.setEndingPettyCash(NumberUtils.cleanCurrencyFormatedNumber(totalAmountEditText.getText().toString()));
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
        if (shift != null && shift.getShiftStatus() == Shift.ShiftStatus.PENDING) {
            BigDecimal totalEndingCash = new BigDecimal(NumberUtils.cleanCurrencyFormatedNumber(shift.getTotal_ending_cash()));
            switch (total.compareTo(totalEndingCash.doubleValue())) {
                case 1:
                    shortOverStatusTextView.setText(
                            String.format("%s %s", getString(R.string.over_amount), Global.formatDoubleToCurrency(totalEndingCash.subtract(BigDecimal.valueOf(total)).abs().doubleValue())));
                    totalAmountEditText.setTextColor(Color.RED);
                    shortOverStatusTextView.setTextColor(Color.RED);
                    shortOverStatusTextView.setVisibility(View.VISIBLE);
                    break;
                case -1:
                    totalAmountEditText.setTextColor(Color.RED);
                    shortOverStatusTextView.setTextColor(Color.RED);
                    shortOverStatusTextView.setVisibility(View.VISIBLE);
                    shortOverStatusTextView.setText(
                            String.format("%s (%s)", getString(R.string.short_amount), Global.formatDoubleToCurrency(totalEndingCash.subtract(BigDecimal.valueOf(total)).doubleValue())));
                    break;
                case 0:
                    totalAmountEditText.setTextColor(Color.BLUE);
                    shortOverStatusTextView.setTextColor(Color.BLUE);
                    shortOverStatusTextView.setVisibility(View.VISIBLE);
                    shortOverStatusTextView.setText(
                            String.format("%s %s", getString(R.string.even_amount), Global.formatDoubleToCurrency(totalEndingCash.subtract(BigDecimal.valueOf(total)).doubleValue())));
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        if (global.isApplicationSentToBackground())
            Global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (!Global.loggedIn) {
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
            Global.loggedIn = false;
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

    private void openUI() {
        setContentView(R.layout.activity_shifts);
        shift = ShiftDAO.getShiftByClerkId(Integer.parseInt(preferences.getClerkID()));
        Clerk clerk;
        if (shift == null) {
            shift = ShiftDAO.getShiftByClerkId(Integer.parseInt(preferences.getClerkID()));
        }
        if (shift == null) {
            clerk = ClerkDAO.getByEmpId(Integer.parseInt(preferences.getClerkID()));
        } else {
            clerk = ClerkDAO.getByEmpId(shift.getClerkId());
        }
        TextView clerkName = findViewById(R.id.clerkNameShifttextView);
        clerkName.setText(clerk == null ? "" : clerk.getEmpName());
        endingCashAmounteditText = findViewById(R.id.endingCashAmounteditText);
        totalAmountEditText = findViewById(R.id.totalAmounteditText);
        shortOverStatusTextView = findViewById(R.id.shortOverStatustextView23);
        shortOverStatusTextView.setVisibility(View.GONE);
        openOnLbl = findViewById(R.id.openOnLbltextView25);
        openOnDate = findViewById(R.id.openOnDatetextView26);
        closeAmountLbl = findViewById(R.id.closeAmountLbltextView21);
        pettyCashLbl = findViewById(R.id.pettyCashLbltextView27);
        pettyCash = findViewById(R.id.beginningPettyCashtextView);

        oneDollarEditText = findViewById(R.id.oneDollareditText);
        fiveDollarEditText = findViewById(R.id.fiveDollareditText);
        tenDollarEditText = findViewById(R.id.tenDollareditText);
        twentyDollarEditText = findViewById(R.id.twentyDollareditText);
        fiftyDollarEditText = findViewById(R.id.fiftyDollareditText);
        hundredDollarEditText = findViewById(R.id.hundredDollareditText);
        oneCentEditText = findViewById(R.id.oneCenteditText);
        fiveCentsEditText = findViewById(R.id.fiveCentseditText);
        tenCentsEditText = findViewById(R.id.tenCentseditText);
        quarterCentsEditText = findViewById(R.id.quartesCentseditText);

        oneDollarTextView = findViewById(R.id.oneDollarTotaltextView35);
        fiveDollarTextView = findViewById(R.id.fiveDollarTotaltextView35);
        tenDollarTextView = findViewById(R.id.tenDollarTotaltextView35);
        twentyDollarTextView = findViewById(R.id.twentyDollarTotaltextView35);
        fiftyDollarTextView = findViewById(R.id.fiftyDollarTotaltextView35);
        hundredDollarTextView = findViewById(R.id.hundredDollarTotaltextView35);
        oneCentTextView = findViewById(R.id.oneCentTotaltextView35);
        fiveCentsTextView = findViewById(R.id.fiveCentsTotaltextView35);
        tenCentsTextView = findViewById(R.id.tenCentsTotaltextView35);
        quarterCentsTextView = findViewById(R.id.quartesCentsTotaltextView35);

        oneDollarEditText.setOnFocusChangeListener(this);
        fiveDollarEditText.setOnFocusChangeListener(this);
        tenDollarEditText.setOnFocusChangeListener(this);
        twentyDollarEditText.setOnFocusChangeListener(this);
        hundredDollarEditText.setOnFocusChangeListener(this);
        oneCentEditText.setOnFocusChangeListener(this);
        fiveCentsEditText.setOnFocusChangeListener(this);
        tenCentsEditText.setOnFocusChangeListener(this);
        quarterCentsEditText.setOnFocusChangeListener(this);
        fiftyDollarEditText.setOnFocusChangeListener(this);


        oneDollarEditText.addTextChangedListener(new NumberFieldsTextWatcher(oneDollarEditText));
        fiveDollarEditText.addTextChangedListener(new NumberFieldsTextWatcher(fiveDollarEditText));
        tenDollarEditText.addTextChangedListener(new NumberFieldsTextWatcher(tenDollarEditText));
        twentyDollarEditText.addTextChangedListener(new NumberFieldsTextWatcher(twentyDollarEditText));
        hundredDollarEditText.addTextChangedListener(new NumberFieldsTextWatcher(hundredDollarEditText));
        oneCentEditText.addTextChangedListener(new NumberFieldsTextWatcher(oneCentEditText));
        fiveCentsEditText.addTextChangedListener(new NumberFieldsTextWatcher(fiveCentsEditText));
        tenCentsEditText.addTextChangedListener(new NumberFieldsTextWatcher(tenCentsEditText));
        quarterCentsEditText.addTextChangedListener(new NumberFieldsTextWatcher(quarterCentsEditText));
        fiftyDollarEditText.addTextChangedListener(new NumberFieldsTextWatcher(fiftyDollarEditText));

        Button minusOneDollar = findViewById(R.id.oneDollarMinusbutton);
        Button minusFiveDollar = findViewById(R.id.fiveDollarMinusbutton);
        Button minusTenDollar = findViewById(R.id.tenDollarMinusbutton);
        Button minusTwentyDollar = findViewById(R.id.twentyDollarMinusbutton);
        Button minusFiftyDollar = findViewById(R.id.fiftyDollarMinusbutton);
        Button minusHundredDollar = findViewById(R.id.hundredDollarMinusbutton);
        Button minusOneCent = findViewById(R.id.oneCentMinusbutton);
        Button minusFiveCent = findViewById(R.id.fiveCentsMinusbutton);
        Button minusTenCent = findViewById(R.id.tenCentsMinusbutton);
        Button minusQuarterCent = findViewById(R.id.quartesCentsMinusbutton);
        Button plusOneDollar = findViewById(R.id.oneDollarPlusbutton);
        Button plusFiveDollar = findViewById(R.id.fiveDollarPlusbutton);
        Button plusTenDollar = findViewById(R.id.tenDollarPlusbutton);
        Button plusTwentyDollar = findViewById(R.id.twentyDollarPlusbutton);
        Button plusFiftyDollar = findViewById(R.id.fiftyDollarPlusbutton);
        Button plusHundredDollar = findViewById(R.id.hundredDollarPlusbutton);
        Button plusOneCent = findViewById(R.id.oneCentPlusbutton);
        Button plusFiveCent = findViewById(R.id.fiveCentsPlusbutton);
        Button plusTenCent = findViewById(R.id.tenCentsPlusbutton);
        Button plusQuarterCent = findViewById(R.id.quartesCentsPlusbutton);
        submitShiftbutton = findViewById(R.id.submitShiftbutton);
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

    private boolean sendShifts() {
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
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void onFocusChange(final View view, boolean hasFocus) {
        if (hasFocus) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    ((EditText) view).setSelection(((EditText) view).getText().toString().length());
                }
            });
//                    Selection.setSelection(oneDollarEditText.getText(), oneDollarEditText.getText().toString().length());
        }
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
                    Crashlytics.logException(e);
                }
            }
            sendShifts();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            openUI();
        }
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

            return sendShifts();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            dialog.dismiss();
            if (shift.getShiftStatus() == Shift.ShiftStatus.CLOSED) {
                finish();
            }
        }
    }
}