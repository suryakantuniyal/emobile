package drivers.star.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
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
import drivers.star.utils.RasterDocument.RasPageEndMode;
import drivers.star.utils.RasterDocument.RasTopMargin;

import com.starmicronics.starioextension.commandbuilder.Bitmap.SCBBitmapConverter;
import com.emobilepos.app.R;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;
import com.starmicronics.starioextension.commandbuilder.ISCBBuilder;
import com.starmicronics.starioextension.commandbuilder.SCBFactory;

import java.util.ArrayList;
import java.util.Arrays;
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

		// commands.add(0x1b, 0x64, 0x03); // Cut Paper

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

	/**
	 * This function is used to print a Java bitmap directly to the printer.
	 * There are 2 ways a printer can print images: through raster commands or
	 * line mode commands This function uses raster commands to print an image.
	 * Raster is supported on the TSP100 and all Star Thermal POS printers. Line
	 * mode printing is not supported by the TSP100. There is no example of
	 * using this method in this sample.
	 * 
	 * @param context
	 *            - Activity for displaying messages to the user
	 * @param portName
	 *            - Port name to use for communication. This should be (TCP:
	 *            <IPAddress>)
	 * @param portSettings
	 *            - Should be blank
	 * @param source
	 *            - The bitmap to convert to Star Raster data
	 * @param maxWidth
	 *            - The maximum width of the image to print. This is usually the
	 *            page width of the printer. If the image exceeds the maximum
	 *            width then the image is scaled down. The ratio is maintained.
	 */
	public static void PrintBitmap(Context context, String portName, String portSettings, Bitmap source, int maxWidth,
			boolean compressionEnable) {
		ArrayList<Byte> commands = new ArrayList<Byte>();
		Byte[] tempList;

		RasterDocument rasterDoc = new RasterDocument(drivers.star.utils.RasterDocument.RasSpeed.Medium,
				RasPageEndMode.FeedToCutter, RasPageEndMode.FeedToCutter, RasTopMargin.Default, 0,
				0, 0);
		StarBitmap starbitmap = new StarBitmap(source, false, maxWidth);

		byte[] command = rasterDoc.BeginDocumentCommandData();
		tempList = new Byte[command.length];
		CopyArray(command, tempList);
		commands.addAll(Arrays.asList(tempList));

		command = starbitmap.getImageRasterDataForPrinting(compressionEnable);
		tempList = new Byte[command.length];
		CopyArray(command, tempList);
		commands.addAll(Arrays.asList(tempList));

		command = rasterDoc.EndDocumentCommandData();
		tempList = new Byte[command.length];
		CopyArray(command, tempList);
		commands.addAll(Arrays.asList(tempList));

		sendCommand(context, portName, portSettings, commands);
	}

	private static void CopyArray(byte[] srcArray, Byte[] cpyArray) {
		for (int index = 0; index < cpyArray.length; index++) {
			cpyArray[index] = srcArray[index];
		}
	}

	private static void sendCommand(Context context, String portName, String portSettings, ArrayList<Byte> byteList) {
		StarIOPort port = null;
		try {
			/*
			 * using StarIOPort3.1.jar (support USB Port) Android OS Version:
			 * upper 2.2
			 */
			port = StarIOPort.getPort(portName, portSettings, 10000, context);
			/*
			 * using StarIOPort.jar Android OS Version: under 2.1 port =
			 * StarIOPort.getPort(portName, portSettings, 10000);
			 */
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}

			/*
			 * Using Begin / End Checked Block method When sending large amounts
			 * of raster data, adjust the value in the timeout in the
			 * "StarIOPort.getPort" in order to prevent "timeout" of the
			 * "endCheckedBlock method" while a printing.
			 * 
			 * If receipt print is success but timeout error occurs(Show message
			 * which is
			 * "There was no response of the printer within the timeout period."
			 * ), need to change value of timeout more longer in
			 * "StarIOPort.getPort" method. (e.g.) 10000 -> 30000
			 */
			StarPrinterStatus status = port.beginCheckedBlock();

			if (true == status.offline) {
				throw new StarIOPortException("A printer is offline");
			}

			byte[] commandToSendToPrinter = convertFromListByteArrayTobyteArray(byteList);
			port.writePort(commandToSendToPrinter, 0, commandToSendToPrinter.length);

			port.setEndCheckedBlockTimeoutMillis(30000);// Change the timeout
														// time of
														// endCheckedBlock
														// method.
			status = port.endCheckedBlock();

			if (true == status.coverOpen) {
				throw new StarIOPortException("Printer cover is open");
			} else if (true == status.receiptPaperEmpty) {
				throw new StarIOPortException("Receipt paper is empty");
			} else if (true == status.offline) {
				throw new StarIOPortException("Printer is offline");
			}
		} catch (StarIOPortException e) {
			Builder dialog = new AlertDialog.Builder(context);
			dialog.setNegativeButton("Ok", null);
			AlertDialog alert = dialog.create();
			alert.setTitle("Failure");
			alert.setMessage(e.getMessage());
			alert.setCancelable(false);
			alert.show();
		} finally {
			if (port != null) {
				try {
					StarIOPort.releasePort(port);
				} catch (StarIOPortException e) {
				}
			}
		}
	}

	private static byte[] convertFromListByteArrayTobyteArray(List<Byte> ByteArray) {
		byte[] byteArray = new byte[ByteArray.size()];
		for (int index = 0; index < byteArray.length; index++) {
			byteArray[index] = ByteArray.get(index);
		}

		return byteArray;
	}
}
