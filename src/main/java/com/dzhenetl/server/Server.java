package com.dzhenetl.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    ExecutorService pool = Executors.newFixedThreadPool(64);

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(9999)) {
            while (true) {
                Socket socket = serverSocket.accept();
                pool.execute(new ServerThread(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
