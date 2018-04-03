package com.android.emobilepos.ordering;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.emobilepos.R;
import com.android.support.Global;
import com.android.support.NetworkUtils;


public class OrderLoyalty_FR extends Fragment implements OnClickListener {

    private TextView tapTxtLabel;
    private ImageButton btnTap;
    private SwiperLoyaltyCallback callBackLoyaltySwiper;
    private EditText fieldPointBalance, fieldPointsSubTotal, fieldPointsInUse,
            fieldPointsAvailable, fieldPointsAcumulable;
    private TextView grandTotalValue;
    private String balance = "0";


    interface SwiperLoyaltyCallback {
        void startLoyaltySwiper();
        void prefetchLoyaltyPoints();
    }


    public static OrderLoyalty_FR init(int val) {
        OrderLoyalty_FR frag = new OrderLoyalty_FR();
        Bundle args = new Bundle();
        args.putInt("val", val);
        frag.setArguments(args);
        return frag;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_loyalty_layout, container, false);
//        myFrag = this;

        callBackLoyaltySwiper = (SwiperLoyaltyCallback) getActivity();
        btnTap = view.findViewById(R.id.btnTapLoyalty);
        btnTap.setOnClickListener(this);
        tapTxtLabel = view.findViewById(R.id.tapLabel);

        fieldPointBalance = view.findViewById(R.id.fieldPointsBalance);
        fieldPointsSubTotal = view.findViewById(R.id.fieldPointsSubTotal);
        fieldPointsInUse = view.findViewById(R.id.fieldPointsInUse);
        fieldPointsAvailable = view.findViewById(R.id.fieldPointsAvailable);
        fieldPointsAcumulable = view.findViewById(R.id.fieldPointsAcumulable);
        grandTotalValue = view.findViewById(R.id.grandTotalValue);
        new PrefetchLoyaltyPointTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnTapLoyalty:
                callBackLoyaltySwiper.startLoyaltySwiper();
                break;
        }
    }


    public void hideTapButton() {
        btnTap.setVisibility(View.GONE);
        tapTxtLabel.setVisibility(View.GONE);
    }


    public void setPointBalance(String value) {
        balance = value;
        fieldPointBalance.setText(balance);
        fieldPointsAvailable.setText(balance);
    }


    public  void recalculatePoints(String pointsSubTotal, String pointsUsed, String pointsAcumulable, String grandTotal) {
        int temp = 0;
        if (fieldPointsSubTotal != null) {
            fieldPointsSubTotal.setText(pointsSubTotal);
            grandTotalValue.setText(grandTotal);
            fieldPointsInUse.setText(pointsUsed);
            fieldPointsAcumulable.setText(pointsAcumulable);
            temp = (int) (Double.parseDouble(balance) - Double.parseDouble(pointsUsed));
            fieldPointsAvailable.setText(Integer.toString(temp));
        }

        //Used for later loyalty sync at checkout
        Global.loyaltyCharge = pointsUsed;
        Global.loyaltyAddAmount = pointsAcumulable;
        Global.loyaltyPointsAvailable = Integer.toString(temp);
    }


    public boolean isValidPointClaim(String newUsedPoints) {
        double tempAvailable = Double.parseDouble(fieldPointsAvailable.getText().toString());
        double tempNewUsedPoints = Double.parseDouble(newUsedPoints);
        return tempAvailable >= tempNewUsedPoints;
    }

    private class PrefetchLoyaltyPointTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            return NetworkUtils.isConnectedToInternet(getActivity());
        }

        @Override
        protected void onPostExecute(Boolean isConnected) {
            if (isConnected) {
                callBackLoyaltySwiper.prefetchLoyaltyPoints();
            }
        }
    }

}
