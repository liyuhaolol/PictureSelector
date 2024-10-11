package spa.lyh.cn.chooser.engine;

import com.luck.picture.lib.engine.CropFileEngine;


import spa.lyh.cn.chooser.PicChooser;

public interface ChooserCropFileEngine extends CropFileEngine {
    void setPicChooser(PicChooser picChooser);
}
