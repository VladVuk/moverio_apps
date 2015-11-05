package org.lcsr.moverio.moverio_ros;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import org.ros.address.InetAddressFactory;
import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.RosActivity;
import org.ros.android.view.RosImageView;
import org.ros.android.view.camera.RosCameraPreviewView;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import sensor_msgs.CompressedImage;

public class MainActivity extends RosActivity {
    private int cameraId;
    private RosCameraPreviewView rosCameraPreviewView;
    private RosImageView<CompressedImage> rosImageView;
    private SensorPublisher sensorPublisher;
    private LinearLayout mainLayout;
    private Button switchCameraBtn;

    public MainActivity() {
        super("MOVERIO_ROS", "MOVERIO_ROS");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
        rosCameraPreviewView = (RosCameraPreviewView) findViewById(R.id.ros_camera_preview_view);
        rosImageView = (RosImageView<CompressedImage>) findViewById( R.id.ros_image_view);
        rosImageView.setTopicName("/moverio/view/compressed");
        rosImageView.setMessageType(CompressedImage._TYPE);
        rosImageView.setMessageToBitmapCallable(new BitmapFromCompressedImage());
        mainLayout = (LinearLayout) findViewById( R.id.main_layout );

        switchCameraBtn = (Button) findViewById( R.id.switch_camera_button);
        switchCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            int numberOfCameras = Camera.getNumberOfCameras();
            final Toast toast;
            if (numberOfCameras > 1) {
                cameraId = (cameraId + 1) % numberOfCameras;
                rosCameraPreviewView.releaseCamera();
                rosCameraPreviewView.setCamera(Camera.open(cameraId));
                toast = Toast.makeText(MainActivity.this, "Switching cameras.", Toast.LENGTH_SHORT);
            } else {
                toast = Toast.makeText(MainActivity.this, "No alternative cameras to switch to.", Toast.LENGTH_SHORT);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toast.show();
                }
            });
            }
        });

        sensorPublisher = new SensorPublisher( (SensorManager) this.getSystemService(Context.SENSOR_SERVICE) );
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        cameraId = 0;
        String hostAddress = InetAddressFactory.newNonLoopback().getHostAddress();
        Camera cam = Camera.open(cameraId);
        Camera.Parameters param = cam.getParameters();
//        param.setPictureSize(640,480);
        param.setPreviewFpsRange(10000, 15000);
//        param.setPreviewFrameRate(5);
        cam.setParameters(param);
        rosCameraPreviewView.setCamera(cam);
        NodeConfiguration ncCamera = NodeConfiguration.newPublic(hostAddress);
        ncCamera.setNodeName("camera");
        ncCamera.setMasterUri(getMasterUri());
        nodeMainExecutor.execute(rosCameraPreviewView, ncCamera);

        NodeConfiguration ncImage = NodeConfiguration.newPublic(hostAddress);
        ncImage.setMasterUri(getMasterUri());
        ncImage.setNodeName("moverio/view");
        nodeMainExecutor.execute(rosImageView, ncImage);

        NodeConfiguration ncSensor = NodeConfiguration.newPublic(hostAddress);
        ncSensor.setMasterUri(getMasterUri());
        ncSensor.setNodeName("moverio/sensor");
        nodeMainExecutor.execute(sensorPublisher, ncSensor);
    }

    @Override
    public void onResume(){
        super.onResume();
//        hideCameraPreview();
    }

    private void hideCameraPreview() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(10,10);
        rosCameraPreviewView.setLayoutParams(params);
    }
}
