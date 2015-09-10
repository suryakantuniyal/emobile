package com.android.emobilepos;


import com.android.menuadapters.SynchMenuAdapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class SynchMenuActivity extends Fragment {
	private SynchMenuAdapter myAdapter;
	public ListView myListview;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.synchronization_layout, container, false);

		myListview = (ListView) view.findViewById(R.id.synchListView);
		myAdapter = new SynchMenuAdapter(getActivity());
		myListview.setAdapter(myAdapter);
		return view;

	}
}
