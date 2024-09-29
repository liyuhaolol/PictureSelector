package com.luck.pictureselector

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.luck.picture.lib.basic.PictureSelectionModel
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
import spa.lyh.cn.chooser.PicChooser
import com.luck.pictureselector.newlib.UpPictureSelectorStyle
import spa.lyh.cn.peractivity.ManifestPro
import spa.lyh.cn.peractivity.PermissionActivity

class TestActivity :PermissionActivity(){
    lateinit var b:ActivityTestBinding
    var mark = 1
    lateinit var pc: PicChooser

    lateinit var testAdapter: TestAdapter
    var list:ArrayList<String> = arrayListOf()


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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                openPhoto()
            }else{
                askForPermission(REQUIRED_LOAD_METHOD, ManifestPro.permission.READ_EXTERNAL_STORAGE)
                mark = 1
            }
        }
        pc = PicChooser.getInstance(this)
            .setImageEngine(GlideEngine.createGlideEngine())
            .openGallery(SelectMimeType.ofImage())
            .isGif(false)
            .setSelectionMode(SelectModeConfig.MULTIPLE)
            .setMaxSelectNum(5)
            .setSelectorUIStyle(UpPictureSelectorStyle())
            .setOpenGalleryEngine(AndroidGalleryEngine(this))
            //.setCropEngine(ImageFileCropEngine(this))
            .setCompressEngine(ImageFileCompressEngine())


    }

    override fun permissionAllowed() {
        super.permissionAllowed()
        if (mark == 1){
            openPhoto()
        }
    }

    fun openPhoto(){
        val no = 5-list.size
        pc.setMaxSelectNum(no)
        pc.forResult(this,object : OnResultCallbackListener<LocalMedia?> {
            override fun onResult(result: ArrayList<LocalMedia?>?) {
                if (result != null){
                    list.clear()
                    for (localMedia in result){
                        list.add(localMedia!!.compressPath)
                    }
                    testAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancel() {
                Log.e("qwer","整体被取消了")
            }

        })
    }



}