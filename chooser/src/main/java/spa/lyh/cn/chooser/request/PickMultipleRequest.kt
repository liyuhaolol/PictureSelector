package spa.lyh.cn.chooser.request

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.luck.picture.lib.config.SelectMimeType
import java.util.ArrayList

class PickMultipleRequest(private var maxItems:Int, val imageMimeTypeList: ArrayList<String>, val allMimeTypeList: ArrayList<String>): ActivityResultContracts.PickMultipleVisualMedia(maxItems) {
    var chooseMode = SelectMimeType.ofAll()
    var isGif = true

    fun updateMaxItems(newMaxItems:Int){
        maxItems = newMaxItems
    }
    override fun createIntent(context: Context, input: PickVisualMediaRequest): Intent {
        return super.createIntent(context, input).apply {
            if (!isGif){
                if (chooseMode == SelectMimeType.ofAll()){
                    putExtra(Intent.EXTRA_MIME_TYPES, allMimeTypeList.toTypedArray())
                }else if (chooseMode == SelectMimeType.ofImage()){
                    putExtra(Intent.EXTRA_MIME_TYPES, imageMimeTypeList.toTypedArray())
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, maxItems)
            }
        }
    }
}