package org.lcsr.teleoperate.tcp;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;


/**
 * Created by qian on 1/26/16.
 */
public class MainActivity extends Activity {
    private static String TAG = "MainActivity";

    private LinearLayout mainLayout;
    private PopupWindow popupWindow;
    private View popupWindowView;

    private Button confirmBtn;
    private EditText hostnameEdit;

    private int width = 639;
    private int height = 479;
    private int clickDistance = 2;
    private int downX, downY, lastX, lastY, downScreenX, downScreenY, screenX, screenY;
    private int moveCount = 0;
    private boolean justClicked = false;
    private int seq = 0;

    private String defHostname = "10.189.175.110";
    private Handler clientHandler;
    private MyClient client;
    private Bundle clientBundle;
    private boolean clientStarted = false;
    private boolean clientInfoSet = false;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main);

        mainLayout = (LinearLayout) this.findViewById(R.id.main_layout);

        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupWindowView = inflater.inflate(R.layout.hostname_window, (ViewGroup) findViewById(R.id.popup_view));
        popupWindow = new PopupWindow(popupWindowView, 400, 200, true);


        confirmBtn = (Button) popupWindowView.findViewById(R.id.confirmButton);
        hostnameEdit = (EditText) popupWindowView.findViewById(R.id.editText);
        hostnameEdit.setText(defHostname);


        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempHostname = hostnameEdit.getText().toString();
                client = new MyClient(tempHostname, 18944);
                client.start();
                clientInfoSet = true;
                defHostname = tempHostname;
                popupWindow.dismiss();
                Log.i(TAG, "Hostname confirmed");
            }
        });


        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (clientStarted) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        downX = x;
                        downY = y;
                        lastX = x;
                        lastY = y;
                        downScreenX = screenX;
                        downScreenY = screenY;
                        moveCount = 0;
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        moveCount += 1;
                        if (moveCount > 7)
                            justClicked = false;
                        screenX = screenX + x - lastX;
                        screenY = screenY + y - lastY;
                        lastX = x;
                        lastY = y;
                        if (screenX < 0) { screenX = 0;}
                        else if (screenX > width) { screenX = width;}
                        if (screenY < 0) { screenY = 0;}
                        else if (screenY > height) { screenY = height;}
                        sendMessage(1, screenX + ";" + screenY + ";-1;");
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        // click
                        if ((x - downX < clickDistance && downX - x < clickDistance)
                                && (y - downY < clickDistance && downY - y < clickDistance)
                                && moveCount < 7 && (!justClicked)) {
                            sendMessage(1, downScreenX + ";" + downScreenY + ";1;");
                            justClicked = true;
                        }
                        moveCount = 0;
                        v.performClick();
                    }
                } else {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (!clientInfoSet) {
                            popupWindow.showAtLocation(popupWindowView, Gravity.CENTER, 0, 0);
                            Log.i(TAG, "window popup");
                        }
                        else {
                            clientStarted = true;
                            clientHandler = client.getClientHandler();
                            clientBundle = new Bundle();
                            sendMessage(0, "");
                        }
                    }
                }
                return true;
            }
        });

        Log.i(TAG, "onCreate");
    }

    public void sendMessage(int what, String text) {
        if ( !clientStarted ) {
            Log.i(TAG, "client hasn't started");
            return;
        }
        Log.i(TAG, text);
        seq += 1;
        Message clientMessage = clientHandler.obtainMessage();
        clientBundle.putString("message", text + seq + ";");
        clientMessage.setData(clientBundle);
        clientMessage.what = what;
        clientHandler.sendMessage(clientMessage);
    }

    public void onResume(){
        super.onResume();
        Log.i(TAG, "onResume");
    }

    public void onPause(){
        super.onPause();
        Log.i(TAG, "onPause");
    }

    public void onStop(){
        if (clientStarted){
            Message clientMessage = clientHandler.obtainMessage();
            clientMessage.what = -1;
            clientHandler.sendMessage(clientMessage);
        }
        super.onStop();
        Log.i(TAG, "onStop");
    }




}
