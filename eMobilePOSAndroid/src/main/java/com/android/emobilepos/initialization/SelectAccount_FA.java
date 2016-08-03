package com.android.emobilepos.initialization;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.android.emobilepos.R;
import com.android.emobilepos.mainmenu.MainMenu_FA;
import com.android.saxhandler.SaxLoginHandler;
import com.android.database.DBManager;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Post;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class SelectAccount_FA extends BaseFragmentActivityActionBar {
    private Context thisContext;
    private ProgressDialog myProgressDialog;
    private Activity activity;
    private Dialog promptDialog;

    private DBManager dbManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        activity = this;
        final MyPreferences myPref = new MyPreferences(this);

        if (myPref.getLogIn()) {
            dbManager = new DBManager(activity, Global.FROM_LOGIN_ACTIVITTY);
            if (dbManager.isNewDBVersion()) {
                if (dbManager.unsynchItemsLeft()) {
                    //there are unsynch item left...

                    Intent intent = new Intent(this, MainMenu_FA.class);
                    Bundle extras = new Bundle();
                    extras.putBoolean("unsynched_items", true);

                    intent.putExtras(extras);
                    startActivity(intent);
                    finish();
                } else {

                    AlertDialog.Builder alertDlogBuilder = new AlertDialog.Builder(activity);

                    promptDialog = alertDlogBuilder.setTitle("Urgent").setCancelable(false)
                            .setMessage("A new Database version must be installed...").
                                    setPositiveButton("Install", new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface thisDialog, int which) {
                                            // TODO Auto-generated method stub
                                            dbManager.updateDB();
                                            promptDialog.dismiss();
                                        }
                                    }).create();

                    promptDialog.show();
                }

            } else {
                Intent intent = new Intent(this, MainMenu_FA.class);
                startActivityForResult(intent, 0);
                finish();
            }
        } else {

            setContentView(R.layout.initialization_layout);
            final EditText acctNumber = (EditText) findViewById(R.id.initAccountNumber);
            final EditText acctPassword = (EditText) findViewById(R.id.initPassword);
            Button login = (Button) findViewById(R.id.loginButton);
            thisContext = this;


            login.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (!myPref.getLogIn()) {
                        myPref.setAcctNumber(acctNumber.getText().toString());
                        myPref.setAcctPassword(acctPassword.getText().toString());
                        String android_id = Secure.getString(thisContext.getContentResolver(), Secure.ANDROID_ID);
                        myPref.setDeviceID(android_id);

                        new validateLoginAsync().execute("");
                    }
                }
            });
        }
    }

    public class validateLoginAsync extends AsyncTask<String, String, String> {
        boolean proceed = false;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(thisContext);
            myProgressDialog.setMessage("Loading...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub

            Post post = new Post();
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SaxLoginHandler handler = new SaxLoginHandler();

            try {
                String xml = post.postData(0, activity, "");
                InputSource inSource = new InputSource(new StringReader(xml));

                SAXParser sp = spf.newSAXParser();
                XMLReader xr = sp.getXMLReader();
                xr.setContentHandler(handler);
                xr.parse(inSource);
                proceed = Boolean.parseBoolean(handler.getData().toLowerCase(Locale.getDefault()));
            } catch (Exception e) {
                // TODO Auto-generated catch block
//				Tracker tracker = EasyTracker.getInstance(activity);
//				tracker.send(MapBuilder.createException(e.getStackTrace().toString(), false).build());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();
            MyPreferences myPref = new MyPreferences(activity);
            if (proceed) {

                Intent intent = new Intent(thisContext, SelectEmployee_FA.class);
                startActivityForResult(intent, 0);

            } else {
                myPref.setLogIn(false);
                AlertDialog.Builder dialog = new AlertDialog.Builder(thisContext);
                dialog.setTitle("Error");
                dialog.setMessage("The provided information could not be validated. Please try again.");
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.cancel();
                    }
                });
                dialog.create().show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            finish();
        }
    }
}
