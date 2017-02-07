package com.emobilepos.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.android.emobilepos.R;

public class ShiftsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shifts);
        EditText oneDollarEditText = (EditText) findViewById(R.id.oneDollareditText);
        EditText fiveDollarEditText = (EditText) findViewById(R.id.fiveDollareditText);
        EditText tenDollarEditText = (EditText) findViewById(R.id.tenDollareditText);
        EditText twentyDollarEditText = (EditText) findViewById(R.id.twentyDollareditText);
        EditText fiftyDollarEditText = (EditText) findViewById(R.id.fiftyDollareditText);
        EditText hundredDollarEditText = (EditText) findViewById(R.id.hundredDollareditText);
        EditText oneCentEditText = (EditText) findViewById(R.id.oneCenteditText);
        EditText fiveCentsEditText = (EditText) findViewById(R.id.fiveCentseditText);
        EditText tenCentsEditText = (EditText) findViewById(R.id.tenCentseditText);
        EditText quarterCentsEditText = (EditText) findViewById(R.id.quartesCentseditText);
        Button minusOneDollar = (Button) findViewById(R.id.oneDollarMinusbutton);
        Button minusFiveDollar = (Button) findViewById(R.id.fiveDollarMinusbutton);
        Button minusTenDollar = (Button) findViewById(R.id.tenDollarMinusbutton);
        Button minusTwentyDollar = (Button) findViewById(R.id.twentyDollarMinusbutton);
    }

}
