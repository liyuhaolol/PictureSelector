package spa.lyh.cn.chooser.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Display
import android.view.Surface
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import spa.lyh.cn.chooser.PicChooser
import spa.lyh.cn.chooser.R
import spa.lyh.cn.chooser.listener.ResultCallback
import spa.lyh.cn.chooser.request.PickMultipleRequest
import spa.lyh.cn.chooser.request.PickRequest


class ChooserFragment: Fragment() {
    companion object{
        private val imageMimeTypeList = arrayListOf<String>()
        private val allMimeTypeList = arrayListOf<String>()

        private var mScreenOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        @JvmStatic
        fun launch(activity:FragmentActivity, imageMimeTypeList: ArrayList<String>, allMimeTypeList: ArrayList<String>,picChooser: PicChooser,callback:ResultCallback?){
            ChooserFragment.imageMimeTypeList.clear()
            ChooserFragment.imageMimeTypeList.addAll(imageMimeTypeList)
            ChooserFragment.allMimeTypeList.clear()
            ChooserFragment.allMimeTypeList.addAll(allMimeTypeList)
            val fragment = ChooserFragment()
            // 设置保留实例，不会因为屏幕方向或配置变化而重新创建
            fragment.setRetainInstance(true)
            // 设置权限申请标记
            fragment.setRequestFlag(true)
            // 设置权限回调监听
            fragment.onResultCallbackListener(callback)
            // 绑定到 Activity 上面
            fragment.attachByActivity(activity,picChooser)
        }
    }

    fun attachByActivity(activity: FragmentActivity,picChooser: PicChooser){
        this.picChooser = picChooser
        val fragmentManager: FragmentManager? = activity.supportFragmentManager
        if (fragmentManager == null){
            return
        }
        fragmentManager.beginTransaction().add(this,this.toString()).commitAllowingStateLoss()
    }

    fun detachByActivity(activity: FragmentActivity){
        picChooser = null
        val fragmentManager:FragmentManager? = activity.supportFragmentManager
        if (fragmentManager == null) {
            return
        }
        fragmentManager.beginTransaction().remove(this).commitAllowingStateLoss()
    }

    private lateinit var pickRequest: PickRequest
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest?>
    private lateinit var pickMultipleRequest: PickMultipleRequest
    private lateinit var pickMultipleMedia: ActivityResultLauncher<PickVisualMediaRequest?>
    private var picChooser: PicChooser? = null
    private lateinit var type: ActivityResultContracts.PickVisualMedia.VisualMediaType

    private var mRequestFlag: Boolean = false

    private var callback: ResultCallback? = null

