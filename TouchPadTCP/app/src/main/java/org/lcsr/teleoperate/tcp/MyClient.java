package org.lcsr.teleoperate.tcp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by qian on 1/31/16.
 */
public class MyClient extends Thread {
    private final static String TAG = "MyClient";

    private Handler clientHandler;

    private String serverIpAddress;
    private int serverPort;

    private boolean connected = false;

    private Socket socket;
    private BufferedWriter bufferedWriter;

    public MyClient(String addr, int port){
        serverIpAddress = addr;
        serverPort = port;
        Log.i(TAG, "constructed");
    }

    public Handler getClientHandler(){
        return clientHandler;
    }


    @Override
    public void run(){
        Looper.prepare();

        clientHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                // Connect
                if(msg.what == 0){
                    if ( !connected ) {
                        try {
                            socket = new Socket(serverIpAddress, serverPort);
                            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                            Log.i(TAG, "Connected");
                            connected = true;
                        }
                        catch (Exception e) {
                            Log.e(TAG, "Connect error", e);
                            connected = false;
                        }
                    }
                }
                // Send message
                else if (msg.what == 1) {
                    try {
                        Bundle u = msg.getData();
                        bufferedWriter.write(u.getString("message"));
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                    catch (Exception e) {
                        Log.e(TAG, "Send error", e);
                    }
                }
                // Stop
                else if (msg.what == -1 ){
                    if (connected){
                        try{
                            socket.close();
                            Log.i(TAG, "Disconnected");
                        }
                        catch (Exception e) {
                            Log.e(TAG, "Stop error", e);
                            connected = true;
                        }
                    }

                }

            }
        };

        Looper.loop();
    }
}
