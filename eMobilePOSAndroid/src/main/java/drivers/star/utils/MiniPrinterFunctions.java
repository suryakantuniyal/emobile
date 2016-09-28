package drivers.star.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;

import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import util.*;
import util.RasterDocument;

public class MiniPrinterFunctions 
{
	enum BarcodeWidth {_125, _250, _375, _500, _625, _750, _875, _1_0}

	enum BarcodeType {code39, ITF, code93, code128}

	private static StarIOPort portForMoreThanOneFunction = null;
	
	public static void AddRange(ArrayList<Byte> array, Byte[] newData)
	{
		for(int index=0; index<newData.length; index++)
		{
			array.add(newData[index]);
		}
	}
	
	/**
	 * This function is not supported by portable printers.
	 * @param context - Activity for displaying messages
	 * @param portName - Port name to use for communication
	 * @param portSettings - The port settings to use
	 */
	public static void OpenCashDrawer(Context context, String portName, String portSettings)
	{
		Builder dialog = new AlertDialog.Builder(context);
		dialog.setNegativeButton("Ok", null);
		AlertDialog alert = dialog.create();
		alert.setTitle("Feature Not Available");
		alert.setMessage("Cash drawer functionality is supported only on POS printer models");
		alert.setCancelable(false);
		alert.show(); 
	}

	/**
	 * This function shows how to get the firmware information of a printer
	 * @param context - Activity for displaying messages to the user
	 * @param portName - Port name to use for communication. This should be (TCP:<IPAddress> or BT:<DeviceName> for bluetooth)
	 * @param portSettings - Should be mini, the port settings mini is used for portable printers
	 */
	public static void CheckFirmwareVersion(Context context, String portName, String portSettings)
	{
		StarIOPort port = null;
		try 
    	{
			/*
				using StarIOPort3.1.jar (support USB Port)
				Android OS Version: upper 2.2
			*/
			port = StarIOPort.getPort(portName, portSettings, 10000, context);
			/* 
				using StarIOPort.jar
				Android OS Version: under 2.1
				port = StarIOPort.getPort(portName, portSettings, 10000);
			*/
			
			//A sleep is used to get time for the socket to completely open
			try
			{
				Thread.sleep(500);
			}
			catch(InterruptedException e) {}
			
			Map<String,String> firmware = port.getFirmwareInformation();
			
			String modelName = firmware.get("ModelName");
			String firmwareVersion = firmware.get("FirmwareVersion");

            String message = "Model Name:" + modelName;
            message += "\nFirmware Version:" + firmwareVersion;
			
			Builder dialog = new AlertDialog.Builder(context);
    		dialog.setNegativeButton("Ok", null);
    		AlertDialog alert = dialog.create();
    		alert.setTitle("Firmware Information");
    		alert.setMessage(message);
    		alert.setCancelable(false);
    		alert.show();
			
		}
    	catch (StarIOPortException e)
    	{
    		Builder dialog = new AlertDialog.Builder(context);
    		dialog.setNegativeButton("Ok", null);
    		AlertDialog alert = dialog.create();
    		alert.setTitle("Failure");
    		alert.setMessage("Failed to connect to printer");
    		alert.setCancelable(false);
    		alert.show();
    	}
		finally
		{
			if(port != null)
			{
				try {
					StarIOPort.releasePort(port);
				} catch (StarIOPortException e) {}
			}
		}
	}
	
