package com.luck.pictureselector.newlib;


import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener;
import com.luck.picture.lib.utils.SdkVersionUtils;

import spa.lyh.cn.chooser.MediaDataBuild;
import spa.lyh.cn.chooser.PicChooser;
import spa.lyh.cn.chooser.PicListData;
import spa.lyh.cn.chooser.engine.OpenGalleryEngine;
import spa.lyh.cn.chooser.request.PickMultipleRequest;
import spa.lyh.cn.chooser.request.PickRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class AndroidGalleryEngine implements OpenGalleryEngine {
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;
    public PickMultipleRequest pickMultipleRequest;
    private PickRequest pickRequest;
    private PicChooser picChooser;

    public AndroidGalleryEngine(Fragment fragment){
        if (pickMedia == null){
            pickRequest = new PickRequest(FileMimeType.getImageMimeType(),FileMimeType.getImageAndVideoMimeType());
            pickMedia = fragment.registerForActivityResult(pickRequest, uri -> {
                if (uri != null) {
                    LocalMedia media = MediaDataBuild.buildLocalMedia(fragment.requireActivity(),uri.toString());
                    PicListData.getInstance().mediaList.add(media);
                    if (picChooser.cropFileEngine != null){
                        ArrayList<Uri> uris = new ArrayList<>();
                        uris.add(uri);
                        picChooser.cropFileEngine.setPicChooser(picChooser);
                        picChooser.cropFileEngine.onStartCrop(fragment.requireActivity(),uris);
                    }else if(picChooser.compressFileEngine != null){
                        goCompress(fragment.requireActivity(),picChooser);
                    }else{
                        if (picChooser.callback != null){
                            picChooser.callback.onResult(PicListData.getInstance().mediaList);
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
            pickMultipleRequest = new PickMultipleRequest(2,FileMimeType.getImageMimeType(),FileMimeType.getImageAndVideoMimeType());
            pickMultipleMedia = fragment.registerForActivityResult(pickMultipleRequest, uris -> {
                if (!uris.isEmpty()) {
                    for (Uri uri : uris){
                        LocalMedia media = MediaDataBuild.buildLocalMedia(fragment.requireActivity(),uri.toString());
                        PicListData.getInstance().mediaList.add(media);
                    }
                    if (picChooser.cropFileEngine != null){
                        picChooser.cropFileEngine.setPicChooser(picChooser);
                        picChooser.cropFileEngine.onStartCrop(fragment.requireActivity(),uris);
                    }else if(picChooser.compressFileEngine != null){
                        goCompress(fragment.requireActivity(),picChooser);
                    }else{
                        if (picChooser.callback != null){
                            picChooser.callback.onResult(PicListData.getInstance().mediaList);
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
    public AndroidGalleryEngine(ComponentActivity activity){
        if (pickMedia == null){
            pickRequest = new PickRequest(FileMimeType.getImageMimeType(),FileMimeType.getImageAndVideoMimeType());
            pickMedia = activity.registerForActivityResult(pickRequest, uri -> {
                if (uri != null) {
                    LocalMedia media = MediaDataBuild.buildLocalMedia(activity,uri.toString());
                    PicListData.getInstance().mediaList.add(media);
                    if (picChooser.cropFileEngine != null){
                        ArrayList<Uri> uris = new ArrayList<>();
                        uris.add(uri);
                        picChooser.cropFileEngine.setPicChooser(picChooser);
                        picChooser.cropFileEngine.onStartCrop(activity,uris);
                    }else if(picChooser.compressFileEngine != null){
                        goCompress(activity,picChooser);
                    }else{
                        if (picChooser.callback != null){
                            picChooser.callback.onResult(PicListData.getInstance().mediaList);
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

            pickMultipleRequest = new PickMultipleRequest(2,FileMimeType.getImageMimeType(),FileMimeType.getImageAndVideoMimeType());
            pickMultipleMedia = activity.registerForActivityResult(pickMultipleRequest, uris -> {
                if (!uris.isEmpty()) {
                    for (Uri uri : uris){
                        LocalMedia media = MediaDataBuild.buildLocalMedia(activity,uri.toString());
                        PicListData.getInstance().mediaList.add(media);
                    }
                    if (picChooser.cropFileEngine != null){
                        picChooser.cropFileEngine.setPicChooser(picChooser);
                        picChooser.cropFileEngine.onStartCrop(activity,uris);
                    }else if(picChooser.compressFileEngine != null){
                        goCompress(activity,picChooser);
                    }else{
                        if (picChooser.callback != null){
                            picChooser.callback.onResult(PicListData.getInstance().mediaList);
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
    public void launch(PicChooser chooser){
        this.picChooser = chooser;
        if (pickMedia != null && pickMultipleMedia != null){
            ActivityResultContracts.PickVisualMedia.VisualMediaType type;
            if (picChooser.chooseMode == SelectMimeType.ofVideo()){
                pickRequest.setChooseMode(SelectMimeType.ofVideo());
                pickMultipleRequest.setChooseMode(SelectMimeType.ofVideo());
                type = ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE;
            }else {
                pickRequest.setGif(picChooser.isGif);
                pickMultipleRequest.setGif(picChooser.isGif);
                if (picChooser.isGif){
                    //show gif
                    if (picChooser.chooseMode == SelectMimeType.ofImage()){
                        type = ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE;
                    }else {
                        type = ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE;
                    }
                }else{
                    if (picChooser.chooseMode == SelectMimeType.ofImage()){
                        pickRequest.setChooseMode(SelectMimeType.ofImage());
                        pickMultipleRequest.setChooseMode(SelectMimeType.ofImage());
                    }else {
                        pickRequest.setChooseMode(SelectMimeType.ofAll());
                        pickMultipleRequest.setChooseMode(SelectMimeType.ofAll());
                    }
                    type = new ActivityResultContracts.PickVisualMedia.SingleMimeType("*/*");
                }
            }

            if (picChooser.selectionMode == SelectModeConfig.MULTIPLE){
                pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(type)
                        .build());
            }else {
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(type)
                        .build());
            }
        }
    }

    @Override
    public void updateMaxItems(int maxSelectNum){
        if (pickMultipleRequest != null){
            pickMultipleRequest.updateMaxItems(maxSelectNum >1?maxSelectNum:2);
        }
    }

    private void goCompress(Activity activity,PicChooser picChooser){
        ArrayList<Uri> uris = new ArrayList<>();
        ConcurrentHashMap<String, LocalMedia> queue = new ConcurrentHashMap<>();
        for (int i = 0; i < PicListData.getInstance().mediaList.size(); i++) {
            LocalMedia mediaC = PicListData.getInstance().mediaList.get(i);
            String availablePath = mediaC.getAvailablePath();
            if (PictureMimeType.isHasImage(mediaC.getMimeType())) {
                Uri a = PictureMimeType.isContent(availablePath) ? Uri.parse(availablePath) : Uri.fromFile(new File(availablePath));
                uris.add(a);
                queue.put(availablePath, mediaC);
            }
        }
        if (queue.size() == 0) {
            if (picChooser.callback != null){
                picChooser.callback.onResult(PicListData.getInstance().mediaList);
            }
        }else{
            picChooser.compressFileEngine.onStartCompress(activity, uris, new OnKeyValueResultCallbackListener() {
                @Override
                public void onCallback(String srcPath, String compressPath) {
                    if (TextUtils.isEmpty(srcPath)) {
                        if (picChooser.callback != null){
                            picChooser.callback.onResult(PicListData.getInstance().mediaList);
                        }
                    } else {
                        LocalMedia media = queue.get(srcPath);
                        if (media != null) {
                            if (SdkVersionUtils.isQ()){
                                if (!TextUtils.isEmpty(compressPath) && (compressPath.contains("Android/data/")
                                        || compressPath.contains("data/user/"))) {
                                    media.setCompressPath(compressPath);
                                    media.setCompressed(!TextUtils.isEmpty(compressPath));
                                    media.setSandboxPath(media.getCompressPath());
                                }
                            } else {
                                media.setCompressPath(compressPath);
                                media.setCompressed(!TextUtils.isEmpty(compressPath));
                            }
                            queue.remove(srcPath);
                        }
                        if (queue.size() == 0) {
                            if (picChooser.callback != null){
                                picChooser.callback.onResult(PicListData.getInstance().mediaList);
                            }
                        }
                    }
                }
            });
        }
    }

}
