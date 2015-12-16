package com.android.emobilepos.shifts;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.database.ClerksHandler;
import com.android.database.ShiftPeriodsDBHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.ShiftPeriods;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class OpenShift_FA extends BaseFragmentActivityActionBar implements OnClickListener, OnItemClickListener {

    private Global global;
    private boolean hasBeenCreated = false;

    private ListView lView;

    private Cursor myCursor;
    private Activity activity;
    private int selectedPos = -1;
    private EditText pettyCashField;
    private double pettyCash = 0;
    private MyPreferences myPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        myPref = new MyPreferences(this);
        global = (Global) getApplication();
        if (!myPref.getIsTablet()) // reset to default layout (not as dialog)
            super.setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_shift_main_layout);
        activity = this;

        ClerksHandler ch = new ClerksHandler(activity);

        myCursor = ch.getAllClerks();
        lView = (ListView) findViewById(R.id.openShiftListView);
        Button openShiftDoneButton = (Button) findViewById(R.id.openShiftDoneButton);
        Button openShiftCancelButton = (Button) findViewById(R.id.openShiftCancelButton);
        openShiftCancelButton.setOnClickListener(this);
        openShiftDoneButton.setOnClickListener(this);
        pettyCashField = (EditText) findViewById(R.id.pettyCashAmount);

        int i_name = myCursor.getColumnIndex("emp_name");
        int i_id = myCursor.getColumnIndex("_id");
        String[] test = new String[myCursor.getCount()];
        int i = 0;
        if (myCursor.moveToFirst()) {
            do {
                test[i] = myCursor.getString(i_name) + " (" + myCursor.getString(i_id) + ")";
                i++;
            } while (myCursor.moveToNext());
        }

        lView.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_multiple_choice, test) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(Color.BLACK);
                return textView;
            }
        });

        lView.setItemsCanFocus(false);
        lView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lView.setOnItemClickListener(this);

        this.pettyCashField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        this.pettyCashField.setText(Global.formatDoubleToCurrency(0));
        this.pettyCashField.setOnFocusChangeListener(getFocusListener());
        this.pettyCashField.addTextChangedListener(getTextWatcher());

        hasBeenCreated = true;
    }

    @Override
    public void onResume() {

        if (global.isApplicationSentToBackground(this))
            global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !global.loggedIn) {
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        selectedPos = position;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.openShiftDoneButton:
                openShift();
                break;
            case R.id.openShiftCancelButton:
                activity.setResult(0);
                activity.finish();
                break;
        }

    }

    private OnFocusChangeListener getFocusListener() {
        OnFocusChangeListener focusListener = new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (v.hasFocus()) {
                    Selection.setSelection(pettyCashField.getText(), pettyCashField.getText().length());
                }

            }
        };

        return focusListener;
    }

    private TextWatcher getTextWatcher() {
        TextWatcher textWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                parseInputedCurrency(s, R.id.processCardAmount);
            }
        };

        return textWatcher;
    }

    private void openShift() {
        if (selectedPos != -1) {
            myPref.setShiftIsOpen(false);
            myCursor.moveToPosition(selectedPos);
            myPref.setShiftClerkName(myCursor.getString(myCursor.getColumnIndex("emp_name")));
            myPref.setShiftClerkID(myCursor.getString(myCursor.getColumnIndex("_id")));

            ShiftPeriods sp = new ShiftPeriods(true);
            myPref.setShiftID(sp.shift_id);
            ShiftPeriodsDBHandler handler = new ShiftPeriodsDBHandler(activity);
            sp.assignee_id = myCursor.getString(myCursor.getColumnIndex("_id"));
            sp.assignee_name = myCursor.getString(myCursor.getColumnIndex("emp_name"));
            sp.beginning_petty_cash = Double.toString(pettyCash);
            sp.ending_petty_cash = Double.toString(pettyCash);
            sp.total_ending_cash = Double.toString(pettyCash);

            handler.insert(sp);

            activity.setResult(1);
            activity.finish();
        } else {
            Toast.makeText(activity, "No Clerks Selected...", Toast.LENGTH_LONG).show();
        }
    }

    private void parseInputedCurrency(CharSequence s, int type) {
        DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(Locale.getDefault());
        DecimalFormatSymbols sym = format.getDecimalFormatSymbols();
        StringBuilder sb = new StringBuilder();
        sb.append("^\\").append(sym.getCurrencySymbol()).append("\\s(\\d{1,3}(\\").append(sym.getGroupingSeparator())
                .append("\\d{3})*|(\\d+))(");
        sb.append(sym.getDecimalSeparator()).append("\\d{2})?$");

        if (!s.toString().matches(sb.toString())) {
            String userInput = "" + s.toString().replaceAll("[^\\d]", "");
            StringBuilder cashAmountBuilder = new StringBuilder(userInput);

            while (cashAmountBuilder.length() > 3 && cashAmountBuilder.charAt(0) == '0') {
                cashAmountBuilder.deleteCharAt(0);
            }
            while (cashAmountBuilder.length() < 3) {
                cashAmountBuilder.insert(0, '0');
            }

            cashAmountBuilder.insert(cashAmountBuilder.length() - 2, sym.getDecimalSeparator());
            cashAmountBuilder.insert(0, sym.getCurrencySymbol() + " ");

            this.pettyCashField.setText(cashAmountBuilder.toString());
            pettyCash = (float) (Global.formatNumFromLocale(cashAmountBuilder.toString().replaceAll("[^\\d\\,\\.]", "").trim()));

        }
        Selection.setSelection(this.pettyCashField.getText(), this.pettyCashField.getText().length());
    }
}
