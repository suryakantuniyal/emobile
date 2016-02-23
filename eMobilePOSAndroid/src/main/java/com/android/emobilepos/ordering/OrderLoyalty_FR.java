package com.android.emobilepos.ordering;

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


public class OrderLoyalty_FR extends Fragment implements OnClickListener {

    private TextView tapTxtLabel;
    private ImageButton btnTap;
    private static OrderLoyalty_FR myFrag;
    private SwiperLoyaltyCallback callBackLoyaltySwiper;
    private static EditText fieldPointBalance, fieldPointsSubTotal, fieldPointsInUse,
            fieldPointsAvailable, fieldPointsAcumulable;
    private static TextView grandTotalValue;
    private static String balance = "0", subTotal = "0", inUse = "0", available = "0", acumulable = "0";


    public interface SwiperLoyaltyCallback {
        void startLoyaltySwiper();
    }


    public static OrderLoyalty_FR init(int val) {
        OrderLoyalty_FR frag = new OrderLoyalty_FR();
        Bundle args = new Bundle();
        args.putInt("val", val);
        frag.setArguments(args);
        myFrag = frag;
        return frag;
    }


    public static OrderLoyalty_FR getFrag() {
        return myFrag;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_loyalty_layout, container, false);
        myFrag = this;

        callBackLoyaltySwiper = (SwiperLoyaltyCallback) getActivity();
        btnTap = (ImageButton) view.findViewById(R.id.btnTapLoyalty);
        btnTap.setOnClickListener(this);
        tapTxtLabel = (TextView) view.findViewById(R.id.tapLabel);

        fieldPointBalance = (EditText) view.findViewById(R.id.fieldPointsBalance);
        fieldPointsSubTotal = (EditText) view.findViewById(R.id.fieldPointsSubTotal);
        fieldPointsInUse = (EditText) view.findViewById(R.id.fieldPointsInUse);
        fieldPointsAvailable = (EditText) view.findViewById(R.id.fieldPointsAvailable);
        fieldPointsAcumulable = (EditText) view.findViewById(R.id.fieldPointsAcumulable);
        grandTotalValue = (TextView) view.findViewById(R.id.grandTotalValue);


        if (savedInstanceState == null && OrderTotalDetails_FR.getFrag() != null) {
            Global global = (Global) OrderLoyalty_FR.this.getActivity().getApplication();
            OrderTotalDetails_FR.getFrag().reCalculate(global.orderProducts);
        }
        return view;
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
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


    public static void setPointBalance(String value) {
        balance = value;
        fieldPointBalance.setText(balance);
        fieldPointsAvailable.setText(balance);
    }


    public static void recalculatePoints(String pointsSubTotal, String pointsUsed, String pointsAcumulable, String grandTotal) {
        if (fieldPointsSubTotal != null) {
            fieldPointsSubTotal.setText(pointsSubTotal);
            grandTotalValue.setText(grandTotal);
            fieldPointsInUse.setText(pointsUsed);
            fieldPointsAcumulable.setText(pointsAcumulable);
            int temp = (int) (Double.parseDouble(balance) - Double.parseDouble(pointsUsed));
            fieldPointsAvailable.setText(Integer.toString(temp));
        }

        //Used for later loyalty sync at checkout
        Global.loyaltyCharge = pointsUsed;
        Global.loyaltyAddAmount = pointsAcumulable;
    }


    public static boolean isValidPointClaim(String newUsedPoints) {
        double tempAvailable = Double.parseDouble(fieldPointsAvailable.getText().toString());
        double tempNewUsedPoints = Double.parseDouble(newUsedPoints);
        return tempAvailable >= tempNewUsedPoints;
    }


}
