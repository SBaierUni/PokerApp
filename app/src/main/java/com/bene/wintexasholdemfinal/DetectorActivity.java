package com.bene.wintexasholdemfinal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Size;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bene.wintexasholdemfinal.env.ImageUtils;
import com.bene.wintexasholdemfinal.tflite.Classifier;
import com.bene.wintexasholdemfinal.tflite.TFLiteObjectDetectionAPIModel;

public class DetectorActivity extends CameraActivity {
    // Configuration values for the prepackaged SSD model.
    private static final int MODEL_INPUT_SIZE = 600;
    private static final boolean MODEL_IS_QUANTIZED = false;
    private static final String MODEL_FILE = "cards.tflite";
    private static final String LABELS_FILE = "file:///android_asset/labels.txt";
    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE = 0.5f;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);

    private Classifier detector;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Matrix frameToCropTransform;

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        try {
            detector = TFLiteObjectDetectionAPIModel.create(
                    getAssets(),
                    MODEL_FILE,
                    LABELS_FILE,
                    MODEL_INPUT_SIZE,
                    MODEL_IS_QUANTIZED);
        } catch (final IOException e) {
            e.printStackTrace();
            finish();
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        int sensorOrientation = rotation - getScreenOrientation();
        int cropSize = MODEL_INPUT_SIZE;

        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

        frameToCropTransform = ImageUtils.getTransformationMatrix(previewWidth, previewHeight,
                cropSize, cropSize, sensorOrientation, false);
    }

    @Override
    protected void processImage() {
        if (switchBack) {
            rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

            final Canvas canvas = new Canvas(croppedBitmap);
            canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);

            runInBackground(() -> {
                runOnUiThread(() -> setPredictionView("Performing recognition..."));

                final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
                final ArrayList<String> det_cards = new ArrayList<>();
                for (final Classifier.Recognition result : results) {
                    if (result.getConfidence() >= MINIMUM_CONFIDENCE && !det_cards.contains(result.getTitle()))
                        det_cards.add(result.getTitle());
                }

                // printing prediction on view
                runOnUiThread(() -> setPredictionView("Recognized: " + det_cards.toString()));
                switchBackToCallerActivity(det_cards);
            });
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment_tracking;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    private void switchBackToCallerActivity(ArrayList<String> prediction) {
        Intent i = new Intent();
        // Sending param key as 'website' and value as 'androidhive.info'
        i.putStringArrayListExtra("prediction", prediction);

        // Setting resultCode to 100 to identify on old activity
        setResult(100, i);

        finish();   // terminate activity and switch back to manual_entry_layout
    }
}
