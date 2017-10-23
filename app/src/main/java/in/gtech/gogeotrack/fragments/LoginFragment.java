package in.gtech.gogeotrack.fragments;

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

import in.gtech.gogeotrack.FCM.SendRegistrationTokentoServer;
import in.gtech.gogeotrack.R;
import in.gtech.gogeotrack.activity.CircularActivity;
import in.gtech.gogeotrack.api.APIServices;
import in.gtech.gogeotrack.network.ResponseStringCallback;
import in.gtech.gogeotrack.utils.URLContstant;

/**
 * Created by silence12 on 19/6/17.
 */

public class LoginFragment extends Fragment {

    TextView login;
    EditText username, password;
    TextView forgotpassword;
    CheckBox mCbShowPwd;
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;
    SharedPreferences.Editor arrayEditor;
    private static ProgressDialog progressDialog;
    byte[] data = new byte[0];

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_fragment, container, false);
        login = (TextView) rootView.findViewById(R.id.login);
        username = (EditText) rootView.findViewById(R.id.username);
        password = (EditText) rootView.findViewById(R.id.loginpassword);
        forgotpassword = (TextView) rootView.findViewById(R.id.forgotpassword);
        mCbShowPwd = (CheckBox) rootView.findViewById(R.id.cbShowPwd);
        mSharedPreferences = getActivity().getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);

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

                    final String newUrl = URLContstant.SESSION_URL + "?" + "email=" + user + "&password=" + pass;

                    APIServices.getInstance().PostProblem(getActivity(), newUrl, new ResponseStringCallback() {

                        @Override
                        public void OnResponse(String Response) {
                            Log.e("Response Comming", Response);
                            progressDialog.dismiss();
                            if (Response != null) {
                                try {
                                   // allOnlineVehicle(user, pass);
                                    JSONObject jsonObject = new JSONObject(Response);
                                    mEditor = mSharedPreferences.edit();
                                    mEditor.putString(URLContstant.KEY_USERNAME, user);
                                    mEditor.putString(URLContstant.KEY_PASSWORD,pass);
                                    mEditor.putBoolean(URLContstant.KEY_LOGGED_IN, true);
                                    mEditor.putString(URLContstant.FCM_TOKEN, jsonObject.getString("token"));
                                    mEditor.apply();
                                    Intent sendTokenservice = new Intent(getActivity(), SendRegistrationTokentoServer.class);
                                    getActivity().startService(sendTokenservice);

                                    Intent  intent = new Intent(getActivity(),CircularActivity.class);
                                    intent.putExtra("logged",true);
                                    startActivity(intent);
                                    getActivity().finish();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(getActivity(), "Server Error. Try again Later", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void OnFial(int Response) {
                            if(Response == 401){
                                progressDialog.dismiss();
                                Toast.makeText(getActivity(), "Incorrect user name or password", Toast.LENGTH_SHORT).show();
                            }

                        }

                    });

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

 /*   public void allOnlineVehicle(String userName, String password) {

        APIServices.GetAllOnlineVehicleList(getActivity(),userName,password, new ResponseOnlineVehicle() {
            @Override
            public void onSuccessOnline(JSONArray result) {
                SessionHandler.updateSnessionHandler(getContext(), result, mSharedPreferences);
                Intent  intent = new Intent(getActivity(),CircularActivity.class);
                intent.putExtra("logged",true);
                startActivity(intent);
                getActivity().finish();

            }
        });

    };
*/
    @Override
    public void onResume() {
        super.onResume();
    }



}
