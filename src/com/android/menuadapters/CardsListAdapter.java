package com.android.menuadapters;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.database.PayMethodsHandler;
import com.android.emobilepos.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class CardsListAdapter extends BaseAdapter implements Filterable {

	private String total;
	private String paid;
	private Context context;
	//private int index = 0;
	public final Map<String, String> icons = createMap();

	// private String [] icons = new
	// String[]{"amex","cash","debit","discover","mastercard","visa"};
	// private String[] text = new
	// String[]{"","American Express","Cash","Debit Card","Discover","MasterCard","Visa"};
	private List<String[]> payType;
	private LayoutInflater myInflater;

	public CardsListAdapter(Context context, String total, String paid, Activity activity) {
		Bundle extras = activity.getIntent().getExtras();
		myInflater = LayoutInflater.from(context);
		this.context = context;
		PayMethodsHandler handler = new PayMethodsHandler(activity);

		payType = handler.getPayMethod();

		if (extras.getBoolean("histinvoices")) {
			this.total = total;
			this.paid = paid;
		} else if (extras.getBoolean("salespayment")) {
			this.total = total;
			this.paid = paid;
		} else if (extras.getBoolean("salesrefund")) {
			this.total = total;
			this.paid = paid;
		}
	}

	private static Map<String, String> createMap() {
		HashMap<String, String> result = new HashMap<String, String>();

		result.put("American Express", "amex");
		result.put("Cash", "cash");
		result.put("Check", "debit");
		result.put("Discover", "discover");
		result.put("MasterCard", "mastercard");
		result.put("Visa", "visa");
		result.put("Debit Card", "debit");
		result.put("Gift Card", "debit");
		result.put("E-Check", "debit");
		result.put("Genius", "genius");
		result.put("Tupyx", "tupyx");

		return Collections.unmodifiableMap(result);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		ViewHolder holder;
		int type = getItemViewType(position);
		int iconId = 0;

		if (convertView == null) {

			holder = new ViewHolder();
			switch (type) {
			case 0:// dividers layout
				convertView = myInflater.inflate(R.layout.card_listrow1_adapter, null);

				holder.textLine = (TextView) convertView.findViewById(R.id.totalValue);
				holder.textLine2 = (TextView) convertView.findViewById(R.id.paidValue);

				holder.textLine.setText("$" + total);
				holder.textLine2.setText("$" + paid);

				// myText = listText[position];
				break;
			case 1: // header
				convertView = myInflater.inflate(R.layout.card_listrow2_adapter, null);

				// holder.iconImage = (ImageView)
				// convertView.findViewById(R.id.cardsListicon);
				holder.textLine2 = (TextView) convertView.findViewById(R.id.cardsListname);

				// iconId =
				// context.getResources().getIdentifier(icons[position],
				// "drawable", context.getString(R.string.pkg_name));
				// holder.iconImage.setImageResource(iconId);
				// holder.textLine2.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(iconId),
				// null, null, null);
				String value = payType.get(position - 1)[1];
				holder.textLine2.setTag(value);
				holder.textLine2.setText(value);
				// myText = listText[position]+"\n\n"+curDate;

				break;
			}

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (type == 0) {
			holder.textLine.setText("$" + total);
			holder.textLine2.setText("$" + paid);
		}

		else {

			String key = payType.get(position - 1)[1];
			iconId = context.getResources().getIdentifier(icons.get(key), "drawable", context.getString(R.string.pkg_name));
			// holder.iconImage.setImageResource(iconId);
			holder.textLine2.setTag(key);
			holder.textLine2.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(iconId), null, null, null);
			holder.textLine2.setText(key);
		}

		return convertView;
	}

	public class ViewHolder {
		TextView textLine;
		TextView textLine2;
		ImageView iconImage;

	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return 0;
		}
		return 1;

	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return payType.size() + 1;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Filter getFilter() {
		// TODO Auto-generated method stub
		return null;
	}

}
