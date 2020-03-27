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
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
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
import com.example.is1303_group5.activity.service.SoundService;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Song> songListInDevice;
    private ArrayList<Song> songListDisplay;
    private ListView songView;
    private EditText searchInput;
    private ImageButton playPause;
    private SoundService soundService;
    private Intent playIntent;
    private boolean musicBound = false;
    private SeekBar timeLine;
    private TextView seekbarHint;
    private int totalDuration = 0;
    private boolean isLoop = true;
    private boolean isMix = false;
    private ImageButton btnLoop;
    private ImageButton btnMix;
    private TextView songName;
    private NotificationChannel mChannel;
    String CHANNEL_ID = "my_channel_01";
    RemoteViews remoteViews;
    Notification notification;
    NotificationManager mNotificationManager;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();

        connectView();
        searchInput.clearFocus();
        initVariable();
        getSongListOnDevice();

//      registerReceiver(receiveData, new IntentFilter("musicRequest"));
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
//        else {
//            nextActivity();
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Không đủ quyền truy cập, ứng dụng tự động thoát", Toast.LENGTH_LONG).show();
                    finish();
                }
//                else {
//                    nextActivity();
//                }
            }
        }
    }

    // go to main activity if have enough permission
    public void nextActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }

    public void showConfirmDialog(final Activity activity, final String[] listPermission) {
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

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, SoundService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SoundService.MusicBinder binder = (SoundService.MusicBinder) service;
            soundService = binder.getService();
            soundService.setList(songListInDevice);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    private void connectView() {
        songView = findViewById(R.id.listSong);
        searchInput = findViewById(R.id.inputSearch);
        searchInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable editable) {
                String strSearch = editable.toString();
                songListDisplay.clear();
                for (Song s : songListInDevice) {
                    if (containsIgnoreCase(s.getTitle(), strSearch)) {
                        songListDisplay.add(s);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                String strSearch = searchInput.getText().toString();
                songListDisplay.clear();
                for (Song song : songListInDevice) {
                    if (containsIgnoreCase(song.getTitle(), strSearch)) {
                        songListDisplay.add(song);
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String strSearch = searchInput.getText().toString();
                songListDisplay.clear();
                for (Song song : songListInDevice) {
                    if (containsIgnoreCase(song.getTitle(), strSearch)) {
                        songListDisplay.add(song);
                    }
                }
            }
        });
        playPause = findViewById(R.id.btnPlayPause);
        timeLine = findViewById(R.id.timeLine);
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
                soundService.setDurationSong(duration);
            }
        });

        seekbarHint = findViewById(R.id.timePlay);
        btnLoop = findViewById(R.id.btnLoop);
        btnMix = findViewById(R.id.btnMix);
        songName = findViewById(R.id.songName);
    }

    private boolean containsIgnoreCase(String str, String subString) {
        return str.toLowerCase().contains(subString.toLowerCase());
    }

    public void getSongListOnDevice() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        Log.e("haha", musicUri.toString());
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                long duration = musicCursor.getLong(durationColumn);
                String path = musicCursor.getString(pathColumn);
                songListInDevice.add(new Song(thisId, thisTitle, thisArtist, duration, path));
                songListDisplay.add(new Song(thisId, thisTitle, thisArtist, duration, path));
            }
            while (musicCursor.moveToNext());
        }
        musicCursor.close();
    }

    public void search(View view) {
        searchInput.clearFocus();
        String strSearch = searchInput.getText().toString();
        songListDisplay.clear();
        for (Song s : songListInDevice) {
            if (containsIgnoreCase(s.getTitle(), strSearch)) {
                songListDisplay.add(s);
            }
        }
        SongAdapter songAdt = new SongAdapter(this, songListDisplay);
        songView.setAdapter(songAdt);
    }

    private void initVariable() {
        songListInDevice = new ArrayList<>();
        songListDisplay = new ArrayList<>();
        SongAdapter songAdt = new SongAdapter(this, songListDisplay);
        songView.setAdapter(songAdt);
    }
}