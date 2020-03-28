package com.example.is1303_group5.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.is1303_group5.R;
import com.example.is1303_group5.activity.adapter.SongAdapter;
import com.example.is1303_group5.activity.model.Song;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Song> songListInDevice;
    private ArrayList<Song> songListDisplay;
    private ListView songView;
    private EditText searchInput;
    private ImageButton playPause;
    private SeekBar timeLine;
    private TextView seekbarHint;
    private boolean isLoop = true;
    private boolean isMix = false;
    private ImageButton btnLoop;
    private ImageButton btnMix;
    private TextView songName;
    private TextView timeEnd;
    private ImageButton btnPlayPause;
    private TextView timePlay;

    private NotificationChannel mChannel;
    String CHANNEL_ID = "my_channel_01";
    RemoteViews remoteViews;
    Notification notification;
    NotificationManager mNotificationManager;
    MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        connectView();
        initVariable();
        getSongListOnDevice();
        updateList();
    }

    private void initVariable() {
        songListInDevice = new ArrayList<>();
        songListDisplay = new ArrayList<>();
        searchInput.clearFocus();
        SongAdapter songAdt = new SongAdapter(this, songListDisplay);
        songView.setAdapter(songAdt);
    }

    //check permission when start app
    private void checkPermission() {
        String[] listPermission = new String[]{
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WAKE_LOCK,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        boolean isHaveEnoughPermission = true;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            isHaveEnoughPermission = false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            isHaveEnoughPermission = false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            isHaveEnoughPermission = false;
        }
        if (!isHaveEnoughPermission) {
            showConfirmDialog(this, listPermission);
        }
        Log.e("haha",isHaveEnoughPermission+"");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Không đủ quyền truy cập, ứng dụng tự động thoát", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }


    public void showConfirmDialog(final Activity activity, final String[] listPermission) {
        Log.e("haha","hád");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận");
        builder.setMessage("Ứng dụng cần một số quyền để có thể tiếp tục, bạn có muốn tiếp tục?");
        builder.setCancelable(false);
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Toast.makeText(activity, "Không đủ quyền truy cập, ứng dụng tự động thoát", Toast.LENGTH_LONG).show();
                finish();
            }
        });
        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                ActivityCompat.requestPermissions(activity, listPermission, 1);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public String textSearch;

    private void connectView() {
        songView = findViewById(R.id.listSong);
        searchInput = findViewById(R.id.inputSearch);
        playPause = findViewById(R.id.btnPlayPause);
        timeLine = findViewById(R.id.timeLine);
        seekbarHint = findViewById(R.id.timePlay);
        btnLoop = findViewById(R.id.btnLoop);
        btnMix = findViewById(R.id.btnMix);
        songName = findViewById(R.id.songName);
        timeEnd = findViewById(R.id.timeEnd);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        timePlay = findViewById(R.id.timePlay);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                textSearch = editable.toString();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                textSearch = searchInput.getText().toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textSearch = searchInput.getText().toString();
            }
        });
        this.timeLine.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // Khi giá trị progress thay đổi.
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
            }

            // Khi người dùng bắt đầu cử chỉ kéo thanh gạt.
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            // Khi người dùng kết thúc cử chỉ kéo thanh gạt.
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int duration = seekBar.getProgress();
                setTimePlay(duration);
            }
        });
    }

    private boolean containsIgnoreCase(String str, String subString) {
        return str.toLowerCase().contains(subString.toLowerCase());
    }

    public void getSongListOnDevice() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int DATA = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                int duration = musicCursor.getInt(durationColumn);
                String path = musicCursor.getString(DATA);
                songListInDevice.add(new Song(thisId, thisTitle, duration, path));
                songListDisplay.add(new Song(thisId, thisTitle, duration, path));
            }
            while (musicCursor.moveToNext());
        }
        musicCursor.close();
    }

    public void search(View view) {
        songListDisplay.clear();
        for (Song s : songListInDevice) {
            if (containsIgnoreCase(s.getTitle(), textSearch)) {
                songListDisplay.add(s);
            }
        }
        SongAdapter songAdt = new SongAdapter(this, songListDisplay);


        if (songAdt.getCount() == 0) {
            songAdt = new SongAdapter(this, songListInDevice);
        }
        songView.setAdapter(songAdt);
    }

    void updateList() {
        Log.e("haha", "load");
        songListDisplay.clear();
        songListDisplay = songListInDevice;
        SongAdapter songAdt = new SongAdapter(this, songListDisplay);
        songView.setAdapter(songAdt);
    }

    int indexSong = 0;

    public void onClickItem(View view) throws IOException {
        stop();
        indexSong = Integer.parseInt(view.getTag().toString());
        Log.e("haha", indexSong + "");
        play(indexSong);
    }

    public void onPlayAndPause(View view) throws IOException {
        if (mediaPlayer != null) {
            stop();
        } else {
            play(indexSong);
        }

    }


    String formatTime(int durationInMillis) {
        int second = (durationInMillis / 1000) % 60;
        int minute = (durationInMillis / (1000 * 60)) % 60;
        int hour = (durationInMillis / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    public void play(int index) {
        try {
            mediaPlayer = new MediaPlayer();
            String filePath = songListDisplay.get(index).getPath();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            songName.setText(songListDisplay.get(index).getTitle());
            timeEnd.setText(formatTime(songListDisplay.get(index).getDuration()));
            btnPlayPause.setBackgroundResource(R.mipmap.pause_foreground);
            updateTimeSong();
        } catch (Exception e) {
            Log.e("play", e.getMessage());
        }
    }


    public void stop() {
        if (mediaPlayer != null) {
            btnPlayPause.setBackgroundResource(R.mipmap.play_foreground);
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    final Handler handler = new Handler();
    Runnable runnable;

    public void updateTimeSong() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    String time = formatTime(mediaPlayer.getCurrentPosition());
                    timePlay.setText(time);
                    timeLine.setMax(songListDisplay.get(indexSong).getDuration());
                    timeLine.setProgress(mediaPlayer.getCurrentPosition());
                    handler.postDelayed(this, 100);
                }
            }
        };
        handler.postDelayed(runnable, 100);
    }

    public void setTimePlay(int duration) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(duration);
        }
    }

    public void removeSong(View view) {
        final File file = new File(songListDisplay.get(indexSong).getPath());
//        if (file.exists()) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle("Remove!");
//            builder.setMessage("Bạn muốn xóa bài " + file.getName() + "?");
//            builder.setCancelable(true);
//            builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    if (file.delete()) {
//                        Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.cancel();
//                }
//            });
//            builder.show();
//        }
        getSongListOnDevice();
        updateList();

    }


}