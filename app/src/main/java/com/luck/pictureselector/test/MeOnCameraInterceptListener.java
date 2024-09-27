package com.luck.pictureselector.test;

import android.content.Context;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.luck.lib.camerax.CameraImageEngine;
import com.luck.lib.camerax.SimpleCameraX;
import com.luck.picture.lib.interfaces.OnCameraInterceptListener;

import java.io.File;

public class MeOnCameraInterceptListener implements OnCameraInterceptListener {
    @Override
    public void openCamera(Fragment fragment, int cameraMode, int requestCode) {
        SimpleCameraX camera = SimpleCameraX.of();
        camera.isAutoRotation(true);
        camera.setCameraMode(cameraMode);
        camera.setVideoFrameRate(25);
        camera.setVideoBitRate(3 * 1024 * 1024);
        camera.isDisplayRecordChangeTime(true);
        camera.isManualFocusCameraPreview(true);//手指点击对焦
        camera.isZoomCameraPreview(true);//手指缩放照相机
        camera.setOutputPathDir(getSandboxCameraOutputPath(fragment.getContext()));
        camera.setImageEngine(new CameraImageEngine() {
            @Override
            public void loadImage(Context context, String url, ImageView imageView) {
                Glide.with(context).load(url).into(imageView);
            }
        });
        camera.start(fragment.getActivity(), fragment, requestCode);
    }


    /**
     * 创建相机自定义输出目录
     *
     * @return
     */
    private String getSandboxCameraOutputPath(Context context) {
        File externalFilesDir = context.getExternalFilesDir("");
        File customFile = new File(externalFilesDir.getAbsolutePath(), "Sandbox");
        if (!customFile.exists()) {
            customFile.mkdirs();
        }
        return customFile.getAbsolutePath() + File.separator;
    }
}