	/**
	 * This function shows how to get the status of a printer
	 * @param context - Activity for displaying messages to the user
	 * @param portName - Port name to use for communication. This should be (TCP:<IPAddress> or BT:<DeviceName> for bluetooth)
	 * @param portSettings - Should be mini, the port settings mini is used for portable printers
	 */
	public static void CheckStatus(Context context, String portName, String portSettings)
	{
		StarIOPort port = null;
		try 
    	{
			/*
				using StarIOPort3.1.jar (support USB Port)
				Android OS Version: upper 2.2
			*/
			port = StarIOPort.getPort(portName, portSettings, 10000, context);
			/* 
				using StarIOPort.jar
				Android OS Version: under 2.1
				port = StarIOPort.getPort(portName, portSettings, 10000);
			*/
			
			//A sleep is used to get time for the socket to completely open
			try
			{
				Thread.sleep(500);
			}
			catch(InterruptedException e) {}
			
			StarPrinterStatus status = port.retreiveStatus();
			
			if(status.offline == false)
			{
				Builder dialog = new AlertDialog.Builder(context);
	    		dialog.setNegativeButton("Ok", null);
	    		AlertDialog alert = dialog.create();
	    		alert.setTitle("Printer");
	    		alert.setMessage("Printer is Online");
	    		alert.setCancelable(false);
	    		alert.show();
			}
			else
			{
				String message = "Printer is offline";
				if(status.receiptPaperEmpty == true)
				{
					message += "\nPaper is Empty";
				}
				if(status.coverOpen == true)
				{
					message += "\nCover is Open";
				}
				Builder dialog = new AlertDialog.Builder(context);
	    		dialog.setNegativeButton("Ok", null);
	    		AlertDialog alert = dialog.create();
	    		alert.setTitle("Printer");
	    		alert.setMessage(message);
	    		alert.setCancelable(false);
	    		alert.show();
			}
		}
    	catch (StarIOPortException e)
    	{
    		Builder dialog = new AlertDialog.Builder(context);
    		dialog.setNegativeButton("Ok", null);
    		AlertDialog alert = dialog.create();
    		alert.setTitle("Failure");
    		alert.setMessage("Failed to connect to printer");
    		alert.setCancelable(false);
    		alert.show();
    	}
		finally
		{
			if(port != null)
			{
				try {
					StarIOPort.releasePort(port);
				} catch (StarIOPortException e) {}
			}
		}
	}
	
	/**
	 * This function is used to print any of the barcodes supported by portable printers
	 * This example supports 4 barcode types code39, code93, ITF, code128.  For a complete list of supported barcodes see manual (pg 35).
	 * @param context - Activity for displaying messages to the user
	 * @param portName - Port name to use for communication. This should be (TCP:<IPAddress> or BT:<DeviceName> for bluetooth)
	 * @param portSettings - Should be mini, the port settings mini is used for portable printers
	 * @param height - The height of the barcode, max is 255
	 * @param width - Sets the width of the barcode, value of this should be 1 to 8. See pg 34 of the manual for the definitions of the values.
	 * @param type - The type of barcode to print.  This program supports code39, code93, ITF, code128.
	 * @param barcodeData - The data to print.  The type of characters supported varies.  See pg 35 for a complete list of all support characters
	 */
	public static void PrintBarcode(Context context, String portName, String portSettings, byte height, BarcodeWidth width, BarcodeType type, byte[] barcodeData)
	{
		ArrayList<Byte> commands = new ArrayList<Byte>();
		
		Byte[] height_Commands = new Byte[] {0x1d, 0x68, 0x00};
		height_Commands[2] = height;
		AddRange(commands, height_Commands);
		
		Byte[] width_Commands = new Byte[] {0x1d, 0x77, 0x00};
		switch(width)
		{
		case _125:
			width_Commands[2] = 1;
			break;
		case _250:
			width_Commands[2] = 2;
			break;
		case _375:
			width_Commands[2] = 3;
			break;
		case _500:
			width_Commands[2] = 4;
			break;
		case _625:
			width_Commands[2] = 5;
			break;
		case _750:
			width_Commands[2] = 6;
			break;
		case _875:
			width_Commands[2] = 7;
			break;
		case _1_0:
			width_Commands[2] = 8;
			break;
		}
		AddRange(commands, width_Commands);
		
		Byte[] print_Barcode = new Byte[4 + barcodeData.length + 1];
		print_Barcode[0] = 0x1d;
		print_Barcode[1] = 0x6b;
		switch(type)
		{
		case code39:
			print_Barcode[2] = 69;
			break;
		case ITF:
			print_Barcode[2] = 70;
			break;
		case code93:
			print_Barcode[2] = 72;
			break;
		case code128:
			print_Barcode[2] = 73;
			break;
		}
		print_Barcode[3] = (byte)barcodeData.length;
		for(int index=0; index < barcodeData.length; index++)
		{
			print_Barcode[4 + index] = barcodeData[index];
		}
		
		AddRange(commands, print_Barcode);
		
		AddRange(commands, new Byte[] {0x0a, 0x0a, 0x0a, 0x0a});
		
		sendCommand(context, portName, portSettings, commands);
	}
	
