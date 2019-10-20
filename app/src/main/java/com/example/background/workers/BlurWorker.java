package com.example.background.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.background.Constants;
import com.example.background.R;

public class BlurWorker extends Worker {

    public BlurWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    private static final String TAG = BlurWorker.class.getSimpleName();

    @NonNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();
        String resourceUri = getInputData().getString(Constants.KEY_IMAGE_URI);

        try {
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input uri");
                throw new IllegalArgumentException("Invalid input uri");
            }
            ContentResolver resolver = applicationContext.getContentResolver();
            // Create a bitmap
            Bitmap picture = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri)));
//            Bitmap picture = BitmapFactory.decodeResource(
//                    applicationContext.getResources(),
//                    R.drawable.test);

            Bitmap bluredBitmap = WorkerUtils.blurBitmap(picture, applicationContext);

            Uri uriBluredImage = WorkerUtils.writeBitmapToFile(applicationContext, bluredBitmap);

            WorkerUtils.makeStatusNotification("Image is blurred" + "Output is "
                    + uriBluredImage.toString(), applicationContext);

            Data outputData = new Data.Builder()
                    .putString(Constants.KEY_IMAGE_URI, uriBluredImage.toString())
                    .build();

            return Result.success(outputData);

        } catch (Throwable error) {
            Log.e(TAG, "Error applying blur", error);

            return Result.failure();
        }
    }
}
