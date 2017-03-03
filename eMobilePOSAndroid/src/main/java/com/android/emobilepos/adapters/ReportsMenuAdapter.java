package com.android.emobilepos.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.android.dao.PayMethodsDAO;
import com.android.database.PayMethodsHandler;
import com.android.database.PaymentsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.realms.PaymentMethod;
import com.android.support.Global;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;

public class ReportsMenuAdapter extends BaseAdapter implements Filterable {

    private LayoutInflater mInflater;
    private String[] curDate;


    private int offset = 3;                    //will include 2 dividers,the report date, and the total.
    private String reportDate;
    private String granTotal = "0.00";
    private Map<String, String[]> hashedReport;
    private List<PaymentMethod> paymentMethods;
    private Context activity;
    private Resources resource;


    public ReportsMenuAdapter(Context activity, String[] date) {
        if (activity != null) {
            mInflater = LayoutInflater.from(activity.getApplicationContext());
            this.activity = activity;
            this.curDate = date;
            resource = activity.getResources();
            hashedReport = createReportMap();

            reportDate = resource.getString(R.string.report_but_title) + "\n\n" + curDate[0];
        }
    }


    private Map<String, String[]> createReportMap() {
        HashMap<String, String[]> result = new HashMap<>();
        String[] labelLegend = new String[]{resource.getString(R.string.report_payments), resource.getString(R.string.report_refunds), "Total"};
        PayMethodsHandler methodsHandler = new PayMethodsHandler(activity);
        paymentMethods = PayMethodsDAO.getAllSortByName(false);
        int size = paymentMethods.size();
        result.put("Method", labelLegend);
        String[][] amounts = new String[size][3];
        int size2 = size * 3;
        int count = 0, count2 = 0;
        PaymentsHandler paymentHandler = new PaymentsHandler(activity);
        String date = curDate[1];
        String tempVal;
        DecimalFormat frmt = new DecimalFormat("0.00");
        for (int i = 0; i < size2; i++) {
            switch (count2) {
                case 0:                    //payments
                {
                    tempVal = paymentHandler.getTotalPayAmount(paymentMethods.get(count).getPaymethod_id(), date);
                    if (tempVal.contains(".")) {
                        tempVal = frmt.format(Double.parseDouble(tempVal));
                    } else {
                        tempVal = frmt.format((double) Integer.parseInt(tempVal));
                    }
                    amounts[count][count2] = tempVal;
                    break;
                }
                case 1:                    //refunds
                {
                    tempVal = paymentHandler.getTotalRefundAmount(paymentMethods.get(count).getPaymethod_id(), date);
                    if (tempVal.contains("."))
                        tempVal = frmt.format(Double.parseDouble(tempVal));
                    else
                        tempVal = frmt.format((double) Integer.parseInt(tempVal));
                    amounts[count][count2] = tempVal;
                    break;
                }
                case 2:                    //Total
                {
                    amounts[count][count2] = Global.addSubsStrings(false, amounts[count][count2 - 2], amounts[count][count2 - 1]);
                    granTotal = Global.addSubsStrings(true, granTotal, amounts[count][count2]);
                    break;
                }
            }

            if ((i + 1) % 3 == 0) {
                result.put(paymentMethods.get(count).getPaymethod_id(), amounts[count]);
                count++;
                count2 = 0;
            } else {
                count2++;
            }
        }
        return Collections.unmodifiableMap(result);
    }


    @Override
    public int getCount() {
        return hashedReport.size() + offset;
    }

    @Override
    public Object getItem(int index) {
        return null;
    }

    @Override
    // use the 'position' or array index as item id
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int type = getItemViewType(position);
        if (convertView == null) {
            holder = new ViewHolder();
            switch (type) {
                case 0: {
                    convertView = mInflater.inflate(R.layout.report_listviewheader, null);
                    holder.textLine = (TextView) convertView.findViewById(R.id.reportHeader);
                    setHolderValues(type, position, holder);
                    break;
                }
                case 1:        //transaction divider
                {
                    convertView = mInflater.inflate(R.layout.report_listviewdivider, null);
                    holder.textLine = (TextView) convertView.findViewById(R.id.reportTitle);
                    setHolderValues(type, position, holder);
                    break;
                }
                case 2:                //transaction content
                {
                    convertView = mInflater.inflate(R.layout.report_listviewadapter, null);
                    holder.textLine = (TextView) convertView.findViewById(R.id.reportLeft);
                    holder.rightOne = (TextView) convertView.findViewById(R.id.reportRight);
                    holder.rightTwo = (TextView) convertView.findViewById(R.id.reportRight2);
                    holder.rightThree = (TextView) convertView.findViewById(R.id.reportRight3);
                    setHolderValues(type, position, holder);
                    break;
                }
                case 3: {
                    convertView = mInflater.inflate(R.layout.reports_listviewadapter2, null);
                    holder.textLine = (TextView) convertView.findViewById(R.id.reportRight);
                    setHolderValues(type, position, holder);

                    break;
                }
            }
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
            setHolderValues(type, position, holder);
        }
        return convertView;
    }

    private void setHolderValues(int type, int position, ViewHolder holder) {
        switch (type) {
            case 0: {
                holder.textLine.setText(reportDate);
                break;
            }
            case 1: {
                holder.textLine.setText(resource.getString(R.string.report_transactions));
                break;
            }
            case 2: {
                String[] rightVal;

                if (position == 2) {
                    holder.textLine.setText(resource.getString(R.string.report_methods));
                    rightVal = hashedReport.get("Method");
                    holder.rightOne.setText(rightVal[0]);
                    holder.rightTwo.setText(rightVal[1]);
                    holder.rightThree.setText(rightVal[2]);
                } else {
                    holder.textLine.setText(paymentMethods.get(position - 3).getPaymethod_name());
                    rightVal = hashedReport.get(paymentMethods.get(position - 3).getPaymethod_id());

                    holder.rightOne.setText(Global.getCurrencyFormat(rightVal[0]));
                    holder.rightTwo.setText(Global.getCurrencyFormat(rightVal[1]));
                    holder.rightThree.setText(Global.getCurrencyFormat(rightVal[2]));
                }
                break;
            }
            case 3: {
                holder.textLine.setText(Global.getCurrencyFormat(granTotal));
                break;
            }
        }
    }

    public class ViewHolder {
        TextView textLine;
        TextView rightOne;
        TextView rightTwo;
        TextView rightThree;

    }

    @Override
    public int getItemViewType(int position) {

        if (position == 0)                                                    //report date
            return 0;
        else if (position == 1)                                            //transaction divider
            return 1;
        else if (position >= 2 && position < hashedReport.size() + 2)            //display all transactions
            return 2;

        else                        //show gran total
            return 3;

    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public Filter getFilter() {
        return null;
    }
}
