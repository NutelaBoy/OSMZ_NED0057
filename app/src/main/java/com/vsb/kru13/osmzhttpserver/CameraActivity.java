package com.vsb.kru13.osmzhttpserver;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.TimerTask;

public class CameraActivity extends Activity {

    private Camera mCamera;
    private FrameLayout frameLayout;
    private CamPreview camPreview;
    private Button captureImage;
    private boolean safeToTakePicture = true;
    public static byte[] imageInBytes;
    public static boolean streamingIsUp = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        frameLayout = (FrameLayout)findViewById(R.id.camera_preview);
        captureImage = (Button)findViewById(R.id.makePicture);
        Button stopStream = (Button)findViewById(R.id.stopStream);

        if(checkCameraHardware(this)){
            Toast.makeText(this,"Camera is ready", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this,"Camera is not ready", Toast.LENGTH_LONG).show();
        }

        mCamera = Camera.open();

        camPreview = new CamPreview(this, mCamera, safeToTakePicture);

        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timerTask.run();
            }
        });

        frameLayout.addView(camPreview);

        stopStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                streamingIsUp = false;
            }
        });
    }

    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            camPreview.getSafeToTakePicture();
            mCamera.setPreviewCallback(previewCallback);
            /*if (safeToTakePicture) {
                mCamera.takePicture(null, null, mPictureCallback);
                (new Handler()).postDelayed(this, 333);
                safeToTakePicture = false;
            }*/
        }
    };

    //Kod funguje, ale v timer tasku pouzivam onpreviewCallback
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File picture = getOutputMediaFile();
            imageInBytes = data;

            mCamera.startPreview();
            safeToTakePicture = true;


          /*  if(picture == null){
                safeToTakePicture = true;
                return;
            }else{

                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(picture);
                    fileOutputStream.write(data);
                    fileOutputStream.close();

                    mCamera.startPreview();
                    safeToTakePicture = true;

                }catch (IOException e){
                    e.printStackTrace();
                }

            }*/
        }
    };

    public static byte[] convertToImg(byte[] data, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();

        YuvImage image = new YuvImage(data, parameters.getPreviewFormat(), size.width, size.height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        image.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, out);
        byte[] imageBytes = out.toByteArray();
        return imageBytes;
    }

    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            File picture = getOutputMediaFile();

            imageInBytes = convertToImg(data, camera);


            try {


                mCamera.startPreview();


                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            safeToTakePicture = true;
        }
    };

    private File getOutputMediaFile() {
        String state = Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED)){
            return null;
        }else{
            File folderSnapshot = new File(Environment.getExternalStorageDirectory() + File.separator + "Snapshot");

            if(!folderSnapshot.exists()){
                folderSnapshot.mkdirs();
            }

            File outputFile = new File(folderSnapshot, "snap1.jpg");
            return outputFile;
        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        } else {
            return false;
        }
    }

}
