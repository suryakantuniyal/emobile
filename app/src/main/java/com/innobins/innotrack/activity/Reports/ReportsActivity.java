package com.innobins.innotrack.activity.Reports;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.innobins.innotrack.activity.PdfFile.PdfGenerator;
import com.innobins.innotrack.model.VehicleList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.innobins.innotrack.R;

import com.innobins.innotrack.adapter.SimpleListAdapter;
import com.innobins.innotrack.utils.URLContstant;

public class ReportsActivity extends AppCompatActivity {
    Spinner reportSpinner;
    private List<String> reportType = new ArrayList<>();
    private List<String> devicename = new ArrayList<String>();
    private List<Integer> deviceId = new ArrayList<Integer>();
    private List<Integer>newDivId = new ArrayList<Integer>();
    private ArrayList<VehicleList> listArrayList;
    List<List>vehicleDetail = new ArrayList<List>();
    ArrayList<Integer> selectedItem1 = new ArrayList<>();

//-------------------------------new testng--------------------------------------------
    ArrayList<String>divname = new ArrayList<String>();
    ArrayList<Integer>divId = new ArrayList<Integer>();

    Map<String, List> data = new HashMap<String, List>();
    List<List> value = new ArrayList<List>(data.values());

    String positionId;
    int newDeviceId,divIdReport;
    String reportType_String,spinnerValue="",vhcleNoPopup_str,vhcleNoPopup_str1,vhcleNoPopup_str2,startDate_str,endDate_str,startTime_str,endTime_str;
    int vhcleNoPopup_int;
    Button confrigure_btn,genrateReprt_btn,submtConfig_btn,addVehicle_btn;
    final Context context = this;
    TextView startDate_tv,endDate_tv,startTime_tv,endTime_tv,vehcleNo_tv,vehcleNo_tv1,vehcleNo_tv2;
    int dateTime;
    SharedPreferences mSharedPreferences;
    ArrayAdapter<String> adapter;
    ArrayAdapter<Integer>divIdAdaptr;
    SimpleListAdapter simpleListAdapter;
    int click = 0;
    int vehicleNo;
    ImageView close_img,close1_img;
    String userName,password;

    View dateView;
    ImageView dateImg;
    boolean isDown;
    Date startDate,endDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.luncher_icon);
        setTitle("Reports");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listArrayList = new ArrayList<VehicleList>();

        mSharedPreferences = getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
        userName = mSharedPreferences.getString(URLContstant.KEY_USERNAME, "");
        password = mSharedPreferences.getString(URLContstant.KEY_PASSWORD,"");

        startDate_tv = (TextView)findViewById(R.id.frmDate_config);
        endDate_tv = (TextView)findViewById(R.id.toDate_config);
        startTime_tv = (TextView)findViewById(R.id.frmTime_report);
        endTime_tv = (TextView)findViewById(R.id.toTime_report);
        vehcleNo_tv = (TextView)findViewById(R.id.vehicleNo_report);

        vehcleNo_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // getUpdatedDevice();
                //listArrayList = getDeviceName();
                getDeviceName();
                android.support.v7.app.AlertDialog.Builder bodylist = new android.support.v7.app.AlertDialog.Builder(context);
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                v = layoutInflater.inflate(R.layout.simplelist, null);
                // View v = layoutInflater.inflate(R.layout.s)
                final ListView listView = (ListView)v.findViewById(R.id.lists);
                final Button listSubmit = (Button)v.findViewById(R.id.listbutton);
                bodylist.setView(v);
                final Dialog dialog = bodylist.create();
                dialog.show();
                deviceId = deviceId;
                Log.d("newDeviceIdof",String.valueOf(deviceId));
               /* simpleListAdapter = new SimpleListAdapter(context,android.R.layout.simple_list_item_multiple_choice,devicename);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                listView.setAdapter(simpleListAdapter);*/
                divIdAdaptr = new ArrayAdapter<Integer>(context,android.R.layout.simple_list_item_multiple_choice,deviceId);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                listView.setAdapter(divIdAdaptr);
                adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_multiple_choice, devicename);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                           // positionId = String.valueOf(deviceId.get(position));
                           // int posold = deviceId.get(position);
                        Object obj = divIdAdaptr.getItem(position);
                        positionId = obj.toString();
                        positionId = positionId+","+obj;
                           /* positionId = String.valueOf(deviceId.get(position));
                            positionId = positionId+",";*/
                           // positionId = (positionId+","+deviceId.get(position));

                        Log.d("positionnew", String.valueOf(positionId));
                    }

                });
                listSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SparseBooleanArray checked = listView.getCheckedItemPositions();
                        ArrayList<String> selectedItem = new ArrayList<String>();

                        Log.d("selecteditem",String.valueOf(selectedItem1));

                        for (int i = 0; i < checked.size(); i++){
                            int position = checked.keyAt(i);
                            if (checked.valueAt(i))
                                selectedItem.add(adapter.getItem(position));
                                selectedItem1.add(divIdAdaptr.getItem(position));
                        }
