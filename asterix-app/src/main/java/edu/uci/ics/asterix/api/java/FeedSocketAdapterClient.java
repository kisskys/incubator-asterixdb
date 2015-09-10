package edu.uci.ics.asterix.api.java;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class FeedSocketAdapterClient {
    
    private static final int OUTPUT_BUFFER_SIZE = 32* 1024; 
    private String adapterUrl;
    private int port;
    private Socket socket;
    private String sourceFilePath;
    private int recordCount;
    private int maxCount;
    private ByteBuffer outputBuffer = ByteBuffer.allocate(OUTPUT_BUFFER_SIZE);
    private OutputStream out = null;
    
    public FeedSocketAdapterClient(String adapterUrl, int port, String sourceFilePath, int maxCount) {
        this.adapterUrl = adapterUrl;
        this.port = port;
        this.sourceFilePath = sourceFilePath;
        this.maxCount = maxCount;
    }
    
    public void initialize() {
        try {
            socket = new Socket(adapterUrl, port);
        } catch (IOException e) {
            System.err.println("Problem in creating socket against host "+adapterUrl+" on the port "+port);
            e.printStackTrace();
        }
    }
    
    public void finalize() {
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Problem in closing socket against host "+adapterUrl+" on the port "+port);
            e.printStackTrace();
        }       
    }

    public void ingest() {
        recordCount = 0;
        BufferedReader br = null;
        try {
            out = socket.getOutputStream();
            br = new BufferedReader(new FileReader(sourceFilePath));
            String nextRecord;
            byte[] b = null;
            byte[] newLineBytes = "\n".getBytes();

            while ((nextRecord = br.readLine()) != null) {
                b = nextRecord.getBytes();
                if (outputBuffer.position() + b.length > outputBuffer.limit() - newLineBytes.length) {
                    outputBuffer.put(newLineBytes);
                    flush();
                    outputBuffer.put(b);
                } else {
                    outputBuffer.put(b);
                }
                recordCount++;
                if (recordCount == maxCount) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void flush() throws IOException {
        outputBuffer.flip();
        out.write(outputBuffer.array(), 0, outputBuffer.limit());
        outputBuffer.position(0);
        outputBuffer.limit(OUTPUT_BUFFER_SIZE);
    }

}
