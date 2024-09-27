package com.luck.picture.lib.engine;

import android.net.Uri;
import android.util.Log;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class OpenGalleryEngine {
    public ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    public OpenGalleryEngine(AppCompatActivity activity){
        pickMedia = activity.registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri uri) {
                if (uri != null) {
                    Log.e("qwer", "Selected URI: $uri");
                } else {
                    Log.e("qwer", "No media selected");
                }
            }
        });
    }

    protected void dispatchTransformResult(){}
}
