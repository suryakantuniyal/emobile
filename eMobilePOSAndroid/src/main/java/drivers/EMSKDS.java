package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Xml;

import com.android.dao.AssignEmployeeDAO;
import com.android.dao.DeviceTableDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.models.ClockInOut;
import com.android.emobilepos.models.EMSCategory;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.SplittedOrder;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.Device;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.ShiftExpense;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.starmicronics.starioextension.IConnectionCallback;

import org.apache.commons.lang3.NotImplementedException;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

public class EMSKDS extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate, IConnectionCallback {

    // these comes from EMSStar
    private int connectionRetries = 0;

    private String portName;
    private int port = 0;
    private String host = "";
    private String kdsStation = "";

    private EMSDeviceManager edm;
    private EMSDeviceDriver thisInstance;
    private Context activity;

    // Dialog
    private ProgressDialog myProgressDialog;
    // Socket Client
    private Socket clientSocket;
    private OutputStream out;
    private BufferedReader in;

    // Order mapping
    String transType = "1";
    // End of Order mapping
    public EMSKDS(String portName,int port){
        String[] vals = portName.split("_");
        if(vals != null && vals.length == 2){
            this.host = getIP(vals[0]);
            this.kdsStation = vals[1];
        }else{
            this.host = getIP(portName);
            this.kdsStation = "";
        }
        this.port = port;
    }
    public static void main1(String[] args){
        final int port    = 4000;

        EMSKDS client = null;
        String ip   = "192.168.1.72";
        try{
            client  = new EMSKDS(ip,port);
            client.startConnection();
            String response = client.sendMessage("");
        }catch (Exception x){
            x.printStackTrace();
        }finally {
            if(client != null){
                try{
                    client.stopConnection();
                }catch (Exception x){
                    x.printStackTrace();
                }
            }
        }
    }
    // Writes provided 4-byte integer to a 4 element byte array in Little-Endian order.
    public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value & 0xff),
                (byte)(value >> 8 & 0xff),
                (byte)(value >> 16 & 0xff),
                (byte)(value >>> 24)
        };
    }
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
    private static byte[] intToLittleEndian(int numero) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt((int) numero);
        return bb.array();
    }
    private static byte[] intToLittleEndian(long numero) {
        byte[] b = new byte[4];
        b[0] = (byte) (numero & 0xFF);
        b[1] = (byte) ((numero >> 8) & 0xFF);
        b[2] = (byte) ((numero >> 16) & 0xFF);
        b[3] = (byte) ((numero >> 24) & 0xFF);
        return b;
    }
    public void startConnection() throws IOException {
        clientSocket = new Socket(host, port);
        out = clientSocket.getOutputStream();
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }
    public String sendMessage(String msg) throws Exception{
        try{
            byte[] ptext = msg.getBytes(ISO_8859_1);
            String value = new String(ptext, UTF_8);
            byte[] utf8 = value.getBytes();
            //Full packet = stx + command + len high + len low+ payload + etx
            int     nlen    = utf8.length + 5; //Length of full packet is length of payload + 5
            byte[]  buf     = new byte[nlen];
            buf[0]  = (byte)0x02; //stx
            buf[1]  = (byte)0x05; //command
            // data length high byte
            buf[2] = intToByteArray(utf8.length)[1]; // (utf8.length >> 8); //high byte
            // data length low byte
            buf[3] = intToByteArray(utf8.length)[0] ; //low byte
            System.arraycopy(utf8,0,buf,4,utf8.length);
            buf[4 + utf8.length] = (byte) 0x3; //etx
            String resp = "";
            if(!clientSocket.isClosed()){
                for(int i = 0; i < buf.length; i++){
                    out.write(buf[i]);
                }
                if(in != null && in.ready()){
                    while(in.ready()){
                        resp = resp + in.readLine();
                    }
                }
            }
            return resp;
        }catch (Exception x){
            throw x;
        }
    }

    public void stopConnection() throws Exception {
        try{
            if(in != null)
                in.close();
            if(out != null)
                out.close();
            if(clientSocket != null)
            {
                clientSocket.close();
            }
        }catch (Exception x){
            x.printStackTrace();
            throw x;
        }
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
        return false;
    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
        return false;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold) {
        return false;
    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold) {
        return false;
    }

    @Override
    public boolean printPaymentDetails(String payID, int isFromMainMenu, boolean isReprint, EMVContainer emvContainer) {
        return false;
    }

    @Override
    public boolean printBalanceInquiry(HashMap<String, String> values) {
        return false;
    }

    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
        return false;
    }

    @Override
    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
        return false;
    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
        return false;
    }

    @Override
    public boolean printRemoteStation(List<Orders> ordersList, String ordID) {
        try{
            // Use order list to create message for the kitchengo printer
            startConnection();
            String msg = getOrderString(ordersList,ordID,false,"","","", "",new ArrayList<EMSCategory>());
            String response = sendMessage(msg);
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }finally {
            try{
                stopConnection();
            }catch (Exception x){
                x.printStackTrace();
                return false;
            }
        }
        return true;
    }
    public boolean printRemoteStation(List<Orders> ordersList, String ordID, boolean isFromHold, String serverName, String guestTable, String comment, String category, List<EMSCategory> emsCategoryList ) {
        try{
            // Use order list to create message for the kitchengo printer
            startConnection();
            String msg = getOrderString(ordersList, ordID, isFromHold, serverName, guestTable, comment, category,emsCategoryList);
            String response = sendMessage(msg);
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }finally {
            try{
                stopConnection();
            }catch (Exception x){
                x.printStackTrace();
                return false;
            }
        }
        return true;
    }
    private EMSCategory getEMSCategoryById(List<EMSCategory> emsCategoryList, String categoryId){
        if(emsCategoryList != null && categoryId != null){
            for(EMSCategory emsCategory: emsCategoryList){
                if(emsCategory.getCategoryId().equals(categoryId) ){
                    return emsCategory;
                }
            }
        }
        return null;
    }
    public String getOrderString(List<Orders> ordersList, String orderId, boolean isFromHold, String serverName, String guestTable, String comment, String category, List<EMSCategory> emsCategoryList) {
        String namespace = "";
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        //String posTerminal = "1"; // emp_id
        transType = "1";

        String destination = "Food";
        String userInfo = "";
        String messagesCount = "1";
        String orderMessage = comment;
        String orderMessage1 = "Order Message 1";
        String itemTransType = getTransType(isFromHold);
        AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee();
        String posTerminal = ""+assignEmployee.getEmpId();

        String itemPreModifierCount = "1";

        String orderStatus = "0";
        try {

            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag(namespace,"Transaction");
            serializer.startTag(namespace,"Order");
            serializer.startTag(namespace,"ID");
            serializer.text(orderId);
            serializer.endTag(namespace,"ID");

            serializer.startTag(namespace,"PosTerminal");
            serializer.text(posTerminal);
            serializer.endTag(namespace,"PosTerminal");

            serializer.startTag(namespace,"TransType");
            serializer.text(transType);
            serializer.endTag(namespace,"TransType");

            serializer.startTag(namespace,"OrderStatus");
            serializer.text(orderStatus);
            serializer.endTag(namespace,"OrderStatus");

            serializer.startTag(namespace,"OrderType");
            serializer.text("");
            serializer.endTag(namespace,"OrderType");

            serializer.startTag(namespace,"ServerName");
            serializer.text(serverName);
            serializer.endTag(namespace,"ServerName");

            serializer.startTag(namespace,"Destination");
            serializer.text(destination);
            serializer.endTag(namespace,"Destination");

            serializer.startTag(namespace,"GuestTable");
            serializer.text(guestTable);
            serializer.endTag(namespace,"GuestTable");

            serializer.startTag(namespace,"UserInfo");
            serializer.text(userInfo);
            serializer.endTag(namespace,"UserInfo");

            serializer.startTag(namespace,"OrderMessages");
            serializer.startTag(namespace,"Count");

            serializer.text(messagesCount);
            serializer.endTag(namespace,"Count");

            serializer.startTag(namespace,"S0");
            serializer.text(orderMessage != null ? orderMessage:"");
            serializer.endTag(namespace,"S0");

            serializer.endTag(namespace,"OrderMessages");

            boolean isItemPendingForClose = false;
            for(Orders order: ordersList){
                // Item or addon ?
                if(!order.isAddon()){
                    // Detect new Item which is not included
                    if(isItemPendingForClose){
                        serializer.endTag(namespace,"Item");
                        isItemPendingForClose = false;
                    }
                    serializer.startTag(namespace,"Item");

                    serializer.startTag(namespace,"ID");
                    serializer.text(order.getOrdprodID());
                    serializer.endTag(namespace,"ID");

                    serializer.startTag(namespace,"TransType");
                    serializer.text(itemTransType);
                    serializer.endTag(namespace,"TransType");

                    serializer.startTag(namespace,"Name");
                    serializer.text(order.getName());
                    serializer.endTag(namespace,"Name");


                    EMSCategory cat =getEMSCategoryById( emsCategoryList,  order.getCatID());
                    //category = itemKDSStation = getKDSStationByCategory(category);
                    //category = itemKDSStation = kdsStation;

                    serializer.startTag(namespace,"Category");
                    serializer.text(category);
                    serializer.endTag(namespace,"Category");

                    serializer.startTag(namespace,"Quantity");
                    serializer.text(order.getQty());
                    serializer.endTag(namespace,"Quantity");

                    serializer.startTag(namespace,"KDSStation");
                    serializer.text(kdsStation);
                    serializer.endTag(namespace,"KDSStation");
                    /**
                     * PreModifiers for Item
                     */
                    serializer.startTag(namespace,"PreModifier");

                    serializer.startTag(namespace,"Count");
                    serializer.text(itemPreModifierCount);
                    serializer.endTag(namespace,"Count");

                    serializer.startTag(namespace,"S0");
                    serializer.text(order.getOrderProdComment());
                    serializer.endTag(namespace,"S0");

                    serializer.endTag(namespace,"PreModifier");
                    serializer.startTag(namespace,"Color");
                    serializer.attribute(namespace, "BG", "108");
                    serializer.attribute(namespace, "FG", "120");
                    serializer.text("");
                    serializer.endTag(namespace,"Color");
                    if(!order.hasAddon())
                    {
                        serializer.endTag(namespace,"Item");
                    }else{
                        isItemPendingForClose = true;
                    }
                }else{
                    // isAddOn: Ketchup, Mayo, Onions
                    serializer.startTag(namespace,"Condiment");
                    serializer.startTag(namespace,"ID");
                    serializer.text(order.getOrdprodID());
                    serializer.endTag(namespace,"ID");
                    //TransType
                    serializer.startTag(namespace,"TransType");
                    serializer.text(itemTransType);
                    serializer.endTag(namespace,"TransType");
                    //Name
                    serializer.startTag(namespace,"Name");
                    serializer.text( (!order.isAdded()?"No ":"") + order.getName());
                    serializer.endTag(namespace,"Name");
                    //Color
                    serializer.startTag(namespace,"Color");
                    serializer.attribute(namespace, "BG", "108");
                    serializer.attribute(namespace, "FG", "20");
                    serializer.text("");
                    serializer.endTag(namespace,"Color");
                    //Action
                    serializer.startTag(namespace,"Action");
                    serializer.text(!order.isAdded()?"-1":"");
                    serializer.endTag(namespace,"Action");
                    serializer.endTag(namespace,"Condiment");
                }
            }
            // Detect new Item which is not included
            if(isItemPendingForClose){
                serializer.endTag(namespace,"Item");
                isItemPendingForClose = false;
            }
            serializer.endTag(namespace,"Order");
            serializer.endTag(namespace,"Transaction");
            serializer.endDocument();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    @Override
    public boolean printOpenInvoices(String invID) {
        return false;
    }

    @Override
    public boolean printOnHold(Object onHold) {
        return false;
    }

    @Override
    public void setBitmap(Bitmap bmp) {

    }

    @Override
    public void playSound() {

    }

    @Override
    public void turnOnBCR() {

    }

    @Override
    public void turnOffBCR() {

    }

    @Override
    public boolean printReport(String curDate) {
        return false;
    }

    @Override
    public void printShiftDetailsReport(String shiftID) {

    }

    @Override
    public void printEndOfDayReport(String date, String clerk_id, boolean printDetails) {

    }
    private String getIP(String portName){
        String ip = portName;
        if (portName.contains("TCP:")) {
            ip = portName.substring(portName.indexOf(':') + 1);
        }
        return ip;
    }
    @Override
    public void registerPrinter() {
        edm.setCurrentDevice(this);
        String ip = "";
        if (portName.contains("TCP:")) {
            ip = portName.substring(portName.indexOf(':') + 1);
        }
        Device kitchenPrinter = DeviceTableDAO.getByIp(ip);
        if (kitchenPrinter == null) {
            Global.mainPrinterManager = edm;
        }
    }
    @Override
    public void unregisterPrinter() {
        edm.setCurrentDevice(null);
    }

    @Override
    public void loadCardReader(EMSCallBack callBack, boolean isDebitCard) {

    }

    @Override
    public void loadScanner(EMSCallBack _callBack) {

    }

    @Override
    public void releaseCardReader() {

    }

    @Override
    public void openCashDrawer() {

    }

    @Override
    public void printHeader() {

    }

    @Override
    public void printFooter() {

    }

    @Override
    public boolean isUSBConnected() {
        return false;
    }

    @Override
    public void toggleBarcodeReader() {

    }

    @Override
    public void printReceiptPreview(SplittedOrder splitedOrder) {

    }

    @Override
    public void salePayment(Payment payment, CreditCardInfo creditCardInfo) {

    }

    @Override
    public void saleReversal(Payment payment, String originalTransactionId, CreditCardInfo creditCardInfo) {

    }

    @Override
    public void refund(Payment payment, CreditCardInfo creditCardInfo) {

    }

    @Override
    public void refundReversal(Payment payment, String originalTransactionId, CreditCardInfo creditCardInfo) {

    }

    @Override
    public void printEMVReceipt(String text) {

    }

    @Override
    public void sendEmailLog() {

    }

    @Override
    public void updateFirmware() {

    }

    @Override
    public void submitSignature() {

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void printClockInOut(List<ClockInOut> clockInOuts, String clerkID) {

    }

    @Override
    public void printExpenseReceipt(ShiftExpense expense) {

    }

    @Override
    public void onConnected(ConnectResult connectResult) {

    }

    @Override
    public void onDisconnected() {

    }
    public String getPortName() {
        return portName;
    }
    // AsyncTask
    public class KDSProcessConnectionAsync extends AsyncTask<String, String, String> {

        String msg = "";
        boolean didConnect = false;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage(activity.getString(R.string.progress_connecting_printer));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String ip           = params[0];
                String portNumber   = params[1];
                Socket clientSocket = null;
                try{
                    String[] vals = portName.split("_");
                    if(vals != null && vals.length == 2){
                        portName = getIP(vals[0]);
                    }
                    clientSocket = new Socket(getIP(portName), Integer.valueOf(portNumber));
                    didConnect = clientSocket.isConnected();
                }catch (Exception x){
                    didConnect = false;
                    x.printStackTrace();
                }finally {
                    if(clientSocket != null)
                    {
                        clientSocket.close();
                    }
                }
            } catch (Exception e) {
                msg = "Failed: \n" + e.getMessage();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String unused) {
            boolean isDestroyed = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (((Activity) activity).isDestroyed()) {
                    isDestroyed = true;
                }
            }
            if (!((Activity) activity).isFinishing() && !isDestroyed && myProgressDialog.isShowing()) {
                myProgressDialog.dismiss();
            }
            if (didConnect) {
                edm.driverDidConnectToDevice(thisInstance, true, activity);
            } else {
                edm.driverDidNotConnectToDevice(thisInstance, msg, true, activity);
            }
        }
    }
    // connect
    @Override
    public void connect(Context activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm){
        try{
            throw new NotImplementedException("Method not implemented!");
        }catch (Exception x){
            x.printStackTrace();
        }
    }
    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        boolean didConnect = false;
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        portName    = _portName;
        this.isPOSPrinter = isPOSPrinter;
        this.edm = edm;
        Socket clientSocket = null;
        try{
            String[] vals = portName.split("_");
            if(vals != null && vals.length == 2){
                portName = vals[0];
                this.host = getIP(vals[0]);
            }
            clientSocket = new Socket(getIP(portName), Integer.valueOf(_portNumber));
            didConnect = clientSocket.isConnected();
        }catch (Exception x){
            didConnect = false;
            x.printStackTrace();
        }
        thisInstance = this;
        if (didConnect) {
            this.edm.driverDidConnectToDevice(thisInstance, false, activity);
        } else {
            this.edm.driverDidNotConnectToDevice(thisInstance, null, false, activity);
        }
        return didConnect;
    }
    @Override
    public void registerAll() {
        this.registerPrinter();
    }
    public enum TransType {
        RECALL_CHECK("3"),
        START_CHECK("1");
        private String type;
        TransType(String type){
            this.type   = type;
        }
        public String getType(){
            return this.type;
        }
    }
    private String getTransType(boolean isFromHold){
        if(isFromHold){
            return TransType.RECALL_CHECK.getType();
        }else{
            return TransType.START_CHECK.getType();
        }
    }
    @Override
    public boolean printGiftReceipt(Order order, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold) {
        return false;
    }
}