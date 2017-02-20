package com.android.emobilepos.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.emobilepos.R;
import com.android.emobilepos.models.realms.ShiftExpense;

import java.util.List;

/**
 * Created by guarionex on 02-11-17.
 */

public class ShiftExpensesListAdapter extends ArrayAdapter<ShiftExpense> {

    private Context context;
    private int resource;
    private List<ShiftExpense> shiftExpenses;

    public ShiftExpensesListAdapter(Context context, int resource, List<ShiftExpense> shiftExpenses) {
        super(context, resource, shiftExpenses);
        this.context = context;
        this.resource = resource;
        this.shiftExpenses = shiftExpenses;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Holder holder;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.shift_expenses_lvadapter, parent, false);

            holder = new Holder();
            holder.productName = (TextView) row.findViewById(R.id.productName);
            holder.amount = (TextView) row.findViewById(R.id.cAmount);
            holder.expenseID = (TextView) row.findViewById(R.id.expenseID);
            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }

        ShiftExpense expense = shiftExpenses.get(position);
        holder.productName.setText(expense.getProductName());
        holder.amount.setText(expense.getCashAmount());
        holder.expenseID.setText(String.format("Expense ID: %s", expense.getExpenseId()));

        return row;
    }

    static class Holder {
        TextView productName, amount, expenseID;
    }
}
