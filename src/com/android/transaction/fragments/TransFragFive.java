package com.android.transaction.fragments;


import com.android.database.CustomersHandler;
import com.android.database.OrdersHandler;
import com.android.emobilepos.HistTransOrderDetailMenu;
import com.android.emobilepos.R;

import com.android.support.Global;

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

public class TransFragFive extends Fragment {

	private CustomCursorAdapter adap2;

	private Cursor myCursor;
	private OrdersHandler handler;
	private ListView lView;
	private String type = "'5'";
	
	private boolean isFromCustomers = false;
	private String receivedCustID;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.trans_listview_layout, container, false);
		lView = (ListView) view.findViewById(R.id.transactionListview);

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
				String test = s.toString().trim();
				if (test.isEmpty()) {
					if (myCursor != null)
						myCursor.close();

					handler = new OrdersHandler(getActivity());
					
					if(isFromCustomers)
						myCursor = handler.getReceipts1CustData(type, receivedCustID);
					else
						myCursor = handler.getReceipts1Data(type);
					

					adap2 = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
					lView.setAdapter(adap2);
				}
			}
		});

		handler = new OrdersHandler(getActivity());

		final Bundle extras = getActivity().getIntent().getExtras();
		if(extras!=null)
			isFromCustomers = extras.getBoolean("is_from_customers", false);
		
		
		if(isFromCustomers)
		{
			receivedCustID = extras.getString("cust_id");
			myCursor = handler.getReceipts1CustData(type, receivedCustID);
		}
		else
		{
			myCursor = handler.getReceipts1Data(type);
		}
		
		adap2 = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);

		lView.setAdapter(adap2);

		lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub

				myCursor.moveToPosition(position);
				String ordID = myCursor.getString(myCursor.getColumnIndex("_id")); // ord_id
																					// is
																					// being
																					// returned
																					// as
																					// _id
				Intent intent = new Intent(arg0.getContext(), HistTransOrderDetailMenu.class);
				intent.putExtra("ord_id", ordID);
				intent.putExtra("trans_type", "5");
				startActivityForResult(intent,0);
			}
		});

		return view;

	}

	public void performSearch(String text) {
		if (myCursor != null)
			myCursor.close();
		
		if(isFromCustomers)
			myCursor = handler.getSearchOrder(type, text, receivedCustID);
		else
			myCursor = handler.getSearchOrder(type, text, receivedCustID);
		

		adap2 = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
		lView.setAdapter(adap2);

	}

	public class CustomCursorAdapter extends CursorAdapter {
		/*LayoutInflater inflater;
		CustomersHandler custHandler = new CustomersHandler(getActivity());
		Global global = (Global) getActivity().getApplication();

		public CustomCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
			// TODO Auto-generated constructor stub
			inflater = LayoutInflater.from(context);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub

			TextView title = (TextView) view.findViewById(R.id.transLVtitle);
			TextView clientName = (TextView) view.findViewById(R.id.transLVid);
			TextView amount = (TextView) view.findViewById(R.id.transLVamount);
			ImageView syncIcon = (ImageView) view.findViewById(R.id.transIcon);
			TextView voidText = (TextView) view.findViewById(R.id.transVoidText);

			String totalPrice = cursor.getString(cursor.getColumnIndex("ord_total"));
			final String ordID = cursor.getString(cursor.getColumnIndex("_id")); // getting ord_id as _id

			// used to change icons
			final String isSync = cursor.getString(cursor.getColumnIndex("ord_issync"));
			final String isVoid = cursor.getString(cursor.getColumnIndex("isVoid"));

			if (isSync.equals("0"))// it is synch
			{
				// change img
				syncIcon.setImageResource(R.drawable.is_sync);
			} else {
				syncIcon.setImageResource(R.drawable.is_not_sync);
			}

			if (isVoid.equals("0"))// is not VOID
			{
				voidText.setVisibility(View.GONE);
			} else // it is Void
			{
				voidText.setVisibility(View.VISIBLE);
			}
		
			title.setText(ordID);

			totalPrice = totalPrice.replace("$", "");
			amount.setText(format(totalPrice));
			String custID = cursor.getString(cursor.getColumnIndex("cust_id"));
			clientName.setText(custHandler.getSpecificValue("cust_name", custID));
		}

		public String format(String text) {
			if (text.isEmpty())
				return Global.formatDoubleToCurrency(0.00);
			return Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(text)));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub

			final View retView = inflater.inflate(R.layout.trans_lvadapter, parent, false);
			return retView;
		}*/
		
		LayoutInflater inflater;
		CustomersHandler custHandler = new CustomersHandler(getActivity());
		Global global = (Global) getActivity().getApplication();
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
			// TODO Auto-generated method stub

			myHolder = (ViewHolder)view.getTag();
			
			myHolder.title.setText(cursor.getString(myHolder.i_ord_id));
			temp = cursor.getString(myHolder.i_cust_id);
			myHolder.clientName.setText(custHandler.getSpecificValue("cust_name",temp ));
			temp = Global.formatDoubleStrToCurrency(cursor.getString(myHolder.i_ord_total));
			myHolder.amount.setText(temp);
			
			if(cursor.getString(myHolder.i_ord_issync).equals("1")) //it is synched
				myHolder.syncIcon.setImageResource(R.drawable.is_sync);
			else
				myHolder.syncIcon.setImageResource(R.drawable.is_not_sync);
			
			if(cursor.getString(myHolder.i_isVoid).equals("0"))//it is not void
				myHolder.voidText.setVisibility(View.GONE);
			else
				myHolder.voidText.setVisibility(View.VISIBLE);
			
			
		}

		public String format(String text) {

			if (text.isEmpty())
				return Global.formatDoubleToCurrency(0.00);
			return Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(text)));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub

			View retView = inflater.inflate(R.layout.trans_lvadapter, parent, false);
			
			ViewHolder holder = new ViewHolder();
			
			holder.title = (TextView)retView.findViewById(R.id.transLVtitle);
			holder.clientName = (TextView) retView.findViewById(R.id.transLVid);
			holder.amount = (TextView)retView.findViewById(R.id.transLVamount);
			holder.voidText = (TextView) retView.findViewById(R.id.transVoidText);
			holder.syncIcon = (ImageView)retView.findViewById(R.id.transIcon);
			
			holder.i_ord_id = cursor.getColumnIndex("_id");
			holder.i_cust_id = cursor.getColumnIndex("cust_id");
			holder.i_ord_total = cursor.getColumnIndex("ord_total");
			holder.i_ord_issync = cursor.getColumnIndex("ord_issync");
			holder.i_isVoid = cursor.getColumnIndex("isVoid");
			retView.setTag(holder);
			return retView;
		}
		
		private class ViewHolder
		{
			TextView title,clientName,amount,voidText;
			ImageView syncIcon;
			
			int i_ord_id,i_cust_id,i_ord_total,i_isVoid,i_ord_issync;
		}
	}
}
