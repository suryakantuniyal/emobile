package com.android.support;

import com.zzzapi.uart.uart;

import drivers.EMSPAT100;

/**
 * Created by Guarionex on 11/23/2015.
 */
public class TerminalDisplay {
    public static void setTerminalDisplay(MyPreferences myPref, String row1, String row2) {
        if (myPref.isSam4s(true, true)) {
            uart uart_tool = new uart();
            uart_tool.config(3, 9600, 8, 1);
            uart_tool.write(3, Global.emptySpaces(40, 0, false));
            uart_tool.write(3, Global.formatSam4sCDT(row1, row2));
        } else if (myPref.isPAT100(true, true)) {
            EMSPAT100.getTerminalDisp().clearText();
            EMSPAT100.getTerminalDisp().displayText(Global.formatSam4sCDT(row1, row2));
        }
    }
}
