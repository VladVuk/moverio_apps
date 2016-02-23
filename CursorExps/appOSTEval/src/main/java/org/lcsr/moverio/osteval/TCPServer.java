package org.lcsr.moverio.osteval;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by qian on 1/31/16.
 */
public class TCPServer  {
    private final static String TAG = "TCPServer";
    private boolean status = false;
    private Handler uiHandler;
    private int port;
    private TCPAction act;

    public TCPServer(int p){
        port = p;
        Log.i(TAG, "constructed");
    }

    public boolean getStatus(){
        return status;
    }

    public void setHandler(Handler h){
        uiHandler = h;
    }

    public void startTask(){
        act = new TCPAction();
        status = true;
        act.execute(new Void[0]);
    }

    public void stopTask(){
        act.cancel(true);
        status = false;
    }

    public class TCPAction extends AsyncTask<Void, Void, Void> {
        private ServerSocket ss;
        private Socket s;
        private BufferedReader in;
//        private Bundle u;


        public TCPAction(){
            ;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                ss = new ServerSocket(port);
                s = ss.accept();
                Log.i(TAG, "accept");
                in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String incomingMsg;
                while ((incomingMsg = in.readLine()) != null ){
                    Bundle u = new Bundle();
                    final String finalMessage = incomingMsg;
                    String[] slist = finalMessage.split(";");
                    if ( slist.length >= 4 ) {
                        Message m = uiHandler.obtainMessage();
                        m.what = 1;
                        u.putInt("x", Integer.parseInt(slist[0]));
                        u.putInt("y", Integer.parseInt(slist[1]));
                        u.putInt("z", Integer.parseInt(slist[2]));
                        u.putInt("s", Integer.parseInt(slist[3]));
                        m.setData(u);
                        uiHandler.sendMessage(m);
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            try {
                s.close();
                Log.i(TAG, "cancelled");
            }
            catch (Exception e){
                e.printStackTrace();
            }
            super.onCancelled();
        }
    }


}
