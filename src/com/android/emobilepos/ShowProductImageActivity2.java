package com.android.emobilepos;



import java.io.File;

import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.TouchImageView;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.Window;


public class ShowProductImageActivity2 extends Activity {
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	
	private boolean hasBeenCreated = false;
	private Global global;
	private Activity activity;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		global = (Global)getApplication();
		activity = this;
		
		final TouchImageView img = new TouchImageView(this);
		Bundle extras = getIntent().getExtras();

		MyPreferences myPref = new MyPreferences(this);

		File cacheDir = new File(myPref.getCacheDir());
		
		if(!cacheDir.exists())
			cacheDir.mkdirs();
		
		ImageLoader.getInstance().destroy();
		imageLoader = ImageLoader.getInstance();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration
				.Builder(this).discCache(new UnlimitedDiscCache(cacheDir)).build();
		
		imageLoader.init(config);
		options = new DisplayImageOptions.Builder().imageScaleType(ImageScaleType.EXACTLY).build();

		String url = extras.getString("url");
		int width = getResources().getDisplayMetrics().widthPixels*2;
		int height = getResources().getDisplayMetrics().heightPixels*2;

		int screenOrientation = this.getResources().getConfiguration().orientation;
		if ((screenOrientation == Configuration.ORIENTATION_PORTRAIT && width > height)
				|| (screenOrientation == Configuration.ORIENTATION_LANDSCAPE && width < height)) {
			int tmp = width;
			width = height;
			height = tmp;
		}
	
		
		//ImageLoaderTest imageLoaderTest = new ImageLoaderTest(activity);
		//img.setImageBitmap(imageLoaderTest.getCachedImage(url,activity));
		//img.setImageBitmap(imageLoader.getImageBitmap(url));
		

		imageLoader.loadImage( url,new SimpleImageLoadingListener() {
		    @Override
		    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		        // Do whatever you want with Bitmap
		    	img.setImageBitmap(loadedImage);
		    	img.setMaxZoom(5f);
		    	setContentView(img);
		    }
		});
		
		
		//img.setMaxZoom(4f);
		//setContentView(img);
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
}
