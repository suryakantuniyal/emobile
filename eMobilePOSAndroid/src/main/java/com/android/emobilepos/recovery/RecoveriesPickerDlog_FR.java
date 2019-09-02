package com.android.emobilepos.recovery;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.database.OrdersHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.holders.Recoveries_Holder;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Luis Camayd on 8/30/2019.
 */
public class RecoveriesPickerDlog_FR extends DialogFragment implements Comparator<Recoveries_Holder> {

    private List<Recoveries_Holder> allRecoveriesList;
    private RecoveriesPicker_Listener listener;

    public void setListener(RecoveriesPicker_Listener listener) {
        this.listener = listener;
    }

    public boolean fillAllRecoveries(Context context) {
        if (allRecoveriesList == null) {
            try {
                allRecoveriesList = new ArrayList<>();

                OrdersHandler ordersHandler = new OrdersHandler(context);
                allRecoveriesList = ordersHandler.getRecoveriesOrders();
                return allRecoveriesList.size() > 0;
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
        return false;
    }

    public static RecoveriesPickerDlog_FR newInstance(String dialogTitle) {
        RecoveriesPickerDlog_FR picker = new RecoveriesPickerDlog_FR();
        Bundle bundle = new Bundle();
        bundle.putString("dialogTitle", dialogTitle);
        picker.setArguments(bundle);
        return picker;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate view
        View view = inflater.inflate(R.layout.recovery_picker, null);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.bg_dlog);
        getDialog().setCanceledOnTouchOutside(true);

        // Set dialog title if show as dialog
        String dialogTitle = getString(R.string.dlog_title_recovered_orders);
        getDialog().setTitle(dialogTitle);

        int width = getResources().getDimensionPixelSize(R.dimen.cp_dialog_width);
        int height = getResources().getDimensionPixelSize(R.dimen.cp_dialog_height);
        getDialog().getWindow().setLayout(width, height);

        // Get view components
        ListView recoveriesListView = view.findViewById(R.id.recoveries_picker_listview);

        // Set adapter
        RecoveriesLV_Adapter adapter = new RecoveriesLV_Adapter(getActivity(), allRecoveriesList);
        recoveriesListView.setAdapter(adapter);

        // Inform listener
        recoveriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) {
                    Recoveries_Holder recovery = allRecoveriesList.get(position);
                    listener.onSelected(recovery);
                }
            }
        });

        return view;
    }

    @Override
    public int compare(Recoveries_Holder recoveries_holder, Recoveries_Holder t1) {
        return 0;
    }
}
