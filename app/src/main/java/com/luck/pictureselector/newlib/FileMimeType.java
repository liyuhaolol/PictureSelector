package com.luck.pictureselector.newlib;

import java.util.ArrayList;

public class FileMimeType {

   public static ArrayList<String> getImageMimeType(){
      //返回已知的图片类型，去除gif
      ArrayList<String> list = new ArrayList<>();
      list.add("image/bmp");
      list.add("image/jpeg");
      list.add("image/png");
      list.add("image/webp");
      return list;
   }

   public static ArrayList<String> getImageAndVideoMimeType(){
      ArrayList<String> list = getImageMimeType();
      list.add("video/3gpp");
      list.add("video/x-ms-asf");
      list.add("video/x-msvideo");
      list.add("video/x-flv");
      list.add("video/x-m4v");
      list.add("video/quicktime");
      list.add("video/mp4");
      list.add("video/mpeg");
      return list;
   }
}
