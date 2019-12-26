/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bene.wintexasholdemfinal.object_detection;

import android.app.Fragment;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.bene.wintexasholdemfinal.R;
import com.bene.wintexasholdemfinal.object_detection.env.ImageUtils;

public abstract class CameraActivity extends AppCompatActivity
        implements Camera.PreviewCallback,
        View.OnClickListener {

    protected int previewWidth = 0;
    protected int previewHeight = 0;
    private Handler handler;
    private HandlerThread handlerThread;
    private boolean isProcessingFrame = false;
    private int[] rgbBytes = null;
    private Runnable postInferenceCallback;
    private Runnable imageConverter;
    private TextView pred_view;
    protected boolean switchBack = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setFragment();  // requires camera permission

        pred_view = findViewById(R.id.predictionView);
        Button capt_img = findViewById(R.id.captBtn);
        capt_img.setOnClickListener(this);
    }

    protected int[] getRgbBytes() {
        imageConverter.run();
        return rgbBytes;
    }

    @Override
    public void onPreviewFrame(final byte[] bytes, final Camera camera) {
        if (isProcessingFrame) {
            return;
        }

        try {
            // Initialize the storage bitmaps once when the resolution is known.
            if (rgbBytes == null) {
                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                previewHeight = previewSize.height;
                previewWidth = previewSize.width;
                rgbBytes = new int[previewWidth * previewHeight];
                onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }

        isProcessingFrame = true;

        imageConverter = () -> ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);

        postInferenceCallback = () -> {
            camera.addCallbackBuffer(bytes);
            isProcessingFrame = false;
        };
        processImage();
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    protected void setFragment() {
        Fragment fragment = new LegacyCameraConnectionFragment(this, getLayoutId(), getDesiredPreviewFrameSize());
        getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    protected void readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        }
    }

    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.captBtn) {
            v.setClickable(false);
            readyForNextImage();
            switchBack = true;
        }
    }

    protected void setPredictionView(String prediction) {
        pred_view.setText(prediction);
    }

    protected abstract void processImage();

    protected abstract void onPreviewSizeChosen(final Size size, final int rotation);

    protected abstract int getLayoutId();

    protected abstract Size getDesiredPreviewFrameSize();

    @Override
    public synchronized void onStart() {
        super.onStart();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public synchronized void onPause() {
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        super.onPause();
    }

    @Override
    public synchronized void onStop() {
        super.onStop();
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
    }
}
