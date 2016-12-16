package com.android.emobilepos.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.emobilepos.R;
import com.android.support.Global;
import com.android.testimgloader.ImageLoaderTest;

import java.util.LinkedHashMap;


public class SectionedGridViewAdapter extends BaseAdapter implements
		View.OnClickListener {

	//private static final String TAG = "SectionedGridViewAdapter";
	//private SparseBooleanArray idAnimations = new SparseBooleanArray();
	private int listItemRowWidth = -1;
	private int gridItemSize = -1;
	//private int listViewHeight = -1;

	private int numberOfChildrenInRow = -1;

	private int[] childrenSpacing = null;

	private int childSpacing = -1;

	private LinkedHashMap<String, Cursor> sectionCursors = null;

	private LinkedHashMap<String, Integer> sectionRowsCount = new LinkedHashMap<String, Integer>();

	private Activity activity = null;

	public static final int VIEW_TYPE_HEADER = 0;

	public static final int VIEW_TYPE_ROW = 1;

	public static final int MIN_SPACING = 10;
	
	private ImageLoaderTest imageLoaderTest;
	
	private final int SELECT_EMPTY = 0, SELECT_CHECKED = 1, SELECT_CROSS = 2;
	
	private final int COLOR_GREEN = Color.rgb(0, 112, 60),COLOR_RED = Color.RED, COLOR_BLACK = Color.BLACK;
	private Global global;
	

	public interface OnGridItemClickListener {
		void onGridItemClicked(String sectionName, int position, View v);
	}

	private OnGridItemClickListener listener = null;

	public SectionedGridViewAdapter(Activity activity,ImageLoaderTest imageLoader,LinkedHashMap<String, Cursor> sectionCursors, int listItemRowSize,int listViewHeight, int gridItemSquareSize) 
	{

		this.sectionCursors = sectionCursors;
		imageLoaderTest = imageLoader;
		this.listItemRowWidth = listItemRowSize;
		this.gridItemSize = gridItemSquareSize;
		global = (Global) activity.getApplication();
		//this.listViewHeight = listViewHeight;

		// griditem size is always less that list item size

		if (gridItemSize > this.listItemRowWidth) {
			throw new IllegalArgumentException(
					"Griditem size cannot be greater that list item row size");
		}
		// calculate items number of items that can fit into row size

		numberOfChildrenInRow = listItemRowWidth / gridItemSize;

		int reminder = listItemRowWidth % gridItemSize;

		if (reminder == 0) {
			numberOfChildrenInRow = numberOfChildrenInRow - 1;
			reminder = gridItemSize;
		}

		int numberOfGaps = 0;
		int toReduce = 0;
		while (childSpacing < MIN_SPACING) {
			numberOfChildrenInRow = numberOfChildrenInRow - toReduce;
			reminder += toReduce * gridItemSize;
			numberOfGaps = numberOfChildrenInRow - 1;
			childSpacing = reminder / numberOfGaps;
			toReduce++;
		}

		int spacingReminder = reminder % numberOfGaps;

		// distribute spacing gap equally first
		childrenSpacing = new int[numberOfGaps];

		for (int i = 0; i < numberOfGaps; i++) {
			childrenSpacing[i] = childSpacing;
		}

		// extra reminder distribute from beginning
		for (int i = 0; i < spacingReminder; i++) {
			childrenSpacing[i]++;
		}

		this.activity = activity;

	}

	@Override
	public int getCount() {

		sectionRowsCount.clear();

		// count is cursors count + sections count
		int sections = sectionCursors.size();

		int count = sections;
		
		// count items in each section
		for (String sectionName : sectionCursors.keySet()) {
			int sectionCount = sectionCursors.get(sectionName).getCount();
			int numberOfRows = sectionCount / numberOfChildrenInRow;
			if (sectionCount % numberOfChildrenInRow != 0) {
				numberOfRows++;
			}

			sectionRowsCount.put(sectionName, numberOfRows);
			count += numberOfRows;
		}

		return count;
	}

	@Override
	public Object getItem(int position) {
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
		View v = null;
		boolean isSectionheader = isSectionHeader(position);

		if (convertView == null) 
		{
			LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			if (isSectionheader) 
			{
				v = inflater.inflate(R.layout.addons_picker_listview_header, null);
			} 
			
			else {
				LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.addons_picker_listview_row, null);
				v = ll;
				ll = (LinearLayout) ll.findViewById(R.id.row_item);
				// add childrenCount to this
				for (int i = 0; i < numberOfChildrenInRow; i++) {
					// add a child
					View child = inflater.inflate(R.layout.addons_picker_listview_item, null);
					ll.addView(child, new LinearLayout.LayoutParams(gridItemSize, gridItemSize));

					if (i < numberOfChildrenInRow - 1) {
						// now add space view
						View spaceItem = new View(activity);
						ll.addView(spaceItem, new LinearLayout.LayoutParams(20, 0));
					}
				}
				
			}

		}
		else 
		{
			v = convertView;
		}

		String sectionID = whichSection(position);

		if (isSectionheader) {
			TextView tv = (TextView) v.findViewById(R.id.section_header_title);
			ImageView sectionImg = (ImageView) v.findViewById(R.id.section_header_image);
			
			int i = Global.productParentAddonsDictionary.get(sectionID);
			
//			tv.setText(Global.productParentAddons.get(i).get("cat_name"));
//			imageLoaderTest.DisplayImage(Global.productParentAddons.get(i).get("url"), sectionImg,true);
		} 
		else 
		{
			LinearLayout ll = (LinearLayout) v;
			ll = (LinearLayout) ll.findViewById(R.id.row_item);
			//View divider = ll.findViewById(R.id.row_item_divider);
			//divider.setVisibility(View.VISIBLE);

			// check if this position corresponds to last row
			boolean isLastRowInSection = isLastRowInSection(position);
			int positionInSection = positionInSection(position);

			Cursor c = sectionCursors.get(sectionID);

			// --
			int cursorStartAt = numberOfChildrenInRow * positionInSection;
			int i_prod_name = c.getColumnIndex("prod_name");
			int i_prod_img_name = c.getColumnIndex("prod_img_name"); 
			int i_master_price = c.getColumnIndex("master_price");

			// set all children visible first
			for (int i = 0; i < 2 * numberOfChildrenInRow - 1; i++) {
				// we need to hide grid item and gap
				View child = ll.getChildAt(i);
				child.setVisibility(View.VISIBLE);

				// leave alternate
				if (i % 2 == 0) {
					// its not gap
					if (c.moveToPosition(cursorStartAt)) {
						//String dataName = c.getString(0);
						TextView productNameView = (TextView) child.findViewById(R.id.data_item_text_top);
						TextView productPriceView = (TextView)child.findViewById(R.id.data_item_text_bottom);
						ImageView itemImage = (ImageView) child.findViewById(R.id.data_item_image);
						ImageView itemIconImage = (ImageView)child.findViewById(R.id.data_item_image_icon);
						
						
						productNameView.setText(c.getString(i_prod_name));
						productPriceView.setText(Global.formatDoubleStrToCurrency(c.getString(i_master_price)));
						
						

						imageLoaderTest.DisplayImage(c.getString(i_prod_img_name), itemImage,true);
						ButtonViewHolder holder = new ButtonViewHolder();
						holder.sectionName = sectionID;
						holder.positionInSection = cursorStartAt;
						holder.parent = child;
						holder.prod_id = c.getString(c.getColumnIndex("_id"));
						itemImage.setTag(holder);
						itemImage.setOnClickListener(this);
						
						
						String[] switchCase = new String[]{"0"};
						
						if(global.addonSelectionType.containsKey(holder.prod_id))
							switchCase = global.addonSelectionType.get(holder.prod_id);
						else
							global.addonSelectionType.put(holder.prod_id,new String[]{Integer.toString(SELECT_EMPTY),holder.sectionName,Integer.toString(holder.positionInSection)});
						
						
						
						switch(Integer.parseInt(switchCase[0]))
						{
							case SELECT_EMPTY:
								productNameView.setBackgroundColor(COLOR_BLACK);
								productPriceView.setBackgroundColor(COLOR_BLACK);
								itemIconImage.setVisibility(View.INVISIBLE);
								break;
							case SELECT_CHECKED:
								productNameView.setBackgroundColor(COLOR_GREEN);
								productPriceView.setBackgroundColor(COLOR_GREEN);
								itemIconImage.setImageResource(R.drawable.check_button_green);
								itemIconImage.setVisibility(View.VISIBLE);
								break;
							case SELECT_CROSS:
								productNameView.setBackgroundColor(COLOR_RED);
								productPriceView.setBackgroundColor(COLOR_RED);
								itemIconImage.setImageResource(R.drawable.cross_button_red);
								itemIconImage.setVisibility(View.VISIBLE);
								break;
						}
						
						
					}

					// set listener on image button

					cursorStartAt++;

				}
			}

			if (isLastRowInSection) 
			{
				//divider.setVisibility(View.INVISIBLE);
				// check how many items needs to be hidden in last row
				int sectionCount = sectionCursors.get(sectionID).getCount();

				int childrenInLastRow = sectionCount % numberOfChildrenInRow;

				if (childrenInLastRow > 0) {
					int gaps = childrenInLastRow - 1;

					for (int i = childrenInLastRow + gaps; i < ll.getChildCount(); i++) {
						// we need to hide grid item and gap
						View child = ll.getChildAt(i);
						child.setVisibility(View.INVISIBLE);
					}

				}
			}

		}

		
		return v;
	}

	private boolean isLastRowInSection(int position) {

		for (String key : sectionCursors.keySet()) {
			int size = sectionRowsCount.get(key) + 1;

			if (position == size - 1)
				return true;

			position -= size;
		}

		return false;
	}

	private boolean isSectionHeader(int position) {

		for (String key : sectionCursors.keySet()) {
			int size = sectionRowsCount.get(key) + 1;

			if (position == 0)
				return true;

			position -= size;
		}

		return false;

	}

	private String whichSection(int position) {

		for (String key : sectionCursors.keySet()) {
			int size = sectionRowsCount.get(key) + 1;

			if (position < size) {
				return key;
			}

			position -= size;
		}

		return null;

	}

	private int positionInSection(int position) {

		for (String key : sectionCursors.keySet()) {
			int size = sectionRowsCount.get(key) + 1;

			if (position < size) {
				return position - 1;
			}

			position -= size;
		}

		return -1;

	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		if (isSectionHeader(position)) {
			return VIEW_TYPE_HEADER;
		}

		return VIEW_TYPE_ROW;
	}

	@Override
	public boolean isEnabled(int position) {
		// if (isSectionHeader(position)) {
		// return false;
		// }
		//
		// return true;

		return false;

	}

	public int gapBetweenChildrenInRow() {
		return childSpacing;
	}
	
	

	public void setListener(OnGridItemClickListener listener) {
		this.listener = listener;
	}
	
	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		ButtonViewHolder holder = (ButtonViewHolder) v.getTag();
		if (this.listener != null) 
		{
			TextView productNameView = (TextView) holder.parent.findViewById(R.id.data_item_text_top);
			TextView productPriceView = (TextView)holder.parent.findViewById(R.id.data_item_text_bottom);
			ImageView itemIconImage = (ImageView) holder.parent.findViewById(R.id.data_item_image_icon);
			
			String[] temp = global.addonSelectionType.get(holder.prod_id);
			
			switch(Integer.parseInt(temp[0]))
			{
			case SELECT_EMPTY:
				productNameView.setBackgroundColor(COLOR_GREEN);
				productPriceView.setBackgroundColor(COLOR_GREEN);
				itemIconImage.setImageResource(R.drawable.check_button_green);
				itemIconImage.setVisibility(View.VISIBLE);
				global.addonSelectionType.put(holder.prod_id, new String[]{Integer.toString(SELECT_CHECKED),holder.sectionName,Integer.toString(holder.positionInSection)});
				break;
			case SELECT_CHECKED:
				productNameView.setBackgroundColor(COLOR_RED);
				productPriceView.setBackgroundColor(COLOR_RED);
				itemIconImage.setImageResource(R.drawable.cross_button_red);
				itemIconImage.setVisibility(View.VISIBLE);
				global.addonSelectionType.put(holder.prod_id, new String[]{Integer.toString(SELECT_CROSS),holder.sectionName,Integer.toString(holder.positionInSection)});
				break;
			case SELECT_CROSS:
				productNameView.setBackgroundColor(COLOR_BLACK);
				productPriceView.setBackgroundColor(COLOR_BLACK);
				itemIconImage.setVisibility(View.INVISIBLE);
				global.addonSelectionType.put(holder.prod_id, new String[]{Integer.toString(SELECT_EMPTY),holder.sectionName,Integer.toString(holder.positionInSection)});
				break;
			}
			this.notifyDataSetChanged();
			
			listener.onGridItemClicked(holder.sectionName,holder.positionInSection, holder.parent);
		}
	}

	public static class ButtonViewHolder {
		String sectionName;
		int positionInSection;
		View parent;
		String prod_id;
	}

	// TODO -- cleaning view and click listners and making sure context aint
	// leaked

}
