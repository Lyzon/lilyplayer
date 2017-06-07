package com.lyzon.ui.lilyplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.lyzon.ui.lilyplayer.bean.Music;

import java.io.IOException;

/**
 * Created by laoyongzhi on 2017/6/4.
 */

public class PlayService extends Service {

    private PlayBinder mPlayBinder = new PlayBinder();

    private MediaPlayer mMediaPlayer;

    private boolean onCompletion;

    private ControlBroadcastReceiver myReceiver;

    @Override
    public void onCreate(){
        super.onCreate();
        myReceiver = new ControlBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(myReceiver, filter);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return mPlayBinder;
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                onCompletion = true;
            }
        });
    }

    public class PlayBinder extends Binder {

        public void play(Music music, boolean autoPlay) {

            onCompletion = false;

            if (mMediaPlayer == null)
                initMediaPlayer();

            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            initMediaPlayer();

            //重新加载资源
            try {
                mMediaPlayer.setDataSource(music.uri);
                mMediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (autoPlay)
                mMediaPlayer.start();
        }


        public void playOrPause() {

            if (mMediaPlayer.isPlaying())
                mMediaPlayer.pause();
            else
                mMediaPlayer.start();
        }


        public int getCurrentPosition() {
            return mMediaPlayer.getCurrentPosition();
        }

        public void seekTo(int position) {
            mMediaPlayer.seekTo(position);
            if (!mMediaPlayer.isPlaying())
                mMediaPlayer.start();
        }

        public boolean isPlaying() {
            return mMediaPlayer.isPlaying();
        }

        public boolean isComplete() {
            return onCompletion;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
        unregisterReceiver(myReceiver);
    }


    public class ControlBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String ctrl_code = intent.getAction();

            //来电时暂停播放
            if(AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(ctrl_code))
                mPlayBinder.playOrPause();

        }

    }
}