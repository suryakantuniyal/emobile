package com.android.emobilepos.initialization;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.dao.AssignEmployeeDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.saxhandler.SaxAllEmployeesHandler;
import com.android.saxhandler.SaxLoginHandler;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Post;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import util.json.JsonUtils;

public class SelectEmployee_FA extends BaseFragmentActivityActionBar {
    private List<String> empName = new ArrayList<>();
    private List<String> empID = new ArrayList<>();
    private ProgressDialog myProgressDialog;
    MyPreferences preferences;
    private int error_msg_id = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initialization_select_employee);
        preferences = new MyPreferences(this);
        new validateEmployeesAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
    }

    public class validateEmployeesAsync extends AsyncTask<String, String, String> {
        boolean succeeded = false;
        String errorMsg = "";

        @Override
        protected void onPreExecute() {
            Global.lockOrientation(SelectEmployee_FA.this);
            myProgressDialog = new ProgressDialog(SelectEmployee_FA.this);
            myProgressDialog.setMessage(getString(R.string.loading));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            Post post = new Post(SelectEmployee_FA.this);
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SaxLoginHandler handler = new SaxLoginHandler();

            try {
                String xml = post.postData(1, "");
                InputSource inSource = new InputSource(new StringReader(xml));
                SAXParser sp = spf.newSAXParser();
                XMLReader xr = sp.getXMLReader();
                xr.setContentHandler(handler);
                xr.parse(inSource);
                boolean deviceID = Boolean.parseBoolean(handler.getData().toLowerCase(Locale.getDefault()));
                if (!deviceID) {
                    xml = post.postData(3, "");
                    SaxAllEmployeesHandler hdl = new SaxAllEmployeesHandler();
                    inSource = new InputSource(new StringReader(xml));
                    xr.setContentHandler(hdl);
                    xr.parse(inSource);
                    empName = hdl.getEmpName();
                    empID = hdl.getEmpId();
                    succeeded = true;
                } else
                    errorMsg = "The provided information could not be validated. Please try again.";

            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            Global.dismissDialog(SelectEmployee_FA.this, myProgressDialog);
            Global.releaseOrientation(SelectEmployee_FA.this);
            if (succeeded) {
                ListView myListView = findViewById(R.id.employeeListView);
                ListViewAdapter myAdapter = new ListViewAdapter(SelectEmployee_FA.this);
                myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                        promptValidate(position);

                    }
                });

                Button reload = findViewById(R.id.reloadButton);
                reload.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        new validateEmployeesAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
                    }
                });

                myListView.setAdapter(myAdapter);
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SelectEmployee_FA.this);
                dialog.setTitle(getString(R.string.dlog_title_error));
                dialog.setMessage(errorMsg);
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                });
                dialog.create().show();
            }
        }
    }

    private class selectEmployeesAsync extends AsyncTask<String, String, String> {
        boolean succeeded = false;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(SelectEmployee_FA.this);
            myProgressDialog.setMessage(getString(R.string.loading));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            if (AssignEmployees() && getFirstAvailLicense() && DisableEmployee() && DownloadPayID()) {
                succeeded = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();
            if (succeeded) {
                Intent intent = new Intent(SelectEmployee_FA.this, SelectPassword_FA.class);
                startActivityForResult(intent, 0);
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SelectEmployee_FA.this);
                dialog.setTitle("Error");
                dialog.setMessage(error_msg_id);
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialog.create().show();
            }
        }
    }

    String _license = "";

    public boolean AssignEmployees() {
        Post post = new Post(this);
        try {
            String xml = post.postData(Global.S_GET_ASSIGN_EMPLOYEES, "");
            Gson gson = JsonUtils.getInstance();
            Type listType = new com.google.gson.reflect.TypeToken<List<AssignEmployee>>() {
            }.getType();
            List<AssignEmployee> assignEmployees = gson.fromJson(xml, listType);
            AssignEmployeeDAO.insertAssignEmployee(assignEmployees);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            error_msg_id = R.string.dlog_msg_error_downloading_employee_data;
        }
        return false;
    }

    private boolean getFirstAvailLicense() {
        Post post = new Post(this);
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SaxLoginHandler handler = new SaxLoginHandler();
        try {
            String xml = post.postData(2, "");
            InputSource inSource = new InputSource(new StringReader(xml));
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(handler);
            xr.parse(inSource);
            inSource = new InputSource(new StringReader(xml));
            xr.parse(inSource);
            if (!handler.getData().isEmpty() && !handler.getData().equals("0")) {
                _license = handler.getData();
                MyPreferences myPref = new MyPreferences(this);
                myPref.setActivKey(handler.getData());
                return true;
            } else {
                error_msg_id = R.string.dlog_msg_error_no_avail_license;
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            error_msg_id = R.string.dlog_msg_error_no_avail_license;
        }
        return false;
    }

    public boolean DisableEmployee() {
        Post post = new Post(this);
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SaxLoginHandler handler = new SaxLoginHandler();
        try {
            String xml = post.postData(5, "");
            InputSource inSource = new InputSource(new StringReader(xml));
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(handler);
            xr.parse(inSource);
            return Boolean.parseBoolean(handler.getData());
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            error_msg_id = R.string.dlog_msg_error_failed_disable_employee;
        }
        return false;
    }

    public boolean DownloadPayID() {
        Post post = new Post(this);
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SaxLoginHandler handler = new SaxLoginHandler();
        try {
            String xml = post.postData(6, "");
            InputSource inSource = new InputSource(new StringReader(xml));
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(handler);
            xr.parse(inSource);

            if (!handler.getData().isEmpty()) {
                MyPreferences myPref = new MyPreferences(this);
                myPref.setLastPayID(handler.getData());
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            error_msg_id = R.string.dlog_msg_error_failed_download_pay_id;
        }
        return false;
    }

    public class ListViewAdapter extends BaseAdapter {
        private LayoutInflater myInflater;

        ListViewAdapter(Context context) {
            myInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return empName.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = myInflater.inflate(R.layout.select_customer_adapter, null);
                holder.employeeName = convertView.findViewById(R.id.employeeName);
                holder.employeeName.setText(
                        String.format("%s (%s)", empName.get(position), empID.get(position)));

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();

                holder.employeeName.setText(
                        String.format("%s (%s)", empName.get(position), empID.get(position)));
            }
            return convertView;
        }

        public class ViewHolder {
            TextView employeeName;
        }
    }

    private void promptValidate(final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.login_select_this_employee));
        dialog.setMessage(empName.get(position));
        dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
//                MyPreferences myPref = new MyPreferences(activity);
//                myPref.setEmpID(empID.get(position));
                AssignEmployee assignEmployee = new AssignEmployee();
                assignEmployee.setEmpId(Integer.parseInt(empID.get(position)));
                try {
                    List<AssignEmployee> assignEmployees = new ArrayList<>();
                    assignEmployees.add(assignEmployee);
                    AssignEmployeeDAO.insertAssignEmployee(assignEmployees);
                    preferences.setEmpIdFromPreferences(String.valueOf(assignEmployee.getEmpId()));
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
                new selectEmployeesAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

            }
        });
        dialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {

            setResult(-1);
            finish();
        }
    }

    @Override
    protected void onPause() {
        if(myProgressDialog!=null && myProgressDialog.isShowing())
        {
            myProgressDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(myProgressDialog!=null && myProgressDialog.isShowing())
        {
            myProgressDialog.dismiss();
        }
        super.onDestroy();
    }
}