	/**
	 * The function is used to print a QRCode for portable printers
	 * @param context - Activity for displaying messages to the user
	 * @param portName - Port name to use for communication. This should be (TCP:<IPAddress> or BT:<DeviceName> for bluetooth)
	 * @param portSettings - Should be mini, the port settings mini is used for portable printers
	 * @param correctionLevel - The correction level for the QRCode.  This value should be 0x4C, 0x4D, 0x51, or 0x48.  See pg 41 for for definition of values 
	 * @param sizeByECLevel - This specifies the symbol version.  This value should be 1 to 40.  See pg 41 for the definition of the level
	 * @param moduleSize - The module size of the QRCode.  This value should be 1 to 8.
	 * @param barcodeData - The characters to print in the QRCode
	 */
	public static void PrintQrcode(Context context, String portName, String portSettings, PrinterFunctions.CorrectionLevelOption correctionLevel, byte sizeByECLevel, byte moduleSize, byte[] barcodeData)
	{
		ArrayList<Byte> commands = new ArrayList<Byte>();
		
		//The printer supports 3 2d bar code types, this one selects qrcode
		Byte[] selectedBarcodeType = new Byte[] {0x1d, 0x5a, 0x02};
		AddRange(commands, selectedBarcodeType);
		
		//This builds the qrcommand
		Byte[] print2dbarcode = new Byte[7 + barcodeData.length];
		print2dbarcode[0] = 0x1b;
		print2dbarcode[1] = 0x5a;
		print2dbarcode[2] = sizeByECLevel;
		switch(correctionLevel)
		{
		case Low:
			print2dbarcode[3] = 'L';
			break;
		case Middle:
			print2dbarcode[3] = 'M';
			break;
		case Q:
			print2dbarcode[3] = 'Q';
			break;
		case High:
			print2dbarcode[3] = 'H';
			break;
		}
		print2dbarcode[4] = moduleSize;
		print2dbarcode[5] = (byte)(barcodeData.length % 256);
		print2dbarcode[6] = (byte)(barcodeData.length / 256);
		for(int index=0;index<barcodeData.length; index++)
		{
			print2dbarcode[7 + index] = barcodeData[index];
		}
		AddRange(commands, print2dbarcode);
		
		commands.add((byte) 10);
		commands.add((byte) 10);
		commands.add((byte) 10);
		commands.add((byte) 10);
		
		sendCommand(context, portName, portSettings, commands);
	}
	
	/**
	 * This function prints PDF417 barcodes for portable printers
	 * @param context - Activity for displaying messages to the user
	 * @param portName - Port name to use for communication. This should be (TCP:<IPAddress> or BT:<DeviceName> for Bluetooth)
	 * @param portSettings - Should be mini, the port settings mini is used for portable printers
	 * @param width - This is the width of the PDF417 barcode to print.  This is the same width used by the 1D barcodes.  See pg 34 of the command manual.
	 * @param columnNumber - This is the column number of the PDF417 barcode to print.  The value of this should be between 1 and 30.
	 * @param securityLevel - The represents how well the barcode can be restored if damaged.  The value should be between 0 and 8.
	 * @param ratio - The value representing the horizontal and vertical ratio of the barcode.  This value should between 2 and 5.
	 * @param barcodeData - The characters that will be in the barcode
	 */
	public static void PrintPDF417(Context context, String portName, String portSettings, BarcodeWidth width, byte columnNumber, byte securityLevel, byte ratio, byte[] barcodeData)
	{
		ArrayList<Byte> commands = new ArrayList<Byte>();
		
		Byte[] barcodeWidthCommand = new Byte[] {0x1d, 'w', 0x00};
		switch(width)
		{
		case _125:
			barcodeWidthCommand[2] = 1;
			break;
		case _250:
			barcodeWidthCommand[2] = 2;
			break;
		case _375:
			barcodeWidthCommand[2] = 3;
			break;
		case _500:
			barcodeWidthCommand[2] = 4;
			break;
		case _625:
			barcodeWidthCommand[2] = 5;
			break;
		case _750:
			barcodeWidthCommand[2] = 6;
			break;
		case _875:
			barcodeWidthCommand[2] = 7;
			break;
		case _1_0:
			barcodeWidthCommand[2] = 8;
			break;
		}
		
		AddRange(commands, barcodeWidthCommand);
		
		Byte[] setBarcodePDF= new Byte[]{0x1d, 0x5a, 0x00};
		AddRange(commands, setBarcodePDF);
		
		Byte[] barcodeCommand = new Byte[7 + barcodeData.length];
		barcodeCommand[0] = 0x1b;
		barcodeCommand[1] = 0x5a;
		barcodeCommand[2] = columnNumber;
		barcodeCommand[3] = securityLevel;
		barcodeCommand[4] = ratio;
		barcodeCommand[5] = (byte)(barcodeData.length % 256);
		barcodeCommand[6] = (byte)(barcodeData.length / 256);
		
		for(int index=0; index<barcodeData.length; index++)
		{
			barcodeCommand[index + 7] = barcodeData[index];
		}
		AddRange(commands, barcodeCommand);
		
		AddRange(commands, new Byte[] {0x0a, 0x0a, 0x0a, 0x0a});

		sendCommand(context, portName, portSettings, commands);
	}
	
