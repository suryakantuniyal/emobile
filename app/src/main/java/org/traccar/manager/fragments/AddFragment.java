package org.traccar.manager.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.traccar.manager.R;

/**
 * Created by silence12 on 4/7/17.
 */

public class AddFragment extends Fragment implements View.OnClickListener{

public View rootView;
public static LinearLayout linlaHeaderProgress;
private OnFragmentInteractionListener mListener;
private TextView no_loads_available;
static int page_pos = 1;
private CardView addTruckCardView;
private Button addtruckBuution;

public AddFragment() {
        }

public static AddFragment newInstance(int position) {
        AddFragment fragment = new AddFragment();
        Bundle args = new Bundle();
        args.putInt("pos",position);
        fragment.setArguments(args);
        return fragment;
        }

@Override
public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        }

@Nullable
@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
        rootView = inflater.inflate(R.layout.fragment_add, container, false);
        addTruckCardView = (CardView) rootView.findViewById(R.id.addload_cardView);
        addTruckCardView.setOnClickListener(this);
        Bundle b =  getArguments();
        page_pos = b.getInt("pos");

        } else {

final ViewParent parent = rootView.getParent();
        if (parent != null && parent instanceof ViewGroup) {
        ((ViewGroup) parent).removeView(rootView);
        }
        }
        return rootView;
        }


public void onButtonPressed(Uri uri) {
        if (mListener != null) {
        mListener.onFragmentInteraction(uri);
        }
        }

@Override
public void onClick(View v) {

//        switch (v.getId()){
//        case R.id.addload_cardView:
//        Intent addfleetsintent = new Intent(getContext(), AllServicesActivity.class);
//        startActivity(addfleetsintent);
//        break;
//
//        }
        }

public interface OnFragmentInteractionListener {
    public void onFragmentInteraction(Uri uri);
}
}

