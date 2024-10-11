package spa.lyh.cn.chooser;

import com.luck.picture.lib.entity.LocalMedia;

import java.util.ArrayList;

public class PicListData {

    public ArrayList<LocalMedia> mediaList;

    private static PicListData instance;

    // 提供全局访问点
    public static synchronized PicListData getInstance() {
        if (instance == null) {
            instance = new PicListData();
        }
        return instance;
    }

    private PicListData(){
        mediaList = new ArrayList<>();
    }
}
