package com.yalantis.ucrop;

import com.yalantis.ucrop.listener.OnResultListener;

public class ListenerContainer {
    private static ListenerContainer instance;
    private OnResultListener listener;

    private ListenerContainer() {
        // 私有构造函数，防止外部实例化
    }

    public static synchronized ListenerContainer getInstance() {
        if (instance == null) {
            instance = new ListenerContainer();
        }
        return instance;
    }

    public void setOnResultListener(OnResultListener listener) {
        this.listener = listener;
    }

    public OnResultListener getOnResultListener(){
        return listener;
    }
}
