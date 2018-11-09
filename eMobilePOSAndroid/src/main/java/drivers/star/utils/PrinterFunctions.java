package drivers.star.utils;

import android.graphics.Bitmap;

import com.starmicronics.starioextension.ICommandBuilder;
import com.starmicronics.starioextension.StarIoExt;

public class PrinterFunctions {

    public enum CorrectionLevelOption {Low, Middle, Q, High}

    public enum Alignment {Left, Center, Right}

    public static byte[] createCommandsEnglishRasterModeCoupon(Bitmap bitmap,
                                                               StarIoExt.Emulation emulation,
                                                               int paperWidth) {

        int logoPosition = (paperWidth - bitmap.getWidth()) / 2;
        ICommandBuilder builder = StarIoExt.createCommandBuilder(emulation);
        builder.beginDocument();
        builder.appendBitmapWithAbsolutePosition(bitmap, false, logoPosition);
        builder.endDocument();
        return builder.getCommands();
    }

    public static byte[] createCommandsOpenCashDrawer() {
        byte[] commands = new byte[1];
        commands[0] = 0x07; // BEL
        return commands;
    }
}