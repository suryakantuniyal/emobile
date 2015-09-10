package com.android.emobilepos;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

import com.android.catalog.fragments.CatalogFragOne;
import com.android.catalog.fragments.CatalogLandscapeOne;
import com.android.database.CategoriesHandler;
import com.android.emobilepos.SalesReceiptFragment.ListViewAdapter.ViewHolder;
import com.android.support.Global;

public class CatalogTabsFragment extends Fragment implements OnTabChangeListener {
	private static final String[] TABS = new String[] { "name", "desc", "type", "upc" };
	private static final String[] TABS_TAG = new String[] { "Name", "Desc", "Type", "UPC" };
	private static final int[] TABS_ID = new int[] { R.id.catalog_name_tab, R.id.catalog_desc_tab, R.id.catalog_type_tab, R.id.catalog_upc_tab };

	private int curTab;
	private TabHost tabHost;
	private View myRoot;
	private int searchType;
	private Button category;
	private Dialog dialog;
	private CategoriesHandler handler;
	private List<String[]> categories = new ArrayList<String[]>();
	private List<String> catName = new ArrayList<String>();
	private boolean flag = false;
	private ListViewAdapter myAdapter;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		myRoot = inflater.inflate(R.layout.catalog_tabs_fragment, null);

		tabHost = (TabHost) myRoot.findViewById(android.R.id.tabhost);
		category = (Button) myRoot.findViewById(R.id.categoryButton);
		handler = new CategoriesHandler(getActivity());

		final Global global = (Global) getActivity().getApplication();
		searchType = global.searchType;

		if (!flag) {
			categories = handler.getCategories();
			int size = categories.size();
			catName.add("All");
			for (int i = 0; i < size; i++) {
				catName.add(categories.get(i)[0]);
			}
		}
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final ListView list = new ListView(getActivity());

