package com.android.emobilepos.adapters;

import android.app.Activity;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.emobilepos.R;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.util.ArrayList;
import java.util.List;

public class SalesMenuAdapter extends BaseAdapter implements Filterable {
    private LayoutInflater mInflater;
    private Activity activity;

    private int listViewSize = 0;

    private List<Integer> indexOfEnabled = new ArrayList<Integer>();
    private String[] mainMenuList;
    private SparseArray<String> mainMenuIconsMap;


    public SalesMenuAdapter(Activity activity, boolean custSelected) {
        mInflater = LayoutInflater.from(activity);
        this.activity = activity;
        MyPreferences preferences = new MyPreferences(activity);
        if (preferences.isRestaurantMode()) {
            mainMenuList = activity.getResources().getStringArray(R.array.mainMenuRestaurantArray);
        } else {
            mainMenuList = activity.getResources().getStringArray(R.array.mainMenuArray);
        }

        mainMenuIconsMap = new SparseArray<>();
        mainMenuIconsMap.put(Global.TransactionType.SALE_RECEIPT.getCode(), "list");
        mainMenuIconsMap.put(Global.TransactionType.ORDERS.getCode(), "order");
        mainMenuIconsMap.put(Global.TransactionType.RETURN.getCode(), "return_icon");
        mainMenuIconsMap.put(Global.TransactionType.INVOICE.getCode(), "invoice");
        mainMenuIconsMap.put(Global.TransactionType.ESTIMATE.getCode(), "estimate");
        mainMenuIconsMap.put(Global.TransactionType.PAYMENT.getCode(), "payment");
        mainMenuIconsMap.put(Global.TransactionType.GIFT_CARD.getCode(), "gift_card");
        mainMenuIconsMap.put(Global.TransactionType.LOYALTY_CARD.getCode(), "loyalty_card");
        mainMenuIconsMap.put(Global.TransactionType.REWARD_CARD.getCode(), "gift_card");
        mainMenuIconsMap.put(Global.TransactionType.REFUND.getCode(), "return_icon");
        mainMenuIconsMap.put(Global.TransactionType.ROUTE.getCode(), "routes");
        mainMenuIconsMap.put(Global.TransactionType.ON_HOLD.getCode(), "list_red");
        mainMenuIconsMap.put(Global.TransactionType.CONSIGNMENT.getCode(), "list");
        mainMenuIconsMap.put(Global.TransactionType.LOCATION.getCode(), "list");
        mainMenuIconsMap.put(Global.TransactionType.TIP_ADJUSTMENT.getCode(), "order");
        mainMenuIconsMap.put(Global.TransactionType.SHIFTS.getCode(), "shifts");
        mainMenuIconsMap.put(Global.TransactionType.SHIFT_EXPENSES.getCode(), "shift_expenses");


        MyPreferences myPref = new MyPreferences(activity);
        boolean[] temp = myPref.getMainMenuPreference();

        boolean[] enabledSettings;
        if (custSelected) {
            enabledSettings = temp;
        } else {
            enabledSettings = new boolean[]{temp[0], false, temp[2], false, false, temp[5], temp[6],
                    temp[7], temp[8], temp[9], false, temp[11], false,
                    temp[13], temp[14], temp[15], temp[16]};
        }

        int size = mainMenuList.length;
        for (int i = 0; i < size; i++) {
            if (i == Global.TransactionType.LOYALTY_CARD.getCode() && !TextUtils.isEmpty(myPref.getDefaultUnitsName())) {
                mainMenuList[i] = String.format(activity.getString(R.string.default_unit_name_format), myPref.getDefaultUnitsName());
            }
            if (enabledSettings[i]) {
                indexOfEnabled.add(i);
                listViewSize++;
            }
        }
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.main_menu_listviewadapter, null);

            holder = new ViewHolder();
            holder.textLine = (TextView) convertView.findViewById(R.id.salesText);
            holder.iconLine = (ImageView) convertView.findViewById(R.id.salesIcon);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String resourceName = mainMenuIconsMap.get(indexOfEnabled.get(position));
        int iconId = activity.getResources().getIdentifier(resourceName, "drawable", activity.getPackageName());
        holder.iconLine.setImageResource(iconId);
        holder.textLine.setText(mainMenuList[indexOfEnabled.get(position)]);

        return convertView;
    }


    public class ViewHolder {
        TextView textLine;
        ImageView iconLine;
    }


    @Override
    public Filter getFilter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub

        return listViewSize;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return indexOfEnabled.get(position);
    }

}