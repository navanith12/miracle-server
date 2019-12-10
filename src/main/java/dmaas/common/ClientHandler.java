//=============================================================================
// Filename:    ClientHandler.java
// Author:      Prasad V Lokam
// Date:        Nov.30.2019
// Description: This Class is used by both the Server Executables.
//              This is the class that implements the CLient Handler that 
//              runs with in the Worker thread after accepting the socket.
// 
//              Compile using the command: javac -cp ../.. ClientHandler.java
//==============================================================================
package dmaas.common;

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
import dmaas.common.*;

// ClientHandler class
public class ClientHandler {
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd");
    DateFormat fortime = new SimpleDateFormat("hh:mm:ss");

    // Constructor
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
    }

    public void process() {
        String  received = "";
        String  toreturn = "";
        Boolean flag = true;
        String  fileName = "";
        int     fileSize = 0;
        String  cmdString = "";

        while (flag) {
             

            try {
                // Ask user what he wants
                dos.writeUTF("What do you want?[Date | Time | Tx]..\n" + "Type Exit to terminate connection.");

                // receive the Command from client
                cmdString = dis.readUTF();
                
                // Capture the Start Time at the begining of this Thread
                Instant start = Instant.now();

                // creating Date object
                Date date = new Date();
                System.out.println("Received the Command: " + cmdString);

                // write on output stream based on the
                // answer from the client
                switch (cmdString) {
                case "Date":
                    toreturn = fordate.format(date);
                    dos.writeUTF(toreturn);
                    break;

                case "Time":
                    toreturn = fortime.format(date);
                    dos.writeUTF(toreturn);
                    break;

                case "Tx":
                    fileName = dis.readUTF();
                    received = dis.readUTF();
                    fileSize = Integer.parseInt(received);
                    System.out.println("Recived: Filename: " + fileName+ " Filesize: " + fileSize);
                    MyFile fIn = new MyFile(fileName, fileSize);
                    if (fIn.LargeFile()) {
                        fIn.RxLargeFile(dis);
                    } else {
                        fIn.RxFile(dis);
                    }
                    toreturn = "Received the file :" + fileName + " Of size: " + fileSize;
                    dos.writeUTF(toreturn);
                    break;

                case "Rx":
                    System.out.println("Client is asking for Rx: Dont know what to do.:");
                    dos.writeUTF("Did not Know what to do with Rx");
                    break;

                case "Exit":
                    System.out.println("Client " + this.s + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.s.close();
                    System.out.println("Connection closed");
                    flag = false;
                    break;

                default:
                    dos.writeUTF("Invalid input");
                    break;
                }
                // Capture the Duration at the end of this Thread
                Instant finish = Instant.now();
                System.out.println("Total Time for executing " + cmdString + " in millis: "
                    + Duration.between(start, finish).toMillis());

            } catch (IOException e) {
                e.printStackTrace();
                flag = false;
            }
        }

        try {
            // closing resources
            this.dis.close();
            this.dos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

