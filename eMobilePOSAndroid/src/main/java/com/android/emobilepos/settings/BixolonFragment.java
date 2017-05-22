package com.android.emobilepos.settings;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.database.MemoTextHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.Tax;
import com.android.emobilepos.models.realms.Bixolon;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.thefactoryhka.android.controls.PrinterException;

import java.util.Date;
import java.util.List;

import drivers.EMSBixolonRD;

public class BixolonFragment extends Fragment implements View.OnClickListener {

    private EMSBixolonRD bixolon;
    private TextView time;
    private TextView date;
    private TextView[] headersViews;
    private TextView[] footersViews;
    private TextView[] taxesViews;
    private TextView[] typeTaxesViews;
    private List<Tax> taxes;
    private String[] headers;
    private String[] footers;

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
        if (bixolon != null) {
            new InitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            getView().findViewById(R.id.sendDateTimebutton2).setOnClickListener(this);
            getView().findViewById(R.id.sendHeaderbutton2b).setOnClickListener(this);
            getView().findViewById(R.id.sendFooterbutton2c).setOnClickListener(this);
            getView().findViewById(R.id.sendTaxesbutton28).setOnClickListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveInfo();
    }

    private void saveInfo() {
        Bixolon bixolon;
    }

    private enum Bixoloncommand {
        SEND_DATE, SEND_HEADER, SEND_FOOTER, SEND_TAXES
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendDateTimebutton2:
                new SendBixolonCommandTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Bixoloncommand.SEND_DATE);
                break;
            case R.id.sendHeaderbutton2b:
                new SendBixolonCommandTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Bixoloncommand.SEND_HEADER);
                break;
            case R.id.sendFooterbutton2c:
                new SendBixolonCommandTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Bixoloncommand.SEND_FOOTER);
                break;
            case R.id.sendTaxesbutton28:
                new SendBixolonCommandTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Bixoloncommand.SEND_TAXES);
                break;
        }
    }

    private class SendBixolonCommandTask extends AsyncTask<Bixoloncommand, Void, Boolean> {
        ProgressDialog dialog = new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute() {
            dialog.setIndeterminate(true);
            dialog.setMessage(getString(R.string.processing));
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Bixoloncommand... params) {
            Bixoloncommand command = params[0];
            switch (command) {
                case SEND_DATE:
                    return bixolon.sendDateTimeCommand(new Date());
                case SEND_HEADER:
                    return bixolon.sendHeaders(headers);
                case SEND_FOOTER:
                    return bixolon.sendFooters(footers);
                case SEND_TAXES:
                    TaxesHandler taxesHandler = new TaxesHandler(getActivity());
                    return bixolon.sendTaxes(taxesHandler.getTaxes());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            dialog.dismiss();
            if (result) {
                Global.showPrompt(getActivity(), R.string.dlog_title_success, getString(R.string.bixolon_command_succeed));
            } else {
                Global.showPrompt(getActivity(), R.string.dlog_title_error, getString(R.string.bixolon_command_fail));
            }
        }
    }

    private class InitTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        private Date printerDate;
//        private double taxes[] = new double[5];
//        private int typeTaxes[] = new int[5];


        @Override
        protected void onPreExecute() {
            dialog.setIndeterminate(true);
            dialog.setMessage(getString(R.string.loading));
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                TaxesHandler taxesHandler = new TaxesHandler(getActivity());
                taxes = taxesHandler.getTaxes();

                printerDate = bixolon.getPrinterTFHKA().getS1PrinterData().getCurrentPrinterDate();
//                this.taxes[0] = bixolon.getPrinterTFHKA().getS3PrinterData().getTax1();
//                typeTaxes[0] = bixolon.getPrinterTFHKA().getS3PrinterData().getTypeTax1();
//                this.taxes[1] = bixolon.getPrinterTFHKA().getS3PrinterData().getTax2();
//                typeTaxes[1] = bixolon.getPrinterTFHKA().getS3PrinterData().getTypeTax2();
//                this.taxes[2] = bixolon.getPrinterTFHKA().getS3PrinterData().getTax3();
//                typeTaxes[2] = bixolon.getPrinterTFHKA().getS3PrinterData().getTypeTax3();
//                this.taxes[3] = bixolon.getPrinterTFHKA().getS3PrinterData().getTax4();
//                typeTaxes[3] = bixolon.getPrinterTFHKA().getS3PrinterData().getTypeTax4();
//                this.taxes[4] = bixolon.getPrinterTFHKA().getS3PrinterData().getTax5();
//                typeTaxes[4] = bixolon.getPrinterTFHKA().getS3PrinterData().getTypeTax5();

                MemoTextHandler handler = new MemoTextHandler(getActivity());
                headers = handler.getHeader();
                footers = handler.getFooter();
            } catch (PrinterException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            headersViews = new TextView[3];
            footersViews = new TextView[3];
            taxesViews = new TextView[5];
            typeTaxesViews = new TextView[5];

            date = (TextView) getView().findViewById(R.id.bixolondatetextView25);
            headersViews[0] = (TextView) getView().findViewById(R.id.bixolonheader1textView25b);
            headersViews[1] = (TextView) getView().findViewById(R.id.bixolonheader2textView25);
            headersViews[2] = (TextView) getView().findViewById(R.id.bixolonheader3textView25);

            footersViews[0] = (TextView) getView().findViewById(R.id.bixolonfooter1textView25c);
            footersViews[1] = (TextView) getView().findViewById(R.id.bixolonfooter2textView25c);
            footersViews[2] = (TextView) getView().findViewById(R.id.bixolonfooter3textView25c);

            taxesViews[0] = (TextView) getView().findViewById(R.id.bixolontaxes1textView25);
            taxesViews[1] = (TextView) getView().findViewById(R.id.bixolontaxes2textView26);
            taxesViews[2] = (TextView) getView().findViewById(R.id.bixolontaxes3textView27);
            taxesViews[3] = (TextView) getView().findViewById(R.id.bixolontaxes4textView28);
            taxesViews[4] = (TextView) getView().findViewById(R.id.bixolontaxes5textView29);

            typeTaxesViews[0] = (TextView) getView().findViewById(R.id.taxType1textView25);
            typeTaxesViews[1] = (TextView) getView().findViewById(R.id.taxType2textView29);
            typeTaxesViews[2] = (TextView) getView().findViewById(R.id.taxType3textView26);
            typeTaxesViews[3] = (TextView) getView().findViewById(R.id.taxType4textView27);
            typeTaxesViews[4] = (TextView) getView().findViewById(R.id.taxType5textView28);

            int i = 0;
            for (Tax tax : taxes) {
                if (tax.getTaxType().equalsIgnoreCase("S")) {
                    taxesViews[i].setText(String.format("%s %s%%", getString(R.string.text_tax),
                            String.valueOf(tax.getTaxRate())));
                    typeTaxesViews[i].setText(String.format("%s %s", getString(R.string.taxtype),
                            String.valueOf(tax.getTaxName())));
                    i++;
                    if (i >= taxesViews.length) {
                        break;
                    }
                }
            }

//            for (int i = 0; i < 5; i++) {
//                taxesViews[i].setText(String.format("%s %s", getString(R.string.text_tax),
//                        String.valueOf(taxes[i])));
//                typeTaxesViews[i].setText(String.format("%s %s", getString(R.string.taxtype),
//                        String.valueOf(typeTaxes[i])));
//            }

            if (printerDate != null) {
                date.setText(DateUtils.getDateAsString(printerDate, DateUtils.DATE_MMM_dd_yyyy_h_mm_a));
            } else {
                date.setText(DateUtils.getDateAsString(new Date(), DateUtils.DATE_MMM_dd_yyyy_h_mm_a));
            }

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
            dialog.dismiss();
        }
    }
}
