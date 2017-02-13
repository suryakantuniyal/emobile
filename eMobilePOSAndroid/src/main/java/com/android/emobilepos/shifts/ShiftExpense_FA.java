package com.android.emobilepos.shifts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.dao.ShiftDAO;
import com.android.dao.ShiftExpensesDAO;
import com.android.database.ProductsHandler;
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
    private Activity activity;
    private boolean hasBeenCreated = false;
    private ProductsHandler productExpenses;
    private String[] theSpinnerNames;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shift_add_expense);
        activity = this;
        cashAmount = (EditText) findViewById(R.id.cashAmount);
        comments = (EditText) findViewById(R.id.expenseCommentseditText);

        Button btnCancel = (Button) findViewById(R.id.buttonCancel);
        btnCancel.setOnClickListener(this);
        Button btnSubmit = (Button) findViewById(R.id.buttonSubmit);
        btnSubmit.setOnClickListener(this);
        productExpenses = new ProductsHandler(activity);
        theSpinnerNames = getResources().getStringArray(R.array.expenseTypes);
        Spinner spinnerView = (Spinner) findViewById(R.id.expenseSpinner);
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
        hasBeenCreated = true;
    }

    @Override
    public void onClick(View v) {
        double theAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(cashAmount));
        switch (v.getId()) {
            case R.id.buttonCancel:
                activity.finish();
                break;
            case R.id.buttonSubmit:
                //verify valid amount
                if (theAmount <= 0) {
                    AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
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
        MyPreferences myPref;
        myPref = new MyPreferences(this);
        Shift openShift = ShiftDAO.getOpenShift(Integer.parseInt(myPref.getClerkID()));
        double amount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(cashAmount));
        ShiftExpense expense = new ShiftExpense();
        expense.setCashAmount(String.valueOf(amount));
        expense.setProductId(expenseProductIDSelected);
        expense.setExpenseId(UUID.randomUUID().toString());
        expense.setProductName(expenseName);
        expense.setShiftId(openShift != null ? openShift.getShiftId() : null);
        expense.setCreationDate(now);
        expense.setProductDescription(comments.getText().toString());
        expense.setProductOption(expenseName);
        ShiftExpensesDAO.insertOrUpdate(expense);
        BigDecimal totalExpense = new BigDecimal(openShift != null ? openShift.getTotalExpenses() : "0");
        totalExpense = expenseProductIDSelected == 3 ? totalExpense.add(BigDecimal.valueOf(amount))
                : totalExpense.subtract(BigDecimal.valueOf(amount));
        if (openShift != null) {
            openShift.setTotalExpenses(String.valueOf(totalExpense));
        }
        ShiftDAO.insertOrUpdate(openShift);
        Toast.makeText(activity, "Expense Added", Toast.LENGTH_LONG).show();
        if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null)
            Global.mainPrinterManager.getCurrentDevice().openCashDrawer();
        finish();
    }

}
