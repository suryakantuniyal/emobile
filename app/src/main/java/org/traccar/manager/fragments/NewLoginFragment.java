package org.traccar.manager.fragments;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.traccar.manager.R;
import org.traccar.manager.utils.URLContstant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by silence12 on 20/6/17.
 */

public class NewLoginFragment extends Fragment {

    String Message;
    TextView login,newSignUp;
    EditText username,password;
    TextView forgotpassword;
    CheckBox mCbShowPwd;
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;
    byte[] data = new byte[0];

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_fragment, container, false);
        login = (TextView) rootView.findViewById(R.id.login);
        newSignUp = (TextView) rootView.findViewById(R.id.newsignup);
        username = (EditText) rootView.findViewById(R.id.username);
        password = (EditText) rootView.findViewById(R.id.loginpassword);
        forgotpassword = (TextView) rootView.findViewById(R.id.forgotpassword);
        mCbShowPwd = (CheckBox) rootView.findViewById(R.id.cbShowPwd);
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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
                        String requestedUrl = URLContstant.BASE_URL + "/" + "api/session" ;
                        Log.e("Url",requestedUrl);
                        StringBuilder builder = new StringBuilder();
                        HttpClient client = new DefaultHttpClient();
                        HttpGet httpGet = new HttpGet(requestedUrl);
                        httpGet.addHeader(BasicScheme.authenticate( new UsernamePasswordCredentials("yash.bhat94%40gmail.com", "admin"), "UTF-8", false));
                        httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded");
                        try {
                            HttpResponse response = client.execute(httpGet);
                            StatusLine statusLine = response.getStatusLine();
                            int statusCode = statusLine.getStatusCode();
                            Log.e("UrlResponse", String.valueOf(statusCode));
                            if (statusCode == 200) {
                                HttpEntity entity = response.getEntity();
                                InputStream content = entity.getContent();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    builder.append(line);
                                }
                            } else {
                                Log.e("", "Failed to download file");
                            }
                        } catch (ClientProtocolException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

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

        }

        return rootView;
    }


}

