package com.luck.pictureselector.newlib.out.style;


import com.luck.picture.lib.style.PictureSelectorStyle;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;

public class UpPictureSelectorStyle extends PictureSelectorStyle {

   public UpPictureSelectorStyle(){
      PictureWindowAnimationStyle animationStyle = new PictureWindowAnimationStyle();
      animationStyle.setActivityEnterAnimation(com.luck.picture.lib.R.anim.ps_anim_up_in);
      animationStyle.setActivityExitAnimation(com.luck.picture.lib.R.anim.ps_anim_down_out);
      setWindowAnimationStyle(animationStyle);
   }
}
