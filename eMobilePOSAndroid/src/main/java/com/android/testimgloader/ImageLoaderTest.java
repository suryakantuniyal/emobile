package com.android.testimgloader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.android.emobilepos.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ImageLoaderTest {
    
    MemoryCache memoryCache=new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService; 
    private Activity activity;
    
    
    public ImageLoaderTest(Activity activity){
    	this.activity = activity;
        fileCache=new FileCache(activity);
        executorService=Executors.newFixedThreadPool(5);
    }
    
    @SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public void DisplayImage(String url, ImageView imageView,boolean isLandscape)
    {
    	
        imageViews.put(imageView, url);
        Bitmap bitmap=memoryCache.get(url);
        if(bitmap!=null)
        {
        	Drawable d =new BitmapDrawable(activity.getResources(),bitmap);
        	if(Double.valueOf(android.os.Build.VERSION.SDK_INT)>=16)
        		imageView.setBackgroundDrawable(d);
        	else
        		imageView.setBackgroundDrawable(d);
        }
        else
        {
            queuePhoto(url, imageView);
            if(!isLandscape)
            {
            	imageView.setBackgroundResource(android.R.color.transparent);
            }
            else
            {
            	if(url==null||url.isEmpty())
            	{
            		imageView.setBackgroundResource(R.drawable.no_image);
            	}
            	else
            	{
            		imageView.setBackgroundResource(R.drawable.loading_image);
            	}
            }
        }
    }
        
    
    public Bitmap getCachedImage(String url,Activity activity)
    {
    	Bitmap bitmap = null;
    	if(url==null)
    		bitmap = BitmapFactory.decodeResource(activity.getResources(), android.R.color.transparent);
    	else
    	{
    		bitmap = this.getBitmap(url);
    		if(bitmap == null)
    			bitmap = BitmapFactory.decodeResource(activity.getResources(), android.R.color.transparent);
    	}
    	return bitmap;
    }
    
    
    private void queuePhoto(String url, ImageView imageView)
    {
        PhotoToLoad p=new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }
    
    private Bitmap getBitmap(String url) 
    {
        File f=fileCache.getFile(url);
        
        //from SD cache
        Bitmap b = decodeFile(f);
        if(b!=null)
            return b;
        
        //from web
        try {
            Bitmap bitmap=null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is=conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f);
            return bitmap;
        } catch (Throwable ex){
           ex.printStackTrace();
           if(ex instanceof OutOfMemoryError)
               memoryCache.clear();
           return null;
        }
    }

    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f){
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);
            
            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE=2000;
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            if(width_tmp>REQUIRED_SIZE||height_tmp>REQUIRED_SIZE)
            {
            	scale = (int)Math.pow(2, (int) Math.round(Math.log(REQUIRED_SIZE / 
            	           (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }
            /*while(true){
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }*/
            
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }
    
    //Task for the queue
    private class PhotoToLoad
    {
        public String url;
        public ImageView imageView;
        public PhotoToLoad(String u, ImageView i){
            url=u; 
            imageView=i;
        }
    }
    
    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
        PhotosLoader(PhotoToLoad photoToLoad){
            this.photoToLoad=photoToLoad;
        }
        
        @Override
        public void run() {
            if(imageViewReused(photoToLoad))
                return;
            Bitmap bmp=getBitmap(photoToLoad.url);
            memoryCache.put(photoToLoad.url, bmp);
            if(imageViewReused(photoToLoad))
                return;
            BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
            Activity a=(Activity)photoToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }
    
    boolean imageViewReused(PhotoToLoad photoToLoad){
        String tag=imageViews.get(photoToLoad.imageView);
        return tag == null || !tag.equals(photoToLoad.url);
    }
    
    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        public BitmapDisplayer(Bitmap b, PhotoToLoad p){bitmap=b;photoToLoad=p;}
        @SuppressWarnings("deprecation")
		@SuppressLint("NewApi")
		public void run()
        {
            if(imageViewReused(photoToLoad))
                return;
            if(bitmap!=null)
            {
                Drawable d =new BitmapDrawable(activity.getResources(),bitmap);
            	if(Double.valueOf(android.os.Build.VERSION.SDK_INT)>=16)
            		photoToLoad.imageView.setBackgroundDrawable(d);
            	else
            		photoToLoad.imageView.setBackgroundDrawable(d);
            }
            else
            {
            	photoToLoad.imageView.setBackgroundResource(R.drawable.no_image);
            }
        }
    }

    public void clearCache() {
        memoryCache.clear();
    }

}
