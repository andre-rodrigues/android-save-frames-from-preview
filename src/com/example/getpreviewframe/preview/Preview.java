package com.example.getpreviewframe.preview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Preview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
	private Camera mCamera;
	private SurfaceHolder mHolder;
	private String framesDir;
    private int width;
    private int height;
    private int framesCount = 0;
    private long initialTime = 0;
    private long endTime = 0;
    
    public Preview(Context context, Camera camera) {
    	super(context);
    	this.mCamera = camera;
    	
    	initialTime = System.currentTimeMillis(); 
    	
    	Parameters p = camera.getParameters();
    	this.width = p.getPreviewSize().width;
    	this.height = p.getPreviewSize().height;
    	
    	// Install a SurfaceHolder.Callback so we get notified when the
    	// underlying surface is created and destroyed.
    	mHolder = getHolder();
    	mHolder.addCallback(this);
    	// deprecated setting, but required on Android versions prior to 3.0
    	mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
    	
    	framesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/frames";
    	File path = new File(framesDir);
    	if (!path.exists()) {
    		path.mkdirs();
    	}
    }

	public long getInitialTime() {
		return initialTime;
	}

	public void setInitialTime(long initialTime) {
		this.initialTime = initialTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {
		// If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            initialTime = System.currentTimeMillis();
            mCamera.setPreviewCallback(this);

        } catch (Exception e){
            Log.d("Preview", "Error starting camera preview: " + e.getMessage());
        }
		
	}

	@SuppressLint("NewApi")
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            initialTime = System.currentTimeMillis();
            mCamera.setPreviewCallback(this);
        } catch (IOException e) {
            Log.d("Preview", "Error setting camera preview: " + e.getMessage());
        }
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        ByteArrayOutputStream outstr = new ByteArrayOutputStream();
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        
        // outstr contains image in jpeg
        Rect rect = new Rect(0, 0, width, height);
        yuvimage.compressToJpeg(rect, 25, outstr);
        
        framesCount++;
    	File frame = new File(framesDir + "/frame-" + String.format("%03d", framesCount) + ".jpeg");
		try {
			FileOutputStream fos = new FileOutputStream(frame.getPath());
			outstr.writeTo(fos);
			
			fos.close();
		} catch (java.io.IOException e) {
			Log.e("Preview frame", "Can't save frame", e);
		}
    }
	
	public float getPreviewFps() {
		long end = (endTime == 0) ? System.currentTimeMillis() : endTime;
		float spentSeconds = (end - initialTime) / 1000;
		return framesCount / spentSeconds;
	}
}