package com.android.emobilepos;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

import com.android.refunds.fragment.HistRefundFragFour;
import com.android.refunds.fragment.HistRefundFragOne;
import com.android.refunds.fragment.HistRefundFragThree;
import com.android.refunds.fragment.HistRefundFragTwo;

public class HistRefundTabsFragment extends Fragment implements OnTabChangeListener {
	private static final String[] TABS = new String[] { "cash", "check", "card", "other" };
	private static String[] TABS_TAG;// = new String[] { "Cash", "Check", "Card", "Other" };
	private static final int[] TABS_ID = new int[] { R.id.cash_tab, R.id.check_tab, R.id.card_tab, R.id.other_tab };

	private int curTab;
	private TabHost tabHost;
	private View myRoot;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		myRoot = inflater.inflate(R.layout.hist_payment_tabs, null);
		tabHost = (TabHost) myRoot.findViewById(android.R.id.tabhost);

		TextView headTitle = (TextView) myRoot.findViewById(R.id.pmtHeaderTitle);

		
		
		Resources resources = getActivity().getResources();
		headTitle.setText(resources.getString(R.string.hist_payments));
		TABS_TAG = new String[] {resources.getString(R.string.pay_tab_cash),resources.getString(R.string.pay_tab_check),
				resources.getString(R.string.pay_tab_card),resources.getString(R.string.pay_tab_other)};
		
		
		
		
		headTitle.setText(resources.getString(R.string.hist_refunds));

		initTabs();
		return myRoot;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);

		tabHost.setOnTabChangedListener(this);
		tabHost.setCurrentTab(curTab);

		updateMyTabs(TABS[0], TABS_ID[0]);
	}

	private void initTabs() {
		tabHost.setup();
		int length = TABS.length;
		for (int i = 0; i < length; i++) {
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
			if (tabID.equals(TABS[0])) {
				fragManager.beginTransaction().replace(placeHolder, new HistRefundFragOne(), tabID).commit();
			} else if (tabID.equals(TABS[1])) {
				fragManager.beginTransaction().replace(placeHolder, new HistRefundFragTwo(), tabID).commit();
			} else if (tabID.equals(TABS[2])) {
				fragManager.beginTransaction().replace(placeHolder, new HistRefundFragThree(), tabID).commit();
			} else {
				fragManager.beginTransaction().replace(placeHolder, new HistRefundFragFour(), tabID).commit();
			}
		}
	}

	@Override
	public void onTabChanged(String tabID) {
		// TODO Auto-generated method stub
		if (tabID.equals(TABS[0])) {
			updateMyTabs(tabID, TABS_ID[0]);
			curTab = 0;
			return;
		} else if (tabID.equals(TABS[1])) {
			updateMyTabs(tabID, TABS_ID[1]);
			curTab = 1;
			return;
		} else if (tabID.equals(TABS[2])) {
			updateMyTabs(tabID, TABS_ID[2]);
			curTab = 2;
			return;
		} else {
			updateMyTabs(tabID, TABS_ID[3]);
			curTab = 3;
			return;
		}
	}
}