	/**
	 * Cut is not supported on portable printers
	 * @param context - Activity to send the message that cut is not supported to the user
	 */
	public static void performCut(Context context)
	{
		Builder dialog = new AlertDialog.Builder(context);
		dialog.setNegativeButton("Ok", null);
		AlertDialog alert = dialog.create();
		alert.setTitle("Feature Not Available");
		alert.setMessage("Cut functionality is supported only on POS printer models");
		alert.setCancelable(false);
		alert.show(); 
	}

	/**
	 * This function is used to print a java bitmap directly to a portable printer.
	 * @param context - Activity for displaying messages to the user
	 * @param portName - Port name to use for communication. This should be (TCP:<IPAddress> or BT:<Device pair name>)
	 * @param portSettings - Should be mini, the port settings mini is used for portable printers
	 * @param source - The bitmap to convert to Star printer data for portable printers
	 * @param maxWidth - The maximum width of the image to print.  This is usually the page width of the printer.  If the image exceeds the maximum width then the image is scaled down.  The ratio is maintained. 
	 */
	public static void PrintBitmap(Context context, String portName, String portSettings, Bitmap source, int maxWidth, boolean compressionEnable, boolean pageModeEnable)
	{
        ArrayList<Byte> commands = new ArrayList<Byte>();
        Byte[] tempList;
		
		StarBitmap starbitmap = new StarBitmap(source, false, maxWidth);
		
		try {
			byte[] command = null;
			
			command = starbitmap.getImageEscPosDataForPrinting(compressionEnable, pageModeEnable);
			
	        tempList = new Byte[command.length];
			CopyArray(command, tempList);
			commands.addAll(Arrays.asList(tempList));
			
			sendCommand(context, portName, portSettings, commands);
		} catch (StarIOPortException e) {
			Builder dialog = new AlertDialog.Builder(context);
			dialog.setNegativeButton("Ok", null);
			AlertDialog alert = dialog.create();
			alert.setTitle("Failure");
			alert.setMessage(e.getMessage());
			alert.setCancelable(false);
			alert.show();
		}
	}
	
	/**
	 * This function is used to print a java bitmap directly to a portable printer.
	 * @param context - Activity for displaying messages to the user
	 * @param portName - Port name to use for communication. This should be (TCP:<IPAddress> or BT:<Device pair name>)
	 * @param portSettings - Should be mini, the port settings mini is used for portable printers
	 * @param res - The resources object containing the image data
	 * @param source - The resource id of the image data
	 * @param maxWidth - The maximum width of the image to print.  This is usually the page width of the printer.  If the image exceeds the maximum width then the image is scaled down.  The ratio is maintained. 
	 */
	public static void PrintBitmapImage(Context context, String portName, String portSettings, Bitmap bm, int maxWidth, boolean compressionEnable, boolean pageModeEnable)
	{
        ArrayList<Byte> commands = new ArrayList<Byte>();
        Byte[] tempList;
        
		StarBitmap starbitmap = new StarBitmap(bm, false, maxWidth);
		
		try {
			byte[] command = null;
			
			command = starbitmap.getImageEscPosDataForPrinting(compressionEnable, pageModeEnable);
			
	        tempList = new Byte[command.length];
			CopyArray(command, tempList);
			commands.addAll(Arrays.asList(tempList));
			
			sendCommand(context, portName, portSettings, commands);
		} catch (StarIOPortException e) {
			Builder dialog = new AlertDialog.Builder(context);
			dialog.setNegativeButton("Ok", null);
			AlertDialog alert = dialog.create();
			alert.setTitle("Failure");
			alert.setMessage(e.getMessage());
			alert.setCancelable(false);
			alert.show();
		}
	}

