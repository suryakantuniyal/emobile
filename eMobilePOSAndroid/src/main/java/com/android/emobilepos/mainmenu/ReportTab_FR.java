package com.android.emobilepos.mainmenu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.emobilepos.R;
import com.android.emobilepos.report.ViewEndOfDayReport_FA;
import com.android.emobilepos.report.ViewReport_FA;
import com.android.emobilepos.security.SecurityManager;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.thefactoryhka.android.controls.PrinterException;

import drivers.EMSBixolonRD;

public class ReportTab_FR extends Fragment implements OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.reports_main_layout, container, false);
        boolean hasPermissions = SecurityManager.hasPermissions(getActivity(), SecurityManager.SecurityAction.PRINT_REPORTS);
        MyPreferences preferences = new MyPreferences(getActivity());
        Button btnDaySummary = (Button) view.findViewById(R.id.btnReportDaySummary);
        Button btnPerShift = (Button) view.findViewById(R.id.btnReportPerShift);
        Button btnEndOfDay = (Button) view.findViewById(R.id.btnEndOfDay);
        Button btnReportZ = (Button) view.findViewById(R.id.bixolonPrintReportZ);
        Button btnReportX = (Button) view.findViewById(R.id.bixolonPrintReportX);
        if (preferences.isBixolonRD()) {
            btnReportX.setVisibility(View.VISIBLE);
            btnReportZ.setVisibility(View.VISIBLE);
            btnReportX.setOnClickListener(this);
            btnReportZ.setOnClickListener(this);
        } else {
            btnReportX.setVisibility(View.GONE);
            btnReportZ.setVisibility(View.GONE);
        }
        btnDaySummary.setOnClickListener(this);
        btnPerShift.setOnClickListener(this);
        btnEndOfDay.setOnClickListener(this);
        btnDaySummary.setEnabled(hasPermissions);
        btnEndOfDay.setEnabled(hasPermissions);
        btnPerShift.setEnabled(hasPermissions);
        if (!hasPermissions) {
            Toast.makeText(getActivity(), R.string.permission_denied, Toast.LENGTH_LONG).show();
        }
        return view;

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), ViewReport_FA.class);
        switch (v.getId()) {
            case R.id.btnReportDaySummary:
                intent.putExtra("isShiftReport", false);
                startActivity(intent);
                break;
            case R.id.btnReportPerShift:
                intent.putExtra("isShiftReport", true);
                startActivity(intent);
                break;
            case R.id.btnEndOfDay:
                intent = new Intent(getActivity(), ViewEndOfDayReport_FA.class);
                startActivity(intent);
                break;
            case R.id.bixolonPrintReportZ:
            case R.id.bixolonPrintReportX:
                if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
                    EMSBixolonRD bixolonRD = (EMSBixolonRD) Global.mainPrinterManager.getCurrentDevice();
                    try {
                        switch (v.getId()) {
                            case R.id.bixolonPrintReportZ:
                                bixolonRD.printZReport();
                                break;
                            case R.id.bixolonPrintReportX:
                                bixolonRD.printXReport();
                                break;
                        }
                    } catch (PrinterException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}
