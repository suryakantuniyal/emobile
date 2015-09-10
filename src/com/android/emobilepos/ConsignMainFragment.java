package com.android.emobilepos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


import com.android.database.ConsignmentSignaturesDBHandler;
import com.android.database.ConsignmentTransactionHandler;
import com.android.database.CustomerInventoryHandler;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.ProductsHandler;
import com.android.database.TaxesHandler;

import com.android.support.ConsignmentTransaction;
import com.android.support.GenerateNewID;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Order;
import com.android.support.OrderProducts;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


public class ConsignMainFragment extends Fragment
{
	private ListView myListview; 
	private ListViewAdapter myAdapter;
	private ProgressDialog myProgressDialog;
	private CustomerInventoryHandler custInventoryHandler;
	private MyPreferences myPref;
	private Global global;
	private Activity activity;
	private int orientation;
	
	private List<ConsignmentTransaction>consTransactionList = new ArrayList<ConsignmentTransaction>();
	private OrdersHandler ordersHandler;
	private OrderProductsHandler orderProductsHandler;
	private boolean ifInvoice = false;
	private double ordTotal = 0;
	private ConsignmentTransactionHandler consTransDBHandler;
	private HashMap<String,String>signatureMap = new HashMap<String,String>();
	private String encodedImage = new String();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.consign_fragment_layout, container, false);
		activity = getActivity();
		myListview = (ListView) view.findViewById(R.id.consignSummaryListView);
		myAdapter = new ListViewAdapter(activity);
		
		custInventoryHandler = new CustomerInventoryHandler(activity);
		myPref = new MyPreferences(activity);
		
		global = (Global) getActivity().getApplication();
		myListview.setAdapter(myAdapter);
			
		
		Button process = (Button)view.findViewById(R.id.saveConsignButton);
		
		process.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new processAsync().execute();
			}
		});
		return view;
	}
	
	
	public void notifyListViewChange()
	{
		myAdapter.notifyDataSetChanged();
	}
	
	
	private class ListViewAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		
		private List<String>idList;

		
		public ListViewAdapter(Context context) 
		{
			mInflater = LayoutInflater.from(context);
			
			idList = Global.consignMapKey;
		}

		
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			ViewHolder holder;

			if (convertView == null) 
			{
				convertView = mInflater.inflate(R.layout.consign_lisview_adapter, null);

				holder = new ViewHolder();
				
				holder.prodName = (TextView) convertView.findViewById(R.id.consignProdName);
				holder.prodID = (TextView) convertView.findViewById(R.id.consignProdID);
				holder.originalQty = (TextView)convertView.findViewById(R.id.consignOriginalQty);
				holder.rackQty = (TextView)convertView.findViewById(R.id.consignStackQty);
				holder.returnQty = (TextView)convertView.findViewById(R.id.consignReturnQty);
				holder.fillupQty = (TextView)convertView.findViewById(R.id.consignFillupQty);
				holder.issueQty = (TextView) convertView.findViewById(R.id.consignIssueQty);
				
				convertView.setTag(holder);

			}
			else 
			{
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.prodName.setText(getContentValues(position,0));
			holder.prodID.setText(getContentValues(position,1));
			holder.rackQty.setText(getContentValues(position,2));
			holder.returnQty.setText(getContentValues(position,3));
			holder.fillupQty.setText(getContentValues(position,4));
			holder.issueQty.setText(getContentValues(position,5));
			holder.originalQty.setText(getContentValues(position,6));

			return convertView;
		}

		
		public class ViewHolder 
		{
			TextView prodName,prodID,rackQty,returnQty,fillupQty,issueQty,originalQty;
		}
		
		
		private String getContentValues(int position, int type)
		{
			String value = new String();
			String empStr = "";
			switch(type)
			{
			case 0://Name
				value = Global.consignSummaryMap.get(Global.consignMapKey.get(position)).get("ordprod_name");
				if(value==null)
					value = empStr;
				break;
			case 1://ID
				value = Global.consignSummaryMap.get(Global.consignMapKey.get(position)).get("prod_id");
				if(value==null)
					value = empStr;
				else
					value = " ("+value+")";
				break;
			case 2:	//Rack
				value = Global.consignSummaryMap.get(Global.consignMapKey.get(position)).get("rack");
				if(value==null)
					value = "0";
				break;
			case 3:
				value = Global.consignSummaryMap.get(Global.consignMapKey.get(position)).get("return");
				if(value==null)
					value = "0";
				break;
			case 4:
				value = Global.consignSummaryMap.get(Global.consignMapKey.get(position)).get("fillup");
				if(value==null)
					value = "0";
				break;
			case 5:
				value = Global.consignSummaryMap.get(Global.consignMapKey.get(position)).get("invoice");
				if(value==null)
					value = "0";
				break;
			case 6:
				value = Global.consignSummaryMap.get(Global.consignMapKey.get(position)).get("original_qty");
				if(value==null)
					value = "0";
				break;
			}
			return value;
		}

		@Override
		public long getItemId(int position) 
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getCount() 
		{
			// TODO Auto-generated method stub
			return idList.size();
		}

		@Override
		public Object getItem(int position) 
		{
			// TODO Auto-generated method stub
			return idList.get(position);
		}
	}
	
	
	private class processAsync extends AsyncTask<String, String, String> 
	{
		ConsignmentTransaction consTransaction;
		

		
		
		@Override
		protected void onPreExecute() 
		{
			myProgressDialog = new ProgressDialog(activity);
			myProgressDialog.setMessage("Processing...");
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			myProgressDialog.show();
		}
		
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub

			ordersHandler = new OrdersHandler(activity);
			orderProductsHandler = new OrderProductsHandler(activity);
			consTransDBHandler = new ConsignmentTransactionHandler(activity);
			ProductsHandler prodHandler = new ProductsHandler(activity);
			
			GenerateNewID generator = new GenerateNewID(activity);

			if (Global.lastOrdID.isEmpty()&&ordersHandler.getDBSize() == 0)
				Global.lastOrdID = generator.generate("",0);
			else
			{
				if(Global.lastOrdID.isEmpty())
					Global.lastOrdID = generator.generate(ordersHandler.getLastOrdID(),0);
				else
					Global.lastOrdID = generator.generate(Global.lastOrdID,0);
			}
			
			custInventoryHandler.insertUpdate(Global.custInventoryList);
			consTransaction = new ConsignmentTransaction();
			int size = Global.consignMapKey.size();
			String temp = new String();
			int index = 0;
			double tempQty = 0.0;
			double returnQty = 0,fillupQty = 0;
			
			String consTransID = "";
			double onHandQty = -1;
			
			if(consTransDBHandler.getDBSize()>0)
				consTransID = consTransDBHandler.getLastConsTransID();
			else if(!myPref.getLastConsTransID().isEmpty())
				consTransID = myPref.getLastConsTransID();
			
			
			consTransID = generator.generate(consTransID, 3);
			signatureMap.put("ConsTrans_ID", consTransID);
				
			
			for(int i = 0 ; i < size; i++)
			{
				
				
				consTransaction.getSetData("ConsEmp_ID", false,myPref.getEmpID() );
				consTransaction.getSetData("ConsCust_ID", false, myPref.getCustID());
				consTransaction.getSetData("ConsProd_ID",false, Global.consignMapKey.get(i));
				
				consTransaction.getSetData("ConsTrans_ID", false, consTransID);
				
				temp = Global.consignSummaryMap.get(Global.consignMapKey.get(i)).get("original_qty");
				if(temp==null)
					temp = "0.0";
				consTransaction.getSetData("ConsOriginal_Qty", false, temp);
				
				
				temp = Global.consignSummaryMap.get(Global.consignMapKey.get(i)).get("rack");
				if(temp==null)
					temp = "0.0";
				consTransaction.getSetData("ConsStock_Qty", false, temp);
				
				
				
				temp = Global.consignSummaryMap.get(Global.consignMapKey.get(i)).get("return_index");
				if(temp!=null)
				{
					index = Integer.parseInt(temp);
					temp =  Global.cons_return_products.get(index).getSetData("ordprod_qty", true, null);
					onHandQty = Double.parseDouble(Global.cons_return_products.get(index).getSetData("onHand", true, null));
					returnQty = Double.parseDouble(temp);
					
					consTransaction.getSetData("ConsReturn_Qty", false,temp);
					temp = Global.cons_return_products.get(index).getSetData("ord_id", true, null);
					consTransaction.getSetData("ConsReturn_ID", false,temp);
				}
				
				temp = Global.consignSummaryMap.get(Global.consignMapKey.get(i)).get("fillup_index");
				if(temp!=null)
				{
					index = Integer.parseInt(temp);
					temp =  Global.cons_fillup_products.get(index).getSetData("ordprod_qty", true, null);
					onHandQty = Double.parseDouble(Global.cons_fillup_products.get(index).getSetData("onHand", true, null));
					fillupQty = Double.parseDouble(temp);
					
					consTransaction.getSetData("ConsDispatch_Qty", false,temp);
					temp = Global.cons_fillup_products.get(index).getSetData("ord_id", true, null);
					consTransaction.getSetData("ConsDispatch_ID", false,temp);
					
				}
				tempQty = Double.parseDouble(consTransaction.getSetData("ConsStock_Qty", true, null))+Double.parseDouble(consTransaction.getSetData("ConsDispatch_Qty", true, null));
				consTransaction.getSetData("ConsNew_Qty",false,Double.toString(tempQty));
				
				if(onHandQty!=-1)
				{
					prodHandler.updateProductOnHandQty(Global.consignMapKey.get(i), onHandQty-fillupQty+returnQty);
				}
				
				
				temp = Global.consignSummaryMap.get(Global.consignMapKey.get(i)).get("invoice");
				if(temp!=null)
				{
					tempQty = Double.parseDouble(temp);
					
					consTransaction.getSetData("ConsInvoice_Qty", false, temp);
					consTransaction.getSetData("invoice_total", false, Global.consignSummaryMap.get(Global.consignMapKey.get(i)).get("invoice_total"));
					
					if(tempQty>0)
					{
						ifInvoice = true;
						consTransaction.getSetData("ConsInvoice_ID", false, Global.lastOrdID);
						generateOrder(i,consTransaction);
					}
					
				}
				
				consTransactionList.add(consTransaction);
				consTransaction = new ConsignmentTransaction();
			}
			
			return null;
		}

		
		@Override
		protected void onPostExecute(String unused) 
		{
			myProgressDialog.dismiss();
						
			orientation = getResources().getConfiguration().orientation;
			Intent intent = new Intent(getActivity(), DrawReceiptActivity.class); 
			if (orientation == Configuration.ORIENTATION_PORTRAIT)
				intent.putExtra("inPortrait", true);
			else
				intent.putExtra("inPortrait", false);
			startActivityForResult(intent, Global.S_CONSIGNMENT_TRANSACTION);
		}
		
		
				
		private void generateOrder(int pos,ConsignmentTransaction consTransaction)
		{
			
			OrderProducts ord = new OrderProducts();
			double temp = Double.parseDouble(consTransaction.getSetData("ConsInvoice_Qty", true, null));
						
			// add order to db
			ord.getSetData("ordprod_qty", false, Double.toString(temp));
			ord.getSetData("ordprod_name", false, Global.consignSummaryMap.get(Global.consignMapKey.get(pos)).get("ordprod_name"));
			ord.getSetData("ordprod_desc", false, Global.consignSummaryMap.get(Global.consignMapKey.get(pos)).get("ordprod_desc"));
			ord.getSetData("prod_id", false, Global.consignSummaryMap.get(Global.consignMapKey.get(pos)).get("prod_id"));
			ord.getSetData("overwrite_price", false, Global.consignSummaryMap.get(Global.consignMapKey.get(pos)).get("prod_price"));
			
			
			
			
			
			/*ord.getSetData("onHand", false, extras.getString("prod_on_hand"));
			ord.getSetData("imgURL", false, extras.getString("url"));

			// Still need to do add the appropriate tax/discount value
			ord.getSetData("prod_taxValue", false, taxAmount);
			ord.getSetData("discount_value", false, disAmount);

			
			// for calculating taxes and discount at receipt
			ord.getSetData("prod_taxId", false, prod_taxId);
			ord.getSetData("discount_id", false, discount_id);
			ord.getSetData("taxAmount", false, taxAmount);
			ord.getSetData("taxTotal", false, taxTotal);
			ord.getSetData("disAmount", false, disAmount);
			ord.getSetData("disTotal", false, disTotal);
			
			ord.getSetData("pricelevel_id", false, priceLevelID);
			ord.getSetData("priceLevelName", false, priceLevelName);
			
			ord.getSetData("prod_price", false, Double.toString(Global.formatNumFromLocale(extras.getString("prod_price"))));
			ord.getSetData("tax_position", false, Integer.toString(tax_position));
			ord.getSetData("discount_position", false, Integer.toString(discount_position));
			ord.getSetData("pricelevel_position", false, Integer.toString(pricelevel_position));
			ord.getSetData("uom_position", false, Integer.toString(uom_position));
			
			
			
			//Add UOM attributes to the order
			ord.getSetData("uom_name", false, uomName);
			ord.getSetData("uom_id", false, uomID);
			ord.getSetData("uom_conversion", false, Double.toString(uomMultiplier));*/
			

		
			//OrdersHandler handler = new OrdersHandler(activity);

			

			ord.getSetData("ord_id", false, Global.lastOrdID);


			if (global.orderProducts == null) {
				global.orderProducts = new ArrayList<OrderProducts>();
			}

			UUID uuid = UUID.randomUUID();
			String randomUUIDString = uuid.toString();

			global.orderProducts.add(ord);
			ord.getSetData("ordprod_id", false, randomUUIDString);

			// end of adding to db;
			ordTotal += Double.parseDouble(Global.consignSummaryMap.get(Global.consignMapKey.get(pos)).get("invoice_total"));

		}

	}

	private void finishConsignment()
	{
		if(!signatureMap.get("encoded_signature").isEmpty())
		{
			ConsignmentSignaturesDBHandler signHandler = new ConsignmentSignaturesDBHandler(activity);
			signHandler.insert(signatureMap);
		}
		consTransDBHandler.insert(consTransactionList);
		if(!ifInvoice)
			activity.finish();
		else
			showYesNoPrompt(false,R.string.dlog_title_confirm,R.string.take_payment_now);		
	}
	
	
	private void processOrder()
	{
		//OrdersHandler handler = new OrdersHandler(activity);
		global.order = new Order(activity);

		TaxesHandler taxHandler = new TaxesHandler(activity);
		
		double _order_total = ordTotal;
		if(!myPref.getCustTaxCode().isEmpty())
		{
			String _tax_id = myPref.getCustTaxCode();
			double _tax_rate = Double.parseDouble(taxHandler.getTaxRate(_tax_id, ""));
			global.order.getSetData("tax_id", false, _tax_id);
			double _tax_amount = ordTotal*_tax_rate/100;
			global.order.getSetData("ord_taxamount", false, Double.toString(_tax_amount));
			_order_total+=_tax_amount;
		}
		//double temp =Global.formatNumWithCurrFromLocale(granTotal.getText().toString());
		global.order.getSetData("ord_total", false, Double.toString(_order_total));
		//temp =Global.formatNumWithCurrFromLocale(subTotal.getText().toString());
		global.order.getSetData("ord_subtotal", false, Double.toString(ordTotal));

		ordTotal=_order_total;
		
		global.order.getSetData("ord_id", false, Global.lastOrdID);
		global.order.getSetData("qbord_id", false, Global.lastOrdID.replace("-", ""));
		/*String email = emailHolder;
		if (email == null)
			global.order.getSetData("c_email", false, "");
		else
			global.order.getSetData("c_email", false, email);*/

		global.order.getSetData("cust_id", false, myPref.getCustID());

		global.order.getSetData("ord_type", false, Global.IS_CONSIGNMENT_INVOICE);
		
		//global.order.getSetData("tax_id", false, taxID);
		//global.order.getSetData("ord_discount_id", false, discountID);
		

		global.order.getSetData("total_lines", false, Integer.toString(global.orderProducts.size()));
		global.order.getSetData("ord_signature", false, encodedImage);
		
		//global.order.getSetData("ord_taxamount", false, Double.toString(tax_amount));
		//global.order.getSetData("ord_discount", false, Double.toString(discount_amount));
		
		
		/*global.order.getSetData("ord_shipvia", false, global.getSelectedShippingMethodString());
		global.order.getSetData("ord_delivery", false, global.getSelectedDeliveryDate());
		global.order.getSetData("ord_terms", false, global.getSelectedTermsMethodsString());
		global.order.getSetData("ord_shipto", false, global.getSelectedAddressString());
		global.order.getSetData("ord_comment", false, global.getSelectedComments());
		global.order.getSetData("ord_po", false, global.getSelectedPO());*/
		
		String[] location = Global.getCurrLocation(activity);
		global.order.getSetData("ord_latitude", false, location[0]);
		global.order.getSetData("ord_longitude", false, location[1]);


		//OrderProductsHandler handler2 = new OrderProductsHandler(activity);
		
		
		

		
		ordersHandler.insert(global.order);
		
		orderProductsHandler.insert(global.orderProducts);
	}
	
	
	
	private void showYesNoPrompt(final boolean isPrintPrompt,int title,int msg)
	{
		final Dialog dlog = new Dialog(activity,R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setCancelable(false);
		dlog.setContentView(R.layout.dlog_btn_left_right_layout);
		
		TextView viewTitle = (TextView)dlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)dlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(title);
		viewMsg.setText(msg);
		Button btnYes = (Button)dlog.findViewById(R.id.btnDlogLeft);
		Button btnNo = (Button)dlog.findViewById(R.id.btnDlogRight);
		btnYes.setText(R.string.button_yes);
		btnNo.setText(R.string.button_no);
		
		btnYes.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				if(isPrintPrompt)
				{
					new printAsync().execute("");
				}
				else
				{
					Intent intent = new Intent(getActivity(), RefundMenuActivity.class);
					intent.putExtra("typeOfProcedure",Integer.parseInt(Global.IS_INVOICE));
					intent.putExtra("salesinvoice", true);
					intent.putExtra("ord_subtotal",Double.toString(ordTotal));
					intent.putExtra("ord_taxID", "");
					intent.putExtra("amount",Double.toString(ordTotal));
					intent.putExtra("paid", "0.00");
					intent.putExtra("job_id",Global.lastOrdID );
					intent.putExtra("ord_type", Global.IS_INVOICE);
					
					intent.putExtra("cust_id", myPref.getCustID());
					intent.putExtra("custidkey", myPref.getCustIDKey());
					
					startActivityForResult(intent, 0);
					activity.finish();
				}
			}
		});
		btnNo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				if(isPrintPrompt)
				{
					finishConsignment();
				}
				else
				{
					activity.finish();
				}
			}
		});
		dlog.show();
	}
	
	
	

	private class printAsync extends AsyncTask<String, String, String> 
	{
		boolean printSuccessful = true;
		@Override
		protected void onPreExecute() {
			myProgressDialog = new ProgressDialog(activity);
			myProgressDialog.setMessage("Printing...");
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			myProgressDialog.show();

		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub

			if(Global.mainPrinterManager!=null&&Global.mainPrinterManager.currentDevice!=null)
				printSuccessful = Global.mainPrinterManager.currentDevice.printConsignment(consTransactionList,encodedImage);
			
			return null;
		}

		@Override
		protected void onPostExecute(String unused) {
			myProgressDialog.dismiss();
			if(printSuccessful)
				showYesNoPrompt(true,R.string.dlog_title_confirm,R.string.dlog_msg_want_to_reprint);
			else
				showYesNoPrompt(true,R.string.dlog_title_error,R.string.dlog_msg_failed_print);
		}
	}
	
	
	
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if(resultCode == Global.FROM_DRAW_RECEIPT_PORTRAIT)
		{
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}
		
		encodedImage = global.encodedImage;
		global.encodedImage = new String();
		signatureMap.put("encoded_signature", encodedImage);
		
		if(ifInvoice)
			processOrder();
		
		if(Global.cons_return_products.size()>0)
		{
			Global.cons_return_order.getSetData("ord_signature", false, encodedImage);
			Global.cons_return_order.getSetData("ord_type", false, Global.IS_CONSIGNMENT_RETURN);
			ordersHandler.insert(Global.cons_return_order);
			orderProductsHandler.insert(Global.cons_return_products);
		}
		if(Global.cons_fillup_products.size()>0)
		{
			Global.cons_fillup_order.getSetData("ord_signature", false, encodedImage);
			ordersHandler.insert(Global.cons_fillup_order);
			orderProductsHandler.insert(Global.cons_fillup_products);
		}
		
		
		
		if (myPref.getPreferences(MyPreferences.pref_enable_printing)) {
			if (!myPref.getPreferences(MyPreferences.pref_automatic_printing))
				showYesNoPrompt(true,R.string.dlog_title_confirm,R.string.dlog_msg_want_to_print);
			else
				new printAsync().execute();
		} else
			finishConsignment();
		
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
}
