package com.android.support;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Stack;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class ManageImages {

	private long cacheDuration;
	private DateFormat mDateFormatter;

	public static HashMap<String, SoftReference<Bitmap>> imageMap = new HashMap<String, SoftReference<Bitmap>>();

	// private HashMap<String,Bitmap=""> cache = new
	// HashMap<String,Bitmap="">();

	private File cacheDir;
	private ImageQueue imageQueue = new ImageQueue();
	private Thread imageLoaderThread = new Thread(new ImageQueueManager());
	private boolean isIcon;
	private int screenWidth;
	private int screenHeight;

	public ManageImages(Context context, long _cacheDuration, boolean icon, int w, int h) {

		cacheDuration = _cacheDuration;
		// TODO Make this match the date format of your server
		mDateFormatter = new SimpleDateFormat("EEE',' dd MMM yyyy HH:mm:ss zzz");

		// Make background thread low priority, to avoid affecting UI
		// performance
		imageLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);

		isIcon = icon;
		screenWidth = w;
		screenHeight = h;

		// Find the dir to save cached images

		/*
		 * String sdState = android.os.Environment.getExternalStorageState();
		 * 
		 * if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) { File
		 * sdDir = android.os.Environment.getExternalStorageDirectory();
		 * cacheDir = new File(sdDir,"/download/emobilepos/images");
		 * 
		 * } else { cacheDir = context.getCacheDir(); //cacheDir =
		 * context.getExternalCacheDir(); }
		 * 
		 * if (!cacheDir.exists()) { cacheDir.mkdirs(); }
		 */

		cacheDir = context.getCacheDir();
		if (!cacheDir.exists()) {
			cacheDir.mkdir();
		}

	}

	public void displayImage(String url, ImageView imageView, int defaultDrawableId) {

		if (imageMap.containsKey(url)) {
			imageView.setImageBitmap(imageMap.get(url).get());

		} else {
			queueImage(url, imageView, defaultDrawableId);
			imageView.setImageResource(defaultDrawableId);
		}
	}

	public boolean isInCache(String url) {
		return (imageMap.containsKey(url));
	}

	public void queueImage(String url, ImageView imageView, int defaultDrawableId) {
		// This ImageView might have been used for other images, so we clear
		// the queue of old tasks before starting.
		imageQueue.Clean(imageView);
		ImageRef p = new ImageRef(url, imageView, defaultDrawableId);
		imageView.setImageResource(defaultDrawableId);
		synchronized (imageQueue.imageRefs) {
			imageQueue.imageRefs.push(p);
			imageQueue.imageRefs.notifyAll();
		}

		// Start thread if it's not started yet
		if (imageLoaderThread.getState() == Thread.State.NEW) {
			imageLoaderThread.start();
		}
	}

	public Bitmap getBmp(String url) {
		String filename = String.valueOf(url.hashCode());
		File bitmapFile = new File(cacheDir, filename);
		Bitmap bmp = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(bitmapFile.getPath()), 300, 300, true);
		return bmp;
	}

	public Bitmap getBitmap(String url) {

		try {
			URLConnection openConnection = new URL(url).openConnection();

			String filename = String.valueOf(url.hashCode());

			File bitmapFile = new File(cacheDir, filename);
			Bitmap bitmap = BitmapFactory.decodeFile(bitmapFile.getPath());

			long currentTimeMillis = System.currentTimeMillis();

			// Is the bitmap in our cache?
			if (bitmap != null) {

				// Has it expired?
				long bitmapTimeMillis = bitmapFile.lastModified();
				if ((currentTimeMillis - bitmapTimeMillis) < cacheDuration) {

					return bitmap;
				}

				// Check also if it was modified on the server before
				// downloading it
				// String lastMod =
				// openConnection.getHeaderField("Last-Modified");
				// long lastModTimeMillis =
				// mDateFormatter.parse(lastMod).getTime();

				// if ( lastModTimeMillis <= bitmapTimeMillis ) {

				// Discard the connection and return the cached version

				return bitmap;

				// }
			}

			// Nope, have to download it
			bitmap = BitmapFactory.decodeStream(openConnection.getInputStream());
			// save bitmap to cache for later

			/*
			 * Bitmap bMap = Bitmap.createScaledBitmap(bitmap, 500, 500, true);
			 * writeFile(bMap,bitmapFile); return bMap;
			 */

			writeFile(bitmap, bitmapFile);

			return bitmap;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private void writeFile(Bitmap bmp, File f) {
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(f);

			int scale = bmp.getWidth() / screenWidth;

			if (scale > 2) {
				Bitmap bMap = Bitmap.createScaledBitmap(bmp, 2 * bmp.getWidth() / scale, 2 * bmp.getHeight() / scale, true);
				bMap.compress(Bitmap.CompressFormat.PNG, 5, out);
				// bmp.recycle();
				// bMap.recycle();
			} else {
				/*
				 * Bitmap bMap = Bitmap.createScaledBitmap(bmp,
				 * bmp.getWidth(),bmp.getHeight(), true);
				 * bMap.compress(Bitmap.CompressFormat.PNG, 5,out);
				 */
				bmp.compress(Bitmap.CompressFormat.PNG, 0, out);
				// bmp.recycle();
			}
			/*
			 * Bitmap bMap = Bitmap.createScaledBitmap(bmp, 300, 300, true);
			 * bMap.compress(Bitmap.CompressFormat.PNG, 80,out);
			 */

			// bmp.compress(Bitmap.CompressFormat.PNG, 50, out);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (Exception ex) {
			}
		}
	}

	/** Classes **/

	private class ImageRef {
		public String url;
		public ImageView imageView;
		public int defDrawableId;

		public ImageRef(String u, ImageView i, int defaultDrawableId) {
			url = u;
			imageView = i;
			defDrawableId = defaultDrawableId;
		}
	}

	// stores list of images to download
	private class ImageQueue {
		private Stack<ImageRef> imageRefs = new Stack<ImageRef>();

		// removes all instances of this ImageView
		public void Clean(ImageView view) {

			for (int i = 0; i < imageRefs.size();) {
				if (imageRefs.get(i).imageView == view)
					imageRefs.remove(i);
				else
					++i;
			}
		}
	}

	private class ImageQueueManager implements Runnable {
		@Override
		public void run() {
			try {
				while (true) {
					// Thread waits until there are images in the
					// queue to be retrieved
					if (imageQueue.imageRefs.size() == 0) {
						synchronized (imageQueue.imageRefs) {
							imageQueue.imageRefs.wait();
						}
					}

					// When we have images to be loaded
					if (imageQueue.imageRefs.size() != 0) {
						ImageRef imageToLoad;

						synchronized (imageQueue.imageRefs) {
							imageToLoad = imageQueue.imageRefs.pop();
						}

						Bitmap bmp = getBitmap(imageToLoad.url);
						imageMap.put(imageToLoad.url, new SoftReference<Bitmap>(bmp));
						Object tag = imageToLoad.imageView.getTag();

						// Make sure we have the right view - thread safety
						// defender
						if (tag != null && ((String) tag).equals(imageToLoad.url)) {
							BitmapDisplayer bmpDisplayer = new BitmapDisplayer(bmp, imageToLoad.imageView, imageToLoad.defDrawableId);

							Activity a = (Activity) imageToLoad.imageView.getContext();

							a.runOnUiThread(bmpDisplayer);
						}
					}

					if (Thread.interrupted())
						break;
				}
			} catch (InterruptedException e) {
			}
		}
	}

	// Used to display bitmap in the UI thread
	private class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		ImageView imageView;
		int defDrawableId;

		public BitmapDisplayer(Bitmap b, ImageView i, int defaultDrawableId) {
			bitmap = b;
			imageView = i;
			defDrawableId = defaultDrawableId;
		}

		public void run() {
			if (bitmap != null) {
				if (!isIcon) {
					imageView.setImageBitmap(bitmap);
					// bitmap.recycle();
				} else {
					Bitmap bmp = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
					imageView.setImageBitmap(bmp);
					// bmp.recycle();
				}
			} else
				imageView.setImageResource(defDrawableId);
		}
	}
}
