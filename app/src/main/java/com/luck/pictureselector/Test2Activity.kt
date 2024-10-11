package com.luck.pictureselector

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.pictureselector.adapter.TestAdapter
import com.luck.pictureselector.databinding.ActivityTestTwoBinding
import com.luck.pictureselector.fragment.Test2Fragment
import com.luck.pictureselector.newlib.AndroidGalleryEngine
import com.luck.pictureselector.newlib.GlideEngine
import com.luck.pictureselector.newlib.ImageFileCompressEngine
import com.luck.pictureselector.newlib.ImageFileCropEngine
import com.luck.pictureselector.newlib.MeOnCameraInterceptListener
import com.luck.pictureselector.newlib.UpPictureSelectorStyle
import spa.lyh.cn.chooser.PicChooser
import spa.lyh.cn.peractivity.ManifestPro
import spa.lyh.cn.peractivity.PermissionActivity

class Test2Activity:PermissionActivity() {
    lateinit var b: ActivityTestTwoBinding
    private lateinit var fm: FragmentManager
    private lateinit var test2Fragment: Test2Fragment
    var mark = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityTestTwoBinding.inflate(layoutInflater)
        setContentView(b.root)
        initView()
    }

    fun initView(){
        fm = supportFragmentManager
        val ft: FragmentTransaction = fm.beginTransaction()
        test2Fragment = Test2Fragment()
        ft.add(R.id.fl_change, test2Fragment)
        ft.commit()

    }

    fun askForPermissionPhoto(){
        mark = 1
        askForPermission(REQUIRED_LOAD_METHOD, ManifestPro.permission.READ_EXTERNAL_STORAGE_BLOW_ANDROID_13)
    }

    fun askForPermissionCamera(){
        mark = 2
        askForPermission(REQUIRED_LOAD_METHOD, ManifestPro.permission.READ_EXTERNAL_STORAGE_BLOW_ANDROID_13,ManifestPro.permission.CAMERA)
    }



    override fun permissionAllowed() {
        super.permissionAllowed()
        if (mark == 1){
            test2Fragment.openPhoto()
        }else if (mark == 2){
            test2Fragment.openCamera()
        }
    }
}