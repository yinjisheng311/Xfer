package Server;

import Client.CP2Client;
import Client.Client;
import sample.HomeController;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Created by nicholas on 21-May-17.
 */
public class TestServerClient {

    public static void main(String[] args) {
//        Runnable server = new ServerClassCP2MultiThread();
//        Runnable client = new CP2Client();

//        new Thread(server).start();
//        new Thread(client).start();

        Runnable server = new Server3(6666);
        Runnable server2 = new Server3(6665);
        ArrayList<File> test = new ArrayList<File>();

        test.add(new File("D:\\Backup\\SUTD\\ISTD\\Computer Systems Engineering\\CSE-Programming-Assignments\\CSE-Programming-Assignment-2\\Eclipse Project\\largeFile.txt"));
        test.add(new File("D:\\Backup\\SUTD\\ISTD\\Computer Systems Engineering\\Course Resources\\modernOperatingSystemsWithJava.pdf"));

        ArrayList<File> test1 = new ArrayList<File>();

        test1.add(new File("D:\\Backup\\SUTD\\ISTD\\Computer Systems Engineering\\Computer Networking A Top-Down Approach.pdf"));
        test1.add(new File("D:\\Backup\\SUTD\\ISTD\\Computer Systems Engineering\\CSE-Programming-Assignments\\CSE-Programming-Assignment-2\\Eclipse Project\\medianFile.txt"));
        test1.add(new File("D:\\Backup\\SUTD\\ISTD\\Computer Systems Engineering\\CSE-Programming-Assignments\\CSE-Programming-Assignment-2\\Eclipse Project\\smallFile.txt"));
        Runnable client = new Client("user",test , "localhost",6666);
        Runnable client1 = new Client("user1",test1,"localhost",6666);

        new Thread(server).start();
//        new Thread(server2).start();
        new Thread(client).start();
        new Thread(client1).start();
    }
}
