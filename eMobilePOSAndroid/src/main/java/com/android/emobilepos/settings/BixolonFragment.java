package com.android.emobilepos.settings;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.database.MemoTextHandler;
import com.android.emobilepos.R;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.thefactoryhka.android.controls.PrinterException;

import java.util.Date;

import drivers.EMSBixolonRD;

public class BixolonFragment extends Fragment {

    private EMSBixolonRD bixolon;
    private TextView time;
    private TextView date;
    private TextView[] headersViews;
    private TextView[] footersViews;

    public BixolonFragment() {
        if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null
                && Global.mainPrinterManager.getCurrentDevice() instanceof EMSBixolonRD) {
            bixolon = (EMSBixolonRD) Global.mainPrinterManager.getCurrentDevice();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bixolon, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        try {
            if (bixolon != null) {
                loadPrinterInfo();
            }
        } catch (PrinterException e) {
            e.printStackTrace();
        }
    }

    private void loadPrinterInfo() throws PrinterException {
        headersViews = new TextView[3];
        footersViews = new TextView[3];
        Date printerDate = bixolon.getPrinterTFHKA().getS1PrinterData().getCurrentPrinterDate();
        double tax1 = bixolon.getPrinterTFHKA().getS3PrinterData().getTax1();
        double typeTax1 = bixolon.getPrinterTFHKA().getS3PrinterData().getTypeTax1();
        date = (TextView) getView().findViewById(R.id.bixolondatetextView25);
        headersViews[0] = (TextView) getView().findViewById(R.id.bixolonheader1textView25b);
        headersViews[1] = (TextView) getView().findViewById(R.id.bixolonheader2textView25);
        headersViews[2] = (TextView) getView().findViewById(R.id.bixolonheader3textView25);
        footersViews[0] = (TextView) getView().findViewById(R.id.bixolonfooter1textView25c);
        footersViews[1] = (TextView) getView().findViewById(R.id.bixolonfooter2textView25c);
        footersViews[2] = (TextView) getView().findViewById(R.id.bixolonfooter3textView25c);
        if (printerDate != null) {
            date.setText(DateUtils.getDateAsString(printerDate, DateUtils.DATE_MMM_dd_yyyy_h_mm_a));
        } else {
            date.setText("");
        }
        MemoTextHandler handler = new MemoTextHandler(getActivity());
        String[] headers = handler.getHeader();
        if (headers[0] != null && !headers[0].isEmpty()) {
            headersViews[0].setText(headers[0]);
        } else {
            headersViews[0].setVisibility(View.GONE);
        }
        if (headers[1] != null && !headers[1].isEmpty()) {
            headersViews[1].setText(headers[1]);
        } else {
            headersViews[1].setVisibility(View.GONE);
        }
        if (headers[2] != null && !headers[2].isEmpty()) {
            headersViews[2].setText(headers[2]);
        } else {
            headersViews[2].setVisibility(View.GONE);
        }

        String[] footers = handler.getFooter();
        if (footers[0] != null && !footers[0].isEmpty()) {
            footersViews[0].setText(footers[0]);
        } else {
            footersViews[0].setVisibility(View.GONE);
        }
        if (footers[1] != null && !footers[1].isEmpty()) {
            footersViews[1].setText(footers[1]);
        } else {
            footersViews[1].setVisibility(View.GONE);
        }
        if (footers[2] != null && !footers[2].isEmpty()) {
            footersViews[2].setText(footers[2]);
        } else {
            footersViews[2].setVisibility(View.GONE);
        }
    }
}
