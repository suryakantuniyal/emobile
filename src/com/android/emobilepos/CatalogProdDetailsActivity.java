package com.android.emobilepos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import com.android.database.ProductsHandler;
import com.android.database.ProductsImagesHandler;
import com.android.support.Global;
import com.google.analytics.tracking.android.EasyTracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.app.Activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class CatalogProdDetailsActivity extends Activity {
	private List<String> infoTitle;// = Arrays.asList(new String[] { "Name", "Description", "Extra Description", "Product Details Type", "Tax Code","Price", "Discount Type" });
	private List<String> inventTitle;// = Arrays.asList(new String[] { "On Hand" });
	private List<String> identTitle;// = Arrays.asList(new String[] { "Sku", "Category Id", "Upc" });

	private List<String> infoInfo = new ArrayList<String>();
	private List<String> inventInfo = new ArrayList<String>();
	private List<String> identInfo = new ArrayList<String>();
	private List<String> attributes = new ArrayList<String>();
	private Global global;
	private Activity activity;
	private boolean hasBeenCreated = false;

	ListView myListView;
	ListViewAdapter adapter;

	// public ManageImages manager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.catalog_proddetails_layout);
		myListView = (ListView) findViewById(R.id.catalogProdDetailsLV);
		activity = this;
		
		
		infoTitle = Arrays.asList(new String[]{getAString(R.string.catalog_name),getAString(R.string.catalog_description),
				getAString(R.string.cat_details_extra_desc),getAString(R.string.cat_details_prod_det_type),getAString(R.string.cat_details_tax_code),
				getAString(R.string.cat_details_price),getAString(R.string.cat_details_dis_type)
				,(getAString(R.string.redeem)+" Points"),(getAString(R.string.accumulable)+" Points")});
		inventTitle = Arrays.asList(new String[]{getAString(R.string.cat_picker_onhand)});
		identTitle = Arrays.asList(new String[]{getAString(R.string.cat_details_sku),getAString(R.string.cat_details_cat_id),
				getAString(R.string.cat_details_upc)});
		
		adapter = new ListViewAdapter(this);
		final Context thisContext = this;
		global = (Global)getApplication();
		

		Bundle extras = this.getIntent().getExtras();
		final String url = extras.getString("url");
		final String prod_id = extras.getString("prod_id");

		View header = getLayoutInflater().inflate(R.layout.catalog_proddetails_header, (ViewGroup) findViewById(R.id.header_layout_root));
		TextView videoLink = (TextView) header.findViewById(R.id.catalogVideoLink);
		ImageView productImg = (ImageView) header.findViewById(R.id.catalogHeaderImage);

		

		
		/*
		 * productImg.setTag(extras.getString("url")); int width =
		 * getResources().getDisplayMetrics().widthPixels; int height =
		 * getResources().getDisplayMetrics().heightPixels; manager = new
		 * ManageImages(this.getApplicationContext(),10,true,width,height);
		 * manager
		 * .displayImage(extras.getString("url"),productImg,R.drawable.emobile_icon
		 * );
		 */
		ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.init(ImageLoaderConfiguration.createDefault(this));
		DisplayImageOptions options = new DisplayImageOptions.Builder().showStubImage(R.drawable.emobile_icon).cacheInMemory().cacheOnDisc()
				.showImageForEmptyUri(R.drawable.ic_launcher).build();
		imageLoader.displayImage(url, productImg, options);

		ProductsHandler prodHandler = new ProductsHandler(this);

		infoInfo = prodHandler.getProdInformation(prod_id);
		inventInfo = prodHandler.getProdInventory(prod_id);
		identInfo = prodHandler.getProdIdentification(prod_id);
		attributes = Arrays.asList(new String[] { "" });
		ProductsImagesHandler imgHandler = new ProductsImagesHandler(this);
		final String vidLink = imgHandler.getSpecificLink("V", prod_id);
		// myDB.close();

		if (!vidLink.isEmpty()) {
			videoLink.setText("View Video");
			videoLink.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					//Toast.makeText(thisContext, "video link button", Toast.LENGTH_SHORT).show();

					// Intent intent = new
					// Intent(thisContext,ShowProductVideoActivity.class);

					/*
					 * Intent intent = new Intent(Intent.ACTION_VIEW);
					 * intent.setData(Uri.parse(vidLink));
					 * startActivity(intent);
					 */

					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(vidLink)));

				}
			});
		} else {
			videoLink.setText("No Video");
		}

		productImg.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Toast.makeText(thisContext, "image button", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(thisContext, ShowProductImageActivity2.class);
				intent.putExtra("url", url);
				startActivity(intent);
			}
		});
		

		myListView.addHeaderView(header);

		myListView.setAdapter(adapter);
		hasBeenCreated = true;

	}

	/*
	 * public String getVideoRTSP(String youtubeURL) { String gdata =
	 * "http://gdata.youtube.com/feeds/api/videos?format=5&q="; String link =
	 * null; try { DocumentBuilder documentBuilder =
	 * DocumentBuilderFactory.newInstance().newDocumentBuilder(); String id =
	 * getYoutubeID(youtubeURL); URL url = new URL(gdata+id);
	 * 
	 * HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	 * Document doc = documentBuilder.parse(connection.getInputStream());
	 * Element el = doc.getDocumentElement(); NodeList nl =
	 * el.getElementsByTagName("entry");
	 * 
	 * if(nl!=null&&nl.getLength()>0) { for (int i = 0;i<nl.getLength();i++) {
	 * Element entry = (Element)nl.item(i); Element rsp =
	 * (Element)entry.getElementsByTagName("media:content").item(0); link =
	 * rsp.getAttribute("url"); String t = link;
	 * 
	 * } } } catch (ParserConfigurationException e) { // TODO Auto-generated
	 * catch block e.printStackTrace(); } catch (MalformedURLException e) { //
	 * TODO Auto-generated catch block e.printStackTrace(); } catch (IOException
	 * e) { // TODO Auto-generated catch block e.printStackTrace(); } catch
	 * (SAXException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } String test = link; return link; }
	 * 
	 * public String getYoutubeID(String url) { String id = null; try { String
	 * query = new URL(url).getQuery();
	 * 
	 * if(query!=null) { String[] parameters = query.split("&"); for(String
	 * row:parameters)
	 * 
	 * { String[] param = row.split("="); if(param[0].equals("v")) {
	 * id=param[1]; } } } } catch (MalformedURLException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } return id; }
	 */
	
	
	@Override
	public void onResume() {

		if(global.isApplicationSentToBackground(activity))
			global.loggedIn = false;
		global.stopActivityTransitionTimer();
		
		if(hasBeenCreated&&!global.loggedIn)
		{
			if(global.getGlobalDlog()!=null)
				global.getGlobalDlog().dismiss();
			global.promptForMandatoryLogin(activity);
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
	
	
	
	private String getAString(int id)
	{
		Resources resources = activity.getResources();
		return resources.getString(id);
	}
	
	
	
	public class ListViewAdapter extends BaseAdapter {

		private int size1;
		private int size2;
		private int size3;
		//private int size4;
		LayoutInflater myInflater;
		//AlertDialog.Builder dialog;
		Context thisContext;

		public ListViewAdapter(Context context) {
			thisContext = context;
			myInflater = LayoutInflater.from(context);
			size1 = infoTitle.size();
			size2 = inventTitle.size();
			size3 = identTitle.size();
			//size4 = attributes.size();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return infoTitle.size() + inventTitle.size() + identTitle.size() + 4; // +3
																					// for
																					// the
																					// dividers
																					// and
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
			// TODO Auto-generated method stub
			ViewHolder holder;
			int type = getItemViewType(position);
			final int index = position;
			if (convertView == null) {
				holder = new ViewHolder();
				switch (type) {
				case 0:// dividers
				{
					convertView = myInflater.inflate(R.layout.orddetails_lvdivider_adapter, null);
					holder.leftTitle = (TextView) convertView.findViewById(R.id.orderDivLeft);
					if (position == 0) {
						holder.leftTitle.setText("Information");
					} else if (position == (size1 + 1)) {
						holder.leftTitle.setText("Inventory");
					} else if (position == (size1 + size2 + 2)) {
						holder.leftTitle.setText("Identification");
					} else {
						holder.leftTitle.setText("Attributes");
					}

					break;
				}
				case 1: {
					convertView = myInflater.inflate(R.layout.catalog_proddetails_adapter1, null);
					holder.leftTitle = (TextView) convertView.findViewById(R.id.prodDetailsTitle);
					holder.leftSubtitle = (TextView) convertView.findViewById(R.id.prodDetailsSubtitle);

					holder.leftTitle.setText(infoTitle.get(position - 1));
					holder.leftSubtitle.setText(infoInfo.get(position - 1));
					break;
				}
				case 2: {
					convertView = myInflater.inflate(R.layout.catalog_proddetails_adapter2, null);
					holder.leftTitle = (TextView) convertView.findViewById(R.id.prodDetailsTitle);
					holder.leftSubtitle = (TextView) convertView.findViewById(R.id.prodDetailsSubtitle);
					holder.moreInfoIcon = (ImageView) convertView.findViewById(R.id.prodMoreDetailsIcon);

					holder.leftTitle.setText(infoTitle.get(position - 1));
					holder.leftSubtitle.setText(infoInfo.get(position - 1));

					holder.moreInfoIcon.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							showPrompt(infoTitle.get(index-1), infoInfo.get(index-1));
//							dialog = new AlertDialog.Builder(thisContext);
//							dialog.setTitle(infoTitle.get(index - 1));
//							dialog.setMessage(infoInfo.get(index - 1));
//							dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//
//								@Override
//								public void onClick(DialogInterface dialog, int which) {
//									// TODO Auto-generated
//									// method stub
//									dialog.dismiss();
//								}
//							});
//							dialog.create().show();
						}
					});
					break;
				}
				case 3: {
					convertView = myInflater.inflate(R.layout.catalog_proddetails_adapter3, null);
					holder.leftTitle = (TextView) convertView.findViewById(R.id.prodDetailsLeft);
					holder.rightTitle = (TextView) convertView.findViewById(R.id.prodDetailsRight);
					if (position > 3 && position <= (size1)) {
						holder.leftTitle.setText(infoTitle.get(position - 1));
						holder.rightTitle.setText(infoInfo.get(position - 1));
					} else if (position <= size1 + size2 + 2) {
						holder.leftTitle.setText(inventTitle.get(position - (size1 + 2)));
						holder.rightTitle.setText(inventInfo.get(position - (size1 + 2)));
					} else if (position <= size1 + size2 + size3 + 3) {
						holder.leftTitle.setText(identTitle.get(position - (size1 + size2 + 3)));
						holder.rightTitle.setText(identInfo.get(position - (size1 + size2 + 3)));
					} else {
						holder.leftTitle.setText(attributes.get(position - (size1 + size2 + size3 + 4)));
					}
					break;
				}
				}
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (type == 0) {
				if (position == 0) {
					holder.leftTitle.setText(getAString(R.string.cat_details_info));
				} else if (position == (size1 + 1)) {
					holder.leftTitle.setText(getAString(R.string.cat_details_inventory));
				} else if (position == (size1 + size2 + 2)) {
					holder.leftTitle.setText(getAString(R.string.cat_details_identification));
				} else {
					holder.leftTitle.setText(getAString(R.string.cat_picker_attributes));
				}
			} else if (type == 1) {
				holder.leftTitle.setText(infoTitle.get(position - 1));
				holder.leftSubtitle.setText(infoInfo.get(position - 1));
			} else if (type == 2) {
				holder.leftTitle.setText(infoTitle.get(position - 1));
				holder.leftSubtitle.setText(infoInfo.get(position - 1));

				holder.moreInfoIcon.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						showPrompt(infoTitle.get(index-1), infoInfo.get(index-1));
						
//						dialog = new AlertDialog.Builder(thisContext);
//						dialog.setTitle(infoTitle.get(index - 1));
//						dialog.setMessage(infoInfo.get(index - 1));
//						dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//
//							@Override
//							public void onClick(DialogInterface dialog, int which) {
//								// TODO Auto-generated method
//								// stub
//								dialog.dismiss();
//							}
//						});
//						dialog.create().show();
					}
				});

			} else {
				if (position > 3 && position <= (size1)) {
					holder.leftTitle.setText(infoTitle.get(position - 1));
					holder.rightTitle.setText(infoInfo.get(position - 1));
				} else if (position <= size1 + size2 + 2) {
					holder.leftTitle.setText(inventTitle.get(position - (size1 + 2)));
					holder.rightTitle.setText(inventInfo.get(position - (size1 + 2)));
				} else if (position <= size1 + size2 + size3 + 3) {
					holder.leftTitle.setText(identTitle.get(position - (size1 + size2 + 3)));
					holder.rightTitle.setText(identInfo.get(position - (size1 + size2 + 3)));
				} else {
					holder.leftTitle.setText(attributes.get(position - (size1 + size2 + size3 + 4)));
				}
			}
			return convertView;
		}

		@Override
		public int getItemViewType(int position) {
			if (position == 0 || (position == size1 + 1) || (position == (size1 + size2 + 2)) || (position == (size1 + size2 + size3 + 3))) {

				return 0;
			} else if (position == 1) {
				return 1;
			} else if (position == 2 || position == 3) {
				return 2;
			}
//			} else if ((position > 3 && position <= (size1)) || (position > (size1 + 2)) && (position <= size1 + size2 + 2)
//					|| (position > size1 + size2 + 3) && (position <= size1 + size2 + size3 + 4)) {
//				return 3;
//			}

			return 3;

		}

		@Override
		public int getViewTypeCount() {
			return 5;
		}

		public class ViewHolder {
			TextView leftTitle;
			TextView leftSubtitle;
			TextView rightTitle;
			ImageView moreInfoIcon;
		}
	}
	
	
	public void showPrompt(String title,String msg)
	{
		final Dialog dlog = new Dialog(activity,R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setCancelable(true);
		dlog.setCanceledOnTouchOutside(true);
		dlog.setContentView(R.layout.dlog_btn_single_layout);
		
		TextView viewTitle = (TextView)dlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)dlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(title);
		viewMsg.setText(msg);
		Button btnOk = (Button)dlog.findViewById(R.id.btnDlogSingle);
		btnOk.setText(R.string.button_ok);
		btnOk.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
			}
		});
		dlog.show();
	}

}
