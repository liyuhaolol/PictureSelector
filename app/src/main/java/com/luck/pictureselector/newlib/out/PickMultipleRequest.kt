package com.luck.pictureselector.newlib.out

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

class PickMultipleRequest(private var maxItems:Int): ActivityResultContracts.PickMultipleVisualMedia(maxItems) {
    fun updateMaxItems(newMaxItems:Int){
        maxItems = newMaxItems
    }
    override fun createIntent(context: Context, input: PickVisualMediaRequest): Intent {
        return super.createIntent(context, input).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, maxItems)
            }
        }
    }
}