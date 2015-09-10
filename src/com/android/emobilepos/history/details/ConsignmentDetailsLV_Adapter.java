package com.android.emobilepos.history.details;

import java.io.File;
import java.util.HashMap;

import com.emobilepos.app.R;
import com.android.support.MyPreferences;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import android.app.Activity;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ConsignmentDetailsLV_Adapter extends BaseAdapter implements StickyListHeadersAdapter {
	private LayoutInflater inflater;
	private HashMap<String, String> map;
	private Cursor c;
	private int listSize = 0;
	private final int TYPE_SUMMARY = 0, TYPE_ITEMS = 1;
	private int viewType = 0;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	private boolean isPickup;

	public ConsignmentDetailsLV_Adapter(Activity activity, Cursor c, HashMap<String, String> _map, boolean _isPickup) {
		isPickup = _isPickup;
		inflater = LayoutInflater.from(activity);
		this.map = _map;
		listSize = c.getCount();
		this.c = c;

		MyPreferences myPref = new MyPreferences(activity);
		File cacheDir = new File(myPref.getCacheDir());

		if (!cacheDir.exists())
			cacheDir.mkdirs();
		imageLoader = ImageLoader.getInstance();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(activity).memoryCacheExtraOptions(100, 100)
				.discCache(new UnlimitedDiscCache(cacheDir)).build();

		imageLoader.init(config);
		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading_image).showImageForEmptyUri(R.drawable.no_image)
				.cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).build();

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listSize + 1;// plus 1 to include the summary section
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0)
			return TYPE_SUMMARY;
		else
			return TYPE_ITEMS;

	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View getView(int pos, View view, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;

		viewType = getItemViewType(pos);
		if (view == null) {
			holder = new ViewHolder();

			switch (viewType) {
			case TYPE_SUMMARY:
				view = inflater.inflate(R.layout.consignment_details_lv_row1, parent, false);
				holder.consignment_id = (TextView) view.findViewById(R.id.consID);
				holder.total_sold = (TextView) view.findViewById(R.id.consTotalSold);
				holder.total_return = (TextView) view.findViewById(R.id.consTotalReturned);
				holder.total_disp = (TextView) view.findViewById(R.id.consTotalDispatched);
				holder.total_items = (TextView) view.findViewById(R.id.consTotalItems);
				holder.grand_total = (TextView) view.findViewById(R.id.consGrandTotal);

				break;
			case TYPE_ITEMS:
				c.moveToPosition(pos - 1);
				view = inflater.inflate(R.layout.consignment_details_lv_row2, parent, false);
				holder.prod_name = (TextView) view.findViewById(R.id.consign_details_prod_name);
				holder.ori_qty = (TextView) view.findViewById(R.id.consign_details_orig_qty);
				holder.rack_qty = (TextView) view.findViewById(R.id.consign_details_rack_qty);
				holder.return_qty = (TextView) view.findViewById(R.id.consign_details_return_qty);
				holder.sold_qty = (TextView) view.findViewById(R.id.consign_details_sold_qty);
				holder.disp_qty = (TextView) view.findViewById(R.id.consign_details_dispatch_qty);
				holder.new_qty = (TextView) view.findViewById(R.id.consign_details_new_qty);
				holder.prod_price = (TextView) view.findViewById(R.id.consign_details_prod_price);
				holder.item_subtotal = (TextView) view.findViewById(R.id.consign_details_subtotal);
				holder.credit_memo = (TextView) view.findViewById(R.id.consign_details_credit_memo);
				holder.item_total = (TextView) view.findViewById(R.id.consign_details_total);
				holder.prod_img = (ImageView) view.findViewById(R.id.consign_details_prod_img);

				holder.i_prod_name = c.getColumnIndex("prod_name");
				holder.i_ori_qty = c.getColumnIndex("ConsOriginal_Qty");
				holder.i_rack_qty = c.getColumnIndex("ConsStock_Qty");
				holder.i_return_qty = c.getColumnIndex("ConsReturn_Qty");
				holder.i_sold_qty = c.getColumnIndex("ConsInvoice_Qty");
				holder.i_disp_qty = c.getColumnIndex("ConsDispatch_Qty");
				holder.i_new_qty = c.getColumnIndex("ConsNew_Qty");
				holder.i_prod_price = c.getColumnIndex("price");
				holder.i_item_subtotal = c.getColumnIndex("item_subtotal");
				holder.i_credit_memo = c.getColumnIndex("credit_memo");
				holder.i_item_total = c.getColumnIndex("item_total");
				holder.i_prog_img_url = c.getColumnIndex("prod_img_name");
				break;
			}
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		switch (viewType) {
		case TYPE_SUMMARY:
			holder.total_sold = (TextView) view.findViewById(R.id.consTotalSold);
			holder.total_return = (TextView) view.findViewById(R.id.consTotalReturned);
			holder.total_disp = (TextView) view.findViewById(R.id.consTotalDispatched);
			holder.total_items = (TextView) view.findViewById(R.id.consTotalItems);
			holder.grand_total = (TextView) view.findViewById(R.id.consGrandTotal);

			holder.consignment_id.setText(map.get(holder.key_consignment_id));
			holder.total_sold.setText(map.get(holder.total_items_sold));
			holder.total_return.setText(map.get(holder.total_items_returned));
			holder.total_disp.setText(map.get(holder.total_items_dispatched));
			holder.total_items.setText(map.get(holder.total_line_items));
			holder.grand_total.setText(map.get(holder.total_grand_total));
			break;
		case TYPE_ITEMS:
			c.moveToPosition(pos - 1);

			holder.prod_name.setText(validStr(c.getString(holder.i_prod_name)));
			holder.ori_qty.setText(validStr(c.getString(holder.i_ori_qty)));
			holder.rack_qty.setText(validStr(c.getString(holder.i_rack_qty)));
			holder.return_qty.setText(validStr(c.getString(holder.i_return_qty)));
			holder.sold_qty.setText(validStr(c.getString(holder.i_sold_qty)));
			holder.disp_qty.setText(validStr(c.getString(holder.i_disp_qty)));
			holder.new_qty.setText(validStr(c.getString(holder.i_new_qty)));
			holder.prod_price.setText(validStr(c.getString(holder.i_prod_price)));
			holder.credit_memo.setText(validStr(c.getString(holder.i_credit_memo)));
			if (!isPickup) {
				holder.item_subtotal.setText(c.getString(holder.i_item_subtotal));
				holder.item_total.setText(c.getString(holder.i_item_total));
			} else {
				holder.item_subtotal.setText("0");
				holder.item_total.setText("0");
			}
			imageLoader.displayImage(c.getString(holder.i_prog_img_url), holder.prod_img, options);
			break;
		}

		return view;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		// c.moveToPosition(position);
		HeaderViewHolder holder;
		if (convertView == null) {
			holder = new HeaderViewHolder();
			convertView = inflater.inflate(R.layout.consignment_details_lv_header, parent, false);
			holder.headerTitle = (TextView) convertView.findViewById(R.id.headerTitle);

			convertView.setTag(holder);
		} else {
			holder = (HeaderViewHolder) convertView.getTag();
		}

		switch ((int) getHeaderId(position)) {
		case TYPE_SUMMARY:
			holder.headerTitle.setText("Summary");
			// holder.headerTitle.setBackgroundColor(Color.GREEN);
			break;
		case TYPE_ITEMS:
			holder.headerTitle.setText("Items");
			// holder.headerTitle.setBackgroundColor(Color.BLUE);
			break;
		}

		return convertView;
	}

	private String validStr(String val) {
		return val != null ? val : "";
	}

	@Override
	public long getHeaderId(int position) {
		// TODO Auto-generated method stub

		if (position == 0)
			return TYPE_SUMMARY;
		else
			return TYPE_ITEMS;
	}

	private class HeaderViewHolder {
		TextView headerTitle;

	}

	private class ViewHolder {

		TextView consignment_id, total_sold, total_return, total_disp, total_items, grand_total;
		TextView prod_name, ori_qty, rack_qty, return_qty, sold_qty, disp_qty, new_qty, prod_price, item_subtotal, credit_memo, item_total;
		ImageView prod_img;

		int i_prod_name, i_ori_qty, i_rack_qty, i_return_qty, i_sold_qty, i_disp_qty, i_new_qty, i_prod_price, i_item_subtotal,
				i_credit_memo, i_item_total, i_prog_img_url;

		String total_items_sold = "total_items_sold", total_items_returned = "total_items_returned",
				total_items_dispatched = "total_items_dispatched";
		String total_line_items = "total_line_items", total_grand_total = "total_grand_total", key_consignment_id = "ConsTrans_ID";

	}

}
