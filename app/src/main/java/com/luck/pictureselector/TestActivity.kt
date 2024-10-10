package com.luck.pictureselector

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.pictureselector.adapter.TestAdapter
import com.luck.pictureselector.databinding.ActivityTestBinding
import com.luck.pictureselector.newlib.AndroidGalleryEngine
import com.luck.pictureselector.newlib.GlideEngine
import com.luck.pictureselector.newlib.ImageFileCompressEngine
import com.luck.pictureselector.newlib.ImageFileCropEngine
import com.luck.pictureselector.newlib.MeOnCameraInterceptListener
import com.luck.pictureselector.newlib.UpPictureSelectorStyle
import spa.lyh.cn.chooser.PicChooser
import spa.lyh.cn.peractivity.ManifestPro
import spa.lyh.cn.peractivity.PermissionActivity
import spa.lyh.cn.utils_io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException


class TestActivity :PermissionActivity(){
    lateinit var b:ActivityTestBinding
    var mark = 1
    lateinit var pc: PicChooser

    lateinit var testAdapter: TestAdapter
    var list:ArrayList<Uri> = arrayListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityTestBinding.inflate(layoutInflater)
        setContentView(b.root)
        initView()
    }

    fun initView(){
        testAdapter = TestAdapter(this,list)
        b.recy.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        b.recy.adapter = testAdapter
        b.btnOpenGalley.setOnClickListener{
            mark = 1
            askForPermission(REQUIRED_LOAD_METHOD, ManifestPro.permission.READ_EXTERNAL_STORAGE_BLOW_ANDROID_13)
        }
        b.btnOpenCamera.setOnClickListener{
            mark = 2
            askForPermission(REQUIRED_LOAD_METHOD, ManifestPro.permission.READ_EXTERNAL_STORAGE_BLOW_ANDROID_13,ManifestPro.permission.CAMERA)
        }
        pc = PicChooser.getInstance(this)
            .setImageEngine(GlideEngine.createGlideEngine())
            .openGallery(SelectMimeType.ofAll())
            .isGif(false)
            .setSelectionMode(SelectModeConfig.SINGLE)
            //.setMaxSelectNum(5)
            .setSelectorUIStyle(UpPictureSelectorStyle())
            .setOpenGalleryEngine(AndroidGalleryEngine(this))
            //.setCropEngine(ImageFileCropEngine().initResultLauncher(this))
            //.setCompressEngine(ImageFileCompressEngine())


    }

    override fun permissionAllowed() {
        super.permissionAllowed()
        if (mark == 1){
            openPhoto()
        }else if (mark == 2){
            openCamera()
        }
    }

    fun openPhoto(){
        //val no = 5-list.size
        //pc.setMaxSelectNum(no)
        pc.forResult(this,object : OnResultCallbackListener<LocalMedia?> {
            override fun onResult(result: ArrayList<LocalMedia?>?) {
                if (result != null){
                    list.clear()
                    for (localMedia in result){
                        val path = localMedia!!.realPath
                        Log.e("qwer",path)
                        list.add(localMedia.uri)
                    }
                    testAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancel() {
                Log.e("qwer","整体被取消了")
            }

        })
    }

    fun openCamera(){
        PictureSelector.create(this)
            .openCamera(SelectMimeType.ofImage())
            .setCameraInterceptListener(MeOnCameraInterceptListener())
            //.setCropEngine(ImageFileCropEngine())
            //.setCompressEngine(ImageFileCompressEngine())
            .forResult(object : OnResultCallbackListener<LocalMedia?> {
                override fun onResult(result: java.util.ArrayList<LocalMedia?>?) {
                    if (result != null){
                        list.clear()
                        for (localMedia in result){
                            val path = localMedia!!.realPath
                            Log.e("qwer",path)
                            //list.add(IOUtils.getFileUri(this@TestActivity,localMedia!!.realPath))
                            list.add(localMedia.uri)
                        }
                        testAdapter.notifyDataSetChanged()
                    }
                }

                override fun onCancel() {
                    Log.e("qwer","拍照取消了")
                }
            })
    }



}