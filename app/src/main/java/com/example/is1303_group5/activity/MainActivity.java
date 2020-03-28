package com.example.is1303_group5.activity;

import androidx.annotation.IdRes;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
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
import java.util.Random;
import java.util.logging.Logger;

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
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private TextView timePlay;

    private String[] listPermission = new String[]{
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WAKE_LOCK,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

    ImageButton playByNotification;
    TextView nameSongNotification;

    private NotificationChannel mChannel;
    String CHANNEL_ID = "IS1303_GROUP5";
    RemoteViews remoteViews;
    Notification notification;
    NotificationManager mNotificationManager;
    MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectView();
        initVariable();
        if (checkPermission()) {
            getSongListOnDevice();
        } else {
            showConfirmDialog(this, listPermission);
        }

        registerReceiver(receiveData, new IntentFilter("musicRequest"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CreateNotification.CHANNEL_ID, "Music Player", NotificationManager.IMPORTANCE_LOW);
            remoteViews = new RemoteViews(getPackageName(), R.layout.notify);

            Notification.MediaStyle style = new Notification.MediaStyle();
            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setCustomContentView(remoteViews)
                    .setCustomBigContentView(remoteViews)
                    .setStyle(style)
                    .setChannelId(CHANNEL_ID)
                    .setOngoing(true);

            notification = builder.build();
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(mChannel);
            showNoti();
        }
    }

    private void showNoti() {
        remoteViews.setImageViewResource(R.id.btnPlayPauseNoti, R.mipmap.play_foreground);
        remoteViews.setTextViewText(R.id.nameSongNotiTv, songListInDevice.size() == 0 ? "No Songs" : songListDisplay.get(0).getTitle());
        remoteViews.setOnClickPendingIntent(R.id.btnPlayPauseNoti, onNotiPauseClick(R.id.btnPlayPauseNoti));
        remoteViews.setOnClickPendingIntent(R.id.btnPreviousNoti, onPreviousClick(R.id.btnPreviousNoti));
        remoteViews.setOnClickPendingIntent(R.id.btnNextNoti, onNextClick(R.id.btnNextNoti));
        mNotificationManager.notify(1, notification);
    }

    private PendingIntent onNotiPauseClick(@IdRes int id) {
        Intent intent = new Intent("musicRequest");
        intent.putExtra("pause", "request");
        return PendingIntent.getBroadcast(this, id, intent, 0);
    }

    private PendingIntent onNextClick(@IdRes int id) {
        Intent intent = new Intent("musicRequest");
        intent.putExtra("next", "request");
        return PendingIntent.getBroadcast(this, id, intent, 0);
    }

    private PendingIntent onPreviousClick(@IdRes int id) {
        Intent intent = new Intent("musicRequest");
        intent.putExtra("previous", "request");
        return PendingIntent.getBroadcast(this, id, intent, 0);
    }

    private BroadcastReceiver receiveData = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String name = bundle.getString("name");
                if (name != null) {
                    nameSongNotification.setText(name);
                    remoteViews.setTextViewText(R.id.nameSongNotiTv, name);
                }
                String pause = bundle.getString("pause");
                if (pause != null && pause.equals("request")) {
                    Log.e("haha","hihi" );
                    try {
                        onPlayAndPause(null);


                    } catch (IOException e) {
                        e.printStackTrace();;
                    }
                }
                String next = bundle.getString("next");
                if (next != null && next.equals("request")) {
                    Log.e("haha","hihi" );
                    try {
                        doPlayNext(null);
                        remoteViews.setTextViewText(R.id.nameSongNotiTv, songListDisplay.get(indexSong).getTitle());
                    } catch (Exception e) {
                        e.printStackTrace();;
                    }
                }
                String previous = bundle.getString("previous");
                if (previous != null && previous.equals("request")) {
                    Log.e("haha","hihi" );
                    try {
                        doPlayPrevious(null);
                        remoteViews.setTextViewText(R.id.nameSongNotiTv, songListDisplay.get(indexSong).getTitle());
                    } catch (Exception e) {
                        e.printStackTrace();;
                    }
                }
            }
        }
    };

    private void initVariable() {
        songListDisplay = new ArrayList<>();
        songListInDevice = new ArrayList<>();
        searchInput.clearFocus();
        SongAdapter songAdt = new SongAdapter(this, songListDisplay);
        songView.setAdapter(songAdt);
    }

    //check permission when start app
    private boolean checkPermission() {

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
//        if (!isHaveEnoughPermission) {
//            showConfirmDialog(this, listPermission);
//        }
        return isHaveEnoughPermission;
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
        Log.e("haha", "hád");
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
                while(!checkPermission());
                getSongListOnDevice();
                updateList();
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
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        playByNotification = findViewById(R.id.btnPlayPauseNoti);
        nameSongNotification = findViewById(R.id.nameSongNotiTv);
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
        String selection = MediaStore.Audio.Media.IS_MUSIC;
        Cursor musicCursor = musicResolver.query(musicUri, null, selection, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int DATA = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                int duration = musicCursor.getInt(durationColumn);
                String path = musicCursor.getString(DATA);
                File file = new File(path);
                if (file.exists()) {
                    songListInDevice.add(new Song(thisId, thisTitle, duration, path));
                    songListDisplay.add(new Song(thisId, thisTitle, duration, path));
                }
            }
            while (musicCursor.moveToNext());
        }
        musicCursor.close();

    }

    public void search(View view) {
        indexSong = 0;
        songListDisplay.clear();
        if (!textSearch.equals("")) {
            for (Song s : songListInDevice) {
                if (containsIgnoreCase(s.getTitle(), textSearch)) {
                    songListDisplay.add(s);
                }
            }
        }
        SongAdapter songAdt = new SongAdapter(this, songListDisplay);
        if (songAdt.getCount() == 0) {
            songAdt = new SongAdapter(this, songListInDevice);
        }
        songView.setAdapter(songAdt);
    }

    public void uploadList(View view) {
        updateList();
    }

    void updateList() {
        SongAdapter songAdt = new SongAdapter(this, songListInDevice);
        songView.setAdapter(songAdt);
    }

    int indexSong = 0;

    public void onClickItem(View view) throws IOException {
        stop();
        indexSong = Integer.parseInt(view.getTag().toString());
        Log.e("haha", indexSong + "");
        play(indexSong);
        mNotificationManager.notify(1, notification);
    }

    public void onPlayAndPause(View view) throws IOException {
        if (mediaPlayer != null) {
            stop();
        } else {
            play(indexSong);
        }


    }

    Boolean shuffle = false;
    Boolean loop = false;
    private Random rand = new Random();

    public void doPlayNext() throws Exception {
        stop();
            if(loop){
                play(indexSong);
            }else {
                if (!shuffle) {
                    indexSong++;
                    if (indexSong > songListDisplay.size() - 1) {
                        indexSong = 0;
                    }
                    play(indexSong);
                } else {
                    int next = 0;
                    do {
                        next = rand.nextInt(songListDisplay.size() );
                    } while (next == indexSong);
                    indexSong = next;
                    play(indexSong);
                }
            }


    }
    public void doPlayNext(View view) throws Exception {
        stop();

        if(loop){
            play(indexSong);
        }else {
            if (!shuffle) {
                indexSong++;
                if (indexSong > songListDisplay.size() - 1) {
                    indexSong = 0;
                }
                play(indexSong);
            } else {
                int next = 0;
                do {
                    next = rand.nextInt(songListDisplay.size() );
                } while (next == indexSong);
                indexSong = next;
                play(indexSong);
            }
        }

    }

    public void doPlayPrevious(View view) throws Exception {
        stop();
        indexSong--;
        if (indexSong < 0) {
            indexSong = songListDisplay.size() - 1;
        }
        play(indexSong);
    }

    public void doMix(View view) throws Exception {
        shuffle = !shuffle;
        if(shuffle){
            btnMix.setAlpha(1f);
        }else {
            btnMix.setAlpha(0.3f);
        }
    }

    public void doLoop(View view) throws Exception {
        loop = !loop;
        if(loop){
            btnLoop.setAlpha(1f);
        }else {
            btnLoop.setAlpha(0.3f);
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

            remoteViews.setTextViewText(R.id.nameSongNotiTv, songListDisplay.get(indexSong).getTitle());
            remoteViews.setImageViewResource(R.id.btnPlayPauseNoti, R.mipmap.pause_foreground);
            mNotificationManager.notify(1, notification);
        } catch (Exception e) {
            Log.e("play", e.getMessage());
        }
    }


    public void stop() {
        if (mediaPlayer != null) {
            btnPlayPause.setBackgroundResource(R.mipmap.play_foreground);
            remoteViews.setImageViewResource(R.id.btnPlayPauseNoti, R.mipmap.play_foreground);
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;

            mNotificationManager.notify(1, notification);
        }
    }

    final Handler handler = new Handler();
    Runnable runnable;

    public void updateTimeSong() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            try {
                                doPlayNext();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
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
        if (file.exists()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Remove!");
            builder.setMessage("Bạn muốn xóa bài " + file.getName() + "?");
            builder.setCancelable(true);
            builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (file.delete()) {
                        Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();
                        songListInDevice.clear();
                        getSongListOnDevice();
                        updateList();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }


    }


}