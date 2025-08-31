package com.luck.pictureselector.newlib;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener;
import com.luck.picture.lib.utils.DateUtils;
import com.luck.picture.lib.utils.SdkVersionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import spa.lyh.cn.chooser.PicChooser;
import spa.lyh.cn.chooser.PicListData;
import spa.lyh.cn.chooser.engine.ChooserCompressFileEngine;
import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnNewCompressListener;
import top.zibin.luban.OnRenameListener;

public class ImageFileCompressEngine implements ChooserCompressFileEngine {
   @Override
   public void onStartCompress(Context context, ArrayList<Uri> source, OnKeyValueResultCallbackListener call) {
      Luban.with(context).load(source).ignoreBy(100).setRenameListener(new OnRenameListener() {
         @Override
         public String rename(String filePath) {
            int indexOf = filePath.lastIndexOf(".");
            String postfix = indexOf != -1 ? filePath.substring(indexOf) : ".jpg";
            return DateUtils.getCreateFileName("CMP_") + postfix;
         }
      }).filter(new CompressionPredicate() {
         @Override
         public boolean apply(String path) {
            if (PictureMimeType.isUrlHasImage(path) && !PictureMimeType.isHasHttp(path)) {
               return true;
            }
            return !PictureMimeType.isUrlHasGif(path);
         }
      }).setCompressListener(new OnNewCompressListener() {
         @Override
         public void onStart() {

         }

         @Override
         public void onSuccess(String source, File compressFile) {
            if (call != null) {
               call.onCallback(source, compressFile.getAbsolutePath());
            }
         }

         @Override
         public void onError(String source, Throwable e) {
            if (call != null) {
               call.onCallback(source, null);
            }
         }
      }).launch();
   }

   @Override
   public void goCompress(Context context, PicChooser picChooser) {
      ArrayList<Uri> uris = new ArrayList<>();
      ConcurrentHashMap<String, LocalMedia> queue = new ConcurrentHashMap<>();
      for (int i = 0; i < PicListData.getInstance().mediaList.size(); i++) {
         LocalMedia mediaC = PicListData.getInstance().mediaList.get(i);
         String availablePath = mediaC.getAvailablePath();
         if (PictureMimeType.isHasImage(mediaC.getMimeType())) {
            Uri a = PictureMimeType.isContent(availablePath) ? Uri.parse(availablePath) : Uri.fromFile(new File(availablePath));
            uris.add(a);
            queue.put(availablePath, mediaC);
         }
      }
      if (queue.size() == 0) {
         if (picChooser.callback != null){
            picChooser.callback.onResult(PicListData.getInstance().mediaList);
         }
      }else{
         picChooser.compressFileEngine.onStartCompress(context, uris, new OnKeyValueResultCallbackListener() {
            @Override
            public void onCallback(String srcPath, String compressPath) {
               if (TextUtils.isEmpty(srcPath)) {
                  if (picChooser.callback != null){
                     picChooser.callback.onResult(PicListData.getInstance().mediaList);
                  }
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
                     if (picChooser.callback != null){
                        picChooser.callback.onResult(PicListData.getInstance().mediaList);
                     }
                  }
               }
            }
         });
      }
   }
}
