package com.android.emobilepos;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.PriceLevelHandler;
import com.android.database.ProductsHandler;
import com.android.database.TaxesHandler;

import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Order;
import com.android.support.OrderProducts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class SalesReceiptMenuActivity extends Activity implements OnClickListener {

	private Context context = this;
	private Activity activity = this;
	private EditText custName;
	private EditText globalDiscount, globalTax, subTotal;
	private ListViewAdapter myAdapter;
	private ListView myListView;
	private AlertDialog.Builder dialog;
	private TextView granTotal;
	private final String empstr = "";
	private int caseSelected;
	private boolean custSelected;

	private Spinner taxSpinner, discountSpinner;
	private List<String[]> taxList, discountList;
	private CustomAdapter taxAdapter, discountAdapter;
	private int taxSelected = 0;
	private int discountSelected = 0;

	private double tax_amount = 0;
	private double discount_amount = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_main_layout);
		TextView title = (TextView) findViewById(R.id.add_main_title);
		custName = (EditText) findViewById(R.id.membersField);
		myListView = (ListView) findViewById(R.id.receiptListView);
		granTotal = (TextView) findViewById(R.id.grandTotalValue);
		subTotal = (EditText) findViewById(R.id.subtotalField);
		taxSpinner = (Spinner) findViewById(R.id.globalTaxSpinner);
		globalTax = (EditText) findViewById(R.id.globalTaxField);
		discountSpinner = (Spinner) findViewById(R.id.globalDiscountSpinner);
		globalDiscount = (EditText) findViewById(R.id.globalDiscountField);

		myAdapter = new ListViewAdapter(this);

		final Global global = (Global) getApplication();
		final Bundle extras = this.getIntent().getExtras();
		MyPreferences myPref = new MyPreferences(this);

		global.order = new Order(this);
		custSelected = myPref.isCustSelected();

		if (custSelected) {
			switch (extras.getInt("option_number")) {
			case 0: {
				title.setText("Receipt");
				custName.setText(myPref.getCustName());
				caseSelected = 0;
				break;
			}
			case 1: {
				title.setText("Order");
				custName.setText(myPref.getCustName());
				caseSelected = 1;
				break;
			}
			case 2: {
				title.setText("Return");
				custName.setText(myPref.getCustName());
				caseSelected = 2;
				break;
			}
			case 3: {
				title.setText("Invoice");
				custName.setText(myPref.getCustName());
				caseSelected = 3;
				break;
			}
			case 4: {
				title.setText("Estimate");
				custName.setText(myPref.getCustName());
				caseSelected = 4;
				break;
			}
			}
		} else {
			switch (extras.getInt("option_number")) {
			case 0: {
				title.setText("Receipt");
				caseSelected = 0;
				// custName.setText(prefs.getString("customer_name", ""));
				break;
			}
			case 1: {
				title.setText("Return");
				caseSelected = 2;
				// custName.setText(prefs.getString("customer_name", ""));
				break;
			}
			case 2: //
			{
				title.setText("Receipt"); // Without Customer was chosen in
											// Sales Receipts
				caseSelected = 0;
				setResult(2);
			}
			}
		}
		// title.setText("Receipt");

		Button checkout = (Button) findViewById(R.id.checkoutButton);
		checkout.setOnClickListener(this);

		ImageView plusBut = (ImageView) findViewById(R.id.plusButton);
		plusBut.setOnClickListener(this);

		Button addProd = (Button) findViewById(R.id.addProdButton);
		addProd.setOnClickListener(this);

		/*
		 * Button globDiscBut = (Button)findViewById(R.id.globalDiscountButton);
		 * globDiscBut.setOnClickListener(this);
		 * 
		 * Button globTaxBut = (Button)findViewById(R.id.globalTaxButton);
		 * globTaxBut.setOnClickListener(this);
		 */

		Button templateBut = (Button) findViewById(R.id.templateButton);
		templateBut.setOnClickListener(this);

		Button holdBut = (Button) findViewById(R.id.holdButton);
		holdBut.setOnClickListener(this);

		Button detailsBut = (Button) findViewById(R.id.detailsButton);
		detailsBut.setOnClickListener(this);

		Button signBut = (Button) findViewById(R.id.signButton);
		signBut.setOnClickListener(this);

		myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub
				LayoutInflater myInflater = getLayoutInflater();
				final View dialogLayout = myInflater.inflate(R.layout.picked_item_dialog, null);
				final AlertDialog.Builder builder = new AlertDialog.Builder(arg0.getContext());
				builder.setView(dialogLayout);

				final AlertDialog dialog = builder.create();

				TextView itemName = (TextView) dialogLayout.findViewById(R.id.itemName);
				// itemName.setText(global.cur_orders.get(position).getName());
				itemName.setText(global.orderProducts.get(position).getSetData("ordprod_name", true, empstr));

				Button remove = (Button) dialogLayout.findViewById(R.id.removeButton);
				Button cancel = (Button) dialogLayout.findViewById(R.id.cancelButton);
				final int pos = position;
				remove.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						String quant = global.orderProducts.get(pos).getSetData("ordprod_qty", true, empstr);
						int qty = Integer.parseInt(quant);
						// int qty =
						// Integer.parseInt(global.cur_orders.get(pos).getQty());
						String prodID = global.orderProducts.get(pos).getSetData("prod_id", true, empstr);
						int totalQty = Integer.parseInt(global.qtyCounter.get(prodID));
						int sum = totalQty - qty;
						global.qtyCounter.put(prodID, Integer.toString(sum));
						global.orderProducts.remove(pos);
						myListView.invalidateViews();
						dialog.dismiss();
					}
				});

				cancel.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						dialog.dismiss();

					}
				});

				dialog.show();
			}
		});

		myListView.setAdapter(myAdapter);

		initSpinners();
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.checkoutButton:
			// Toast.makeText(context, "Checkout button pressed",
			// Toast.LENGTH_SHORT).show();
			if (myListView.getCount() == 0) {
				Toast.makeText(context, "No products have been added, try again.", Toast.LENGTH_SHORT).show();
			} else {
				dialog = new AlertDialog.Builder(context);
				dialog.setTitle("Confirm");
				dialog.setMessage("Do you wish to add more products?");
				dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}
				});
				dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.cancel();
						showDlog();
					}
				});
				dialog.create().show();
			}
			break;
		case R.id.plusButton:
			// Toast.makeText(context, "Add button pressed",
			// Toast.LENGTH_SHORT).show();
			intent = new Intent(this, CustomerSelectionMenuActivity.class);
			startActivityForResult(intent, 0);
			break;
		case R.id.addProdButton:
			// Toast.makeText(context, "Add Prod button pressed",
			// Toast.LENGTH_SHORT).show();
			intent = new Intent(this, CatalogMenuFragmentActivity.class);
			startActivityForResult(intent, 0);
			break;
		/*
		 * case R.id.globalDiscountButton: Toast.makeText(context,
		 * "Discount button pressed", Toast.LENGTH_SHORT).show(); break; case
		 * R.id.globalTaxButton: Toast.makeText(context, "Tax button pressed",
		 * Toast.LENGTH_SHORT).show(); break;
		 */
		case R.id.templateButton:
			Toast.makeText(context, "Template button pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.holdButton:
			Toast.makeText(context, "Hold button pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.detailsButton:
			Toast.makeText(context, "Details button pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.signButton:
			// Toast.makeText(context, "Sign button pressed",
			// Toast.LENGTH_SHORT).show();
			intent = new Intent(this, DrawReceiptActivity.class);
			startActivity(intent);
			break;

		}
	}

	public void initSpinners() {
		List<String> taxes = new ArrayList<String>();
		List<String> discount = new ArrayList<String>();

		taxes.add("Global Tax");
		discount.add("Global Discount");

		TaxesHandler handler = new TaxesHandler(activity);
		taxList = handler.getTaxes();
		ProductsHandler handler2 = new ProductsHandler(activity);
		discountList = handler2.getDiscounts();
		int size = taxList.size();
		int size2 = discountList.size();
		for (int i = 0; i < size; i++) {
			taxes.add(taxList.get(i)[0]);
			if (i < size2)
				discount.add(discountList.get(i)[0]);
		}
		taxAdapter = new CustomAdapter(activity, android.R.layout.simple_spinner_item, taxes, taxList, true);
		discountAdapter = new CustomAdapter(activity, android.R.layout.simple_spinner_item, discount, discountList, false);

		taxSpinner.setAdapter(taxAdapter);
		taxSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				// your code here
				taxSelected = position;
				/*
				 * if(taxSelected!=0) { tax_amount =
				 * Double.parseDouble(taxList.get(position-1)[2]); double
				 * subtotal =
				 * Double.parseDouble(subTotal.getText().toString().replace("$",
				 * "")); if(subtotal>0) { double addedTax =
				 * (subtotal*tax_amount)/100; subtotal = subtotal + addedTax;
				 * DecimalFormat frmt = new DecimalFormat("0.00");
				 * granTotal.setText(frmt.format(subtotal)); } }
				 */
				calcTaxDisc();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				// your code here
				taxSelected = 0;
			}
		});

		discountSpinner.setAdapter(discountAdapter);
		discountSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				// your code here
				discountSelected = position;
				/*
				 * if(discountSelected!=0) {
				 * if(discountList.get(position-1)[1].equals("Fixed")) {
				 * discount_amount =
				 * Double.parseDouble(discountList.get(position-1)[2]); double
				 * total =
				 * Double.parseDouble(subTotal.getText().toString().replace("$",
				 * "")); if(total>0) { total = total-discount_amount;
				 * DecimalFormat frmt = new DecimalFormat("0.00");
				 * granTotal.setText("$"+frmt.format(total));
				 * globalDiscount.setText("$"+frmt.format(discount_amount)); } }
				 * else { discount_amount =
				 * Double.parseDouble(discountList.get(position-1)[2]); double
				 * total =
				 * Double.parseDouble(subTotal.getText().toString().replace("$",
				 * "")); if(total>0) { double disc =
				 * (total*discount_amount)/100; total = total - disc;
				 * DecimalFormat frmt = new DecimalFormat("0.00");
				 * granTotal.setText(frmt.format(total)); }
				 * 
				 * } }
				 */
				calcTaxDisc();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				// your code here
				discountSelected = 0;
			}
		});
	}

	public void calcTaxDisc() {
		if (discountSelected != 0) {
			if (discountList.get(discountSelected - 1)[1].equals("Fixed")) {
				discount_amount = Double.parseDouble(discountList.get(discountSelected - 1)[2]);
				String t = subTotal.getText().toString();
				double total = Double.parseDouble(subTotal.getText().toString().replace("$", ""));
				if (total > 0) {
					total = total - discount_amount + tax_amount;
					DecimalFormat frmt = new DecimalFormat("0.00");
					granTotal.setText("$" + frmt.format(total));
					globalDiscount.setText("$" + frmt.format(discount_amount));
				}
			} else {
				discount_amount = Double.parseDouble(discountList.get(discountSelected - 1)[2]);
				double total = Double.parseDouble(subTotal.getText().toString().replace("$", ""));
				if (total > 0) {
					double disc = (total * discount_amount) / 100;
					total = total - disc + tax_amount;
					DecimalFormat frmt = new DecimalFormat("0.00");
					granTotal.setText(frmt.format(total));
				}

			}
		}

		if (taxSelected != 0) {
			tax_amount = Double.parseDouble(taxList.get(taxSelected - 1)[2]);
			double subtotal = Double.parseDouble(subTotal.getText().toString().replace("$", ""));
			if (subtotal > 0) {
				double addedTax = (subtotal * tax_amount) / 100;
				subtotal = subtotal + addedTax - discount_amount;
				DecimalFormat frmt = new DecimalFormat("0.00");
				granTotal.setText(frmt.format(subtotal));
			}
		}
	}

	public void showDlog() {
		LayoutInflater myInflater = getLayoutInflater();
		final View dialogLayout = myInflater.inflate(R.layout.checkout_dialog_layout, null);
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		final Global global = (Global) getApplication();
		builder.setView(dialogLayout);

		final AlertDialog dialog = builder.create();
		final EditText input = (EditText) dialogLayout.findViewById(R.id.emailTxt);
		Button done = (Button) dialogLayout.findViewById(R.id.OKButton);

		done.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();

				MyPreferences myPref = new MyPreferences(activity);

				OrdersHandler handler = new OrdersHandler(activity);
				global.order = new Order(activity);

				global.order.getSetData("ord_total", false, granTotal.getText().toString());
				global.order.getSetData("ord_id", false, global.lastOrdID);
				global.order.getSetData("ord_signature", false, global.encodedImage);
				global.order.getSetData("qbord_id", false, global.lastOrdID.replace("-", empstr));
				String email = input.getText().toString();
				if (email == null)
					global.order.getSetData("c_email", false, "");
				else
					global.order.getSetData("c_email", false, email);
				// String t =myPref.getCustID();
				global.order.getSetData("cust_id", false, myPref.getCustID());
				global.order.getSetData("ord_type", false, Integer.toString(caseSelected));

				handler.insert(global.order);

				OrderProductsHandler handler2 = new OrderProductsHandler(activity);
				handler2.insert(global.orderProducts);
				// global.order = new Order(activity);
				global.orderProducts = new ArrayList<OrderProducts>();

				switch (caseSelected) {
				case 0: // is Sales Receipt
				{
					isSalesReceipt();

					break;
				}
				case 1: {
					if (custSelected) // is Order
						finish();
					// showPrintDlg();
					// isOrder();
					else
						// is Return
						showRefundDlg();
					// isReturn();
					break;
				}
				case 2: {
					if (custSelected)
						showRefundDlg();
					// else
					// showPaymentDlg();
					// isReturn();
					break;
				}
				case 3: // is Invoice
				{
					if (custSelected)
						showPaymentDlg();
					// isInvoice();
					break;
				}
				case 4: {
					if (custSelected)
						finish();
					// isEstimate();
					break;
				}
				}

				/*
				 * Intent intent = new Intent(context,RefundMenuActivity.class);
				 * intent.putExtra("salespayment", true);
				 * intent.putExtra("amount","99.99"); intent.putExtra("paid",
				 * "5.50"); intent.putExtra("is_receipt", true);
				 * intent.putExtra("prodID", global.order.getSetData("ord_id",
				 * true, empstr)); startActivityForResult(intent,0);
				 */
			}
		});
		dialog.show();
	}

	public void isSalesReceipt() {
		Global global = (Global) getApplication();
		Intent intent = new Intent(context, RefundMenuActivity.class);
		intent.putExtra("salespayment", true);
		intent.putExtra("amount", global.order.getSetData("ord_total", true, empstr));
		intent.putExtra("paid", "5.50");
		intent.putExtra("is_receipt", true);
		intent.putExtra("prodID", global.order.getSetData("ord_id", true, empstr));
		startActivityForResult(intent, 0);
	}

	public void showPrintDlg() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
				// Yes button clicked
				{
					dialog.dismiss();
					break;
				}

				case DialogInterface.BUTTON_NEGATIVE:
				// No button clicked
				{
					dialog.dismiss();
					finish();
					break;
				}
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Print?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
	}

	public void showRefundDlg() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
				// Yes button clicked

				{
					Global global = (Global) getApplication();
					Intent intent = new Intent(context, RefundMenuActivity.class);
					intent.putExtra("salesrefund", true);
					intent.putExtra("amount", global.order.getSetData("ord_total", true, empstr));
					intent.putExtra("paid", "0.00");
					intent.putExtra("is_receipt", true);
					intent.putExtra("prodID", global.order.getSetData("ord_id", true, empstr));

					dialog.dismiss();
					startActivityForResult(intent, 0);

					break;
				}
				case DialogInterface.BUTTON_NEGATIVE:
				// No button clicked
				{
					dialog.dismiss();
					finish();
					// showPrintDlg();
					break;
				}
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Do you want to make a refund?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener)
				.show();
	}

	public void showPaymentDlg() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
				// Yes button clicked
				{

					Global global = (Global) getApplication();
					Intent intent = new Intent(context, RefundMenuActivity.class);
					intent.putExtra("salesinvoice", true);

					intent.putExtra("amount", global.order.getSetData("ord_total", true, empstr));
					intent.putExtra("paid", "0.00");
					intent.putExtra("prodID", global.order.getSetData("ord_id", true, empstr));

					dialog.dismiss();
					startActivityForResult(intent, 0);

					break;
				}
				case DialogInterface.BUTTON_NEGATIVE:
				// No button clicked
				{
					dialog.dismiss();
					finish();
					// showPrintDlg();
					break;
				}
				}
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Take payment now?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
	}

	public class ListViewAdapter extends BaseAdapter {
		private Global global = (Global) getApplication();

		private LayoutInflater myInflater;

		public ListViewAdapter(Context context) {

			myInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (global.orderProducts != null) {
				return global.orderProducts.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
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

				convertView = myInflater.inflate(R.layout.product_receipt_adapter, null);
				holder.itemQty = (TextView) convertView.findViewById(R.id.itemQty);
				holder.itemName = (TextView) convertView.findViewById(R.id.itemName);
				holder.itemAmount = (TextView) convertView.findViewById(R.id.itemAmount);
				holder.distQty = (TextView) convertView.findViewById(R.id.distQty);
				holder.distAmount = (TextView) convertView.findViewById(R.id.distAmount);
				holder.taxQty = (TextView) convertView.findViewById(R.id.taxQty);
				holder.taxAmount = (TextView) convertView.findViewById(R.id.taxAmount);
				holder.granTotal = (TextView) convertView.findViewById(R.id.granTotal);

				setHolderValues(holder, position);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
				setHolderValues(holder, position);
			}

			return convertView;
		}

		public void setHolderValues(ViewHolder holder, int position) {
			DecimalFormat frmt = new DecimalFormat("0.00");

			holder.itemQty.setText(global.orderProducts.get(position).getSetData("ordprod_qty", true, empstr));
			holder.itemName.setText(global.orderProducts.get(position).getSetData("ordprod_name", true, empstr));
			holder.itemAmount.setText(global.orderProducts.get(position).getSetData("overwrite_price", true, empstr));

			holder.distQty.setText(global.orderProducts.get(position).getSetData("discount_value", true, empstr));

			String val = holder.itemAmount.getText().toString();
			if (val.isEmpty() || val == null)
				val = "0.00";
			double prodPrice = Double.parseDouble(val);
			int quantity = Integer.parseInt(holder.itemQty.getText().toString());
			double discount = Double.parseDouble(holder.distQty.getText().toString());
			double discountAmount = 0.00;
			if (discount > 0)
				discountAmount = quantity * (prodPrice / discount);

			holder.distAmount.setText(frmt.format(discountAmount));

			holder.taxQty.setText(global.orderProducts.get(position).getSetData("prod_taxValue", true, empstr));
			double taxVal = Double.parseDouble(holder.taxQty.getText().toString());

			// to-do calculate tax
			holder.taxAmount.setText(frmt.format(taxVal));

			holder.granTotal.setText(frmt.format(quantity * prodPrice));

		}

		public class ViewHolder {
			TextView itemQty;
			TextView itemName;
			TextView itemAmount;

			TextView distQty;
			TextView distAmount;

			TextView taxQty;
			TextView taxAmount;

			TextView granTotal;

		}
	}

	public class CustomAdapter extends ArrayAdapter<String> {
		private Activity context;
		List<String> leftData = null;
		List<String[]> rightData = null;
		boolean isTax = false;

		public CustomAdapter(Activity activity, int resource, List<String> left, List<String[]> right, boolean isTax) {
			super(activity, resource, left);
			this.context = activity;
			this.leftData = left;
			this.rightData = right;
			this.isTax = isTax;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);

			// we know that simple_spinner_item has android.R.id.text1 TextView:

			TextView text = (TextView) view.findViewById(android.R.id.text1);
			text.setTextColor(Color.WHITE);// choose your color
			text.setTextSize(12);
			text.setPadding(35, 0, 0, 0);

			return view;

			// return super.getView(position,convertView,parent);
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				row = inflater.inflate(R.layout.spinner_layout, parent, false);
			}

			TextView taxName = (TextView) row.findViewById(R.id.taxName);
			TextView taxValue = (TextView) row.findViewById(R.id.taxValue);
			ImageView checked = (ImageView) row.findViewById(R.id.checkMark);
			checked.setVisibility(View.INVISIBLE);
			taxName.setText(leftData.get(position));
			int type = getItemViewType(position);
			switch (type) {
			case 0: {
				taxValue.setText("");
				break;
			}
			case 1: {
				setValues(taxValue, position);
				checked.setVisibility(View.VISIBLE);
				break;
			}
			case 2: {
				// taxValue.setText(rightData.get(position-1)[2]);
				setValues(taxValue, position);
				break;
			}
			}

			return row;
		}

		public void setValues(TextView taxValue, int position) {
			StringBuilder sb = new StringBuilder();
			if (isTax) {
				sb.append("%").append(rightData.get(position - 1)[2]);
				taxValue.setText(sb.toString());
			} else {
				if (rightData.get(position - 1)[1].equals("Fixed")) {
					DecimalFormat frmt = new DecimalFormat("0.00");
					double value = Double.parseDouble(rightData.get(position - 1)[2]);

					sb.append("$").append(frmt.format(value));
					taxValue.setText(sb.toString());
				} else {
					sb.append("%").append(rightData.get(position - 1)[2]);
					taxValue.setText(sb.toString());
				}
			}
		}

		@Override
		public int getItemViewType(int position) {
			if (position == 0) {
				return 0;
			} else if ((isTax && position == taxSelected) || (!isTax && position == discountSelected)) {
				return 1;
			}
			return 2;

		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == 1) {

			Bundle extras = data.getExtras();
			String newName = extras.getString("customer_name");
			Intent results = new Intent();
			results.putExtra("customer_name", newName);
			custName.setText(newName);
			setResult(1, results);
		}

		if (resultCode == 2)// add item to listview
		{
			final Global global = (Global) getApplication();

			Toast.makeText(this, "item added", Toast.LENGTH_SHORT).show();
			myListView.invalidateViews();

			int size = global.orderProducts.size();
			if (size > 0) {
				double amount = 0.00;
				int qty = 0;
				double prodPrice = 0.00;
				for (int i = 0; i < size; i++) {
					qty = Integer.parseInt(global.orderProducts.get(i).getSetData("ordprod_qty", true, empstr));
					String val = global.orderProducts.get(i).getSetData("overwrite_price", true, empstr);
					if (val.isEmpty() || val == null)
						val = "0.00";
					prodPrice = Double.parseDouble(val);
					amount += (qty * prodPrice);
				}
				DecimalFormat frmt = new DecimalFormat("0.00");
				subTotal.setText(frmt.format(amount));
				granTotal.setText(frmt.format(amount));
				calcTaxDisc();

			}
		}
		if (resultCode == 3) {
			finish();
		}
		if (resultCode == -1) // exit from payment
		{
			finish();
		}
	}

}
