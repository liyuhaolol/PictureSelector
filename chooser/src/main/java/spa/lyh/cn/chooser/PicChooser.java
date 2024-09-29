package spa.lyh.cn.chooser;


import android.app.Activity;
import android.os.Build;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.luck.picture.lib.basic.PictureSelectionModel;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.config.SelectorConfig;
import com.luck.picture.lib.config.SelectorProviders;
import com.luck.picture.lib.engine.CompressFileEngine;
import com.luck.picture.lib.engine.CropFileEngine;
import com.luck.picture.lib.engine.ImageEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnPermissionsInterceptListener;
import com.luck.picture.lib.interfaces.OnRequestPermissionListener;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.luck.picture.lib.style.PictureSelectorStyle;
import com.luck.picture.lib.utils.BitmapUtils;
import com.luck.picture.lib.utils.SdkVersionUtils;

import java.util.ArrayList;

import spa.lyh.cn.chooser.engine.OpenGalleryEngine;

public class PicChooser {
    private Activity activity;
    public int chooseMode = SelectMimeType.ofAll();
    public boolean isGif = true;
    public int selectionMode = SelectModeConfig.MULTIPLE;
    public int maxSelectNum = 1;
    public CropFileEngine cropFileEngine = null;
    public CompressFileEngine compressFileEngine = null;
    private PictureSelectionModel model = null;
    private PictureSelectorStyle uiStyle = null;
    public OpenGalleryEngine openGalleryEngine = null;
    public OnResultCallbackListener<LocalMedia> callback = null;
    public ArrayList<LocalMedia> mediaList;
    private ImageEngine imageEngine = null;

    private static PicChooser instance;


    // 提供全局访问点
    public static synchronized PicChooser getInstance(Activity activity) {
        if (instance == null) {
            instance = new PicChooser(activity);
        }
        return instance;
    }


    private PicChooser(Activity activity){
        this.activity = activity;
        mediaList = new ArrayList<>();
    }
/*    public static PicChooser create(Activity activity) {
        return new PicChooser(activity);
    }*/

    public PicChooser openGallery(int chooseMode) {
        this.chooseMode = chooseMode;
        return this;
    }

    public PicChooser isGif(boolean isGif) {
        this.isGif = isGif;
        return this;
    }

    public PicChooser setSelectionMode(int selectionMode) {
        this.selectionMode = selectionMode;
        return this;
    }

    public PicChooser setMaxSelectNum(int maxSelectNum) {
        this.maxSelectNum = maxSelectNum;
        if (openGalleryEngine != null){
            openGalleryEngine.updateMaxItems(maxSelectNum);
        }
        return this;
    }

    public PicChooser setCropEngine(CropFileEngine engine) {
        cropFileEngine = engine;
        return this;
    }

    public PicChooser setCompressEngine(CompressFileEngine engine) {
        compressFileEngine = engine;
        return this;
    }

    public PicChooser setSelectorUIStyle(PictureSelectorStyle uiStyle) {
        this.uiStyle = uiStyle;
        return this;
    }

    public PicChooser setOpenGalleryEngine(OpenGalleryEngine openGalleryEngine) {
        this.openGalleryEngine = openGalleryEngine;
        return this;
    }

    public PicChooser setImageEngine(ImageEngine engine) {
        this.imageEngine = engine;
        return this;
    }

/*    public PicChooser build(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidGalleryEngine != null){
                androidGalleryEngine.init(activity);
            }else {
                build12();
            }
        }else{
            build12();
        }
        return this;
    }*/

    private void build12(){
        model = PictureSelector.create(activity)
                .openGallery(chooseMode)
                .isGif(isGif)
                .setSelectionMode(selectionMode);
        if (selectionMode == SelectModeConfig.MULTIPLE){
            model.setMaxSelectNum(maxSelectNum);
        }
        model.setSelectorUIStyle(uiStyle)
                .setImageEngine(imageEngine)
                .isDisplayCamera(false)
                .setCropEngine(cropFileEngine)
                .setCompressEngine(compressFileEngine)
                .isDirectReturnSingle(true)
                .setPermissionsInterceptListener(new OnPermissionsInterceptListener() {
                    @Override
                    public void requestPermission(Fragment fragment, String[] permissionArray, OnRequestPermissionListener call) {
                        call.onCall(permissionArray,true);
                    }

                    @Override
                    public boolean hasPermissions(Fragment fragment, String[] permissionArray) {
                        return true;
                    }

                });
    }

    public void forResult(Activity activity,OnResultCallbackListener<LocalMedia> callback){
        this.callback = callback;
        if (maxSelectNum == 1){
            setSelectionMode(SelectModeConfig.SINGLE);
        }
        build12();
        mediaList.clear();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (openGalleryEngine != null){
                openGalleryEngine.launch(activity);
            }else {
                forResult12();
            }
        }else{
            forResult12();
        }

    }

    private void forResult12(){
        if (model != null){
            model.forResult(new OnResultCallbackListener<LocalMedia>() {
                @Override
                public void onResult(ArrayList<LocalMedia> result) {
                    callback.onResult(result);
                }

                @Override
                public void onCancel() {
                    callback.onCancel();
                }
            });
        }else{
            Log.e("Chooser","请先执行build完成初始化");
        }
    }

    public LocalMedia buildLocalMedia(Activity activity,String absolutePath) {
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
