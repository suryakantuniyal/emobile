package com.android.proxies;

import android.content.Context;

import com.android.emobilepos.R;
import com.android.emobilepos.models.DinningTable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guarionex on 4/7/2016.
 */
public class DinningTablesProxy {
    public static List<DinningTable> getDinningTables(Context context) {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<DinningTable>>() {
        }.getType();
        String json = context.getString(R.string.dinningTables);
        final List<DinningTable> dinningTables = gson.fromJson(json, listType);
        return dinningTables;
    }


}
