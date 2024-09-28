package com.luck.pictureselector.newlib.out;


import android.app.Activity;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.luck.picture.lib.basic.PictureSelectionModel;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.engine.CompressFileEngine;
import com.luck.picture.lib.engine.CropFileEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnPermissionsInterceptListener;
import com.luck.picture.lib.interfaces.OnRequestPermissionListener;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.luck.picture.lib.style.PictureSelectorStyle;
import com.luck.pictureselector.GlideEngine;

import java.util.ArrayList;

public class PicChooser {
    Activity activity;
    int chooseMode = SelectMimeType.ofAll();
    boolean isGif = true;
    int selectionMode = SelectModeConfig.MULTIPLE;
    int maxSelectNum = 1;
    CropFileEngine cropFileEngine = null;
    CompressFileEngine compressFileEngine = null;
    PictureSelectionModel model = null;
    PictureSelectorStyle uiStyle = null;
    private PicChooser(Activity activity){
        this.activity = activity;
    }
    public static PicChooser create(Activity activity) {
        return new PicChooser(activity);
    }

    public PicChooser openGallery(int chooseMode) {
        this.chooseMode = chooseMode;
        return this;
    }

    public PicChooser isGif(boolean isGif) {
        this.isGif = isGif;
        return this;
    }

    public PicChooser setSelectionMode(int selectionMode) {
        this.selectionMode = selectionMode;
        return this;
    }

    public PicChooser setMaxSelectNum(int maxSelectNum) {
        this.maxSelectNum = maxSelectNum;
        return this;
    }

    public PicChooser setCropEngine(CropFileEngine engine) {
        cropFileEngine = engine;
        return this;
    }

    public PicChooser setCompressEngine(CompressFileEngine engine) {
        compressFileEngine = engine;
        return this;
    }

    public PicChooser setSelectorUIStyle(PictureSelectorStyle uiStyle) {
        this.uiStyle = uiStyle;
        return this;
    }

    public PicChooser build(){
        model = PictureSelector.create(activity)
                .openGallery(chooseMode)
                .isGif(isGif)
                .setSelectionMode(selectionMode);
        if (selectionMode == SelectModeConfig.MULTIPLE){
            model.setMaxSelectNum(maxSelectNum);
        }
        model.setSelectorUIStyle(uiStyle)
                .setImageEngine(GlideEngine.createGlideEngine())
                .isDisplayCamera(false)
                .setCropEngine(cropFileEngine)
                .setCompressEngine(compressFileEngine)
                .isDirectReturnSingle(true)
                .setPermissionsInterceptListener(new OnPermissionsInterceptListener() {
                    @Override
                    public void requestPermission(Fragment fragment, String[] permissionArray, OnRequestPermissionListener call) {
                        call.onCall(permissionArray,true);
                    }

                    @Override
                    public boolean hasPermissions(Fragment fragment, String[] permissionArray) {
                        return true;
                    }

                });
        return this;
    }

    public void forResult(OnResultCallbackListener<LocalMedia> callback){
        if (model != null){
            model.forResult(new OnResultCallbackListener<LocalMedia>() {
                @Override
                public void onResult(ArrayList<LocalMedia> result) {
                    callback.onResult(result);
                }

                @Override
                public void onCancel() {
                    callback.onCancel();
                }
            });
        }else{
            Log.e("Chooser","请先执行build完成初始化");
        }
    }
}
