package in.gtech.gogeotrack.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import in.gtech.gogeotrack.FCM.SendRegistrationTokentoServer;
import in.gtech.gogeotrack.R;
import in.gtech.gogeotrack.activity.SplashActivity;
import in.gtech.gogeotrack.api.APIServices;
import in.gtech.gogeotrack.model.VehicleList;
import in.gtech.gogeotrack.network.DetailResponseCallback;
import in.gtech.gogeotrack.network.ResponseCallbackEvents;
import in.gtech.gogeotrack.network.ResponseOfflineVehicle;
import in.gtech.gogeotrack.network.ResponseOnlineVehicle;
import in.gtech.gogeotrack.network.ResponseStringCallback;
import in.gtech.gogeotrack.utils.URLContstant;

import in.gtech.gogeotrack.activity.Main2Activity;

/**
 * Created by silence12 on 19/6/17.
 */

public class LoginFragment extends Fragment {

    String Message;
    TextView login;
    EditText username, password;
    TextView forgotpassword;
    CheckBox mCbShowPwd;
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;
    SharedPreferences sharedPrefs, sharedPreferences;
    SharedPreferences.Editor arrayEditor, editor;
    private static ProgressDialog progressDialog;
    byte[] data = new byte[0];
    public static ArrayList<VehicleList> listArrayList;
    public static ArrayList<VehicleList> latlongList;
    public static ArrayList<VehicleList> onLineList;
    public static ArrayList<VehicleList> offlineList;
    public static int AllSize, onlinesize, offlinesize;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        progressDialog = new ProgressDialog(getContext());
//        progressDialog.setMessage("Wait a moment...");
//        progressDialog.setCancelable(false);
//        progressDialog.show();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_fragment, container, false);
        login = (TextView) rootView.findViewById(R.id.login);
        username = (EditText) rootView.findViewById(R.id.username);
        password = (EditText) rootView.findViewById(R.id.loginpassword);
        forgotpassword = (TextView) rootView.findViewById(R.id.forgotpassword);
        sharedPrefs = getContext().getSharedPreferences("ArrayList", Context.MODE_PRIVATE);
        sharedPreferences = getContext().getSharedPreferences("OfflineList", Context.MODE_PRIVATE);
        mCbShowPwd = (CheckBox) rootView.findViewById(R.id.cbShowPwd);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Signing......");
                progressDialog.setCancelable(false);
                progressDialog.show();
                if (username.getText().toString().equals("")) {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Username required", Toast.LENGTH_SHORT).show();
                } else if (password.getText().toString().equals("")) {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Wrong password or password is empty", Toast.LENGTH_SHORT).show();
                } else {

                    final String user = username.getText().toString();
                    final String pass = password.getText().toString();

                    String newUrl = URLContstant.SESSION_URL + "?" + "email=" + user + "&password=" + pass;

                    APIServices.getInstance().PostProblem(getActivity(), newUrl, new ResponseStringCallback() {
                        @Override
                        public void OnResponse(String Response) {
                            Log.e("Response Comming", Response);
                            progressDialog.dismiss();
                            if (Response != null) {
                                try {
                                    JSONObject jsonObject = new JSONObject(Response);
                                    mSharedPreferences = getActivity().getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
                                    mEditor = mSharedPreferences.edit();
                                    mEditor.putString(URLContstant.KEY_USERNAME, user);
                                    mEditor.putString(URLContstant.KEY_PASSWORD,pass);
                                    mEditor.putBoolean(URLContstant.KEY_LOGGED_IN, true);
                                    mEditor.putString(URLContstant.FCM_TOKEN, jsonObject.getString("token"));
                                    mEditor.apply();
                                    Intent sendTokenservice = new Intent(getActivity(), SendRegistrationTokentoServer.class);
                                    getActivity().startService(sendTokenservice);
//                                    allparser(user,pass);
                                    Intent mainactivityIntent = new Intent(getActivity(), Main2Activity.class);
                                    startActivity(mainactivityIntent);
                                    getActivity().finish();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(getActivity(), "Server Error. Try again Later", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame, new forgotFragment()).addToBackStack(null).commit();
            }
        });

        mCbShowPwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // checkbox status is changed from uncheck to checked.
                if (!isChecked) {
                    // show password
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    // hide password
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        return rootView;
    }

    private void parseView(String userName,String password) {
        APIServices.GetAllVehicleList(getActivity(),userName,password, new ResponseCallbackEvents() {
                    @Override
                    public void onSuccess(final ArrayList<VehicleList> result) {
//                        progressDialog.dismiss();
                        for (int i = 0; i < result.size(); i++) {

                            AllSize = result.size();
                            int id = result.get(i).positionId;
                            final int finalI = i;
                            APIServices.GetVehicleDetailById(getActivity(), id, new DetailResponseCallback() {
                                @Override
                                public void OnResponse(VehicleList Response) {
                                    VehicleList vehicles = new VehicleList(result.get(finalI).id, result.get(finalI).name, result.get(finalI).uniqueId
                                            , result.get(finalI).status, result.get(finalI).lastUpdates, result.get(finalI).category, result.get(finalI).positionId, Response.address,
                                            result.get(finalI).time, result.get(finalI).timeDiff);
                                    listArrayList.add(vehicles);
//                            if(result.get(finalI).status.equals("online")){
//                                onLineList.add(vehicles);
//                            }
//                            if(result.get(finalI).status.equals("offline")){
//                                offlineList.add(vehicles);
//                            }
                                    VehicleList latlong = new VehicleList(result.get(finalI).id, result.get(finalI).name, result.get(finalI).status, Response.latitute, Response.longitute);
                                    latlongList.add(latlong);
                                }
                            });
                        }

                    }
                }

        );
    }


    public void allparser(String userName,String password){
        parseView(userName,password);
        allOnlineVehicle(userName,password);
        allOfflineVehicle(userName,password);
        Intent mainactivityIntent = new Intent(getActivity(), Main2Activity.class);
        startActivity(mainactivityIntent);
        getActivity().finish();

    }
    public void allOnlineVehicle(String userName,String password) {
        APIServices.GetAllOnlineVehicleList(getActivity(),userName,password, new ResponseOnlineVehicle() {
            @Override
            public void onSuccessOnline(final ArrayList<VehicleList> result) {
                final ArrayList<VehicleList> arrayList = new ArrayList<VehicleList>();
                for (int i = 0; i < result.size(); i++) {
                    onlinesize = result.size();
                    int id = result.get(i).positionId;
                    final int finalI = i;
                    APIServices.GetVehicleDetailById(getActivity(), id, new DetailResponseCallback() {
                        @Override
                        public void OnResponse(VehicleList Response) {
                            VehicleList vehicles = new VehicleList(result.get(finalI).id, result.get(finalI).name, result.get(finalI).uniqueId
                                    , result.get(finalI).status, result.get(finalI).lastUpdates, result.get(finalI).category, result.get(finalI).positionId, Response.address,
                                    result.get(finalI).time, result.get(finalI).timeDiff);
                            arrayList.add(vehicles);
                            arrayEditor = sharedPrefs.edit();
                            arrayEditor.clear();
                            Gson gson = new Gson();
                            String json = gson.toJson(arrayList);
                            arrayEditor.putString("onlist", json);
                            arrayEditor.putString("onlinesize", String.valueOf(onlinesize));
                            arrayEditor.commit();
                        }
                    });
                }
            }

        });
    }

    public void allOfflineVehicle(String userName,String password) {
        APIServices.GetAllOfflineVehicleList(getActivity(),userName,password, new ResponseOfflineVehicle() {
            @Override
            public void onSuccessOffline(final ArrayList<VehicleList> result) {
                final ArrayList<VehicleList> arrayLi = new ArrayList<VehicleList>();
                for (int i = 0; i < result.size(); i++) {
                    offlinesize = result.size();
                    Log.d("onlineSize", String.valueOf(offlinesize));
                    int id = result.get(i).positionId;
                    final int finalI = i;
                    APIServices.GetVehicleDetailById(getActivity(), id, new DetailResponseCallback() {
                        @Override
                        public void OnResponse(VehicleList Response) {
                            VehicleList vehicles = new VehicleList(result.get(finalI).id, result.get(finalI).name, result.get(finalI).uniqueId
                                    , result.get(finalI).status, result.get(finalI).lastUpdates, result.get(finalI).category, result.get(finalI).positionId, Response.address,
                                    result.get(finalI).time, result.get(finalI).timeDiff);
                            arrayLi.add(vehicles);
                            editor = sharedPreferences.edit();
                            editor.clear();
                            Gson gson = new Gson();
                            String json = gson.toJson(arrayLi);
                            editor.putString("off", json);
                            editor.commit();
                        }
                    });
                }

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