/*
                        for (int j=0;j<checked.size();j++){
                            int position = checked.keyAt(j);
                            if (checked.valueAt(j))
                                selectedItem1.add(divIdAdaptr.getItem(position));
                        }
*/
                        String [] output = new String[selectedItem.size()];
                        Integer[] posId = new Integer[selectedItem1.size()];

                        for (int i =0 ;i<selectedItem.size();i++)
                        {
                            output[i] = selectedItem.get(i);
                            // vehcleNo_tv.setText(vehcleNo_tv.getText() + ""+output[i]+" ,");
                            vehcleNo_tv.setText(vehcleNo_tv.getText()+""+output[i]+",");
                            String vhcle = vehcleNo_tv.getText().toString();
                        }
                        for (int i= 0;i<selectedItem1.size();i++){
                            posId[i] = selectedItem1.get(i);
                            int id = posId[i];
                            newDivId.add(id);
                            Log.d("idofadpt", String.valueOf(id));
                        }
                        for (int j=0;j<newDivId.size();j++){

                            Log.d("newdivIdarr", String.valueOf(newDivId.get(j)));
                        }

                        dialog.dismiss();
                    }
                });

                //listView.setAdapter(adapter);

               // simpleListAdapter = new SimpleListAdapter(context,devicename);
               // simpleListAdapter = new SimpleListAdapter(context,android.R.layout.simple_list_item_multiple_choice,deviceName);
               /* listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                listView.setAdapter(simpleListAdapter);*/
            }



        });


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


/*
        APIServices.GetAllOnlineVehicleList(ReportsActivity.this, userName, password, new ResponseOnlineVehicle() {
            @Override
            public void onSuccessOnline(JSONObject result) {
                //SessionHandler.updateSnessionHandler(getBaseContext(), result, mSharedPreferences);

            }
        });
*/
        //listArrayList = new ArrayList<>();
       // reportSpinner = (Spinner)findViewById(R.id.reportSpinner);
       // confrigure_btn = (Button)findViewById(R.id.configureReportBtn);
        genrateReprt_btn=(Button)findViewById(R.id.genrateReport_btn);
        dateView = (View)findViewById(R.id.card_view1);
       //dateImg = (ImageView)findViewById(R.id.reportDateImg);
        //genrateReprt_btn.setEnabled(false);
/*        final MaterialBetterSpinner materialDesignSpinner = (MaterialBetterSpinner)
                findViewById(R.id.reportSpinner);*/
        reportSpinner = (Spinner)findViewById(R.id.reportSpinner);
        //materialDesignSpinner.setBackgroundResource(R.drawable.textviewoutline);
        reportType.add("Route");
        reportType.add("Events");
        reportType.add("Summary");
        reportType.add("Trips");
