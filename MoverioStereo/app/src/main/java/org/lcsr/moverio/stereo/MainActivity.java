package org.lcsr.moverio.stereo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private StereoRenderer mGLSurfaceView;
    private LinearLayout mainLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainLayout = (LinearLayout)findViewById(R.id.topLayout);
        mGLSurfaceView = new StereoRenderer(this);
        mainLayout.addView(mGLSurfaceView);
    }

    @Override
    protected void onResume()
    {

        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause()
    {

        super.onPause();
        mGLSurfaceView.onPause();
    }
}