		/*
		 * ArrayAdapter<String>adapter = new
		 * ArrayAdapter<String>(getActivity(),android
		 * .R.layout.simple_list_item_2,android.R.id.text1,catName);
		 * list.setAdapter(adapter);
		 */
		myAdapter = new ListViewAdapter(getActivity());
		list.setAdapter(myAdapter);
		builder.setView(list);
		dialog = builder.create();
		category.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.show();
				list.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
						if (position == 0) {
							global.cat_id = "0";
						} else {

							global.cat_id = categories.get(position - 1)[1];
						}
						// int i = position;
						dialog.dismiss();
						updateFragment();
					}
				});

			}
		});

		initTabs();
		updateFragment();

		return myRoot;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);

		tabHost.setOnTabChangedListener(this);
		tabHost.setCurrentTab(curTab);

		flag = true;
		updateMyTabs(TABS[0], TABS_ID[0]);
	}

	private void initTabs() {
		tabHost.setup();
		int length = TABS.length;
		for (int i = 0; i < length; i++) {
			tabHost.addTab(newTab(TABS[i], TABS_TAG[i], TABS_ID[i]));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		System.gc();
		Global global = (Global) getActivity().getApplication();
		searchType = global.searchType;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		System.gc();
	}

	private void updateFragment() {
		getActivity().setResult(2);
		int orientation = getResources().getConfiguration().orientation;
		FragmentManager fragManager = getFragmentManager();
		FragmentTransaction trans = fragManager.beginTransaction();
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// final FragmentTransaction ft =
			// this.getFragmentManager().beginTransaction();
			trans = fragManager.beginTransaction();
			trans.replace(R.id.fragment_container, new CatalogLandscapeOne(), "Landscape");
			// trans.addToBackStack(null);
			trans.commit();
			System.gc();
		} else {

			trans = fragManager.beginTransaction();

			trans.replace(R.id.fragment_container, new CatalogFragOne(), "Portrait");
			// trans.addToBackStack(null);

			trans.commit();
			System.gc();
			// fragManager.beginTransaction().replace(R.id.fragment_container,
			// new CatalogFragOne(),"Portrait").commit();
		}
	}

	private TabSpec newTab(String tag, String label, int tabView) {
		// TODO Auto-generated method stub

		View indicator = LayoutInflater.from(getActivity()).inflate(R.layout.tab_layout_catalog, (ViewGroup) myRoot.findViewById(android.R.id.tabs),
				false);

		TextView tabLabel = (TextView) indicator.findViewById(R.id.tabTitle);

		tabLabel.setText(label);

		TabSpec tabSpec = tabHost.newTabSpec(tag);
		tabSpec.setIndicator(indicator);

		tabSpec.setContent(tabView);

		return tabSpec;
	}

	private void updateMyTabs(String tabID, int placeHolder) {
		FragmentManager fragManager = getFragmentManager();

		if (fragManager.findFragmentByTag(tabID) == null) {
			int orientation = getResources().getConfiguration().orientation;
			Global global = (Global) getActivity().getApplication();

			if (tabID.equals(TABS[0])) {
				// global.searchType = 0;

				if (orientation == Configuration.ORIENTATION_PORTRAIT) {
					Fragment f1 = fragManager.findFragmentByTag("Portrait");
					if (fragManager.findFragmentByTag("Portrait") != null && searchType != global.searchType) {
						searchType = 0;
						EditText field = (EditText) f1.getView().findViewById(R.id.catalogSearchField);
						field.getEditableText().clear();
						// field.setText("");
					}
				} else {
					if (fragManager.findFragmentByTag("Landscape") != null) {
						searchType = 0;
					}
				}

				/*
				 * fragManager.beginTransaction() .replace(placeHolder, new
				 * CatalogFragOne(),tabID).commit();
				 */

			} else if (tabID.equals(TABS[1])) {
				// global.searchType = 1;

				if (orientation == Configuration.ORIENTATION_PORTRAIT) {
					Fragment f1 = fragManager.findFragmentByTag("Portrait");
					if (fragManager.findFragmentByTag("Portrait") != null && searchType != global.searchType) {
						searchType = 1;
						EditText field = (EditText) f1.getView().findViewById(R.id.catalogSearchField);
						field.getEditableText().clear();
						// field.setText("");
					}
				} else {
					if (fragManager.findFragmentByTag("Landscape") != null) {
						searchType = 1;
					}
				}
				/*
				 * fragManager.beginTransaction() .replace(placeHolder, new
				 * CatalogFragOne(),tabID).commit();
				 */
			} else if (tabID.equals(TABS[2])) {
				// global.searchType = 2;

				if (orientation == Configuration.ORIENTATION_PORTRAIT) {
					Fragment f1 = fragManager.findFragmentByTag("Portrait");
					if (fragManager.findFragmentByTag("Portrait") != null && searchType != global.searchType) {
						searchType = 2;
						EditText field = (EditText) f1.getView().findViewById(R.id.catalogSearchField);
						field.getEditableText().clear();
						// field.setText("");
					}
				} else {
					if (fragManager.findFragmentByTag("Landscape") != null) {
						searchType = 2;
					}
				}
				/*
				 * fragManager.beginTransaction() .replace(placeHolder, new
				 * CatalogFragOne(),tabID).commit();
				 */
			} else {
				// global.searchType = 3;

				if (orientation == Configuration.ORIENTATION_PORTRAIT) {
					Fragment f1 = fragManager.findFragmentByTag("Portrait");
					if (fragManager.findFragmentByTag("Portrait") != null && searchType != global.searchType) {
						searchType = 3;
						EditText field = (EditText) f1.getView().findViewById(R.id.catalogSearchField);
						field.getEditableText().clear();
						// field.setText("");
					}
				} else {
					if (fragManager.findFragmentByTag("Landscape") != null) {
						searchType = 3;
					}
				}
				/*
				 * fragManager.beginTransaction() .replace(placeHolder, new
				 * CatalogFragOne(),tabID).commit();
				 */
			}
		}
	}

	@Override
	public void onTabChanged(String tabID) {
		Global global = (Global) getActivity().getApplication();
		// TODO Auto-generated method stub
		if (tabID.equals(TABS[0])) {
			global.searchType = 0;
			updateMyTabs(tabID, TABS_ID[0]);
			curTab = 0;
			return;
		} else if (tabID.equals(TABS[1])) {
			global.searchType = 1;
			updateMyTabs(tabID, TABS_ID[1]);
			curTab = 1;
			return;
		} else if (tabID.equals(TABS[2])) {
			global.searchType = 2;
			updateMyTabs(tabID, TABS_ID[2]);
			curTab = 2;
			return;
		} else if (tabID.equals(TABS[3])) {
			global.searchType = 3;
			updateMyTabs(tabID, TABS_ID[3]);
			curTab = 3;
			return;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == 2)// add item to listview
		{
			getActivity().setResult(2);
		}
		if (resultCode == -2)
			getActivity().setResult(-2);

	}

	public class ListViewAdapter extends BaseAdapter {
		// private Global global = (Global)getActivity().getApplication();

		private LayoutInflater myInflater;
		private Context context;

		public ListViewAdapter(Context context) {
			this.context = context;
			myInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return catName.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			ViewHolder holder;
			// int type = getItemViewType(position);
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = myInflater.inflate(R.layout.categories_layout_adapter, null);
				holder.categoryName = (TextView) convertView.findViewById(R.id.categoryName);
				holder.icon = (ImageView) convertView.findViewById(R.id.subcategoryIcon);

				holder.categoryName.setText(catName.get(position));

				holder.icon.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Toast.makeText(context, catName.get(position) + " button pressed", Toast.LENGTH_SHORT).show();
					}
				});
				// setHolderValues(holder,position);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();

				holder.categoryName.setText(catName.get(position));

				holder.icon.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Toast.makeText(context, catName.get(position) + " button pressed", Toast.LENGTH_SHORT).show();
					}
				});
				// setHolderValues(holder,position);
			}

			return convertView;
		}

		/*
		 * @Override public int getItemViewType(int position) { if(position ==
		 * 0) { return 0; } return 1;
		 * 
		 * }
		 * 
		 * @Override public int getViewTypeCount() { return 2; }
		 */
		public void setHolderValues(ViewHolder holder, int position) {

		}

		public class ViewHolder {
			TextView categoryName;
			ImageView icon;
		}
	}
}