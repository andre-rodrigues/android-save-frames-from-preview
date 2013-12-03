package com.example.getpreviewframe;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;

import com.example.getpreviewframe.preview.Preview;

public class MainActivity extends Activity {
	private Camera mCamera;
	private Preview mPreview;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mCamera = Camera.open();
		Parameters params = mCamera.getParameters();
		params.setPreviewFpsRange(29000, 29000);
		mCamera.setParameters(params);
		
		mPreview = new Preview(this, mCamera);
		addContentView(mPreview, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (mCamera == null) {
			mCamera = Camera.open();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
		}
	}
}