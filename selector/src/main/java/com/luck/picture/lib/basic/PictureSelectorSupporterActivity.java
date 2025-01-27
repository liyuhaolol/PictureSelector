package com.luck.picture.lib.basic;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.luck.picture.lib.PictureSelectorFragment;
import com.luck.picture.lib.R;
import com.luck.picture.lib.config.SelectorConfig;
import com.luck.picture.lib.config.SelectorProviders;
import com.luck.picture.lib.language.LanguageConfig;
import com.luck.picture.lib.language.PictureLanguageUtils;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;

/**
 * @author：luck
 * @date：2021/11/17 9:59 上午
 * @describe：PictureSelectorSupporterActivity
 */
public class PictureSelectorSupporterActivity extends AppCompatActivity {
    private SelectorConfig selectorConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(
                this,
                SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Window window = this.getWindow();
            if (window != null){
                window.setStatusBarContrastEnforced(false);
                window.setNavigationBarContrastEnforced(false);
            }
        }
        initSelectorConfig();
        setContentView(R.layout.ps_activity_container);
        setupFragment();
    }

    private void initSelectorConfig() {
        selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
    }

    private void setupFragment() {
        FragmentInjectManager.injectFragment(this, PictureSelectorFragment.TAG,
                PictureSelectorFragment.newInstance());
    }

    /**
     * set app language
     */
    public void initAppLanguage() {
        if (selectorConfig != null && selectorConfig.language != LanguageConfig.UNKNOWN_LANGUAGE && !selectorConfig.isOnlyCamera) {
            PictureLanguageUtils.setAppLanguage(this, selectorConfig.language, selectorConfig.defaultLanguage);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initAppLanguage();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SelectorConfig selectorConfig = SelectorProviders.getInstance().getSelectorConfig();
        if (selectorConfig != null) {
            super.attachBaseContext(PictureContextWrapper.wrap(newBase, selectorConfig.language, selectorConfig.defaultLanguage));
        } else {
            super.attachBaseContext(newBase);
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (selectorConfig != null) {
            PictureWindowAnimationStyle windowAnimationStyle = selectorConfig.selectorStyle.getWindowAnimationStyle();
            overridePendingTransition(0, windowAnimationStyle.activityExitAnimation);
        }
    }
}
