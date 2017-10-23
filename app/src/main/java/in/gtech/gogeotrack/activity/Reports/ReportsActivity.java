package in.gtech.gogeotrack.activity.Reports;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import in.gtech.gogeotrack.R;
import in.gtech.gogeotrack.activity.SessionHandler;
import in.gtech.gogeotrack.adapter.SimpleListAdapter;
import in.gtech.gogeotrack.api.APIServices;
import in.gtech.gogeotrack.network.DetailResponseCallback;
import in.gtech.gogeotrack.utils.URLContstant;

/**
 * Created by silence12 on 4/7/17.
 */

public class ReportsActivity extends AppCompatActivity {
    Spinner reportSpinner;
    private List<String> reportType = new ArrayList<>();
    private List<String> deviceName = new ArrayList<>();
    private List<Integer> deviceId = new ArrayList<>();

    int positionId;
    int newDeviceId,divIdReport;
    String reportType_String,spinnerValue,vhcleNoPopup_str,startDate_str,endDate_str,startTime_str,endTime_str;
    int vhcleNoPopup_int;
    Button confrigure_btn,genrateReprt_btn,submtConfig_btn;
    final Context context = this;
    TextView startDate_tv,endDate_tv,startTime_tv,endTime_tv,vehcleNo_tv;
    int dateTime;
    SharedPreferences mSharedPreferences;
    SimpleListAdapter simpleListAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.luncher_icon);
        setTitle("Reports");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);




        //listArrayList = new ArrayList<>();

//        reportSpinner = (Spinner)findViewById(R.id.reportSpinner);
        confrigure_btn = (Button)findViewById(R.id.configureReportBtn);
        genrateReprt_btn=(Button)findViewById(R.id.genrateReport_btn);

        //genrateReprt_btn.setEnabled(false);

        final MaterialBetterSpinner materialDesignSpinner = (MaterialBetterSpinner)
                findViewById(R.id.reportSpinner);
        materialDesignSpinner.setBackgroundResource(R.drawable.textviewoutline);
        reportType.add("Route");
        reportType.add("Events");
        reportType.add("Summary");
        reportType.add("Trips");

        String befor = materialDesignSpinner.getText().toString();
        Toast.makeText(context,befor,Toast.LENGTH_LONG).show();

        materialDesignSpinner.setSelection(0);

        materialDesignSpinner.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
               // spinnerValue = materialDesignSpinner.getText().toString();



            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                spinnerValue = materialDesignSpinner.getText().toString();
            }
        });

//        String Text = String.valueOf(reportSpinner.getSelectedItem());

        ArrayAdapter<String> reportAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,reportType);
       // reportAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        materialDesignSpinner.setAdapter(reportAdapter);
        materialDesignSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                reportType_String = parent.getItemAtPosition(position).toString();
                Log.d("valuestring",String.valueOf(reportType_String));

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
      /*  reportSpinner.setAdapter(reportAdapter);
        reportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reportType_String = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/
        confrigure_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReport();
            }
        });

        genrateReprt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinnerValue!=null && spinnerValue.equalsIgnoreCase("--Type--")){
                    Toast.makeText(context,"Please Select Report Type",Toast.LENGTH_LONG).show();
                }
                //getValue();
               /* if (spinnerValue.equals("--Type--"))
                {
                    Toast.makeText(context,"please configure first",Toast.LENGTH_LONG).show();
                }*/
