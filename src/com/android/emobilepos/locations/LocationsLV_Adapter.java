package com.android.emobilepos.locations;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.database.Locations_DB;
import com.emobilepos.app.R;
import com.android.emobilepos.holders.Locations_Holder;

public class LocationsLV_Adapter extends BaseAdapter {

	private Context context;
	List<Locations_Holder> locations;
	LayoutInflater inflater;

	public LocationsLV_Adapter(Context context, List<Locations_Holder> locations) {
		super();
		this.context = context;
		this.locations = locations;
		inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return locations.size();
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

	/**
	 * Return row for each country
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View cellView = convertView;
		Cell cell;
		Locations_Holder location = locations.get(position);

		if (convertView == null) {
			cell = new Cell();
			cellView = inflater.inflate(R.layout.country_picker_adapter, null);
			cell.textView = (TextView) cellView.findViewById(R.id.row_title);
			cellView.setTag(cell);
		} else {
			cell = (Cell) cellView.getTag();
		}

		cell.textView.setText(location.get(Locations_DB.loc_name));
		return cellView;
	}

	/**
	 * Holder for the cell
	 * 
	 */
	static class Cell {
		public TextView textView;
	}

}
