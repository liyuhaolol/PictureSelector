package spa.lyh.cn.chooser.engine;

import android.app.Activity;

import spa.lyh.cn.chooser.PicChooser;

public interface OpenGalleryEngine {

    void launch(PicChooser chooser);

    void updateMaxItems(int maxItems);
}
