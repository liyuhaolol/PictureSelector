package spa.lyh.cn.chooser.request

import android.content.Context
import android.content.Intent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.luck.picture.lib.config.SelectMimeType
import java.util.ArrayList

open class PickRequest(val imageMimeTypeList:ArrayList<String>,val allMimeTypeList:ArrayList<String>) : ActivityResultContracts.PickVisualMedia(){
    var chooseMode = SelectMimeType.ofAll()
    var isGif = true

    override fun createIntent(context: Context, input: PickVisualMediaRequest): Intent {
        return super.createIntent(context, input).apply {
            if (!isGif){
                if (chooseMode == SelectMimeType.ofAll()){
                    putExtra(Intent.EXTRA_MIME_TYPES, allMimeTypeList.toTypedArray())
                }else if (chooseMode == SelectMimeType.ofImage()){
                    putExtra(Intent.EXTRA_MIME_TYPES, imageMimeTypeList.toTypedArray())
                }
            }
        }
    }
}