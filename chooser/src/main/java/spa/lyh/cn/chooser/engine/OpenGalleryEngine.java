package spa.lyh.cn.chooser.engine;

import android.app.Activity;

public interface OpenGalleryEngine {

    void launch(Activity activity);

    void updateMaxItems(int maxItems);
}
