package com.android.emobilepos.shifts;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.android.dao.ShiftDAO;
import com.android.dao.ShiftExpensesDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.adapters.ShiftExpensesListAdapter;
import com.android.emobilepos.models.realms.Shift;
import com.android.emobilepos.models.realms.ShiftExpense;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.util.List;

/**
 * Created by tirizar on 1/5/2016.
 */
public class ShiftExpensesList_FA extends BaseFragmentActivityActionBar implements View.OnClickListener {
    //    private CustomCursorAdapter adapter;
    ShiftExpensesListAdapter adapter;
    //    private Activity activity;
    //    private Cursor expensesByShift;
    private MyPreferences myPref;
    //    private ShiftExpensesDBHandler shiftExpensesDBHandler;
//    private String spID;
    private ListView lView;
    //    private Global global;
    private List<ShiftExpense> expenses;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shift_expenses_list);
//        activity = this;
        myPref = new MyPreferences(this);
//        spID = myPref.getShiftID();
//        shiftExpensesDBHandler = new ShiftExpensesDBHandler(activity);
//        global = (Global) getApplication();
        Button btnProcess = (Button) findViewById(R.id.processAddExpenseButton);
        btnProcess.setOnClickListener(this);
        lView = (ListView) findViewById(R.id.shiftExpensesListView);
        //get cursor with expenses for this shift
//        expensesByShift = shiftExpensesDBHandler.getShiftExpenses(spID);
        //bing expenses to list view
//        adapter = new CustomCursorAdapter(activity, expensesByShift, CursorAdapter.NO_SELECTION);
        Shift openShift = ShiftDAO.getOpenShift(Integer.parseInt(myPref.getShiftID()));
        expenses = ShiftExpensesDAO.getShiftExpenses(openShift.getShiftId());
        adapter = new ShiftExpensesListAdapter(this, R.layout.shift_expenses_lvadapter, expenses);
        lView.setAdapter(adapter);
//        hasBeenCreated = true;
    }


    @Override
    public void onResume() {
        super.onResume();
        //get cursor with expenses for this shift
//        expensesByShift = shiftExpensesDBHandler.getShiftExpenses(spID);
        //bing expenses to list view
        adapter = new ShiftExpensesListAdapter(this, R.layout.shift_expenses_lvadapter, expenses);
        lView.setAdapter(adapter);
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

//    public class CustomCursorAdapter extends CursorAdapter {
//        LayoutInflater inflater;
//        ViewHolder myHolder;
//        String temp = new String();
//        String empStr = "";
//
//        public CustomCursorAdapter(Context context, Cursor c, int flags) {
//            super(context, c, flags);
//            inflater = LayoutInflater.from(context);
//        }
//
//        @Override
//        public void bindView(View view, Context context, Cursor cursor) {
//            myHolder = (ViewHolder) view.getTag();
//            myHolder.productName.setText(cursor.getString(myHolder.i_productName));
//
//            temp = cursor.getString(myHolder.i_cAmount);
//            if (temp == null && !temp.isEmpty())
//                temp = empStr;
//            else
//                temp = Global.formatDoubleStrToCurrency(temp);
//
//            myHolder.cAmount.setText(temp);
//            temp = cursor.getString(myHolder.i_expenseID);
//            myHolder.expenseID.setText("Expense ID: " + temp);
//        }
//
//        @Override
//        public View newView(Context context, Cursor cursor, ViewGroup parent) {
//            View retView = inflater.inflate(R.layout.shift_expenses_lvadapter, parent, false);
//            ViewHolder holder = new ViewHolder();
//            holder.productName = (TextView) retView.findViewById(R.id.productName);
//            holder.cAmount = (TextView) retView.findViewById(R.id.cAmount);
//            holder.expenseID = (TextView) retView.findViewById(R.id.expenseID);
//            holder.i_productName = cursor.getColumnIndex("productName");
//            holder.i_cAmount = cursor.getColumnIndex("cashAmount");
//            holder.i_expenseID = cursor.getColumnIndex("_id");
//            retView.setTag(holder);
//            return retView;
//        }
//
//        private class ViewHolder {
//            TextView productID, productName, cAmount, expenseID, shiftPeriodID;
//            int i_productID, i_productName, i_cAmount, i_expenseID, i_shiftPeriodID;
//        }
//    }
}
