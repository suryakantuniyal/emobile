package drivers.epson;

import com.epson.epos2.printer.Printer;
import java.util.ArrayList;

public class EpsonDevices {

    private ArrayList<SpnModelsItem> deviceList = new ArrayList<SpnModelsItem>();


    public ArrayList<SpnModelsItem> getDeviceList() {
        preparelist();
        return deviceList;
    }

    private void preparelist() {
        deviceList.clear();
        deviceList.add(new SpnModelsItem("TM-m10", Printer.TM_M10));
        deviceList.add(new SpnModelsItem("TM-m30", Printer.TM_M30));
        deviceList.add(new SpnModelsItem("TM-P20", Printer.TM_P20));
        deviceList.add(new SpnModelsItem("TM-P60", Printer.TM_P60));
        deviceList.add(new SpnModelsItem("TM-P60II", Printer.TM_P60II));
        deviceList.add(new SpnModelsItem("TM-P80", Printer.TM_P80));
        deviceList.add(new SpnModelsItem("TM-T20", Printer.TM_T20));
        deviceList.add(new SpnModelsItem("TM-T60", Printer.TM_T60));
        deviceList.add(new SpnModelsItem("TM-T70", Printer.TM_T70));
        deviceList.add(new SpnModelsItem("TM-T81", Printer.TM_T81));
        deviceList.add(new SpnModelsItem("TM-T82", Printer.TM_T82));
        deviceList.add(new SpnModelsItem("TM-T83", Printer.TM_T83));
        deviceList.add(new SpnModelsItem("TM-T83III", Printer.TM_T83III));
        deviceList.add(new SpnModelsItem("TM-T88", Printer.TM_T88));
        deviceList.add(new SpnModelsItem("TM-T90", Printer.TM_T90));
        deviceList.add(new SpnModelsItem("TM-T90KP", Printer.TM_T90KP));
        deviceList.add(new SpnModelsItem("TM-T100", Printer.TM_T100));
        deviceList.add(new SpnModelsItem("TM-U220", Printer.TM_U220));
        deviceList.add(new SpnModelsItem("TM-U330", Printer.TM_U330));
        deviceList.add(new SpnModelsItem("TM-L90", Printer.TM_L90));
        deviceList.add(new SpnModelsItem("TM-H6000", Printer.TM_H6000));
    }
}
