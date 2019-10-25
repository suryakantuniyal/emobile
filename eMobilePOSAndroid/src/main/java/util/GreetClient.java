package util;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import static java.nio.charset.StandardCharsets.*;

public class GreetClient {
    private Socket clientSocket;
    private OutputStream out;
    private BufferedReader in;
    private final static int port    = 4000;

    public static void main(String[] args){
        GreetClient client = null;
        String ip   = "192.168.1.72";
        try{
            client  = new GreetClient();
            client.startConnection(ip,port);
            String response = client.sendMessage(client.getTestOrder2());
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
    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
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
    public String getTestOrder3(){
        String xml = "&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;" +
                "&lt;Transaction&gt;" +
                "&lt;Order&gt;" +
                " &lt;ID&gt;199&lt;/ID&gt;" +
                " &lt;PosTerminal&gt;2&lt;/PosTerminal&gt;" +
                "&lt;TransType&gt;1&lt;/TransType&gt;" +
                " &lt;OrderStatus&gt;0&lt;/OrderStatus&gt;" +
                " &lt;OrderType&gt;RUSH&lt;/OrderType&gt;" +
                " &lt;ServerName&gt;Jack&lt;/ServerName&gt;" +
                " &lt;Destination&gt;FastFood&lt;/Destination&gt;" +
                " &lt;GuestTable&gt;23&lt;/GuestTable&gt;" +
                " &lt;UserInfo&gt;userinfo&lt;/UserInfo&gt;" +
                "&lt;OrderMessages&gt;" +
                "&lt;Count&gt;2&lt;/Count&gt;" +
                "&lt;S0&gt;Order Message 0&lt;/S0&gt;" +
                "&lt;S1&gt;Order Message 1&lt;/S1&gt;" +
                "&lt;/OrderMessages&gt;" +
                "&lt;Item&gt;" +
                " &lt;ID&gt;14&lt;/ID&gt;" +
                " &lt;TransType&gt;1&lt;/TransType&gt;" +
                " &lt;Name&gt;Cheese Sandwish&lt;/Name&gt;" +
                " &lt;Category&gt;Sandwish&lt;/Category&gt;" +
                " &lt;Quantity&gt;" +
                "2&lt;/Quantity&gt;" +
                " &lt;KDSStation&gt;" +
                "0&lt;/KDSStation&gt;" +
                " &lt;PreModifier&gt;" +
                "&lt;Count&gt;" +
                "2&lt;/Count&gt;" +
                "&lt;S0&gt;Pre" +
                "-Modifier 0&lt;/S0&gt;" +
                "&lt;S1&gt;Pre" +
                "-Modifier 1&lt;/S1&gt;" +
                " &lt;/PreModifier&gt;" +
                " &lt;Color BG" +
                "=\"108\" FG" +
                "=\"120\"&gt;&lt;/Color&gt;" +
                " &lt;Condiment&gt;" +
                " &lt;ID&gt;" +
                "0&lt;/ID&gt;" +
                "&lt;PreModifier&gt;" +
                "&lt;Count&gt;" +
                "1&lt;/Count&gt;" +
                "&lt;S0&gt;Seat 1&lt;/S0&gt;" +
                "&lt;/PreModifier&gt;" +
                " &lt;TransType&gt;" +
                "1&lt;/TransType&gt;" +
                " &lt;Name&gt;Spicy&lt;/Name&gt;" +
                " &lt;Color BG" +
                "=\"128\" FG" +
                "=\"20\"&gt;&lt;/Color&gt;" +
                " &lt;Action&gt;" +
                "1&lt;/Action&gt;" +
                " &lt;/Condiment&gt;" +
                " &lt;Condiment&gt;" +
                " &lt;ID&gt;" +
                "2&lt;/ID&gt;" +
                " &lt;TransType&gt;" +
                "1&lt;/TransType&gt;" +
                " &lt;Name&gt;tomato&lt;/Name&gt;" +
                " &lt;Color BG" +
                "=\"8\" FG" +
                "=\"213\"&gt;&lt;/Color&gt;" +
                " &lt;Action&gt;" +
                "-" +
                "1&lt;/Action&gt;" +
                " &lt;/Condiment&gt;" +
                " &lt;/Item&gt;" +
                " &lt;Item&gt;" +
                " &lt;ID&gt;10&lt;/ID&gt;" +
                " &lt;TransType&gt;" +
                "1&lt;/TransType&gt;" +
                " &lt;Name&gt;Coffee&lt;/Name&gt;" +
                " &lt;Category&gt;Beverages&lt;/Category&gt;" +
                "" +
                "&lt;Quantity&gt;" +
                "1&lt;/Quantity&gt;" +
                " &lt;Color BG" +
                "=\"28\" FG" +
                "=\"200\"&gt;&lt;/Color&gt;" +
                "&lt;KDSStation&gt;" +
                "0&lt;/KDSStation&gt;" +
                "&lt;PreModifier&gt;" +
                "&lt;Count&gt;" +
                "2&lt;/Count&gt;" +
                "&lt;S0&gt;Seat 2&lt;/S0&gt;" +
                "&lt;S1&gt;extra msg &lt;/S1&gt;" +
                "&lt;/PreModifier&gt;" +
                " &lt;Condiment&gt;" +
                " &lt;ID&gt;" +
                "0&lt;/ID&gt;" +
                " &lt;TransType&gt;" +
                "1&lt;/TransType&gt;" +
                " &lt;Name&gt;Suger&lt;/Name&gt;" +
                " &lt;Color BG" +
                "=\"48\" FG" +
                "=\"100\"&gt;&lt;/Color&gt;" +
                " &lt;Action&gt;" +
                "1&lt;/Action&gt;" +
                " &lt;/Condiment&gt;" +
                " &lt;Condiment&gt;" +
                " &lt;ID&gt;" +
                "1&lt;/ID&gt;" +
                " &lt;TransType&gt;" +
                "1&lt;/TransType&gt;" +
                " &lt;Name&gt;Milk&lt;/Name&gt;" +
                "" +
                "&lt;Color BG" +
                "=\"58\" FG" +
                "=\"70\"&gt;&lt;/Color&gt;" +
                " &lt;Action&gt;" +
                "-" +
                "1&lt;/Action&gt;" +
                " &lt;/Condiment&gt;" +
                " &lt;/Item&gt;" +
                "&lt;Item&gt;" +
                " &lt;ID&gt;11&lt;/ID&gt;" +
                " &lt;TransType&gt;1&lt;/TransType&gt;" +
                " &lt;Name&gt;Vege Soup&lt;/Name&gt;" +
                " &lt;Category&gt;Soup&lt;/Category&gt;" +
                " &lt;Quantity&gt;2&lt;/Quantity&gt;" +
                " &lt;Color BG=\"108\" FG=\"120\"&gt;&lt;/Color&gt;" +
                "" +
                "" +
                " &lt;/Item&gt;" +
                "&lt;Item&gt;" +
                " &lt;ID&gt;12&lt;/ID&gt;" +
                " &lt;TransType&gt;1&lt;/TransType&gt;" +
                " &lt;Name&gt;Apple Pie&lt;/Name&gt;" +
                " &lt;Category&gt;Desserts&lt;/Category&gt;" +
                " &lt;Quantity&gt;2&lt;/Quantity&gt;" +
                " &lt;Color BG=\"108\" FG=\"120\"&gt;&lt;/Color&gt;" +
                " &lt;Condiment&gt;" +
                " &lt;ID&gt;0&lt;/ID&gt;" +
                " &lt;TransType&gt;1&lt;/TransType&gt;" +
                " &lt;Name&gt;butter&lt;/Name&gt;" +
                " &lt;Color BG=\"128\" FG=\"20\"&gt;&lt;/Color&gt;" +
                " &lt;Action&gt;1&lt;/Action&gt;" +
                " &lt;/Condiment&gt;" +
                " &lt;Condiment&gt;" +
                " &lt;ID&gt;2&lt;/ID&gt;" +
                " &lt;TransType&gt;1&lt;/TransType&gt;" +
                " &lt;Name&gt;cheese&lt;/Name&gt;" +
                " &lt;Color BG=\"8\" FG=\"213\"&gt;&lt;/Color&gt;" +
                " &lt;Action&gt;-1&lt;/Action&gt;" +
                " &lt;/Condiment&gt;" +
                " &lt;/Item&gt;" +
                "&lt;Item&gt;" +
                " &lt;ID&gt;13&lt;/ID&gt;" +
                " &lt;TransType&gt;1&lt;/TransType&gt;" +
                " &lt;Name&gt;Garden salad&lt;/Name&gt;" +
                " &lt;Category&gt;Salads&lt;/Category&gt;" +
                " &lt;Quantity&gt;2&lt;/Quantity&gt;" +
                " &lt;Color BG=\"108\" FG=\"120\"&gt;&lt;/Color&gt;" +
                " &lt;Condiment&gt;" +
                " &lt;ID&gt;0&lt;/ID&gt;" +
                " &lt;TransType&gt;1&lt;/TransType&gt;" +
                " &lt;Name&gt;Ranch&lt;/Name&gt;" +
                " &lt;Color BG=\"128\" FG=\"20\"&gt;&lt;/Color&gt;" +
                " &lt;Action&gt;1&lt;/Action&gt;" +
                " &lt;/Condiment&gt;" +
                " &lt;Condiment&gt;" +
                " &lt;ID&gt;2&lt;/ID&gt;" +
                " &lt;TransType&gt;1&lt;/TransType&gt;" +
                " &lt;Name&gt;No Dressing&lt;/Name&gt;" +
                " &lt;Color BG=\"8\" FG=\"213\"&gt;&lt;/Color&gt;" +
                " &lt;Action&gt;-1&lt;/Action&gt;" +
                " &lt;/Condiment&gt;" +
                " &lt;/Item&gt;" +
                "&lt;/Order&gt;" +
                "&lt;/Transaction&gt;";
        return xml;
    }
    public String getTestOrder2(){
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<Transaction>" +
                "<Order>" +
                " <ID>213</ID>" +
                " <PosTerminal>2</PosTerminal>" +
                "<TransType>1</TransType>" +
                " <OrderStatus>0</OrderStatus>" +
                " <OrderType></OrderType>" +
                " <ServerName>Jack</ServerName>" +
                " <Destination>FastFood</Destination>" +
                " <GuestTable>23</GuestTable>" +
                " <UserInfo>userinfo</UserInfo>" +
                "<OrderMessages>" +
                "<Count>2</Count>" +
                "<S0>Order Message 0</S0>" +
                "<S1>Order Message 1</S1>" +
                "</OrderMessages>" +
                "<Item>" +
                " <ID>14</ID>" +
                " <TransType>1</TransType>" +
                " <Name>Cheese Sandwish</Name>" +
                " <Category>Sandwish</Category>" +
                " <Quantity>" +
                "2</Quantity>" +
                " <KDSStation>" +
                "0</KDSStation>" +
                " <PreModifier>" +
                "<Count>" +
                "2</Count>" +
                "<S0>Pre" +
                "-Modifier 0</S0>" +
                "<S1>Pre" +
                "-Modifier 1</S1>" +
                " </PreModifier>" +
                " <Color BG" +
                "=\"108\" FG" +
                "=\"120\"></Color>" +
                " <Condiment>" +
                " <ID>" +
                "0</ID>" +
                "<PreModifier>" +
                "<Count>" +
                "1</Count>" +
                "<S0>Seat 1</S0>" +
                "</PreModifier>" +
                " <TransType>" +
                "1</TransType>" +
                " <Name>Spicy</Name>" +
                " <Color BG" +
                "=\"128\" FG" +
                "=\"20\"></Color>" +
                " <Action>" +
                "1</Action>" +
                " </Condiment>" +
                " <Condiment>" +
                " <ID>" +
                "2</ID>" +
                " <TransType>" +
                "1</TransType>" +
                " <Name>tomato</Name>" +
                " <Color BG" +
                "=\"8\" FG" +
                "=\"213\"></Color>" +
                " <Action>" +
                "-" +
                "1</Action>" +
                " </Condiment>" +
                " </Item>" +
                " <Item>" +
                " <ID>10</ID>" +
                " <TransType>" +
                "1</TransType>" +
                " <Name>Coffee</Name>" +
                " <Category>Beverages</Category>" +
                "" +
                "<Quantity>" +
                "1</Quantity>" +
                " <Color BG" +
                "=\"28\" FG" +
                "=\"200\"></Color>" +
                "<KDSStation>" +
                "0</KDSStation>" +
                "<PreModifier>" +
                "<Count>" +
                "2</Count>" +
                "<S0>Seat 2</S0>" +
                "<S1>extra msg </S1>" +
                "</PreModifier>" +
                " <Condiment>" +
                " <ID>" +
                "0</ID>" +
                " <TransType>" +
                "1</TransType>" +
                " <Name>Suger</Name>" +
                " <Color BG" +
                "=\"48\" FG" +
                "=\"100\"></Color>" +
                " <Action>" +
                "1</Action>" +
                " </Condiment>" +
                " <Condiment>" +
                " <ID>" +
                "1</ID>" +
                " <TransType>" +
                "1</TransType>" +
                " <Name>Milk</Name>" +
                "" +
                "<Color BG" +
                "=\"58\" FG" +
                "=\"70\"></Color>" +
                " <Action>" +
                "-" +
                "1</Action>" +
                " </Condiment>" +
                " </Item>" +
                "<Item>" +
                " <ID>11</ID>" +
                " <TransType>1</TransType>" +
                " <Name>Vege Soup</Name>" +
                " <Category>Soup</Category>" +
                " <Quantity>2</Quantity>" +
                " <Color BG=\"108\" FG=\"120\"></Color>" +
                "" +
                "" +
                " </Item>" +
                "<Item>" +
                " <ID>12</ID>" +
                " <TransType>1</TransType>" +
                " <Name>Apple Pie</Name>" +
                " <Category>Desserts</Category>" +
                " <Quantity>2</Quantity>" +
                " <Color BG=\"108\" FG=\"120\"></Color>" +
                " <Condiment>" +
                " <ID>0</ID>" +
                " <TransType>1</TransType>" +
                " <Name>butter</Name>" +
                " <Color BG=\"128\" FG=\"20\"></Color>" +
                " <Action>1</Action>" +
                " </Condiment>" +
                " <Condiment>" +
                " <ID>2</ID>" +
                " <TransType>1</TransType>" +
                " <Name>cheese</Name>" +
                " <Color BG=\"8\" FG=\"213\"></Color>" +
                " <Action>-1</Action>" +
                " </Condiment>" +
                " </Item>" +
                "<Item>" +
                " <ID>13</ID>" +
                " <TransType>1</TransType>" +
                " <Name>Garden salad</Name>" +
                " <Category>Salads</Category>" +
                " <Quantity>2</Quantity>" +
                " <Color BG=\"108\" FG=\"120\"></Color>" +
                " <Condiment>" +
                " <ID>0</ID>" +
                " <TransType>1</TransType>" +
                " <Name>Ranch</Name>" +
                " <Color BG=\"128\" FG=\"20\"></Color>" +
                " <Action>1</Action>" +
                " </Condiment>" +
                " <Condiment>" +
                " <ID>2</ID>" +
                " <TransType>1</TransType>" +
                " <Name>No Dressing</Name>" +
                " <Color BG=\"8\" FG=\"213\"></Color>" +
                " <Action>-1</Action>" +
                " </Condiment>" +
                " </Item>" +
                "</Order>" +
                "</Transaction>";
    }
    public String getTestOrder(){
        String order = "&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;" +
                "&lt;Transaction&gt;" +
                "&lt;Order&gt;" +
                " &lt;ID&gt;199&lt;/ID&gt;" +
                " &lt;PosTerminal&gt;2&lt;/PosTerminal&gt;" +
                "&lt;TransType&gt;1&lt;/TransType&gt;" +
                " &lt;OrderStatus&gt;0&lt;/OrderStatus&gt;" +
                " &lt;OrderType&gt;RUSH&lt;/OrderType&gt;" +
                " &lt;ServerName&gt;Jack&lt;/ServerName&gt;" +
                " &lt;Destination&gt;FastFood&lt;/Destination&gt;" +
                " &lt;GuestTable&gt;23&lt;/GuestTable&gt;" +
                " &lt;UserInfo&gt;userinfo&lt;/UserInfo&gt;" +
                "&lt;OrderMessages&gt;" +
                "&lt;Count&gt;2&lt;/Count&gt;" +
                "&lt;S0&gt;Order Message 0&lt;/S0&gt;" +
                "&lt;S1&gt;Order Message 1&lt;/S1&gt;" +
                "&lt;/OrderMessages&gt;" +
                "&lt;Item&gt;" +
                " &lt;ID&gt;14&lt;/ID&gt;" +
                " &lt;TransType&gt;1&lt;/TransType&gt;" +
                " &lt;Name&gt;Cheese Sandwish&lt;/Name&gt;" +
                " &lt;Category&gt;Sandwish&lt;/Category&gt;" +
                " &lt;Quantity&gt;" +
                "2&lt;/Quantity&gt;" +
                " &lt;KDSStation&gt;" +
                "0&lt;/KDSStation&gt;" +
                " &lt;PreModifier&gt;" +
                "&lt;Count&gt;" +
                "2&lt;/Count&gt;" +
                "&lt;S0&gt;Pre" +
                "-Modifier 0&lt;/S0&gt;" +
                "&lt;S1&gt;Pre" +
                "-Modifier 1&lt;/S1&gt;" +
                " &lt;/PreModifier&gt;" +
                " &lt;Color BG" +
                "=\"108\" FG" +
                "=\"120\"&gt;&lt;/Color&gt;" +
                " &lt;Condiment&gt;" +
                " &lt;ID&gt;" +
                "0&lt;/ID&gt;" +
                "&lt;PreModifier&gt;" +
                "&lt;Count&gt;" +
                "1&lt;/Count&gt;" +
                "&lt;S0&gt;Seat 1&lt;/S0&gt;" +
                "&lt;/PreModifier&gt;" +
                " &lt;TransType&gt;" +
                "1&lt;/TransType&gt;" +
                " &lt;Name&gt;Spicy&lt;/Name&gt;" +
                " &lt;Color BG" +
                "=\"128\" FG" +
                "=\"20\"&gt;&lt;/Color&gt;" +
                " &lt;Action&gt;" +
                "1&lt;/Action&gt;" +
                " &lt;/Condiment&gt;" +
                " &lt;Condiment&gt;" +
                " &lt;ID&gt;" +
                "2&lt;/ID&gt;" +
                " &lt;TransType&gt;" +
                "1&lt;/TransType&gt;" +
                " &lt;Name&gt;tomato&lt;/Name&gt;" +
                " &lt;Color BG" +
                "=\"8\" FG" +
                "=\"213\"&gt;&lt;/Color&gt;" +
                " &lt;Action&gt;" +
                "-" +
                "1&lt;/Action&gt;" +
                " &lt;/Condiment&gt;" +
                " &lt;/Item&gt;" +
                " &lt;Item&gt;" +
                " &lt;ID&gt;10&lt;/ID&gt;" +
                " &lt;TransType&gt;" +
                "1&lt;/TransType&gt;" +
                " &lt;Name&gt;Coffee&lt;/Name&gt;" +
                " &lt;Category&gt;Beverages&lt;/Category&gt;" +
                "" +
                "&lt;Quantity&gt;" +
                "1&lt;/Quantity&gt;" +
                " &lt;Color BG" +
                "=\"28\" FG" +
                "=\"200\"&gt;&lt;/Color&gt;" +
                "&lt;KDSStation&gt;" +
                "0&lt;/KDSStation&gt;" +
                "&lt;PreModifier&gt;" +
                "&lt;Count&gt;" +
                "2&lt;/Count&gt;" +
                "&lt;S0&gt;Seat 2&lt;/S0&gt;" +
                "&lt;S1&gt;extra msg &lt;/S1&gt;" +
                "&lt;/PreModifier&gt;" +
                " &lt;Condiment&gt;" +
                " &lt;ID&gt;" +
                "0&lt;/ID&gt;" +
                " &lt;TransType&gt;" +
                "1&lt;/TransType&gt;" +
                " &lt;Name&gt;Suger&lt;/Name&gt;" +
                " &lt;Color BG" +
                "=\"48\" FG" +
                "=\"100\"&gt;&lt;/Color&gt;" +
                " &lt;Action&gt;" +
                "1&lt;/Action&gt;" +
                " &lt;/Condiment&gt;" +
                " &lt;Condiment&gt;" +
                " &lt;ID&gt;" +
                "1&lt;/ID&gt;" +
                " &lt;TransType&gt;" +
                "1&lt;/TransType&gt;" +
                " &lt;Name&gt;Milk&lt;/Name&gt;" +
                "" +
                "&lt;Color BG" +
                "=\"58\" FG" +
                "=\"70\"&gt;&lt;/Color&gt;" +
                " &lt;Action&gt;" +
                "-" +
                "1&lt;/Action&gt;" +
                " &lt;/Condiment&gt;" +
                " &lt;/Item&gt;" +
                "&lt;Item&gt;" +
                " &lt;ID&gt;11&lt;/ID&gt;" +
                " &lt;TransType&gt;1&lt;/TransType&gt;" +
                " &lt;Name&gt;Vege Soup&lt;/Name&gt;" +
                " &lt;Category&gt;Soup&lt;/Category&gt;" +
                " &lt;Quantity&gt;2&lt;/Quantity&gt;" +
                " &lt;Color BG=\"108\" FG=\"120\"&gt;&lt;/Color&gt;" +
                "" +
                "" +
                " &lt;/Item&gt;" +
                "&lt;Item&gt;" +
                " &lt;ID&gt;12&lt;/ID&gt;" +
                " &lt;TransType&gt;1&lt;/TransType&gt;" +
                " &lt;Name&gt;Apple Pie&lt;/Name&gt;" +
                " &lt;Category&gt;Desserts&lt;/Category&gt;" +
                " &lt;Quantity&gt;2&lt;/Quantity&gt;" +
                " &lt;Color BG=\"108\" FG=\"120\"&gt;&lt;/Color&gt;" +
                " &lt;Condiment&gt;" +
                " &lt;ID&gt;0&lt;/ID&gt;" +
                " &lt;TransType&gt;1&lt;/TransType&gt;" +
                " &lt;Name&gt;butter&lt;/Name&gt;" +
                " &lt;Color BG=\"128\" FG=\"20\"&gt;&lt;/Color&gt;" +
                " &lt;Action&gt;1&lt;/Action&gt;" +
                " &lt;/Condiment&gt;" +
                " &lt;Condiment&gt;" +
                " &lt;ID&gt;2&lt;/ID&gt;" +
                " &lt;TransType&gt;1&lt;/TransType&gt;" +
                " &lt;Name&gt;cheese&lt;/Name&gt;" +
                " &lt;Color BG=\"8\" FG=\"213\"&gt;&lt;/Color&gt;" +
                " &lt;Action&gt;-1&lt;/Action&gt;" +
                " &lt;/Condiment&gt;" +
                " &lt;/Item&gt;" +
                "&lt;Item&gt;" +
                " &lt;ID&gt;13&lt;/ID&gt;" +
                " &lt;TransType&gt;1&lt;/TransType&gt;" +
                " &lt;Name&gt;Garden salad&lt;/Name&gt;" +
                " &lt;Category&gt;Salads&lt;/Category&gt;" +
                " &lt;Quantity&gt;2&lt;/Quantity&gt;" +
                " &lt;Color BG=\"108\" FG=\"120\"&gt;&lt;/Color&gt;" +
                " &lt;Condiment&gt;" +
                " &lt;ID&gt;0&lt;/ID&gt;" +
                " &lt;TransType&gt;1&lt;/TransType&gt;" +
                " &lt;Name&gt;Ranch&lt;/Name&gt;" +
                " &lt;Color BG=\"128\" FG=\"20\"&gt;&lt;/Color&gt;" +
                " &lt;Action&gt;1&lt;/Action&gt;" +
                " &lt;/Condiment&gt;" +
                " &lt;Condiment&gt;" +
                " &lt;ID&gt;2&lt;/ID&gt;" +
                " &lt;TransType&gt;1&lt;/TransType&gt;" +
                " &lt;Name&gt;No Dressing&lt;/Name&gt;" +
                " &lt;Color BG=\"8\" FG=\"213\"&gt;&lt;/Color&gt;" +
                " &lt;Action&gt;-1&lt;/Action&gt;" +
                " &lt;/Condiment&gt;" +
                " &lt;/Item&gt;" +
                "&lt;/Order&gt;" +
                "&lt;/Transaction&gt;";
        return order;
    }
    public String getXmlRafael(){
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<Transaction>" +
                "<Order>" +
                "<ID>4007</ID>" +
                "<PosTerminal>1</PosTerminal>" +
                "<TransType>1</TransType>" +
                "<OrderStatus>0</OrderStatus>" +
                "<OrderType></OrderType>" +
                "<ServerName>Jack</ServerName>" +
                "<Destination>FastFood</Destination>" +
                "<GuestTable>23</GuestTable>" +
                "<UserInfo>userinfo</UserInfo>" +
                "<OrderMessages>" +
                "<Count>2</Count>" +
                "<S0>Order Message 0</S0>" +
                "<S1>Order Message 1</S1>" +
                "</OrderMessages>" +
                "<Item>" +
                "<ID>200</ID>" +
                "<TransType>1</TransType>" +
                "<Name>Cheese Sandwish</Name>" +
                "<Category>Sandwish</Category>" +
                "<Quantity>2</Quantity>" +
                "<KDSStation>1:0</KDSStation>" +
                "<PreModifier>" +
                "<Count>2</Count>" +
                "<S0>Pre-Modifier 0</S0>" +
                "<S1>Pre-Modifier 1</S1>" +
                "</PreModifier>" +
                "<Color  BG=\"108\" FG=\"120\"></Color>" +
                "<Condiment>" +
                "<ID>210</ID>" +
                "<PreModifier>" +
                "<Count>1</Count>" +
                "<S0>Seat 1</S0>" +
                "</PreModifier>" +
                "<TransType>1</TransType>" +
                "<Name>Spicy</Name>" +
                "<Color  BG=\"128\" FG=\"20\"></Color>" +
                "<Action>1</Action>" +
                "</Condiment>" +
                "<Condiment>" +
                "<ID>220</ID>" +
                "<TransType>1</TransType>" +
                "<Name>tomato</Name>" +
                "<Color BG=\"8\" FG=\"213\"></Color>" +
                "<Action>-1</Action>" +
                "</Condiment>" +
                "</Item>" +
                "</Order>" +
                "</Transaction>";
        return xml;
    }

}

