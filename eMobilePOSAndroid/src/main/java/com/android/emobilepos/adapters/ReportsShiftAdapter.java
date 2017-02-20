package com.android.emobilepos.adapters;


import android.app.Activity;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.android.dao.ShiftDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.models.realms.Shift;
import com.android.support.DateUtils;
import com.android.support.Global;

import java.util.Date;
import java.util.List;


public class ReportsShiftAdapter extends BaseAdapter implements Filterable {

    private List<Shift> shifts;
    private LayoutInflater mInflater;
    private String[] curDate;


    private int offset = 2;                    //will include 2 dividers,the report date, and the total.
    private String reportDate;
    //	private Cursor myCursor;
    private int listViewSize;
    private Activity activity;
    private Resources resource;
    private String temp;


    public ReportsShiftAdapter(Activity activity, String[] date) {
        if (activity != null) {
            mInflater = LayoutInflater.from(activity.getApplicationContext());
            this.curDate = date;
            resource = activity.getResources();
            this.activity = activity;
            reportDate = resource.getString(R.string.report_but_title) + "\n\n" + curDate[0];
//            ShiftPeriodsDBHandler handler = new ShiftPeriodsDBHandler(activity);
            shifts = ShiftDAO.getShift(DateUtils.getDateStringAsDate(curDate[1], DateUtils.DATE_yyyy_MM_dd));
//			myCursor =handler.getAllShiftsReport(curDate[1]);
            listViewSize = shifts.size(); //myCursor.getCount();
        }
    }

    public boolean findValue(int[] array, int position) {
        int size = array.length;

        for (int i = 0; i < size; i++) {
            if (array[i] == position) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getCount() {
        return listViewSize + offset;
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
                    holder.top = (TextView) convertView.findViewById(R.id.reportHeader);
                    setHolderValues(type, position, holder);
                    break;
                }
                case 1:        //transaction divider
                {
                    convertView = mInflater.inflate(R.layout.report_listviewdivider, null);
                    holder.top = (TextView) convertView.findViewById(R.id.reportTitle);
                    setHolderValues(type, position, holder);
                    break;
                }
                case 2:                //transaction content
                {
                    convertView = mInflater.inflate(R.layout.report_shift_lv_adapter, null);
                    Shift shift = shifts.get(position - offset);
//                    holder.i_startTime = myCursor.getColumnIndex("startTime");
//                    holder.i_end_type = myCursor.getColumnIndex("end_type");
//                    holder.i_assignee_name = myCursor.getColumnIndex("assignee_name");
//                    holder.i_beginning_petty_cash = myCursor.getColumnIndex("beginning_petty_cash");
                    holder.top = (TextView) convertView.findViewById(R.id.shiftPeriod);
                    holder.bottom = (TextView) convertView.findViewById(R.id.clerkName);

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
                holder.top.setText(reportDate);
                break;
            }
            case 1: {
                holder.top.setText(R.string.report_per_shift);
                break;
            }
            case 2: {
                Shift shift = shifts.get(position - offset);
                temp = shift.getShiftStatus().name();
                if (shift.getShiftStatus() == Shift.ShiftStatus.CLOSED)
                    temp = DateUtils.getDateAsString(shift.getEndTime(), DateUtils.DATE_yyyy_MM_dd);
                holder.top.setText(DateUtils.getDateAsString(shift.getStartTime(), DateUtils.DATE_yyyy_MM_dd) + " - " + temp);
                holder.bottom.setText(shift.getAssigneeName() + " - " +
                        Global.formatDoubleStrToCurrency(shift.getBeginningPettyCash()));
                break;
            }

        }
    }

    public class ViewHolder {
        TextView top, bottom;
//        int i_startTime, i_end_type, i_assignee_name, i_beginning_petty_cash;

    }

    public String getShiftID(int position) {
        return shifts.get(position - offset).getShiftId();
//        return myCursor.getString(myCursor.getColumnIndex("shift_id"));
    }

    @Override
    public int getItemViewType(int position) {

        if (position == 0)                                                    //report date
            return 0;
        else if (position == 1)                                            //transaction divider
            return 1;
        /*else if(position>=2&&position<listViewSize+2)			//display all transactions
            return 2;
		
		else 						//show gran total
			return 3;*/
        else
            return 2;

    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public Filter getFilter() {
        // TODO Auto-generated method stub
        return null;
    }
}
