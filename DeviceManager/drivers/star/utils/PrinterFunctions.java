package drivers.star.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.starmicronics.starioextension.commandbuilder.Bitmap.SCBBitmapConverter;
import com.emobilepos.app.R;
import com.starmicronics.starioextension.commandbuilder.ISCBBuilder;
import com.starmicronics.starioextension.commandbuilder.SCBFactory;

import java.util.List;

public class PrinterFunctions {

	public static byte[] createCommandsEnglish2inchLineModeReceipt() {
		CommandDataList commands = new CommandDataList();

		commands.add(0x1b, 0x20, 0x00); // ANK Right Space
		commands.add(0x1b, 0x1d, 0x61, 0x01); // Alignment(center)
		commands.add("Star Clothing Boutique\r\n");
		commands.add("123 Star Road\r\nCity, State 12345\r\n\r\n");
		commands.add(0x1b, 0x1d, 0x61, 0x00); // Alignment(left)
		commands.add(0x1b, 0x44, 0x02, 0x10, 0x00); // SetHT
		commands.add("Date: MM/DD/YYYY");
		commands.add(" ");
		commands.add(0x09); // HT
		commands.add(" ");
		commands.add("Time:HH:MM PM\r\n--------------------------------\r\n\r\n");
		commands.add(0x1b, 0x45); // Set Bold
		commands.add("SALE \r\n");
		commands.add(0x1b, 0x46); // Cancel Bold
		commands.add("SKU         Description    Total\r\n");
		commands.add("300678566   PLAIN T-SHIRT  10.99\r\n");
		commands.add("300692003   BLACK DENIM    29.99\r\n");
		commands.add("300651148   BLUE DENIM     29.99\r\n");
		commands.add("300642980   STRIPED DRESS  49.99\r\n");
		commands.add("300638471   BLACK BOOTS    35.99\r\n\r\n");
		commands.add("Subtotal ");
		commands.add(0x09); // HT
		commands.add("          156.95\r\n");
		commands.add("Tax ");
		commands.add(0x09); // HT
		commands.add("            0.00\r\n");
		commands.add("--------------------------------\r\n");
		commands.add("Total");
		commands.add(0x09, 0x1b, 0x69, 0x01, 0x01); // Set DoubleHW
		commands.add("$156.95\r\n");
		commands.add(0x1b, 0x69, 0x00, 0x00); // Cancel DoubleHW
		commands.add("--------------------------------\r\n\r\n");
		commands.add("Charge\r\n159.95\r\n");
		commands.add("Visa XXXX-XXXX-XXXX-0123\r\n\r\n");
		commands.add(0x1b, 0x34); // Set invert
		commands.add("Refunds and Exchanges");
		commands.add(0x1b, 0x35); // Cancel invert
		commands.add("\r\n");
		commands.add("Within ");
		commands.add(0x1b, 0x2d, 0x01); // Set underline
		commands.add("30 days");
		commands.add(0x1b, 0x2d, 0x00); // Cancel underline
		commands.add(" with receipt\r\n");
		commands.add("And tags attached\r\n\r\n");
		commands.add(0x1b, 0x1d, 0x61, 0x01); // Alignment(center)
		commands.add(0x1b, 0x62, 0x06, 0x02, 0x02, 0x20).add("mPOP").add(0x1e); // Code
																				// 128
																				// BarCode
		commands.add("\r\n");
		commands.add(0x1b, 0x64, 0x03); // Cut

		return commands.getByteArray();
	}

	public static byte[] createCommandsEnglish2inchRasterModeReceipt(int width) {
		CommandDataList commands = new CommandDataList();

		String textToPrint = "   Star Clothing Boutique\r\n" + "        123 Star Road\r\n"
				+ "      City, State 12345\r\n" + "\r\n" + "Date:MM/DD/YYYY Time:HH:MM PM\r\n"
				+ "-----------------------------\r\n" + "SALE\r\n" + "SKU       Description   Total\r\n"
				+ "300678566 PLAIN T-SHIRT 10.99\n" + "300692003 BLACK DENIM   29.99\n"
				+ "300651148 BLUE DENIM    29.99\n" + "300642980 STRIPED DRESS 49.99\n"
				+ "30063847  BLACK BOOTS   35.99\n" + "\n" + "Subtotal               156.95\r\n"
				+ "Tax                      0.00\r\n" + "-----------------------------\r\n"
				+ "Total                 $156.95\r\n" + "-----------------------------\r\n" + "\r\n"
				+ "Charge\r\n159.95\r\n" + "Visa XXXX-XXXX-XXXX-0123\r\n" + "Refunds and Exchanges\r\n"
				+ "Within 30 days with receipt\r\n" + "And tags attached\r\n";

		int textSize = 22;
		Typeface typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
		Bitmap bitmap = createBitmapFromText(textToPrint, textSize, width, typeface);

		ISCBBuilder builder = SCBFactory.createBuilder(SCBFactory.Emulation.Star);
		builder.appendBitmap(bitmap, false, width);

		List<byte[]> listBuf = builder.getBuffer();

		for (byte[] buf : listBuf) {
			commands.add(buf);
		}

		commands.add(0x1b, 0x64, 0x03); // Cut Paper

		return commands.getByteArray();
	}

