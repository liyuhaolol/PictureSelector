package com.luck.pictureselector.newlib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

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
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectorConfig;
import com.luck.picture.lib.config.SelectorProviders;
import com.luck.picture.lib.engine.CropFileEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.utils.BitmapUtils;
import com.luck.picture.lib.utils.DateUtils;
import com.luck.picture.lib.utils.FileDirMap;
import com.luck.picture.lib.utils.SdkVersionUtils;
import com.luck.pictureselector.newlib.out.PicChooser;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropImageEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageFileCropEngine implements CropFileEngine {
    private ActivityResultLauncher<Intent> resultLauncher;

    public ImageFileCropEngine(AppCompatActivity activity){
        resultLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK){
                            Log.e("qwer","成功");
                            //Uri output = Crop.getOutput(result.getData());
                            //Log.e("qwer",output.toString());

                        }else if(result.getResultCode() == Crop.RESULT_CROP_ERROR){
                            Log.e("qwer","错误");
                        }else if(result.getResultCode() == Activity.RESULT_CANCELED){
                            Log.e("qwer","取消");
                        }
                    }
                }
        );
    }

    @Override
    public void onStartCrop(Fragment fragment, Uri srcUri, Uri destinationUri, ArrayList<String> dataSource, int requestCode) {
        UCrop uCrop = inituCrop(fragment.getContext(),srcUri,destinationUri,dataSource);
        uCrop.start(fragment.requireActivity(), fragment, requestCode);
    }

    public void onStartCrop(Activity activity, Uri srcUri) {
        ArrayList<Uri> uris = new ArrayList<>();
        uris.add(srcUri);
        onStartCrop(activity,uris);
    }

    public void onStartCrop(Activity activity, List<Uri> Uris) {
        Uri srcUri = null;
        Uri destinationUri = null;
        ArrayList<String> dataCropSource = new ArrayList<>();
        for (int i = 0; i < Uris.size(); i++) {
            LocalMedia media = PicChooser.getInstance(activity).buildLocalMedia(activity,Uris.get(i).toString());
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
}
