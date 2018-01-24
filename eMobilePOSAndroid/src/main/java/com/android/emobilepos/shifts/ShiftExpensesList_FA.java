package com.android.emobilepos.shifts;

import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.android.dao.ShiftDAO;
import com.android.dao.ShiftExpensesDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.adapters.ShiftExpensesListAdapter;
import com.android.emobilepos.models.realms.Shift;
import com.android.emobilepos.models.realms.ShiftExpense;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.util.List;

/**
 * Created by tirizar on 1/5/2016.
 */
public class ShiftExpensesList_FA extends BaseFragmentActivityActionBar implements View.OnClickListener {
    ShiftExpensesListAdapter adapter;
    private MyPreferences myPref;
    private ListView lView;
    private List<ShiftExpense> expenses;
    private Shift openShift;
    private Global global;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shift_expenses_list);
        myPref = new MyPreferences(this);
        global = (Global) getApplication();
        Button btnProcess = findViewById(R.id.processAddExpenseButton);
        btnProcess.setOnClickListener(this);
        lView = findViewById(R.id.shiftExpensesListView);
        openShift = ShiftDAO.getOpenShift();
        expenses = ShiftExpensesDAO.getShiftExpenses(openShift.getShiftId());
        adapter = new ShiftExpensesListAdapter(this, R.layout.shift_expenses_lvadapter, expenses);
        lView.setAdapter(adapter);
    }


    @Override
    public void onResume() {
        super.onResume();
        expenses.clear();
        expenses.addAll(ShiftExpensesDAO.getShiftExpenses(openShift.getShiftId()));
        adapter.notifyDataSetChanged();
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.processAddExpenseButton:
                Intent intent = new Intent(this, ShiftExpense_FA.class);
                startActivity(intent);
                break;
        }
    }
}
