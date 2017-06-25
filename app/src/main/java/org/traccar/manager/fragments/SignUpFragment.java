package org.traccar.manager.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.telephony.gsm.SmsManager;
import android.telephony.gsm.SmsMessage;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.traccar.manager.R;
import org.traccar.manager.activity.MainActivity;
import org.traccar.manager.activity.SignUpAccount;
import org.traccar.manager.api.APIServices;
import org.traccar.manager.network.ResponseCallback;
import org.traccar.manager.FCM.SendRegistrationTokentoServer;
import org.traccar.manager.utils.URLContstant;
import org.traccar.manager.utils.UtilFunctions;

import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 * Created by silence12 on 19/6/17.
 */

public class SignUpFragment extends Fragment implements View.OnTouchListener {
    Button signup;
    EditText password, otp, name, email;
    String storedOtp = null, unVarifiedNumber;
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;
    Bundle args;
    byte[] data = new byte[0];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.signup_fragment, container, false);
        signup = (Button) rootView.findViewById(R.id.signup);
        email = (EditText) rootView.findViewById(R.id.email);
        password = (EditText) rootView.findViewById(R.id.loginpassword);
        name = (EditText) rootView.findViewById(R.id.name);
        name.requestFocus();
        otp = (EditText) rootView.findViewById(R.id.otp);
        mSharedPreferences = getActivity().getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
        args = getArguments();
        sendotpTouser(args.getString("phone"));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storedOtp = mSharedPreferences.getString(URLContstant.KEY_OTP, "0000");
                unVarifiedNumber = mSharedPreferences.getString(URLContstant.KEY_UNVARIFIED_NUMBER, "9999999999");
                if (!UtilFunctions.isNetworkAvailable(getActivity().getBaseContext())) {
                    UtilFunctions.makeToast(getActivity().getBaseContext(), "No network", Toast.LENGTH_SHORT);
                } else if (name.getText().toString().length() != 0) {
                    if (password.getText().toString().length() >= 5 && password.getText().toString().length() <= 10) {
                        if (email.getText().toString().length() != 0) {
                            if (isValidEmail(email.getText().toString())) {

                                if (otp.getText().toString().length() != 0) {
                                    if (otp.getText().toString().equals(storedOtp)) {
                                        try {
                                            data = password.getText().toString().getBytes("UTF-8");
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                        String encryptedpass = Base64.encodeToString(data, Base64.NO_WRAP);
                                        Log.d("pass", encryptedpass);
                                        JSONObject jsonObject = new JSONObject();
                                        try {
                                            jsonObject.put("name", name.getText().toString());
                                            jsonObject.put("password", encryptedpass);
                                            jsonObject.put("phone", unVarifiedNumber);
                                            jsonObject.put("email", email.getText().toString());
                                        } catch (Exception e) {
                                            Log.d("error", "error in creating jsonobject signup method");
                                        }
                                        newSignup(jsonObject);
                                    } else {
                                        UtilFunctions.makeToast(getActivity().getBaseContext(), "OTP Does not Match", Toast.LENGTH_SHORT);
                                    }
                                } else {
                                    UtilFunctions.makeToast(getActivity().getBaseContext(), "OTP can't be blank!", Toast.LENGTH_SHORT);
                                }
                            } else {
                                UtilFunctions.makeToast(getActivity().getBaseContext(), "Invalid Email Id !!", Toast.LENGTH_SHORT);
                            }
                        } else {
                            UtilFunctions.makeToast(getActivity().getBaseContext(), "Email can't be blank!", Toast.LENGTH_SHORT);
                        }
                    } else {
                        UtilFunctions.makeToast(getActivity().getBaseContext(), "Password Length should be between 5 to 10", Toast.LENGTH_SHORT);
                    }
                } else {
                    UtilFunctions.makeToast(getActivity().getBaseContext(), "Enter Name!!", Toast.LENGTH_SHORT);
                }
            }
        });
        return rootView;
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        getFragmentManager().popBackStack();
        return false;
    }

    private void sendotpTouser(String username) {
        JSONObject jsonObject = new JSONObject();

        //Random number generation ///
        int START = 100000;
        int jb;
        int END = 999999;
        Random rand = new Random();
        //get the range, casting to long to avoid overflow problems
        long range = (long) END - (long) START + 1;
        // compute a fraction of the range, 0 <= frac < range
        long fraction = (long) (range * rand.nextDouble());
        final int randomNumber = (int) (fraction + START);
        if (!SignUpAccount.isDestroy) {
            mSharedPreferences = getActivity().getSharedPreferences(URLContstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
            mEditor = mSharedPreferences.edit();
            mEditor.putString(URLContstant.KEY_OTP, Integer.toString(randomNumber));
            mEditor.putString(URLContstant.KEY_UNVARIFIED_NUMBER, username);
            mEditor.apply();
        }
        try {
            jsonObject.put("phone", username);
            jsonObject.put("otp", randomNumber);
        } catch (Exception e) {
            Log.d("error", "Error in verifying number ");
        }
        APIServices.getInstance().PostCall(getActivity(), "api/signup/otp/", jsonObject, new ResponseCallback() {
            @Override
            public void OnResponse(JSONObject Response) {
                if (Response != null) {
                    Log.d("otp", String.valueOf(randomNumber));
                }
            }
        });
    }


    public void newSignup(JSONObject data) {
        boolean bool;
        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("SignUp Request...");
        progressDialog.show();
        APIServices.getInstance().PostCall(getActivity(), "api/signup/", data, new ResponseCallback() {
            @Override
            public void OnResponse(JSONObject Response) {
                if (Response != null) {
                    progressDialog.dismiss();
                    if (!SignUpAccount.isDestroy) {
                        mEditor = mSharedPreferences.edit();
                        try {
                            mEditor.putString(URLContstant.KEY_USERNAME, name.getText().toString());
                            mEditor.putString(URLContstant.KEY_USER_EMAIL, email.getText().toString());
                            mEditor.putString(URLContstant.KEY_USER_PHONE, unVarifiedNumber);
                            mEditor.putBoolean(URLContstant.KEY_LOGGED_IN, true);
                            mEditor.putString(URLContstant.KEY_API_KEY, Response.getJSONObject("datasets").getString("API_KEY"));
                            mEditor.apply();
//                            JSONObject jsonuserInfo = new JSONObject();
//                            jsonuserInfo.put("username",name.getText().toString());
//                            jsonuserInfo.put("contact",unVarifiedNumber);
//                            AccountActivity.mixpanel.track("New user signup Successfully",jsonuserInfo);
                            Intent sendTokenservice = new Intent(getActivity(), SendRegistrationTokentoServer.class);
                            getActivity().startService(sendTokenservice);
                            Intent mainactivityIntent = new Intent(getActivity(), MainActivity.class);
                            mainactivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainactivityIntent);
                            getActivity().finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    progressDialog.dismiss();
                    UtilFunctions.makeToast(getActivity().getBaseContext(), "Already Registered On Moovo!!", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final SmsManager sms = SmsManager.getDefault();
            boolean isConfirmVisible = SignUpAccount.isActivityVisible();
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                storedOtp = mSharedPreferences.getString(URLContstant.KEY_OTP, "");
                try {
                    if (isConfirmVisible) {
                        Bundle bundle = intent.getExtras();
                        SmsMessage[] smsMessages = null;
                        String messages = "";
                        if (bundle != null) {
                            Object[] pdus = (Object[]) bundle.get("pdus");
                            smsMessages = new SmsMessage[pdus.length];
                            String Sender = null;
                            for (int i = 0; i < smsMessages.length; i++) {
                                smsMessages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                                Sender = smsMessages[i].getOriginatingAddress();
                                messages += "SMS From: " + smsMessages[i].getOriginatingAddress();
                                messages += " : ";
                                messages += smsMessages[i].getMessageBody();
                                messages += "\n";
                            }
                            String str = "MOOVO";
                            assert Sender != null;
                            Log.d("message", Sender + " " + storedOtp);
                            if (Sender != null && Sender.contains(str)) {
                                Readotp(storedOtp);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void Readotp(final String motp) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Reading OTP for verification");
        progressDialog.show();
        progressDialog.setCancelable(false);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (otp.getText().toString().equals("")) {
                    otp.setText(motp);
                    progressDialog.dismiss();
                }
            }
        }, 2000);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        getActivity().registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Receiver not registered")) {
                // Ignore this exception. This is exactly what is desired
            } else {
                // unexpected, re-throw
                throw e;
            }
        }
    }
}
