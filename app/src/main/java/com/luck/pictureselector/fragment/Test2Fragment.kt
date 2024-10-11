package com.luck.pictureselector.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.pictureselector.Test2Activity
import com.luck.pictureselector.adapter.TestAdapter
import com.luck.pictureselector.databinding.FragmentTestTwoBinding
import com.luck.pictureselector.newlib.AndroidGalleryEngine
import com.luck.pictureselector.newlib.GlideEngine
import com.luck.pictureselector.newlib.ImageFileCompressEngine
import com.luck.pictureselector.newlib.ImageFileCropEngine
import com.luck.pictureselector.newlib.MeOnCameraInterceptListener
import com.luck.pictureselector.newlib.UpPictureSelectorStyle
import spa.lyh.cn.chooser.PicChooser
import spa.lyh.cn.peractivity.ManifestPro
import spa.lyh.cn.peractivity.PermissionActivity.Companion.REQUIRED_LOAD_METHOD
import kotlin.text.clear

class Test2Fragment: Fragment() {
    lateinit var b: FragmentTestTwoBinding
    lateinit var pc: PicChooser

    lateinit var testAdapter: TestAdapter
    var list:ArrayList<String> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        b = FragmentTestTwoBinding.inflate(layoutInflater,container,false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        testAdapter = TestAdapter(requireActivity(),list)
        b.recy.layoutManager = LinearLayoutManager(requireActivity(),LinearLayoutManager.HORIZONTAL,false)
        b.recy.adapter = testAdapter
        b.btnOpenGalley.setOnClickListener{
            (requireActivity() as Test2Activity).askForPermissionPhoto()
        }
        b.btnOpenCamera.setOnClickListener{
            (requireActivity() as Test2Activity).askForPermissionCamera()
        }
        pc = PicChooser()
            .setImageEngine(GlideEngine.createGlideEngine())
            .openGallery(SelectMimeType.ofAll())
            .isGif(false)
            .setSelectionMode(SelectModeConfig.MULTIPLE)
            .setMaxSelectNum(5)
            .setSelectorUIStyle(UpPictureSelectorStyle())
            .setOpenGalleryEngine(AndroidGalleryEngine(this))
            .setCropEngine(ImageFileCropEngine().initResultLauncher(this))
            .setCompressEngine(ImageFileCompressEngine())
    }

    fun openPhoto(){
        //val no = 5-list.size
        //pc.setMaxSelectNum(no)
        pc.forResult(this,object : OnResultCallbackListener<LocalMedia?> {
            override fun onResult(result: ArrayList<LocalMedia?>?) {
                if (result != null){
                    list.clear()
                    for (localMedia in result){
                        val path = localMedia!!.compressPath
                        list.add(path)
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
            .setCropEngine(ImageFileCropEngine())
            .setCompressEngine(ImageFileCompressEngine())
            .forResult(object : OnResultCallbackListener<LocalMedia?> {
                override fun onResult(result: java.util.ArrayList<LocalMedia?>?) {
                    if (result != null){
                        list.clear()
                        for (localMedia in result){
                            val path = localMedia!!.compressPath
                            list.add(path)
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