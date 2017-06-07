package com.lyzon.ui.lilyplayer.main;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lyzon.ui.lilyplayer.R;
import com.lyzon.ui.lilyplayer.bean.Music;
import com.lyzon.ui.lilyplayer.menuswipe.MenuSwipe;
import com.lyzon.ui.lilyplayer.service.PlayService;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> , ViewPager.OnPageChangeListener ,OnRecyclerViewClickListener ,View.OnClickListener{

    //本地音乐列表
   private List<Music> musicList = new ArrayList<>();
    //展示音乐列表的RecyclerView
    private RecyclerView musicListRecyclerView;

    private ViewPager coverViewPager;

    //歌曲名和歌手名
    private TextView title;
    private TextView artist;

    //用于改变背景色
    private View rootView;
    private View topText;

    //三个按钮
    private ImageButton buttonNext;
    private ImageButton buttonPrevious;
    private ImageButton buttonPlay;

    //时长
    private TextView textDuration;
    private TextView textProgress;
    private SeekBar seekBar;

    private MenuSwipe menuSwipe;

    private MusicAdapter adapter;
    private CoverPagerAdapter coverPagerAdapter;

    private PlayService.PlayBinder mPlayService;

    private boolean seekBarOnFocus = false;

    public MainFragment() {
        // Required empty public constructor
    }

    Handler handler = new Handler();

    private Runnable runnable = new Runnable(){
        //这个方法是运行在UI线程中的
        @Override
        public void run() {
            updateSeekBar();
            handler.postDelayed(this, 1000);// 1000ms后执行this，即runable
        }
    };

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("service","service connected");
            mPlayService = (PlayService.PlayBinder) service;
            mPlayService.play(musicList.get(coverViewPager.getCurrentItem()),false);
            handler.postDelayed(runnable, 1000);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("service","dis connected");
            mPlayService = null;
        }
    };

    private void updateSeekBar(){
        Log.e("sssss","update");
        int position = mPlayService.getCurrentPosition();
        if(!seekBarOnFocus)
            seekBar.setProgress(position);

        if(mPlayService.isComplete())
            coverViewPager.setCurrentItem(coverViewPager.getCurrentItem()+1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        musicListRecyclerView = (RecyclerView) view.findViewById(R.id.music_list_view);
        musicListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        musicListRecyclerView.setAdapter(adapter = new MusicAdapter());
        adapter.setOnClickListener(this);

        coverViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        coverViewPager.setAdapter(coverPagerAdapter = new CoverPagerAdapter());
        coverViewPager.addOnPageChangeListener(this);

        menuSwipe = (MenuSwipe) view.findViewById(R.id.menuSwipe);
        rootView = view.findViewById(R.id.play_root_view);
        topText = view.findViewById(R.id.topText);
        title = (TextView) view.findViewById(R.id.title);
        artist = (TextView) view.findViewById(R.id.artist);

        buttonNext = (ImageButton) view.findViewById(R.id.next);
        buttonPrevious = (ImageButton) view.findViewById(R.id.previous);
        buttonPlay = (ImageButton) view.findViewById(R.id.play);
        buttonNext.setOnClickListener(this);
        buttonPrevious.setOnClickListener(this);
        buttonPlay.setOnClickListener(this);

        textDuration = (TextView) view.findViewById(R.id.duration);
        textProgress = (TextView) view.findViewById(R.id.progress);

        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /*
            * seekbar改变时的事件监听处理
            * */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textProgress.setText(formatTime(progress));
            }
            /*
            * 按住seekbar时的事件监听处理
            * */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarOnFocus = true ;
            }
            /*
            * 放开seekbar时的时间监听处理
            * */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBarOnFocus = false;
                mPlayService.seekTo(seekBar.getProgress());
                setButtonPlaySrc();
            }
        });

        getLoaderManager().initLoader(0, null, this);

        return view;
    }


    /**
     *音乐列表项点击监听
     */
    @Override
    public void onItemClick(int position) {
        if(mPlayService!=null && coverViewPager.getCurrentItem() != position)
            coverViewPager.setCurrentItem(position,false);

        menuSwipe.closeMenu();
    }

    @Override
    public void onClick(View v) {
        int i = coverViewPager.getCurrentItem();
        switch (v.getId()){
            case R.id.next:
                if(coverViewPager.getCurrentItem() == musicList.size()-1)
                    coverViewPager.setCurrentItem(0);
                else
                    coverViewPager.setCurrentItem(++i);
                break;
            case R.id.previous:
                if(coverViewPager.getCurrentItem() == 0)
                    coverViewPager.setCurrentItem(musicList.size()-1);
                else
                    coverViewPager.setCurrentItem(--i);
                break;
            case R.id.play:
                mPlayService.playOrPause();
                setButtonPlaySrc();
                break;
            default:
                break;
        }
    }

    private void setButtonPlaySrc(){
        if(mPlayService.isPlaying())
            buttonPlay.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.ic_pause_circle_outline_white_48dp));
        else
            buttonPlay.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.ic_play_circle_outline_white_48dp));
    }

    /**
     * 播放音乐
     * @param position 当前音乐在列表中的位置
     */
    private void play(int position){
        seekBar.setProgress(0);
        setMusicInfo(position);
        mPlayService.play(musicList.get(position),true);
        setButtonPlaySrc();
    }

    public  String formatTime(int time) {
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if (min.length() < 2) {
            min = "0" + time / (1000 * 60) + "";
        } else {
            min = time / (1000 * 60) + "";
        }
        if (sec.length() == 4) {
            sec = "0" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 3) {
            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {
            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // 查询音乐
        return new CursorLoader(getActivity(),
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(data == null)
            return;

        while (data.moveToNext()) {
            // 是否为音乐
            int isMusic = data.getInt(data.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            if (isMusic == 0) {
                continue;
            }

            Music music = new Music();
            music.id = data.getLong(data.getColumnIndex(MediaStore.Audio.Media._ID));
            music.title = data.getString((data.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            music.artist = data.getString(data.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            music.duration =data.getLong(data.getColumnIndex(MediaStore.Audio.Media.DURATION));
            music.album = data.getString((data.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
            music.albumId = data.getLong(data.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            music.albumCoverUrl = getAlbumCoverUri(music.albumId);
            music.uri = data.getString(data.getColumnIndex(MediaStore.Audio.Media.DATA));
            music.fileSize = data.getLong(data.getColumnIndex(MediaStore.Audio.Media.SIZE));

            musicList.add(music);
        }

        showMusicList();

        //好像不需要手动调用，但是调用了不知道有没有副作用，还是忍不住写上了。
        data.close();
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        musicList.clear();
        adapter.notifyDataSetChanged();
    }

    /**
     *展示音乐列表
     */
    public void showMusicList(){
        Collections.shuffle(musicList);
        adapter.notifyItemRangeChanged(0,musicList.size());
        coverPagerAdapter.notifyDataSetChanged();
        bindPlayService();
        setMusicInfo(0);
    }


    /**
     *设置当前播放的音乐信息
     */
    private void setMusicInfo(int position){
        Music music = musicList.get(position);
        title.setText(music.title);
        artist.setText(music.artist);
        textDuration.setText(formatTime((int)music.duration));
        //初始化seekbar
        seekBar.setMax((int)music.duration);
        setBackgroundColor();
    }


    /**
     *绑定播放服务
     */
    private void bindPlayService(){
        Log.e("service","bind ");
        //启动服务
        Intent intent = new Intent(getContext(),PlayService.class);
        getContext().bindService(intent,connection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 查询专辑封面图片uri
     */
    private String getAlbumCoverUri(long albumId) {
        String uri = null;
        Cursor cursor = getActivity().getContentResolver().query(
                Uri.parse("content://media/external/audio/albums/" + albumId),
                new String[]{"album_art"}, null, null, null);
        if (cursor != null) {
            cursor.moveToNext();
            uri = cursor.getString(0);
            cursor.close();
        }
        return uri;
    }

    /**
     * 取出当前显示的专辑图片的bitmap活力色调，把背景颜色改变。
     */
    private void setBackgroundColor() {

        ImageView view = (ImageView)coverViewPager.findViewWithTag(coverViewPager.getCurrentItem());

        if(view == null)
            return;
        if(view.getDrawable() == null)
            return;

        Bitmap bitmap = ((BitmapDrawable) view.getDrawable()).getBitmap();

        if (bitmap != null) {
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    //1.活力颜色
                    Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                    Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();
                    if (darkVibrantSwatch != null) {
                        rootView.setBackgroundColor(darkVibrantSwatch.getRgb());
                        topText.setBackgroundColor(darkVibrantSwatch.getRgb());
                        return;
                    }
                    if(vibrantSwatch != null){
                        rootView.setBackgroundColor(vibrantSwatch.getRgb());
                        topText.setBackgroundColor(vibrantSwatch.getRgb());
                    }else{
                        rootView.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.contentPrimary));
                        topText.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.contentPrimary));
                    }

                }
            });
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }
    @Override
    public void onPageSelected(int position) {
        play(position);
    }
    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        handler.removeCallbacks(runnable);
        getActivity().unbindService(connection);
    }

    class CoverPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return musicList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.cover_item,null);
            ImageView cover = (ImageView) view.findViewById(R.id.cover_iv);
            Picasso.with(getContext()).load(new File(musicList.get(position).albumCoverUrl)).into(cover);
            cover.setTag(position);
            container.addView(view);
            return view;
        }

    }


    class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicListViewHolder> {

        OnRecyclerViewClickListener listener;

        @Override
        public MusicListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MusicListViewHolder(LayoutInflater.from(
                    getActivity()).inflate(R.layout.music_list_item, parent,
                    false));
        }

        @Override
        public void onBindViewHolder(MusicListViewHolder holder, int position) {
            Music music = musicList.get(position);
            holder.title.setText(music.title);
            holder.author.setText(music.artist);
        }

        @Override
        public int getItemCount() {
            return musicList.size();
        }

        public void setOnClickListener(OnRecyclerViewClickListener listener){
            this.listener = listener;
        }

        class MusicListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private TextView title;
            private TextView author;

            public MusicListViewHolder(View view) {
                super(view);
                title = (TextView) view.findViewById(R.id.music_title);
                author = (TextView) view.findViewById(R.id.music_author);
                view.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if(listener != null)
                    listener.onItemClick(getLayoutPosition());
            }

        }
    }

}
