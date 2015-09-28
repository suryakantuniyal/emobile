package com.android.emobilepos.mainmenu;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import main.EMSDeviceManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.database.PrintersHandler;
import com.android.emobilepos.adapters.SynchMenuAdapter;
import com.emobilepos.app.R;
import com.android.support.DBManager;
import com.android.support.Global;
import com.android.support.MyPreferences;

public class MainMenu_FA extends FragmentActivity {

	private static AdapterTabs tabsAdapter;
	private ViewPager viewPager;
	public static Activity activity;
	private Global global;
	private boolean hasBeenCreated = false;
	private static MyPreferences myPref;
	private static int selectedPage = 0;
	private static ViewPager childViewPager;
	private TextView synchTextView, tvStoreForward;
	private ActionBar myBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main_menu);
		viewPager = (ViewPager) findViewById(R.id.main_menu_pager);
		synchTextView = (TextView) findViewById(R.id.synch_title);
		synchTextView.setVisibility(View.GONE);
		tvStoreForward = (TextView) findViewById(R.id.label_cc_offline);

		myPref = new MyPreferences(this);

		activity = this;
		global = (Global) getApplication();
		myBar = this.getActionBar();

		myBar.setDisplayShowTitleEnabled(false);
		myBar.setDisplayShowHomeEnabled(false);
		tabsAdapter = new AdapterTabs(this, viewPager);

		tabsAdapter.addTab(myBar.newTab().setText(R.string.sales_title), SalesTab_FR.class, null);
		myBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		tabsAdapter.addTab(myBar.newTab().setText(R.string.sync_title), SyncTab_FR.class, null);
		myBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		tabsAdapter.addTab(myBar.newTab().setText(R.string.hist_title), HistoryTab_FR.class, null);
		myBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		tabsAdapter.addTab(myBar.newTab().setText(R.string.routes_title), RoutesTab_FR.class, null);
		myBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// tabsAdapter.addTab(myBar.newTab().setText(R.string.admin_title),
		// SettingsMenuActivity.class, null);
		tabsAdapter.addTab(myBar.newTab().setText(R.string.admin_title), SettingsTab_FR.class, null);
		myBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		tabsAdapter.addTab(myBar.newTab().setText(R.string.report_title).setTag("Reports Fragment"), ReportTab_FR.class,
				null);
		myBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		tabsAdapter.addTab(myBar.newTab().setText(R.string.clock_title), ClockTab_FR.class, null);
		myBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		tabsAdapter.addTab(myBar.newTab().setText(R.string.about_title), AboutTab_FR.class, null);
		myBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		forceTabs();

		Bundle extras = activity.getIntent().getExtras();
		if (extras != null && extras.getBoolean("unsynched_items", false))
			myBar.setSelectedNavigationItem(1);

		hasBeenCreated = true;
	}

	@Override
	public void onResume() {

		if (global.isApplicationSentToBackground(activity))
			global.loggedIn = false;
		global.stopActivityTransitionTimer();

		if (hasBeenCreated && !global.loggedIn
				&& (myPref.printerType(true, -2) != Global.POWA || (myPref.printerType(true, -2) == Global.POWA
						&& (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null)))) {
			if (global.getGlobalDlog() != null)
				global.getGlobalDlog().dismiss();
			global.promptForMandatoryLogin(activity);
		}

		if (myPref.getPreferences(MyPreferences.pref_automatic_sync) && hasBeenCreated) {
			DBManager dbManager = new DBManager(activity, Global.FROM_SYNCH_ACTIVITY);
			// SQLiteDatabase db = dbManager.openWritableDB();
			dbManager.synchSend(false, true);
		}

		if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward))
			tvStoreForward.setVisibility(View.VISIBLE);
		else
			tvStoreForward.setVisibility(View.GONE);

		new autoConnectPrinter().execute("");
		super.onResume();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		forceTabs();
	}

	public void forceTabs() {
		try {
			final ActionBar actionBar = getActionBar();
			final Method setHasEmbeddedTabsMethod = actionBar.getClass().getDeclaredMethod("setHasEmbeddedTabs",
					boolean.class);
			setHasEmbeddedTabsMethod.setAccessible(true);
			setHasEmbeddedTabsMethod.invoke(actionBar, false);
		} catch (final Exception e) {
			// Handle issues as needed: log, warn user, fallback etc
			// This error is safe to ignore, standard tabs will appear.
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			this.moveTaskToBack(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onPause() {
		super.onPause();
		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		boolean isScreenOn = powerManager.isScreenOn();
		if (!isScreenOn)
			global.loggedIn = false;
		global.startActivityTransitionTimer();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		global.loggedIn = false;
		this.finish();
	}

	private class autoConnectPrinter extends AsyncTask<String, String, String> {

		StringBuilder sb = new StringBuilder();
		boolean isUSB = false;

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub

			PrintersHandler ph = new PrintersHandler(activity);
			Cursor c = ph.getPrinters();
			HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
			EMSDeviceManager edm;

			int size = c.getCount();

			if (c != null && size > 0
					&& (Global.multiPrinterManager == null || Global.multiPrinterManager.size() == 0)) {
				int i = 0;
				int i_printer_id = c.getColumnIndex("printer_id");
				int i_printer_type = c.getColumnIndex("printer_type");
				int i_cat_id = c.getColumnIndex("cat_id");
				int i_printer_ip = c.getColumnIndex("printer_ip");
				int i_printer_port = c.getColumnIndex("printer_port");
				do {
					if (tempMap.containsKey(c.getString(i_printer_id))) {
						Global.multiPrinterMap.put(c.getString(i_cat_id), tempMap.get(c.getString(i_printer_id)));
					} else {
						tempMap.put(c.getString(i_printer_id), i);
						Global.multiPrinterMap.put(c.getString(i_cat_id), i);

						edm = new EMSDeviceManager();
						Global.multiPrinterManager.add(edm);

						if (Global.multiPrinterManager.get(i).loadMultiDriver(activity, Global.STAR, 48, true,
								"TCP:" + c.getString(i_printer_ip), c.getString(i_printer_port)))
							sb.append(c.getString(i_printer_ip)).append(": ").append("Connected\n");
						else
							sb.append(c.getString(i_printer_ip)).append(": ").append("Failed to connect\n");

						i++;
					}

				} while (c.moveToNext());
			}
			c.close();
			String _portName = "";
			String _peripheralName = "";
			if ((myPref.swiperType(true, -2) != -1) && (Global.btSwiper == null)) {
				edm = new EMSDeviceManager();
				_portName = myPref.swiperMACAddress(true, null);
				_peripheralName = Global.getPeripheralName(myPref.swiperType(true, -2));
				Global.btSwiper = edm.getManager();
				// Global.btSwiper.loadDrivers(activity, myPref.swiperType(true,
				// -2), false);
				if (Global.btSwiper.loadMultiDriver(activity, myPref.swiperType(true, -2), 0, false,
						myPref.swiperMACAddress(true, null), null))
					sb.append(_peripheralName).append(": ").append("Connected\n");
				else
					sb.append(_peripheralName).append(": ").append("Failed to connect\n");
			}
			if ((myPref.sledType(true, -2) != -1) && (Global.btSled == null)) {
				edm = new EMSDeviceManager();
				Global.btSled = edm.getManager();
				_peripheralName = Global.getPeripheralName(myPref.sledType(true, -2));
				// Global.btSwiper.loadDrivers(activity, myPref.swiperType(true,
				// -2), false);
				if (Global.btSled.loadMultiDriver(activity, myPref.sledType(true, -2), 0, false, null, null))
					sb.append(_peripheralName).append(": ").append("Connected\n");
				else
					sb.append(_peripheralName).append(": ").append("Failed to connect\n");
			}
			if ((myPref.printerType(true, -2) != -1) && (Global.mainPrinterManager == null)) // ||(Global.mainPrinterManager!=null&&Global.mainPrinterManager.currentDevice==null)))
			{
				edm = new EMSDeviceManager();
				Global.mainPrinterManager = edm.getManager();
				_peripheralName = Global.getPeripheralName(myPref.printerType(true, -2));
				_portName = myPref.printerMACAddress(true, null);
				String _portNumber = myPref.getStarPort();
				boolean isPOS = myPref.posPrinter(true, false);
				int txtAreaSize = myPref.printerAreaSize(true, -1);

				if (myPref.printerType(true, -2) != Global.POWA) {
					if (Global.mainPrinterManager.loadMultiDriver(activity, myPref.printerType(true, -2), txtAreaSize,
							isPOS, _portName, _portNumber))
						sb.append(_peripheralName).append(": ").append("Connected\n");
					else
						sb.append(_peripheralName).append(": ").append("Failed to connect\n");
				} else
					isUSB = true;

			}

			return null;
		}

		@Override
		protected void onPostExecute(String unused) {
			if (!isUSB && sb.toString().length() > 0)
				Toast.makeText(activity, sb.toString(), Toast.LENGTH_LONG).show();
			else if (isUSB && Global.mainPrinterManager.currentDevice == null) {
				if (global.getGlobalDlog() != null)
					global.getGlobalDlog().dismiss();
				Global.mainPrinterManager.loadMultiDriver(activity, myPref.printerType(true, -2), 0, true, "", "");
			}
		}
	}

	public ViewPager getViewPager() {
		return childViewPager;
	}

	public TextView getSynchTextView() {
		return synchTextView;
	}

	private class AdapterTabs extends FragmentPagerAdapter
			implements ActionBar.TabListener, ViewPager.OnPageChangeListener {

		private final Context myContext;
		private final ViewPager myViewPager;
		private final ActionBar myActionBar;
		private final ArrayList<TabInfo> myTabs = new ArrayList<TabInfo>();

		final class TabInfo {
			private final Class<?> clazz;
			private final Bundle args;

			TabInfo(Class<?> _clazz, Bundle _args) {
				clazz = _clazz;
				args = _args;
			}
		}

		public AdapterTabs(FragmentActivity activity, ViewPager pager) {
			super(activity.getSupportFragmentManager());
			myContext = activity;
			myActionBar = activity.getActionBar();
			myViewPager = pager;
			myViewPager.setAdapter(this);
			myViewPager.setOnPageChangeListener(this);
		}

		public void addTab(ActionBar.Tab tab, Class<?> clzz, Bundle args) {
			TabInfo info = new TabInfo(clzz, args);
			tab.setTag(info);
			tab.setTabListener(this);
			myTabs.add(info);
			myActionBar.addTab(tab);
			notifyDataSetChanged();
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
			Object tag = tab.getTag();
			int size = myTabs.size();
			for (int i = 0; i < size; i++) {
				if (myTabs.get(i) == tag) {
					myViewPager.setCurrentItem(i);
				}
			}

			if (myTabs.get(0) == tag && hasBeenCreated) {
				// Toast.makeText(activity, "launch default trans",
				// Toast.LENGTH_LONG).show();
				SalesTab_FR.startDefault(activity, myPref.getPreferencesValue(MyPreferences.pref_default_transaction));
			} else if (selectedPage == 1) // Sync tab
			{
				childViewPager = myViewPager;
				ListView listView = (ListView) myViewPager.findViewById(R.id.synchListView);
				if (listView != null) {
					SynchMenuAdapter adapter = (SynchMenuAdapter) listView.getAdapter();
					adapter.notifyDataSetChanged();
				}
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageSelected(int index) {
			// TODO Auto-generated method stub
			selectedPage = index;
			myActionBar.setSelectedNavigationItem(index);

		}

		@Override
		public Fragment getItem(int index) {
			// TODO Auto-generated method stub
			TabInfo info = myTabs.get(index);
			return Fragment.instantiate(myContext, info.clazz.getName(), info.args);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return myTabs.size();
		}

	}

}
