package com.android.database;

import com.android.emobilepos.models.DinningTable;

import java.util.List;

/**
 * Created by Guarionex on 4/11/2016.
 */
public class DinningTableHandler {
    public static List<DinningTable> dinningTables;

    public static void insert(List<DinningTable> dinningTables) {
//        DBManager._db.beginTransaction();
//        for (DinningTable table : dinningTables) {
////            DBManager._db.insert()
//        }
        DinningTableHandler.dinningTables = dinningTables;
    }

    public static List<DinningTable> getDinningTables() {
        return dinningTables;
    }
}