/*
        dateView.setVisibility(View.INVISIBLE);
        isDown = false;
        dateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideDown();
            }
        });*/

       /* String befor = reportSpinner.getSelectedItem().toString();
        Toast.makeText(context,befor,Toast.LENGTH_LONG).show();

       /* materialDesignSpinner.setSelection(0);
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
*/
        //String Text = String.valueOf(reportSpinner.getSelectedItem());

        ArrayAdapter<CharSequence> reportAdapter = ArrayAdapter.createFromResource(this,R.array.Type,android.R.layout.simple_spinner_item);
        reportAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       // reportAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportSpinner.setAdapter(reportAdapter);
        reportType_String = reportSpinner.getSelectedItem().toString();

        reportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               // reportType_String = parent.getItemAtPosition(position).toString();
                spinnerValue = parent.getItemAtPosition(position).toString();

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
       /* materialDesignSpinner.setAdapter(reportAdapter);
        materialDesignSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                reportType_String = parent.getItemAtPosition(position).toString();
                Log.d("valuestring",String.valueOf(reportType_String));

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });*/
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
/*
        confrigure_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReport();
            }
        });
*/
        genrateReprt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ReportsActivity.this, PdfGenerator.class);
                startActivity(intent);
                finish();

              /*  getValue();
                if (vhcleNoPopup_str.equals("")||startDate_tv.equals("")||endDate_str.equals("")||startTime_str.equals("")||endTime_str.equals(""))
                {
                    Toast.makeText(context,"Please Fill All Fields",Toast.LENGTH_LONG).show();
                }

                else if (startDate_str.compareTo(endDate_str)>0){
                    Toast.makeText(context,"Please enter proper date",Toast.LENGTH_LONG).show();

                }else {
                    showReport();
                }*/

               // getValue();
            }

        });
    }

    private void slideDown() {

        dateImg.setVisibility(View.VISIBLE );
        TranslateAnimation animation = new TranslateAnimation(
                0,
                0,
                dateView.getHeight(),
                0);
        animation.setDuration(500);
        animation.setFillAfter(true);
        dateView.startAnimation(animation);
    }

            private ArrayList<VehicleList> getDeviceName() {
                devicename.clear();
                String name ;
                int positionId = 0;
                int id = 0;
                JSONObject previousData = null;
                try {
                    previousData = new JSONObject(mSharedPreferences.getString("deviceData","{}"));
                    JSONArray vehicleList = previousData.getJSONArray("totalList");
                    for (int i=0;i<vehicleList.length();i++){
                        name = vehicleList.getJSONObject(i).getString("name");
                        id = vehicleList.getJSONObject(i).getInt("deviceId");
                        VehicleList vehicleList1 = new VehicleList(name,id);

                        listArrayList.add(vehicleList1);

                        Log.d("arraylistvhcl", String.valueOf(listArrayList));

                        Log.d("idofvhcle",String.valueOf(id));
                        Log.d("newVehicleName",name);
                        Log.d("reportPosition",String.valueOf(id));
                        /*    divname.add(name);
                        divId.add(id);
                        data.put("devicename",divname);
                        data.put("divId",divId);*/
                        devicename.add(name);
                        deviceId.add(id);
                        vehicleDetail.add(deviceId);
                        vehicleDetail.add(devicename);
                        Log.d("vehicleDetailmap",String.valueOf(vehicleDetail));
                       // data.put("vehicleDetail", (Map<String, Integer>) vehicleDetail);
                       // deviceId.add(id);
                    }

                }catch (JSONException e){
                    e.printStackTrace();
                }

                return listArrayList;
            }


    private void getValue() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        startDate_str   = startDate_tv.getText().toString();
        endDate_str    = endDate_tv.getText().toString();
        Log.d("startdatenew",startDate_str);
        try {
            Date startDate = formatter.parse(startDate_str);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        vhcleNoPopup_str = vehcleNo_tv.getText().toString();
        Log.d("vehicleNoPopu",vhcleNoPopup_str);

       /* try {
         startDate = formatter.parse(startDate_str);
         endDate = formatter.parse(endDate_str);
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
     /*   Date date1 = dateFormat.parse("2013-01-01");
        Date date2 = dateFormat.parse("2013-01-02");*/

        startTime_str  = startTime_tv.getText().toString();
        endTime_str  = endTime_tv.getText().toString();
        divId = selectedItem1;

        for(Integer item:divId){
            Log.d("itemofVhcle", String.valueOf(item));
        }
       /* for (int i=0;i<selectedItem1.size();i++){
            Log.d("sizeofselctd", String.valueOf(selectedItem1.size()));

            divId = selectedItem1;
           // divIdReport = selectedItem1.listIterator();
        }*/
        Log.d("vehclenO",vhcleNoPopup_str);
        Log.d("divIdReport",String.valueOf(divId));


//
//        vhcleNoPopup_int = Integer.parseInt(vehcleNo_tv.getText().toString());
    }

    private void showReport() {

        if(spinnerValue.equals("Events")){
            Intent evntIntnt = new Intent(context,EventesReportActivity.class);
            getValue();
            evntIntnt.putExtra("deviceName",vhcleNoPopup_str);
            evntIntnt.putExtra("startdate",startDate_str);
            evntIntnt.putExtra("endDate",endDate_str);
            evntIntnt.putExtra("startTime",startTime_str);
            evntIntnt.putExtra("endTime",endTime_str);
            evntIntnt.putExtra("divReport",selectedItem1);
            startActivity(evntIntnt);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }

        if (spinnerValue.equals("Trips")){
            Intent intent = new Intent(context,TripsReportActiviy.class);
            intent.putExtra("deviceName",vhcleNoPopup_str);
            intent.putExtra("startdate",startDate_str);
            intent.putExtra("endDate",endDate_str);
            intent.putExtra("startTime",startTime_str);
            intent.putExtra("endTime",endTime_str);
            intent.putExtra("divReport",divIdReport);
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
        if (spinnerValue.equals("Route")){
            Intent routeIntnt = new Intent(context,RouteReportActivity.class);
            getValue();
            routeIntnt.putExtra("deviceName",vhcleNoPopup_str);
            routeIntnt.putExtra("startdate",startDate_str);
            routeIntnt.putExtra("endDate",endDate_str);
            routeIntnt.putExtra("startTime",startTime_str);
            routeIntnt.putExtra("endTime",endTime_str);
            routeIntnt.putExtra("divReport",divIdReport);
            startActivity(routeIntnt);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }

     /*   else {
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
                      startDate_tv.setText(datePicker.getDayOfMonth() +"-"+newMonth+"-"+datePicker.getYear());
                      break;
                  case 2:
                     // endDate_tv.setText(datePicker.getDayOfMonth()+"-"+String.valueOf(newMonth)+"-"+datePicker.getYear());
                      endDate_tv.setText(datePicker.getDayOfMonth() +"-"+newMonth+"-"+datePicker.getYear());
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

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        searchMenuItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
               // searchVehicle(searchView);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }*/
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
