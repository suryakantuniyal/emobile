package com.android.support;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.android.emobilepos.R;


public class MyEditText extends EditText{
 
    //The image we are going to use for the Clear button
	private Drawable imgCloseButton = getResources().getDrawable(R.drawable.ic_action_clear);
	private Context context;
	private boolean isForSearching = false;
	private EditText defaultReturnField;
     
    public MyEditText(Context context) {
        super(context);
        this.context = context;
        init();
        
    }
 
    public void setIsForSearching(Activity _activity,EditText returnFieldFocus)
    {
    	this.context = _activity;
    	isForSearching = true;
    	defaultReturnField = returnFieldFocus;
    }
    public MyEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
 
    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    @Override
    public boolean onKeyPreIme (int keyCode, KeyEvent event){
//    	 if (keyCode == KeyEvent.KEYCODE_BACK&&isForSearching&&OrderingMain_FA.invisibleSearchMain!=null) {
    	        // User has pressed Back key. So hide the keyboard
//    	        InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//    	        mgr.hideSoftInputFromWindow(this.getWindowToken(), 0);
//    	        
//    	        if(isForSearching&&OrderingMain_FA.invisibleSearchMain!=null)
//    	        	OrderingMain_FA.invisibleSearchMain.requestFocus();
//    	        // TODO: Hide your view as you do it in your activity
//    	    }
//    	    return false;

    	    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK &&isForSearching&&defaultReturnField!=null) {
    	        // Do your thing.
    	        // User has pressed Back key. So hide the keyboard
    	        InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    	        mgr.hideSoftInputFromWindow(this.getWindowToken(), 0);
    	        
//    	        if(isForSearching&&OrderingMain_FA.invisibleSearchMain!=null)
//    	        	OrderingMain_FA.invisibleSearchMain.requestFocus();
    	        defaultReturnField.requestFocus();
    	        return true;  // So it is not propagated.
    	      }
    	      return super.dispatchKeyEvent(event);
    }
     
    void init() {
         
        // Set bounds of the Clear button so it will look ok
    	this.setBackgroundResource(android.R.drawable.edit_text);
        imgCloseButton.setBounds(0, 0, imgCloseButton.getIntrinsicWidth(), imgCloseButton.getIntrinsicHeight());
 
        // There may be initial text in the field, so we may need to display the  button
        handleClearButton();
 
        //if the Close image is displayed and the user remove his finger from the button, clear it. Otherwise do nothing
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
 
                MyEditText et = MyEditText.this;
 
                if (et.getCompoundDrawables()[2] == null)
                    return false;
                 
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                 
                if (event.getX() > et.getWidth() - et.getPaddingRight() - imgCloseButton.getIntrinsicWidth()) {
                    et.setText("");
                    MyEditText.this.handleClearButton();
                }
                return false;
            }
        });
 
        //if text changes, take care of the button
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
 
                MyEditText.this.handleClearButton();
            }
 
            @Override
            public void afterTextChanged(Editable arg0) {
            }
 
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
    }
     
   /*
    //intercept Typeface change and set it with our custom font
    public void setTypeface(Typeface tf, int style) {
        if (style == Typeface.BOLD) {
            super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Vegur-B 0.602.otf"));
        } else {
            super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Vegur-R 0.602.otf"));
        }
    }*/
     
    void handleClearButton() {
        if (!this.isEnabled()||this.getText().toString().equals(""))
        {
            // add the clear button
            this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], null, this.getCompoundDrawables()[3]);
        }
        else
        {
            //remove clear button
            this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], imgCloseButton, this.getCompoundDrawables()[3]);
        }
    }
}

