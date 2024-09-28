package com.luck.pictureselector

import android.os.Build
import android.os.Bundle
import com.luck.picture.lib.basic.PictureSelectionModel
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.pictureselector.databinding.ActivityTestBinding
import com.luck.pictureselector.newlib.AndroidGalleryEngine
import com.luck.pictureselector.newlib.ImageFileCropEngine
import com.luck.pictureselector.newlib.out.PicChooser
import com.luck.pictureselector.newlib.UpPictureSelectorStyle
import spa.lyh.cn.lib_image.app.ImageLoadUtil
import spa.lyh.cn.peractivity.ManifestPro
import spa.lyh.cn.peractivity.PermissionActivity

class TestActivity :PermissionActivity(){
    lateinit var b:ActivityTestBinding
    var mark = 1
    lateinit var psm: PictureSelectionModel
    //lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    lateinit var pc:PicChooser

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
//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
/*        psm = PictureSelector.create(this)
            .openGallery(SelectMimeType.ofImage())
            .isGif(false)
            .setSelectionMode(SelectModeConfig.MULTIPLE)
            .setMaxSelectNum(5)
            .setSelectorUIStyle(UpPictureSelectorStyle())
            .setImageEngine(GlideEngine.createGlideEngine())
            .isDisplayCamera(false)
            .setCropEngine(ImageFileCropEngine(this))
            //.setCompressEngine(ImageFileCompressEngine())
            .isDirectReturnSingle(true)
            .setCameraInterceptListener(MeOnCameraInterceptListener())
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
            })*/
        pc = PicChooser.getInstance(this)
            .openGallery(SelectMimeType.ofImage())
            .isGif(false)
            .setSelectionMode(SelectModeConfig.SINGLE)
            .setMaxSelectNum(5)
            .setSelectorUIStyle(UpPictureSelectorStyle())
            .setOpenGalleryEngine(AndroidGalleryEngine(this))
            .setCropEngine(ImageFileCropEngine(this))
            //.setCompressEngine(ImageFileCompressEngine())


    }

    override fun permissionAllowed() {
        super.permissionAllowed()
        if (mark == 1){
            openPhoto()
        }
    }

    fun openPhoto(){
/*            psm.forResult(object : OnResultCallbackListener<LocalMedia?> {
                override fun onResult(result: ArrayList<LocalMedia?>?) {
                    if (result != null){
                        ImageLoadUtil.displayImage(this@TestActivity,result[0]!!.cutPath,b.img)
                    }
                }

                override fun onCancel() {

                }

            })*/

        pc.forResult(object : OnResultCallbackListener<LocalMedia?> {
            override fun onResult(result: ArrayList<LocalMedia?>?) {
                if (result != null){
                    ImageLoadUtil.displayImage(this@TestActivity,result[0]!!.realPath,b.img)
                }
            }

            override fun onCancel() {

            }

        })
    }



}