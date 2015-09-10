package com.android.emobilepos.payment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.emobilepos.app.R;
import com.android.support.Global;
import com.google.analytics.tracking.android.EasyTracker;
import com.iparse.checkcapture.CheckCaptureActivity;

public class CaptureCheck_FA extends FragmentActivity implements OnClickListener{
	
	private ImageView imgFront,imgBack;
	private final int CAPTURE_CHECK_FRONT = 100,CAPTURE_CHECK_BACK = 101;
	
	private Global global;
	private boolean hasBeenCreated = false;
	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.check_capture_layout);
        
        global = (Global)getApplication();
        Button btnSave = (Button)findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);
        
        imgFront = (ImageView)findViewById(R.id.btnCaptureFront);
        imgFront.setOnTouchListener(Global.opaqueImageOnClick());
        imgFront.setOnClickListener(this);
        
        imgBack = (ImageView)findViewById(R.id.btnCaptureBack);
        imgBack.setOnTouchListener(Global.opaqueImageOnClick());
        imgBack.setOnClickListener(this);
        
        if(savedInstanceState!=null)
        {
        	if(Global.imgFrontCheck!=null&&!Global.imgFrontCheck.isEmpty())
        	{
        		imgFront.setImageBitmap(Global.decodeBase64Bitmap(Global.imgFrontCheck));
        	}
        	if(Global.imgBackCheck!=null&&!Global.imgBackCheck.isEmpty())
        		imgBack.setImageBitmap(Global.decodeBase64Bitmap(Global.imgBackCheck));
        }
        
        hasBeenCreated = true;
	}

	
	@Override
	public void onResume() {

		if(global.isApplicationSentToBackground(this))
			global.loggedIn = false;
		global.stopActivityTransitionTimer();
		
		if(hasBeenCreated&&!global.loggedIn)
		{
			if(global.getGlobalDlog()!=null)
				global.getGlobalDlog().dismiss();
			global.promptForMandatoryLogin(this);
		}
		super.onResume();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
		boolean isScreenOn = powerManager.isScreenOn();
		if(!isScreenOn)
			global.loggedIn = false;
		global.startActivityTransitionTimer();
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btnCaptureFront:
			Intent intent_1 = setupIntent();
			intent_1.putExtra(CheckCaptureActivity.kResultFaceKey, "FrontFace");
			startActivityForResult(intent_1, CAPTURE_CHECK_FRONT);
			break;
		case R.id.btnCaptureBack:
			Intent intent_2 = setupIntent();
			intent_2.putExtra(CheckCaptureActivity.kResultFaceKey, "BackFace");
			startActivityForResult(intent_2, CAPTURE_CHECK_BACK);
			break;
		case R.id.btnSave:
			if(validCheckCapture())
			{
				setResult(RESULT_OK);
				finish();
			}
			else
				Global.showPrompt(this, R.string.dlog_title_error, getString(R.string.dlog_msg_check_capture_invalid));
			break;
		}
	}
	
	
	
	private Intent setupIntent()
	{
		Intent intent = new Intent(this, CheckCaptureActivity.class);

		// Specify if we are capturing the front or back of the document

		// Specify the color model of the returned document image
		intent.putExtra(CheckCaptureActivity.kResultImageColor, "BW");
		// Specify if the device's torch should be used or automatic
		intent.putExtra(CheckCaptureActivity.kTorchKey, "OFF");
		// It's possible to add a border for use with Mitek or other backends
		// that don't expect the image to already be deskewed. 10% is good in
		// those cases.
		intent.putExtra(CheckCaptureActivity.kResultPercentBorder, "0");
		// Specify the width of the stored capture image. The height will be
		// scaled to maintain the aspect ratio
		intent.putExtra(CheckCaptureActivity.kDocumentSizeWidth, 900);
		intent.putExtra(CheckCaptureActivity.kResultImageFormat, CheckCaptureActivity.kImageFormatPNG);
		return intent;
	}
	
	
	
	private boolean validCheckCapture()
	{
		if(!Global.imgBackCheck.isEmpty()&&!Global.imgFrontCheck.isEmpty())
			return true;
		return false;
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == CAPTURE_CHECK_FRONT && resultCode == RESULT_OK) {
			// Make sure the request was successful
			// The Intent's extra fields define the result
			
			String imagePath = data.getStringExtra(CheckCaptureActivity.kResultImageKey);
			Bitmap tempBmp = BitmapFactory.decodeFile(imagePath);
			Global.imgFrontCheck = Global.encodeBitmapToBase64(tempBmp);
			imgFront.setImageBitmap(tempBmp);
			// TODO: read the image from path and display or submit to modbile
			// deposit service.
		} else if (requestCode == CAPTURE_CHECK_BACK && resultCode == RESULT_OK) {
			String imagePath = data.getStringExtra(CheckCaptureActivity.kResultImageKey);
			Bitmap tempBmp = BitmapFactory.decodeFile(imagePath);
			Global.imgBackCheck = Global.encodeBitmapToBase64(tempBmp);
			imgBack.setImageBitmap(tempBmp);
		}
	}
	
}
