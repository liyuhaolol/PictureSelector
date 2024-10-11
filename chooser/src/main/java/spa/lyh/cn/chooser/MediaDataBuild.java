package spa.lyh.cn.chooser;

import android.app.Activity;

import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectorConfig;
import com.luck.picture.lib.config.SelectorProviders;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.utils.BitmapUtils;
import com.luck.picture.lib.utils.SdkVersionUtils;

public class MediaDataBuild {

    public static LocalMedia buildLocalMedia(Activity activity, String absolutePath) {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        LocalMedia media = LocalMedia.generateLocalMedia(activity, absolutePath);
        media.setChooseModel(selectorConfig.chooseMode);
        if (SdkVersionUtils.isQ() && !PictureMimeType.isContent(absolutePath)) {
            media.setSandboxPath(absolutePath);
        } else {
            media.setSandboxPath(null);
        }
        if (selectorConfig.isCameraRotateImage && PictureMimeType.isHasImage(media.getMimeType())) {
            BitmapUtils.rotateImage(activity, absolutePath);
        }
        return media;
    }
}
