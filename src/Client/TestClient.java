package Client;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by G on 21/5/17.
 */
//public class TestClient {
//    public static void main(String[] args) throws Exception {
//        System.out.println("Starting ...");
//        SocketAddress sA = new InetSocketAddress("192.168.11.1", 3451);
//        Socket s = new Socket();
//        s.connect(sA, 8080);
//        System.out.println("Connected");
//
//    }
//}

////code for client
//import java.io.*;
//import java.net.*;
//
//public class TestClient
//{
//    Socket s;
//    DataInputStream din;
//    DataOutputStream dout;
//    public TestClient()
//    {
//        System.out.println("Client started");
//        try
//        {
//            //s=new Socket("10.10.0.3,10");
//            s=new Socket("192.168.11.1",10);
//            System.out.println(s);
//            din= new DataInputStream(s.getInputStream());
//            dout= new DataOutputStream(s.getOutputStream());
//            ClientChat();
//        }
//        catch(Exception e)
//        {
//            System.out.println(e);
//        }
//    }
//    public void ClientChat() throws IOException
//    {
//        BufferedReader br= new BufferedReader(new InputStreamReader(System.in));
//        String s1;
//        do
//        {
//            s1=br.readLine();
//            dout.writeUTF(s1);
//            dout.flush();
//            System.out.println("Server Message:"+din.readUTF());
//        }
//        while(!s1.equals("stop"));
//    }
//    public static void main(String as[])
//    {
//        new TestClient();
//    }
//}

public class TestClient{
    public static void main(String[] args) throws Exception {
//        System.out.println("Server Started ... ");
//        int portnum = 3451;
//        ServerSocket s = new ServerSocket(portnum);
//        Socket temp = s.accept();
//        System.out.println("Client Connected!");
//        Runnable client = new CP2Client();
//        new Thread(client).start();
    }
}