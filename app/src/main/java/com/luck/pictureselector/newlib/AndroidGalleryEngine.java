package com.luck.pictureselector.newlib;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;

import androidx.fragment.app.FragmentActivity;

import com.luck.picture.lib.entity.LocalMedia;

import org.jetbrains.annotations.NotNull;

import spa.lyh.cn.chooser.MediaDataBuild;
import spa.lyh.cn.chooser.PicChooser;
import spa.lyh.cn.chooser.PicListData;
import spa.lyh.cn.chooser.engine.OpenGalleryEngine;
import spa.lyh.cn.chooser.fragment.ChooserFragment;
import spa.lyh.cn.chooser.listener.ResultCallback;

import java.util.List;

public class AndroidGalleryEngine implements OpenGalleryEngine {

    @Override
    public void launch(Context context,PicChooser picChooser){
        FragmentActivity activity = findActivity(context);
        if (activity != null){
            ChooserFragment.launch(
                    activity,
                    FileMimeType.getImageMimeType(),
                    FileMimeType.getImageAndVideoMimeType(),
                    picChooser,
                    new ResultCallback() {
                        @Override
                        public void onActivityResult(@NotNull List<? extends @NotNull Uri> result) {
                            if (result.isEmpty()){
                                if (picChooser.callback != null){
                                    picChooser.callback.onCancel();
                                }
                            }else{
                                List<Uri> uris = (List<Uri>) result;
                                for (Uri uri : uris){
                                    LocalMedia media = MediaDataBuild.buildLocalMedia(activity,uri.toString());
                                    PicListData.getInstance().mediaList.add(media);
                                }
                                if (picChooser.cropFileEngine != null){
                                    picChooser.cropFileEngine.setPicChooser(picChooser);
                                    picChooser.cropFileEngine.onStartCrop(activity,uris);
                                }else if(picChooser.compressFileEngine != null){
                                    picChooser.compressFileEngine.goCompress(activity,picChooser);
                                }else{
                                    if (picChooser.callback != null){
                                        picChooser.callback.onResult(PicListData.getInstance().mediaList);
                                    }
                                }
                            }
                        }
                    });
        }
    }

    private FragmentActivity findActivity(Context context){
        Context ctx = context;
        while (ctx != null) {
            if (ctx instanceof FragmentActivity) {
                return (FragmentActivity) ctx;
            } else if (ctx instanceof ContextWrapper) {
                ctx = ((ContextWrapper) ctx).getBaseContext();
            } else {
                return null;
            }
        }
        return null;
    }

}
