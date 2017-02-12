package com.android.emobilepos.shifts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import java.util.Date;
import java.util.UUID;

/**
 * Created by tirizar on 1/5/2016.
 */
public class ShiftExpense_FA extends BaseFragmentActivityActionBar implements View.OnClickListener {
    private Activity activity;
    private boolean hasBeenCreated = false;
    private Intent intent;
    private ProductsHandler productExpenses;
    private String[] theSpinnerNames;
    private int[] theSpinnerValues;
    private int expenseProductIDSelected = 0;
    private String expenseName = "";
    AdapterView.OnItemSelectedListener onItemSelectedListenerSpinner =
            new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    expenseName = (String) parent.getItemAtPosition(position);
                    //TextView textViewSelected;

                    //textViewSelected =(TextView)findViewById(R.id.textViewSelected);
                    //map the selected name and find the product ID value from the array
                    expenseProductIDSelected = theSpinnerValues[position];
                    //textViewSelected.setText("Product id selected: " + expenseProductIDSelected);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            };
    private EditText cashAmount;
    private NumberUtils numberUtils = new NumberUtils();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shift_add_expense);
        activity = this;
        cashAmount = (EditText) findViewById(R.id.cashAmount);
        Button btnCancel = (Button) findViewById(R.id.buttonCancel);
        btnCancel.setOnClickListener(this);
        Button btnSubmit = (Button) findViewById(R.id.buttonSubmit);
        btnSubmit.setOnClickListener(this);
        productExpenses = new ProductsHandler(activity);
//        productExpensesCursor = productExpenses.getProductsTypeExpense();
//        productExpensesCursor.moveToFirst();
        theSpinnerNames = getResources().getStringArray(R.array.expenseTypes);
        //new String[productExpensesCursor.getCount()];
        theSpinnerValues = new int[theSpinnerNames.length];
        int i = 0;
        for (String str : theSpinnerNames) {
            theSpinnerValues[i] = i + 1; //get the expense ids
        }
//        while (!productExpensesCursor.isAfterLast()) {
//            theSpinnerValues[i] = productExpensesCursor.getString(0); //get the expense ids
//            theSpinnerNames[i] = productExpensesCursor.getString(1); //get the expense name
//            i++;
//            productExpensesCursor.moveToNext();
//        }

        Spinner spinnerView = (Spinner) findViewById(R.id.expenseSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, theSpinnerNames);
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
//        String spID = openShift.getShiftId();
//        ShiftExpensesDBHandler shiftExpensesDBHandler = new ShiftExpensesDBHandler(activity);
        double theAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(cashAmount));
        ShiftExpense expense = new ShiftExpense();
        expense.setCashAmount(String.valueOf(theAmount));
        expense.setProductId(expenseProductIDSelected);
        expense.setExpenseId(UUID.randomUUID().toString());
        expense.setProductName(expenseName);
        expense.setShiftId(openShift.getShiftId());
        expense.setCreationDate(now);
        expense.setProductOption(expenseName);
        ShiftExpensesDAO.insertOrUpdate(expense);
//        shiftExpensesDBHandler.insert(expenseProductIDSelected, expenseName, theAmount, spID);
        Toast.makeText(activity, "Expense Added", Toast.LENGTH_LONG).show();
        finish();
    }

}
