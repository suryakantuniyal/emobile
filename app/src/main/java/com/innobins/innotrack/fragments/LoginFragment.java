package com.innobins.innotrack.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.innobins.innotrack.FCM.SendRegistrationTokentoServer;
import com.innobins.innotrack.R;
import com.innobins.innotrack.home.HomeActivity;
import com.innobins.innotrack.network.ResponseCallback;
import com.innobins.innotrack.network.WebserviceHelper;
import com.innobins.innotrack.utils.URLContstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import com.innobins.innotrack.activity.CircularActivity;

/**
 * Created by silence12 on 19/6/17.
 */

public class LoginFragment extends Fragment {

    TextView login;
    EditText username, password;
    TextView forgotpassword;
    CheckBox mCbShowPwd,rememberMe;
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;
    private static ProgressDialog progressDialog;
    private Boolean saveLogin;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_fragment, container, false);
        login = (TextView) rootView.findViewById(R.id.login);
        username = (EditText) rootView.findViewById(R.id.username);
        password = (EditText) rootView.findViewById(R.id.loginpassword);
        forgotpassword = (TextView) rootView.findViewById(R.id.forgotpassword);
        mCbShowPwd = (CheckBox) rootView.findViewById(R.id.cbShowPwd);
        rememberMe = (CheckBox)rootView.findViewById(R.id.rememberme);
        mSharedPreferences = getActivity().getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
        saveLogin = mSharedPreferences.getBoolean(URLContstant.KEY_SAVED_LOGIN,false);
        if(saveLogin == true){
            username.setText(mSharedPreferences.getString(URLContstant.KEY_REMBR_USER,""));
            password.setText(mSharedPreferences.getString(URLContstant.KEY_REMBR_PASS,""));
            rememberMe.setChecked(true);
        }
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(getContext());
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
                    String sessionUrl = "https://mtrack-api.appspot.com/api/session/";
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("user",user);
                        jsonObject.put("password",pass);
                        WebserviceHelper.getInstance().PostCall(getContext(), sessionUrl,jsonObject , new ResponseCallback() {
                            @Override
                            public void OnResponse(JSONObject Response) {
                                progressDialog.dismiss();
                                try {
                                    JSONObject jsonObject1 = Response.getJSONObject("datasets");
                                    if(jsonObject1.getInt("Message")==2) {
                                        mEditor = mSharedPreferences.edit();
                                        mEditor.putString(URLContstant.KEY_USERNAME, user);
                                        mEditor.putString(URLContstant.KEY_PASSWORD, pass);
                                        mEditor.putInt(URLContstant.KEY_LOGEDIN_USERID,jsonObject1.getInt("userId"));
                                        mEditor.putBoolean(URLContstant.KEY_LOGGED_IN, true);
                                        if (rememberMe.isChecked()) {
                                            mEditor.putString(URLContstant.KEY_REMBR_USER,user);
                                            mEditor.putString(URLContstant.KEY_REMBR_PASS,pass);
                                            mEditor.putBoolean(URLContstant.KEY_SAVED_LOGIN,true);
                                        }
                                        mEditor.apply();
                                        Intent sendTokenservice = new Intent(getActivity(), SendRegistrationTokentoServer.class);
                                        getActivity().startService(sendTokenservice);
                                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                                        intent.putExtra("logged", true);
                                        startActivity(intent);
                                        getActivity().finish();
                                    } else if(jsonObject1.getInt("Message")==1){
                                        Toast.makeText(getContext(),"User doesn't exit",Toast.LENGTH_SHORT).show();
                                    }else if(jsonObject1.getInt("Message")==3){
                                        Toast.makeText(getContext(),"Wrong password",Toast.LENGTH_SHORT).show();
                                    }else if(jsonObject1.getInt("Message")==0){
                                        Toast.makeText(getContext(),"Server Error. Try again Later",Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Contact organisation to reset password", Toast.LENGTH_SHORT).show();
//                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame, new forgotFragment()).addToBackStack(null).commit();
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

    @Override
    public void onResume() {
        super.onResume();
    }


    private void vehicleStatusData(int id) {
        String mUrl = "https://mtrack-api.appspot.com/api/get/summary/byuser/" ;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userid",id);
            WebserviceHelper.getInstance().PostCall(getContext(), mUrl, jsonObject, new ResponseCallback() {
                @Override
                public void OnResponse(JSONObject Response) {
                    try {
                        JSONArray jsonArray = Response.getJSONArray("summaryData");
                        JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                        mEditor = mSharedPreferences.edit();
                        mEditor.putInt("active", jsonObject1.getInt("online_vehicle"));
                        mEditor.putInt("inactive", jsonObject1.getInt("offline_vehicle"));
                        mEditor.putInt("running",jsonObject1.getInt("running_vehicle"));
                        mEditor.putInt("total", jsonObject1.getInt("total_vehicle"));
                        mEditor.putInt("unknown",jsonObject1.getInt("unknown_vehicle"));
                        mEditor.apply();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
