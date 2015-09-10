package com.android.imageloader;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public interface UrlImageViewCallback {
	void onLoaded(ImageView imageView, Drawable loadedDrawable, String url, boolean loadedFromCache);
}
