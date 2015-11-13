package com.android.emobilepos.locations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.android.database.Locations_DB;
import com.android.emobilepos.holders.Locations_Holder;
import com.emobilepos.app.R;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

public class LocationsPickerDlog_FR extends DialogFragment implements Comparator<Locations_Holder> {
	/**
	 * View components
	 */
	private EditText searchEditText;
	private ListView countryListView;

	/**
	 * Adapter for the listview
	 */
	private LocationsLV_Adapter adapter;

	/**
	 * Hold all countries, sorted by country name
	 */
	private List<Locations_Holder> allLocationsList;

	/**
	 * Hold countries that matched user query
	 */
	private List<Locations_Holder> selectedLocationsList;

	/**
	 * Listener to which country user selected
	 */
	private LocationsPicker_Listener listener;

	/**
	 * Set listener
	 * 
	 * @param listener
	 */
	public void setListener(LocationsPicker_Listener listener) {
		this.listener = listener;
	}

	public EditText getSearchEditText() {
		return searchEditText;
	}


	/**
	 * Get all countries with code and name from res/raw/countries.json
	 * 
	 * @return
	 */
	private List<Locations_Holder> getAllCountries() {
		if (allLocationsList == null) {
			try {
				allLocationsList = new ArrayList<Locations_Holder>();

				Locations_DB dbLocations = new Locations_DB(getActivity());
				allLocationsList = dbLocations.getLocationsList();
				
//				Locations_Holder temp = new Locations_Holder();
//				temp.set(Locations_DB.loc_key, "1");
//				temp.set(Locations_DB.loc_id, "Hello");
//				temp.set(Locations_DB.loc_name, "Walmart - San Juan");
//				allLocationsList.add(temp);
//				
//				temp = new Locations_Holder();
//				temp.set(Locations_DB.loc_key, "2");
//				temp.set(Locations_DB.loc_id, "Bond");
//				temp.set(Locations_DB.loc_name, "Walmart - Rio Piedras");
//				allLocationsList.add(temp);
//				
//				temp = new Locations_Holder();
//				temp.set(Locations_DB.loc_key, "3");
//				temp.set(Locations_DB.loc_id, "Bond2");
//				temp.set(Locations_DB.loc_name, "Walmart - Carolina");
//				allLocationsList.add(temp);
				
//				// Read from local file
//				String allCountriesString = readFileAsString(getActivity());
//				JSONObject jsonObject = new JSONObject(allCountriesString);
//				Iterator<?> keys = jsonObject.keys();
//
//				// Add the data to all countries list
//				while (keys.hasNext()) {
//					String key = (String) keys.next();
//					Country country = new Country();
//					country.setCode(key);
//					country.setName(jsonObject.getString(key));
//					allCountriesList.add(country);
//				}

				// Sort the all countries list based on country name
				//Collections.sort(allLocationsList, this);

				// Initialize selected countries with all countries
				selectedLocationsList = new ArrayList<Locations_Holder>();
				selectedLocationsList.addAll(allLocationsList);

				// Return
				return allLocationsList;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}



	/**
	 * To support show as dialog
	 * 
	 * @param dialogTitle
	 * @return
	 */
	public static LocationsPickerDlog_FR newInstance(String dialogTitle) {
		LocationsPickerDlog_FR picker = new LocationsPickerDlog_FR();
		Bundle bundle = new Bundle();
		bundle.putString("dialogTitle", dialogTitle);
		picker.setArguments(bundle);
		return picker;
	}

	/**
	 * Create view
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate view
		View view = inflater.inflate(R.layout.country_picker, null);
		//getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		// Get countries from the json
		getAllCountries();

		getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		getDialog().getWindow().setBackgroundDrawableResource(R.drawable.bg_dlog);
		getDialog().setCanceledOnTouchOutside(false);
		// Set dialog title if show as dialog
		Bundle args = getArguments();
		if (args != null) {
			String dialogTitle = getString(R.string.dlog_title_select_origin_location);
			if(!getArguments().getBoolean("showOrigin"))
				dialogTitle = getString(R.string.dlog_title_select_destination_location);
			getDialog().setTitle(dialogTitle);
			//headerTitle.setText(dialogTitle);

			int width = getResources().getDimensionPixelSize(R.dimen.cp_dialog_width);
			int height = getResources().getDimensionPixelSize(R.dimen.cp_dialog_height);
			getDialog().getWindow().setLayout(width, height);
		}
		

		// Get view components
		searchEditText = (EditText) view.findViewById(R.id.country_picker_search);
		countryListView = (ListView) view.findViewById(R.id.country_picker_listview);

		// Set adapter
		adapter = new LocationsLV_Adapter(getActivity(), selectedLocationsList);
		countryListView.setAdapter(adapter);

		// Inform listener
		countryListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (listener != null) {
					Locations_Holder location = selectedLocationsList.get(position);
					listener.onSelectLocation(location);
				}
			}
		});

		// Search for which countries matched user query
		searchEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				search(s.toString());
			}
		});

		return view;
	}

	/**
	 * Search allCountriesList contains text and put result into
	 * selectedCountriesList
	 * 
	 * @param text
	 */
	@SuppressLint("DefaultLocale")
	private void search(String text) {
		selectedLocationsList.clear();

		for (Locations_Holder location : allLocationsList) {
			if (location.get(Locations_DB.loc_name).toLowerCase(Locale.ENGLISH).contains(text.toLowerCase())) {
				selectedLocationsList.add(location);
			}
		}

		adapter.notifyDataSetChanged();
	}


	@Override
	public int compare(Locations_Holder lhs, Locations_Holder rhs) {
		// TODO Auto-generated method stub
		return lhs.get(Locations_DB.loc_name).compareTo(Locations_DB.loc_name);
	}

}
