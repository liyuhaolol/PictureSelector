package com.luck.pictureselector.newlib;

import android.app.Activity;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.luck.picture.lib.entity.LocalMedia;
import com.luck.pictureselector.newlib.out.PicChooser;

import java.util.ArrayList;

public class AndroidGalleryEngine {
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    public AndroidGalleryEngine(AppCompatActivity activity){
        if (pickMedia == null){
            pickMedia = activity.registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    PicChooser picChooser = PicChooser.getInstance(activity);
                    if (picChooser.cropFileEngine != null){
                        Log.e("qwer","去裁剪");
                    }else if(picChooser.compressFileEngine != null){

                    }else{
                        if (picChooser.callback != null){
                            LocalMedia media = PicChooser.getInstance(activity).buildLocalMedia(activity,uri.toString());
                            ArrayList<LocalMedia> list = new ArrayList<>();
                            list.add(media);
                            picChooser.callback.onResult(list);
                        }
                    }
                } else {
                    Log.d("PhotoPicker", "No media selected");
                }
            });
        }
    }

    public void launch(){
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

}
