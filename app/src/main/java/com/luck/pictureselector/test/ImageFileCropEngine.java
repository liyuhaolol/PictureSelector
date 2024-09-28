package com.luck.pictureselector.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectorConfig;
import com.luck.picture.lib.config.SelectorProviders;
import com.luck.picture.lib.engine.CropFileEngine;
import com.luck.pictureselector.ImageLoaderUtils;
import com.yalantis.ucrop.ListenerContainer;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropImageEngine;
import com.yalantis.ucrop.listener.OnResultListener;

import java.io.File;
import java.util.ArrayList;

public class ImageFileCropEngine implements CropFileEngine {
    @Override
    public void onStartCrop(Fragment fragment, Uri srcUri, Uri destinationUri, ArrayList<String> dataSource, int requestCode) {
        ListenerContainer listener = ListenerContainer.getInstance();
        UCrop.Options options = buildOptions(fragment.getContext());
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
        uCrop.start(fragment.requireActivity(), fragment, requestCode);
    }

    @Override
    public void onStartCrop(Activity activity, Uri srcUri, Uri destinationUri, ArrayList<String> dataSource, int requestCode) {
        ListenerContainer listener = ListenerContainer.getInstance();
        listener.setOnResultListener(new OnResultListener() {
            @Override
            public void onResult(Intent intent) {
                SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
                if (selectorConfig.openGalleryEngine != null){
                    selectorConfig.openGalleryEngine.onResult(intent,requestCode);
                }
                listener.setOnResultListener(null);
            }
        });
        UCrop.Options options = buildOptions(activity);
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
        Intent intent = uCrop.getIntent(activity);
        activity.startActivity(intent);
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
