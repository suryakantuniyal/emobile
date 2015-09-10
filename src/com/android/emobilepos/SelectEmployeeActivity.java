package com.android.emobilepos;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.android.saxhandler.SaxAllEmployeesHandler;
import com.android.saxhandler.SaxLoginHandler;
import com.android.saxhandler.SaxSelectedEmpHandler;
import com.android.support.MyPreferences;
import com.android.support.Post;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class SelectEmployeeActivity extends Activity {
	private ListView myListView;
	private ListViewAdapter myAdapter;
	private List<String> empName = new ArrayList<String>();
	private List<String> empID = new ArrayList<String>();
	private Context thisContext;
	private Activity activity;
	
	private ProgressDialog myProgressDialog;
	private AlertDialog.Builder dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.initialization_select_employee);
		thisContext = this;
		activity = this;
		
		new validateEmployeesAsync().execute("");

	}

	public class validateEmployeesAsync extends AsyncTask<String, String, String> {
		boolean succeeded = false;
		String errorMsg = "";

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
				String xml = post.postData(1, activity, "");
				InputSource inSource = new InputSource(new StringReader(xml));
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
				xr.setContentHandler(handler);
				xr.parse(inSource);
				boolean deviceID = Boolean.parseBoolean(handler.getData().toLowerCase());
				if (!deviceID) {
					xml = post.postData(2, activity, "");
					inSource = new InputSource(new StringReader(xml));
					xr.parse(inSource);
					if (!handler.getData().isEmpty()&&!handler.getData().equals("0")) {
						MyPreferences myPref = new MyPreferences(activity);
						myPref.setActivKey(handler.getData());
						
						xml = post.postData(3, activity, "");
						SaxAllEmployeesHandler hdl = new SaxAllEmployeesHandler();
						inSource = new InputSource(new StringReader(xml));
						xr.setContentHandler(hdl);
						xr.parse(inSource);

						empName = hdl.getEmpName();
						empID = hdl.getEmpId();

						succeeded = true;
					}
					else
					{
						errorMsg = "There were no license available, please contact support.";
					}
				}
				else
					errorMsg = "The provided information could not be validated. Please try again.";

			} catch (Exception e) {
				// TODO Auto-generated catch block
				StringBuilder sb = new StringBuilder();
				sb.append(e.getMessage()).append(" [com.android.emobilepos.SelectEmployeeActiv (at Class.validateEmployeeAsync)]");
				handleGoogleAnalytic (sb.toString());
			}
			return null;
		}

		@Override
		protected void onPostExecute(String unused) {
			myProgressDialog.dismiss();
			if (succeeded) {

				myListView = (ListView) findViewById(R.id.employeeListView);
				myAdapter = new ListViewAdapter(thisContext);
				myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
						// TODO Auto-generated method stub

						/*MyPreferences myPref = new MyPreferences(activity);
						myPref.setEmpID(empID.get(position));
						new selectEmployeesAsync().execute("");*/
						promptValidate(position);
						
					}
				});

				Button reload = (Button) findViewById(R.id.reloadButton);
				reload.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						// myListView.invalidateViews();
						new validateEmployeesAsync().execute("");
					}
				});

				myListView.setAdapter(myAdapter);
			} else {
				AlertDialog.Builder dialog = new AlertDialog.Builder(thisContext);
				dialog.setTitle("Error");
				dialog.setMessage(errorMsg);
				dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.cancel();
						finish();
					}
				});
				dialog.create().show();
			}

		}

	}

	public class selectEmployeesAsync extends AsyncTask<String, String, String> {
		boolean succeeded = false;

		@Override
		protected void onPreExecute() {
			myProgressDialog = new ProgressDialog(thisContext);
			myProgressDialog.setMessage("Loading...");
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			// myProgressDialog.setMax(100);
			myProgressDialog.show();

		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			if (AssignEmployees() && DownloadPayID()&&DisableEmployee()) 												// &&DisableEmployee()
			{
				succeeded = true;
			}
			return null;
		}

		@Override
		protected void onPostExecute(String unused) {
			myProgressDialog.dismiss();
			if (succeeded) {
				Intent intent = new Intent(thisContext, RegisterPasswordActivity.class);
				startActivityForResult(intent, 0);
			} else {
				AlertDialog.Builder dialog = new AlertDialog.Builder(thisContext);
				dialog.setTitle("Error");
				dialog.setMessage("This employee selection could not be processed. Please try again.");
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

	public boolean AssignEmployees() {
		Post post = new Post();

		SAXParserFactory spf = SAXParserFactory.newInstance();
		SaxSelectedEmpHandler handler = new SaxSelectedEmpHandler(this);

		try {
			String xml = post.postData(4, activity, "");
			InputSource inSource = new InputSource(new StringReader(xml));

			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(handler);
			xr.parse(inSource);

			MyPreferences myPref = new MyPreferences(activity);
			List<String[]> data = handler.getEmpData();
			myPref.setAllEmpData(data);

			return true;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.SelectEmployeeActiv (at Class.AssignEmployee)]");
			handleGoogleAnalytic (sb.toString());
		}
		return false;
	}

	public boolean DisableEmployee() {
		Post post = new Post();

		SAXParserFactory spf = SAXParserFactory.newInstance();
		SaxLoginHandler handler = new SaxLoginHandler();

		try {
			String xml = post.postData(5, activity, "");
			InputSource inSource = new InputSource(new StringReader(xml));

			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(handler);
			xr.parse(inSource);

			return Boolean.parseBoolean(handler.getData());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.SelectEmployeeActiv (at Class.DisableEmployee)]");
			handleGoogleAnalytic (sb.toString());
		}
		return false;
	}

	public boolean DownloadPayID() {
		Post post = new Post();

		SAXParserFactory spf = SAXParserFactory.newInstance();
		SaxLoginHandler handler = new SaxLoginHandler();

		try {
			String xml = post.postData(6, activity, "");
			InputSource inSource = new InputSource(new StringReader(xml));

			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(handler);
			xr.parse(inSource);

			if (!handler.getData().isEmpty()) {
				MyPreferences myPref = new MyPreferences(activity);
				myPref.setLastPayID(handler.getData());
				return true;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.SelectEmployeeActiv (at Class.DownloadPayID)]");
			handleGoogleAnalytic (sb.toString());
		}
		return false;
	}

	public class ListViewAdapter extends BaseAdapter {
		private LayoutInflater myInflater;

		public ListViewAdapter(Context context) {
			myInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return empName.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder;

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = myInflater.inflate(R.layout.select_customer_adapter, null);
				holder.employeeName = (TextView) convertView.findViewById(R.id.employeeName);
				holder.employeeName.setText(empName.get(position));

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();

				holder.employeeName.setText(empName.get(position));
			}
			return convertView;
		}

		public class ViewHolder {
			TextView employeeName;
		}
	}
	
	private void promptValidate(final int position)
	{
		dialog = new AlertDialog.Builder(activity);
		dialog.setTitle(getString(R.string.login_select_this_employee));
		dialog.setMessage(empName.get(position));
		dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				MyPreferences myPref = new MyPreferences(activity);
				myPref.setEmpID(empID.get(position));
				new selectEmployeesAsync().execute("");
				
			}
		});
		dialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		dialog.create().show();
	}
	
	
	private void handleGoogleAnalytic (String stack)
	{
		Tracker tracker = EasyTracker.getInstance(activity);
		tracker.send(MapBuilder.createException(stack, false).build());
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == -1) {

			setResult(-1);
			finish();
		}
	}

}
