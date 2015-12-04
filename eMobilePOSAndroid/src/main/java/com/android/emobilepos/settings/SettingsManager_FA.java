package com.android.emobilepos.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.database.CategoriesHandler;
import com.android.database.PayMethodsHandler;
import com.android.database.PrintersHandler;
import com.android.database.ShiftPeriodsDBHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.country.CountryPicker;
import com.android.emobilepos.country.CountryPickerListener;
import com.android.emobilepos.mainmenu.SettingsTab_FR;
import com.android.emobilepos.shifts.OpenShift_FA;
import com.android.support.DBManager;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.SynchMethods;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import main.EMSDeviceManager;

public class SettingsManager_FA extends FragmentActivity {
	private static int settingsType = 0;
	private static Activity activity;
	private static FragmentManager fragManager;
	private Global global;
	private boolean hasBeenCreated = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;
		global = (Global) getApplication();
		Bundle extras = this.getIntent().getExtras();
		settingsType = extras.getInt("settings_type");
		fragManager = getSupportFragmentManager();

		getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
		hasBeenCreated = true;
	}

	@Override
	public void onResume() {

		if (global.isApplicationSentToBackground(this))
			global.loggedIn = false;
		global.stopActivityTransitionTimer();

		if (hasBeenCreated && !global.loggedIn) {
			if (global.getGlobalDlog() != null)
				global.getGlobalDlog().dismiss();
			global.promptForMandatoryLogin(this);
		}
		super.onResume();
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

	public static class PrefsFragment extends PreferenceFragment implements OnPreferenceClickListener {
		private Dialog promptDialog;
		private AlertDialog.Builder dialogBuilder;
		private MyPreferences myPref;
		private List<String> macAddressList = new ArrayList<String>();
		private CheckBoxPreference storeForwardFlag;
		private Preference openShiftPref, defaultCountry, storeForwardTransactions;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			myPref = new MyPreferences(activity);
			PreferenceManager prefManager = null;
			// Load the preferences from an XML resource
			switch (settingsType) {
			case SettingsTab_FR.CASE_ADMIN:
				addPreferencesFromResource(R.xml.settings_admin_layout);
				prefManager = getPreferenceManager();
				prefManager.findPreference("pref_change_password").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_open_cash_drawer").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_configure_cash_drawer").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_transaction_num_prefix").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_customer_display").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_clear_images_cache").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_printek_info").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_star_info").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_snbc_setup").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_configure_ingenico_settings").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_connect_to_bluetooth_peripheral").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_connect_to_usb_peripheral").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_redetect_peripherals").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_delete_saved_peripherals").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_force_upload").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_backup_data").setOnPreferenceClickListener(this);
				prefManager.findPreference(MyPreferences.pref_config_genius_peripheral)
						.setOnPreferenceClickListener(this);
				configureDefaultCategory();
				configureDefaultPaymentMethod();

				prefManager.findPreference("pref_attribute_to_display").setOnPreferenceClickListener(this);

				openShiftPref = (Preference) getPreferenceManager().findPreference("pref_open_shift");
				if (!myPref.getShiftIsOpen()) {
					CharSequence c = new String(
							"\t\t" + getString(R.string.admin_close_shift) + " <" + myPref.getShiftClerkName() + ">");
					openShiftPref.setSummary(c);
				}
				openShiftPref.setOnPreferenceClickListener(this);

				prefManager.findPreference("pref_expenses").setOnPreferenceClickListener(this);

				defaultCountry = (Preference) prefManager.findPreference("pref_default_country");
				CharSequence temp = new String("\t\t" + myPref.defaultCountryName(true, null));
				defaultCountry.setSummary(temp);
				defaultCountry.setOnPreferenceClickListener(this);

				CheckBoxPreference _cbp_use_location_inv = (CheckBoxPreference) prefManager
						.findPreference("pref_enable_location_inventory");

				storeForwardTransactions = (Preference) prefManager
						.findPreference("pref_store_and_forward_transactions");
				storeForwardFlag = (CheckBoxPreference) prefManager.findPreference("pref_use_store_and_forward");
				storeForwardTransactions.setOnPreferenceClickListener(this);
				if (!myPref.storedAndForward(true, false)) {
					((PreferenceGroup) prefManager.findPreference("payment_section"))
							.removePreference(storeForwardTransactions);
					((PreferenceGroup) prefManager.findPreference("payment_section"))
							.removePreference(storeForwardFlag);
				}
				// ((PreferenceGroup)prefManager.findPreference("payment_section")).removePreference(storeForwardTransactions);
				_cbp_use_location_inv.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						// TODO Auto-generated method stub
						if (newValue instanceof Boolean) {
							if ((Boolean) newValue) {
								// sync Location Inventory
								DBManager dbManager = new DBManager(activity);
								SynchMethods sm = new SynchMethods(dbManager);
								sm.getLocationsInventory();
							}
						}
						return true;
					}
				});
				break;
			case SettingsTab_FR.CASE_MANAGER:
				addPreferencesFromResource(R.xml.settings_manager_layout);
				prefManager = getPreferenceManager();
				prefManager.findPreference("pref_clear_images_cache").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_connect_to_bluetooth_peripheral").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_connect_to_usb_peripheral").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_redetect_peripherals").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_delete_saved_peripherals").setOnPreferenceClickListener(this);
				openShiftPref = (Preference) getPreferenceManager().findPreference("pref_open_shift");
				if (!myPref.getShiftIsOpen()) {
					CharSequence c = new String(
							"\t\t" + getString(R.string.admin_close_shift) + " <" + myPref.getShiftClerkName() + ">");
					openShiftPref.setSummary(c);
				}
				openShiftPref.setOnPreferenceClickListener(this);

				break;
			case SettingsTab_FR.CASE_GENERAL:
				addPreferencesFromResource(R.xml.settings_general_layout);
				prefManager = getPreferenceManager();
				prefManager.findPreference("pref_clear_images_cache").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_connect_to_bluetooth_peripheral").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_connect_to_usb_peripheral").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_redetect_peripherals").setOnPreferenceClickListener(this);
				prefManager.findPreference("pref_delete_saved_peripherals").setOnPreferenceClickListener(this);
				break;

			}
		}

		@Override
		public boolean onPreferenceClick(Preference preference) {
			// TODO Auto-generated method stub
			Intent intent = null;
			switch (preference.getTitleRes()) {
			case R.string.config_change_password:
				changePassword(false, null);
				break;
			case R.string.config_open_cash_drawer:
				if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null)
					Global.mainPrinterManager.currentDevice.openCashDrawer();
				break;
			case R.string.config_configure_cash_drawer:
				break;
			case R.string.config_transaction_num_prefix:
				break;
			case R.string.config_customer_display:
				configureCustomerDisplayTerminal();
				break;
			case R.string.config_clear_images_cache:
				clearCache();
				break;
			case R.string.config_printek_info:
				break;
			case R.string.config_genius_peripheral:
				configureGeniusPeripheral();
				break;
			case R.string.config_star_info:
				promptStarPrinter();
				break;
			case R.string.config_snbc_setup:
				promptSNBCSetup();
				break;
			case R.string.config_configure_ingenico_settings:
				break;
			case R.string.config_connect_to_bluetooth_peripheral:
				connectBTDevice();
				break;
			case R.string.config_connect_to_usb_peripheral:
				connectUSBDevice();
				break;
			case R.string.config_redetect_peripherals:
				new autoConnectPrinter().execute();
				break;
			case R.string.config_store_and_forward_transactions:
				intent = new Intent(activity, ViewStoreForwardTrans_FA.class);
				startActivity(intent);
				break;
			case R.string.config_delete_saved_peripherals:
				myPref.forgetPeripherals();
				Toast.makeText(activity, "Peripherals have been erased", Toast.LENGTH_LONG).show();
				break;
			case R.string.config_attribute_to_display:
				break;
			case R.string.config_open_shift:
				if (myPref.getShiftIsOpen()) {
					intent = new Intent(activity, OpenShift_FA.class);
					startActivityForResult(intent, 0);
				} else
					promptCloseShift(true, 0);
				break;
			case R.string.config_expenses:
				break;
			case R.string.config_default_country:
				CountryPicker picker = new CountryPicker();
				final DialogFragment newFrag = picker;
				picker.setListener(new CountryPickerListener() {

					@Override
					public void onSelectCountry(String name, String code) {
						// TODO Auto-generated method stub
						myPref.defaultCountryCode(false, code);
						myPref.defaultCountryName(false, name);

						CharSequence temp = new String("\t\t" + name);
						defaultCountry.setSummary(temp);

						newFrag.dismiss();
					}
				});
				newFrag.show(fragManager, "dialog");
				break;
			case R.string.config_force_upload:
				confirmTroubleshoot(R.string.config_force_upload);
				break;
			case R.string.config_backup_data:
				confirmTroubleshoot(R.string.config_backup_data);
				break;
			}
			return false;
		}

		private void changePassword(final boolean isReenter, final String origPwd) {
			final Dialog globalDlog = new Dialog(activity, R.style.Theme_TransparentTest);
			globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			globalDlog.setCancelable(true);
			globalDlog.setCanceledOnTouchOutside(true);
			globalDlog.setContentView(R.layout.dlog_field_single_layout);

			final EditText viewField = (EditText) globalDlog.findViewById(R.id.dlogFieldSingle);
			viewField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
			TextView viewTitle = (TextView) globalDlog.findViewById(R.id.dlogTitle);
			TextView viewMsg = (TextView) globalDlog.findViewById(R.id.dlogMessage);
			viewTitle.setText(R.string.dlog_title_confirm);
			if (!isReenter) {
				viewTitle.setText(R.string.enter_password);
				viewMsg.setText(R.string.password_five_char_long);
			} else {
				viewTitle.setText(R.string.reenter_password);
				viewMsg.setVisibility(View.GONE);
			}

			Button btnOk = (Button) globalDlog.findViewById(R.id.btnDlogSingle);
			btnOk.setText(R.string.button_ok);
			btnOk.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					globalDlog.dismiss();

					String value = viewField.getText().toString().trim();
					if (!isReenter && value.length() >= 5) {
						changePassword(true, value);
					} else if (isReenter && origPwd != null && value.equals(origPwd)) {
						myPref.setApplicationPassword(value);
					}
				}
			});
			globalDlog.show();
		}

		private void configureDefaultCategory() {
			ListPreference lp = (ListPreference) getPreferenceManager()
					.findPreference(MyPreferences.pref_default_category);
			CategoriesHandler handler = new CategoriesHandler(activity);
			List<String[]> categories = handler.getCategories();
			int size = categories.size();
			CharSequence[] catEntries = new String[size + 1];
			CharSequence[] catEntriesValues = new String[size + 1];

			if (size > 0) {
				catEntries[0] = "None";
				catEntriesValues[0] = "0";
				for (int i = 0; i < size; i++) {
					catEntries[i + 1] = categories.get(i)[0];
					catEntriesValues[i + 1] = categories.get(i)[1];
				}
			}

			if (catEntries[0] == null || catEntriesValues[0] == null) {
				catEntries[0] = "None";
				catEntriesValues[0] = "0";
			}

			lp.setEntries(catEntries);
			lp.setEntryValues(catEntriesValues);
		}

		private void configureDefaultPaymentMethod() {
			ListPreference lp = (ListPreference) getPreferenceManager()
					.findPreference(MyPreferences.pref_default_payment_method);
			PayMethodsHandler handler = new PayMethodsHandler(activity);
			List<String[]> list = handler.getPayMethod();
			int size = list.size();
			CharSequence[] entries = new String[size + 1];
			CharSequence[] entriesValues = new String[size + 1];
			if (size > 0) {
				entries[0] = "None";
				entriesValues[0] = "0";
				for (int i = 0; i < size; i++) {
					entries[i + 1] = list.get(i)[1];
					entriesValues[i + 1] = list.get(i)[0];
				}
			}
			if (entries[0] == null || entriesValues[0] == null) {
				entries[0] = "None";
				entriesValues[0] = "0";
			}
			lp.setEntries(entries);
			lp.setEntryValues(entriesValues);
		}

		private void configureCustomerDisplayTerminal() {
			final Dialog globalDlog = new Dialog(activity, R.style.Theme_TransparentTest);
			globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			globalDlog.setCancelable(true);
			globalDlog.setCanceledOnTouchOutside(true);
			globalDlog.setContentView(R.layout.dlog_field_double_layout);

			final EditText row1 = (EditText) globalDlog.findViewById(R.id.dlogFieldRow1);
			final EditText row2 = (EditText) globalDlog.findViewById(R.id.dlogFieldRow2);

			row1.setHint(myPref.cdtLine1(true, null));
			row2.setHint(myPref.cdtLine2(true, null));
			TextView viewTitle = (TextView) globalDlog.findViewById(R.id.dlogTitle);
			TextView viewMsg = (TextView) globalDlog.findViewById(R.id.dlogMessage);
			viewTitle.setText(R.string.dlog_title_customer_display);
			viewMsg.setText(R.string.dlog_msg_enter_data);

			Button btnOk = (Button) globalDlog.findViewById(R.id.btnDlogSingle);
			btnOk.setText(R.string.button_ok);
			btnOk.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					String value1 = row1.getText().toString().trim();
					String value2 = row2.getText().toString().trim();
					int size1 = value1.length();
					int size2 = value2.length();
					if (size1 > 20 || size2 > 20) {
						if (size1 > 20)
							row1.setText("");
						if (size2 > 20)
							row2.setText("");

						Toast.makeText(activity, "Only 20 characters are allowed per line", Toast.LENGTH_LONG).show();
					} else {
						myPref.cdtLine1(false, value1);
						myPref.cdtLine2(false, value2);

						if (myPref.isSam4s(true, true)) {
							Global.showCDTDefault(activity);
						}

						globalDlog.dismiss();
					}
				}
			});
			globalDlog.show();
		}

		private void clearCache() {
			File cacheDir = new File(myPref.getCacheDir());
			File[] files = cacheDir.listFiles();
			if (files != null) {
				for (File file : files)
					file.delete();
			}
			Toast.makeText(activity, "Cache cleared", Toast.LENGTH_LONG).show();
		}

		private void confirmTroubleshoot(final int type) {
			promptDialog = new Dialog(activity, R.style.Theme_TransparentTest);
			promptDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			promptDialog.setCancelable(false);
			promptDialog.setContentView(R.layout.dlog_btn_left_right_layout);

			TextView viewTitle = (TextView) promptDialog.findViewById(R.id.dlogTitle);
			TextView viewMsg = (TextView) promptDialog.findViewById(R.id.dlogMessage);
			viewTitle.setText(R.string.dlog_title_confirm);
			if (type == R.string.config_force_upload)
				viewMsg.setText(R.string.dlog_msg_confirm_force_upload);
			else
				viewMsg.setText(R.string.dlog_msg_confirm_backup_data);
			Button btnYes = (Button) promptDialog.findViewById(R.id.btnDlogLeft);
			Button btnNo = (Button) promptDialog.findViewById(R.id.btnDlogRight);
			btnYes.setText(R.string.button_yes);
			btnNo.setText(R.string.button_no);

			btnYes.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					promptDialog.dismiss();
					switch (type) {
					case R.string.config_force_upload:
						DBManager dbManager = new DBManager(activity, Global.FROM_SYNCH_ACTIVITY);
						// SQLiteDatabase db = dbManager.openWritableDB();
						dbManager.forceSend();
						break;
					case R.string.config_backup_data:
						DBManager manag = new DBManager(activity);
						manag.exportDBFile();
						break;
					}
				}
			});
			btnNo.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					promptDialog.dismiss();
				}
			});
			promptDialog.show();
		}

		private void configureGeniusPeripheral() {
			final MyPreferences myPref = new MyPreferences(activity);
			final EditText input = new EditText(activity);
			input.setText(myPref.getGeniusIP());
			input.setInputType(InputType.TYPE_CLASS_TEXT);
			dialogBuilder = new AlertDialog.Builder(activity);
			input.setSelection(input.getText().length());
			dialogBuilder.setView(input);

			promptDialog = dialogBuilder.setTitle("Enter Genius IP Address")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface thisDialog, int which) {
							// TODO Auto-generated method stub
							final String firstValue = input.getText().toString();
							if (firstValue.length() > 0) {
								myPref.setGeniusIP(firstValue);
								thisDialog.dismiss();
							} else
								promptDialog.setTitle("Try again...");

						}
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface thisDialog, int which) {
							// TODO Auto-generated method stub
							thisDialog.dismiss();

						}
					}).create();

			promptDialog.show();
		}

		private void promptStarPrinter() {
			final EditText ipAddress = new EditText(activity);
			final EditText portNumber = new EditText(activity);

			ipAddressFilter(ipAddress);

			ipAddress.setHint(R.string.dlog_star_ip);
			portNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
			portNumber.setHint(R.string.dlog_star_port);

			ipAddress.setText(myPref.getStarIPAddress());
			portNumber.setText(myPref.getStarPort());
			LinearLayout ll = new LinearLayout(activity);
			ll.setOrientation(LinearLayout.VERTICAL);
			ll.addView(ipAddress);
			ll.addView(portNumber);

			dialogBuilder = new AlertDialog.Builder(activity);
			dialogBuilder.setView(ll);
			dialogBuilder.setTitle(R.string.dlog_star_congifure);

			dialogBuilder.setCancelable(true);
			dialogBuilder.setPositiveButton(R.string.button_connect, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub

					myPref.setStarIPAddress(ipAddress.getText().toString());
					myPref.setStarPort(portNumber.getText().toString());
					myPref.printerMACAddress(false, "TCP:" + ipAddress.getText().toString());

					EMSDeviceManager edm = new EMSDeviceManager();
					Global.mainPrinterManager = edm.getManager();
					Global.mainPrinterManager.loadDrivers(activity, Global.STAR, true);

				}
			}).create();
			promptDialog = dialogBuilder.create();

			promptDialog.show();
		}

		private void promptSNBCSetup() {
			final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
			dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dlog.setCancelable(true);
			dlog.setCanceledOnTouchOutside(false);
			dlog.setContentView(R.layout.config_snbc_setup_layout);

			final Button btnConnect = (Button) dlog.findViewById(R.id.btnConnect);

			btnConnect.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					myPref.setPrinterType(Global.SNBC);
					// myPref.printerMACAddress(false, macAddressList.get(pos));

					EMSDeviceManager edm = new EMSDeviceManager();
					Global.mainPrinterManager = edm.getManager();
					Global.mainPrinterManager.loadDrivers(activity, Global.SNBC, false);
					dlog.dismiss();
				}
			});
			dlog.show();
		}

		private void ipAddressFilter(EditText et) {
			et.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
			// et.setInputType(InputType.TYPE_CLASS_PHONE);
			et.setFilters(new InputFilter[] { new InputFilter() {
				@Override
				public CharSequence filter(CharSequence source, int start, int end, android.text.Spanned dest,
						int dstart, int dend) {
					if (end > start) {
						String destTxt = dest.toString();
						String resultingTxt = destTxt.substring(0, dstart) + source.subSequence(start, end)
								+ destTxt.substring(dend);
						if (!resultingTxt.matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
							return "";
						} else {
							String[] splits = resultingTxt.split("\\.");
							for (int i = 0; i < splits.length; i++) {
								if (Integer.valueOf(splits[i]) > 255) {
									return "";
								}
							}
						}
					}
					return null;
				}
			} });

			et.addTextChangedListener(new TextWatcher() {
				boolean deleting = false;
				int lastCount = 0;

				@Override
				public void afterTextChanged(Editable s) {
					if (!deleting) {
						String working = s.toString();
						String[] split = working.split("\\.");
						String string = split[split.length - 1];
						if (string.length() == 3 || string.equalsIgnoreCase("0")
								|| (string.length() == 2 && Character.getNumericValue(string.charAt(0)) > 1)) {
							s.append('.');
							return;
						}
					}
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					if (lastCount < count) {
						deleting = false;
					} else {
						deleting = true;
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					// Nothing happens here
				}
			});
		}

		private void promptCloseShift(final boolean askAmount, final double amount) {
			final MyPreferences myPref = new MyPreferences(activity);
			final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
			dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dlog.setCancelable(false);
			dlog.setCanceledOnTouchOutside(false);
			dlog.setContentView(R.layout.dlog_field_single_two_btn);

			final EditText viewField = (EditText) dlog.findViewById(R.id.dlogFieldSingle);
			TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
			TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
			if (askAmount) {
				viewField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
				viewTitle.setText(R.string.enter_cash_close_amount);
				viewMsg.setVisibility(View.GONE);

			} else {
				viewField.setVisibility(View.GONE);
				viewTitle.setText(R.string.dlog_title_confirm);
				viewMsg.setText(
						activity.getString(R.string.close_amount_is) + " " + Global.formatDoubleToCurrency(amount));

			}
			Button btnYes = (Button) dlog.findViewById(R.id.btnDlogLeft);
			Button btnNo = (Button) dlog.findViewById(R.id.btnDlogRight);
			btnYes.setText(R.string.button_ok);
			btnNo.setText(R.string.button_cancel);

			btnYes.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dlog.dismiss();
					if (askAmount) {
						promptCloseShift(false, Global.formatNumFromLocale(viewField.getText().toString()));
					} else {

						ShiftPeriodsDBHandler handler = new ShiftPeriodsDBHandler(activity);
						handler.updateShift(myPref.getShiftID(), "entered_close_amount", Double.toString(amount));
						handler.updateShift(myPref.getShiftID(), "endTime", Global.getCurrentDate());
						handler.updateShift(myPref.getShiftID(), "endTimeLocal", Global.getCurrentDate());

						myPref.setShiftIsOpen(true);
						openShiftPref.setSummary("");
					}
				}
			});
			btnNo.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dlog.dismiss();
				}
			});
			dlog.show();
		}

		private void connectBTDevice() {
			ListView listViewPairedDevices = new ListView(activity);
			ArrayAdapter<String> bondedAdapter;
			dialogBuilder = new AlertDialog.Builder(activity);

			List<String> pairedDevicesList = getListPairedDevices();
			final String[] val = pairedDevicesList.toArray(new String[pairedDevicesList.size()]);
			bondedAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, val);
			listViewPairedDevices.setAdapter(bondedAdapter);

			listViewPairedDevices.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int pos, long arg3) {
					// TODO Auto-generated method stub
					promptDialog.dismiss();

					dialogBuilder = new AlertDialog.Builder(activity);
					dialogBuilder.setTitle(R.string.dlog_title_connect_to_device);
					dialogBuilder.setMessage(val[pos]);
					dialogBuilder.setNegativeButton(R.string.button_no, null);
					dialogBuilder.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							MyPreferences myPref = new MyPreferences(activity);
							if (val[pos].toUpperCase(Locale.getDefault()).contains("MAGTEK")) // magtek
																								// swiper
							{
								myPref.swiperType(false, Global.MAGTEK);
								myPref.swiperMACAddress(false, macAddressList.get(pos));

								EMSDeviceManager edm = new EMSDeviceManager();
								Global.btSwiper = edm.getManager();
								Global.btSwiper.loadDrivers(activity, Global.MAGTEK, false);

							} else if (val[pos].toUpperCase(Locale.getDefault()).contains("STAR")) // star
																									// micronics
							{
								myPref.setPrinterType(Global.STAR);
								myPref.printerMACAddress(false, "BT:" + macAddressList.get(pos));

								EMSDeviceManager edm = new EMSDeviceManager();
								Global.mainPrinterManager = edm.getManager();
								Global.mainPrinterManager.loadDrivers(activity, Global.STAR, false);

							} else if (val[pos].toUpperCase(Locale.getDefault()).contains("P25")) // bamboo
							{
								myPref.setPrinterType(Global.BAMBOO);
								myPref.printerMACAddress(false, macAddressList.get(pos));

								EMSDeviceManager edm = new EMSDeviceManager();
								Global.mainPrinterManager = edm.getManager();
								Global.mainPrinterManager.loadDrivers(activity, Global.BAMBOO, false);

							} else if (val[pos].toUpperCase(Locale.getDefault()).contains("ISMP")
									|| val[pos].toUpperCase(Locale.getDefault()).contains("ICM")) {
								myPref.sledType(false, Global.ISMP);

								EMSDeviceManager edm = new EMSDeviceManager();

								Global.btSled = edm.getManager();
								Global.btSled.loadDrivers(activity, Global.ISMP, false);
							} else if (val[pos].toUpperCase(Locale.getDefault()).contains("EM220")) // Zebra
							{
								myPref.setPrinterType(Global.ZEBRA);
								myPref.printerMACAddress(false, macAddressList.get(pos));

								EMSDeviceManager edm = new EMSDeviceManager();
								Global.mainPrinterManager = edm.getManager();
								Global.mainPrinterManager.loadDrivers(activity, Global.ZEBRA, false);
							} else if (val[pos].toUpperCase(Locale.getDefault()).contains("MP")) // Oneil
							{
								myPref.setPrinterType(Global.ONEIL);
								myPref.printerMACAddress(false, macAddressList.get(pos));

								EMSDeviceManager edm = new EMSDeviceManager();
								Global.mainPrinterManager = edm.getManager();
								Global.mainPrinterManager.loadDrivers(activity, Global.ONEIL, false);
							} else {
								Toast.makeText(activity, R.string.err_invalid_device, Toast.LENGTH_LONG).show();
							}
						}
					});

					promptDialog = dialogBuilder.create();
					promptDialog.show();

					return false;
				}
			});

			dialogBuilder.setView(listViewPairedDevices);
			dialogBuilder.setTitle(R.string.dlog_title_select_device_to_connect);
			dialogBuilder.setNegativeButton(R.string.button_cancel, null);
			promptDialog = dialogBuilder.create();
			promptDialog.show();
		}

		private void connectUSBDevice() {

			MyPreferences myPref = new MyPreferences(activity);
			EMSDeviceManager edm = new EMSDeviceManager();

			if (myPref.isAsura(true, false)) {
				myPref.setPrinterType(Global.ASURA);
				Global.mainPrinterManager = edm.getManager();
				Global.mainPrinterManager.loadDrivers(activity, Global.ASURA, false);

			} else if (myPref.isPAT100()) {
				myPref.setPrinterType(Global.PAT100);
				Global.mainPrinterManager = edm.getManager();
				Global.mainPrinterManager.loadDrivers(activity, Global.PAT100, false);
			} else if (myPref.isEM100()) {
				myPref.setPrinterType(Global.EM100);
				Global.mainPrinterManager = edm.getManager();
				Global.mainPrinterManager.loadDrivers(activity, Global.EM100, false);
			} else {
				myPref.setPrinterType(Global.POWA);
				Global.mainPrinterManager = edm.getManager();
				Global.mainPrinterManager.loadDrivers(activity, Global.POWA, false);
			}
		}

		private List<String> getListPairedDevices() {
			List<String> nameList = new ArrayList<String>();
			try {
				BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				Set<BluetoothDevice> bondedSet = bluetoothAdapter.getBondedDevices();

				if (bondedSet.size() > 0) {
					for (BluetoothDevice device : bondedSet) {
						nameList.add(device.getName());
						macAddressList.add(device.getAddress());
					}
					return nameList;
				} else {
					nameList.clear();
					nameList.add("No Devices");
					macAddressList.clear();
					return nameList;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				StringBuilder sb = new StringBuilder();
				sb.append(e.getMessage())
						.append(" [com.android.emobilepos.SettingsMenuActiv (at Class.getPairedDevices)]");

//				Tracker tracker = EasyTracker.getInstance(activity);
//				tracker.send(MapBuilder.createException(sb.toString(), false).build());
			}
			return null;
		}

		private class autoConnectPrinter extends AsyncTask<Void, Void, Void> {

			StringBuilder sb = new StringBuilder();
			private ProgressDialog progressDlog;

			@Override
			protected void onPreExecute() {
				progressDlog = new ProgressDialog(activity);
				progressDlog.setMessage("Connecting...");
				progressDlog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDlog.setCancelable(false);
				progressDlog.show();
			}

			@Override
			protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub

				PrintersHandler ph = new PrintersHandler(activity);
				Cursor c = ph.getPrinters();
				HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
				EMSDeviceManager edm;

				int size = c.getCount();

				if (c != null && size > 0) {
					Global.multiPrinterManager.clear();
					Global.multiPrinterMap.clear();
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

				String _portName = "";
				String _peripheralName = "";
				if ((myPref.swiperType(true, -2) != -1)
						&& (Global.btSwiper == null || Global.btSwiper.currentDevice == null)) {
					edm = new EMSDeviceManager();
					_portName = myPref.swiperMACAddress(true, null);
					_peripheralName = Global.getPeripheralName(myPref.swiperType(true, -2));
					Global.btSwiper = edm.getManager();
					if (Global.btSwiper.loadMultiDriver(activity, myPref.swiperType(true, -2), 0, false,
							myPref.swiperMACAddress(true, null), null))
						sb.append(_peripheralName).append(": ").append("Connected\n");
					else
						sb.append(_peripheralName).append(": ").append("Failed to connect\n");
				} else if (myPref.swiperType(true, -2) != -1 && Global.btSwiper != null
						&& Global.btSwiper.currentDevice != null) {
					_peripheralName = Global.getPeripheralName(myPref.swiperType(true, -2));
					sb.append(_peripheralName).append(": ").append("Connected\n");
				}
				if ((myPref.getPrinterType()!= -1)
						&& (Global.mainPrinterManager == null || (Global.mainPrinterManager.currentDevice == null))) {
					edm = new EMSDeviceManager();
					Global.mainPrinterManager = edm.getManager();
					_peripheralName = Global.getPeripheralName(myPref.getPrinterType());
					_portName = myPref.printerMACAddress(true, null);
					String _portNumber = myPref.getStarPort();
					boolean isPOS = myPref.posPrinter(true, false);
					int txtAreaSize = myPref.printerAreaSize(true, -1);

					if (Global.mainPrinterManager.loadMultiDriver(activity, myPref.getPrinterType(), txtAreaSize,
							isPOS, _portName, _portNumber))
						sb.append(_peripheralName).append(": ").append("Connected\n");
					else
						sb.append(_peripheralName).append(": ").append("Failed to connect\n");

				} else if (myPref.getPrinterType() != -1 && Global.mainPrinterManager != null
						&& Global.mainPrinterManager.currentDevice != null) {
					_peripheralName = Global.getPeripheralName(myPref.getPrinterType());
					sb.append(_peripheralName).append(": ").append("Connected\n");

				}

				return null;
			}

			@Override
			protected void onPostExecute(Void unused) {
				progressDlog.dismiss();
				if (sb.toString().length() > 0)
					Global.showPrompt(activity, R.string.dlog_title_confirm, sb.toString());
			}
		}

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {

			if (resultCode == 1) {
				if (!myPref.getShiftIsOpen()) {
					CharSequence c = new String(
							"\t\t" + getString(R.string.admin_close_shift) + " <" + myPref.getShiftClerkName() + ">");
					openShiftPref.setSummary(c);
				}
			}
		}

	}

}