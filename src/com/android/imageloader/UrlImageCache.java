package com.android.imageloader;

import android.graphics.drawable.Drawable;

public final class UrlImageCache extends SoftReferenceHashTable<String, Drawable> implements UrlImageHashTable {
	private static UrlImageCache mInstance = new UrlImageCache();

	public static UrlImageCache getInstance() {
		return mInstance;
	}

	private UrlImageCache() {
	}
}
