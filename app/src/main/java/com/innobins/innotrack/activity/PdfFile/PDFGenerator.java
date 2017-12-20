package in.gtech.gogeotrack.activity.PdfFile;

import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;

/**
 * Created by surya on 23/11/17.
 */

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public  class PDFGenerator  implements PdfPCellEvent {
    @Override
    public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
        PdfContentByte cb = canvases[PdfPTable.LINECANVAS];
        cb.roundRectangle(position.getLeft()+1.5f,position.getBottom()+1.5f,position.getWidth()-3,
                position.getHeight()-3,4);
        cb.stroke();
        PdfDocument document = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300,300,1).create();
        PdfDocument.Page page = document.startPage(pageInfo);


      /*  cell = new PdfPCell(innerTable);
        PdfPCellEvent roundRectangle;
        cell.setCellEvent(roundRectangle);*/
    }

    /*//create a new Documnet
    PdfDocument document = new PdfDocument();
    //create a Page discription
    PageInfo.Builder pageInfo = new PageInfo.Builder(12,12,1);
    //start a page
    Page page = document.startPage(pageInfo.create());
    //draw something on page
    View content = getContentView();
    content.dr
    private View getContentView() {
    }*/

   /* public static void process(DocumentsContract.Document document, JSONObject json) throws JSONException, DocumentException {
        for (String k : json.keySet()) {
            Object object = json.get(k);
            if (object instanceof JSONArray) {
                JSONArray list = json.getJSONArray(k);
                process(document, list);
            } else if (object instanceof JSONObject) {
                process(document, json.getJSONObject(k));
            } else {
                document.add(new Paragraph(k + " " + json.get(k)));
            }
        }
    }
    public static void process(DocumentsContract.Document document, JSONArray json) throws JSONException, DocumentException {
        for (int x = 0; x < json.length(); x++) {
            Object object = json.get(x);
            if (object instanceof JSONArray) {
                JSONArray list = json.getJSONArray(x);
                process(document, list);
            } else if (object instanceof JSONObject) {
                process(document, json.getJSONObject(x));
            } else {
                document.add(new Paragraph(json.get(x).toString()));
            }
        }
    }
    public static File jsonTopdf(JSONObject json) throws IOException, DocumentException {
        DocumentsContract.Document document = new DocumentsContract.Document(PageSize.A4, 70, 55, 100, 55);
        File file = File.createTempFile("consulta", ".pdf");
        FileOutputStream output = new FileOutputStream(file);
        PdfWriter writer = PdfWriter.getInstance(document, output);
        writer.setEncryption("a".getBytes(), "b".getBytes(), PdfWriter.ALLOW_PRINTING, PdfWriter.STANDARD_ENCRYPTION_128);
        writer.createXmpMetadata();
        writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));
        document.open();
        document.addCreationDate();
        document.addTitle("documento");
        document.newPage();
        process(document, json);
        document.close();
        return file;
    }*/

}

