package com.android.emobilepos.ordering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.android.database.ProductAddonsHandler;
import com.android.emobilepos.models.OrderProducts;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.emobilepos.app.R;
import com.mobeta.android.dslv.DragSortListView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class ReceiptRestLV_Adapter extends BaseAdapter implements DragSortListView.DropListener {
    
    private final static int SECTION_DIV = 0;
    private final static int SECTION_ITEMS = 1;

  
    public List<Integer>divIndexList = new ArrayList<Integer>();

    private LayoutInflater mInflater;
    private Global global;
    private Activity activity;
    private MyPreferences myPref;

    public ReceiptRestLV_Adapter(Activity activity) {
        super();
        this.activity = activity;
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        divIndexList = Arrays.asList(new Integer[]{0,1});
        global = (Global)activity.getApplication();
        myPref = new MyPreferences(activity);
    }

    @Override
    public void drop(int from, int to) {
        if (from != to) {
        	
            OrderProducts data = global.orderProducts.remove(dataPosition(from));
            shiftDivision(from,to);
            global.orderProducts.add(dataPosition(to), data);
            
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
    	if(global.orderProducts!=null)
    		return global.orderProducts.size()+divIndexList.size();
        return divIndexList.size();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return !divIndexList.contains(position);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public String getItem(int position) {
    	return "";
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (divIndexList.contains(position)) {
            return SECTION_DIV;
        } 
        else {
            return SECTION_ITEMS;
        }
    }

    public int dataPosition(int position) {
    	int size = divIndexList.size();
    	for(int i = size-1 ; i>=0;i--)
    	{
    		if(position>divIndexList.get(i))
    		{
    			return position-(i+1);
    		}
    	}
    	return position;
    }
    
    private void shiftDivision(int from,int to)
    {
    	int size = divIndexList.size();
    	int tempIndex = 0;
    	for(int i = size-1;i>=0;i--)
    	{
    		tempIndex = divIndexList.get(i);
    		if(from<tempIndex&&to>=tempIndex)
    			divIndexList.set(i,tempIndex-1);
    		else if(from>tempIndex&&to<=tempIndex)
    			divIndexList.set(i,tempIndex+1);
    	}
    }
    
    public void updateDivisionPos(int deletedPos)
    {
    	int size = divIndexList.size();
    	int tempIndex = 0;
    	for(int i = size-1;i>=0;i--)
    	{
    		tempIndex = divIndexList.get(i);
    		if(deletedPos<tempIndex)
    			divIndexList.set(i,tempIndex-1);
    	}
    }

    
    
    public Drawable getBGDrawable(int type) {
        Drawable d;
        d = activity.getResources().getDrawable(R.drawable.bg_drag_drop_selector);
        d.setLevel(3000);
        return d;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int type = getItemViewType(position);

		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			switch (type) {
			case SECTION_DIV:
				convertView = mInflater.inflate(R.layout.hist_invoices_lvdivider, parent, false);
				break;
			case SECTION_ITEMS:
				convertView = mInflater.inflate(R.layout.product_receipt_drag_drop, parent, false);
				holder.itemQty = (TextView) convertView.findViewById(R.id.itemQty);
				holder.itemName = (TextView) convertView.findViewById(R.id.itemName);
				holder.itemAmount = (TextView) convertView.findViewById(R.id.itemAmount);
				holder.distQty = (TextView) convertView.findViewById(R.id.distQty);
				holder.distAmount = (TextView) convertView.findViewById(R.id.distAmount);
				holder.granTotal = (TextView) convertView.findViewById(R.id.granTotal);

				holder.addonButton = (Button) convertView.findViewById(R.id.addonButton);
				if (holder.addonButton != null)
					holder.addonButton.setFocusable(false);
				setHolderValues(holder, dataPosition(position));
				break;
			}
			convertView.setTag(holder);
		}
		if (type != SECTION_DIV) {
			// bind data
			holder = (ViewHolder) convertView.getTag();
			setHolderValues(holder, dataPosition(position));
		}

		return convertView;
	}
    
    
    public void setHolderValues(ViewHolder holder, int position) {

		final int pos = position;
		final String tempId = global.orderProducts.get(pos).ordprod_id;
		
		if(!myPref.getPreferences(MyPreferences.pref_restaurant_mode)||(myPref.getPreferences(MyPreferences.pref_restaurant_mode)&&(Global.addonSelectionMap==null||(Global.addonSelectionMap!=null&&!Global.addonSelectionMap.containsKey(tempId)))))
		{
			if(holder.addonButton!=null)
				holder.addonButton.setVisibility(View.INVISIBLE);
		}
		else
		{
			if(holder.addonButton!=null)
			{
				holder.addonButton.setVisibility(View.VISIBLE);
				holder.addonButton.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(activity, PickerAddon_FA.class);
						String prodID = global.orderProducts.get(pos).prod_id;
						global.addonSelectionType = Global.addonSelectionMap.get(tempId);
						
						intent.putExtra("addon_map_key", tempId);
						intent.putExtra("isEditAddon", true);						
						intent.putExtra("prod_id",prodID);
						
						
						ProductAddonsHandler prodAddonsHandler = new ProductAddonsHandler(activity);
						Global.productParentAddons  = prodAddonsHandler.getParentAddons(prodID);
						
						activity.startActivityForResult(intent, 0);
					}
				});
			}
		}
		
		holder.itemQty.setText(global.orderProducts.get(position).ordprod_qty);
		holder.itemName.setText(global.orderProducts.get(position).ordprod_name);
		
		String temp = Global.formatNumToLocale(Double.parseDouble(global.orderProducts.get(position).overwrite_price));
		holder.itemAmount.setText(Global.getCurrencyFormat(temp));
		
		
		holder.distQty.setText(global.orderProducts.get(position).disAmount);
		temp = Global.formatNumToLocale(Double.parseDouble(global.orderProducts.get(position).disTotal));
		holder.distAmount.setText(Global.getCurrencyFormat(temp));

		// to-do calculate tax

		temp = Global.formatNumToLocale(Double.parseDouble(global.orderProducts.get(position).itemTotal));
		holder.granTotal.setText(Global.getCurrencyFormat(temp));

	}

	
	public class ViewHolder 
	{
		TextView itemQty;
		TextView itemName;
		TextView itemAmount;
		TextView distQty;
		TextView distAmount;
		TextView granTotal;
		
		Button addonButton;
	}
}
