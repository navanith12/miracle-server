//=============================================================================
// Filename:    MyFile.java
// Author:      Prasad V Lokam
// Date:        Nov.30.2019
// Description: This Class is used by both the Client and Server Executables.
//              Contains the RxFile and TxFile and the RxLargeFile and TxLargeFile
// 
//              Compile using the command: javac -cp ../.. MyFile.java
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


public class MyFile {
    String filename = "";
    int fileSize = 0;
    static final int MAX_BUF_SIZE = 25000;
    static final int TCP_BUF_SIZE = 8192;

//    static final int MAX_BUF_SIZE = 8192;
//    static final int TCP_BUF_SIZE = 8192;

    // Constructor
    public MyFile(final String filename, final int fileSize) {
        this.filename = filename;
        this.fileSize = fileSize;
    }

    public Boolean LargeFile() {
        if (this.fileSize > this.MAX_BUF_SIZE) {
            //System.out.println(this.filename + " of size " + this.fileSize + " Bytes is Large");
            return true;
        } else {
            return false;
        }
    }

    
    public int RxFile(InputStream is) {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        int bytesRead = 0, current = 0;

        System.out.println("Rx File Filename: " + this.filename + "Size: " + this.fileSize);

        try {
            // Open the Output file and create a BufferredOutPutStream
            fos = new FileOutputStream(this.filename);
            bos = new BufferedOutputStream(fos);

            final byte[] mybytearray = new byte[fileSize];

            // Read the Buffer from the Socket InputStream
            bytesRead = is.read(mybytearray, 0, mybytearray.length);

            current = bytesRead;
            do {
                bytesRead = is.read(mybytearray, current, (mybytearray.length - current));
                if (bytesRead >= 0)
                    current += bytesRead;
            } while (current < fileSize);
            bos.write(mybytearray, 0, current);
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null)
                    bos.close();
                if (fos != null)
                    fos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
        return (1);
    }

   
    public static String print(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for (byte b : bytes) {
            sb.append(String.format("0x%02X ", b));
        }
        sb.append("]");
        System.out.println("String Buffer "+sb.toString());
        return sb.toString();
    }

    public int RxLargeFile(InputStream is) {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        int bytesRead = 0, current = 0;
        int curBuf = 0,buffers=0;
        byte[] bufCnt = new byte[6];
        boolean flag = true;

        System.out.println("Rx Large File Filename: " + this.filename + " Size: " + this.fileSize);

        try {
        	
        	//check whether the file already existing or not in server
        	File sFile = new File(this.filename);
        	
        	boolean alreadyExisting  = sFile.exists();  
        	
        	byte[] existByteArray = null;
        	
        	InputStream filIns = null;
        	
        	int exisBytesRead = 0;
        	
        	int exiCurr = 0;  
        	
        	boolean skipFlag = false;
        	
        	 if(alreadyExisting) {
             	
        		 existByteArray  = new byte[MyFile.TCP_BUF_SIZE];
             	
        		 filIns = new FileInputStream(this.filename); 
        		 
        		 skipFlag = true;
             }
        	
            // Open the Output file and create a BufferredOutPutStream
            fos = new FileOutputStream(this.filename);
            bos = new BufferedOutputStream(fos);

            final byte[] mybytearray = new byte[this.TCP_BUF_SIZE];

            // Estimate the No. of Buffers to Receive from the Client
            buffers = (this.fileSize/this.TCP_BUF_SIZE)+1;

            do {
                System.out.println("Loop::Buffers Cnt: " + buffers);
                
                // Read the CurrentBuffer Id from the Socket InputStream
                bytesRead = is.read(bufCnt, 0, 6);
                // print(bufCnt);
                curBuf = Integer.parseInt(new String(bufCnt));
                System.out.println("Current Buffer received from Client: " + curBuf);

                // Read the Buffer from the Socket InputStream of MAX_BUF_SIZE
                bytesRead = is.read(mybytearray, 0, this.TCP_BUF_SIZE);
                System.out.println("Bytes read in First Try :" + bytesRead);
                // if the Total size of expected buffer is read then dont go into the loop
                if (bytesRead == TCP_BUF_SIZE) flag = false;

                current = bytesRead;
                while (flag) {
                    System.out.println("In the Loop");
                    bytesRead = is.read(mybytearray, current, (this.TCP_BUF_SIZE - current));
                    System.out.println("RxLargeFile::Bytes Read in Loop :" + bytesRead + " Current: " + current);
                    if (bytesRead > 0) {
                        current += bytesRead;
                    }
                    else {
                        System.out.println("NO Bytes Read is Loop");
                        flag = false;
                    }
                }
                
                if(alreadyExisting && skipFlag) {
                	
                	exisBytesRead = filIns.read(existByteArray ,exiCurr, this.TCP_BUF_SIZE);
                 	
                 	if(exisBytesRead > 0 ) {
                 		
                 		exiCurr += exisBytesRead;
                 	}else {
                 		skipFlag = false;
                 		
                 		 bos.write(mybytearray, 0, bytesRead);
                         System.out.println("Written the " + curBuf + "Buffer to Output Stream File ");
                         bos.flush();
                 	}
                	
                }else {
                	
                	 bos.write(mybytearray, 0, bytesRead);
                     System.out.println("Written the " + curBuf + "Buffer to Output Stream File ");
                     bos.flush();
                }         

                // Reduce the count of Buffers as we have already received 1 Buffer
                buffers--;
            } while (buffers > 0);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null)
                    bos.close();
                if (fos != null)
                    fos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }    
        return(1);
    }
   
}