	/**
	 * This function prints raw text to a Star portable printer.  It shows how the text can be modified like changing its size.
	 * @param context - Activity for displaying messages to the user
	 * @param portName - Port name to use for communication. This should be (TCP:<IPAddress> or BT:<Device pair name>)
	 * @param portSettings - Should be mini, the port settings mini is used for portable printers
	 * @param underline - boolean variable that tells the printer to underline the text
	 * @param emphasized - boolean variable that tells the printer to emphasize the text.  This is somewhat like bold. It isn't as dark, but darker than regular characters.
	 * @param upsideDown - boolean variable that tells the printer to print text upside down.
	 * @param invertColor - boolean variable that tells the printer to invert text.  All white space will become black but the characters will be left white.
	 * @param heightExpansion - This integer tells the printer what the character height should be, ranging from 0 to 7 and representing multiples from 1 to 8.
	 * @param widthExpansion - This integer tell the printer what the character width should be, ranging from 0 to 7 and representing multiples from 1 to 8.
	 * @param leftMargin - Defines the left margin for text on Star portable printers.  This number can be from 0 to 65536. However, remember how much space is available as the text can be pushed off the page.
	 * @param alignment - Defines the alignment of the text. The printers support left, right, and center justification.
	 * @param textToPrint - The text to send to the printer.
	 */
	public static void PrintText(Context context, String portName, String portSettings, boolean underline, boolean emphasized, boolean upsidedown, boolean invertColor, byte heightExpansion, byte widthExpansion, int leftMargin, PrinterFunctions.Alignment alignment, byte[] textToPrint)
	{
		ArrayList<Byte> commands = new ArrayList<Byte>();
		
		Byte[] initCommand = new Byte[] {0x1b, 0x40};               // Initialization
		AddRange(commands, initCommand);

		Byte[] underlineCommand = new Byte[] {0x1b, 0x2d, 0x00};
		if(underline)
		{
			underlineCommand[2] = 49;
		}
		else
		{
			underlineCommand[2] = 48;
		}
		AddRange(commands, underlineCommand);
		
		Byte[] emphasizedCommand = new Byte[] {0x1b, 0x45, 0x00};
		if(emphasized)
		{
			emphasizedCommand[2] = 1;
		}
		else
		{
			emphasizedCommand[2] = 0;
		}
		AddRange(commands, emphasizedCommand);
		
		Byte[] upsidedownCommand = new Byte[] {0x1b, 0x7b, 0x00};
		if(upsidedown)
		{
			upsidedownCommand[2] = 1;
		}
		else
		{
			upsidedownCommand[2] = 0;
		}
		AddRange(commands, upsidedownCommand);
		
		Byte[] invertColorCommand = new Byte[] {0x1d, 0x42, 0x00};
		if(invertColor)
		{
			invertColorCommand[2] = 1;
		}
		else
		{
			invertColorCommand[2] = 0;
		}
		AddRange(commands, invertColorCommand);
		
		
		Byte[] characterSizeCommand = new Byte[] {0x1d, 0x21, 0x00};
		characterSizeCommand[2] = (byte) (heightExpansion | (widthExpansion << 4));
		AddRange(commands, characterSizeCommand);
		
		Byte[] leftMarginCommand = new Byte[] {0x1d, 0x4c, 0x00, 0x00};
		leftMarginCommand[2] = (byte) (leftMargin % 256);
		leftMarginCommand[3] = (byte) (leftMargin / 256);
		AddRange(commands, leftMarginCommand);
		
		Byte[] justificationCommand = new Byte[] {0x1b, 0x61, 0x00};
		switch(alignment)
		{
		case Left:
			justificationCommand[2] = 48;
			break;
		case Center:
			justificationCommand[2] = 49;
			break;
		case Right:
			justificationCommand[2] = 50;
			break;
		}
		AddRange(commands, justificationCommand);

		for (int i = 0; i < textToPrint.length; i++)
		{
			commands.add(textToPrint[i]);
		}
		
		commands.add((byte) 0x0a);
		
		sendCommand(context, portName, portSettings, commands);
	}
	
