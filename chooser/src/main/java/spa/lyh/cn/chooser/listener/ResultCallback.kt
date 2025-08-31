package spa.lyh.cn.chooser.listener

import android.net.Uri

interface ResultCallback {
    fun onActivityResult(result: List<Uri>)
}