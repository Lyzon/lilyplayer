package com.lyzon.ui.lilyplayer.service;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.lyzon.ui.lilyplayer.R;
import com.lyzon.ui.lilyplayer.bean.Music;
import com.lyzon.ui.lilyplayer.main.MainActivity;
import com.lyzon.ui.lilyplayer.main.PlayerView;

import java.io.IOException;

/**
 * Created by laoyongzhi on 2017/6/4.
 */

public class PlayService extends Service {

    private PlayBinder mPlayBinder = new PlayBinder();

    private MediaPlayer mMediaPlayer;

    private PlayerView mPlayerView;

    private ControlBroadcastReceiver myReceiver;

    private Handler handler = new Handler();

    private Music playingMusic ;

    @Override
    public void onCreate(){
        super.onCreate();
        myReceiver = new ControlBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        filter.addAction("TTPlayer.PlayAction.Play");
        filter.addAction("TTPlayer.PlayAction.Next");
        registerReceiver(myReceiver, filter);
    }

    private Runnable runnable = new Runnable(){
        //这个方法是运行在UI线程中的
        @Override
        public void run() {
            mPlayerView.progressUpdate(mMediaPlayer.getCurrentPosition());
            handler.postDelayed(this, 1000);// 1000ms后执行this，即runable
        }
    };

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
                mPlayerView.onComplete();
            }
        });
    }

    public class PlayBinder extends Binder {

        public void setPlayView(PlayerView view){
            mPlayerView = view ;
            handler.postDelayed(runnable, 1000);
        }

        public void play(Music music, boolean autoPlay) {

            playingMusic = music;

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

            if (autoPlay){
                mMediaPlayer.start();
                setUpNotification();
            }

            mPlayerView.playStateChange(mMediaPlayer.isPlaying());
        }


        public void playOrPause() {
            if (mMediaPlayer.isPlaying())
                mMediaPlayer.pause();
            else
                mMediaPlayer.start();

            mPlayerView.playStateChange(mMediaPlayer.isPlaying());

            setUpNotification();
        }

        public void seekTo(int position) {
            mMediaPlayer.seekTo(position);
            if (!mMediaPlayer.isPlaying()){
                mMediaPlayer.start();
                mPlayerView.playStateChange(mMediaPlayer.isPlaying());
            }
        }

    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
        super.onDestroy();
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
        unregisterReceiver(myReceiver);
    }

    private void setUpNotification(){

        RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.player_notification_view);
        remoteViews.setTextViewText(R.id.notification_music_title,playingMusic.title);
        remoteViews.setTextViewText(R.id.notification_music_author,playingMusic.artist);

        if(mMediaPlayer.isPlaying())
            remoteViews.setImageViewResource(R.id.notification_play, R.drawable.ic_pause_circle_outline_white_48dp);
        else
            remoteViews.setImageViewResource(R.id.notification_play, R.drawable.ic_play_circle_outline_white_48dp);

        PendingIntent contentIntent = PendingIntent.getActivity(this,0,new Intent(this, MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent playIntent = PendingIntent.getBroadcast(this,0,new Intent("TTPlayer.PlayAction.Play"),0);
        PendingIntent nextIntent = PendingIntent.getBroadcast(this,0,new Intent("TTPlayer.PlayAction.Next"),0);
        remoteViews.setOnClickPendingIntent(R.id.notification_play,playIntent);
        remoteViews.setOnClickPendingIntent(R.id.notification_next,nextIntent);

        Notification notification = new Notification.Builder(this)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.mipmap.ic_launcher)
                //新方法要API 24才支持 #setCustomContentView
                .setContent(remoteViews)
                .setContentIntent(contentIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .build();

        startForeground(1,notification);
    }


    public class ControlBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String ctrl_code = intent.getAction();

            switch (ctrl_code) {
                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                case "TTPlayer.PlayAction.Play":
                    mPlayBinder.playOrPause();
                    break;
                case "TTPlayer.PlayAction.Next":
                    mPlayerView.onComplete();
                default:
                    break;
            }

        }

    }
}