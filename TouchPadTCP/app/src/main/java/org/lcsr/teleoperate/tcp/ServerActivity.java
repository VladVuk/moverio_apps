//package org.lcsr.teleoperate.tcp;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.os.Handler;
//import android.util.Log;
//import android.widget.TextView;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.net.SocketException;
//import java.util.Enumeration;
//
///**
// * Created by qian on 1/31/16.
// */
//public class ServerActivity extends Activity {
//
//    private TextView serverStatus;
//
//    // DEFAULT IP
//    public static String SERVERIP = "10.0.2.15";
//
//    // DESIGNATE A PORT
//    public static final int SERVERPORT = 8080;
//
//    private Handler handler = new Handler();
//
//    private ServerSocket serverSocket;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.server);
//        serverStatus = (TextView) findViewById(R.id.server_status);
//
//        SERVERIP = getLocalIpAddress();
//
//        Thread fst = new Thread(new ServerThread());
//        fst.start();
//    }
//
//    public class ServerThread implements Runnable {
//
//        public void run() {
//            try {
//                if (SERVERIP != null) {
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            serverStatus.setText("Listening on IP: " + SERVERIP);
//                        }
//                    });
//                    serverSocket = new ServerSocket(SERVERPORT);
//                    while (true) {
//                        // LISTEN FOR INCOMING CLIENTS
//                        Socket client = serverSocket.accept();
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                serverStatus.setText("Connected.");
//                            }
//                        });
//
//                        try {
//                            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
//                            String line = null;
//                            while ((line = in.readLine()) != null) {
//                                Log.d("ServerActivity", line);
//                                handler.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        // DO WHATEVER YOU WANT TO THE FRONT END
//                                        // THIS IS WHERE YOU CAN BE CREATIVE
//                                    }
//                                });
//                            }
//                            break;
//                        } catch (Exception e) {
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    serverStatus.setText("Oops. Connection interrupted. Please reconnect your phones.");
//                                }
//                            });
//                            e.printStackTrace();
//                        }
//                    }
//                } else {
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            serverStatus.setText("Couldn't detect internet connection.");
//                        }
//                    });
//                }
//            } catch (Exception e) {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        serverStatus.setText("Error");
//                    }
//                });
//                e.printStackTrace();
//            }
//        }
//    }
//
//    // GETS THE IP ADDRESS OF YOUR PHONE'S NETWORK
//    private String getLocalIpAddress() {
//        try {
//            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
//                NetworkInterface intf = en.nextElement();
//                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
//                    InetAddress inetAddress = enumIpAddr.nextElement();
//                    if (!inetAddress.isLoopbackAddress()) { return inetAddress.getHostAddress().toString(); }
//                }
//            }
//        } catch (SocketException ex) {
//            Log.e("ServerActivity", ex.toString());
//        }
//        return null;
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        try {
//            // MAKE SURE YOU CLOSE THE SOCKET UPON EXITING
//            serverSocket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//}