package com.lyzon.ui.lilyplayer.main;

/**
 * Created by laoyongzhi on 2017/6/8.
 */

public interface PlayerView {

    void playStateChange(boolean isPlaying);

    void onComplete();

    void progressUpdate(int progress);

}