/*
                vhcleNoPopup_str = vehcleNo_tv.getText().toString();
                startDate_str   = startDate_tv.getText().toString();
                endDate_str    = endDate_tv.getText().toString();
                startTime_str  = startTime_tv.getText().toString();
                endTime_str  = endTime_tv.getText().toString();
                divIdReport = positionId;*/

              /*  Log.d("vehclenO",vhcleNoPopup_str);
                Log.d("divIdReport",String.valueOf(divIdReport));
*/
               /* if ( vhcleNoPopup_str.equals(null)||startTime_str.equals(null)||endDate_str.equals(null)
                        || startTime_str.equals(null)||endTime_str.equals(null)){
                    Toast.makeText(context,"Please Go to Confifigure",Toast.LENGTH_LONG).show();
                }
*/
                if (spinnerValue.equals("Events")){
                    Intent evntIntnt = new Intent(context,EventesReportActivity.class);
                    getValue();
                    evntIntnt.putExtra("deviceName",vhcleNoPopup_str);
                    evntIntnt.putExtra("startdate",startDate_str);
                    evntIntnt.putExtra("endDate",endDate_str);
                    evntIntnt.putExtra("startTime",startTime_str);
                    evntIntnt.putExtra("endTime",endTime_str);
                    evntIntnt.putExtra("divReport",divIdReport);
                    startActivity(evntIntnt);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                }
                if (spinnerValue.equals("Trips")){
                    Intent intent = new Intent(context,TripsReportActiviy.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                }
                if (spinnerValue.equals("Summary")){
                    Intent intent = new Intent(context,SummaryReportActivity.class);
                    getValue();
                    intent.putExtra("deviceName",vhcleNoPopup_str);
                    intent.putExtra("startdate",startDate_str);
                    intent.putExtra("endDate",endDate_str);
                    intent.putExtra("startTime",startTime_str);
                    intent.putExtra("endTime",endTime_str);
                    intent.putExtra("divReport",divIdReport);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                }
                   /* else {
                        Intent generateRptIntnt = new Intent(context,RouteReportActivity.class);
                        getValue();
                        generateRptIntnt.putExtra("deviceName",vhcleNoPopup_str);
                        generateRptIntnt.putExtra("startdate",startDate_str);
                        generateRptIntnt.putExtra("endDate",endDate_str);
                        generateRptIntnt.putExtra("startTime",startTime_str);
                        generateRptIntnt.putExtra("endTime",endTime_str);
                        generateRptIntnt.putExtra("divReport",divIdReport);
                        startActivity(generateRptIntnt);
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        Toast.makeText(context,"Coming soon",Toast.LENGTH_LONG).show();

                    }*/
            }
        });
    }
    private void showReport() {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.configure_layout,null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptsView);

        startTime_tv = (TextView)promptsView.findViewById(R.id.frmTime_config);
        endTime_tv   = (TextView)promptsView.findViewById(R.id.toTime_config);
        startDate_tv = (TextView)promptsView.findViewById(R.id.frmDate_config);
        endDate_tv  = (TextView)promptsView.findViewById(R.id.toDate_config);
        //reportSpinner = (Spinner)promptsView.findViewById(R.id.vehcleNo_cnfgPop);

        vehcleNo_tv = (TextView)promptsView.findViewById(R.id.vehcleNo_cnfgPop);
       // vehcleNo_tv = (TextView)promptsView.findViewById(R.id.vehcleNo_cnfgPop);
        submtConfig_btn = (Button)promptsView.findViewById(R.id.configReprt_btn);


        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
       // vehcleNo_tv = new EditText(this);

        vehcleNo_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getDeviceName();

                android.support.v7.app.AlertDialog.Builder bodylist = new android.support.v7.app.AlertDialog.Builder(context);
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                v = layoutInflater.inflate(R.layout.simplelist, null);
                ListView listView = (ListView)v.findViewById(R.id.lists);
                bodylist.setView(v);
                final Dialog dialog = bodylist.create();
                dialog.show();
                simpleListAdapter = new SimpleListAdapter(context,deviceName);
                listView.setAdapter(simpleListAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        dialog.dismiss();
                        vehcleNo_tv.setText(deviceName.get(position));

                        positionId = (deviceId.get(position));

                        //getDeviceId(positionId);

                        Toast.makeText(context,"devicename with device id" +positionId,Toast.LENGTH_LONG).show();


                    }
                });
            }
        });

        submtConfig_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getValue();


                if (vhcleNoPopup_str.equals("")||startDate_tv.equals("")||endDate_str.equals("")||startTime_str.equals("")||endTime_str.equals(""))
                {
                    Toast.makeText(context,"Please Fill All Fields",Toast.LENGTH_LONG).show();
                }
                if (startDate_str.compareTo(endDate_str)>0){
                    Toast.makeText(context,"Please enter proper date",Toast.LENGTH_LONG).show();

                }else {
                    genrateReprt_btn.setVisibility(View.VISIBLE);
                     alertDialog.dismiss();
                }
               /* vhcleNoPopup_str = vehcleNo_tv.getText().toString();
                vhcleNoPopup_int = Integer.parseInt(vehcleNo_tv.getText().toString());*/
            }
        });

    /*    reportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                VehicleList vehcl = new VehicleList();
                String name ;

                JSONObject previousData = null;
                try {
                    previousData = new JSONObject(mSharedPreferences.getString("deviceData", "{}"));
                    JSONArray vehicleList = previousData.getJSONArray("totalLst");
                    for (int i = 0; i < vehicleList.length(); i++){
                        JSONObject jsonObject = vehicleList.getJSONObject(i);
                        // name = jsonObject.getString("name");
                        name = vehicleList.getJSONObject(i).getString("name");
                        listArrayList.add(name);
                        //listArrayList = vehicleList.getJSONObject(i).getString("name");
                        name = vehicleList.getJSONObject(i).getString("name");
                        Toast.makeText(context,"value is"+name,Toast.LENGTH_LONG).show();

                    }

                }catch (JSONException e){
                    e.printStackTrace();
                }



            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/
      /*  reportSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                VehicleList vehcl = new VehicleList();
                String name ;

                JSONObject previousData = null;
                try {
                    previousData = new JSONObject(mSharedPreferences.getString("deviceData", "{}"));
                    JSONArray vehicleList = previousData.getJSONArray("totalLst");
                    for (int i = 0; i < vehicleList.length(); i++){
                        JSONObject jsonObject = vehicleList.getJSONObject(i);
                        // name = jsonObject.getString("name");
                        listArrayList = vehicleList.getJSONObject(i).getString("name");
                        name = vehicleList.getJSONObject(i).getString("name");
                        Toast.makeText(context,"value is"+name,Toast.LENGTH_LONG).show();

                    }

                }catch (JSONException e){
                    e.printStackTrace();
                }


            }
        });*/

       /* ArrayAdapter<String> reportAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, listArrayList);
        reportSpinner.setAdapter(reportAdapter);
*/
/*        vehcleNo_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VehicleList vehcl = new VehicleList();
                String name ;

                JSONObject previousData = null;
                try {
                    previousData = new JSONObject(mSharedPreferences.getString("deviceData", "{}"));
                    JSONArray vehicleList = previousData.getJSONArray("totalLst");
                    for (int i = 0; i < vehicleList.length(); i++){
                        JSONObject jsonObject = vehicleList.getJSONObject(i);
                       // name = jsonObject.getString("name");
                        name = vehicleList.getJSONObject(i).getString("name");
                        Toast.makeText(context,"value is"+name,Toast.LENGTH_LONG).show();

                    }

                }catch (JSONException e){
                    e.printStackTrace();
                }


               // Log.d("nameofvhcle",name);
               // JSONObject jsonObject = vehicleList.getJSONObject(i);
            }
        });*/
        startDate_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datepicker(1);
            }
        });
        endDate_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datepicker(2);
            }
        });
        startTime_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timepicker(1);
            }
        });
        endTime_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timepicker(2);
            }
        });
    }

    private void getDeviceName() {
        String name ;
        int positionId = 0;
        int id = 0;
        JSONObject previousData = null;
        try {
            previousData = new JSONObject(mSharedPreferences.getString("deviceData", "{}"));
            JSONArray vehicleList = previousData.getJSONArray("totalLst");
            for (int i = 0; i < vehicleList.length(); i++){
                name = vehicleList.getJSONObject(i).getString("name");
                Log.d("nameofvhcle",name);
               // positionId = vehicleList.getJSONObject(i).getInt("positionId");
                id = vehicleList.getJSONObject(i).getInt("divId");
               // positionId = vehicleList.getJSONObject(i).getInt("deviceId");
                //id = vehicleList.getJSONObject(i).getInt("deviceId");
                //Log.d("id",String.valueOf(id));
                Log.d("reportPosition",String.valueOf(id));

                deviceName.add(name);
                deviceId.add(id);
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void getDeviceId(int positionId) {

        Toast.makeText(context,"getdevice id from deice name" +positionId,Toast.LENGTH_LONG).show();

        APIServices.GetVehicleDetailById(context, positionId, new DetailResponseCallback() {
            @Override
            public void OnResponse(JSONArray result) {

                SessionHandler.updateSnessionHandler(getBaseContext(), result, mSharedPreferences);

                Log.d("NEW_RELEASE",String.valueOf(result));

                try {
                    newDeviceId = result.getJSONObject(0).getInt("deviceId");
                    //int id = result.getJSONObject(0).getInt("deviceId");
                    Log.d("newd",String.valueOf(deviceId));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getValue() {

        vhcleNoPopup_str = vehcleNo_tv.getText().toString();
        startDate_str   = startDate_tv.getText().toString();
        endDate_str    = endDate_tv.getText().toString();
        startTime_str  = startTime_tv.getText().toString();
        endTime_str  = endTime_tv.getText().toString();
        divIdReport = positionId;

        //check start date is less than enddate
        if (startDate_str.compareTo(endDate_str)>0){
            Toast.makeText(context,"Please enter proper date",Toast.LENGTH_LONG).show();

        }
        Log.d("vehclenO",vhcleNoPopup_str);
        Log.d("divIdReport",String.valueOf(divIdReport));

//
//        vhcleNoPopup_int = Integer.parseInt(vehcleNo_tv.getText().toString());
    }

    private void timepicker(final int i) {
        View mTimeView = LayoutInflater.from(context).inflate(R.layout.timelayout,null);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setView(mTimeView);
        final TimePicker timePicker = (TimePicker) mTimeView.findViewById(R.id.timepicker);
        builder.setPositiveButton("set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch (i){
                    case 1:
                        startTime_tv.setText(timePicker.getCurrentHour()+":"+timePicker.getCurrentMinute());

                        break;
                    case 2:
                        endTime_tv.setText(timePicker.getCurrentHour()+":"+timePicker.getCurrentMinute());
                        break;
                }
            }
        })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        Dialog dialog = builder.create();
        dialog.show();
    }

    private void datepicker(final int i) {
        View mDateView = LayoutInflater.from(context).inflate(R.layout.datetimelayout,null);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setView(mDateView);
        final DatePicker datePicker = (DatePicker) mDateView.findViewById(R.id.datePicker);
        builder.setPositiveButton("set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Integer newMonth = ((int) datePicker.getMonth()) + 1;

              switch (i){
                  case 1:
                     // startDate_tv.setText(datePicker.getDayOfMonth()+ "-"+String.valueOf(newMonth)+"-"+datePicker.getYear());
                      startDate_tv.setText(datePicker.getYear() +"-"+newMonth+"-"+datePicker.getDayOfMonth());

                      break;
                  case 2:
                     // endDate_tv.setText(datePicker.getDayOfMonth()+"-"+String.valueOf(newMonth)+"-"+datePicker.getYear());
                      endDate_tv.setText(datePicker.getYear() +"-"+newMonth+"-"+datePicker.getDayOfMonth());
                      break;
              }
            }
        })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        Dialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
