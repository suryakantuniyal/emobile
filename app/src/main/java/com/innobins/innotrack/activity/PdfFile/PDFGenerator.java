package com.innobins.innotrack.activity.PdfFile;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.innobins.innotrack.R;
import com.innobins.innotrack.activity.Reports.ReportsActivity;

import java.io.File;
import java.util.List;

public class PDFGenerator extends AppCompatActivity {

    private static String FILE;
    public static MenuItem shareMenuItem;
    String[] invoiceTitlelistArray, invoicepricelistArray, invoiceQNTlistArray, invoiceNOlistArray;
    String invoiceNO = "";
    TextView path_pdf;
    File storagePath;
    String endpoint;
    ViewPager viewPager=null;
    WebView webView;
    private List<Integer> number ;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_generator);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
       // getSupportActionBar().setIcon(R.mipmap.luncher_icon);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(" Report");
        //
    }
/*
    public static class CreateTable {
        public static void main(String[] args) throws FileNotFoundException, DocumentException {
            Document document = new Document();
            PdfPTable table = new PdfPTable(new float[] { 2, 1, 2 });
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell("Name");
            table.addCell("Age");
            table.addCell("Location");
            table.setHeaderRows(1);
            PdfPCell[] cells = table.getRow(0).getCells();
            for (int j=0;j<cells.length;j++){
                cells[j].setBackgroundColor(BaseColor.GRAY);
            }
            for (int i=1;i<5;i++){
                table.addCell("Name:"+i);
                table.addCell("Age:"+i);
                table.addCell("Location:"+i);
            }
            PdfWriter.getInstance(document, new FileOutputStream("sample3.pdf"));
            document.open();
            document.add(table);
            document.close();
            System.out.println("Done");
        }
    }
*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu1, menu);
        shareMenuItem = menu.findItem(R.id.menu_item_share);
/*

        MenuItemCompat.setOnActionExpandListener(shareMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {

            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true;
            }
        });
*/


        //searchMenuItem = menu.findItem(R.id.action_search);
/*
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                return true;
            }
        });
*/

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "AndroidSolved");
                sharingIntent.putExtra(Intent.ACTION_ATTACH_DATA, "Now Learn Android with AndroidSolved clicke here to visit https://androidsolved.wordpress.com/ ");
                startActivity(Intent.createChooser(sharingIntent, "Share via"));

                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(PDFGenerator.this, ReportsActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }


    private class WebViewController extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
