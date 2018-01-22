package drivers.star.utils;

import android.graphics.Bitmap;

import com.starmicronics.starioextension.ICommandBuilder;
import com.starmicronics.starioextension.StarIoExt;

public class PrinterFunctions {

	public final static StarIoExt.Emulation emulation = StarIoExt.Emulation.StarGraphic;

	public enum CorrectionLevelOption {Low, Middle, Q, High}

	public enum Alignment {Left, Center, Right}




	public  static byte[] createCommandsEnglishRasterModeCoupon(int width, ICommandBuilder.BitmapConverterRotation rotation,
			Bitmap bitmap) {


		// Bitmap bitmap = BitmapFactory.decodeResource( resource,
		// R.drawable.amex);

		ICommandBuilder builder = StarIoExt.createCommandBuilder(emulation);

		builder.beginDocument();
		builder.appendBitmap(bitmap, false, width, false, rotation);

//		List<byte[]> listBuf = builder.getBuffer();
//
//		for (byte[] buf : listBuf) {
//			commands.add(buf);
//		}

		// commands.add(0x1b, 0x64, 0x03); // Cut Paper
		builder.endDocument();
		return builder.getCommands();
	}

	public static byte[] createCommandsOpenCashDrawer() {
		byte[] commands = new byte[1];

		commands[0] = 0x07; // BEL

		return commands;
	}

}
