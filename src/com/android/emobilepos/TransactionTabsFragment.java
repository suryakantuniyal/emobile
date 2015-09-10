package com.android.emobilepos;

import com.android.transaction.fragments.TransFragFive;
import com.android.transaction.fragments.TransFragFour;
import com.android.transaction.fragments.TransFragOne;
import com.android.transaction.fragments.TransFragThree;
import com.android.transaction.fragments.TransFragTwo;
import com.android.emobilepos.R;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class TransactionTabsFragment extends Fragment implements OnTabChangeListener {
	
	private static final String[] TABS = new String[] { "orders", "returns", "invoices", "estimates", "receipts"};
	private static  String[] TABS_TAG ;//= new String[] { "Orders", "Returns", "Invoices", "Estimates", "Receipts" };
	private static final int[] TABS_ID = new int[] { R.id.orders_tab, R.id.returns_tab, R.id.invoices_tab, 
		R.id.estimates_tab, R.id.receipts_tab};

	
	private int curTab;
	private TabHost tabHost;
	private View myRoot;

	
	
	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);
	}

	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		myRoot = inflater.inflate(R.layout.transaction_tabs_layout, null);
		tabHost = (TabHost) myRoot.findViewById(android.R.id.tabhost);
		TextView headTitle = (TextView) myRoot.findViewById(R.id.transHeaderTitle);
		Resources resources = getActivity().getResources();
		headTitle.setText(resources.getString(R.string.hist_transac));
		TABS_TAG = new String[]{resources.getString(R.string.trans_tab_orders),resources.getString(R.string.trans_tab_returns),
				resources.getString(R.string.trans_tab_invoices),resources.getString(R.string.trans_tab_estimates),
				resources.getString(R.string.trans_tab_receipts)};
		initTabs();
		return myRoot;
	}

	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);

		tabHost.setOnTabChangedListener(this);
		tabHost.setCurrentTab(curTab);

		updateMyTabs(TABS[0], TABS_ID[0]);
	}

	private void initTabs() {
		tabHost.setup();
		int length = TABS.length;
		for (int i = 0; i < length; i++) 
		{
			tabHost.addTab(newTab(TABS[i], TABS_TAG[i], TABS_ID[i]));
		}
	}

	private TabSpec newTab(String tag, String label, int tabView) {
		// TODO Auto-generated method stub

		View indicator = LayoutInflater.from(getActivity()).inflate(R.layout.tabs_layout, (ViewGroup) myRoot.findViewById(android.R.id.tabs), false);

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
			if (tabID.equals(TABS[0])) 			//Orders
			{
				fragManager.beginTransaction().replace(placeHolder, new TransFragOne(), tabID).commit();
			} else if (tabID.equals(TABS[1])) //returns
			{
				fragManager.beginTransaction().replace(placeHolder, new TransFragTwo(), tabID).commit();
			} else if (tabID.equals(TABS[2])) //invoices
			{
				fragManager.beginTransaction().replace(placeHolder, new TransFragThree(), tabID).commit();
			} else if (tabID.equals(TABS[3])) //estimates
			{
				fragManager.beginTransaction().replace(placeHolder, new TransFragFour(), tabID).commit();
			} 
			else	//receipts 
			{
				fragManager.beginTransaction().replace(placeHolder, new TransFragFive(), tabID).commit();
			}
		}
	}

	@Override
	public void onTabChanged(String tabID) {
		// TODO Auto-generated method stub
		if (tabID.equals(TABS[0]))			//Orders 
		{
			updateMyTabs(tabID, TABS_ID[0]);
			curTab = 0;
			return;
		} else if (tabID.equals(TABS[1]))	//Returns 
		{
			updateMyTabs(tabID, TABS_ID[1]);
			curTab = 1;
			return;
		} else if (tabID.equals(TABS[2]))	//Invoices 
		{
			updateMyTabs(tabID, TABS_ID[2]);
			curTab = 2;
			return;
		} else if (tabID.equals(TABS[3]))	//Estimates 
		{
			updateMyTabs(tabID, TABS_ID[3]);
			curTab = 3;
			return;
		} else	//Sales Receipts
		{
			updateMyTabs(tabID, TABS_ID[4]);
			curTab = 4;
			return;
		}
	}
}
