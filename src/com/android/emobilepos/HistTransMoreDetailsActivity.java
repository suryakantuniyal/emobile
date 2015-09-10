package com.android.emobilepos;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class HistTransMoreDetailsActivity extends Activity 
{
	
	private final static List<String> allInfoLeft = Arrays.asList(new String[] { "Total", "Amount Paid", "Tip", "Clerk ID", "Comment", "Comment",
			"Ship Via","Terms","Deliver","e-Mail"});
	private String order_id;
	private List<String[]> orderedProd;
	
	private ListView myListView;
	private HashMap<String, String> map = new HashMap<String, String>();
	private String empStr = "";

	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		
	}

}
