package spa.lyh.cn.chooser.request

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

open class PickRequest : ActivityResultContracts.PickVisualMedia(){

    override fun createIntent(context: Context, input: PickVisualMediaRequest): Intent {
        return super.createIntent(context, input).apply {
            //putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/jpg", "image/png"))
            Log.e("qwer","哈哈")
        }
    }
}