//=============================================================================
// Filename:    MTPoolServer.java
// Author:      Prasad V Lokam
// Date:        Nov.30.2019
// Description: This is the main server program that implements a thread pool
//              and invokes the ClientHandler upon accepting a Socket connection
//              from a Cleint in a Thread.
// 
//              Compile using the command: javac -cp ../.. MTPoolServer.java
//              Execute this using: java -cp ../.. DMaas.server.MTPoolServer
//
//              We execute the above 2 commands while in the Directory
//              /Dmaas/server
//              It will be a good idea to package this into a Nice Maven Build 
//              Package and use that for Build.
//==============================================================================
package dmaas.server;

import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.InputStream;
import java.io.OutputStream;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import javax.lang.model.util.ElementScanner6;
import dmaas.common.ClientHandler;

public class MTPoolServer implements Runnable {

    protected int serverPort = 8090;
    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;
    protected Thread runningThread = null;
    protected int poolSize = 3;

    public static void main(String[] args) throws IOException {

        int PORT_NO = 8090;
        MTPoolServer server = new MTPoolServer(PORT_NO);
        System.out.println("Starting the Multi Thread Pool Server on Port No:" + PORT_NO);

        new Thread(server).start();

        try {
            System.out.println("About to go to Sleep");
            Thread.sleep(20 * 100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Stopping Multi Thread Pool Server");
        server.stop();
    }

    protected ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);

    public MTPoolServer(int port) {
        this.serverPort = port;
    }

    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();
        while (!isStopped()) {
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
                System.out.println("Just Accepted a Client Connection");
            } catch (IOException e) {
                if (isStopped()) {
                    System.out.println("Server Stopped.");
                    break;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
            this.threadPool.execute(new WorkerRunnable(clientSocket, "Thread Pooled Server"));
            System.out.println("Just Completed Spinning off a WorkerRunnable Thread");
        }
        this.threadPool.shutdown();
        System.out.println("Server Stopped.");
    }

    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop() {
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
            System.out.println("Just Created a Server Socket");
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port" + this.serverPort, e);
        }
    }

}

class WorkerRunnable implements Runnable {

    protected Socket clientSocket = null;
    protected String serverText = null;

    public WorkerRunnable(Socket clientSocket, String serverText) {
        this.clientSocket = clientSocket;
        this.serverText = serverText;
    }

    public void run() {
        try {
            // Capture the Start Time at the begining of this Thread
            Instant start = Instant.now();

            // obtaining input and out streams
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());

            System.out.println("Assigning new thread for this client");

            // create a new ClientHandler and invoking the Process Method
            ClientHandler t = new ClientHandler(clientSocket, dis, dos);

            // Invoking the Process() method
            t.process();

            // output.write(("HTTP/1.1 200 OK\n\nWorkerRunnable: " + this.serverText + " - "
            // + time + "").getBytes());
            dis.close();
            dos.close();

            // Capture the Duration at the end of this Thread
            Instant finish = Instant.now();
            System.out.println("Total execution time for this ClientHandler Thread in milli seconds: "
                    + Duration.between(start, finish).toMillis());

        } catch (IOException e) {
            // report exception somewhere.
            e.printStackTrace();
        }
    }
}
