package org.lcsr.moverio.stereo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private static String TAG = "MainActivity";
    private StereoView mGLSurfaceView;
    private LinearLayout mainLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainLayout = (LinearLayout)findViewById(R.id.topLayout);
        mGLSurfaceView = new StereoView(this);
        mainLayout.addView(mGLSurfaceView);
        Log.i(TAG, "onCreate finish");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mGLSurfaceView.onResume();
        Log.i(TAG, "onResume finish");
    }

    @Override
    protected void onPause()
    {

        super.onPause();
        mGLSurfaceView.onPause();
    }
}
