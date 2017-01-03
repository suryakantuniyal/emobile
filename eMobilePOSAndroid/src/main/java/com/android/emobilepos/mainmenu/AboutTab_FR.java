package com.android.emobilepos.mainmenu;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dao.AssignEmployeeDAO;
import com.android.database.DBManager;
import com.android.emobilepos.R;
import com.android.emobilepos.models.AssignEmployee;
import com.android.support.MyPreferences;

public class AboutTab_FR extends Fragment implements OnClickListener {

    private long _last_time = 0;
    private long _time_difference = 0;
    private int counter = 0;
    private Activity activity;
    private boolean deleteIsRunning = false;
    private ImageView posLogo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.about_layout, container, false);
        AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee();
        activity = getActivity();
        MyPreferences myPref = new MyPreferences(getActivity());
        TextView acctNumber = (TextView) view.findViewById(R.id.acctNum);
        TextView employee = (TextView) view.findViewById(R.id.employeeNameID);
        TextView version = (TextView) view.findViewById(R.id.versionID);
        TextView deviceName = (TextView) view.findViewById(R.id.deviceModelText);
        deviceName.setText(Build.MODEL);
        posLogo = (ImageView) view.findViewById(R.id.aboutMainLogo);
        posLogo.setOnClickListener(this);
        acctNumber.setText(myPref.getAcctNumber());
        employee.setText(assignEmployee.getEmpName() + " (" + assignEmployee.getEmpId() + ")");
        version.setText(myPref.getBundleVersion());

        return view;
    }

    @Override
    public void onClick(View v) {
        if (!deleteIsRunning) {
            if (_last_time != 0) {
                _time_difference = System.currentTimeMillis() - _last_time;
            }

            if (_time_difference < 500) {
                if (counter == 12) {
                    posLogo.setOnClickListener(null);
                    deleteIsRunning = true;
                    new deleteTablesAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    _last_time = 0;
                    deleteIsRunning = false;
                    posLogo.setOnClickListener(this);
                } else
                    counter++;
            } else
                counter = 0;

            _last_time = System.currentTimeMillis();
        }
    }


    private class deleteTablesAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            Toast.makeText(activity, "Data being deleted", Toast.LENGTH_LONG).show();

        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            DBManager dbManager = new DBManager(activity);
            dbManager.deleteAllTablesData();
            return null;
        }

        protected void onPostExecute(Void unused) {
            Toast.makeText(activity, "Data was deleted", Toast.LENGTH_LONG).show();
        }

    }

}
