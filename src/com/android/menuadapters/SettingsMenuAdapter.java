package com.android.menuadapters;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;

import android.widget.ToggleButton;

import android.widget.TextView;

import com.android.emobilepos.R;
import com.android.support.Global;
import com.android.support.MyPreferences;

public class SettingsMenuAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private MyPreferences myPref;

	private Activity activity;

	private List<Integer> requireAdminPass;

	private int[] lvTitle;
	private Map<Integer, Integer> settingsType;
	private Dialog promptDialog;
	private AlertDialog.Builder dialogBuilder;
	private boolean isFirstLaunch = true;
	private boolean validPassword = true;


	public SettingsMenuAdapter(Activity activity) {
		mInflater = LayoutInflater.from(activity.getApplicationContext());
		this.activity = activity;
		myPref = new MyPreferences(activity);

	
//		lvTitle = Global.getSettingsArray();
//		settingsType = Global.settingsMap;
//		requireAdminPass = Arrays.asList(Global.auto_synch, Global.change_pass, Global.require_pass, Global.show_remove_void,
//				Global.configure_home, Global.block_price_level, Global.admin_price, Global.require_pass_clockout);

	}


	
	private String getString(int id)
	{
		return activity.getResources().getString(id);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final ViewHolder holder;
		int type = getItemViewType(position);

		if (convertView == null) {

			
			holder = new ViewHolder();
			switch (type) {
			case 0: // Divider
				convertView = mInflater.inflate(R.layout.settings_listviewdivider, null);

				holder.textLine = (TextView) convertView.findViewById(R.id.settingsTitle);
				break;

			case 1: // On/off Switch
				convertView = mInflater.inflate(R.layout.settings_lvswitchadapter, null);

				isFirstLaunch = true;
				holder.textLine = (TextView) convertView.findViewById(R.id.toggleText);
				holder.lvSwitch = (ToggleButton) convertView.findViewById(R.id.toggleButton1);
				
				holder.lvSwitch.setTextOff(getString(R.string.switch_off));
				holder.lvSwitch.setTextOn(getString(R.string.switch_on));

				holder.textLine.setText(lvTitle[position]);

				holder.lvSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						// TODO Auto-generated method stub
						if (isChecked)
						{
							if(!requireAdminPass.contains(lvTitle[position]))
								myPref.setSettings(lvTitle[position], true);
							else 
								{if(!isFirstLaunch)
								askForAdminPassDlg(lvTitle[position],true,holder);	
								}
						}
						else
						{
							if(!requireAdminPass.contains(lvTitle[position]))
								myPref.setSettings(lvTitle[position], false);
							else 
								{if(!isFirstLaunch)
								askForAdminPassDlg(lvTitle[position],false,holder);
								}
						}
					}
				});

				holder.lvSwitch.setChecked(myPref.getSettings(lvTitle[position]));
				isFirstLaunch = false;
				break;

			case 2: // only item

				convertView = mInflater.inflate(R.layout.settings_listviewtextadapter, null);

				holder.textLine = (TextView) convertView.findViewById(R.id.settingslvText);
				break;
			}

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
			

			if (type == 1) {
				isFirstLaunch = true;
				holder.lvSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						// TODO Auto-generated method stub
						if (isChecked)
						{
							if(!requireAdminPass.contains(lvTitle[position]))
								myPref.setSettings(lvTitle[position], true);
							else 
								{if(!isFirstLaunch)
								askForAdminPassDlg(lvTitle[position],true,holder);	
								}
						}
						else
						{
							if(!requireAdminPass.contains(lvTitle[position]))
								myPref.setSettings(lvTitle[position], false);
							else 
								{if(!isFirstLaunch)
								askForAdminPassDlg(lvTitle[position],false,holder);
								}
						}
					}
				});
				holder.lvSwitch.setChecked(myPref.getSettings(lvTitle[position]));
				isFirstLaunch = false;
			}
		}
		if(lvTitle[position] == R.string.admin_open_shift&&!myPref.getShiftIsOpen())
		{
			holder.textLine.setText(getString(R.string.admin_close_shift)+" <"+myPref.getShiftClerkName()+">");
		}
		else
			holder.textLine.setText(lvTitle[position]);

		
		return convertView;
	}

	public static class ViewHolder {
		TextView textLine;
		ToggleButton lvSwitch;

	}

	private void askForAdminPassDlg(final int settingTitle,final boolean isChecked,final ViewHolder holder)
	{
		final EditText input = new EditText(activity);
		dialogBuilder = new AlertDialog.Builder(activity);
		input.setTransformationMethod(PasswordTransformationMethod.getInstance());
		dialogBuilder.setView(input);
		
		if(!validPassword)
			dialogBuilder.setTitle(activity.getResources().getString(R.string.dlog_title_invalid_admin_password));
		else
			dialogBuilder.setTitle(activity.getResources().getString(R.string.dlog_title_enter_admin_password));
		
		dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface thisDialog, int which) {
						// TODO Auto-generated method stub
						String value = input.getText().toString();
						isFirstLaunch = true;
						if (value.equals(myPref.getPOSAdminPass())) // validate admin password
						{
							validPassword = true;
							myPref.setSettings(settingTitle, isChecked);
							holder.lvSwitch.setChecked(isChecked);
							notifyDataSetChanged();
							thisDialog.dismiss();
						}
						else
						{
							holder.lvSwitch.setChecked(!isChecked);
							validPassword = false;
							notifyDataSetChanged();
							thisDialog.dismiss();
							
							askForAdminPassDlg(settingTitle,!isChecked,holder);
						}
						
						
						
						
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface thisDialog, int which) {
						// TODO Auto-generated method stub
						isFirstLaunch = true;
						validPassword = true;
						holder.lvSwitch.setChecked(!isChecked);
						notifyDataSetChanged();
						
						thisDialog.dismiss();
						
					}
				});
		
		promptDialog = dialogBuilder.create();
		promptDialog.show();
	}
	
	
	@Override
	public int getItemViewType(int position) {
		if (settingsType.get(lvTitle[position]) == 0) {
			return 0;
		} else if (settingsType.get(lvTitle[position]) == 1) {
			return 1;
		}
		return 2;
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return lvTitle.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return lvTitle[position];
	}

	public int getItemKeyValue(int key) {
		return (settingsType.get(key));
	}

}
