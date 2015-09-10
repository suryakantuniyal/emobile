package com.android.refunds.fragment;

import java.text.DecimalFormat;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.database.PaymentsHandler;
import com.android.emobilepos.HistPayDetailsMenuActivity;
import com.android.emobilepos.R;
import com.android.support.Global;

public class HistRefundFragOne extends Fragment {

	private PaymentsHandler handler;
	private Cursor payCursor;
	private CustomCursorAdapter myAdapter;
	private ListView lView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.histpay_listview_layout, container, false);
		lView = (ListView) view.findViewById(R.id.histPaymentListview);

		TextView subTitle = (TextView) view.findViewById(R.id.synchStatic);
		subTitle.setText("");

		EditText field = (EditText) view.findViewById(R.id.searchField);
		field.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					String text = v.getText().toString().trim();
					if (!text.isEmpty())
						performSearch(text);
					return true;
				}
				return false;
			}
		});
		
		field.addTextChangedListener(new TextWatcher() 
		{

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
				String test = s.toString().trim();
				if (test.isEmpty()) {
					if (payCursor != null)
						payCursor.close();

					handler = new PaymentsHandler(getActivity());
					payCursor = handler.getCashCheckGiftPayment("Cash",true); // type = Cash

					myAdapter = new CustomCursorAdapter(getActivity(), payCursor, CursorAdapter.NO_SELECTION);
					lView.setAdapter(myAdapter);
				}
			}
		});

		handler = new PaymentsHandler(getActivity());
		payCursor = handler.getCashCheckGiftPayment("Cash",true);
		myAdapter = new CustomCursorAdapter(getActivity(), payCursor, CursorAdapter.NO_SELECTION);

		lView.setAdapter(myAdapter);

		lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub

				Intent intent = new Intent(arg0.getContext(), HistPayDetailsMenuActivity.class);
				intent.putExtra("histpay", true);
				payCursor.moveToPosition(position);
				String pay_id = payCursor.getString(payCursor.getColumnIndex("_id"));				//pay_id is returned as _id
				intent.putExtra("pay_id", pay_id);
				intent.putExtra("job_id", payCursor.getString(payCursor.getColumnIndex("job_id")));
				intent.putExtra("pay_amount", payCursor.getString(payCursor.getColumnIndex("pay_amount")));
				intent.putExtra("cust_name", payCursor.getString(payCursor.getColumnIndex("cust_name")));
				intent.putExtra("paymethod_name", "Cash");
				startActivity(intent);

			}
		});
		return view;

	}

	public void performSearch(String text) {
		if (payCursor != null)
			payCursor.close();
		payCursor = handler.searchCashCheckGift("Cash", text);

		myAdapter = new CustomCursorAdapter(getActivity(), payCursor, CursorAdapter.NO_SELECTION);
		lView.setAdapter(myAdapter);
	}

	public class CustomCursorAdapter extends CursorAdapter {
		LayoutInflater inflater;
		ViewHolder myHolder;
		String temp = new String();
		String empStr = "";

		public CustomCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
			// TODO Auto-generated constructor stub
			inflater = LayoutInflater.from(context);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stubs
			
			/*TextView title = (TextView) view.findViewById(R.id.histpayTitle);
			TextView amount = (TextView) view.findViewById(R.id.histpaySubtitle);

			String custName = cursor.getString(cursor.getColumnIndex("cust_name"));
			String amnt = cursor.getString(cursor.getColumnIndex("pay_amount"));
			
			if (custName == null)
				custName = "";
			if (amnt == null)
				amnt = "";

			title.setText(custName);
			amount.setText(format(amnt));*/
			
			myHolder = (ViewHolder)view.getTag();
			
			temp = cursor.getString(myHolder.i_cust_name);
			if(temp==null)
				temp = empStr;
			myHolder.title.setText(temp);
			
			temp = cursor.getString(myHolder.i_pay_amount);
			if(temp==null&&!temp.isEmpty())
				temp = empStr;
			else
				temp = Global.formatDoubleStrToCurrency(temp);
			
			myHolder.amount.setText(temp);
			
			if(cursor.getString(myHolder.i_pay_issync).equals("1"))//it is synch
				myHolder.iconImage.setImageResource(R.drawable.is_sync);
			else
				myHolder.iconImage.setImageResource(R.drawable.is_not_sync);
			
			if(cursor.getString(myHolder.i_isVoid).equals("0"))//is not VOID
				myHolder.voidText.setVisibility(View.GONE);
			else
				myHolder.voidText.setVisibility(View.VISIBLE);
			

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub

			final View retView = inflater.inflate(R.layout.histpay_lvadapter, parent, false);
			
			ViewHolder holder = new ViewHolder();
			holder.title = (TextView) retView.findViewById(R.id.histpayTitle);
			holder.amount = (TextView)retView.findViewById(R.id.histpaySubtitle);
			holder.voidText = (TextView)retView.findViewById(R.id.histpayVoidText);
			holder.iconImage = (ImageView)retView.findViewById(R.id.histpayIcon);
			
			holder.i_cust_name = cursor.getColumnIndex("cust_name");
			holder.i_pay_amount = cursor.getColumnIndex("pay_amount");
			holder.i_pay_issync = cursor.getColumnIndex("pay_issync");
			holder.i_isVoid = cursor.getColumnIndex("isVoid");
			retView.setTag(holder);
			
			return retView;
		}

		
		private class ViewHolder
		{
			TextView title,amount,voidText;
			
			ImageView iconImage;
			
			int i_cust_name,i_pay_amount,i_pay_issync,i_isVoid;
		}
		
		
		public String format(String text) {
			DecimalFormat frmt = new DecimalFormat("0.00");
			if (text.isEmpty())
				return "0.00";
			return frmt.format(Double.parseDouble(text));
		}

	}
}