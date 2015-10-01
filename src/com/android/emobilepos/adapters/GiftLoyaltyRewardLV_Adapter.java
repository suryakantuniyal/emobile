package com.android.emobilepos.adapters;

import com.android.emobilepos.cardmanager.CardManager_FA.GiftCardActions;
import com.emobilepos.app.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GiftLoyaltyRewardLV_Adapter extends BaseAdapter {

	private ViewHolder holder;
	private LayoutInflater inflater;
	private GiftCardActions[] giftCardActions;
	private Activity activity;

	public GiftLoyaltyRewardLV_Adapter(Activity _activity, int type) {
		activity = _activity;
		inflater = LayoutInflater.from(activity);
		if (type == 0)
			giftCardActions = new GiftCardActions[] { GiftCardActions.CASE_ACTIVATE, GiftCardActions.CASE_DEACTIVATE,
					GiftCardActions.CASE_ADD_BALANCE, GiftCardActions.CASE_BALANCE_INQUIRY,
					GiftCardActions.CASE_MANUAL_ADD };
		else
			giftCardActions = new GiftCardActions[] { GiftCardActions.CASE_ACTIVATE,
					GiftCardActions.CASE_BALANCE_INQUIRY, GiftCardActions.CASE_MANUAL_ADD };
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return giftCardActions.length;
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
	public View getView(int pos, View view, ViewGroup arg2) {
		// TODO Auto-generated method stub
		if (view == null) {
			view = inflater.inflate(R.layout.adapter_two_column_with_icon, null);
			holder = new ViewHolder();
			holder.label = (TextView) view.findViewById(R.id.twoColumnRightText);
			holder.icon = (ImageView) view.findViewById(R.id.twoColumnLeftIcon);
			holder.giftCardActions = giftCardActions[pos];
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		holder.label.setText(giftCardActions[pos].getLabelByCode(activity));
		return view;
	}

	public class ViewHolder {
		TextView label;
		ImageView icon;
		public GiftCardActions giftCardActions;
	}

}
