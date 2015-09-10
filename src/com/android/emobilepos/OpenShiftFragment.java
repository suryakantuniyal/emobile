package com.android.emobilepos;


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.android.database.ClerksHandler;
import com.android.database.ShiftPeriodsDBHandler;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.ShiftPeriods;

import android.app.Activity;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



public class OpenShiftFragment extends Fragment
{
	private ListView lView;

	private Cursor myCursor;
	private Activity activity;
	private int selectedPos = -1;
	private EditText pettyCashField;
	private double pettyCash = 0;
	private MyPreferences myPref;
	
	
	
	 @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		 
		 activity = getActivity();
	        return inflater.inflate(R.layout.open_shift_main_layout, container, false);
	    }
	
	 
	 @Override
	    public void onViewCreated(View view, Bundle savedInstanceState) 
	 {
	        super.onViewCreated(view, savedInstanceState);
	        			
			ClerksHandler ch = new ClerksHandler(activity);
			myPref = new MyPreferences(activity);
			
			myCursor = ch.getAllClerks();
			//adapter = new CustomCursorAdapter(activity, myCursor, CursorAdapter.NO_SELECTION);
			lView = (ListView)view.findViewById(R.id.openShiftListView);
			Button openShiftDoneButton = (Button)view.findViewById(R.id.openShiftDoneButton);
			pettyCashField = (EditText) view.findViewById(R.id.pettyCashAmount);
			
			int i_name = myCursor.getColumnIndex("emp_name");
			int i_id = myCursor.getColumnIndex("_id");
			String[] test = new String[myCursor.getCount()];
			int i = 0;
			if(myCursor.moveToFirst())
			{
				do
				{
					test[i]=myCursor.getString(i_name)+" ("+myCursor.getString(i_id)+")";
					i++;
				}
				while(myCursor.moveToNext());
			}
			
			
			lView.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_multiple_choice, test) {
			    @Override
			    public View getView(int position, View convertView, ViewGroup parent) {
			        TextView textView = (TextView) super.getView(position, convertView, parent);
			        textView.setTextColor(Color.BLACK);
			        return textView;
			    }
			});
			
			
			lView.setItemsCanFocus(false);
			lView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			
			lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int pos, long arg3) {
					// TODO Auto-generated method stub
					selectedPos = pos;
				}
			});
			
			
			
			openShiftDoneButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(selectedPos!=-1)
					{
						myPref.setShiftIsOpen(false);
						myCursor.moveToPosition(selectedPos);
						myPref.setShiftClerkName(myCursor.getString(myCursor.getColumnIndex("emp_name")));
						myPref.setShiftClerkID(myCursor.getString(myCursor.getColumnIndex("_id")));
						
						ShiftPeriods sp = new ShiftPeriods(true);
						myPref.setShiftID(sp.getSetData("shift_id", true, null));
						ShiftPeriodsDBHandler handler = new ShiftPeriodsDBHandler(activity);
						sp.getSetData("assignee_id", false, myCursor.getString(myCursor.getColumnIndex("_id")));
						sp.getSetData("assignee_name", false, myCursor.getString(myCursor.getColumnIndex("emp_name")));
						sp.getSetData("beginning_petty_cash", false, Double.toString(pettyCash));
						sp.getSetData("ending_petty_cash", false, Double.toString(pettyCash));
						sp.getSetData("total_ending_cash", false, Double.toString(pettyCash));
						
						handler.insert(sp);
						
						
						activity.setResult(1);
						activity.finish();
					}
					else
					{
						Toast.makeText(activity, "No Clerks Selected...", Toast.LENGTH_LONG).show();
					}
				}
			});
			
			
			
			this.pettyCashField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
			this.pettyCashField.setText(Global.formatDoubleToCurrency(0));
			this.pettyCashField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					// TODO Auto-generated method stub
					if(v.hasFocus())
					{
						Selection.setSelection(pettyCashField.getText(),pettyCashField.getText().length());
					}
					
				}
			});
			this.pettyCashField.addTextChangedListener(new TextWatcher() 
			{
		        public void afterTextChanged(Editable s) {				
		        }
		        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		        public void onTextChanged(CharSequence s, int start, int before, int count) {parseInputedCurrency(s,R.id.processCardAmount);}
		    });
			
	 }
	
	 
	 
	 
	 private void parseInputedCurrency(CharSequence s,int type)
	 {
	    	DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(Locale.getDefault());
	        DecimalFormatSymbols sym = format.getDecimalFormatSymbols();
	    	StringBuilder sb = new StringBuilder();
	    	sb.append("^\\").append(sym.getCurrencySymbol()).append("\\s(\\d{1,3}(\\").append(sym.getGroupingSeparator()).append("\\d{3})*|(\\d+))(");
	    	sb.append(sym.getDecimalSeparator()).append("\\d{2})?$");
	    	
	        if(!s.toString().matches(sb.toString()))
	        {
	            String userInput= ""+s.toString().replaceAll("[^\\d]", "");
	            StringBuilder cashAmountBuilder = new StringBuilder(userInput);

	            while (cashAmountBuilder.length() > 3 && cashAmountBuilder.charAt(0) == '0') {
	                cashAmountBuilder.deleteCharAt(0);
	            }
	            while (cashAmountBuilder.length() < 3) {
	                cashAmountBuilder.insert(0, '0');
	            }

	            cashAmountBuilder.insert(cashAmountBuilder.length()-2, sym.getDecimalSeparator());
	            cashAmountBuilder.insert(0, sym.getCurrencySymbol()+" ");
	            
	            
	            this.pettyCashField.setText(cashAmountBuilder.toString());
	            pettyCash = (float)(Global.formatNumFromLocale(cashAmountBuilder.toString().replaceAll("[^\\d\\,\\.]", "").trim()));
	            
	            
	        }
	        Selection.setSelection(this.pettyCashField.getText(), this.pettyCashField.getText().length());
	 }
}
