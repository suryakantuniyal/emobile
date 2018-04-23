package com.android.emobilepos.settings.printers;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.android.dao.DeviceTableDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.models.realms.Device;

import io.realm.RealmList;

/**
 * A fragment representing a single device detail screen.
 * This fragment is either contained in a {@link DeviceListActivity}
 * in two-pane mode (on tablets) or a {@link DeviceDetailActivity}
 * on handsets.
 */
public class DeviceDetailFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {
    Switch paymetReceiptSwitch;
    Switch transactionReceiptSwitch;
    Switch paymentReprintASwitch;
    Switch transactionReprintASwitch;
    Switch reportsASwitch;
    public static final String ARG_ITEM_ID = "item_id";
    private Device device;

    public DeviceDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String name = getArguments().getString(DeviceDetailFragment.ARG_ITEM_ID);
        device = DeviceTableDAO.getByName(name);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.device_detail, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        paymetReceiptSwitch = view.findViewById(R.id.printPaymentReceiptswitch1);
        transactionReceiptSwitch = view.findViewById(R.id.printTransactionReceiptswitch1);
        paymentReprintASwitch = view.findViewById(R.id.reprintPaymentReceiptswitch1);
        transactionReprintASwitch = view.findViewById(R.id.reprintTransactionReceiptswitch1);
        reportsASwitch = view.findViewById(R.id.printReportsswitch1);
        if (device != null) {
            for (String name : device.getSelectedPritables()) {
                Device.Printables printables = Device.Printables.valueOf(name);
                switch (printables) {
                    case REPORTS:
                        reportsASwitch.setChecked(true);
                        break;
                    case PAYMENT_RECEIPT:
                        paymetReceiptSwitch.setChecked(true);
                        break;
                    case TRANSACTION_RECEIPT:
                        transactionReceiptSwitch.setChecked(true);
                        break;
                    case PAYMENT_RECEIPT_REPRINT:
                        paymentReprintASwitch.setChecked(true);
                        break;
                    case TRANSACTION_RECEIPT_REPRINT:
                        transactionReprintASwitch.setChecked(true);
                        break;
                }
            }
        }
        paymetReceiptSwitch.setOnCheckedChangeListener(this);
        transactionReceiptSwitch.setOnCheckedChangeListener(this);
        transactionReprintASwitch.setOnCheckedChangeListener(this);
        reportsASwitch.setOnCheckedChangeListener(this);
        paymentReprintASwitch.setOnCheckedChangeListener(this);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        RealmList<String> values = new RealmList<>();
        if (paymetReceiptSwitch.isChecked()) {
            values.add(Device.Printables.PAYMENT_RECEIPT.name());
        }
        if (paymentReprintASwitch.isChecked()) {
            values.add(Device.Printables.PAYMENT_RECEIPT_REPRINT.name());
        }
        if (transactionReceiptSwitch.isChecked()) {
            values.add(Device.Printables.TRANSACTION_RECEIPT.name());
        }
        if (transactionReprintASwitch.isChecked()) {
            values.add(Device.Printables.TRANSACTION_RECEIPT_REPRINT.name());
        }
        if (reportsASwitch.isChecked()) {
            values.add(Device.Printables.REPORTS.name());
        }
        device.setSelectedPritables(values);
        DeviceTableDAO.upsert(device);
    }
}
