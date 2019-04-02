package com.android.emobilepos.mainmenu;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.android.emobilepos.BuildConfig;
import com.android.emobilepos.R;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.support.MyPreferences;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Locale;

public class AboutTab_FR extends Fragment implements OnClickListener {

    private long _last_time = 0;
    private long _time_difference = 0;
    private int counter = 0;
    private boolean deleteIsRunning = false;
    private ImageView posLogo;
    TextView footerText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.about_layout, container, false);
        AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee();
        MyPreferences myPref = new MyPreferences(getActivity());
        TextView acctNumber = view.findViewById(R.id.acctNum);
        TextView employee = view.findViewById(R.id.employeeNameID);
        TextView version = view.findViewById(R.id.versionID);
        TextView androidVersion = view.findViewById(R.id.androidVersionID);
        TextView copyright = view.findViewById(R.id.copyrightText);
        TextView deviceName = view.findViewById(R.id.deviceModelText);
        deviceName.setText(Build.MODEL);
        posLogo = view.findViewById(R.id.aboutMainLogo);
        posLogo.setOnClickListener(this);
        acctNumber.setText(myPref.getAcctNumber());
        if (assignEmployee != null) {
            employee.setText(String.format(Locale.getDefault(),
                    "%s (%d)", assignEmployee.getEmpName(), assignEmployee.getEmpId()));
        } else {
            employee.setText(getString(R.string.unknown));
        }
        version.setText(BuildConfig.VERSION_NAME);
        androidVersion.setText(Build.VERSION.RELEASE);
        copyright.setText(String.format(getString(R.string.about_copyright),
                String.valueOf(Calendar.getInstance().get(Calendar.YEAR))));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        footerText = view.findViewById(R.id.footerText);
        new TLSTesterTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
            Toast.makeText(getActivity(), "Data being deleted", Toast.LENGTH_LONG).show();

        }

        @Override
        protected Void doInBackground(Void... params) {
            DBManager dbManager = new DBManager(getActivity());
            dbManager.deleteAllTablesData();
            return null;
        }

        protected void onPostExecute(Void unused) {
            Toast.makeText(getActivity(), "Data was deleted", Toast.LENGTH_LONG).show();
        }

    }

    private class TLSTesterTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            HttpURLConnection httpURLConnection = null;
            int responseCode = 0;
            try {
                httpURLConnection = oauthclient.HttpClient.getHttpURLConnection("https://bo.enablermobile.com/App_Themes/BONewDesign/images/login/EMobileLogo_login.png", oauthclient.HttpClient.HTTPMethod.GET);
                responseCode = httpURLConnection.getResponseCode();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return responseCode;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == 200) {
                footerText.setText(String.format("%s TLS2", getString(R.string.about_footer)));
            }
        }
    }
}