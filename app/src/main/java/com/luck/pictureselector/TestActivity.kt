package com.luck.pictureselector

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.luck.picture.lib.basic.PictureSelectionSystemModel
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnPermissionsInterceptListener
import com.luck.picture.lib.interfaces.OnRequestPermissionListener
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.utils.MediaUtils
import com.luck.picture.lib.utils.PictureFileUtils
import com.luck.pictureselector.MainActivity.MeOnSelectLimitTipsListener
import com.luck.pictureselector.MainActivity.MeSandboxFileEngine
import com.luck.pictureselector.databinding.ActivityTestBinding
import com.luck.pictureselector.test.ImageFileCompressEngine
import com.luck.pictureselector.test.ImageFileCropEngine
import com.luck.pictureselector.test.MeOnCameraInterceptListener
import com.luck.pictureselector.test.UpPictureSelectorStyle
import spa.lyh.cn.peractivity.ManifestPro
import spa.lyh.cn.peractivity.PermissionActivity

class TestActivity :PermissionActivity(){
    lateinit var b:ActivityTestBinding
    var mark = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityTestBinding.inflate(layoutInflater)
        setContentView(b.root)
        initView()
    }

    fun initView(){
        b.btnOpenGalley.setOnClickListener{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                openPhoto()
            }else{
                askForPermission(REQUIRED_LOAD_METHOD, ManifestPro.permission.READ_EXTERNAL_STORAGE)
                mark = 1
            }
        }
    }

    override fun permissionAllowed() {
        super.permissionAllowed()
        if (mark == 1){
            openPhoto()
        }
    }

    fun openPhoto(){
/*        PictureSelector.create(this)
            .openGallery(SelectMimeType.ofImage())
            .setSelectorUIStyle(UpPictureSelectorStyle())
            .setImageEngine(GlideEngine.createGlideEngine())
            .isDisplayCamera(false)
            .setCropEngine(ImageFileCropEngine())
            .setCompressEngine(ImageFileCompressEngine())
            .setSelectionMode(SelectModeConfig.SINGLE)
            .isDirectReturnSingle(true)
            .setCameraInterceptListener(MeOnCameraInterceptListener())
            .isGif(false)
            .setPermissionsInterceptListener(object : OnPermissionsInterceptListener {
                override fun requestPermission(
                    fragment: Fragment,
                    permissionArray: Array<String>,
                    call: OnRequestPermissionListener
                ) {
                    call.onCall(permissionArray, true)
                }

                override fun hasPermissions(
                    fragment: Fragment,
                    permissionArray: Array<String>
                ): Boolean {
                    return true
                }
            })
            .forResult(object : OnResultCallbackListener<LocalMedia?> {
                override fun onResult(result: ArrayList<LocalMedia?>?) {
                    if (result != null){
                        b.show.text = "选择了${result.size}个图片或视频"
                    }
                }

                override fun onCancel() {

                }

            })*/

        //////////////////////

        val systemGalleryMode: PictureSelectionSystemModel = PictureSelector.create(this)
            .openSystemGallery(SelectMimeType.ofImage())
            .setSelectionMode(SelectModeConfig.MULTIPLE)
            .setCompressEngine(ImageFileCompressEngine())
            .setCropEngine(ImageFileCropEngine())
            //.setSkipCropMimeType(*getNotSupportCrop())
            .setSelectLimitTipsListener(MeOnSelectLimitTipsListener())
            //.setAddBitmapWatermarkListener(getAddBitmapWatermarkListener())
            //.setVideoThumbnailListener(getVideoThumbnailEventListener())
            //.setCustomLoadingListener(getCustomLoadingListener())
            .isOriginalControl(true)
            .setPermissionsInterceptListener(object : OnPermissionsInterceptListener {
                override fun requestPermission(
                    fragment: Fragment,
                    permissionArray: Array<String>,
                    call: OnRequestPermissionListener
                ) {
                    call.onCall(permissionArray, true)
                }

                override fun hasPermissions(
                    fragment: Fragment,
                    permissionArray: Array<String>
                ): Boolean {
                    return true
                }
            })
            //.setPermissionDescriptionListener(getPermissionDescriptionListener())
            .setSandboxFileEngine(MeSandboxFileEngine())
        systemGalleryMode.forSystemResult(MeOnResultCallbackListener())
    }

    private class MeOnResultCallbackListener : OnResultCallbackListener<LocalMedia> {
        override fun onResult(result: java.util.ArrayList<LocalMedia>) {
            analyticalSelectResults(result)
        }

        override fun onCancel() {
            Log.e("qwer", "PictureSelector Cancel")
        }

        private fun analyticalSelectResults(result: java.util.ArrayList<LocalMedia>) {
            for (media in result) {
                /*if (media.width == 0 || media.height == 0) {
                    if (PictureMimeType.isHasImage(media.mimeType)) {
                        val imageExtraInfo = MediaUtils.getImageSize(getContext(), media.path)
                        media.width = imageExtraInfo.width
                        media.height = imageExtraInfo.height
                    } else if (PictureMimeType.isHasVideo(media.mimeType)) {
                        val videoExtraInfo = MediaUtils.getVideoSize(getContext(), media.path)
                        media.width = videoExtraInfo.width
                        media.height = videoExtraInfo.height
                    }
                }*/
                Log.e("qwer", "文件名: " + media.fileName)
                Log.e("qwer", "是否压缩:" + media.isCompressed)
                Log.e("qwer", "压缩:" + media.compressPath)
                Log.e("qwer", "初始路径:" + media.path)
                Log.e("qwer", "绝对路径:" + media.realPath)
                Log.e("qwer", "是否裁剪:" + media.isCut)
                Log.e("qwer", "裁剪路径:" + media.cutPath)
                Log.e("qwer", "是否开启原图:" + media.isOriginal)
                Log.e("qwer", "原图路径:" + media.originalPath)
                Log.e("qwer", "沙盒路径:" + media.sandboxPath)
                Log.e("qwer", "水印路径:" + media.watermarkPath)
                Log.e("qwer", "视频缩略图:" + media.videoThumbnailPath)
                Log.e("qwer", "原始宽高: " + media.width + "x" + media.height)
                Log.e("qwer",
                    "裁剪宽高: " + media.cropImageWidth + "x" + media.cropImageHeight
                )
                Log.e("qwer",
                    "文件大小: " + PictureFileUtils.formatAccurateUnitFileSize(media.size)
                )
                Log.e("qwer", "文件时长: " + media.duration)
            }
        }
    }

}