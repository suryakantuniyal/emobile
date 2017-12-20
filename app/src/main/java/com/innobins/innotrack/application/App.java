package com.innobins.innotrack.application;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by silence12 on 20/6/17.
 */

public class App extends Application {

    RequestQueue queue = Volley.newRequestQueue(this);
}
