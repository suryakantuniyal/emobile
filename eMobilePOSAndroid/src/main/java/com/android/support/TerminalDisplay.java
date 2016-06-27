package com.android.support;

import com.zzzapi.uart.uart;

import drivers.EMSELO;
import drivers.EMSPAT100;
import drivers.EMSPAT215;
import util.StringUtil;

/**
 * Created by Guarionex on 11/23/2015.
 */
public class TerminalDisplay {
    public static void setTerminalDisplay(MyPreferences myPref, String row1, String row2) {
        row1 = StringUtil.nullStringToEmpty(row1);
        row2 = StringUtil.nullStringToEmpty(row2);
        if (myPref.isSam4s(true, true)) {
            uart uart_tool = new uart();
            uart_tool.config(3, 9600, 8, 1);
            uart_tool.write(3, Global.emptySpaces(40, 0, false));
            uart_tool.write(3, Global.formatSam4sCDT(row1, row2));
        } else if (myPref.isPAT100()) {
            EMSPAT100.getTerminalDisp().clearText();
            EMSPAT100.getTerminalDisp().displayText(Global.formatSam4sCDT(row1, row2));
        } else if (myPref.isESY13P1()) {
            EMSELO.printTextOnCFD(row1, row2);
        } else if (myPref.isPAT215()) {
            EMSPAT215.getTerminalDisp().clearText();
            EMSPAT215.getTerminalDisp().displayText(Global.formatSam4sCDT(row1, row2));
        }
    }
}
