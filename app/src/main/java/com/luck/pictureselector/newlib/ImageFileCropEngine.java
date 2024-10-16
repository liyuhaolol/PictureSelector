package com.luck.pictureselector.newlib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.luck.picture.lib.config.Crop;
import com.luck.picture.lib.config.CustomIntentKey;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.engine.CropFileEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener;
import com.luck.picture.lib.utils.DateUtils;
import com.luck.picture.lib.utils.FileDirMap;

import spa.lyh.cn.chooser.MediaDataBuild;
import spa.lyh.cn.chooser.PicChooser;
import spa.lyh.cn.chooser.PicListData;
import spa.lyh.cn.chooser.engine.ChooserCropFileEngine;

import com.luck.picture.lib.utils.SdkVersionUtils;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropImageEngine;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ImageFileCropEngine implements ChooserCropFileEngine {
    private ActivityResultLauncher<Intent> resultLauncher;
    private PicChooser picChooser;


    //这是给Andriod13以上原生图片选择器初始化ForActivityResult用的，需要在onCreate里调用。相机，或者三方相册不需要调用这个方法
    public ImageFileCropEngine initResultLauncher(ComponentActivity activity){
        resultLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        PicListData picListData = PicListData.getInstance();
                        if (result.getResultCode() == Activity.RESULT_OK){
                            if (picListData.mediaList.size() == 1){
                                LocalMedia media = picListData.mediaList.get(0);
                                assert result.getData() != null;
                                Uri output = Crop.getOutput(result.getData());
                                media.setCutPath(output != null ? output.getPath() : "");
                                media.setCut(!TextUtils.isEmpty(media.getCutPath()));
                                media.setCropImageWidth(Crop.getOutputImageWidth(result.getData()));
                                media.setCropImageHeight(Crop.getOutputImageHeight(result.getData()));
                                media.setCropOffsetX(Crop.getOutputImageOffsetX(result.getData()));
                                media.setCropOffsetY(Crop.getOutputImageOffsetY(result.getData()));
                                media.setCropResultAspectRatio(Crop.getOutputCropAspectRatio(result.getData()));
                                media.setCustomData(Crop.getOutputCustomExtraData(result.getData()));
                                media.setSandboxPath(media.getCutPath());
                            }else{
                                assert result.getData() != null;
                                String extra = result.getData().getStringExtra(MediaStore.EXTRA_OUTPUT);
                                if (TextUtils.isEmpty(extra)) {
                                    extra = result.getData().getStringExtra(CustomIntentKey.EXTRA_OUTPUT_URI);
                                }
                                try {
                                    JSONArray array = new JSONArray(extra);
                                    if (array.length() == picListData.mediaList.size()) {
                                        for (int i = 0; i < picListData.mediaList.size(); i++) {
                                            LocalMedia media = picListData.mediaList.get(i);
                                            JSONObject item = array.optJSONObject(i);
                                            media.setCutPath(item.optString(CustomIntentKey.EXTRA_OUT_PUT_PATH));
                                            media.setCut(!TextUtils.isEmpty(media.getCutPath()));
                                            media.setCropImageWidth(item.optInt(CustomIntentKey.EXTRA_IMAGE_WIDTH));
                                            media.setCropImageHeight(item.optInt(CustomIntentKey.EXTRA_IMAGE_HEIGHT));
                                            media.setCropOffsetX(item.optInt(CustomIntentKey.EXTRA_OFFSET_X));
                                            media.setCropOffsetY(item.optInt(CustomIntentKey.EXTRA_OFFSET_Y));
                                            media.setCropResultAspectRatio((float) item.optDouble(CustomIntentKey.EXTRA_ASPECT_RATIO));
                                            media.setCustomData(item.optString(CustomIntentKey.EXTRA_CUSTOM_EXTRA_DATA));
                                            media.setSandboxPath(media.getCutPath());
                                        }
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                            if(picChooser.compressFileEngine != null){
                                goCompress(activity,picChooser);
                            }else{
                                if (picChooser.callback != null){
                                    picChooser.callback.onResult(PicListData.getInstance().mediaList);
                                }
                            }
                        }else if(result.getResultCode() == Crop.RESULT_CROP_ERROR){
                            Log.e("ImageFileCropEngine","图片裁剪出现错误");
                        }else if(result.getResultCode() == Activity.RESULT_CANCELED){
                            if (picChooser.callback != null){
                                picChooser.callback.onCancel();
                            }
                        }
                    }
                }
        );
        return this;
    }

    public ImageFileCropEngine initResultLauncher(Fragment fragment){
        resultLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        PicListData picListData = PicListData.getInstance();
                        if (result.getResultCode() == Activity.RESULT_OK){
                            if (picListData.mediaList.size() == 1){
                                LocalMedia media = picListData.mediaList.get(0);
                                assert result.getData() != null;
                                Uri output = Crop.getOutput(result.getData());
                                media.setCutPath(output != null ? output.getPath() : "");
                                media.setCut(!TextUtils.isEmpty(media.getCutPath()));
                                media.setCropImageWidth(Crop.getOutputImageWidth(result.getData()));
                                media.setCropImageHeight(Crop.getOutputImageHeight(result.getData()));
                                media.setCropOffsetX(Crop.getOutputImageOffsetX(result.getData()));
                                media.setCropOffsetY(Crop.getOutputImageOffsetY(result.getData()));
                                media.setCropResultAspectRatio(Crop.getOutputCropAspectRatio(result.getData()));
                                media.setCustomData(Crop.getOutputCustomExtraData(result.getData()));
                                media.setSandboxPath(media.getCutPath());
                            }else{
                                assert result.getData() != null;
                                String extra = result.getData().getStringExtra(MediaStore.EXTRA_OUTPUT);
                                if (TextUtils.isEmpty(extra)) {
                                    extra = result.getData().getStringExtra(CustomIntentKey.EXTRA_OUTPUT_URI);
                                }
                                try {
                                    JSONArray array = new JSONArray(extra);
                                    if (array.length() == picListData.mediaList.size()) {
                                        for (int i = 0; i < picListData.mediaList.size(); i++) {
                                            LocalMedia media = picListData.mediaList.get(i);
                                            JSONObject item = array.optJSONObject(i);
                                            media.setCutPath(item.optString(CustomIntentKey.EXTRA_OUT_PUT_PATH));
                                            media.setCut(!TextUtils.isEmpty(media.getCutPath()));
                                            media.setCropImageWidth(item.optInt(CustomIntentKey.EXTRA_IMAGE_WIDTH));
                                            media.setCropImageHeight(item.optInt(CustomIntentKey.EXTRA_IMAGE_HEIGHT));
                                            media.setCropOffsetX(item.optInt(CustomIntentKey.EXTRA_OFFSET_X));
                                            media.setCropOffsetY(item.optInt(CustomIntentKey.EXTRA_OFFSET_Y));
                                            media.setCropResultAspectRatio((float) item.optDouble(CustomIntentKey.EXTRA_ASPECT_RATIO));
                                            media.setCustomData(item.optString(CustomIntentKey.EXTRA_CUSTOM_EXTRA_DATA));
                                            media.setSandboxPath(media.getCutPath());
                                        }
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                            if(picChooser.compressFileEngine != null){
                                goCompress(fragment.getActivity(),picChooser);
                            }else{
                                if (picChooser.callback != null){
                                    picChooser.callback.onResult(PicListData.getInstance().mediaList);
                                }
                            }
                        }else if(result.getResultCode() == Crop.RESULT_CROP_ERROR){
                            Log.e("ImageFileCropEngine","图片裁剪出现错误");
                        }else if(result.getResultCode() == Activity.RESULT_CANCELED){
                            if (picChooser.callback != null){
                                picChooser.callback.onCancel();
                            }
                        }
                    }
                }
        );
        return this;
    }

    @Override
    public void setPicChooser(PicChooser picChooser){
        this.picChooser = picChooser;
    }

    @Override
    public void onStartCrop(Fragment fragment, Uri srcUri, Uri destinationUri, ArrayList<String> dataSource, int requestCode) {
        UCrop uCrop = inituCrop(fragment.getContext(),srcUri,destinationUri,dataSource);
        uCrop.start(fragment.requireActivity(), fragment, requestCode);
    }


    @Override
    public void onStartCrop(Activity activity, List<Uri> Uris) {
        Uri srcUri = null;
        Uri destinationUri = null;
        ArrayList<String> dataCropSource = new ArrayList<>();
        for (int i = 0; i < Uris.size(); i++) {
            LocalMedia media = MediaDataBuild.buildLocalMedia(activity,Uris.get(i).toString());
            dataCropSource.add(media.getAvailablePath());
            if (srcUri == null && PictureMimeType.isHasImage(media.getMimeType())) {
                String currentCropPath = media.getAvailablePath();
                if (PictureMimeType.isContent(currentCropPath) || PictureMimeType.isHasHttp(currentCropPath)) {
                    srcUri = Uri.parse(currentCropPath);
                } else {
                    srcUri = Uri.fromFile(new File(currentCropPath));
                }
                String fileName = DateUtils.getCreateFileName("CROP_") + ".jpg";
                File externalFilesDir = new File(FileDirMap.getFileDirPath(activity, SelectMimeType.TYPE_IMAGE));
                File outputFile = new File(externalFilesDir.getAbsolutePath(), fileName);
                destinationUri = Uri.fromFile(outputFile);
            }
        }
        UCrop uCrop = inituCrop(activity,srcUri,destinationUri,dataCropSource);
        resultLauncher.launch(uCrop.getIntent(activity));
    }

    private UCrop inituCrop(Context context,Uri srcUri, Uri destinationUri, ArrayList<String> dataSource){
        UCrop.Options options = buildOptions(context);
        UCrop uCrop = UCrop.of(srcUri, destinationUri, dataSource);
        uCrop.withOptions(options);
        uCrop.setImageEngine(new UCropImageEngine() {
            @Override
            public void loadImage(Context context, String url, ImageView imageView) {
                if (!ImageLoaderUtils.assertValidRequest(context)) {
                    return;
                }
                Glide.with(context).load(url).override(180, 180).into(imageView);
            }

            @Override
            public void loadImage(Context context, Uri url, int maxWidth, int maxHeight, OnCallbackListener<Bitmap> call) {
                Glide.with(context).asBitmap().load(url).override(maxWidth, maxHeight).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        if (call != null) {
                            call.onCall(resource);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        if (call != null) {
                            call.onCall(null);
                        }
                    }
                });
            }
        });
        return uCrop;
    }




    /**
     * 创建自定义输出目录
     *
     * @return
     */
    private String getCropPath(Context context) {
        File externalCacheDir = context.getExternalCacheDir();
        File customFile = new File(externalCacheDir.getAbsolutePath(), "Crop");
        if (!customFile.exists()) {
            customFile.mkdirs();
        }
        return customFile.getAbsolutePath() + File.separator;
    }

    /**
     * 配制UCrop，可根据需求自我扩展
     *
     * @return
     */
    private UCrop.Options buildOptions(Context context) {
        UCrop.Options options = new UCrop.Options();
        options.setHideBottomControls(true);
        options.setFreeStyleCropEnabled(false);
        options.setShowCropFrame(true);
        options.setShowCropGrid(true);
        options.setCircleDimmedLayer(true);
        options.withAspectRatio(1, 1);
        options.setCropOutputPathDir(getCropPath(context));
        options.isCropDragSmoothToCenter(true);
        options.isUseCustomLoaderBitmap(true);
        options.setSkipCropMimeType(PictureMimeType.ofGIF(), PictureMimeType.ofWEBP());
        options.isForbidCropGifWebp(true);
        options.isForbidSkipMultipleCrop(false);
        options.setMaxScaleMultiplier(100);
        options.setStatusBarColor(ContextCompat.getColor(context, com.luck.picture.lib.R.color.ps_color_grey));
        options.setToolbarColor(ContextCompat.getColor(context, com.luck.picture.lib.R.color.ps_color_grey));
        options.setToolbarWidgetColor(ContextCompat.getColor(context, com.luck.picture.lib.R.color.ps_color_white));
        return options;
    }


    private void goCompress(Context context,PicChooser picChooser){
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
            picChooser.compressFileEngine.onStartCompress(context, uris, new OnKeyValueResultCallbackListener() {
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
