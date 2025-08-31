package spa.lyh.cn.chooser.engine;

import android.content.Context;

import com.luck.picture.lib.engine.CompressFileEngine;

import spa.lyh.cn.chooser.PicChooser;

public interface ChooserCompressFileEngine extends CompressFileEngine {

    void goCompress(Context context, PicChooser picChooser);
}
