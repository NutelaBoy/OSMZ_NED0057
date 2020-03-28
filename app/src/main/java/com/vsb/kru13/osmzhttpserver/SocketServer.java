package com.vsb.kru13.osmzhttpserver;

import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;


public class SocketServer extends Thread {

    ServerSocket serverSocket;
    public final int port = 12345;
    boolean bRunning;
    private Handler handler;
    private int numberOfThreads;
    private Semaphore semaphore;
    private byte[] data;

    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.d("SERVER", "Error, probably interrupted in accept(), see log");
            e.printStackTrace();
        }
        bRunning = false;
    }

    public SocketServer(Handler handler, String numOfThreads) {
        this.handler = handler;
        this.numberOfThreads = Integer.valueOf(numOfThreads);
        this.semaphore = new Semaphore(numberOfThreads);
    }


    public void run() {
        try {
            Log.d("SERVER", "Creating Socket");
            serverSocket = new ServerSocket(port);
            bRunning = true;

            while (bRunning) {
                Log.d("SERVER", "Socket Waiting for connection");
                Socket s = serverSocket.accept();
                if(semaphore.tryAcquire()){
                    Thread thread = new ClientThreads(s,handler,semaphore);
                    thread.start();
                    Log.d("semaphore", "Start");
                }else{
                    Log.d("semaphore", "FULL");
                }

            }
        }
        catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed())
                Log.d("SERVER", "Normal exit");
            else {
                Log.d("SERVER", "Error");
                e.printStackTrace();
            }
        }
        finally {
            serverSocket = null;
            bRunning = false;
        }
    }

}

