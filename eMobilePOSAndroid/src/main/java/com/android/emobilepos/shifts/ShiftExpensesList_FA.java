package com.android.emobilepos.shifts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.database.ShiftExpensesDBHandler;
import com.android.emobilepos.R;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

/**
 * Created by tirizar on 1/5/2016.
 */
public class ShiftExpensesList_FA extends BaseFragmentActivityActionBar implements View.OnClickListener {
    private Activity activity;
    private boolean hasBeenCreated = false;
    private Intent intent;
    private Cursor expensesByShift;
    private MyPreferences myPref;
    private ShiftExpensesDBHandler shiftExpensesDBHandler;
    private String spID;
    private ListView lView;
    private CustomCursorAdapter adapter;
    private Global global;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shift_expenses_list);
        activity = this;
        myPref = new MyPreferences(this);
        spID = myPref.getShiftID();
        shiftExpensesDBHandler = new ShiftExpensesDBHandler(activity);

        global = (Global)getApplication();
        Button btnProcess = (Button) findViewById(R.id.processAddExpenseButton);
        btnProcess.setOnClickListener(this);

        lView = (ListView)findViewById(R.id.shiftExpensesListView);

        //get cursor with expenses for this shift
        expensesByShift = shiftExpensesDBHandler.getShiftExpenses(spID);

        //bing expenses to list view
        adapter = new CustomCursorAdapter(activity, expensesByShift, CursorAdapter.NO_SELECTION);
        lView.setAdapter(adapter);


//        int i = 0;
//        String expName;
//        while (!expensesByShift.isAfterLast()) {
//            expName = expensesByShift.getString(0); //get the expense id
//            //theSpinnerNames[i] = productExpensesCursor.getString(2); //get if expense
//            i++;
//            expensesByShift.moveToNext();
//        }
        hasBeenCreated = true;

    }


    @Override
    public void onResume() {

//        if(global.isApplicationSentToBackground(activity))
//            global.loggedIn = false;
//        global.stopActivityTransitionTimer();
//
//        if(hasBeenCreated&&!global.loggedIn)
//        {
//            if(global.getGlobalDlog()!=null)
//                global.getGlobalDlog().dismiss();
//            global.promptForMandatoryLogin(activity);
//        }
        super.onResume();
        //get cursor with expenses for this shift
        expensesByShift = shiftExpensesDBHandler.getShiftExpenses(spID);

        //bing expenses to list view
        adapter = new CustomCursorAdapter(activity, expensesByShift, CursorAdapter.NO_SELECTION);
        lView.setAdapter(adapter);
    }


//    @Override
//    protected void onResume(){
//        expensesByShift = shiftExpensesDBHandler.getShiftExpenses(spID);
//
//        int i = 0;
//        String expName;
//        while (!expensesByShift.isAfterLast()) {
//            expName = expensesByShift.getString(0); //get the expense id
//            //theSpinnerNames[i] = productExpensesCursor.getString(2); //get if expense
//            i++;
//            expensesByShift.moveToNext();
//        }
//    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.processAddExpenseButton:
//                Toast.makeText(activity, "Processing Add Expense", Toast.LENGTH_LONG).show();
                intent = new Intent(activity, ShiftExpense_FA.class);
                startActivity(intent);
                break;
        }
    }

    public class CustomCursorAdapter extends CursorAdapter {
        LayoutInflater inflater;
        ViewHolder myHolder;
        String temp = new String();
        String empStr = "";

        public CustomCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            // TODO Auto-generated constructor stub
            inflater = LayoutInflater.from(context);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // TODO Auto-generated method stubs

            myHolder = (ViewHolder)view.getTag();

//            temp = cursor.getString(myHolder.i_cust_name);
//            if(temp==null)
//                temp = empStr;
//            if(!temp.isEmpty())
//                temp=" ("+temp+")";
            myHolder.productName.setText(cursor.getString(myHolder.i_productName));

            temp = cursor.getString(myHolder.i_cAmount);
            if(temp==null&&!temp.isEmpty())
                temp = empStr;
            else
                temp = Global.formatDoubleStrToCurrency(temp);

            myHolder.cAmount.setText(temp);


            temp = cursor.getString(myHolder.i_expenseID);
//            if(temp==null&&!temp.isEmpty())
//                temp = empStr;
//            else
//                temp = Global.formatDoubleStrToCurrency(temp);

            myHolder.expenseID.setText("Expense ID: " + temp);

//            if(cursor.getString(myHolder.i_pay_issync).equals("1"))//it is synch
//                myHolder.iconImage.setImageResource(R.drawable.is_sync);
//            else
//                myHolder.iconImage.setImageResource(R.drawable.is_not_sync);
//
//            if(cursor.getString(myHolder.i_isVoid).equals("0"))//is not VOID
//                myHolder.voidText.setVisibility(View.INVISIBLE);
//            else
//                myHolder.voidText.setVisibility(View.VISIBLE);
//
//            myHolder.tip.setText("(Tip: "+Global.formatDoubleStrToCurrency(cursor.getString(myHolder.i_pay_tip))+")");


        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            // TODO Auto-generated method stub

            View retView = inflater.inflate(R.layout.shift_expenses_lvadapter, parent, false);

            ViewHolder holder = new ViewHolder();
            holder.productName = (TextView) retView.findViewById(R.id.productName);
            holder.cAmount = (TextView)retView.findViewById(R.id.cAmount);
            holder.expenseID = (TextView) retView.findViewById(R.id.expenseID);
            // holder.voidText = (TextView)retView.findViewById(R.id.histpayVoidText);
            //holder.iconImage = (ImageView)retView.findViewById(R.id.histpayIcon);

            holder.i_productName = cursor.getColumnIndex("productName");
            holder.i_cAmount = cursor.getColumnIndex("cashAmount");
            holder.i_expenseID = cursor.getColumnIndex("_id");
            //holder.i_shiftPeriodID = cursor.getColumnIndex("shiftPeriodID");
            //holder.i_productID = cursor.getColumnIndex("productID");


            retView.setTag(holder);

            return retView;
        }


        private class ViewHolder
        {
            //TextView title,amount,voidText,tip;
            TextView productID, productName, cAmount, expenseID, shiftPeriodID;

            //ImageView iconImage;

            int i_productID,i_productName,i_cAmount,i_expenseID,i_shiftPeriodID;
        }
    }
}
