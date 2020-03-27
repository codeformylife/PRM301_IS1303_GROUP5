package com.example.is1303_group5.activity.service;

import android.content.Intent;
import android.media.MediaPlayer;
import android.app.Service;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.is1303_group5.activity.model.Song;

import java.util.ArrayList;

public class SoundService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    public class MusicBinder extends Binder {
        public SoundService getService() {
            return SoundService.this;
        }
    }

    private final IBinder musicBind = new MusicBinder();

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songIndex;
    public boolean isPlay = false;
    private boolean isLoop = true;
    private boolean isMix = false;

    public SoundService() {
    }

    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
    }

    public void setDurationSong(int a) {
        if (isPlay) {
            player.seekTo(a);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
