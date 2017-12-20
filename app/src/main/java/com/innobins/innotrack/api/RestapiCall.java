package com.innobins.innotrack.api;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by silence12 on 15/6/17.
 */

public class RestapiCall {
    private static RestapiCall mInstance;
    private static Context mContext;
    private RequestQueue mRequestQueue;

    private RestapiCall(Context context) {
        mContext = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized RestapiCall getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RestapiCall(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);

    }
}