	public static byte[] createCommandsEnglish3inchRasterModeReceipt(int width, boolean bothScale) {
		CommandDataList commands = new CommandDataList();

		String textToPrint = "        Star Clothing Boutique\r\n" + "             123 Star Road\r\n"
				+ "           City, State 12345\r\n" + "\r\n" + "Date: MM/DD/YYYY         Time:HH:MM PM\r\n"
				+ "--------------------------------------\r\n" + "SALE\r\n"
				+ "SKU            Description       Total\r\n" + "300678566      PLAIN T-SHIRT     10.99\n"
				+ "300692003      BLACK DENIM       29.99\n" + "300651148      BLUE DENIM        29.99\n"
				+ "300642980      STRIPED DRESS     49.99\n" + "30063847       BLACK BOOTS       35.99\n" + "\n"
				+ "Subtotal                        156.95\r\n" + "Tax                               0.00\r\n"
				+ "--------------------------------------\r\n" + "Total                          $156.95\r\n"
				+ "--------------------------------------\r\n" + "\r\n" + "Charge\r\n159.95\r\n"
				+ "Visa XXXX-XXXX-XXXX-0123\r\n" + "Refunds and Exchanges\r\n" + "Within 30 days with receipt\r\n"
				+ "And tags attached\r\n";

		int textSize = 24;
		Typeface typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
		Bitmap bitmap = createBitmapFromText(textToPrint, textSize, 576, typeface);

		ISCBBuilder builder = SCBFactory.createBuilder(SCBFactory.Emulation.Star);
		builder.appendBitmap(bitmap, false, width, bothScale);

		List<byte[]> listBuf = builder.getBuffer();

		for (byte[] buf : listBuf) {
			commands.add(buf);
		}

		commands.add(0x1b, 0x64, 0x03); // Cut Paper

		return commands.getByteArray();
	}

	public final static byte[] createCommandsEnglishRasterModeCoupon(int width, SCBBitmapConverter.Rotation rotation,
			Bitmap bitmap) {
		CommandDataList commands = new CommandDataList();

		// Bitmap bitmap = BitmapFactory.decodeResource( resource,
		// R.drawable.amex);

		ISCBBuilder builder = SCBFactory.createBuilder(SCBFactory.Emulation.Star);

		builder.appendBitmap(bitmap, false, width, rotation);

		List<byte[]> listBuf = builder.getBuffer();

		for (byte[] buf : listBuf) {
			commands.add(buf);
		}

		//commands.add(0x1b, 0x64, 0x03); // Cut Paper

		return commands.getByteArray();
	}

	public final static byte[] getCutPaperCommand() {
		byte[] ba = { 0x1b, 0x64, 0x03 };
		return ba;
	}

	public final static byte[] createCommandsOpenCashDrawer() {
		byte[] commands = new byte[1];

		commands[0] = 0x07; // BEL

		return commands;
	}

	public static Bitmap createBitmapFromText(String printText, int textSize, int printWidth, Typeface typeface) {
		Paint paint = new Paint();
		Bitmap bitmap;
		Canvas canvas;

		paint.setTextSize(textSize);
		paint.setTypeface(typeface);

		paint.getTextBounds(printText, 0, printText.length(), new Rect());

		TextPaint textPaint = new TextPaint(paint);
		android.text.StaticLayout staticLayout = new StaticLayout(printText, textPaint, printWidth,
				Layout.Alignment.ALIGN_NORMAL, 1, 0, false);

		// Create bitmap
		bitmap = Bitmap.createBitmap(staticLayout.getWidth(), staticLayout.getHeight(), Bitmap.Config.ARGB_8888);

		// Create canvas
		canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);
		canvas.translate(0, 0);
		staticLayout.draw(canvas);

		return bitmap;
	}
}
