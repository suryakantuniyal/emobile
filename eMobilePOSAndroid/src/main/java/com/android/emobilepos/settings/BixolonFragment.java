package com.android.emobilepos.settings;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.dao.BixolonDAO;
import com.android.dao.PaymentMethodDAO;
import com.android.database.MemoTextHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.Tax;
import com.android.emobilepos.models.realms.Bixolon;
import com.android.emobilepos.models.realms.PaymentMethod;
import com.android.support.DateUtils;
import com.android.support.DeviceUtils;
import com.android.support.Global;
import com.thefactoryhka.android.controls.PrinterException;

import java.util.Date;
import java.util.List;

import drivers.EMSBixolonRD;
import main.EMSDeviceManager;

public class BixolonFragment extends Fragment implements View.OnClickListener {

    private EMSBixolonRD bixolonDevice;
    private TextView time;
    private TextView date;
    private TextView[] headersViews;
    private TextView[] footersViews;
    private TextView[] taxesViews;
    private TextView[] typeTaxesViews;
    private List<Tax> taxes;
    private String[] headers;
    private String[] footers;
    private List<PaymentMethod> paymentMethods;

    public BixolonFragment() {
        EMSDeviceManager emsDeviceManager = DeviceUtils.getEmsDeviceManager(Global.BIXOLON_RD, Global.printerDevices);
        if (emsDeviceManager != null && emsDeviceManager.getCurrentDevice() != null
                && emsDeviceManager.getCurrentDevice() instanceof EMSBixolonRD) {
            bixolonDevice = (EMSBixolonRD) emsDeviceManager.getCurrentDevice();
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
        if (bixolonDevice != null && getView() != null) {
            new InitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            getView().findViewById(R.id.sendDateTimebutton2).setOnClickListener(this);
            getView().findViewById(R.id.sendHeaderbutton2b).setOnClickListener(this);
            getView().findViewById(R.id.sendFooterbutton2c).setOnClickListener(this);
            getView().findViewById(R.id.sendTaxesbutton28).setOnClickListener(this);
            getView().findViewById(R.id.sendPaymentMethodsbutton28).setOnClickListener(this);
            getView().findViewById(R.id.printZReportbutton3).setOnClickListener(this);
            getView().findViewById(R.id.printXReportbutton2).setOnClickListener(this);
            getView().findViewById(R.id.printBixolonSettingsbutton).setOnClickListener(this);
        }
        Bixolon bixolon = BixolonDAO.getBixolon();
        if (bixolon != null && getView() != null) {
            ((EditText) getView().findViewById(R.id.bixolonructextView2)).setText(bixolon.getRuc());
            ((EditText) getView().findViewById(R.id.bixolonNCFEditText)).setText(bixolon.getNcf());
            ((EditText) getView().findViewById(R.id.bixolonmerchantNametextView2)).setText(bixolon.getMerchantName());

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveInfo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void saveInfo() {
        Bixolon bixolon = new Bixolon();
        bixolon.setRuc(((EditText) getView().findViewById(R.id.bixolonructextView2)).getText().toString());
        bixolon.setNcf(((EditText) getView().findViewById(R.id.bixolonNCFEditText)).getText().toString());
        bixolon.setMerchantName(((EditText) getView().findViewById(R.id.bixolonmerchantNametextView2)).getText().toString());
        BixolonDAO.save(bixolon);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.printBixolonSettingsbutton:
                new SendBixolonCommandTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Bixoloncommand.PRINT_SETTINGS);
                break;
            case R.id.printXReportbutton2:
                new SendBixolonCommandTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Bixoloncommand.PRINT_X);
                break;
            case R.id.printZReportbutton3:
                new SendBixolonCommandTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Bixoloncommand.PRINT_Z);
                break;
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
            case R.id.sendPaymentMethodsbutton28:
                new SendBixolonCommandTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Bixoloncommand.SEND_PAYMENT_METHODS);
                break;

        }
    }

    private enum Bixoloncommand {
        SEND_DATE, SEND_HEADER, SEND_FOOTER, SEND_TAXES, SEND_PAYMENT_METHODS, PRINT_Z, PRINT_X, PRINT_SETTINGS
    }

    private class SendBixolonCommandTask extends AsyncTask<Bixoloncommand, Void, Boolean> {
        ProgressDialog dialog = new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute() {
            getActivity().setRequestedOrientation(Global.getScreenOrientation(getActivity()));
            dialog.setIndeterminate(true);
            dialog.setMessage(getString(R.string.processing));
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Bixoloncommand... params) {
            Bixoloncommand command = params[0];
            switch (command) {
                case PRINT_SETTINGS:
                    return bixolonDevice.printBixolonSettings();
                case PRINT_Z:
                    try {
                        bixolonDevice.printZReport();
                    } catch (PrinterException e) {
                        return false;
                    }
                    return true;
                case PRINT_X:
                    try {
                        bixolonDevice.printXReport();
                    } catch (PrinterException e) {
                        return false;
                    }
                    return true;
                case SEND_DATE:
                    return bixolonDevice.sendDateTimeCommand(new Date());
                case SEND_HEADER:
                    return bixolonDevice.sendHeaders(headers);
                case SEND_FOOTER:
                    return bixolonDevice.sendFooters(footers);
                case SEND_TAXES:
                    TaxesHandler taxesHandler = new TaxesHandler(getActivity());
                    return bixolonDevice.sendTaxes(taxesHandler.getProductTaxes(false));
                case SEND_PAYMENT_METHODS:
                    return bixolonDevice.sendPaymentMethods(paymentMethods);
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
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    private class InitTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        private Date printerDate;

        @Override
        protected void onPreExecute() {
            getActivity().setRequestedOrientation(Global.getScreenOrientation(getActivity()));
            dialog.setIndeterminate(true);
            dialog.setMessage(getString(R.string.loading));
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
//            try {
            TaxesHandler taxesHandler = new TaxesHandler(getActivity());
            taxes = taxesHandler.getProductTaxes(false);
            printerDate = bixolonDevice.getCurrentPrinterDateTime();
            MemoTextHandler handler = new MemoTextHandler(getActivity());
            headers = handler.getHeader();
            footers = handler.getFooter();
            paymentMethods = PaymentMethodDAO.getPaymentMethods();
//            } catch (PrinterException e) {
//                e.printStackTrace();
//            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            headersViews = new TextView[3];
            footersViews = new TextView[3];
            taxesViews = new TextView[5];
            typeTaxesViews = new TextView[5];
            if (getView() != null && getView().findViewById(R.id.bixolon_paymentMethodsContainer) != null) {
                LinearLayout payMethodContainer = getView().findViewById(R.id.bixolon_paymentMethodsContainer);
                LayoutInflater layoutInflater = getActivity().getLayoutInflater();
                for (PaymentMethod paymentMethod : paymentMethods) {
                    LinearLayout row = (LinearLayout) layoutInflater.inflate(R.layout.bixolon_two_cols_row_layout, null);
                    TextView col1 = row.findViewById(R.id.bixoloncolumn1);
                    TextView col2 = row.findViewById(R.id.bixoloncolumn2);
                    col1.setText(paymentMethod.getPaymentmethod_type());
                    col2.setText(paymentMethod.getPaymethod_name());
                    payMethodContainer.addView(row);
                }

                date = getView().findViewById(R.id.bixolondatetextView25);
                headersViews[0] = getView().findViewById(R.id.bixolonheader1textView25b);
                headersViews[1] = getView().findViewById(R.id.bixolonheader2textView25);
                headersViews[2] = getView().findViewById(R.id.bixolonheader3textView25);

                footersViews[0] = getView().findViewById(R.id.bixolonfooter1textView25c);
                footersViews[1] = getView().findViewById(R.id.bixolonfooter2textView25c);
                footersViews[2] = getView().findViewById(R.id.bixolonfooter3textView25c);

                taxesViews[0] = getView().findViewById(R.id.bixolontaxes1textView25);
                taxesViews[1] = getView().findViewById(R.id.bixolontaxes2textView26);
                taxesViews[2] = getView().findViewById(R.id.bixolontaxes3textView27);
                taxesViews[3] = getView().findViewById(R.id.bixolontaxes4textView28);
                taxesViews[4] = getView().findViewById(R.id.bixolontaxes5textView29);

                typeTaxesViews[0] = getView().findViewById(R.id.taxType1textView25);
                typeTaxesViews[1] = getView().findViewById(R.id.taxType2textView29);
                typeTaxesViews[2] = getView().findViewById(R.id.taxType3textView26);
                typeTaxesViews[3] = getView().findViewById(R.id.taxType4textView27);
                typeTaxesViews[4] = getView().findViewById(R.id.taxType5textView28);

                int i = 0;
                for (Tax tax : taxes) {
                    taxesViews[i].setText(String.format("%s %s%%", getString(R.string.text_tax),
                            String.valueOf(tax.getTaxRate())));
                    typeTaxesViews[i].setText(String.format("%s %s", getString(R.string.taxtype),
                            String.valueOf(tax.getTaxName())));
                    i++;
                    if (i >= taxesViews.length) {
                        break;
                    }
                }
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
            }
            dialog.dismiss();
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }
}