	/**
	 * This function prints raw JP-Kanji text to a Star portable printer.  It shows how the text can be modified like changing its size.
	 * @param context - Activity for displaying messages to the user
	 * @param portName - Port name to use for communication. This should be (TCP:<IPAddress> or BT:<Device pair name>)
	 * @param portSettings - Should be mini, the port settings mini is used for portable printers
	 * @param underline - boolean variable that tells the printer to underline the text
	 * @param emphasized - boolean variable that tells the printer to emphasize the text.  This is somewhat like bold. It isn't as dark, but darker than regular characters.
	 * @param upsideDown - boolean variable that tells the printer to print text upside down.
	 * @param invertColor - boolean variable that tells the printer to invert text.  All white space will become black but the characters will be left white.
	 * @param heightExpansion - This integer tells the printer what the character height should be, ranging from 0 to 7 and representing multiples from 1 to 8.
	 * @param widthExpansion - This integer tell the printer what the character width should be, ranging from 0 to 7 and representing multiples from 1 to 8.
	 * @param leftMargin - Defines the left margin for text on Star portable printers.  This number can be from 0 to 65536. However, remember how much space is available as the text can be pushed off the page.
	 * @param alignment - Defines the alignment of the text. The printers support left, right, and center justification.
	 * @param textToPrint - The text to send to the printer.
	 */
	public static void PrintTextKanji(Context context, String portName, String portSettings, boolean underline, boolean emphasized, boolean upsidedown, boolean invertColor, byte heightExpansion, byte widthExpansion, int leftMargin, PrinterFunctions.Alignment alignment, byte[] textToPrint)
	{
		ArrayList<Byte> commands = new ArrayList<Byte>();
		
		Byte[] initCommand = new Byte[] {0x1b, 0x40};               // Initialization
		AddRange(commands, initCommand);

		Byte[] shiftJISCommand = new Byte[] {0x1c, 0x43, 0x31};     // Shift-JIS Kanji Mode
		AddRange(commands, shiftJISCommand);

		Byte[] underlineCommand = new Byte[] {0x1b, 0x2d, 0x00};
		if(underline)
		{
			underlineCommand[2] = 49;
		}
		else
		{
			underlineCommand[2] = 48;
		}
		AddRange(commands, underlineCommand);
		
		Byte[] emphasizedCommand = new Byte[] {0x1b, 0x45, 0x00};
		if(emphasized)
		{
			emphasizedCommand[2] = 1;
		}
		else
		{
			emphasizedCommand[2] = 0;
		}
		AddRange(commands, emphasizedCommand);
		
		Byte[] upsidedownCommand = new Byte[] {0x1b, 0x7b, 0x00};
		if(upsidedown)
		{
			upsidedownCommand[2] = 1;
		}
		else
		{
			upsidedownCommand[2] = 0;
		}
		AddRange(commands, upsidedownCommand);
		
		Byte[] invertColorCommand = new Byte[] {0x1d, 0x42, 0x00};
		if(invertColor)
		{
			invertColorCommand[2] = 1;
		}
		else
		{
			invertColorCommand[2] = 0;
		}
		AddRange(commands, invertColorCommand);
		
		
		Byte[] characterSizeCommand = new Byte[] {0x1d, 0x21, 0x00};
		characterSizeCommand[2] = (byte) (heightExpansion | (widthExpansion << 4));
		AddRange(commands, characterSizeCommand);
		
		Byte[] leftMarginCommand = new Byte[] {0x1d, 0x4c, 0x00, 0x00};
		leftMarginCommand[2] = (byte) (leftMargin % 256);
		leftMarginCommand[3] = (byte) (leftMargin / 256);
		AddRange(commands, leftMarginCommand);
		
		Byte[] justificationCommand = new Byte[] {0x1b, 0x61, 0x00};
		switch(alignment)
		{
		case Left:
			justificationCommand[2] = 48;
			break;
		case Center:
			justificationCommand[2] = 49;
			break;
		case Right:
			justificationCommand[2] = 50;
			break;
		}
		AddRange(commands, justificationCommand);

		// textToPrint Encoding!!
		String  strData = new String(textToPrint);
		byte [] rawData = null;
		try
		{
			rawData = strData.getBytes("Shift_JIS");    // Shift JIS code
		}
	    catch (UnsupportedEncodingException e)
	    {
			rawData = strData.getBytes();
	    }
		
		for (int i = 0; i < rawData.length; i++)
		{
			commands.add(rawData[i]);
		}
		
		commands.add((byte) 0x0a);
		
		sendCommand(context, portName, portSettings, commands);
	}

