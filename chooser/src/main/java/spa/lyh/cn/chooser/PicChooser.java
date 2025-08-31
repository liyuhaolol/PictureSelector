package spa.lyh.cn.chooser;


import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.luck.picture.lib.basic.PictureSelectionModel;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.engine.ImageEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnPermissionsInterceptListener;
import com.luck.picture.lib.interfaces.OnRequestPermissionListener;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.luck.picture.lib.style.PictureSelectorStyle;

import java.util.ArrayList;

import spa.lyh.cn.chooser.engine.ChooserCompressFileEngine;
import spa.lyh.cn.chooser.engine.ChooserCropFileEngine;
import spa.lyh.cn.chooser.engine.OpenGalleryEngine;

public class PicChooser {
    public int chooseMode = SelectMimeType.ofAll();
    public boolean isGif = true;
    public int selectionMode = SelectModeConfig.MULTIPLE;
    public int maxSelectNum = 1;
    public ChooserCropFileEngine cropFileEngine = null;
    public ChooserCompressFileEngine compressFileEngine = null;
    private PictureSelectionModel model = null;
    private PictureSelectorStyle uiStyle = null;
    public OpenGalleryEngine openGalleryEngine = null;
    public OnResultCallbackListener<LocalMedia> callback = null;
    private ImageEngine imageEngine = null;
    /////////////////////////////////////
    private Context context;

    private PicChooser(Context context){
        this.context = context;
    }

    public static PicChooser with(Context context){
        return new PicChooser(context);
    }
    public static PicChooser with(Fragment fragment){
        return new PicChooser(fragment.requireActivity());
    }

    public static PicChooser with(android.app.Fragment fragment){
        return new PicChooser(fragment.getActivity());
    }
    //////////////////////////////////////////////////
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
        return this;
    }

    public PicChooser setCropEngine(ChooserCropFileEngine engine) {
        cropFileEngine = engine;
        return this;
    }

    public PicChooser setCompressEngine(ChooserCompressFileEngine engine) {
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

    private void build12(){
        build12after(PictureSelector.create(context));
    }
    private void build12after(PictureSelector selector){
        model = selector
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

    public void forResult(OnResultCallbackListener<LocalMedia> callback){
        this.callback = callback;
        if (maxSelectNum <= 1){
            if (selectionMode == SelectModeConfig.MULTIPLE){
                Log.e("Chooser","选取数量为1，强制设置选择模式为单选");
            }
            setSelectionMode(SelectModeConfig.SINGLE);
        }
        build12();
        PicListData.getInstance().mediaList.clear();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (openGalleryEngine != null){
                openGalleryEngine.launch(context,this);
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

}
