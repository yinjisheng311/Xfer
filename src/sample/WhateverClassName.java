package sample;

import Client.CP2Client;
import Server.ServerClassCP2MultiThread;

/**
 * Created by G on 21/5/17.
 */
public class WhateverClassName {
    public static void main(String[] args) {
        Runnable server = new ServerClassCP2MultiThread();
        new Thread(server).start();
//        Runnable client = new CP2Client();
//        new Thread(client).start();

    }
}
