package com.example.is1303_group5.activity.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.is1303_group5.R;
import com.example.is1303_group5.activity.model.Song;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.example.is1303_group5.R;

import android.widget.ImageButton;

import java.util.concurrent.TimeUnit;

import android.widget.TextView;

public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songs;
    private LayoutInflater songInf;

    public SongAdapter(Context c, ArrayList<Song> theSongs) {
        songs = theSongs;
        songInf = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song layout
        FlexboxLayout songLayout = (FlexboxLayout) songInf.inflate(R.layout.song_display_layout, parent, false);
        //get title and artist views
        TextView songView = (TextView) songLayout.findViewById(R.id.songName);
        TextView duration = (TextView) songLayout.findViewById(R.id.duration);
        ImageButton delete = (ImageButton) songLayout.findViewById(R.id.delete);
        //get song using position
        Song currSong = songs.get(position);
        //get title and artist strings
        songView.setText(currSong.getTitle());
        duration.setText(convertDuration(currSong.getDuration()));
        //set position as tag
        songLayout.setTag(position);
        return songLayout;
    }

    private String convertDuration(long timeSec) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeSec),
                TimeUnit.MILLISECONDS.toSeconds(timeSec) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeSec))
        );
    }
}
