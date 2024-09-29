package com.luck.pictureselector.newlib;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.app.Activity;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.entity.LocalMedia;
import spa.lyh.cn.chooser.PicChooser;
import spa.lyh.cn.chooser.engine.OpenGalleryEngine;
import spa.lyh.cn.chooser.request.PickMultipleRequest;
import spa.lyh.cn.chooser.request.PickRequest;

import java.util.ArrayList;

public class AndroidGalleryEngine implements OpenGalleryEngine {
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;
    public PickMultipleRequest pickMultipleRequest;

    public AndroidGalleryEngine(AppCompatActivity activity){
        PicChooser picChooser = PicChooser.getInstance(activity);
        if (pickMedia == null){
            pickMedia = activity.registerForActivityResult(new PickRequest(), uri -> {
                if (uri != null) {
                    LocalMedia media = PicChooser.getInstance(activity).buildLocalMedia(activity,uri.toString());
                    picChooser.mediaList.add(media);
                    if (picChooser.cropFileEngine != null){
                        ArrayList<Uri> uris = new ArrayList<>();
                        uris.add(uri);
                        picChooser.cropFileEngine.onStartCrop(activity,uris);
                    }else if(picChooser.compressFileEngine != null){

                    }else{
                        if (picChooser.callback != null){
                            picChooser.callback.onResult(picChooser.mediaList);
                        }
                    }
                } else {
                    if (picChooser.callback != null){
                        picChooser.callback.onCancel();
                    }
                }
            });
        }

        if (pickMultipleMedia == null){
            pickMultipleRequest = new PickMultipleRequest(picChooser.maxSelectNum);
            pickMultipleMedia = activity.registerForActivityResult(pickMultipleRequest, uris -> {
                if (!uris.isEmpty()) {
                    for (Uri uri : uris){
                        LocalMedia media = PicChooser.getInstance(activity).buildLocalMedia(activity,uri.toString());
                        picChooser.mediaList.add(media);
                    }
                    if (picChooser.cropFileEngine != null){
                        picChooser.cropFileEngine.onStartCrop(activity,uris);
                    }else if(picChooser.compressFileEngine != null){

                    }else{
                        if (picChooser.callback != null){
                            picChooser.callback.onResult(picChooser.mediaList);
                        }
                    }
                } else {
                    if (picChooser.callback != null){
                        picChooser.callback.onCancel();
                    }
                }
            });
        }
    }

    @Override
    public void launch(Activity activity){
        PicChooser picChooser = PicChooser.getInstance(activity);
        if (picChooser.selectionMode == SelectModeConfig.MULTIPLE){
            pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                    .build());
        }else {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        }
    }

    @Override
    public void updateMaxItems(int maxSelectNum){
        if (pickMultipleRequest != null){
            pickMultipleRequest.updateMaxItems(maxSelectNum >0?maxSelectNum:1);
        }
    }

}
