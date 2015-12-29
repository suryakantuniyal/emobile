package com.android.emobilepos.ordering;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.emobilepos.R;
import com.android.support.Global;

import java.math.BigDecimal;

public class OrderRewards_FR extends Fragment implements OnClickListener {

    private static OrderRewards_FR myFrag;
    private SwiperRewardCallback callBackRewardSwiper;
    private ImageButton btnTap;
    private TextView tapTxtLabel;
    private static EditText fieldRewardBalance;
    private static TextView subTotalValue;
    private static String balance = "";
    private static String subtotal = "0";
    private Button btnPayRewards;


    public interface SwiperRewardCallback {
        void startRewardSwiper();
    }


    public static OrderRewards_FR init(int val) {
        OrderRewards_FR frag = new OrderRewards_FR();
        Bundle args = new Bundle();
        args.putInt("val", val);
        frag.setArguments(args);
        myFrag = frag;
        return frag;
    }


    public static OrderRewards_FR getFrag() {
        return myFrag;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_rewards_layout, container, false);

        callBackRewardSwiper = (SwiperRewardCallback) getActivity();
        myFrag = this;
        btnTap = (ImageButton) view.findViewById(R.id.btnTapReward);
        btnTap.setOnClickListener(this);

        btnPayRewards = (Button) view.findViewById(R.id.btnPayWithRewards);
        btnPayRewards.setOnClickListener(this);
        tapTxtLabel = (TextView) view.findViewById(R.id.tapLabel);

        fieldRewardBalance = (EditText) view.findViewById(R.id.fieldRewardBalance);

        subTotalValue = (TextView) view.findViewById(R.id.subtotalValue);

        if (savedInstanceState == null && OrderTotalDetails_FR.getFrag() != null) {
            Global global = (Global) getActivity().getApplication();
            OrderTotalDetails_FR.getFrag().reCalculate();
        }

        if (OrderingMain_FA.rewardsWasRead)
            hideTapButton();
        return view;
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btnTapReward:
                callBackRewardSwiper.startRewardSwiper();
                break;
            case R.id.btnPayWithRewards:
                BigDecimal bdSubtotal = new BigDecimal(subtotal);
                BigDecimal bdBalance = new BigDecimal(balance);

                if (bdBalance.compareTo(bdSubtotal) == 1)//bgBalance>bgSubtotal
                {
                    bdBalance = new BigDecimal(subtotal);
                    Global.rewardChargeAmount = bdBalance;
                } else {
                    Global.rewardChargeAmount = bdBalance;
                }
                if (OrderTotalDetails_FR.getFrag() != null) {
                    Global global = (Global) getActivity().getApplication();
                    OrderTotalDetails_FR.getFrag().reCalculate();
                }
                btnPayRewards.setClickable(false);
                break;
        }
    }


    public void hideTapButton() {
        btnTap.setVisibility(View.GONE);
        tapTxtLabel.setVisibility(View.GONE);
    }


    public static void setRewardBalance(String value) {

        balance = value;
        if (fieldRewardBalance != null)
            fieldRewardBalance.setText(balance);
        Global.rewardCardInfo.setOriginalTotalAmount(value);
    }


    public static void setRewardSubTotal(String value) {
        subtotal = value;
        if (subTotalValue != null) {
            Global.rewardAccumulableSubtotal = new BigDecimal(subtotal);
            subTotalValue.setText(value);
        }
    }
}