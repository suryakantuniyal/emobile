package com.android.testimgloader;

import java.io.File;

import com.android.support.MyPreferences;

import android.app.Activity;


public class FileCache {
    
    private File cacheDir;
    
    public FileCache(Activity activity){
        //Find the dir to save cached images
    	    	
    	MyPreferences myPref = new MyPreferences(activity);

		cacheDir = new File(myPref.getCacheDir());
		
		if(!cacheDir.exists())
			cacheDir.mkdirs();
		
		
    }
    
    
    public File getFile(String url){
        //I identify images by hashcode. Not a perfect solution, good for the demo.
        String filename=String.valueOf(url.hashCode());
        //Another possible solution (thanks to grantland)
        //String filename = URLEncoder.encode(url);
        File f = new File(cacheDir, filename);
        return f;
        
    }
    
    public void clear(){
        File[] files=cacheDir.listFiles();
        if(files==null)
            return;
        for(File f:files)
            f.delete();
    }

}