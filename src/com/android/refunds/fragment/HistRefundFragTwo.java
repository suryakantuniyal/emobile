package com.android.refunds.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.emobilepos.HistPayDetailsMenuActivity;
import com.android.emobilepos.R;

public class HistRefundFragTwo extends Fragment {

	private ListViewAdapter adap;
	

	private final static List<String> allTitles = Arrays.asList(new String[] {});
	private final static List<String> allAmounts = Arrays.asList(new String[] {});

	private List<String> filteredTitles;
	private List<String> filteredAmounts;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.histpay_listview_layout, container, false);
		ListView lView = (ListView) view.findViewById(R.id.histPaymentListview);
		adap = new ListViewAdapter(getActivity());

		TextView subTitle = (TextView) view.findViewById(R.id.synchStatic);
		subTitle.setText("");

		EditText field = (EditText) view.findViewById(R.id.searchField);

		field.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

				adap.getFilter().filter(s);
			}
		});

		lView.setAdapter(adap);

		lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub

				Intent intent = new Intent(arg0.getContext(), HistPayDetailsMenuActivity.class);
				intent.putExtra("histpay", false);
				startActivity(intent);

			}
		});

		return view;

	}

	public class ListViewAdapter extends BaseAdapter implements Filterable {
		private LayoutInflater myInflater;
		private MyLVFilter filter;

		public ListViewAdapter(Context context) {
			myInflater = LayoutInflater.from(context);

			filteredTitles = allTitles;
			filteredAmounts = allAmounts;

		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return filteredTitles.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return filteredTitles.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			ViewHolder holder;

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = myInflater.inflate(R.layout.histpay_lvadapter, null);
				holder.title = (TextView) convertView.findViewById(R.id.histpayTitle);

				holder.amount = (TextView) convertView.findViewById(R.id.histpaySubtitle);

				holder.title.setText(filteredTitles.get(position));

				holder.amount.setText(filteredAmounts.get(position));

				convertView.setTag(holder);
			}

			else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.title.setText(filteredTitles.get(position));

			holder.amount.setText(filteredAmounts.get(position));

			return convertView;
		}

		@Override
		public Filter getFilter() {
			// TODO Auto-generated method stub
			if (filter == null) {
				filter = new MyLVFilter();

			}
			return filter;
		}

		private class MyLVFilter extends Filter {
			ArrayList<String> filtTitle;

			ArrayList<String> filtAmount;

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				// TODO Auto-generated method stub

				constraint = constraint.toString().toLowerCase();

				FilterResults result = new FilterResults();

				if (constraint != null && constraint.toString().length() > 0) {
					filtTitle = new ArrayList<String>();

					filtAmount = new ArrayList<String>();

					for (int i = 0, l = allTitles.size(); i < l; i++) {
						String curTitle = allTitles.get(i);

						if (curTitle.toLowerCase().contains(constraint)) {
							filtTitle.add(curTitle);

							filtAmount.add(allAmounts.get(i));
						}

					}
					result.count = filtTitle.size();
					result.values = filtTitle;

				} else {
					synchronized (this) {
						result.values = allTitles;
						result.count = allTitles.size();
					}
				}
				return result;
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				// TODO Auto-generated method stub
				if (results.count > 0 && constraint.toString().length() > 0) {
					filteredTitles = filtTitle;

					filteredAmounts = filtAmount;
					adap.notifyDataSetChanged();
				} else {
					if (constraint.toString().length() == 0 || constraint == null) {
						filteredTitles = allTitles;

						filteredAmounts = allAmounts;
						adap.notifyDataSetInvalidated();

					} else {
						filteredTitles = filtTitle;

						filteredAmounts = filtAmount;
						adap.notifyDataSetInvalidated();
					}
				}
			}

		}

		public class ViewHolder {
			TextView title;
			TextView amount;
			ImageView iconImage;

		}

	}
}