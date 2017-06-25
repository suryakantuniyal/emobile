package org.traccar.manager.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.traccar.manager.R;
import org.traccar.manager.activity.MainActivity;
import org.traccar.manager.api.APIServices;
import org.traccar.manager.network.ResponseCallback;
import org.traccar.manager.FCM.SendRegistrationTokentoServer;
import org.traccar.manager.network.ResponseStringCallback;
import org.traccar.manager.utils.URLContstant;

/**
 * Created by silence12 on 19/6/17.
 */

public class LoginFragment extends Fragment {

    String Message;
    TextView login,newSignUp;
    EditText username,password;
    TextView forgotpassword;
    CheckBox mCbShowPwd;
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;
    byte[] data = new byte[0];

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_fragment,container,false);
        login=(TextView) rootView.findViewById(R.id.login);
        newSignUp=(TextView)rootView.findViewById(R.id.newsignup);
        username=(EditText) rootView.findViewById(R.id.username);
        password=(EditText) rootView.findViewById(R.id.loginpassword);
        forgotpassword=(TextView)rootView.findViewById(R.id.forgotpassword);
        mCbShowPwd = (CheckBox) rootView.findViewById(R.id.cbShowPwd);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Signing......");
                progressDialog.setCancelable(false);
                progressDialog.show();
                if (username.getText().toString().equals("")){
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(),"Username required", Toast.LENGTH_SHORT).show();
                } else if (password.getText().toString().equals("")){
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(),"Wrong password or password is empty", Toast.LENGTH_SHORT).show();
                } else {

                    String user = username.getText().toString();
                    String pass = password.getText().toString();

                    String callUrl= "http://35.189.74.0:8082/api/session/?"+"email=yash.bhat94%40gmail.com&password=admin";
                    String newUrl = "http://35.189.74.0:8082/api/session/?"+"email="+user+"&password="+pass;
                    Log.e("CallUrl",callUrl);

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

                                    mEditor.putString(URLContstant.KEY_USERNAME,jsonObject.getString("email"));
                                    mEditor.putString(URLContstant.KEY_USER_EMAIL,jsonObject.getString("email"));
                                    mEditor.putString(URLContstant.KEY_USER_PHONE,jsonObject.getString("phone"));
                                    mEditor.putBoolean(URLContstant.KEY_LOGGED_IN,true);
                                    mEditor.putString(URLContstant.FCM_TOKEN,jsonObject.getString("token"));
                                    mEditor.apply();
                                    Intent sendTokenservice = new Intent(getActivity(),SendRegistrationTokentoServer.class);
                                    getActivity().startService(sendTokenservice);
                                    Intent mainactivityIntent = new Intent(getActivity(), MainActivity.class);
                                    startActivity(mainactivityIntent);
                                    getActivity().finish();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                Toast.makeText(getActivity(),"Server Error. Try again Later",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        newSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame,new verifyFragment()).addToBackStack(null).commit();
            }
        });

        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame,new forgotFragment()).addToBackStack(null).commit();
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

        return rootView ;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