    //pick必须在oncreate里初始化
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (picChooser != null){
            pickRequest = PickRequest(imageMimeTypeList, allMimeTypeList)
            pickMedia = registerForActivityResult(pickRequest,object : ActivityResultCallback<Uri?>{
                override fun onActivityResult(result: Uri?) {
                    val list: MutableList<Uri> = mutableListOf()
                    if (result != null){
                        list.add(result)
                    }
                    callback?.onActivityResult(list)
                    detachByActivity(requireActivity())
                }
            })
            pickMultipleRequest = PickMultipleRequest(if (picChooser!!.maxSelectNum > 1){picChooser!!.maxSelectNum}else{2}, imageMimeTypeList, allMimeTypeList)
            pickMultipleMedia = registerForActivityResult(pickMultipleRequest,object : ActivityResultCallback<List<Uri>>{
                override fun onActivityResult(result: List<Uri>) {
                    callback?.onActivityResult(result)
                    detachByActivity(requireActivity())
                }
            })
            ///
            if (picChooser!!.chooseMode == SelectMimeType.ofVideo()) {
                pickRequest.chooseMode = SelectMimeType.ofVideo()
                pickMultipleRequest.chooseMode = SelectMimeType.ofVideo()
                type = ActivityResultContracts.PickVisualMedia.VideoOnly
            } else {
                pickRequest.isGif = picChooser!!.isGif
                pickMultipleRequest.isGif = picChooser!!.isGif
                if (picChooser!!.isGif) {
                    //show gif
                    if (picChooser!!.chooseMode == SelectMimeType.ofImage()) {
                        type = ActivityResultContracts.PickVisualMedia.ImageOnly
                    } else {
                        type = ActivityResultContracts.PickVisualMedia.ImageAndVideo
                    }
                } else {
                    if (picChooser!!.chooseMode == SelectMimeType.ofImage()) {
                        pickRequest.chooseMode = SelectMimeType.ofImage()
                        pickMultipleRequest.chooseMode = SelectMimeType.ofImage()
                    } else {
                        pickRequest.chooseMode = SelectMimeType.ofAll()
                        pickMultipleRequest.chooseMode = SelectMimeType.ofAll()
                    }
                    type = ActivityResultContracts.PickVisualMedia.SingleMimeType("*/*")
                }
            }
        }
    }

    fun setRequestFlag(flag: Boolean){
        mRequestFlag = flag
    }

    fun onResultCallbackListener(callback:ResultCallback?){
        this.callback = callback
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity == null){
            return
        }
        // 如果当前没有锁定屏幕方向就获取当前屏幕方向并进行锁定
        mScreenOrientation = requireActivity().getRequestedOrientation()
        if (mScreenOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            return
        }
        // 锁定当前 Activity 方向
        lockActivityOrientation(requireActivity())
    }

    override fun onDetach() {
        super.onDetach()
        if (activity == null || mScreenOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED ||
            requireActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            return
        }
        // 为什么这里不用跟上面一样 try catch ？因为这里是把 Activity 方向取消固定，只有设置横屏或竖屏的时候才可能触发 crash
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 取消引用监听器，避免内存泄漏
        callback = null
    }

    override fun onResume() {
        super.onResume()
        // 如果当前 Fragment 是通过系统重启应用触发的，则不进行权限申请
        if (!mRequestFlag) {
            detachByActivity(requireActivity())
            return
        }
        val options =
            ActivityOptionsCompat.makeCustomAnimation(requireContext(), R.anim.slide_in, R.anim.stay)
        if (picChooser!!.selectionMode == SelectModeConfig.MULTIPLE) {
            pickMultipleMedia.launch(
                PickVisualMediaRequest.Builder()
                    .setMediaType(type)
                    .build(), options
            )
        } else {
            pickMedia.launch(
                PickVisualMediaRequest.Builder()
                    .setMediaType(type)
                    .build(), options
            )
        }
    }

    /** * 锁定当前 Activity 的方向 */
    @SuppressLint("SwitchIntDef")
    fun lockActivityOrientation(activity: Activity) {
        try {
            // 兼容问题：在 Android 8.0 的手机上可以固定 Activity 的方向，但是这个 Activity 不能是透明的，否则就会抛出异常
            // 复现场景：只需要给 Activity 主题设置 <item name="android:windowIsTranslucent">true</item> 属性即可
            when (activity.resources.configuration.orientation){
                Configuration.ORIENTATION_LANDSCAPE -> {
                    activity.requestedOrientation = if (isActivityReverse(activity)) {
                        ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    }else {
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                }
                Configuration.ORIENTATION_PORTRAIT -> {
                    activity.requestedOrientation = if (isActivityReverse(activity)) {
                        ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    }else{
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                }
            }
        }catch (e: IllegalStateException) {
            // java.lang.IllegalStateException: Only fullscreen activities can request orientation
            e.printStackTrace()
        }
    }

    /**
     * 判断 Activity 是否反方向旋转了
     */
    fun isActivityReverse(activity: Activity): Boolean {
        val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.display
        } else {
            val windowManager = activity.windowManager
            windowManager?.defaultDisplay
        }

        if (display == null) {
            return false
        }

        // 获取 Activity 旋转的角度
        val activityRotation = display.rotation
        return when (activityRotation) {
            Surface.ROTATION_180, Surface.ROTATION_270 -> true
            Surface.ROTATION_0, Surface.ROTATION_90 -> false
            else -> false
        }
    }
}