	/**
	 * This function shows how to read the MSR data(credit card) of a portable printer.
	 * The function first puts the printer into MSR read mode, then asks the user to swipe a credit card
	 * The function waits for a response from the user.
	 * The user can cancel MSR mode or have the printer read the card.
	 * @param context - Activity for displaying messages to the user
	 * @param portName - Port name to use for communication. This should be (TCP:<IPAddress> or BT:<Device pair name>)
	 * @param portSettings - Should be mini, the port settings mini is used for portable printers
	 */
	public static void MCRStart(final Context context, String portName, String portSettings)
	{
		try 
    	{
			/*
				using StarIOPort3.1.jar (support USB Port)
				Android OS Version: upper 2.2
			*/
			portForMoreThanOneFunction = StarIOPort.getPort(portName, portSettings, 10000, context);
			/* 
				using StarIOPort.jar
				Android OS Version: under 2.1
				portForMoreThanOneFunction = StarIOPort.getPort(portName, portSettings, 10000);
			*/

			try
			{
				Thread.sleep(500);
			}
			catch(InterruptedException e) {}

			portForMoreThanOneFunction.writePort(new byte[] {0x1b, 0x4d, 0x45}, 0, 3);
			
			Builder dialog = new AlertDialog.Builder(context);
    		dialog.setNegativeButton("Cancel", new OnClickListener() {
				//If the user cancels MSR mode, the character 0x04 is sent to the printer
    			//This function also closes the port
				public void onClick(DialogInterface dialog, int which)
				{
	              	  ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
	              	  ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
					try
					{
						portForMoreThanOneFunction.writePort(new byte[] {0x04}, 0, 1);
						try
						{
							Thread.sleep(3000);
						}
						catch(InterruptedException e) {}
					}
					catch(StarIOPortException e)
					{
						
					}
					finally
					{
						if(portForMoreThanOneFunction != null)
						{
							try {
								StarIOPort.releasePort(portForMoreThanOneFunction);
							} catch (StarIOPortException e1) {}
						}
					}
				}
			});
    		AlertDialog alert = dialog.create();
    		alert.setTitle("");
    		alert.setMessage("Slide credit card");
    		alert.setCancelable(false);
    		alert.setButton("OK", new OnClickListener()
    		{	
    			//If the user presses ok then the magnetic stripe is read and displayed to the user
    			//This function also closes the port
				public void onClick(DialogInterface dialog, int which)
				{
	              	((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
	              	((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
					try
					{
						byte[] mcrData = new byte[100];
						portForMoreThanOneFunction.readPort(mcrData, 0, mcrData.length);
						
						Builder dialog1 = new AlertDialog.Builder(context);
			    		dialog1.setNegativeButton("Ok", null);
			    		AlertDialog alert = dialog1.create();
			    		alert.setTitle("");
			    		alert.setMessage(new String(mcrData));
			    		alert.show();
					}
					catch(StarIOPortException e)
					{
						
					}
					finally
					{
						if(portForMoreThanOneFunction != null)
						{
							try {
								StarIOPort.releasePort(portForMoreThanOneFunction);
							} catch (StarIOPortException e1) {}
						}
					}
				}
			});
    		alert.show();
		}
    	catch (StarIOPortException e)
    	{
    		Builder dialog = new AlertDialog.Builder(context);
    		dialog.setNegativeButton("Ok", null);
    		AlertDialog alert = dialog.create();
    		alert.setTitle("Failure");
    		alert.setMessage("Failed to connect to printer");
    		alert.setCancelable(false);
    		alert.show();
			if(portForMoreThanOneFunction != null)
			{
				try {
					StarIOPort.releasePort(portForMoreThanOneFunction);
				} catch (StarIOPortException e1) {}
			}
    	}
		finally
		{

		}
	}

	
    
    private static byte[] createShiftJIS(String inputText) {
    	byte[] byteBuffer = null;
    	
    	try {
			byteBuffer = inputText.getBytes("Shift_JIS");
		} catch (UnsupportedEncodingException e) {
			byteBuffer = inputText.getBytes();
		}
    	
    	return byteBuffer;
    }
    
    private static void CopyArray(byte[] srcArray, Byte[] cpyArray) {
    	for (int index = 0; index < cpyArray.length; index++) {
    		cpyArray[index] = srcArray[index];
    	}
    }
    
	private static byte[] convertFromListByteArrayTobyteArray(List<Byte> ByteArray)
	{
		byte[] byteArray = new byte[ByteArray.size()];
		for(int index = 0; index < byteArray.length; index++)
		{
			if (null == ByteArray.get(index)) {
				byteArray[index] = 0;
			}
			else
			{
			    byteArray[index] = ByteArray.get(index);
			}
		}
		
		return byteArray;
	}

	/*
	private static void checkPrinterSendToComplete(StarIOPort port) throws StarIOPortException
	{
		int timeout = 20000;
		long timeCount = 0;
		int readSize = 0;
		byte[] statusCommand = new byte[] { 0x1b, 0x76 };
		byte[] statusReadByte = new byte[] { 0x00 };

		try
		{
			port.writePort(statusCommand, 0, statusCommand.length);

			StarPrinterStatus status = port.retreiveStatus();
			
            if (status.coverOpen)
			{
				throw new StarIOPortException("printer is cover open");
			}
			if (status.receiptPaperEmpty)
			{
				throw new StarIOPortException("paper is empty");
			}
			if (status.offline)
			{
				throw new StarIOPortException("printer is offline");
			}

			long timeStart = System.currentTimeMillis();

			while (timeCount < timeout)
			{
				readSize = port.readPort(statusReadByte, 0, 1);

				if (readSize == 1)
				{
					break;
				}

				timeCount = System.currentTimeMillis() - timeStart;
			}
		}
		catch (StarIOPortException e)
		{
			try {
				try
				{
					Thread.sleep(500);
				}
				catch(InterruptedException ie) {}
				
				StarPrinterStatus status = port.retreiveStatus();
	            if (status.coverOpen)
				{
					throw new StarIOPortException("printer is cover open");
				}
				if (status.receiptPaperEmpty)
				{
					throw new StarIOPortException("paper is empty");
				}
				if (status.offline)
				{
					throw new StarIOPortException("printer is offline");
				}
				
				long timeStart = System.currentTimeMillis();

				while (timeCount < timeout)
				{
					readSize = port.readPort(statusReadByte, 0, 1);

					if (readSize == 1)
					{
						break;
					}

					timeCount = System.currentTimeMillis() - timeStart;
				}
			}
			catch (StarIOPortException ex)
			{
			    throw new StarIOPortException(ex.getMessage());
			}
		}
	}
	*/
	
	private static void sendCommand(Context context, String portName, String portSettings, ArrayList<Byte> byteList) {
		StarIOPort port = null;
		try
		{
			/*
				using StarIOPort3.1.jar (support USB Port)
				Android OS Version: upper 2.2
			*/
			port = StarIOPort.getPort(portName, portSettings, 20000, context);
			/* 
				using StarIOPort.jar
				Android OS Version: under 2.1
				port = StarIOPort.getPort(portName, portSettings, 10000);
			*/
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e) { }

			/*
		    Portable Printer Firmware Version 2.4 later, SM-S220i(Firmware Version 2.0 later) 

            Using Begin / End Checked Block method for preventing "data detective".
            
            When sending large amounts of raster data,
            use Begin / End Checked Block method and adjust the value in the timeout in the "StarIOPort.getPort"
            in order to prevent "timeout" of the "endCheckedBlock method" while a printing.
            
            *If receipt print is success but timeout error occurs(Show message which is "There was no response of the printer within the timeout period."),
             need to change value of timeout more longer in "StarIOPort.getPort" method. (e.g.) 10000 -> 30000
            *When use "Begin / End Checked Block Sample Code", do comment out "query commands Sample code".
		    */

		    /* Start of Begin / End Checked Block Sample code */
			StarPrinterStatus status = port.beginCheckedBlock();

			if (true == status.offline)
			{
				throw new StarIOPortException("A printer is offline");
			}

			byte[] commandToSendToPrinter = convertFromListByteArrayTobyteArray(byteList);
			port.writePort(commandToSendToPrinter, 0, commandToSendToPrinter.length);

			port.setEndCheckedBlockTimeoutMillis(30000);//Change the timeout time of endCheckedBlock method.
			status = port.endCheckedBlock();

			if (true == status.coverOpen)
			{
				throw new StarIOPortException("Printer cover is open");
			}
			else if (true == status.receiptPaperEmpty)
			{
				throw new StarIOPortException("Receipt paper is empty");
			}
			else if (true == status.offline)
			{
				throw new StarIOPortException("Printer is offline");
			}
			/* End of Begin / End Checked Block Sample code*/



			/*
			    Portable Printer Firmware Version 2.3 earlier

                Using query commands for preventing "data detective".
                
				When sending large amounts of raster data,
				send query commands after writePort data for confirming the end of printing 
				and adjust the value in the timeout in the "checkPrinterSendToComplete" method
				in order to prevent "timeout" of the "sending query commands" while a printing.
                
				*If receipt print is success but timeout error occurs(Show message which is "There was no response of the printer within the timeout period."),
				 need to change value of timeout more longer in "checkPrinterSendToComplete" method. (e.g.) 10000 -> 30000
				*When use "query commands Sample code", do comment out "Begin / End Checked Block Sample Code".
			 */

			/* Start of query commands Sample code */
//            byte[] commandToSendToPrinter = convertFromListByteArrayTobyteArray(byteList);
//			port.writePort(commandToSendToPrinter, 0, commandToSendToPrinter.length);
//			
//			checkPrinterSendToComplete(port);
			/* End of query commands Sample code */
		}
		catch (StarIOPortException e)
		{
//			Builder dialog = new AlertDialog.Builder(context);
//			dialog.setNegativeButton("Ok", null);
//			AlertDialog alert = dialog.create();
//			alert.setTitle("Failure");
//			alert.setMessage(e.getMessage());
//			alert.setCancelable(false);
//			alert.show();
		}
		finally
		{
			if (port != null)
			{
				try
				{
					StarIOPort.releasePort(port);
				}
				catch (StarIOPortException e) { }
			}
		}
	}
}
