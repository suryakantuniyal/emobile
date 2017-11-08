package com.android.support;

import com.elo.device.DeviceManager;
import com.elo.device.enums.EloPlatform;
import com.elo.device.exceptions.UnsupportedEloPlatform;
import com.elo.device.peripherals.CFD;
import com.elotouch.paypoint.register.printer.SerialPort;
import com.zzzapi.uart.uart;

import java.io.File;
import java.io.OutputStream;

import drivers.EMSELO;
import drivers.EMSPAT100;
import drivers.EMSPAT215;
import util.StringUtil;

/**
 * Created by Guarionex on 11/23/2015.
 */
public class TerminalDisplay {
    public static void setTerminalDisplay(final MyPreferences myPref, String row1, String row2) {
        row1 = StringUtil.nullStringToEmpty(row1);
        row2 = StringUtil.nullStringToEmpty(row2);
        if (myPref.isSam4s()) {
            uart uart_tool = new uart();
            uart_tool.config(3, 9600, 8, 1);
            uart_tool.write(3, Global.emptySpaces(40, 0, false));
            uart_tool.write(3, Global.formatSam4sCDT(row1, row2));
        } else if (myPref.isPAT100()) {
            EMSPAT100.getTerminalDisp().clearText();
            EMSPAT100.getTerminalDisp().displayText(Global.formatSam4sCDT(row1, row2));
        } else if (myPref.isESY13P1()) {
            final String finalRow = row1;
            final String finalRow1 = row2;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    EMSELO.printTextOnCFD(finalRow, finalRow1, myPref.context);
                }
            }).start();
        } else if (myPref.isPAT215()) {
            EMSPAT215.getTerminalDisp().clearText();
            EMSPAT215.getTerminalDisp().displayText(Global.formatSam4sCDT(row1, row2));
        } else if (MyPreferences.isTeamSable()) {
            SerialPort mSerialPort;
            try {
                mSerialPort = new SerialPort(new File("/dev/ttymxc0"), 9600, 0);
                OutputStream fs = mSerialPort.getOutputStream();
                fs.write(Global.emptySpaces(40, 0, false).getBytes());
                fs.write(Global.formatSam4sCDT(row1, row2).getBytes());
                mSerialPort.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
