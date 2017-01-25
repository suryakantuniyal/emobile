package com.android.emobilepos.mainmenu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.android.emobilepos.R;
import com.android.emobilepos.adapters.HistoryMenuAdapter;
import com.android.emobilepos.history.HistoryConsignment_FA;
import com.android.emobilepos.history.HistoryGiftRewardLoyalty_FA;
import com.android.emobilepos.history.HistoryInventoryTransfer_FA;
import com.android.emobilepos.history.HistoryOpenInvoices_FA;
import com.android.emobilepos.history.HistoryPayments_FA;
import com.android.emobilepos.history.HistoryTransactions_FA;

public class HistoryTab_FR extends Fragment {
	GridView myListview;
	private HistoryMenuAdapter myAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.history_layout, container, false);

		myListview = (GridView) view.findViewById(R.id.historyListView);
		myAdapter = new HistoryMenuAdapter(getActivity());
		return view;

	}

	/* if update information is needed on layout */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Activity activity = getActivity();

		if (activity != null) {

			myAdapter = new HistoryMenuAdapter(activity);

			if (myListview != null) {
				myListview.setAdapter(myAdapter);
				myListview.setOnItemClickListener(new AdapterView.OnItemClickListener() 
				{

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,final int position, long arg3) {
						// TODO Auto-generated method stub

						Intent intent = null;
						switch(position)
						{
						case 0://Transactions
							intent = new Intent(arg0.getContext(), HistoryTransactions_FA.class);
							break;
						case 1:
							intent = new Intent(arg0.getContext(), HistoryPayments_FA.class);
							intent.putExtra("isRefunds", false);
							break;
						case 2:
							intent = new Intent(arg0.getContext(), HistoryPayments_FA.class);
							intent.putExtra("isRefunds", true);
							break;
						case 3:
							intent = new Intent(arg0.getContext(), HistoryGiftRewardLoyalty_FA.class);
							intent.putExtra("cardTypeCase", HistoryGiftRewardLoyalty_FA.CASE_GIFTCARD);
							break;
						case 4:
							intent = new Intent(arg0.getContext(), HistoryGiftRewardLoyalty_FA.class);
							intent.putExtra("cardTypeCase", HistoryGiftRewardLoyalty_FA.CASE_LOYALTY);
							break;
						case 5:
							intent = new Intent(arg0.getContext(), HistoryGiftRewardLoyalty_FA.class);
							intent.putExtra("cardTypeCase", HistoryGiftRewardLoyalty_FA.CASE_REWARD);
							break;
						case 6:
							intent = new Intent(arg0.getContext(), HistoryOpenInvoices_FA.class);
							break;
						case 7:
							intent = new Intent(arg0.getContext(), HistoryConsignment_FA.class);
							break;
						case 8:
							intent = new Intent(arg0.getContext(),HistoryInventoryTransfer_FA.class);
							break;
						}
						
						if(intent!=null)
							startActivity(intent);
					}
				});
			}

		}

	}
}
