package com.android.emobilepos.shifts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dao.ShiftDAO;
import com.android.dao.ShiftExpensesDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.models.realms.Shift;
import com.android.emobilepos.models.realms.ShiftExpense;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NumberUtils;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * Created by tirizar on 1/5/2016.
 */
public class ShiftExpense_FA extends BaseFragmentActivityActionBar implements View.OnClickListener {
    MyPreferences preferences;
    private int expenseProductIDSelected = 1;
    private String expenseName = "";
    AdapterView.OnItemSelectedListener onItemSelectedListenerSpinner =
            new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    expenseName = (String) parent.getItemAtPosition(position);
                    expenseProductIDSelected = position + 1;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            };
    private EditText cashAmount, comments;
    private Global global;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shift_add_expense);
        preferences = new MyPreferences(this);
        cashAmount = findViewById(R.id.cashAmount);
        comments = findViewById(R.id.expenseCommentseditText);
        global = (Global) getApplication();
        Button btnCancel = findViewById(R.id.buttonCancel);
        btnCancel.setOnClickListener(this);
        Button btnSubmit = findViewById(R.id.buttonSubmit);
        btnSubmit.setOnClickListener(this);
        String[] theSpinnerNames = getResources().getStringArray(R.array.expenseTypes);
        Spinner spinnerView = findViewById(R.id.expenseSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, theSpinnerNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerView.setAdapter(adapter);
        spinnerView.setOnItemSelectedListener(onItemSelectedListenerSpinner);

        this.cashAmount.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                NumberUtils.parseInputedCurrency(s, cashAmount);
            }
        });
        cashAmount.setText(R.string.amount_zero_lbl);
    }

    @Override
    public void onClick(View v) {
        double theAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(cashAmount));
        switch (v.getId()) {
            case R.id.buttonCancel:
                finish();
                break;
            case R.id.buttonSubmit:
                //verify valid amount
                if (theAmount <= 0) {
                    AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                    alertDialog.setTitle("Validation");
                    alertDialog.setMessage("Provide a valid amount!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    break;
                }
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(cashAmount.getWindowToken(), 0);
                addExpense();
                break;
        }
    }

    private void addExpense() {
        Date now = new Date();
        Shift openShift = ShiftDAO.getOpenShift();
        double amount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(cashAmount));
        ShiftExpense expense = new ShiftExpense();
        expense.setProductId(expenseProductIDSelected);
        expense.setExpenseId(UUID.randomUUID().toString());
        expense.setProductName(expenseName);
        expense.setShiftId(openShift != null ? openShift.getShiftId() : null);
        expense.setCreationDate(now);
        expense.setProductDescription(comments.getText().toString());
        expense.setProductOption(expenseName);
        BigDecimal totalExpense = new BigDecimal(openShift != null ? openShift.getTotalExpenses() : "0");
        if (expenseProductIDSelected == 3) {
            expense.setCashAmount(String.valueOf(amount));
            totalExpense = totalExpense.add(BigDecimal.valueOf(amount));
//            BigDecimal endingPettyCash = new BigDecimal(openShift.getEndingPettyCash()).add(BigDecimal.valueOf(amount));
//            openShift.setEndingPettyCash(String.valueOf(endingPettyCash));
        } else {
            expense.setCashAmount(String.valueOf(amount * -1));
            totalExpense = totalExpense.subtract(BigDecimal.valueOf(amount));
        }
        ShiftExpensesDAO.insertOrUpdate(expense);

        if (openShift != null) {
            openShift.setTotalExpenses(String.valueOf(totalExpense));
            BigDecimal beginningPettyCash = new BigDecimal(openShift.getBeginningPettyCash());
            BigDecimal transactionsCash = new BigDecimal(openShift.getTotalTransactionsCash());
            BigDecimal totalExpenses = ShiftExpensesDAO.getShiftTotalExpenses(openShift.getShiftId());
            BigDecimal totalEndingCash = Global.getRoundBigDecimal(beginningPettyCash.add(transactionsCash).add(totalExpenses), 2);
            openShift.setTotal_ending_cash(String.valueOf(totalEndingCash));
            openShift.setTotalExpenses(String.valueOf(totalExpenses));
            ShiftDAO.insertOrUpdate(openShift);
        }
        Toast.makeText(this, "Expense Added", Toast.LENGTH_LONG).show();
        showPrintDlg(expense);
    }

    private class PrintReceiptTask extends AsyncTask<ShiftExpense, Void, ShiftExpense> {

        @Override
        protected ShiftExpense doInBackground(ShiftExpense... shiftExpenses) {
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
                Global.mainPrinterManager.getCurrentDevice().openCashDrawer();
                if (preferences.getPreferences(MyPreferences.pref_enable_printing)) {
                    Global.mainPrinterManager.getCurrentDevice().printExpenseReceipt(shiftExpenses[0]);
                }
            }
            return shiftExpenses[0];
        }

        @Override
        protected void onPostExecute(ShiftExpense expense) {
            if (preferences.isMultiplePrints()) {
                showPrintDlg(expense);
            }else {
                finish();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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


    private void showPrintDlg(final ShiftExpense expense) {
        final Dialog dlog = new Dialog(this, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        viewMsg.setText(R.string.dlog_msg_want_to_print);

        Button btnYes = dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = dlog.findViewById(R.id.btnDlogRight);
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);

        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);

        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                new PrintReceiptTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, expense);
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                finish();
            }
        });
        dlog.show();
    }
}
