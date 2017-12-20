package com.innobins.innotrack.activity.PdfFile;

import android.content.Intent;
import android.net.Uri;
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

import java.io.File;
import java.util.List;

import in.innobins.innotrack.R;
import com.innobins.innotrack.activity.Reports.ReportsActivity;

public class PdfGenerator extends AppCompatActivity {

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
    /*    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        //
        webView = new WebView(this);
        webView = (WebView)findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(true);
        String filename ="http://www.gaplogix.com/images/salary.pdf";
        webView.loadUrl("http://docs.google.com/gview?embedded=true&url=" + filename);
//        setContentView(webView);
        webView.setWebViewClient(new WebViewController());

      /*  webView.setWebViewClient(new WebViewClient(){
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(PdfGenerator.this, description, Toast.LENGTH_SHORT).show();
            }
        });*/

       // setContentView(webView);

        // webView.setWebViewClient(new MyWebViewClient());
        //setContentView(webView);
        // Uri uri = Uri.parse("http://www3.nd.edu/~cpoellab/teaching/cse40816/android_tutorial.pdf");
        Uri uri = Uri.parse("http://www.pdf995.com/samples/pdf.pdf");

        // webView.setWebViewClient(new WebViewClient());
        String pdfUrl = "http://www3.nd.edu/~cpoellab/teaching/cse40816/android_tutorial.pdf";
        //webView.loadUrl(pdfUrl);
/*
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setAllowFileAccess(true);
        webView.loadUrl("http://docs.google.com/gview?embedded=true&url=" +pdfUrl);*/

     /*   Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse( "http://docs.google.com/viewer?url=" + pdfUrl), "text/html");
        startActivity(intent);*/

       /* Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(browserIntent);
        finish();*/


    /*    Document document = new Document();

        PdfPTable table = new PdfPTable(new float[]{2,1,2});
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell("Name");
        table.addCell("Age");
        table.addCell("Location");
        table.setHeaderRows(1);
        PdfPCell[] cells = table.getRow(0).getCells();
        for (int j=0;j<cells.length;j++){
            cells[j].setBackgroundColor(BaseColor.GRAY);

        }
        for (int i=0;i<5;i++){
            table.addCell("Name"+i);
            table.addCell("Age"+i);
            table.addCell("Loation"+i);
            Log.d("tabledata",String.valueOf(table));
        }
        try {
            PdfWriter.getInstance(document,new FileOutputStream("sample.pdf"));
            document.open();
            document.add(table);
            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    *//*    PdfWriter pdfWriter = null;
        DecimalFormat df = new DecimalFormat("0.00");
        try {
            Font font = new Font(Font.FontFamily.TIMES_ROMAN,12,Font.BOLD,new BaseColor(0,0,0));
            Font font1 = new Font(Font.FontFamily.TIMES_ROMAN,12);
           // PdfDocument.PageInfo.Builder pageInfo = new PdfDocument.PageInfo.Builder(12,12,1);
            PdfWriter.getInstance(document,new FileOutputStream("sample1.pdf"));
            document.addAuthor("New Pdf Document");
            document.addCreationDate();
            document.addProducer();
            document.addCreator("First Pdf");
            document.addTitle("Heading");
            document.setPageSize(PageSize.LETTER);
            document.open();
            Paragraph paragraph = new Paragraph("This is first pdf doc");
            Log.d("paragraph",String.valueOf(paragraph));
            document.add(new Paragraph("sample 1: this is my first page"));
            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*//*


       *//* number = new ArrayList<Integer>();
        //create a new document
        PdfDocument document = new PdfDocument();
        //create a page discription
        PdfDocument.PageInfo.Builder pageInfo = new PdfDocument.PageInfo.Builder(12,12,1);
        //start page
        PdfDocument.Page page = document.startPage(pageInfo.create());
        //draw somthing on page
        View content = getContentView();
       // content.draw(page.getCanvas());
        Bitmap b = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        content.draw(c);

        *//**//*endpoint = Environment.getExternalStorageDirectory().getAbsolutePath()+"/ADUREC";
        File dir = new File(endpoint);
        if (!dir.exists())
            dir.mkdirs();

        for (int i=1;i<=10;++i)
            number.add(i);
            Collections.shuffle(number);
        try {
            File file = new File(endpoint,"Document"+number+".pdf");
            FileOutputStream fout = new FileOutputStream(file);
            document.writeTo(fout);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            document.finishPage(page);
        }
        catch (IOException e) {
            e.printStackTrace();
        }*//**//*

//        content.draw(page.getCanvas());
        //finish page
        document.finishPage(page);
*//**//*
        try {
            document.writeTo(getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
*//**//*
        document.close();
    }
    private View getContentView() {
        return null;
    }
    private OutputStream getOutputStream() {
        return null;
    }
    *//**//*    private void fromNetwork(String endpoint) {
            new VigerPDF(this).initFromFile(new OnResultListener() {
                @Override
                public void resultData(Bitmap bitmap) {
                }
                @Override
                public void progressData(int i) {
                }
                @Override
                public void failed(Throwable throwable) {
                }
                @Override
                public void resultData(ArrayList<Bitmap> data) {
                    VigerAdapter adapter = new VigerAdapter(getApplicationContext(),data);
                    viewPager.setAdapter(adapter);
                }
            });
        }
    }*//**//*
*/
    }

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
        Intent intent = new Intent(PdfGenerator.this, ReportsActivity.class);
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
