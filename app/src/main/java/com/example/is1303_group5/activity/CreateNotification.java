package com.example.is1303_group5.activity;

import android.app.Notification;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.renderscript.RenderScript;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.is1303_group5.R;
import com.example.is1303_group5.activity.model.Song;

public class CreateNotification {

    public static final String CHANNEL_ID = "IS1303_GROUP5";
    public static final String ACTIONPREVIOUS = "action_previous";
    public static final String CHANNEL_PLAY = "channel_play";
    public static final String CHANNEL_NEXT = "channel_next";

    public static Notification notification;

    public static void createNotification(Context context, Song song, int playbutton, int pos, int size) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "tag");

            notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(song.getTitle())
                    .setContentText("")
                    .setOnlyAlertOnce(true)
                    .setShowWhen(false)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();

            notificationManagerCompat.notify(1, notification);
        }
    }

}
