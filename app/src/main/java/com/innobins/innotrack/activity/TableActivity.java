package com.innobins.innotrack.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.innobins.innotrack.R;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class TableActivity extends AppCompatActivity {
    TableView tableView;
    private static final String[] TABLE_HEADERS = { "latitute", "longitute", "address", "test" };

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);
        tableView.setHeaderAdapter(new SimpleTableHeaderAdapter(this, TABLE_HEADERS));

        tableView = (TableView)findViewById(R.id.tableView);
        tableView.setColumnCount(4);

        TableColumnWeightModel columnModel = new TableColumnWeightModel(4);
        columnModel.setColumnWeight(1, 2);
        columnModel.setColumnWeight(2, 2);
        tableView.setColumnModel(columnModel);
/*
        TableColumnPxWidthModel columnModel1 = new TableColumnPxWidthModel(4, 350);
        columnModel1.setColumnWidth(1, 500);
        columnModel1.setColumnWidth(2, 600);
        tableView.setColumnModel(columnModel1);*/


    }


}

