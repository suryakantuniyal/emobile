package org.traccar.manager.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;
import org.traccar.manager.R;
import org.traccar.manager.activity.SignUpAccount;
import org.traccar.manager.api.APIServices;
import org.traccar.manager.network.ResponseCallback;

/**
 * Created by silence12 on 19/6/17.
 */

public class forgotFragment extends Fragment {

    EditText mUsername;
    Button login;
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;
    ProgressDialog progressDialog;
    Bundle args;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.forgot_verify, container, false);
        mUsername=(EditText) rootView.findViewById(R.id.username);
        login=(Button) rootView.findViewById(R.id.login);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Requesting otp.....");
        args = new Bundle();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                View view = ((Activity) getActivity()).getCurrentFocus();
                if (view != null){
                    inputMethodManager.hideSoftInputFromInputMethod(view.getWindowToken(),0);
                }
                String user = mUsername.getText().toString();

                if ((user.length() == 10 || user.length() == 11)&&isValidPhoneNum(user)) {
                    args.putString("phone",user);
                    CheckUserStatus(user);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),"Invalid Number", Toast.LENGTH_LONG).show();
                }
            }
        });
        return rootView ;
    }

    public boolean isValidPhoneNum(String phoneNum) {
        String ePattern = "^[789]\\d{9}$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(phoneNum);
        return m.matches();
    }

    private void CheckUserStatus(final String username){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("phone", username);
        }catch (Exception e){
            Log.d("status", "CheckUserStatus ");
        }
        APIServices.getInstance().PostCall(getActivity(), "api/profile/check/", jsonObject, new ResponseCallback() {
            @Override
            public void OnResponse(JSONObject Response) {

                if (Response != null){
                    Fragment fragment = null;
                    try{
                        progressDialog.dismiss();
                        if (SignUpAccount.isDestroy){
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame,fragment).addToBackStack(null).commitAllowingStateLoss();
                        } else {
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame,fragment).addToBackStack(null).commit();
                            getFragmentManager().executePendingTransactions();
                        }
                    }
                    catch (Exception e){
                        Log.d("error", e.toString());
                    }
                }
            }
        });
    }
}
