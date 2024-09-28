package com.luck.picture.lib.engine;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.luck.picture.lib.basic.IBridgePictureBehavior;
import com.luck.picture.lib.basic.PictureCommonFragment;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.Crop;
import com.luck.picture.lib.config.CustomIntentKey;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectorConfig;
import com.luck.picture.lib.config.SelectorProviders;
import com.luck.picture.lib.dialog.PictureLoadingDialog;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnCallbackIndexListener;
import com.luck.picture.lib.interfaces.OnCallbackListener;
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener;
import com.luck.picture.lib.thread.PictureThreadUtils;
import com.luck.picture.lib.utils.ActivityCompatHelper;
import com.luck.picture.lib.utils.BitmapUtils;
import com.luck.picture.lib.utils.DateUtils;
import com.luck.picture.lib.utils.FileDirMap;
import com.luck.picture.lib.utils.SdkVersionUtils;
import com.luck.picture.lib.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OpenGalleryEngine {
    public ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private AppCompatActivity activity;
    private Dialog mLoadingDialog;
    protected IBridgePictureBehavior iBridgePictureBehavior;

    public OpenGalleryEngine(AppCompatActivity activity){
        this.activity = activity;
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        if (selectorConfig.onCustomLoadingListener != null) {
            mLoadingDialog = selectorConfig.onCustomLoadingListener.create(activity);
        } else {
            mLoadingDialog = new PictureLoadingDialog(activity);
        }
        //iBridgePictureBehavior = (IBridgePictureBehavior) activity;
        try{
            pickMedia = activity.registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        Log.e("qwer", "Selected URI: $uri");
                        dispatchTransformResult(uri);
                    } else {
                        Log.e("OpenGalleryEngine", "No media selected");
                    }
                }
            });
        }catch (IllegalStateException e){
            Log.e("OpenGalleryEngine","OpenGalleryEngine需要在Activity执行Start生命周期方法之前进行初始化");
        }

    }

    protected void dispatchTransformResult(Uri uri){
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        ArrayList<LocalMedia> result = new ArrayList<>();
        LocalMedia media = buildLocalMedia(uri.toString());
        media.setPath(SdkVersionUtils.isQ() ? media.getPath() : media.getRealPath());
        result.add(media);
        selectorConfig.addSelectResult(media);
        if (checkCropValidity()) {
            onCrop(result);
        } else if (checkOldCropValidity()) {
            onOldCrop(result);
        } else if (checkCompressValidity()) {
            onCompress(result);
        } else if (checkOldCompressValidity()) {
            onOldCompress(result);
        } else {
            onResultEvent(result);
        }
    }

    public boolean checkCropValidity() {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        if (selectorConfig.cropFileEngine != null) {
            HashSet<String> filterSet = new HashSet<>();
            List<String> filters = selectorConfig.skipCropList;
            if (filters != null && filters.size() > 0) {
                filterSet.addAll(filters);
            }
            if (selectorConfig.getSelectCount() == 1) {
                String mimeType = selectorConfig.getResultFirstMimeType();
                boolean isHasImage = PictureMimeType.isHasImage(mimeType);
                if (isHasImage) {
                    if (filterSet.contains(mimeType)) {
                        return false;
                    }
                }
                return isHasImage;
            } else {
                int notSupportCropCount = 0;
                for (int i = 0; i < selectorConfig.getSelectCount(); i++) {
                    LocalMedia media = selectorConfig.getSelectedResult().get(i);
                    if (PictureMimeType.isHasImage(media.getMimeType())) {
                        if (filterSet.contains(media.getMimeType())) {
                            notSupportCropCount++;
                        }
                    }
                }
                return notSupportCropCount != selectorConfig.getSelectCount();
            }
        }
        return false;
    }

    public boolean checkOldCropValidity() {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        if (selectorConfig.cropEngine != null) {
            HashSet<String> filterSet = new HashSet<>();
            List<String> filters = selectorConfig.skipCropList;
            if (filters != null && filters.size() > 0) {
                filterSet.addAll(filters);
            }
            if (selectorConfig.getSelectCount() == 1) {
                String mimeType = selectorConfig.getResultFirstMimeType();
                boolean isHasImage = PictureMimeType.isHasImage(mimeType);
                if (isHasImage) {
                    if (filterSet.contains(mimeType)) {
                        return false;
                    }
                }
                return isHasImage;
            } else {
                int notSupportCropCount = 0;
                for (int i = 0; i < selectorConfig.getSelectCount(); i++) {
                    LocalMedia media = selectorConfig.getSelectedResult().get(i);
                    if (PictureMimeType.isHasImage(media.getMimeType())) {
                        if (filterSet.contains(media.getMimeType())) {
                            notSupportCropCount++;
                        }
                    }
                }
                return notSupportCropCount != selectorConfig.getSelectCount();
            }
        }
        return false;
    }


    public boolean checkCompressValidity() {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        if (selectorConfig.compressFileEngine != null) {
            for (int i = 0; i < selectorConfig.getSelectCount(); i++) {
                LocalMedia media = selectorConfig.getSelectedResult().get(i);
                if (PictureMimeType.isHasImage(media.getMimeType())) {
                    return true;
                }
            }
        }
        return false;
    }


    public boolean checkOldCompressValidity() {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        if (selectorConfig.compressEngine != null) {
            for (int i = 0; i < selectorConfig.getSelectCount(); i++) {
                LocalMedia media = selectorConfig.getSelectedResult().get(i);
                if (PictureMimeType.isHasImage(media.getMimeType())) {
                    return true;
                }
            }
        }
        return false;
    }


    protected LocalMedia buildLocalMedia(String absolutePath) {
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


    public void onCrop(ArrayList<LocalMedia> result) {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        Uri srcUri = null;
        Uri destinationUri = null;
        ArrayList<String> dataCropSource = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            LocalMedia media = result.get(i);
            dataCropSource.add(media.getAvailablePath());
            if (srcUri == null && PictureMimeType.isHasImage(media.getMimeType())) {
                String currentCropPath = media.getAvailablePath();
                if (PictureMimeType.isContent(currentCropPath) || PictureMimeType.isHasHttp(currentCropPath)) {
                    srcUri = Uri.parse(currentCropPath);
                } else {
                    srcUri = Uri.fromFile(new File(currentCropPath));
                }
                String fileName = DateUtils.getCreateFileName("CROP_") + ".jpg";
                Context context = activity;
                File externalFilesDir = new File(FileDirMap.getFileDirPath(context, SelectMimeType.TYPE_IMAGE));
                File outputFile = new File(externalFilesDir.getAbsolutePath(), fileName);
                destinationUri = Uri.fromFile(outputFile);
            }
        }
        selectorConfig.cropFileEngine.onStartCrop(activity, srcUri, destinationUri, dataCropSource, Crop.REQUEST_CROP);
    }

    public void onOldCrop(ArrayList<LocalMedia> result) {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        LocalMedia currentLocalMedia = null;
        for (int i = 0; i < result.size(); i++) {
            LocalMedia item = result.get(i);
            if (PictureMimeType.isHasImage(result.get(i).getMimeType())) {
                currentLocalMedia = item;
                break;
            }
        }
        selectorConfig.cropEngine.onStartCrop(activity, currentLocalMedia, result, Crop.REQUEST_CROP);
    }

    public void onCompress(ArrayList<LocalMedia> result) {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        showLoading();
        ConcurrentHashMap<String, LocalMedia> queue = new ConcurrentHashMap<>();
        ArrayList<Uri> source = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            LocalMedia media = result.get(i);
            String availablePath = media.getAvailablePath();
            if (PictureMimeType.isHasHttp(availablePath)) {
                continue;
            }
            if (selectorConfig.isCheckOriginalImage && selectorConfig.isOriginalSkipCompress) {
                continue;
            }
            if (PictureMimeType.isHasImage(media.getMimeType())) {
                Uri uri = PictureMimeType.isContent(availablePath) ? Uri.parse(availablePath) : Uri.fromFile(new File(availablePath));
                source.add(uri);
                queue.put(availablePath, media);
            }
        }
        if (queue.size() == 0) {
            onResultEvent(result);
        } else {
            selectorConfig.compressFileEngine.onStartCompress(activity, source, new OnKeyValueResultCallbackListener() {
                @Override
                public void onCallback(String srcPath, String compressPath) {
                    if (TextUtils.isEmpty(srcPath)) {
                        onResultEvent(result);
                    } else {
                        LocalMedia media = queue.get(srcPath);
                        if (media != null) {
                            if (SdkVersionUtils.isQ()){
                                if (!TextUtils.isEmpty(compressPath) && (compressPath.contains("Android/data/")
                                        || compressPath.contains("data/user/"))) {
                                    media.setCompressPath(compressPath);
                                    media.setCompressed(!TextUtils.isEmpty(compressPath));
                                    media.setSandboxPath(media.getCompressPath());
                                }
                            } else {
                                media.setCompressPath(compressPath);
                                media.setCompressed(!TextUtils.isEmpty(compressPath));
                            }
                            queue.remove(srcPath);
                        }
                        if (queue.size() == 0) {
                            onResultEvent(result);
                        }
                    }
                }
            });
        }
    }

    public void showLoading() {
        try {
            if (ActivityCompatHelper.isDestroy(activity)) {
                return;
            }
            if (mLoadingDialog!= null && !mLoadingDialog.isShowing()) {
                mLoadingDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onResultEvent(ArrayList<LocalMedia> result) {
        if (checkTransformSandboxFile()) {
            uriToFileTransform29(result);
        } else if (checkOldTransformSandboxFile()) {
            copyExternalPathToAppInDirFor29(result);
        } else {
            mergeOriginalImage(result);
            dispatchUriToFileTransformResult(result);
        }
    }

    public boolean checkTransformSandboxFile() {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        return SdkVersionUtils.isQ() && selectorConfig.uriToFileTransformEngine != null;
    }

    public boolean checkOldTransformSandboxFile() {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        return SdkVersionUtils.isQ() && selectorConfig.sandboxFileEngine != null;
    }

    /**
     * SDK > 29 把外部资源copy一份至应用沙盒内
     *
     * @param result
     */
    private void uriToFileTransform29(ArrayList<LocalMedia> result) {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        showLoading();
        ConcurrentHashMap<String, LocalMedia> queue = new ConcurrentHashMap<>();
        for (int i = 0; i < result.size(); i++) {
            LocalMedia media = result.get(i);
            queue.put(media.getPath(), media);
        }
        if (queue.size() == 0) {
            dispatchUriToFileTransformResult(result);
        } else {
            PictureThreadUtils.executeByIo(new PictureThreadUtils.SimpleTask<ArrayList<LocalMedia>>() {

                @Override
                public ArrayList<LocalMedia> doInBackground() {
                    for (Map.Entry<String, LocalMedia> entry : queue.entrySet()) {
                        LocalMedia media = entry.getValue();
                        if (selectorConfig.isCheckOriginalImage || TextUtils.isEmpty(media.getSandboxPath())) {
                            selectorConfig.uriToFileTransformEngine.onUriToFileAsyncTransform(activity, media.getPath(), media.getMimeType(), new OnKeyValueResultCallbackListener() {
                                @Override
                                public void onCallback(String srcPath, String resultPath) {
                                    if (TextUtils.isEmpty(srcPath)) {
                                        return;
                                    }
                                    LocalMedia media = queue.get(srcPath);
                                    if (media != null) {
                                        if (TextUtils.isEmpty(media.getSandboxPath())) {
                                            media.setSandboxPath(resultPath);
                                        }
                                        if (selectorConfig.isCheckOriginalImage) {
                                            media.setOriginalPath(resultPath);
                                            media.setOriginal(!TextUtils.isEmpty(resultPath));
                                        }
                                        queue.remove(srcPath);
                                    }
                                }
                            });
                        }
                    }
                    return result;
                }

                @Override
                public void onSuccess(ArrayList<LocalMedia> result) {
                    PictureThreadUtils.cancel(this);
                    dispatchUriToFileTransformResult(result);
                }
            });
        }
    }

    /**
     * SDK > 29 把外部资源copy一份至应用沙盒内
     *
     * @param result
     */
    @Deprecated
    private void copyExternalPathToAppInDirFor29(ArrayList<LocalMedia> result) {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        showLoading();
        PictureThreadUtils.executeByIo(new PictureThreadUtils.SimpleTask<ArrayList<LocalMedia>>() {
            @Override
            public ArrayList<LocalMedia> doInBackground() {
                for (int i = 0; i < result.size(); i++) {
                    LocalMedia media = result.get(i);
                    selectorConfig.sandboxFileEngine.onStartSandboxFileTransform(activity, selectorConfig.isCheckOriginalImage, i,
                            media, new OnCallbackIndexListener<LocalMedia>() {
                                @Override
                                public void onCall(LocalMedia data, int index) {
                                    LocalMedia media = result.get(index);
                                    media.setSandboxPath(data.getSandboxPath());
                                    if (selectorConfig.isCheckOriginalImage) {
                                        media.setOriginalPath(data.getOriginalPath());
                                        media.setOriginal(!TextUtils.isEmpty(data.getOriginalPath()));
                                    }
                                }
                            });
                }
                return result;
            }

            @Override
            public void onSuccess(ArrayList<LocalMedia> result) {
                PictureThreadUtils.cancel(this);
                dispatchUriToFileTransformResult(result);
            }
        });
    }

    private void dispatchUriToFileTransformResult(ArrayList<LocalMedia> result) {
        showLoading();
        if (checkAddBitmapWatermark()) {
            addBitmapWatermark(result);
        } else if (checkVideoThumbnail()) {
            videoThumbnail(result);
        } else {
            onCallBackResult(result);
        }
    }

    public boolean checkAddBitmapWatermark() {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        return selectorConfig.onBitmapWatermarkListener != null;
    }

    public boolean checkVideoThumbnail() {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        return selectorConfig.onVideoThumbnailEventListener != null;
    }

    private void addBitmapWatermark(ArrayList<LocalMedia> result) {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        ConcurrentHashMap<String, LocalMedia> queue = new ConcurrentHashMap<>();
        for (int i = 0; i < result.size(); i++) {
            LocalMedia media = result.get(i);
            if (PictureMimeType.isHasAudio(media.getMimeType())) {
                continue;
            }
            String availablePath = media.getAvailablePath();
            queue.put(availablePath, media);
        }
        if (queue.size() == 0) {
            dispatchWatermarkResult(result);
        } else {
            for (Map.Entry<String, LocalMedia> entry : queue.entrySet()) {
                String srcPath = entry.getKey();
                LocalMedia media = entry.getValue();
                selectorConfig.onBitmapWatermarkListener.onAddBitmapWatermark(activity,
                        srcPath, media.getMimeType(), new OnKeyValueResultCallbackListener() {
                            @Override
                            public void onCallback(String srcPath, String resultPath) {
                                if (TextUtils.isEmpty(srcPath)) {
                                    dispatchWatermarkResult(result);
                                } else {
                                    LocalMedia media = queue.get(srcPath);
                                    if (media != null) {
                                        media.setWatermarkPath(resultPath);
                                        queue.remove(srcPath);
                                    }
                                    if (queue.size() == 0) {
                                        dispatchWatermarkResult(result);
                                    }
                                }
                            }
                        });
            }
        }
    }
    private void dispatchWatermarkResult(ArrayList<LocalMedia> result) {
        if (checkVideoThumbnail()) {
            videoThumbnail(result);
        } else {
            onCallBackResult(result);
        }
    }

    private void videoThumbnail(ArrayList<LocalMedia> result) {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        ConcurrentHashMap<String, LocalMedia> queue = new ConcurrentHashMap<>();
        for (int i = 0; i < result.size(); i++) {
            LocalMedia media = result.get(i);
            String availablePath = media.getAvailablePath();
            if (PictureMimeType.isHasVideo(media.getMimeType()) || PictureMimeType.isUrlHasVideo(availablePath)) {
                queue.put(availablePath, media);
            }
        }
        if (queue.size() == 0) {
            onCallBackResult(result);
        } else {
            for (Map.Entry<String, LocalMedia> entry : queue.entrySet()) {
                selectorConfig.onVideoThumbnailEventListener.onVideoThumbnail(activity, entry.getKey(), new OnKeyValueResultCallbackListener() {
                    @Override
                    public void onCallback(String srcPath, String resultPath) {
                        LocalMedia media = queue.get(srcPath);
                        if (media != null) {
                            media.setVideoThumbnailPath(resultPath);
                            queue.remove(srcPath);
                        }
                        if (queue.size() == 0) {
                            onCallBackResult(result);
                        }
                    }
                });
            }
        }
    }

    private void onCallBackResult(ArrayList<LocalMedia> result) {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        if (!ActivityCompatHelper.isDestroy(activity)) {
            dismissLoading();
            if (selectorConfig.isActivityResultBack) {
                activity.setResult(RESULT_OK, PictureSelector.putIntentResult(result));
                onSelectFinish(RESULT_OK, result);
            } else {
                if (selectorConfig.onResultCallListener != null) {
                    selectorConfig.onResultCallListener.onResult(result);
                }
            }
            //onExitPictureSelector();
        }
    }

    public void dismissLoading() {
        try {
            if (ActivityCompatHelper.isDestroy(activity)) {
                return;
            }
            if (mLoadingDialog!= null && mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onSelectFinish(int resultCode, ArrayList<LocalMedia> result) {
        if (null != iBridgePictureBehavior) {
            PictureCommonFragment.SelectorResult selectorResult = getResult(resultCode, result);
            iBridgePictureBehavior.onSelectFinish(selectorResult);
        }
    }

    protected PictureCommonFragment.SelectorResult getResult(int resultCode, ArrayList<LocalMedia> data) {
        return new PictureCommonFragment.SelectorResult(resultCode, data != null ? PictureSelector.putIntentResult(data) : null);
    }

    private void mergeOriginalImage(ArrayList<LocalMedia> result) {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        if (selectorConfig.isCheckOriginalImage) {
            for (int i = 0; i < result.size(); i++) {
                LocalMedia media = result.get(i);
                media.setOriginal(true);
                media.setOriginalPath(media.getPath());
            }
        }
    }

    public void onOldCompress(ArrayList<LocalMedia> result) {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        showLoading();
        if (selectorConfig.isCheckOriginalImage && selectorConfig.isOriginalSkipCompress) {
            onResultEvent(result);
        } else {
            selectorConfig.compressEngine.onStartCompress(activity, result,
                    new OnCallbackListener<ArrayList<LocalMedia>>() {
                        @Override
                        public void onCall(ArrayList<LocalMedia> result) {
                            onResultEvent(result);
                        }
                    });
        }
    }

    public void onResult(Intent data,int requestCode){
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        List<LocalMedia> selectedResult = selectorConfig.getSelectedResult();
        try {
            if (selectedResult.size() == 1) {
                LocalMedia media = selectedResult.get(0);
                Uri output = Crop.getOutput(data);
                media.setCutPath(output != null ? output.getPath() : "");
                media.setCut(!TextUtils.isEmpty(media.getCutPath()));
                media.setCropImageWidth(Crop.getOutputImageWidth(data));
                media.setCropImageHeight(Crop.getOutputImageHeight(data));
                media.setCropOffsetX(Crop.getOutputImageOffsetX(data));
                media.setCropOffsetY(Crop.getOutputImageOffsetY(data));
                media.setCropResultAspectRatio(Crop.getOutputCropAspectRatio(data));
                media.setCustomData(Crop.getOutputCustomExtraData(data));
                media.setSandboxPath(media.getCutPath());
            } else {
                String extra = data.getStringExtra(MediaStore.EXTRA_OUTPUT);
                if (TextUtils.isEmpty(extra)) {
                    extra = data.getStringExtra(CustomIntentKey.EXTRA_OUTPUT_URI);
                }
                JSONArray array = new JSONArray(extra);
                if (array.length() == selectedResult.size()) {
                    for (int i = 0; i < selectedResult.size(); i++) {
                        LocalMedia media = selectedResult.get(i);
                        JSONObject item = array.optJSONObject(i);
                        media.setCutPath(item.optString(CustomIntentKey.EXTRA_OUT_PUT_PATH));
                        media.setCut(!TextUtils.isEmpty(media.getCutPath()));
                        media.setCropImageWidth(item.optInt(CustomIntentKey.EXTRA_IMAGE_WIDTH));
                        media.setCropImageHeight(item.optInt(CustomIntentKey.EXTRA_IMAGE_HEIGHT));
                        media.setCropOffsetX(item.optInt(CustomIntentKey.EXTRA_OFFSET_X));
                        media.setCropOffsetY(item.optInt(CustomIntentKey.EXTRA_OFFSET_Y));
                        media.setCropResultAspectRatio((float) item.optDouble(CustomIntentKey.EXTRA_ASPECT_RATIO));
                        media.setCustomData(item.optString(CustomIntentKey.EXTRA_CUSTOM_EXTRA_DATA));
                        media.setSandboxPath(media.getCutPath());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.showToast(activity, e.getMessage());
        }

        ArrayList<LocalMedia> result = new ArrayList<>(selectedResult);
        if (checkCompressValidity()) {
            onCompress(result);
        } else if (checkOldCompressValidity()) {
            onOldCompress(result);
        } else {
            onResultEvent(result);
        }
    }
}
