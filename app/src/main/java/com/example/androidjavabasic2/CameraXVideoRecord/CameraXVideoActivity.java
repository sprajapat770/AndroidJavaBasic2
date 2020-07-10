package com.example.androidjavabasic2.CameraXVideoRecord;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.VideoCaptureConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.androidjavabasic2.R;

import java.io.File;

public class CameraXVideoActivity extends AppCompatActivity {
    VideoView vidView;
    ImageView imgFlip, imgClick;
    TextureView viewFinder;
    static final int REQUEST_VIDEO_RECORD = 12;
    String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    File file;
    private CameraX.LensFacing lensFacing = CameraX.LensFacing.BACK;
    boolean rec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerax_video);
        findViewById();
        init();
    }

    private void findViewById() {

        imgClick = findViewById(R.id.imgClick);
        imgFlip = findViewById(R.id.imgFlip);
        viewFinder = findViewById(R.id.viewFinder);
        vidView = findViewById(R.id.vidView);
    }

    private void init() {
        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(CameraXVideoActivity.this, REQUIRED_PERMISSIONS, REQUEST_VIDEO_RECORD);
        }

        imgFlip.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                if (CameraX.LensFacing.FRONT == lensFacing) {
                    lensFacing = CameraX.LensFacing.BACK;
                } else {
                    lensFacing = CameraX.LensFacing.FRONT;
                }
                try {
                    if (allPermissionsGranted()) {
                        CameraX.getCameraWithLensFacing(lensFacing);
                        startCamera(); //start camera if permission has been granted by user
                    } else {
                        ActivityCompat.requestPermissions(CameraXVideoActivity.this, REQUIRED_PERMISSIONS, REQUEST_VIDEO_RECORD);
                    }

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressLint("RestrictedApi")
    public void startCamera() {
        CameraX.unbindAll();

        Rational aspectRatio = new Rational(viewFinder.getWidth(), viewFinder.getHeight());
        Size screen = new Size(viewFinder.getWidth(), viewFinder.getHeight()); //size of the screen

        Rational aspectRatiovideoView = new Rational(viewFinder.getWidth(), viewFinder.getHeight());
        Size screenvideoView = new Size(viewFinder.getWidth(), viewFinder.getHeight()); //size of the screen

        final PreviewConfig pConfig = new PreviewConfig.Builder().setLensFacing(lensFacing).setTargetAspectRatio(aspectRatio).setTargetResolution(screen).build();
        final Preview preview = new Preview(pConfig);

        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            //to update the surface texture we  have to destroy it first then re-add it
            @Override
            public void onUpdated(final Preview.PreviewOutput output) {
                final ViewGroup parent = (ViewGroup) viewFinder.getParent();
                parent.removeView(viewFinder);
                parent.addView(viewFinder, 0);

                viewFinder.setSurfaceTexture(output.getSurfaceTexture());
                updateTransform();
            }
        });


        VideoCaptureConfig videoCaptureConfig = new VideoCaptureConfig.Builder().setLensFacing(lensFacing).setTargetAspectRatio(aspectRatiovideoView).setTargetResolution(screenvideoView).build();

        final VideoCapture vidCap = new VideoCapture(videoCaptureConfig);
        // file = new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".mp4");
        file = new File(Environment.getExternalStorageDirectory() + "/" + "recordVideo" + ".mp4");

        imgClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (!rec) {
                    rec = true;
                    imgFlip.setEnabled(false);
                    vidCap.startRecording(file, new VideoCapture.OnVideoSavedListener() {
                        @Override
                        public void onVideoSaved(File file) {
                            final String str = file.getAbsolutePath();
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    showVideo(str);
                                }
                            });
                        }


                        @Override
                        public void onError(VideoCapture.UseCaseError useCaseError, String message, @Nullable Throwable cause) {
                            Log.e("nothing", message);
                        }
                    });

                } else {
                    rec = false;
                    vidCap.stopRecording();
                    imgFlip.setEnabled(true);
                }
            }
        });

        CameraX.bindToLifecycle(this, preview, vidCap);
    }

    private void updateTransform() {
        Matrix mx = new Matrix();
        float w = viewFinder.getMeasuredWidth();
        float h = viewFinder.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int) viewFinder.getRotation();

        switch (rotation) {
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float) rotationDgr, cX, cY);
        viewFinder.setTransform(mx);
    }

    private void showVideo(String str) {
        Uri vidUri = Uri.parse(str);
        vidView.requestFocus();
        vidView.setVideoURI(vidUri);
        vidView.start();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_VIDEO_RECORD) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(getApplicationContext(), "You Don't have permission to gallery access", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private boolean allPermissionsGranted() {

        for (String permission : new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}